// Listens for fainted Pokémon in parties. For the Nuzlocke crowd.
package rs.expand.pixelmonbroadcasts.listeners;

import com.pixelmonmod.pixelmon.api.events.PixelmonFaintEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import rs.expand.pixelmonbroadcasts.enums.EventData;

import static rs.expand.pixelmonbroadcasts.utilities.PlaceholderMethods.getSafePlayer;
import static rs.expand.pixelmonbroadcasts.utilities.PlaceholderMethods.iterateAndBroadcast;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.logEvent;

public class PokemonFaintListener
{
    // Drop event priority to lowest, and only proceed if the event is still alive by the time we get to it.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPartyPokemonFaintEvent(PixelmonFaintEvent event)
    {
        if (!event.isCanceled())
        {
            // Make sure our Pokémon has an owner!
            if (event.pokemon.hasOwner())
            {
                // Get a sanitized player, to work around a weird issue where "event.player" can go null.
                final EntityPlayerMP player = getSafePlayer("EventData.Others.FAINT", event.player, event.pokemon);

                // Did we get a player back? If not, things broke horribly.
                if (player != null)
                {
                    // Create more shorthand variables for convenience.
                    final String baseName = event.pokemon.getPokemonName();
                    final String localizedName = event.pokemon.getLocalizedName();

                    // If we're in a localized setup, format a string for logging both names.
                    final String nameString = baseName.equals(localizedName) ? baseName : baseName + " (" + localizedName + ")";

                    // Send a log message if we're set up to do logging for this event.
                    logEvent(EventData.Others.FAINT, player.getEntityWorld().getWorldInfo().getWorldName(),
                            player.getPosition(), player.getName(), nameString);

                    // Send enabled broadcasts to people who should receive them.
                    iterateAndBroadcast(EventData.Others.FAINT, event.pokemon, null, player, null);
                }
            }
        }
    }
}
