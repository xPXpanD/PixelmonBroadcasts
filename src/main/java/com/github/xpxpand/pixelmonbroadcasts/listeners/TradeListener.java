// Listens for successful Pokémon trades.
package com.github.xpxpand.pixelmonbroadcasts.listeners;

import com.pixelmonmod.pixelmon.api.events.PixelmonTradeEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.github.xpxpand.pixelmonbroadcasts.enums.EventData;

import static com.github.xpxpand.pixelmonbroadcasts.PixelmonBroadcasts.logger;
import static com.github.xpxpand.pixelmonbroadcasts.utilities.PlaceholderMethods.iterateAndBroadcast;

// TODO: Eggs need better support. Hiding IVs and names for the time being.
// TODO: Hoverable IVs would still be nice, but don't work with the current line-wide setup. Might not be worth it.
// NOTE: May be at risk of getting null players. If so, add a new PlaceholderMethods#getSafePlayer() implementation.
public class TradeListener
{
    // Drop event priority to lowest, and only proceed if the event is still alive by the time we get to it.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onTradeCompletedEvent(final PixelmonTradeEvent event)
    {
        if (!event.isCanceled())
        {
            // Don't pass this to PrintingMethods#logEvent(), far too messy.
            if (EventData.Others.TRADE.options() != null && EventData.Others.TRADE.options().contains("log"))
            {
                // Set up some strings for showing shinyness.
                final String pokemon1ShinyStatus = event.pokemon1.isShiny() ? "shiny " : "normal ";
                final String pokemon2ShinyStatus = event.pokemon2.isShiny() ? "shiny " : "normal ";

                // Set up variables for cleanly checking whether we're in a localized setup.
                final String baseName1 = event.pokemon1.getSpecies().getPokemonName();
                final String baseName2 = event.pokemon2.getSpecies().getPokemonName();
                final String localizedName1 = event.pokemon1.getSpecies().getLocalizedName();
                final String localizedName2 = event.pokemon2.getSpecies().getLocalizedName();

                // If we're in a localized setup, log both names.
                final String name1String =
                        baseName1.equals(localizedName1) ? baseName1 : baseName1 + " (" + localizedName1 + ")";
                final String name2String =
                        baseName2.equals(localizedName2) ? baseName2 : baseName2 + " (" + localizedName2 + ")";

                // Print a trade message to console, if enabled.
                logger.info
                (
                        "§" + EventData.Others.TRADE.color() +
                        "Player " + event.player1.getName() +
                        " has traded a " + pokemon1ShinyStatus + name1String +
                        " for " + event.player2.getName() +
                        "'s " + pokemon2ShinyStatus + name2String
                );
            }

            // Check whether any broadcasts are enabled, and send them to people who are set up to receive them.
            iterateAndBroadcast(EventData.Others.TRADE, event.pokemon1, event.pokemon2, event.player1, event.player2);
        }
    }
}
