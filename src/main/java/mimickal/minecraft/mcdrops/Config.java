/****************************************************************************************
 * This file is part of MCDrops, a Minecraft mod that drops items for players.
 * Copyright (C) 2014 Mimickal (Mia Moretti).
 *
 * MCDrops is free software under the GNU Affero General Public License v3.0.
 * See LICENSE or <https://www.gnu.org/licenses/agpl-3.0.en.html> for more information.
 ****************************************************************************************/
package mimickal.minecraft.mcdrops;

import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.google.common.base.Strings;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.List;

public class Config {
    /** Registers the main config, and the events that load the drops table. */
    public static void register() {
        String configPath = Path.of(DropsMod.MOD_ID, CONFIG_NAME).toString();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, CONFIG_SPEC, configPath);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Config::loadDropsTable);
    }

    /**
     * The event that loads (or reloads) the drops table. This needs to happen separately for two reasons:
     * <ol>
     * <li>We need the path to the currently loaded world's config directory.
     * <li>We use a TOML table array for drops, which Forge's default config handler doesn't support.
     */
    public static void loadDropsTable(ModConfigEvent event) {
        Path configPath = event.getConfig().getFullPath().getParent().resolve(TABLE_NAME);
        CommentedFileConfig config = CommentedFileConfig.builder(configPath)
            .writingMode(WritingMode.REPLACE)
            .defaultData(Config.class.getResource("/example_drops.toml"))
            .build();

        config.load();

        drops = new ObjectConverter().toObject(config, DropList::new)
            .drops
            .stream()
            .map(Drop::validate)
            .toList();

        // TODO can we write out corrections validate makes?

        LOGGER.debug("Loaded drop table {}", drops);
        // TODO notify consumers of drops somehow
    }

    public static final IntValue dropInterval;
    public static final BooleanValue variableIntervalEnabled;
    public static final IntValue minDropInterval;
    public static final IntValue maxDropInterval;
    public static List<Drop> drops;

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String CONFIG_NAME = "settings.toml";
    private static final String TABLE_NAME = "drops.toml";
    private static final ForgeConfigSpec CONFIG_SPEC;
    private static final int DEFAULT_DROP_INTERVAL = 15 * 60; // 15 minutes
    private static final boolean DEFAULT_VARIABLE_INTERVAL_ENABLED = false;
    private static final int DEFAULT_MIN_DROP_INTERVAL = 10 * 60; // 10 minutes
    private static final int DEFAULT_MAX_DROP_INTERVAL = 20 * 60; // 20 minutes

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        dropInterval = builder
            .comment("Seconds between drops")
            .defineInRange("drop_interval", DEFAULT_DROP_INTERVAL, 1, Integer.MAX_VALUE);

        variableIntervalEnabled = builder
            .comment("Make drop interval a random range instead of a static value")
            .define("enable_variable_interval", DEFAULT_VARIABLE_INTERVAL_ENABLED);

        minDropInterval = builder
            .comment("Lower end of variable drop interval range. Does nothing if enable_variable_interval is disabled.")
            .defineInRange("min_drop_interval", DEFAULT_MIN_DROP_INTERVAL, 1, Integer.MAX_VALUE);

        maxDropInterval = builder
            .comment("Upper end of variable drop interval range. Does nothing if enable_variable_interval is disabled.")
            .defineInRange("max_drop_interval", DEFAULT_MAX_DROP_INTERVAL, 1, Integer.MAX_VALUE);

        CONFIG_SPEC = builder.build();
    }

    private Config() { /* Prevent instantiation */ }

    /** {@link ObjectConverter} uses this to deserialize the TOML list. */
    private static class DropList {
        private List<Drop> drops;
    }

    /** Represents a single drop table in the array of drop tables */
    public static class Drop {
        // The warnings in here are a little wrong because they're not
        // accounting for the deserializer populating these fields.
        
        // ObjectConverter needs the transient modifier here, otherwise it tries to populate these fields.
        private static transient final int DEFAULT_WEIGHT = 0;
        private static transient final int DEFAULT_COUNT = 1;

        // These values are enforced by validate(). This class is only created by ObjectConverter
        // where we also call validate(), so these tags are correct.
        @NotNull private String tag;
        @NotNull private Integer weight;
        @Nullable private Integer count;
        @Nullable private Integer min;
        @Nullable private Integer max;

        private Drop() { /* Prevent instantiation */ }

        @Override
        public String toString() {
            return String.format(
                "Drop{tag='%s', weight=%d, count=%d, min=%d, max=%d}",
                this.tag, this.weight, this.count, this.min, this.max
            );
        }

        /**
         * Apply default values to bad or unprovided config values.
         * Throw exceptions for bad config values we can't safely recover from.
         */
        Drop validate() {
            if (Strings.isNullOrEmpty(this.tag))
                throw new RuntimeException("Drop tag cannot be empty");

            if (this.weight == null || this.weight <= 0) {
                LOGGER.warn("Drop {} bad weight {}. Changing to {}", this.tag, this.weight, DEFAULT_WEIGHT);
                this.weight = DEFAULT_WEIGHT;
            }

            // Only change range values when count is not set, because count takes precedence over range.
            // Why? Because I say so.
            if (this.count == null) {
                if (this.min == null && this.max == null) {
                    LOGGER.info("Drop {} missing count. Defaulting count={}", this.tag, DEFAULT_COUNT);
                    this.count = DEFAULT_COUNT;
                }

                if (this.min != null && this.min <= 0) {
                    LOGGER.warn("Drop {} bad min {}. Changing min={}", this.tag, this.min, DEFAULT_COUNT);
                    this.min = DEFAULT_COUNT;
                }

                if (this.max != null && this.max <= 0) {
                    LOGGER.warn("Drop {} bad max {}. Changing max={}", this.tag, this.max, DEFAULT_COUNT);
                    this.max = DEFAULT_COUNT;
                }

                // Use sane defaults when missing min or max in a range
                if (this.min != null && this.max == null) {
                    LOGGER.warn("Drop {} min={} without max. Defaulting max={}", this.tag, this.min, this.min);
                    this.max = this.min;
                }

                if (this.min == null && this.max != null) {
                    LOGGER.info("Drop {} missing min. Defaulting min={}", this.tag, DEFAULT_COUNT);
                    this.min = DEFAULT_COUNT;
                }

                if (this.min != null && this.max != null && this.max < this.min) {
                    LOGGER.warn("Drop {} max={} < min={}. Changing max={}", this.tag, this.max, this.min, this.min);
                    this.max = this.min;
                }
            } else {
                if (this.count <= 0) {
                    LOGGER.warn("Drop {} bad count {}. Changing count={}", this.tag, this.count, DEFAULT_COUNT);
                    this.count = DEFAULT_COUNT;
                }

                // Warn when range and count are used together.
                // Don't need to do anything else since we'll just use count anyway.
                if (this.min != null) {
                    LOGGER.warn("Drop {} conflicting count={} and min={}. Using count", this.tag, this.count, this.min);
                }

                if (this.max != null) {
                    LOGGER.warn("Drop {} conflicting count={} and max={}. Using count", this.tag, this.count, this.max);
                }
            }

            return this;
        }
    }
}
