package matteroverdrive.compat.modules.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import matteroverdrive.MatterOverdrive;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * CraftTweaker ZenClass for Matter Overdrive's replicator blacklist.
 * Items on this list cannot be replicated even if they have a matter value.
 *
 * <pre>{@code
 * import mods.matteroverdrive.replicator;
 *
 * replicator.add(<matteroverdrive:matter_dust>);
 * replicator.add(<ore:ingotGold>);
 * }</pre>
 */
@ZenRegister
@ZenClass("mods.matteroverdrive.replicator")
public class CTReplicatorBlacklist {

    @ZenMethod
    public static void add(IItemStack stack) {
        CraftTweakerAPI.apply(new IAction() {
            @Override
            public void apply() {
                ItemStack mc = (ItemStack) stack.getInternal();
                if (!mc.isEmpty()) {
                    MatterOverdrive.MATTER_REGISTRY.addToReplicationBlacklist(mc.getItem());
                }
            }

            @Override
            public String describe() {
                return "[MatterOverdrive] Adding " + stack.getDisplayName() + " to replicator blacklist";
            }
        });
    }

    @ZenMethod
    public static void add(IIngredient ingredient) {
        for (IItemStack stack : ingredient.getItems()) {
            add(stack);
        }
    }
}
