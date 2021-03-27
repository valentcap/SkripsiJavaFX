package ParsingClasses;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.GenericListVisitorAdapter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileParser extends GenericListVisitorAdapter<JSONObject, Void> {
    //    List<String> res = new ArrayList<String>();
    List<JSONObject> res = new ArrayList<>();
    Path codePath;

    public FileParser(Path codePath){
        this.codePath = codePath;
    }

    @Override
    public List<JSONObject> visit(final ClassOrInterfaceDeclaration ci,final Void arg) {
        super.visit(ci, arg);
        JSONObject obj = new JSONObject();
        //class declarations
        String x = ci.getNameAsString();
        String lineNum = String.valueOf(ci.getName().getBegin().get().line);
        String colNum = String.valueOf(ci.getName().getBegin().get().column);
        String accessSpecifier = String.valueOf(ci.getAccessSpecifier());
        obj.put("ClassName", ci.getNameAsString());
        obj.put("LineNum", lineNum);
        obj.put("ColNum", colNum);
        obj.put("Identifier", accessSpecifier);


        //methods
        JSONArray arrMethods = new JSONArray();

//        System.out.println("Method punya "+ci.getNameAsString()+": "+ci.getMethods().size());
        List<MethodDeclaration> methods = ci.getMethods();
        for(int i=0; i<ci.getMethods().size(); i++){
            JSONObject objMethods = new JSONObject();
            JSONArray params = new JSONArray();
            objMethods.put("MethodName" ,methods.get(i).getNameAsString());
            //params
            int paramSize = methods.get(i).getParameters().size();
            for(int j=0; j<paramSize; j++){
                params.put(methods.get(i).getParameters().get(j));
            }
            //return value
            objMethods.put("MethodReturnType", methods.get(i).getType().toString());
//            System.out.println(methods.get(i).getNameAsString()+": "+methods.get(i).getType().toString());

            objMethods.put("MethodParams", params);
            arrMethods.put(objMethods);
        }
        obj.put("Methods", arrMethods);

        //parent
        String parent = ci.getExtendedTypes().toString();
        if(parent.length() > 2) {
            parent = parent.substring(1, parent.length() - 1);
        }else{
            parent = "";
        }
//        System.out.println(parent);
        obj.put("Parent", parent);

        //implements
        JSONArray implementedArr = new JSONArray();
//        System.out.println("Implemented punya "+ci.getNameAsString()+": "+ci.getImplementedTypes().size());
        NodeList<ClassOrInterfaceType> implementedTypes = ci.getImplementedTypes();
        for(int i=0; i<implementedTypes.size(); i++){
//            System.out.println(implementedTypes.get(i).getNameAsString());
            implementedArr.put(implementedTypes.get(i).getNameAsString());
        }
        obj.put("Implements", implementedArr);

        //constructors
        JSONArray arrConstructor = new JSONArray();

//        System.out.println("Method punya "+ci.getNameAsString()+": "+ci.getMethods().size());
        List<ConstructorDeclaration> constructors = ci.getConstructors();
        for(int i=0; i<ci.getConstructors().size(); i++){
            JSONObject objConstructor = new JSONObject();
            JSONArray params = new JSONArray();
//            System.out.println(constructors.get(i).getNameAsString());
            constructors.get(i).getParameters();
            int paramSize = constructors.get(i).getParameters().size();

            for(int j=0; j<paramSize; j++){
//                System.out.println(methods.get(i).getParameters().get(j));
                params.put(constructors.get(i).getParameters().get(j));
            }
//            JSONArray sub = new JSONArray();
//            sub.put(params);
//            objConstructor.put("Params", sub);
            objConstructor.put("Params", params);
            arrConstructor.put(objConstructor);
        }
        obj.put("Constructors", arrConstructor);

        //location
        obj.put("Location", this.codePath.toString());

//        res.add(x);
//        res.add(lineNum);
//        res.add(colNum);
        res.add(obj);
        return  res;
    }
}
