package com.miss.apt_processor;

import com.miss.apt_annotation.BindView;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 *  获取注解中的id，生成辅助类
 * Created by Vola on 2020/8/2.
 */
@SupportedAnnotationTypes("com.miss.apt_annotation.BindView")
public class BindViewProcessor extends AbstractProcessor {

    private Messager messager;
    private Elements elementUtils;
    private Map<String, ClassCreator> mProxyMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        messager = processingEnv.getMessager();
        elementUtils = processingEnv.getElementUtils();
    }

    /**
     * 用来指定你使用的Java版本
     * @return
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * 这里必须指定，这个注解处理器是注册给哪个注解的。
     * 注意，它的返回值是一个字符串的集合，包含本处理器想要处理的注解类型的合法全称。
     * 换句话说，在这里定义你的注解处理器注册到哪些注解上。
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> supportTypes = new LinkedHashSet<>();
        supportTypes.add(BindView.class.getCanonicalName());
        return supportTypes;
    }

    /**
     * 这相当于每个处理器的主函数main()。
     * 在这里写扫描、评估和处理注解的代码，以及生成Java文件。
     * @param set
     * @param roundEnvironment 输入参数RoundEnviroment，可以让查询出包含特定注解的被注解元素。
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        messager.printMessage(Diagnostic.Kind.NOTE, "process---->");
        mProxyMap.clear();
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        for (Element element : elements) {
            //因为 BindView 的作用对象是 FIELD，因此 element 可以直接转化为 VariableElement
            VariableElement variableElement = (VariableElement) element;
            TypeElement classElement = (TypeElement) variableElement.getEnclosingElement();
            String classFullName = classElement.getQualifiedName().toString();

            ClassCreator classCreator = mProxyMap.get(classFullName);
            if (classCreator == null) {
                String packageName = elementUtils.getPackageOf(classElement).getQualifiedName().toString();
                classCreator = new ClassCreator(classElement, packageName);
                mProxyMap.put(classFullName, classCreator);
            }
            BindView bindView = variableElement.getAnnotation(BindView.class);
            int id = bindView.value();
            classCreator.setElement(id, variableElement);
        }
        //通过遍历mProxyMap，创建java文件
        for (String key : mProxyMap.keySet()) {
            ClassCreator classCreator = mProxyMap.get(key);
            try {
                messager.printMessage(Diagnostic.Kind.NOTE, " --> " + classCreator.generateJavaCode());

                JavaFileObject fileObject = processingEnv.getFiler().createSourceFile(classCreator.getClassFullName(), classCreator.getTypeElement());
                Writer writer = fileObject.openWriter();
                writer.write(classCreator.generateJavaCode());
                //  清空缓冲区数据
                writer.flush();
                writer.close();
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.NOTE, " --> create " + classCreator.getClassFullName() + "error");
            }
        }

        return true;
    }
}
