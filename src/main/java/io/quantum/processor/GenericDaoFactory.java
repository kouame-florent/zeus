/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.quantum.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import java.io.IOException;
import java.util.List;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;

/**
 *
 * @author root
 */
public class GenericDaoFactory {
     
    private JavaFile buildCode() {
        
        TypeVariableName entityType = TypeVariableName.get("E");
        TypeVariableName idType = TypeVariableName.get("ID");
        
        ClassName optional = ClassName.get("java.util", "Optional");
        ClassName list = ClassName.get("java.util", "List");
        
        TypeName optionalType = ParameterizedTypeName.get(optional, entityType);
               
        MethodSpec makePersistent = MethodSpec.methodBuilder("makePersistent")
                .addModifiers(Modifier.PUBLIC,Modifier.ABSTRACT)
                .addParameter(entityType, "e")
                .returns(optionalType)
                .build();
        
        MethodSpec makeTransient = MethodSpec.methodBuilder("makeTransient")
                .addModifiers(Modifier.PUBLIC,Modifier.ABSTRACT)
                .addParameter(entityType, "e")
                .returns(void.class)
                .build();
        
        MethodSpec findById = MethodSpec.methodBuilder("findById")
                .addModifiers(Modifier.PUBLIC,Modifier.ABSTRACT)
                .addParameter(idType, "e")
                .returns(optionalType)
                .build();
        
        MethodSpec findReferenceById = MethodSpec.methodBuilder("findReferenceById")
                .addModifiers(Modifier.PUBLIC,Modifier.ABSTRACT)
                .addParameter(idType, "e")
                .returns(optionalType)
                .build();
        
        MethodSpec findAll = MethodSpec.methodBuilder("findAll")
                .addModifiers(Modifier.PUBLIC,Modifier.ABSTRACT)
                .returns(list)
                .build();
        
        MethodSpec getCount = MethodSpec.methodBuilder("getCount")
                .addModifiers(Modifier.PUBLIC,Modifier.ABSTRACT)
                .returns(Long.class)
                .build();
        
        TypeSpec genericDao = TypeSpec.interfaceBuilder("GenericDAO")
                .addTypeVariables(List.of(entityType,idType))
                .addModifiers(Modifier.PUBLIC)
                .addMethods(List.of(makePersistent, makeTransient, findById, findReferenceById,
                        findAll, getCount))
                .build();
          
        return JavaFile.builder(PackageName.GENERIC_DAO.pkgName(), genericDao)
                .skipJavaLangImports(true)
                .indent("    ")
                .build();
        
    }
    
    public void generateCode(Filer filer,ProcessingEnvironment processingEnv){
        JavaFile javaFile = buildCode();
        try {
             javaFile.writeTo(filer);
            
        } catch (IOException ex) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,"[Zeus] GenricDao already generated! ");
        }
    }
}
