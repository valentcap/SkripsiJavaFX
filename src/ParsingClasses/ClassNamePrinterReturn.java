package ParsingClasses;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.GenericListVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

public class ClassNamePrinterReturn extends GenericListVisitorAdapter<String, Void> {
    @Override
    public List<String> visit(final ClassOrInterfaceDeclaration ci,final Void arg) {
        List<String> res = new ArrayList<String>();
        super.visit(ci, arg);
        String x = ci.getNameAsString();
        String lineNum = String.valueOf(ci.getName().getBegin().get().line);
        String colNum = String.valueOf(ci.getName().getBegin().get().column);
        System.out.println("line= "+lineNum);
        System.out.println("column= "+colNum);
        res.add(x);
        res.add(lineNum);
        res.add(colNum);
        return  res;
    }
}