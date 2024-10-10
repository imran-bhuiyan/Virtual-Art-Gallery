package com.example.test_project;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class CustomerPaintingCheckoutController extends BaseController {

    @FXML
    private TableView<CartItem> cartTable;

    @FXML
    private TableColumn<CartItem, String> nameColumn;

    @FXML
    private TableColumn<CartItem, Integer> quantityColumn;

    @FXML
    private TableColumn<CartItem, Double> priceColumn;

    @FXML
    private Label subTotal;

    @FXML
    private Label shipping;

    @FXML
    private Label total;

    private DataBaseConnection dbConnection = new DataBaseConnection();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        shipping.setText("70.0");
    }

    @Override
    public void setUserId(int userId) {
        super.setUserId(userId);
        loadCartItems();
        calculateTotal();
    }

    private void loadCartItems() {
        String query = "SELECT p.name as Name, SUM(c.quantity) as Quantity, SUM(p.price * c.quantity) as Price " +
                "FROM cart c JOIN paintings p ON c.painting_id = p.painting_id " +
                "WHERE c.user_id = ? GROUP BY p.name";

        ObservableList<CartItem> cartItems = FXCollections.observableArrayList();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                cartItems.add(new CartItem(
                        rs.getString("Name"),
                        rs.getInt("Quantity"),
                        rs.getDouble("Price")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        cartTable.setItems(cartItems);
    }

    private void calculateTotal() {
        String query = "SELECT SUM(p.price * c.quantity) as Subtotal " +
                "FROM cart c JOIN paintings p ON c.painting_id = p.painting_id " +
                "WHERE c.user_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                double subtotal = rs.getDouble("Subtotal");
                subTotal.setText(String.format("%.2f", subtotal));
                double shippingCost = Double.parseDouble(shipping.getText());
                total.setText(String.format("%.2f", subtotal + shippingCost));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void ordernow(ActionEvent event) {
        String insertOrderQuery = "INSERT INTO Orders (user_id, total_amount, order_date) VALUES (?, ?, ?)";
        String insertOrderItemQuery = "INSERT INTO Orderitems (order_id, painting_id, quantity, price) " +
                "SELECT ?, c.painting_id, c.quantity, p.price " +
                "FROM cart c JOIN paintings p ON c.painting_id = p.painting_id " +
                "WHERE c.user_id = ?";
        String deleteCartQuery = "DELETE FROM cart WHERE user_id = ?";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);

            // Insert order
            try (PreparedStatement pstmt = conn.prepareStatement(insertOrderQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, userId);
                pstmt.setDouble(2, Double.parseDouble(total.getText()));
                pstmt.setObject(3, LocalDateTime.now());
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                int orderId = -1;
                if (rs.next()) {
                    orderId = rs.getInt(1);
                }

                // Insert order items
                try (PreparedStatement pstmt2 = conn.prepareStatement(insertOrderItemQuery)) {
                    pstmt2.setInt(1, orderId);
                    pstmt2.setInt(2, userId);
                    pstmt2.executeUpdate();
                }

                // Delete cart items
                try (PreparedStatement pstmt3 = conn.prepareStatement(deleteCartQuery)) {
                    pstmt3.setInt(1, userId);
                    pstmt3.executeUpdate();
                }
            }

            conn.commit();
            // Show success message and navigate to confirmation page
            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Order Confirmation");
            alert.setHeaderText(null);
            alert.setContentText("Your order has been placed successfully!");
            alert.showAndWait();


        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            // Show error message
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Order Error");
            alert.setHeaderText(null);
            alert.setContentText("There was an error processing your order. Please try again.");
            alert.showAndWait();


        }
        finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @FXML
    void cancel(ActionEvent event) {
        String deleteCartQuery = "DELETE FROM cart WHERE user_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteCartQuery)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();

            // Navigate back to the paintings page or home page


            goCustomerPaintings(event);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            // Show error message
        }
    }

    // Navigation methods
    @FXML
    void goCustomerHome(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Customer/customerHomePage.fxml");
    }

    @FXML
    void goCustomerPaintings(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Customer/customerPaintingPage.fxml");
    }

    @FXML
    void goCustomerTopArt(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Customer/customerPageTopArt.fxml");
    }

    @FXML
    void goCustomerEvents(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Customer/customerEventPage.fxml");
    }

    @FXML
    void goCustomerAuction(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Customer/customerLiveAuctionPage.fxml");
    }

    @FXML
    void goCustomerNFT(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Customer/customerNFTpage.fxml");
    }

    @FXML
    void goCustomerProfile(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Customer/customerProfilePage.fxml");
    }

    @FXML
    void goCustomerCart(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Customer/customerPaintingCheckout.fxml");
    }

    @FXML
    void goCustomerNotification(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Customer/customerNotification.fxml");
    }

    @FXML
    void goCustomerMessages(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Customer/customerMessengerPage.fxml");
    }

    @FXML
    void customerLogout(ActionEvent event) throws IOException {
        System.out.println("CustomerNotification: Logging out user");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Customer/customerLoginPage.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
        System.out.println("CustomerNotificationController: Navigated to login page");
    }

    private void loadPageWithUserId(ActionEvent event, String fxmlPath) throws IOException {
        System.out.println("CustomerNotificationController: loadPageWithUserId() called with path: " + fxmlPath);
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        BaseController controller = loader.getController();
        if (controller != null) {
            System.out.println("CustomerNotificationController: Setting userId " + userId + " on new controller");
            controller.setUserId(this.userId);
        } else {
            System.out.println("CustomerNotificationController: Warning - controller is null");
        }
        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    // Helper class for TableView
    public static class CartItem {
        private String name;
        private int quantity;
        private double price;

        public CartItem(String name, int quantity, double price) {
            this.name = name;
            this.quantity = quantity;
            this.price = price;
        }

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
    }
}