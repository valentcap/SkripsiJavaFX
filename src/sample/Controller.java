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
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

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
        Runtime.getRuntime().exec("cmd /c start cmd.exe /K \""+solrpath.substring(0,2) +" && cd "+solrpath+"\\bin && solr start -p 8983\"");
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
//                        System.out.println("CUUU\n"+compilationUnit.);
//                        cacat parsing -> jika ada switch case
//                                tidak dapat parsing dengan benar dengan javaparser


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
//            this.session.close();
//            this.driver.close();
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

        rd.close();
    }

    public String fieldGetter(org.json.simple.JSONObject jsonObject, String field){

        return "";
    }

    public void chooseIndexing(ActionEvent actionEvent) throws IOException, ParseException {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(parsingResultLocation));

        File selectedDirectory = directoryChooser.showDialog(btnSelectDir.getScene().getWindow());
        if(selectedDirectory != null) {
            String command = "cmd /c start cmd.exe /K \""+"java -jar -Dc="+this.activeCore+" -Dauto "+installLocation+"/post.jar "+selectedDirectory.getAbsolutePath()+"\\*\"";
            Runtime.getRuntime().exec(command);
            File[] files = selectedDirectory.listFiles();
            String neo4jQuery = "";
            for (File file : files) {
                JSONParser jparser = new JSONParser();
                Object x = jparser.parse(new FileReader(file.getAbsolutePath()));
                org.json.simple.JSONArray res = (org.json.simple.JSONArray) x;





                for(int i=0; i<res.size(); i++){
                    org.json.simple.JSONObject temp = (org.json.simple.JSONObject) res.get(i);
                    //putting to graph DB (neo4j)
                    String interfaceOrClass = (temp.get("InterfaceOrClass").toString() == null) ? "" : temp.get("InterfaceOrClass").toString();

                    String className = (temp.get("ClassName") == null) ? "" : temp.get("ClassName").toString();

                    String accessSpecifier = (temp.get("AccessSpecifier") == null) ? "" : temp.get("AccessSpecifier").toString();

                    String location = (temp.get("Location") == null) ? "" : temp.get("Location").toString().replace("\\", "\\\\");

                    String colNum = (temp.get("ColNum") == null) ? "" : temp.get("ColNum").toString();

                    String lineNum = (temp.get("LineNum") == null) ? "" : temp.get("LineNum").toString();

                    String fields = (temp.get("Fields") == null) ? "" : temp.get("Fields").toString();
                    fields = fields.substring(1, fields.length()-1);
                    String[] fieldArr = fields.split(",");

                    String implemented = (temp.get("Implements") == null) ? "" : temp.get("Implements").toString();
                    implemented = implemented.substring(1, implemented.length()-1);
                    String[] implementedArr = implemented.split(",");


                    String parent = (temp.get("Parent") == null) ? "" : temp.get("Parent").toString();

                    //initial insert Node
                    //putting to graph DB (neo4j)

                    String neo4jCommand = "MERGE (n:"+interfaceOrClass+" {name:\""+className+"\"}) ";

                    //cek jika memiliki parent
                    if(!parent.equals("")){
                        neo4jCommand += "MERGE (parent:"+interfaceOrClass+" {name:\""+parent+"\"}) ";
                        neo4jCommand += "MERGE (n)-[:extends]->(parent) ";
                    }

                    if(implementedArr.length > 0){

                        for(int j=0; j<implementedArr.length; j++){
                            if(implementedArr[j].length()>0){
                                String iA = implementedArr[j].substring(1, implementedArr[j].length()-1);
                                neo4jCommand += "MERGE (implemented"+j+":Interface {name:\""+iA+"\"}) ";
                                neo4jCommand += "MERGE (n)-[:implements]->(implemented"+j+") ";
                            }
                        }
                    }
                    session.run(neo4jCommand);

                    //temp.get("LineNum")
//                    String neo4jCommand = "";


                    neo4jQuery = "MERGE (n {name: '"+className+"'}) "+
                    "SET n = {" +
                            "name: '"+className+"', " +
                            "identifier: '"+accessSpecifier+"', "+
                            "location: '"+location+"', " +
                            "colNum: '"+colNum+"', "+
                            "lineNum: '"+lineNum+"'"+
                            "} "+
                    "RETURN n";
                    session.run(neo4jQuery);
                }

            }
        }
        else {
            System.out.println("tidak memilih directory");
        }
    }

    public void startNeo4jService() throws IOException, InterruptedException {
        //start neo4j
        Runtime.getRuntime().exec("cmd /c start cmd.exe /K \""+neo4jPath.substring(0,2) +" && cd "+neo4jPath+" && neo4j console\"");
//        Thread.sleep(10000);p
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