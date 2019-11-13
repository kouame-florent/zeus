/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.quantum.processor;


import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import io.quantum.annotation.WithDao;

/**
 *
 * @author root
 */

@SupportedAnnotationTypes({
    "io.quantum.annotation.WithDao",
    "io.quantum.annotation.Sealed"
})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class DaoProcessor extends AbstractProcessor{
    
    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;
    
    EntityDaoFactory entityDaoFactory;
    EntityDaoImplFactory entityDaoImplFactory;
    
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv){ 
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        
        
        entityDaoFactory = new EntityDaoFactory(filer, messager);
        entityDaoImplFactory = new EntityDaoImplFactory(filer, messager);
    }
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
               
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(WithDao.class)) {
            if(annotatedElement.getKind() != ElementKind.CLASS){
                error(annotatedElement, "Only classes can be annotated with @%s", WithDao.class.getSimpleName());
                return true;
            }else{
                
                entityDaoFactory.add(annotatedElement);
                entityDaoImplFactory.add(annotatedElement);
                
            }
        }
    
        GenericDaoFactory daoFactory = new GenericDaoFactory();
        daoFactory.generateCode(filer, processingEnv);
        
        GenericDaoImplFActory daoImplFActory = new GenericDaoImplFActory();
        daoImplFActory.generateCode(filer, processingEnv);
        
        entityDaoFactory.generateCode(processingEnv);
        entityDaoImplFactory.generateCode(processingEnv);
        
        entityDaoFactory.clearAnnotatedElements();
        entityDaoImplFactory.clearAnnotatedElements();
        
        return true;
        
    }  
    
    private void error(Element e, String msg, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR,String.format(msg, args),e);
    }
    
   private void log(Element e, String msg, Object... args) {
        messager.printMessage(Diagnostic.Kind.NOTE,String.format(msg, args),e);
    }

}
