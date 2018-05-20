// The one and only. Accept no imitations.
package rs.expand.pixelmonbroadcasts.commands;

// Remote imports.
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

// Local imports.
import rs.expand.pixelmonbroadcasts.utilities.ConfigMethods;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.printBasicError;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.printBasicMessage;

// Note: printBasicError and printBasicMessage are static imports for functions from PrintingMethods, for convenience.
public class BaseCommand implements CommandExecutor
{
    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        src.sendMessage(Text.of("hey the base command works"));

        return CommandResult.success();
    }
}
