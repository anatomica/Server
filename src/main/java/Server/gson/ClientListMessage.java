package Server.gson;
import com.google.gson.Gson;

import java.util.List;

public class ClientListMessage {

    public List<String> online;
    public String from;

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static ClientListMessage fromJson(String json) {
        return new Gson().fromJson(json, ClientListMessage.class);
    }
}
