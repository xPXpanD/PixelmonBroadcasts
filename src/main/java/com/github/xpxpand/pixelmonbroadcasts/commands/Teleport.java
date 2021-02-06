// Lets people teleport to the given location in the given world. Mostly used internally for event warps.
package com.github.xpxpand.pixelmonbroadcasts.commands;

import com.github.xpxpand.pixelmonbroadcasts.utilities.PlayerMethods;
import com.github.xpxpand.pixelmonbroadcasts.utilities.PrintingMethods;
import com.github.xpxpand.pixelmonbroadcasts.utilities.external.TeleportUtils;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.DimensionManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;
import java.util.Optional;

import static com.github.xpxpand.pixelmonbroadcasts.PixelmonBroadcasts.logger;

// FIXME: Teleporting across dimensions is flaky in SP and sometimes in MP too.
// FIXME: Teleporting sometimes fails even in the same dimension. Some sort of MC-built-in safety check?
public class Teleport extends HubCommand
{
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        if (sender.canUseCommand(4, "pixelmonbroadcasts.action.staff.teleport"))
            return true;
        else
        {
            PrintingMethods.sendTranslation(sender, "teleport.no_permissions");
            return false;
        }
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
    {
        if (sender instanceof EntityPlayer)
        {
            // Subcommand (teleport) option is the first arg.
            if (args.length >= 6)
            {
                // Is the first argument a valid player?
                if (isValidPlayer(args[1]) && getOptionalPlayerEntity(args[1]).isPresent())
                {
                    // Is the second argument a valid dimension ID?
                    if (args[2].matches("^-?\\d+$") && DimensionManager.isDimensionRegistered(Integer.parseInt(args[2])))
                    {
                        // Are all provided coordinates valid?
                        if (NumberUtils.isParsable(args[3]) && NumberUtils.isParsable(args[4]) && NumberUtils.isParsable(args[5]))
                        {
                            // Finally grab the player entity.
                            EntityPlayer entity = getOptionalPlayerEntity(args[1]).get();

                            int dimension = Integer.parseInt(args[2]);
                            BlockPos location = new BlockPos(Double.parseDouble(args[3]), Double.parseDouble(args[4]) + 1, Double.parseDouble(args[5]));
                            /*double x = Double.parseDouble(args[3]);
                            double y = Double.parseDouble(args[4] + 1);
                            double z = Double.parseDouble(args[5]);
                            attemptTeleport(entity, dimension, x, y, z, entity.rotationYaw, entity.rotationPitch);*/

                            // Dismount the player, then teleport them.
                            TeleportUtils.teleport(entity, dimension, location);

                            // Tell the player what we did.
                            PrintingMethods.sendTranslation(sender, "teleport.executed");
                        }
                        else
                            sendErrorWithUsage(sender, "teleport.invalid_coordinates");
                    }
                    else
                        sendErrorWithUsage(sender, "teleport.invalid_dimension");
                }
                else
                    sendErrorWithUsage(sender, "teleport.invalid_player");
            }
            else
                PrintingMethods.sendTranslation(sender, "teleport.usage");
        }
        else
            logger.error("This command can only be run by players.");
    }

/*
    public EntityPlayerMP tryGetServerEntity(EntityPlayer player)
    {
        if(!player.world.isRemote && player instanceof EntityPlayerMP)
        {
            logger.warn("Passed check!");
            return (EntityPlayerMP) player;
        }
        else
        {
            logger.error("Failed check :(");
            return null;
        }
    }

    public void attemptTeleport(EntityPlayer player, int dimension, double x, double y, double z, float yaw, float pitch)
    {
        if (player.world.isRemote)
            DimensionHelper.forceTeleport((EntityPlayerMP) player, dimension, x, y, z, yaw, pitch);
    }
*/


            // Was the provided String a valid online player?
    private boolean isValidPlayer(String playername)
    {
        List<EntityPlayer> playerList = PlayerMethods.getOnlinePlayers();
        return playerList.stream().anyMatch(s -> StringUtils.containsIgnoreCase(s.getName(), playername));
    }

    // Get a player entity from their name.
    private Optional<EntityPlayer> getOptionalPlayerEntity(String playername)
    {
        List<EntityPlayer> playerList = PlayerMethods.getOnlinePlayers();
        return playerList.stream().filter(s -> s.getName().equals(playername)).findFirst();
    }

    // Send an error, and then show usage.
    private void sendErrorWithUsage(ICommandSender sender, String key)
    {
        PrintingMethods.sendTranslation(sender, key);
        PrintingMethods.sendTranslation(sender, "teleport.usage");
    }
}
