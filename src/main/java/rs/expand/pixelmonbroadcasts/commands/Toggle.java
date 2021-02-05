// Allows people to toggle individual event notifications through a fancy clickable menu.
package rs.expand.pixelmonbroadcasts.commands;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import rs.expand.pixelmonbroadcasts.enums.EventData;

import java.util.ArrayList;
import java.util.List;

import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.commandAlias;
import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.logger;
import static rs.expand.pixelmonbroadcasts.utilities.PlaceholderMethods.canReceiveBroadcast;
import static rs.expand.pixelmonbroadcasts.utilities.PlaceholderMethods.checkToggleStatus;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.getTranslation;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.sendTranslation;

// TODO: Maybe get paginated lists working.
public class Toggle extends HubCommand
{
    // Create a List of Texts that we can dump ready-to-print messages into. Goes down with the class instance.
    List<TextComponentString> toggleMessageList = new ArrayList<>();

    @Override
    public String getName()
    {
        return "toggle";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/pixelmonbroadcasts toggle <event flag>";
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return true;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
    {
        // Were we called by a player? Let's not try toggling flags on things that can't have flags.
        if (sender instanceof EntityPlayer)
        {
            if (commandAlias == null)
            {
                logger.error("Could not read config node \"§4commandAlias§c\" while executing toggle command.");
                logger.error("We'll continue with the command, but aliases will break. Check your config.");
            }

            // Do we have an argument? If valid, use it to toggle the matching setting and then show. (if valid)
            if (args.length > 1)
            {
                // We have an argument, extract it.
                final String input = args[1];

                // See if the argument is a valid flag, either from a remote caller or from getClickableLine.
                switch (input)
                {
                    // Normals.
                    case "showNormalCatch": case "showNormalBlackout": case "showNormalHatch":
                    // Legendaries.
                    case "showLegendarySpawn": case "showLegendaryChallenge": case "showLegendaryCatch":
                    case "showLegendaryVictory": case "showLegendaryBlackout": case "showLegendaryForfeit":
                    case "showLegendaryHatch":
                    // Shinies.
                    case "showShinySpawn": case "showShinyChallenge": case "showShinyCatch": case "showShinyVictory":
                    case "showShinyBlackout": case "showShinyForfeit": case "showShinyHatch":
                    // Ultra Beasts.
                    case "showUltraBeastSpawn": case "showUltraBeastChallenge": case "showUltraBeastCatch":
                    case "showUltraBeastVictory": case "showUltraBeastBlackout": case "showUltraBeastForfeit":
                    case "showUltraBeastHatch":
                    // Wormholes.
                    case "showWormholeSpawn":
                    // Generic bosses and boss catch-alls.
                    case "showUncommonBossSpawn": case "showUncommonBossChallenge": case "showUncommonBossVictory":
                    case "showUncommonBossBlackout": case "showBossForfeit":
                    // Rare bosses.
                    case "showRareBossSpawn": case "showRareBossChallenge": case "showRareBossVictory":
                    case "showRareBossBlackout":
                    // Legendary bosses.
                    case "showLegendaryBossSpawn": case "showLegendaryBossChallenge": case "showLegendaryBossVictory":
                    case "showLegendaryBossBlackout":
                    // Ultimate bosses.
                    case "showUltimateBossSpawn": case "showUltimateBossChallenge": case "showUltimateBossVictory":
                    case "showUltimateBossBlackout":
                    // Trainers.
                    case "showTrainerChallenge": case "showTrainerVictory": case "showTrainerBlackout":
                    case "showTrainerForfeit":
                    // Boss trainers.
                    case "showBossTrainerChallenge": case "showBossTrainerVictory": case "showBossTrainerBlackout":
                    case "showBossTrainerForfeit":
                    // PVP stuff.
                    case "showPVPChallenge": case "showPVPVictory": case "showPVPDraw":
                    // Miscellany.
                    case "showEvolve": case "showFaint": case "showTrade": /* case "showBirdTrioSummon": */
                    {
                        // Got a valid flag. Toggle it.
                        toggleFlag(sender, input);
                        break;
                    }
                }
            }

            // Get a player entity.
            EntityPlayer player = (EntityPlayer) sender;

            // These are linked, and used to show available toggles. If one has two entries, the other gets two, too!
            final List<String> messages = new ArrayList<>();
            final List<String> flags = new ArrayList<>();

            // Get the separator message so we don't have to read it dozens of times.
            final String separator = getTranslation("hover.status.separator");

            /*                  *\
               BLACKOUT TOGGLES
            \*                  */
            // Check perms. Add toggle status if perms look good.
            if (canReceiveBroadcast(player, EventData.Blackouts.NORMAL))
            {
                flags.add("showNormalBlackout");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showNormalBlackout"))
                    messages.add(getTranslation("toggle.normal.on") + separator);
                else
                    messages.add(getTranslation("toggle.normal.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Blackouts.SHINY))
            {
                flags.add("showShinyBlackout");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showShinyBlackout"))
                    messages.add(getTranslation("toggle.shiny.on") + separator);
                else
                    messages.add(getTranslation("toggle.shiny.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Blackouts.LEGENDARY))
            {
                flags.add("showLegendaryBlackout");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showLegendaryBlackout"))
                    messages.add(getTranslation("toggle.legendary.on") + separator);
                else
                    messages.add(getTranslation("toggle.legendary.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Blackouts.ULTRA_BEAST))
            {
                flags.add("showUltraBeastBlackout");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showUltraBeastBlackout"))
                    messages.add(getTranslation("toggle.ultra_beast.on") + separator);
                else
                    messages.add(getTranslation("toggle.ultra_beast.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Blackouts.UNCOMMON_BOSS))
            {
                flags.add("showUncommonBossBlackout");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showUncommonBossBlackout"))
                    messages.add(getTranslation("toggle.uncommon_boss.on") + separator);
                else
                    messages.add(getTranslation("toggle.uncommon_boss.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Blackouts.RARE_BOSS))
            {
                flags.add("showRareBossBlackout");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showRareBossBlackout"))
                    messages.add(getTranslation("toggle.rare_boss.on") + separator);
                else
                    messages.add(getTranslation("toggle.rare_boss.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Blackouts.LEGENDARY_BOSS))
            {
                flags.add("showLegendaryBossBlackout");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showLegendaryBossBlackout"))
                    messages.add(getTranslation("toggle.legendary_boss.on") + separator);
                else
                    messages.add(getTranslation("toggle.legendary_boss.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Blackouts.ULTIMATE_BOSS))
            {
                flags.add("showUltimateBossBlackout");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showUltimateBossBlackout"))
                    messages.add(getTranslation("toggle.ultimate_boss.on") + separator);
                else
                    messages.add(getTranslation("toggle.ultimate_boss.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Blackouts.TRAINER))
            {
                flags.add("showTrainerBlackout");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showTrainerBlackout"))
                    messages.add(getTranslation("toggle.trainer.on") + separator);
                else
                    messages.add(getTranslation("toggle.trainer.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Blackouts.BOSS_TRAINER))
            {
                flags.add("showBossTrainerBlackout");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showBossTrainerBlackout"))
                    messages.add(getTranslation("toggle.boss_trainer.on") + separator);
                else
                    messages.add(getTranslation("toggle.boss_trainer.off") + separator);
            }

            // If we have any toggles lined up, print and clear.
            if (!messages.isEmpty())
            {
                // Get and add the "blackout toggles" header message.
                toggleMessageList.add(new TextComponentString(getTranslation("toggle.blackout_toggles")));

                // Submit one or more clickable lines with up to 5 toggles each. Split where necessary.
                addClickableLine(messages, flags);

                // Clear the Lists so we can reuse them, if need be.
                messages.clear();
                flags.clear();
            }

            /*               *\
               CATCH TOGGLES
            \*               */
            // Check perms. Add toggle status if perms look good.
            if (canReceiveBroadcast(player, EventData.Catches.NORMAL))
            {
                flags.add("showNormalCatch");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showNormalCatch"))
                    messages.add(getTranslation("toggle.normal.on") + separator);
                else
                    messages.add(getTranslation("toggle.normal.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Catches.SHINY))
            {
                flags.add("showShinyCatch");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showShinyCatch"))
                    messages.add(getTranslation("toggle.shiny.on") + separator);
                else
                    messages.add(getTranslation("toggle.shiny.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Catches.LEGENDARY))
            {
                flags.add("showLegendaryCatch");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showLegendaryCatch"))
                    messages.add(getTranslation("toggle.legendary.on") + separator);
                else
                    messages.add(getTranslation("toggle.legendary.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Catches.ULTRA_BEAST))
            {
                flags.add("showUltraBeastCatch");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showUltraBeastCatch"))
                    messages.add(getTranslation("toggle.ultra_beast.on") + separator);
                else
                    messages.add(getTranslation("toggle.ultra_beast.off") + separator);
            }

            // If we have any toggles lined up, print and clear.
            if (!messages.isEmpty())
            {
                // Get and add the "catch toggles" header message.
                toggleMessageList.add(new TextComponentString(getTranslation("toggle.catch_toggles")));

                // Submit one or more clickable lines with up to 5 toggles each. Split where necessary.
                addClickableLine(messages, flags);

                // Clear the Lists so we can reuse them, if need be.
                messages.clear();
                flags.clear();
            }

            /*                   *\
               CHALLENGE TOGGLES
            \*                   */
            // Check perms. Add toggle status if perms look good.
            if (canReceiveBroadcast(player, EventData.Challenges.SHINY))
            {
                flags.add("showShinyChallenge");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showShinyChallenge"))
                    messages.add(getTranslation("toggle.shiny.on") + separator);
                else
                    messages.add(getTranslation("toggle.shiny.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Challenges.LEGENDARY))
            {
                flags.add("showLegendaryChallenge");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showLegendaryChallenge"))
                    messages.add(getTranslation("toggle.legendary.on") + separator);
                else
                    messages.add(getTranslation("toggle.legendary.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Challenges.ULTRA_BEAST))
            {
                flags.add("showUltraBeastChallenge");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showUltraBeastChallenge"))
                    messages.add(getTranslation("toggle.ultra_beast.on") + separator);
                else
                    messages.add(getTranslation("toggle.ultra_beast.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Challenges.UNCOMMON_BOSS))
            {
                flags.add("showUncommonBossChallenge");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showUncommonBossChallenge"))
                    messages.add(getTranslation("toggle.uncommon_boss.on") + separator);
                else
                    messages.add(getTranslation("toggle.uncommon_boss.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Challenges.RARE_BOSS))
            {
                flags.add("showRareBossChallenge");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showRareBossChallenge"))
                    messages.add(getTranslation("toggle.rare_boss.on") + separator);
                else
                    messages.add(getTranslation("toggle.rare_boss.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Challenges.LEGENDARY_BOSS))
            {
                flags.add("showLegendaryBossChallenge");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showLegendaryBossChallenge"))
                    messages.add(getTranslation("toggle.legendary_boss.on") + separator);
                else
                    messages.add(getTranslation("toggle.legendary_boss.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Challenges.ULTIMATE_BOSS))
            {
                flags.add("showUltimateBossChallenge");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showUltimateBossChallenge"))
                    messages.add(getTranslation("toggle.ultimate_boss.on") + separator);
                else
                    messages.add(getTranslation("toggle.ultimate_boss.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Challenges.TRAINER))
            {
                flags.add("showTrainerChallenge");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showTrainerChallenge"))
                    messages.add(getTranslation("toggle.trainer.on") + separator);
                else
                    messages.add(getTranslation("toggle.trainer.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Challenges.BOSS_TRAINER))
            {
                flags.add("showBossTrainerChallenge");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showBossTrainerChallenge"))
                    messages.add(getTranslation("toggle.boss_trainer.on") + separator);
                else
                    messages.add(getTranslation("toggle.boss_trainer.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Challenges.PVP))
            {
                flags.add("showPVPChallenge");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showPVPChallenge"))
                    messages.add(getTranslation("toggle.pvp.on") + separator);
                else
                    messages.add(getTranslation("toggle.pvp.off") + separator);
            }

            // If we have any toggles lined up, print and clear.
            if (!messages.isEmpty())
            {
                // Get and add the "challenge toggles" header message.
                toggleMessageList.add(new TextComponentString(getTranslation("toggle.challenge_toggles")));

                // Submit one or more clickable lines with up to 5 toggles each. Split where necessary.
                addClickableLine(messages, flags);

                // Clear the Lists so we can reuse them, if need be.
                messages.clear();
                flags.clear();
            }

            /*                 *\
               FORFEIT TOGGLES
            \*                 */
            // Check perms. Add toggle status if perms look good.
            if (canReceiveBroadcast(player, EventData.Forfeits.SHINY))
            {
                flags.add("showShinyForfeit");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showShinyForfeit"))
                    messages.add(getTranslation("toggle.shiny.on") + separator);
                else
                    messages.add(getTranslation("toggle.shiny.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Forfeits.LEGENDARY))
            {
                flags.add("showLegendaryForfeit");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showLegendaryForfeit"))
                    messages.add(getTranslation("toggle.legendary.on") + separator);
                else
                    messages.add(getTranslation("toggle.legendary.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Forfeits.ULTRA_BEAST))
            {
                flags.add("showUltraBeastForfeit");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showUltraBeastForfeit"))
                    messages.add(getTranslation("toggle.ultra_beast.on") + separator);
                else
                    messages.add(getTranslation("toggle.ultra_beast.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Forfeits.BOSS))
            {
                flags.add("showBossForfeit");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showBossForfeit"))
                    messages.add(getTranslation("toggle.boss.on") + separator);
                else
                    messages.add(getTranslation("toggle.boss.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Forfeits.TRAINER))
            {
                flags.add("showTrainerForfeit");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showTrainerForfeit"))
                    messages.add(getTranslation("toggle.trainer.on") + separator);
                else
                    messages.add(getTranslation("toggle.trainer.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Forfeits.BOSS_TRAINER))
            {
                flags.add("showBossTrainerForfeit");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showBossTrainerForfeit"))
                    messages.add(getTranslation("toggle.boss_trainer.on") + separator);
                else
                    messages.add(getTranslation("toggle.boss_trainer.off") + separator);
            }

            // If we have any toggles lined up, print and clear.
            if (!messages.isEmpty())
            {
                // Get and add the "forfeit toggles" header message.
                toggleMessageList.add(new TextComponentString(getTranslation("toggle.forfeit_toggles")));

                // Submit one or more clickable lines with up to 5 toggles each. Split where necessary.
                addClickableLine(messages, flags);

                // Clear the Lists so we can reuse them, if need be.
                messages.clear();
                flags.clear();
            }

            /*               *\
               SPAWN TOGGLES
            \*               */
            // Check perms. Add toggle status if perms look good.
            if (canReceiveBroadcast(player, EventData.Spawns.SHINY))
            {
                flags.add("showShinySpawn");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showShinySpawn"))
                    messages.add(getTranslation("toggle.shiny.on") + separator);
                else
                    messages.add(getTranslation("toggle.shiny.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Spawns.LEGENDARY))
            {
                flags.add("showLegendarySpawn");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showLegendarySpawn"))
                    messages.add(getTranslation("toggle.legendary.on") + separator);
                else
                    messages.add(getTranslation("toggle.legendary.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Spawns.ULTRA_BEAST))
            {
                flags.add("showUltraBeastSpawn");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showUltraBeastSpawn"))
                    messages.add(getTranslation("toggle.ultra_beast.on") + separator);
                else
                    messages.add(getTranslation("toggle.ultra_beast.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Spawns.WORMHOLE))
            {
                flags.add("showWormholeSpawn");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showWormholeSpawn"))
                    messages.add(getTranslation("toggle.wormhole.on") + separator);
                else
                    messages.add(getTranslation("toggle.wormhole.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Spawns.UNCOMMON_BOSS))
            {
                flags.add("showUncommonBossSpawn");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showUncommonBossSpawn"))
                    messages.add(getTranslation("toggle.uncommon_boss.on") + separator);
                else
                    messages.add(getTranslation("toggle.uncommon_boss.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Spawns.RARE_BOSS))
            {
                flags.add("showRareBossSpawn");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showRareBossSpawn"))
                    messages.add(getTranslation("toggle.rare_boss.on") + separator);
                else
                    messages.add(getTranslation("toggle.rare_boss.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Spawns.LEGENDARY_BOSS))
            {
                flags.add("showLegendaryBossSpawn");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showLegendaryBossSpawn"))
                    messages.add(getTranslation("toggle.legendary_boss.on") + separator);
                else
                    messages.add(getTranslation("toggle.legendary_boss.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Spawns.ULTIMATE_BOSS))
            {
                flags.add("showUltimateBossSpawn");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showUltimateBossSpawn"))
                    messages.add(getTranslation("toggle.ultimate_boss.on") + separator);
                else
                    messages.add(getTranslation("toggle.ultimate_boss.off") + separator);
            }

            // If we have any toggles lined up, print and clear.
            if (!messages.isEmpty())
            {
                // Get and add the "spawn toggles" header message.
                toggleMessageList.add(new TextComponentString(getTranslation("toggle.spawning_toggles")));

                // Submit one or more clickable lines with up to 5 toggles each. Split where necessary.
                addClickableLine(messages, flags);

                // Clear the Lists so we can reuse them, if need be.
                messages.clear();
                flags.clear();
            }

            /*               *\
               SUMMON TOGGLES
            \*
            // Check perms. Add toggle status if perms look good.
            if (showBirdTrioSummons && player.hasPermission("pixelmonbroadcasts.notify.spawn.shiny"))
            {
                flags.add("showBirdTrioSummon");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showBirdTrioSummon"))
                    messages.add(getTranslation("toggle.bird_trio.on") + separator);
                else
                    messages.add(getTranslation("toggle.bird_trio.off") + separator);
            }

            // If we have any toggles lined up, print and clear.
            if (!messages.isEmpty())
            {
                // Get and add the "summon toggles" header message.
                toggleMessageList.add(new TextComponentString(getTranslation("toggle.summon_toggles")));

                // Submit one or more clickable lines with up to 5 toggles each. Split where necessary.
                addClickableLine(messages, flags);

                // Clear the Lists so we can reuse them, if need be.
                messages.clear();
                flags.clear();
            }*/

            /*                 *\
               VICTORY TOGGLES
            \*                 */
            // Check perms. Add toggle status if perms look good.
            if (canReceiveBroadcast(player, EventData.Victories.SHINY))
            {
                flags.add("showShinyVictory");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showShinyVictory"))
                    messages.add(getTranslation("toggle.shiny.on") + separator);
                else
                    messages.add(getTranslation("toggle.shiny.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Victories.LEGENDARY))
            {
                flags.add("showLegendaryVictory");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showLegendaryVictory"))
                    messages.add(getTranslation("toggle.legendary.on") + separator);
                else
                    messages.add(getTranslation("toggle.legendary.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Victories.ULTRA_BEAST))
            {
                flags.add("showUltraBeastVictory");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showUltraBeastVictory"))
                    messages.add(getTranslation("toggle.ultra_beast.on") + separator);
                else
                    messages.add(getTranslation("toggle.ultra_beast.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Victories.UNCOMMON_BOSS))
            {
                flags.add("showUncommonBossVictory");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showUncommonBossVictory"))
                    messages.add(getTranslation("toggle.uncommon_boss.on") + separator);
                else
                    messages.add(getTranslation("toggle.uncommon_boss.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Victories.RARE_BOSS))
            {
                flags.add("showRareBossVictory");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showRareBossVictory"))
                    messages.add(getTranslation("toggle.rare_boss.on") + separator);
                else
                    messages.add(getTranslation("toggle.rare_boss.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Victories.LEGENDARY_BOSS))
            {
                flags.add("showLegendaryBossVictory");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showLegendaryBossVictory"))
                    messages.add(getTranslation("toggle.legendary_boss.on") + separator);
                else
                    messages.add(getTranslation("toggle.legendary_boss.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Victories.ULTIMATE_BOSS))
            {
                flags.add("showUltimateBossVictory");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showUltimateBossVictory"))
                    messages.add(getTranslation("toggle.ultimate_boss.on") + separator);
                else
                    messages.add(getTranslation("toggle.ultimate_boss.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Victories.TRAINER))
            {
                flags.add("showTrainerVictory");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showTrainerVictory"))
                    messages.add(getTranslation("toggle.trainer.on") + separator);
                else
                    messages.add(getTranslation("toggle.trainer.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Victories.BOSS_TRAINER))
            {
                flags.add("showBossTrainerVictory");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showBossTrainerVictory"))
                    messages.add(getTranslation("toggle.boss_trainer.on") + separator);
                else
                    messages.add(getTranslation("toggle.boss_trainer.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Victories.PVP))
            {
                flags.add("showPVPVictory");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showPVPVictory"))
                    messages.add(getTranslation("toggle.pvp.on") + separator);
                else
                    messages.add(getTranslation("toggle.pvp.off") + separator);
            }

            // If we have any toggles lined up, print and clear.
            if (!messages.isEmpty())
            {
                // Get and add the "victory toggles" header message.
                toggleMessageList.add(new TextComponentString(getTranslation("toggle.victory_toggles")));

                // Submit one or more clickable lines with up to 5 toggles each. Split where necessary.
                addClickableLine(messages, flags);

                // Clear the Lists so we can reuse them, if need be.
                messages.clear();
                flags.clear();
            }

            /*              *\
               DRAW TOGGLES
            \*              */
            // Check perms. Add toggle status if perms look good.
            if (canReceiveBroadcast(player, EventData.Draws.PVP))
            {
                flags.add("showPVPDraw");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showPVPDraw"))
                    messages.add(getTranslation("toggle.pvp.on") + separator);
                else
                    messages.add(getTranslation("toggle.pvp.off") + separator);
            }

            // If we have any toggles lined up, print. No need to clear here, GC should handle it.
            if (!messages.isEmpty())
            {
                // Get and add the "draw toggles" header message.
                toggleMessageList.add(new TextComponentString(getTranslation("toggle.draw_toggles")));

                // Submit one or more clickable lines with up to 5 toggles each. Split where necessary.
                addClickableLine(messages, flags);

                // Clear the Lists so we can reuse them, if need be.
                messages.clear();
                flags.clear();
            }

            /*               *\
               HATCH TOGGLES
            \*               */
            // Check perms. Add toggle status if perms look good.
            if (canReceiveBroadcast(player, EventData.Hatches.NORMAL))
            {
                flags.add("showNormalHatch");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showNormalHatch"))
                    messages.add(getTranslation("toggle.normal.on") + separator);
                else
                    messages.add(getTranslation("toggle.normal.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Hatches.SHINY))
            {
                flags.add("showShinyHatch");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showShinyHatch"))
                    messages.add(getTranslation("toggle.shiny.on") + separator);
                else
                    messages.add(getTranslation("toggle.shiny.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Hatches.LEGENDARY))
            {
                flags.add("showLegendaryHatch");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showLegendaryHatch"))
                    messages.add(getTranslation("toggle.legendary.on") + separator);
                else
                    messages.add(getTranslation("toggle.legendary.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Hatches.ULTRA_BEAST))
            {
                flags.add("showUltraBeastHatch");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showUltraBeastHatch"))
                    messages.add(getTranslation("toggle.ultra_beast.on") + separator);
                else
                    messages.add(getTranslation("toggle.ultra_beast.off") + separator);
            }

            // If we have any toggles lined up, print and clear.
            if (!messages.isEmpty())
            {
                // Get and add the "victory toggles" header message.
                toggleMessageList.add(new TextComponentString(getTranslation("toggle.hatch_toggles")));

                // Submit one or more clickable lines with up to 5 toggles each. Split where necessary.
                addClickableLine(messages, flags);

                // Clear the Lists so we can reuse them, if need be.
                messages.clear();
                flags.clear();
            }

            /*                       *\
               MISCELLANEOUS TOGGLES
            \*                       */
            // Check perms. Add toggle status if perms look good.
            if (canReceiveBroadcast(player, EventData.Others.EVOLVE))
            {
                flags.add("showEvolve");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showEvolve"))
                    messages.add(getTranslation("toggle.evolve.on") + separator);
                else
                    messages.add(getTranslation("toggle.evolve.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Others.FAINT))
            {
                flags.add("showFaint");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showFaint"))
                    messages.add(getTranslation("toggle.faint.on") + separator);
                else
                    messages.add(getTranslation("toggle.faint.off") + separator);
            }
            if (canReceiveBroadcast(player, EventData.Others.TRADE))
            {
                flags.add("showTrade");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showTrade"))
                    messages.add(getTranslation("toggle.trade.on") + separator);
                else
                    messages.add(getTranslation("toggle.trade.off") + separator);
            }

            // If we have any toggles lined up, print. No need to clear here, GC should handle it.
            if (!messages.isEmpty())
            {
                // Get and add the "other toggles" header message.
                toggleMessageList.add(new TextComponentString(getTranslation("toggle.other_toggles")));

                // Submit one or more clickable lines with up to 5 toggles each. Split where necessary.
                addClickableLine(messages, flags);
            }

            /*                *\
               END OF TOGGLES
            \*                */
            if (toggleMessageList.isEmpty())
            {
                // Show a clean error since we have no allowed toggles.
                sendTranslation(player, "universal.no_permissions");
            }
            else
            {
                // Add a header.
                sendTranslation(player, "toggle.header");

                // Send all toggle messages.
                for (TextComponentString toggleMessage : toggleMessageList)
                    player.sendMessage(toggleMessage);

                // Add a footer.
                sendTranslation(player, "universal.footer");

                // Clear the list.
                toggleMessageList = new ArrayList<>();
            }
        }
        else
            logger.error("This command can only be run by players.");
    }

    // Takes two matched Lists, combines their entries and sends them off to the toggle List, ready to print.
    private void addClickableLine(List<String> messages, List<String> flags)
    {
        // What's the size of our messages List? Always matched with the flags List.
        int listSize = messages.size();

        // Get the last entry in the messages array and shank the trailing comma and space.
        String lastEntry = messages.get(listSize - 1);
        lastEntry = lastEntry.substring(0, lastEntry.length() - 2);
        messages.set(listSize - 1, lastEntry);

        // Get a clickable line with all the toggles that we can squeeze onto it. Split lines at >5 toggles.
        if (listSize > 5)
        {
            // Sublisting is stupid. Lower bound is inclusive, upper bound exclusive. 0-5 = 0, 1, 2, 3, 4. Angery. 3 hours!!
            toggleMessageList.add(createClickablePair(messages.subList(0, 5), flags.subList(0, 5)));
            toggleMessageList.add(createClickablePair(messages.subList(5, listSize), flags.subList(5, listSize)));
        }
        else
            toggleMessageList.add(createClickablePair(messages, flags));
    }

    // Takes a list of messages and assigns a matching toggle from a list of flags. Allows people to toggle by clicking!
    private TextComponentString createClickablePair(List<String> messages, List<String> flags)
    {
        // Set up temporary variables for putting stuff we're processing into. Append as we go.
        TextComponentString returnText = new TextComponentString(getTranslation("toggle.line_start"));
        ITextComponent messageComponent;

        // Set up a basic Text with our line starter. Add the rest of the line's contents as we go.

        // Grab the size of one of our Lists, as they should be matched. Add clickable elements as we go.
        for (int i = 0; i < messages.size(); i++)
        {
            // Get the message.
            messageComponent = new TextComponentString(messages.get(i));

            // Add our click action.
            messageComponent.getStyle().setClickEvent(
                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pixelmonbroadcasts toggle " + flags.get(i)));

            // Iteratively build up our returnable text String.
            returnText.appendSibling(messageComponent);
        }

        // Return the built text String.
        return returnText;
    }

    // Toggle a message-showing flag if it exists already, or create one if it does not.
    // TODO: To avoid bloat, don't save "true" flags if we ever get to a point where old data can be invalidated. (1.15+?)
    private void toggleFlag(ICommandSender sender, String flag)
    {
        // Get a player entity.
        EntityPlayer player = (EntityPlayer) sender;

        // If the NBT "folder" we use does not exist, create it.
        if (player.getEntityData().getCompoundTag("pbToggles").hasNoTags())
            player.getEntityData().setTag("pbToggles", new NBTTagCompound());

        // Does the flag key not exist yet? Do this.
        if (!player.getEntityData().getCompoundTag("pbToggles").hasKey(flag))
        {
            // Set the new flag to "false", since showing messages is on by default. Return that.
            player.getEntityData().getCompoundTag("pbToggles").setBoolean(flag, false);
        }
        else
        {
            // Get the current status of the flag we're toggling.
            final boolean flagStatus = player.getEntityData().getCompoundTag("pbToggles").getBoolean(flag);

            // Set the opposite value.
            player.getEntityData().getCompoundTag("pbToggles").setBoolean(flag, !flagStatus);
        }
    }
}
