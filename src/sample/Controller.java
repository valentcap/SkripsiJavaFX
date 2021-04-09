package sample;

import ParsingClasses.FileParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.GenericListVisitorAdapter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.neo4j.driver.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

public class Controller implements Initializable {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));

    @FXML
    private Button btnSelectDir;
    @FXML
    private Label path;
    @FXML
    private TextField projectName;
    @FXML
    private Button btnSearch;
    @FXML
    private Label noCoreError;

    private String installLocation;
    private String parsingResultLocation;
    private String solrpath;
    private String activeCore;
    private String neo4jPath;

    private Driver driver = GraphDatabase.driver( "bolt://localhost:7687", AuthTokens.basic( "neo4j", "nepal-cartoon-flex-sport-tape-8099" ));
    Session session = driver.session();

    public Controller() throws IOException, ParseException {
        this.getSettings();
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
//        try {
//            this.getSolrCores();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
    }

    public void startSolr() throws IOException, InterruptedException, ParseException {
        //start solr
        Runtime.getRuntime().exec("cmd /c start cmd.exe /K \""+solrpath.substring(0,2) +" && cd "+solrpath+" && solr start -p 8983\"");
        Thread.sleep(10000);
        this.getSolrCores();
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
            this.installLocation = res;
            res = prop.getProperty("parsingResultLocation");
            this.parsingResultLocation = res;
            res = prop.getProperty("solrPath");
            this.solrpath = res;
            res = prop.getProperty("activeCore");
            this.activeCore = res;
            res = prop.getProperty("neo4jPath");
            this.neo4jPath = res;
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public void showFiles(File[] files) throws IOException {
        FileWriter jsonFile;
        int i=0;

        for (File file : files) {
            if (file.isDirectory()) {
                showFiles(file.listFiles()); // Calls same method again.
            } else {
                String fileName = file.getName();
                //kemudian bacalah file nyaa jika .java
                if(fileName.endsWith(".java")) {
                    CompilationUnit compilationUnit;
                    Path codePath = Paths.get(file.getAbsolutePath());

                    try{
                        compilationUnit = StaticJavaParser.parse(Files.readString(codePath));

//                        cacat parsing -> jika ada switch case
//                                tidak dapat parsing dengan benar dengan javaparser




                        //parse nama class
//                        GenericListVisitorAdapter<String, Void> classNameReturn = new ClassNamePrinterReturn();
//                        List<String> listClass = classNameReturn.visit(compilationUnit, null);

                        GenericListVisitorAdapter<JSONObject, Void> fileParser = new FileParser(codePath, this.session);
                        List<JSONObject> objList = fileParser.visit(compilationUnit, null);

                        JSONArray classArr = new JSONArray();
                        for(int x=0; x<objList.size(); x++){
                            classArr.put(objList.get(x));
                        }

                        //create file JSON
                        String pn = projectName.getText();
                        if(!pn.equals("")){
                            File newDir = new File(this.parsingResultLocation+"/"+pn);
                            boolean dirCreated = newDir.mkdir();
                            jsonFile = new FileWriter(this.parsingResultLocation+"/"+pn+"/"+fileName.substring(0, fileName.length() - 5) + ".json");
//                            File cek = new File(this.parsingResultLocation+"/"+pn+"/"+fileName.substring(0, fileName.length()-5)+".json");
//                            if(!cek.exists()) {
//                                jsonFile = new FileWriter(this.parsingResultLocation+"/"+pn+"/"+fileName.substring(0, fileName.length() - 5) + ".json");
//                                System.out.println(cek.getAbsolutePath());
//                            }
//                            else {
//                                jsonFile = new FileWriter(this.parsingResultLocation +"/"+pn+"/"+ fileName.substring(0, fileName.length() - 5) + "_A.json");
//                                System.out.println(cek.getAbsolutePath());
//                            }
                        }else{
                            jsonFile = new FileWriter(this.parsingResultLocation+"/"+pn+"/"+fileName.substring(0, fileName.length() - 5) + ".json");
//                            File cek = new File(this.parsingResultLocation+"/"+pn+"/"+fileName.substring(0, fileName.length()-5)+".json");
//                            if(!cek.exists()) {
//                                jsonFile = new FileWriter(this.parsingResultLocation+"/"+pn+"/"+fileName.substring(0, fileName.length() - 5) + ".json");
//                                System.out.println(cek.getAbsolutePath());
//                            }
//                            else {
//                                jsonFile = new FileWriter(this.parsingResultLocation +"/"+pn+"/"+ fileName.substring(0, fileName.length() - 5) + "_A.json");
//                                System.out.println(cek.getAbsolutePath());
//                            }
                        }
                        jsonFile.write(classArr.toString(4));
//                        System.out.println("\nJSON Object saved to: " + classArr.get("Location"));
                        jsonFile.flush();
                        jsonFile.close();
                    }catch (Exception e){
                        System.out.println("Exception Caught");
                        System.out.println(e.toString());
                    }


                }
            }
            i++;
        }
    }

    public void chooseDirFunc(ActionEvent actionEvent) throws IOException {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("../Java_Code"));

        File selectedDirectory = directoryChooser.showDialog(btnSelectDir.getScene().getWindow());

        if(selectedDirectory != null) {
            path.setText(selectedDirectory.getAbsolutePath());
            showFiles(selectedDirectory.listFiles());
            this.session.close();
            this.driver.close();
        }
        else {
            System.out.println("tidak memilih directory");
        }
    }

    public void goToSearch(ActionEvent actionEvent) throws IOException {
        Stage stage;
        Parent root;

        stage = (Stage) btnSearch.getScene().getWindow();
        root = FXMLLoader.load(getClass().getResource("Search.fxml"));

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void goToSettings(ActionEvent actionEvent) throws IOException {
        Stage stage;
        Parent root;

        stage = (Stage) btnSearch.getScene().getWindow();
        root = FXMLLoader.load(getClass().getResource("Settings.fxml"));

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void getSolrCores() throws IOException, ParseException {
        URL url = new URL("http://localhost:8983/solr/admin/cores?action=STATUS");
        HttpURLConnection getCoreCon = (HttpURLConnection) url.openConnection();
        getCoreCon.setRequestMethod("GET");
        getCoreCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        getCoreCon.setUseCaches(false);
        getCoreCon.setDoOutput(true);
        getCoreCon.setDoOutput(true);

        DataOutputStream wr = new DataOutputStream(
                getCoreCon.getOutputStream()
        );
//        wr.writeBytes(param);
        wr.close();

        InputStream is = getCoreCon.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        StringBuilder response = new StringBuilder();
        String line;
        while((line = rd.readLine()) != null) {
            response.append(line);
            response.append("\r");
        }
        JSONParser parser = new JSONParser();
        org.json.simple.JSONObject json = (org.json.simple.JSONObject) parser.parse(response.toString());
        json = (org.json.simple.JSONObject) json.get("status");
        Set<String> x = json.keySet();
        if(json.size()==0){
            noCoreError.setText("There are no SOLR core/collection(s), please create one in settings");
        }else{
            String c = "You have "+json.size()+" SOLR core(s) available";
            noCoreError.setTextFill(Color.color(0,0,0));
            noCoreError.setText(c);
        }

        rd.close();
    }

    public void chooseIndexing(ActionEvent actionEvent) throws IOException {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(installLocation));

        File selectedDirectory = directoryChooser.showDialog(btnSelectDir.getScene().getWindow());
        if(selectedDirectory != null) {
            String command = "cmd /c start cmd.exe /K \""+"java -jar -Dc="+this.activeCore+" -Dauto "+installLocation+"/post.jar "+selectedDirectory.getAbsolutePath()+"\\*\"";
            System.out.println(command);
            Runtime.getRuntime().exec(command);
        }
        else {
            System.out.println("tidak memilih directory");
        }
    }

    public void startNeo4jService() throws IOException, InterruptedException {
        //start neo4j
        Runtime.getRuntime().exec("cmd /c start cmd.exe /K \""+neo4jPath.substring(0,2) +" && cd "+neo4jPath+" && neo4j console\"");
//        Thread.sleep(10000);
        driver = GraphDatabase.driver( "bolt://localhost:7687", AuthTokens.basic( "neo4j", "nepal-cartoon-flex-sport-tape-8099" ));


//        Session session = driver.session();
//
//        session.run("CREATE (baeldung:Company {name:\"Baeldung\"}) " +
//                "-[:owns]-> (tesla:Car {make: 'tesla', model: 'modelX'})" +
//                "RETURN baeldung, tesla");
//
//        Result res = session.run("MATCH (company:Company)-[:owns]-> (car:Car)" +
//                "WHERE car.make='tesla' and car.model='modelX'" +
//                "RETURN company.name");
//
//        session.close();
//        driver.close();
    }
}