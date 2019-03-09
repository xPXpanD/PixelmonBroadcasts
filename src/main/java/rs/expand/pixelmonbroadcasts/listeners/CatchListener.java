// Listens for Pokémon captures with balls.
package rs.expand.pixelmonbroadcasts.listeners;

import com.pixelmonmod.pixelmon.api.events.CaptureEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import rs.expand.pixelmonbroadcasts.enums.EventData;

import static rs.expand.pixelmonbroadcasts.utilities.PlaceholderMethods.iterateAndBroadcast;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.logEvent;

public class CatchListener
{
    @SubscribeEvent
    public void onCatchPokemonEvent(final CaptureEvent.SuccessfulCapture event)
    {
        // Create shorthand variables for convenience.
        final EntityPixelmon pokemonEntity = event.getPokemon();
        final Pokemon pokemon = pokemonEntity.getPokemonData();
        final BlockPos location = event.pokeball.getPosition();
        final String baseName = pokemonEntity.getSpecies().getPokemonName();
        final String localizedName = pokemonEntity.getSpecies().getLocalizedName();
        final String worldName = event.player.world.getWorldInfo().getWorldName();

        // Sets the position of the entity we created, as entity coordinates get real weird in this event. Dunno why.
        pokemonEntity.setPosition(location.getX(), location.getY(), location.getZ());

        // If we're in a localized setup, log both names.
        final String nameString = baseName.equals(localizedName) ? baseName : baseName + " (" + localizedName + ")";

        if (EnumSpecies.legendaries.contains(baseName))
        {
            if (pokemon.isShiny())
            {
                // Send a log message if we're set up to do logging for this event.
                logEvent(EventData.Catches.SHINY_LEGENDARY,
                        worldName, location, event.player.getName(), "shiny legendary " + nameString);

                // Send enabled broadcasts to people who should receive them.
                iterateAndBroadcast(EventData.Catches.SHINY_LEGENDARY,
                        pokemon, null, event.player, null);
            }
            else
            {
                // Send a log message if we're set up to do logging for this event.
                logEvent(EventData.Catches.LEGENDARY,
                        worldName, location, event.player.getName(), "legendary " + nameString);

                // Send enabled broadcasts to people who should receive them.
                iterateAndBroadcast(EventData.Catches.LEGENDARY,
                        pokemon, null, event.player, null);
            }
        }
        else if (EnumSpecies.ultrabeasts.contains(baseName))
        {
            if (pokemon.isShiny())
            {
                // Send a log message if we're set up to do logging for this event.
                logEvent(EventData.Catches.SHINY_ULTRA_BEAST,
                        worldName, location, event.player.getName(), "shiny " + nameString + " Ultra Beast");

                // Send enabled broadcasts to people who should receive them.
                iterateAndBroadcast(EventData.Catches.SHINY_ULTRA_BEAST,
                        pokemon, null, event.player, null);
            }
            else
            {
                // Send a log message if we're set up to do logging for this event.
                logEvent(EventData.Catches.ULTRA_BEAST,
                        worldName, location, event.player.getName(), nameString + " Ultra Beast");

                // Send enabled broadcasts to people who should receive them.
                iterateAndBroadcast(EventData.Catches.ULTRA_BEAST,
                        pokemon, null, event.player, null);
            }
        }
        else if (pokemon.isShiny())
        {
            // Send a log message if we're set up to do logging for this event.
            logEvent(EventData.Catches.SHINY,
                    worldName, location, event.player.getName(), "shiny " + nameString);

            // Send enabled broadcasts to people who should receive them.
            iterateAndBroadcast(EventData.Catches.SHINY,
                    pokemon, null, event.player, null);
        }
        else
        {
            // Send a log message if we're set up to do logging for this event.
            logEvent(EventData.Catches.NORMAL,
                    worldName, location, event.player.getName(), nameString);

            // Send enabled broadcasts to people who should receive them.
            iterateAndBroadcast(EventData.Catches.NORMAL,
                    pokemon, null, event.player, null);
        }
    }
}
