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
            final String pokemon1ShinynessString = pokemon1.isShiny() ? "shiny " : "normal ";
            final String pokemon2ShinynessString = pokemon2.isShiny() ? "shiny " : "normal ";

            // Set up variables for cleanly checking whether we're in a localized setup.
            final String baseName1 = pokemon1.getSpecies().getPokemonName();
            final String baseName2 = pokemon2.getSpecies().getPokemonName();
            final String localizedName1 = pokemon1.getSpecies().getLocalizedName();
            final String localizedName2 = pokemon2.getSpecies().getLocalizedName();

            // If we're in a localized setup, log both names.
            final String name1String =
                    baseName1.equals(localizedName1) ? baseName1 : baseName1 + " §7(§f" + localizedName1 + "§7)";
            final String name2String =
                    baseName2.equals(localizedName2) ? baseName2 : baseName2 + " §7(§f" + localizedName2 + "§7)";

            // Print a trade message to console.
            printUnformattedMessage
            (
                    "§5PBR §f// §7Player §f" + player1.getName() +
                    "§7 has traded a " + pokemon1ShinynessString + "§f" + name1String +
                    "§7 for §f" + player2.getName() +
                    "§7's " + pokemon2ShinynessString + "§f" + name2String
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
