// Listens for successful Pokémon trades.
package rs.expand.pixelmonbroadcasts.listeners;

// Remote imports.
import com.pixelmonmod.pixelmon.api.events.PixelmonTradeEvent;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
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
        // Create shorthand variables for convenience.
        final EntityPlayer player1 = event.player1;
        final EntityPlayer player2 = event.player2;
        final BlockPos player1pos = event.player1.getPosition();
        final BlockPos player2pos = event.player2.getPosition();

        // Create entities to pass on from both players' Pokémon. A bit awkward, but it should work.
        final EntityPixelmon pokemon1 =
                event.pokemon1.getOrSpawnPixelmon(player1.getEntityWorld(), player1pos.getX(), player1pos.getY(), player1pos.getZ());
        final EntityPixelmon pokemon2 =
                event.pokemon2.getOrSpawnPixelmon(player2.getEntityWorld(), player2pos.getX(), player2pos.getY(), player2pos.getZ());

        if (logTrades)
        {
            String pokemon1ShinynessString = pokemon1.getPokemonData().getIsShiny() ? "shiny " : "normal ";
            String pokemon2ShinynessString = pokemon2.getPokemonData().getIsShiny() ? "shiny " : "normal ";

            // Print a trade message to console.
            printBasicMessage
            (
                    "§5PBR §f// §dPlayer §5" + player1.getName() +
                    "§d has traded a " + pokemon1ShinynessString +
                    "§5" + pokemon1.getPokemonName() +
                    "§d for §5" + player2.getName() +
                    "§d's " + pokemon2ShinynessString +
                    "§5" + pokemon2.getPokemonName()
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
