// Allows people to toggle individual event notifications through a fancy clickable menu.
package rs.expand.pixelmonbroadcasts.commands;

// Remote imports.
import net.minecraft.entity.player.EntityPlayerMP;
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

// FIXME: Fix the blank line that shows up for players with only the command perm, but no notifier perms.
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

            // Add a header from our language file.
            sendTranslation(src, "universal.marginals.header");

            // Do we have an argument in the first slot?
            if (args.<String>getOne("setting").isPresent())
            {
                // We have an argument, extract it.
                final String input = args.<String>getOne("setting").get();

                // See if the argument is a valid flag, either from a remote caller or from sendClickableLine.
                switch (input)
                {
                    case "showLegendarySpawn": case "showLegendaryCatch": case "showLegendaryDefeat":
                    case "showShinySpawn": case "showShinyCatch": case "showShinyDefeat":
                    case "showShinyLegendarySpawn": case "showShinyLegendaryCatch": case "showShinyLegendaryDefeat":
                    case "showBossSpawn": case "showBossDefeat":
                    case "showHatch": case "showShinyHatch":
                    case "showTrade":
                    {
                        // Got a valid flag. Toggle it.
                        toggleFlag(src, input);
                        break;
                    }
                    default:
                    {
                        // Malformed input provided. A bit odd, but let's just ignore it and show the syntax.
                        sendTranslation(src, "toggle.messages.info");
                        break;
                    }
                }
            }
            else
            {
                // No input was provided, as intended. Show the syntax.
                sendTranslation(src, "toggle.messages.info");
            }

            // Add a space below whatever message we were displaying, and above the toggles.
            src.sendMessage(Text.of(""));

            // Get a player entity.
            EntityPlayerMP player = (EntityPlayerMP) src;

            // These are linked, and used to show available toggles. If one has two entries, the other gets two, too!
            final List<String> messages = new ArrayList<>();
            final List<String> flags = new ArrayList<>();

            /*               *\
               CATCH TOGGLES
            \*               */
            // Check perms. Add toggle status if perms look good.
            if (showShinyCatches && src.hasPermission("pixelmonbroadcasts.notify.shinycatch"))
            {
                flags.add("showShinyCatch");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showShinyCatch"))
                    messages.add(getTranslation("toggle.text.shiny.on") + "§r, ");
                else
                    messages.add(getTranslation("toggle.text.shiny.off") + "§r, ");
            }
            if (showShinyLegendaryCatches && src.hasPermission("pixelmonbroadcasts.notify.shinylegendarycatch"))
            {
                flags.add("showShinyLegendaryCatch");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showShinyLegendaryCatch"))
                    messages.add(getTranslation("toggle.text.shiny_legendary.on") + "§r, ");
                else
                    messages.add(getTranslation("toggle.text.shiny_legendary.off") + "§r, ");
            }
            if (showLegendaryCatches && src.hasPermission("pixelmonbroadcasts.notify.legendarycatch"))
            {
                flags.add("showLegendaryCatch");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showLegendaryCatch"))
                    messages.add(getTranslation("toggle.text.legendary.on") + "§r, ");
                else
                    messages.add(getTranslation("toggle.text.legendary.off") + "§r, ");
            }

            // If we have any toggles lined up, print and clear.
            if (!messages.isEmpty())
            {
                // Send the "catch toggles" header message.
                sendTranslation(src, "toggle.messages.catch_toggles");

                // Send off our list to a special method made for handling them.
                sendClickableLine(src, messages, flags);

                // Clear the Lists so we can reuse them, if need be.
                messages.clear();
                flags.clear();
            }

            /*                *\
               DEFEAT TOGGLES
            \*                */
            // Check perms. Add toggle status if perms look good.
            if (showShinyDefeats && src.hasPermission("pixelmonbroadcasts.notify.shinydefeat"))
            {
                flags.add("showShinyDefeat");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showShinyDefeat"))
                    messages.add(getTranslation("toggle.text.shiny.on") + "§r, ");
                else
                    messages.add(getTranslation("toggle.text.shiny.off") + "§r, ");
            }
            if (showShinyLegendaryDefeats && src.hasPermission("pixelmonbroadcasts.notify.shinylegendarydefeat"))
            {
                flags.add("showShinyLegendaryDefeat");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showShinyLegendaryDefeat"))
                    messages.add(getTranslation("toggle.text.shiny_legendary.on") + "§r, ");
                else
                    messages.add(getTranslation("toggle.text.shiny_legendary.off") + "§r, ");
            }
            if (showLegendaryDefeats && src.hasPermission("pixelmonbroadcasts.notify.legendarydefeat"))
            {
                flags.add("showLegendaryDefeat");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showLegendaryDefeat"))
                    messages.add(getTranslation("toggle.text.legendary.on") + "§r, ");
                else
                    messages.add(getTranslation("toggle.text.legendary.off") + "§r, ");
            }
            if (showBossDefeats && src.hasPermission("pixelmonbroadcasts.notify.bossdefeat"))
            {
                flags.add("showBossDefeat");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showBossDefeat"))
                    messages.add(getTranslation("toggle.text.boss.on") + "§r, ");
                else
                    messages.add(getTranslation("toggle.text.boss.off") + "§r, ");
            }

            // If we have any toggles lined up, print and clear.
            if (!messages.isEmpty())
            {
                // Send the "defeat toggles" header message.
                sendTranslation(src, "toggle.messages.defeat_toggles");

                // Send off our list to a special method made for handling them.
                sendClickableLine(src, messages, flags);

                // Clear the Lists so we can reuse them, if need be.
                messages.clear();
                flags.clear();
            }

            /*               *\
               SPAWN TOGGLES
            \*               */
            // Check perms. Add toggle status if perms look good.
            if (showShinySpawns && src.hasPermission("pixelmonbroadcasts.notify.shinyspawn"))
            {
                flags.add("showShinySpawn");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showShinySpawn"))
                    messages.add(getTranslation("toggle.text.shiny.on") + "§r, ");
                else
                    messages.add(getTranslation("toggle.text.shiny.off") + "§r, ");
            }
            if (showShinyLegendarySpawns && src.hasPermission("pixelmonbroadcasts.notify.shinylegendaryspawn"))
            {
                flags.add("showShinyLegendarySpawn");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showShinyLegendarySpawn"))
                    messages.add(getTranslation("toggle.text.shiny_legendary.on") + "§r, ");
                else
                    messages.add(getTranslation("toggle.text.shiny_legendary.off") + "§r, ");
            }
            if (showLegendarySpawns && src.hasPermission("pixelmonbroadcasts.notify.legendaryspawn"))
            {
                flags.add("showLegendarySpawn");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showLegendarySpawn"))
                    messages.add(getTranslation("toggle.text.legendary.on") + "§r, ");
                else
                    messages.add(getTranslation("toggle.text.legendary.off") + "§r, ");
            }
            if (showBossSpawns && src.hasPermission("pixelmonbroadcasts.notify.bossspawn"))
            {
                flags.add("showBossSpawn");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showBossSpawn"))
                    messages.add(getTranslation("toggle.text.boss.on") + "§r, ");
                else
                    messages.add(getTranslation("toggle.text.boss.off") + "§r, ");
            }

            // If we have any toggles lined up, print and clear.
            if (!messages.isEmpty())
            {
                // Send the "spawn toggles" header message.
                sendTranslation(src, "toggle.messages.spawning_toggles");

                // Send off our list to a special method made for handling them.
                sendClickableLine(src, messages, flags);

                // Clear the Lists so we can reuse them, if need be.
                messages.clear();
                flags.clear();
            }

            /*                       *\
               MISCELLANEOUS TOGGLES
            \*                       */
            // Check perms. Add toggle status if perms look good.
            if (showHatches && src.hasPermission("pixelmonbroadcasts.notify.hatch"))
            {
                flags.add("showHatch");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showHatch"))
                    messages.add(getTranslation("toggle.text.hatch.on") + "§r, ");
                else
                    messages.add(getTranslation("toggle.text.hatch.off") + "§r, ");
            }
            if (showShinyHatches && src.hasPermission("pixelmonbroadcasts.notify.shinyhatch"))
            {
                flags.add("showShinyHatch");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showShinyHatch"))
                    messages.add(getTranslation("toggle.text.shiny_hatch.on") + "§r, ");
                else
                    messages.add(getTranslation("toggle.text.shiny_hatch.off") + "§r, ");
            }
            if (showTrades && src.hasPermission("pixelmonbroadcasts.notify.trade"))
            {
                flags.add("showTrade");

                // Only returns "false" if explicitly toggled off by the user.
                if (checkToggleStatus(player, "showTrade"))
                    messages.add(getTranslation("toggle.text.trade.on") + "§r, ");
                else
                    messages.add(getTranslation("toggle.text.trade.off") + "§r, ");
            }

            // If we have any toggles lined up, print. No need to clear here, GC should handle it.
            if (!messages.isEmpty())
            {
                // Send the "miscellaneous toggles" header message.
                sendTranslation(src, "toggle.messages.other_toggles");

                // Send off our list to a special method made for handling them.
                sendClickableLine(src, messages, flags);
            }

            // Cap things off with a nice lang file footer.
            sendTranslation(src, "universal.marginals.footer");
        }
        else
            printBasicError("This command can only be run by players.");

        return CommandResult.success();
    }

    // Takes two matched Lists, and combines their entries.
    private void sendClickableLine(CommandSource src, List<String> messages, List<String> flags)
    {
        // Get the last entry in the messages array, shank the last trailing comma and space, and then apply a period.
        String lastEntry = messages.get(messages.size() - 1);
        lastEntry = lastEntry.substring(0, lastEntry.length() - 2) + '.';
        messages.set(messages.size() - 1, lastEntry);

        // Start setting up pairs of texts and their matching actions. The first pair is always there.
        final Text actionPair = Text.builder(messages.get(0))
                .onClick(TextActions.runCommand("/pixelmonbroadcasts toggle " + flags.get(0)))
                .build();

        // We might have more pairs, so set them up now.
        Text actionPair2 = Text.EMPTY, actionPair3 = Text.EMPTY, actionPair4 = Text.EMPTY;

        // Since both Lists should form a pair of matched contents, pick one and get its size.
        // Format another pair for every extra entry we have.
        if (messages.size() > 1)
        {
            actionPair2 = Text.builder(messages.get(1))
                    .onClick(TextActions.runCommand("/pixelmonbroadcasts toggle " + flags.get(1)))
                    .build();
        }
        if (messages.size() > 2)
        {
            actionPair3 = Text.builder(messages.get(2))
                    .onClick(TextActions.runCommand("/pixelmonbroadcasts toggle " + flags.get(2)))
                    .build();
        }
        if (messages.size() > 3)
        {
            actionPair4 = Text.builder(messages.get(3))
                    .onClick(TextActions.runCommand("/pixelmonbroadcasts toggle " + flags.get(3)))
                    .build();
        }

        // Send the pairs.
        src.sendMessage(Text.of("➡ ", actionPair, actionPair2, actionPair3, actionPair4));
    }

    // Toggle a message-showing flag if it exists already, or create one if it does not.
    private void toggleFlag(CommandSource src, String flag)
    {
        // Get a player entity.
        EntityPlayerMP player = (EntityPlayerMP) src;

        // If the NBT "folder" we use does not exist, create it.
        if (player.getEntityData().getCompoundTag("pbToggles").hasNoTags())
            player.getEntityData().setTag("pbToggles", new NBTTagCompound());

        // Does the flag key not exist yet? Do this.
        if (!player.getEntityData().getCompoundTag("pbToggles").hasKey(flag))
        {
            // Set the new flag to "false", since showing messages is on by default. Return that.
            player.getEntityData().getCompoundTag("pbToggles").setBoolean(flag, false);
            sendTranslation(src, "toggle.messages.disable");
        }
        else
        {
            // Get the current status of the flag we're toggling.
            final boolean flagStatus = player.getEntityData().getCompoundTag("pbToggles").getBoolean(flag);

            // Set the opposite value.
            player.getEntityData().getCompoundTag("pbToggles").setBoolean(flag, !flagStatus);

            // Inform the player of what we've done.
            if (flagStatus)
                sendTranslation(src, "toggle.messages.disable");
            else
                sendTranslation(src, "toggle.messages.enable");
        }
    }
}