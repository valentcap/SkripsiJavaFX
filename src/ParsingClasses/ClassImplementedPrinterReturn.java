package ParsingClasses;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.GenericListVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

public class ClassImplementedPrinterReturn extends GenericListVisitorAdapter<String, Void> {
    @Override
    public List<String> visit(final ClassOrInterfaceDeclaration ci, final Void arg) {
        List<String> res = new ArrayList<>();
        super.visit(ci, arg);
        String x = ci.getImplementedTypes().toString();
        x = x.substring(1, x.length()-1);
        res.add(x);
        return  res;
    }
}
