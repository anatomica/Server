package Server;

import Server.auth.BaseAuthService;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataMessage {

    private static final String server_key = "AAAAbc8j05s:APA91bFtu-FuYUl2BfQ38piqy2mK4GJzPgsQp94mwbhYJFRjFBL6YRN9GFZ6tR0afwLbORmEWAlnSCkvsox3GvdJtjBfcf9AFn8YAEn8V3-tLqn9TzmlsjaLINzBAvvKXmvUSWhDnduw";
    private MyServer myServer;
    private static Connection conn;
    private static Statement stmt;
    public List<String> allClients = new ArrayList<>();
    public List<String> allClientsFromGroup = new ArrayList<>();
    public String pathToHistory;
    public File fileHistory;
    public String pathToHistoryWIN;
    public String pathToHistoryLINUX;

    public DataMessage (MyServer myServer) {
        this.myServer = myServer;
    }

    public void checkPath() {
        try {
            URI uri = ServerApp.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            pathToHistory = new File(uri).getParent();
            pathToHistoryWIN = pathToHistory + "\\";
            pathToHistoryLINUX = pathToHistory + "/";
            System.out.println(pathToHistory);
        } catch (URISyntaxException e) {
            System.out.println(e.getMessage());
        }
    }

    public void addClientToList(){
        try {
            connection();
            allClients.clear();
            ResultSet rs = stmt.executeQuery("select * from LoginData");
            while (rs.next()) {
                allClients.add(rs.getString("Nick"));
            }
            disconnect();
        }  catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void addClientToGroupList(String nameGroup) {
        try {
            connection();
            allClientsFromGroup.clear();
            ResultSet rs = stmt.executeQuery(String.format("SELECT * FROM '%s'", nameGroup));
            while (rs.next()) {
                String Nick = getNick(rs.getString("Login"));
                allClientsFromGroup.add(Nick);
            }
            disconnect();
        }  catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void checkMessageFileOnStart() {
        for (int i = 0; i < allClients.size(); i++) {
            createFile(allClients.get(i));
        }
    }

    public void writeMessageToFile(List<String> Clients, String message) {
        System.out.println(Clients + " " + message);
        for (String Client : Clients) {
            writeToFile(Client, message);
        }
    }

    private void writeToFile(String nameClient, String messageText) {
        FCM.send_FCM_Notification(getToken(nameClient), server_key, messageText);
        createFile(nameClient);
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(fileHistory, true), "UTF-8"))) {
            bw.write(messageText + "\n");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void createFile(String nameClient) {
        try {
            fileHistory = new File(pathToHistoryLINUX + getID(nameClient) + ".txt");
            if (fileHistory.createNewFile()) System.out.println("Файл истории " + nameClient + " номер: " + getID(nameClient) + " создан!");
            else System.out.println("Файл истории " + nameClient + " номер: " + getID(nameClient) + " ранее создан и найден!");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void cleanFile(String nameClient) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(pathToHistoryLINUX + getID(nameClient) + ".txt", false), "UTF-8"))) {
            bw.write("");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public String getID(String nick){
        String ID = null;
        try {
            connection();
            ResultSet rs = stmt.executeQuery(String.format("SELECT id from LoginData where Nick = '%s'", nick));
            ID = rs.getString("id");
            disconnect();
        }  catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return ID;
    }

    public String getNick(String login){
        String Nick = null;
        try {
            connection();
            ResultSet rs = stmt.executeQuery(String.format("SELECT Nick from LoginData where Login = '%s'", login));
            Nick = rs.getString("Nick");
            disconnect();
        }  catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Nick;
    }

    public String getLogin(String nick){
        String Login = null;
        try {
            connection();
            ResultSet rs = stmt.executeQuery(String.format("SELECT Login from LoginData where Nick = '%s'", nick));
            Login = rs.getString("Login");
            disconnect();
        }  catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Login;
    }

    public String getToken(String nick){
        String token = null;
        try {
            connection();
            ResultSet rs = stmt.executeQuery(String.format("SELECT Token from LoginData where Nick = '%s'", nick));
            token = rs.getString("Token");
            disconnect();
        }  catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return token;
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
