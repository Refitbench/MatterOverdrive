package matteroverdrive.compat.modules.groovyscript;

import com.cleanroommc.groovyscript.compat.mods.GroovyContainer;
import com.cleanroommc.groovyscript.compat.mods.GroovyPropertyContainer;

/**
 * Property container exposed to Groovy scripts as {@code mods.matteroverdrive}
 * and {@code mods.mo}. Holds the per-subsystem registry instances as public
 * final fields; {@link GroovyPropertyContainer#addPropertyFieldsOf} picks them
 * up automatically.
 */
public class MatterOverdriveContainer extends GroovyPropertyContainer {

    public final MatterCompat matter = new MatterCompat();
    public final InscriberCompat inscriber = new InscriberCompat();
    public final AndroidCompat android = new AndroidCompat();
    public final ReplicatorCompat replicator = new ReplicatorCompat();

    @Override
    public void initialize(GroovyContainer<?> owner) {
        super.initialize(owner);
    }
}
