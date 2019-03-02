// Listens for Pokémon that get defeated by players.
package rs.expand.pixelmonbroadcasts.listeners;

import com.pixelmonmod.pixelmon.api.events.BeatWildPixelmonEvent;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import rs.expand.pixelmonbroadcasts.enums.EventData;

import static rs.expand.pixelmonbroadcasts.utilities.PlaceholderMethods.iterateAndBroadcast;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.logEvent;

// TODO: Log/announce boss types? Mega vs normal.
// FIXME: Self-sacrifice moves like Explosion do not seem to fire this.
public class WildDefeatListener
{
    @SubscribeEvent
    public void onBeatWildPokemonEvent(final BeatWildPixelmonEvent event)
    {
        // Create shorthand variables for convenience.
        final EntityPixelmon pokemonEntity = (EntityPixelmon) event.wpp.getEntity();
        final String baseName = pokemonEntity.getSpecies().getPokemonName();
        final String localizedName = pokemonEntity.getSpecies().getLocalizedName();
        final BlockPos location = pokemonEntity.getPosition();
        final String worldName = pokemonEntity.getEntityWorld().getWorldInfo().getWorldName();

        // If we're in a localized setup, log both names.
        final String nameString =
                baseName.equals(localizedName) ? baseName : baseName + " (" + localizedName + ")";

        if (pokemonEntity.isBossPokemon())
        {
            if (EventData.Victories.BOSS.checkSettingsOrError("bossVictoryOptions"))
            {
                // Send a log message if we're set up to do logging for this event.
                logEvent(EventData.Victories.BOSS,
                        worldName, location, event.player.getName(),  "boss " + nameString);

                // Send enabled broadcasts to people who should receive them.
                iterateAndBroadcast(EventData.Victories.BOSS,
                        pokemonEntity, null, event.player, null);
            }
        }
        else if (EnumSpecies.legendaries.contains(baseName))
        {
            if (pokemonEntity.getPokemonData().isShiny())
            {
                if (EventData.Victories.SHINY_LEGENDARY.checkSettingsOrError(
                        "legendaryVictoryOptions", "shinyVictoryOptions"))
                {
                    // Send a log message if we're set up to do logging for this event.
                    logEvent(EventData.Victories.SHINY_LEGENDARY,
                            worldName, location, event.player.getName(), "shiny legendary " + nameString);

                    // Send enabled broadcasts to people who should receive them.
                    iterateAndBroadcast(EventData.Victories.SHINY_LEGENDARY,
                            pokemonEntity, null, event.player, null);
                }
            }
            else
            {
                if (EventData.Victories.LEGENDARY.checkSettingsOrError("legendaryVictoryOptions"))
                {
                    // Send a log message if we're set up to do logging for this event.
                    logEvent(EventData.Victories.LEGENDARY,
                            worldName, location, event.player.getName(),  "legendary " + nameString);

                    // Send enabled broadcasts to people who should receive them.
                    iterateAndBroadcast(EventData.Victories.LEGENDARY,
                            pokemonEntity, null, event.player, null);
                }
            }
        }
        else if (EnumSpecies.ultrabeasts.contains(baseName))
        {
            if (pokemonEntity.getPokemonData().isShiny())
            {
                if (EventData.Victories.SHINY_ULTRA_BEAST.checkSettingsOrError(
                        "ultraBeastVictoryOptions", "shinyVictoryOptions"))
                {
                    // Send a log message if we're set up to do logging for this event.
                    logEvent(EventData.Victories.SHINY_ULTRA_BEAST,
                            worldName, location, event.player.getName(),  "shiny " + nameString + " Ultra Beast");

                    // Send enabled broadcasts to people who should receive them.
                    iterateAndBroadcast(EventData.Victories.SHINY_ULTRA_BEAST,
                            pokemonEntity, null, event.player, null);
                }
            }
            else
            {
                if (EventData.Victories.ULTRA_BEAST.checkSettingsOrError("ultraBeastVictoryOptions"))
                {
                    // Send a log message if we're set up to do logging for this event.
                    logEvent(EventData.Victories.ULTRA_BEAST,
                            worldName, location, event.player.getName(),  "normal " + nameString + " Ultra Beast");

                    // Send enabled broadcasts to people who should receive them.
                    iterateAndBroadcast(EventData.Victories.ULTRA_BEAST,
                            pokemonEntity, null, event.player, null);
                }
            }
        }
        else if (pokemonEntity.getPokemonData().isShiny())
        {
            if (EventData.Victories.SHINY.checkSettingsOrError("shinyVictoryOptions"))
            {
                // Send a log message if we're set up to do logging for this event.
                logEvent(EventData.Victories.SHINY,
                        worldName, location, event.player.getName(),  "shiny " + nameString);

                // Send enabled broadcasts to people who should receive them.
                iterateAndBroadcast(EventData.Spawns.SHINY,
                        pokemonEntity, null, event.player, null);
            }
        }
    }
}
