package matteroverdrive.compat.modules.groovyscript;

import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.api.documentation.annotations.Example;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.Reference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;


/**
 * GroovyScript registry for Matter Overdrive's replicator blacklist.
 * Items on this list cannot be replicated even if they have a matter value.
 */
@RegistryDescription(linkGenerator = Reference.MOD_ID, category = RegistryDescription.Category.ENTRIES)
public class Replicator extends VirtualizedRegistry<Item> {

    @Override
    public void onReload() {
        for (Item item : removeScripted()) {
            MatterOverdrive.MATTER_REGISTRY.removeFromReplicationBlacklist(item);
        }
    }

    @MethodDescription(type = MethodDescription.Type.ADDITION, example = {@Example("item('matteroverdrive:matter_dust')"), @Example("ore('blockGold')")})
    public void addBlacklist(IIngredient ingredient) {
        for (ItemStack stack : ingredient.getMatchingStacks()) {
            if (!stack.isEmpty()) {
                MatterOverdrive.MATTER_REGISTRY.addToReplicationBlacklist(stack.getItem());
                addScripted(stack.getItem());
            }
        }
    }

}
