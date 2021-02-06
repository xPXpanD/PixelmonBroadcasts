package com.github.xpxpand.pixelmonbroadcasts.utilities;

import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.List;

import static com.github.xpxpand.pixelmonbroadcasts.PixelmonBroadcasts.logger;

// Methods that find or work on online players.
public class PlayerMethods
{
    // Get all of the online players.
    public static List<EntityPlayer> getOnlinePlayers()
    {
        // Figure out what side we're on, and get either a single player (SP) or a list of players (MP).
        List<EntityPlayer> players = new ArrayList<>();
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT)
        {
            logger.error("We're on the client side according to getOnlinePlayers()");
            players.add(Minecraft.getMinecraft().player);
        }
        else
            players = new ArrayList<>(FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers());

        return players;
    }

    // See if a player has a given permission, or is in single player. Staff stuff needs the highest possible permission level.
    // When in singleplayer everything except for teleports should be free game, even with cheats disabled.
    public static boolean hasPermission(EntityPlayer player, String permission)
    {
        return player.canUseCommand(4, permission) || FMLCommonHandler.instance().getSide() == Side.CLIENT;
    }
    public static boolean hasPermission(ICommandSender sender, String permission)
    {
        return sender.canUseCommand(4, permission) || FMLCommonHandler.instance().getSide() == Side.CLIENT;
    }
}
