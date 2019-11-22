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
import io.quantum.annotation.DAOImpl;
import io.quantum.annotation.QueryImpl;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 *
 * @author root
 */
public class DaoImplFactory {
    
    private final ProcessingEnvironment processingEnv;
    private final Filer filer;
    private final Messager messager;
    private final Types typesUtils;
    private final Elements elementsUtils;
//    private static final String ENTITY_ANNOTATION_FQCN = "javax.persistence.Entity";
    
    public List<Element> annotatedElements = new ArrayList<>();
    public List<Element> badAnnotatedElements = new ArrayList<>();

    public DaoImplFactory(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
        this.elementsUtils = processingEnv.getElementUtils();
        this.typesUtils = processingEnv.getTypeUtils();
    }
    
     public void checkType(Element annotatedElement){
        if(annotatedElement.getKind() != ElementKind.INTERFACE){
            badAnnotatedElements.add(annotatedElement);
        }
    }
       
  
    public void add(Element annotatedElement){
        System.out.printf("[ZEUS] ANNOTATED ELT: %s \n",annotatedElement.toString());
        System.out.printf("[ZEUS] ANNOTATED ELT KIND: %s \n",annotatedElement.getKind().toString());
        if(annotatedElement.getKind() == ElementKind.INTERFACE){
            annotatedElements.add(annotatedElement);
        }
    }
    
    private String targetClassName(Element interElement){
        return interElement.getSimpleName() + "Impl";
    }
    
    private String sourceInterfaceName(Element element){
       return element.getSimpleName().toString();
    }
    
    
    private String annotationParamCanonicalName(Element annotatedElement){
        try{
           DAOImpl daoImplAnnotation = annotatedElement.getAnnotation(DAOImpl.class);
           return daoImplAnnotation.forClass().getCanonicalName();
           
        }catch (MirroredTypeException e) {
            System.out.printf("[ZEUS] MIRRORED TYPE EXCEPTION: %s \n",e.getTypeMirror());
            return e.getTypeMirror().toString();
        }
 
    }
    
    
    
    
    
        
    SimpleEntry<TypeSpec, Element> buildClassBody(Element element) {
 
        String entityDaoImplName = targetClassName(element);
        String entityDaoName = sourceInterfaceName(element);
      
        ClassName genricDaoImpl = ClassName.get(PackageName.GENERIC_DAO_IMPL.pkgName(), "GenericDAOImpl");
        ClassName entityDaoClassName = ClassName.get((TypeElement)element);
        
        String entityCanonicalName = annotationParamCanonicalName(element);
        
        TypeName entityTypeName = 
                ClassName.get(elementsUtils.getTypeElement(entityCanonicalName));
        
        System.out.printf("[ZEUS] ENTITY TYPE NAME: %s \n",entityTypeName);
           
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("super($L)",entityTypeName.toString()+".class")
                .build();
                       
        TypeSpec entityDao = TypeSpec.classBuilder(entityDaoImplName)
                .superclass(ParameterizedTypeName.get(genricDaoImpl,entityTypeName,TypeName.get(String.class)))
                .addSuperinterface(entityDaoClassName)
                .addModifiers(Modifier.PUBLIC)
//                .addAnnotation(statelessClassName)
                .addMethod(constructor)
                .build();
        
        List<Element> enclosedElements = (List<Element>) element.getEnclosedElements()
                .stream().filter(e -> e.getKind() == ElementKind.METHOD)
                .collect(Collectors.toList());
             
        return new SimpleEntry<>(entityDao,element);
    }
    
    private void handleMethodElement(List<Element> elements){
        List<Element> notAnnotatedMethods =
               elements.stream()
                       .filter(e -> e.getAnnotationMirrors().isEmpty())
                       .collect(Collectors.toList());
       
        List<Element>  annotatedMethods = 
               elements.stream()
                       .filter(e -> !e.getAnnotationMirrors().isEmpty())
                       .collect(Collectors.toList()); 
        
        
               
    }
    
    
    private TypeSpec addMethods(SimpleEntry<TypeSpec, Element> entry){
//        Element annotatedElement = entry.getValue();
//        TypeSpec clazz = entry.getKey();
//      
//        MethodSpec method = MethodSpec.methodBuilder(annotatedElement.getSimpleName().toString())
//                .addModifiers(Modifier.PUBLIC)
//                .addAnnotation(Override.class)
//                .build();
//      
//        clazz.methodSpecs.set(0, method);
      
        return entry.getKey();
   }
  
  private JavaFile buildClass(TypeSpec typeSpec){
      return JavaFile.builder(PackageName.ENTITY_DAO_IMPL.pkgName(), typeSpec)
                .skipJavaLangImports(true)
                .indent("    ")
                .build();
  }
  
    
   void generateCode(ProcessingEnvironment processingEnv){
        annotatedElements.stream()
            .map(elt -> buildClassBody(elt))
            .map(ent -> addMethods(ent))
            .map(this::buildClass)
            .forEach(jf -> writeFile(jf, processingEnv));
    }
   
   private void writeFile(JavaFile javaFile,ProcessingEnvironment processingEnv){
        try {
            javaFile.writeTo(filer);
        } catch (IOException ex) {
            System.out.printf("[Zeus] File already generated! %s",ex);
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,"[Zeus] File already generated! ");
        }
    }
    
}
