package matteroverdrive.compat.modules.groovyscript;

import matteroverdrive.compat.Compat;
import matteroverdrive.util.MOLog;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Marker compat module for GroovyScript. The actual integration lives in
 * {@link MOGroovyPlugin}, which GroovyScript discovers automatically via
 * classpath scan of {@link com.cleanroommc.groovyscript.api.GroovyPlugin}
 * implementations.
 *
 * <p>This class only exists so the standard {@link MatterOverdriveCompat}
 * pipeline reports the GroovyScript module as loaded.</p>
 */
@Compat(CompatGroovyScript.ID)
public class CompatGroovyScript {

    public static final String ID = "groovyscript";

    @Compat.PreInit
    public static void preInit(FMLPreInitializationEvent event) {
        MOLog.info("GroovyScript detected — Matter Overdrive compat plugin will be loaded by GroovyScript.");
    }
}
