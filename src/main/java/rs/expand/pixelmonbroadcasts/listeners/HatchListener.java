// Listens for Pokémon hatching from eggs.
package rs.expand.pixelmonbroadcasts.listeners;

import com.pixelmonmod.pixelmon.api.events.EggHatchEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import rs.expand.pixelmonbroadcasts.enums.Events;

import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.logger;

// FIXME: Eggs don't show IV percentages, so they get an extra space.
// TODO: Add UB/legendary hatches.
public class HatchListener
{
    @SubscribeEvent
    public void onHatchEvent(final EggHatchEvent event)
    {
        // Create shorthand variables for convenience.
        final String baseName = event.pokemon.getSpecies().getPokemonName();
        final String localizedName = event.pokemon.getSpecies().getLocalizedName();
        final EntityPlayer player = event.pokemon.getOwnerPlayer();
        final BlockPos location = player.getPosition();
        final World world = player.getEntityWorld();

        // If we're in a localized setup, log both names.
        final String nameString =
                baseName.equals(localizedName) ? baseName : baseName + " §7(§f" + localizedName + "§7)";

        if (event.pokemon.isShiny())
        {
            if (Events.Hatches.SHINY.settings.toLowerCase().contains("log"))
            {
                // Print a hatch message to console, if enabled.
                logger.info
                (
                        "§5PBR §f// §7Player §f" + player.getName() +
                        "§7's shiny §f" + nameString +
                        "§7 egg hatched in world \"§f" + world.getWorldInfo().getWorldName() +
                        "§7\" at X:§f" + location.getX() +
                        "§7 Y:§f" + location.getY() +
                        "§7 Z:§f" + location.getZ()
                );
            }

            if (printShinyHatches)
            {
                // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                doBroadcast(EnumBroadcastTypes.PRINT, Events.Hatches.SHINY,
                        event.pokemon, null, player, null);
            }

            if (notifyShinyHatches)
            {
                // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                doBroadcast(EnumBroadcastTypes.NOTIFY, Events.Hatches.SHINY,
                        event.pokemon, null, player, null);
            }
        }
        else
        {
            if (logNormalHatches)
            {
                // Print a hatch message to console, if enabled.
                logger.info
                (
                        "§5PBR §f// §7Player §f" + player.getName() +
                        "§7's normal §f" + nameString +
                        "§7 egg hatched in world \"§f" + world.getWorldInfo().getWorldName() +
                        "§7\" at X:§f" + location.getX() +
                        "§7 Y:§f" + location.getY() +
                        "§7 Z:§f" + location.getZ()
                );
            }

            if (printNormalHatches)
            {
                // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                doBroadcast(EnumBroadcastTypes.PRINT, Events.Hatches.NORMAL,
                        event.pokemon, null, player, null);
            }

            if (notifyNormalHatches)
            {
                // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                doBroadcast(EnumBroadcastTypes.NOTIFY, Events.Hatches.NORMAL,
                        event.pokemon, null, player, null);
            }
        }
    }
}

