/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.quantum.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 *
 * @author root
 */
public class EntityDaoFactory extends DaoBaseFactory{
  
    public EntityDaoFactory(Filer filer, Messager messager) {
        super(filer, messager);
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
    
   private String interfaceName(String elementSimpleName){
       return elementSimpleName + "DAO";
   }
       
    String entityDaoName; 
   
    @Override
    JavaFile buildCode(Element element) {
        
        entityDaoName = interfaceName(element.getSimpleName().toString());
        ClassName genricDao = ClassName.get("io.quantum.dao", "GenericDAO");
         
        TypeName entityTypeName = TypeName.get(element.asType());
        TypeVariableName idTypeVarName = TypeVariableName.get("String");
           
        TypeSpec entityDao = TypeSpec.interfaceBuilder(entityDaoName)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ParameterizedTypeName.get(genricDao,entityTypeName,idTypeVarName))
                .build();
          
        return JavaFile.builder("io.quantum.dao", entityDao)
                .skipJavaLangImports(true)
                .indent("    ")
                .build();
   }
    
    @Override
    void writeFile(JavaFile javaFile,ProcessingEnvironment processingEnv){
        try {
            javaFile.writeTo(filer);
            
        } catch (IOException ex) {
            System.out.printf("[Zeus] File already generated! %s",ex);
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,"[Zeus] File already generated! ");
        }
    }
 
}
