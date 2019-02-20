// Listens for Pokémon captures with balls.
package rs.expand.pixelmonbroadcasts.listeners;

// Remote imports.
import com.pixelmonmod.pixelmon.api.events.CaptureEvent;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import net.minecraft.entity.player.EntityPlayer;
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
        // Create shorthand variables for convenience.
        final EntityPixelmon pokemon = event.getPokemon();
        final String baseName = pokemon.getSpecies().getPokemonName();
        final String localizedName = pokemon.getSpecies().getLocalizedName();
        final BlockPos location = event.pokeball.getPosition();
        final EntityPlayer player = event.player;

        // If we're in a localized setup, log both names.
        final String nameString =
                baseName.equals(localizedName) ? baseName : baseName + " §2(§a" + localizedName + "§2)";

        if (EnumSpecies.legendaries.contains(baseName) && pokemon.getPokemonData().isShiny())
        {
            if (logLegendaryCatches || logShinyCatches)
            {
                // Print a catch message to console, with the above shiny String mixed in.
                printUnformattedMessage
                (
                        "§5PBR §f// §2Player §a" + player.getName() +
                        "§2 caught a shiny legendary §a" + nameString +
                        "§2 in world \"§a" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                        "§2\", at X:§a" + location.getX() +
                        "§2 Y:§a" + location.getY() +
                        "§2 Z:§a" + location.getZ()
                );
            }

            if (showLegendaryCatches)
            {
                // Sets the position of the entity we created, as it's 0 on all coordinates by default.
                pokemon.setPosition(location.getX(), location.getY(), location.getZ());

                // Get a broadcast from the broadcasts config file, if the key can be found.
                final String broadcast = getBroadcast("broadcast.catch.shiny_legendary");

                // Did we find a message? Iterate all available players, and send to those who should receive!
                if (broadcast != null)
                {
                    iterateAndSendBroadcast(broadcast, pokemon, null, player, null,
                            hoverLegendaryCatches, true, revealLegendaryCatches,
                            "catch.shinylegendary", "showLegendaryCatch", "showShinyCatch");
                }
            }
            else if (showShinyCatches)
            {
                // Sets the position of the entity we created, as it's 0 on all coordinates by default.
                pokemon.setPosition(location.getX(), location.getY(), location.getZ());

                // Get a broadcast from the broadcasts config file, if the key can be found.
                final String broadcast = getBroadcast("broadcast.catch.shiny_legendary");

                // Did we find a message? Iterate all available players, and send to those who should receive!
                if (broadcast != null)
                {
                    iterateAndSendBroadcast(broadcast, pokemon, null, player, null,
                            hoverShinyCatches, true, revealShinyCatches,
                            "catch.shinylegendary", "showLegendaryCatch", "showShinyCatch");
                }
            }
        }
        else if (EnumSpecies.legendaries.contains(baseName))
        {
            if (logLegendaryCatches)
            {
                // Print a catch message to console, with the above shiny String mixed in.
                printUnformattedMessage
                (
                        "§5PBR §f// §2Player §a" + player.getName() +
                        "§2 caught a legendary §a" + nameString +
                        "§2 in world \"§a" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                        "§2\", at X:§a" + location.getX() +
                        "§2 Y:§a" + location.getY() +
                        "§2 Z:§a" + location.getZ()
                );
            }

            if (showLegendaryCatches)
            {
                // Sets the position of the entity we created, as it's 0 on all coordinates by default.
                pokemon.setPosition(location.getX(), location.getY(), location.getZ());

                // Get a broadcast from the broadcasts config file, if the key can be found.
                final String broadcast = getBroadcast("broadcast.catch.legendary");

                // Did we find a message? Iterate all available players, and send to those who should receive!
                if (broadcast != null)
                {
                    iterateAndSendBroadcast(broadcast, pokemon, null, player, null,
                            hoverLegendaryCatches, true, revealLegendaryCatches,
                            "catch.legendary", "showLegendaryCatch");
                }
            }
        }
        else if (pokemon.getPokemonData().isShiny())
        {
            if (logShinyCatches)
            {
                // Print a catch message to console.
                printUnformattedMessage
                (
                        "§5PBR §f// §bPlayer §3" + player.getName() +
                        "§b caught a shiny §3" + nameString +
                        "§b in world \"§3" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                        "§b\", at X:§3" + location.getX() +
                        "§b Y:§3" + location.getY() +
                        "§b Z:§3" + location.getZ()
                );
            }

            if (showShinyCatches)
            {
                // Sets the position of the entity we created, as it's 0 on all coordinates by default.
                pokemon.setPosition(location.getX(), location.getY(), location.getZ());

                // Get a broadcast from the broadcasts config file, if the key can be found.
                final String broadcast = getBroadcast("broadcast.catch.shiny");

                // Did we find a message? Iterate all available players, and send to those who should receive!
                if (broadcast != null)
                {
                    iterateAndSendBroadcast(broadcast, pokemon, null, player, null,
                            hoverShinyCatches, true, revealShinyCatches,
                            "catch.shiny", "showShinyCatch");
                }
            }
        }
        else
        {
            if (logNormalCatches)
            {
                // Print a catch message to console.
                printUnformattedMessage
                (
                        "§5PBR §f// §fPlayer §7" + player.getName() +
                        "§f caught a normal §7" + nameString +
                        "§f in world \"§7" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                        "§f\", at X:§7" + location.getX() +
                        "§f Y:§7" + location.getY() +
                        "§f Z:§7" + location.getZ()
                );
            }

            if (showNormalCatches)
            {
                // Sets the position of the entity we created, as it's 0 on all coordinates by default.
                pokemon.setPosition(location.getX(), location.getY(), location.getZ());

                // Get a broadcast from the broadcasts config file, if the key can be found.
                final String broadcast = getBroadcast("broadcast.catch.normal");

                // Did we find a message? Iterate all available players, and send to those who should receive!
                if (broadcast != null)
                {
                    iterateAndSendBroadcast(broadcast, pokemon, null, player, null,
                            hoverNormalCatches, true, revealNormalCatches,
                            "catch.normal", "showNormalCatch");
                }
            }
        }
    }
}
