package TwentyOnePoint;

import java.util.ArrayList;

public final class CardPlayer 
{
    public final ArrayList<Card> cards;
    public final Object id;
    public final String name;
    public final boolean isBanker;
    public boolean isTaking;
    public Card last_card;

    public CardPlayer(Object id, String name, boolean isBanker)
    {
        this.id = id;
        this.isBanker = isBanker;
        cards = new ArrayList<Card>();
        isTaking = true;
        this.name = name;
    }

    public final void takeCard(Card card)
    {
        this.cards.add(card);
        this.last_card = card;
    }

    public final void stopTakeCard()
    {
        this.isTaking = false;
    }

    public final int getTotalPoint()
    {
        int num = 0;
        for(Card card : this.cards)
        {
            num += card.point.toPoint();
        }
        return num;
    }

    @Override
    public final String toString()
    {
        String result = "";
        int idx = 0;
        int size = this.cards.size();
        for(idx = 0; idx < size; idx++)
        {
            result += this.cards.get(idx).toString();
            if(idx != size - 1)
            {
                result += " + ";
            }
        }

        result += " = ";
        result += this.getTotalPoint() + "点";

        return result;
    }
    
}

