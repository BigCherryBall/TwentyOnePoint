package TwentyOnePoint;

import java.util.Collections;
import java.util.LinkedList;

public class CardManager 
{
    private LinkedList<Card> current_cards;

    public synchronized Card getCard()
    {
        return this.current_cards.pollLast();
    }

    public Card lookCard()
    {
        return this.current_cards.getLast();
    }

    public void reset() 
    {
        this.current_cards = new LinkedList<Card>(CardPoll.cards);
        Collections.shuffle(current_cards);
    }
    
}