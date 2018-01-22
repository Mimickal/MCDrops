package mimickal.mc.mcdrops;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class Config {

    private static final String CONFIG_NAME = "config.cfg";
    private static final String CATEGORY = "config";

    private static final int DROP_INTERVAL_DEFAULT = 15;
    private static final boolean VARIABLE_INTERVAL_DEFAULT = false;
    private static final int MIN_DROP_INTERVAL_DEFAULT = 10;
    private static final int MAX_DROP_INTERVAL_DEFAULT = 20;

    public static int dropInterval = DROP_INTERVAL_DEFAULT;
    public static boolean variableInterval = VARIABLE_INTERVAL_DEFAULT;
    public static int minDropInterval = MIN_DROP_INTERVAL_DEFAULT;
    public static int maxDropInterval = MAX_DROP_INTERVAL_DEFAULT;

    public static void load() {
        loadConfig();
    }

    private static void loadConfig() {
        File configFile = new File("config/" + DropsMod.MODID + "/" + CONFIG_NAME);
        Configuration config = new Configuration(configFile);

        config.load();

        dropInterval = config.getInt(
                "drop_interval", CATEGORY,
                DROP_INTERVAL_DEFAULT, 1, Integer.MAX_VALUE,
                "Minutes between drops"
        );

        variableInterval = config.getBoolean(
                "variable_interval", CATEGORY, VARIABLE_INTERVAL_DEFAULT,
                "Variable drop intervals are not used unless this is set to true"
        );

        minDropInterval = config.getInt(
                "min_drop_interval", CATEGORY,
                MIN_DROP_INTERVAL_DEFAULT, 1, Integer.MAX_VALUE,
                "Minimum amount of time to wait to drop items."
        );

        maxDropInterval = config.getInt(
                "max_drop_interval", CATEGORY,
                MAX_DROP_INTERVAL_DEFAULT, 1, Integer.MAX_VALUE,
                "Maximum amount of time to wait to drop items."
        );

        // Catch when users mix these up
        if (minDropInterval > maxDropInterval) {
            int tmp = maxDropInterval;
            maxDropInterval = minDropInterval;
            minDropInterval = tmp;
        }

        config.save();
    }

}
