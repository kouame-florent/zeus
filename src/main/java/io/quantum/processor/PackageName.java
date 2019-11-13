/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.quantum.processor;

/**
 *
 * @author root
 */
public enum PackageName {
    
    GENERIC_DAO("io.quantum.dao"),
    GENERIC_DAO_IMPL("io.quantum.dao"),
    ENTITY_DAO("io.quantum.dao"),
    ENTITY_DAO_IMPL("io.quantum.dao.impl");
    
    private final String pkgName;
    
    private PackageName(String name){
        this.pkgName = name;
    }

    public String pkgName() {
        return pkgName;
    }
}
