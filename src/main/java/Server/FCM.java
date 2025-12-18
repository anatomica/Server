package Server;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

class FCM {

    /**
     *
     * Method to send a push notification to Android FireBased Cloud messaging
     * Server.
     *
     * @param tokenId Generated and provided from Android Client Developer
     * @param message which contains actual information.
     *
     */

    static void send_FCM_Notification(String tokenId, String message) {
        if (message.startsWith("0")) {
            StringBuilder stringBuilder = new StringBuilder(message);
            stringBuilder.delete(0, 1);
            message = stringBuilder.toString();
        }

        try {
            Message messageToSend = Message.builder()
                    .setToken(tokenId)
                    .setNotification(Notification.builder()
                            .setTitle("Непрочитанные сообщения:")
                            .setBody(message)
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(messageToSend);
            Server.ClientHandler.logger.info("Successfully sent FCM message: " + response);

        } catch (FirebaseMessagingException e) {
            Server.ClientHandler.logger.error("Error sending FCM message: " + e.getMessage());
        }
    }
}
