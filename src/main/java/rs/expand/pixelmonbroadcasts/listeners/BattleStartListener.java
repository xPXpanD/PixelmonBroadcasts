// Listens for started battles.
package rs.expand.pixelmonbroadcasts.listeners;

// Remote imports.
import com.pixelmonmod.pixelmon.api.events.BattleStartedEvent;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.TrainerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.WildPixelmonParticipant;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

// Local imports.
import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.*;
import static rs.expand.pixelmonbroadcasts.utilities.PlaceholderMethods.iterateAndSendEventMessage;
import static rs.expand.pixelmonbroadcasts.utilities.PlaceholderMethods.replacePlaceholders;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.printBasicError;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.printBasicMessage;

// FIXME: Pokémon using moves like Teleport to warp away from you show up as YOU having fled.
public class BattleStartListener
{
    @SubscribeEvent
    public void onBattleStartEvent(final BattleStartedEvent event)
    {
        // Reject extra participants, for now. Probably needs some work to support double battles, later.
        BattleParticipant participant1 = event.participant1[0];
        BattleParticipant participant2 = event.participant2[0];

        // Are there any players in this battle?
        if (participant1 instanceof PlayerParticipant || participant2 instanceof PlayerParticipant)
        {
            // Did a PvP battle just start? (two players, one on either side)
            if (participant1 instanceof PlayerParticipant && participant2 instanceof PlayerParticipant)
            {
                if (logPVPStarts)
                {
                    final BlockPos location = participant1.getEntity().getPosition();

                    // Print a PvP starting message to console.
                    printBasicMessage
                    (
                            "§5PBR §f// §7Player §f" + participant1.getDisplayName() +
                            "§7 started battling player §f" + participant2.getDisplayName() +
                            "§7 in world \"§f" + participant1.getWorld().getWorldInfo().getWorldName() +
                            "§7\", at X:§f" + location.getX() +
                            "§7 Y:§f" + location.getY() +
                            "§7 Z:§f" + location.getZ()
                    );
                }

                if (showPVPStarts)
                {
                    // Parse placeholders and print!
                    if (pvpStartMessage != null)
                    {
                        // Create short variables for convenience.
                        final BlockPos winPos = ((PlayerParticipant) participant1).player.getPosition();
                        final BlockPos losePos = ((PlayerParticipant) participant2).player.getPosition();

                        // Set up our message. This is the same for all eligible players, so call it once and store it.
                        String finalMessage;
                        finalMessage = replacePlaceholders(pvpStartMessage, participant1.getDisplayName(),
                                false, false, null, winPos);
                        finalMessage = replacePlaceholders(finalMessage, participant2.getDisplayName(),
                                false, true, null, losePos);

                        // Send off the message, the needed notifier permission and the flag to check.
                        // Pass null for the Pokémon, as we don't have one. Automatically disables some placeholders.
                        iterateAndSendEventMessage(finalMessage, null,
                                false, true, false, "start.pvp", "showPVPStart");
                    }
                    else
                        printBasicError("The PvP battle start message is broken, broadcast failed.");
                }
            }
            // Are there any trainer NPCs in the battle?
            else if (participant1 instanceof TrainerParticipant || participant2 instanceof TrainerParticipant)
            {
                // Create some variables for the player and trainer.
                final PlayerParticipant player;
                final TrainerParticipant npc;

                // See which side of the battle has our player, and which side has our NPC trainer. Fill things in.
                if (participant1 instanceof PlayerParticipant)
                {
                    player = (PlayerParticipant) participant1;
                    npc = (TrainerParticipant) participant2;
                }
                else
                {
                    player = (PlayerParticipant) participant2;
                    npc = (TrainerParticipant) participant1;
                }

                // Set up even more variables.
                final String playerName = player.getDisplayName();
                final String worldName = player.getWorld().getWorldInfo().getWorldName();
                final BlockPos location = player.getEntity().getPosition();

                if (npc.trainer.getBossMode().isBossPokemon())
                {
                    if (logBossTrainerChallenges)
                    {
                        // Print a challenge message to console.
                        printBasicMessage
                        (
                                "§5PBR §f// §7Player §f" + playerName +
                                "§7 challenged a §fboss trainer §7in world \"§f" + worldName +
                                "§7\", at X:§f" + location.getX() +
                                "§7 Y:§f" + location.getY() +
                                "§7 Z:§f" + location.getZ()
                        );
                    }

                    if (showBossTrainerChallenges)
                    {
                        // Parse placeholders and print!
                        if (bossTrainerChallengeMessage != null)
                        {
                            // Set up our message. This is the same for all eligible players, so call it once and store it.
                            final String finalMessage = replacePlaceholders(bossTrainerChallengeMessage,
                                    playerName, false, false, null, location);

                            // Send off the message, the needed notifier permission and the flag to check.
                            iterateAndSendEventMessage(
                                    finalMessage, null, false, false,
                                    false, "challenge.bosstrainer", "showBossTrainerChallenge");
                        }
                        else
                            printBasicError("The boss trainer challenge message is broken, broadcast failed.");
                    }
                }
                else
                {
                    if (logTrainerChallenges)
                    {
                        // Print a challenge message to console.
                        printBasicMessage
                        (
                                "§5PBR §f// §7Player §f" + playerName +
                                "§7 challenged a §ftrainer §7in world \"§f" + worldName +
                                "§7\", at X:§f" + location.getX() +
                                "§7 Y:§f" + location.getY() +
                                "§7 Z:§f" + location.getZ()
                        );
                    }

                    if (showTrainerChallenges)
                    {
                        // Parse placeholders and print!
                        if (trainerChallengeMessage != null)
                        {
                            // Set up our message. This is the same for all eligible players, so call it once and store it.
                            final String finalMessage = replacePlaceholders(trainerChallengeMessage,
                                    playerName, false, false, null, location);

                            // Send off the message, the needed notifier permission and the flag to check.
                            iterateAndSendEventMessage(
                                    finalMessage, null, false, false,
                                    false, "challenge.trainer", "showTrainerChallenge");
                        }
                        else
                            printBasicError("The trainer challenge message is broken, broadcast failed.");
                    }
                }
            }
            // Are there any wild Pokémon in the battle?
            else if (participant1 instanceof WildPixelmonParticipant || participant2 instanceof WildPixelmonParticipant)
            {
                // Create some variables for the player and Pokémon.
                final PlayerParticipant player;
                final WildPixelmonParticipant pokemon;

                // See which side of the battle has our player, and which side has our Pokémon. Fill things in.
                if (participant1 instanceof PlayerParticipant)
                {
                    player = (PlayerParticipant) participant1;
                    pokemon = (WildPixelmonParticipant) participant2;
                }
                else
                {
                    player = (PlayerParticipant) participant2;
                    pokemon = (WildPixelmonParticipant) participant1;
                }

                // Set up even more common variables.
                final String playerName = player.getDisplayName();
                final String pokemonName = pokemon.getDisplayName();
                final World world = pokemon.getWorld();
                final BlockPos location = pokemon.getEntity().getPosition();
                final EntityPixelmon pokemonEntity = (EntityPixelmon) pokemon.getEntity();

                // Make sure our Pokémon participant has no owner -- it has to be wild.
                // I put bosses under this check, as well. Who knows what servers cook up for player parties?
                if (!pokemonEntity.hasOwner())
                {
                    // Is the Pokémon a boss?
                    if (pokemonEntity.isBossPokemon())
                    {
                        if (logBossChallenges)
                        {
                            // Print a challenge message to console.
                            printBasicMessage
                            (
                                    "§5PBR §f// §ePlayer §6" + player.getDisplayName() +
                                    "§e engaged a boss §6" + pokemonName +
                                    "§e in world \"§6" + world.getWorldInfo().getWorldName() +
                                    "§e\", at X:§6" + location.getX() +
                                    "§e Y:§6" + location.getY() +
                                    "§e Z:§6" + location.getZ()
                            );
                        }

                        if (showBossChallenges)
                        {
                            // Parse placeholders and print!
                            if (bossChallengeMessage != null)
                            {
                                // Set up our message. This is the same for all eligible players, so call it once and store it.
                                final String finalMessage = replacePlaceholders(bossChallengeMessage,
                                        playerName, false, false, pokemonEntity, location);

                                // Send off the message, the needed notifier permission and the flag to check.
                                iterateAndSendEventMessage(finalMessage, pokemonEntity, showBossChallenges, true,
                                        false, "challenge.boss", "showBossChallenge");
                            }
                            else
                                printBasicError("The boss challenge message is broken, broadcast failed.");
                        }
                    }
                    else if (EnumPokemon.legendaries.contains(pokemonName) && pokemonEntity.getIsShiny())
                    {
                        if (logShinyLegendaryChallenges)
                        {
                            // Print a challenge message to console.
                            printBasicMessage
                            (
                                    "§5PBR §f// §aPlayer §2" + player.getDisplayName() +
                                    "§a engaged a shiny legendary §2" + pokemonName +
                                    "§a in world \"§2" + world.getWorldInfo().getWorldName() +
                                    "§a\", at X:§2" + location.getX() +
                                    "§a Y:§2" + location.getY() +
                                    "§a Z:§2" + location.getZ()
                            );
                        }

                        if (showShinyLegendaryChallenges)
                        {
                            // Parse placeholders and print!
                            if (shinyLegendaryChallengeMessage != null)
                            {
                                // Set up our message. This is the same for all eligible players, so call it once and store it.
                                // We use the normal legendary permission for shiny legendaries, as per the config's explanation.
                                final String finalMessage = replacePlaceholders(shinyLegendaryChallengeMessage,
                                        playerName, false, false, pokemonEntity, location);

                                // Send off the message, the needed notifier permission and the flag to check.
                                iterateAndSendEventMessage(finalMessage, pokemonEntity, showShinyLegendaryChallenges, true,
                                        false, "challenge.shinylegendary", "showShinyLegendaryChallenge");
                            }
                            else
                                printBasicError("The shiny legendary challenge message is broken, broadcast failed.");
                        }
                    }
                    else if (EnumPokemon.legendaries.contains(pokemonName))
                    {
                        if (logLegendaryChallenges)
                        {
                            // Print a challenge message to console.
                            printBasicMessage
                            (
                                    "§5PBR §f// §aPlayer §2" + player.getDisplayName() +
                                    "§a engaged a legendary §2" + pokemonName +
                                    "§a in world \"§2" + world.getWorldInfo().getWorldName() +
                                    "§a\", at X:§2" + location.getX() +
                                    "§a Y:§2" + location.getY() +
                                    "§a Z:§2" + location.getZ()
                            );
                        }

                        if (showLegendaryChallenges)
                        {
                            // Parse placeholders and print!
                            if (legendaryChallengeMessage != null)
                            {
                                // Set up our message. This is the same for all eligible players, so call it once and store it.
                                final String finalMessage = replacePlaceholders(legendaryChallengeMessage,
                                        playerName, false, false, pokemonEntity, location);

                                // Send off the message, the needed notifier permission and the flag to check.
                                iterateAndSendEventMessage(finalMessage, pokemonEntity, showLegendaryChallenges, true,
                                        false, "challenge.legendary", "showLegendaryChallenge");
                            }
                            else
                                printBasicError("The legendary challenge message is broken, broadcast failed.");
                        }
                    }
                    else if (pokemonEntity.getIsShiny())
                    {
                        if (logShinyChallenges)
                        {
                            // Print a challenge message to console.
                            printBasicMessage
                            (
                                    "§5PBR §f// §bPlayer §3" + player.getDisplayName() +
                                    "§b engaged a shiny §3" + pokemonName +
                                    "§b in world \"§3" + world.getWorldInfo().getWorldName() +
                                    "§b\", at X:§3" + location.getX() +
                                    "§b Y:§3" + location.getY() +
                                    "§b Z:§3" + location.getZ()
                            );
                        }

                        if (showShinyChallenges)
                        {
                            // Parse placeholders and print!
                            if (shinyChallengeMessage != null)
                            {
                                // Set up our message. This is the same for all eligible players, so call it once and store it.
                                final String finalMessage = replacePlaceholders(shinyChallengeMessage,
                                        playerName, false, false, pokemonEntity, location);

                                // Send off the message, the needed notifier permission and the flag to check.
                                iterateAndSendEventMessage(finalMessage, pokemonEntity, showShinyChallenges, true,
                                        false, "challenge.shiny", "showShinyChallenge");
                            }
                            else
                                printBasicError("The shiny challenge message is broken, broadcast failed.");
                        }
                    }
                }
            }
        }
    }
}
