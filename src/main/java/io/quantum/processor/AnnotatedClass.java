/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.quantum.processor;


import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;

/**
 *
 * @author root
 */
public class AnnotatedClass {
    
    private final TypeElement annotatedElement;
    private final Name qualifiedTypeName;
    private final Name simpleTypeName;

    public AnnotatedClass(TypeElement annotatedElement) {
        this.annotatedElement = annotatedElement;
        this.qualifiedTypeName = annotatedElement.getQualifiedName();
        this.simpleTypeName = annotatedElement.getSimpleName();
        
    }

    public TypeElement getAnnotatedElement() {
        return annotatedElement;
    }

    public Name getQualifiedTypeName() {
        return qualifiedTypeName;
    }

    public Name getSimpleTypeName() {
        return simpleTypeName;
    }
    
    
}
