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
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

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
       
    @Override
    JavaFile buildCode(Element element) {
        
        String entityDaoName = interfaceName(element.getSimpleName().toString());
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
    
 
}
