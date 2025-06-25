package net.mcreator.concoction.recipe.butterChurn;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.block.state.BlockState;

public record ButterChurnRecipeInput(BlockState state, NonNullList<ItemStack> stack, int count) implements RecipeInput {
    public ButterChurnRecipeInput(BlockState state, NonNullList<ItemStack> stack) {
        this(state, stack, stack.getFirst().getCount());
    }

    public ItemStack getItem(int pIndex) {
        return this.stack.get(pIndex);
    }

    public int size() {
        return 1;
    }
}
