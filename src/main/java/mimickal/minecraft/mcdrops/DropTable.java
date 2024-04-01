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
    public static void recalculate() {
        totalWeight = Config.drops
            .stream()
            .mapToInt(Config.Drop::getWeight)
            .sum();

        drops = Config.drops
            .stream()
            .map(Drop::fromConfigDrop)
            .filter(Objects::nonNull)
            .filter(drop -> drop.itemStack != null)
            .toList();
    }

    /** Pick a random drop from the channel. */
    public static ItemStack nextDrop() {
        if (drops.isEmpty()) {
            return DEFAULT_DROP;
        }

        // Sum drop weights in order until we find the weight interval this roll falls between
        int roll = RNG.nextInt(totalWeight);
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
    private static ItemStack itemStackFromTag(String tagName) {
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
    public static class Drop {

        @Nullable
        private static Drop fromConfigDrop(Config.Drop drop) {
            ItemStack itemStack = itemStackFromTag(drop.getTag());
            if (itemStack == null) {
                LOGGER.warn("Skipping drop for unknown tag {}", drop.getTag());
                return null;
            }

            return new Drop(
                itemStackFromTag(drop.getTag()),
                drop.getWeight(),
                // This NullPointerException warning is a lie. We verify one of these is defined in Config.
                Optional.ofNullable(drop.getCount()).orElse(drop.getMin()),
                Optional.ofNullable(drop.getCount()).orElse(drop.getMax())
            );
        }

        private final int min;
        private final int max;
        private final int weight;
        private final ItemStack itemStack;

        private Drop(ItemStack itemStack, int weight, int min, int max) {
            this.itemStack = itemStack;
            this.weight = weight;
            this.min = min;
            this.max = max;
        }

        public ItemStack getRandomStack() {
            ItemStack drop = this.itemStack.copy();
            // FIXME this is terrible
            drop.setCount(this.min == this.max ? this.min : RNG.nextInt(this.min, this.max));
            return drop;
        }
    }
}
