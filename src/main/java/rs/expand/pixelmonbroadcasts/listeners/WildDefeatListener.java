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
    public void onBeatWildLegendaryEvent(final BeatWildPixelmonEvent event)
    {
        // Create shorthand variables for convenience.
        final String broadcast;
        final EntityPixelmon pokemon = (EntityPixelmon) event.wpp.getEntity();
        final String baseName = pokemon.getSpecies().getPokemonName();
        final String localizedName = pokemon.getSpecies().getLocalizedName();
        final EntityPlayer player = event.player;
        final BlockPos location = pokemon.getPosition();

        if (pokemon.isBossPokemon())
        {
            if (logBossVictories)
            {
                // If we're in a localized setup, log both names.
                final String nameString =
                        baseName.equals(localizedName) ? baseName : baseName + " §e(§6" + localizedName + "§e)";

                // Print a victory message to console.
                printBasicMessage
                (
                        "§5PBR §f// §ePlayer §6" + player.getName() +
                        "§e defeated a boss §6" + nameString +
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
                    iterateAndSendBroadcast(broadcast, pokemon, null, player, null,
                            hoverBossVictories, false, revealBossVictories,
                            "victory.boss", "showBossVictory");
                }
            }
        }
        else if (EnumSpecies.legendaries.contains(baseName) && pokemon.getPokemonData().getIsShiny())
        {
            if (logShinyLegendaryVictories)
            {
                // If we're in a localized setup, log both names.
                final String nameString =
                        baseName.equals(localizedName) ? baseName : baseName + " §c(§4" + localizedName + "§c)";

                // Print a victory message to console, with the above shiny String mixed in.
                printBasicMessage
                (
                        "§5PBR §f// §cPlayer §4" + player.getName() +
                        "§c defeated a shiny legendary §4" + nameString +
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
                    iterateAndSendBroadcast(broadcast, pokemon, null, player, null,
                            hoverShinyLegendaryVictories, false, revealShinyLegendaryVictories,
                            "victory.shinylegendary", "showShinyLegendaryVictory");
                }
            }
        }
        else if (EnumSpecies.legendaries.contains(baseName))
        {
            if (logLegendaryVictories)
            {
                // If we're in a localized setup, log both names.
                final String nameString =
                        baseName.equals(localizedName) ? baseName : baseName + " §c(§4" + localizedName + "§c)";

                // Print a victory message to console, with the above shiny String mixed in.
                printBasicMessage
                (
                        "§5PBR §f// §cPlayer §4" + player.getName() +
                        "§c defeated a legendary §4" + nameString +
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
                    iterateAndSendBroadcast(broadcast, pokemon, null, player, null,
                            hoverLegendaryVictories, false, revealLegendaryVictories,
                            "victory.legendary", "showLegendaryVictory");
                }
            }
        }
        else if (pokemon.getPokemonData().getIsShiny())
        {
            if (logShinyVictories)
            {
                // If we're in a localized setup, log both names.
                final String nameString =
                        baseName.equals(localizedName) ? baseName : baseName + " §c(§4" + localizedName + "§c)";

                // Print a victory message to console.
                printBasicMessage
                (
                        "§5PBR §f// §cPlayer §4" + player.getName() +
                        "§c defeated a shiny §4" + nameString +
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
                    iterateAndSendBroadcast(broadcast, pokemon, null, player, null,
                            hoverShinyVictories, false, revealShinyVictories,
                            "victory.shiny", "showShinyVictory");
                }
            }
        }
    }
}
