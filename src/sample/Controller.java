package sample;

import ParsingClasses.MethodNamePrinter;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.VoidVisitor;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Vector;

public class Controller {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));

    @FXML
    private Button selectDir;
    @FXML
    private Label path;

    public Controller() throws IOException {
    }


    public static Vector<File> showFiles(File[] files){
        Vector<File> temp = new Vector<File>();
        int i=0;
        for (File file : files) {
            if (file.isDirectory()) {
//                System.out.println("Directory: " + file.getName());
                showFiles(file.listFiles()); // Calls same method again.
            } else {
                String fileName = file.getName();
                //kemudian bacalah file nyaa jika .java
                //membedakan tipe file
                if(fileName.endsWith(".java")){
                    temp.add(file);
                }else{
//                  Bukan file java
                }
            }
            i++;
        }
        return temp;
    }

    public void chooseDirFunc(ActionEvent actionEvent) throws IOException {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("../../Kuliah/"));

        File selectedDirectory = directoryChooser.showDialog(selectDir.getScene().getWindow());
        if(selectedDirectory != null) {
            path.setText(selectedDirectory.getAbsolutePath());
            Vector<File> files = showFiles(selectedDirectory.listFiles());

            //loop files from input
            for(int i=0; i<files.size(); i++){

            }
            CompilationUnit compilationUnit;
            Path codePath = Paths.get(files.get(0).getAbsolutePath());
            compilationUnit = StaticJavaParser.parse(Files.readString(codePath));
            VoidVisitor<Void> methodNameVisitor = new MethodNamePrinter();
            methodNameVisitor.visit(compilationUnit, null);
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