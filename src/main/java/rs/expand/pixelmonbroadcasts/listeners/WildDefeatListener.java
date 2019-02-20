// Listens for Pokémon that get defeated by players.
package rs.expand.pixelmonbroadcasts.listeners;

// Remote imports.
import com.pixelmonmod.pixelmon.api.events.BeatWildPixelmonEvent;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
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
    public void onBeatWildPokemonEvent(final BeatWildPixelmonEvent event)
    {
        // Create shorthand variables for convenience.
        final String broadcast;
        final EntityPixelmon pokemon = (EntityPixelmon) event.wpp.getEntity();
        final String baseName = pokemon.getSpecies().getPokemonName();
        final String localizedName = pokemon.getSpecies().getLocalizedName();
        final EntityPlayer player = event.player;
        final BlockPos location = pokemon.getPosition();

        // If we're in a localized setup, log both names.
        final String nameString =
                baseName.equals(localizedName) ? baseName : baseName + " §4(§c" + localizedName + "§4)";

        if (pokemon.isBossPokemon())
        {
            if (logBossVictories)
            {
                // Print a victory message to console.
                printUnformattedMessage
                (
                        "§5PBR §f// §4Player §c" + player.getName() +
                        "§4 defeated a boss §c" + nameString +
                        "§4 boss in world \"§c" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                        "§4\", at X:§c" + location.getX() +
                        "§4 Y:§c" + location.getY() +
                        "§4 Z:§c" + location.getZ()
                );
            }

            if (showBossVictories)
            {
                // Get a broadcast from the broadcasts config file, if the key can be found.
                broadcast = getBroadcast("broadcast.victory.boss");

                // Did we find a message? Iterate all available players, and send to those who should receive!
                if (broadcast != null)
                {
                    iterateAndSendBroadcast(broadcast, pokemon, null, player, null,
                            hoverBossVictories, false, revealBossVictories,
                            "victory.boss", "showBossVictory");
                }
            }
        }
        else if (EnumSpecies.legendaries.contains(baseName) && pokemon.getPokemonData().isShiny())
        {
            if (logLegendaryVictories || logShinyVictories)
            {
                // Print a victory message to console, with the above shiny String mixed in.
                printUnformattedMessage
                (
                        "§5PBR §f// §4Player §c" + player.getName() +
                        "§4 defeated a shiny legendary §c" + nameString +
                        "§4 in world \"§c" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                        "§4\", at X:§c" + location.getX() +
                        "§4 Y:§c" + location.getY() +
                        "§4 Z:§c" + location.getZ()
                );
            }

            if (showLegendaryVictories)
            {
                // Get a broadcast from the broadcasts config file, if the key can be found.
                broadcast = getBroadcast("broadcast.victory.shiny_legendary");

                // Did we find a message? Iterate all available players, and send to those who should receive!
                if (broadcast != null)
                {
                    iterateAndSendBroadcast(broadcast, pokemon, null, player, null,
                            hoverLegendaryVictories, false, revealLegendaryVictories,
                            "victory.shinylegendary", "showLegendaryVictory", "showShinyVictory");
                }
            }
            else if (showShinyVictories)
            {
                // Get a broadcast from the broadcasts config file, if the key can be found.
                broadcast = getBroadcast("broadcast.victory.shiny_legendary");

                // Did we find a message? Iterate all available players, and send to those who should receive!
                if (broadcast != null)
                {
                    iterateAndSendBroadcast(broadcast, pokemon, null, player, null,
                            hoverShinyVictories, false, revealShinyVictories,
                            "victory.shinylegendary", "showLegendaryVictory", "showShinyVictory");
                }
            }
        }
        else if (EnumSpecies.legendaries.contains(baseName))
        {
            if (logLegendaryVictories)
            {
                // Print a victory message to console, with the above shiny String mixed in.
                printUnformattedMessage
                (
                        "§5PBR §f// §4Player §c" + player.getName() +
                        "§4 defeated a legendary §c" + nameString +
                        "§4 in world \"§c" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                        "§4\", at X:§c" + location.getX() +
                        "§4 Y:§c" + location.getY() +
                        "§4 Z:§c" + location.getZ()
                );
            }

            if (showLegendaryVictories)
            {
                // Get a broadcast from the broadcasts config file, if the key can be found.
                broadcast = getBroadcast("broadcast.victory.legendary");

                // Did we find a message? Iterate all available players, and send to those who should receive!
                if (broadcast != null)
                {
                    iterateAndSendBroadcast(broadcast, pokemon, null, player, null,
                            hoverLegendaryVictories, false, revealLegendaryVictories,
                            "victory.legendary", "showLegendaryVictory");
                }
            }
        }
        else if (pokemon.getPokemonData().isShiny())
        {
            if (logShinyVictories)
            {
                // Print a victory message to console.
                printUnformattedMessage
                (
                        "§5PBR §f// §4Player §c" + player.getName() +
                        "§4 defeated a shiny §c" + nameString +
                        "§4 in world \"§c" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                        "§4\", at X:§c" + location.getX() +
                        "§4 Y:§c" + location.getY() +
                        "§4 Z:§c" + location.getZ()
                );
            }

            if (showShinyVictories)
            {
                // Get a broadcast from the broadcasts config file, if the key can be found.
                broadcast = getBroadcast("broadcast.victory.shiny");

                // Did we find a message? Iterate all available players, and send to those who should receive!
                if (broadcast != null)
                {
                    iterateAndSendBroadcast(broadcast, pokemon, null, player, null,
                            hoverShinyVictories, false, revealShinyVictories,
                            "victory.shiny", "showShinyVictory");
                }
            }
        }
    }
}
