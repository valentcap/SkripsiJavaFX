package ParsingClasses;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.GenericListVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

public class ClassNamePrinterReturn extends GenericListVisitorAdapter<String, Void> {
    List<String> res = new ArrayList<String>();
    @Override
    public List<String> visit(final ClassOrInterfaceDeclaration ci,final Void arg) {
        super.visit(ci, arg);
        String x = ci.getNameAsString();
        System.out.println(ci.isClassOrInterfaceDeclaration());
        String lineNum = String.valueOf(ci.getName().getBegin().get().line);
        String colNum = String.valueOf(ci.getName().getBegin().get().column);
        res.add(x);
        res.add(lineNum);
        res.add(colNum);
        return  res;
    }
}