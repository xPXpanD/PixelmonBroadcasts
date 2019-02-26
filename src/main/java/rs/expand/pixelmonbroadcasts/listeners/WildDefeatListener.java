// Listens for Pokémon that get defeated by players.
package rs.expand.pixelmonbroadcasts.listeners;


import com.pixelmonmod.pixelmon.api.events.BeatWildPixelmonEvent;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import rs.expand.pixelmonbroadcasts.enums.EventData;

// TODO: Log/announce boss types? Mega vs normal.
// FIXME: Self-sacrifice moves like Explosion do not seem to fire this.
public class WildDefeatListener
{
    @SubscribeEvent
    public void onBeatWildPokemonEvent(final BeatWildPixelmonEvent event)
    {
        // Create shorthand variables for convenience.
        final EntityPixelmon pokemon = (EntityPixelmon) event.wpp.getEntity();
        final String baseName = pokemon.getSpecies().getPokemonName();
        final String localizedName = pokemon.getSpecies().getLocalizedName();
        final BlockPos location = pokemon.getPosition();

        // If we're in a localized setup, log both names.
        final String nameString =
                baseName.equals(localizedName) ? baseName : baseName + " §4(§c" + localizedName + "§4)";

        if (pokemon.isBossPokemon())
        {
            if (logBossVictories)
            {
                // Print a victory message to console, if enabled.
                logger.info
                (
                        "§5PBR §f// §4Player §c" + event.player.getName() +
                        "§4 defeated a boss §c" + nameString +
                        "§4 boss in world \"§c" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                        "§4\", at X:§c" + location.getX() +
                        "§4 Y:§c" + location.getY() +
                        "§4 Z:§c" + location.getZ()
                );
            }

            if (printBossVictories)
            {
                // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                doBroadcast(EnumBroadcastTypes.PRINT, EventData.Victories.BOSS,
                        pokemon, null, event.player, null);
            }

            if (notifyBossVictories)
            {
                // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                doBroadcast(EnumBroadcastTypes.NOTIFY, EventData.Victories.BOSS,
                        pokemon, null, event.player, null);
            }
        }
        else if (EnumSpecies.legendaries.contains(baseName) && pokemon.getPokemonData().isShiny())
        {
            if (logLegendaryVictories || logShinyVictories)
            {
                // Print a victory message to console, with the above shiny String mixed in.
                logger.info
                (
                        "§5PBR §f// §4Player §c" + event.player.getName() +
                        "§4 defeated a shiny legendary §c" + nameString +
                        "§4 in world \"§c" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                        "§4\", at X:§c" + location.getX() +
                        "§4 Y:§c" + location.getY() +
                        "§4 Z:§c" + location.getZ()
                );
            }

            if (printLegendaryVictories || notifyLegendaryVictories)
            {
                if (printLegendaryVictories)
                {
                    // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                    doBroadcast(
                            EnumBroadcastTypes.PRINT, EventData.Victories.SHINY_LEGENDARY_AS_LEGENDARY,
                            pokemon, null, event.player, null);
                }

                if (notifyLegendaryVictories)
                {
                    // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                    doBroadcast(
                            EnumBroadcastTypes.NOTIFY, EventData.Victories.SHINY_LEGENDARY_AS_LEGENDARY,
                            pokemon, null, event.player, null);
                }
            }
            else if (printShinyVictories || notifyShinyVictories)
            {
                if (printShinyVictories)
                {
                    // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                    doBroadcast(
                            EnumBroadcastTypes.PRINT, EventData.Victories.SHINY_LEGENDARY_AS_SHINY,
                            pokemon, null, event.player, null);
                }

                if (notifyShinyVictories)
                {
                    // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                    doBroadcast(
                            EnumBroadcastTypes.NOTIFY, EventData.Victories.SHINY_LEGENDARY_AS_SHINY,
                            pokemon, null, event.player, null);
                }
            }
        }
        else if (EnumSpecies.legendaries.contains(baseName))
        {
            if (logLegendaryVictories)
            {
                // Print a victory message to console, with the above shiny String mixed in.
                logger.info
                (
                        "§5PBR §f// §4Player §c" + event.player.getName() +
                        "§4 defeated a legendary §c" + nameString +
                        "§4 in world \"§c" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                        "§4\", at X:§c" + location.getX() +
                        "§4 Y:§c" + location.getY() +
                        "§4 Z:§c" + location.getZ()
                );
            }

            if (printLegendaryVictories)
            {
                // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                doBroadcast(EnumBroadcastTypes.PRINT, EventData.Victories.LEGENDARY,
                        pokemon, null, event.player, null);
            }

            if (notifyLegendaryVictories)
            {
                // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                doBroadcast(EnumBroadcastTypes.NOTIFY, EventData.Victories.LEGENDARY,
                        pokemon, null, event.player, null);
            }
        }
        else if (pokemon.getPokemonData().isShiny())
        {
            if (logShinyVictories)
            {
                // Print a victory message to console, if enabled.
                logger.info
                (
                        "§5PBR §f// §4Player §c" + event.player.getName() +
                        "§4 defeated a shiny §c" + nameString +
                        "§4 in world \"§c" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                        "§4\", at X:§c" + location.getX() +
                        "§4 Y:§c" + location.getY() +
                        "§4 Z:§c" + location.getZ()
                );
            }

            if (printShinyVictories)
            {
                // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                doBroadcast(EnumBroadcastTypes.PRINT, EventData.Victories.SHINY,
                        pokemon, null, event.player, null);
            }

            if (notifyShinyVictories)
            {
                // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                doBroadcast(EnumBroadcastTypes.NOTIFY, EventData.Victories.SHINY,
                        pokemon, null, event.player, null);
            }
        }
    }
}
