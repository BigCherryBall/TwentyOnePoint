package TwentyOnePoint;

public enum CardColor 
{
    clubs(CardColor.CLUBS, "♣️"),
    diamonds(CardColor.DIAMONDS, "♦️"),
    hearts(CardColor.HEARTS, "♥️"),
    spades(CardColor.SPADES, "♠️");

    public static final String CLUBS = "梅花";
    public static final String DIAMONDS = "方块";
    public static final String HEARTS = "红桃";
    public static final String SPADES = "黑桃";


    private final String name;
    private final String face;

    CardColor(String name, String face)
    {
        this.name = name;
        this.face = face;
    }

    @Override
    public final String toString()
    {
        return name;
    }

    public final String toFace()
    {
        return this.face;
    }
}