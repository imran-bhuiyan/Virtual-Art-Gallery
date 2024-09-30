package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ArtistPaintingPageController {

    @FXML private VBox paintingsContainer;

    private int artistId;

    public void initialize() {
        loadPaintings();
    }

    public void setArtistId(int artistId) {
        System.out.println("artistId From Artist Painting Controller : " + artistId);
        this.artistId = artistId;
        loadPaintings();
    }

    public void loadPaintings() {
        int artistId = CurrentArtist.getInstance().getArtistId();
        paintingsContainer.getChildren().clear();
        String query = "SELECT p.*, u.name as artist_name, " +
                "(SELECT COUNT(*) FROM Reactions r WHERE r.painting_id = p.painting_id) as reaction_count " +
                "FROM Paintings p " +
                "JOIN Users u ON p.artist_id = u.user_id";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                AnchorPane paintingPane = createPaintingPane(rs);
                paintingsContainer.getChildren().add(paintingPane);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load paintings");
        }
    }

    private AnchorPane createPaintingPane(ResultSet rs) throws SQLException {
        AnchorPane pane = new AnchorPane();
        pane.setPrefHeight(250.0);
        pane.setPrefWidth(848.0);
        pane.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1;");

        // Add ImageView for the painting
        ImageView paintingImage = new ImageView();
        paintingImage.setFitHeight(200.0);
        paintingImage.setFitWidth(200.0);
        paintingImage.setLayoutX(10.0);
        paintingImage.setLayoutY(10.0);
        paintingImage.setPreserveRatio(true);

        // Load the image
        String imagePath = rs.getString("image_url");
        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            Image image = new Image(imageFile.toURI().toString());
            paintingImage.setImage(image);

            // Add click event to open larger image
            paintingImage.setOnMouseClicked(event -> openLargerImage(image));
        } else {
            // Set a placeholder image or show an error message
            paintingImage.setImage(new Image("file:placeholder.png"));
        }

        Text nameText = new Text(rs.getString("name"));
        nameText.setLayoutX(220.0);
        nameText.setLayoutY(30.0);
        nameText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Text artistLabel = new Text("Artist:");
        artistLabel.setLayoutX(220.0);
        artistLabel.setLayoutY(57.0);

        Text artistName = new Text(rs.getString("artist_name"));
        artistName.setLayoutX(255.0);
        artistName.setLayoutY(57.0);

        Text yearLabel = new Text("Year:");
        yearLabel.setLayoutX(221.0);
        yearLabel.setLayoutY(87.0);

        Text year = new Text(String.valueOf(rs.getInt("year")));
        year.setLayoutX(259.0);
        year.setLayoutY(87.0);




        Text categoryLabel = new Text("Category:");
        categoryLabel.setLayoutX(220.0);
        categoryLabel.setLayoutY(114.0);

        Text category = new Text(rs.getString("category"));
        category.setLayoutX(279.0);
        category.setLayoutY(114.0);




        Text priceLabel = new Text("Price:");
        priceLabel.setLayoutX(220.0);
        priceLabel.setLayoutY(142.0);

        Text price = new Text(String.format("$%.2f", rs.getDouble("price")));
        price.setLayoutX(258.0);
        price.setLayoutY(142.0);

        Button addToCartButton = new Button("Add to Cart");
        addToCartButton.setLayoutX(750.0);
        addToCartButton.setLayoutY(10.0);
        addToCartButton.setStyle("-fx-background-color: #40E0D0;");

        Button reactButton = new Button();
        reactButton.setLayoutX(218.0);
        reactButton.setLayoutY(153.0);
        //ImageView reactIcon = new ImageView(new Image("file:/path/to/your/icon/Love.png"));
        ImageView reactIcon = new ImageView(new Image("file:D:\\Trimester\\8th\\AOOP\\IntellijIdea\\Test_Project\\src\\main\\java\\Icon\\Love.png"));

        reactIcon.setFitHeight(20.0);
        reactIcon.setFitWidth(20.0);
        reactButton.setGraphic(reactIcon);

        Text reactionCount = new Text(String.valueOf(rs.getInt("reaction_count")));
        reactionCount.setLayoutX(269.0);
        reactionCount.setLayoutY(170.0);

        pane.getChildren().addAll(paintingImage, nameText, artistLabel, artistName, yearLabel, year,
                categoryLabel, category, priceLabel, price, addToCartButton,
                reactButton, reactionCount);



//        Text artistIcon = new Text("\uf007"); // FontAwesome user icon
//        artistIcon.getStyleClass().add("icon");
//        artistIcon.setLayoutX(220.0);
//        artistIcon.setLayoutY(57.0);
//
//
//
//        Text yearIcon = new Text("\uf073"); // FontAwesome calendar icon
//        yearIcon.getStyleClass().add("icon");
//        yearIcon.setLayoutX(220.0);
//        yearIcon.setLayoutY(87.0);
//
//
//
//        Text priceIcon = new Text("\uf155"); // FontAwesome dollar icon
//        priceIcon.getStyleClass().add("icon");
//        priceIcon.setLayoutX(220.0);
//        priceIcon.setLayoutY(117.0);
//
//
//
//        Text reactionIcon = new Text("\uf004"); // FontAwesome heart icon
//        reactionIcon.getStyleClass().add("icon");
//        reactionIcon.setLayoutX(220.0);
//        reactionIcon.setLayoutY(147.0);





        return pane;
    }

    private void openLargerImage(Image image) {
//        Stage popupStage = new Stage();
//        popupStage.initModality(Modality.APPLICATION_MODAL);
//        popupStage.setTitle("Larger View");
//
//        ImageView largerImageView = new ImageView(image);
//        largerImageView.setPreserveRatio(true);
//        largerImageView.setFitWidth(600); // Adjust this value as needed
//
//        Scene scene = new Scene(new AnchorPane(largerImageView));
//        popupStage.setScene(scene);
//        popupStage.show();

        Stage stage = new Stage();
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(600); // Adjust this value to change the size of the larger view

        Scene scene = new Scene(new VBox(imageView));
        stage.setScene(scene);
        stage.setTitle("Larger Image View");
        stage.show();


    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    void artistDashboard(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistDashboard.fxml");
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
    void artistMyPainting(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistMyPaintPage.fxml");
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
}