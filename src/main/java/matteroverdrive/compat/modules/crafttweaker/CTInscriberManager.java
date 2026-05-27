package matteroverdrive.compat.modules.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import matteroverdrive.data.recipes.InscriberRecipe;
import matteroverdrive.init.MatterOverdriveRecipes;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.List;

/**
 * CraftTweaker ZenClass for Matter Overdrive's Inscriber recipes.
 *
 * <pre>{@code
 * import mods.matteroverdrive.inscriber;
 *
 * inscriber.addRecipe(<matteroverdrive:circuit_basic>,
 *     <minecraft:redstone>, <minecraft:gold_ingot>, 1000, 80);
 * inscriber.removeByOutput(<matteroverdrive:circuit_basic>);
 * inscriber.removeByInputs(<minecraft:redstone>, <minecraft:gold_ingot>);
 * inscriber.removeAll();
 * }</pre>
 */
@ZenRegister
@ZenClass("mods.matteroverdrive.inscriber")
public class CTInscriberManager {

    @ZenMethod
    public static void addRecipe(IItemStack output, IItemStack main, IItemStack secondary,
                                  int energy, int time) {
        CraftTweakerAPI.apply(new IAction() {
            @Override
            public void apply() {
                ItemStack mcMain = (ItemStack) main.getInternal();
                ItemStack mcSec  = (ItemStack) secondary.getInternal();
                ItemStack mcOut  = (ItemStack) output.getInternal();
                MatterOverdriveRecipes.INSCRIBER.register(
                        new InscriberRecipe(mcMain, mcSec, mcOut, energy, time));
            }

            @Override
            public String describe() {
                return "[MatterOverdrive] Adding Inscriber recipe -> " + output.getDisplayName();
            }
        });
    }

    @ZenMethod
    public static void removeByOutput(IItemStack output) {
        CraftTweakerAPI.apply(new IAction() {
            @Override
            public void apply() {
                List<InscriberRecipe> removed =
                        MatterOverdriveRecipes.INSCRIBER.removeByOutput((ItemStack) output.getInternal());
                if (removed.isEmpty()) {
                    CraftTweakerAPI.logWarning(
                            "[MatterOverdrive] inscriber.removeByOutput: no recipe found for "
                                    + output.getDisplayName());
                }
            }

            @Override
            public String describe() {
                return "[MatterOverdrive] Removing Inscriber recipes with output " + output.getDisplayName();
            }
        });
    }

    @ZenMethod
    public static void removeByInputs(IItemStack main, IItemStack secondary) {
        CraftTweakerAPI.apply(new IAction() {
            @Override
            public void apply() {
                List<InscriberRecipe> removed = MatterOverdriveRecipes.INSCRIBER.removeByInputPair(
                        (ItemStack) main.getInternal(), (ItemStack) secondary.getInternal());
                if (removed.isEmpty()) {
                    CraftTweakerAPI.logWarning(
                            "[MatterOverdrive] inscriber.removeByInputs: no matching recipe found");
                }
            }

            @Override
            public String describe() {
                return "[MatterOverdrive] Removing Inscriber recipes with inputs "
                        + main.getDisplayName() + " + " + secondary.getDisplayName();
            }
        });
    }

    @ZenMethod
    public static void removeAll() {
        CraftTweakerAPI.apply(new IAction() {
            @Override
            public void apply() {
                MatterOverdriveRecipes.INSCRIBER.getRecipesMutable().clear();
            }

            @Override
            public String describe() {
                return "[MatterOverdrive] Removing all Inscriber recipes";
            }
        });
    }
}
