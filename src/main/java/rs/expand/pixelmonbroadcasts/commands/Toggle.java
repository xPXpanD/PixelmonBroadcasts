// Allows people to toggle individual event notifications through a fancy clickable menu.
package rs.expand.pixelmonbroadcasts.commands;

// Remote imports.
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;

import java.util.ArrayList;
import java.util.List;

// Local imports.
import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.*;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.*;

// TODO: Maybe get paginated lists working. Tried it before, but it seems to cut things off randomly...
public class Toggle implements CommandExecutor
{
    // The command executor.
    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        // Were we called by a player? Let's not try toggling flags on things that can't have flags.
        if (src instanceof Player)
        {
            if (commandAlias == null)
            {
                printBasicError("Could not read config node \"§4commandAlias§c\" while executing toggle command.");
                printBasicError("We'll continue with the command, but aliases will break. Check your config.");
            }

            // Do we have an argument in the first slot? If valid, use it to toggle the matching setting and then show.
            if (args.<String>getOne("setting").isPresent())
            {
                // We have an argument, extract it.
                final String input = args.<String>getOne("setting").get();

                // See if the argument is a valid flag, either from a remote caller or from getClickableLine.
                switch (input)
                {
                    case "showNormalCatch": case "showNormalBlackout":
                    case "showLegendarySpawn": case "showLegendaryChallenge": case "showLegendaryCatch":
                    case "showLegendaryVictory": case "showLegendaryBlackout": case "showLegendaryForfeit":
                    case "showShinySpawn": case "showShinyChallenge": case "showShinyCatch":
                    case "showShinyVictory": case "showShinyBlackout": case "showShinyForfeit":
                    case "showShinyLegendarySpawn": case "showShinyLegendaryChallenge": case "showShinyLegendaryCatch":
                    case "showShinyLegendaryVictory": case "showShinyLegendaryBlackout": case "showShinyLegendaryForfeit":
                    case "showBossSpawn": case "showBossChallenge":
                    case "showBossVictory": case "showBossBlackout": case "showBossForfeit":
                    case "showTrainerChallenge": case "showTrainerVictory":
                    case "showTrainerBlackout": case "showTrainerForfeit":
                    case "showBossTrainerChallenge": case "showBossTrainerVictory":
                    case "showBossTrainerBlackout": case "showBossTrainerForfeit":
                    case "showPVPChallenge": case "showPVPVictory": case "showPVPDraw":
                    case "showNormalHatch": case "showShinyHatch":
                    case "showTrade":
                    //case "showBirdTrioSummon":
                    {
                        // Got a valid flag. Toggle it.
                        toggleFlag(src, input);
                        break;
                    }
                }
            }

            // Get a player entity.
            EntityPlayer player = (EntityPlayer) src;

            // These are linked, and used to show available toggles. If one has two entries, the other gets two, too!
            final List<String> messages = new ArrayList<>();
            final List<String> flags = new ArrayList<>();

            // Create a List of Texts that we can dump messages that are ready to be printed into.
            final List<Text> toggleMessageList = new ArrayList<>();

            // Get the separator message so we don't have to read it dozens of times.
            final String separator = getTranslation("hover.status.separator");

            // Add a header so we can start counting and printing.
            sendTranslation(src, "toggle.header");

            /*                  *\
               BLACKOUT TOGGLES
            \*                  */
            // Check perms. Add toggle status if perms look good.
            if (showNormalBlackouts && src.hasPermission("pixelmonbroadcasts.notify.blackout.normal"))
            {
                flags.add("showNormalBlackout");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showNormalBlackout"))
                    messages.add(getTranslation("toggle.normal.on") + separator);
                else
                    messages.add(getTranslation("toggle.normal.off") + separator);
            }
            if (showShinyBlackouts && src.hasPermission("pixelmonbroadcasts.notify.blackout.shiny"))
            {
                flags.add("showShinyBlackout");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showShinyBlackout"))
                    messages.add(getTranslation("toggle.shiny.on") + separator);
                else
                    messages.add(getTranslation("toggle.shiny.off") + separator);
            }
            if (showLegendaryBlackouts && src.hasPermission("pixelmonbroadcasts.notify.blackout.legendary"))
            {
                flags.add("showLegendaryBlackout");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showLegendaryBlackout"))
                    messages.add(getTranslation("toggle.legendary.on") + separator);
                else
                    messages.add(getTranslation("toggle.legendary.off") + separator);
            }
            if (showShinyLegendaryBlackouts && src.hasPermission("pixelmonbroadcasts.notify.blackout.shinylegendary"))
            {
                flags.add("showShinyLegendaryBlackout");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showShinyLegendaryBlackout"))
                    messages.add(getTranslation("toggle.shiny_legendary.on") + separator);
                else
                    messages.add(getTranslation("toggle.shiny_legendary.off") + separator);
            }
            if (showBossBlackouts && src.hasPermission("pixelmonbroadcasts.notify.blackout.boss"))
            {
                flags.add("showBossBlackout");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showBossBlackout"))
                    messages.add(getTranslation("toggle.boss.on") + separator);
                else
                    messages.add(getTranslation("toggle.boss.off") + separator);
            }
            if (showTrainerBlackouts && src.hasPermission("pixelmonbroadcasts.notify.blackout.trainer"))
            {
                flags.add("showTrainerBlackout");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showTrainerBlackout"))
                    messages.add(getTranslation("toggle.trainer.on") + separator);
                else
                    messages.add(getTranslation("toggle.trainer.off") + separator);
            }
            if (showBossTrainerBlackouts && src.hasPermission("pixelmonbroadcasts.notify.blackout.bosstrainer"))
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
                toggleMessageList.add(Text.of(getTranslation("toggle.blackout_toggles")));

                // Get a clickable line with all the toggles that we can squeeze onto it.
                toggleMessageList.add(getClickableLine(messages, flags));

                // Clear the Lists so we can reuse them, if need be.
                messages.clear();
                flags.clear();
            }

            /*               *\
               CATCH TOGGLES
            \*               */
            // Check perms. Add toggle status if perms look good.
            if (showNormalCatches && src.hasPermission("pixelmonbroadcasts.notify.catch.normal"))
            {
                flags.add("showNormalCatch");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showNormalCatch"))
                    messages.add(getTranslation("toggle.normal.on") + separator);
                else
                    messages.add(getTranslation("toggle.normal.off") + separator);
            }
            if (showShinyCatches && src.hasPermission("pixelmonbroadcasts.notify.catch.shiny"))
            {
                flags.add("showShinyCatch");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showShinyCatch"))
                    messages.add(getTranslation("toggle.shiny.on") + separator);
                else
                    messages.add(getTranslation("toggle.shiny.off") + separator);
            }
            if (showLegendaryCatches && src.hasPermission("pixelmonbroadcasts.notify.catch.legendary"))
            {
                flags.add("showLegendaryCatch");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showLegendaryCatch"))
                    messages.add(getTranslation("toggle.legendary.on") + separator);
                else
                    messages.add(getTranslation("toggle.legendary.off") + separator);
            }
            if (showShinyLegendaryCatches && src.hasPermission("pixelmonbroadcasts.notify.catch.shinylegendary"))
            {
                flags.add("showShinyLegendaryCatch");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showShinyLegendaryCatch"))
                    messages.add(getTranslation("toggle.shiny_legendary.on") + separator);
                else
                    messages.add(getTranslation("toggle.shiny_legendary.off") + separator);
            }

            // If we have any toggles lined up, print and clear.
            if (!messages.isEmpty())
            {
                // Get and add the "catch toggles" header message.
                toggleMessageList.add(Text.of(getTranslation("toggle.catch_toggles")));

                // Get a clickable line with all the toggles that we can squeeze onto it.
                toggleMessageList.add(getClickableLine(messages, flags));

                // Clear the Lists so we can reuse them, if need be.
                messages.clear();
                flags.clear();
            }

            /*                   *\
               CHALLENGE TOGGLES
            \*                   */
            // Check perms. Add toggle status if perms look good.
            if (showShinyChallenges && src.hasPermission("pixelmonbroadcasts.notify.challenge.shiny"))
            {
                flags.add("showShinyChallenge");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showShinyChallenge"))
                    messages.add(getTranslation("toggle.shiny.on") + separator);
                else
                    messages.add(getTranslation("toggle.shiny.off") + separator);
            }
            if (showLegendaryChallenges && src.hasPermission("pixelmonbroadcasts.notify.challenge.legendary"))
            {
                flags.add("showLegendaryChallenge");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showLegendaryChallenge"))
                    messages.add(getTranslation("toggle.legendary.on") + separator);
                else
                    messages.add(getTranslation("toggle.legendary.off") + separator);
            }
            if (showShinyLegendaryChallenges && src.hasPermission("pixelmonbroadcasts.notify.challenge.shinylegendary"))
            {
                flags.add("showShinyLegendaryChallenge");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showShinyLegendaryChallenge"))
                    messages.add(getTranslation("toggle.shiny_legendary.on") + separator);
                else
                    messages.add(getTranslation("toggle.shiny_legendary.off") + separator);
            }
            if (showBossChallenges && src.hasPermission("pixelmonbroadcasts.notify.challenge.boss"))
            {
                flags.add("showBossChallenge");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showBossChallenge"))
                    messages.add(getTranslation("toggle.boss.on") + separator);
                else
                    messages.add(getTranslation("toggle.boss.off") + separator);
            }
            if (showTrainerChallenges && src.hasPermission("pixelmonbroadcasts.notify.challenge.trainer"))
            {
                flags.add("showTrainerChallenge");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showTrainerChallenge"))
                    messages.add(getTranslation("toggle.trainer.on") + separator);
                else
                    messages.add(getTranslation("toggle.trainer.off") + separator);
            }
            if (showBossTrainerChallenges && src.hasPermission("pixelmonbroadcasts.notify.challenge.bosstrainer"))
            {
                flags.add("showBossTrainerChallenge");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showBossTrainerChallenge"))
                    messages.add(getTranslation("toggle.boss_trainer.on") + separator);
                else
                    messages.add(getTranslation("toggle.boss_trainer.off") + separator);
            }
            if (showPVPChallenges && src.hasPermission("pixelmonbroadcasts.notify.challenge.pvp"))
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
                toggleMessageList.add(Text.of(getTranslation("toggle.challenge_toggles")));

                // Get a clickable line with all the toggles that we can squeeze onto it.
                toggleMessageList.add(getClickableLine(messages, flags));

                // Clear the Lists so we can reuse them, if need be.
                messages.clear();
                flags.clear();
            }

            /*              *\
               DRAW TOGGLES
            \*              */
            // Check perms. Add toggle status if perms look good.
            if (showPVPDraws && src.hasPermission("pixelmonbroadcasts.notify.draw.pvp"))
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
                toggleMessageList.add(Text.of(getTranslation("toggle.draw_toggles")));

                // Get a clickable line with all the toggles that we can squeeze onto it.
                toggleMessageList.add(getClickableLine(messages, flags));

                // Clear the Lists so we can reuse them, if need be.
                messages.clear();
                flags.clear();
            }

            /*                 *\
               FORFEIT TOGGLES
            \*                 */
            // Check perms. Add toggle status if perms look good.
            if (showShinyForfeits && src.hasPermission("pixelmonbroadcasts.notify.forfeit.shiny"))
            {
                flags.add("showShinyForfeit");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showShinyForfeit"))
                    messages.add(getTranslation("toggle.shiny.on") + separator);
                else
                    messages.add(getTranslation("toggle.shiny.off") + separator);
            }
            if (showLegendaryForfeits && src.hasPermission("pixelmonbroadcasts.notify.forfeit.legendary"))
            {
                flags.add("showLegendaryForfeit");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showLegendaryForfeit"))
                    messages.add(getTranslation("toggle.legendary.on") + separator);
                else
                    messages.add(getTranslation("toggle.legendary.off") + separator);
            }
            if (showShinyLegendaryForfeits && src.hasPermission("pixelmonbroadcasts.notify.forfeit.shinylegendary"))
            {
                flags.add("showShinyLegendaryForfeit");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showShinyLegendaryForfeit"))
                    messages.add(getTranslation("toggle.shiny_legendary.on") + separator);
                else
                    messages.add(getTranslation("toggle.shiny_legendary.off") + separator);
            }
            if (showBossForfeits && src.hasPermission("pixelmonbroadcasts.notify.forfeit.boss"))
            {
                flags.add("showBossForfeit");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showBossForfeit"))
                    messages.add(getTranslation("toggle.boss.on") + separator);
                else
                    messages.add(getTranslation("toggle.boss.off") + separator);
            }
            if (showTrainerForfeits && src.hasPermission("pixelmonbroadcasts.notify.forfeit.trainer"))
            {
                flags.add("showTrainerForfeit");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showTrainerForfeit"))
                    messages.add(getTranslation("toggle.trainer.on") + separator);
                else
                    messages.add(getTranslation("toggle.trainer.off") + separator);
            }
            if (showBossTrainerForfeits && src.hasPermission("pixelmonbroadcasts.notify.forfeit.bosstrainer"))
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
                toggleMessageList.add(Text.of(getTranslation("toggle.forfeit_toggles")));

                // Get a clickable line with all the toggles that we can squeeze onto it.
                toggleMessageList.add(getClickableLine(messages, flags));

                // Clear the Lists so we can reuse them, if need be.
                messages.clear();
                flags.clear();
            }

            /*               *\
               SPAWN TOGGLES
            \*               */
            // Check perms. Add toggle status if perms look good.
            if (showShinySpawns && src.hasPermission("pixelmonbroadcasts.notify.spawn.shiny"))
            {
                flags.add("showShinySpawn");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showShinySpawn"))
                    messages.add(getTranslation("toggle.shiny.on") + separator);
                else
                    messages.add(getTranslation("toggle.shiny.off") + separator);
            }
            if (showLegendarySpawns && src.hasPermission("pixelmonbroadcasts.notify.spawn.legendary"))
            {
                flags.add("showLegendarySpawn");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showLegendarySpawn"))
                    messages.add(getTranslation("toggle.legendary.on") + separator);
                else
                    messages.add(getTranslation("toggle.legendary.off") + separator);
            }
            if (showShinyLegendarySpawns && src.hasPermission("pixelmonbroadcasts.notify.spawn.shinylegendary"))
            {
                flags.add("showShinyLegendarySpawn");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showShinyLegendarySpawn"))
                    messages.add(getTranslation("toggle.shiny_legendary.on") + separator);
                else
                    messages.add(getTranslation("toggle.shiny_legendary.off") + separator);
            }
            if (showBossSpawns && src.hasPermission("pixelmonbroadcasts.notify.spawn.boss"))
            {
                flags.add("showBossSpawn");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showBossSpawn"))
                    messages.add(getTranslation("toggle.boss.on") + separator);
                else
                    messages.add(getTranslation("toggle.boss.off") + separator);
            }

            // If we have any toggles lined up, print and clear.
            if (!messages.isEmpty())
            {
                // Get and add the "spawn toggles" header message.
                toggleMessageList.add(Text.of(getTranslation("toggle.spawning_toggles")));

                // Get a clickable line with all the toggles that we can squeeze onto it.
                toggleMessageList.add(getClickableLine(messages, flags));

                // Clear the Lists so we can reuse them, if need be.
                messages.clear();
                flags.clear();
            }

            /*               *\
               SUMMON TOGGLES
            \*
            // Check perms. Add toggle status if perms look good.
            if (showBirdTrioSummons && src.hasPermission("pixelmonbroadcasts.notify.spawn.shiny"))
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
                toggleMessageList.add(Text.of(getTranslation("toggle.summon_toggles")));

                // Get a clickable line with all the toggles that we can squeeze onto it.
                toggleMessageList.add(getClickableLine(messages, flags));

                // Clear the Lists so we can reuse them, if need be.
                messages.clear();
                flags.clear();
            }*/

            /*                 *\
               VICTORY TOGGLES
            \*                 */
            // Check perms. Add toggle status if perms look good.
            if (showShinyVictories && src.hasPermission("pixelmonbroadcasts.notify.victory.shiny"))
            {
                flags.add("showShinyVictory");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showShinyVictory"))
                    messages.add(getTranslation("toggle.shiny.on") + separator);
                else
                    messages.add(getTranslation("toggle.shiny.off") + separator);
            }
            if (showLegendaryVictories && src.hasPermission("pixelmonbroadcasts.notify.victory.legendary"))
            {
                flags.add("showLegendaryVictory");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showLegendaryVictory"))
                    messages.add(getTranslation("toggle.legendary.on") + separator);
                else
                    messages.add(getTranslation("toggle.legendary.off") + separator);
            }
            if (showShinyLegendaryVictories && src.hasPermission("pixelmonbroadcasts.notify.victory.shinylegendary"))
            {
                flags.add("showShinyLegendaryVictory");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showShinyLegendaryVictory"))
                    messages.add(getTranslation("toggle.shiny_legendary.on") + separator);
                else
                    messages.add(getTranslation("toggle.shiny_legendary.off") + separator);
            }
            if (showBossVictories && src.hasPermission("pixelmonbroadcasts.notify.victory.boss"))
            {
                flags.add("showBossVictory");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showBossVictory"))
                    messages.add(getTranslation("toggle.boss.on") + separator);
                else
                    messages.add(getTranslation("toggle.boss.off") + separator);
            }
            if (showTrainerVictories && src.hasPermission("pixelmonbroadcasts.notify.victory.trainer"))
            {
                flags.add("showTrainerVictory");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showTrainerVictory"))
                    messages.add(getTranslation("toggle.trainer.on") + separator);
                else
                    messages.add(getTranslation("toggle.trainer.off") + separator);
            }
            if (showBossTrainerVictories && src.hasPermission("pixelmonbroadcasts.notify.victory.bosstrainer"))
            {
                flags.add("showBossTrainerVictory");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showBossTrainerVictory"))
                    messages.add(getTranslation("toggle.boss_trainer.on") + separator);
                else
                    messages.add(getTranslation("toggle.boss_trainer.off") + separator);
            }
            if (showPVPVictories && src.hasPermission("pixelmonbroadcasts.notify.victory.pvp"))
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
                toggleMessageList.add(Text.of(getTranslation("toggle.victory_toggles")));

                // Get a clickable line with all the toggles that we can squeeze onto it.
                toggleMessageList.add(getClickableLine(messages, flags));

                // Clear the Lists so we can reuse them, if need be.
                messages.clear();
                flags.clear();
            }

            /*                       *\
               MISCELLANEOUS TOGGLES
            \*                       */
            // Check perms. Add toggle status if perms look good.
            if (showNormalHatches && src.hasPermission("pixelmonbroadcasts.notify.hatch.normal"))
            {
                flags.add("showNormalHatch");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showNormalHatch"))
                    messages.add(getTranslation("toggle.normal_hatch.on") + separator);
                else
                    messages.add(getTranslation("toggle.normal_hatch.off") + separator);
            }
            if (showShinyHatches && src.hasPermission("pixelmonbroadcasts.notify.hatch.shiny"))
            {
                flags.add("showShinyHatch");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showShinyHatch"))
                    messages.add(getTranslation("toggle.shiny_hatch.on") + separator);
                else
                    messages.add(getTranslation("toggle.shiny_hatch.off") + separator);
            }
            if (showTrades && src.hasPermission("pixelmonbroadcasts.notify.trade"))
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
                toggleMessageList.add(Text.of(getTranslation("toggle.other_toggles")));

                // Get a clickable line with all the toggles that we can squeeze onto it.
                toggleMessageList.add(getClickableLine(messages, flags));
            }

            /*                *\
               END OF TOGGLES
            \*                */
            if (toggleMessageList.isEmpty())
            {
                // Show a clean error since we have no allowed toggles.
                toggleMessageList.add(Text.of("§cYou have no permissions for any broadcast toggles."));
                toggleMessageList.add(Text.of("§cPlease contact staff if you believe this to be in error."));
            }
            else
            {
                // Send all toggle messages.
                for (Text toggleMessage : toggleMessageList)
                    src.sendMessage(toggleMessage);
            }

            // Add a footer.
            sendTranslation(src, "universal.footer");
        }
        else
            printBasicError("This command can only be run by players.");

        return CommandResult.success();
    }

    // Takes two matched Lists, and combines their entries.
    private Text getClickableLine(List<String> messages, List<String> flags)
    {
        // Get the last entry in the messages array and shank the trailing comma and space.
        String lastEntry = messages.get(messages.size() - 1);
        lastEntry = lastEntry.substring(0, lastEntry.length() - 2);
        messages.set(messages.size() - 1, lastEntry);

        // Set up a basic Text that we'll be returning, after adding all passed hoverable options to it.
        Text returnText = Text.of(getTranslation("toggle.line_start"));

        // Set up a temporary Text for putting the message/flag pair that we're currently processing into.
        Text actionPair;

        // Grab the size of one of our Lists, as they should be matched. Iterate over it.
        for (int i = 0; i < messages.size(); i++)
        {
            // Set up a temporary pair.
            actionPair = Text.builder(messages.get(i))
                .onClick(TextActions.runCommand("/pixelmonbroadcasts toggle " + flags.get(i)))
                .build();

            // Add the pair to our returnable Text.
            returnText = returnText.toBuilder().append(actionPair).build();
        }

        // Return the finalized Text.
        return returnText;
    }

    // Toggle a message-showing flag if it exists already, or create one if it does not.
    private void toggleFlag(CommandSource src, String flag)
    {
        // Get a player entity.
        EntityPlayer player = (EntityPlayer) src;

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