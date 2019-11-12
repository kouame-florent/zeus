/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.quantum.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.DeclaredType;
import javax.tools.Diagnostic;

/**
 *
 * @author root
 */
public class EntityDaoImplFactory {
    
    private final Filer filer;
    private final Messager messager;
    private static final String ENTITY_ANNOTATION_FQCN = "javax.persistence.Entity";
        
    public List<Element> annotatedElements = new ArrayList<>();
    public List<Element> badAnnotatedElements = new ArrayList<>();

    public EntityDaoImplFactory(Filer filer, Messager messager) {
        this.filer = filer;
        this.messager = messager;
    }
    

    public void add(Element element){
        annotatedElements.add(element);
    }
    
    public void clearAnnotatedElements(){
        annotatedElements.clear();
    }
    
    public void checkType(Element annotatedElement){
        if(annotatedElement.getKind() != ElementKind.CLASS){
            badAnnotatedElements.add(annotatedElement);
        }
    }
    
   private String className(String elementSimpleName){
       return elementSimpleName + "DAO" + "Impl";
   }
   
   private String interfaceName(String elementSimpleName){
       return elementSimpleName + "DAO" ;
   }
   
       
    private JavaFile buildCode(Element element) {
        
        String entityName = element.getSimpleName().toString();
        String entityDaoImplName = className(entityName);
        String entityDaoName = interfaceName(entityName);
        
        ClassName genricDaoImpl = ClassName.get("io.quantum.dao", "GenericDAOImpl");
        ClassName entityDaoClassName = ClassName.get("io.quantum.dao",entityDaoName );
        TypeName entityTypeName = ClassName.get(element.asType());
              
        ClassName statelessClassName = ClassName.get("javax.ejb", "Stateless");
             
        
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("super($L)",element.getSimpleName()+".class")
                .build();
                       
        TypeSpec entityDao = TypeSpec.classBuilder(entityDaoImplName)
                .superclass(ParameterizedTypeName.get(genricDaoImpl,entityTypeName,TypeName.get(String.class)))
                .addSuperinterface(entityDaoClassName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(statelessClassName)
                .addMethod(constructor)
                .build();
        
          
        return JavaFile.builder("io.quantum.dao.impl", entityDao)
                .skipJavaLangImports(true)
                .indent("    ")
                .build();
   }
    
    private boolean isEntity(Element element){
        return element.getAnnotationMirrors()
                .stream().map(AnnotationMirror::getAnnotationType)
                .map(DeclaredType::toString)
                .anyMatch(n -> n.equalsIgnoreCase("javax.persistence.Entity"));
    }
   
   public void generateCode(ProcessingEnvironment processingEnv){
        annotatedElements.stream()
            .filter(this::isEntity)
            .map(e -> buildCode(e))
            .forEach(jf -> writeFile(jf, processingEnv));
    }
   
    public void writeFile(JavaFile javaFile,ProcessingEnvironment processingEnv){
        try {
            
            javaFile.writeTo(filer);
        } catch (IOException ex) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,"[Zeus] File already generated! ");
        }
    }
}
