package Server.gson;
import com.google.gson.Gson;

public class GroupMessage {

    public String from;
    public String message;
    public String nameGroup;

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static GroupMessage fromJson(String json) {
        return new Gson().fromJson(json, GroupMessage.class);
    }

}
