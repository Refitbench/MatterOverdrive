package matteroverdrive.compat.modules.groovyscript;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.documentation.annotations.Example;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.api.android.IBioticStat;
import matteroverdrive.data.biostats.AbstractBioticStat;
import net.minecraft.item.ItemStack;

/**
 * GroovyScript registry for tweaking Android biotic stat parameters.
 * Mutations are captured into undo actions so {@code /groovyscript reload}
 * restores the previous values.
 */
@RegistryDescription(category = RegistryDescription.Category.ENTRIES)
public class AndroidCompat extends VirtualizedRegistry<Runnable> {

    private final Deque<Runnable> undoStack = new ArrayDeque<>();

    @Override
    public void onReload() {
        while (!undoStack.isEmpty()) {
            try {
                undoStack.pop().run();
            } catch (Exception e) {
                GroovyLog.get().error("Failed to undo a Matter Overdrive android stat change: {}", e.getMessage());
            }
        }
    }

    private static AbstractBioticStat asAbs(IBioticStat stat) {
        if (!(stat instanceof AbstractBioticStat)) {
            GroovyLog.get().error("Stat {} is not an AbstractBioticStat; cannot mutate via GroovyScript",
                    stat == null ? "null" : stat.getUnlocalizedName());
            return null;
        }
        return (AbstractBioticStat) stat;
    }

    @MethodDescription(type = MethodDescription.Type.VALUE, example = @Example("androidStat('shield'), 25"))
    public void setXp(IBioticStat stat, int xp) {
        AbstractBioticStat a = asAbs(stat);
        if (a == null) return;
        int prev = a.getXp();
        a.setXp(xp);
        undoStack.push(() -> a.setXp(prev));
    }

    private void setEnabled(IBioticStat stat, Boolean enabled) {
        AbstractBioticStat a = asAbs(stat);
        if (a == null) return;
        Boolean prev = a.getEnabledOverride();
        a.setEnabledOverride(enabled);
        undoStack.push(() -> a.setEnabledOverride(prev));
    }

    @MethodDescription(type = MethodDescription.Type.VALUE)
    public void disable(IBioticStat stat) {
        setEnabled(stat, Boolean.FALSE);
    }

    @MethodDescription(type = MethodDescription.Type.VALUE)
    public void enable(IBioticStat stat) {
        setEnabled(stat, Boolean.TRUE);
    }

    @MethodDescription(type = MethodDescription.Type.ADDITION)
    public void addRequiredItem(IBioticStat stat, ItemStack item) {
        AbstractBioticStat a = asAbs(stat);
        if (a == null) return;
        a.addReqiredItm(item);
        undoStack.push(() -> {
            List<ItemStack> reqs = a.getRequiredItems();
            // remove last matching instance
            for (int i = reqs.size() - 1; i >= 0; i--) {
                if (ItemStack.areItemStacksEqual(reqs.get(i), item)) {
                    reqs.remove(i);
                    break;
                }
            }
        });
    }

    @MethodDescription
    public void clearRequiredItems(IBioticStat stat) {
        AbstractBioticStat a = asAbs(stat);
        if (a == null) return;
        List<ItemStack> prev = new ArrayList<>(a.getRequiredItems());
        a.clearRequiredItems();
        undoStack.push(() -> {
            a.clearRequiredItems();
            for (ItemStack s : prev) a.addReqiredItm(s);
        });
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public List<String> getStatNames() {
        return MatterOverdrive.STAT_REGISTRY.getStats().stream()
                .map(IBioticStat::getUnlocalizedName)
                .sorted()
                .collect(Collectors.toList());
    }

    @MethodDescription(example = @Example("'cloak'"))
    public void unregister(String unlocalizedName) {
        IBioticStat removed = MatterOverdrive.STAT_REGISTRY.getStat(unlocalizedName);
        if (removed == null) return;
        MatterOverdrive.STAT_REGISTRY.unregisterStat(unlocalizedName);
        undoStack.push(() -> MatterOverdrive.STAT_REGISTRY.registerStat(removed));
    }
}
