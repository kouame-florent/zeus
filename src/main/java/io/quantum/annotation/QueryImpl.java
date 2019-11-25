/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.quantum.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author root
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface QueryImpl {
    String queryName() default "";
    Type returnType() default Type.LIST;
    
    enum Type{LIST,OPTIONAL;};
}
