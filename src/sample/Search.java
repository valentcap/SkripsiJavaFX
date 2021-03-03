package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class Search {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("search.fxml"));

    @FXML
    private Button btnBack;

    public Search() throws IOException {
    }

    public void backToHome(ActionEvent actionEvent) throws IOException {
        Stage stage;
        Parent root;

        stage = (Stage) btnBack.getScene().getWindow();
        root = FXMLLoader.load(getClass().getResource("Sample.fxml"));

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
