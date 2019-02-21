// Listens for successful Pokémon trades.
package rs.expand.pixelmonbroadcasts.listeners;

// Remote imports.
import com.pixelmonmod.pixelmon.api.events.PixelmonTradeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import rs.expand.pixelmonbroadcasts.enums.EnumBroadcastTypes;
import rs.expand.pixelmonbroadcasts.enums.EnumEvents;

// Local imports.
import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.*;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.*;
import static rs.expand.pixelmonbroadcasts.utilities.PlaceholderMethods.*;

// TODO: Eggs need better support. Hiding IVs and names for the time being.
// TODO: Hoverable IVs would still be nice, but don't work with the current line-wide setup. Might not be worth it.
public class TradeListener
{
    @SubscribeEvent
    public void onTradeCompletedEvent(final PixelmonTradeEvent event)
    {
        if (logTrades)
        {
            // Set up some strings for showing shinyness.
            final String pokemon1ShinynessString = event.pokemon1.isShiny() ? "shiny " : "normal ";
            final String pokemon2ShinynessString = event.pokemon2.isShiny() ? "shiny " : "normal ";

            // Set up variables for cleanly checking whether we're in a localized setup.
            final String baseName1 = event.pokemon1.getSpecies().getPokemonName();
            final String baseName2 = event.pokemon2.getSpecies().getPokemonName();
            final String localizedName1 = event.pokemon1.getSpecies().getLocalizedName();
            final String localizedName2 = event.pokemon2.getSpecies().getLocalizedName();

            // If we're in a localized setup, log both names.
            final String name1String =
                    baseName1.equals(localizedName1) ? baseName1 : baseName1 + " §7(§f" + localizedName1 + "§7)";
            final String name2String =
                    baseName2.equals(localizedName2) ? baseName2 : baseName2 + " §7(§f" + localizedName2 + "§7)";

            // Print a trade message to console.
            printUnformattedMessage
            (
                    "§5PBR §f// §7Player §f" + event.player1.getName() +
                    "§7 has traded a " + pokemon1ShinynessString + "§f" + name1String +
                    "§7 for §f" + event.player2.getName() +
                    "§7's " + pokemon2ShinynessString + "§f" + name2String
            );
        }

        if (printTrades)
        {
            // Replace placeholders with our message, if it exists. Send to the chats of online-and-enabled players.
            replacePlaceholdersAndSend(
                    EnumBroadcastTypes.PRINT, EnumEvents.Trades.NORMAL, event.pokemon1, event.pokemon2, event.player1, event.player2);
        }

        if (notifyTrades)
        {
            // Replace placeholders with our message, if it exists. Send to the chats of online-and-enabled players.
            replacePlaceholdersAndSend(
                    EnumBroadcastTypes.NOTIFY, EnumEvents.Trades.NORMAL, event.pokemon1, event.pokemon2, event.player1, event.player2);
        }
    }
}
