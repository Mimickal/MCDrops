/****************************************************************************************
 * This file is part of MCDrops, a Minecraft mod that drops items for players.
 * Copyright (C) 2014 Mimickal (Mia Moretti).
 *
 * MCDrops is free software under the GNU Affero General Public License v3.0.
 * See LICENSE or <https://www.gnu.org/licenses/agpl-3.0.en.html> for more information.
 ****************************************************************************************/
package mimickal.minecraft.mcdrops;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DropCommand {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ARG_PLAYERS = "player";

    /** Subcommand: {@code /drop reload} */
//    private static final LiteralArgumentBuilder<CommandSourceStack> SUBCMD_RELOAD =
//        Commands.literal("reload")
//            .executes(DropCommand::reloadDropConfig);

    /** Subcommand: {@code /drop for <name>} */
    private static final LiteralArgumentBuilder<CommandSourceStack> SUBCMD_DROP_FOR =
        Commands.literal("for")
            .then(Commands.argument(ARG_PLAYERS, EntityArgument.players())
                .executes(DropCommand::manualDropFor)
            );

    /** Top-level command: {@code /drop} */
    private static final LiteralArgumentBuilder<CommandSourceStack> CMD_DROP =
        Commands.literal("drop")
            .requires(req -> req.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(SUBCMD_DROP_FOR)
            .executes(DropCommand::manualDrop);

    @SubscribeEvent
    public static void register(final RegisterCommandsEvent event) {
        event.getDispatcher().register(CMD_DROP);
    }

    /** Immediately triggers the drop routine. Does not interrupt or change existing drop timer. */
    private static int manualDrop(CommandContext<CommandSourceStack> context) {
        LOGGER.info("{} used drop command", getInvoker(context));
        sendMsg(context, "Dropped items for all players");
        DropTickEvent.dropItems();
        return Command.SINGLE_SUCCESS;
    }

    /** Runs drop routine for a single player. */
    private static int manualDropFor(CommandContext<CommandSourceStack> context) {
        LOGGER.info("{} used drop command", getInvoker(context));
        List<ServerPlayer> players;
        try {
            players = EntityArgument.getEntities(context, ARG_PLAYERS)
                .stream()
                .map(entity -> (ServerPlayer) entity)
                .toList();
        } catch (CommandSyntaxException e) {
            sendMsg(context, "Player not found!");
            return 0;
        }

        DropTickEvent.dropItemsFor(players);
        sendMsg(context, "Dropped items for", players.get(0).getScoreboardName());
        return Command.SINGLE_SUCCESS;
    }

    /** Reloads the drop configuration from file. */
//    private static int reloadDropConfig(CommandContext<CommandSourceStack> context) {
//        LOGGER.info("{} reloaded drop config", getInvoker(context));
//        sendMsg(context, "Reloaded drop config");
//        return Command.SINGLE_SUCCESS;
//    }

    private static String getInvoker(CommandContext<CommandSourceStack> context) {
        try {
            return context.getSource().getPlayerOrException().getScoreboardName();
        } catch (CommandSyntaxException e) {
            return "Server";
        }
    }

    /** Prints a message to the screen in response to a command. */
    private static void sendMsg(CommandContext<CommandSourceStack> context, Object... parts) {
        String message = Arrays.stream(parts).map(Object::toString).collect(Collectors.joining(" "));
        context.getSource().sendSuccess(new TextComponent(message), false);
    }
}
