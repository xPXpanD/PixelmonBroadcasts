// Lets people teleport to the given location in the given world. Mostly used internally for event warps.
package com.github.xpxpand.pixelmonbroadcasts.commands;

import com.github.xpxpand.pixelmonbroadcasts.utilities.PlayerMethods;
import com.github.xpxpand.pixelmonbroadcasts.utilities.PrintingMethods;
import com.pixelmonmod.pixelmon.util.helpers.DimensionHelper;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Arrays;
import java.util.List;

import static com.github.xpxpand.pixelmonbroadcasts.PixelmonBroadcasts.logger;

public class Teleport extends HubCommand
{
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
    {
        if (sender instanceof EntityPlayer)
        {
            if (PlayerMethods.hasPermission(sender, "pixelmonbroadcasts.command.staff.teleport"))
            {
                // Subcommand (teleport) option is the first arg.
                if (args.length >= 5)
                {
                    // Get all dimensions, loaded and unloaded.
                    List<Integer> dimensions = Arrays.asList(DimensionManager.getStaticDimensionIDs());

                    // Is the second argument a valid dimension ID?
                    if (args[1].matches("^-?\\d+$") && dimensions.contains(Integer.parseInt(args[1])))
                    {
                        // Are all provided coordinates valid?
                        if (NumberUtils.isParsable(args[2]) && NumberUtils.isParsable(args[3]) && NumberUtils.isParsable(args[4]))
                        {
                            final int dimension = Integer.parseInt(args[1]);
                            double x = Double.parseDouble(args[2]);
                            double y = Double.parseDouble(args[3]) + 1; // Increment vertical by one to avoid falling.
                            double z = Double.parseDouble(args[4]);

                            // TODO: Find a way to reliably get all Nether-type worlds. Probably not a huge deal.
                            if (((EntityPlayer) sender).dimension == -1)
                            {
                                logger.info("Detected a teleport coming from the Nether, fixing coordinates...");
                                x = x / 8;
                                z = z / 8;
                            }

                            // Get the current player from our player list. Needed so we can get the serverside entity.
                            EntityPlayerMP serverPlayer = getServerEntity(sender.getName());
                            if (serverPlayer != null)
                                DimensionHelper.teleport(serverPlayer, dimension, x, y, z);

/*                                // Dismount the player, then teleport them. If we're on the client side, use a safer but less reliable method.
                            if (FMLCommonHandler.instance().getSide() == Side.CLIENT)
                                //teleport(entity, dimension, new BlockPos(x, y, z));
                                teleport2(entity, dimension, new BlockPos(x, y, z));
                            else
                                DimensionHelper.teleport((EntityPlayerMP) entity, dimension, x, y, z);*/

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
                    PrintingMethods.sendTranslation(sender, "teleport.usage");
            }
            else
            {
                if (FMLCommonHandler.instance().getSide() == Side.CLIENT)
                    PrintingMethods.sendTranslation(sender, "teleport.cheats_disabled");
                else
                    PrintingMethods.sendTranslation(sender, "teleport.no_permissions");
            }
        }
        else
            logger.error("This command can only be run by players.");
    }

    // Send an error, and then show usage.
    private void sendErrorWithUsage(ICommandSender sender, String key)
    {
        PrintingMethods.sendTranslation(sender, key);
        PrintingMethods.sendTranslation(sender, "teleport.usage");
    }

    private EntityPlayerMP getServerEntity(String name)
    {
        for (EntityPlayer player : PlayerMethods.getOnlinePlayers())
        {
            if (player.getName().equals(name))
                return (EntityPlayerMP) player;
        }

        return null;
    }
}
