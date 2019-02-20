// Listens for ended battles.
package rs.expand.pixelmonbroadcasts.listeners;

// Remote imports.
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.TrainerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.WildPixelmonParticipant;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.battle.BattleResults;
import com.pixelmonmod.pixelmon.enums.battle.EnumBattleEndCause;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.*;

// Local imports.
import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.*;
import static rs.expand.pixelmonbroadcasts.utilities.PlaceholderMethods.*;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.*;

// TODO: %pokedollars% placeholder.
// TODO: Double battle support, whenever.
// TODO: See if tracking gym leaders is possible. Maybe look into tagging placed leaders with trainer.isGymLeader.
// FIXME: Keep name ordering (from battle start message) persistent regardless of outcome. Pre-sort alphabetically?
// FIXME: In PvP, if both sides use a self-killing move or otherwise die it picks a winner. Make this a draw, somehow.
// FIXME: Similarly, using Explosion to kill something special occasionally prints no message.
public class BattleEndListener
{
    @SubscribeEvent
    public void onBattleEndEvent(final BattleEndEvent event)
    {
        // See who won, and who lost. We populate a list, but often use only the first result. Seems reliable, so far...
        final List<BattleParticipant> winners = new ArrayList<>(), losers = new ArrayList<>(), neutrals = new ArrayList<>();
        for (Map.Entry<BattleParticipant, BattleResults> entry : event.results.entrySet())
        {
            //printUnformattedMessage("Looping. Participant is " + entry.getValue().name() + ", result is " + entry.getKey().getName());

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
        final boolean endedInDraw =
                neutrals.size() > 1 && event.results.entrySet().iterator().next().getValue() == BattleResults.DRAW;
        final boolean endedInFlee =
                neutrals.size() > 1 && event.results.entrySet().iterator().next().getValue() == BattleResults.FLEE;
        final boolean battleForfeited = event.cause == EnumBattleEndCause.FORFEIT;

        // Set up two participants.
        BattleParticipant participant1, participant2;

        // Check if our battle result was a draw. Both participants should have been set to "DRAW", in this case.
        if (endedInDraw)
        {
            participant1 = neutrals.get(0);
            participant2 = neutrals.get(1);
        }
        // Check if we have a winner AND a loser amongst the participants. Unsure if this is still used.
        else if (!winners.isEmpty() && !losers.isEmpty())
        {
            participant1 = winners.get(0);
            participant2 = losers.get(0);
        }
        // Check if we have a winner and a neutral result amongst the participants. Usually trainer/PvP stuff.
        else if (!winners.isEmpty() && !neutrals.isEmpty())
        {
            participant1 = winners.get(0);
            participant2 = neutrals.get(0);
        }
        // Check if somebody fled. We should have one participant set to "VICTORY" (and added to winners), and one "FLEE".
        else if (endedInFlee)
        {
            // Hardwire participant1 to be the Pokémon participant.
            if (neutrals.get(0) instanceof WildPixelmonParticipant && neutrals.get(1) instanceof PlayerParticipant)
            {
                participant1 = neutrals.get(0);
                participant2 = neutrals.get(1);
            }
            else if (neutrals.get(0) instanceof PlayerParticipant && neutrals.get(1) instanceof WildPixelmonParticipant)
            {
                participant1 = neutrals.get(1);
                participant2 = neutrals.get(0);
            }
            // We got a weird result (two Pokémon?), stop execution.
            else return;
        }
        // We didn't hit anything that was valid, stop execution.
        else return;

        // FIXME: Null checks are there for safety, as names can apparently go null. Actually look into this.
        if (participant1.getName() != null && participant2.getName() != null)
        {
            // If we're still going, set up some more commonly-used variables. World stuff should be the same for both.
            final String worldName = participant1.getWorld().getWorldInfo().getWorldName();
            final BlockPos location = participant1.getEntity().getPosition();

            // Needed to prevent a weird issue where it would sometimes register a loss when catching a Pokémon.
            if (event.cause != EnumBattleEndCause.FORCE)
            {
                // Create a shorthand broadcast variable for convenience.
                final String broadcast;

                // Was our battle between two valid players?
                if (participant1 instanceof PlayerParticipant && participant2 instanceof PlayerParticipant)
                {
                    // Create some more shorthand variables for convenience.
                    final EntityPlayer player1Entity = (EntityPlayer) participant1.getEntity();
                    final EntityPlayer player2Entity = (EntityPlayer) participant2.getEntity();

                    if (endedInDraw || battleForfeited)
                    {
                        if (logPVPDraws)
                        {
                            // Print a PvP draw message to console.
                            printUnformattedMessage
                            (
                                    "§5PBR §f// §7Players §f" + player1Entity.getName() +
                                    "§7 and §f" + player2Entity.getName() +
                                    "§7 ended their battle in a draw, in world \"§f" + worldName +
                                    "§7\", at X:§f" + location.getX() +
                                    "§7 Y:§f" + location.getY() +
                                    "§7 Z:§f" + location.getZ()
                            );
                        }

                        if (showPVPDraws)
                        {
                            // Get a broadcast from the broadcasts config file, if the key can be found.
                            broadcast = getBroadcast("broadcast.draw.pvp");

                            // Did we find a message? Iterate all available players, and send to those who should receive!
                            if (broadcast != null)
                            {
                                // Did we find a message? Iterate all available players, and send to those who should receive!
                                iterateAndSendBroadcast(broadcast, null, null,
                                        player1Entity, player2Entity, false, false, false,
                                        "draw.pvp", "showPVPDraw");
                            }
                        }
                    }
                    else
                    {
                        if (logPVPVictories)
                        {
                            // Print a PvP victory message to console.
                            printUnformattedMessage
                            (
                                    "§5PBR §f// §7Player §f" + participant1.getName().getUnformattedText() +
                                    "§7 defeated player §f" + participant2.getName().getUnformattedText() +
                                    "§7 in world \"§f" + worldName +
                                    "§7\", at X:§f" + location.getX() +
                                    "§7 Y:§f" + location.getY() +
                                    "§7 Z:§f" + location.getZ()
                            );
                        }

                        if (showPVPVictories)
                        {
                            // Get a broadcast from the broadcasts config file, if the key can be found.
                            broadcast = getBroadcast("broadcast.victory.pvp");

                            // Did we find a message? Iterate all available players, and send to those who should receive!
                            if (broadcast != null)
                            {
                                // Did we find a message? Iterate all available players, and send to those who should receive!
                                iterateAndSendBroadcast(broadcast, null, null,
                                        player1Entity, player2Entity, false, false, false,
                                        "victory.pvp", "showPVPVictory");
                            }
                        }
                    }
                }
                // Did a trainer win from a player? Participant orders got figured out earlier, if a winner and loser were present.
                else if (participant1 instanceof TrainerParticipant && participant2 instanceof PlayerParticipant)
                {
                    // We have a trainer, so create some convenient variables to avoid repeated casts.
                    final TrainerParticipant trainer = (TrainerParticipant) participant1;
                    final EntityPlayer playerEntity = (EntityPlayer) participant2.getEntity();

                    // Was the battle forfeited? I thiiink only the player can do this, right now.
                    if (battleForfeited)
                    {
                        // Is our trainer a boss trainer?
                        if (trainer.trainer.getBossMode().isBossPokemon())
                        {
                            if (logBossTrainerForfeits)
                            {
                                // Print a forfeit message to console.
                                printUnformattedMessage
                                (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 fled from a boss trainer in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                                );
                            }

                            if (showBossTrainerForfeits)
                            {
                                // Get a broadcast from the broadcasts config file, if the key can be found.
                                broadcast = getBroadcast("broadcast.forfeit.boss_trainer");

                                // Did we find a message? Iterate all available players, and send to those who should receive!
                                if (broadcast != null)
                                {
                                    iterateAndSendBroadcast(broadcast, null, null,
                                            playerEntity, null, false, false, false,
                                            "forfeit.bosstrainer", "showBossTrainerForfeit");
                                }
                            }
                        }
                        else
                        {
                            if (logTrainerForfeits)
                            {
                                // Print a forfeit message to console.
                                printUnformattedMessage
                                (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 fled from a normal trainer in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                                );
                            }

                            if (showTrainerForfeits)
                            {
                                // Get a broadcast from the broadcasts config file, if the key can be found.
                                broadcast = getBroadcast("broadcast.forfeit.trainer");

                                // Did we find a message? Iterate all available players, and send to those who should receive!
                                if (broadcast != null)
                                {
                                    iterateAndSendBroadcast(broadcast, null, null,
                                            playerEntity, null,false, false, false,
                                            "forfeit.trainer", "showTrainerForfeit");
                                }
                            }
                        }
                    }
                    else
                    {
                        // Is our trainer a boss trainer?
                        if (trainer.trainer.getBossMode().isBossPokemon())
                        {
                            if (logBossTrainerBlackouts)
                            {
                                // Print a blackout message to console.
                                printUnformattedMessage
                                (
                                        "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                        "§1 was knocked out by a boss trainer in world \"§9" + worldName +
                                        "§1\", at X:§9" + location.getX() +
                                        "§1 Y:§9" + location.getY() +
                                        "§1 Z:§9" + location.getZ()
                                );
                            }

                            if (showBossTrainerBlackouts)
                            {
                                // Get a broadcast from the broadcasts config file, if the key can be found.
                                broadcast = getBroadcast("broadcast.blackout.boss_trainer");

                                // Did we find a message? Iterate all available players, and send to those who should receive!
                                if (broadcast != null)
                                {
                                    iterateAndSendBroadcast(broadcast, null, null,
                                            playerEntity, null, false, false, false,
                                            "blackout.bosstrainer", "showBossTrainerBlackout");
                                }
                            }
                        }
                        else if (showTrainerBlackouts)
                        {
                            if (logTrainerBlackouts)
                            {
                                // Print a blackout message to console.
                                printUnformattedMessage
                                (
                                        "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                        "§1 was knocked out by a normal trainer in world \"§9" + worldName +
                                        "§1\", at X:§9" + location.getX() +
                                        "§1 Y:§9" + location.getY() +
                                        "§1 Z:§9" + location.getZ()
                                );
                            }

                            if (showTrainerBlackouts)
                            {
                                // Get a broadcast from the broadcasts config file, if the key can be found.
                                broadcast = getBroadcast("broadcast.blackout.trainer");

                                // Did we find a message? Iterate all available players, and send to those who should receive!
                                if (broadcast != null)
                                {
                                    iterateAndSendBroadcast(broadcast, null, null,
                                            playerEntity, null, false, false, false,
                                            "blackout.trainer", "showTrainerBlackout");
                                }
                            }
                        }
                    }
                }
                // Did a player defeat a trainer? Participant orders got figured out earlier, if a winner and loser were present.
                else if (participant1 instanceof PlayerParticipant && participant2 instanceof TrainerParticipant)
                {
                    // We have a trainer, so create some convenient variables to avoid repeated casts.
                    final TrainerParticipant trainer2 = (TrainerParticipant) participant2;
                    final EntityPlayer playerEntity = (EntityPlayer) participant1.getEntity();

                    // Is our trainer a boss trainer?
                    if ((trainer2.trainer.getBossMode().isBossPokemon()))
                    {
                        if (logBossTrainerVictories)
                        {
                            // Print a victory message to console.
                            printUnformattedMessage
                            (
                                    "§5PBR §f// §1Player §9" + participant1.getName().getUnformattedText() +
                                    "§1 defeated a boss trainer in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        if (showBossTrainerVictories)
                        {
                            // Get a broadcast from the broadcasts config file, if the key can be found.
                            broadcast = getBroadcast("broadcast.victory.boss_trainer");

                            // Did we find a message? Iterate all available players, and send to those who should receive!
                            if (broadcast != null)
                            {
                                iterateAndSendBroadcast(broadcast, null, null,
                                        playerEntity, null, false, false, false,
                                        "victory.bosstrainer", "showBossTrainerVictory");
                            }
                        }
                    }
                    else
                    {
                        if (logTrainerVictories)
                        {
                            // Print a victory message to console.
                            printUnformattedMessage
                            (
                                    "§5PBR §f// §1Player §9" + participant1.getName().getUnformattedText() +
                                    "§1 defeated a normal trainer in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        if (showTrainerVictories)
                        {
                            // Get a broadcast from the broadcasts config file, if the key can be found.
                            broadcast = getBroadcast("broadcast.victory.trainer");

                            // Did we find a message? Iterate all available players, and send to those who should receive!
                            if (broadcast != null)
                            {
                                iterateAndSendBroadcast(broadcast, null, null,
                                        playerEntity, null, false, false, false,
                                        "victory.trainer", "showTrainerVictory");
                            }
                        }
                    }
                }
                // Did a player lose to a wild Pokémon? Participant orders got figured out earlier, if a winner and loser were present.
                else if (participant1 instanceof WildPixelmonParticipant && participant2 instanceof PlayerParticipant && !endedInFlee)
                {
                    // Create shorthand variables for convenience.
                    final EntityPlayer playerEntity = (EntityPlayer) participant2.getEntity();
                    final EntityPixelmon pokemonEntity = (EntityPixelmon) participant1.getEntity();
                    final String baseName = pokemonEntity.getPokemonName();
                    final String localizedName = pokemonEntity.getLocalizedName();

                    // If we're in a localized setup, format a string for logging both names.
                    final String nameString =
                        baseName.equals(localizedName) ? baseName : baseName + " §c(§4" + localizedName + "§c)";

                    // Is the Pokémon a boss?
                    if (pokemonEntity.isBossPokemon())
                    {
                        if (logBossBlackouts)
                        {
                            // Print a blackout message to console.
                            printUnformattedMessage
                            (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 was knocked out by a boss §9" + nameString +
                                    "§1 in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        if (showBossBlackouts)
                        {
                            // Get a broadcast from the broadcasts config file, if the key can be found.
                            broadcast = getBroadcast("broadcast.blackout.boss");

                            // Did we find a message? Iterate all available players, and send to those who should receive!
                            if (broadcast != null)
                            {
                                iterateAndSendBroadcast(broadcast, pokemonEntity, null, playerEntity,
                                        null, hoverBossBlackouts, false, revealBossBlackouts,
                                        "blackout.boss", "showBossBlackout");
                            }
                        }
                    }
                    else if (EnumSpecies.legendaries.contains(baseName) && pokemonEntity.getPokemonData().isShiny())
                    {
                        if (logLegendaryBlackouts || logShinyBlackouts)
                        {
                            // Print a blackout message to console.
                            printUnformattedMessage
                            (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 was knocked out by a shiny legendary §9" + nameString +
                                    "§1 in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        if (showLegendaryBlackouts)
                        {
                            // Get a broadcast from the broadcasts config file, if the key can be found.
                            broadcast = getBroadcast("broadcast.blackout.shiny_legendary");

                            // Did we find a message? Iterate all available players, and send to those who should receive!
                            if (broadcast != null)
                            {
                                iterateAndSendBroadcast(broadcast, pokemonEntity, null, playerEntity,
                                        null, hoverLegendaryBlackouts, false, revealLegendaryBlackouts,
                                        "blackout.shinylegendary", "showLegendaryBlackout", "showShinyBlackout");
                            }
                        }
                        else if (showShinyBlackouts)
                        {
                            // Get a broadcast from the broadcasts config file, if the key can be found.
                            broadcast = getBroadcast("broadcast.blackout.shiny_legendary");

                            // Did we find a message? Iterate all available players, and send to those who should receive!
                            if (broadcast != null)
                            {
                                iterateAndSendBroadcast(broadcast, pokemonEntity, null, playerEntity,
                                        null, hoverShinyBlackouts, false, revealShinyBlackouts,
                                        "blackout.shinylegendary", "showLegendaryBlackout", "showShinyBlackout");
                            }
                        }
                    }
                    else if (EnumSpecies.legendaries.contains(baseName))
                    {
                        if (logLegendaryBlackouts)
                        {
                            // Print a blackout message to console.
                            printUnformattedMessage
                            (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 was knocked out by a legendary §9" + nameString +
                                    "§1 in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        if (showLegendaryBlackouts)
                        {
                            // Get a broadcast from the broadcasts config file, if the key can be found.
                            broadcast = getBroadcast("broadcast.blackout.legendary");

                            // Did we find a message? Iterate all available players, and send to those who should receive!
                            if (broadcast != null)
                            {
                                iterateAndSendBroadcast(broadcast, pokemonEntity, null, playerEntity,
                                        null, hoverLegendaryBlackouts, false, revealLegendaryBlackouts,
                                        "blackout.legendary", "showLegendaryBlackout");
                            }
                        }
                    }
                    else if (pokemonEntity.getPokemonData().isShiny())
                    {
                        if (logShinyBlackouts)
                        {
                            // Print a blackout message to console.
                            printUnformattedMessage
                            (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 was knocked out by a shiny §9" + nameString +
                                    "§1 in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        if (showShinyBlackouts)
                        {
                            // Get a broadcast from the broadcasts config file, if the key can be found.
                            broadcast = getBroadcast("broadcast.blackout.shiny");

                            // Did we find a message? Iterate all available players, and send to those who should receive!
                            if (broadcast != null)
                            {
                                iterateAndSendBroadcast(broadcast, pokemonEntity, null, playerEntity,
                                        null, hoverShinyBlackouts, false, revealShinyBlackouts,
                                        "blackout.shiny", "showShinyBlackout");
                            }
                        }
                    }
                    else
                    {
                        if (logNormalBlackouts)
                        {
                            // Print a blackout message to console.
                            printUnformattedMessage
                            (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 was knocked out by a normal §9" + nameString +
                                    "§1 in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        if (showNormalBlackouts)
                        {
                            // Get a broadcast from the broadcasts config file, if the key can be found.
                            broadcast = getBroadcast("broadcast.blackout.normal");

                            // Did we find a message? Iterate all available players, and send to those who should receive!
                            if (broadcast != null)
                            {
                                iterateAndSendBroadcast(broadcast, pokemonEntity, null, playerEntity,
                                        null, hoverNormalBlackouts, false, revealNormalBlackouts,
                                        "blackout.normal", "showNormalBlackout");
                            }
                        }
                    }
                }
                // Did a player flee from battle?
                else if (endedInFlee)
                {
                    // Create shorthand variables for convenience.
                    final EntityPlayer playerEntity = (EntityPlayer) participant2.getEntity();
                    final EntityPixelmon pokemonEntity = (EntityPixelmon) participant1.getEntity();
                    final String baseName = pokemonEntity.getPokemonName();
                    final String localizedName = pokemonEntity.getLocalizedName();

                    // If we're in a localized setup, format a string for logging both names.
                    final String nameString =
                        baseName.equals(localizedName) ? baseName : baseName + " §1(§9" + localizedName + "§1)";

                    // Is the Pokémon a boss?
                    if (pokemonEntity.isBossPokemon())
                    {
                        if (logBossForfeits)
                        {
                            // Print a forfeit message to console.
                            printUnformattedMessage
                            (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 fled from a boss §9" + nameString +
                                    "§1 in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        if (showBossForfeits)
                        {
                            // Get a broadcast from the broadcasts config file, if the key can be found.
                            broadcast = getBroadcast("broadcast.forfeit.boss");

                            // Did we find a message? Iterate all available players, and send to those who should receive!
                            if (broadcast != null)
                            {
                                iterateAndSendBroadcast(broadcast, pokemonEntity, null, playerEntity,
                                        null, hoverBossForfeits, false, revealBossForfeits,
                                        "forfeit.boss", "showBossForfeit");
                            }
                        }
                    }
                    else if (EnumSpecies.legendaries.contains(baseName) && pokemonEntity.getPokemonData().isShiny())
                    {
                        if (logLegendaryForfeits || showShinyForfeits)
                        {
                            // Print a forfeit message to console.
                            printUnformattedMessage
                            (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 fled from a shiny legendary §9" + nameString +
                                    "§1 in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        if (showLegendaryForfeits)
                        {
                            // Get a broadcast from the broadcasts config file, if the key can be found.
                            broadcast = getBroadcast("broadcast.forfeit.shiny_legendary");

                            // Did we find a message? Iterate all available players, and send to those who should receive!
                            if (broadcast != null)
                            {
                                iterateAndSendBroadcast(broadcast, pokemonEntity, null, playerEntity,
                                        null, hoverLegendaryForfeits, false, revealLegendaryForfeits,
                                        "forfeit.shinylegendary", "showLegendaryForfeit", "showShinyForfeit");
                            }
                        }
                        else if (showShinyForfeits)
                        {
                            // Get a broadcast from the broadcasts config file, if the key can be found.
                            broadcast = getBroadcast("broadcast.forfeit.shiny_legendary");

                            // Did we find a message? Iterate all available players, and send to those who should receive!
                            if (broadcast != null)
                            {
                                iterateAndSendBroadcast(broadcast, pokemonEntity, null, playerEntity,
                                        null, hoverShinyForfeits, false, revealShinyForfeits,
                                        "forfeit.shinylegendary", "showLegendaryForfeit", "showShinyForfeit");
                            }
                        }
                    }
                    else if (EnumSpecies.legendaries.contains(baseName))
                    {
                        if (logLegendaryForfeits)
                        {
                            // Print a forfeit message to console.
                            printUnformattedMessage
                            (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 fled from a legendary §9" + nameString +
                                    "§1 in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        if (showLegendaryForfeits)
                        {
                            // Get a broadcast from the broadcasts config file, if the key can be found.
                            broadcast = getBroadcast("broadcast.forfeit.legendary");

                            // Did we find a message? Iterate all available players, and send to those who should receive!
                            if (broadcast != null)
                            {
                                iterateAndSendBroadcast(broadcast, pokemonEntity, null, playerEntity,
                                        null, hoverLegendaryForfeits, false, revealLegendaryForfeits,
                                        "forfeit.legendary", "showLegendaryForfeit");
                            }
                        }
                    }
                    else if (pokemonEntity.getPokemonData().isShiny())
                    {
                        if (logShinyForfeits)
                        {
                            // Print a forfeit message to console.
                            printUnformattedMessage
                            (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 fled from a shiny §9" + nameString +
                                    "§1 in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        if (showShinyForfeits)
                        {
                            // Get a broadcast from the broadcasts config file, if the key can be found.
                            broadcast = getBroadcast("broadcast.forfeit.shiny");

                            // Did we find a message? Iterate all available players, and send to those who should receive!
                            if (broadcast != null)
                            {
                                iterateAndSendBroadcast(broadcast, pokemonEntity,null, playerEntity,
                                        null, hoverShinyForfeits, false, revealShinyForfeits,
                                        "forfeit.shiny", "showShinyForfeit");
                            }
                        }
                    }
                }
            }
        }
    }
}
