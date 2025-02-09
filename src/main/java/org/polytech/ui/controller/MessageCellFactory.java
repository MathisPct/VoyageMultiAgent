package org.polytech.ui.controller;

import javafx.geometry.Insets;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import org.polytech.agent.TypeOffer;
import org.polytech.messaging.Message;

public class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {
    @Override
    public ListCell<Message> call(ListView<Message> param) {
        return new ListCell<Message>() {
            private final VBox container = new VBox(5);
            private final Text headerText = new Text();
            private final Text contentText = new Text();

            {
                container.getStyleClass().add("message-cell");
                container.setPadding(new Insets(8));
                headerText.setStyle("-fx-font-weight: bold;");
            }

            @Override
            protected void updateItem(Message item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // En-tête avec l'heure et les agents
                    headerText.setText(String.format("[%s] %s → %s",
                            item.getDateEmission().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")),
                            item.getIssuer().getName(),
                            item.getReceiver().getName()));

                    // Contenu avec le prix et le type d'offre
                    contentText.setText(String.format("Prix: %.2f€ - %s",
                            item.getOffer().getPrice(),
                            formatOfferType(item.getOffer().getTypeOffer())));

                    container.getChildren().setAll(headerText, contentText);
                    setGraphic(container);

                    // Style basé sur le type d'offre
                    String style = getStyleForOfferType(item.getOffer().getTypeOffer());
                    contentText.setStyle(style);
                }
            }

            private String formatOfferType(TypeOffer type) {
                return switch(type) {
                    case INITIAL -> "Offre initiale";
                    case FIRST_ACCEPT -> "Acceptation préliminaire";
                    case END_FIRST_PHASE_NEGOCIATION -> "Fin de la première phase";
                    case DEMAND_CONFIRMATION_ACHAT -> "Demande de confirmation d'achat";
                    case POSITIVE_RESPONSE_CONFIRMATION_ACHAT -> "Confirmation d'achat acceptée";
                    case NEGATIVE_RESPONSE_CONFIRMATION_ACHAT -> "Confirmation d'achat refusée";
                    case AGAINST_PROPOSITION -> "Contre-proposition";
                };
            }

            private String getStyleForOfferType(TypeOffer type) {
                String baseStyle = "-fx-font-size: 14;";
                String colorStyle = switch(type) {
                    case INITIAL -> "-fx-fill: #2196F3;"; // Bleu
                    case FIRST_ACCEPT -> "-fx-fill: #4CAF50;"; // Vert
                    case END_FIRST_PHASE_NEGOCIATION -> "-fx-fill: #9C27B0;"; // Violet
                    case DEMAND_CONFIRMATION_ACHAT -> "-fx-fill: #FF9800;"; // Orange
                    case POSITIVE_RESPONSE_CONFIRMATION_ACHAT -> "-fx-fill: #4CAF50;"; // Vert
                    case NEGATIVE_RESPONSE_CONFIRMATION_ACHAT -> "-fx-fill: #F44336;"; // Rouge
                    case AGAINST_PROPOSITION -> "-fx-fill: #607D8B;"; // Gris bleuté
                };
                return baseStyle + colorStyle;
            }
        };
    }
}