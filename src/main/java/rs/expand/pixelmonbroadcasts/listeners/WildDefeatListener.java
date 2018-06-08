// Listens for Pokémon that get defeated by players.
package rs.expand.pixelmonbroadcasts.listeners;

// Remote imports.
import com.pixelmonmod.pixelmon.api.events.BeatWildPixelmonEvent;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
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
    public void onBeatWildLegendaryEvent(final BeatWildPixelmonEvent event)
    {
        // Create shorthand variables for convenience.
        final String broadcast;
        final EntityPixelmon pokemon = (EntityPixelmon) event.wpp.getEntity();
        final EntityPlayer player = event.player;
        final BlockPos location = pokemon.getPosition();
        final String pokemonName = pokemon.getLocalizedName();

        if (pokemon.isBossPokemon())
        {
            if (logBossVictories)
            {
                // Print a victory message to console.
                printBasicMessage
                (
                        "§5PBR §f// §ePlayer §6" + player.getName() +
                        "§e defeated a boss §6" + pokemonName +
                        "§e boss in world \"§6" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                        "§e\", at X:§6" + location.getX() +
                        "§e Y:§6" + location.getY() +
                        "§e Z:§6" + location.getZ()
                );
            }

            if (showBossVictories)
            {
                // Get a broadcast from the broadcasts config file, if the key can be found.
                broadcast = getBroadcast("broadcast.victory.boss");

                // Did we find a message? Iterate all available players, and send to those who should receive!
                if (broadcast != null)
                {
                    iterateAndSendBroadcast(broadcast, pokemon, player, hoverBossVictories,
                            false, true, "victory.boss", "showBossVictory");
                }
            }
        }
        else if (EnumPokemon.legendaries.contains(pokemonName) && pokemon.getIsShiny())
        {
            if (logShinyLegendaryVictories)
            {
                // Print a victory message to console, with the above shiny String mixed in.
                printBasicMessage
                (
                        "§5PBR §f// §cPlayer §4" + player.getName() +
                        "§c defeated a shiny legendary §4" + pokemonName +
                        "§c in world \"§4" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                        "§c\", at X:§4" + location.getX() +
                        "§c Y:§4" + location.getY() +
                        "§c Z:§4" + location.getZ()
                );
            }

            if (showShinyLegendaryVictories)
            {
                // Get a broadcast from the broadcasts config file, if the key can be found.
                broadcast = getBroadcast("broadcast.victory.shiny_legendary");

                // Did we find a message? Iterate all available players, and send to those who should receive!
                if (broadcast != null)
                {
                    iterateAndSendBroadcast(broadcast, pokemon, player, hoverShinyLegendaryVictories,
                            false, true, "victory.shinylegendary", "showShinyLegendaryVictory");
                }
            }
        }
        else if (EnumPokemon.legendaries.contains(pokemonName))
        {
            if (logLegendaryVictories)
            {
                // Print a victory message to console, with the above shiny String mixed in.
                printBasicMessage
                (
                        "§5PBR §f// §cPlayer §4" + player.getName() +
                        "§c defeated a legendary §4" + pokemonName +
                        "§c in world \"§4" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                        "§c\", at X:§4" + location.getX() +
                        "§c Y:§4" + location.getY() +
                        "§c Z:§4" + location.getZ()
                );
            }

            if (showLegendaryVictories)
            {
                // Get a broadcast from the broadcasts config file, if the key can be found.
                broadcast = getBroadcast("broadcast.victory.legendary");

                // Did we find a message? Iterate all available players, and send to those who should receive!
                if (broadcast != null)
                {
                    iterateAndSendBroadcast(broadcast, pokemon, player, hoverLegendaryVictories,
                            false, true, "victory.legendary", "showLegendaryVictory");
                }
            }
        }
        else if (pokemon.getIsShiny())
        {
            if (logShinyVictories)
            {
                // Print a victory message to console.
                printBasicMessage
                (
                        "§5PBR §f// §cPlayer §4" + player.getName() +
                        "§c defeated a shiny §4" + pokemonName +
                        "§c in world \"§4" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                        "§c\", at X:§4" + location.getX() +
                        "§c Y:§4" + location.getY() +
                        "§c Z:§4" + location.getZ()
                );
            }

            if (showShinyVictories)
            {
                // Get a broadcast from the broadcasts config file, if the key can be found.
                broadcast = getBroadcast("broadcast.victory.shiny");

                // Did we find a message? Iterate all available players, and send to those who should receive!
                if (broadcast != null)
                {
                    iterateAndSendBroadcast(broadcast, pokemon, player, hoverShinyVictories,
                            false, true, "victory.shiny", "showShinyVictory");
                }
            }
        }
    }
}
