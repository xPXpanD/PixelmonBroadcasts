// Listens for Pokémon captures with balls.
package rs.expand.pixelmonbroadcasts.listeners;

// Remote imports.
import com.pixelmonmod.pixelmon.api.events.CaptureEvent;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

// Local imports.
import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.*;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.*;
import static rs.expand.pixelmonbroadcasts.utilities.PlaceholderMethods.*;

public class CatchListener
{
    @SubscribeEvent
    public void onCatchPokemonEvent(final CaptureEvent.SuccessfulCapture event)
    {
        final EntityPixelmon pokemon = event.getPokemon();
        final String pokemonName = pokemon.getLocalizedName();
        final String playerName = event.player.getName();
        final World world = pokemon.getEntityWorld();
        final BlockPos location = event.pokeball.getPosition();
        final NBTTagCompound nbt = pokemon.serializeNBT();

        if (EnumPokemon.legendaries.contains(pokemonName))
        {
            if (logLegendaryCatches)
            {
                // Add "shiny" to our console message if we have a shiny legendary.
                String shinyAddition = "§b";
                if (pokemon.getIsShiny())
                    shinyAddition = "§bshiny ";

                // Print a catch message to console, with the above shiny String mixed in.
                printBasicMessage
                (
                        "§5PBR §f// §3Player §b" + playerName +
                        "§3 caught a " + shinyAddition + pokemonName +
                        "§3 in world \"§b" + world.getWorldInfo().getWorldName() +
                        "§3\", at X:§b" + location.getX() +
                        "§3 Y:§b" + location.getY() +
                        "§3 Z:§b" + location.getZ()
                );
            }

            if (showShinyLegendaryCatches && pokemon.getIsShiny())
            {
                // Parse placeholders and print!
                if (shinyLegendaryCatchMessage != null)
                {
                    // Set up our message. This is the same for all eligible players, so call it once and store it.
                    final String finalMessage = replacePlaceholders(shinyLegendaryCatchMessage, playerName, pokemon, location);

                    // Send off the message, the needed notifier permission and the flag to check.
                    // We use the normal legendary permission for shiny legendaries, as per the config's explanation.
                    iterateAndSendEventMessage(finalMessage, nbt, hoverShinyLegendaryCatches, true,
                            "shinylegendarycatch", "showShinyLegendaryCatch");
                }
                else
                    printBasicError("The shiny legendary catch message is broken, broadcast failed.");
            }
            else if (showLegendaryCatches)
            {
                // Parse placeholders and print!
                if (legendaryCatchMessage != null)
                {
                    // Set up our message. This is the same for all eligible players, so call it once and store it.
                    final String finalMessage = replacePlaceholders(legendaryCatchMessage, playerName, pokemon, location);

                    // Send off the message, the needed notifier permission and the flag to check.
                    iterateAndSendEventMessage(finalMessage, nbt, hoverLegendaryCatches, true,
                            "legendarycatch", "showLegendaryCatch");
                }
                else
                    printBasicError("The legendary catch message is broken, broadcast failed.");
            }
        }
        else if (pokemon.getIsShiny())
        {
            if (logShinyCatches)
            {
                // Print a catch message to console.
                printBasicMessage
                (
                        "§5PBR §f// §3Player §b" + playerName +
                        "§3 caught a §bshiny " + pokemonName +
                        "§3 in world \"§b" + world.getWorldInfo().getWorldName() +
                        "§3\", at X:§b" + location.getX() +
                        "§3 Y:§b" + location.getY() +
                        "§3 Z:§b" + location.getZ()
                );
            }

            if (showShinyCatches)
            {
                // Parse placeholders and print!
                if (shinyCatchMessage != null)
                {
                    // Set up our message. This is the same for all eligible players, so call it once and store it.
                    final String finalMessage = replacePlaceholders(shinyCatchMessage, playerName, pokemon, location);

                    // Send off the message, the needed notifier permission and the flag to check.
                    iterateAndSendEventMessage(finalMessage, nbt, hoverShinyCatches, true,
                            "shinycatch", "showShinyCatch");
                }
                else
                    printBasicError("The shiny catch message is broken, broadcast failed.");
            }
        }
    }
}
