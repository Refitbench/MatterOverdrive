package matteroverdrive.compat.modules.groovyscript;

import com.cleanroommc.groovyscript.api.Result;
import com.cleanroommc.groovyscript.compat.mods.GroovyContainer;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.api.android.IBioticStat;
import matteroverdrive.api.dialog.IDialogMessage;
import matteroverdrive.data.matter.MatterEntryItem;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

/**
 * Object mappers (a.k.a. bracket handlers) registered with the MO GroovyScript
 * container. Provides:
 *
 * <ul>
 *   <li>{@code matter('modid:itemid')} → {@link MatterEntryItem}</li>
 *   <li>{@code androidStat('unlocalized_name')} → {@link IBioticStat}</li>
 *   <li>{@code dialog('mo:resource_name')} → {@link IDialogMessage}</li>
 * </ul>
 */
final class MOObjectMappers {

    private MOObjectMappers() {}

    static void register(GroovyContainer<?> container) {
        container.objectMapperBuilder("matter", MatterEntryItem.class)
                .parser((s, args) -> {
                    Item item = Item.getByNameOrId(s);
                    if (item == null) return Result.error("Unknown item id: " + s);
                    MatterEntryItem entry = MatterOverdrive.MATTER_REGISTRY.getItemEntries().get(item);
                    return entry == null ? Result.error("No matter entry for: " + s) : Result.some(entry);
                })
                .docOfType("matter entry")
                .register();

        container.objectMapperBuilder("androidStat", IBioticStat.class)
                .parser((s, args) -> {
                    IBioticStat stat = MatterOverdrive.STAT_REGISTRY.getStat(s);
                    return stat == null ? Result.error("Unknown android stat: " + s) : Result.some(stat);
                })
                .docOfType("android biotic stat")
                .register();

        container.objectMapperBuilder("dialog", IDialogMessage.class)
                .parser((s, args) -> {
                    ResourceLocation rl;
                    try {
                        rl = new ResourceLocation(s);
                    } catch (Exception e) {
                        return Result.error("Invalid dialog resource location: " + s);
                    }
                    IDialogMessage msg = MatterOverdrive.DIALOG_REGISTRY.getMessage(rl);
                    return msg == null ? Result.error("Unknown dialog message: " + s) : Result.some(msg);
                })
                .docOfType("dialog message")
                .register();
    }
}
