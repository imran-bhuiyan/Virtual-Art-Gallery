package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.File;
import java.io.IOException;
import java.sql.*;

public class ArtistMyPaintPageController {

    @FXML
    private VBox paintingsContainer;

    private int artistId;

    public void setArtistId(int artistId) {
        System.out.println("artisId From Artist My Paint Controller : " + artistId);
        this.artistId = artistId;
        loadPaintings();
    }

    public void initialize() {
        loadPaintings();
    }

    public void refreshPaintings() {
        paintingsContainer.getChildren().clear();
        loadPaintings();
    }

    private void loadPaintings() {
        int artistId = CurrentArtist.getInstance().getArtistId();

        String query = "SELECT p.painting_id, p.name, p.year, p.category, p.price, p.image_url, u.name AS artist_name, " +
                "(SELECT COUNT(*) FROM Reactions r WHERE r.painting_id = p.painting_id) AS total_reactions " +
                "FROM Paintings p " +
                "JOIN Users u ON p.artist_id = u.user_id " +
                "WHERE p.artist_id = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, artistId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                AnchorPane paintingPane = createPaintingPane(
                        rs.getInt("painting_id"),
                        rs.getString("name"),
                        rs.getString("artist_name"),
                        rs.getInt("year"),
                        rs.getString("category"),
                        rs.getDouble("price"),
                        rs.getInt("total_reactions"),
                        rs.getString("image_url")
                );
                paintingsContainer.getChildren().add(paintingPane);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load paintings: " + e.getMessage());
        }
    }

    private AnchorPane createPaintingPane(int paintingId, String name, String artistName, int year, String category, double price, int totalReactions, String imageUrl) {
        AnchorPane pane = new AnchorPane();
        pane.setPrefHeight(213.0);
        pane.setPrefWidth(860.0);
        pane.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1;");

        ImageView imageView = new ImageView();
        imageView.setFitHeight(200.0);
        imageView.setFitWidth(200.0);
        imageView.setLayoutX(10.0);
        imageView.setLayoutY(10.0);
        imageView.setPreserveRatio(true);

        File file = new File(imageUrl);
        if (file.exists()) {
            Image image = new Image(file.toURI().toString());
            imageView.setImage(image);

            imageView.setOnMouseClicked(event -> openLargerImage(image));
        } else {
            System.out.println("Image file not found: " + imageUrl);
        }

        Text nameText = createText(name, 220.0, 30.0, "-fx-font-size: 18px; -fx-font-weight: bold;");
        Text artistText = createText("Artist: " + artistName, 220.0, 57.0, null);
        Text yearText = createText("Year: " + year, 221.0, 87.0, null);
        Text categoryText = createText("Category: " + category, 220.0, 114.0, null);
        Text priceText = createText("Price: $" + price, 220.0, 142.0, null);
        Text reactionsText = createText("Reactions: " + totalReactions, 220.0, 170.0, null);

        Button editButton = new Button("Edit");
        editButton.setLayoutX(750.0);
        editButton.setLayoutY(170.0);
        editButton.setOnAction(event -> openEditPage(paintingId));

        pane.getChildren().addAll(imageView, nameText, artistText, yearText, categoryText, priceText, reactionsText, editButton);

        return pane;
    }

    private void openLargerImage(Image image) {
        Stage stage = new Stage();
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(600); // Adjust this value to change the size of the larger view

        Scene scene = new Scene(new VBox(imageView));
        stage.setScene(scene);
        stage.setTitle("Larger Image View");
        stage.show();
    }

    private Text createText(String content, double layoutX, double layoutY, String style) {
        Text text = new Text(content);
        text.setLayoutX(layoutX);
        text.setLayoutY(layoutY);
        if (style != null) {
            text.setStyle(style);
        }
        return text;
    }

    private void openEditPage(int paintingId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Artist/ArtistEditPainting.fxml"));
            Parent root = loader.load();

            ArtistEditPaintingController controller = loader.getController();
            controller.setPaintingId(paintingId);
            controller.setRefreshCallback(this::refreshPaintings);

            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Edit Painting");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open edit page: " + e.getMessage());
        }
    }

    @FXML
    void artistDashboard(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistDashboard.fxml");
    }

    @FXML
    void artistPaintings(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistPaintingPage.fxml");
    }

    @FXML
    void artistAddPainting(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistAddPaint.fxml");
    }

    @FXML
    void artistMessages(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistMessage.fxml");
    }

    @FXML
    void artistAddAuctions(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistAddAuction.fxml");
    }

    @FXML
    void artistMyProfile(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistProfile.fxml");
    }

    @FXML
    void artistLogout(ActionEvent event) throws IOException {
        loadPage(event, "Guest/userOrGuestHomePage.fxml");
    }

    private void loadPage(ActionEvent event, String fxmlFile) throws IOException {
        int artistId = CurrentArtist.getInstance().getArtistId();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();

        Object controller = loader.getController();
        if (controller instanceof ArtistPageController) {
            ((ArtistPageController) controller).setArtistId(CurrentArtist.getInstance().getArtistId());
        }

        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}