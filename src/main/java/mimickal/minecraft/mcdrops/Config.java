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
import com.electronwill.nightconfig.core.io.WritingException;
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
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class Config {
    /** Registers the main config, and the events that load the drops table. */
    public static void register() {
        String configPath = Path.of(DropsMod.MOD_ID, CONFIG_NAME).toString();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, CONFIG_SPEC, configPath);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Config::loadDropsTable);
    }

    /**
     * The event that loads the drops table. This needs to happen separately for two reasons:
     * <ol>
     * <li>We need the path to the currently loaded world's config directory.
     * <li>We use a TOML table array for drops, which Forge's default config handler doesn't support.
     */
    public static void loadDropsTable(final ModConfigEvent event) {
        Path configPath = event.getConfig().getFullPath().getParent().resolve(TABLE_NAME);

        try (CommentedFileConfig config = CommentedFileConfig.builder(configPath)
            .writingMode(WritingMode.REPLACE)
            .defaultData(Config.class.getResource("/example_drops.toml"))
            .build()
        ) {
            config.load();

            DropTable.setDrops(new ObjectConverter().toObject(config, DropList::new)
                .drops
                .stream()
                .map(Config::applyRules)
                .filter(Objects::nonNull)
                .toList()
            );
        } catch (WritingException e) {
            LOGGER.warn("Skipping config load after error. This is expected on first world creation.", e);
        }

        // TODO can we write out corrections the rules make?
    }

    public static final IntValue dropInterval;
    public static final BooleanValue variableIntervalEnabled;
    public static final IntValue minDropInterval;
    public static final IntValue maxDropInterval;

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String CONFIG_NAME = "settings.toml";
    private static final String TABLE_NAME = "drops.toml";
    private static final ForgeConfigSpec CONFIG_SPEC;
    private static final int DEFAULT_DROP_COUNT = 1;
    private static final int DEFAULT_DROP_WEIGHT = 0;
    private static final int DEFAULT_DROP_INTERVAL = 15 * 60; // 15 minutes
    private static final int DEFAULT_DROP_INTERVAL_MIN = 10 * 60; // 10 minutes
    private static final int DEFAULT_DROP_INTERVAL_MAX = 20 * 60; // 20 minutes
    private static final boolean DEFAULT_VARIABLE_INTERVAL_ENABLED = false;

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
            .defineInRange("min_drop_interval", DEFAULT_DROP_INTERVAL_MIN, 1, Integer.MAX_VALUE);

        maxDropInterval = builder
            .comment("Upper end of variable drop interval range. Does nothing if enable_variable_interval is disabled.")
            .defineInRange("max_drop_interval", DEFAULT_DROP_INTERVAL_MAX, 1, Integer.MAX_VALUE);

        CONFIG_SPEC = builder.build();
    }

    private Config() { /* Prevent instantiation */ }

    /** {@link ObjectConverter} uses this to deserialize the TOML list. */
    private static class DropList {
        // ObjectConverter writes this field during deserialization.
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        private List<DropEntry> drops;
    }

    /** Represents a single table in the TOML table array. */
    public static class DropEntry {
        // ObjectConverter writes this field during deserialization.
        @SuppressWarnings("NotNullFieldNotInitialized")
        @NotNull private String tag;

        // ObjectConverter writes this field during deserialization.
        @SuppressWarnings("NotNullFieldNotInitialized")
        @NotNull private Integer weight;

        @Nullable private Integer count;
        @Nullable private Integer min;
        @Nullable private Integer max;

        private DropEntry() { /* Prevent instantiation */ }

        public String tag() {
            return this.tag;
        }

        public Integer weight() {
            return this.weight;
        }

        public DropEntry setWeight(Integer newWeight) {
            this.weight = newWeight;
            return this;
        }

        public Integer count() {
            return this.count;
        }

        public DropEntry setCount(Integer newCount) {
            this.count = newCount;
            return this;
        }

        public Integer min() {
            return this.min;
        }

        public DropEntry setMin(Integer newMin) {
            this.min = newMin;
            return this;
        }

        public Integer max() {
            return this.max;
        }

        public DropEntry setMax(Integer newMax) {
            this.max = newMax;
            return this;
        }
    }

    /**
     * Contains a rule that all {@link DropEntry} need to follow.
     * @param test Function that tests whether a {@link DropEntry} is correct.
     * @param log Function that prints a warning to the log when {@link #test} fails.
     * @param mutate Function that modifies the entry when {@link #test} fails.
     *               Returning {@code null} implies we can't automatically fix the {@link DropEntry} and should not use it.
     */
    private record DropRule(
        Function<DropEntry, Boolean> test,
        Consumer<DropEntry> log,
        Function<DropEntry, @Nullable DropEntry> mutate
    ) {}

    /** Applies all the rules to the given entry, logging if any entry fails a rule. See {@link #RULES}. */
    private static DropEntry applyRules(DropEntry entry) {
        for (DropRule rule : RULES) {
            if (rule.test.apply(entry)) {
                rule.log.accept(entry);
                entry = rule.mutate.apply(entry);

                if (entry == null) {
                    return null;
                }
            }
        }
        return entry;
    }

    /**
     * Rules all {@link DropEntry}s need to follow.<br>
     * If you think this is bad, you should see the conditional chain it's replacing.
     */
    private static final List<DropRule> RULES = List.of(
        new DropRule(
            entry -> Strings.isNullOrEmpty(entry.tag),
            entry -> LOGGER.warn("Ignoring drop with missing tag"),
            entry -> null
        ),
        new DropRule(
            entry -> DropTable.itemStackFromTag(entry.tag) == null,
            (entry -> LOGGER.warn("Cannot find item with tag {}", entry.tag)),
            entry -> null
        ),
        new DropRule(
            entry -> entry.weight == null || entry.weight <= 0,
            entry -> LOGGER.warn("Drop {} bad weight {}. Changing to {}", entry.tag, entry.weight, DEFAULT_DROP_WEIGHT),
            entry -> entry.setWeight(DEFAULT_DROP_WEIGHT)
        ),
        new DropRule(
            entry -> ObjectUtils.allNotNull(entry.count, entry.min),
            entry -> LOGGER.warn("Drop {} conflicting count={} and min={}. Using count", entry.tag, entry.count, entry.min),
            entry -> entry
        ),
        new DropRule(
            entry -> ObjectUtils.allNotNull(entry.count, entry.max),
            entry -> LOGGER.warn("Drop {} conflicting count={} and max={}. Using count", entry.tag, entry.count, entry.max),
            entry -> entry
        ),
        new DropRule(
            entry -> ObjectUtils.allNull(entry.count, entry.min, entry.max),
            entry -> LOGGER.info("Drop {} missing count. Defaulting count={}", entry.tag, DEFAULT_DROP_COUNT),
            entry -> entry.setCount(DEFAULT_DROP_COUNT)
        ),
        new DropRule(
            entry -> definedButBad(entry.count),
            entry -> LOGGER.warn("Drop {} bad count {}. Changing count={}", entry.tag, entry.count, DEFAULT_DROP_COUNT),
            entry -> entry.setCount(DEFAULT_DROP_COUNT)
        ),
        new DropRule(
            entry -> entry.count == null && definedButBad(entry.min),
            entry -> LOGGER.warn("Drop {} bad min {}. Changing min={}", entry.tag, entry.min, DEFAULT_DROP_COUNT),
            entry -> entry.setMin(DEFAULT_DROP_COUNT)
        ),
        new DropRule(
            entry -> entry.count == null && definedButBad(entry.max),
            entry -> LOGGER.warn("Drop {} bad max {}. Changing max={}", entry.tag, entry.max, DEFAULT_DROP_COUNT),
            entry -> entry.setMax(DEFAULT_DROP_COUNT)
        ),
        new DropRule(
            entry -> entry.count == null && entry.max != null && entry.min == null,
            entry -> LOGGER.warn("Drop {} max={} without min. Defaulting min={}", entry.tag, entry.max, DEFAULT_DROP_COUNT),
            entry -> entry.setMin(DEFAULT_DROP_COUNT)
        ),
        new DropRule(
            entry -> entry.count == null && entry.max == null && entry.min != null,
            entry -> LOGGER.warn("Drop {} min={} without max. Defaulting max={}", entry.tag, entry.min, entry.min),
            entry -> entry.setMax(entry.min)
        ),
        new DropRule(
            entry -> entry.count == null && entry.min != null && entry.max != null && entry.max < entry.min,
            entry -> LOGGER.warn("Drop {} max={} < min={}. Changing max={}", entry.tag, entry.max, entry.min, entry.min),
            entry -> entry.setMax(entry.min)
        )
    );

    private static boolean definedButBad(Integer value) {
        return value != null && value <= 0;
    }
}
