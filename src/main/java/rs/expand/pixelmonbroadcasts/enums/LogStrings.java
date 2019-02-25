package rs.expand.pixelmonbroadcasts.enums;

// A list of different events for logging purposes, and their matching unique colors and bits of text.
public enum LogStrings
{
    // Generic events.
    BLACKOUT('6', " was knocked out by a "), // Gold. (orange)
    CATCH('b', " caught a "), // Aqua. (light blue)
    CHALLENGE('9', " started fighting a "), // Blue.
    FORFEIT('e', " fled from a "), // Yellow.
    VICTORY('2', " defeated a "), // Dark Green.
    HATCH('d', " hatched a ", null), // Light Purple. (pink)

    // More involved generics. These get TWO messages! Or more. Gee, Bill.
    TRADE('5', " traded their ", " for ", "'s "), // Dark Purple.
    DRAW('7', "'s battle with ", " ended in a draw"), // Gray. (light gray)
    FAINT('c', "'s ", " fainted"), // Red.

    // This one is unique and needs its own printer. Just grab the color, for now. (so we can edit those from here, too)
    SPAWN('a', null, null); // Green. (light green)

    public char color; // Still usable: Dark Blue (1), Dark Aqua (3), Dark Red (4), Dark Gray (8), maybe White (f)
    public String[] message;

    LogStrings(final char color, final String... message)
    {
        this.color = color;
        this.message = message;
    }
}
