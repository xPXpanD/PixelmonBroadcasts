// Lets people teleport to the given location in the given world. Mostly used internally for event warps.
package rs.expand.pixelmonbroadcasts.commands;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import rs.expand.pixelmonbroadcasts.utilities.PlayerMethods;
import rs.expand.pixelmonbroadcasts.utilities.TeleportUtils;

import java.util.List;
import java.util.Optional;

import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.logger;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.sendTranslation;

public class Teleport extends HubCommand
{
    @Override
    public String getName()
    {
        return "teleport";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/pixelmonbroadcasts teleport <playername> <dimension ID> <x> <y> <z>";
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        if (sender.canUseCommand(4, "pixelmonbroadcasts.action.staff.teleport"))
            return true;
        else
        {
            sendTranslation(sender, "action.teleport.no_permissions");
            return false;
        }
    }

    // TODO: Lang the errors.
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
    {
        if (sender instanceof EntityPlayer)
        {
            // Subcommand (teleport) option is the first arg.
            if (args.length >= 6)
            {
                // Is the first argument a valid player?
                if (isValidPlayer(args[1]))
                {
                    // Is the second argument a valid dimension ID?
                    if (StringUtils.isNumeric(args[2]) && DimensionManager.isDimensionRegistered(Integer.parseInt(args[2])))
                    {
                        // Are all provided coordinates valid?
                        if (NumberUtils.isParsable(args[3]) && NumberUtils.isParsable(args[4]) && NumberUtils.isParsable(args[5]))
                        {
                            // One last sanity check, as this can return an empty Optional. Don't think this will happen.
                            Optional<EntityPlayer> optionalEntity = getPlayerEntity(args[1]);

                            if (optionalEntity.isPresent())
                            {
                                // Finally grab the player entity.
                                EntityPlayer entity = optionalEntity.get();
                                WorldServer worldServer = server.getWorld(entity.dimension);

                                // Dismount the player, then teleport them.
                                // Updating on TP seems to sync client and server, preventing "too fast" warnings?
                                /*entity.dismountRidingEntity();
                                entity.changeDimension(Integer.parseInt(args[2]));
                                entity.setPositionAndUpdate(Double.parseDouble(args[3]), Double.parseDouble(args[4]), Double.parseDouble(args[5]));*/

                                // TODO: Uses code that's not mine. Remove or get permission.
                                TeleportUtils.teleport(entity, Integer.parseInt(args[2]), new BlockPos(Double.parseDouble(args[3]), Double.parseDouble(args[4]), Double.parseDouble(args[5])));

                                // Tell the player what we did.
                                sendTranslation(sender, "action.teleport.executed");
                            }
                            else
                                sender.sendMessage(new TextComponentString("§cCould not teleport! The targeted player may be offline?"));
                        }
                        else
                            sender.sendMessage(new TextComponentString("§cCould not teleport! Invalid coordinates."));
                    }
                    else
                        sender.sendMessage(new TextComponentString("§cCould not teleport! Invalid dimension ID."));
                }
                else
                    sender.sendMessage(new TextComponentString("§cCould not teleport! Could not find the provided player."));
            }
            else
                sender.sendMessage(new TextComponentString("§cCould not teleport! Invalid number of arguments."));
        }
        else
            logger.error("This command can only be run by players.");
    }

    // Was the provided String a valid online player?
    private boolean isValidPlayer(String playername)
    {
        List<EntityPlayer> playerList = PlayerMethods.getOnlinePlayers();
        return playerList.stream().anyMatch(s -> StringUtils.containsIgnoreCase(s.getName(), playername));
    }

    // Get a player entity from their name.
    private Optional<EntityPlayer> getPlayerEntity(String playername)
    {
        List<EntityPlayer> playerList = PlayerMethods.getOnlinePlayers();
        return playerList.stream().filter(s -> s.getName().equals(playername)).findFirst();
    }
}
