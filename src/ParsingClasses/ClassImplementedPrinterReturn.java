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
//        System.out.println(x);
        x = x.substring(1, x.length()-1);
        res.add(x);
        return  res;
    }
//    @Override
//    public List<String> visit(final ConstructorDeclaration c, final Void arg) {
//        List<String> res = new ArrayList<>();
//        super.visit(c, arg);
//        String x = c.getParameters().toString();
//        x = x.substring(1, x.length()-1);
//        res.add(x);
//        return  res;
//    }
}
