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
public enum DefaultPackage {
    ENTITY_DAO("io.quantum.dao"),
    ENTITY_DAO_IMPL("io.quantum.dao.impl");
      
    private final String packageName;
  
    
    private DefaultPackage(String packageName){
        this.packageName = packageName;
        
    }

    public String packageName(){
        return packageName;
    }
}
