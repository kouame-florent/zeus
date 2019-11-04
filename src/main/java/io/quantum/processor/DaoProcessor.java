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
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 *
 * @author root
 */

@SupportedAnnotationTypes("io.quantum.annotation.Dao")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class DaoProcessor extends AbstractProcessor{
    
    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;
    
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv){ 
        super.init(processingEnv);
    }
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,"---> FOUND @Dao at ");
        
        return true;
    }
    
   
    
    
    
}
