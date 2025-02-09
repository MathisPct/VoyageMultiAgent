package org.polytech.messaging;

import org.polytech.agent.Agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessageManagerSimpleImpl implements MessageManager {
    private HashMap<AgentCouple, List<Message>> agentMessageHashMap = new HashMap<>();

    public HashMap<AgentCouple, List<Message>> getAgentMessageHashMap() {
        return agentMessageHashMap;
    }

    public synchronized void sendMessage(Agent recipient, Message message) {
        Agent firstAgent, secondAgent;
        if (message.getIssuer() instanceof org.polytech.agent.Provider) {
            firstAgent = message.getIssuer();
            secondAgent = recipient;
        } else if (recipient instanceof org.polytech.agent.Provider) {
            firstAgent = recipient;
            secondAgent = message.getIssuer();
        } else {
            throw new IllegalArgumentException("At least one agent must be a Provider");
        }
        AgentCouple agentCouple = new AgentCouple(firstAgent, secondAgent);

        List<Message> messages = agentMessageHashMap.computeIfAbsent(agentCouple, k -> new ArrayList<>());
        messages.add(message);
        recipient.receiveMessage(message);
    }

    public void reset() {
        this.agentMessageHashMap.clear();
    }
}
