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
public enum DefaultStatement {
    
    UNSUPPORTED_OPERATION_EXCEPTION("throw new UnsupportedOperationException(\"Not supported yet.\")");
    
    private final String statement;
    
    private DefaultStatement(String statement){
        this.statement = statement;
    }
    
    public String statement(){
        return this.statement;
    }
}
