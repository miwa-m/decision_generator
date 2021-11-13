package desicion_generator.parser.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Class.
 * @author komur
 */
public class Class implements Meta, Serializable {
    private static final long serialVersionUID = -830313342349777622L;
    
    private List<Class> innerClasses = new ArrayList<>();
    private List<Method> methods = new ArrayList<>();
    public String packageName;
    public String className;
    
    public Class (String packageName, String className) {
        this.packageName = packageName;
        this.className = className;
    }
    
    public void addMethod(Method method) {
        this.methods.add(method);
    }
    
    public void setMethods(List<Method> methods) {
        this.methods = methods;
    }
    
    public List<Method> getMethods() {
        return this.methods;
    }
    
    public void setInnerClasses(List<Class> innerClasses) {
        this.innerClasses = innerClasses;
    }
    
    public void addInnerClass(Class innerClass) {
        this.innerClasses.add(innerClass);
    }
    
    public List<Class> getInnerClasses() {
        return this.innerClasses;
    }
}
