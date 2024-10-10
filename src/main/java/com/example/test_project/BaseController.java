package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

import java.io.IOException;

public abstract class BaseController implements UserIdAware {
    protected int userId;

    @Override
    public void setUserId(int userId) {
        this.userId = userId;
        System.out.println(getClass().getSimpleName() + ": userId set to " + userId);
    }

    protected void loadPage(ActionEvent event, String fxmlFile) throws IOException {


        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();

        Object controller = loader.getController();
        if (controller instanceof UserIdAware) {
            ((UserIdAware) controller).setUserId(this.userId);
            System.out.println(getClass().getSimpleName() + ": Passing userId " + this.userId + " to " + controller.getClass().getSimpleName());
        }

//        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        Stage stage;

        if (event.getSource() instanceof MenuItem) {
            // If the event source is a MenuItem, we need to get the stage differently
            MenuItem menuItem = (MenuItem) event.getSource();
            stage = (Stage) menuItem.getParentPopup().getOwnerWindow();
        } else if (event.getSource() instanceof Node) {
            // If it's a regular Node (like a Button), we can get the stage as before
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        } else {
            throw new IllegalArgumentException("Event source not recognized");
        }

        stage.setScene(scene);
        stage.show();

    }
}