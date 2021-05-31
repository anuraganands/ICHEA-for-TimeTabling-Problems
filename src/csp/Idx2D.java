/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package csp;

import java.io.Serializable;

/**
 *
 * @author s425770
 */
  
/**
* Specifically designed for 2D problems like time tabling where it is 
* important to identify a given value (eg. course in a timetable). This
* value is identified by horizontal column {@code col} and verticle position
* {@code pos}.
*/
public class Idx2D implements Serializable, Cloneable{
    public int col;
    public int position;
    
    /**
     * Specifically designed for 2D problems like time tabling where it is 
     * important to identify a given value (eg. course in a timetable). This
     * value is identified by horizontal column {@code col} and verticle position
     * {@code pos}.
     * @param col
     * @param pos 
     */
    public Idx2D(int col, int pos){
        this.col = col;
        this.position = pos;
    }
    
    public Idx2D(){
        col = -1;
        position = -1;
    }    
    
    public boolean isEmpty(){
        return (col == -1 || position == -1);
    }
    
    @Override
    public Object clone() {
        try{
            return super.clone();
        }catch(CloneNotSupportedException cnse){
            cnse.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        if(col == -1 || position == -1)
            return " ";
        else
            return "[" + col + "," + position + ']';
    }
    
    
}