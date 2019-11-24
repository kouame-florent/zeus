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
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

/**
 *
 * @author root
 */
public class EntityDaoImplFactory extends DaoBaseFactory{
    
//    public EntityDaoImplFactory(Filer filer, Messager messager) {
//        super(filer, messager);
//    }
//
//    public void add(Element element){
//        annotatedElements.add(element);
//    }
//    
//    public void clearAnnotatedElements(){
//        annotatedElements.clear();
//    }
//    
//    public void checkType(Element annotatedElement){
//        if(annotatedElement.getKind() != ElementKind.CLASS){
//            badAnnotatedElements.add(annotatedElement);
//        }
//    }
//    
//   private String className(String elementSimpleName){
//       return elementSimpleName + "DAO" + "Impl";
//   }
//   
//   private String interfaceName(String elementSimpleName){
//       return elementSimpleName + "DAO" ;
//   }
//          
//    @Override
//    JavaFile buildCode(Element element) {
//        
//        String entityName = element.getSimpleName().toString();
//        String entityDaoImplName = className(entityName);
//        String entityDaoName = interfaceName(entityName);
//        
//        ClassName genricDaoImpl = ClassName.get(PackageName.GENERIC_DAO_IMPL.pkgName(), "GenericDAOImpl");
//        ClassName entityDaoClassName = ClassName.get(PackageName.ENTITY_DAO.pkgName(),entityDaoName );
//        TypeName entityTypeName = ClassName.get(element.asType());
//              
//        ClassName statelessClassName = ClassName.get("javax.ejb", "Stateless");
//             
//        
//        MethodSpec constructor = MethodSpec.constructorBuilder()
//                .addModifiers(Modifier.PUBLIC)
//                .addStatement("super($L)",element.getSimpleName()+".class")
//                .build();
//                       
//        TypeSpec entityDao = TypeSpec.classBuilder(entityDaoImplName)
//                .superclass(ParameterizedTypeName.get(genricDaoImpl,entityTypeName,TypeName.get(String.class)))
//                .addSuperinterface(entityDaoClassName)
//                .addModifiers(Modifier.PUBLIC)
////                .addAnnotation(statelessClassName)
//                .addMethod(constructor)
//                .build();
//        
//        return JavaFile.builder(PackageName.ENTITY_DAO_IMPL.pkgName(), entityDao)
//                .skipJavaLangImports(true)
//                .indent("    ")
//                .build();
//   }
    
}
