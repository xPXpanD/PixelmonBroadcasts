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
import static rs.expand.pixelmonbroadcasts.utilities.PlaceholderMethods.*;

// TODO: Log/announce boss types? Mega vs normal.
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
                        "§5PBR §f// §6Player §e" + playerName +
                        "§6 beat a §e" + pokemonName +
                        "§6 boss in world \"§e" + world.getWorldInfo().getWorldName() +
                        "§6\", at X:§e" + location.getX() +
                        "§6 Y:§e" + location.getY() +
                        "§6 Z:§e" + location.getZ()
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
                String shinyAddition = "§e";
                if (pokemon.getIsShiny())
                    shinyAddition = "§eshiny ";

                // Print a defeat message to console, with the above shiny String mixed in.
                printBasicMessage
                (
                        "§5PBR §f// §6Player §e" + playerName +
                        "§6 defeated a " + shinyAddition + pokemonName +
                        "§6 in world \"§e" + world.getWorldInfo().getWorldName() +
                        "§6\", at X:§e" + location.getX() +
                        "§6 Y:§e" + location.getY() +
                        "§6 Z:§e" + location.getZ()
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
                        "§5PBR §f// §6Player §e" + playerName +
                        "§6 beat a §eshiny " + pokemonName +
                        "§6 in world \"§e" + world.getWorldInfo().getWorldName() +
                        "§6\", at X:§e" + location.getX() +
                        "§6 Y:§e" + location.getY() +
                        "§6 Z:§e" + location.getZ()
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
