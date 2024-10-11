package com.example.test_project;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class adminOrderDetailsController extends BaseController {


    public void setUserId(int userId) {
        this.userId = userId;
        System.out.println("userId set to " + userId);

    }


    @FXML
    private TableView<ObservableList<String>> orderTable;

    @FXML
    private TableColumn<ObservableList<String>, String> orderIdColumn;

    @FXML
    private TableColumn<ObservableList<String>, String> customerNameColumn;

    @FXML
    private TableColumn<ObservableList<String>, String> customerEmailColumn;

    @FXML
    private TableColumn<ObservableList<String>, String> totalAmountColumn;

    @FXML
    private TableColumn<ObservableList<String>, String> orderDateColumn;
    @FXML
    private TableColumn<ObservableList<String>, String> orderStatus;

    public void initialize() {
        orderIdColumn.setCellValueFactory(data -> javafx.beans.binding.Bindings.createObjectBinding(() -> data.getValue().get(0)));
        customerNameColumn.setCellValueFactory(data -> javafx.beans.binding.Bindings.createObjectBinding(() -> data.getValue().get(1)));
        customerEmailColumn.setCellValueFactory(data -> javafx.beans.binding.Bindings.createObjectBinding(() -> data.getValue().get(2)));
        totalAmountColumn.setCellValueFactory(data -> javafx.beans.binding.Bindings.createObjectBinding(() -> data.getValue().get(3)));
        orderDateColumn.setCellValueFactory(data -> javafx.beans.binding.Bindings.createObjectBinding(() -> data.getValue().get(4)));
        orderStatus.setCellValueFactory(data -> javafx.beans.binding.Bindings.createObjectBinding(() -> data.getValue().get(5)));

        loadOrderData();
    }

    private void loadOrderData() {
        String query = "SELECT o.order_id, u.name, u.email, o.total_amount, o.order_date , o.order_status " +
                "FROM orders o JOIN users u ON o.user_id = u.user_id";

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(rs.getString("order_id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("email"));
                row.add(String.format("%.2f", rs.getDouble("total_amount")));
                row.add(rs.getTimestamp("order_date").toLocalDateTime().toString());
                row.add(rs.getString("order_status"));
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }

        orderTable.setItems(data);
    }
    // Navigation methods
    @FXML
    void goLogout(ActionEvent event) throws IOException {
        loadPage(event, "Customer/customerLoginPage.fxml");
    }

    @FXML
    void goDashboard(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminDashboard.fxml");
    }

    @FXML
    void addArtist(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminAddArtist.fxml");
    }

    @FXML
    void addArtworks(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminAddArtwork.fxml");
    }

    @FXML
    void addEvent(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminAddEvent.fxml");
    }

    @FXML
    void addCustomer(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminAddCustomers.fxml");
    }

    @FXML
    void manageCustomer(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminManageCustomers.fxml");
    }

    @FXML
    void manageArtist(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminManageArtist.fxml");
    }

    @FXML
    void manageArtwork(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminManageArtwork.fxml");
    }

    @FXML
    void manageEvent(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminManageEvent.fxml");
    }

    @FXML
    void goOrders(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminOrderPage.fxml");
    }

    @FXML
    void goMessages(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminMessages.fxml");
    }

    @FXML
    void goQueries(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminQueriesPage.fxml");
    }

    @FXML
    void goAuctionRequest(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminAuctionPage.fxml");
    }

    private void loadPageWithUserId(ActionEvent event, String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        BaseController controller = loader.getController();
        if (controller != null) {
            controller.setUserId(this.userId);
        }

        Scene scene = new Scene(root);
        Stage stage;

        if (event.getSource() instanceof MenuItem) {
            MenuItem menuItem = (MenuItem) event.getSource();
            stage = (Stage) menuItem.getParentPopup().getOwnerWindow();
        } else if (event.getSource() instanceof Node) {
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        } else {
            throw new IllegalArgumentException("Event source not recognized");
        }

        stage.setScene(scene);
        stage.show();
    }
}