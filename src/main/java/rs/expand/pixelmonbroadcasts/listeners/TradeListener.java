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

// Note: All the main class stuff and printing stuff is added through static imports.
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
                    "§5PBR §f// §7Player §8" + event.player1.getName() +
                    "§7 has traded a §8" + event.pokemon1.getString(NbtKeys.NAME) +
                    "§7 for §8" + event.player2.getName() +
                    "§7's §8" + event.pokemon2.getString(NbtKeys.NAME)
            );
        }

        if (showTradeMessage)
        {
            // Parse placeholders and print!
            if (tradeMessage != null)
            {
                // Create an entity to pass on from player 1's Pokémon.
                final EntityPixelmon sentPokemonEntity =
                        (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(event.pokemon1, event.player1.getEntityWorld());

                // Do the same for player 2.
                final EntityPixelmon receivedPokemonEntity =
                        (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(event.pokemon2, event.player2.getEntityWorld());

                // Build up an output message String, and then pass it through both sides of the placeholder parser.
                // This ensures that we have working placeholders for everything on the player AND target sides.
                String finalMessage;
                finalMessage = replacePlaceholders(
                        tradeMessage, event.player1.getName(), sentPokemonEntity, event.player1.getPosition());
                finalMessage = replaceAltPlayerPlaceholders(
                        finalMessage, event.player2.getName(), receivedPokemonEntity, event.player2.getPosition());

                // Send off the message, the needed notifier permission and the flag to check.
                iterateAndSendEventMessage(finalMessage, "trade", "showTrade");
            }
            else
                printBasicError("The trade message is broken, broadcast failed.");
        }
    }
}
