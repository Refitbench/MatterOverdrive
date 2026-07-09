
package matteroverdrive.data.recipes;

import java.util.ArrayList;
import java.util.List;

import matteroverdrive.tile.TileEntityInscriber;
import net.minecraft.item.ItemStack;

/**
 * @author shadowfacts
 */
public class InscriberRecipeManager extends RecipeManager<TileEntityInscriber, InscriberRecipe> {

	public InscriberRecipeManager() {
		super(InscriberRecipe.class);
	}

	public boolean isPrimaryInput(ItemStack stack) {
		return recipes.stream().map(InscriberRecipe::getMain).anyMatch(s -> ItemStack.areItemsEqual(s, stack));
	}

	public boolean isSecondaryInput(ItemStack stack) {
		return recipes.stream().map(InscriberRecipe::getSec).anyMatch(s -> ItemStack.areItemsEqual(s, stack));
	}

	/**
	 * Removes any recipes that produce the given output stack. Returns the list of
	 * removed recipes so callers can restore them later.
	 */
	public List<InscriberRecipe> removeByOutput(ItemStack output) {
		List<InscriberRecipe> removed = new ArrayList<>();
		recipes.removeIf(r -> {
			if (ItemStack.areItemsEqual(r.getOutput(), output)) {
				removed.add(r);
				return true;
			}
			return false;
		});
		return removed;
	}

	/**
	 * Removes any recipes that match both the primary and secondary input stacks.
	 * Returns the list of removed recipes so callers can restore them later.
	 */
	public List<InscriberRecipe> removeByInputPair(ItemStack main, ItemStack sec) {
		List<InscriberRecipe> removed = new ArrayList<>();
		recipes.removeIf(r -> {
			if (ItemStack.areItemsEqual(r.getMain(), main) && ItemStack.areItemsEqual(r.getSec(), sec)) {
				removed.add(r);
				return true;
			}
			return false;
		});
		return removed;
	}

}
