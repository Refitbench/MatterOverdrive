package matteroverdrive.compat.modules.groovyscript;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.documentation.annotations.Admonition;
import com.cleanroommc.groovyscript.api.documentation.annotations.Example;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.Reference;
import matteroverdrive.api.android.IBioticStat;
import matteroverdrive.data.biostats.AbstractBioticStat;

import net.minecraft.item.ItemStack;

/**
 * GroovyScript registry for tweaking Android biotic stat parameters.
 * Mutations are captured into undo actions so {@code /groovyscript reload}
 * restores the previous values.
 */
@RegistryDescription(
        linkGenerator = Reference.MOD_ID,
        category = RegistryDescription.Category.CUSTOM,
        admonition = {
                @Admonition(value = "groovyscript.wiki.matteroverdrive.android.note0", type = Admonition.Type.INFO),
                @Admonition(value = "groovyscript.wiki.matteroverdrive.android.note1", type = Admonition.Type.TIP)
        })
public class Android extends VirtualizedRegistry<Runnable> {

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

    @MethodDescription(type = MethodDescription.Type.ADDITION, example = @Example("androidStat('shield'), 25"))
    public void setXp(AbstractBioticStat stat, int xp) {
        int prev = stat.getXp();
        stat.setXp(xp);
        undoStack.push(() -> stat.setXp(prev));
    }

    private void setEnabled(AbstractBioticStat stat, Boolean enabled) {
        Boolean prev = stat.getEnabledOverride();
        stat.setEnabledOverride(enabled);
        undoStack.push(() -> stat.setEnabledOverride(prev));
    }

    private void setHidden(AbstractBioticStat stat, boolean hidden) {
        boolean prev = stat.isHiddenOverride();
        stat.setHiddenOverride(hidden);
        undoStack.push(() -> stat.setHiddenOverride(prev));
    }

    @MethodDescription(type = MethodDescription.Type.VALUE)
    public void disable(AbstractBioticStat stat) {
        setEnabled(stat, Boolean.FALSE);
    }

    @MethodDescription(type = MethodDescription.Type.VALUE)
    public void enable(AbstractBioticStat stat) {
        setEnabled(stat, Boolean.TRUE);
    }

    @MethodDescription(type = MethodDescription.Type.ADDITION, example = @Example("androidStat('shield'), item('minecraft:iron_ingot') * 5"))
    public void addRequiredItem(AbstractBioticStat stat, ItemStack item) {
        stat.addReqiredItm(item);
        undoStack.push(() -> {
            List<ItemStack> reqs = stat.getRequiredItems();
            // remove last matching instance
            for (int i = reqs.size() - 1; i >= 0; i--) {
                if (ItemStack.areItemStacksEqual(reqs.get(i), item)) {
                    reqs.remove(i);
                    break;
                }
            }
        });
    }

    @MethodDescription(example = @Example("androidStat('shield')"))
    public void clearRequiredItems(AbstractBioticStat stat) {
        List<ItemStack> prev = new ArrayList<>(stat.getRequiredItems());
        stat.clearRequiredItems();
        undoStack.push(() -> {
            stat.clearRequiredItems();
            for (ItemStack s : prev) stat.addReqiredItm(s);
        });
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public List<String> getStatNames() {
        return MatterOverdrive.STAT_REGISTRY.getStats().stream()
                .map(IBioticStat::getUnlocalizedName)
                .sorted()
                .collect(Collectors.toList());
    }

    @MethodDescription(type = MethodDescription.Type.VALUE, example = @Example("androidStat('cloak')"))
    public void unregister(AbstractBioticStat stat) {
        setHidden(stat, true);
    }
}
