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
        final String broadcast;
        final EntityPixelmon pokemon = event.getPokemon();
        final EntityPlayer player = event.player;
        final String baseName = pokemon.getPokemonName();
        final BlockPos location = event.pokeball.getPosition();

        if (EnumSpecies.legendaries.contains(baseName) && pokemon.getPokemonData().getIsShiny())
        {
            if (logShinyLegendaryCatches)
            {
                // Print a catch message to console, with the above shiny String mixed in.
                printBasicMessage
                (
                        "§5PBR §f// §aPlayer §2" + player.getName() +
                        "§a caught a shiny legendary §2" + baseName +
                        "§a in world \"§2" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                        "§a\", at X:§2" + location.getX() +
                        "§a Y:§2" + location.getY() +
                        "§a Z:§2" + location.getZ()
                );
            }

            if (showShinyLegendaryCatches)
            {
                // Sets the position of the entity we created, as it's 0 on all coordinates by default.
                pokemon.setPosition(location.getX(), location.getY(), location.getZ());

                // Get a broadcast from the broadcasts config file, if the key can be found.
                broadcast = getBroadcast("broadcast.catch.shiny_legendary");

                // Did we find a message? Iterate all available players, and send to those who should receive!
                if (broadcast != null)
                {
                    iterateAndSendBroadcast(broadcast, pokemon, player, hoverShinyLegendaryCatches, true,
                            revealShinyLegendaryCatches, "catch.shinylegendary", "showShinyLegendaryCatch");
                }
            }
        }
        else if (EnumSpecies.legendaries.contains(baseName))
        {
            if (logLegendaryCatches)
            {
                // Print a catch message to console, with the above shiny String mixed in.
                printBasicMessage
                (
                        "§5PBR §f// §aPlayer §2" + player.getName() +
                        "§a caught a legendary §2" + baseName +
                        "§a in world \"§2" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                        "§a\", at X:§2" + location.getX() +
                        "§a Y:§2" + location.getY() +
                        "§a Z:§2" + location.getZ()
                );
            }

            if (showLegendaryCatches)
            {
                // Sets the position of the entity we created, as it's 0 on all coordinates by default.
                pokemon.setPosition(location.getX(), location.getY(), location.getZ());

                // Get a broadcast from the broadcasts config file, if the key can be found.
                broadcast = getBroadcast("broadcast.catch.legendary");

                // Did we find a message? Iterate all available players, and send to those who should receive!
                if (broadcast != null)
                {
                    iterateAndSendBroadcast(broadcast, pokemon, player, hoverLegendaryCatches, true,
                            revealLegendaryCatches, "catch.legendary", "showLegendaryCatch");
                }
            }
        }
        else if (pokemon.getPokemonData().getIsShiny())
        {
            if (logShinyCatches)
            {
                // Print a catch message to console.
                printBasicMessage
                (
                        "§5PBR §f// §bPlayer §3" + player.getName() +
                        "§b caught a shiny §3" + baseName +
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
                broadcast = getBroadcast("broadcast.catch.shiny");

                // Did we find a message? Iterate all available players, and send to those who should receive!
                if (broadcast != null)
                {
                    iterateAndSendBroadcast(broadcast, pokemon, player, hoverShinyCatches, true,
                            revealShinyCatches, "catch.shiny", "showShinyCatch");
                }
            }
        }
        else
        {
            if (logNormalCatches)
            {
                // Print a catch message to console.
                printBasicMessage
                (
                        "§5PBR §f// §fPlayer §7" + player.getName() +
                        "§f caught a normal §7" + baseName +
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
                broadcast = getBroadcast("broadcast.catch.normal");

                // Did we find a message? Iterate all available players, and send to those who should receive!
                if (broadcast != null)
                {
                    iterateAndSendBroadcast(broadcast, pokemon, player, hoverNormalCatches, true,
                            revealNormalCatches, "catch.normal", "showNormalCatch");
                }
            }
        }
    }
}
