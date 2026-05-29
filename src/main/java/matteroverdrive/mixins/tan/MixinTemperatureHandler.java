package matteroverdrive.mixins.tan;

import matteroverdrive.compat.modules.tan.TANHelper;
import matteroverdrive.entity.android_player.AndroidPlayer;
import matteroverdrive.entity.player.MOPlayerCapabilityProvider;
import matteroverdrive.init.OverdriveBioticStats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import toughasnails.temperature.TemperatureHandler;

/**
 * Skips TAN's per-tick temperature calculation entirely for androids that have
 * the Thermal Regulation stat active in suppress mode.
 */
@Mixin(value = TemperatureHandler.class, remap = false)
public class MixinTemperatureHandler {

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void mo_skipForSuppressedAndroid(EntityPlayer player, World world, TickEvent.Phase phase, CallbackInfo ci) {
        if (world.isRemote) return;
        if (!TANHelper.temperatureSuppressMode) return;

        AndroidPlayer android = MOPlayerCapabilityProvider.GetAndroidCapability(player);
        if (android == null || !android.isAndroid()) return;
        if (!android.isUnlocked(OverdriveBioticStats.tanTemperature, 1)) return;
        if (!OverdriveBioticStats.tanTemperature.isEnabled(android, 1)) return;

        ci.cancel();
    }
}
