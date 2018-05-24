package rs.expand.pixelmonbroadcasts.utilities;

// Remote imports.
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentTranslation;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.text.Text;
import java.util.*;

// A collection of methods that are commonly used. One changed word or color here, and half the mod changes. Sweet.
public class PrintingMethods
{
    // Remove the ugly prefix from console commands, so we can roll our own. Thanks for the examples, NickImpact!
    private static Optional<ConsoleSource> getConsole()
    {
        if (Sponge.isServerAvailable())
            return Optional.of(Sponge.getServer().getConsole());
        else
            return Optional.empty();
    }

    // If we need to print something without any major formatting, do it here. Good for console lists.
    public static void printBasicMessage(final String inputString)
    {
        getConsole().ifPresent(console ->
                console.sendMessage(Text.of("§f" + inputString)));
    }

    // If we need to show a basic error, do it here.
    public static void printBasicError(final String inputString)
    {
        getConsole().ifPresent(console ->
                console.sendMessage(Text.of("§4PBR §f// §4Error: §c" + inputString)));
    }

    // If we can't read a main config options bundle (really just a String), format and throw this error.
    static void printOptionsNodeError(final List<String> nodes)
    {
        for (final String node : nodes)
            printBasicMessage("§cCould not read options node \"§4" + node + "§c\".");

        printBasicMessage("§cThe main config contains invalid options. Disabling these.");
        printBasicMessage("§cCheck the config, and when fixed use §4/pixelmonbroadcasts reload§c.");
    }

    // If we can't read a main config message, format and throw this error.
    static void printMessageNodeError(final List<String> nodes)
    {
        for (final String node : nodes)
            printBasicMessage("§cCould not read message node \"§4" + node + "§c\".");

        printBasicMessage("§cThe main config contains invalid messages. Hiding these.");
        printBasicMessage("§cCheck the config, and when fixed use §4/pixelmonbroadcasts reload§c.");
    }

    // Check if the given player has the given flag (or flags) set, and if so, return a clean status that we can use.
    public static boolean checkToggleStatus(final EntityPlayerMP recipient, final String... flags)
    {
        // Did we get provided just one flag? This will generally be the most common situation.
        if (flags.length == 1)
        {
            // Does the player have a flag set for this notification?
            if (recipient.getEntityData().getCompoundTag("pbToggles").hasKey(flags[0]))
            {
                // Return the flag's status.
                return recipient.getEntityData().getCompoundTag("pbToggles").getBoolean(flags[0]);
            }

            // Player does not have the flag, so return the default state ("true").
            return true;
        }
        /*                                                                                                    *\
           vvv Currently unused. Was used for a little bit, but then disabled in favor of specific flags. vvv

        // We do not, look through all the flags and see if any are disabled.
        else if (flags.length > 1)
        {
            // Set up a Set of the state of all provided flags.
            Set<Boolean> flagSet = new HashSet<>();

            // Loop through all provided flags, and add them to the Set.
            for (String flag : flags)
            {
                // Does the player have a flag set for this notification?
                if (recipient.getEntityData().getCompoundTag("pbToggles").hasKey(flag))
                {
                    // Return the flag's status.
                    flagSet.add(recipient.getEntityData().getCompoundTag("pbToggles").getBoolean(flag));
                }
                else
                    flagSet.add(true);
            }

            // Return "true" if all flags are either true or unset (default state is true). Return "false" otherwise.
            return !flagSet.contains(false);
        }
        \*                                                                                                    */
        else
        {
            printBasicError("Could not find any player toggles to check. Swallowing message.");
            printBasicError("This is a bug, please report this. Include the specific event.");
            return false;
        }
    }

    // Gets a key from the language file, formats it using our own custom parseRemoteString, and then returns it.
    public static String getTranslation(final String key, final Object... params)
    {
        // Format the key grabbed from our language file, replacing ampersands with section symbols.
        return parseRemoteString(new TextComponentTranslation(key, params).getUnformattedComponentText());
    }

    // Gets a key from the language file, formats it using our own custom parseRemoteString, and then sends it.
    public static void sendTranslation(CommandSource recipient, String key, Object... params)
    {
        // Format the key grabbed from our language file, replacing ampersands with section symbols.
        final String formattedInput =
                parseRemoteString(new TextComponentTranslation(key, params).getUnformattedComponentText());

        // Send the now-formatted input directly to the player.
        recipient.sendMessage(Text.of(formattedInput));
    }

    // Takes a config String and changes any ampersands to section symbols, which we can use internally.
    // Only runs during config (re-)loads, after which we commit the parsed strings to memory.
    static String parseRemoteString(final String input)
    {
        // Were we able to read the String being checked from the config?
        if (input != null)
        {
            // Set up a list of valid formatting codes.
            final List<Character> validFormattingCharacters = Arrays.asList
            (
                    // Color numbers.
                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                    // Color letters, lower and upper case.
                    'a', 'b', 'c', 'd', 'e', 'f', 'A', 'B', 'C', 'D', 'E', 'F',
                    // Other formatting codes.
                    'k', 'l', 'm', 'n', 'o', 'r'
            );

            // Start replacing our ampersands.
            final StringBuilder mutableInput = new StringBuilder(input);
            for (int i = 0; i < mutableInput.length(); i++)
            {
                // Is the character that's currently being checked an ampersand?
                if (mutableInput.charAt(i) == '&')
                {
                    // Make sure the iterator is still inside of the input String's length. Let's not check out of bounds.
                    if ((i + 1) < mutableInput.length())
                    {
                        // Look ahead: Does the next character contain a known formatting character? Replace the ampersand!
                        if (validFormattingCharacters.contains(mutableInput.charAt(i + 1)))
                            mutableInput.setCharAt(i, '§');
                    }
                }
            }

            // Replace our old input String with the one that we fixed formatting on.
            return mutableInput.toString();
        }

        // If we could not read from the config, this is hit.
        return null;
    }
}
