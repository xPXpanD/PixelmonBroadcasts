// The one and only. Accept no imitations.
package rs.expand.pixelmonbroadcasts.commands;

// Remote imports.
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

// Local imports.
import rs.expand.pixelmonbroadcasts.utilities.ConfigMethods;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.printBasicMessage;

// Note: printBasicMessage is a static import for a function from PrintingMethods, for convenience.
public class Reload implements CommandExecutor
{
    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        if (src instanceof Player)
        {
            printBasicMessage(
                    "§4PBR §f// §dInfo: §3Player §b" + src.getName() + "§3 reloaded the Pixelmon Broadcasts config!");
        }

        // Load/create config, and then unload and reload command mappings.
        ConfigMethods.tryCreateAndLoadConfig();
        ConfigMethods.registerCommands();

        if (src instanceof Player)
        {
            src.sendMessage(Text.of("§7-----------------------------------------------------"));
            src.sendMessage(Text.of("§bReloaded the Pixelmon Broadcasts main config!"));
            src.sendMessage(Text.of("§bPlease check the console for any errors."));
            src.sendMessage(Text.of("§7-----------------------------------------------------"));
        }
        else
        {
            src.sendMessage(Text.of("§bReloaded the main Pixelmon Broadcasts config!"));
            src.sendMessage(Text.of("§bPlease check the console for any errors."));
        }

        return CommandResult.success();
    }
}
