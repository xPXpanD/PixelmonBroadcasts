// Listens for Pokémon that get defeated.
package rs.expand.pixelmonbroadcasts.listeners;

// Remote imports.
import com.pixelmonmod.pixelmon.api.events.BeatWildPixelmonEvent;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

// Local imports.
import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.*;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.*;

// TODO: Log/announce boss types? Mega vs normal.
// Note: All the main class stuff and printing stuff is added through static imports.
public class DefeatListener
{
    @SubscribeEvent
    public void onBeatWildLegendaryEvent(final BeatWildPixelmonEvent event)
    {
        final EntityPixelmon pokemon = (EntityPixelmon) event.wpp.getEntity();
        final String pokemonName = pokemon.getLocalizedName();
        final String playerName = event.player.getName();
        final World world = pokemon.getEntityWorld();
        final BlockPos location = pokemon.getPosition();

        if (pokemon.isBossPokemon())
        {
            if (logBossDefeats)
            {
                // Print a defeat message to console.
                printBasicMessage
                (
                        "§5PBR §f// §ePlayer §6" + playerName +
                        "§e beat a §6" + pokemonName +
                        "§e boss in world \"§6" + world.getWorldInfo().getWorldName() +
                        "§e\", at X:§6" + location.getX() +
                        "§e Y:§6" + location.getY() +
                        "§e Z:§6" + location.getZ()
                );
            }

            if (showBossDefeatMessage)
            {
                // Parse placeholders and print!
                if (bossDefeatMessage != null)
                {
                    // Set up our message. This is the same for all eligible players, so call it once and store it.
                    final String finalMessage = replacePlaceholders(bossDefeatMessage, playerName, pokemon, location);

                    // Send off the message, the needed notifier permission and the flag to check.
                    iterateAndSendEventMessage(finalMessage, "bossdefeat", "showBossDefeat");
                }
                else
                    printBasicError("The boss defeat message is broken, broadcast failed.");
            }
        }
        else if (EnumPokemon.legendaries.contains(pokemonName))
        {
            if (logLegendaryDefeats)
            {
                // Add "shiny" to our console message if we have a shiny legendary.
                String shinyAddition = "§6";
                if (pokemon.getIsShiny())
                    shinyAddition = "shiny §6";

                // Print a defeat message to console, with the above shiny String mixed in.
                printBasicMessage
                (
                        "§5PBR §f// §ePlayer §6" + playerName +
                        "§e defeated a " + shinyAddition + pokemonName +
                        "§e in world \"§6" + world.getWorldInfo().getWorldName() +
                        "§e\", at X:§6" + location.getX() +
                        "§e Y:§6" + location.getY() +
                        "§e Z:§6" + location.getZ()
                );
            }

            if (showLegendaryDefeatMessage)
            {
                // Shiny legendary message logic, go!
                if (pokemon.getIsShiny())
                {
                    // Parse placeholders and print!
                    if (shinyLegendaryDefeatMessage != null)
                    {
                        // Set up our message. This is the same for all eligible players, so call it once and store it.
                        final String finalMessage = replacePlaceholders(shinyLegendaryDefeatMessage, playerName, pokemon, location);

                        // Send off the message, the needed notifier permission and the flag to check.
                        // We use the normal legendary permission for shiny legendaries, as per the config's explanation.
                        iterateAndSendEventMessage(finalMessage, "legendarydefeat", "showLegendaryDefeat");
                    }
                    else
                        printBasicError("The shiny legendary defeat message is broken, broadcast failed.");
                }
                else
                {
                    // Parse placeholders and print!
                    if (legendaryDefeatMessage != null)
                    {
                        // Set up our message. This is the same for all eligible players, so call it once and store it.
                        final String finalMessage = replacePlaceholders(legendaryDefeatMessage, playerName, pokemon, location);

                        // Send off the message, the needed notifier permission and the flag to check.
                        iterateAndSendEventMessage(finalMessage, "legendarydefeat", "showLegendaryDefeat");
                    }
                    else
                        printBasicError("The legendary defeat message is broken, broadcast failed.");
                }
            }
        }
        else if (pokemon.getIsShiny())
        {
            if (logShinyDefeats)
            {
                // Print a defeat message to console.
                printBasicMessage
                (
                        "§5PBR §f// §ePlayer §6" + playerName +
                        "§e beat a shiny §6" + pokemonName +
                        "§e in world \"§6" + world.getWorldInfo().getWorldName() +
                        "§e\", at X:§6" + location.getX() +
                        "§e Y:§6" + location.getY() +
                        "§e Z:§6" + location.getZ()
                );
            }

            if (showShinyDefeatMessage)
            {
                // Parse placeholders and print!
                if (shinyDefeatMessage != null)
                {
                    // Set up our message. This is the same for all eligible players, so call it once and store it.
                    final String finalMessage = replacePlaceholders(shinyDefeatMessage, playerName, pokemon, location);

                    // Send off the message, the needed notifier permission and the flag to check.
                    iterateAndSendEventMessage(finalMessage, "shinydefeat", "showShinyDefeat");
                }
                else
                    printBasicError("The shiny defeat message is broken, broadcast failed.");
            }
        }
    }
}
