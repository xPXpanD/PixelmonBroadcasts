// Listens for successful Pokémon trades.
package rs.expand.pixelmonbroadcasts.listeners;

// Remote imports.
import com.pixelmonmod.pixelmon.api.events.PixelmonTradeEvent;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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
        // Create shorthand Player variables for convenience.
        final EntityPlayer player1 = event.player1;
        final EntityPlayer player2 = event.player2;

        // Create entities to pass on from both players' Pokémon.
        final EntityPixelmon pokemon1 =
                (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(event.pokemon1, player1.getEntityWorld());
        final EntityPixelmon pokemon2 =
                (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(event.pokemon2, player2.getEntityWorld());

        if (logTrades)
        {
            String pokemon1ShinynessString = pokemon1.getIsShiny() ? "shiny " : "normal ";
            String pokemon2ShinynessString = pokemon2.getIsShiny() ? "shiny " : "normal ";

            // Print a trade message to console.
            printBasicMessage
            (
                    "§5PBR §f// Player §7" + player1.getName() +
                    "§f has traded a " + pokemon1ShinynessString +
                    "§7" + pokemon1.getLocalizedName() +
                    "§f for §7" + player2.getName() +
                    "§f's " + pokemon2ShinynessString +
                    "§7" + pokemon2.getLocalizedName()
            );
        }

        if (showTrades)
        {
            // Get a broadcast from the broadcasts config file, if the key can be found.
            String broadcast = getBroadcast("broadcast.trade");

            // Did we find a message? Iterate all available players, and send to those who should receive!
            if (broadcast != null)
            {
                // Replace the placeholders for player 2's side, first. We'll grab the normal ones in the final sweep.
                broadcast = replacePlayer2Placeholders(broadcast, pokemon2, player2);

                // Swap player 1 placeholders, and then send.
                iterateAndSendBroadcast(broadcast, pokemon1, player1, false, true, false,
                        "trade", "showTrade");
            }
        }
    }
}
