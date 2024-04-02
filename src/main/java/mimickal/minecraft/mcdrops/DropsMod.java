/****************************************************************************************
 * This file is part of MCDrops, a Minecraft mod that drops items for players.
 * Copyright (C) 2014 Mimickal (Mia Moretti).
 *
 * MCDrops is free software under the GNU Affero General Public License v3.0.
 * See LICENSE or <https://www.gnu.org/licenses/agpl-3.0.en.html> for more information.
 ****************************************************************************************/
package mimickal.minecraft.mcdrops;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod(DropsMod.MOD_ID)
public class DropsMod {
    public static final String MOD_ID = "mcdrops";

    public DropsMod() {
        Config.register();
        MinecraftForge.EVENT_BUS.register(DropTickEvent.class);
        MinecraftForge.EVENT_BUS.register(DropCommand.class);
    }
}
