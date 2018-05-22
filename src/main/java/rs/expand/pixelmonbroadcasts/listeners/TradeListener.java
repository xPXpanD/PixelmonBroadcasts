// Listens for successful Pokémon trades.
package rs.expand.pixelmonbroadcasts.listeners;

// Remote imports.
import com.pixelmonmod.pixelmon.api.events.PixelmonTradeEvent;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

// Local imports.
import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.*;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.*;
import static rs.expand.pixelmonbroadcasts.utilities.PlaceholderMethods.*;

// TODO: Add shiny status support. Something that works with multiple languages, preferably. Placeholder?
public class TradeListener
{
    @SubscribeEvent
    public void onTradeCompletedEvent(final PixelmonTradeEvent event)
    {
        if (logTrades)
        {
            // Print a trade message to console.
            printBasicMessage
            (
                    "§5PBR §f// §8Player §7" + event.player1.getName() +
                    "§8 has traded a §7" + event.pokemon1.getString(NbtKeys.NAME) +
                    "§8 for §7" + event.player2.getName() +
                    "§8's §7" + event.pokemon2.getString(NbtKeys.NAME)
            );
        }

        if (showTradeMessage)
        {
            // Parse placeholders and print!
            if (tradeMessage != null)
            {
                // Create entities to pass on from both players' Pokémon.
                final EntityPixelmon pokemon1Entity =
                        (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(event.pokemon1, event.player1.getEntityWorld());
                final EntityPixelmon pokemon2Entity =
                        (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(event.pokemon2, event.player2.getEntityWorld());

                // Build up an output message String, and then pass it through both sides of the placeholder parser.
                // This ensures that we have working placeholders for everything that the config can provide.
                String finalMessage;
                finalMessage = replacePlaceholders(
                        tradeMessage, event.player1.getName(), pokemon1Entity, event.player1.getPosition());
                finalMessage = replaceAltPlayerPlaceholders(
                        finalMessage, event.player2.getName(), pokemon2Entity, event.player2.getPosition());

                // Send off the message, the needed notifier permission and the flag to check.
                iterateAndSendEventMessage(finalMessage, "trade", "showTrade", event.pokemon1, event.pokemon2);
            }
            else
                printBasicError("The trade message is broken, broadcast failed.");
        }
    }
}
