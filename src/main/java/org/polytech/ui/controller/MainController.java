package org.polytech.ui.controller;

import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import org.polytech.agent.*;
import org.polytech.agent.constraints.BuyerConstraints;
import org.polytech.agent.strategy.InterestBasedBuyerStrategy;
import org.polytech.agent.strategy.InterestBasedProviderStrategy;
import org.polytech.messaging.AgentCouple;
import org.polytech.messaging.Message;
import org.polytech.messaging.MessageManagerSimpleImpl;

import java.net.URL;
import java.util.*;

public class MainController implements Initializable {
    @FXML
    private ListView<AgentCouple> conversationsList;
    @FXML
    private ListView<Message> messagesList;

    private Provider provider;
    private final List<Buyer> buyers = new ArrayList<>();
    private MessageManagerSimpleImpl messageManager = new MessageManagerSimpleImpl();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Setup du Provider
        provider = new Provider(messageManager, List.of(
                new Ticket(85, 85 * 0.9, "Paris", "Amsterdam", Company.TRANSAVIA),
                new Ticket(75, 75 * 0.9, "Paris", "Amsterdam", Company.KLM),
                new Ticket(70, 70 * 0.9, "Paris", "Amsterdam", Company.KLM),
                new Ticket(80, 80 * 0.9, "Paris", "Amsterdam", Company.TRANSAVIA),
                new Ticket(75, 75 * 0.9, "Paris", "Lyon", Company.TRANSAVIA),
                new Ticket(85, 85 * 0.9, "Paris", "Lille", Company.TRANSAVIA),
                new Ticket(90, 90 * 0.9, "Paris", "Bordeaux", Company.AIR_FRANCE),
                new Ticket(100, 100 * 0.9, "Amsterdam", "Suede", Company.KLM)
        ), 5);
        provider.setNegociationStrategy(new InterestBasedProviderStrategy());

        // Fabrication de quelques Buyers
        createBuyer("Buyer1", 7, 75, List.of(Company.KLM), List.of("Amsterdam"));
        createBuyer("Buyer2", 6, 75, List.of(Company.KLM), List.of("Amsterdam"));
        createBuyer("Buyer3", 6, 85, List.of(), List.of("Suede"));

        // Configuration de l'affichage des conversations
        conversationsList.setCellFactory(param -> new ListCell<AgentCouple>() {
            @Override
            protected void updateItem(AgentCouple item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("Conversation: %s - %s", 
                        item.agent1().getName(), 
                        item.agent2().getName()));
                }
            }
        });

        // Configuration de l'affichage des messages
        messagesList.setCellFactory(param -> new ListCell<Message>() {
            @Override
            protected void updateItem(Message item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("[%s] %s -> %s: %.2f€ (%s)", 
                        item.getDateEmission().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")),
                        item.getIssuer().getName(),
                        item.getReceiver().getName(),
                        item.getOffer().getPrice(),
                        item.getOffer().getTypeOffer()));
                }
            }
        });

        // Gestionnaire d'événements pour la sélection d'une conversation
        conversationsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                messagesList.getItems().clear();
                List<Message> messages = messageManager.getAgentMessageHashMap().get(newVal);
                if (messages != null) {
                    List<Message> sortedMessages = new ArrayList<>(messages);
                    sortedMessages.sort(Comparator.comparing(Message::getDateEmission));
                    messagesList.getItems().addAll(sortedMessages);
                }
            }
        });

        // Timer pour mettre à jour la liste des conversations régulièrement
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                javafx.application.Platform.runLater(() -> updateConversationsList());
            }
        }, 0, 1000); // Met à jour toutes les secondes
    }

    private void updateConversationsList() {
        Set<AgentCouple> currentConversations = messageManager.getAgentMessageHashMap().keySet();
        if (!currentConversations.equals(new HashSet<>(conversationsList.getItems()))) {
            conversationsList.getItems().setAll(currentConversations);
        }
    }

    /**
     * Pour simplifier la création d'un Buyer, factorisée en méthode.
     */
    private void createBuyer(String name, int interest, int budget,
                             List<Company> allowedCompanies, List<String> destinations) {
        BuyerConstraints constraints = new BuyerConstraints(budget);
        allowedCompanies.forEach(constraints::addAllowedCompany);
        destinations.forEach(constraints::addDestination);

        Buyer buyer = new Buyer(messageManager ,constraints, name, interest);
        buyer.setNegociationStrategy(new InterestBasedBuyerStrategy());
        buyers.add(buyer);
    }

    /**
     * Lance la négociation en démarrant les threads des agents
     */
    @FXML
    public void launchNegociation(ActionEvent actionEvent) {
        Thread providerThread = new Thread(provider);
        providerThread.start();

        for (Buyer b : buyers) {
            new Thread(b).start();
        }
    }
}