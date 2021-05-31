/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package csp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 *
 * @author s425770
 */
public class MyRandom extends Random{
    public MyRandom(){
        super();        
    }
    
     /**
     * randomly generates an integer between minVal and maxVal.
     *
     * @param minVal = minimum value of sequence INCLUSIVE
     * @param maxVal = maximum value of sequence INCLUSIVE
     * @return - Integer value between minVal to maxVal
     */
    public int randVal(int minVal, int maxVal){
        int rand;
        
        if(minVal > maxVal)
            throw new ArithmeticException();

        rand = minVal+this.nextInt(maxVal-minVal+1);              
        return rand;    
    }

    /**
     * randomly generates a double val between minVal and maxVal
     *
     * @param minVal = minimum value of sequence INCLUSIVE
     * @param maxVal = maximum value of sequence INCLUSIVE
     * @return - Double value between minVal to maxVal
     */
    public Double randVal(Double minVal, Double maxVal){
        Double rand;

        if(minVal > maxVal)
            throw new ArithmeticException();

        rand = minVal + (maxVal - minVal)*this.nextDouble();
        return rand;
    }
    
    public ArrayList<Integer> randComb(int minVal, int maxVal, int totalNum){
        ArrayList<Integer> rand = new ArrayList<Integer>();
        int randVal;

        if(minVal > maxVal)
            throw new ArithmeticException();
        
        for (int i = 0; i < totalNum; i++) {
            randVal = this.randVal(minVal, maxVal);
            rand.add(randVal);
        }
        
        return rand;
    }
    
    /**
     * randperm - Similar to randperm of Matlab
     * Generates random integer sequence from minVal inclusive to maxVal inclusive
     * @param minVal - minimum value (inclusive) of the sequence requested
     * @param maxVal - maximum value (EXclusive) of the sequence requested <br>
     * older version was INclusive.
     * @return ArrayList<Integer> of random sequence from minVal to maxVal
     */
    public static ArrayList<Integer> randperm(int minVal, int maxVal){
        ArrayList<Integer> rand = new ArrayList<Integer>();

        if(minVal > maxVal)
            throw new ArithmeticException();

        for (int i = minVal; i < maxVal; i++) {
            rand.add(i);
        }
        Collections.shuffle(rand);
        return rand;    
    }    
}
