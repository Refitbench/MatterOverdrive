
package matteroverdrive.tile;

import matteroverdrive.api.gravity.AnomalySuppressor;
import matteroverdrive.api.inventory.UpgradeTypes;
import matteroverdrive.blocks.BlockGravitationalAnomaly;
import matteroverdrive.blocks.includes.MOBlock;
import matteroverdrive.client.render.RenderParticlesHandler;
import matteroverdrive.fx.GravitationalStabilizerBeamParticle;
import matteroverdrive.handler.ConfigurationHandler;
import matteroverdrive.machines.MachineNBTCategory;
import matteroverdrive.machines.configs.ConfigPropertyStringList;
import matteroverdrive.init.MatterOverdriveSounds;
import matteroverdrive.machines.events.MachineEvent;
import matteroverdrive.proxy.ClientProxy;
import matteroverdrive.util.MOStringHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundEvent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.EnumSet;

import static matteroverdrive.util.MOBlockHelper.getAboveSide;

public class TileEntityMachineGravitationalStabilizer extends MOTileEntityMachineEnergy {
	public static Color color1 = new Color(0xFFFFFF);
	public static Color color2 = new Color(0xFF0000);
	public static Color color3 = new Color(0x115A84);

	// Energy/scan config — populated by BlockGravitationalStabilizer.onConfigChanged()
	public static int ENERGY_CAPACITY = 100000;
	public static int MAX_ENERGY_RECEIVE = 2000;
	public static int BASE_ENERGY_PER_TICK = 20;
	public static int MAX_ENERGY_PER_TICK = 200;
	public static int RESCAN_RATE = 40;

	RayTraceResult hit;
	private EnumFacing cachedFront;
	private int rescanTimer = RESCAN_RATE; // start at RESCAN_RATE so first tick triggers an immediate scan

	public TileEntityMachineGravitationalStabilizer() {
		super(4);
		energyStorage.setCapacity(ENERGY_CAPACITY);
		energyStorage.setMaxReceive(MAX_ENERGY_RECEIVE);
		energyStorage.setMaxExtract(MAX_ENERGY_PER_TICK);
	}

	@Override
	protected void registerComponents() {
		super.registerComponents();
		getConfigs().addProperty(new ConfigPropertyStringList("comparatorMode", "gui.config.comparator_mode",
				new String[] { MOStringHelper.translateToLocal("gui.config.comparator_mode.power"),
						MOStringHelper.translateToLocal("gui.config.comparator_mode.stability") },
				0));
	}

	public int getComparatorMode() {
		return getConfigs().getEnum("comparatorMode", 0);
	}

	@Override
	public void update() {
		super.update();
		rescanTimer++;

		if (world.isRemote) {
			if (rescanTimer >= RESCAN_RATE) {
				hit = seacrhForAnomalies(world);
				rescanTimer = 0;
			}
			spawnParticles(world);
		} else {
			if (getRedstoneActive()) {
				manageAnomalies(world);
			}
			UpdateClientPower();
		}
	}

	@Override
	protected void onMachineEvent(MachineEvent event) {
		if (event instanceof MachineEvent.NeighborChange) {
			cachedFront = null;
			EnumFacing front = getFront();
			BlockPos pos1 = getPos().offset(front, 1);
			IBlockState bs = world.getBlockState(pos1);
			if (bs.getBlock() instanceof BlockGravitationalAnomaly || bs.getMaterial().isOpaque()) {
				// something just entered pos 1 — update hit immediately, no rescan needed
				hit = new RayTraceResult(
						new Vec3d(pos1).subtract(Math.abs(front.getDirectionVec().getX() * 0.5),
								Math.abs(front.getDirectionVec().getY() * 0.5),
								Math.abs(front.getDirectionVec().getZ() * 0.5)),
						front.getOpposite(), pos1);
			} else if (hit != null && hit.getBlockPos().equals(pos1)) {
				// pos 1 was cleared — rescan positions 2+ next tick to find new hit
				hit = null;
				rescanTimer = RESCAN_RATE;
			}
		}
	}

	private EnumFacing getFront() {
		if (cachedFront == null) {
			cachedFront = world.getBlockState(getPos()).getValue(MOBlock.PROPERTY_DIRECTION).getOpposite();
		}
		return cachedFront;
	}

	RayTraceResult seacrhForAnomalies(World world) {
		EnumFacing front = getFront();
		for (int i = 2; i < 64; i++) { // pos 1 is handled exclusively by the NeighborChange event
			IBlockState blockState = world.getBlockState(getPos().offset(front, i));
			if (blockState.getBlock() instanceof BlockGravitationalAnomaly || blockState.getMaterial().isOpaque()) {
				return new RayTraceResult(
						new Vec3d(getPos().offset(front, i)).subtract(Math.abs(front.getDirectionVec().getX() * 0.5),
								Math.abs(front.getDirectionVec().getY() * 0.5),
								Math.abs(front.getDirectionVec().getZ() * 0.5)),
						front.getOpposite(), getPos().offset(front, i));
			}
		}
		return null;
	}

	void manageAnomalies(World world) {
		if (rescanTimer >= RESCAN_RATE) {
			rescanTimer = 0;
			hit = seacrhForAnomalies(world);
		}
		if (hit != null) {
			TileEntity te = world.getTileEntity(hit.getBlockPos());
			if (te instanceof TileEntityGravitationalAnomaly) {
				suppressWithPower((TileEntityGravitationalAnomaly) te);
			} else {
				hit = null;
			}
		}
	}

	private void suppressWithPower(TileEntityGravitationalAnomaly anomaly) {
		int drain = getDrainForCurrentStress();
		int stored = energyStorage.getEnergyStored();
		if (stored >= drain) {
			energyStorage.modifyEnergyStored(-drain);
			anomaly.suppress(new AnomalySuppressor(getPos(), 20, 0.7f));
		} else if (stored > 0) {
			// partial power: proportionally less suppression
			float ratio = (float) stored / drain;
			float amount = 0.7f + 0.3f * (1.0f - ratio);
			energyStorage.modifyEnergyStored(-stored);
			anomaly.suppress(new AnomalySuppressor(getPos(), 20, amount));
		}
		// no power → no suppress call → suppressor expires naturally
	}

	private int getDrainForCurrentStress() {
		float stress = getPercentage();
		if (stress < 0) return BASE_ENERGY_PER_TICK;
		return BASE_ENERGY_PER_TICK + (int) ((MAX_ENERGY_PER_TICK - BASE_ENERGY_PER_TICK) * stress);
	}

	public float getPercentage() {
		if (hit != null) {
			TileEntity tile = world.getTileEntity(hit.getBlockPos());
			if (tile instanceof TileEntityGravitationalAnomaly) {
				return Math.max(0, Math
						.min((float) (((TileEntityGravitationalAnomaly) tile).getEventHorizon() - 0.3f) / 2.3f, 1f));
			}
		}
		return -1;
	}

	@SideOnly(Side.CLIENT)
	void spawnParticles(World world) {
		if (hit != null && energyStorage.getEnergyStored() > 0
				&& world.getTileEntity(hit.getBlockPos()) instanceof TileEntityGravitationalAnomaly) {
			if (random.nextFloat() < 0.3f) {

				float r = (float) getParticleColorR();
				float g = (float) getParticleColorG();
				float b = (float) getParticleColorB();
				EnumFacing up = getAboveSide(getFront().getOpposite()).getOpposite();
				GravitationalStabilizerBeamParticle particle = new GravitationalStabilizerBeamParticle(world,
						new Vector3f(getPos().getX() + 0.5f, getPos().getY() + 0.5f, getPos().getZ() + 0.5f),
						new Vector3f(hit.getBlockPos().getX() + 0.5f, hit.getBlockPos().getY() + 0.5f,
								hit.getBlockPos().getZ() + 0.5f),
						new Vector3f(up.getXOffset(), up.getYOffset(), up.getZOffset()), 1f, 0.3f, 80);
				particle.setColor(r, g, b, 1);
				ClientProxy.renderHandler.getRenderParticlesHandler().addEffect(particle,
						RenderParticlesHandler.Blending.Additive);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 4086 * 2;
	}

	@Nonnull
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		AxisAlignedBB bb = Block.FULL_BLOCK_AABB.offset(getPos());
		if (hit != null) {
			return bb.expand(hit.getBlockPos().getX() - getPos().getX(), hit.getBlockPos().getY() - getPos().getY(),
					hit.getBlockPos().getZ() - getPos().getZ());
		}
		return bb;
	}

	@Override
	public SoundEvent getSound() {
		return MatterOverdriveSounds.forceField;
	}

	@Override
	public boolean hasSound() {
		return true;
	}

	@Override
	public boolean getServerActive() {
		return hit != null;
	}

	@Override
	public float soundVolume() {
		if (getUpgradeMultiply(UpgradeTypes.Muffler) >= 2d) {
			return 0.0f;
		}

		return getPercentage() * 0.5f;
	}

	public double getBeamColorR() {
		float percent = getPercentage();
		if (percent == -1)
			return color3.getRed();
		return (color2.getRed() * percent + color1.getRed() * (1 - percent)) / 255;
	}

	public double getBeamColorG() {
		float percent = getPercentage();
		if (percent == -1)
			return color3.getGreen();
		return (color2.getGreen() * percent + color1.getGreen() * (1 - percent)) / 255;
	}

	public double getBeamColorB() {
		float percent = getPercentage();
		if (percent == -1)
			return color3.getBlue();
		return (color2.getBlue() * percent + color1.getBlue() * (1 - percent)) / 255;
	}

	public double getParticleColorR() {
		return getBeamColorR();
	}

	public double getParticleColorG() {
		return getBeamColorG();
	}

	public double getParticleColorB() {
		return getBeamColorB();
	}

	public RayTraceResult getHit() {
		return hit;
	}

	@Override
	public boolean shouldRenderInPass(int pass) {
		return pass == 1;
	}

	@Override
	public boolean isAffectedByUpgrade(UpgradeTypes type) {
		return type == UpgradeTypes.Muffler;
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		return new int[0];
	}

	public void clearTarget() {
		hit = null;
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, EnumSet<MachineNBTCategory> categories, boolean toDisk) {
		super.writeCustomNBT(nbt, categories, toDisk);
		if (categories.contains(MachineNBTCategory.DATA)) {
			nbt.setLong("targetPos", hit != null ? hit.getBlockPos().toLong() : Long.MIN_VALUE);
		}
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, EnumSet<MachineNBTCategory> categories) {
		super.readCustomNBT(nbt, categories);
		if (categories.contains(MachineNBTCategory.DATA) && nbt.hasKey("targetPos")) {
			long encoded = nbt.getLong("targetPos");
			if (encoded != Long.MIN_VALUE && world != null) {
				BlockPos targetPos = BlockPos.fromLong(encoded);
				EnumFacing front = getFront();
				hit = new RayTraceResult(
						new Vec3d(targetPos).subtract(
								Math.abs(front.getDirectionVec().getX() * 0.5),
								Math.abs(front.getDirectionVec().getY() * 0.5),
								Math.abs(front.getDirectionVec().getZ() * 0.5)),
						front.getOpposite(), targetPos);
			} else {
				hit = null;
			}
		}
	}
}