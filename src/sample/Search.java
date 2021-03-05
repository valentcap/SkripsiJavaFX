package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
import java.util.Set;

public class Search {
    private static final java.util.UUID UUID = null;
    FXMLLoader loader = new FXMLLoader(getClass().getResource("search.fxml"));
//    final SolrClient client;


    @FXML
    private Button btnBack;
    @FXML
    private TextField keyword;

    public Search() throws IOException, SolrServerException, ParseException {
//        client = this.getSolrClient();
        this.addDocument();
//        this.searching();
        this.getSolrCores();
    }


    //add document manual
    public void addDocument() throws IOException, SolrServerException {
        final SolrClient client = getSolrClient();

        final SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", UUID.randomUUID().toString());
        doc.addField("name", "tambahan");

        final UpdateResponse updateResponse = client.add("project" ,doc);
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

        final Map<String, String> queryParamMap = new HashMap<String, String>();
        //query
        queryParamMap.put("q", "name:*"+inputKeyword+"*");
        queryParamMap.put("fl", "id, name");
        queryParamMap.put("sort", "id asc");
        MapSolrParams queryParams = new MapSolrParams(queryParamMap);

        final QueryResponse response = client.query("project", queryParams);
        final SolrDocumentList documents = response.getResults();
        System.out.println("Found " + documents.getNumFound() + " documents");
//        print("Found " + documents.getNumFound() + " documents");
        for(SolrDocument document : documents) {
            final String id = (String) document.getFirstValue("id");
            final String name = (String) document.getFirstValue("name");
//            print("id: " + id + "; name: " + name);
            System.out.println("id: " + id + "; name: " + name);
        }
    }

    public void addJSON() throws IOException {
        String urlString = "http://localhost:8983/solr/gettingstarted/update";
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        String command = "curl 'http://localhost:8983/solr/gettingstarted/update?commit=true' --data-binary @example/exampledocs/books.json -H 'Content-type:application/json'";
        ProcessBuilder pb = new ProcessBuilder(command.split(" "));

        pb.directory(new File("/home/"));
        Process process = pb.start();

        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type","application/json");


        con.setDoOutput(true);


    }

    public void startSolr() throws IOException {
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
            System.out.println("core ke-"+i+"= "+x.toArray()[i]);
        }

        rd.close();
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
