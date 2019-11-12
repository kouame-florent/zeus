/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.quantum.processor;

import io.quantum.annotation.WithDao;
import io.quantum.annotation.WithQuery;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 *
 * @author root
 */
@SupportedAnnotationTypes({
    "io.quantum.annotation.WithQuery"
})
public class QueryProcessor extends AbstractProcessor{
    
    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;
    
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv){ 
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(WithQuery.class)) {
            if(annotatedElement.getKind() != ElementKind.METHOD){
                error(annotatedElement, "Only classes can be annotated with @%s", WithQuery.class.getSimpleName());
                return true;
            }else{
                
                System.out.printf("[Zeus] WITHQUERY ELEMENT FOUND: %s",annotatedElement.getSimpleName().toString());
            }
        }
        
        return true;
    }
    
     private void error(Element e, String msg, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR,String.format(msg, args),e);
    }
    
   private void log(Element e, String msg, Object... args) {
        messager.printMessage(Diagnostic.Kind.NOTE,String.format(msg, args),e);
    }
    
}
