package TwentyOnePoint;

public class Req21Game
{
    /* @NotNull 文本消息，用于匹配指令 */
    public String msg;
    /* @NotNull 指令发出的源所属的群组（这是一个多人游戏，必须在群组里面执行. 会在回调中返回该对象，同Res21Ganme.belong) */
    public Object belong;
    /* @NotNull 指令发出者（会在回调中返回该对象，同Res21Ganme.sender） */
    public Object sender;
    /* @NotNull 指令发出者的昵称 */
    public String sender_name;
}
