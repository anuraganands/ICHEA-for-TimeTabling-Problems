/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package csp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

/**
 * This class is used only when data is read from external file
 * @param dataFile input text file
 * @param populaion chromosomes population- can be user input or determined
 * automatically from input data file if designed for specific problem .
 * @param generation must be determined by the user. Cannot determine automatically
 * @param t - type of data - currently Integer for nominal data and Double
 * for any other type of data like int, double etc.
 * @throws InstantiationException
 * @throws IllegalAccessException
 * @throws MyException
 * @author Anurag Sharma
 */
public abstract class ExternalData implements Cloneable, Serializable {
    //public ArrayList<ArrayList<Integer>> courses; //10 courses 0-9
    //protected Scanner fileName_;
    //protected ArrayList<Chromosome> chromosome_;
    protected String fileName_;
    protected final UserInput userInput_;
    protected int initializeCounter;
    protected int CUR_PREF;
    protected int PREV_PREF;
    protected int nextPrefLimit;
    public int immunitySize;
    protected int pStart;
    boolean bConsiderLength;
    protected static int curHstConstNo;
    protected final int maxDynamicConstraints = 3;
    protected static ArrayList<Integer> dynConsList = new ArrayList<Integer>(); //These stores additional tabu constraints IDs. i.e  maxIDs + 1,2,3..etc
    protected static ArrayList<Integer> constLimits = new ArrayList<Integer>();
    public double localBestComparision = 0.2;
    private int maxHamDist = 10;
    
    /**
     * This one has direct implications in calculating fitness value based
     * on preference. <BR>
     * Basically, it ignores all the preference constraints from [0 - pStart].<BR>
     * <b>Note</b> that pStart is initialized with 0, unless data is read from a file.
     */
    public void pStartIncrement(){
        this.pStart++;
        if(this.pStart>maxPref()){
            pStart = maxPref();
        }
    }
    
    public int getMaxHamDist(){
        return maxHamDist;
    }

    public void setMaxHamDistByProportion(double per){
//        final int lastIdx = Math.min(maxDynamicConstraints,CspProcess.dynamicConstraints.size()-1);
//        int sz = 0;
//        for (int i = 0; i < CspProcess.dynamicConstraints.get(lastIdx).size(); i++) {
//            sz+=CspProcess.dynamicConstraints.get(lastIdx).get(i).size();
//        }
//        maxHamDist = (int)(sz*per);
        maxHamDist = (int)(userInput_.totalConstraints*per);
    }
    
    public void pStartDecrement(){
        this.pStart--;
        if(this.pStart>maxPref()){
            pStart = maxPref();
        }
        if(this.pStart<0){
            pStart = 0;
        }
    }
    
    public int getpStart() {
        return pStart;
    }
    
    protected double getIndividualPrefFit(final int D, final int viosList[]){
        double thisFit = 0.0;
        final int L = userInput_.totalConstraints;
        if(viosList.length != D+1){
            throw new RuntimeException("incorrect size of input viosList array");
        }
        
        for (int i = 0; i <= D; i++) {
            thisFit+=Math.pow(2*L + 2, i)*viosList[i];
        }
        return thisFit;
    }
    
    public abstract boolean mutationCluster(final ArrayList<ArrayList> sat, final ArrayList<Double> vals, ArrayList<Double> fitness_, 
    final Idx2D[] valVsConstIdx_, final ByRef tabuViolations) throws SolutionFoundException;
    
    public abstract void kempe(final ArrayList<ArrayList> sat, final ArrayList<Double> vals, 
    final Idx2D[] valVsConstIdx, final ArrayList<Double> fitness_, final ArrayList<Integer> noGood, final ByRef tabuViolations, 
    final double Apercent, final double Bpercent, final boolean inCorner, final boolean useAppend,
    final int n, Element valTo, final boolean isDynamic) throws SolutionFoundException;
    
//    public abstract void decrementCSPsizeBy(int inc);
    public abstract void alterCSPsizeBy(int inc);
    public abstract int getCSPsize();
    /**
     * get problem specific functional value instead of fitness value which is
     * a generalized value, not necessarily the actual functional value.
     * converts fitness to problem specific functional value
     * @return 
     */
    public abstract double getFunctionalVal(double fitness);            

    private ExternalData(){
        userInput_ = null;
        //chromosome_ = null;
        initializeCounter = Integer.MIN_VALUE;
        CUR_PREF = -1;
        PREV_PREF = -1;
    }

    /**
     * This class is used only when data is read from external file
     * @param dataFile input text file
     * @param populaion chromosomes population- can be user input or determined
     * automatically from input data file if designed for specific problem .
     * @param generation must be determined by the user. Cannot determine automatically
     * @param t - type of data - currently Integer for nominal data and Double
     * for any other type of data like int, double etc.
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws MyException
     */
    public ExternalData(String absFileName, String fileName, int populaion, int generation, 
            int curPref, int prevPref, boolean saveChromes, int solutionBy, Class t, int hardConstViosTolerance) 
            throws InstantiationException, IllegalAccessException, MyException {
        //this.courses = new ArrayList<ArrayList<Integer>>();
        this.fileName_ = absFileName;
        userInput_ = new UserInput(t, saveChromes);
        userInput_.population = populaion;
        userInput_.generation = generation;
        userInput_.hardConstViosTolerance = hardConstViosTolerance;
        userInput_.solutionBy = solutionBy;
        userInput_.chosenFile = fileName;
        //this.chromosome_ = new ArrayList<Chromosome>();
        initializeCounter = 0;
        this.CUR_PREF = curPref;
        this.PREV_PREF = prevPref;
        this.nextPrefLimit = Integer.MAX_VALUE;   
        this.pStart = 0;//????
        bConsiderLength = false;
        
        if(constLimits.isEmpty()){
            constLimits.add(0);//Initial limit is 0 
        }

    }
    
//    public abstract ArrayList<ArrayList<Double>> refreshIndividualAffinity(final ArrayList<ArrayList> sat);
    
    public abstract boolean getForcedCSPsol(ArrayList<ArrayList> chromeConstraints, boolean bShowProgress);
   //    public abstract void tryForcedCSPsolUpdate(final ArrayList<Double> vals, final ArrayList<Double> fitness, 
//    final ArrayList<ArrayList> chromeConstraints, final ArrayList<Double> noGood,
//    final Idx2D[] valVsConstIdx_, boolean bShowProgress);
       
    public int getNextPrefLimit(){
        if(this.nextPrefLimit >= Integer.MAX_VALUE){
            return CUR_PREF;
        }else
            return this.nextPrefLimit;
    }
    
    public abstract void printProblemSpecificSol(ArrayList<ArrayList> sat);

    public int getCurPref() {
        return CUR_PREF;
    }
    
    public int getPrevPref(){
        return PREV_PREF;
    }

    public void setCurPref(int CUR_PREF) {
        this.CUR_PREF = CUR_PREF;
    }

    public void setPrevPref(int PREV_PREF) {
        this.PREV_PREF = PREV_PREF;
    }    
    
    protected abstract int maxPref();
    
    public abstract void refreshConstLimits();
    public abstract ArrayList<Double> getNoGoodsPartialCSP(ArrayList<Double> vals);
    /**
     * if sat is modified then valVsConstIdx MUST also be modified as it carries
     * the reference to the values in sat.
     * @param valVsConstIdx_
     * @param sat 
     */
    public abstract void refreshValVsConstIdx(final Idx2D[] valVsConstIdx_, final ArrayList<ArrayList> sat);
    public abstract boolean hasSameHammingDist(final ArrayList<ArrayList> tabuSat, final ArrayList<ArrayList> sat, 
            final Idx2D[] valVsConstIdx_, ByRef ... hamDist);
    
    /**
     * read data from file and store in private attribute(s)
     * fill userInput member by gathering information from data file.
     * @throws MyException
     */
    protected abstract void readData()throws MyException;     

    /**
     * User input is auto determined using the file data.
     * @return auto generated user input returned.
     */
    public final UserInput getUserInput() {
        return userInput_;
    }
    
//    public abstract ArrayList<Double> negateVal(ArrayList<Double> val);

    /**
     * User must have a knowledge of transforming file data into genetic
     * algorithm chromosomes. The definition must be changed for each
     * problem. It is problem specific
     * @param population number of elements to be created
     * @return ArrayList of newly generated chromosomes are returned.
     */
    protected abstract ArrayList<Chromosome> initializeExternalChrmosomes(final int population, boolean bRestart) throws SolutionFoundException; //, final boolean bInitialStage);

    /**
     * Checks if two given data are violated?
     * @param obj1 first object to be compared with second one
     * @param obj2 second object to be compared with first one
     * @param additionalInfo If there is any additional information to provided,
     * otherwise keep null.
     * @return true if violated and false otherwise
     */
    protected abstract boolean isViolated(final Object obj1, final Object obj2, final Object... additionalInfo);

    protected abstract boolean isHighlyConstrained(final Object obj);
    
    /**
     * It is problem dependant. If degree of violation is needed in fitness
     * function calculation. It may work as penalty function.
     * @param obj1 checks degree of violation with obj2
     * @param obj2 checks degree of violation with obj1
     * @return 
     */
    //protected abstract double degreeOfViolation(Object obj1, Object obj2);

    /**
     * Applicable for nominal data only. Gets constraint number from the stored domain value.
     * domain value is userInput.val_
     * @param val domain value i.e. userInput.val_
     * @return convert domain value representing constraint to any valid value;
     */
    protected abstract int getConstraintID(Double val);    
    
    /**
     * This function rearranges constraints and recalculates fitness function.
     * It assumes that the vals have been reshuffled so each value is appended to 
     * the arraylist again so in this manner constraints are rearranged and fitness
     * values are recalculated. <BR>
     * The main drawback of this function is the recaluclation is based on the 
     * current preference specified by the user. For example if the current pref
     * is 4 so the buildup of constraints is based on this pref. No special 
     * preference will be given for lower value of prefs. Hence this function is
     * hardly use. This system uses it only in  {@link CspProcess.#notVals(csp.Chromosome) } 
     * @param vals
     * @param fitness_
     * @param constraints
     * @param noGood
     * @param valVsConstIdx 
     */
    protected abstract void objectiveFnReset(final ArrayList<Double> vals, final ArrayList<Double> fitness_, 
            final ArrayList<ArrayList> constraints, final ArrayList<Integer> noGood, 
            final Idx2D[] valVsConstIdx, ByRef tabuVios) throws SolutionFoundException;
    
    /**
     * The function {@link ExternalData.#ObjectiveFnAppend(ArrayList, ArrayList, 
     * ArrayList, ArrayList, Idx2D[])  } produces dirty fitness function. However,
     * it is able to filter out promising solution by putting more weight on 
     * higher preferenced values. It enables the reusability of partial solutions.
     * <br>
     * This function produces the correct fitness function with correct allocations
     * of values in contraint group with corresponding pref values.<br>
     * <b>Note</b>The output fitness value and the vals value are based on constraints value
     * provided
     * 
     * @param constraints
     * @param fitness_ 
     */
    protected abstract void objectiveFnRefresh(final ArrayList<ArrayList> constraints, 
            final ArrayList<Double> fitness_,ArrayList<Double> vals, Idx2D[] valVsConstIdx_, 
            ByRef tabuVios, boolean forcePartialSolFit) throws SolutionFoundException;
    
    public abstract void objectiveFnSwapNrefresh(final ArrayList<ArrayList> sat, final ArrayList<Double> vals, ArrayList<Double> fitness_, 
        final Idx2D[] valVsConstIdx_, final ByRef tabuViolations, int[] p1, int[] p2);
    protected abstract void objectiveFnAppendLocalSearch(final ArrayList<Double> vals, final ArrayList<Double> fitness, 
    final ArrayList<ArrayList> chromeConstraints, final ArrayList<Double> noGood,
    final Idx2D[] valVsConstIdx_, final ByRef tabuViolations) throws SolutionFoundException;

    
    /**
     * Appends <i>only</i> feasible vals<br>
     * The append function has been developed for efficient calculation of 
     * fitness and buildup of constraints grouping.<br>
     * It also helps in filtering candidate solutions with more higher preferences
     * elements by correctly reading previous partial solutions.<br><br>
     * The major drawback of this method is it keeps on adding new value without
     * realizing that previous preferences are being affected. For example if it
     * has 10, 0 pref values and a new value with 1 pref is added its fitness 
     * function assumes it has 10 Pref0 and 1 Pref1. Infact the new added Pref1
     * can effect some existing Pref0 values. For example the final result can be 
     * 8 Pref0 and 3 Pref1 because the new one can affect another 2 pref0 values.
     * @param vals val array's only last element is appended.
     * @param fitness_ fitness value also <b>"updated"</b> with the appended
     * value. Actually it can cause <b>dirty</b> buildup of fitness function.
     * @param constraints constraints structure to be maintained. Either accept
     * the appended value or discard it.
     * @param noGood If the new value cannot be appended anyhow then this value
     * goes to the noGood array.
     * @param valVsConstIdx this array links the elements of val array with 
     * the constraints array. Indexes are maintained correctly.
     * @return return True if value is successfully appended.
     */
    protected abstract boolean objectiveFnAppend(final ArrayList<Double> vals, final ArrayList<Double> fitness_, 
            final ArrayList<ArrayList> constraints, final ArrayList<Integer> noGood,
            final Idx2D[] valVsConstIdx, ByRef tabuVios) throws SolutionFoundException;     
    protected abstract void objectiveFnRemove(final ArrayList<Double> vals, final ArrayList<Double> fitness_, 
            final boolean forcePartialFitSol, final ArrayList<ArrayList> constraints, final Idx2D[] valVsConstIdx, final int idx, 
            ByRef tabuVios) throws Exception;
//    protected abstract void objectiveFnRemove(final ArrayList<Double> vals, final ArrayList<Double> fitness_, 
//            final ArrayList<ArrayList> constraints, final Idx2D[] valVsConstIdx, final Object val, 
//            ByRef tabuVios) throws Exception;
//    protected abstract void ObjectiveFnSwapGroupUpdate(final ArrayList<Double> vals, final ArrayList<Double> fitness_, 
//            final ArrayList<ArrayList> constraints, final ArrayList<Double> noGood,
//            final Idx2D[] valVsConstIdx, int []grpSwap);
    
    
    
     
    /**
     * This method works as a queue for tabu constraints, As the added tabu constraint
     * exceeds the limit the oldest ones are deleted to make room for new ones.<BR>
     * <B>post process:</B> must call {@link ExternalData.#ObjectiveFnRefresh(java.util.ArrayList, java.util.ArrayList, java.util.ArrayList, csp.Idx2D[], java.util.ArrayList, csp.ByRef)} 
     * afterwards to refres fitness function with addition of "new" i.e. tabu constraints.
     * @param localOptConstraints 
     */
     public void addTabuConstraint(ArrayList<ArrayList> localOptConstraints) {                                
//    public void addTabuConstraint(ArrayList<Double> vals) {                                
        while(CspProcess.dynamicConstraints.size()>=maxDynamicConstraints){
            CspProcess.dynamicConstraints.remove(0);
            ExternalData.dynConsList.remove(0);
            //userInput_.total__updatedConstraints--; //in nqueen
        }
        
        int ID = userInput_.totalConstraints;
        
        if(!ExternalData.dynConsList.isEmpty()){
            ID = ExternalData.dynConsList.get(ExternalData.dynConsList.size()-1)+1;//Just increment to previous val            
        }
        
        //sort it first
         for (int i = 0; i < localOptConstraints.size(); i++) {
             Collections.sort(localOptConstraints.get(i));
         }
        
        CspProcess.dynamicConstraints.add(localOptConstraints);
        ExternalData.dynConsList.add(ID); // i think because it starts from 0
        //userInput_.total__updatedConstraints++; //in nqueen
    }
    
    public abstract void cleanCSP(ArrayList<ArrayList> sat, int threshold);
    public abstract int popConstLimit(int amt);
    
    /**
     * 
     * @param D worst preference
     * @param l current satisfied constraints
     * @param p current preference
     * @param fitness - fitness[0] = fitness value, fitness[1..D] = lp value i.e.
     * aggregateP number of courses for all preferences from 1..D
     * @return 
     */
    protected double fitnessValPrefBased(int D, final int curP, final int lemda, ArrayList<Double> fitness){      
        double sum; 
        int lp;
        int L = userInput_.totalConstraints;
        int unoccupiedL = 0;
        
        if(fitness.isEmpty()){
            fitness.add(Double.NaN);
            for (int i = 0; i < D+1; i++) {
                fitness.add(0.0);//add sum of all types of preferences.                    
            }
            fitness.set(curP+1, 1.0); //one just added          
        }else{            
            fitness.set(curP+1, fitness.get(curP+1)+1); //add new satisfied constraint           
        }  
        
        double aggregateP = 0.0;
        for (int i = 0; i <= pStart; i++) {
            aggregateP += fitness.get(i+1);
        }
        
        sum = 0;
        for (int p=pStart; p <= D; p++) {
            if(p == pStart)
                lp = (int)aggregateP;
            else
                lp = fitness.get(p+1).intValue();
            sum += Math.pow(2.0*L, D-p)*(L-lp);
        }
                        
        if(bConsiderLength){
            for (int i = 0; i < D; i++) { //last one is 0; should be..
                unoccupiedL += fitness.get(i+1);
            } 
            unoccupiedL = L - unoccupiedL;                        
            sum += Math.pow(2*L,D+1)*unoccupiedL+L;
        }
        
        fitness.set(0, sum);
        
        return sum;
    }
    
    public abstract ArrayList<Integer> getCluster(ArrayList<Integer> column);
    
    public abstract void RAinfluenceKempe(final ArrayList<Double> valsInfluencee, final ArrayList<Double> fitnessInfluencee,
        final ArrayList<ArrayList> satInfluencee, final Idx2D[] valVsConstIdxInfluencee, final ArrayList<Integer> noGoodInfluencee,
        final ByRef tabuVios, final ArrayList<ArrayList> satInfluencer, final int degree_of_influence) throws Exception;

    
    public abstract void RAinfluenceFull(final ArrayList<Double> valsInfluencee, final ArrayList<Double> fitnessInfluencee,
        final ArrayList<ArrayList> satInfluencee, final Idx2D[] valVsConstIdxInfluencee, ByRef tabuVios,
        final ArrayList<ArrayList> satInfluencer, final int degree_of_influence) throws Exception;
    /**
     * This is more "expensive" than {@link ExternalData.#RAinfluenceFull(java.util.ArrayList, java.util.ArrayList, int)  }
     * @param influencee
     * @param influencer
     * @param degree_of_influence 
     */
    public abstract void RAinfluencePartial(final ArrayList<ArrayList> influencee, final ArrayList<ArrayList> influencer, final int degree_of_influence);
    public abstract void RAinitialize(final ArrayList<ArrayList> sat, final ArrayList<Double> fitness, 
            ArrayList<Double> vals, Idx2D[] valVsConstIdx_, ByRef tabuVios); 
//    public abstract void RAupdateFitness(final ArrayList<ArrayList> sat, final ArrayList<Double> fitness, 
//            ArrayList<Double> vals, Idx2D[] valVsConstIdx_, ByRef tabuVios);
    protected abstract double updateFitnessValWeightBased(final int penalty, ArrayList<Double> fitnessz);
    
////    /**
////     * Only applicable to Nominal data. Use minVal and maxVal properties for
////     * the Continuous data.
////     * @param dimension - provide dimension for which the domain values are sought
////     * @return set of domain values of a given dimension.
////     */
////    protected abstract Set<Double> domainValues(int dimension);

    /**
     * Clone defined for ExternalData is ONLY SHALLOW CLONE.
     * @return Object.clone();
     * @throws CloneNotSupportedException
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
                
     