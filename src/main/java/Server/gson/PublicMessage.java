package Server.gson;
import com.google.gson.Gson;

public class PublicMessage {

    public String from;
    public String message;
    public String nameGroup;

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static PublicMessage fromJson(String json) {
        return new Gson().fromJson(json, PublicMessage.class);
    }

}
