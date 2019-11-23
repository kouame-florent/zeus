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
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;

/**
 *
 * @author root
 */
public class DaoFactory extends DaoBaseFactory{
  
    public DaoFactory(Filer filer, Messager messager) {
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
        ClassName genricDao = ClassName.get(PackageName.GENERIC_DAO.pkgName(), "GenericDAO");
         
        TypeName entityTypeName = TypeName.get(element.asType());
        TypeVariableName idTypeVarName = TypeVariableName.get("String");
           
        TypeSpec entityDao = TypeSpec.interfaceBuilder(entityDaoName)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ParameterizedTypeName.get(genricDao,entityTypeName,idTypeVarName))
                .build();
        
           
        return JavaFile.builder(PackageName.ENTITY_DAO.pkgName(), entityDao)
                .skipJavaLangImports(true)
                .indent("    ")
                .build();
   }
    
   private void buildMethods(){
   
   }
   
   private void buildClass(){
   
   }
  
    
    @Override
    void writeFile(JavaFile javaFile,ProcessingEnvironment processingEnv){
       
        try {
//            javaFile.writeTo(Path.of("/home/florent/projects/task/src/main/java/task"));
//            ClassLoader classLoader = getClass().getClassLoader();
//            File file = new File(classLoader.getResource("io.quantum.task.domain.entity.Project").getFile());
//            System.out.printf("[Zeus]--> LOADER: %s \n",classLoader);
//            Arrays.stream(classLoader.getDefinedPackages())
//                    .forEach(p -> System.out.printf("[Zeus]--> PACKAGE: %s \n",p));
//            URL url = classLoader.getDefinedPackages()
//            System.out.printf("[Zeus]--> URL: %s \n",url);
            javaFile.writeTo(filer);
            
        } catch (IOException ex) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,"[Zeus] File already generated! ");
        }
    }
 
}
