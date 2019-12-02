/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.quantum.processor;

import io.quantum.annotation.util.DefaultType;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.quantum.annotation.util.DefaultPackage;
import io.quantum.annotation.util.TypeNameUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import io.quantum.annotation.Query;


/**
 *
 * @author root
 */
public class DaoImplFactory extends DaoBaseFactory{
 
    public List<Element> annotatedElements = new ArrayList<>();
    public List<Element> claimedElements = new ArrayList<>();

    public DaoImplFactory(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }
 
    
    void generateCode(ProcessingEnvironment processingEnv){
        annotatedElements.stream()
            .filter(elt -> isAccepted(elt))
            .map(elt -> buildClassBody(elt))
            .map(this::buildClassFile)
            .forEach(jf -> writeFile(jf, processingEnv));
    }
     
   private boolean isAccepted(Element element){
       return ((element.getKind() == ElementKind.INTERFACE) && 
               (element.getAnnotation(acceptedClass) != null));
    }
   
    TypeSpec buildClassBody(Element element) {
 
        String entityDaoImplName = targetClassName(element);
        String entityDaoName = targetSuperInterfaceName(element);
      
        ClassName genricDaoImpl = ClassName.get(DefaultType.GENERIC_DAO_IMPL.packageName(), "GenericDAOImpl");
        ClassName entityDaoClassName = ClassName.get(DefaultPackage.ENTITY_DAO.packageName(), 
                entityDaoName);
        
        String entityCanonicalName = daoAnnotationClassParamName(element);
        
        TypeName entityTypeName = 
                ClassName.get(elementsUtils.getTypeElement(entityCanonicalName));
          
        System.out.printf("[ZEUS] ENTITY TYPE NAME: %s \n",entityTypeName);
           
        List<Element> enclosedElements = (List<Element>) element.getEnclosedElements()
                .stream().filter(e -> e.getKind() == ElementKind.METHOD)
                .collect(Collectors.toList());
        
        List<MethodSpec> methods =  getAnnotatedMethods(enclosedElements).stream()
                .map(e -> buildMethods(e)).collect(Collectors.toList());
                   
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("super($L)",entityTypeName.toString()+".class")
                .build();
        
        ClassName statelessAnnotationClassName = ClassName.get("javax.ejb", "Stateless");
                       
        TypeSpec entityDao = TypeSpec.classBuilder(entityDaoImplName)
                .superclass(ParameterizedTypeName.get(genricDaoImpl,entityTypeName,TypeName.get(String.class)))
                .addSuperinterface(entityDaoClassName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(statelessAnnotationClassName)
                .addMethod(constructor)
                .addMethods(methods)
                .build();
            
        return entityDao;
    }
    
    private JavaFile buildClassFile(TypeSpec typeSpec){
        return JavaFile.builder(DefaultPackage.ENTITY_DAO_IMPL.packageName(), typeSpec)
                  .skipJavaLangImports(true) 
                  .indent("\t")
                  .build();
    }
    
    public void add(Element annotatedElement){
//        System.out.printf("[ZEUS] ANNOTATED ELT: %s \n",annotatedElement.toString());
//        System.out.printf("[ZEUS] ANNOTATED ELT KIND: %s \n",annotatedElement.getKind().toString());
        if(annotatedElement.getKind() == ElementKind.INTERFACE){
            annotatedElements.add(annotatedElement);
        }
    }
    
    public void clearAnnotatedElements(){
        annotatedElements.clear();
    }
    
    private String targetClassName(Element interfaceElement){
        return daoAnnotationClassParamSimpleName(interfaceElement) + "DAO" + "Impl";
    }
    
    private String targetSuperInterfaceName(Element interfaceElement){
        return daoAnnotationClassParamSimpleName(interfaceElement) + "DAO";
    }
    
    private List<Element> getNotAnnotatedMethods(List<Element> elements){
        return  elements.stream()
                    .filter(e -> e.getAnnotationMirrors().isEmpty())
                    .collect(Collectors.toList());
    }
    
    private List<Element> getAnnotatedMethods(List<Element> elements){
        return  elements.stream()
                    .filter(e -> !e.getAnnotationMirrors().isEmpty())
                    .collect(Collectors.toList()); 
    }
     
    private MethodSpec buildMethods(Element methodElt){
        
        MethodSpec methodSpec = MethodSpec
            .methodBuilder(methodElt.getSimpleName().toString())
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override.class)
            .addParameters(parameters(methodElt))
            .returns(returnType(methodElt))
            .addCode(addCodeBlockStatement(methodElt))
//            .addStatement(DefaultStatement.UNSUPPORTED_OPERATION_EXCEPTION.statement())
            .build();
       
        return methodSpec;
    
    }
    
    private List<ParameterSpec> parameters(Element methodElt){
        ExecutableElement execElt = (ExecutableElement)methodElt;
        List<? extends VariableElement> params = execElt.getParameters();
        return params.stream().map(ParameterSpec::get).collect(Collectors.toList());
    }
    
    private TypeName returnType(Element methodElt){
        ExecutableElement execElt = (ExecutableElement)methodElt;
        TypeMirror returnType = execElt.getReturnType();
        return ClassName.get(returnType);
    
    } 
    
    private CodeBlock addCodeBlockStatement(Element methodElt){
        ExecutableElement execElt = (ExecutableElement)methodElt;
        TypeMirror returnType = execElt.getReturnType();
        String qualifiedName = TypeNameUtils.isPrimitive(returnType) ? 
                TypeNameUtils.getDeclaredTypeName(processingEnv, returnType, true) : 
                TypeNameUtils.getDeclaredTypeName(processingEnv, returnType, false);
 
       System.out.printf("[ZEUS] -- RETURN TYPE NAME: %s \n",qualifiedName);
        
        switch(qualifiedName){
            case "java.util.List":
                 return listReturnCode(methodElt);
            case "java.util.Optional":
                return optionalReturnCode(methodElt);
            case "java.lang.Integer":
                return intReturnCode(methodElt);
            case "java.lang.Long":
                return longReturnCode(methodElt);
            case "java.lang.Double":
                return doubleReturnCode(methodElt);
            default:
                return listReturnCode(methodElt);
        }

    }
  
    
    private CodeBlock listReturnCode(Element methodElt){
        
        System.out.printf("[ZEUS] -- METHOD ELEMENT: %s \n",methodElt);
        
        Element enclosingElement = methodElt.getEnclosingElement();
        
        System.out.printf("[ZEUS] -- ENCLOSING ELEMENT: %s \n",enclosingElement);
        
        String entityCanonicalName = daoAnnotationClassParamName(enclosingElement);
        
        TypeName entityTypeName = ClassName.get(elementsUtils.getTypeElement(entityCanonicalName));
        ClassName typedQueryClassName = ClassName.get("javax.persistence", "TypedQuery");

        TypeName entityParameterizedTypeName = ParameterizedTypeName.get(typedQueryClassName, entityTypeName);
        String namedQuery = implAnnotationParamName(methodElt);
        
        String type = daoAnnotationClassParamSimpleName(enclosingElement);
//        ClassName listClassName = ClassName.get(List.class);
        return CodeBlock.builder()
            .addStatement("$T query = em.createNamedQuery($S, $L)",entityParameterizedTypeName,namedQuery,entityTypeName.toString()+".class")
//            .addStatement("$T<$L> results =  query.getResultList()",listClassName,type)
            .add(paramsBlock(methodElt))
            .addStatement("return query.getResultList()")
            .build();
        
//        return CodeBlock.builder().addStatement("return query.getResultList()").build();
    }

    
    private CodeBlock optionalReturnCode(Element methodElt){
        
        Element enclosingElement = methodElt.getEnclosingElement();
        String entityCanonicalName = daoAnnotationClassParamName(enclosingElement);
        
        TypeName entityTypeName = ClassName.get(elementsUtils.getTypeElement(entityCanonicalName));
        ClassName typedQueryClassName = ClassName.get("javax.persistence", "TypedQuery");

        TypeName entityParameterizedTypeName = ParameterizedTypeName.get(typedQueryClassName, entityTypeName);
        String namedQuery = implAnnotationParamName(methodElt);
        
        String type = daoAnnotationClassParamSimpleName(enclosingElement);
        ClassName listClassName = ClassName.get(List.class);
        return CodeBlock.builder()
            .addStatement("$T query = em.createNamedQuery($S, $L)",
                   entityParameterizedTypeName,namedQuery,entityTypeName.toString()+".class")
            .add(paramsBlock(methodElt))
            .addStatement("$T<$L> results =  query.getResultList()",listClassName,type)
            .beginControlFlow("if(!results.isEmpty())")
            .addStatement("return Optional.of(results.get(0))")
            .endControlFlow()
            .addStatement("return Optional.empty()")
            .build();
    }
    
    private CodeBlock intReturnCode(Element methodElt){
        String namedQuery = implAnnotationParamName(methodElt);
        ClassName typedQueryClassName = ClassName.get("javax.persistence", "Query");
         
        return CodeBlock.builder()
                .addStatement("$T query = em.createNamedQuery($S)",typedQueryClassName,namedQuery)
                .add(paramsBlock(methodElt))
                .addStatement("return (Integer)query.getSingleResult()")
                .build();
    }
    
    private CodeBlock longReturnCode(Element methodElt){
        String namedQuery = implAnnotationParamName(methodElt);
        ClassName typedQueryClassName = ClassName.get("javax.persistence", "Query");
         
        return CodeBlock.builder()
                .addStatement("$T query = em.createNamedQuery($S)",typedQueryClassName,namedQuery)
                .add(paramsBlock(methodElt))
                .addStatement("return (Long)query.getSingleResult()")
                .build();
    }
    
     private CodeBlock doubleReturnCode(Element methodElt){
        String namedQuery = implAnnotationParamName(methodElt);
        ClassName typedQueryClassName = ClassName.get("javax.persistence", "Query");
         
        return CodeBlock.builder()
                .addStatement("$T query = em.createNamedQuery($S)",typedQueryClassName,namedQuery)
                .add(paramsBlock(methodElt))
                .addStatement("return (Double)query.getSingleResult()")
                .build();
    }
    
    private CodeBlock paramsBlock(Element executableElement){
        ExecutableElement execElt = (ExecutableElement)executableElement;
        List<CodeBlock> codeBlocks = execElt.getParameters().stream()
                  .map(ve -> setParamsValues(ve))
                  .collect(Collectors.toList());

        return CodeBlock.join(codeBlocks, "");
    }
        
    private CodeBlock setParamsValues(VariableElement variableElement){
        return CodeBlock.builder()
                    .addStatement("query.setParameter($S, $L)", 
                            variableElement.getSimpleName().toString(),
                            variableElement.getSimpleName().toString()).build();
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
