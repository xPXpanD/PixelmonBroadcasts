// The one and only. Accept no imitations.
package rs.expand.pixelmonbroadcasts.commands;

// Remote imports.
import org.spongepowered.api.block.tileentity.CommandBlock;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;

// Local imports.
import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.*;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.*;

public class BaseCommand implements CommandExecutor
{
    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        // Are we being called by a player, or something else capable of receiving multiple messages? Beware: negation.
        if (!(src instanceof CommandBlock))
        {
            // Add a header from our language file.
            sendTranslation(src, "universal.marginals.header");

            // Show an error if the alias isn't set right. Continue after.
            if (commandAlias == null)
            {
                printBasicError("Could not read config node \"§4commandAlias§c\" while executing hub command.");
                printBasicError("We'll continue with the command, but aliases will break. Check your config.");
            }

            // Set up a flag to see where we're running from. Gets set to true if we weren't called by a player.
            final boolean calledRemotely = !(src instanceof Player);

            // Also set up a flag for seeing whether the user had any permissions.
            boolean hasPermissions = false;

            // Start permission checks.
            if (src.hasPermission("pixelmonbroadcasts.command.toggle"))
            {
                hasPermissions = true;

                final String finalMessage = getTranslation("hub.messages.toggle_syntax", commandAlias);

                LiteralText clickableLine = Text.builder(finalMessage)
                        .onClick(TextActions.runCommand("/pixelmonbroadcasts toggle"))
                        .build();
                
                src.sendMessage(clickableLine);

                if (src instanceof Player)
                    src.sendMessage(Text.of(statLineStart + getTranslation("hub.messages.toggle_info")));
                else
                {
                    // Message locked in, as it's not visible in-game. Keeps the lang workload down, with minimal loss.
                    src.sendMessage(Text.of(
                            "➡ §eAllows in-game players to toggle event messages by clicking them."));
                }
            }
            if (src.hasPermission("pixelmonbroadcasts.command.staff.reload"))
            {
                hasPermissions = true;

                final String finalMessage = getTranslation("hub.messages.reload_syntax", commandAlias);

                LiteralText clickableLine = Text.builder(finalMessage)
                        .onClick(TextActions.runCommand("/pixelmonbroadcasts reload"))
                        .build();

                src.sendMessage(clickableLine);
                    src.sendMessage(Text.of(statLineStart + getTranslation("hub.messages.reload_info")));
            }

            if (!hasPermissions)
            {
                sendTranslation(src, "hub.errors.no_permissions");
                sendTranslation(src, "hub.errors.contact_staff");
            }
            else if (calledRemotely)
            {
                // Messages locked in, as they're not visible in-game. Keeps the lang workload down, with minimal loss.
                src.sendMessage(Text.of(""));
                src.sendMessage(Text.of("§6Please note: §eThe \"toggle\" sub-command will only work when used in-game."));
            }

            // Cap things off with a nice lang file footer.
            sendTranslation(src, "universal.marginals.footer");
        }
        else
            src.sendMessage(Text.of("§cThis command cannot run from command blocks."));

        return CommandResult.success();
    }
}
