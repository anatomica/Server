package Server.gson;
import com.google.gson.Gson;

public class PrivateMessage {

    public String from;
    public String to;
    public String message;

    public String toJson() {
//        Gson gson = new GsonBuilder()
//                .setPrettyPrinting()
//                .create();
//        return gson.toJson(this);
        return new Gson().toJson(this);
    }

    public static PrivateMessage fromJson(String json) {
        return new Gson().fromJson(json, PrivateMessage.class);
    }

    public static void main(String[] args) {
        PrivateMessage m = new PrivateMessage();
        m.from = "Nick1";
        m.to = "Nick2";
        m.message = "Hello, World!";

        System.out.println(m.toJson());
    }

}
