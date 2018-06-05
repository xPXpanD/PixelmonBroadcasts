// Listens for ended battles.
package rs.expand.pixelmonbroadcasts.listeners;

// Remote imports.
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.TrainerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.WildPixelmonParticipant;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import com.pixelmonmod.pixelmon.enums.battle.BattleResults;
import com.pixelmonmod.pixelmon.enums.battle.EnumBattleEndCause;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.*;

// Local imports.
import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.*;
import static rs.expand.pixelmonbroadcasts.utilities.PlaceholderMethods.iterateAndSendEventMessage;
import static rs.expand.pixelmonbroadcasts.utilities.PlaceholderMethods.replacePlaceholders;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.printBasicError;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.printBasicMessage;

// TODO: Add flee messages.
// TODO: Check for abnormal ends.
public class BattleEndListener
{
    @SubscribeEvent
    public void onBattleEndEvent(final BattleEndEvent event)
    {
        // TODO: %pokedollars% placeholder.
        // TODO: Double battle support, whenever.
        // TODO: See if tracking gym leaders is possible. Maybe look into marking placed leaders with trainer.isGymLeader.
        // FIXME: Keep name ordering (from battle start message) persistent regardless of outcome. Pre-sort alphabetically?
        // See who won, and who lost. We populate a list, but generally only use the first result. Seems reliable...
        final List<BattleParticipant> winners = new ArrayList<>(), losers = new ArrayList<>(), neutrals = new ArrayList<>();
        for (Map.Entry<BattleParticipant, BattleResults> entry : event.results.entrySet())
        {
            switch (entry.getValue())
            {
                case VICTORY:
                    winners.add(entry.getKey()); break;
                case DEFEAT: // Forfeiting trainer battles also registers as a defeat on the player's side. Be careful.
                    losers.add(entry.getKey()); break;
                case DRAW: case FLEE: // Only added to in a draw, or when fleeing from a wild Pokémon.
                    neutrals.add(entry.getKey()); break;
            }
        }

        printBasicError("winners: " + winners);
        printBasicError("losers: " + losers);
        printBasicError("neutrals: " + neutrals);
        printBasicError("result: " + event.results);
        printBasicError("cause: " + event.cause);

        // Get our battle results, and whether we have the right amount of participants.
        final boolean endedInDraw = neutrals.size() > 1 && getResult(event.results) == BattleResults.DRAW;
        final boolean endedInFlee = neutrals.size() > 1 && getResult(event.results) == BattleResults.FLEE;
        final boolean hasWinnerAndLoser = !winners.isEmpty() && !losers.isEmpty();
        final boolean battleForfeited = event.cause == EnumBattleEndCause.FORFEIT;

        // Set up two participants.
        final BattleParticipant participant1, participant2;

        // Check if our battle result was a draw. Both participants should have been set to "DRAW", in this case.
        if (endedInDraw)
        {
            participant1 = neutrals.get(0);
            participant2 = neutrals.get(1);
        }
        // Check if somebody fled. We should have one participant set to "VICTORY" (and added to winners), and one "FLEE".
        else if (endedInFlee)
        {
            // Hardwire participant1 to be the Pokémon participant.
            if (neutrals.get(0) instanceof WildPixelmonParticipant)
            {
                participant1 = neutrals.get(0);
                participant2 = neutrals.get(1);
            }
            else
            {
                participant1 = neutrals.get(1);
                participant2 = neutrals.get(0);
            }
        }
        // Check if we have a winner AND a loser amongst the participants. Usually trainer/PvP stuff.
        else if (hasWinnerAndLoser)
        {
            // Should be safe -- haven't managed to get two defeats without it deciding on a draw instead.
            participant1 = winners.get(0);
            participant2 = losers.get(0);
        }
        // We didn't hit anything that was valid, stop execution.
        else return;

        // If we're still going, set up some more commonly-used variables.
        final String worldName = participant1.getWorld().getWorldInfo().getWorldName();
        final BlockPos location = participant1.getEntity().getPosition();
        final String participant1Name = participant1.getDisplayName();
        final String participant2Name = participant2.getDisplayName();

        // Was our battle between two valid players?
        if (participant1 instanceof PlayerParticipant && participant2 instanceof PlayerParticipant)
        {
            // Create some internal convenience variables.
            final BlockPos player1Pos = ((PlayerParticipant) participant1).player.getPosition();
            final BlockPos player2Pos = ((PlayerParticipant) participant2).player.getPosition();

            if (endedInDraw || battleForfeited)
            {
                if (logPVPDraws)
                {
                    // Print a PvP draw message to console.
                    printBasicMessage
                    (
                            "§5PBR §f// §7Players §f" + participant1.getDisplayName() +
                            "§7 and §f" + participant2.getDisplayName() +
                            "§7 ended their battle in a draw, in world \"§f" + worldName +
                            "§7\", at X:§f" + location.getX() +
                            "§7 Y:§f" + location.getY() +
                            "§7 Z:§f" + location.getZ()
                    );
                }

                if (showPVPDraws)
                {
                    // Parse placeholders and print!
                    if (pvpDrawMessage != null)
                    {
                        // Set up our message. This is the same for all eligible players, so call it once and store it.
                        String finalMessage;
                        finalMessage = replacePlaceholders(pvpDrawMessage, participant1Name,
                                false, false, null, player1Pos);
                        finalMessage = replacePlaceholders(finalMessage, participant2Name,
                                false, true, null, player2Pos);

                        // Send off the message, the needed notifier permission and the flag to check.
                        iterateAndSendEventMessage(finalMessage, null,
                                false, false, false, "pvpdraw", "showPVPDraws");
                    }
                    else
                        printBasicError("The PvP battle draw message is broken, broadcast failed.");
                }
            }
            else
            {
                if (logPVPDefeats)
                {
                    // Print a PvP defeat message to console.
                    printBasicMessage
                    (
                            "§5PBR §f// §7Player §f" + participant1Name +
                            "§7 defeated player §f" + participant2Name +
                            "§7 in world \"§f" + worldName +
                            "§7\", at X:§f" + location.getX() +
                            "§7 Y:§f" + location.getY() +
                            "§7 Z:§f" + location.getZ()
                    );
                }

                if (showPVPDefeats)
                {
                    // Parse placeholders and print!
                    if (pvpDefeatMessage != null)
                    {
                        // Set up our message. This is the same for all eligible players, so call it once and store it.
                        String finalMessage;
                        finalMessage = replacePlaceholders(pvpDefeatMessage, participant1Name,
                                false, false, null, player1Pos);
                        finalMessage = replacePlaceholders(finalMessage, participant2Name,
                                false, true, null, player2Pos);

                        // Send off the message, the needed notifier permission and the flag to check.
                        iterateAndSendEventMessage(finalMessage, null,
                                false, false, false, "pvpdefeat", "showPVPDefeats");
                    }
                    else
                        printBasicError("The PvP battle end message is broken, broadcast failed.");
                }
            }
        }
        // Do we have a trainer who won from a player?
        else if (participant1 instanceof TrainerParticipant && participant2 instanceof PlayerParticipant)
        {
            // We know now that we have a trainer, so make a variable for it here so we don't have to keep casting.
            final TrainerParticipant trainer1 = (TrainerParticipant) participant1;

            // Was the battle forfeited? I thiiink only the player can do this, right now.
            if (battleForfeited)
            {
                // Is our trainer a boss trainer?
                if (trainer1.trainer.getBossMode().isBossPokemon())
                {
                    if (logBossTrainerForfeits)
                    {
                        // Print a forfeit message to console.
                        printBasicMessage
                        (
                                "§5PBR §f// §6Player §e" + participant2Name +
                                "§6 fled from a §eboss trainer §6in world \"§e" + worldName +
                                "§6\", at X:§e" + location.getX() +
                                "§6 Y:§e" + location.getY() +
                                "§6 Z:§e" + location.getZ()
                        );
                    }
    
                    if (showBossTrainerForfeits)
                    {
                        // Parse placeholders and print!
                        if (bossTrainerForfeitMessage != null)
                        {
                            // Set up our message. This is the same for all eligible players, so call it once and store it.
                            final String finalMessage = replacePlaceholders(bossTrainerForfeitMessage,
                                    participant2Name, false, false, null, location);
    
                            // Send off the message, the needed notifier permission and the flag to check.
                            iterateAndSendEventMessage(
                                    finalMessage, null, false, false,
                                    false, "bosstrainerforfeit", "showBossTrainerForfeit");
                        }
                        else
                            printBasicError("The boss trainer forfeiting message is broken, broadcast failed.");
                    }
                }
                else
                {
                    if (logTrainerForfeits)
                    {
                        // Print a forfeit message to console.
                        printBasicMessage
                        (
                                "§5PBR §f// §6Player §e" + participant2Name +
                                "§6 fled from a §etrainer §6in world \"§e" + worldName +
                                "§6\", at X:§e" + location.getX() +
                                "§6 Y:§e" + location.getY() +
                                "§6 Z:§e" + location.getZ()
                        );
                    }
    
                    if (showTrainerForfeits)
                    {
                        // Parse placeholders and print!
                        if (trainerForfeitMessage != null)
                        {
                            // Set up our message. This is the same for all eligible players, so call it once and store it.
                            final String finalMessage = replacePlaceholders(trainerForfeitMessage,
                                    participant2Name, false, false, null, location);
    
                            // Send off the message, the needed notifier permission and the flag to check.
                            iterateAndSendEventMessage(
                                    finalMessage, null, false, false,
                                    false, "trainerforfeit", "showTrainerForfeit");
                        }
                        else
                            printBasicError("The trainer forfeiting message is broken, broadcast failed.");
                    }
                }
            }
            else
            {
                // Is our trainer a boss trainer?
                if (trainer1.trainer.getBossMode().isBossPokemon())
                {
                    if (logBossTrainerLosses)
                    {
                        // Print a loss message to console.
                        printBasicMessage
                        (
                                "§5PBR §f// §6Player §e" + participant2Name +
                                "§6 was defeated by a §eboss trainer §6in world \"§e" + worldName +
                                "§6\", at X:§e" + location.getX() +
                                "§6 Y:§e" + location.getY() +
                                "§6 Z:§e" + location.getZ()
                        );
                    }

                    if (showBossTrainerLosses)
                    {
                        // Parse placeholders and print!
                        if (bossTrainerLoseToMessage != null)
                        {
                            // Set up our message. This is the same for all eligible players, so call it once and store it.
                            final String finalMessage = replacePlaceholders(bossTrainerLoseToMessage,
                                    participant2Name, false, false, null, location);

                            // Send off the message, the needed notifier permission and the flag to check.
                            iterateAndSendEventMessage(
                                    finalMessage, null, false, false,
                                    false, "bosstrainerloss", "showBossTrainerLoseTo");
                        }
                        else
                            printBasicError("The boss trainer loss message is broken, broadcast failed.");
                    }
                }
                else if (showTrainerLosses)
                {
                    if (logTrainerLosses)
                    {
                        // Print a loss message to console.
                        printBasicMessage
                        (
                                "§5PBR §f// §6Player §e" + participant2Name +
                                "§6 was defeated by a §etrainer §6in world \"§e" + worldName +
                                "§6\", at X:§e" + location.getX() +
                                "§6 Y:§e" + location.getY() +
                                "§6 Z:§e" + location.getZ()
                        );
                    }

                    if (showTrainerLosses)
                    {
                        // Parse placeholders and print!
                        if (trainerLoseToMessage != null)
                        {
                            // Set up our message. This is the same for all eligible players, so call it once and store it.
                            final String finalMessage = replacePlaceholders(trainerLoseToMessage,
                                    participant2Name, false, false, null, location);

                            // Send off the message, the needed notifier permission and the flag to check.
                            iterateAndSendEventMessage(
                                    finalMessage, null, false, false,
                                    false, "trainerloss", "showTrainerLoseTo");
                        }
                        else
                            printBasicError("The trainer loss message is broken, broadcast failed.");
                    }
                }
            }
        }
        // Did a player defeat a trainer?
        else if (participant1 instanceof PlayerParticipant && participant2 instanceof TrainerParticipant)
        {
            // We know now that we have a trainer, so make a variable for it here so we don't have to keep casting.
            final TrainerParticipant trainer2 = (TrainerParticipant) participant2;

            // Is our trainer a boss trainer?
            if ((trainer2.trainer.getBossMode().isBossPokemon()))
            {
                if (logBossTrainerDefeats)
                {
                    // Print a defeat message to console.
                    printBasicMessage
                    (
                            "§5PBR §f// §7Player §1" + participant1Name +
                            "§7 defeated a §fboss trainer §7in world \"§f" + worldName +
                            "§7\", at X:§f" + location.getX() +
                            "§7 Y:§f" + location.getY() +
                            "§7 Z:§f" + location.getZ()
                    );
                }

                if (showBossTrainerDefeats)
                {
                    // Parse placeholders and print!
                    if (bossTrainerDefeatMessage != null)
                    {
                        // Set up our message. This is the same for all eligible players, so call it once and store it.
                        final String finalMessage = replacePlaceholders(bossTrainerDefeatMessage,
                                participant1Name, false, false, null, location);

                        // Send off the message, the needed notifier permission and the flag to check.
                        iterateAndSendEventMessage(
                                finalMessage, null, false, false,
                                false, "bosstrainerdefeat", "showBossTrainerDefeat");
                    }
                    else
                        printBasicError("The boss trainer defeat message is broken, broadcast failed.");
                }
            }
            else
            {
                if (logTrainerDefeats)
                {
                    // Print a defeat message to console.
                    printBasicMessage
                    (
                            "§5PBR §f// §7Player §f" + participant1Name +
                            "§7 defeated a §ftrainer §7in world \"§f" + worldName +
                            "§7\", at X:§f" + location.getX() +
                            "§7 Y:§f" + location.getY() +
                            "§7 Z:§f" + location.getZ()
                    );
                }

                if (showTrainerDefeats)
                {
                    // Parse placeholders and print!
                    if (trainerDefeatMessage != null)
                    {
                        // Set up our message. This is the same for all eligible players, so call it once and store it.
                        final String finalMessage = replacePlaceholders(trainerDefeatMessage,
                                participant1Name, false, false, null, location);

                        // Send off the message, the needed notifier permission and the flag to check.
                        iterateAndSendEventMessage(
                                finalMessage, null, false, false,
                                false, "trainerdefeat", "showTrainerDefeat");
                    }
                    else
                        printBasicError("The trainer defeat message is broken, broadcast failed.");
                }
            }
        }
        // Did a player lose to a wild Pokémon?
        else if (participant1 instanceof WildPixelmonParticipant && participant2 instanceof PlayerParticipant)
        {
            final EntityPixelmon pokemon = (EntityPixelmon) participant1.getEntity();
            final String pokemonName = participant1.getDisplayName();

            // Is the Pokémon a boss?
            if (pokemon.isBossPokemon())
            {
                if (logBossLosses)
                {
                    // Print a loss message to console.
                    printBasicMessage
                    (
                            "§5PBR §f// §ePlayer §6" + participant2Name +
                            "§e lost to a boss §6" + pokemonName +
                            "§e in world \"§6" + worldName +
                            "§e\", at X:§6" + location.getX() +
                            "§e Y:§6" + location.getY() +
                            "§e Z:§6" + location.getZ()
                    );
                }

                if (showBossLosses)
                {
                    // Parse placeholders and print!
                    if (bossLoseToMessage != null)
                    {
                        // Set up our message. This is the same for all eligible players, so call it once and store it.
                        final String finalMessage = replacePlaceholders(bossLoseToMessage,
                                participant2Name, true, false, pokemon, location);

                        // Send off the message, the needed notifier permission and the flag to check.
                        iterateAndSendEventMessage(finalMessage, pokemon, hoverBossLosses, true,
                                true, "bossloss", "showBossLoseTo");
                    }
                    else
                        printBasicError("The boss loss message is broken, broadcast failed.");
                }
            }
            else if (EnumPokemon.legendaries.contains(pokemonName) && pokemon.getIsShiny())
            {
                if (logShinyLegendaryLosses)
                {
                    // Print a loss message to console.
                    printBasicMessage
                    (
                            "§5PBR §f// §cPlayer §4" + participant2Name +
                            "§c lost to a shiny legendary §4" + pokemonName +
                            "§c in world \"§4" + worldName +
                            "§c\", at X:§4" + location.getX() +
                            "§c Y:§4" + location.getY() +
                            "§c Z:§4" + location.getZ()
                    );
                }

                if (showShinyLegendaryLosses)
                {
                    // Parse placeholders and print!
                    if (shinyLegendaryLoseToMessage != null)
                    {
                        // Set up our message. This is the same for all eligible players, so call it once and store it.
                        // We use the normal legendary permission for shiny legendaries, as per the config's explanation.
                        final String finalMessage = replacePlaceholders(shinyLegendaryLoseToMessage,
                                participant2Name, true, false, pokemon, location);

                        // Send off the message, the needed notifier permission and the flag to check.
                        iterateAndSendEventMessage(finalMessage, pokemon, hoverShinyLegendaryLosses, true,
                                true, "shinylegendaryloss", "showShinyLegendaryLoseTo");
                    }
                    else
                        printBasicError("The shiny legendary loss message is broken, broadcast failed.");
                }
            }
            else if (EnumPokemon.legendaries.contains(pokemonName))
            {
                if (logLegendaryLosses)
                {
                    // Print a loss message to console.
                    printBasicMessage
                    (
                            "§5PBR §f// §cPlayer §4" + participant2Name +
                            "§c lost to a legendary §4" + pokemonName +
                            "§c in world \"§4" + worldName +
                            "§c\", at X:§4" + location.getX() +
                            "§c Y:§4" + location.getY() +
                            "§c Z:§4" + location.getZ()
                    );
                }

                if (showLegendaryLosses)
                {
                    // Parse placeholders and print!
                    if (legendaryLoseToMessage != null)
                    {
                        // Set up our message. This is the same for all eligible players, so call it once and store it.
                        final String finalMessage = replacePlaceholders(legendaryLoseToMessage,
                                participant2Name, true, false, pokemon, location);

                        // Send off the message, the needed notifier permission and the flag to check.
                        iterateAndSendEventMessage(finalMessage, pokemon, hoverLegendaryLosses, true,
                                true, "legendaryloss", "showLegendaryLoseTo");
                    }
                    else
                        printBasicError("The legendary loss message is broken, broadcast failed.");
                }
            }
            else if (pokemon.getIsShiny())
            {
                if (logShinyLosses)
                {
                    // Print a loss message to console.
                    printBasicMessage
                    (
                            "§5PBR §f// §cPlayer §4" + participant2Name +
                            "§c lost to a shiny §4" + pokemonName +
                            "§c in world \"§4" + worldName +
                            "§c\", at X:§4" + location.getX() +
                            "§c Y:§4" + location.getY() +
                            "§c Z:§4" + location.getZ()
                    );
                }

                if (showShinyLosses)
                {
                    // Parse placeholders and print!
                    if (shinyLoseToMessage != null)
                    {
                        // Set up our message. This is the same for all eligible players, so call it once and store it.
                        final String finalMessage = replacePlaceholders(shinyLoseToMessage,
                                participant2Name, true, false, pokemon, location);

                        // Send off the message, the needed notifier permission and the flag to check.
                        iterateAndSendEventMessage(finalMessage, pokemon, hoverShinyLosses, true,
                                true, "shinyloss", "showShinyLoseTo");
                    }
                    else
                        printBasicError("The shiny loss message is broken, broadcast failed.");
                }
            }
        }
        else if (endedInFlee)
        {
            final EntityPixelmon pokemon = (EntityPixelmon) participant1.getEntity();
            final String pokemonName = participant1.getDisplayName();

            // Is the Pokémon a boss?
            if (pokemon.isBossPokemon())
            {
                if (logBossForfeits)
                {
                    // Print a forfeit message to console.
                    printBasicMessage
                    (
                            "§5PBR §f// §ePlayer §6" + participant2Name +
                            "§e fled from a boss §6" + pokemonName +
                            "§e in world \"§6" + worldName +
                            "§e\", at X:§6" + location.getX() +
                            "§e Y:§6" + location.getY() +
                            "§e Z:§6" + location.getZ()
                    );
                }

                if (showBossForfeits)
                {
                    // Parse placeholders and print!
                    if (bossForfeitMessage != null)
                    {
                        // Set up our message. This is the same for all eligible players, so call it once and store it.
                        final String finalMessage = replacePlaceholders(bossForfeitMessage,
                                participant2Name, true, false, pokemon, location);

                        // Send off the message, the needed notifier permission and the flag to check.
                        iterateAndSendEventMessage(finalMessage, pokemon, hoverBossForfeits, true,
                                true, "bossforfeit", "showBossForfeit");
                    }
                    else
                        printBasicError("The boss forfeit message is broken, broadcast failed.");
                }
            }
            else if (EnumPokemon.legendaries.contains(pokemonName) && pokemon.getIsShiny())
            {
                if (logShinyLegendaryForfeits)
                {
                    // Print a forfeit message to console.
                    printBasicMessage
                    (
                            "§5PBR §f// §cPlayer §4" + participant2Name +
                            "§c fled from a shiny legendary §4" + pokemonName +
                            "§c in world \"§4" + worldName +
                            "§c\", at X:§4" + location.getX() +
                            "§c Y:§4" + location.getY() +
                            "§c Z:§4" + location.getZ()
                    );
                }

                if (showShinyLegendaryForfeits)
                {
                    // Parse placeholders and print!
                    if (shinyLegendaryForfeitMessage != null)
                    {
                        // Set up our message. This is the same for all eligible players, so call it once and store it.
                        // We use the normal legendary permission for shiny legendaries, as per the config's explanation.
                        final String finalMessage = replacePlaceholders(shinyLegendaryForfeitMessage,
                                participant2Name, true, false, pokemon, location);

                        // Send off the message, the needed notifier permission and the flag to check.
                        iterateAndSendEventMessage(finalMessage, pokemon, hoverShinyLegendaryForfeits, true,
                                true, "shinylegendaryforfeit", "showShinyLegendaryForfeit");
                    }
                    else
                        printBasicError("The shiny legendary forfeit message is broken, broadcast failed.");
                }
            }
            else if (EnumPokemon.legendaries.contains(pokemonName))
            {
                if (logLegendaryForfeits)
                {
                    // Print a forfeit message to console.
                    printBasicMessage
                    (
                            "§5PBR §f// §cPlayer §4" + participant2Name +
                            "§c fled from a legendary §4" + pokemonName +
                            "§c in world \"§4" + worldName +
                            "§c\", at X:§4" + location.getX() +
                            "§c Y:§4" + location.getY() +
                            "§c Z:§4" + location.getZ()
                    );
                }

                if (showLegendaryForfeits)
                {
                    // Parse placeholders and print!
                    if (legendaryForfeitMessage != null)
                    {
                        // Set up our message. This is the same for all eligible players, so call it once and store it.
                        final String finalMessage = replacePlaceholders(legendaryForfeitMessage,
                                participant2Name, true, false, pokemon, location);

                        // Send off the message, the needed notifier permission and the flag to check.
                        iterateAndSendEventMessage(finalMessage, pokemon, hoverLegendaryForfeits, true,
                                true, "legendaryforfeit", "showLegendaryForfeit");
                    }
                    else
                        printBasicError("The legendary forfeit message is broken, broadcast failed.");
                }
            }
            else if (pokemon.getIsShiny())
            {
                if (logShinyForfeits)
                {
                    // Print a forfeit message to console.
                    printBasicMessage
                    (
                            "§5PBR §f// §cPlayer §4" + participant2Name +
                            "§c fled from a shiny §4" + pokemonName +
                            "§c in world \"§4" + worldName +
                            "§c\", at X:§4" + location.getX() +
                            "§c Y:§4" + location.getY() +
                            "§c Z:§4" + location.getZ()
                    );
                }

                if (showShinyForfeits)
                {
                    // Parse placeholders and print!
                    if (shinyForfeitMessage != null)
                    {
                        // Set up our message. This is the same for all eligible players, so call it once and store it.
                        final String finalMessage = replacePlaceholders(shinyForfeitMessage,
                                participant2Name, true, false, pokemon, location);

                        // Send off the message, the needed notifier permission and the flag to check.
                        iterateAndSendEventMessage(finalMessage, pokemon, hoverShinyForfeits, true,
                                true, "shinyforfeit", "showShinyForfeit");
                    }
                    else
                        printBasicError("The shiny forfeit message is broken, broadcast failed.");
                }
            }
        }
    }

    // Returns the first battle result from the provided Map.
    private BattleResults getResult(Map<BattleParticipant, BattleResults> map)
    {
        Map.Entry<BattleParticipant, BattleResults> entry = map.entrySet().iterator().next();
        return entry.getValue();
    }
}
