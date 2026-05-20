package matteroverdrive.compat.modules.sd;

/**
 * Lightweight sentinel for Simple Difficulty presence.
 */
public class SDHelper {
    public static boolean isLoaded = false;
    /** Cached at init; avoids a QuickConfig lookup every tick. */
    public static boolean thirstEnabled = false;
    public static boolean temperatureEnabled = false;
    public static boolean temperatureSuppressMode = false;
    /** True when SD is loaded AND MO compat is enabled in config. */
    public static boolean enabled = false;
    public static int thirstEnergyCost = 256;
    public static int temperatureEnergyCost = 128;
}
