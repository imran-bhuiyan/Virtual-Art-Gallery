package com.example.test_project;

import javafx.fxml.FXML;
import javafx.scene.text.Text;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ArtistBalanceController extends BaseController{
    @FXML
    private Text balanceText;

    private int userId;

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void loadBalance() {
        try (Connection conn = DataBaseConnection.getConnection()) {
            String query = "SELECT credits FROM users WHERE user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        double balance = rs.getDouble("credits");
                        balanceText.setText(String.format("$%.2f", balance));
                    } else {
                        balanceText.setText("Balance not found");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            balanceText.setText("Error loading balance");
        }
    }
}