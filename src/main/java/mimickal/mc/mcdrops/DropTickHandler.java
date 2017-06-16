package mimickal.mc.mcdrops;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

public class DropTickHandler {

    private long ticks = 0;

    @SubscribeEvent
    public void onServerTick(ServerTickEvent event) {
        ticks++;

        // Convert drop minutes to ticks
        if (ticks >= Config.dropInterval * 60 * 20) {
            ticks = 0;
            // TODO Do drops
        }
    }

}
