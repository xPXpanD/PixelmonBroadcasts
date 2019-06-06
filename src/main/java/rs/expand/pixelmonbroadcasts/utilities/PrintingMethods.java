package rs.expand.pixelmonbroadcasts.utilities;

import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import rs.expand.pixelmonbroadcasts.enums.EventData;

import java.util.List;

import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.logger;
import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.messagesConfig;

// A collection of methods that are commonly used. One changed word or color here, and half the mod changes. Sweet.
public class PrintingMethods
{
    // Prints an event message to console, if it is enabled. One size fits all, with the help of the LogType enum.
    public static void logEvent(final EventData event, final String worldName, final BlockPos location, final String... inputs)
    {
        // If options() is null, we'll catch that and error in our iterateAndBroadcast call. Don't worry about it here.
        if (event.options() != null && event.options().toLowerCase().contains("log"))
        {
            if (event instanceof EventData.Spawns) // Spawn logging needs some special logic.
            {
                logger.info
                (
                        '§' + event.color() +
                        "A " + inputs[0] +
                        " has spawned in world \"" + worldName +
                        "\", at X:" + location.getX() +
                        " Y:" + location.getY() +
                        " Z:" + location.getZ()
                );
            }
            else if (event.messages().length == 2) // Generally used for events that have two players.
            {
                // An example from the battle draw event follows.
                logger.info
                (
                        '§' + event.color() +
                //                  player 1    's battle with        player 2    ended in a draw
                        "Player " + inputs[0] + event.messages()[0] + inputs[1] + event.messages()[1] +
                        " in world \"" + worldName +
                        "\", at X:" + location.getX() +
                        " Y:" + location.getY() +
                        " Z:" + location.getZ()
                );
            }
            else
            {
                // An example from the forfeit event follows.
                logger.info
                (
                        '§' + event.color() +
                //                  player      fled from a           pokémon/trainer
                        "Player " + inputs[0] + event.messages()[0] + inputs[1] +
                        " in world \"" + worldName +
                        "\", at X:" + location.getX() +
                        " Y:" + location.getY() +
                        " Z:" + location.getZ()
                );
            }
        }
    }

    // If we can't read a main config options bundle, throw this error. Called during reloads and hybrid checks.
    static void printOptionsNodeError(final List<String> nodes)
    {
        for (final String node : nodes)
            logger.info("    §cCould not read settings.conf node \"§4" + node + "§c\"!");

        logger.info("    §cThe main configuration file contains one or more invalid options.");
        logger.info("    §cCheck the config, and when fixed use §4/pixelmonbroadcasts reload§c.");
    }

    // Gets a value matching the given messages key, formats it (ampersands to section characters), and then sends it.
    // Also swaps any provided placeholders with String representations of the Objects given, if present.
    public static void sendTranslation(CommandSource recipient, String key, Object... params)
    {
        // This is slightly hacky, but split the incoming key up into separate nodes so we can read it.
        final String[] keySet = key.split("\\.");

        // Get the message from the message config, if it's there.
        String message = messagesConfig.getNode((Object[]) keySet).getString();

        // Did we get a message?
        if (message != null)
        {
            // Swallow the message if it's been emptied out.
            if (!message.equals(""))
            {
                // If any parameters are available, find all placeholders in the message and replace them.
                for (int i = 0; i < params.length; i++)
                    message = message.replace("{" + (i+1) + "}", params[i].toString());

                recipient.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(message).toText());
            }
        }
        // We did not get a message, return the provided key and make sure it's unformatted.
        else
            recipient.sendMessage(Text.of("§r" + key));
    }

    // Gets a value matching the given messages key, formats it (ampersands to section characters), and then returns it.
    // Also swaps any provided placeholders with String representations of the Objects given, if present.
    public static String getTranslation(String key, final Object... params)
    {
        // This is slightly hacky, but split the incoming key up into separate nodes so we can read it.
        final String[] keySet = key.split("\\.");

        // Get the message from the message config, if it's there.
        String message = messagesConfig.getNode((Object[]) keySet).getString();

        // Did we get a message?
        if (message != null)
        {
            // If any parameters are available, find all placeholders in the message and replace them.
            for (int i = 0; i < params.length; i++)
                message = message.replace("{" + (i+1) + "}", params[i].toString());

            return TextSerializers.FORMATTING_CODE.replaceCodes(message, '§');
        }
        // We did not get a message, return the provided key and make sure it's unformatted.
        else
            return "§r" + key;
    }

    // Inserts the correct tense into lang keys that might have multiple tenses. Returns the translation.
    static String getTensedTranslation(final boolean presentTense, final String key, final Object... params)
    {
        // Set up a String to translate and then return.
        final String tensedKey;

        // Splits our input key, adds the correct tense at a constant known location, and then pieces it back together.
        if (presentTense)
            tensedKey = key.substring(0, 6) + "present_tense." + key.substring(6);
        else
            tensedKey = key.substring(0, 6) + "past_tense." + key.substring(6);

        // Send back the translation of our new freshly-tensed key.
        return getTranslation(tensedKey, params);
    }

    // Get translated names for a given nature's positive and negative stats from the lang.
    static String getTranslatedNatureStat(StatsType stat)
    {
        switch(stat)
        {
            case Attack:
                return getTranslation("hover.status.attack");
            case Defence:
                return getTranslation("hover.status.defense");
            case SpecialAttack:
                return getTranslation("hover.status.special_attack");
            case SpecialDefence:
                return getTranslation("hover.status.special_defense");
            case Speed:
                return getTranslation("hover.status.speed");
            default:
                return getTranslation("hover.status.none");
        }
    }
}
