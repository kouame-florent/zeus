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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
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
       
//    public Element interfaceElement(Element annotatedElement){
//       return annotatedElement.getEnclosingElement();
//    }
    
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
    
//    private String annotationParamClassName(Element annotatedElement){
//         DAOImpl dAOImplAnnotation = annotatedElement.getAnnotation(DAOImpl.class);
////        System.out.printf("[ZEUS] GETTIN MIRRORTYPE %s \n",dAOImpl);
//        try{
//            dAOImplAnnotation.forClass().getClass();
//            return null;
//        }catch (MirroredTypeException e) {
//            System.out.printf("[ZEUS] MIRRORED TYPE EXCEPTION: %s \n",e.getTypeMirror());
//            return e.getTypeMirror().toString();
//        }
//    }
////    
    
//    private TypeElement annotatedTypeElement(Element annotatedElement){
//        typesUtils.asElement(tm)
//        elementsUtils.getTypeElement(annotatedElement.);
//    }
    
    private TypeMirror annotationParamTypeMirror(Element annotatedElement){
        DAOImpl dAOImplAnnotation = annotatedElement.getAnnotation(DAOImpl.class);
//        System.out.printf("[ZEUS] GETTIN MIRRORTYPE %s \n",dAOImpl);
        try{
            dAOImplAnnotation.forClass().getClass();
            return null;
        }catch (MirroredTypeException e) {
            System.out.printf("[ZEUS] MIRRORED TYPE EXCEPTION: %s \n",e.getTypeMirror());
            return e.getTypeMirror();
        }
        
        
//       List<? extends AnnotationMirror> annotationMirrors = annotatedElement.getAnnotationMirrors();
//       for (AnnotationMirror annotationMirror : annotationMirrors) {
//           Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues
//                    = annotationMirror.getElementValues();
//           
//           System.out.printf("[ZEUS] ELEMENTS VALUES PAIRS: %s \n",elementValues);
//            
//            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry
//                    : elementValues.entrySet()) {
//                String key = entry.getKey().getSimpleName().toString();
//                Object value = entry.getValue().getValue();
//                
//                System.out.printf("[ZEUS] ELEMENT KEY : %s \n",key);
//                System.out.printf("[ZEUS] ELEMENT VALUE : %s \n",value);
////                
//                if(key.equals("forClass")){
//                    TypeMirror typeMirror = (TypeMirror) value;
//                    System.out.printf("[ZEUS] TYPE MIRROR: %s",typeMirror);
//                    return typeMirror;
//                }
//            }
//       }
//            
//       return null;
    }
    
    
//    private String getSimpleName(Element annotatedElement){
//        String[] pathComponents = annotationParamTypeMirror(annotatedElement)
//                .toString().split(".");
//        
//        if(pathComponents.length > 1){
//            return pathComponents[pathComponents.length -1];
//        }
//        return "";
//        
//    }
//    
       
    
    JavaFile buildClassBody(Element element) {
        
//        String entityName = element.getSimpleName().toString();
//        Element interfaceElement = interfaceElement(element);
          String entityDaoImplName = targetClassName(element);
          String entityDaoName = sourceInterfaceName(element);
//        
          ClassName genricDaoImpl = ClassName.get(PackageName.GENERIC_DAO_IMPL.pkgName(), "GenericDAOImpl");
          ClassName entityDaoClassName = ClassName.get((TypeElement)element);
//          ClassName entityDaoClassName = ClassName.get(PackageName.ENTITY_DAO.pkgName(),entityDaoName );
//        
          TypeName entityTypeName = ClassName.get(annotationParamTypeMirror(element));
          System.out.printf("[ZEUS] ENTITY TYPE NAME: %s \n",entityTypeName);
//              
//        ClassName statelessClassName = ClassName.get("javax.ejb", "Stateless");
             
        
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
        
        return JavaFile.builder(PackageName.ENTITY_DAO_IMPL.pkgName(), entityDao)
                .skipJavaLangImports(true)
                .indent("    ")
                .build();
   }
    
  public void addMethod(TypeSpec typeSpec){
  
  }
    
   void generateCode(ProcessingEnvironment processingEnv){
        annotatedElements.stream()
            .map(e -> buildClassBody(e))
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
