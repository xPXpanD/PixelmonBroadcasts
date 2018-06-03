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
import static rs.expand.pixelmonbroadcasts.utilities.PlaceholderMethods.*;

public class HatchListener
{
    @SubscribeEvent
    public void onHatchEvent(final EggHatchEvent event)
    {
        final String playerName = event.player.getName();
        final World world = event.player.getEntityWorld();
        final BlockPos location = event.player.getPosition();
        final EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(event.nbt, world);

        if (pokemon.getIsShiny())
        {
            if (logShinyHatches)
            {
                // Print a hatch message to console.
                printBasicMessage
                (
                        "§5PBR §f// §dPlayer §5" + playerName +
                        "§d's shiny §5" + event.nbt.getString(NbtKeys.NAME) +
                        "§d egg hatched in world \"§5" + world.getWorldInfo().getWorldName() +
                        "§d\" at X:§5" + location.getX() +
                        "§d Y:§5" + location.getY() +
                        "§d Z:§5" + location.getZ()
                );
            }

            if (showShinyHatches)
            {
                // Parse placeholders and print!
                if (shinyHatchMessage != null)
                {
                    // Set up our message. This is the same for all eligible players, so call it once and store it.
                    // We use the normal hatch permission for shiny hatches, as per the config's explanation.
                    final String finalMessage = replacePlaceholders(
                            shinyHatchMessage, playerName, true, false, pokemon, location);

                    // Send off the message, the needed notifier permission and the flag to check.
                    iterateAndSendEventMessage(finalMessage, pokemon, hoverShinyHatches, true,true,
                            "shinyhatch", "showShinyHatch");
                }
                else
                    printBasicError("The shiny egg hatching message is broken, broadcast failed.");
            }
        }
        else
        {
            if (logHatches)
            {
                // Print a hatch message to console.
                printBasicMessage
                (
                        "§5PBR §f// §dPlayer §5" + playerName +
                        "§d's §5" + event.nbt.getString(NbtKeys.NAME) +
                        "§d egg hatched in world \"§5" + world.getWorldInfo().getWorldName() +
                        "§d\" at X:§5" + location.getX() +
                        "§d Y:§5" + location.getY() +
                        "§d Z:§5" + location.getZ()
                );
            }

            if (showHatches)
            {
                // Parse placeholders and print!
                if (hatchMessage != null)
                {
                    // Set up our message. This is the same for all eligible players, so call it once and store it.
                    final String finalMessage = replacePlaceholders(
                            hatchMessage, playerName, true, false, pokemon, location);

                    // Send off the message, the needed notifier permission and the flag to check.
                    // We use the basic hatch permission for shiny hatches, as per the config's explanation.
                    iterateAndSendEventMessage(finalMessage, pokemon, hoverHatches, true, true,
                            "hatch", "showHatch");
                }
                else
                    printBasicError("The egg hatching message is broken, broadcast failed.");
            }
        }
    }
}

