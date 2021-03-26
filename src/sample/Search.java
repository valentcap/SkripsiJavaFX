package sample;

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
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.MapSolrParams;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class Search {
    private static final java.util.UUID UUID = null;
    FXMLLoader loader = new FXMLLoader(getClass().getResource("search.fxml"));
//    final SolrClient client;
    String core = "";


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
    }


    //add document manual
    public void addDocument() throws IOException, SolrServerException {
        final SolrClient client = getSolrClient();

        final SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", UUID.randomUUID().toString());
        doc.addField("name", "tambahan");

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
        if(term.getText().length()>0){
            inputKeyword = term.getText();
            term.setText("");
        }

        final Map<String, String> queryParamMap = new HashMap<String, String>();
        //query
        queryParamMap.put("q", "ClassName:*"+inputKeyword+"*");
        queryParamMap.put("fl", "ClassName, LineNum, ColNum, Parent");
        queryParamMap.put("sort", "id asc");
        MapSolrParams queryParams = new MapSolrParams(queryParamMap);

        final QueryResponse response = client.query(this.core, queryParams);
        final SolrDocumentList documents = response.getResults();
        System.out.println("Found " + documents.getNumFound() + " documents");
//        print("Found " + documents.getNumFound() + " documents");
        for(SolrDocument document : documents) {
            String className = (String) document.getFirstValue("ClassName");
            String colNum = Long.toString((Long) document.getFirstValue("ColNum"));
            String lineNum = Long.toString((Long) document.getFirstValue("LineNum"));
            String parent = "";
            if(document.get("Parent") != null) {
                parent = document.get("Parent").toString();
                parent = parent.substring(1, parent.length() - 1);
            }
            System.out.println("ClassName: "+className);
            System.out.println("Column Number: "+colNum);
            System.out.println("Line Number: "+lineNum);
            if(parent != "")
                System.out.println("Parent Class: "+ parent);
//
            if(parent!=""){
                term.setText(parent);
                this.searching();
            }

            //safe
//            System.out.println(document);

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
