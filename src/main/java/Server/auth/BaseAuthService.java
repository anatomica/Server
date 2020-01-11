package Server.auth;

import Server.ClientHandler;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;

public class BaseAuthService implements AuthService {

    private static Connection conn;
    private static Statement stmt;
    public static String pathInWin = "\\LoginData.db";
    public static String pathInLinux = "/LoginData.db";

    @Override
    public String getNickByLoginPass(String login, String pass, String token) throws SQLException {

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
                    String nick = rs.getString("Nick");
                    stmt.executeUpdate(String.format("UPDATE LoginData SET Token = '%s' WHERE Nick = '%s'", token, nick));
                    return nick;
                }
            }
        }
        return null;
    }

    private static void connection() throws ClassNotFoundException, SQLException {
        try {
            URI uri = BaseAuthService.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            String pathToDB = new File(uri).getParent() + pathInLinux;
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + pathToDB);
            stmt = conn.createStatement();
        } catch (URISyntaxException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void disconnect() throws SQLException {
        stmt.close();
        conn.close();
    }

    @Override
    public void start() {
        System.out.println("Сервис авторизации запущен!");
        ClientHandler.logger.info("Сервис авторизации запущен!");
    }

    @Override
    public void stop() {
        System.out.println("Сервис автоизации остановлен!");
    }
}
