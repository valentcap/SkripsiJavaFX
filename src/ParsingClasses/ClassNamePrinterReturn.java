package ParsingClasses;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

public class ClassNamePrinterReturn extends GenericVisitorAdapter<List<String>, Void> {
    @Override
    public List<String> visit(final ClassOrInterfaceDeclaration ci,final Void arg) {
        List<String> res = new ArrayList<String>();
        super.visit(ci, arg);
        String x = ci.getNameAsString();
        res.add(x);
        return  res;
    }
}