
package matteroverdrive.data.recipes;

import java.util.List;

import com.google.common.collect.ImmutableList;

import matteroverdrive.tile.TileEntityInscriber;
import net.minecraft.item.ItemStack;

/**
 * @author shadowfacts
 */
public class InscriberRecipe extends Recipe<TileEntityInscriber> {

	private ItemStack main;
	private ItemStack sec;
	private ItemStack output;
	private int energy;
	private int time;

	public InscriberRecipe(ItemStack main, ItemStack sec, ItemStack output, int energy, int time) {
		this.main = main;
		this.sec = sec;
		this.output = output;
		this.energy = energy;
		this.time = time;
	}

	public ItemStack getMain() {
		return main;
	}

	public ItemStack getSec() {
		return sec;
	}

	public ItemStack getOutput() {
		return output;
	}

	public int getEnergy() {
		return energy;
	}

	public int getTime() {
		return time;
	}

	@Override
	public boolean matches(TileEntityInscriber machine) {
		ItemStack primary = machine.getStackInSlot(TileEntityInscriber.MAIN_INPUT_SLOT_ID);
		ItemStack secondary = machine.getStackInSlot(TileEntityInscriber.SEC_INPUT_SLOT_ID);
		return ItemStack.areItemsEqual(primary, this.main) && ItemStack.areItemsEqual(secondary, this.sec);
	}

	public ItemStack getOutput(TileEntityInscriber machine) {
		return output.copy();
	}

	@Override
	public List<ItemStack> getInputs() {
		return ImmutableList.of(main, sec);
	}

}
