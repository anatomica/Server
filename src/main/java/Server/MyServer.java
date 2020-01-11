package Server;

import Server.auth.AuthService;
import Server.auth.BaseAuthService;
import Server.gson.Message;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

class MyServer {

    private static final int PORT = 8189;
    private final AuthService authService = new BaseAuthService();
    private DataMessage dataMessage = new DataMessage(this);
    public List<ClientHandler> clients = new ArrayList<>();
    private ServerSocket serverSocket = null;

    MyServer() {
        // this.dataMessage = dataMessage;
        dataMessage.checkPath();
        dataMessage.addClientToList();
        dataMessage.checkMessageFileOnStart();
        System.out.println("Сервер запущен!");
        try {
            serverSocket = new ServerSocket(PORT);
            authService.start();
            while (true) {
                System.out.println("Ожидание подключения клиентов ...");
                Socket socket = serverSocket.accept();
                System.out.println("Клиент подключен!");
                new ClientHandler(socket, this, dataMessage);
            }
        } catch (IOException e) {
            System.err.println("Ошибка в работе сервера. Причина: " + e.getMessage());
            e.printStackTrace();
        } finally {
            shutdownServer();
        }
    }

    private void shutdownServer() {
        try {
            authService.stop();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastClientsList();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sendFileToClient(clientHandler);
    }

    public void sendFileToClient(ClientHandler clientHandler) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(dataMessage.pathToHistoryLINUX +
                            dataMessage.nickToID(clientHandler.getClientName()) + ".txt"), "UTF-8"));
            String tmp;
            int count = 0;
            while ((tmp = br.readLine()) != null) {
                if (count == 0) {
                    // privateMessage("Новые сообщения:", clientHandler.getClientName(), clientHandler);
                    clientHandler.sendMessage("Новые сообщения:");
                    count++;
                }
                // privateMessage(tmp, clientHandler.getClientName(), clientHandler);
                TimeUnit.MILLISECONDS.sleep(500);
                clientHandler.sendMessage(tmp);
                System.out.println(" To: " + clientHandler.getClientName() + " Message: " + tmp);
            }
            dataMessage.cleanFile(clientHandler.getClientName());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void broadcastClientsList() {
        List<String> nicknames = new ArrayList<>();
        nicknames.add("< ДЛЯ ВСЕХ >");
        for (ClientHandler client : clients) {
            nicknames.add(client.getClientName());
        }

        Message message = Message.createClientList(nicknames);
        broadcastMessage(message.toJson());
    }

    synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastClientsList();
    }

    AuthService getAuthService() {
        return authService;
    }

    synchronized boolean isNickBusy(String nick) {
        for (ClientHandler client : clients) {
            if (client.getClientName().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    synchronized void broadcastMessage(String message, ClientHandler... unfilteredClients) {
        List<ClientHandler> unfiltered = Arrays.asList(unfilteredClients);
        List<String> filteredClients = dataMessage.allClients;
        for (ClientHandler client : clients) {
            filteredClients.remove(client.getClientName());
            if (!unfiltered.contains(client)) {
                client.sendMessage(message);
            }
        }
        // PushNotifactionHelper.sendPushNotification(filteredClients, message);
        dataMessage.writeMessageToFile(filteredClients, message);
        dataMessage.addClientToList();
    }

    synchronized void privateMessage(String message, String nick, ClientHandler sender) {
        for (int i = 0; i < clients.size(); i++) {
            if (clients.get(i).getClientName().equals(nick)) {
                clients.get(i).sendMessage(message);
                return;
            }
        }
        sender.sendMessage("Сервер: Этот клиент не подключен!");
    }
}
