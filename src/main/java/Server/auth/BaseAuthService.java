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
            if (rs.getString("Token") != null) {
                if (rs.getString("Token").equals(token))
                    stmt.executeUpdate(String.format("UPDATE LoginData SET Token = '%s' WHERE Token = '%s'", "", token));
            }
        }
        ResultSet resultSet = stmt.executeQuery("select * from LoginData");
        while (resultSet.next()) {
            if (resultSet.getString("Login").equals(login) && resultSet.getString("Pass").equals(pass)) {
                String nick = resultSet.getString("Nick");
                stmt.executeUpdate(String.format("UPDATE LoginData SET Token = '%s' WHERE Nick = '%s'", token, nick));
                return nick;
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
        ClientHandler.logger.info("Сервис авторизации запущен!");
    }

    @Override
    public void stop() {
        ClientHandler.logger.info("Сервис авторизации остановлен!");
    }
}
