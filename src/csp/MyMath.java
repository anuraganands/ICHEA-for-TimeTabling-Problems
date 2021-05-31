/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package csp;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import javax.activation.UnsupportedDataTypeException;
import org.jdesktop.application.Application;

/**
 *
 * @author Anurag
 */
public class MyMath {
    /**
     * Calculates euclidean distance between two vectors.<br>
     */
    public static final int DIST_EUCLEADIAN = 1;  
    /**
     * Calculate hamming distance by checking dissimilarity of each element.
     * The hamming distance is the count of dissimilar elements.
     * For eg.<BR>
     * <code>1 2 3 4 <BR>
     * 1 3 7 <BR>
     * ----------- <BR>
     * 0 1 1 1 = 3<BR>
     * </code>
     */
    public static final int DIST_HAMMING_ABSOLUTE = 2;
    /**
     * <B>&lt;Not working properly></B> Currently only checks
     * the number of mismatches in bigger vector. 
     * For eg. <BR>
     * <code> p1 = {1 2 3 4}, p2 = {1 3 7} <BR>
     * mismatch => {2,4} in p1 because p1 is bigger.<BR></code>
     */
    public static final int DIST_DUPLICATE_MISMATCH = 3;
    /**
     * This is a hamming distance between two arrays based on the <B>pattern
     * matching</B>. For eg. <BR> 
     * <code>
     * p1 = {1 <u>2 3 8 9</u>}, p2 = {<u>2 3</u> 4 5 6 <u>8 9</u> 10} has some common
     * elements (patterns) that are {2 3} and {8 9}. The hamming distance is total
     * mismatched elements which is {1} from p1 and {4 5 6 10} from p2 which
     * is 1+4 = 5. Hence the hamming distance is 5.
     * </code><BR>
     * <B>Precaution:</B> input arrays will be sorted.
     */
    public static final int DIST_HAMMING_PATTERN = 4;
    /**
     * Checks only the matching values. For eg. <BR>
     * <code>
     * p1 = {1 <u>2 3 8 9</u>}, p2 = {<u>2 3</u> 4 5 6 <u>8</u> 10} has some common
     * elements (patterns) that are {2 3} and {8} = {2 3 8}. So the total match
     * is 3.
     * </code>
     * <B>Precaution:</B> input arrays will be sorted.
     */
    public static final int DIST_COMMON_MATCH = 5;
    private static ArrayList<String> permutes;
    
    /**
     * NOTE: It gives unpredictable results if Double value is very large
     * and cannot be converted to long.
     * @param n
     * @param decimalPlaces
     * @return 
     */
    public static double roundN(Double n, int decimalPlaces) {
        if(decimalPlaces < 0){
            //throw new InvalidAlgorithmParameterException("dcimal Place should be positive");
            System.err.println("decimal Place should be positive");
            Application.getInstance().exit();
        }
        
        String fmt = "#.";
        for (int i = 0; i < decimalPlaces; i++) {
            fmt += "#";
        }
        String num = new DecimalFormat(fmt).format(n);
        return Double.valueOf(num);
        
//        if(n > -1.0E14 && n < 1.0E14){
//            long temp;       
//            temp = Math.round(n*Math.pow(10.0,decimalPlaces));
//            return temp/Math.pow(10.0,decimalPlaces);
//        }else{           
//            String snum = n.toString();
//            int dpIdx;
//            int Eidx;
//            int totalDp;
//
//            dpIdx = snum.indexOf("."); //cannot be < 0 in java
//            Eidx = snum.indexOf("E");
//            String numPart = "";
//            String EPart = ""; 
//
//            if(Eidx<0){
//                totalDp = snum.length()-(dpIdx+1);
//                numPart = snum;
//            }else{
//                totalDp = Eidx - (dpIdx+1);
//                EPart = snum.substring(Eidx+1, snum.length());
//                numPart = snum.substring(0, Eidx);
//            }
//
//            String zeros = "";
//            String round = "";
//            if(decimalPlaces > totalDp){
//                for (int i = 0; i < decimalPlaces-totalDp; i++) {
//                    zeros += "0";
//                }
//
//                numPart += zeros;
//            }else if (decimalPlaces == totalDp){
//                ;//no changes
//            }else{//round
//                round = numPart.substring(dpIdx+decimalPlaces+1, dpIdx+decimalPlaces+2);
//                if(Integer.parseInt(round)>4)
//            }
//        }
    }

    /**
     * Retrieves the indices of outliers of the given list. Doesn't remove any 
     * element.
     * @param <T>
     * @param fit
     * @param bRemoveLeft - low values
     * @param bRemoveRight - hight values
     * @param criteria can be 0.0 - 1.5 - 3.0 etc
     * @return index of array to be removed in REVERSE order
     */
    public static <T extends Number & Comparable<T>> ArrayList<Integer> getOutliers(final ArrayList<T> fit, boolean bSortFirst,
            boolean bRemoveLeft, boolean bRemoveRight, double criteria){
        ArrayList<Integer> idx = new ArrayList<Integer>();
        double median = MyMath.median(fit, bSortFirst);
        double LQ = MyMath.median(new ArrayList<T>(fit.subList(0, fit.size()/2 )), false);
        double UQ = MyMath.median(new ArrayList<T>(fit.subList(fit.size()/2,fit.size() )), false);
        double IQR = UQ-LQ;
//        double criteria = 0.0;//can also use 3.0
        double lowerLimit = LQ - criteria*IQR;
        double upperLImit = UQ + criteria*IQR;
        
        for (int i = fit.size()-1; i>=0; i--) {
            if(fit.get(i).doubleValue()>upperLImit){
                idx.add(i);  
            }else{
                break;
            }            
        }

        for (int i = 0; i < fit.size(); i++) {
            if(fit.get(i).doubleValue()<lowerLimit){
                idx.add(i); 
            }else{
                break;
            }
        }

        Collections.sort(idx, Collections.reverseOrder());
        return idx;
    }
    
    /**
     * Sequence from minVal to maxVal incremented by 1. 
     * @param minVal minimum value of the sequence
     * @param maxVal maximum value of the sequence
     * @return sequence returned
     */
    public static ArrayList<Integer> sequence(int minVal, int maxVal){
        ArrayList<Integer> seq = new ArrayList<Integer>();
        
        for (int i = minVal; i <= maxVal; i++) {
            seq.add(i);
        }
        return seq;
    }
    
    public static ArrayList<String> getBinPermutes(int size){
         permutes = new ArrayList<String>();
        
         printBin("", size);
         return permutes;
    }

    public static void printBin(String soFar, int iterations) {
        if(iterations == 0) {
            permutes.add(soFar);
        }
        else {
            printBin(soFar + "0", iterations - 1);
            printBin(soFar + "1", iterations - 1);
        }
    }
    
    public static ArrayList<ArrayList<Double>> getXrandomBinPermutes(final int size,final int limit){
        ArrayList<ArrayList<Double>> combinations = new ArrayList<ArrayList<Double>>();
        
         //printBin("", size);
        double pmOne; //plus minus one
        //int []bits;
        final int combSize = (int)Math.min(limit, Math.pow(2, size));
        
        for (int i = 0; i < combSize; i++) {
            combinations.add(new ArrayList<Double>());
        }
        
        for (int i = 0; i < combSize; i++) {
            //bits = new int[size];
            for (int j = 0; j < size; j++) {
                pmOne = 1.0;
                if(Math.random() < 0.5){
                    pmOne = -1.0;
                } 
                combinations.get(i).add(pmOne);          
            }            
        }
         return combinations;
    }
    
    public static ArrayList<Double> constMultiplicationToVector(double var, final ArrayList<Double> v){
        ArrayList<Double> result;        
        result = new ArrayList<Double>();
                
        for (int i = 0; i < v.size(); i++) {
            result.add(var*v.get(i));
        }
        
        return result;
    }
    
    public static double stdDev(ArrayList<Double> v){
        double diffSq = 0; //(xi - mu)^2
        double mu = mean(v);
        
        for (int i = 0; i < v.size(); i++) {
            diffSq = Math.pow(v.get(i).doubleValue() - mu,2);            
        }
        return Math.sqrt(diffSq/v.size());
    }
    
    
    /**
     * Vector multiplication <br>
     * @param entryByEntry true or false <br>
     * if entryByEntry is true then - entry of v1 is multiplied by same index entry of v2
     * if entryByEntry is false - CURRENTLY NOT IMPLEMENTED
     * @param v1 first vector or scalar
     * @param v2 second vector or scalar
     * @return vector multiplication
     * @throws UnsupportedDataTypeException
     */
    public static ArrayList<Double> vectorMultiplication(boolean entryByEntry, final ArrayList<Double> v1, final ArrayList<Double> v2) throws UnsupportedDataTypeException{
        ArrayList<Double> result;
        int resultSize;  
        
        resultSize = v1.size();
        if (v2.size() > v1.size()){
            resultSize = v2.size();
        }
        
        result = new ArrayList<Double>(resultSize);
        
        if(entryByEntry){
                      
               
            if (v1.size() == 1){
                if (v2.size() == 1){
                    result.add(v1.get(0)*v2.get(0));
                }
                else{
                    for (int i = 0; i < v2.size(); i++) {
                        result.add(v1.get(0)*v2.get(i));
                    }
                }
            }
            else{
                if (v2.size() == 1){
                    for (int i = 0; i < v1.size(); i++) {
                        result.add(v1.get(i) * v2.get(0));
                    }
                }
                else{
                    if(v1.size() != v2.size()){
                        throw new UnsupportedOperationException("Dimensions of both point must be same.");
                    }
                    for (int i = 0; i < v1.size(); i++) {
                        result.add(v1.get(i)*v2.get(i));
                    }
                }
            }
            return result;
        }
        return null;
    }
    
    /**
     * Double norm is similar to Matlab's norm. It calculates distance
     * between 2 n dimensional points/vectors.
     * @param p1 - first n dimensional point
     * @param p2 - second n dimensional point
     * @param distancType Following distance types are accepted: <BR>
     * -DIST_EUCLEADIAN = 1: calculates euclidean distance between two vectors.<br>
     * -DIST_HAMMING_ABSOLUTE = 2: calc hamming distance by checking dissimilarity of each element.
     * the hamming distance is the count of dissimilar elements.
     * <I>For eg.</I><BR>
     * <code> 1 2 3 4 <BR>
     * 1 3 7 <BR>
     * ----------- <BR>
     * 0 1 1 1 = 3<BR>
     * </code>
     * -DIST_DUPLICATE_MISMATCH = 3:<<B> Not working properly></B> Currently only checks
     * the number of mismatches in bigger vector. 
     * For eg. <BR>
     * <code> p1 = {1 2 3 4}, p2 = {1 3 7} <BR>
     * mismatch => {2,4} in p1 because p1 is bigger. norm = 2. <BR></code>
     * @param additionalInfo This is used only for {@link #DIST_HAMMING_ABSOLUTE} only. It requires
     * two additional parameters. First one is for "max distance allowed" and second one
     * only to check if "max distance allowed" is failed or not. The returned distance
     * is will not be correct if {@code additionalInfo[1]} is true;  
     * @return Euclidean distance between the given 2 points is returned. <B>Note</B> if
     * calculating hamming distance and {@code additionalInfo[1]} is true then the returned
     * value is <B>incorrect</B>.
     */
    public static <T extends Number & Comparable<T>> double norm(final ArrayList<T> p1, final ArrayList<T> p2, 
            final int distanceType, Object ... additionalInfo){
        int size;
        Double sum;
        
        if(distanceType == DIST_EUCLEADIAN){
            if(p1.size() != p2.size()){
                throw new UnsupportedOperationException("Dimensions of both point must be same.");
            }

            size = p1.size();

            sum = 0.0;
            for (int i = 0; i < size; i++) {
                sum += Math.pow(p1.get(i).doubleValue() - p2.get(i).doubleValue(),2);
            }
            sum = Math.sqrt(sum);
        }else if(distanceType == DIST_DUPLICATE_MISMATCH){
            int duplicates = 0;
            int min;
            ArrayList<Number> unique = new ArrayList<Number>(p1);
            unique.addAll(p2);
            
            min = p1.size();
            if(p2.size()<p1.size())
                min = p2.size();
            
            HashSet<Number> hashSet = new HashSet<Number>(unique);
            unique = new ArrayList<Number>(hashSet);
            sum = (double)(unique.size() - min);
//            for (Double d : p1) {
//                if(!p2.contains(d)){
//                    duplicates++;
//                }
//            } 
//            for (Double d : p2) {
//                if(!p1.contains(d)){
//                    duplicates++;
//                }
//            } 
//            sum = (double)duplicates;
        }else if(distanceType == DIST_HAMMING_ABSOLUTE){
            int maxV = Math.max(p1.size(), p2.size());
            int minV = Math.min(p1.size(), p2.size());
            int dist = 0;
            int maxDistAllowed;
            boolean chkMaxDistOnly;
            
            chkMaxDistOnly = false;
            maxDistAllowed = maxV;
            
            if(additionalInfo.length >= 1){
                maxDistAllowed = (Integer)additionalInfo[0];            
                if(additionalInfo.length == 2){
                    chkMaxDistOnly = (Boolean)additionalInfo[1];
                }
            }

            for (int i = 0; i < minV; i++) {
                if(Math.abs(roundN(p2.get(i).doubleValue()-p1.get(i).doubleValue(),5))>0){
                    dist++;
                    if(dist>=maxDistAllowed){
                        break;
                    }
                } 
                if(chkMaxDistOnly){
                    if((i+1-dist)>(minV-maxDistAllowed)){
                        dist = maxDistAllowed-1; //This is NOT correct dist but for fast calculation
                        break;
                    }
                }
            }
            dist += maxV-minV; //if unequal size
            sum = dist*1.0;        
        }else if (distanceType == DIST_COMMON_MATCH || distanceType == DIST_HAMMING_PATTERN){
            ArrayList collectCommonID = null;
            if(additionalInfo.length == 1){
                collectCommonID = (ArrayList)additionalInfo[0];            
            }
            int common = 0;
            int p1ptr = 0;
            int p2ptr = 0;
            if(p1.isEmpty()){
                sum = p2.size()*1.0;
            }else if(p2.isEmpty()){
                sum = p1.size()*1.0;
            }else{                                    
                Collections.sort(p1);
                Collections.sort(p2); 

                while(p1ptr<p1.size() && p2ptr<p2.size()){
                    while(p1ptr<p1.size() && p1.get(p1ptr).intValue() < p2.get(p2ptr).intValue()){
                        p1ptr++;
                    }

                    if(p1ptr>=p1.size()){
                        break;
                    }
                    if(p1.get(p1ptr).intValue() == p2.get(p2ptr).intValue()){
                        common++;
                        if(collectCommonID!=null)
                            ((ArrayList)additionalInfo[0]).add(p1.get(p1ptr).intValue());
                        p1ptr++;
                        p2ptr++;
                    }
                    if(p1ptr>=p1.size() || p2ptr >= p2.size()){
                        break;
                    }
                    while(p2ptr<p2.size() && p2.get(p2ptr).intValue()<p1.get(p1ptr).intValue()){
                        p2ptr++;
                    }

                    if(p2ptr>= p2.size()){
                        break;
                    }

                    if(p1.get(p1ptr).intValue() == p2.get(p2ptr).intValue()){
                        common++;
                        if(collectCommonID!=null)
                            ((ArrayList)additionalInfo[0]).add(p1.get(p1ptr).intValue());
                        p1ptr++;
                        p2ptr++;
                    }
                }
                if(distanceType == DIST_COMMON_MATCH)
                    sum = 1.0*common;
                else if(distanceType == DIST_HAMMING_PATTERN)
                    sum = 1.0*p1.size()+p2.size() - 2*common;
                else
                    sum = -1.0;
            }
        }else{
            sum = Double.NaN;
        }

        return sum;
    }

    public static ArrayList<ArrayList> xxxtrackCommonPath(ArrayList<ArrayList<ArrayList>> history, ArrayList<ArrayList> sat, int depth){
        int match;
        
        ArrayList<ArrayList> commonSat = new ArrayList<ArrayList>();        
        
        depth = Math.min(depth, history.size());
        if(depth == 0){
            return null;
        }
        //depth - 1
        match = MyMath.norm2D(MyMath.DIST_COMMON_MATCH, sat, 
                history.get(0), commonSat, -1);
        
        //depth > 1
        if(match>0 && depth>1){
            ArrayList<ArrayList> satTemp = new ArrayList<ArrayList>();
            for (int i = 0; i < commonSat.size(); i++) {
                satTemp.add((ArrayList)commonSat.get(i).clone());
            }
            for (int d = 2; d <= depth; d++) {                
                commonSat.clear(); //otherwise may get duplicate values               
                match = MyMath.norm2D(MyMath.DIST_COMMON_MATCH, satTemp, //WRONG> only ID>>>
                    history.get(d-1), commonSat, -1);                
                
                satTemp.clear();
                for (int i = 0; i < commonSat.size(); i++) {
                    satTemp.add((ArrayList)commonSat.get(i).clone());
                }
                
                if(match<=0)
                    break;
            }
        }
        return commonSat;
    } 
    
    /**
     * This is hamming distance based on pattern matching (mismatched values) as explained in
     * {@link MyMath.#DIST_HAMMING_PATTERN}<BR>
     * <B>NOTE: </B>pFrom and pTo will be sorted after calling this method.
     * @param pFrom first input arraylist. Order matters if using {@code collectCommonID}.
     * @param pTo second input arraylist. Order matters if using {@code collectCommonID}.
     * @param collectCommonID  the common values of {@code pFrom}
     * @param upLimit: for efficiency purpose algorithm stops at upLimit. If 
     * upLimit is -1 then upper limit is not considered. 
     * @return 
     */
    public static int norm2D(final int distanceType, final ArrayList<ArrayList> pFrom, final ArrayList<ArrayList> pTo, final ArrayList<ArrayList> collectCommonID, final int upLimit){              
        if(pFrom == null){
            return pTo.size();//max
        }
        
        if(pFrom.isEmpty()){
            return pTo.size();
        }
      
        if(pFrom.size() != pTo.size()){
            throw new UnsupportedOperationException("sizes should be same for both arrays");
        }
        
        if(collectCommonID!=null){
            for (int i = 0; i < pFrom.size(); i++) {
                collectCommonID.add(new ArrayList());
            }
        }
        
        for (ArrayList al : pFrom) {
            Collections.sort(al);
        }
        for (ArrayList al : pTo) {
            Collections.sort(al);
        }
        
        ArrayList<Integer> rndIdx = MyRandom.randperm(0, pFrom.size());
        
        int hammingDist = 0;
               
        for(int i: rndIdx){//        for (int i = 0; i < pFrom.size(); i++) { 
            if(collectCommonID!=null)
                hammingDist += norm(pFrom.get(i), pTo.get(i), distanceType, collectCommonID.get(i));
            else
                hammingDist += norm(pFrom.get(i), pTo.get(i), distanceType);
            
            if(hammingDist >= upLimit && upLimit >=0){ //maximum is reached
                return upLimit;
            }
        }
               
        return hammingDist;
    }
  
    public static <T extends Number & Comparable<T>> double median(final ArrayList<T> v, boolean bSortFirst){
        if(bSortFirst)
            Collections.sort(v);
        
        if(v.size() == 0){
            return Double.NaN;
        }
        if(v.size() == 1){
            return v.get(0).doubleValue();
        }
        
        double median;
        if (v.size() % 2 == 0) //even
            median = (v.get(v.size()/2).doubleValue() + v.get(v.size()/2 - 1).doubleValue())/2;
        else
            median = v.get(v.size()/2).doubleValue();
        return median;
    }
    
    /**
    * mean - calculates mean of the given ArrayList<Double>
    * @param v - ArrayList<Double> of whom mean is to be found
    * @return return mean/average
    */
    public static double mean(ArrayList v){
        if(v.isEmpty()){
            return Double.NaN;
        }        

        double sum = 0.0;
        for (int i = 0; i < v.size(); i++) {
            sum += Double.parseDouble(v.get(i).toString());
        }
        return sum/v.size();
    }

    /**
     * Double sum does the summation of an array from index minIdx to maxIdx
     * @param obj Array to be summed up
     * @param minIdx - minimum index of the array to be included in the summation
     * @param maxIdx - maximum index of the array to be included in the summation
     * @return summation from minIdx to maxIdx
     */
    public static Double sum(Double[] obj, int minIdx, int maxIdx){
        Double sum = 0.0;
        if (maxIdx > obj.length-1)
            throw new ArrayIndexOutOfBoundsException("maxIdx is greater than array length");
        if (minIdx < 0)
            throw new ArrayIndexOutOfBoundsException("minIdx is less than 0");

        for (int i = minIdx; i <= maxIdx; i++) {
            sum += obj[i];
        }

        return sum;
    }

    /**
     * Same as overloaded method sum(Double[] obj,...) at {@link csp.MyMath}
     * @see csp.MyMath
     * @param obj
     * @param minIdx
     * @param maxIdx
     * @return
     */
     public static Integer sum(Integer[] obj, int minIdx, int maxIdx){
        Integer sum = 0;
        if (maxIdx > obj.length-1)
            throw new ArrayIndexOutOfBoundsException("maxIdx is greater than array length");
        if (minIdx < 0)
            throw new ArrayIndexOutOfBoundsException("minIdx is less than 0");

        for (int i = minIdx; i <= maxIdx; i++) {
            sum += obj[i];
        }

        return sum;
    }   
     
    /**
      * vectorAddition add 2 vectors (one dimesional matrix) of same size
      * @param m1 first vector
      * @param m2 second vector
      * @return summation of 2 vectors
      */ 
    public static ArrayList<Double> vectorAddition(ArrayList<Double> m1, ArrayList<Double> m2){
        ArrayList<Double> result;

        if(m1.size() != m2.size()){
            throw new UnsupportedOperationException("Dimensions of both vectors must be same.");      
        }
        result = new ArrayList<Double>(m1.size());

        for (int i = 0; i < m1.size(); i++) {
            result.add(m1.get(i)+m2.get(i));            
        }
        return result;
    } 
    
        /**
     * get missing values in arraylist of integers only that has values from 0-maxVal
     * @param arrs
     * @param maxVal maximum value of arraylist. Minimum is always 0.
     * @return 
     */
    public static ArrayList getMissingVals(ArrayList arrs,final int maxVal){
                //Check common values in both candidate parents.
        int [] commonVals = new int[maxVal+1]; //all initialized to 0
        int constVal = 1;
        
        double iVal;
        for (int j = 0; j < arrs.size(); j++) { // checking for duplicate values
            Object v = arrs.get(j); //order O(l1) + O(l2) 
            if(!(v instanceof Integer || v instanceof Double)){
                throw new UnsupportedOperationException("Error! Integer/Double arraylist expected.");
            }
            if(v instanceof Integer)
                iVal = (Integer)v;
            else
                iVal = (Double)v;
            commonVals[(int)iVal] = constVal;
        }
 
        ArrayList missing = new ArrayList();
        for (int i = 0; i < commonVals.length; i++) {
                      
            if(commonVals[i]>0){
                ;
            }else{         
                missing.add(i);
            }
        }
        
        return missing;
    }
    
    
/**
     * 
     * @param arrs
     * @param maxVal - max value of the arrays.
     * @param bcommons - select common or uncommon
     * @param matchCount - number of arrays to check for - all common or all uncommon
     * for example Arraylist size is 3 (3D) and you want commons only in "any two"
     * of the arrays.
     * @return 
     */
    public static ArrayList<Integer> getElementsPattern(ArrayList<ArrayList> arrs, 
            final int maxVal, boolean bcommons, int matchCount){
                //Check common values in both candidate parents.
        int [] commonVals = new int[maxVal+1]; //all initialized to 0
        int constVal = 1;
        if(matchCount<2){
            matchCount = 2;
        }
        
        if(arrs.size()<2){
             throw new UnsupportedOperationException("At least TWO Array should be provided.");
        }
        
        int iVal;
        for (int j = 0; j < arrs.size(); j++) { // checking for duplicate values
            for (Object v : arrs.get(j)) { //order O(l1) + O(l2) 
                if(!(v instanceof Integer)){
                    throw new UnsupportedOperationException("Error! Integer arraylist expected.");
                }
                iVal = (Integer)v;
                commonVals[iVal] += constVal;
            }
            constVal = constVal*10;
        }

        ArrayList<Integer> onlyCommon = new ArrayList<Integer>();
        ArrayList<Integer> onlyUnCommon = new ArrayList<Integer>();
        String num;
        int count;
        for (int i = 0; i < commonVals.length; i++) {
            num = Integer.toString(commonVals[i]);
            count = num.length() - num.replace("1", "").length();
            if(count == 0){ //element not available
                continue; 
            }
            
            if(count==1){
                onlyUnCommon.add(i);
            }else if(count >= matchCount){         
//            if(commonVals[i]> Math.pow(10,arrs.size()-1) && commonVals[i]<Math.pow(10,arrs.size())){
                onlyCommon.add(i);
            }
        }
        
//        for (int i = 0; i < commonVals.length; i++) {
//            System.out.println(i + ", " + commonVals[i]);
//        }
        
        if(bcommons)
            return onlyCommon;
        else
            return onlyUnCommon;
    }
    
    /**
     * Subtracts two vectors element by element (m1 - m2)
     * @param m1 - first vector
     * @param m2 - second vector
     * @return result of (m1-m2)
     */
    public static ArrayList<Double> vectorSubtraction(ArrayList<Double> m1, ArrayList<Double> m2){
        ArrayList<Double> result;

        if(m1.size() != m2.size()){
            throw new UnsupportedOperationException("Dimensions of both vectors must be same.");      
        }
        result = new ArrayList<Double>(m1.size());

        for (int i = 0; i < m1.size(); i++) {
            result.add(m1.get(i)-m2.get(i));            
        }
        return result;
    } 
    
    /**
     * Find <b>Index</b> of min value of any type of given array
     * @param in ArrayList must be instance of Comparable
     * @return return index of first found minimum element
     */
    public static int minIdx(ArrayList<? extends Comparable> in){        
        Comparable minVal;
        int minIdx = 0;
        
        if(in.isEmpty()){
            minVal = -1;
        }        
        
        minVal = in.get(minIdx);
        
        for (int i = 1; i < in.size(); i++) {
            if(minVal.compareTo(in.get(i))>0){
                minVal = in.get(i);
                minIdx = i;
            }            
        }  
        
        return minIdx;
    }
    
    /**
     * Linear Form: y = mx+c where c = 0, hence y = mx
     * @param list
     * @param reqPop
     * @param debugPrint
     * @return 
     */
    public static ArrayList<Integer> linearFnSelection(final int listSize, final int reqPop, final boolean debugPrint){
        ArrayList<Integer> tmpIdx = new ArrayList<Integer>();
        
        if(reqPop <= 0){
            ;// tmpIdx is empty
        }else if(reqPop == 1){
            tmpIdx.add(0); // only first index
        }else{ //>1        
            final double m = (listSize-1)/(reqPop-1);
        
            for (int i = 0; i < reqPop; i++) {
                tmpIdx.add((int)Math.floor(m*i));
            }
        }
        
        if(debugPrint){
            String msg = "Category Gen(" + CspProcess.curGen + "): ";
            msg += tmpIdx.toString();
            CspProcess.printGuiDebug(msg);
        }

        return tmpIdx;
    }
    /**
     * this function is used to get the probability based on tanh graph. tanh is
     * slightly modified to have <code>[0 x 1] xlimit</code> and <code>[0 x maxVal] ylimit</code>.
     * @param in input value like current generation
     * @param maxVal total xValue like total generations
     * @return probability based on tanh
     */
    public static double expProbablity(int in, int maxVal){
        final double LIMIT = 2;
        double x = in;
        x = x*LIMIT*2/maxVal-LIMIT;
        double y = Math.tanh(x);
        y = (y+1)/LIMIT;
        return y;
    }
    
    
    /**
     * Exponential form: <I>&alpha;e<sup>&rho;x</sup></I>
     * <BR><B>USAGE:</B> used to pick the most 'promising' chromes in exponential manner.
     * Not necessarily the 'best' ones are picked. This method is used to get the diverse
     * population. It is used in {@link CspProcess.#categorizeChromesList(java.util.ArrayList, int, double, double, int, double, java.util.ArrayList) }.
     * @param list input list whose only promising chromes are to be selected
     * @param reqPop required size of the list.
     * @param rho mathematical usage in the exponential function in <I>&alpha;e<sup>&rho;x</sup></I>
     * @return exponentially selected chromes from the sorted list provided
     * @see #expFnSelection(java.util.ArrayList, int, double) 
     */
    public static ArrayList<Integer> negExpFnSelection(final int listSize, final int reqPop, final double rho, final boolean debugPrint){
        double x,y; // x-axis values for the function Ae^rho.x
        
        ArrayList<Integer> tmpIdx = new ArrayList<Integer>(); //y-axis values for the function Ae^rho.x  
        ArrayList chTmp = new ArrayList();
        final int lastIdxInitPop = listSize-1;
        double minVal = Double.MAX_VALUE;
        for (int i = reqPop-1; i>=0; i--) {
            x = i*1.0/(reqPop-1);
            y = Math.exp(-rho*x);
            if(i == reqPop-1){
                minVal = y;
            }
            tmpIdx.add((int)Math.floor((y-minVal)*lastIdxInitPop*1.0/(1-minVal)));
        }
        
        //re allocate tmpIdx because exponential eq. might have introduced duplicates. remove duplicates
        int curMaxIdx;
        if(!tmpIdx.isEmpty()){
            curMaxIdx = tmpIdx.get(0); //first one
            for (int j = 1; j < tmpIdx.size(); j++) {
                if(tmpIdx.get(j)<= curMaxIdx){
                    tmpIdx.set(j, curMaxIdx+1);
                } 
                curMaxIdx = tmpIdx.get(j);
            }
        }
                
//        for (int j = 0; j < tmpIdx.size(); j++) {
//            chTmp.add(list.get(tmpIdx.get(j)));                    
//        }  
//        return chTmp;
        
        if(debugPrint){
            String msg = "Category Gen(" + CspProcess.curGen + "): ";
            msg += tmpIdx.toString();
            CspProcess.printGuiDebug(msg);
        }
        
        return tmpIdx;
    }
    
    /**
     * Exponential form: <I>&alpha;e<sup>&rho;x</sup></I> <B>BUT</B> the value of &lt;&rho;&gt; is meaningless 
     * as this is canceled when calculating <code>pickedIdx</code>. <code>pickedIdx = </code>
     * <I>&alpha;e<sup>&rho;const/&rho;</sup> = &alpha;e<sup>const</sup></I>. <I>const</I> is a 
     * constant value calculated for calculation for all the <code>pickedIdx</code>.
     * <BR><B>USAGE:</B> used to pick the most 'promising' chromes in exponential manner.
     * Not necessarily the 'best' ones are picked. This method is used to get the diverse
     * population. It is used in {@link CspProcess.#categorizeChromesList(java.util.ArrayList, int, double, double, int, double, java.util.ArrayList) }.
     * @param list input list whose only promising chromes are to be selected
     * @param reqPop required size of the list.
     * @param alpha mathematical usage in the exponential function in <I>&alpha;e<sup>&rho;x</sup></I>
     * @return exponentially selected chromes from the sorted list provided
     * @see #negExpFnSelection(java.util.ArrayList, int, double) 
     */
    public static ArrayList<Integer> expFnSelection(final int listSize, final int reqPop, final double alpha){
        double maxExp = Math.log((listSize-1)/alpha);//corresponds to the value of grouping.get(i).size()
        maxExp = maxExp/(reqPop-1); //value of each division

        ArrayList<Integer> tmpIdx = new ArrayList<Integer>();
        ArrayList chTmp = new ArrayList();
        int pickedIdx, curMaxIdx;

        for (int j = 0; j < reqPop; j++) {
            pickedIdx = (int)Math.floor(alpha*Math.exp(j*maxExp));
            tmpIdx.add(pickedIdx);
        }
        
        //re allocate tmpIdx because exponential eq. might have introduced duplicates. remove duplicates
        if(!tmpIdx.isEmpty()){
            curMaxIdx = tmpIdx.get(0); //first one
            for (int j = 1; j < tmpIdx.size(); j++) {
                if(tmpIdx.get(j)<= curMaxIdx){
                    tmpIdx.set(j, curMaxIdx+1);
                } 
                curMaxIdx = tmpIdx.get(j);
            }
        }
                
//        for (int j = 0; j < tmpIdx.size(); j++) {
//            chTmp.add(list.get(tmpIdx.get(j)));                    
//        }          
//        return chTmp;
        return tmpIdx;
    } 
    
    
}
