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
// FIXME: Self-sacrifice moves like Explosion do not seem to fire this.
public class WildDefeatListener
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
                        "§e defeated a boss §6" + pokemonName +
                        "§e boss in world \"§6" + world.getWorldInfo().getWorldName() +
                        "§e\", at X:§6" + location.getX() +
                        "§e Y:§6" + location.getY() +
                        "§e Z:§6" + location.getZ()
                );
            }

            if (showBossDefeats)
            {
                // Parse placeholders and print!
                if (bossDefeatMessage != null)
                {
                    // Set up our message. This is the same for all eligible players, so call it once and store it.
                    final String finalMessage = replacePlaceholders(
                            bossDefeatMessage, playerName, true, false, pokemon, location);

                    // Send off the message, the needed notifier permission and the flag to check.
                    iterateAndSendEventMessage(
                            finalMessage, pokemon, hoverBossDefeats, false,
                            true, "boss.defeat", "showBossDefeat");
                }
                else
                    printBasicError("The boss defeat message is broken, broadcast failed.");
            }
        }
        else if (EnumPokemon.legendaries.contains(pokemonName) && pokemon.getIsShiny())
        {
            if (logShinyLegendaryDefeats)
            {
                // Print a defeat message to console, with the above shiny String mixed in.
                printBasicMessage
                (
                        "§5PBR §f// §cPlayer §4" + playerName +
                        "§c defeated a shiny legendary §4" + pokemonName +
                        "§c in world \"§4" + world.getWorldInfo().getWorldName() +
                        "§c\", at X:§4" + location.getX() +
                        "§c Y:§4" + location.getY() +
                        "§c Z:§4" + location.getZ()
                );
            }

            // Shiny legendary message logic, go!
            if (showShinyLegendaryDefeats)
            {
                // Parse placeholders and print!
                if (shinyLegendaryDefeatMessage != null)
                {
                    // Set up our message. This is the same for all eligible players, so call it once and store it.
                    final String finalMessage = replacePlaceholders(
                            shinyLegendaryDefeatMessage, playerName, true, false, pokemon, location);

                    // Send off the message, the needed notifier permission and the flag to check.
                    // We use the normal legendary permission for shiny legendaries, as per the config's explanation.
                    iterateAndSendEventMessage(finalMessage, pokemon, hoverShinyLegendaryDefeats, false,
                            true, "shinylegendary.defeat", "showShinyLegendaryDefeat");
                }
                else
                    printBasicError("The shiny legendary defeat message is broken, broadcast failed.");
            }
        }
        else if (EnumPokemon.legendaries.contains(pokemonName))
        {
            if (logLegendaryDefeats)
            {
                // Print a defeat message to console, with the above shiny String mixed in.
                printBasicMessage
                (
                        "§5PBR §f// §cPlayer §4" + playerName +
                        "§c defeated a legendary §4" + pokemonName +
                        "§c in world \"§4" + world.getWorldInfo().getWorldName() +
                        "§c\", at X:§4" + location.getX() +
                        "§c Y:§4" + location.getY() +
                        "§c Z:§4" + location.getZ()
                );
            }

            if (showLegendaryDefeats)
            {
                // Parse placeholders and print!
                if (legendaryDefeatMessage != null)
                {
                    // Set up our message. This is the same for all eligible players, so call it once and store it.
                    final String finalMessage = replacePlaceholders(
                            legendaryDefeatMessage, playerName, true, false, pokemon, location);

                    // Send off the message, the needed notifier permission and the flag to check.
                    iterateAndSendEventMessage(finalMessage, pokemon, hoverLegendaryDefeats, false,
                            true, "legendary.defeat", "showLegendaryDefeat");
                }
                else
                    printBasicError("The legendary defeat message is broken, broadcast failed.");
            }
        }
        else if (pokemon.getIsShiny())
        {
            if (logShinyDefeats)
            {
                // Print a defeat message to console.
                printBasicMessage
                (
                        "§5PBR §f// §cPlayer §4" + playerName +
                        "§c defeated a shiny §4" + pokemonName +
                        "§c in world \"§4" + world.getWorldInfo().getWorldName() +
                        "§c\", at X:§4" + location.getX() +
                        "§c Y:§4" + location.getY() +
                        "§c Z:§4" + location.getZ()
                );
            }

            if (showShinyDefeats)
            {
                // Parse placeholders and print!
                if (shinyDefeatMessage != null)
                {
                    // Set up our message. This is the same for all eligible players, so call it once and store it.
                    final String finalMessage = replacePlaceholders(
                            shinyDefeatMessage, playerName, true, false, pokemon, location);

                    // Send off the message, the needed notifier permission and the flag to check.
                    iterateAndSendEventMessage(
                            finalMessage, pokemon, hoverShinyDefeats, false,
                            true, "shiny.defeat", "showShinyDefeat");
                }
                else
                    printBasicError("The shiny defeat message is broken, broadcast failed.");
            }
        }
    }
}
