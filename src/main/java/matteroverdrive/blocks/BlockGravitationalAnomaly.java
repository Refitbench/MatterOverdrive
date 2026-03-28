
package matteroverdrive.blocks;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import matteroverdrive.api.IScannable;
import matteroverdrive.blocks.includes.MOBlockContainer;
import matteroverdrive.data.world.GenPositionWorldData;
import matteroverdrive.data.world.WorldPosition2D;
import matteroverdrive.handler.ConfigurationHandler;
import matteroverdrive.tile.TileEntityGravitationalAnomaly;
import matteroverdrive.util.IConfigSubscriber;
import matteroverdrive.world.MOWorldGen;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockGravitationalAnomaly extends MOBlockContainer<TileEntityGravitationalAnomaly>
		implements IScannable, IConfigSubscriber {
	public BlockGravitationalAnomaly(Material material, String name) {
		super(material, name);
		setBoundingBox(new AxisAlignedBB(0.3f, 0.3f, 0.3f, 0.6f, 0.6f, 0.6f));
		setBlockUnbreakable();
		setResistance(6000000.0F);
		disableStats();
	}

	@Override
	@Deprecated
	public boolean isNormalCube(IBlockState blockState) {
		return false;
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
	  if(placer instanceof EntityPlayer) {
			GenPositionWorldData data = MOWorldGen.getWorldPositionData(world);
			data.addPosition("gravitational_anomaly", new WorldPosition2D(pos.getX(), pos.getZ()));
	  }
	}

	@Override
	@Deprecated
	public RayTraceResult collisionRayTrace(IBlockState state, @Nonnull World world, @Nonnull BlockPos pos,
			@Nonnull Vec3d start, @Nonnull Vec3d end) {
		return super.collisionRayTrace(state, world, pos, start, end);
	}

	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
		return true;
	}

	@Nonnull
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		TileEntityGravitationalAnomaly tileEntity = getTileEntity(source, pos);
		if (tileEntity != null) {
			double range = tileEntity.getEventHorizon();
			range = Math.max(range, 0.4);
			float rangeMin = (float) (0.5 - (range / 2));
			float rangeMax = (float) (0.5 + (range / 2));
			return new AxisAlignedBB(rangeMin, rangeMin, rangeMin, rangeMax, rangeMax, rangeMax);
		}
		return super.getBoundingBox(state, source, pos);
	}

	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		return Block.NULL_AABB;
	}

	@Override
	@Deprecated
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	@Deprecated
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public Class<TileEntityGravitationalAnomaly> getTileEntityClass() {
		return TileEntityGravitationalAnomaly.class;
	}

	@Nonnull
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TileEntityGravitationalAnomaly();
	}

	@Override
	public void addInfo(World world, double x, double y, double z, List<String> infos) {
		TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

		if (tileEntity instanceof TileEntityGravitationalAnomaly) {
			((TileEntityGravitationalAnomaly) tileEntity).addInfo(world, x, y, z, infos);
		}
	}

	@Override
	public void onScan(World world, double x, double y, double z, EntityPlayer player, ItemStack scanner) {

	}

	@Override
	public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
		return false;
	}

	@Nonnull
	@Override
	@Deprecated
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	public void onConfigChanged(ConfigurationHandler config) {
		TileEntityGravitationalAnomaly.BLOCK_ENTETIES = config.getBool(
				ConfigurationHandler.KEY_GRAVITATIONAL_ANOMALY_BLOCK_ENTITIES,
				ConfigurationHandler.CATEGORY_SERVER + "." + getTranslationKey().substring(5), true,
				"Should the blocks drop entities or be directly consumed when destroyed by the gravitational anomaly");
		TileEntityGravitationalAnomaly.FALLING_BLOCKS = config.getBool(
				ConfigurationHandler.KEY_GRAVITATIONAL_ANOMALY_FALLING_BLOCKS,
				ConfigurationHandler.CATEGORY_SERVER + "." + getTranslationKey().substring(5), true,
				"Should blocks be turned into falling blocks when broken");
		TileEntityGravitationalAnomaly.VANILLA_FLUIDS = config.getBool(
				ConfigurationHandler.KEY_GRAVITATIONAL_ANOMALY_VANILLA_FLUIDS,
				ConfigurationHandler.CATEGORY_SERVER + "." + getTranslationKey().substring(5), true,
				"Should vanilla fluid block such as water and lava be consumed by the anomaly");
		TileEntityGravitationalAnomaly.FORGE_FLUIDS = config.getBool(
				ConfigurationHandler.KEY_GRAVITATIONAL_ANOMALY_FORGE_FLUIDS,
				ConfigurationHandler.CATEGORY_SERVER + "." + getTranslationKey().substring(5), true,
				"Should other mod fluid blocks be consumed by the anomaly");
		TileEntityGravitationalAnomaly.BLOCK_DESTRUCTION = config.getBool("block destruction",
				ConfigurationHandler.CATEGORY_SERVER + "." + getTranslationKey().substring(5), true,
				"Should the gravitational anomaly destroy blocks");
		TileEntityGravitationalAnomaly.GRAVITATION = config.getBool("gravitational pull",
				ConfigurationHandler.CATEGORY_SERVER + "." + getTranslationKey().substring(5), true,
				"Should the gravitational entity pull entities towards it");
		TileEntityGravitationalAnomaly.SOUND = config.getBool("gravitational anomaly souund",
				ConfigurationHandler.CATEGORY_CLIENT, true,
				"Should the gravitational anomaly have sound?");
		String cat = ConfigurationHandler.CATEGORY_SERVER + "." + getTranslationKey().substring(5);
		TileEntityGravitationalAnomaly.BLOCK_SCAN_SLICES = config.getInt(
				ConfigurationHandler.KEY_GRAVITATIONAL_ANOMALY_BLOCK_SCAN_SLICES, cat, 8,
				"Divides the block scan sphere into N vertical slices, each processed on a separate scan cycle. "
				+ "Higher values spread CPU cost across more ticks, reducing lag spikes at the cost of slower "
				+ "destruction updates. 1 = full sphere scanned per cycle (default). Recommended range: 1-8.");
		TileEntityGravitationalAnomaly.BLOCK_SCAN_INTERVAL_TICKS = config.getInt(
				ConfigurationHandler.KEY_GRAVITATIONAL_ANOMALY_SCAN_INTERVAL_TICKS, cat, 10,
				"How often (in server ticks) the anomaly runs a block destruction scan cycle. "
				+ "Default is 20 (once per second). Increase to reduce server CPU usage at the expense of "
				+ "slower block consumption. Combine with block scan slices for maximum performance gain.");
		TileEntityGravitationalAnomaly.ENTITY_GRAVITATION_INTERVAL = config.getInt(
				ConfigurationHandler.KEY_GRAVITATIONAL_ANOMALY_ENTITY_GRAV_INTERVAL, cat, 2,
				"How often (in server ticks) the anomaly applies gravitational pull to nearby entities. "
				+ "Default is 2 (every other tick). Higher values reduce server load but make pull physics "
				+ "less smooth. Minimum effective value: 1.");
		TileEntityGravitationalAnomaly.SCAN_Y_MODE = config.getInt(
				ConfigurationHandler.KEY_GRAVITATIONAL_ANOMALY_SCAN_Y_MODE, cat, 0,
				"Controls the vertical scan depth for block destruction. "
				+ "0 = FULL: scan the entire spherical range on all axes (default). "
				+ "1 = LIMITED: clamp vertical scan to half the break range above and below, reducing underground scanning. "
				+ "2 = SURFACE: only scan within 8 blocks above and below the anomaly's Y position.");
		TileEntityGravitationalAnomaly.FORCE_LOAD_ENABLED = config.getBool(
				ConfigurationHandler.KEY_GRAVITATIONAL_ANOMALY_FORCE_LOAD_ENABLED, cat, true,
				"When true, the anomaly force-loads all chunks within its break range using Forge's chunk loader. "
				+ "This prevents synchronous chunk loads during block scanning which can cause lag spikes. "
				+ "Set to false to disable chunk force-loading entirely if it causes issues on your server.");
	}
}
