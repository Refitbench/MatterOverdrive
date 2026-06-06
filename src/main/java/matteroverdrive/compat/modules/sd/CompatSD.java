package matteroverdrive.compat.modules.sd;

import com.charles445.simpledifficulty.api.SDBlocks;
import com.charles445.simpledifficulty.api.SDCapabilities;
import com.charles445.simpledifficulty.api.config.QuickConfig;
import com.charles445.simpledifficulty.api.temperature.ITemperatureCapability;
import com.charles445.simpledifficulty.api.temperature.TemperatureEnum;
import com.charles445.simpledifficulty.api.thirst.IThirstCapability;
import matteroverdrive.MatterOverdrive;
import matteroverdrive.compat.Compat;
import matteroverdrive.entity.android_player.AndroidPlayer;
import matteroverdrive.init.OverdriveBioticStats;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Compat(CompatSD.ID)
public class CompatSD {

    public static final String ID = "simpledifficulty";

    @SuppressWarnings("null")
    @Compat.PreInit
    public static void preInit(FMLPreInitializationEvent event) {
        // Add required items in PreInit so they exist before CraftTweaker applies its actions.
        if (SDBlocks.heater != null) {
            OverdriveBioticStats.tanTemperature.addReqiredItm(new ItemStack(SDBlocks.heater, 5));
        }
        if (SDBlocks.chiller != null) {
            OverdriveBioticStats.tanTemperature.addReqiredItm(new ItemStack(SDBlocks.chiller, 5));
        }
    }

    @SuppressWarnings("null")
    @Compat.Init
    public static void init(FMLInitializationEvent event) {
        SDHelper.isLoaded = true;
        SDHelper.thirstEnabled = QuickConfig.isThirstEnabled();
        SDHelper.temperatureEnabled = QuickConfig.isTemperatureEnabled();
        // Shared config category with TAN; SD reads the same fields.
        SDHelper.enabled = MatterOverdrive.CONFIG_HANDLER.tanCompatEnabled;
        SDHelper.thirstEnergyCost = MatterOverdrive.CONFIG_HANDLER.tanThirstEnergyCost;
        SDHelper.temperatureEnergyCost = MatterOverdrive.CONFIG_HANDLER.tanTemperatureEnergyCost;
        SDHelper.temperatureSuppressMode = MatterOverdrive.CONFIG_HANDLER.tanTemperatureSuppressMode;

        if (Loader.isModLoaded("toughasnails")) {
            MatterOverdrive.LOGGER.warn("Both ToughAsNails and SimpleDifficulty are present. "
                    + "These mods provide overlapping gameplay systems and Matter Overdrive treats them "
                    + "as mutually exclusive. Both compat modules will run independently.");
        }
    }

    /**
     * Restores any thirst deficit and bills RF for both the restored points and
     * the unconverted exhaustion drift accumulated since the last check.
     */
    public static void suppressThirst(AndroidPlayer android, int energyPerPoint) {
        if (!SDHelper.thirstEnabled) return;
        IThirstCapability thirst = SDCapabilities.getThirstData(android.getPlayer());
        if (thirst == null) return;

        int currentThirst = thirst.getThirstLevel();
        int thirstNeeded = 20 - currentThirst;
        float exhaustion = thirst.getThirstExhaustion();

        if (thirstNeeded <= 0 && exhaustion <= 0.0f) return;

        // Bill RF for pending exhaustion (fractional) + thirst deficit (whole points).
        float totalDeficit = thirstNeeded + exhaustion / 4.0f;
        int rfNeeded = (int) Math.ceil(totalDeficit * energyPerPoint);
        if (rfNeeded <= 0) return;

        int extracted = android.extractEnergyRaw(rfNeeded, false);
        if (extracted <= 0) return;

        int pointsRestored = Math.min(thirstNeeded, extracted / energyPerPoint);
        if (pointsRestored > 0) {
            thirst.setThirstLevel(currentThirst + pointsRestored);
            thirst.setThirstSaturation(5.0f);
        }
        if (exhaustion > 0.0f) {
            thirst.setThirstExhaustion(0.0f);
        }
    }

    /**
     * Regulates android body temperature according to the configured mode.
     *
     * Suppress mode:
     *   Jumps directly to neutral. No energy cost. Hides SD thermometer HUD.
     *
     * Threshold mode:
     *   Acts when temperature leaves the NORMAL safe range (11-14). Nudges one
     *   step toward neutral and extracts energyPerStep RF. If lacks energy the
     *   correction is skipped, allowing drift.
     */
    public static void suppressTemperature(AndroidPlayer android, int energyPerStep) {
        if (!SDHelper.temperatureEnabled) return;
        ITemperatureCapability temp = SDCapabilities.getTemperatureData(android.getPlayer());
        if (temp == null) return;
        int current = temp.getTemperatureLevel();
        int neutral = TemperatureEnum.NORMAL.getMiddle(); // 12

        if (SDHelper.temperatureSuppressMode) {
            // Freeze directly at neutral — no energy cost.
            if (current != neutral) {
                temp.setTemperatureLevel(neutral);
            }
        } else {
            // FREEZING 0-5, COLD 6-10, NORMAL 11-14, HOT 15-19, BURNING 20-25.
            if (current > 6 && current < 19) return;
            int delta = current - neutral;
            if (delta == 0) return;
            int direction = (int) Math.signum(-delta);
            int extracted = android.extractEnergyRaw(energyPerStep, false);
            if (extracted >= energyPerStep) {
                temp.addTemperatureLevel(direction);
            }
        }
    }
}
