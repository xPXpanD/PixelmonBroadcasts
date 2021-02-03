// Listens for Pokémon evolving into others. This isn't even my final form.
package rs.expand.pixelmonbroadcasts.listeners;

import com.pixelmonmod.pixelmon.api.events.EvolveEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import rs.expand.pixelmonbroadcasts.enums.EventData;

import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.logger;
import static rs.expand.pixelmonbroadcasts.utilities.PlaceholderMethods.iterateAndBroadcast;

public class EvolutionListener
{
    // Drop event priority to lowest, and only proceed if the event is still alive by the time we get to it.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPostEvolveEvent(final EvolveEvent.PostEvolve event)
    {
        if (!event.isCanceled())
        {
            // Don't pass this to PrintingMethods#logEvent(), far too messy.
            if (EventData.Others.EVOLVE.options() != null && EventData.Others.EVOLVE.options().contains("log"))
            {
                // Set up variables for cleanly checking whether we're in a localized setup.
                final String preEvoBaseName = event.preEvo.getName();
                final String postEvoBaseName = event.pokemon.getName();
                final String preEvoLocalizedName = event.preEvo.getLocalizedName();
                final String postEvoLocalizedName = event.pokemon.getLocalizedName();

                // If we're in a localized setup, log both names.
                final String preEvoNameString =
                        preEvoBaseName.equals(preEvoLocalizedName) ? preEvoBaseName : preEvoBaseName + " (" + preEvoLocalizedName + ")";
                final String postEvoNameString =
                        postEvoBaseName.equals(postEvoLocalizedName) ? postEvoBaseName : postEvoBaseName + " (" + postEvoLocalizedName + ")";

                // Print an evolution message to console, if enabled.
                logger.info
                (
                        "§" + EventData.Others.EVOLVE.color() +
                        "Player " + event.player.getName() +
                        "'s " + preEvoNameString +
                        " evolved into " + postEvoNameString + "."
                );
            }

            // Check whether any broadcasts are enabled, and send them to people who are set up to receive them.
            iterateAndBroadcast(EventData.Others.EVOLVE, event.preEvo, event.pokemon, event.player, null);
        }
    }
}
