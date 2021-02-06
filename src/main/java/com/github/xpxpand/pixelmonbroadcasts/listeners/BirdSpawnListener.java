/*
// Listens for people using bird shrines. Work in progress, currently commented out in production code.
package com.github.xpxpand.pixelmonbroadcasts.listeners;


//import static com.github.xpxpand.pixelmonbroadcasts.PixelmonBroadcasts.logBirdTrioSummons;
//import static com.github.xpxpand.pixelmonbroadcasts.PixelmonBroadcasts.printBirdTrioSummons;

public class BirdSpawnListener
{
    // Drop event priority to lowest, and only proceed if the event is still alive by the time we get to it.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onActivateBirdShrineEvent(final PlayerActivateShrineEvent event)
    {
        if (!event.isCanceled())
        {
            // Create shorthand variables for convenience.
            final String broadcast;
            final EntityPlayer player = event.player;
            final String pokemonName = event.shrineType.name();
            final BlockPos location = event.player.getPosition();

            if (logBirdTrioSummons)
            {
                // Print a summon message to console, if enabled.
                logger.info
                (
                        "§ePlayer §6" + player.getName() +
                        "§e has summoned a §6" + pokemonName +
                        "§e in world \"§6" + player.getEntityWorld().getWorldInfo().getWorldName() +
                        "§e\", at X:§6" + location.getX() +
                        "§e Y:§6" + location.getY() +
                        "§e Z:§6" + location.getZ()
                );
            }

            if (showBirdTrioSummons)
            {
                // See which of the three birds we got.
                switch (event.shrineType)
                {
                    case Articuno:
                        broadcast = getBroadcast("broadcast.summon.articuno"); break;
                    case Moltres:
                        broadcast = getBroadcast("broadcast.summon.moltres"); break;
                    default: // Three names in the enum, so just default to the rarest. Ensures a broadcast is always present.
                        broadcast = getBroadcast("broadcast.summon.zapdos"); break;
                }

                // Did we find a message? Iterate all available players, and send to those who should receive!
                if (broadcast != null)
                {
                    iterateAndSendBroadcast(broadcast, null, player, false,
                            true, false, "summon.birdtrio", "showBirdTrioSummon");
                }
            }
        }
    }
}
*/
