package mimickal.mc.mcdrops;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;

public class DropTickHandler {

    private long ticks = 0;

    @SubscribeEvent
    public void onServerTick(ServerTickEvent event) {
        ticks++;

        // Convert drop minutes to ticks
        if (ticks >= Config.dropInterval * 60 * 20) {
            ticks = 0;
            dropItems();
        }
    }

    /**
     * Selects and drops a random item for all players in the server
     */
    private void dropItems() {
        // Get a list of currently connected player entities.
        // For some reason, there isn't a built-in method for this.
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        PlayerList playerList = server.getPlayerList();

        List<EntityPlayerMP> players = new ArrayList<>();
        for (String name : server.getAllUsernames()) {
            players.add(playerList.getPlayerByUsername(name));
        }

        // TODO drop an item for each player
        // player.dropItem(Items.APPLE, 1);
    }

}
