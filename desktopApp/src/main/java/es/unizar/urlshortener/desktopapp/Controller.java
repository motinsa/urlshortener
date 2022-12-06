package es.unizar.urlshortener.desktopapp;

import com.google.gson.Gson;
import es.unizar.urlshortener.desktopapp.models.*;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.FileInputStream;


import javax.swing.text.Element;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.awt.*;
import java.net.http.HttpResponse;

public class Controller {
    @FXML
    private TextField urlField;

    @FXML
    private Button shortBtn;

    @FXML
    private CheckBox limit;

    @FXML
    private Spinner<Integer> redNumber;

    @FXML
    private VBox urlRes;

    @FXML
    private Hyperlink shortURL;

    @FXML
    private Button copyBtn;

    @FXML
    private Button qrBtn;

    @FXML
    private Label urlLabel;

    @FXML
    private VBox vboxParent;

    @FXML
    private Label superMargin;

    @FXML
    private VBox errorBox;

    @FXML
    private Label errorTxt;

    @FXML
    protected void initialize(){
        // Disable the result box
        hideResultBoxes();

        // Apply some CSS styles to the button
        shortBtn.setStyle("-fx-font-family: 'Helvetica Neue';" +
                "-fx-font-size: 14px;" +
                "-fx-background-color: #007AFF;" +
                "-fx-text-fill: white;" +
                "-fx-border-radius: 10px;");

        qrBtn.setStyle("-fx-font-family: 'Helvetica Neue';" +
                "-fx-font-size: 11px;" +
                "-fx-background-color: #007AFF;" +
                "-fx-text-fill: white;" +
                "-fx-border-radius: 10px;");

        copyBtn.setStyle("-fx-font-family: 'Helvetica Neue';" +
                "-fx-font-size: 11px;" +
                "-fx-background-color: #007AFF;" +
                "-fx-text-fill: white;" +
                "-fx-border-radius: 10px;");

        // Registrar accion de abrir el stage en el btn del qr
        qrBtn.setOnAction(event -> {

            Stage stage = App.getStage();
            StackPane secondaryLayout = new StackPane();

            //get de los bytes
            WritableImage image = null;
            try {
                HttpClientGet client = new HttpClientGet(shortURL.getText() + "/qr");
                image = client.getImage();
                if (image == null) {
                    Label secondLabel = new Label("URI de destino no validada todavía");
                    secondLabel.setStyle(
                            "-fx-font-size: 14px;" +
                            "-fx-text-fill: #ff726d;");
                    secondaryLayout.getChildren().add(secondLabel);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            ImageView view = new ImageView(image);
            view.setFitWidth(300);
            view.setFitHeight(300);
            secondaryLayout.getChildren().add(view);

            Scene secondScene = new Scene(secondaryLayout, 300, 300);

            // New window (Stage)
            Stage newWindow = new Stage();
            newWindow.setTitle("QR Code");
            newWindow.setScene(secondScene);

            // Specifies the modality for new window.
            newWindow.initModality(Modality.WINDOW_MODAL);

            // Specifies the owner Window (parent) for new window
            newWindow.initOwner(stage);

            // Set position of second window, related to primary window.
            newWindow.setX(stage.getX() + 250);
            newWindow.setY(stage.getY() + 200);
            newWindow.setResizable(false);

            newWindow.show();
        });

        // Listen for changes to the descField textField.
        urlField.textProperty().addListener((observable, oldValue, newValue) -> {
            hideResultBoxes();
            checkButton();
        });
    }

    private void hideResultBoxes() {
        urlRes.setVisible(false);
        errorBox.setVisible(false);
        urlRes.managedProperty().bind(urlRes.visibleProperty());
        errorBox.managedProperty().bind(errorBox.visibleProperty());
    }

    /*
     * Listeners call this method to check if the fields are empty or not to enable the "Asignar tarea" button
     */
    private void checkButton() {
        if (urlField.getLength() == 0) {
            System.out.println("URL: " + urlField.getText() + "is empty");
            shortBtn.setDisable(true);
        } else {
            shortBtn.setDisable(false);
        }
    }

    @FXML
    protected void onShortClick() {
        HttpClientPostForm post = new HttpClientPostForm("http://localhost:8080/api/link");
        int lim = 0;
        if (limit.isSelected()) {
            lim = redNumber.getValue();
        }
        HttpResponse<String> response = post.apiLink(urlField.getText(), Integer.toString(lim), "", "");

        Gson gson = new Gson();
        if (response.statusCode() == 201) {
            LinkResponse linkResponse = gson.fromJson(response.body(), LinkResponse.class);
            shortURL.setText(linkResponse.getURL());
            urlLabel.setText(urlField.getText());
            urlRes.setVisible(true);
        } else {
            LinkResponseEroor linkResponseError = gson.fromJson(response.body(), LinkResponseEroor.class);
            errorTxt.setText(linkResponseError.getMessage());
            if (errorTxt.getText() == null) {
                errorTxt.setText("Error desconocido");
            }
            errorBox.setVisible(true);
        }
    }

    @FXML
    protected void onLimitClick() {
        redNumber.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 1));
        redNumber.setDisable(!limit.isSelected());
    }

    @FXML
    protected void onCopyClick() {
        ClipboardContent content = new ClipboardContent();
        content.putString(urlLabel.getText());
        Clipboard.getSystemClipboard().setContent(content);
    }

    @FXML
    protected void OnLinkClick() {
        try {
            Desktop.getDesktop().browse(URI.create(shortURL.getText()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}