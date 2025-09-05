package net.supergamer.growitems.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.*;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.util.collection.DefaultedList;
import net.supergamer.growitems.GrowItems;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemGrowerBlockGrowthTimings {
    // the final map of item to ticks
    public static final Map<Item, Integer> ITEM_TO_TICKS = new HashMap<>();


    // Growth timings in ticks
    private static final int MIN_TICKS = 60;        // 3s
    private static final int MAX_TICKS = 12000;     // 10m
    private static final int DEFAULT_TICKS = 240;   // 12s

    public static float GLOBAL_SCALE = 10.0f; // Global scale for all item grower blocks so can be adjusted according to the difficulty

    private static ServerRecipeManager RECIPES;
    private static RegistryWrapper.WrapperLookup LOOKUP;

    public static void init(ServerRecipeManager recipes, RegistryWrapper.WrapperLookup lookup) {
        RECIPES = recipes;
        LOOKUP = lookup;
    }

    public static void computePut(Item item) {
        if (ITEM_TO_TICKS.containsKey(item)) return;
        int ticks = computeTicks(item);
        if (ticks <= 0) ticks = DEFAULT_TICKS;
        ITEM_TO_TICKS.put(item, Math.round(ticks * GLOBAL_SCALE));
        GrowItems.LOGGER.info("Computed growth time for " + item + " is " + ticks + " ticks");
    }

    public static void computeAll() {
        for (Item item : Registries.ITEM) {
            computePut(item);
        }
    }

    private static int computeTicks(Item item) {
        ItemStack stack = new ItemStack(item);

        // recipe based
        int viaRecipe = recipeCost(item);
        if (viaRecipe >= 0) return clamp(viaRecipe);

        // food based
        FoodComponent foodComponent = stack.get(DataComponentTypes.FOOD);
        if (foodComponent != null) {
            int nutrition = foodComponent.nutrition();
            float saturation = foodComponent.saturation();
            return clamp(80 + nutrition * 20 + (int) (saturation * 80)); // clamp to min and max ticks and compute using nutrition and saturation
        }

        // broad classes
        if (item.equals(Items.ELYTRA)) return clamp(36000); // elytra 3hrs
        if (stack.isIn(ItemTags.CHEST_ARMOR) || stack.isIn(ItemTags.HEAD_ARMOR) || stack.isIn(ItemTags.LEG_ARMOR) || stack.isIn(ItemTags.FOOT_ARMOR))
            return clamp(1200); // armor 1min should not get here because armor is crafted
        if (stack.get(DataComponentTypes.TOOL) != null) return clamp(1200); // tools 1 min

        if (item instanceof BlockItem bi) {
            Block b = bi.getBlock();
            BlockState st = b.getDefaultState();
            if (st.isIn(BlockTags.DIAMOND_ORES)) return clamp(9600);
            if (st.isIn(BlockTags.EMERALD_ORES)) return clamp(3600);
            if (st.isIn(BlockTags.GOLD_ORES)) return clamp(4500);
            if (st.isIn(BlockTags.IRON_ORES)) return clamp(3600);
            if (st.isIn(BlockTags.COAL_ORES)) return clamp(1200);
            if (st.isIn(BlockTags.REDSTONE_ORES)) return clamp(1200);
            if (st.isIn(BlockTags.LOGS)) return clamp(420);
            if (st.isIn(BlockTags.PLANKS)) return clamp(160);
            if (st.isIn(BlockTags.LEAVES)) return clamp(120);
            if (st.isIn(BlockTags.SAPLINGS)) return clamp(300);
        }

        // default
        return clamp(DEFAULT_TICKS);
    }


    private static int recipeCost(Item item) {
        if (RECIPES == null || LOOKUP == null) return -1;

        int bestCost = Integer.MAX_VALUE;
        for (RecipeEntry<?> entry : RECIPES.values()) {
            Recipe<?> recipe = entry.value();
            RegistryKey<Recipe<?>> id = entry.id();

            ItemStack out = getRecipeOutput(recipe);

            if (out.isEmpty() || out.getItem() != item) continue;

            int cost = switch (recipe) {
                case CraftingRecipe cr -> ingredientCost(cr.getIngredientPlacement().getIngredients());
                case AbstractCookingRecipe cook ->
                    // input(1) + cook time with a sensible floor
                        1 + Math.max(100, cook.getCookingTime());
                case StonecuttingRecipe stonecuttingRecipe -> 40;
                case SmithingTransformRecipe smithingTransformRecipe -> 220; // upgrade step

                case SmithingTrimRecipe smithingTrimRecipe -> 140; // cosmetic, cheaper

                default -> 120; // generic fallback for other types

            };

            if (cost < bestCost) bestCost = cost;
        }

        if (bestCost == Integer.MAX_VALUE) return -1;

        int ticks = 60 + Math.round(bestCost * 1.6f);
        return clamp(ticks);
    }


    private static ItemStack getRecipeOutput(Recipe<?> recipe) {
        // Prefer crafting the output for crafting recipes to avoid mapping differences
        if (recipe instanceof CraftingRecipe cr) {
            CraftingRecipeInput input = buildEmptyCraftingInput(cr);
            if (input != null) {
                try {
                    return cr.craft(input, LOOKUP);
                } catch (Throwable ignored) {
                    // fall through to reflection-based preview methods
                }
            }
        }

        // As a last resort, return empty. We only need the preview to filter matching recipes.
        return ItemStack.EMPTY;
    }

    private static CraftingRecipeInput buildEmptyCraftingInput(CraftingRecipe recipe) {
        int width = 3;
        int height = 3;
        try {
            // Prefer exact dimensions if available (ShapedRecipe)
            if (recipe instanceof ShapedRecipe sr) {
                width = sr.getWidth();
                height = sr.getHeight();
            } else {
                // For shapeless, default to 3x3 grid
                width = 3;
                height = 3;
            }

            DefaultedList<ItemStack> grid = DefaultedList.ofSize(width * height, ItemStack.EMPTY);

            // Try static factory: CraftingRecipeInput.create(int, int, DefaultedList<ItemStack>)
            return CraftingRecipeInput.create(width, height, grid);
        } catch (Throwable t) {
            return null;
        }
    }

    private static int ingredientCost(List<Ingredient> ings) {
        int c = 0;
        for (Ingredient ing : ings) if (!ing.isEmpty()) c++;
        return Math.max(1, c);
    }

    private static int clamp(int v) {
        return Math.max(MIN_TICKS, Math.min(MAX_TICKS, v));
    }
}
