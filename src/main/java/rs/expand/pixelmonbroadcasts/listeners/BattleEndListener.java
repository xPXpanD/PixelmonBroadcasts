// Listens for ended battles.
package rs.expand.pixelmonbroadcasts.listeners;

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
import rs.expand.pixelmonbroadcasts.enums.Events;
import rs.expand.pixelmonbroadcasts.enums.EnumLogStrings;
import rs.expand.pixelmonbroadcasts.enums.LogStrings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.logger;
import static rs.expand.pixelmonbroadcasts.utilities.PlaceholderMethods.iterateAndBroadcast;

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
            //logger.info("Looping. Participant is " + entry.getValue().name() + ", result is " + entry.getKey().getName());

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
                // Was our battle between two valid players?
                if (participant1 instanceof PlayerParticipant && participant2 instanceof PlayerParticipant)
                {
                    // Create some more shorthand variables for convenience.
                    final EntityPlayer player1Entity = (EntityPlayer) participant1.getEntity();
                    final EntityPlayer player2Entity = (EntityPlayer) participant2.getEntity();

                    if (endedInDraw || battleForfeited)
                    {
                        if (Events.Others.DRAW.settings.toLowerCase().contains("log"))
                        {
                            // Print a PvP draw message to console, if enabled.
                            logger.info
                            (
                                    "§5PBR §f// §7Players §f" + player1Entity.getName() +
                                    "§7 and §f" + player2Entity.getName() +
                                    "§7 ended their battle in a draw, in world \"§f" + worldName +
                                    "§7\", at X:§f" + location.getX() +
                                    "§7 Y:§f" + location.getY() +
                                    "§7 Z:§f" + location.getZ()
                            );
                        }

                        // Check whether any broadcasts are enabled, and send them to people who are set up to receive them.
                        iterateAndBroadcast(
                                Events.Others.DRAW, null, null, player1Entity, player2Entity);
                    }
                    else
                    {
                        if (Events.Victories.PVP.settings.toLowerCase().contains("log"))
                        {
                            // Print a PvP victory message to console, if enabled.
                            logger.info
                            (
                                    "§5PBR §f// §7Player §f" + participant1.getName().getUnformattedText() +
                                    "§7 defeated player §f" + participant2.getName().getUnformattedText() +
                                    "§7 in world \"§f" + worldName +
                                    "§7\", at X:§f" + location.getX() +
                                    "§7 Y:§f" + location.getY() +
                                    "§7 Z:§f" + location.getZ()
                            );
                        }

                        // Check whether any broadcasts are enabled, and send them to people who are set up to receive them.
                        iterateAndBroadcast(
                                Events.Victories.PVP, null, null, player1Entity, player2Entity);
                    }
                }
                // Did a trainer win from a player? Participant orders got figured out earlier, if a winner and loser were present.
                else if (participant1 instanceof TrainerParticipant && participant2 instanceof PlayerParticipant)
                {
                    // We have a trainer, so create some convenient variables to avoid repetition.
                    final TrainerParticipant trainer = (TrainerParticipant) participant1;
                    final EntityPlayer playerEntity = (EntityPlayer) participant2.getEntity();
                    final String playerName = participant2.getName().getUnformattedText();

                    // Was the battle forfeited? I thiiink only the player can do this, right now.
                    if (battleForfeited)
                    {
                        // Is our trainer a boss trainer?
                        if (trainer.trainer.getBossMode().isBossPokemon())
                        {
                            // Print a forfeit message to console, if enabled.
                            if (Events.Forfeits.BOSS_TRAINER.settings.toLowerCase().contains("log"))
                                printForfeitMessage(playerName, "boss trainer", worldName, location);

                            // Check whether any broadcasts are enabled, and send them to people who are set up to receive them.
                            iterateAndBroadcast(
                                    Events.Forfeits.BOSS_TRAINER, null, null, playerEntity, null);
                        }
                        else
                        {
                            if (Events.Forfeits.TRAINER.settings.toLowerCase().contains("log"))
                                printForfeitMessage(playerName, "normal trainer", worldName, location);

                            // Check whether any broadcasts are enabled, and send them to people who are set up to receive them.
                            iterateAndBroadcast(
                                    Events.Forfeits.TRAINER, null, null, playerEntity, null);
                        }
                    }
                    else
                    {
                        // Is our trainer a boss trainer?
                        if (trainer.trainer.getBossMode().isBossPokemon())
                        {
                            // Print a blackout message to console, if enabled.
                            if (Events.Blackouts.BOSS_TRAINER.settings.toLowerCase().contains("log"))
                                printBlackoutMessage(playerName, "boss trainer", worldName, location);

                            // Check whether any broadcasts are enabled, and send them to people who are set up to receive them.
                            iterateAndBroadcast(
                                    Events.Blackouts.BOSS_TRAINER, null, null, playerEntity, null);
                        }
                        else
                        {
                            if (Events.Blackouts.TRAINER.settings.toLowerCase().contains("log"))
                                printBlackoutMessage(playerName, "normal trainer", worldName, location);

                            // Check whether any broadcasts are enabled, and send them to people who are set up to receive them.
                            iterateAndBroadcast(
                                    Events.Blackouts.TRAINER, null, null, playerEntity, null);
                        }
                    }
                }
                // Did a player defeat a trainer? Participant orders got figured out earlier, if a winner and loser were present.
                else if (participant1 instanceof PlayerParticipant && participant2 instanceof TrainerParticipant)
                {
                    // We have a trainer, so create some convenient variables to avoid repeated casts.
                    final TrainerParticipant trainer2 = (TrainerParticipant) participant2;
                    final EntityPlayer playerEntity = (EntityPlayer) participant1.getEntity();
                    final String playerName = participant1.getName().getUnformattedText();

                    // Is our trainer a boss trainer?
                    if ((trainer2.trainer.getBossMode().isBossPokemon()))
                    {
                        // Print a victory message to console, if enabled.
                        if (Events.Victories.BOSS_TRAINER.settings.toLowerCase().contains("log"))
                            printVictoryMessage(playerName, "boss trainer", worldName, location);

                        // Check whether any broadcasts are enabled, and send them to people who are set up to receive them.
                        iterateAndBroadcast(
                                Events.Victories.BOSS_TRAINER, null, null, playerEntity, null);
                    }
                    else
                    {
                        // Print a victory message to console, if enabled.
                        if (Events.Victories.TRAINER.settings.toLowerCase().contains("log"))
                            printVictoryMessage(playerName, "normal trainer", worldName, location);

                        // Check whether any broadcasts are enabled, and send them to people who are set up to receive them.
                        iterateAndBroadcast(
                                Events.Victories.TRAINER, null, null, playerEntity, null);
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
                    final String playerName = participant2.getName().getUnformattedText();

                    // If we're in a localized setup, format a string for logging both names.
                    final String nameString =
                        baseName.equals(localizedName) ? " §4" + baseName + "§c" : " §4" + baseName + " §c(§4" + localizedName + "§c)";

                    // Is the Pokémon a boss?
                    if (pokemonEntity.isBossPokemon())
                    {
                        // Print a blackout message to console, if enabled.
                        if (Events.Blackouts.BOSS.settings.toLowerCase().contains("log"))
                            printBlackoutMessage(playerName, "boss" + nameString, worldName, location);

                        // Check whether any broadcasts are enabled, and send them to people who are set up to receive them.
                        iterateAndBroadcast(
                                Events.Blackouts.BOSS, pokemonEntity, null, playerEntity, null);
                    }
                    else if (EnumSpecies.legendaries.contains(baseName) && pokemonEntity.getPokemonData().isShiny())
                    {
                        // Print a blackout message to console, if enabled.
                        if (Events.Blackouts.SHINY_LEGENDARY.settings.toLowerCase().contains("log"))
                            printBlackoutMessage(playerName, "shiny legendary" + nameString, worldName, location);

                        // Check whether any broadcasts are enabled, and send them to people who are set up to receive them.
                        iterateAndBroadcast(
                                Events.Blackouts.SHINY_LEGENDARY, pokemonEntity, null, playerEntity, null);
                    }
                    else if (EnumSpecies.legendaries.contains(baseName))
                    {
                        // Print a blackout message to console, if enabled.
                        if (Events.Blackouts.LEGENDARY.settings.toLowerCase().contains("log"))
                            printBlackoutMessage(playerName, "legendary" + nameString, worldName, location);

                        // Check whether any broadcasts are enabled, and send them to people who are set up to receive them.
                        iterateAndBroadcast(
                                Events.Blackouts.LEGENDARY, pokemonEntity, null, playerEntity, null);
                    }
                    else if (EnumSpecies.ultrabeasts.contains(baseName) && pokemonEntity.getPokemonData().isShiny())
                    {
                        // Print a blackout message to console, if enabled.
                        if (Events.Blackouts.SHINY_ULTRA_BEAST.settings.toLowerCase().contains("log"))
                            printBlackoutMessage(playerName, "shiny" + nameString + " Ultra Beast", worldName, location);

                        // Check whether any broadcasts are enabled, and send them to people who are set up to receive them.
                        iterateAndBroadcast(
                                Events.Blackouts.SHINY_ULTRA_BEAST, pokemonEntity, null, playerEntity, null);
                    }
                    else if (EnumSpecies.ultrabeasts.contains(baseName))
                    {
                        // Print a blackout message to console, if enabled.
                        if (Events.Blackouts.ULTRA_BEAST.settings.toLowerCase().contains("log"))
                            printBlackoutMessage(playerName, nameString + " Ultra Beast", worldName, location);
                        // Check whether any broadcasts are enabled, and send them to people who are set up to receive them.
                        iterateAndBroadcast(
                                Events.Blackouts.ULTRA_BEAST, pokemonEntity, null, playerEntity, null);
                    }
                    else if (pokemonEntity.getPokemonData().isShiny())
                    {
                        // Print a blackout message to console, if enabled.
                        if (Events.Blackouts.SHINY.settings.toLowerCase().contains("log"))
                            printBlackoutMessage(playerName, "shiny" + nameString, worldName, location);

                        // Check whether any broadcasts are enabled, and send them to people who are set up to receive them.
                        iterateAndBroadcast(
                                Events.Blackouts.SHINY, pokemonEntity, null, playerEntity, null);
                    }
                    else
                    {
                        // Print a blackout message to console, if enabled.
                        if (Events.Blackouts.NORMAL.settings.toLowerCase().contains("log"))
                            printBlackoutMessage(playerName, "shiny legendary" + nameString, worldName, location);

                        // Check whether any broadcasts are enabled, and send them to people who are set up to receive them.
                        iterateAndBroadcast(
                                Events.Blackouts.NORMAL, pokemonEntity, null, playerEntity, null);
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
                    final String playerName = participant2.getName().getUnformattedText();

                    // If we're in a localized setup, format a string for logging both names.
                    final String nameString =
                        baseName.equals(localizedName) ? baseName : baseName + " §1(§9" + localizedName + "§1)";

                    // Is the Pokémon a boss?
                    if (pokemonEntity.isBossPokemon())
                    {
                        // Print a forfeit message to console, if enabled.
                        if (Events.Forfeits.BOSS.settings.toLowerCase().contains("log"))
                            printForfeitMessage(playerName, "boss" + nameString, worldName, location);

                        // Check whether any broadcasts are enabled, and send them to people who are set up to receive them.
                        iterateAndBroadcast(
                                Events.Forfeits.BOSS, pokemonEntity, null, playerEntity, null);
                    }
                    else if (EnumSpecies.legendaries.contains(baseName) && pokemonEntity.getPokemonData().isShiny())
                    {
                        if (logLegendaryForfeits || logShinyForfeits)
                        {
                            // Print a forfeit message to console, if enabled.
                            logger.info
                            (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 fled from a shiny legendary §9" + nameString +
                                    "§1 in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        // Check whether any broadcasts are enabled, and send them to people who are set up to receive them.
                        iterateAndBroadcast(
                                Events.Forfeits.SHINY_LEGENDARY, pokemonEntity, null, playerEntity, null);

                        if (printLegendaryForfeits || notifyLegendaryForfeits)
                        {
                            if (printLegendaryForfeits)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                                doBroadcast(
                                        EnumBroadcastTypes.PRINT, Events.Forfeits.SHINY_LEGENDARY_AS_LEGENDARY,
                                        pokemonEntity, null, playerEntity, null);
                            }

                            if (notifyLegendaryForfeits)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                                doBroadcast(
                                        EnumBroadcastTypes.NOTIFY, Events.Forfeits.SHINY_LEGENDARY_AS_LEGENDARY,
                                        pokemonEntity, null, playerEntity, null);
                            }
                        }
                        else if (printShinyForfeits || notifyShinyForfeits)
                        {
                            if (printShinyForfeits)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                                doBroadcast(
                                        EnumBroadcastTypes.PRINT, Events.Forfeits.SHINY_LEGENDARY_AS_SHINY,
                                        pokemonEntity, null, playerEntity, null);
                            }

                            if (notifyShinyForfeits)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                                doBroadcast(
                                        EnumBroadcastTypes.NOTIFY, Events.Forfeits.SHINY_LEGENDARY_AS_SHINY,
                                        pokemonEntity, null, playerEntity, null);
                            }
                        }
                    }
                    else if (EnumSpecies.legendaries.contains(baseName))
                    {
                        if (Events.Hatches.SHINY.settings.toLowerCase().contains("log"))
                        {
                            // Print a forfeit message to console, if enabled.
                            logger.info
                            (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 fled from a legendary §9" + nameString +
                                    "§1 in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        // Check whether any broadcasts are enabled, and send them to people who are set up to receive them.
                        iterateAndBroadcast(
                                Events.Blackouts.BOSS_TRAINER, null, null, playerEntity, null);

                        if (printLegendaryForfeits)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                            doBroadcast(EnumBroadcastTypes.PRINT, Events.Forfeits.LEGENDARY,
                                    pokemonEntity, null, playerEntity, null);
                        }

                        if (notifyLegendaryForfeits)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                            doBroadcast(EnumBroadcastTypes.NOTIFY, Events.Forfeits.LEGENDARY,
                                    pokemonEntity, null, playerEntity, null);
                        }
                    }
                    else if (EnumSpecies.ultrabeasts.contains(baseName) && pokemonEntity.getPokemonData().isShiny())
                    {
                        if (logUltraBeastForfeits || logShinyForfeits)
                        {
                            // Print a forfeit message to console, if enabled.
                            logger.info
                            (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 fled from a shiny §9" + nameString +
                                    "§1 Ultra Beast in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        // Check whether any broadcasts are enabled, and send them to people who are set up to receive them.
                        iterateAndBroadcast(
                                Events.Blackouts.BOSS_TRAINER, null, null, playerEntity, null);

                        if (printUltraBeastForfeits || notifyUltraBeastForfeits)
                        {
                            if (printUltraBeastForfeits)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                                doBroadcast(
                                        EnumBroadcastTypes.PRINT, Events.Forfeits.SHINY_ULTRA_BEAST_AS_ULTRA_BEAST,
                                        pokemonEntity, null, playerEntity, null);
                            }

                            if (notifyUltraBeastForfeits)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                                doBroadcast(
                                        EnumBroadcastTypes.NOTIFY, Events.Forfeits.SHINY_ULTRA_BEAST_AS_ULTRA_BEAST,
                                        pokemonEntity, null, playerEntity, null);
                            }
                        }
                        else if (printShinyForfeits || notifyShinyForfeits)
                        {
                            if (printShinyForfeits)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                                doBroadcast(
                                        EnumBroadcastTypes.PRINT, Events.Forfeits.SHINY_ULTRA_BEAST_AS_SHINY,
                                        pokemonEntity, null, playerEntity, null);
                            }

                            if (notifyShinyForfeits)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                                doBroadcast(
                                        EnumBroadcastTypes.NOTIFY, Events.Forfeits.SHINY_ULTRA_BEAST_AS_SHINY,
                                        pokemonEntity, null, playerEntity, null);
                            }
                        }
                    }
                    else if (EnumSpecies.ultrabeasts.contains(baseName))
                    {
                        if (Events.Hatches.SHINY.settings.toLowerCase().contains("log"))
                        {
                            // Print a forfeit message to console, if enabled.
                            logger.info
                            (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 fled from a §9" + nameString +
                                    "§1 Ultra Beast in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        // Check whether any broadcasts are enabled, and send them to people who are set up to receive them.
                        iterateAndBroadcast(
                                Events.Blackouts.BOSS_TRAINER, null, null, playerEntity, null);

                        if (printUltraBeastForfeits)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                            doBroadcast(EnumBroadcastTypes.PRINT, Events.Forfeits.ULTRA_BEAST,
                                    pokemonEntity, null, playerEntity, null);
                        }

                        if (notifyUltraBeastForfeits)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                            doBroadcast(EnumBroadcastTypes.NOTIFY, Events.Forfeits.ULTRA_BEAST,
                                    pokemonEntity, null, playerEntity, null);
                        }
                    }
                    else if (pokemonEntity.getPokemonData().isShiny())
                    {
                        if (Events.Hatches.SHINY.settings.toLowerCase().contains("log"))
                        {
                            // Print a forfeit message to console, if enabled.
                            logger.info
                            (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 fled from a shiny §9" + nameString +
                                    "§1 in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        // Check whether any broadcasts are enabled, and send them to people who are set up to receive them.
                        iterateAndBroadcast(
                                Events.Blackouts.BOSS_TRAINER, null, null, playerEntity, null);

                        if (printShinyForfeits)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                            doBroadcast(EnumBroadcastTypes.PRINT, Events.Forfeits.SHINY,
                                    pokemonEntity, null, playerEntity, null);
                        }

                        if (notifyShinyForfeits)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                            doBroadcast(EnumBroadcastTypes.NOTIFY, Events.Forfeits.SHINY,
                                    pokemonEntity, null, playerEntity, null);
                        }
                    }
                }
            }
        }
    }

    // Prints an event message to console, if enabled. One size fits all, with the help of the LogType enum.
    private void logEvent(final LogStrings logType, final String worldName, final BlockPos location, final String... inputs)
    {
        if (logType == LogStrings.SPAWN) // Spawn logging needs some special logic.
        {
            logger.info
            (
                    "§5PBR §f// §1A wild " + inputs[0] +
                    " has spawned in world \"" + worldName +
                    "\", at X" + location.getX() +
                    " Y" + location.getY() +
                    " Z" + location.getZ()
            );
        }
        else if (logType.message.length == 3)
        {
            // An example of the Trade event follows.
            logger.info
            (
                    "§5PBR §f// §1Player " +
            //      player 1    traded their         pokémon 1   for
                    inputs[0] + logType.message[0] + inputs[1] + logType.message[1] +
            //      player 2    's                   pokémon 2
                    inputs[2] + logType.message[2] + inputs[3] +
                    " in world \"" + worldName +
                    "\", at X" + location.getX() +
                    " Y" + location.getY() +
                    " Z" + location.getZ()
            );
        }
        else
        {
            // An example of the Forfeit event follows.
            logger.info
            (
                    "§5PBR §f// §1Player " +
            //      player      fled from a          pokémon/trainer
                    inputs[0] + logType.message[0] + inputs[1] +
                    " in world \"" + worldName +
                    "\", at X" + location.getX() +
                    " Y" + location.getY() +
                    " Z" + location.getZ()
            );
        }
    }
}
