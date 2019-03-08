// Listens for people looting Pokéloot chests.
package rs.expand.pixelmonbroadcasts.listeners;

import com.pixelmonmod.pixelmon.api.events.PokeLootClaimedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import rs.expand.pixelmonbroadcasts.enums.EventData;

import static rs.expand.pixelmonbroadcasts.utilities.PlaceholderMethods.iterateAndBroadcast;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.logEvent;

public class PokeLootClaimListener
{
    @SubscribeEvent
    public void onPokeLootClaimEvent(final PokeLootClaimedEvent event)
    {
        if (EventData.Others.LOOT_CLAIM.checkSettingsOrError("lootClaimOptions"))
        {
            // Don't pass this to PrintingMethods#logEvent(), far too messy.
            if (EventData.Others.LOOT_CLAIM.options.contains("log"))
            {
                // Create a pretty string for the type of ball we have.
                final String lootType;
                switch (event.chest.getType())
                {
                    case MASTERBALL:
                        lootType = "Master Ball"; break;
                    case ULTRABALL:
                        lootType = "Ultra Ball"; break;
                    case POKEBALL:
                        lootType = "Poké Ball"; break;
                    case BEASTBALL:
                        lootType = "Beast Ball"; break;
                    default:
                        lootType = "no clue"; break; // TODO: Acquire clue.
                }

                if (EventData.Others.LOOT_CLAIM.checkSettingsOrError("bossSpawnOptions"))
                {
                    // Send a log message if we're set up to do logging for this event.
                    logEvent(EventData.Others.LOOT_CLAIM, event.player.getEntityWorld().getWorldInfo().getWorldName(),
                            event.player.getPosition(), event.player.getName(), lootType + " chest");

                    // Send enabled broadcasts to people who should receive them.
                    iterateAndBroadcast(EventData.Others.LOOT_CLAIM,
                            null, null, event.player, null);
                }
            }

            // Check whether any broadcasts are enabled, and send them to people who are set up to receive them.
            iterateAndBroadcast(EventData.Others.LOOT_CLAIM, event.pokemon1, event.pokemon2, event.player1, event.player2);
        }
    }
}
