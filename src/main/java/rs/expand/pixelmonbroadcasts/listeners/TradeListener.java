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
        // Create shorthand Player variables for convenience.
        final EntityPlayer player1 = event.player1;
        final EntityPlayer player2 = event.player2;

        // Create entities to pass on from both players' Pokémon.
        final EntityPixelmon pokemon1 =
                (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(event.pokemon1, player1.getEntityWorld());
        final EntityPixelmon pokemon2 =
                (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(event.pokemon2, player2.getEntityWorld());

        if (pokemon1.getIsShiny() || pokemon2.getIsShiny())
        {
            if (logShinyTrades)
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

            if (showShinyTrades)
            {
                // Get a broadcast from the broadcasts config file, if the key can be found.
                String broadcast = getBroadcast("broadcast.trade.normal");

                // Did we find a message? Iterate all available players, and send to those who should receive!
                if (broadcast != null)
                {
                    // Replace the placeholders for player 2's side, first. We'll grab the normal ones in the final sweep.
                    broadcast = replacePlayer2Placeholders(broadcast, player1, pokemon2, player2.getPosition());

                    // Swap player 1 placeholders, and then send.
                    iterateAndSendBroadcast(broadcast, pokemon1, player1, player1.getPosition(), false,
                            true, false, "trade.normal", "showTrade");
                }
            }
        }
        else
        {
            if (logNormalTrades)
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

            if (showNormalTrades)
            {
                // Get a broadcast from the broadcasts config file, if the key can be found.
                String broadcast = getBroadcast("broadcast.trade.normal");

                // Did we find a message? Iterate all available players, and send to those who should receive!
                if (broadcast != null)
                {
                    // Replace the placeholders for player 2's side, first. We'll grab the normal ones in the final sweep.
                    broadcast = replacePlayer2Placeholders(broadcast, player1, pokemon2, player2.getPosition());

                    // Swap player 1 placeholders, and then send.
                    iterateAndSendBroadcast(broadcast, pokemon1, player1, player1.getPosition(), false,
                            true, false, "trade.normal", "showTrade");
                }
            }
        }
    }
}
