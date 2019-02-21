package rs.expand.pixelmonbroadcasts.enums;

// Super simple, contains the right tags to use for retrieving specific types of broadcasts. Made to be expanded upon.
public enum EnumBroadcastTypes
{
    PRINT("print."),
    NOTIFY("notify.");

    public String prefix;

    EnumBroadcastTypes(final String prefix)
    {
        this.prefix = prefix;
    }
}
