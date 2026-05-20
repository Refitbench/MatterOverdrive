package matteroverdrive.compat.modules.sd;

import matteroverdrive.compat.Compat;
import matteroverdrive.entity.android_player.AndroidPlayer;
import matteroverdrive.entity.player.MOPlayerCapabilityProvider;
import matteroverdrive.init.OverdriveBioticStats;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Compat(CompatSDClient.ID)
public class CompatSDClient {

    public static final String ID = "simpledifficulty";

    @Compat.Init
    public static void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new CompatSDClient());
    }

    // Prevents the thirst overlay from rendering when player has zero calories stat.
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPreRenderOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.AIR) return;
        if (!SDHelper.thirstEnabled) return;

        AndroidPlayer android = MOPlayerCapabilityProvider.GetAndroidCapability(Minecraft.getMinecraft().player);
        if (android == null) return;
        if (!android.isAndroid()) return;
        if (!android.isUnlocked(OverdriveBioticStats.zeroCalories, 1)) return;
        if (!OverdriveBioticStats.zeroCalories.isEnabled(android, 1)) return;

        event.setCanceled(true);
    }
}
