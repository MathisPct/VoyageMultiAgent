package org.polytech.ui.controller;

import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.polytech.agent.Company;
import org.polytech.agent.Provider;
import org.polytech.agent.Ticket;
import org.polytech.agent.strategy.InterestBasedProviderStrategy;
import org.polytech.messaging.MessageManager;

import java.util.ArrayList;

public class ProviderDialogController {
    @FXML private TextField nameField;
    @FXML private ListView<Ticket> ticketsList;
    private final ObservableList<Ticket> tickets = FXCollections.observableArrayList();
    private MessageManager messageManager;
    private Provider providerToEdit;
    private boolean initialized = false;
    private Provider result;
    private Dialog<Provider> dialog;

    public ProviderDialogController() {
        // Default constructor needed for FXML
    }

    public void setMessageManager(MessageManager messageManager) {
        this.messageManager = messageManager;
        ensureInitialized();
    }

    public void setDialog(Dialog<Provider> dialog) {
        this.dialog = dialog;
        // Configuration des boutons après l'initialisation du dialog
        dialog.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(ActionEvent.ACTION, event -> {
            event.consume(); // Empêcher la fermeture automatique
            handleSave();
        });
        
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).addEventFilter(ActionEvent.ACTION, event -> {
            event.consume(); // Empêcher la fermeture automatique
            handleCancel();
        });
    }

    @FXML
    public void initialize() {
        ensureInitialized();
    }

    private void ensureInitialized() {
        if (!initialized && ticketsList != null) {
            ticketsList.setItems(tickets);
            setupTicketsList();
            initialized = true;
        }
    }

    private void setupTicketsList() {
        // Ajouter un bouton de suppression pour chaque ticket
        ticketsList.setCellFactory(lv -> new ListCell<Ticket>() {
            @Override
            protected void updateItem(Ticket ticket, boolean empty) {
                super.updateItem(ticket, empty);
                if (empty || ticket == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    HBox cell = new HBox(10);
                    cell.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    
                    Label ticketLabel = new Label(String.format("%s → %s (%.2f€) - %s", 
                        ticket.getDeparture(), ticket.getArrival(), 
                        ticket.getPrice(), ticket.getCompany()));
                    
                    Button deleteButton = new Button("Supprimer");
                    deleteButton.setOnAction(e -> tickets.remove(ticket));
                    
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    
                    cell.getChildren().addAll(ticketLabel, spacer, deleteButton);
                    setGraphic(cell);
                }
            }
        });
    }

    public void setProviderToEdit(Provider provider) {
        this.providerToEdit = provider;
        if (provider != null) {
            nameField.setText(provider.getName());
            tickets.setAll(provider.getTickets());
        }
    }

    public Provider getResult() {
        return result;
    }

    @FXML
    private void handleAddTicket() {
        Dialog<Ticket> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un ticket");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(10));

        TextField priceField = new TextField();
        TextField fromField = new TextField();
        TextField toField = new TextField();
        ComboBox<Company> companyBox = new ComboBox<>(
                FXCollections.observableArrayList(Company.values())
        );

        grid.add(new Label("Prix:"), 0, 0);
        grid.add(priceField, 1, 0);
        grid.add(new Label("Départ:"), 0, 1);
        grid.add(fromField, 1, 1);
        grid.add(new Label("Destination:"), 0, 2);
        grid.add(toField, 1, 2);
        grid.add(new Label("Compagnie:"), 0, 3);
        grid.add(companyBox, 1, 3);

        dialogPane.setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    if (fromField.getText().isEmpty() || toField.getText().isEmpty()) {
                        throw new IllegalArgumentException("Les champs départ et destination sont obligatoires");
                    }
                    if (companyBox.getValue() == null) {
                        throw new IllegalArgumentException("Veuillez sélectionner une compagnie");
                    }
                    double price = Double.parseDouble(priceField.getText());
                    if (price <= 0) {
                        throw new IllegalArgumentException("Le prix doit être supérieur à 0");
                    }
                    return new Ticket(
                            price,
                            price * 0.9,
                            fromField.getText(),
                            toField.getText(),
                            companyBox.getValue()
                    );
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Le prix doit être un nombre valide");
                    alert.showAndWait();
                    return null;
                } catch (IllegalArgumentException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
                    alert.showAndWait();
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(tickets::add);
    }

    @FXML
    private void handleSave() {
        if (nameField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Le nom ne peut pas être vide");
            alert.showAndWait();
            return;
        }

        if (tickets.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, 
                "Attention : aucun ticket n'a été ajouté. Voulez-vous continuer ?",
                ButtonType.YES, ButtonType.NO);
            if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.NO) {
                return;
            }
        }

        if (providerToEdit != null) {
            // Update existing provider
            providerToEdit.setName(nameField.getText());
            providerToEdit.setTickets(new ArrayList<>(tickets));
            result = providerToEdit;
        } else {
            // Create new provider
            result = new Provider(messageManager, new ArrayList<>(tickets), 5, nameField.getText());
            result.setNegociationStrategy(new InterestBasedProviderStrategy());
        }

        dialog.setResult(result);
        dialog.close();
    }

    @FXML
    private void handleCancel() {
        dialog.setResult(null);
        dialog.close();
    }
}