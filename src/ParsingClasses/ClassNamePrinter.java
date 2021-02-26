package ParsingClasses;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class ClassNamePrinter extends VoidVisitorAdapter<Void> {
    @Override
    public void visit(ClassOrInterfaceDeclaration ci, Void arg) {
        super.visit(ci, arg);
        System.out.println("Class or Interface: " + ci.getName());
    }
}
