package TwentyOnePoint;

public class Card 
{
    public final CardColor color;
    public final CardPoint point;

    public Card(CardColor color, CardPoint point)
    {
        this.color = color;
        this.point = point;
    }

    @Override
    public String toString()
    {
        return this.color.toFace() + this.point.toString();
    }

}



