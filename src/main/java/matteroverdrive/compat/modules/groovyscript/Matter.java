package matteroverdrive.compat.modules.groovyscript;

import java.util.ArrayDeque;
import java.util.Deque;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.documentation.annotations.Example;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.Reference;
import matteroverdrive.data.matter.IMatterEntryHandler;
import matteroverdrive.data.matter.ItemHandler;
import matteroverdrive.data.matter.MatterEntryItem;
import matteroverdrive.data.matter.MatterEntryOre;
import matteroverdrive.handler.MatterRegistry;
import net.minecraft.item.Item;

/**
 * GroovyScript registry for Matter Overdrive's matter value table and matter
 * mod blacklist. Each scripted mutation is recorded as an undo action and
 * replayed in reverse on {@code /groovyscript reload}.
 */
@RegistryDescription(linkGenerator = Reference.MOD_ID, category = RegistryDescription.Category.ENTRIES)
public class Matter extends VirtualizedRegistry<Runnable> {

    private final Deque<Runnable> undoStack = new ArrayDeque<>();

    @Override
    public void onReload() {
        while (!undoStack.isEmpty()) {
            try {
                undoStack.pop().run();
            } catch (Exception e) {
                GroovyLog.get().error("Failed to undo a Matter Overdrive matter registry change: {}", e.getMessage());
            }
        }
    }

    private MatterRegistry registry() {
        return MatterOverdrive.MATTER_REGISTRY;
    }

    // ---- add / replace ----

    @MethodDescription(type = MethodDescription.Type.ADDITION, example = @Example("item('minecraft:gold_ingot').getItem(), 256"))
    public void add(Item item, int matter) {
        add(item, new ItemHandler(matter));
    }

    @MethodDescription(type = MethodDescription.Type.ADDITION)
    public void add(Item item, IMatterEntryHandler handler) {
        MatterEntryItem previous = registry().replace(item, handler);
        undoStack.push(() -> registry().restoreItemEntry(item, previous));
    }

    @MethodDescription(type = MethodDescription.Type.ADDITION, example = @Example("'oreGold', 256"))
    public void addOre(String ore, int matter) {
        addOre(ore, new ItemHandler(matter));
    }

    @MethodDescription(type = MethodDescription.Type.ADDITION)
    public void addOre(String ore, IMatterEntryHandler handler) {
        MatterEntryOre previous = registry().replaceOre(ore, handler);
        undoStack.push(() -> registry().restoreOreEntry(ore, previous));
    }

    // ---- remove ----

    @MethodDescription(example = @Example("item('minecraft:apple').getItem()"))
    public void remove(Item item) {
        MatterEntryItem previous = registry().unregister(item);
        if (previous != null) {
            undoStack.push(() -> registry().restoreItemEntry(item, previous));
        }
    }

    @MethodDescription(example = @Example("'oreCopper'"))
    public void removeOre(String ore) {
        MatterEntryOre previous = registry().unregisterOre(ore);
        if (previous != null) {
            undoStack.push(() -> registry().restoreOreEntry(ore, previous));
        }
    }

    // ---- mod blacklist (matter calculation blacklist) ----

    @MethodDescription(type = MethodDescription.Type.REMOVAL, example = @Example("'thaumcraft'"))
    public void blacklistMod(String modId) {
        if (registry().getModBlacklist().add(modId)) {
            undoStack.push(() -> registry().removeFromBlacklist(modId));
        }
    }

}
