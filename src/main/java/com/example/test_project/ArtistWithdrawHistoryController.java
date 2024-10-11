package com.example.test_project;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ArtistWithdrawHistoryController extends BaseController {

    @FXML
    private TableView<ObservableList<String>> historyTable;

    @FXML
    private TableColumn<ObservableList<String>, String> colNo;

    @FXML
    private TableColumn<ObservableList<String>, String> colAmount;

    @FXML
    private TableColumn<ObservableList<String>, String> colAccountNo;

    @FXML
    private TableColumn<ObservableList<String>, String> colPaymentMethod;

    @FXML
    private TableColumn<ObservableList<String>, String> colStatus;

    @FXML
    private TableColumn<ObservableList<String>, String> colTime;

    public void setUserId(int userId) {
        this.userId = userId;
        System.out.println("userId set to " + userId);
        loadWithdrawHistory();

    }


    public void initialize() {
        colNo.setCellValueFactory(data -> javafx.beans.binding.Bindings.createObjectBinding(() -> data.getValue().get(0)));
        colAmount.setCellValueFactory(data -> javafx.beans.binding.Bindings.createObjectBinding(() -> data.getValue().get(1)));
        colAccountNo.setCellValueFactory(data -> javafx.beans.binding.Bindings.createObjectBinding(() -> data.getValue().get(2)));
        colPaymentMethod.setCellValueFactory(data -> javafx.beans.binding.Bindings.createObjectBinding(() -> data.getValue().get(3)));
        colStatus.setCellValueFactory(data -> javafx.beans.binding.Bindings.createObjectBinding(() -> data.getValue().get(4)));
        colTime.setCellValueFactory(data -> javafx.beans.binding.Bindings.createObjectBinding(() -> data.getValue().get(5)));

        loadWithdrawHistory();
    }
    private void loadWithdrawHistory() {
        String query = "SELECT id, amount, account_no, payment_method, status, approve_date " +
                "FROM withdraw_credit WHERE user_id = ?";

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            // Set the userId parameter in the query
            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ObservableList<String> row = FXCollections.observableArrayList();
                    row.add(rs.getString("id"));
                    row.add(String.format("%.2f", rs.getDouble("amount")));
                    row.add(rs.getString("account_no"));
                    row.add(rs.getString("payment_method"));
                    row.add(rs.getString("status"));
                    row.add(rs.getTimestamp("approve_date") != null ? rs.getTimestamp("approve_date").toLocalDateTime().toString() : "N/A");
                    data.add(row);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();

        }

        historyTable.setItems(data);
    }


}