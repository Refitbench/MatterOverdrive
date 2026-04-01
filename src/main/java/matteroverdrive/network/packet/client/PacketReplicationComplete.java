
package matteroverdrive.network.packet.client;

import io.netty.buffer.ByteBuf;
import matteroverdrive.machines.replicator.TileEntityMachineReplicator;
import matteroverdrive.network.packet.TileEntityUpdatePacket;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketReplicationComplete extends TileEntityUpdatePacket {
	private ItemStack replicatedItem = ItemStack.EMPTY;

	public PacketReplicationComplete() {
		super();
	}

	public PacketReplicationComplete(TileEntity entity, ItemStack replicatedItem) {
		super(entity);
		this.replicatedItem = replicatedItem;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		replicatedItem = ByteBufUtils.readItemStack(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		ByteBufUtils.writeItemStack(buf, replicatedItem);
	}

	public static class ClientHandler extends AbstractClientPacketHandler<PacketReplicationComplete> {
		@SideOnly(Side.CLIENT)
		@Override
		public void handleClientMessage(EntityPlayerSP player, PacketReplicationComplete message, MessageContext ctx) {
			TileEntity entity = message.getTileEntity(player.world);
			if (entity instanceof TileEntityMachineReplicator) {
				((TileEntityMachineReplicator) entity).beginSpawnParticles(message.replicatedItem);
			}
		}
	}
}
