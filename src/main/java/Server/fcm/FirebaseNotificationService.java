package Server.fcm;

import Server.ClientHandler;
import Server.auth.BaseAuthService;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class FirebaseNotificationService {

    public static String pathInWin = "\\serviceAccountKey.json";
    public static String pathInLinux = "/serviceAccountKey.json";

    public void initializeFirebase() {
        FileInputStream serviceAccount;
        try {
            URI uri = BaseAuthService.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            String pathToKeys = new File(uri).getParent() + pathInLinux;
            serviceAccount = new FileInputStream(pathToKeys);
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            ClientHandler.logger.error("Ошибка инициализации Firebase!");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
