package matteroverdrive.compat.modules.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import matteroverdrive.MatterOverdrive;
import matteroverdrive.api.android.IBioticStat;
import matteroverdrive.data.biostats.AbstractBioticStat;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * CraftTweaker ZenClass for tweaking Android biotic stats.
 *
 * <pre>{@code
 * import mods.matteroverdrive.android;
 *
 * android.setXp("shield", 25);
 * android.disable("cloak");
 * android.enable("floatation");
 * android.unregister("tan_temperature");
 * android.addRequiredItem("shield", <minecraft:iron_ingot> * 4);
 * android.clearRequiredItems("shield");
 * }</pre>
 *
 * <p>Use the in-game command {@code /android list} to print all registered
 * biotic stat names to chat. Stat names match their unlocalized names as
 * shown in the Android Station UI.</p>
 */
@ZenRegister
@ZenClass("mods.matteroverdrive.android")
public class CTAndroidStats {

    private static AbstractBioticStat resolve(String statName) {
        IBioticStat stat = MatterOverdrive.STAT_REGISTRY.getStat(statName);
        if (stat == null) {
            CraftTweakerAPI.logError("[MatterOverdrive] android: unknown stat '" + statName + "'");
            return null;
        }
        if (!(stat instanceof AbstractBioticStat)) {
            CraftTweakerAPI.logError("[MatterOverdrive] android: stat '" + statName
                    + "' is not an AbstractBioticStat and cannot be mutated");
            return null;
        }
        return (AbstractBioticStat) stat;
    }

    @ZenMethod
    public static void setXp(String statName, int xp) {
        CraftTweakerAPI.apply(new IAction() {
            @Override
            public void apply() {
                AbstractBioticStat stat = resolve(statName);
                if (stat != null) stat.setXp(xp);
            }

            @Override
            public String describe() {
                return "[MatterOverdrive] Setting XP of '" + statName + "' to " + xp;
            }
        });
    }

    private static void setEnabled(String statName, boolean enabled) {
        CraftTweakerAPI.apply(new IAction() {
            @Override
            public void apply() {
                AbstractBioticStat stat = resolve(statName);
                if (stat != null) stat.setEnabledOverride(enabled);
            }

            @Override
            public String describe() {
                return "[MatterOverdrive] " + (enabled ? "Enabling" : "Disabling") + " stat '" + statName + "'";
            }
        });
    }

    @ZenMethod
    public static void enable(String statName) {
        setEnabled(statName, true);
    }

    @ZenMethod
    public static void disable(String statName) {
        setEnabled(statName, false);
    }

    @ZenMethod
    public static void addRequiredItem(String statName, IItemStack item) {
        CraftTweakerAPI.apply(new IAction() {
            @Override
            public void apply() {
                AbstractBioticStat stat = resolve(statName);
                if (stat != null) stat.addReqiredItm((ItemStack) item.getInternal());
            }

            @Override
            public String describe() {
                return "[MatterOverdrive] Adding required item " + item.getDisplayName()
                        + " to stat '" + statName + "'";
            }
        });
    }

    @ZenMethod
    public static void clearRequiredItems(String statName) {
        CraftTweakerAPI.apply(new IAction() {
            @Override
            public void apply() {
                AbstractBioticStat stat = resolve(statName);
                if (stat != null) stat.clearRequiredItems();
            }

            @Override
            public String describe() {
                return "[MatterOverdrive] Clearing required items from stat '" + statName + "'";
            }
        });
    }

    @ZenMethod
    public static void unregister(String statName) {
        CraftTweakerAPI.apply(new IAction() {
            @Override
            public void apply() {
                IBioticStat stat = MatterOverdrive.STAT_REGISTRY.getStat(statName);
                if (stat == null) {
                    CraftTweakerAPI.logWarning(
                            "[MatterOverdrive] android.unregister: stat '" + statName + "' not found");
                    return;
                }
                MatterOverdrive.STAT_REGISTRY.unregisterStat(statName);
            }

            @Override
            public String describe() {
                return "[MatterOverdrive] Unregistering Android stat '" + statName + "'";
            }
        });
    }
}
