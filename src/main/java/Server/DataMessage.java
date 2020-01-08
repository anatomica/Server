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

    public DataMessage () {
        // this.clientHandler = clientHandler;
        // this.myServer = myServer;
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
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(nameClient + ".txt", true), "UTF-8"))) {
            bw.write(messageText + "\n");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void cleanFile(String nameClient) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(nameClient + ".txt", false), "UTF-8"))) {
            bw.write("");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void connection() throws ClassNotFoundException, SQLException {
        try {
            URI uri = BaseAuthService.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            String pathToDB = new File(uri).getParent() + BaseAuthService.pathInWin;
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
