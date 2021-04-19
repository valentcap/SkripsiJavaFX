package sample;

import ParsingClasses.SearchResultObject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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

public class Search {
    private static final java.util.UUID UUID = null;
    FXMLLoader loader = new FXMLLoader(getClass().getResource("search.fxml"));
//    final SolrClient client;
    String core = "";
    Vector<SearchResultObject> hasilPencarian = new Vector<>();
    SearchResultObject firstResult = null;
    Vector<SearchResultObject> hasilNeo4j = new Vector<>();

    //neo4j
    private Driver driver = GraphDatabase.driver( "bolt://localhost:7687", AuthTokens.basic( "neo4j", "nepal-cartoon-flex-sport-tape-8099" ));
    Session session = driver.session();


    @FXML
    private Button btnBack;
    @FXML
    private TextField keyword;
    @FXML
    private Label term;

        //constructor
    public Search() throws IOException, SolrServerException, ParseException {
//        client = this.getSolrClient();
//        this.addDocument();
//        this.searching();
//        this.getSolrCores();
        this.getSettings();
//        this.addDocument();
    }


    //add document manual
    public void addDocument() throws IOException, SolrServerException {
        final SolrClient client = getSolrClient();

        final SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", "D");
        doc.addField("foo", 20);
        doc.addField("out_edge", "4");
        doc.addField("out_edge", "7");

        doc.addField("in_edge", "3");
        doc.addField("in_edge", "5");



        final UpdateResponse updateResponse = client.add(this.core,doc);
// Indexed documents must be committed
        client.commit("project");
    }

    public HttpSolrClient getSolrClient(){
        final String solrUrl = "http://localhost:8983/solr";
        return new HttpSolrClient.Builder(solrUrl).withConnectionTimeout(10000).withSocketTimeout(60000).build();
    }

    public void searching() throws IOException, SolrServerException {
        final SolrClient client = getSolrClient();
        String inputKeyword = keyword.getText();

        //SEARCHING SOLR
        final Map<String, String> queryParamMap = new HashMap<String, String>();
        //query

        queryParamMap.put("q", "ClassName:*"+inputKeyword+"*");
        queryParamMap.put("fl", "ClassName, LineNum, ColNum, Parent, Implements, Location");
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


            if(x==0) {
                firstResult = new SearchResultObject(className, location);
            }else{
                hasilPencarian.add(new SearchResultObject(className, location));
            }
            //check parent
            if(documents.get(x).get("Parent") != null) {
                parent = documents.get(x).get("Parent").toString();
                parent = parent.substring(1, parent.length() - 1);
            }
            //check implemented classes
            if(documents.get(x).get("Implements") != null){
                implemented = documents.get(x).get("Implements").toString();
                implemented = implemented.substring(1, implemented.length()-1);
                implemented = implemented.replace(" ", "");
                String[] iArr;
                iArr = implemented.split(",");
                //loop for every implemented class
                for(int y=0; y<iArr.length; y++){
                    System.out.println(iArr[y]);
                }

            }
            //safe
//            System.out.println(document);
        }
        //SEARCHING GRAPH (neo4j)
        String neo4jCommand = "MATCH (n {name :'"+firstResult.getClassName()+"' })-[*]-(connected)\n" +
                "RETURN  n.name, n.location, connected.name, connected.location";
        Result graphResult = session.run(neo4jCommand);
        //print raw graph result
//        while(graphResult.hasNext()) {
//            Map<String, Object> row = graphResult.next().asMap();
//            System.out.println(row);
//        }


        //print hasil pencarian
        Map<String, Object> firstRow = null;
        if(graphResult.hasNext()){
            firstRow = graphResult.next().asMap();
            if(firstRow.get("n.name") != null && firstRow.get("n.location") != null){
                hasilNeo4j.add(new SearchResultObject(firstRow.get("n.name").toString(), firstRow.get("n.location").toString()));
            }
            if(firstRow.get("connected.name") != null && firstRow.get("connected.location") != null) {
                hasilNeo4j.add(new SearchResultObject(firstRow.get("connected.name").toString(), firstRow.get("connected.location").toString()));
            }
        }
        while(graphResult.hasNext()) {
            Map<String, Object> row = graphResult.next().asMap();
            if(row.get("connected.name") != null && row.get("connected.location") != null) {
                hasilNeo4j.add(new SearchResultObject(row.get("connected.name").toString(), row.get("connected.location").toString()));
            }
        }

        System.out.println("--------HASIL UTAMA--------");
        System.out.println(firstResult.getClassName());
        System.out.println(firstResult.getLocation());
        System.out.println("--------HASIL NEO4J--------");
        for(int j=0; j<hasilNeo4j.size(); j++){
            System.out.println(hasilNeo4j.get(j).getClassName());
            System.out.println(hasilNeo4j.get(j).getLocation());
        }
        System.out.println("--------HASIL SISA SOLR--------");
        for(int j=0; j<hasilPencarian.size(); j++){
            System.out.println(hasilPencarian.get(j).getClassName());
            System.out.println(hasilPencarian.get(j).getLocation());
        }

    }

    public void parentSearching(String parent){
        System.out.println("Cari dari parent: "+parent);
    }

    public void implementsSearching(String[] implemented){
        for(int i=0; i<implemented.length; i++){
            System.out.println("Pencarian dari implement: "+implemented[i]);
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
