package Server;

import Server.auth.BaseAuthService;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataMessage {

    private MyServer myServer;
    private ClientHandler clientHandler;
    private static Connection conn;
    private static Statement stmt;
    public List<String> allClients = new ArrayList<>();
    public String pathToHistory;
    public File fileHistory;
    public String pathToHistoryWIN;
    public String pathToHistoryLINUX;

    public DataMessage (MyServer myServer) {
        this.myServer = myServer;
        // this.clientHandler = clientHandler;
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

    public void checkMessageFileOnStart() {
        for (int i = 0; i < allClients.size(); i++) {
            createFile(allClients.get(i));
        }
    }

    public void writeMessageToFile(List<String> filteredClients, String message) {
        System.out.println(filteredClients + " " + message);
        for (String filteredClient : filteredClients) {
            if (!message.endsWith("лайн!")) {
                if (!message.startsWith("{\"clientListMessage\":"))
                    writeToFile(filteredClient, message);
            }
        }
    }

    private void writeToFile(String nameClient, String messageText) {
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
            fileHistory = new File(pathToHistoryLINUX + nickToID(nameClient) + ".txt");
            if (fileHistory.createNewFile()) System.out.println("Файл истории " + nameClient + " номер: " + nickToID(nameClient) + " создан!");
            else System.out.println("Файл истории " + nameClient + " номер: " + nickToID(nameClient) + " ранее создан и найден!");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void cleanFile(String nameClient) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(pathToHistoryLINUX + nickToID(nameClient) + ".txt", false), "UTF-8"))) {
            bw.write("");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public String nickToID(String nick){
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
