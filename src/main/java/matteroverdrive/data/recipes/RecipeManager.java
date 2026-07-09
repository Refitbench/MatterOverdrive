
package matteroverdrive.data.recipes;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author shadowfacts
 */
public class RecipeManager<M, R extends Recipe<M>> {

	protected final Class<R> recipeClass;
	protected final List<R> recipes = new ArrayList<>();

	public RecipeManager(Class<R> recipeClass) {
		this.recipeClass = recipeClass;
	}

	public void register(R recipe) {
		recipes.add(recipe);
	}

	/**
	 * Removes the exact recipe instance from this manager. Returns {@code true}
	 * if the recipe was present.
	 */
	public boolean remove(R recipe) {
		return recipes.remove(recipe);
	}

	/**
	 * Returns the live, mutable list. Intended for compatibility layers that need to
	 * add/remove recipes and snapshot state for reload. External callers should prefer
	 * {@link #getRecipes()} for read-only access.
	 */
	public List<R> getRecipesMutable() {
		return recipes;
	}

	public Optional<R> get(M machine) {
		return recipes.stream().filter(r -> r.matches(machine)).findFirst();
	}

	public boolean isInput(ItemStack stack) {
		return recipes.stream().flatMap(r -> r.getInputs().stream())
				.anyMatch(s -> s.getItem() == stack.getItem() && s.getItemDamage() == stack.getItemDamage());
	}

	public List<R> getRecipes() {
		return ImmutableList.copyOf(recipes);
	}

}
