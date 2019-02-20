// Listens for Pokémon hatching from eggs.
package rs.expand.pixelmonbroadcasts.listeners;

// Remote imports.
import com.pixelmonmod.pixelmon.api.events.EggHatchEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

// Local imports.
import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.*;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.*;
import static rs.expand.pixelmonbroadcasts.utilities.PlaceholderMethods.*;

// FIXME: Eggs don't show IV percentages, so they get an extra space.
public class HatchListener
{
    @SubscribeEvent
    public void onHatchEvent(final EggHatchEvent event)
    {
        // Create shorthand variables for convenience.
        final Pokemon pokemon = event.pokemon;
        final String baseName = pokemon.getSpecies().getPokemonName();
        final String localizedName = pokemon.getSpecies().getLocalizedName();
        final EntityPlayer player = pokemon.getOwnerPlayer();
        final BlockPos location = player.getPosition();
        final World world = player.getEntityWorld();

        // If we're in a localized setup, log both names.
        final String nameString =
                baseName.equals(localizedName) ? baseName : baseName + " §7(§f" + localizedName + "§7)";

        if (pokemon.isShiny())
        {
            if (logShinyHatches)
            {
                // Print a hatch message to console.
                printUnformattedMessage
                (
                        "§5PBR §f// §7Player §f" + player.getName() +
                        "§7's shiny §f" + nameString +
                        "§7 egg hatched in world \"§f" + world.getWorldInfo().getWorldName() +
                        "§7\" at X:§f" + location.getX() +
                        "§7 Y:§f" + location.getY() +
                        "§7 Z:§f" + location.getZ()
                );
            }

            if (showShinyHatches)
            {
                // Get a broadcast from the broadcasts config file, if the key can be found.
                final String broadcast = getBroadcast("broadcast.hatch.shiny");

                // Did we find a message? Iterate all available players, and send to those who should receive!
                if (broadcast != null)
                {
                    iterateAndSendBroadcast(broadcast, pokemon, null, player,
                            null, hoverShinyHatches, true, revealShinyHatches,
                            "hatch.shiny", "showShinyHatch");
                }
            }
        }
        else
        {
            if (logNormalHatches)
            {
                // Print a hatch message to console.
                printUnformattedMessage
                (
                        "§5PBR §f// §7Player §f" + player.getName() +
                        "§7's normal §f" + nameString +
                        "§7 egg hatched in world \"§f" + world.getWorldInfo().getWorldName() +
                        "§7\" at X:§f" + location.getX() +
                        "§7 Y:§f" + location.getY() +
                        "§7 Z:§f" + location.getZ()
                );
            }

            if (showNormalHatches)
            {
                // Get a broadcast from the broadcasts config file, if the key can be found.
                final String broadcast = getBroadcast("broadcast.hatch.normal");

                // Did we find a message? Iterate all available players, and send to those who should receive!
                if (broadcast != null)
                {
                    iterateAndSendBroadcast(broadcast, pokemon, null, player,
                            null, hoverNormalHatches, true, revealNormalHatches,
                            "hatch.normal", "showNormalHatch");
                }
            }
        }
    }
}

