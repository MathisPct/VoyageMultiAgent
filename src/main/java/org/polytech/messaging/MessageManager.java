package org.polytech.messaging;

import org.polytech.agent.Agent;

public interface MessageManager {
    void sendMessage(Agent recipient, Message message);
}
