package matteroverdrive.compat.modules.groovyscript;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

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
@RegistryDescription
public class AndroidCompat extends VirtualizedRegistry<Runnable> {

    private final Deque<Runnable> undoStack = new ArrayDeque<>();

    @Override
    public void onReload() {
        while (!undoStack.isEmpty()) {
            try {
                undoStack.pop().run();
            } catch (Exception ignored) {
                // best-effort restoration
            }
        }
    }

    private static AbstractBioticStat asAbs(IBioticStat stat) {
        if (!(stat instanceof AbstractBioticStat)) {
            throw new IllegalArgumentException("Stat " + (stat == null ? "null" : stat.getUnlocalizedName())
                    + " is not an AbstractBioticStat; cannot mutate via GroovyScript");
        }
        return (AbstractBioticStat) stat;
    }

    @MethodDescription(example = @Example("androidStat('shield'), 25"))
    public void setXp(IBioticStat stat, int xp) {
        AbstractBioticStat a = asAbs(stat);
        int prev = a.getXp();
        a.setXp(xp);
        undoStack.push(() -> a.setXp(prev));
    }

    private void setEnabled(IBioticStat stat, Boolean enabled) {
        AbstractBioticStat a = asAbs(stat);
        Boolean prev = a.getEnabledOverride();
        a.setEnabledOverride(enabled);
        undoStack.push(() -> a.setEnabledOverride(prev));
    }

    @MethodDescription
    public void disable(IBioticStat stat) {
        setEnabled(stat, Boolean.FALSE);
    }

    @MethodDescription
    public void enable(IBioticStat stat) {
        setEnabled(stat, Boolean.TRUE);
    }

    @MethodDescription
    public void addRequiredItem(IBioticStat stat, ItemStack item) {
        AbstractBioticStat a = asAbs(stat);
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
        List<ItemStack> prev = new ArrayList<>(a.getRequiredItems());
        a.clearRequiredItems();
        undoStack.push(() -> {
            a.clearRequiredItems();
            for (ItemStack s : prev) a.addReqiredItm(s);
        });
    }

    @MethodDescription(example = @Example("'cloak'"))
    public void unregister(String unlocalizedName) {
        IBioticStat removed = MatterOverdrive.STAT_REGISTRY.getStat(unlocalizedName);
        if (removed == null) return;
        MatterOverdrive.STAT_REGISTRY.unregisterStat(unlocalizedName);
        undoStack.push(() -> MatterOverdrive.STAT_REGISTRY.registerStat(removed));
    }
}
