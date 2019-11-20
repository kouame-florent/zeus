/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.quantum.annotation.visitor;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor9;

/**
 *
 * @author root
 */
public class DAOClassVisitor extends SimpleAnnotationValueVisitor9<Void, Void>{
    
    @Override
    public Void visitType(TypeMirror t, Void p) {
        System.out.printf(">> classValue: %s\n", t.toString());
        return p;
    }
}
