package Server;

import Server.auth.BaseAuthService;
import Server.gson.Message;
import Server.gson.WorkWithGroup;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;

public class DataBase {

    private MyServer myServer;
    private static Connection conn;
    private static Statement stmt;

    public DataBase(MyServer myServer) {
        this.myServer = myServer;
    }

    public boolean checkExistGroup(WorkWithGroup workWithGroup, ClientHandler toClient) {
        try {
            connection();
            ResultSet rs = stmt.executeQuery("select * from sqlite_master");
            while (rs.next()) {
                if (rs.getString("name").equals(workWithGroup.nameGroup)) {
                    toClient.sendMessage("Группа с данным именем существует!");
                    disconnect();
                    return true;
                }
            }
            disconnect();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    void createGroup(WorkWithGroup workWithGroup, ClientHandler toClient) throws SQLException {
        try {
            connection();
            stmt.execute(String.format("create table if not exists %s (\n" +
                    "\t id integer not null \n" +
                    "\t constraint \"%s_pk\" \n" +
                    "\t primary key autoincrement, \n" +
                    "\t Nick text \n" +
                    ");", workWithGroup.nameGroup, workWithGroup.nameGroup));
            toClient.sendMessage("Группа успешно создана!");
            Message msg = buildWorkWithGroup(workWithGroup.nameGroup, "1");
            toClient.sendMessage(msg.toJson());
            disconnect();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Message buildWorkWithGroup(String nameGroup, String identify) {
        WorkWithGroup msg = new WorkWithGroup();
        msg.identify = identify;
        msg.nameGroup = nameGroup;
        return Message.workWithGroup(msg);
    }

    private static void connection() throws ClassNotFoundException, SQLException {
        try {
            URI uri = BaseAuthService.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            String pathToDB = new File(uri).getParent() + BaseAuthService.pathInLinux;
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + pathToDB);
            stmt = conn.createStatement();
        } catch (URISyntaxException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void disconnect() throws SQLException {
        stmt.close();
        conn.close();
    }
}
