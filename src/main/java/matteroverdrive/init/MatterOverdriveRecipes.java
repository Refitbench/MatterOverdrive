
package matteroverdrive.init;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.data.recipes.InscriberRecipe;
import matteroverdrive.data.recipes.InscriberRecipeManager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class MatterOverdriveRecipes {
	public static final InscriberRecipeManager INSCRIBER = new InscriberRecipeManager();

	/**
	 * Registers the built-in Inscriber recipes. Called during preInit so recipes
	 * exist before CraftTweaker and GroovyScript apply their actions.
	 */
	@SuppressWarnings("null")
	public static void registerDefaultInscriberRecipes() {
		// Isolinear Circuit Mk1 + Gold Ingot -> Mk2
		INSCRIBER.register(new InscriberRecipe(
				new ItemStack(MatterOverdrive.ITEMS.isolinear_circuit, 1, 0),
				new ItemStack(Items.GOLD_INGOT),
				new ItemStack(MatterOverdrive.ITEMS.isolinear_circuit, 1, 1),
				64000, 300));
		// Isolinear Circuit Mk2 + Diamond -> Mk3
		INSCRIBER.register(new InscriberRecipe(
				new ItemStack(MatterOverdrive.ITEMS.isolinear_circuit, 1, 1),
				new ItemStack(Items.DIAMOND),
				new ItemStack(MatterOverdrive.ITEMS.isolinear_circuit, 1, 2),
				88000, 600));
		// Isolinear Circuit Mk3 + Emerald -> Mk4
		INSCRIBER.register(new InscriberRecipe(
				new ItemStack(MatterOverdrive.ITEMS.isolinear_circuit, 1, 2),
				new ItemStack(Items.EMERALD),
				new ItemStack(MatterOverdrive.ITEMS.isolinear_circuit, 1, 3),
				114000, 1200));
	}

	@SuppressWarnings("null")
	public static void registerMachineRecipes(FMLInitializationEvent event) {
		// Furnace
		GameRegistry.addSmelting(new ItemStack(MatterOverdrive.ITEMS.tritanium_dust),
				new ItemStack(MatterOverdrive.ITEMS.tritanium_ingot), 5);
		GameRegistry.addSmelting(new ItemStack(MatterOverdrive.BLOCKS.tritaniumOre),
				new ItemStack(MatterOverdrive.ITEMS.tritanium_ingot), 10);
	}
}