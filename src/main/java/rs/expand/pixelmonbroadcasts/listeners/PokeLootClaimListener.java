/*// Listens for people looting Pokéloot chests. TODO: Figure out a way to get world loot... Dead end, it seems.
package rs.expand.pixelmonbroadcasts.listeners;

import com.pixelmonmod.pixelmon.api.events.PokeLootClaimedEvent;
import com.pixelmonmod.pixelmon.blocks.enums.EnumPokeChestType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import rs.expand.pixelmonbroadcasts.enums.EventData;

import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.logger;
import static rs.expand.pixelmonbroadcasts.utilities.PlaceholderMethods.iterateAndBroadcast;

public class PokeLootClaimListener
{
    // Drop event priority to lowest, and only proceed if the event is still alive by the time we get to it.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPokeLootClaimEvent(final PokeLootClaimedEvent event)
    {
        if (!event.isCanceled())
        {
            if (event.chest.getType() != EnumPokeChestType.SPECIAL)
            {
                final ItemStack[] items = event.chest.getCustomDrops();

                if (items.length != 0)
                {
                    // Don't pass this to PrintingMethods#logEvent(), far too messy.
                    if (EventData.Others.LOOT.options() != null && EventData.Others.LOOT.options().contains("log"))
                    {
                        // Print a loot message to console, if enabled.
                        logger.info
                        (
                                '§' + EventData.Others.LOOT.color() +
                                "Player " + event.player.getName() +
                                " looted " + items[0].getCount() +
                                " " + items[0].getDisplayName() +
                                " from a chest of the " + event.chest.getType().name() +
                                " type."
                        );
                    }

                    // Check whether any broadcasts are enabled, and send them to people who are set up to receive them.
                    iterateAndBroadcast(EventData.Others.LOOT, items, null, event.player, null);
                }
                else
                    logger.error("Could not find an item on a Pokéloot claim event! Please report this.");
            }
            else
                logger.error("Hey, we got a SPECIAL chest type. Gift?");
        }
    }
}*/


/*
            else if (object1 instanceof ItemStack[])
            {
                // Make the item stack a bit easier to access.
                final ItemStack[] items = (ItemStack[]) object1;

                // Swap. We've already validated the ItemStack array is not empty before sending.
                broadcast = broadcast.replaceAll("(?i)%item%", items[0].getDisplayName());
            }*/

/*
            // If we're still running, did we get a Pokémon? This is only null if we got an ItemStack array before.
            if (pokemon != null)
            {*/

/*
            if (canReceiveBroadcast(src, EventData.Others.LOOT))
            {
                flags.add("showLoot");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showLoot"))
                    messages.add(getTranslation("toggle.loot.on") + separator);
                else
                    messages.add(getTranslation("toggle.loot.off") + separator);
            }*/
