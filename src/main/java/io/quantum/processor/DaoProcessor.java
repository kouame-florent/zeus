/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.quantum.processor;



import io.quantum.annotation.DAO;
import io.quantum.annotation.DAOImpl;
import io.quantum.annotation.QueryImpl;
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
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import io.quantum.annotation.WithDao;
import javax.annotation.processing.Processor;

/**
 *
 * @author root
 */
//@AutoService(Processor.class)
@SupportedAnnotationTypes({
    "io.quantum.annotation.DAO",
    "io.quantum.annotation.DAOImpl",
    "io.quantum.annotation.QueryImpl"
})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class DaoProcessor extends AbstractProcessor{
    
    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;
    
    
//    EntityDaoImplFactory entityDaoImplFactory;
    
    GenericDaoFactory genericDaoFactory;
    GenericDaoImplFActory genericDaoImplFActory;
    
    DaoFactory daoFactory;
    DaoImplFactory daoImplFactory;
            
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv){ 
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
                          
//        entityDaoFactory = new DaoFactory(filer, messager);
//        entityDaoImplFactory = new EntityDaoImplFactory(filer, messager);


        genericDaoFactory = new GenericDaoFactory();
        genericDaoImplFActory = new GenericDaoImplFActory();
        
        daoFactory = new DaoFactory(processingEnv);
        daoImplFactory = new DaoImplFactory(processingEnv);
    }
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        
        roundEnv.getElementsAnnotatedWithAny(Set.of(DAO.class,DAOImpl.class,QueryImpl.class))
                .stream()
                .forEach(e -> {  
                    daoFactory.add(e);
                    daoImplFactory.add(e);
                });
            
//       GenericDaoFactory daoFactory = new GenericDaoFactory();
//       daoFactory.generateCode(filer, processingEnv);
        
//      GenericDaoImplFActory genericDaoImplFActory = new GenericDaoImplFActory();
        genericDaoFactory.generateCode(filer, processingEnv);
        genericDaoImplFActory.generateCode(filer, processingEnv);
        
        daoFactory.generateCode(processingEnv);
        daoImplFactory.generateCode(processingEnv);
        
        daoFactory.clearAnnotatedElements();
        daoImplFactory.clearAnnotatedElements();
        
        return true;
        
    }   
    
    private void error(Element e, String msg, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR,String.format(msg, args),e);
    }
    
   private void log(Element e, String msg, Object... args) {
        messager.printMessage(Diagnostic.Kind.NOTE,String.format(msg, args),e);
    }

}
