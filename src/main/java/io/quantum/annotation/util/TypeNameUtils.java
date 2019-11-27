/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.quantum.annotation.util;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 *
 * @author root
 */
public class TypeNameUtils {
    
    public static String declareTypeName(ProcessingEnvironment env,TypeElement typeElement){
        return env.getTypeUtils().getDeclaredType(typeElement).toString();
    } 
    
    public static String returnTypeName(ProcessingEnvironment env,ExecutableElement exec){
        TypeMirror typeMirror = exec.getReturnType();
        TypeElement typeElement = (TypeElement)env.getTypeUtils().asElement(typeMirror);
        return env.getTypeUtils().getDeclaredType(typeElement).toString();
    }
    
        
    public static boolean isPrimitive(TypeMirror type) {
        TypeKind kind = type.getKind();
        return kind == TypeKind.BOOLEAN || kind == TypeKind.BYTE
                || kind == TypeKind.CHAR || kind == TypeKind.DOUBLE
                || kind == TypeKind.FLOAT || kind == TypeKind.INT
                || kind == TypeKind.LONG || kind == TypeKind.SHORT;
    }
    
   
    static TypeName box(PrimitiveType primitiveType) {
        switch (primitiveType.getKind()) {
          case BYTE:
            return ClassName.get(Byte.class);
          case SHORT:
            return ClassName.get(Short.class);
          case INT:
            return ClassName.get(Integer.class);
          case LONG:
            return ClassName.get(Long.class);
          case FLOAT:
            return ClassName.get(Float.class);
          case DOUBLE:
            return ClassName.get(Double.class);
          case BOOLEAN:
            return ClassName.get(Boolean.class);
          case CHAR:
            return ClassName.get(Character.class);
          case VOID:
            return ClassName.get(Void.class);
          default:
            throw new AssertionError();
        }
      }
    
    public static PackageElement getPackage(Element type) {
        while (type.getKind() != ElementKind.PACKAGE) {
          type = type.getEnclosingElement();
        }
        return (PackageElement) type;
      }
   
}
