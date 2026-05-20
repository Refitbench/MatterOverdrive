package matteroverdrive.mixins.client.sd;

import com.charles445.simpledifficulty.client.gui.TemperatureGui;
import matteroverdrive.compat.modules.sd.SDHelper;
import matteroverdrive.entity.android_player.AndroidPlayer;
import matteroverdrive.entity.player.MOPlayerCapabilityProvider;
import matteroverdrive.init.OverdriveBioticStats;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppresses SimpleDifficulty's temperature thermometer overlay when an android
 * has the Thermal Regulation bionetic stat active in suppress mode.
 */
@SideOnly(Side.CLIENT)
@Mixin(value = TemperatureGui.class, remap = false)
public class MixinTemperatureGui {

    @Inject(method = "onPreRenderGameOverlay", at = @At("HEAD"), cancellable = true)
    private void mo_suppressForAndroid(RenderGameOverlayEvent.Pre event, CallbackInfo ci) {
        if (event.getType() != ElementType.ALL) return;

        // Only suppress HUD in suppress mode. Threshold mode leaves the thermometer visible
        // so the player can see their temperature and manage it to save power.
        if (!SDHelper.temperatureSuppressMode) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return;

        AndroidPlayer android = MOPlayerCapabilityProvider.GetAndroidCapability(mc.player);
        if (android != null
                && android.isAndroid()
                && android.isUnlocked(OverdriveBioticStats.tanTemperature, 1)
                && OverdriveBioticStats.tanTemperature.isEnabled(android, 1)) {
            ci.cancel();
        }
    }
}
