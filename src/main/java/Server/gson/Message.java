package Server.gson;

import com.google.gson.Gson;

import java.util.Collections;
import java.util.List;

public class Message {

    public ClientListMessage clientListMessage;
    public RegisterMessage registerMessage;
    public PrivateMessage privateMessage;
    public PublicMessage publicMessage;
    public AuthMessage authMessage;
    public ChangeNick changeNick;
    public Command command;

    public static Message createClientList(List<String> nicknames) {
        Message message = create(Command.CLIENT_LIST);
        ClientListMessage msg = new ClientListMessage();
        msg.online = nicknames;
        message.clientListMessage = msg;
        return message;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static Message createPublic(PublicMessage msg) {
        Message m = create(Command.PUBLIC_MESSAGE);
        m.publicMessage = msg;
        return m;
    }
    private static Message create(Command cmd) {
        Message m = new Message();
        m.command = cmd;
        return m;
    }

    public static Message createPrivate(PrivateMessage msg) {
        Message m = create(Command.PRIVATE_MESSAGE);
        m.privateMessage = msg;
        return m;
    }
    public static Message createAuth(AuthMessage msg) {
        Message m = create(Command.AUTH_MESSAGE);
        m.authMessage = msg;
        return m;
    }

    public static Message createNick(ChangeNick msg) {
        Message m = create(Command.CHANGE_NICK);
        m.changeNick = msg;
        return m;
    }

    public static Message createRegister(RegisterMessage msg) {
        Message m = create(Command.REGISTER_MESSAGE);
        m.registerMessage = msg;
        return m;
    }

    public static Message fromJson(String json) {
        return new Gson().fromJson(json, Message.class);
    }
}
