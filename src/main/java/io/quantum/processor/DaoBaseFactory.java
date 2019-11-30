/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.quantum.processor;

import io.quantum.annotation.DAO;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 *
 * @author root
 */
public abstract class DaoBaseFactory {
    
    protected final ProcessingEnvironment processingEnv;
    protected final Filer filer;
    protected final Messager messager;
    protected final Types typesUtils;
    protected final Elements elementsUtils;
    protected final Class<DAO> acceptedClass = DAO.class;

    public DaoBaseFactory(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
        this.elementsUtils = processingEnv.getElementUtils();
        this.typesUtils = processingEnv.getTypeUtils();
    }
        
    protected String daoAnnotationParamName(Element annotatedElement){
        try{
           DAO daoAnnotation = annotatedElement.getAnnotation(DAO.class);
           return daoAnnotation.forClass().getCanonicalName();
           
        }catch (MirroredTypeException e) {
            return e.getTypeMirror().toString();
        }
 
    }
     
    protected String daoAnnotationParamSimpleName(Element annotatedElement){
        try{
           DAO daoAnnotation = annotatedElement.getAnnotation(DAO.class);
           String name = daoAnnotation.forClass().getSimpleName();
           return name;
           
        }catch (MirroredTypeException e) {

            String name = typesUtils.asElement(e.getTypeMirror()).getSimpleName().toString();

            return name;
        }
 
    }
    
}
