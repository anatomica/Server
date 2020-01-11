package Server.gson;
import com.google.gson.Gson;

public class AuthMessage {

    public String login;
    public String password;
    public String token;

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static AuthMessage fromJson(String json) {
        return new Gson().fromJson(json, AuthMessage.class);
    }
}
