/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.quantum.processor;

import io.quantum.annotation.util.DefaultType;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.quantum.annotation.DAO;
import io.quantum.annotation.util.DefaultPackage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 *
 * @author root
 */
public class DaoFactory{
    
    private final ProcessingEnvironment processingEnv;
    private final Filer filer;
    private final Messager messager;
    private final Types typesUtils;
    private final Elements elementsUtils;
    private final Class<DAO> acceptedClass = DAO.class;
     
    public List<Element> annotatedElements = new ArrayList<>();
    public List<Element> badAnnotatedElements = new ArrayList<>();
  
    public DaoFactory(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
        this.elementsUtils = processingEnv.getElementUtils();
        this.typesUtils = processingEnv.getTypeUtils();
    }
    
    public void generateCode(ProcessingEnvironment processingEnv){
       annotatedElements.stream()
            .peek(elt -> System.out.printf("[ZEUS] BEFORE FILTER: %s\n",elt))
            .filter(elt -> isAccepted(elt))
            .peek(elt -> System.out.printf("[ZEUS] AFTER FILTER: %s\n",elt))
            .map(elt -> buildInterfaceBody(elt))
            .map(this::buildInterfaceFile)
            .forEach(jf -> writeFile(jf, processingEnv));
    }
    
    private boolean isAccepted(Element element){
       return ((element.getKind() == ElementKind.INTERFACE) && 
               (element.getAnnotation(acceptedClass) != null));
    }
        
    TypeSpec buildInterfaceBody(Element element) {
      
        String daoInterfaceName = targetClassName(element);
        ClassName genricDao = ClassName.get(DefaultType.GENERIC_DAO.packageName(),
                DefaultType.GENERIC_DAO.className());
        
        TypeName entityTypeName = 
                ClassName.get(elementsUtils.getTypeElement(daoClassParamCanonicalName(element)));
        
        List<Element> enclosedElements = (List<Element>) element.getEnclosedElements()
                .stream().filter(e -> e.getKind() == ElementKind.METHOD)
                .collect(Collectors.toList());
        
        List<MethodSpec> methods =  getAnnotatedMethods(enclosedElements).stream()
                .map(e -> buildAbstractMethods(e)).collect(Collectors.toList());
             
        TypeSpec daoInterface = TypeSpec.interfaceBuilder(daoInterfaceName)
                .addSuperinterface(ParameterizedTypeName.get(genricDao,entityTypeName,TypeName.get(String.class)))
                .addModifiers(Modifier.PUBLIC)
                .addMethods(methods)
                .build();
        
        return daoInterface;
        
    }
    
    private MethodSpec buildAbstractMethods(Element executableElement){
         
        ExecutableElement execElt = (ExecutableElement)executableElement;
//        System.out.printf("[ZEUS] RETURN TYPE NAME: %s \n",execElt.getReturnType());
   
        List<? extends VariableElement> params = execElt.getParameters();
        List<ParameterSpec> paramsSpecs = params.stream().map(ParameterSpec::get).collect(Collectors.toList());
        
        TypeMirror returnType = execElt.getReturnType();
        TypeName returnTypeName = ClassName.get(returnType);
        
        MethodSpec methodSpec = MethodSpec
               .methodBuilder(execElt.getSimpleName().toString())
               .addModifiers(Modifier.PUBLIC,Modifier.ABSTRACT)
               .addParameters(paramsSpecs)
               .returns(returnTypeName)
               .build();
       
        return methodSpec;
        
    }
    
     private String daoClassParamCanonicalName(Element annotatedElement){
        try{
           DAO daoAnnotation = annotatedElement.getAnnotation(DAO.class);
           return daoAnnotation.forClass().getCanonicalName();
           
        }catch (MirroredTypeException e) {
            System.out.printf("[ZEUS] MIRRORED TYPE EXCEPTION: %s \n",e.getTypeMirror());
            return e.getTypeMirror().toString();
        }
 
    }
        
    private String targetClassName(Element interfaceElement){
        return annotationClassParamSimpleName(interfaceElement) + "DAO";
    }
    
    private String annotationClassParamSimpleName(Element annotatedElement){
        try{
           System.out.printf("[ZEUS] annotated : %s \n",annotatedElement);
           DAO daoAnnotation = annotatedElement.getAnnotation(DAO.class);
           String name = daoAnnotation.forClass().getSimpleName();
           System.out.printf("[ZEUS] DAO CLASS SIMPLE NAME : %s \n",name);
           return name;
           
        }catch (MirroredTypeException e) {
            System.out.printf("[ZEUS] MIRRORED TYPE EXCEPTION: %s \n",e.getTypeMirror());
            String name = typesUtils.asElement(e.getTypeMirror()).getSimpleName().toString();
            System.out.printf("[ZEUS] DAO CLASS SIMPLE NAME : %s \n",name);
            return name;
        }
 
    }
    
    private List<Element> getAnnotatedMethods(List<Element> elements){
        return  elements.stream()
                    .filter(e -> !e.getAnnotationMirrors().isEmpty())
                    .collect(Collectors.toList()); 
    }
         
    
    private JavaFile buildInterfaceFile(TypeSpec typeSpec){
        return JavaFile.builder(DefaultPackage.ENTITY_DAO.packageName(), typeSpec)
                  .skipJavaLangImports(true)
                  .indent("\t")
                  .build();
    }
    
  
   private void writeFile(JavaFile javaFile,ProcessingEnvironment processingEnv){
        try {
            javaFile.writeTo(filer);
        } catch (IOException ex) {
            System.out.printf("[Zeus] File already generated! %s",ex);
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,"[Zeus] File already generated! ");
        }
    }
   

    public void add(Element element){
        annotatedElements.add(element);
    }
    
    public void clearAnnotatedElements(){
        annotatedElements.clear();
    }
     
    
  
}
