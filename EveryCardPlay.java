package TwentyOnePoint;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class EveryCardPlay
{
    public static final int banker_look_card_max = 1;
    public int banker_look_card_count;
    private CardPlayer[] players;
    private final CardManager cardManager;
    public static final int NOT_BEGIN = 0;
    public static final int HAS_BEGAN = 1;
    private int state;

    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> futureTask;

    private long last_execute_time;

    private final TimeoutNotify notify;

    private static final long TimeoutSecond = 120L;
    public static final long GiveUpCardSecond = 10L;


    private final Object belong;
    private Card lookup;



    public EveryCardPlay(TimeoutNotify notify, Object belong)
    {
        this.cardManager = new CardManager();
        this.state = EveryCardPlay.NOT_BEGIN;
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.notify = notify;
        this.belong = belong;
    }



    private final CardPlayer add(Object id, String player_name)
    {
        int idx = 0;
        for(idx = 0; idx < this.players.length; idx++)
        {
            if(this.players[idx] == null)
            {
                this.players[idx] = new CardPlayer(id, player_name, false);
                return this.players[idx];
            }
        }
        
        return null;
    }

    public void start(int player_num, Object id, String player_name)
    {
        this.cardManager.reset();
        this.players = new CardPlayer[player_num];
        this.players[0] = new CardPlayer(id, player_name, true);
        this.state = EveryCardPlay.HAS_BEGAN;
        this.futureTask = scheduler.scheduleAtFixedRate(this::checkoutTimeout, 0, 10, TimeUnit.SECONDS);
        this.last_execute_time = System.currentTimeMillis();
        this.banker_look_card_count = 0;
        this.lookup = null;
    }

    public CardPlayer takeCard(Object id, String player_name)
    {
        CardPlayer current = this.getPlayer(id);
        if(current == null)
        {
            current = this.add(id, player_name);
            if(current == null)
            {
                return null;
            }
        }

        current.takeCard(this.cardManager.getCard());
        return current;
    }

    public CardPlayer getPlayer(Object id)
    {
        int idx = 0;
        for(idx = 0; idx < this.players.length; idx++)
        {
            if(this.players[idx] != null)
            {
                if(this.players[idx].id.equals(id))
                {
                    return this.players[idx];
                }
            }
        }
        
        return null;
    }

    public boolean isPlayer(String id)
    {
        return this.getPlayer(id) != null;
    }

    public final String getMainPlayerName()
    {
        if(this.state == EveryCardPlay.NOT_BEGIN)
        {
            return "\n-";
        }
        return "\n-" + this.players[0].name;
    }

    public String getAllNormalPlayerName()
    {
        String result = "";
        int idx = 1;
        CardPlayer player = null;
        if(this.state == EveryCardPlay.NOT_BEGIN)
        {
            return "";
        }

        for(idx = 1; idx < this.players.length; idx++)
        {
            player = this.players[idx];
            if(player != null)
            {
                result +=  "\n-" + player.name;
            }
        }
        return result;
    }

    public String getBankerCard()
    {
        if(this.state == EveryCardPlay.NOT_BEGIN)
        {
            return "";
        }

        return this.players[0].toString();
    }

    public String getNormalPlayerCard()
    {
        String result = "";
        int idx = 1;
        CardPlayer player = null;

        if(this.state == EveryCardPlay.NOT_BEGIN)
        {
            return "";
        }

        for(idx = 1; idx < this.players.length; idx++)
        {
            player = this.players[idx];
            if(player != null)
            {
                result +=  "\n" + player.toString();
            }
        }

        return result;
    }

    public int getState()
    {
        return this.state;
    }

    public void gameOver()
    {
        this.state = EveryCardPlay.NOT_BEGIN;
        this.players = null;
        this.futureTask.cancel(true);
        this.futureTask = null;
        this.last_execute_time = 0L;
        this.lookup = null;
    }

    public boolean isPlayerFull()
    {
        if(this.state != EveryCardPlay.HAS_BEGAN)
        {
            return true;
        }

        for(CardPlayer player : this.players)
        {
            if(player == null)
            {
                return false;
            }
        }
        return true;
    }

    public boolean isNormalPlayerOver()
    {
        int idx = 1;
        CardPlayer player = null;

        if(this.state != EveryCardPlay.HAS_BEGAN)
        {
            return true;
        }

        for(idx = 1; idx < this.players.length; idx++)
        {
            player = this.players[idx];
            if(player == null || player.isTaking)
            {
                return false;
            }
        }

        return true;
    }

    public boolean isBankerOver()
    {
        if(this.state != EveryCardPlay.HAS_BEGAN)
        {
            return true;
        }

        return !this.players[0].isTaking;
    }

    public final boolean hasPlayerAdd()
    {
        int idx = 1;

        if(this.state != EveryCardPlay.HAS_BEGAN)
        {
            return false;
        }

        for(idx = 1; idx < this.players.length; idx++)
        {
            if(this.players[idx] != null )
            {
                return true;
            }
        }

        return false;
    }

    public CardPlayer getMaxPointPlayerFor21Point()
    {
        int idx = 1;
        CardPlayer player = null;
        int banker_point = 0;
        int player_point = 0;

        banker_point = this.players[0].getTotalPoint();

        for(idx = 1; idx < this.players.length; idx++)
        {
            player = this.players[idx];
            if(player == null)
            {
                continue;
            }

            player_point = player.getTotalPoint();
            if(player_point <= 21 && player_point > banker_point)
            {
                return player;
            }
        }

        return this.players[0];
    }

    public Card lookNextCard()
    {
        this.scheduler.schedule(this::giveUpNextCard, EveryCardPlay.GiveUpCardSecond, TimeUnit.SECONDS);
        this.banker_look_card_count++;
        this.lookup = this.cardManager.lookCard();
        return this.lookup;
    }

    private void giveUpNextCard()
    {
        if(this.lookup == null)
        {
            return;
        }

        if(this.lookup != this.cardManager.lookCard())
        {
            return;
        }

        this.cardManager.getCard();
    }

    public void checkoutTimeout()
    {
        if(this.state != EveryCardPlay.HAS_BEGAN)
        {
            return;
        }

        if(((System.currentTimeMillis() - this.last_execute_time) / 1000) > EveryCardPlay.TimeoutSecond)
        {
            this.notify.gameTimerOver(this, this.belong);
            this.gameOver();
        }

    }

    public void updateOutTimer()
    {
        this.last_execute_time = System.currentTimeMillis();
    }

    public void shutdown()
    {
        scheduler.shutdown();
    }

}
