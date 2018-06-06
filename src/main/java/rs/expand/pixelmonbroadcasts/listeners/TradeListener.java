// Listens for successful Pokémon trades.
package rs.expand.pixelmonbroadcasts.listeners;

// Remote imports.
import com.pixelmonmod.pixelmon.api.events.PixelmonTradeEvent;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

// Local imports.
import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.*;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.*;
import static rs.expand.pixelmonbroadcasts.utilities.PlaceholderMethods.*;

// TODO: Add shiny status support. Something that works with multiple languages, preferably. Placeholder?
// FIXME: Eggs need better support. Hiding IVs and names for the time being.
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
                    "§5PBR §f// Player §7" + event.player1.getName() +
                    "§f has traded a §7" + event.pokemon1.getString(NbtKeys.NAME) +
                    "§f for §7" + event.player2.getName() +
                    "§f's §7" + event.pokemon2.getString(NbtKeys.NAME)
            );
        }

        if (showTrades)
        {
            // Parse placeholders and print!
            if (tradeMessage != null)
            {
                // Create shorthand Player variables for convenience.
                final EntityPlayer player1 = event.player1;
                final EntityPlayer player2 = event.player2;

                // Create entities to pass on from both players' Pokémon.
                final EntityPixelmon pokemon1Entity =
                        (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(event.pokemon1, player1.getEntityWorld());
                final EntityPixelmon pokemon2Entity =
                        (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(event.pokemon2, player2.getEntityWorld());

                // Build up an output message String, and then pass it through both sides of the placeholder parser.
                // This ensures that we have working placeholders for everything that the config can provide.
                String finalMessage;
                finalMessage = replacePlaceholders(
                        tradeMessage, player1.getName(), true, false, pokemon1Entity, player1.getPosition());
                finalMessage = replacePlaceholders(
                        finalMessage, player2.getName(), true, true, pokemon2Entity, player2.getPosition());

                // Send off the message, the needed notifier permission and the flag to check.
                iterateAndSendEventMessage(finalMessage, pokemon1Entity,
                        false, false, false, "normal.trade", "showTrade");
            }
            else
                printBasicError("The trade message is broken, broadcast failed.");
        }
    }
}
