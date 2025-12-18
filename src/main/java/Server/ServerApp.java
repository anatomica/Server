package Server;

public class ServerApp {

    public static void main(String[] args) {
        // Устанавливаем кодировку файлов в UTF-8
        System.setProperty("file.encoding", "UTF-8");
        
        // Настраиваем вывод в консоль с правильной кодировкой
        ConsoleOutput.setup();

        new MyServer();
    }
}
