package org.polytech.ui.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.polytech.agent.*;
import org.polytech.agent.constraints.BuyerConstraints;
import org.polytech.agent.strategy.InterestBasedBuyerStrategy;
import org.polytech.agent.strategy.InterestBasedProviderStrategy;
import org.polytech.messaging.AgentCouple;
import org.polytech.messaging.Message;
import org.polytech.messaging.MessageManagerSimpleImpl;

import java.io.File;
import java.net.URL;
import java.util.*;

public class MainController implements Initializable {
    @FXML private ListView<AgentCouple> conversationsList;
    @FXML private ListView<Message> messagesList;
    @FXML private ListView<Provider> providersList;
    @FXML private ListView<Buyer> buyersList;

    private final ObservableList<Provider> providers = FXCollections.observableArrayList();
    private final ObservableList<Buyer> buyers = FXCollections.observableArrayList();
    private MessageManagerSimpleImpl messageManager = new MessageManagerSimpleImpl();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize providers and buyers lists from Agent static lists
        Provider provider = new Provider(messageManager, List.of(
                new Ticket(85, 85 * 0.9, "Paris", "Amsterdam", Company.TRANSAVIA),
                new Ticket(75, 75 * 0.9, "Paris", "Amsterdam", Company.KLM),
                new Ticket(70, 70 * 0.9, "Paris", "Amsterdam", Company.KLM),
                new Ticket(80, 80 * 0.9, "Paris", "Amsterdam", Company.TRANSAVIA),
                new Ticket(75, 75 * 0.9, "Paris", "Lyon", Company.TRANSAVIA),
                new Ticket(85, 85 * 0.9, "Paris", "Lille", Company.TRANSAVIA),
                new Ticket(90, 90 * 0.9, "Paris", "Bordeaux", Company.AIR_FRANCE),
                new Ticket(100, 100 * 0.9, "Amsterdam", "Stockholm ", Company.KLM)
        ), 5, "Provider 1");
        provider.setNegociationStrategy(new InterestBasedProviderStrategy());

        providers.addAll(Agent.getProviders());

        BuyerConstraints buyerConstraints1 = new BuyerConstraints(75);
        buyerConstraints1.addAllowedCompany(Company.KLM);
        buyerConstraints1.addDestination("Amsterdam");
        Buyer buyer1 = new Buyer(messageManager, buyerConstraints1, "Acheteur 1", 6);
        buyer1.setNegociationStrategy(new InterestBasedBuyerStrategy());

        BuyerConstraints buyerConstraints2 = new BuyerConstraints(75);
        buyerConstraints2.addAllowedCompany(Company.KLM);
        buyerConstraints2.addDestination("Amsterdam");
        Buyer buyer2 = new Buyer(messageManager, buyerConstraints2, "Acheteur 2", 6);
        buyer2.setNegociationStrategy(new InterestBasedBuyerStrategy());

        BuyerConstraints buyerConstraints3 = new BuyerConstraints(75);
        buyerConstraints3.addAllowedCompany(Company.KLM);
        buyerConstraints3.addDestination("Stockholm");
        Buyer buyer3 = new Buyer(messageManager, buyerConstraints3, "Acheteur 3", 7);
        buyer3.setNegociationStrategy(new InterestBasedBuyerStrategy());

        buyers.addAll(Agent.getBuyers());
        
        setupListViews();
        setupMessageDisplay();
        startConversationUpdateTimer();
    }

    private void setupListViews() {
        providersList.setItems(providers);
        buyersList.setItems(buyers);

        providersList.setCellFactory(lv -> new ListCell<Provider>() {
            @Override
            protected void updateItem(Provider provider, boolean empty) {
                super.updateItem(provider, empty);
                if (empty || provider == null) {
                    setGraphic(null);
                } else {
                    HBox cell = new HBox(10);
                    cell.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    Label nameLabel = new Label(provider.getName());
                    
                    Button editButton = new Button("Editer");
                    editButton.setOnAction(e -> showEditProviderDialog(provider));
                    
                    Button deleteButton = new Button("Supprimer");
                    deleteButton.setOnAction(e -> {
                        Agent.removeProvider(provider);
                        providers.remove(provider);
                    });
                    
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    
                    cell.getChildren().addAll(nameLabel, spacer, editButton, deleteButton);
                    setGraphic(cell);
                }
            }
        });

        buyersList.setCellFactory(lv -> new ListCell<Buyer>() {
            @Override
            protected void updateItem(Buyer buyer, boolean empty) {
                super.updateItem(buyer, empty);
                if (empty || buyer == null) {
                    setGraphic(null);
                } else {
                    HBox cell = new HBox(10);
                    cell.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    Label nameLabel = new Label(buyer.getName());
                    
                    Button editButton = new Button("Editer");
                    editButton.setOnAction(e -> showEditBuyerDialog(buyer));
                    
                    Button deleteButton = new Button("Supprimer");
                    deleteButton.setOnAction(e -> {
                        Agent.removeBuyer(buyer);
                        buyers.remove(buyer);
                    });
                    
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    
                    cell.getChildren().addAll(nameLabel, spacer, editButton, deleteButton);
                    setGraphic(cell);
                }
            }
        });

        // Setup conversation display
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
    }

    private void setupMessageDisplay() {
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
    }

    private void startConversationUpdateTimer() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                javafx.application.Platform.runLater(() -> updateConversationsList());
            }
        }, 0, 1000);
    }

    private void updateConversationsList() {
        Set<AgentCouple> currentConversations = messageManager.getAgentMessageHashMap().keySet();
        if (!currentConversations.equals(new HashSet<>(conversationsList.getItems()))) {
            conversationsList.getItems().setAll(currentConversations);
        }
    }

    private void showEditProviderDialog(Provider provider) {
        try {
            URL fxmlUrl = getClass().getResource("/provider-dialog.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            ProviderDialogController controller = new ProviderDialogController();
            loader.setController(controller);
            DialogPane dialogPane = loader.load();
            
            Dialog<Provider> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Modifier le fournisseur");
            
            controller.setMessageManager(messageManager);
            controller.setProviderToEdit(provider);
            controller.setDialog(dialog);

            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    return controller.getResult();
                }
                return null;
            });

            dialog.showAndWait().ifPresent(updatedProvider -> {
                if (updatedProvider != null) {
                    int index = providers.indexOf(provider);
                    if (index >= 0) {
                        providers.set(index, updatedProvider);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, 
                "Erreur lors de l'ouverture du dialogue: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void showEditBuyerDialog(Buyer buyer) {
        try {
            URL fxmlUrl = getClass().getResource("/buyer-dialog.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            BuyerDialogController controller = new BuyerDialogController();
            loader.setController(controller);
            DialogPane dialogPane = loader.load();
            
            Dialog<Buyer> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Modifier l'acheteur");
            
            controller.setMessageManager(messageManager);
            controller.setBuyerToEdit(buyer);
            controller.setDialog(dialog);

            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    return controller.getResult();
                }
                return null;
            });

            dialog.showAndWait().ifPresent(updatedBuyer -> {
                if (updatedBuyer != null) {
                    int index = buyers.indexOf(buyer);
                    if (index >= 0) {
                        buyers.set(index, updatedBuyer);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, 
                "Erreur lors de l'ouverture du dialogue: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void showAddProviderDialog() {
        try {
            URL fxmlUrl = getClass().getResource("/provider-dialog.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            ProviderDialogController controller = new ProviderDialogController();
            loader.setController(controller);
            DialogPane dialogPane = loader.load();
            
            Dialog<Provider> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Ajouter un fournisseur");
            
            controller.setMessageManager(messageManager);
            controller.setDialog(dialog);

            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    return controller.getResult();
                }
                return null;
            });

            dialog.showAndWait().ifPresent(provider -> {
                if (provider != null) {
                    providers.add(provider);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, 
                "Erreur lors de l'ouverture du dialogue: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void showAddBuyerDialog() {
        try {
            URL fxmlUrl = getClass().getResource("/buyer-dialog.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            BuyerDialogController controller = new BuyerDialogController();
            loader.setController(controller);
            DialogPane dialogPane = loader.load();
            
            Dialog<Buyer> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Ajouter un acheteur");
            
            controller.setMessageManager(messageManager);
            controller.setDialog(dialog);

            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    return controller.getResult();
                }
                return null;
            });

            dialog.showAndWait().ifPresent(buyer -> {
                if (buyer != null) {
                    buyers.add(buyer);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, 
                "Erreur lors de l'ouverture du dialogue: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void launchNegociation(ActionEvent actionEvent) {
        providers.forEach(provider -> new Thread(provider).start());
        buyers.forEach(buyer -> new Thread(buyer).start());
    }
}