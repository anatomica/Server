package Server.auth;
import java.sql.SQLException;

public interface AuthService {

    void start();
    void stop();

    String getNickByLoginPass(String login, String pass, String token) throws SQLException;

}
