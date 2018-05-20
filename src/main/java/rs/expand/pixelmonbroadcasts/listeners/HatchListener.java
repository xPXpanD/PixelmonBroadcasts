// Listens for Pokémon hatching from owned eggs.
package rs.expand.pixelmonbroadcasts.listeners;

// Remote imports.
import com.pixelmonmod.pixelmon.api.events.EggHatchEvent;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;

// Local imports.
import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.*;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.*;

// Note: All the main class stuff and printing stuff is added through static imports.
public class HatchListener
{
    @SubscribeEvent
    public void onHatchEvent(final EggHatchEvent event)
    {
        if (showHatchMessage)
        {
            final String playerName = event.player.getName();
            final World world = event.player.getEntityWorld();
            final BlockPos location = event.player.getPosition();
            final EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(event.nbt, world);

            // Print a hatch message to console.
            printBasicMessage
            (
                    "§5PBR §f// §7Player §8" + playerName +
                    "§7's §8" + event.nbt.getString(NbtKeys.NAME) +
                    "§7 egg hatched in world \"§8" + world.getWorldInfo().getWorldName() +
                    "§7\" at X:§8" + location.getX() +
                    "§7 Y:§8" + location.getY() +
                    "§7 Z:§8" + location.getZ()
            );

            // Parse placeholders and print!
            if (hatchMessage != null)
            {
                // Set up our message. This is the same for all eligible players, so call it once and store it.
                final Text finalMessage = Text.of(replacePlaceholders(hatchMessage, playerName, pokemon, location));

                // Sift through the online players.
                Sponge.getGame().getServer().getOnlinePlayers().forEach((recipient) ->
                {
                    // Does the iterated player have the needed notifier permission?
                    if (recipient.hasPermission("pixelmonbroadcasts.notify.hatch"))
                    {
                        // Does the iterated player have the message enabled? Send it if we get "true" returned.
                        if (checkToggleStatus((EntityPlayerMP) recipient, "showHatchMessage"))
                            recipient.sendMessage(finalMessage);
                    }
                });
            }
            else
                printBasicError("The hatch message is broken, broadcast failed.");
        }
    }
}
