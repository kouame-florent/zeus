/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.quantum.processor;

import com.squareup.javapoet.JavaFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.tools.Diagnostic;

/**
 *
 * @author root
 */
public abstract class DaoBaseFactory {
    
    protected final Filer filer;
    private final Messager messager;
    private static final String ENTITY_ANNOTATION_FQCN = "javax.persistence.Entity";
    
    public List<Element> annotatedElements = new ArrayList<>();
    public List<Element> badAnnotatedElements = new ArrayList<>();

    public DaoBaseFactory(Filer filer, Messager messager) {
        this.filer = filer;
        this.messager = messager;
    }
        
    
    boolean isEntity(Element element){
        return element.getAnnotationMirrors()
                .stream().map(AnnotationMirror::getAnnotationType)
                .map(DeclaredType::toString)
                .anyMatch(n -> n.equalsIgnoreCase(ENTITY_ANNOTATION_FQCN));
    }
    
    abstract JavaFile buildCode(Element element);
   
    void generateCode(ProcessingEnvironment processingEnv){
        annotatedElements.stream()
            .filter(this::isEntity)
            .map(e -> buildCode(e))
            .forEach(jf -> writeFile(jf, processingEnv));
    }
    
    void writeFile(JavaFile javaFile,ProcessingEnvironment processingEnv){
        try {
           
            javaFile.writeTo(filer);
        } catch (IOException ex) {
            System.out.printf("[Zeus] File already generated! %s",ex);
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,"[Zeus] File already generated! ");
        }
    }
}
