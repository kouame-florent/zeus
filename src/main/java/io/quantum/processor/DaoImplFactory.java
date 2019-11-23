/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.quantum.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.quantum.annotation.DAOImpl;
import io.quantum.annotation.QueryImpl;
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
import javax.lang.model.element.TypeElement;
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
    
    
    private String daoImplParamCanonicalName(Element annotatedElement){
        try{
           DAOImpl daoImplAnnotation = annotatedElement.getAnnotation(DAOImpl.class);
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
        
    TypeSpec buildClassBody(Element element) {
 
        String entityDaoImplName = targetClassName(element);
        String entityDaoName = sourceInterfaceName(element);
      
        ClassName genricDaoImpl = ClassName.get(PackageName.GENERIC_DAO_IMPL.pkgName(), "GenericDAOImpl");
        ClassName entityDaoClassName = ClassName.get((TypeElement)element);
        
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
                       
        TypeSpec entityDao = TypeSpec.classBuilder(entityDaoImplName)
                .superclass(ParameterizedTypeName.get(genricDaoImpl,entityTypeName,TypeName.get(String.class)))
                .addSuperinterface(entityDaoClassName)
                .addModifiers(Modifier.PUBLIC)
                .addMethods(methods)
                .addMethod(constructor)
                .build();
            
        return entityDao;
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
               .addStatement("return query.getResultList()")
               
//               .addStatement(DefaultStatement.UNSUPPORTED_OPERATION_EXCEPTION.statement())
               .returns(returnTypeName)
               .build();
       
        return methodSpec;
    
    }
    
    
  private CodeBlock addCodeBlock(Element executableElement){
      ExecutableElement execElt = (ExecutableElement)executableElement;
     
      List<CodeBlock> codeBlocks = execElt.getParameters().stream()
                .map(ve -> createCodeLine(ve))
                .collect(Collectors.toList());
      
      return CodeBlock.join(codeBlocks, ";");
  }
  
    
    private CodeBlock createCodeLine(VariableElement variableElement){
        return CodeBlock.builder()
                    .addStatement("query.setParameter($S, $L)", 
                            variableElement.getSimpleName().toString(),
                            variableElement.getSimpleName().toString()).build();
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
