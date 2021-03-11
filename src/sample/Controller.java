package sample;

import ParsingClasses.FileParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.GenericListVisitorAdapter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Controller {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));

    @FXML
    private Button btnSelectDir;
    @FXML
    private Label path;
    @FXML
    private TextField projectName;
    @FXML
    private Button btnSearch;

    public Controller() throws IOException {
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

                        GenericListVisitorAdapter<JSONObject, Void> fileParser = new FileParser();
                        List<JSONObject> objList = fileParser.visit(compilationUnit, null);

                        JSONArray classArr = new JSONArray();
                        for(int x=0; x<objList.size(); x++){
                            classArr.put(objList.get(x));
                        }

                        //create file JSON
                        String pn = projectName.getText();
                        if(!pn.equals("")){
                            File newDir = new File("D:/Kuliah/Hasil/"+pn);
                            boolean dirCreated = newDir.mkdir();
                            jsonFile = new FileWriter("../Hasil/"+pn+"/"+fileName.substring(0, fileName.length()-5)+".json");
                        }else{
                            jsonFile = new FileWriter("../Hasil/"+fileName.substring(0, fileName.length()-5)+".json");
                        }
                        jsonFile.write(classArr.toString(4));
//                        System.out.println("\nJSON Object saved to: " + classArr.get("Location"));
                        jsonFile.flush();
                        jsonFile.close();


//                        //parse nama class
//                        GenericListVisitorAdapter<String, Void> classNameReturn = new ClassNamePrinterReturn();
//                        List<String> listClass = classNameReturn.visit(compilationUnit, null);
//                        //parse nama methods
//                        GenericListVisitorAdapter <String, Void> methodNameReturn = new MethodNamePrinterReturn();
//                        List<String> listMethod = methodNameReturn.visit(compilationUnit, null);
//                        //parse parent class (jika ada)
//                        GenericVisitorAdapter<List<String>, Void> classParentReturn = new ClassParentPrinterReturn();
//                        List<String> classParent = classParentReturn.visit(compilationUnit, null);
////                  //parse implemented class / classes (jika ada)
//                        GenericListVisitorAdapter<String, Void> classImplementedReturn = new ClassImplementedPrinterReturn();
//                        List<String> listImplemented = classImplementedReturn.visit(compilationUnit, null);
//
//
//
//                        //Buat object JSON
//                        JSONObject obj = new JSONObject();
//
//                        JSONArray classes = new JSONArray();
//                        if(listClass.size() > 0) {
//                            for(int a=0; a<listClass.size(); a+=3){
//                                JSONObject o = new JSONObject();
//                                o.put("ClassName", listClass.get(a));
//                                o.put("LineNum", listClass.get(a+1));
//                                o.put("ColNum", listClass.get(a+2));
////                            String result = o.toJSONString();
//                                classes.put(o);
//                            }
//                        }
//
//                        JSONArray methods = new JSONArray();
//                        if(listMethod.size() > 0){
//                            for(int a=0; a<listMethod.size(); a+=2) {
//                                JSONObject o = new JSONObject();
//                                o.put("MethodName", listMethod.get(a));
//                                String listParamString = listMethod.get(a+1);
////                                listParamString = listParamString.replace(" ", "");
//                                listParamString = listParamString.replace("[", "");
//                                listParamString = listParamString.replace("]", "");
//                                String[] listParam;
//                                listParam = listParamString.split(",");
//                                JSONArray params = new JSONArray();
//                                for (int z=0; z<listParam.length; z++) {
//                                    params.put(listParam[z]);
//                                }
//                                o.put("MethodParams", params);
////                                System.out.println("object di put"+ o);
//                                methods.put(o);
//                            }
//
//                        }
//
//                        JSONArray implementedArray = new JSONArray();
//                        if(listImplemented.size() > 0){
//                            for(int a=0; a<listImplemented.size(); a++)
//                                implementedArray.put(listImplemented.get(a));
//                        }
//
//
//                        //tambahkan ke JSON hasil
//                        if(classParent != null && classParent.get(0) != null){
//                            obj.put("Parent", classParent.get(0));
//                        }
//                        if(methods != null && methods.length()>0)
//                            obj.put("Methods", methods);
//                        if(implementedArray != null && implementedArray.length()>0)
//                            obj.put("Implements", implementedArray);
//                        if(classes != null && classes.length()>0)
//                            obj.put("Classes",classes);
//
//                        obj.put("Location", codePath.toString());
//
//                        obj.toString(4);
//
//                        //create file JSON
//                        String pn = projectName.getText();
//                        if(!pn.equals("")){
//                            File newDir = new File("D:/Kuliah/Hasil/"+pn);
//                            boolean dirCreated = newDir.mkdir();
//                            jsonFile = new FileWriter("../Hasil/"+pn+"/"+fileName.substring(0, fileName.length()-5)+".json");
//                        }else{
//                            jsonFile = new FileWriter("../Hasil/"+fileName.substring(0, fileName.length()-5)+".json");
//                        }
//                        jsonFile.write(obj.toString(4));
//                        System.out.println("\nJSON Object saved to: " + obj.get("Location"));
//                        jsonFile.flush();
//                        jsonFile.close();
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


            //unused temporary
            //loop files from input
//            for(int i=0; i<files.size(); i++){
//                CompilationUnit compilationUnit;
//                Path codePath = Paths.get(files.get(i).getAbsolutePath());
//                compilationUnit = StaticJavaParser.parse(Files.readString(codePath));
//                VoidVisitor<Void> methodNameVisitor = new MethodNamePrinter();
//                System.out.println("File ke-"+i);
//                methodNameVisitor.visit(compilationUnit, null);
//            }
//            CompilationUnit compilationUnit;
//            Path codePath = Paths.get(files.get(0).getAbsolutePath());
//            compilationUnit = StaticJavaParser.parse(Files.readString(codePath));
//            VoidVisitor<Void> methodNameVisitor = new MethodNamePrinter();
//            methodNameVisitor.visit(compilationUnit, null);
            // Parse all source files
//            SourceRoot sourceRoot = new SourceRoot();
//            sourceRoot.setParserConfiguration(parserConfiguration);
//            List<ParseResult> parseResults = sourceRoot.tryToParse("");

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
}