package matteroverdrive.compat.modules.groovyscript;

import com.cleanroommc.groovyscript.api.GroovyBlacklist;
import com.cleanroommc.groovyscript.compat.mods.GroovyContainer;
import com.cleanroommc.groovyscript.compat.mods.GroovyPropertyContainer;

/**
 * Property container exposed to Groovy scripts as {@code mods.matteroverdrive}
 * and {@code mods.mo}. Holds the per-subsystem registry instances as public
 * final fields; {@link GroovyPropertyContainer#addPropertyFieldsOf} picks them
 * up automatically.
 */
public class MatterOverdriveContainer extends GroovyPropertyContainer {

    @GroovyBlacklist
    public static MatterOverdriveContainer instance;

    public final Matter matter = new Matter();
    public final Inscriber inscriber = new Inscriber();
    public final Android android = new Android();
    public final Replicator replicator = new Replicator();

    @Override
    public void initialize(GroovyContainer<?> owner) {
        super.initialize(owner);
        instance = this;
    }
}
