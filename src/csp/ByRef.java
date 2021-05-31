/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package csp;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author s425770
 */
public class ByRef implements Serializable, Cloneable{
    private Object val;
    
    public ByRef(Object val){
        this.val = val;
    }
    
    public void setValue(Object val){
        this.val = val;
    }
    
    public Object getVal(){
        return this.val;
    }

    private ByRef(){
        ;
    }
    @Override
    public String toString() {
        return val.toString();
    }

    @Override
    public Object clone(){
        ByRef ref;
            
        try {
            ref = (ByRef) super.clone();
            return ref;
        } catch (CloneNotSupportedException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    
    
    
}
