package Server.gson;
import com.google.gson.Gson;

public class RegisterMessage {

    public String nickname;
    public String login;
    public String password;

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static RegisterMessage fromJson(String json) {
        return new Gson().fromJson(json, RegisterMessage.class);
    }
}
