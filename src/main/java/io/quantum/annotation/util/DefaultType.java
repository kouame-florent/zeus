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
    
    GENERIC_DAO("io.quantum.dao","GenericDAO"),
    GENERIC_DAO_IMPL("io.quantum.dao","GenericDAOImpl");
      
    private final String pkgName;
    private final String entityName;
    
    private DefaultType(String pkgName,String entityName){
        this.pkgName = pkgName;
        this.entityName = entityName;
    }

    public String pkgName() {
        return pkgName;
    }
    
    public String entityName(){
        return entityName;
    }
}
