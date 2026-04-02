
package matteroverdrive.tile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import matteroverdrive.api.inventory.UpgradeTypes;
import matteroverdrive.api.machines.IUpgradeHandler;
import matteroverdrive.blocks.includes.MOBlockMachine;
import matteroverdrive.client.sound.MachineSound;
import matteroverdrive.entity.android_player.AndroidPlayer;
import matteroverdrive.entity.player.MOPlayerCapabilityProvider;
import matteroverdrive.init.MatterOverdriveSounds;
import matteroverdrive.machines.events.MachineEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityMachineChargingStation extends MOTileEntityMachineEnergy {

	public static final int ENERGY_CAPACITY = 512000;
	public static final int ENERGY_TRANSFER = 512;
	private static final UpgradeHandler upgradeHandler = new UpgradeHandler();
	public static int BASE_MAX_RANGE = 8;

	private static final float CHARGE_START_THRESHOLD = 0.98f;

	private boolean isActivelyCharging = false;
	private boolean lastAlcoveSoundActive = false;
	private MachineSound alcoveLoopSound = null;
	// Server-side latch: players whose charge session is in progress
	private final Set<UUID> chargingPlayers = new HashSet<>();
	private final Set<UUID> presentPlayers = new HashSet<>();

	public TileEntityMachineChargingStation() {
		super(2);
		this.energyStorage.setCapacity(ENERGY_CAPACITY);
		this.energyStorage.setMaxExtract(ENERGY_TRANSFER);
		this.energyStorage.setMaxReceive(ENERGY_CAPACITY);
		playerSlotsHotbar = true;
		playerSlotsMain = true;
	}

	@Override
	public void update() {
		super.update();
		manageAndroidCharging();
	}

	private void manageAndroidCharging() {
		if (world.isRemote) return;
		if (getEnergyStorage().getEnergyStored() <= 0) {
			if (isActivelyCharging) {
				isActivelyCharging = false;
			}
			return;
		}
		boolean nowCharging = false;
		if (getEnergyStorage().getEnergyStored() > 0) {
			int range = getRange();
			AxisAlignedBB radius = new AxisAlignedBB(getPos().add(-range, -range, -range),
					getPos().add(range, range, range));
			List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, radius);
			presentPlayers.clear();
			for (EntityPlayer player : players) {
				AndroidPlayer android = MOPlayerCapabilityProvider.GetAndroidCapability(player);
				if (!android.isAndroid()) continue;
				presentPlayers.add(player.getUniqueID());
				// Check charge ratio BEFORE this tick's transfer
				float chargeRatio = android.getMaxEnergyStored() > 0
						? (float) android.getEnergyStored() / android.getMaxEnergyStored()
						: 1f;
				int required = getRequiredEnergy(player, range);
				int max = Math.min(getEnergyStorage().getEnergyStored(), getMaxCharging());
				int toExtract = Math.min(required, max);
				int transferred = android.receiveEnergy(toExtract, false);
				getEnergyStorage().extractEnergy(transferred, false);
				// Enter latch if battery was below threshold
				if (!chargingPlayers.contains(player.getUniqueID()) && chargeRatio < CHARGE_START_THRESHOLD) {
					chargingPlayers.add(player.getUniqueID());
				}
				// Exit latch when full (nothing accepted)
				if (chargingPlayers.contains(player.getUniqueID()) && transferred == 0) {
					chargingPlayers.remove(player.getUniqueID());
				}
				if (chargingPlayers.contains(player.getUniqueID())) {
					nowCharging = true;
				}
			}
			// Remove players who left range
			chargingPlayers.retainAll(presentPlayers);
		}
		isActivelyCharging = nowCharging;
	}

	public int getRange() {
		return (int) (BASE_MAX_RANGE * getUpgradeMultiply(UpgradeTypes.Range));
	}

	public int getMaxCharging() {
		return (int) (ENERGY_TRANSFER / getUpgradeMultiply(UpgradeTypes.PowerUsage));
	}

	private int getRequiredEnergy(EntityPlayer player, int maxRange) {
		return (int) (ENERGY_TRANSFER * (1.0D - MathHelper
				.clamp((new Vec3d(player.posX, player.posY, player.posZ).subtract(new Vec3d(getPos())).length()
						/ (double) maxRange), 0, 1)));
	}

	@Override
	public SoundEvent getSound() {
		return null;
	}

	@Override
	public boolean hasSound() {
		return false;
	}

	@Override
	public boolean getServerActive() {
		return isActivelyCharging;
	}

	@Override
	public float soundVolume() {
		return 0;
	}

	@Override
	protected void onMachineEvent(MachineEvent event) {

	}

	@Override
	public boolean isAffectedByUpgrade(UpgradeTypes type) {
		return type.equals(UpgradeTypes.Range) || type.equals(UpgradeTypes.PowerStorage)
				|| type.equals(UpgradeTypes.PowerUsage) || type.equals(UpgradeTypes.Muffler);
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected void manageSound() {
		boolean currentlyActive = isActive();
		float soundMultiply = 1;
		if (getBlockType() instanceof MOBlockMachine) {
			soundMultiply = ((MOBlockMachine<?>) getBlockType()).volume;
		}

		if (getUpgradeMultiply(UpgradeTypes.Muffler) >= 2d) {
			stopAlcoveLoopSound();
			lastAlcoveSoundActive = false;
			return;
		}
		
		if (currentlyActive && !lastAlcoveSoundActive) {
			MachineSound startSound = new MachineSound(
					MatterOverdriveSounds.alcoveChargeStart, SoundCategory.BLOCKS, getPos(), soundMultiply, 1.0f);
			startSound.setRepeat(false);
			FMLClientHandler.instance().getClient().getSoundHandler().playSound(startSound);
			alcoveLoopSound = new MachineSound(
					MatterOverdriveSounds.alcoveChargeLoop, SoundCategory.BLOCKS, getPos(), soundMultiply, 1.0f);
			FMLClientHandler.instance().getClient().getSoundHandler().playSound(alcoveLoopSound);
		} else if (currentlyActive && alcoveLoopSound != null
				&& !FMLClientHandler.instance().getClient().getSoundHandler().isSoundPlaying(alcoveLoopSound)) {
			// Streaming sound was dropped by the engine — resubmit the loop
			alcoveLoopSound = new MachineSound(
					MatterOverdriveSounds.alcoveChargeLoop, SoundCategory.BLOCKS, getPos(), soundMultiply, 1.0f);
			FMLClientHandler.instance().getClient().getSoundHandler().playSound(alcoveLoopSound);
		} else if (!currentlyActive && lastAlcoveSoundActive) {
			stopAlcoveLoopSound();
			MachineSound stopSound = new MachineSound(
					MatterOverdriveSounds.alcoveChargeStop, SoundCategory.BLOCKS, getPos(), soundMultiply, 1.0f);
			stopSound.setRepeat(false);
			FMLClientHandler.instance().getClient().getSoundHandler().playSound(stopSound);
		}
		lastAlcoveSoundActive = currentlyActive;
	}

	@SideOnly(Side.CLIENT)
	private void stopAlcoveLoopSound() {
		if (alcoveLoopSound != null) {
			alcoveLoopSound.stopPlaying();
			FMLClientHandler.instance().getClient().getSoundHandler().stopSound(alcoveLoopSound);
			alcoveLoopSound = null;
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (world.isRemote) {
			stopAlcoveLoopSound();
		}
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if (world.isRemote) {
			stopAlcoveLoopSound();
		}
	}

	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 8192.0D;
	}

	public IUpgradeHandler getUpgradeHandler() {
		return upgradeHandler;
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		return new int[0];
	}

	public static class UpgradeHandler implements IUpgradeHandler {

		@Override
		public double affectUpgrade(UpgradeTypes type, double multiply) {
			if (type.equals(UpgradeTypes.Range)) {
				return Math.min(8, multiply);
			}
			return multiply;
		}
	}
}
