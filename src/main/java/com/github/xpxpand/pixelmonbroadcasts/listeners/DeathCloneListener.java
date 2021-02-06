// Listens for player entities being cloned. Acts if this is happening after death, so that toggles can be restored.
package com.github.xpxpand.pixelmonbroadcasts.listeners;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.github.xpxpand.pixelmonbroadcasts.PixelmonBroadcasts.logger;

public class DeathCloneListener
{
    // Drop event priority to lowest, and only proceed if the event is still alive by the time we get to it.
    // Very important, as we want to gracefully handle any other plugin/mod already having cloned the old entity's NBT.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerCloneEvent(final PlayerEvent.Clone event)
    {
        // Did the player die?
        if (event.isWasDeath())
        {
            // Did the player have any Broadcasts toggles set?
            if (!event.getOriginal().getEntityData().getCompoundTag("pbToggles").hasNoTags())
            {
                // Grab the original toggles.
                final NBTTagCompound tagCompound = event.getOriginal().getEntityData().getCompoundTag("pbToggles");

                // Check if the new entity has any toggles. There shouldn't be any, but some mods/plugins may mess with things.
                if (event.getEntityPlayer().getEntityData().getCompoundTag("pbToggles").hasNoTags())
                {
                    // Apply the old toggles to the new player entity, in the right place.
                    event.getEntityPlayer().getEntityData().setTag("pbToggles", tagCompound);
                }
                else
                {
                    // Toss some debug. Do it at info level so we can be sure it's printed to normal consoles.
                    // TODO: Remove at some point, once we have enough data. (or none; if nothing hits this I won't complain!)
                    logger.info("A player died and had a new entity created, but toggles were already set? Probably due to another plugin/mod.");
                    logger.info("Please check whether this player's toggles still exist. I (XpanD) can't test this, so let me know what happened!");
                    logger.info("Player name: " + event.getEntityPlayer().getName());
                }
            }
        }
    }
}
