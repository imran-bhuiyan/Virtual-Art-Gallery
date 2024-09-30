package com.example.test_project;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;

public class ZoomableImageViewController {

    @FXML
    private ImageView imageView;

    private double zoomFactor = 1.0;
    private static final double MIN_ZOOM = 0.5;
    private static final double MAX_ZOOM = 5.0;

    public void initialize() {
        imageView.setOnScroll(this::zoom);
    }

    public void setImage(Image image) {
        imageView.setImage(image);
        imageView.setFitWidth(image.getWidth());
        imageView.setFitHeight(image.getHeight());
    }

    private void zoom(ScrollEvent event) {
        double deltaY = event.getDeltaY();
        if (deltaY < 0) {
            zoomFactor *= 0.9;
        } else {
            zoomFactor *= 1.1;
        }

        zoomFactor = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoomFactor));

        imageView.setScaleX(zoomFactor);
        imageView.setScaleY(zoomFactor);

        event.consume();
    }
}