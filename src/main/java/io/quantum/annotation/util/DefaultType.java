/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.quantum.annotation.util;

/**
 *
 * @author root
 */
public enum DefaultType {
    
    GENERIC_DAO("io.quantum.dao","GenericDAO","io.quantum.dao.GenericDAO"),
    GENERIC_DAO_IMPL("io.quantum.dao","GenericDAOImpl","io.quantum.dao.GenericDAOImpl"),
    LIST("java.util","List","java.util.List"),
    OPTIONAL("java.util","Optional","java.util.Optional");
      
    private final String packageName;
    private final String className;
    private final String qualifiedName;
    
    private DefaultType(String packageName,String className,String qualifiedName){
        this.packageName = packageName;
        this.className = className;
        this.qualifiedName = qualifiedName;
    }

    public String packageName() {
        return packageName;
    }
    
    public String className(){
        return className;
    }
    
    public String qualifiedName(){
        return qualifiedName;
    }
}
