package rs.expand.pixelmonbroadcasts.utilities;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.List;

public class PlayerMethods
{
    // Get all of the online players.
    public static List<EntityPlayer> getOnlinePlayers()
    {
        // Figure out what side we're on, and get either a single player (SP) or a list of players (MP).
        List<EntityPlayer> players = new ArrayList<>();
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT)
            players.add(Minecraft.getMinecraft().player);
        else // Cast from EntityPlayerMP to EntityPlayer.
            players = new ArrayList<>(FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers());

        return players;
    }
}
