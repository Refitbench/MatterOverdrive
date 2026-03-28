package matteroverdrive.handler;

import matteroverdrive.tile.TileEntityGravitationalAnomaly;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

import java.util.List;

public class AnomalyChunkLoader implements ForgeChunkManager.OrderedLoadingCallback {

    public static final AnomalyChunkLoader INSTANCE = new AnomalyChunkLoader();

    private AnomalyChunkLoader() {}

    public static void init(Object mod) {
        ForgeChunkManager.setForcedChunkLoadingCallback(mod, INSTANCE);
    }

    @Override
    public List<Ticket> ticketsLoaded(List<Ticket> tickets, World world, int maxTicketCount) {
        // Keep all of our tickets; ForgeChunkManager will honour maxTicketCount
        return tickets;
    }

    @Override
    public void ticketsLoaded(List<Ticket> tickets, World world) {
        for (Ticket ticket : tickets) {
            if (ticket.getModData().hasKey("AnomalyPos")) {
                BlockPos pos = BlockPos.fromLong(ticket.getModData().getLong("AnomalyPos"));
                TileEntity te = world.getTileEntity(pos);
                if (te instanceof TileEntityGravitationalAnomaly) {
                    ((TileEntityGravitationalAnomaly) te).onTicketRestored(ticket, world);
                } else {
                    ForgeChunkManager.releaseTicket(ticket);
                }
            } else {
                ForgeChunkManager.releaseTicket(ticket);
            }
        }
    }
}
