package Server.gson;

import com.google.gson.Gson;

public class WorkWithGroup {

    public String identify;
    public String trueOrFalse;
    public String nameGroup;

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static GroupMessage fromJson(String json) {
        return new Gson().fromJson(json, GroupMessage.class);
    }

}
