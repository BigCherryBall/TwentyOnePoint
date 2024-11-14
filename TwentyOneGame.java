package TwentyOnePoint;

import java.util.HashMap;

public final class TwentyOneGame implements TimeoutNotify
{

    private interface ReqHandler
    {
        boolean handle(Req21Game req, EveryCardPlay current);
    }

    private final Game21Notify back;

    private final HashMap<Object, EveryCardPlay> manager;

    private final ReqHandler[] handlers = 
    {
            this::gameDescript,
            this::start,
            this::getCard,
            this::stopTaking,
            this::lookCard,
    };

    public TwentyOneGame(Game21Notify back)
    {
        this.back = back;
        this.manager = new HashMap<>();
    }

    /* 返回true: 是本模块命令, false: 不是本模块命令 */
    public boolean cmd(Req21Game req)
    {
        EveryCardPlay current = this.manager.get(req.belong);
        for(ReqHandler handler : this.handlers)
        {
            if(handler.handle(req, current))
            {
                return true;
            }
        }
        return false;
    }

    private boolean gameDescript(Req21Game req, EveryCardPlay current)
    {
        String tmp = null;

        if(!req.msg.equals("21点帮助") && !req.msg.equals("21点说明"))
        {
            return false;
        }

        tmp = "*****21点小游戏说明*****" + 
            "\nx人21点: 开启21点小游戏" + 
            "\n叫牌: 获取一张卡牌" +
            "\n停牌: 确认手牌, 停止抽牌" +
            "\n看牌: 每局庄家有一次观看牌堆中下一张牌的机会" +
            "\n21点帮助: 查看小游戏帮助";
        
        this.back.backHandle(this.buildRes(req, tmp));
        return true;
    }

    private boolean start(Req21Game req, EveryCardPlay current)
    {
        int person_count = 0;

        if(!req.msg.endsWith("21点"))
        {
            return false;
        }

        if(req.msg.equals("21点"))
        {
            this.startGame(req, current, 2);
        }
        else if(req.msg.endsWith("人21点"))
        {
            person_count = TwentyOneGame.getNum(req.msg.charAt(0));
            if(person_count == 0)
            {
                return true;
            }

            this.startGame(req, current, person_count);
        }
        return true;
    }

    private boolean getCard(Req21Game req, EveryCardPlay current)
    {
        CardPlayer player = null;
        CardPlayer winner = null;
        int point = 0;
        boolean banker_over = false;
        boolean normal_over = false;

        if(!req.msg.equals("叫牌"))
        {
            return false;
        }

        if(current == null || current.getState() == EveryCardPlay.NOT_BEGIN)
        {
            this.back.backHandle(this.buildRes(req, "群组内还没有21点小游戏\n请使用{x人21点}指令即可开始游戏并且做庄"));
            return true;
        }

        player = current.getPlayer(req.sender);
        if(player == null)
        {
            if(current.isPlayerFull())
            {
                this.back.backHandle(this.buildRes(req, "%s, 玩家已满, 无法叫牌\n对局详情:\n庄家: %s\n玩家: %s\n".formatted(
                    req.sender_name, current.getMainPlayerName(), current.getAllNormalPlayerName()
                )));
                return true;
            }
        }
        else if(player.isBanker)
        {
            if(!current.hasPlayerAdd())
            {
                this.back.backHandle(this.buildRes(req, "当前还没有玩家加入, 等待玩家加入后叫牌"));
                return true;
            }
        }

        current.updateOutTimer();

        player = current.takeCard(req.sender, req.sender_name);
        if(player == null)
        {
            return true;
        }

        point = player.getTotalPoint();

        if(player.isBanker)
        {
            if(point > 21)
            {
                player.isTaking = false;
                this.back.backHandle(this.buildRes(req, "%s本轮获取的纸牌: %s\n当前纸牌情况:\n%s\n\n你战败了, 恭喜:%s\n获得胜利!!!".formatted(
                    req.sender_name, player.last_card.toString(), player.toString(), current.getAllNormalPlayerName()
                )));
                current.gameOver();
            }
            else if(point == 21)
            {
                this.back.backHandle(this.buildRes(req, "%s本轮获取的纸牌: %s\n当前纸牌情况:\n%s\n\n 恭喜%s获得胜利!!!".formatted(
                    req.sender_name, player.last_card.toString(), player.toString(), current.getMainPlayerName()
                )));
                current.gameOver();
            }
            else
            {
                this.back.backHandle(this.buildRes(req, "%s本轮获取的纸牌: %s\n当前纸牌情况:\n%s\n\n继续请发送{叫牌}, 结束请发送{停牌}".formatted(
                    req.sender_name, player.last_card.toString(), player.toString()
                )));
            }
            
            return true;
        }
        else
        {
            if(point < 21)
            {
                this.back.backHandle(this.buildRes(req, "%s本轮获取的纸牌: %s\n当前纸牌情况:\n%s\n\n继续请发送{叫牌}, 结束请发送{停牌}".formatted(
                    req.sender_name, player.last_card.toString(), player.toString()
                )));
            }
            else if(point == 21)
            {
                this.back.backHandle(this.buildRes(req, "%s本轮获取的纸牌: %s\n当前纸牌情况:\n%s\n\n 恭喜:%s\n获得胜利!!!\n\n庄家纸牌:\n%s".formatted(
                    req.sender_name, player.last_card.toString(), player.toString(), current.getAllNormalPlayerName(), current.getBankerCard()
                )));
                current.gameOver();
            }
            else
            {
                player.isTaking = false;
                banker_over = current.isBankerOver();
                normal_over = current.isNormalPlayerOver();
                if(normal_over && banker_over)
                {
                    winner = current.getMaxPointPlayerFor21Point();
                    if(winner.isBanker)
                    {
                        this.back.backHandle(this.buildRes(req, "%s本轮获取的纸牌: %s\n当前纸牌情况:\n%s\n\n你战败了, 恭喜:%s\n获得胜利!!!".formatted(
                            req.sender_name, player.last_card.toString(), player.toString(), current.getMainPlayerName()
                        )));
                    }
                    else
                    {
                        this.back.backHandle(this.buildRes(req, "%s本轮获取的纸牌: %s\n当前纸牌情况:\n%s\n\n你战败了, 但是你的队友%s带领你获得了胜利\n恭喜:%s\n获得胜利!!!".formatted(
                            req.sender_name, player.last_card.toString(), player.toString(), winner.name, current.getAllNormalPlayerName()
                        )));
                    }
                    current.gameOver();
                
                }
                else if(normal_over)
                {
                    this.back.backHandle(this.buildRes(req, "%s本轮获取的纸牌: %s\n当前纸牌情况:\n%s\n\n你战败了, 等待庄家叫牌".formatted(
                        req.sender_name, player.last_card.toString(), player.toString()
                    )));
                }
                else
                {
                    this.back.backHandle(this.buildRes(req, "%s本轮获取的纸牌: %s\n当前纸牌情况:\n%s\n\n你战败了, 等待你的队友叫牌".formatted(
                        req.sender_name, player.last_card.toString(), player.toString()
                    )));
                }
            }
        }

        
        return true;
    }

    public boolean stopTaking(Req21Game req, EveryCardPlay current)
    {
        CardPlayer player = null;
        CardPlayer winner = null;
        boolean banker_over = false;
        boolean normal_over = false;

        if(!req.msg.equals("停牌"))
        {
            return false;
        }

        if(current == null || current.getState() == EveryCardPlay.NOT_BEGIN)
        {
            return true;
        }

        player = current.getPlayer(req.sender);
        if(player == null)
        {
            return true;
        }

        current.updateOutTimer();

        player.isTaking = false;
        banker_over = current.isBankerOver();
        normal_over = current.isNormalPlayerOver();

        if(player.isBanker)
        {
            if(!normal_over)
            {
                this.back.backHandle(this.buildRes(req, "庄家已停牌\n等待其他玩家叫牌"));
                return true;
            }

            winner = current.getMaxPointPlayerFor21Point();
            
            if(winner.isBanker)
            {
                this.back.backHandle(this.buildRes(req, "所有玩家均已停牌，游戏结束\n最佳手牌:%s\n恭喜:%s\n获得胜利!!!".formatted(
                    winner.toString(), current.getMainPlayerName()
                )));
            }
            else
            {
                this.back.backHandle(this.buildRes(req, "所有玩家均已停牌，游戏结束\n最佳手牌:%s\n恭喜:%s\n获得胜利!!!".formatted(
                    winner.toString(), current.getAllNormalPlayerName()
                )));
            }
            
            current.gameOver();
            return true;
        }
        else
        {
            if(normal_over && banker_over)
            {
                winner = current.getMaxPointPlayerFor21Point();
                if(winner.isBanker)
                {
                    this.back.backHandle(this.buildRes(req, "所有玩家均已停牌，游戏结束\n最佳手牌:%s\n恭喜:%s\n获得胜利!!!".formatted(
                        winner.toString(), current.getMainPlayerName()
                    )));
                }
                else
                {
                    this.back.backHandle(this.buildRes(req, "所有玩家均已停牌，游戏结束\n最佳手牌:%s\n恭喜:%s\n获得胜利!!!".formatted(
                        winner.toString(), current.getAllNormalPlayerName()
                    )));
                }
                current.gameOver();
            }
            else if(normal_over)
            {
                this.back.backHandle(this.buildRes(req, "你已停牌\n等待庄家叫牌"));
            }
            else
            {
                this.back.backHandle(this.buildRes(req, "你已停牌\n等待其他玩家叫牌"));
            }
        }

        return true;
    }


    public boolean lookCard(Req21Game req, EveryCardPlay current)
    {
        CardPlayer player = null;
        Card card = null;

        if(!req.msg.equals("看牌"))
        {
            return false;
        }

        if(current == null || current.getState() == EveryCardPlay.NOT_BEGIN)
        {
            return true;
        }

        player = current.getPlayer(req.sender);
        if(player == null || !player.isBanker)
        {
            return true;
        }

        current.updateOutTimer();

        if(current.banker_look_card_count >= EveryCardPlay.banker_look_card_max)
        {
            this.back.backHandle(this.buildRes(req, "本局看牌次数已达上限"));
            return true;
        }

        card = current.lookNextCard();
        this.back.backHandle(this.buildRes(req, "下一张卡牌是" + card.toString() + "\n若" + EveryCardPlay.GiveUpCardSecond + "秒内无人叫牌, 该牌将被弃置"));

        return true;
    }

    @Override
    public void gameTimerOver(EveryCardPlay current, Object belong)
    {
        Res21Game tmp = new Res21Game();
        tmp.belong = belong;
        tmp.sender = belong;
        tmp.text = "游戏超时, 对局自动结束";
        this.back.backHandle(tmp);
    }



    private void startGame(Req21Game req, EveryCardPlay current, int num)
    {
        if(current == null)
        {
            current = new EveryCardPlay(this, req.belong);
            this.manager.put(req.belong, current);
        }
        current.updateOutTimer();

        if(current.getState() == EveryCardPlay.HAS_BEGAN)
        {
            if(current.isPlayerFull())
            {
                this.back.backHandle(this.buildRes(req, 
                    "%s, 21点游戏已经开始\n庄家: %s\n玩家: %s\n玩家已满，请耐心等待游戏结束".formatted(req.sender_name, 
                    current.getMainPlayerName(), current.getAllNormalPlayerName())));
            }
            else
            {
                this.back.backHandle(this.buildRes(req, 
                    "%s, 21点游戏已经开始\n庄家: %s\n玩家: %s\n发送{叫牌}即可参与游戏".formatted(req.sender_name, 
                    current.getMainPlayerName(), current.getAllNormalPlayerName())));
            }
            
            return;
        }

        current.start(num, req.sender, req.sender_name);
        this.back.backHandle(this.buildRes(req, "%s发起了小游戏21点!\n任意玩家发送{叫牌}即可入局".formatted(req.sender_name)));
    }

    

    private Res21Game buildRes(Req21Game req, String text)
    {
        Res21Game res = new Res21Game();
        res.belong = req.belong;
        res.sender = req.sender;
        res.text = text;
        return res;
    }

    private static int getNum(char c)
    {
        return switch (c) {
            case '2', '二', '双' -> 2;
            case '3', '三' -> 3;
            case '4', '四' -> 4;
            case '5', '五' -> 5;
            case '6', '六', '多' -> 6;
            default -> 0;
        };
    }
}
