package com.miss.apt_processor;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 *      辅助类
 */
public class ClassCreator {

    private String className;
    private String packageName;
    private TypeElement mTypeElement;
    private Map<Integer, VariableElement> mVariableElementMap;

    /**
     *      获取包名、类名
     * @param classElement
     * @param packageName
     */
    public ClassCreator(TypeElement classElement,String packageName) {
        mVariableElementMap = new HashMap<>();
        mTypeElement = classElement;
        this.packageName = packageName;
        className = classElement.getSimpleName().toString() + "_BindView";
    }

    /**
     *      id、view成对放入
     */
    public void setElement(int id,VariableElement element){
        mVariableElementMap.put(id, element);
    }

    /**
     *      生成 java 代码
     * @return
     */
    public String generateJavaCode(){
        StringBuilder builder = new StringBuilder();
        builder.append("package  " + packageName + ";\n\n");
        builder.append("import com.miss.apt_library.*;\n\n");
        builder.append("public class " + className);
        builder.append("{\n");
        generateMethods(builder);
        builder.append("\n}");
        return builder.toString();
    }

    /**
     *      把要 find id 写进去
     * @param builder
     */
    private void generateMethods(StringBuilder builder) {
        builder.append("public void bind(" + mTypeElement.getQualifiedName() + " host){ \n");
        for (int id : mVariableElementMap.keySet()) {
            VariableElement element = mVariableElementMap.get(id);
            String name = element.getSimpleName().toString();
            builder.append("host." + name + " = ");
            builder.append("((android.app.Activity)host).findViewById(" + id + ");\n");
        }
        builder.append("};");
    }

    public String getClassFullName() {
        return packageName + "." + className;
    }

    public TypeElement getTypeElement() {
        return mTypeElement;
    }
}