package Server;

import Server.auth.AuthService;
import Server.auth.BaseAuthService;
import Server.gson.GroupMessage;
import Server.gson.Message;
import Server.gson.PrivateMessage;
import Server.gson.PublicMessage;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

class MyServer {

    private static final int PORT = 8189;
    private final AuthService authService = new BaseAuthService();
    private DataMessage dataMessage = new DataMessage(this);
    private DataBase dataBase = new DataBase(this, dataMessage);
    public List<ClientHandler> clients = new ArrayList<>();
    private ServerSocket serverSocket = null;

    MyServer() {
        dataMessage.checkPath();
        dataMessage.addClientToList();
        dataMessage.checkMessageFileOnStart();
        ClientHandler.logger.info("Сервер запущен!");
        try {
            serverSocket = new ServerSocket(PORT);
            authService.start();
            while (true) {
                ClientHandler.logger.info("Ожидание подключения клиентов ...");
                Socket socket = serverSocket.accept();
                ClientHandler.logger.info("Клиент подключен!");
                new ClientHandler(socket, this, dataMessage, dataBase);
            }
        } catch (IOException e) {
            ClientHandler.logger.error("Ошибка в работе сервера. Причина: " + e.getMessage());
            e.printStackTrace();
        } finally {
            shutdownServer();
        }
    }

    private void broadcastClientsList() {
        List<String> nicknames = new ArrayList<>();
        nicknames.add("Пользователи группы:");
        Collections.sort(dataMessage.allClients);
        nicknames.addAll(dataMessage.allClients);
        Message msg = Message.createClientList(nicknames, "Сервер");
        for (ClientHandler client : clients) {
            client.sendMessage(msg.toJson());
        }
    }

    public void broadcastClientsListOnline() {
        List<String> nicknames = new ArrayList<>();
        for (ClientHandler client : clients) {
            nicknames.add(client.getClientName());
        }
        Message msg = Message.createClientList(nicknames, "Сервер");
        for (ClientHandler client : clients) {
            client.sendMessage(msg.toJson());
        }
    }

    public void sendClientsList(String nameGroup, String toNick) {
        List<String> nicknames = new ArrayList<>();
        nicknames.add("Пользователи группы:");
        dataMessage.addClientToGroupList(nameGroup);
        Collections.sort(dataMessage.allClientsFromGroup);
        nicknames.addAll(dataMessage.allClientsFromGroup);
        Message msg = Message.createClientList(nicknames, toNick);
        for (ClientHandler client : clients) {
            client.sendMessage(msg.toJson());
        }
    }

    synchronized void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastClientsListOnline();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sendFileToClient(clientHandler);
    }

    synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastClientsListOnline();
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

    synchronized void privateMessage(String message, String nick, ClientHandler fromClient) {
        List<String> client = new ArrayList<>();
        for (ClientHandler clientHandler : clients) {
            if (clientHandler.getClientName().equals(nick)) {
                Message msg = buildPrivateMessage(message, nick, fromClient.getClientName());
                clientHandler.sendMessage(msg.toJson());
                return;
            }
        }
        client.add(nick);
        if (dataMessage.getID(nick) == null) fromClient.sendMessage("Сервер: Этот клиент не зарегистрирован!");
        else dataMessage.writeMessageToFile(client, "0 " + fromClient.getClientName() + " : " + message);
    }

    synchronized void privateMessage(String message) {
        List<String> client = new ArrayList<>();
        for (ClientHandler clientHandler : clients) {
            if (clientHandler.getClientName().equals("Макс")) {
                Message msg = buildPrivateMessage(message, "Макс", "Сервер");
                clientHandler.sendMessage(msg.toJson());
                return;
            }
        }
        client.add("Макс");
        dataMessage.writeMessageToFile(client, "0 " + "Сервер" + " : " + message);
    }

    synchronized void groupMessage(String message, String nameGroup, ClientHandler fromClient, ClientHandler... unfilteredClients) {
        List<ClientHandler> unfiltered = Arrays.asList(unfilteredClients);
        List<String> allClientsFromGroup = dataMessage.allClientsFromGroup;
        List<String> tmpClients = new ArrayList<>(allClientsFromGroup);
        for (String allClients : allClientsFromGroup) {
            for (ClientHandler client : clients) {
                if (allClients.equals(client.getClientName())) {
                    tmpClients.remove(client.getClientName());
                    if (!unfiltered.contains(client)) {
                        Message msg = buildGroupMessage(message, nameGroup, fromClient.getClientName());
                        client.sendMessage(msg.toJson());
                    }
                }
            }
        }
        dataMessage.writeMessageToFile(tmpClients, nameGroup + " " + fromClient.getClientName() + " : " + message);
        dataMessage.addClientToGroupList(nameGroup);
    }

    synchronized void broadcastMessage(String message, ClientHandler fromClient, ClientHandler... unfilteredClients) {
        List<ClientHandler> unfiltered = Arrays.asList(unfilteredClients);
        List<String> allClients = dataMessage.allClients;
        for (ClientHandler client : clients) {
            allClients.remove(client.getClientName());
            if (!unfiltered.contains(client)) {
                Message msg = buildPublicMessage(message, fromClient.getClientName());
                client.sendMessage(msg.toJson());
            }
        }
        dataMessage.writeMessageToFile(allClients, fromClient.getClientName() + " : " + message);
        dataMessage.addClientToList();
    }

    public void sendFileToClient(ClientHandler clientHandler) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(dataMessage.pathToHistoryLINUX +
                            dataMessage.getID(clientHandler.getClientName()) + ".txt"), "UTF-8"));
            int regexIsNumber = 0;
            String tmp;
            StringBuilder group;
            String name;
            String regex = ":";
            String message;
            int count = 0;
            while ((tmp = br.readLine()) != null) {
                for (int i = 0; i < tmp.split("\\s+").length; i++) {
                    if (regex.equals(tmp.split("\\s+")[i])) {
                        regexIsNumber = i;
                        break;
                    }
                }
                group = new StringBuilder();
                for (int i = 0; i < regexIsNumber - 1; i++) {
                    group.append(tmp.split("\\s+")[i]);
                    if (i == regexIsNumber - 2) break;
                    else group.append(" ");
                }
                name = tmp.split("\\s+")[regexIsNumber - 1];
                message = tmp.split("\\s+", regexIsNumber + 2)[regexIsNumber + 1];
                if (count == 0) {
                    clientHandler.sendMessage("Новые сообщения от " + name);
                    count++;
                }
                TimeUnit.MILLISECONDS.sleep(300);
                if (Objects.equals(group.toString(), "0")) {
                    Message msg = buildPrivateMessage(message, clientHandler.getClientName(), name);
                    clientHandler.sendMessage(msg.toJson());
                    ClientHandler.logger.info("From: " + name + "; Send message from file: " + message + "; To: " + clientHandler.getClientName());
                } else {
                    Message msg = buildGroupMessage(message, group.toString(), name);
                    clientHandler.sendMessage(msg.toJson());
                    ClientHandler.logger.info("From: " + name + "; Send message from file: " + message + "; To Group: " + group.toString());
                }
            }
            dataMessage.cleanFile(clientHandler.getClientName());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Message buildPrivateMessage(String message, String selectedNickname, String nickName) {
        PrivateMessage msg = new PrivateMessage();
        msg.from = nickName;
        msg.to = selectedNickname;
        msg.message = message;
        return Message.createPrivate(msg);
    }

    private Message buildGroupMessage(String message, String nameGroup, String sender) {
        GroupMessage msg = new GroupMessage();
        msg.from = sender;
        msg.message = message;
        msg.nameGroup = nameGroup;
        return Message.createGroup(msg);
    }

    private Message buildPublicMessage(String message, String nickName) {
        PublicMessage msg = new PublicMessage();
        msg.from = nickName;
        msg.message = message;
        return Message.createPublic(msg);
    }

    private void shutdownServer() {
        try {
            authService.stop();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
