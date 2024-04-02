/****************************************************************************************
 * This file is part of MCDrops, a Minecraft mod that drops items for players.
 * Copyright (C) 2014 Mimickal (Mia Moretti).
 *
 * MCDrops is free software under the GNU Affero General Public License v3.0.
 * See LICENSE or <https://www.gnu.org/licenses/agpl-3.0.en.html> for more information.
 ****************************************************************************************/
package mimickal.minecraft.mcdrops;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

public class DropTable {
    private static final ItemStack DEFAULT_DROP = new ItemStack(Blocks.SPONGE);
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Random RNG = new Random();

    private static List<Drop> drops = new ArrayList<>();
    private static int totalWeight = 0;

    /** Recalculates the drop table from {@link Config}. */
    public static void setDrops(List<Config.DropEntry> dropEntries) {
        totalWeight = dropEntries
            .stream()
            .mapToInt(Config.DropEntry::weight)
            .sum();

        drops = dropEntries
            .stream()
            .map(Drop::new)
            .toList();

        LOGGER.info("Loaded drop table {}", drops);
    }

    /** Pick a random drop from the config. */
    public static ItemStack nextDrop() {
        if (drops.isEmpty() || totalWeight == 0) {
            return DEFAULT_DROP;
        }

        // Sum drop weights in order until we find the weight interval this roll falls between.
        // Making it start at 1 ensures we skip drops with a weight of 0.
        int roll = RNG.nextInt(1, totalWeight);
        int curWeightSum = 0;

        for (Drop drop : drops) {
            if (curWeightSum <= roll && roll < curWeightSum + drop.weight) {
                return drop.getRandomStack();
            } else {
                curWeightSum += drop.weight;
            }
        }

        // Buy a lottery ticket if this ever happens
        return DEFAULT_DROP;
    }

    /** Create an {@link ItemStack} based on the given tag. */
    @Nullable
    public static ItemStack itemStackFromTag(String tagName) {
        // We don't know if this will be an item or a block, so we need to check both.
        ResourceLocation location = new ResourceLocation(tagName);
        return Optional.ofNullable(ObjectUtils.firstNonNull(
            ForgeRegistries.ITEMS.getValue(location),

            // If only we had optional chaining
            Optional.ofNullable(ForgeRegistries.BLOCKS.getValue(location))
                .map(Block::asItem)
                .orElse(null)
        )).map(ItemStack::new).orElse(null);
    }

    /** A single drop in the table. */
    public record Drop (
        @NotNull ItemStack itemStack,
        int weight,
        int min,
        int max
    ) {
        Drop(Config.DropEntry entry) {
            //noinspection DataFlowIssue -- Rules in Config ensure these values are all valid
            this(
                itemStackFromTag(entry.tag()),
                entry.weight(),
                ObjectUtils.firstNonNull(entry.count(), entry.min()),
                ObjectUtils.firstNonNull(entry.count(), entry.max())
            );
        }

        public ItemStack getRandomStack() {
            ItemStack drop = this.itemStack.copy();
            drop.setCount(this.min == this.max
                ? this.min
                : RNG.nextInt(this.min, this.max)
            );
            return drop;
        }

        @Override
        public String toString() {
            return String.format(
                "Drop{item=%s, weight=%d, min=%d, max=%d}",
                this.itemStack.getItem().getRegistryName(), this.weight, this.min, this.max
            );
        }
    }
}
