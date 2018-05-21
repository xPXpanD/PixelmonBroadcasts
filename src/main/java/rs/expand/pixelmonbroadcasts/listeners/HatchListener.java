// Listens for Pokémon hatching from eggs.
package rs.expand.pixelmonbroadcasts.listeners;

// Remote imports.
import com.pixelmonmod.pixelmon.api.events.EggHatchEvent;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

// Local imports.
import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.*;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.*;

// Note: All the main class stuff and printing stuff is added through static imports.
public class HatchListener
{
    @SubscribeEvent
    public void onHatchEvent(final EggHatchEvent event)
    {
        final String playerName = event.player.getName();
        final World world = event.player.getEntityWorld();
        final BlockPos location = event.player.getPosition();
        final EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(event.nbt, world);

        if (logHatches)
        {
            // Add "shiny" to our console message if we have a shiny legendary.
            String shinyAddition = "§8";
            if (pokemon.getIsShiny())
                shinyAddition = "shiny §8";

            // Print a hatch message to console.
            printBasicMessage
            (
                    "§5PBR §f// §7Player §8" + playerName +
                    "§7's " + shinyAddition + event.nbt.getString(NbtKeys.NAME) +
                    "§7 egg hatched in world \"§8" + world.getWorldInfo().getWorldName() +
                    "§7\" at X:§8" + location.getX() +
                    "§7 Y:§8" + location.getY() +
                    "§7 Z:§8" + location.getZ()
            );
        }

        if (showHatchMessage)
        {
            if (pokemon.getIsShiny())
            {
                // Parse placeholders and print!
                if (shinyHatchMessage != null)
                {
                    // Set up our message. This is the same for all eligible players, so call it once and store it.
                    // We use the normal hatch permission for shiny hatches, as per the config's explanation.
                    final String finalMessage = replacePlaceholders(shinyHatchMessage, playerName, pokemon, location);

                    // Send off the message, the needed notifier permission and the flag to check.
                    iterateAndSendEventMessage(finalMessage, "hatch", "showHatch");
                }
                else
                    printBasicError("The shiny egg hatching message is broken, broadcast failed.");
            }
            else
            {
                // Parse placeholders and print!
                if (hatchMessage != null)
                {
                    // Set up our message. This is the same for all eligible players, so call it once and store it.
                    final String finalMessage = replacePlaceholders(hatchMessage, playerName, pokemon, location);

                    // Send off the message, the needed notifier permission and the flag to check.
                    // We use the basic hatch permission for shiny hatches, as per the config's explanation.
                    iterateAndSendEventMessage(finalMessage, "hatch", "showHatch");
                }
                else
                    printBasicError("The egg hatching message is broken, broadcast failed.");
            }
        }
    }
}

