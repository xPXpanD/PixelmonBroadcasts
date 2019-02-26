// Listens for Pokémon captures with balls.
package rs.expand.pixelmonbroadcasts.listeners;

import com.pixelmonmod.pixelmon.api.events.CaptureEvent;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import rs.expand.pixelmonbroadcasts.enums.EventData;

public class CatchListener
{
    @SubscribeEvent
    public void onCatchPokemonEvent(final CaptureEvent.SuccessfulCapture event)
    {
        // Create shorthand variables for convenience.
        final EntityPlayer player = event.player;
        final EntityPixelmon pokemon = event.getPokemon();
        final String baseName = pokemon.getSpecies().getPokemonName();
        final String localizedName = pokemon.getSpecies().getLocalizedName();
        final BlockPos location = event.pokeball.getPosition();

        // Sets the position of the entity we created, as it's 0 on all coordinates by default.
        pokemon.setPosition(location.getX(), location.getY(), location.getZ());

        // If we're in a localized setup, log both names.
        final String nameString =
                baseName.equals(localizedName) ? baseName : baseName + " §2(§a" + localizedName + "§2)";

        if (EnumSpecies.legendaries.contains(baseName) && pokemon.getPokemonData().isShiny())
        {
            if (logLegendaryCatches || logShinyCatches)
            {
                // Print a catch message to console, with the above shiny String mixed in.
                logger.info
                (
                        "§5PBR §f// §2Player §a" + player.getName() +
                        "§2 caught a shiny legendary §a" + nameString +
                        "§2 in world \"§a" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                        "§2\", at X:§a" + location.getX() +
                        "§2 Y:§a" + location.getY() +
                        "§2 Z:§a" + location.getZ()
                );
            }

            if (printLegendaryCatches || notifyLegendaryCatches)
            {
                if (printLegendaryCatches)
                {
                    // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                    doBroadcast(
                            EnumBroadcastTypes.PRINT, EventData.Catches.SHINY_LEGENDARY_AS_LEGENDARY,
                            pokemon, null, player, null);
                }

                if (notifyLegendaryCatches)
                {
                    // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                    doBroadcast(
                            EnumBroadcastTypes.NOTIFY, EventData.Catches.SHINY_LEGENDARY_AS_LEGENDARY,
                            pokemon, null, player, null);
                }
            }
            else if (printShinyCatches || notifyShinyCatches)
            {
                if (printShinyCatches)
                {
                    // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                    doBroadcast(
                            EnumBroadcastTypes.PRINT, EventData.Catches.SHINY_LEGENDARY_AS_SHINY,
                            pokemon, null, player, null);
                }

                if (notifyShinyCatches)
                {
                    // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                    doBroadcast(
                            EnumBroadcastTypes.NOTIFY, EventData.Catches.SHINY_LEGENDARY_AS_SHINY,
                            pokemon, null, player, null);
                }
            }
        }
        else if (EnumSpecies.legendaries.contains(baseName))
        {
            if (logLegendaryCatches)
            {
                // Print a catch message to console, with the above shiny String mixed in.
                logger.info
                (
                        "§5PBR §f// §2Player §a" + player.getName() +
                        "§2 caught a legendary §a" + nameString +
                        "§2 in world \"§a" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                        "§2\", at X:§a" + location.getX() +
                        "§2 Y:§a" + location.getY() +
                        "§2 Z:§a" + location.getZ()
                );
            }

            if (printLegendaryCatches)
            {
                // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                doBroadcast(EnumBroadcastTypes.PRINT, EventData.Catches.LEGENDARY,
                        pokemon, null, player, null);
            }

            if (notifyLegendaryCatches)
            {
                // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                doBroadcast(EnumBroadcastTypes.NOTIFY, EventData.Catches.LEGENDARY,
                        pokemon, null, player, null);
            }
        }
        else if (EnumSpecies.ultrabeasts.contains(baseName) && pokemon.getPokemonData().isShiny())
        {
            if (logUltraBeastCatches || logShinyCatches)
            {
                // Print a catch message to console, with the above shiny String mixed in.
                logger.info
                (
                        "§5PBR §f// §2Player §a" + player.getName() +
                        "§2 caught a shiny §a" + nameString +
                        "§2 Ultra Beast in world \"§a" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                        "§2\", at X:§a" + location.getX() +
                        "§2 Y:§a" + location.getY() +
                        "§2 Z:§a" + location.getZ()
                );
            }

            if (printUltraBeastCatches || notifyUltraBeastCatches)
            {
                if (printUltraBeastCatches)
                {
                    // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                    doBroadcast(
                            EnumBroadcastTypes.PRINT, EventData.Catches.SHINY_ULTRA_BEAST_AS_ULTRA_BEAST,
                            pokemon, null, player, null);
                }

                if (notifyUltraBeastCatches)
                {
                    // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                    doBroadcast(
                            EnumBroadcastTypes.NOTIFY, EventData.Catches.SHINY_ULTRA_BEAST_AS_ULTRA_BEAST,
                            pokemon, null, player, null);
                }
            }
            else if (printShinyCatches || notifyShinyCatches)
            {
                if (printShinyCatches)
                {
                    // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                    doBroadcast(
                            EnumBroadcastTypes.PRINT, EventData.Catches.SHINY_ULTRA_BEAST_AS_SHINY,
                            pokemon, null, player, null);
                }

                if (notifyShinyCatches)
                {
                    // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                    doBroadcast(
                            EnumBroadcastTypes.NOTIFY, EventData.Catches.SHINY_ULTRA_BEAST_AS_SHINY,
                            pokemon, null, player, null);
                }
            }
        }
        else if (EnumSpecies.ultrabeasts.contains(baseName))
        {
            if (logUltraBeastCatches)
            {
                // Print a catch message to console, with the above shiny String mixed in.
                logger.info
                (
                        "§5PBR §f// §2Player §a" + player.getName() +
                        "§2 caught a §a" + nameString +
                        "§2 Ultra Beast in world \"§a" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                        "§2\", at X:§a" + location.getX() +
                        "§2 Y:§a" + location.getY() +
                        "§2 Z:§a" + location.getZ()
                );
            }

            if (printUltraBeastCatches)
            {
                // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                doBroadcast(EnumBroadcastTypes.PRINT, EventData.Catches.ULTRA_BEAST,
                        pokemon, null, player, null);
            }

            if (notifyUltraBeastCatches)
            {
                // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                doBroadcast(EnumBroadcastTypes.NOTIFY, EventData.Catches.ULTRA_BEAST,
                        pokemon, null, player, null);
            }
        }
        else if (pokemon.getPokemonData().isShiny())
        {
            if (logShinyCatches)
            {
                // Print a catch message to console, if enabled.
                logger.info
                (
                        "§5PBR §f// §bPlayer §3" + player.getName() +
                        "§b caught a shiny §3" + nameString +
                        "§b in world \"§3" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                        "§b\", at X:§3" + location.getX() +
                        "§b Y:§3" + location.getY() +
                        "§b Z:§3" + location.getZ()
                );
            }

            if (printShinyCatches)
            {
                // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                doBroadcast(EnumBroadcastTypes.PRINT, EventData.Catches.SHINY,
                        pokemon, null, player, null);
            }

            if (notifyShinyCatches)
            {
                // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                doBroadcast(EnumBroadcastTypes.NOTIFY, EventData.Catches.SHINY,
                        pokemon, null, player, null);
            }
        }
        else
        {
            if (logNormalCatches)
            {
                // Print a catch message to console, if enabled.
                logger.info
                (
                        "§5PBR §f// §fPlayer §7" + player.getName() +
                        "§f caught a normal §7" + nameString +
                        "§f in world \"§7" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                        "§f\", at X:§7" + location.getX() +
                        "§f Y:§7" + location.getY() +
                        "§f Z:§7" + location.getZ()
                );
            }

            if (printNormalCatches)
            {
                // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                doBroadcast(EnumBroadcastTypes.PRINT, EventData.Catches.NORMAL,
                        pokemon, null, player, null);
            }

            if (notifyNormalCatches)
            {
                // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                doBroadcast(EnumBroadcastTypes.NOTIFY, EventData.Catches.NORMAL,
                        pokemon, null, player, null);
            }
        }
    }
}
