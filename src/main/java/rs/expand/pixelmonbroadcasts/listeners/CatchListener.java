// Listens for Pokémon captures with balls.
package rs.expand.pixelmonbroadcasts.listeners;

// Remote imports.
import com.pixelmonmod.pixelmon.api.events.CaptureEvent;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import net.minecraft.util.math.BlockPos;
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
        final BlockPos location = event.pokeball.getPosition();

        if (EnumPokemon.legendaries.contains(pokemonName) && pokemon.getIsShiny())
        {
            if (logShinyLegendaryCatches)
            {
                // Print a catch message to console, with the above shiny String mixed in.
                printBasicMessage
                (
                        "§5PBR §f// §aPlayer §2" + playerName +
                        "§a caught a shiny legendary §2" + pokemonName +
                        "§a in world \"§2" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                        "§a\", at X:§2" + location.getX() +
                        "§a Y:§2" + location.getY() +
                        "§a Z:§2" + location.getZ()
                );
            }

            if (showShinyLegendaryCatches)
            {
                // Parse placeholders and print!
                if (shinyLegendaryCatchMessage != null)
                {
                    // Set up our message. This is the same for all eligible players, so call it once and store it.
                    final String finalMessage = replacePlaceholders(
                            shinyLegendaryCatchMessage, playerName, true, false, pokemon, location);

                    // Send off the message, the needed notifier permission and the flag to check.
                    // We use the normal legendary permission for shiny legendaries, as per the config's explanation.
                    iterateAndSendEventMessage(finalMessage, pokemon, hoverShinyLegendaryCatches, true,
                            true, "catch.shinylegendary", "showShinyLegendaryCatch");
                }
                else
                    printBasicError("The shiny legendary catch message is broken, broadcast failed.");
            }
        }
        else if (EnumPokemon.legendaries.contains(pokemonName))
        {
            if (logLegendaryCatches)
            {
                // Print a catch message to console, with the above shiny String mixed in.
                printBasicMessage
                (
                        "§5PBR §f// §aPlayer §2" + playerName +
                        "§a caught a legendary §2" + pokemonName +
                        "§a in world \"§2" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                        "§a\", at X:§2" + location.getX() +
                        "§a Y:§2" + location.getY() +
                        "§a Z:§2" + location.getZ()
                );
            }

            if (showLegendaryCatches)
            {
                // Parse placeholders and print!
                if (legendaryCatchMessage != null)
                {
                    // Set up our message. This is the same for all eligible players, so call it once and store it.
                    final String finalMessage = replacePlaceholders(
                            legendaryCatchMessage, playerName, true, false, pokemon, location);

                    // Send off the message, the needed notifier permission and the flag to check.
                    iterateAndSendEventMessage(finalMessage, pokemon, hoverLegendaryCatches, true,
                            true, "catch.legendary", "showLegendaryCatch");
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
                        "§5PBR §f// §bPlayer §3" + playerName +
                        "§b caught a shiny §3" + pokemonName +
                        "§b in world \"§3" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                        "§b\", at X:§3" + location.getX() +
                        "§b Y:§3" + location.getY() +
                        "§b Z:§3" + location.getZ()
                );
            }

            if (showShinyCatches)
            {
                // Parse placeholders and print!
                if (shinyCatchMessage != null)
                {
                    // Set up our message. This is the same for all eligible players, so call it once and store it.
                    final String finalMessage = replacePlaceholders(
                            shinyCatchMessage, playerName, true, false, pokemon, location);

                    // Send off the message, the needed notifier permission and the flag to check.
                    iterateAndSendEventMessage(finalMessage, pokemon, hoverShinyCatches, true,
                            true, "catch.shiny", "showShinyCatch");
                }
                else
                    printBasicError("The shiny catch message is broken, broadcast failed.");
            }
        }
    }
}
