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
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.isBroadcastPresent;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.printBasicError;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.printBasicMessage;

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

        // Needed to prevent a weird issue where it would sometimes register a loss when catching a Pokémon.
        // Seemed like a Pixelmon bug, so I reported it.
        if (event.cause != EnumBattleEndCause.FORCE)
        {
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
                        if (isBroadcastPresent("broadcast.draw.pvp"))
                        {
                            // Set up our message. This is the same for all eligible players, so call it once and store it.
                            String finalMessage;
                            finalMessage = replacePlaceholders(pvpDrawMessage, participant1Name,
                                    false, false, null, player1Pos);
                            finalMessage = replacePlaceholders(finalMessage, participant2Name,
                                    false, true, null, player2Pos);

                            // Send off the message, the needed notifier permission and the flag to check.
                            iterateAndSendEventMessage(finalMessage, null,
                                    false, false, false, "draw.pvp", "showPVPDraw");
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
                        if (isBroadcastPresent("broadcast.defeat.pvp"))
                        {
                            // Set up our message. This is the same for all eligible players, so call it once and store it.
                            String finalMessage;
                            finalMessage = replacePlaceholders(pvpDefeatMessage, participant1Name,
                                    false, false, null, player1Pos);
                            finalMessage = replacePlaceholders(finalMessage, participant2Name,
                                    false, true, null, player2Pos);

                            // Send off the message, the needed notifier permission and the flag to check.
                            iterateAndSendEventMessage(finalMessage, null,
                                    false, false, false, "defeat.pvp", "showPVPDefeat");
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
                            if (isBroadcastPresent("broadcast.forfeit.boss_trainer"))
                            {
                                // Set up our message. This is the same for all eligible players, so call it once and store it.
                                final String finalMessage = replacePlaceholders(bossTrainerForfeitMessage,
                                        participant2Name, false, false, null, location);

                                // Send off the message, the needed notifier permission and the flag to check.
                                iterateAndSendEventMessage(
                                        finalMessage, null, false, false,
                                        false, "forfeit.bosstrainer", "showBossTrainerForfeit");
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
                            if (isBroadcastPresent("broadcast.forfeit.trainer"))
                            {
                                // Set up our message. This is the same for all eligible players, so call it once and store it.
                                final String finalMessage = replacePlaceholders(trainerForfeitMessage,
                                        participant2Name, false, false, null, location);

                                // Send off the message, the needed notifier permission and the flag to check.
                                iterateAndSendEventMessage(
                                        finalMessage, null, false, false,
                                        false, "forfeit.trainer", "showTrainerForfeit");
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
                        if (logBossTrainerBlackouts)
                        {
                            // Print a blackout message to console.
                            printBasicMessage
                            (
                                    "§5PBR §f// §6Player §e" + participant2Name +
                                    "§6 was knocked out by a §eboss trainer §6in world \"§e" + worldName +
                                    "§6\", at X:§e" + location.getX() +
                                    "§6 Y:§e" + location.getY() +
                                    "§6 Z:§e" + location.getZ()
                            );
                        }

                        if (showBossTrainerBlackouts)
                        {
                            // Parse placeholders and print!
                            if (isBroadcastPresent("broadcast.blackout.boss_trainer"))
                            {
                                // Set up our message. This is the same for all eligible players, so call it once and store it.
                                final String finalMessage = replacePlaceholders(bossTrainerBlackoutMessage,
                                        participant2Name, false, false, null, location);

                                // Send off the message, the needed notifier permission and the flag to check.
                                iterateAndSendEventMessage(
                                        finalMessage, null, false, false,
                                        false, "blackout.bosstrainer", "showBossTrainerBlackout");
                            }
                            else
                                printBasicError("The boss trainer blackout message is broken, broadcast failed.");
                        }
                    }
                    else if (showTrainerBlackouts)
                    {
                        if (logTrainerBlackouts)
                        {
                            // Print a blackout message to console.
                            printBasicMessage
                            (
                                    "§5PBR §f// §6Player §e" + participant2Name +
                                    "§6 was knocked out by a §etrainer §6in world \"§e" + worldName +
                                    "§6\", at X:§e" + location.getX() +
                                    "§6 Y:§e" + location.getY() +
                                    "§6 Z:§e" + location.getZ()
                            );
                        }

                        if (showTrainerBlackouts)
                        {
                            // Parse placeholders and print!
                            if (isBroadcastPresent("broadcast.blackout.trainer"))
                            {
                                // Set up our message. This is the same for all eligible players, so call it once and store it.
                                final String finalMessage = replacePlaceholders(trainerBlackoutMessage,
                                        participant2Name, false, false, null, location);

                                // Send off the message, the needed notifier permission and the flag to check.
                                iterateAndSendEventMessage(
                                        finalMessage, null, false, false,
                                        false, "blackout.trainer", "showTrainerBlackout");
                            }
                            else
                                printBasicError("The trainer blackout message is broken, broadcast failed.");
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
                        if (isBroadcastPresent("broadcast.defeat.boss_trainer"))
                        {
                            // Set up our message. This is the same for all eligible players, so call it once and store it.
                            final String finalMessage = replacePlaceholders(bossTrainerDefeatMessage,
                                    participant1Name, false, false, null, location);

                            // Send off the message, the needed notifier permission and the flag to check.
                            iterateAndSendEventMessage(
                                    finalMessage, null, false, false,
                                    false, "defeat.bosstrainer", "showBossTrainerDefeat");
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
                        if (isBroadcastPresent("broadcast.defeat.trainer"))
                        {
                            // Set up our message. This is the same for all eligible players, so call it once and store it.
                            final String finalMessage = replacePlaceholders(trainerDefeatMessage,
                                    participant1Name, false, false, null, location);

                            // Send off the message, the needed notifier permission and the flag to check.
                            iterateAndSendEventMessage(
                                    finalMessage, null, false, false,
                                    false, "defeat.trainer", "showTrainerDefeat");
                        }
                        else
                            printBasicError("The trainer defeat message is broken, broadcast failed.");
                    }
                }
            }
            // Did a player lose to a wild Pokémon?
            else if (participant1 instanceof WildPixelmonParticipant && participant2 instanceof PlayerParticipant && !endedInFlee)
            {
                printBasicError("Abnormal: " + event.abnormal);
                printBasicError("Participant1: " + participant1);
                printBasicError("Participant2: " + participant2);

                final EntityPixelmon pokemon = (EntityPixelmon) participant1.getEntity();
                final String pokemonName = participant1.getDisplayName();

                // Is the Pokémon a boss?
                if (pokemon.isBossPokemon())
                {
                    if (logBossBlackouts)
                    {
                        // Print a blackout message to console.
                        printBasicMessage
                        (
                                "§5PBR §f// §ePlayer §6" + participant2Name +
                                "§e was knocked out by a boss §6" + pokemonName +
                                "§e in world \"§6" + worldName +
                                "§e\", at X:§6" + location.getX() +
                                "§e Y:§6" + location.getY() +
                                "§e Z:§6" + location.getZ()
                        );
                    }

                    if (showBossBlackouts)
                    {
                        // Parse placeholders and print!
                        if (isBroadcastPresent("broadcast.blackout.boss"))
                        {
                            // Set up our message. This is the same for all eligible players, so call it once and store it.
                            final String finalMessage = replacePlaceholders(bossBlackoutMessage,
                                    participant2Name, false, false, pokemon, location);

                            // Send off the message, the needed notifier permission and the flag to check.
                            iterateAndSendEventMessage(finalMessage, pokemon, hoverBossBlackouts, true,
                                    false, "blackout.boss", "showBossBlackout");
                        }
                        else
                            printBasicError("The boss blackout message is broken, broadcast failed.");
                    }
                }
                else if (EnumPokemon.legendaries.contains(pokemonName) && pokemon.getIsShiny())
                {
                    if (logShinyLegendaryBlackouts)
                    {
                        // Print a blackout message to console.
                        printBasicMessage
                        (
                                "§5PBR §f// §cPlayer §4" + participant2Name +
                                "§c was knocked out by a shiny legendary §4" + pokemonName +
                                "§c in world \"§4" + worldName +
                                "§c\", at X:§4" + location.getX() +
                                "§c Y:§4" + location.getY() +
                                "§c Z:§4" + location.getZ()
                        );
                    }

                    if (showShinyLegendaryBlackouts)
                    {
                        // Parse placeholders and print!
                        if (isBroadcastPresent("broadcast.blackout.shiny_legendary"))
                        {
                            // Set up our message. This is the same for all eligible players, so call it once and store it.
                            // We use the normal legendary permission for shiny legendaries, as per the config's explanation.
                            final String finalMessage = replacePlaceholders(shinyLegendaryBlackoutMessage,
                                    participant2Name, false, false, pokemon, location);

                            // Send off the message, the needed notifier permission and the flag to check.
                            iterateAndSendEventMessage(finalMessage, pokemon, hoverShinyLegendaryBlackouts, true,
                                    false, "blackout.shinylegendary", "showShinyLegendaryBlackout");
                        }
                        else
                            printBasicError("The shiny legendary blackout message is broken, broadcast failed.");
                    }
                }
                else if (EnumPokemon.legendaries.contains(pokemonName))
                {
                    if (logLegendaryBlackouts)
                    {
                        // Print a blackout message to console.
                        printBasicMessage
                        (
                                "§5PBR §f// §cPlayer §4" + participant2Name +
                                "§c was knocked out by a legendary §4" + pokemonName +
                                "§c in world \"§4" + worldName +
                                "§c\", at X:§4" + location.getX() +
                                "§c Y:§4" + location.getY() +
                                "§c Z:§4" + location.getZ()
                        );
                    }

                    if (showLegendaryBlackouts)
                    {
                        // Parse placeholders and print!
                        if (isBroadcastPresent("broadcast.blackout.legendary"))
                        {
                            // Set up our message. This is the same for all eligible players, so call it once and store it.
                            final String finalMessage = replacePlaceholders(legendaryBlackoutMessage,
                                    participant2Name, false, false, pokemon, location);

                            // Send off the message, the needed notifier permission and the flag to check.
                            iterateAndSendEventMessage(finalMessage, pokemon, hoverLegendaryBlackouts, true,
                                    false, "blackout.legendary", "showLegendaryBlackout");
                        }
                        else
                            printBasicError("The legendary blackout message is broken, broadcast failed.");
                    }
                }
                else if (pokemon.getIsShiny())
                {
                    if (logShinyBlackouts)
                    {
                        // Print a blackout message to console.
                        printBasicMessage
                        (
                                "§5PBR §f// §cPlayer §4" + participant2Name +
                                "§c was knocked out by a shiny §4" + pokemonName +
                                "§c in world \"§4" + worldName +
                                "§c\", at X:§4" + location.getX() +
                                "§c Y:§4" + location.getY() +
                                "§c Z:§4" + location.getZ()
                        );
                    }

                    if (showShinyBlackouts)
                    {
                        // Parse placeholders and print!
                        if (isBroadcastPresent("broadcast.blackout.shiny"))
                        {
                            // Set up our message. This is the same for all eligible players, so call it once and store it.
                            final String finalMessage = replacePlaceholders(shinyBlackoutMessage,
                                    participant2Name, false, false, pokemon, location);

                            // Send off the message, the needed notifier permission and the flag to check.
                            iterateAndSendEventMessage(finalMessage, pokemon, hoverShinyBlackouts, true,
                                    false, "blackout.shiny", "showShinyBlackout");
                        }
                        else
                            printBasicError("The shiny blackout message is broken, broadcast failed.");
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
                        if (isBroadcastPresent("broadcast.forfeit.boss"))
                        {
                            // Set up our message. This is the same for all eligible players, so call it once and store it.
                            final String finalMessage = replacePlaceholders(bossForfeitMessage,
                                    participant2Name, false, false, pokemon, location);

                            // Send off the message, the needed notifier permission and the flag to check.
                            iterateAndSendEventMessage(finalMessage, pokemon, hoverBossForfeits, true,
                                    false, "forfeit.boss", "showBossForfeit");
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
                        if (isBroadcastPresent("broadcast.forfeit.shiny_legendary"))
                        {
                            // Set up our message. This is the same for all eligible players, so call it once and store it.
                            // We use the normal legendary permission for shiny legendaries, as per the config's explanation.
                            final String finalMessage = replacePlaceholders(shinyLegendaryForfeitMessage,
                                    participant2Name, false, false, pokemon, location);

                            // Send off the message, the needed notifier permission and the flag to check.
                            iterateAndSendEventMessage(finalMessage, pokemon, hoverShinyLegendaryForfeits, true,
                                    false, "forfeit.shinylegendary", "showShinyLegendaryForfeit");
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
                        if (isBroadcastPresent("broadcast.forfeit.legendary"))
                        {
                            // Set up our message. This is the same for all eligible players, so call it once and store it.
                            final String finalMessage = replacePlaceholders(legendaryForfeitMessage,
                                    participant2Name, false, false, pokemon, location);

                            // Send off the message, the needed notifier permission and the flag to check.
                            iterateAndSendEventMessage(finalMessage, pokemon, hoverLegendaryForfeits, true,
                                    false, "forfeit.legendary", "showLegendaryForfeit");
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
                        if (isBroadcastPresent("broadcast.forfeit.shiny"))
                        {
                            // Set up our message. This is the same for all eligible players, so call it once and store it.
                            final String finalMessage = replacePlaceholders(shinyForfeitMessage,
                                    participant2Name, false, false, pokemon, location);

                            // Send off the message, the needed notifier permission and the flag to check.
                            iterateAndSendEventMessage(finalMessage, pokemon, hoverShinyForfeits, true,
                                    false, "forfeit.shiny", "showShinyForfeit");
                        }
                        else
                            printBasicError("The shiny forfeit message is broken, broadcast failed.");
                    }
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
