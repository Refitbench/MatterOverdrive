package matteroverdrive.compat.modules.groovyscript;

import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.api.documentation.annotations.Example;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;

import matteroverdrive.MatterOverdrive;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;


/**
 * GroovyScript registry for Matter Overdrive's replicator blacklist.
 * Items on this list cannot be replicated even if they have a matter value.
 */
@RegistryDescription
public class ReplicatorCompat extends VirtualizedRegistry<Item> {

    @Override
    public void onReload() {
        for (Item item : removeScripted()) {
            MatterOverdrive.MATTER_REGISTRY.removeFromReplicationBlacklist(item);
        }
    }

    @MethodDescription(example = @Example("item('matteroverdrive:matter_dust').getItem()"))
    public void add(Item item) {
        MatterOverdrive.MATTER_REGISTRY.addToReplicationBlacklist(item);
        addScripted(item);
    }

    @MethodDescription
    public void add(ItemStack stack) {
        if (!stack.isEmpty()) add(stack.getItem());
    }

    @MethodDescription
    public void add(IIngredient ingredient) {
        for (ItemStack stack : ingredient.getMatchingStacks()) {
            add(stack);
        }
    }

}
