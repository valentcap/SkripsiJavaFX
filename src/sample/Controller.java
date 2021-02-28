package sample;

import ParsingClasses.ClassNamePrinter;
import ParsingClasses.ClassNamePrinterReturn;
import ParsingClasses.MethodNamePrinter;
import ParsingClasses.MethodNamePrinterReturn;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.ast.visitor.VoidVisitor;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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
    private Button selectDir;
    @FXML
    private Label path;

    public Controller() throws IOException {
    }


    public static void showFiles(File[] files) throws IOException {
//        Vector<File> temp = new Vector<File>();
        FileWriter jsonFile;
        int i=0;
//        System.out.println("--------New Call---------");
        for (File file : files) {
            if (file.isDirectory()) {
//                System.out.println("Directory: " + file.getName());
                showFiles(file.listFiles()); // Calls same method again.
            } else {
                System.out.println("---------new file---------");
                String fileName = file.getName();
                //kemudian bacalah file nyaa jika .java
                if(fileName.endsWith(".java")) {
                    System.out.println("File =" + fileName);
//                    temp.add(file);
                    CompilationUnit compilationUnit;
                    Path codePath = Paths.get(file.getAbsolutePath());

                    compilationUnit = StaticJavaParser.parse(Files.readString(codePath));
                    VoidVisitor<Void> methodNameVisitor = new MethodNamePrinter();
                    VoidVisitor<Void> classNameVisitor = new ClassNamePrinter();


                    GenericVisitorAdapter<List<String>, Void> classNameReturn = new ClassNamePrinterReturn();
                    List<String> listClass = classNameReturn.visit(compilationUnit, null);

                    GenericVisitorAdapter<List<String>, Void> methodNameReturn = new MethodNamePrinterReturn();
                    List<String> listMethod = methodNameReturn.visit(compilationUnit, null);
//                    System.out.println("Nama class=" + listClass.get(0));
//                    System.out.println("Method=" + listMethod.get(0));
//                    System.out.println(l);
//                    classNameVisitor.visit(compilationUnit, null);
//                    methodNameVisitor.visit(compilationUnit, null);

                    //Buat object JSON
                    JSONObject obj = new JSONObject();
                    //JSON Arrays
                    JSONArray classes = new JSONArray();
                    for(int a=0; a<listClass.size(); a++)
                        classes.add("ClassName: "+listClass.get(a));
                    JSONArray methods = new JSONArray();
                    for(int a=0; a<listMethod.size(); a++)
                        methods.add("Method: "+listMethod.get(a));
                    obj.put("class",classes);
                    obj.put("Methods", methods);
                    obj.put("location", file.getAbsolutePath());

                    //create file JSON
                    jsonFile = new FileWriter("../Hasil/"+fileName.substring(0, fileName.length()-5)+".json");
                    jsonFile.write(obj.toJSONString());
                    System.out.println("Successfully Copied JSON Object to File...");
                    System.out.println("\nJSON Object: " + obj);
                    jsonFile.flush();
                    jsonFile.close();
                }
            }
            i++;
        }
        System.out.println("---------end call---------");
//        return temp;
    }

    public void chooseDirFunc(ActionEvent actionEvent) throws IOException {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("../Java_Code"));

        File selectedDirectory = directoryChooser.showDialog(selectDir.getScene().getWindow());
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
}