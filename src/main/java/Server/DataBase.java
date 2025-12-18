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
    private DataMessage dataMessage;
    private static Connection conn;
    private static Statement stmt;

    public DataBase(MyServer myServer, DataMessage dataMessage) {
        this.dataMessage = dataMessage;
        this.myServer = myServer;
    }

    public boolean checkExistGroup(WorkWithGroup workWithGroup, ClientHandler toClient) {
        try {
            connection();
            ResultSet rs = stmt.executeQuery("select * from sqlite_master");
            while (rs.next()) {
                if (rs.getString("name").equals(workWithGroup.nameGroup)) {
                    if (workWithGroup.identify.equals("1")) toClient.sendMessage("Группа с данным именем существует!");
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
            stmt.execute(String.format("create table if not exists '%s' (\n" +
                    "\t id integer not null \n" +
                    "\t constraint \"%s_pk\" \n" +
                    "\t primary key autoincrement, \n" +
                    "\t Login text \n" +
                    ");", workWithGroup.nameGroup, workWithGroup.nameGroup));
            toClient.sendMessage("Группа успешно создана!");
            Message msg = buildWorkWithGroup(workWithGroup.nameGroup, "1");
            toClient.sendMessage(msg.toJson());
            disconnect();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void addClientToGroup(String nameGroup, String nickname) {
        try {
            connection();
            String Login = dataMessage.getLogin(nickname);
            ResultSet rs = stmt.executeQuery(String.format("select * from '%s'", nameGroup));
            while (rs.next()) {
                if (rs.getString("Login").equals(Login)) {
                    disconnect();
                    return;
                }
            }
            stmt.executeUpdate(String.format("INSERT INTO '%s' (Login) VALUES ('%s')",
                    nameGroup, Login));
            disconnect();
        }  catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        dataMessage.addClientToGroupList(nameGroup);
    }

    void deleteClientFromGroup(WorkWithGroup workWithGroup, ClientHandler toClient) {
        try {
            connection();
            String Login = dataMessage.getLogin(toClient.getClientName());
            stmt.executeQuery(String.format("SELECT * FROM '%s'", workWithGroup.nameGroup));
            stmt.executeUpdate(String.format("DELETE FROM '%s' WHERE Login = '%s'",
                    workWithGroup.nameGroup, Login));
            if (workWithGroup.identify.equals("0")) toClient.sendMessage("Вы отписаны от рассылки из данной группы!");
            disconnect();
        }  catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void createPassForGroup(WorkWithGroup workWithGroup, ClientHandler toClient) {
        try {
            connection();
            ResultSet resultSet = stmt.executeQuery("select * from PassGroup");
            while (resultSet.next()) {
                if (resultSet.getString("NameGroup").equals(workWithGroup.nameGroup)) {
                    disconnect();
                    return;
                }
            }
            stmt.executeUpdate(String.format("INSERT INTO PassGroup (NameGroup, PassGroup) VALUES ('%s', '%s')",
                    workWithGroup.nameGroup, workWithGroup.password));
            toClient.sendMessage("Пароль задан!");
            disconnect();
        }  catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean checkPassGroup(WorkWithGroup workWithGroup, ClientHandler toClient) {
        try {
            connection();
            ResultSet resultSet = stmt.executeQuery("select * from PassGroup");
            while (resultSet.next()) {
                if (resultSet.getString("NameGroup").equals(workWithGroup.nameGroup) &&
                        resultSet.getString("PassGroup").equals(workWithGroup.password)) {
                    Message msg = buildWorkWithGroup(workWithGroup.nameGroup, "2");
                    toClient.sendMessage(msg.toJson());
                    disconnect();
                    return true;
                }
            }
            disconnect();
        }  catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        Message msg = buildWorkWithGroup(workWithGroup.nameGroup, "3");
        toClient.sendMessage(msg.toJson());
        return false;
    }

    public void joinToGroup(WorkWithGroup workWithGroup, ClientHandler toClient) {
        Message msg = buildWorkWithGroup(workWithGroup.nameGroup, "1");
        toClient.sendMessage(msg.toJson());
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
            ClientHandler.logger.error(e.getMessage());
        }
    }

    private static void disconnect() throws SQLException {
        stmt.close();
        conn.close();
    }
}
