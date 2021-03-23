package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.ResourceBundle;

public class Settings implements Initializable {
    @FXML
    private Label installLocation;
    @FXML
    private Label parsingResultLocation;
    @FXML
    private Label solrPath;
    @FXML
    private Button backBtn;
    @FXML
    private Button setInstallLocation;
    @FXML
    private Button setParsingResultLocation;
    @FXML
    private Button setSolrPath;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.getSettings();
    }

    public void setSettings(){
        try (OutputStream output = new FileOutputStream("./configs.properties")) {

            Properties prop = new Properties();

            // set the properties value
            Path iL = Paths.get(installLocation.getText());
            prop.setProperty("installLocation", iL.toString());

            Path pL = Paths.get(parsingResultLocation.getText());
            prop.setProperty("parsingResultLocation", pL.toString());

            Path solrL = Paths.get(solrPath.getText());
            prop.setProperty("solrPath", solrL.toString());

            // save properties to project root folder
            prop.store(output, null);

//            System.out.println(prop);

        } catch (IOException io) {
            io.printStackTrace();
        }
    }
    public void getSettings(){
        try (InputStream input = new FileInputStream("./configs.properties")) {
            String res;

            Properties prop = new Properties();
            if (input != null) {
                prop.load(input);
            } else {
                throw new FileNotFoundException("property file not found in the classpath");
            }

            res = prop.getProperty("installLocation");
            installLocation.setText(res);
            res = prop.getProperty("parsingResultLocation");
            parsingResultLocation.setText(res);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }
    public void backToHome(ActionEvent actionEvent) throws IOException {
        Stage stage;
        Parent root;

        stage = (Stage) backBtn.getScene().getWindow();
        root = FXMLLoader.load(getClass().getResource("Sample.fxml"));

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void setInstallLocation(ActionEvent actionEvent) throws IOException {
        String res = "";
        DirectoryChooser directoryChooser = new DirectoryChooser();
        String def = "";
        if(!installLocation.getText().equals("")){
            def = installLocation.getText();
            directoryChooser.setInitialDirectory(new File(def));
        }else{
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        }


        File selectedDirectory = directoryChooser.showDialog(setInstallLocation.getScene().getWindow());
        res = selectedDirectory.getAbsolutePath();
        this.installLocation.setText(res);
    }

    public void setParsingResultLocation(ActionEvent actionEvent) throws IOException {
        String res = "";
        DirectoryChooser directoryChooser = new DirectoryChooser();
        String def = "";
        if(!parsingResultLocation.getText().equals("")){
            def = parsingResultLocation.getText();
            directoryChooser.setInitialDirectory(new File(def));
        }else{
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        }

        File selectedDirectory = directoryChooser.showDialog(setParsingResultLocation.getScene().getWindow());
        res = selectedDirectory.getAbsolutePath();
        this.parsingResultLocation.setText(res);
    }

    public void setSolrPath(ActionEvent actionEvent) {
        String res = "";
        DirectoryChooser directoryChooser = new DirectoryChooser();
        String def = "";
        if(!solrPath.getText().equals("")){
            def = solrPath.getText();
            directoryChooser.setInitialDirectory(new File(def));
        }else{
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        }

        File selectedDirectory = directoryChooser.showDialog(setSolrPath.getScene().getWindow());
        res = selectedDirectory.getAbsolutePath();
        this.solrPath.setText(res);
    }
}
