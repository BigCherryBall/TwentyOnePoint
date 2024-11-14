package TwentyOnePoint;

public enum CardPoint 
{
    A("A", 1),
    C2("2", 2),
    C3("3", 3),
    C4("4", 4),
    C5("5", 5),
    C6("6", 6),
    C7("7", 7),
    C8("8", 8),
    C9("9", 9),
    C10("10", 10),
    J("J", 10),
    Q("Q", 10),
    K("K", 10);

    private final String name;
    private final int point;

    CardPoint(String name, int point)
    {
        this.name = name;
        this.point = point;
    }

    @Override
    public final String toString()
    {
        return this.name;
    }

    public final int toPoint()
    {
        return this.point;
    }
}
