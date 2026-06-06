package matteroverdrive.compat.modules.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.annotations.ZenRegister;
import matteroverdrive.MatterOverdrive;
import matteroverdrive.data.matter.ItemHandler;
import net.minecraft.item.Item;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * CraftTweaker ZenClass for Matter Overdrive's matter value table and matter
 * mod blacklist.
 *
 * <pre>{@code
 * import mods.matteroverdrive.matter;
 *
 * matter.add("minecraft:gold_ingot", 256);
 * matter.addOre("oreGold", 256);
 * matter.remove("minecraft:apple");
 * matter.removeOre("oreCopper");
 * matter.blacklistMod("thaumcraft");
 * }</pre>
 */
@ZenRegister
@ZenClass("mods.matteroverdrive.matter")
public class CTMatterRegistry {

    @ZenMethod
    public static void add(String itemId, int matter) {
        CraftTweakerAPI.apply(new IAction() {
            @Override
            public void apply() {
                Item item = Item.getByNameOrId(itemId);
                if (item == null) {
                    CraftTweakerAPI.logError("[MatterOverdrive] matter.add: unknown item '" + itemId + "'");
                    return;
                }
                MatterOverdrive.MATTER_REGISTRY.replace(item, new ItemHandler(matter));
            }

            @Override
            public String describe() {
                return "[MatterOverdrive] Adding matter value " + matter + " for " + itemId;
            }
        });
    }

    @ZenMethod
    public static void addOre(String ore, int matter) {
        CraftTweakerAPI.apply(new IAction() {
            @Override
            public void apply() {
                MatterOverdrive.MATTER_REGISTRY.replaceOre(ore, new ItemHandler(matter));
            }

            @Override
            public String describe() {
                return "[MatterOverdrive] Adding matter value " + matter + " for ore '" + ore + "'";
            }
        });
    }

    @ZenMethod
    public static void remove(String itemId) {
        CraftTweakerAPI.apply(new IAction() {
            @Override
            public void apply() {
                Item item = Item.getByNameOrId(itemId);
                if (item == null) {
                    CraftTweakerAPI.logError("[MatterOverdrive] matter.remove: unknown item '" + itemId + "'");
                    return;
                }
                MatterOverdrive.MATTER_REGISTRY.unregister(item);
            }

            @Override
            public String describe() {
                return "[MatterOverdrive] Removing matter value for " + itemId;
            }
        });
    }

    @ZenMethod
    public static void removeOre(String ore) {
        CraftTweakerAPI.apply(new IAction() {
            @Override
            public void apply() {
                MatterOverdrive.MATTER_REGISTRY.unregisterOre(ore);
            }

            @Override
            public String describe() {
                return "[MatterOverdrive] Removing matter value for ore '" + ore + "'";
            }
        });
    }

    @ZenMethod
    public static void blacklistMod(String modId) {
        CraftTweakerAPI.apply(new IAction() {
            @Override
            public void apply() {
                MatterOverdrive.MATTER_REGISTRY.getModBlacklist().add(modId);
            }

            @Override
            public String describe() {
                return "[MatterOverdrive] Blacklisting mod '" + modId + "' from matter auto-calculation";
            }
        });
    }
}
