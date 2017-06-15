package mimickal.mc.mcdrops;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class Config {

    private static final String CONFIG_NAME = "config.cfg";
    private static final String CATEGORY = "config";

    private static final int DROP_INTERVAL_DEFAULT = 15;

    public static int dropInterval = DROP_INTERVAL_DEFAULT;

    public static void load() {
        loadConfig();
    }

    private static void loadConfig() {
        File configFile = new File("config/" + DropsMod.MODID + "/" + CONFIG_NAME);
        Configuration config = new Configuration(configFile);

        config.load();

        dropInterval = config.getInt(
                "drop_interval", CATEGORY, DROP_INTERVAL_DEFAULT,
                0, Integer.MAX_VALUE, "Minutes between drops"
        );

        config.save();
    }

}
