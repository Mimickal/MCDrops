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
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;

public class ManualDropCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final LiteralArgumentBuilder<CommandSourceStack> DROP_CMD =
        Commands.literal("drop")
            .requires(req -> req.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .executes(command -> {
                String invokerName;
                try {
                    invokerName = command.getSource().getPlayerOrException().getScoreboardName();
                } catch (CommandSyntaxException e) {
                    invokerName = "Server";
                }

                LOGGER.info("{} used drop command", invokerName);
                DropTickEvent.dropItems();
                return Command.SINGLE_SUCCESS;
            });

    @SubscribeEvent
    public static void register(final RegisterCommandsEvent event) {
        event.getDispatcher().register(DROP_CMD);
    }
}
