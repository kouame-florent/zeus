/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.quantum.processor;

import io.quantum.annotation.util.DefaultType;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
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
public class GenericDaoImplFActory {
    
    ClassName genricDao = ClassName.get(DefaultType.GENERIC_DAO.pkgName(), "GenericDAO");
   
    TypeVariableName entityTypeVarName = TypeVariableName.get("E");
    TypeVariableName idTypeVarName = TypeVariableName.get("ID");
    
    ClassName list = ClassName.get("java.util", "List");
    ClassName optionalName = ClassName.get("java.util", "Optional");
    TypeName optionalType = ParameterizedTypeName.get(optionalName, entityTypeVarName);
    
    private JavaFile buildCode(){
        
        ClassName emType = ClassName.get("javax.persistence", "EntityManager");
        FieldSpec emField = FieldSpec.builder(emType, "em", Modifier.PROTECTED)
                .addAnnotation(ClassName.get("javax.persistence", "PersistenceContext"))
                .build();
        ClassName entityClassName = ClassName.get(Class.class);
        
        FieldSpec entityClassField = FieldSpec.builder(ParameterizedTypeName.get(entityClassName,entityTypeVarName),
               "entityClass", Modifier.PROTECTED).build();
        
        TypeName classType = ParameterizedTypeName.get(entityClassName, entityTypeVarName);
        
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PROTECTED)
                .addParameter(classType, "entityClass")
                .addStatement("this.entityClass = entityClass")
                .build();
       
        MethodSpec makePersistent = MethodSpec.methodBuilder("makePersistent")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(entityTypeVarName, "e")
                .returns(optionalType)
                .addStatement("return Optional.of(em.merge(e))")
                .build();
        
        MethodSpec makeTransient = MethodSpec.methodBuilder("makeTransient")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(entityTypeVarName, "e")
                .returns(void.class)
                .addStatement("em.remove(em.merge(e))")
                .build();
         
        MethodSpec findById = MethodSpec.methodBuilder("findById")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(idTypeVarName, "id")
                .returns(optionalType)
                .addStatement("return Optional.of(em.find(entityClass, id))")
                .build();
        
        MethodSpec findReferenceById = MethodSpec.methodBuilder("findReferenceById")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(idTypeVarName, "id")
                .returns(optionalType)
                .addStatement("return Optional.of(em.getReference(entityClass, id))")
                .build();
        
        ClassName criteriaQueryClassName = ClassName.get("javax.persistence.criteria", "CriteriaQuery");
        TypeName criteriaQueryTypeName = ParameterizedTypeName.get(criteriaQueryClassName, entityTypeVarName);
        
        MethodSpec getCount = MethodSpec.methodBuilder("getCount")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(Long.class)
                .addStatement("$T<Long> c = em.getCriteriaBuilder().createQuery(Long.class)",criteriaQueryClassName)
                .addStatement("c.select(em.getCriteriaBuilder().count(c.from(entityClass)))")
                .addStatement("return em.createQuery(c).getSingleResult()")
                .build();
        
        MethodSpec findAll = MethodSpec.methodBuilder("findAll")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(list, entityTypeVarName))
                .addStatement("$T c = em.getCriteriaBuilder().createQuery(entityClass)",criteriaQueryTypeName)
                .addStatement("c.select(c.from(entityClass))")
                .addStatement("return em.createQuery(c).getResultList()")
                .build();
        
         
        TypeSpec genericDaoImpl = TypeSpec.classBuilder("GenericDAOImpl")
                .addTypeVariables(List.of(entityTypeVarName,idTypeVarName))
                .addModifiers(Modifier.PUBLIC,Modifier.ABSTRACT)
                .addSuperinterface(ParameterizedTypeName.get(genricDao,entityTypeVarName,idTypeVarName))
                .addField(emField)
                .addField(entityClassField)
                .addMethods(List.of(constructor, makePersistent, findById,
                        findReferenceById,makeTransient,getCount,findAll))
                .build();
      
        
        return JavaFile.builder(DefaultType.GENERIC_DAO_IMPL.pkgName(), genericDaoImpl)
                .skipJavaLangImports(true)
                .indent("    ")
                .build();
     
    }
    
    public void generateCode(Filer filer,ProcessingEnvironment processingEnv){
        JavaFile javaFile = buildCode();
        try {
            
            javaFile.writeTo(filer);
            
        } catch (IOException ex) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,"[Zeus] GenricDaoImpl already generated! ");
        }
    }
}
