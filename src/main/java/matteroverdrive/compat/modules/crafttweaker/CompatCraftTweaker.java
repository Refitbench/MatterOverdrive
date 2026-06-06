package matteroverdrive.compat.modules.crafttweaker;

import matteroverdrive.compat.Compat;
import matteroverdrive.util.MOLog;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Marker compat module for CraftTweaker2. The actual integration lives in the
 * {@code CT*} ZenClass files, which CraftTweaker2 discovers automatically via
 * its {@code @ZenRegister} ASM scan during the Construction phase.
 *
 * <p>This class only exists so the standard {@link matteroverdrive.compat.MatterOverdriveCompat}
 * pipeline reports the CraftTweaker module as loaded.</p>
 */
@Compat(CompatCraftTweaker.ID)
public class CompatCraftTweaker {

    public static final String ID = "crafttweaker2";

    @Compat.PreInit
    public static void preInit(FMLPreInitializationEvent event) {
        MOLog.info("CraftTweaker detected — Matter Overdrive CT integration loaded.");
    }
}
