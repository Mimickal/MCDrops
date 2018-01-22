package mimickal.mc.mcdrops;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

import java.util.Random;

public class DropTickHandler {

    private static final int MIN_TO_TICK = 60 * 20; // 20 ticks/second * 60 seconds

    private long ticks = 0;
    private long waitTime = getWaitTime();
    private Random rng = new Random();

    @SubscribeEvent
    public void onServerTick(ServerTickEvent event) {
        ticks++;

        // Convert drop minutes to ticks
        if (ticks >= waitTime) {
            ticks = 0;
            waitTime = getWaitTime();
            dropItems();
        }
    }

    private int getWaitTime() {
        if (Config.variableInterval) {
            return (rng.nextInt(Config.maxDropInterval) - Config.minDropInterval) * MIN_TO_TICK;
        } else {
            return Config.dropInterval * MIN_TO_TICK;
        }
    }

    /**
     * Selects and drops a random item for all players in the server
     */
    public static void dropItems() {
        // Get a list of currently connected player entities.
        // For some reason, there isn't a built-in method for this.
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        PlayerList playerList = server.getPlayerList();

        // Roll and drop a random item for each player
        for (String name : server.getAllUsernames()) {
            EntityPlayerMP player = playerList.getPlayerByUsername(name);
            ItemStack dropItem = DropTable.nextDrop();

            if (dropItem != null) {
                // The boolean value is unused. I have no idea why we need to provide it.
                player.dropItem(dropItem, false);
            }
        }
    }

}
