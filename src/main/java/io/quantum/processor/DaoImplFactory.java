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
import io.quantum.annotation.DAO;
import io.quantum.annotation.QueryImpl;
import io.quantum.annotation.util.DefaultPackage;
import io.quantum.annotation.util.TypeNameUtils;
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
public class DaoImplFactory {
    
    private final ProcessingEnvironment processingEnv;
    private final Filer filer;
    private final Messager messager;
    private final Types typesUtils;
    private final Elements elementsUtils;
    private final Class<DAO> acceptedClass = DAO.class;
    
    public List<Element> annotatedElements = new ArrayList<>();
    public List<Element> claimedElements = new ArrayList<>();

    public DaoImplFactory(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
        this.elementsUtils = processingEnv.getElementUtils();
        this.typesUtils = processingEnv.getTypeUtils();
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
        
        String entityCanonicalName = daoImplParamCanonicalName(element);
        
        TypeName entityTypeName = 
                ClassName.get(elementsUtils.getTypeElement(entityCanonicalName));
          
        System.out.printf("[ZEUS] ENTITY TYPE NAME: %s \n",entityTypeName);
           
        List<Element> enclosedElements = (List<Element>) element.getEnclosedElements()
                .stream().filter(e -> e.getKind() == ElementKind.METHOD)
                .collect(Collectors.toList());
        
        List<MethodSpec> methods =  getAnnotatedMethods(enclosedElements).stream()
                .map(e -> buildMethods(e,element)).collect(Collectors.toList());
        
           
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
                .addMethods(methods)
                .addMethod(constructor)
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
        System.out.printf("[ZEUS] ANNOTATED ELT: %s \n",annotatedElement.toString());
        System.out.printf("[ZEUS] ANNOTATED ELT KIND: %s \n",annotatedElement.getKind().toString());
        if(annotatedElement.getKind() == ElementKind.INTERFACE){
            annotatedElements.add(annotatedElement);
        }
    }
    
    public void clearAnnotatedElements(){
        annotatedElements.clear();
    }
    
    private String targetClassName(Element interfaceElement){
        return annotationClassParamSimpleName(interfaceElement) + "DAO" + "Impl";
    }
    
    private String targetSuperInterfaceName(Element interfaceElement){
        return annotationClassParamSimpleName(interfaceElement) + "DAO";
    }
    

    private String annotationClassParamSimpleName(Element annotatedElement){
        try{
           DAO daoImplAnnotation = annotatedElement.getAnnotation(DAO.class);
           String name = daoImplAnnotation.forClass().getSimpleName();
           return name;
           
        }catch (MirroredTypeException e) {
            System.out.printf("[ZEUS] MIRRORED TYPE EXCEPTION: %s \n",e.getTypeMirror());
            String name = typesUtils.asElement(e.getTypeMirror()).getSimpleName().toString();
            System.out.printf("[ZEUS] DAO CLASS SIMPLE NAME : %s \n",name);
            return name;
        }
 
    }
        
    private String daoImplParamCanonicalName(Element annotatedElement){
        try{
           DAO daoImplAnnotation = annotatedElement.getAnnotation(DAO.class);
           return daoImplAnnotation.forClass().getCanonicalName();
           
        }catch (MirroredTypeException e) {
            System.out.printf("[ZEUS] MIRRORED TYPE EXCEPTION: %s \n",e.getTypeMirror());
            return e.getTypeMirror().toString();
        }
 
    }
        
    private String queryImplParamCanonicalName(Element queryImplElement){
        QueryImpl queryImplAnnotation = queryImplElement.getAnnotation(QueryImpl.class);
        return queryImplAnnotation.queryName();
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
   
  
    private MethodSpec buildMethods(Element executableElement,Element enclosingElement){
        
        ExecutableElement execElt = (ExecutableElement)executableElement;
            
        TypeMirror returnType = execElt.getReturnType();
        TypeName returnTypeName = ClassName.get(returnType);
        
        List<? extends VariableElement> params = execElt.getParameters();
        List<ParameterSpec> paramsSpecs = params.stream().map(ParameterSpec::get).collect(Collectors.toList());
        
        String entityCanonicalName = daoImplParamCanonicalName(enclosingElement);
        
        TypeName entityTypeName = 
                ClassName.get(elementsUtils.getTypeElement(entityCanonicalName));
        ClassName typedQueryClassName = ClassName.get("javax.persistence", "TypedQuery");

        TypeName entityParameterizedTypeName = ParameterizedTypeName.get(typedQueryClassName, entityTypeName);
        
        String namedQuery = queryImplParamCanonicalName(executableElement);
                
        MethodSpec methodSpec = MethodSpec
               .methodBuilder(execElt.getSimpleName().toString())
               .addModifiers(Modifier.PUBLIC)
               .addAnnotation(Override.class)
               .addParameters(paramsSpecs)
               .addStatement("$T query = em.createNamedQuery($S, $L)",
                       entityParameterizedTypeName,namedQuery,entityTypeName.toString()+".class")
               .addCode(addCodeBlock(executableElement))
               .addCode(returnStatement(execElt,enclosingElement))
               
//               .addStatement(DefaultStatement.UNSUPPORTED_OPERATION_EXCEPTION.statement())
               .returns(returnTypeName)
               .build();
       
        return methodSpec;
    
    }
    
    private CodeBlock returnStatement(ExecutableElement execElt,Element enclosingElement){
        String qualifiedName = TypeNameUtils.returnTypeName(processingEnv, execElt);
        System.out.printf("[ZEUS] -- RETURN TYPE NAME: %s \n",qualifiedName);
        
        switch(qualifiedName){
            case "java.util.List":
                 return CodeBlock.builder().addStatement("return query.getResultList()").build();
            case "java.util.Optional":
                return returnTypeCode(enclosingElement);
            default:
                return CodeBlock.builder().addStatement("return query.getResultList()").build();
        }

    }

    
    private CodeBlock returnTypeCode(Element elt){
        String type = annotationClassParamSimpleName(elt);
        ClassName listClassName = ClassName.get(List.class);
        return CodeBlock.builder()
                .addStatement("$T<$L> results =  query.getResultList()",listClassName,type)
                .beginControlFlow("if(!results.isEmpty())")
                .addStatement("return Optional.of(results.get(0))")
                .endControlFlow()
                .addStatement("return Optional.empty()")
                .build();
    }
    
    private CodeBlock addCodeBlock(Element executableElement){
        ExecutableElement execElt = (ExecutableElement)executableElement;

        List<CodeBlock> codeBlocks = execElt.getParameters().stream()
                  .map(ve -> createCodeLine(ve))
                  .collect(Collectors.toList());

        return CodeBlock.join(codeBlocks, "");
    }
  
    
    private CodeBlock createCodeLine(VariableElement variableElement){
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
