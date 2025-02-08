package org.polytech.ui.controller;

import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.polytech.agent.Buyer;
import org.polytech.agent.Company;
import org.polytech.agent.constraints.BuyerConstraints;
import org.polytech.agent.strategy.InterestBasedBuyerStrategy;
import org.polytech.messaging.MessageManager;

import java.util.List;

public class BuyerDialogController {
    @FXML private TextField nameField;
    @FXML private TextField budgetField;
    @FXML private TextField interestField;
    @FXML private ListView<Company> allowedCompanies;
    @FXML private TextField destinationField;
    @FXML private ListView<String> destinationsList;
    
    private final ObservableList<String> destinations = FXCollections.observableArrayList();
    private MessageManager messageManager;
    private Buyer buyerToEdit;
    private boolean initialized = false;
    private Buyer result;
    private Dialog<Buyer> dialog;

    public BuyerDialogController() {
        // Default constructor needed for FXML
    }

    public void setMessageManager(MessageManager messageManager) {
        this.messageManager = messageManager;
        ensureInitialized();
    }

    public void setDialog(Dialog<Buyer> dialog) {
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
        if (!initialized && allowedCompanies != null && destinationsList != null) {
            allowedCompanies.setItems(FXCollections.observableArrayList(Company.values()));
            allowedCompanies.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            destinationsList.setItems(destinations);
            initialized = true;
        }
    }

    public void setBuyerToEdit(Buyer buyer) {
        this.buyerToEdit = buyer;
        if (buyer != null) {
            nameField.setText(buyer.getName());
            interestField.setText(String.valueOf(buyer.getInterest()));
            budgetField.setText(String.valueOf(buyer.getBuyerConstraints().getMaxBudget()));
            
            // Pré-sélectionner les compagnies autorisées
            List<Company> allowedCompaniesList = buyer.getBuyerConstraints().getAllowedCompanies();
            allowedCompanies.getSelectionModel().clearSelection();
            for (Company company : allowedCompaniesList) {
                int index = allowedCompanies.getItems().indexOf(company);
                if (index >= 0) {
                    allowedCompanies.getSelectionModel().select(index);
                }
            }
            
            // Charger les destinations
            destinations.setAll(buyer.getBuyerConstraints().getDestinations());
        }
    }

    public Buyer getResult() {
        return result;
    }

    @FXML
    private void handleAddDestination() {
        String destination = destinationField.getText().trim();
        if (!destination.isEmpty()) {
            destinations.add(destination);
            destinationField.clear();
        }
    }

    @FXML
    private void handleSave() {
        try {
            if (nameField.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Le nom ne peut pas être vide");
                alert.showAndWait();
                return;
            }

            double budget = Double.parseDouble(budgetField.getText());
            int interest = Integer.parseInt(interestField.getText());

            if (interest < 1 || interest > 10) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "L'intérêt doit être entre 1 et 10");
                alert.showAndWait();
                return;
            }

            BuyerConstraints constraints = new BuyerConstraints(budget);
            allowedCompanies.getSelectionModel().getSelectedItems()
                    .forEach(constraints::addAllowedCompany);
            destinations.forEach(constraints::addDestination);

            if (buyerToEdit != null) {
                // Mettre à jour l'acheteur existant
                buyerToEdit.setName(nameField.getText());
                buyerToEdit.setInterest(interest);
                buyerToEdit.setBuyerConstraints(constraints);
                result = buyerToEdit;
            } else {
                // Créer un nouvel acheteur
                result = new Buyer(messageManager, constraints, nameField.getText(), interest);
                result.setNegociationStrategy(new InterestBasedBuyerStrategy());
            }

            dialog.setResult(result);
            dialog.close();
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Les valeurs numériques sont invalides");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleCancel() {
        dialog.setResult(null);
        dialog.close();
    }
}