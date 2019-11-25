package Server.auth;
import java.sql.*;

public class BaseAuthService implements AuthService {

    private static Connection conn;
    private static Statement stmt;

    @Override
    public String getNickByLoginPass(String login, String pass) throws SQLException {

        try {
            connection();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        ResultSet rs = stmt.executeQuery("select * from LoginData");
        while (rs.next()) {
            ResultSetMetaData dataInBase = rs.getMetaData();
            for (int i = 1; i <= dataInBase.getColumnCount(); i++) {
                if (rs.getString("Login").equals(login) && rs.getString("Pass").equals(pass)) {
                    return rs.getString("Nick");
                }
            }
        }
        return null;
    }

    private static void connection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        conn = DriverManager.getConnection("jdbc:sqlite::resource:LoginData.db");
        stmt = conn.createStatement();
    }

    public static void disconect() throws SQLException {
        stmt.close();
        conn.close();
    }

    @Override
    public void start() {
        System.out.println("Сервис авторизации запущен!");
    }

    @Override
    public void stop() {
        System.out.println("Сервис автоизации остановлен!");
    }
}
