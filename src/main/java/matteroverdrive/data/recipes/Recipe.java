
package matteroverdrive.data.recipes;

import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * @author shadowfacts
 */
public abstract class Recipe<M> {

	public abstract boolean matches(M machine);

	public abstract List<ItemStack> getInputs();

	public abstract ItemStack getOutput(M machine);

}
