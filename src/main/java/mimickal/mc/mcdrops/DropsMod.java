package mimickal.mc.mcdrops;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(
        modid = DropsMod.MODID,
        name = DropsMod.NAME,
        version = DropsMod.VERSION,
        acceptedMinecraftVersions = "[1.10.2]",
        acceptableRemoteVersions = "*" // Allows mod to be client optional
)
public class DropsMod {

    public static final String MODID = "mcdrops";
    public static final String NAME = "MCDrops";
    public static final String VERSION = "1.10.2-1.0.0";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    @EventHandler
    public void init(FMLInitializationEvent event) {
        LOGGER.info("Loading Drops");
        Config.load();
        DropTable.loadDropTable();
        MinecraftForge.EVENT_BUS.register(new DropTickHandler());
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new ReloadCommand());
    }
}
