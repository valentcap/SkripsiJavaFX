package ParsingClasses;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.GenericListVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

public class MethodNamePrinterReturn extends GenericListVisitorAdapter<String, Void> {
    @Override
    public List<String> visit(final MethodDeclaration md, final Void arg) {
        List<String> res = new ArrayList<>();
        super.visit(md, arg);
        String x = md.getNameAsString();
        res.add(x);
        return  res;
    }
}
