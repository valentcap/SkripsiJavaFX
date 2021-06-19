package sample;

import ParsingClasses.SearchResultObject;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.MapSolrParams;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.neo4j.driver.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class Search extends Application {
    private static final java.util.UUID UUID = null;
    FXMLLoader loader = new FXMLLoader(getClass().getResource("search.fxml"));
//    final SolrClient client;
    String core = "";


    //neo4j
    private Driver driver = GraphDatabase.driver( "bolt://localhost:7687", AuthTokens.basic( "neo4j", "nepal-cartoon-flex-sport-tape-8099" ));
    Session session = driver.session();


    @FXML
    private Button btnBack;
    @FXML
    private TextField keyword;
    @FXML
    private Label term;
    @FXML
    private VBox searchResultArea;
    @FXML
    private Label docNum;

    //custom element
    Label mainLabel = new Label("Class");
    ListView<TableView> listView = new ListView();
    Label relatedLabel = new Label("Class/Interface yang Berelasi");
    ListView<String> listViewRelated = new ListView();
    Label nextLabel = new Label("Hasil Pencarian Lain");
    ListView<String> listViewNext = new ListView();

    TableView tableView = new TableView();
    TableView tableViewRelated = new TableView();
    TableView tableViewNext = new TableView();


        //constructor
    public Search() throws IOException, SolrServerException, ParseException {
        this.getSettings();
    }

    @Override
    public void start(Stage stage) throws Exception {

    }


    public HttpSolrClient getSolrClient(){
        final String solrUrl = "http://localhost:8983/solr";
        return new HttpSolrClient.Builder(solrUrl).withConnectionTimeout(10000).withSocketTimeout(60000).build();
    }

    public void searching() throws IOException, SolrServerException {
        tableView.getItems().clear();
        tableView.getColumns().clear();
        tableViewRelated.getItems().clear();
        tableViewRelated.getColumns().clear();
        tableViewNext.getItems().clear();
        tableViewNext.getColumns().clear();
        searchResultArea.getChildren().clear();
        Vector<SearchResultObject> hasilPencarian = new Vector<>();
        SearchResultObject firstResult = null;
        Vector<SearchResultObject> hasilNeo4j = new Vector<>();
        final SolrClient client = getSolrClient();
        String inputKeyword = keyword.getText();
        String[] inputs = inputKeyword.split(",");
        Vector<String> neo4jClassNames = new Vector<>();

        //method info (name & return)
        String methodNameSearch = "";
        String methodReturnSearch = "";

        String query = "";
//        if(inputs.length == 1){
//            query = "ClassName:"+inputs[0];
//        }else{
            String temp = "";
            for(int k=0; k<inputs.length; k++){
                temp = inputs[k];
                if(k!=0) query += " AND ";
                temp = temp.replaceAll("\\s+", "");
                if(temp.contains("method:")){
                    methodNameSearch = temp.substring(temp.indexOf(":")+1, temp.length());
//                    System.out.println("Test name = "+methodNameSearch);
                    temp = temp.replace("method:", "Methods.MethodName: ");
//                    temp += "*";
                    query += temp;
                }else if(temp.contains("return:")){
                    methodReturnSearch = temp.substring(temp.indexOf(":")+1, temp.length());
//                    System.out.println("Test return = "+methodReturnSearch);
                    temp = temp.replace("return:", "Methods.MethodReturnType: ");
//                    temp += "* ";
                    query += temp;
                }else{
                    temp = "ClassName: "+temp;
                    query += temp;
                }
            }
//        }
        //SEARCHING SOLR
        final Map<String, String> queryParamMap = new HashMap<String, String>();
        //query

        queryParamMap.put("q", query);
        queryParamMap.put("fl", "ClassName, LineNum, ColNum, Location, Methods.MethodName, Methods.MethodReturnType");
        queryParamMap.put("rows", "999");
        MapSolrParams queryParams = new MapSolrParams(queryParamMap);

        final QueryResponse response = client.query(this.core, queryParams);
        final SolrDocumentList documents = response.getResults();
        for(int x=0; x<documents.size(); x++) {
            String className = (String) documents.get(x).getFirstValue("ClassName");

            String colNum = Long.toString((Long) documents.get(x).getFirstValue("ColNum"));
            String lineNum = Long.toString((Long) documents.get(x).getFirstValue("LineNum"));
            String parent = "";
            String implemented = "";
            String location = (String) documents.get(x).getFirstValue("Location");

            ArrayList<String> methods = (ArrayList<String>) documents.get(x).get("Methods.MethodName");
            ArrayList<String> methodsReturn = (ArrayList<String>) documents.get(x).get("Methods.MethodReturnType");
//            System.out.println("METHODS = " + methods.toString().toLowerCase());
//            System.out.println("RETURN TYPES = " + methodsReturn.toString().toLowerCase());
            int isQualified = 0;
            //cocokkan dengan query
            if(methodNameSearch != "" && methodReturnSearch != ""){
                for(int i=0; i< methods.size(); i++) {
                    //cek jika cocok
                    String methodNameSearchTemp = methodNameSearch.replace("*", "").toLowerCase();
                    String methodReturnSearchTemp = methodReturnSearch.replace("*", "").toLowerCase();
                    if (methods.get(i).toLowerCase().contains(methodNameSearchTemp) && methodsReturn.get(i).toLowerCase().contains(methodReturnSearchTemp)) {
                        //urutan tampilan, first result -> hasil neo4j, hasilPencarian
                        if (x == 0) {
                            firstResult = new SearchResultObject(className, location);
                        } else {
                            boolean flag = true;
                            for(int iter = 0; iter < hasilPencarian.size(); iter++){
                                if(hasilPencarian.get(iter).getClassName().equals(className)) {
                                    flag = false;
                                    break;
                                }
                            }
                            if(flag)
                            hasilPencarian.add(new SearchResultObject(className, location));

                        }
                    } else {
//                        System.out.println(methods.get(i));
//                        System.out.println(methodsReturn.get(i));
                    }
                }
            }else{
                //urutan tampilan, first result -> hasil neo4j, hasilPencarian
                if(x==0) {
                    firstResult = new SearchResultObject(className, location);
                }else{
                    boolean flag = true;
                    for(int iter = 0; iter < hasilPencarian.size(); iter++){
                        if(hasilPencarian.get(iter).getClassName().equals(className)) {
                            flag = false;
                            break;
                        }
                    }
                    if(flag)
                        hasilPencarian.add(new SearchResultObject(className, location));
                }
            }

        }
        //SEARCHING GRAPH (neo4j)
        if(firstResult!= null){
            //all connected
//            String neo4jCommand = "MATCH (n {name :'"+firstResult.getClassName()+"' })-[*]-(connected)\n" +
            //all lower level
            String neo4jCommand = "MATCH (n {name :'"+firstResult.getClassName()+"' })-[*]->(connected)\n" +
                    "RETURN  n.name, n.location, connected.name, connected.location";
            Result graphResult = session.run(neo4jCommand);

            String neo4jCommand2 = "MATCH (n {name :'"+firstResult.getClassName()+"' })<-[*]-(connected)\n" +
                    "RETURN  n.name, n.location, connected.name, connected.location";
            Result graphResult2 = session.run(neo4jCommand2);

            //print hasil pencarian

            while(graphResult.hasNext()) {
                Map<String, Object> row = graphResult.next().asMap();
                if(row.get("connected.name") != null && row.get("connected.location") != null) {
                    hasilNeo4j.add(new SearchResultObject(row.get("connected.name").toString(), row.get("connected.location").toString()));
                    neo4jClassNames.add(row.get("connected.name").toString());
                }else{
                    String x = "Class/Interface Diluar Repository";
                    hasilNeo4j.add(new SearchResultObject(row.get("connected.name").toString(), x));
                    neo4jClassNames.add(row.get("connected.name").toString());
                }
            }
            while(graphResult2.hasNext()) {
                Map<String, Object> row = graphResult2.next().asMap();
                if(row.get("connected.name") != null && row.get("connected.location") != null) {
                    hasilNeo4j.add(new SearchResultObject(row.get("connected.name").toString(), row.get("connected.location").toString()));
                    neo4jClassNames.add(row.get("connected.name").toString());
                }else{
                    String x = "Class/Interface Diluar Repository";
                    hasilNeo4j.add(new SearchResultObject(row.get("connected.name").toString(), x));
                    neo4jClassNames.add(row.get("connected.name").toString());
                }
            }
        }

        if(firstResult != null){
            System.out.println("--------HASIL UTAMA--------");
            System.out.println(firstResult.getClassName());
//            System.out.println(firstResult.getLocation());
            System.out.println("--------HASIL NEO4J--------");
            for(int j=0; j<hasilNeo4j.size(); j++){
                System.out.println(hasilNeo4j.get(j).getClassName());
//                System.out.println(hasilNeo4j.get(j).getLocation());
            }
            System.out.println("--------HASIL NEXT SOLR--------");
            for(int j=0; j<hasilPencarian.size(); j++){
                System.out.println(hasilPencarian.get(j).getClassName());
//                System.out.println(hasilPencarian.get(j).getLocation());
            }


            //hasil utama
            TableColumn<SearchResultObject, String> column1 = new TableColumn<>("Nama Class");
            column1.setCellValueFactory(new PropertyValueFactory<>("className"));
            TableColumn<SearchResultObject, String> column2 = new TableColumn<>("Lokasi File");
            column2.setCellValueFactory(new PropertyValueFactory<>("location"));

            tableView.getColumns().add(column1);
            tableView.getColumns().add(column2);
            tableView.getItems().add(firstResult);
            for(int a=0; a<hasilPencarian.size(); a++){
                //remove duplicates search result
                if(neo4jClassNames.contains(hasilPencarian.get(a).getClassName())){
                    //
                }else {
                    tableView.getItems().add(hasilPencarian.get(a));
                }
            }
            tableView.setOnMouseClicked(eventHandlerMain);
            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            TableColumn<SearchResultObject, String> column3 = new TableColumn<>("Nama Class yang Berelasi");
            column3.setCellValueFactory(new PropertyValueFactory<>("className"));
            TableColumn<SearchResultObject, String> column4 = new TableColumn<>("Lokasi File yang Berelasi");
            column4.setCellValueFactory(new PropertyValueFactory<>("location"));

            TableColumn<SearchResultObject, String> column5 = new TableColumn<>("Nama Class");
            column5.setCellValueFactory(new PropertyValueFactory<>("className"));
            TableColumn<SearchResultObject, String> column6 = new TableColumn<>("Lokasi File");
            column6.setCellValueFactory(new PropertyValueFactory<>("location"));

            tableViewRelated.getColumns().add(column3);
            tableViewRelated.getColumns().add(column4);
            for(int a=0; a<hasilNeo4j.size(); a++){
                SearchResultObject sro = hasilNeo4j.get(a);
                tableViewRelated.getItems().add(hasilNeo4j.get(a));
            }
            tableViewRelated.setOnMouseClicked(eventHandlerRelated);
            tableViewRelated.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

//            tableViewNext.getColumns().add(column5);
//            tableViewNext.getColumns().add(column6);
//            for(int a=0; a<hasilPencarian.size(); a++){
//                tableViewNext.getItems().add(hasilPencarian.get(a));
//            }
            tableViewNext.setOnMouseClicked(eventHandlerNext);
            tableViewNext.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            mainLabel.setTextFill(Paint.valueOf("#00ff00"));
            mainLabel.setStyle("-fx-font: 14 arial");
            mainLabel.setPadding(new Insets(5, 5, 5, 5));
            searchResultArea.getChildren().add(mainLabel);
            searchResultArea.getChildren().add(tableView);
            relatedLabel.setText("Class/Interface yang Berelasi dengan " + firstResult.getClassName());
            relatedLabel.setTextFill(Paint.valueOf("#00ff00"));
            relatedLabel.setStyle("-fx-font: 14 arial");
            relatedLabel.setPadding(new Insets(5, 5, 5, 5));
            searchResultArea.getChildren().add(relatedLabel);
            searchResultArea.getChildren().add(tableViewRelated);

            int x = firstResult == null ? (1 + hasilNeo4j.size() + hasilPencarian.size()) : 0;
            docNum.setText(x == 0 ? "" : x + " documents retieved");

        }else{
            System.out.println("----------+-----------");
            System.out.println("Tidak Ditemukan Hasil");
            System.out.println("----------+-----------");

            Label noResult = new Label("Tidak ditemukan hasil pencarian");
            noResult.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
            noResult.setPadding(new Insets(0, 0, 0, 110));
            searchResultArea.getChildren().add(noResult);
        }

    }

    EventHandler<MouseEvent> eventHandlerMain = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent e) {
            SearchResultObject temp = (SearchResultObject) tableView.getSelectionModel().getSelectedItem();
            String x = temp.getLocation();
            if(e.getClickCount() == 2){
                selectSearch(x);
            }
            tableViewRelated.getItems().clear();
            tableViewRelated.getColumns().clear();
            searchResultArea.getChildren().clear();

            TableColumn<SearchResultObject, String> column7 = new TableColumn<>("Nama Class yang Berelasi");
            column7.setCellValueFactory(new PropertyValueFactory<>("className"));
            TableColumn<SearchResultObject, String> column8 = new TableColumn<>("Lokasi File");
            column8.setCellValueFactory(new PropertyValueFactory<>("location"));
            tableViewRelated.getColumns().add(column7);
            tableViewRelated.getColumns().add(column8);
            tableViewRelated.setOnMouseClicked(eventHandlerRelated);
            tableViewRelated.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            String neo4jCommand = "MATCH (n {name :'"+temp.getClassName()+"' })-[*]->(connected)\n" +
                    "RETURN  n.name, n.location, connected.name, connected.location";
            Result graphRes = session.run(neo4jCommand);

            String neo4jCommand2 = "MATCH (n {name :'"+temp.getClassName()+"' })<-[*]-(connected)\n" +
                    "RETURN  n.name, n.location, connected.name, connected.location";
            Result graphRes2 = session.run(neo4jCommand2);

            Vector<SearchResultObject> hasilReSearch= new Vector<>();
            while(graphRes.hasNext()) {
                Map<String, Object> row = graphRes.next().asMap();
                if(row.get("connected.name") != null && row.get("connected.location") != null) {
                    hasilReSearch.add(new SearchResultObject(row.get("connected.name").toString(), row.get("connected.location").toString()));
                }else{
                    String str = "Class/Interface Diluar Repository";
                    hasilReSearch.add(new SearchResultObject(row.get("connected.name").toString(), str));
                }
            }
            while(graphRes2.hasNext()) {
                Map<String, Object> row = graphRes2.next().asMap();
                if(row.get("connected.name") != null && row.get("connected.location") != null) {
                    hasilReSearch.add(new SearchResultObject(row.get("connected.name").toString(), row.get("connected.location").toString()));
                }else{
                    String str = "Class/Interface Diluar Repository";
                    hasilReSearch.add(new SearchResultObject(row.get("connected.name").toString(), str));
                }
            }


            for(int a=0; a<hasilReSearch.size(); a++){
                tableViewRelated.getItems().add(hasilReSearch.get(a));
            }
            searchResultArea.getChildren().add(mainLabel);
            searchResultArea.getChildren().add(tableView);
            searchResultArea.getChildren().add(relatedLabel);
            relatedLabel.setText("Class/Interface yang Berelasi dengan " + temp.getClassName());
            searchResultArea.getChildren().add(tableViewRelated);
        }
    };
    EventHandler<MouseEvent> eventHandlerRelated = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent e) {
            if(e.getClickCount() == 2) {
                SearchResultObject temp = (SearchResultObject) tableViewRelated.getSelectionModel().getSelectedItem();
                String x = temp.getLocation();
                selectSearch(x);
            }
        }
    };
    EventHandler<MouseEvent> eventHandlerNext = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent e) {

            SearchResultObject temp = (SearchResultObject) tableViewNext.getSelectionModel().getSelectedItem();
            String x = temp.getLocation();
            selectSearch(x);
        }
    };

    public void selectSearch(String fileLocation){
        if(fileLocation.equals("Class/Interface Diluar Repository")){

        }else {
            File file = new File(fileLocation);
            HostServices hostServices = getHostServices();
            hostServices.showDocument(file.getAbsolutePath());
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

            res = prop.getProperty("activeCore");
            this.core = res;
        } catch (IOException io) {
            io.printStackTrace();
        }
    }


    public void startSolr() throws IOException, ParseException {
        Runtime.getRuntime().exec("cmd /c start cmd.exe /K \"D: && cd solr-8.6.0\\bin && solr start -p 8983\"");
//        Runtime.getRuntime().exec("cmd /c start cmd.exe /K \"D: && cd solr-8.6.0\\bin && solr start -e cloud\"");
    }

    public void getSolrCores() throws IOException, ParseException {
            URL url = new URL("http://localhost:8983/solr/admin/cores?action=STATUS");
            HttpURLConnection getCoreCon = (HttpURLConnection) url.openConnection();
            getCoreCon.setRequestMethod("GET");
            getCoreCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            getCoreCon.setUseCaches(false);
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
            JSONObject json = (JSONObject) parser.parse(response.toString());
            json = (JSONObject) json.get("status");
            Set<String> x = json.keySet();
            for(int i=0; i<json.size(); i++){
                System.out.println("core/collection ke-"+i+"= "+x.toArray()[i]);
            }

            rd.close();
    }

    public void deleteDocuments() throws IOException, SolrServerException {
        String urlString = "http://localhost:8983/solr/"+this.core;
        SolrClient solrClient = new HttpSolrClient.Builder(urlString).build();
        //delete all documents
        solrClient.deleteByQuery("*");
        solrClient.commit();

        String neo4jDeleteQuery = "MATCH (n)\n" +
                "DETACH DELETE n";
        session.run(neo4jDeleteQuery);
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
