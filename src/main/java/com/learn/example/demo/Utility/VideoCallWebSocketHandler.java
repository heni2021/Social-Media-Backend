package com.learn.example.demo.Utility;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class VideoCallWebSocketHandler extends TextWebSocketHandler {

    private static final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Add the new session to the set when a connection is established
        sessions.add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        // Handle incoming text messages (e.g., WebRTC signaling)

        // You might want to add more complex logic for signaling
        // For simplicity, this example just echoes the message back to all participants
        String receivedMessage = message.getPayload();
        for (WebSocketSession participant : sessions) {
            if (participant.isOpen()) {
                participant.sendMessage(new TextMessage(receivedMessage));
            }
        }
    }

    public static void notifyParticipants(String roomId, String message) throws IOException {
        for (WebSocketSession participant : sessions) {
            if (participant.isOpen()) {
                // Use a message template or customize the message as needed
                // For simplicity, this example sends the same message to all participants
                // You might want to send different messages based on the role (initiator, receiver, etc.)
                // Example: sendMessageToUser(participant, message);
                participant.sendMessage(new TextMessage(message));
            }
        }
    }
}
