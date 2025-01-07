package org.polytech.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Agent {
    protected static HashMap<Agent, List<Message>> messagesQueue;
    protected static List<Provider> providers;
    protected static List<Buyer> buyers;
    protected int interest;

    static {
        messagesQueue = new HashMap<>();
        providers = new ArrayList<>();
        buyers = new ArrayList<>();
    }

    public Agent() {
        this.interest = new Random().nextInt(1, 10 + 1); // between 1 and 10
    }

    public int getInterest() {
        return interest;
    }

    /**
     * To distribute a message to a recipient
     *
     * @param recipient (destinataire) which will receive the message
     * @param message   the message
     */
    public static void publishToMessageQueue(Agent recipient, Message message) {
        List<Message> messages = Agent.messagesQueue.getOrDefault(recipient, new ArrayList<>());
        messages.add(message);
        Agent.messagesQueue.put(recipient, messages);
    }

    protected Message waitUntilReceiveMessage(Agent receiver) {
        while (true) {
            List<Message> receiverMessages = Agent.messagesQueue.get(receiver);
            if (receiverMessages != null && !receiverMessages.isEmpty()) {
                synchronized (receiverMessages) {
                    for (Message message : receiverMessages) {
                        if (!message.isRead()) {
                            message.setRead(true);
                            receiverMessages.remove(message);
                            return message;
                        }
                    }
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
    }
}
