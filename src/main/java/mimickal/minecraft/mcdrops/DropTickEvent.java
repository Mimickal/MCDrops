/****************************************************************************************
 * This file is part of MCDrops, a Minecraft mod that drops items for players.
 * Copyright (C) 2014 Mimickal (Mia Moretti).
 *
 * MCDrops is free software under the GNU Affero General Public License v3.0.
 * See LICENSE or <https://www.gnu.org/licenses/agpl-3.0.en.html> for more information.
 ****************************************************************************************/
package mimickal.minecraft.mcdrops;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;

public class DropTickEvent {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Random RNG = new Random();

    private static MinecraftServer currentServer;
    private static Instant nextDropTime;

    /** Periodically drops items for all players on the server. */
    @SubscribeEvent
    public static void periodicallyDropItems(final TickEvent.ServerTickEvent event) {
        if (event.side.isClient()) return;
        if (event.phase == TickEvent.Phase.END) return;

        // Recalculate the drop timer when a new world is loaded (and thus, world-specific config changes).
        if (currentServer != ServerLifecycleHooks.getCurrentServer()) {
            currentServer = ServerLifecycleHooks.getCurrentServer();
            nextDropTime = getNextWaitInstant();
        }

        if (Instant.now().isBefore(nextDropTime)) return;

        nextDropTime = getNextWaitInstant();
        dropItems();
    }

    /** Calculates the instant for the next drop. */
    private static Instant getNextWaitInstant() {
        return Instant.now().plus(
            Config.variableIntervalEnabled.get()
                ? RNG.nextInt(Config.minDropInterval.get(), Config.maxDropInterval.get())
                : Config.dropInterval.get(),
            ChronoUnit.SECONDS
        );
    }

    /** Selects and drops a random item for all players in the server. */
    public static void dropItems() {
        dropItemsFor(ServerLifecycleHooks
            .getCurrentServer()
            .getPlayerList()
            .getPlayers()
        );
    }

    /** Selects and drops a random item for all players in the given list. */
    public static void dropItemsFor(List<ServerPlayer> players) {
        players.stream()
            .filter(LivingEntity::isAlive)
            .forEach(player -> {
                ItemStack drop = DropTable.nextDrop();
                player.drop(drop, false); // Not sure why "false", but we need it for this to work.
                LOGGER.debug("Dropped {} {} {}", player.getScoreboardName(), drop.getCount(), drop.getItem().getRegistryName());
            });
    }
}
