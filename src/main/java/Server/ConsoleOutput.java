package Server;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class ConsoleOutput {
    
    public static void setup() {
        // Устанавливаем кодировку файлов в UTF-8
        System.setProperty("file.encoding", "UTF-8");
        
        // Для Windows консоли устанавливаем кодовую страницу UTF-8
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            try {
                // Пытаемся установить UTF-8 кодовую страницу через cmd
                // Используем синхронный вызов для надежности
                String[] command = {"cmd.exe", "/c", "chcp", "65001", ">nul", "2>&1"};
                ProcessBuilder pb = new ProcessBuilder(command);
                pb.redirectErrorStream(true);
                Process process = pb.start();
                
                // Ждем завершения процесса
                int exitCode = process.waitFor();
                
                // Небольшая задержка для применения изменений в консоли
                if (exitCode == 0) {
                    Thread.sleep(100);
                }
            } catch (Exception e) {
                // Если не получилось установить кодовую страницу, продолжаем
                // Пользователю нужно будет установить её вручную через chcp 65001
            }
        }
        
        // Устанавливаем PrintStream с UTF-8
        // Это важно для правильного вывода в консоль
        try {
            PrintStream utf8Out = new PrintStream(System.out, true, "UTF-8");
            PrintStream utf8Err = new PrintStream(System.err, true, "UTF-8");
            System.setOut(utf8Out);
            System.setErr(utf8Err);
        } catch (UnsupportedEncodingException e) {
            // Если UTF-8 не поддерживается, используем системную кодировку по умолчанию
            // В этом случае русский текст может отображаться неправильно
        }
    }
}

