// Listens for successful Pokémon trades.
package rs.expand.pixelmonbroadcasts.listeners;

// Remote imports.
import com.pixelmonmod.pixelmon.api.events.PixelmonTradeEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
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
        // Create shorthand variables for convenience.
        final EntityPlayer player1 = event.player1;
        final EntityPlayer player2 = event.player2;
        final Pokemon pokemon1 = event.pokemon1;
        final Pokemon pokemon2 = event.pokemon2;

        if (logTrades)
        {
            // Set up some strings for showing shinyness.
            final String pokemon1ShinynessString = pokemon1.getIsShiny() ? "shiny " : "normal ";
            final String pokemon2ShinynessString = pokemon2.getIsShiny() ? "shiny " : "normal ";

            // Set up variables for cleanly checking whether we're in a localized setup.
            final String baseName1 = pokemon1.getSpecies().getPokemonName();
            final String baseName2 = pokemon2.getSpecies().getPokemonName();
            final String localizedName1 = pokemon1.getSpecies().getLocalizedName();
            final String localizedName2 = pokemon2.getSpecies().getLocalizedName();

            // If we're in a localized setup, log both names.
            final String name1String =
                    baseName1.equals(localizedName1) ? baseName1 : baseName1 + " §d(§5" + localizedName1 + "§d)";
            final String name2String =
                    baseName2.equals(localizedName2) ? baseName2 : baseName2 + " §d(§5" + localizedName2 + "§d)";

            // Print a trade message to console.
            printBasicMessage
            (
                    "§5PBR §f// §dPlayer §5" + player1.getName() +
                    "§d has traded a " + pokemon1ShinynessString +
                    "§5" + name1String +
                    "§d for §5" + player2.getName() +
                    "§d's " + pokemon2ShinynessString +
                    "§5" + name2String
            );
        }

        if (showTrades)
        {
            // Get a broadcast from the broadcasts config file, if the key can be found.
            final String broadcast = getBroadcast("broadcast.trade");

            // Did we find a message? Iterate all available players, and send to those who should receive!
            if (broadcast != null)
            {
                // Did we find a message? Iterate all available players, and send to those who should receive!
                iterateAndSendBroadcast(broadcast, pokemon1, pokemon2, player1, player2,
                        false, true, false, "trade", "showTrade");
            }
        }
    }
}
