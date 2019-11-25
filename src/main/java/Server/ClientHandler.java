package Server;

import Server.auth.BaseAuthService;
import Server.gson.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.*;

class ClientHandler {

    private MyServer myServer;
    private String clientName;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private ChangeNick changeNick;
    private static Connection conn;
    private static Statement stmt;

    ClientHandler(Socket socket, MyServer myServer) {
        try {
            this.socket = socket;
            this.myServer = myServer;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    while (true) {
                        if (authentication()) {
                            break;
                        }
                    }
                    readMessages();
                } catch (IOException | SQLException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка создания подключения к клиенту!", e);
        }
    }

    private void readMessages() throws IOException, SQLException {
        while (true) {
            String clientMessage = in.readUTF();
            System.out.printf("Сообщение: '%s' от клиента: %s%n", clientMessage, clientName);
            Message m = Message.fromJson(clientMessage);
            switch (m.command) {
                case CHANGE_NICK:
                    changeNick = m.changeNick;

                    try {
                        connection();
                    }  catch (SQLException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    if (verifyNick()) {
                        stmt.executeUpdate(String.format("UPDATE LoginData SET Nick = '%s' WHERE Nick = '%s'",
                                changeNick.nick, clientName));
                        myServer.broadcastMessage(clientName + " сменил(а) ник, теперь он(а): " + changeNick.nick);
                        myServer.unsubscribe(this);
                        clientName = changeNick.nick;
                        myServer.subscribe(this);
                    } else {
                        sendMessage("Данный Ник занят! \nПожалуйста, выберите другой Ник!");
                    }
                    disconect();
                    break;
                case PUBLIC_MESSAGE:
                    PublicMessage publicMessage = m.publicMessage;
                    myServer.broadcastMessage(publicMessage.from + ": " + publicMessage.message, this);
                    break;
                case PRIVATE_MESSAGE:
                    PrivateMessage privateMessage = m.privateMessage;
                    myServer.privateMessage(privateMessage.from + " [private]: " + privateMessage.message, privateMessage.to, ClientHandler.this);
                    break;
                case END:
                    return;
            }
        }
    }

    private boolean authentication() throws IOException, SQLException {
        String clientMessage = in.readUTF();
        Message message = Message.fromJson(clientMessage);
        if (message.command == Command.AUTH_MESSAGE) {
            AuthMessage authMessage = message.authMessage;
            String login = authMessage.login;
            String password = authMessage.password;
            String nick = myServer.getAuthService().getNickByLoginPass(login, password);
            BaseAuthService.disconect();
            if (nick == null) {
                sendMessage("Неверные логин/пароль!");
                return false;
            }
            if (myServer.isNickBusy(nick)) {
                sendMessage("Учетная запись уже используется!");
                return false;
            }
            sendMessage("/authok " + nick);
            clientName = nick;
            myServer.broadcastMessage(clientName + " онлайн!");
            myServer.subscribe(this);
        }
        return true;
    }

    private boolean verifyNick() throws SQLException {
        ResultSet rs = stmt.executeQuery("select * from LoginData");
        while (rs.next()) {
            ResultSetMetaData dataInBase = rs.getMetaData();
            for (int i = 1; i <= dataInBase.getColumnCount(); i++) {
                if (rs.getString("Nick").equals(changeNick.nick)) {
                    return false;
                }
            }
        }
        return true;
    }

    void sendMessage(String message)  {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            System.err.println("Ошибка отправки сообщения пользователю: " + clientName + " : " + message);
            e.printStackTrace();
        }
    }

    String getClientName() {
        return clientName;
    }

    private static void connection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        conn = DriverManager.getConnection("jdbc:sqlite::resource:LoginData.db");
        stmt = conn.createStatement();
    }

    private static void disconect() throws SQLException {
        stmt.close();
        conn.close();
    }

    private void closeConnection() {
        myServer.unsubscribe(this);
        if (clientName != null)
            myServer.broadcastMessage(clientName + " офлайн!");
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Failed to close socket!");
            e.printStackTrace();
        }
    }
}
