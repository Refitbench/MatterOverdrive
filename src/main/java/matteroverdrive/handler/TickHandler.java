
package matteroverdrive.handler;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.proxy.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;

public class TickHandler {
	private final PlayerEventHandler playerEventHandler;
	private long lastTickTime;
	private int lastTickLength;

	public TickHandler(ConfigurationHandler configurationHandler, PlayerEventHandler playerEventHandler) {
		this.playerEventHandler = playerEventHandler;
	}

	// Called when the client ticks.
	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (Minecraft.getMinecraft().player == null || Minecraft.getMinecraft().world == null) {
			return;
		}

		if (ClientProxy.instance().getClientWeaponHandler() != null) {
			ClientProxy.instance().getClientWeaponHandler().onClientTick(event);
		}

		if (!Minecraft.getMinecraft().isGamePaused() && event.phase.equals(TickEvent.Phase.START)) {
			ClientProxy.questHud.onTick();
		}
	}

	// Called when the server ticks. Usually 20 ticks a second.
	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.side == Side.SERVER && event.phase == Phase.END) {
		playerEventHandler.onServerTick(event);

		lastTickLength = (int) (System.nanoTime() - lastTickTime);
		lastTickTime = System.nanoTime();
	}
	}

	public void onServerStart(FMLServerStartedEvent event) {

	}

	// Called when a new frame is displayed (See fps)
	@SubscribeEvent
	public void onRenderTick(TickEvent.RenderTickEvent event) {
		ClientProxy.instance().getClientWeaponHandler().onTick(event);
	}

	// Called when the world ticks
	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event) {
		MatterOverdrive.MO_WORLD.onWorldTick(event);
	}

	public int getLastTickLength() {
		return lastTickLength;
	}
}
