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
public class Element implements Comparable, Serializable {
    public double val;
    public int idx;
    public int [] arrIdx;
    //public static final int ASCENDING = 0;
    //public static final int DESCENDING = 1; 
    //public static int sortOrder = ASCENDING; //Not a very good idea.... Element is a commonly used class bhai. 
                                        //use Collections.sort(arry,Collections.reverseOrder()) for descending

    private Element(){
        ;
    }
    public Element(double val, int idx){//, int ... params){
        this.val = val;
        this.idx = idx; 
        //order = params.length > 0 ? params[0] : this.ASCENDING;
    }
    public Element(double val, int []arrIdx){//, int ... params){
        this.val = val;
        this.arrIdx = arrIdx; 
//        sortOrder = params.length > 0 ? params[0] : this.ASCENDING;
    }
    public Element(int val, int idx){//, int ... params){
        this.val = val;
        this.idx = idx;
//        sortOrder = params.length > 0 ? params[0] : this.ASCENDING;
    }
    public Element(int val, int []arrIdx){//, int ... params){
        this.val = val;
        this.arrIdx = arrIdx; 
//        sortOrder = params.length > 0 ? params[0] : this.ASCENDING;
    }
    public int compareTo(Object o) {
        if (!(o instanceof Element)) {
          throw new ClassCastException("Not an Element object");
        }
        Element e = (Element) o;
////        //if(sortOrder == ASCENDING)
            return (int)(val - e.val);
////        //else
////            //return (int)(e.val - val);
    }

    @Override
    public String toString() {
        return "[val: " + val + ", idx: " + idx + "]";
    }
    
    

}
