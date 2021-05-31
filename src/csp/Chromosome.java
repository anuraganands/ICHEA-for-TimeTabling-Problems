/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package csp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.jdesktop.application.Application;



/**
 *
 * @author Anurag
 */
public class Chromosome implements Comparable, Cloneable, Serializable{
    public static final int BY_FITNESS = 1;
    public static final int BY_VIOLATIONS = 2;
    public static final int BY_SATISFACTIONS = 3;
    /**
     * <B>precondition:</B> must call {@link CspProcess.#getRoValue(java.util.ArrayList, csp.Chromosome) }
     * to get the updated &lt;&rho;&gt; value which must be assigned to {@link Chromosome.#tempRo}.
     */
    public static final int BY_RHO = 4;
    public static final int BY_GOODNESS_AGE = 5;
    public static final int BY_DISCOURAGE = 6;
    public static final int BY_HARD_CONSTRAINT_VIOS = 7;
//    public static final int BY_NEIGHBOUR_DIST = 8;
        
//    public static ArrayList<Integer> discourageVios;
    public ArrayList<Double> vals_;
//    private ArrayList<Integer> position_;
//    private ArrayList<ArrayList<Double>> affinity_;
    public Idx2D[] valVsConstIdx_;//constraint functions tmporaritly public ....
    private ArrayList<Double> fitness_; //change back to private
    private ArrayList<Integer> violations_;
    public ArrayList<ArrayList> satisfactions_;
    private UserInput userInput_;
    private ExternalData externalData_;
    private int noProgressCounter;
    private int goodnessAge = 0;
    private int RAimmunity;
    private int RAimmunityOnGen;
    private final int RA_MAX_IMMUNITY = 3;
    private ArrayList<Integer> noGoods; ///temporaly made it public for testing purpose.
    private boolean isRAagent;
//    public boolean isValid;
    private ByRef tabuVios; // used as int data type. seems NOT in use as code for this in timetabling is commented out.
//    private int totalHardVios;
    
//    public int fitnessRank;
    
    public Double tempRo;
    private boolean RhoReadyFlag = false;
    public static int tmpSortBy;
    public static int totalAppEvals=0;
    public static int totalRefEvals = 0;
    public boolean rmvIndicator;
    public boolean RAisImproved = false;
    public boolean RAisAtheist = false;
    private Chromosome RAmyGuru = null;
    
    private ArrayList<Chromosome> history;
    
    /**
     * a chromosome may stay away from its closeby neighbors to have better diversity.
     */
    class localTabu{
        private Chromosome tabuChrome;
        private double radius=-1;
        private double deltaDist = -1;
        /**
         * a chromosome may stay away from its closeby neighbors to have better diversity.
         * @param tabuChromosome - said tabu chromosome from which the current 
         * chromosome will stay away.
         * @param radius - the current chromosome will stay away upto this distance.
         */
        public localTabu(Chromosome tabuChromosome, double radius, boolean bByFitness){
            this.tabuChrome = tabuChromosome;
            this.radius = radius;
            if(bByFitness){
                deltaDist = Math.abs((getFitnessVal(0)-tabuChromosome.getFitnessVal(0))/Math.max(Math.abs(getFitnessVal(0)), Math.abs(tabuChromosome.getFitnessVal(0))));
            }else{
                deltaDist = (int)MyMath.norm2D(MyMath.DIST_HAMMING_PATTERN, satisfactions_, tabuChromosome.satisfactions_, null, (int)radius);
            }
            refreshValVsConstIdx();
            tabuChromosome.refreshValVsConstIdx();//remove this and add into CSPprocess to make it fast.
            //if chrom is farther than given radius
            if(deltaDist>=radius){ //tested but unqualified for being tabu. Far away..
                this.tabuChrome = null;
            }
        }
        private localTabu(){;}
        public double getDeltaDist(){
            return deltaDist;
        }
    };
    private localTabu lTabu = null;
    
    public class PrevBest implements Serializable, Cloneable{
        private Double prevBestFit;        
        private Integer gen; //record when prev best was updated
        private Double allTimeBest;
        private Integer bestGen;
        
        public PrevBest(){
            prevBestFit = null;
            gen = null;
            allTimeBest = null;
            bestGen = null;
        }
        public int getLastBestGeneration(){
            if (prevBest.gen == null){
                return -1;
            }else{
                return prevBest.gen;
            }            
        }
        public double getLastBestFitness(){
            if (prevBest.prevBestFit == null){
                return Double.NaN;
            }else{
                return prevBest.prevBestFit;
            }            
        }
        
        public double getAllTimeBestFitness(){
            if (prevBest.allTimeBest == null){
                return Double.NaN;
            }else{
                return prevBest.allTimeBest;
            }            
        }
        

        @Override
        protected Object clone() throws CloneNotSupportedException {
            PrevBest pbest;
            try {
                pbest = (PrevBest) super.clone();
                return pbest;

            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                return null;
            } catch (NullPointerException npe){
                npe.printStackTrace();
                return null;
            }
        }
        
        void printVal(int no){
            System.out.println(no + ": " + vals_);
        }
        
        /**
         * Only call this function when the better fitness value has found. 
         * Otherwise previous best values will be corrupted.  
         */
        void update(){            
            if(!isPartialSolution()){
                return;
            }
            prevBestFit = getFitnessVal(0);
            gen = CspProcess.curGen;  
            
            if(allTimeBest == null || getFitnessVal(0)<allTimeBest){
                allTimeBest = getFitnessVal(0);
            }
        }       
    }
    
    
    public PrevBest prevBest;    
    
    /**
     * whoAmI is used to link {@link  csp.CspProcess#chromosomes_} chromosomes
     * with parent chromosomes. <BR>
     * parents have idx id of chromosomes_ and <BR>
     * offspring have (populationSize + idx id of parent chromosomes_)
     */
    public int whoAmI; 

    public void ahamBrahmasi(){
        RAmyGuru.satisfactions_.clear();
        for (int i = 0; i < this.satisfactions_.size(); i++) {
            RAmyGuru.satisfactions_.add((ArrayList)satisfactions_.get(i).clone()); 
        }        
        RAmyGuru.vals_ = (ArrayList<Double>)this.vals_.clone();
        
        try {
            RAmyGuru.refreshFitness();
        } catch (SolutionFoundException ex) {
            Logger.getLogger(Chromosome.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public Chromosome getRAmyGuru() {
        return RAmyGuru;
    }

    public void setRAmyGuru(Chromosome RAmyGuru) {
        this.RAmyGuru = RAmyGuru;
    }
    
    public void clearRAmyGuru() {
        this.RAmyGuru = null;
    }
    
    

    public boolean hasLocalTabu(){
        return lTabu != null;
    }
    
    public localTabu getLocalTabu() {
        if(lTabu == null)
            return null;
        else
            return lTabu;
    }

    public boolean setTabuChromeIf(Chromosome tabuChrome, final double range, final boolean bByFitness) {
        this.lTabu = new localTabu(tabuChrome, range, bByFitness);
        if(this.lTabu.tabuChrome == null){
            this.lTabu = null;
            return false;
        }
        else
            return true;
    }
    
    
    public int getGoodnessAge() {
        return goodnessAge;
    }

    public int getNoProgressCounter() {
        return noProgressCounter;
    }

////    public int getHammingDistFrom(Chromosome from, final int upLimit){
////        return (int)MyMath.getHammingDist(satisfactions_, from.satisfactions_, upLimit);
////    }
    
    public static void forceSetSortType(int type){
        if(type < 1 || type > 7){
            throw new UnsupportedOperationException("sort type unrecognized.");
        }
        Chromosome.tmpSortBy = type;
    }
    
    public static void sort(ArrayList<Chromosome> pop, int type, int orgType, int ... knearest){
        if(type < 1 || type > 7){
            throw new UnsupportedOperationException("sort type unrecognized.");
        }
        if(orgType < 1 || orgType > 7){
            throw new UnsupportedOperationException("sort type unrecognized.");
        }
        Chromosome.tmpSortBy = type;
        
        if(type == BY_RHO){
            if(knearest.length != 1){
                throw new RuntimeException("incorrect original type provided.");
            }
            for (Chromosome c : pop) {
                try {
                    c.tempRo = c.getIntegerRoValue(pop, knearest[0]);
                } catch (MyException ex) {
                    ex.printStackTrace();
                }
            }            
        }
        
        Collections.sort(pop);
        Chromosome.tmpSortBy = orgType;                               
    }

    public static double maxFitness(ArrayList<Chromosome> pop, int orgType){
        if(orgType < 1 || orgType > 7){
            throw new UnsupportedOperationException("sort type unrecognized.");
        }
        if(pop == null || pop.isEmpty()){
            return Double.NaN;
        }
        Chromosome.tmpSortBy = Chromosome.BY_FITNESS;                                
        double maxVal = Collections.max(pop).getFitnessVal(0);
        Chromosome.tmpSortBy = orgType;
        
        return maxVal;
    }
    
    public static double minFitness(ArrayList<Chromosome> pop, int orgType){
        if(orgType < 1 || orgType > 7){
            throw new UnsupportedOperationException("sort type unrecognized.");
        }
        if(pop == null || pop.isEmpty()){
            return Double.NaN;
        }
        Chromosome.tmpSortBy = Chromosome.BY_FITNESS;                                
        double minVal = Collections.min(pop).getFitnessVal(0);
        Chromosome.tmpSortBy = orgType;
        
        return minVal;
    }
    
    public void cleanProgressCounter(){
        noProgressCounter = 0;
    }
    
    public boolean isStagnant(final int noProgressLimit) {
        boolean isStagnant;
        if(noProgressCounter>=noProgressLimit)
            isStagnant = true;
        else
            isStagnant = false;
        
        return isStagnant;
    }

    //private int rankingType;

    private Chromosome(){
        vals_ = new ArrayList<Double>();
//        position_ = new ArrayList<Integer>();
        fitness_ = new ArrayList<Double>();
        
        fitness_.add(Double.MAX_VALUE);
        fitness_.add(0.0);
        
        violations_ = new ArrayList<Integer>();
        satisfactions_ = new ArrayList<ArrayList>();
//        affinity_ = new ArrayList<ArrayList<Double>>();
        tempRo = -1.0; //Invalid negative value
        tmpSortBy = BY_VIOLATIONS; //default sort option
        noProgressCounter = 0;
        RAimmunity = RA_MAX_IMMUNITY; //immunity will decerease
        RAimmunityOnGen = 0; // never used
        //rankingType = BY_VIOLATIONS;
        noGoods = new ArrayList<Integer>();
        rmvIndicator = false;
        isRAagent = false;
//        isValid = true;
//        totalHardVios = 0;
        tabuVios = new ByRef(new Integer(1)); //must assign to default value 1 since it is always multiplied by fitness value.
        history = new ArrayList<Chromosome>();
        prevBest = new PrevBest();
    }

    private Chromosome(UserInput userInput){
        this();
        userInput_ = userInput;
        externalData_ = null;
        if(userInput_ == null){
            System.err.println("No user input provided.");
            Application.getInstance().exit();
        }

        valVsConstIdx_ = new Idx2D[userInput_.totalConstraints];
        for (int i = 0; i < valVsConstIdx_.length; i++) {
            valVsConstIdx_[i] = new Idx2D(); //col = -1; position = -1;
        }
    }
    public Chromosome(int sortValue, UserInput userInput){
        this(userInput);
        tmpSortBy = sortValue; //default sort option
    }

    private Chromosome(ExternalData externalData){
        this();
        externalData_ = externalData;
        userInput_ = externalData_.getUserInput();

        if(userInput_ == null || this.externalData_ == null){
            System.err.println("No user input provided or empty external data.");
            Application.getInstance().exit();
        }
        
        valVsConstIdx_ = new Idx2D[userInput_.totalConstraints];
        for (int i = 0; i < valVsConstIdx_.length; i++) {
            valVsConstIdx_[i] = new Idx2D(); //col = -1; position = -1;
        }
    }
    
    public Chromosome(int sortValue, ExternalData externalData){
        this(externalData);
        tmpSortBy = sortValue; //default sort option
    }

    public ExternalData getExternalData() {
        return externalData_;
    }

    public void setExternalData(ExternalData externalData) {
        this.externalData_ = externalData;
    }
    
    public boolean isValid(){
        boolean valid = false;
        if(this.getTotalHardVios() <= userInput_.hardConstViosTolerance){
            valid = true;
        }
        return valid;
    }
    
    /**
     * precondition: add ONLY if there is some improvement to current chrome.
     * @param c 
     */
    public void addToHistory(Chromosome c){
        if(history.size()<CspProcess.chromHistoryLevel){
            history.add(c);
        }
    }
    
    public int curHistorySize(){
        return history.size();
    }
    
    /**
     * NOTE: may be dangerous or error prone if system changes dynamically. Like
     * change in space, change in constraint etc.
     */
    public void cleanHistory(){
        for (int i = 0; i < history.size(); i++) {
            if(!history.get(i).isPartialSolution()){
                history.remove(i);
                i--;
            }
        }
//        history.clear();      
    }
    
    public Chromosome replaceWithBacktrack(final double  nPercent, Chromosome nowFeasibleCOP){
        int histIdx = getNthGapIndex(nPercent); //to get the farthest all the time.
        Chromosome retCh = null;
        
        if(histIdx>=0){
            retCh =  this.history.remove(histIdx);
            if(nowFeasibleCOP.isPartialSolution()){
                history.add(nowFeasibleCOP);
            }
        }

        return retCh;
    }
    
    public Chromosome removedWithBacktrack(final double  nPercent, Chromosome nowFeasibleCOP){
        int histIdx = getNthGapIndex(nPercent); //I modified it to get the farthest all the time.
        Chromosome retCh = null;
        
        if(histIdx>=0){
            history.get(histIdx).history = history;
            retCh =  history.remove(histIdx);
            if(history.size()>0.7*CspProcess.chromHistoryLevel && nowFeasibleCOP.isPartialSolution()){
                history.add(nowFeasibleCOP);
            }
        }
        
        
        
        return retCh;
    }
    
    
    /**
     * track path taken through backtracking history
     * @param depth the depth of backtrack tree.
     * @return the indices of track values
     */
    public ArrayList<ArrayList> trackCommonPath(int depth){
        int match;       
        ArrayList<ArrayList> commonSat = new ArrayList<ArrayList>();
        
        Chromosome.tmpSortBy = Chromosome.BY_FITNESS;
        Collections.sort(history);
                      
        depth = Math.min(depth, history.size());
        if(depth == 0){ //don't check with history or has NO history
            for (ArrayList s : satisfactions_) {
                commonSat.add((ArrayList)s.clone());
            }  
            return commonSat;
        }
        //depth - 1
        match = MyMath.norm2D(MyMath.DIST_COMMON_MATCH, this.satisfactions_, 
                history.get(0).satisfactions_, commonSat, -1);
        refreshValVsConstIdx();
        refreshValVsConstIdx(history.get(0).valVsConstIdx_, history.get(0).satisfactions_);
        
        //depth > 1
        if(match>0 && depth>1){
            ArrayList<ArrayList> satTemp = new ArrayList<ArrayList>();

            for (ArrayList comList : commonSat) {
                satTemp.add((ArrayList)comList.clone());
            }
            
            for (int d = 2; d <= depth; d++) {                
                commonSat.clear(); //otherwise may get duplicate values               
                match = MyMath.norm2D(MyMath.DIST_COMMON_MATCH, satTemp, //WRONG> only ID>>>
                    history.get(d-1).satisfactions_, commonSat, -1);
                refreshValVsConstIdx(history.get(d-1).valVsConstIdx_, history.get(d-1).satisfactions_);                
                
                satTemp.clear();
                for (ArrayList comList : commonSat) {
                    satTemp.add((ArrayList)comList.clone());
                }
                
                if(match<=0)
                    break;
            }
        }
        return commonSat;
    } 
    
    public void printHistory(){
        System.out.println(MyMath.roundN(this.getFunctionalVal(),4));
        System.out.print("[");
        for (int i = 0; i < history.size(); i++) {
            System.out.print(MyMath.roundN(history.get(i).getFunctionalVal(),4)+", ");
        }
        System.out.println("]"); 
    }
    
    
/**
     * ref: <a href="http://mathforum.org/library/drmath/view/52794.html">http://mathforum.org</a> .
     * This function will only work for sorted array.
     * @param bHistorySorted - indicate whether history is sorted or not. If not
     * then it will be sorted in this function.
     */
    public void removeOutlierHistory(boolean bHistorySorted){
        ArrayList<Double> fit = new ArrayList<Double>();
        int totalRemoved = history.size();
        
        if(!bHistorySorted){
            Chromosome.tmpSortBy = Chromosome.BY_FITNESS;
            Collections.sort(history);//good one first, bad ones later.
        }
        
        for (Chromosome c : history) {
            fit.add(c.getFunctionalVal());
        } 
        
        ArrayList<Integer> rIdx = MyMath.getOutliers(fit, true, true, false, 0.0);
        //index in reverser order
        for (Integer i : rIdx) {
            history.remove(i);
        }               
        
        totalRemoved = totalRemoved - history.size();
        if(totalRemoved>0)
            System.out.println(totalRemoved + " outliers removed");
    }
    
    /**
     * since the history is built without looking at the quality of individuals
     * or particularly their novelty. In this method the history is first sorted,
     * outliers removed then those elements not so novel (20%) will be removed without
     * asking anyone if maximum history is stored.
     * @param nPercent - suggestion: should be more than 0.5
     * @return 
     */
    private int getNthGapIndex(final double nPercent){        
        Chromosome.tmpSortBy = Chromosome.BY_FITNESS;
        Collections.sort(history);//good one first, bad ones later.
        
        if(this.isPartialSolution()){
            cleanHistory();
        }
        
        
        //remove this.... outlier is good.
        removeOutlierHistory(false); //already sorted above. hence false;
        
        ArrayList<Element> gap = new ArrayList<Element>();
        int nthIdx;
        
        if(history.size()==0){
            return -1;
        }
        if(history.size() == 1){
            return 0;
        }
          
        for (int i = 1; i < history.size(); i++) {
            gap.add(new Element(history.get(i).getFitnessVal(0)-history.get(i-1).getFitnessVal(0),i-1));
        }
        //Element.sortOrder = Element.ASCENDING;
        Collections.sort(gap);//Bad ones first, good ones later
        
        //do some cleaning remove 20% bad ones - bad ones are those who are very
        //close to each other "relatively"
        //NOTE: you can use Vector Quantization
        double rmPercent = 1.0; 
        int rmTotal=0;
        if(history.size() >= CspProcess.chromHistoryLevel){ //if history size is maximum
            rmPercent = 0.2;

//            if(history.size() > CspProcess.chromHistoryLevel*rmPercent){
            rmTotal = history.size() - (int)(history.size()*rmPercent);
//            }
            Integer [] rmIdx = new Integer[rmTotal];
            for (int i = 0; i < rmTotal; i++) {
                rmIdx[i] = gap.remove(0).idx;
            }
            Arrays.sort(rmIdx, Collections.reverseOrder());//bigger index first

            for (int r : rmIdx) {
                history.remove(r);
            } 
        }
  
        
        //duplication and expensive.
        gap.clear();
        for (int i = 1; i < history.size(); i++) {
            gap.add(new Element(history.get(i).getFitnessVal(0)-history.get(i-1).getFitnessVal(0),i-1));
        }
        //Element.sortOrder = Element.ASCENDING;
        Collections.sort(gap);//ascending
        
        nthIdx = gap.get((int)(gap.size()*nPercent)).idx;
        if(nthIdx >= history.size()){
            System.out.println("not possible....");
        }
        
        return nthIdx;
    }
    
     /**
     * <b>Note:</b> this function is totally different from immunity used in
     * Chromosome. It refers to the immunity size of ExternalData file.
     * @return immunitySize or satisfactionSize of the best individual of the
     * last partial solution.
     */
//    public boolean hasPartialSolImmunity(Chromosome bestChrom){
//        
//        return (vals_.size() > bestChrom.getValsCopy().size());
//    }
    
//    public int getParitalSolImmunity(){
//        return vals_.size();
//    }
    
//    public int getAffinitySize(){
//        return affinity_.size();
//    }
    
//    public ArrayList<ArrayList<Element>> getSortedAffinity(){
//        Element e;
//        Element.order = Element.DESCENDING;
//        
//        ArrayList<ArrayList<Element>> elms = new ArrayList<ArrayList<Element>>();
//        for (int d = 0; d < affinity_.size(); d++) {
//            elms.add(new ArrayList<Element>());
//            for (int j = 0; j < affinity_.get(d).size(); j++) {
//                e = new Element(affinity_.get(d).get(j),j);
//                elms.get(d).add(e);
//            }                        
//            Collections.sort(elms.get(d));
//        }
//        
//        return elms;
//    }
    
//    public double getAffinityVal(int grpIdx, int idx){
//        return affinity_.get(grpIdx).get(idx);
//    }
    
//    public void refreshAffinity(){
//        affinity_ = externalData_.refreshIndividualAffinity(satisfactions_);
//    }
    
//    public ArrayList<ArrayList<Double>> zzzz(){
//        return affinity_;
//    }
    
    public boolean isRAagent(){
        return isRAagent;
    }
    public void RAinit(){
        if(externalData_ != null){
            isRAagent = true;
            externalData_.RAinitialize(satisfactions_, fitness_,vals_, valVsConstIdx_, tabuVios);
        }
    }
    
    public void RAupdateFitness(boolean ... bUpdateBest) throws SolutionFoundException{
        refreshFitness(bUpdateBest);
//        if(externalData_ != null){
            
//            externalData_.RAupdateFitness(satisfactions_, fitness_, vals_, valVsConstIdx_, tabuVios);
////            this.totalHardVios = fitness_.get(1).intValue();
//        }
    }
    
    
    
    /**
     * get worst element.
     * Make sure to call refreshAffinity before calling this method
     * preprocess - refreshAffinity
     * @param horizontalIdx - which column?
     * @return negative return is no verticleIdx exists, Must check this condition before use.     
     */
//    public int getMaxAffintiyIdx(int horizontalIdx){
//        int verticleIdx = -1;
//        double maxVal = -1.0;
//        
//        for (int d = 0; d < affinity_.get(horizontalIdx).size(); d++) {
//            if(affinity_.get(horizontalIdx).get(d)>maxVal){
//                maxVal = affinity_.get(horizontalIdx).get(d);
//                verticleIdx = d;
//            }
//        }
//        return verticleIdx;
//    }
    
    public ArrayList<Double> getNegVals(){
        ArrayList<Double> negVals = new ArrayList<Double>();  
        double val;
        for (int i = 0; i < vals_.size(); i++) {
            val = userInput_.minVals.get(i)+userInput_.maxVals.get(i) - vals_.get(i);
            if(val<userInput_.minVals.get(i))
                val = userInput_.minVals.get(i);
            if(val>userInput_.maxVals.get(i))
                val = userInput_.maxVals.get(i);
            
            negVals.add(val);
        }
        return negVals;
    }

    public int getTotalHardVios() {
        return this.fitness_.get(1).intValue();
    }
    
    
    
    /**
     * The integer value denoting the rank of an individual in a population.
     * the lower the rank the better the value. It has range from [0 Total_Constraints-1]
     * Gives total violations or total satisfaction in terms of rank<br>
     * for violation <br>
     * rank = size of violation<br>
     * for satisfaction <br>
     * rank = total constraint - size of satisfaction <br>
     * Other types of rankings are NOT implemented
     * @return returns rank - values from good to worse. lower value indicate better chromosome.
     * @throws UnsupportedOperationException
     */
    public double getRank() throws UnsupportedOperationException{
        double rank;
        int sz = 0;
        
        if(tmpSortBy == BY_VIOLATIONS){ // || (tempSortBy == BY_FITNESS && externalData_ == null)){
            rank = violations_.size(); //the higher the violation the lower the rank (ascending order from 0...maxval)
        }else if(tmpSortBy == BY_SATISFACTIONS){
            for (ArrayList s : satisfactions_) {
                sz +=s.size();
            }            
            
            rank = userInput_.total__updatedConstraints - sz;
//            rank = CspProcess.getCurAcceptedConstraints(userInput_.total__updatedConstraints)-sz;
        }else if(tmpSortBy == BY_FITNESS){// & externalData_ != null){
            if(fitness_.isEmpty())
                rank = Double.MAX_VALUE;
            else
                rank =  getFitnessVal(0); // ????????????????? int val????
        }else if (tmpSortBy == BY_HARD_CONSTRAINT_VIOS){
            rank = getTotalHardVios();
        }
//        }else if (tmpSortBy == BY_HAMMING_DIST){
//            if(lTabu == null){
//                throw new UnsupportedOperationException("Error! tabu chromosome not defined!");
//            }
//            rank = lTabu.tmpDist;
//        }
        else if(tmpSortBy == BY_GOODNESS_AGE){
            rank = -goodnessAge;
        }else if (tmpSortBy == BY_RHO){
            rank = -this.tempRo;//higher the better hence low rank.
        }else{
            rank = -1;
            throw new UnsupportedOperationException("Incorrect Ranking Type Defined.");
        }
        
        
//        if(fitness_.isEmpty())
            return rank;
//        else
//            return getFitnessVal(0).intValue(); ////// WHY INT val?????????????????????????????????????????????//    
    }

    public void overrideBestSoFarFitness(double val){
        if(this == CspProcess.getBestSoFarCOP() || this == CspProcess.getBestSoFarCSP())
            fitness_.set(0, val);
    }
    
    public double getFunctionalVal(){
        return externalData_.getFunctionalVal(fitness_.get(0));
    }
    
    /**
     * Check if current chromosome is promising than the given chrome
     * @param c
     * @return 
     */
    public boolean isMorePromisingThan(final Chromosome c){
        
        if(this.getTotalHardVios()<=c.getTotalHardVios()){        
            if(this.isPartialSolution() && !c.isPartialSolution()){
                return true;
            }

            if(this.getFitnessVal(0)<c.getFitnessVal(0)){
                return true; //can replace with C. up to caller.
            }
        }
        
        return false;//no replacement. Return same
    }
    
    
    
    
    public boolean isMorePromisingThanBestCOP(){
        if(this.isPartialSolution() && this.getFitnessVal(0)<CspProcess.getBestSoFarCOP().getFitnessVal(0) && this.isValid()){
            return true; //can replace with C. up to caller.
        }
        
        return false;//no replacement. Return same
    }
    
    public boolean isMorePromisingThanBestCSP(){
        if(this.getFitnessVal(0)<CspProcess.getBestSoFarCSP().getFitnessVal(0) && this.isValid()){
            return true; //can replace with C. up to caller.
        }
        
        return false;//no replacement. Return same
    }
    
    /**
     * Actually numerically low fitness value means high fitness conceptually
     * @param i
     * @return 
     */
    public Double getFitnessVal(int i) {        
        double d;
        
        try{
            if(i == 0)
                d = ((Integer)tabuVios.getVal()+1)* fitness_.get(i); // what if fitness is <=0?????
            else
                d = fitness_.get(i);
        }catch(IndexOutOfBoundsException iobe){
            d = Double.POSITIVE_INFINITY;
        }
        if(d == Double.POSITIVE_INFINITY){
            d = Double.MAX_VALUE;
        }
        if(d == Double.NEGATIVE_INFINITY){
            d = -Double.MAX_VALUE;
        }
        return d; 
        
//        double d;
//        double penalty;   
//        double worstFit;
//        boolean bestIsPositive;
//        
//        int tabuvios_plus_one = (Integer)tabuVios.getVal()+1;
//        
//        try{
//            if(d == 0){
//                if(CspProcess.getCurWorstCOP() != null && tabuvios_plus_one>1){
//                    worstFit = CspProcess.getCurWorstCOP().fitness_.get(d);
//                }else{
//                    worstFit = 0; // see below
//                }
//                penalty = Math.abs(worstFit * tabuvios_plus_one); //positive;
//
//                    //PENALTY ADDITION
//                d = penalty + fitness_.get(d); // whats the theory behind this penalty function....
//            }   
//            else{
//                d = fitness_.get(d); // I think it is TOTAL HARD CONSTRAINT VIOLATIONS
//            }
//        }catch(IndexOutOfBoundsException iobe){
//            d = Double.POSITIVE_INFINITY;
//        }
//        return d; 
    }
        
    
//    private void refreshRank() throws UnsupportedOperationException{
//        GroupCS gcs;
//        
//        if(tempSortBy == BY_VIOLATIONS){
//            violations_.clear();
//            for (Double v : vals_) {
//                violations_.add(v.intValue());
//            }
//        }else if(tempSortBy == BY_SATISFACTIONS){
//            satisfactions_.clear();
//                                    
//            for (Double v : vals_) {
//                gcs = new GroupCS(1);
//                gcs.add(v.intValue());
//                satisfactions_.add(gcs);
//            }
//        }else{
//            throw new UnsupportedOperationException("Incorrect Ranking Type Defined.");
//        }
//    }

    /**
     * Depending on the type of rank used in calculation it return the numeric
     * values of the rank components
     * <BR>
     * In case of violation - it return all violated constraint set
     * <BR>
     * In case of satisfaction - it return all satisfied constraint set
     * @return 
     */
//    public ArrayList<Integer> getRankComponents(){
//        ArrayList<Integer> rankComponents;
//
//        if(userInput_.solutionBy == BY_VIOLATIONS){
//            rankComponents = violations_; //the higher the violation the lower the rank (ascending order from 0...maxval)
//        }else if(userInput_.solutionBy == BY_SATISFACTIONS){
//            rankComponents = satisfactions_;
//        }else{
//            rankComponents = null;
//            throw new UnsupportedOperationException("Incorrect Ranking Type Defined.");
//        }
//        return rankComponents;
//    }
    
    public ArrayList<ArrayList> getSatisfaction(){
        return satisfactions_;
        
//        ArrayList<ArrayList> sat = new ArrayList<ArrayList>();
//            
//        for (int d = 0; d < satisfactions_.size(); d++) {
//            sat.add((ArrayList)satisfactions_.get(d).clone()); 
//        }
//        
//        return sat;
    }
        /**
     * Depending on the type of rank used in calculation it return the numeric
     * values of the rank components
     * <BR>
     * In case of violation - it return all violated constraint set
     * <BR>
     * In case of satisfaction - it return all satisfied constraint set
     * @return 
     */
    public ArrayList<Integer> getRankComponents(){
        ArrayList<Integer> rankComponents;

        if(userInput_.solutionBy == BY_VIOLATIONS || (userInput_.solutionBy == BY_FITNESS && externalData_ == null)){
            rankComponents = violations_; //the higher the violation the lower the rank (ascending order from 0...maxval)
        }else if(userInput_.solutionBy == BY_SATISFACTIONS){
            throw new UnsupportedOperationException("Not supported for satisfaction. use getSatisfaction() instead.");
        }else{
            rankComponents = null;
            throw new UnsupportedOperationException("Incorrect Ranking Type Defined.");
        }
        return new ArrayList<Integer>(rankComponents);
    }
    
    public void tempprintValVsConstIdx(){
        String print = "";
        String sp = " ";
        print += "\n"+sp+sp+"valsVsConstraintIdx:";
        for (int i = 0; i < valVsConstIdx_.length; i++) {
            if(!valVsConstIdx_[i].isEmpty())
                print+= "(" + i + ")"+ valVsConstIdx_[i].toString()+"; ";            
        }
        System.out.println(print);
    }

    /**
     * if sat is modified then this function MUST be called as valVsCons carries
     * the reference to sat values.
     */
    public void refreshValVsConstIdx(final Idx2D[] valVsConstIdx, final ArrayList<ArrayList> sat) {
        if(externalData_ != null)
            externalData_.refreshValVsConstIdx(valVsConstIdx, sat);
        else
            throw new UnsupportedOperationException("Code not yet written. Should be same as externalData.refreshValVsConstIdx");
    }
    
    public void refreshValVsConstIdx() {
        if(externalData_ != null)
            externalData_.refreshValVsConstIdx(valVsConstIdx_, satisfactions_);
        else
            throw new UnsupportedOperationException("Code not yet written. Should be same as externalData.refreshValVsConstIdx");
    }
//    public boolean hasSameHammingDist(final ArrayList<ArrayList> tabuSat, boolean useMaxDist, ByRef ... hamDist){
//        if(externalData_ != null){
//            if(useMaxDist){
//                ByRef maxDist = new ByRef(externalData_.maxHamDist);
//                ByRef[] in = new ByRef[2];
//                in[0] = hamDist[0];
//                in[1] = maxDist;
//                return externalData_.hasSameHammingDist(tabuSat, satisfactions_, valVsConstIdx_, in);
//                
//            }
//            else
//                return externalData_.hasSameHammingDist(tabuSat, satisfactions_, valVsConstIdx_, hamDist);
//        }
//        else
//            throw new UnsupportedOperationException("Code not yet written. Should be same as externalData.hasSameHammingDistPrevBest");
//    }
    
//    public int getHammingDist(final ArrayList<ArrayList> tabuSat){
//        ByRef hamDist = new ByRef(0);
//        if(externalData_ != null){
//            externalData_.hasSameHammingDist(tabuSat, satisfactions_, valVsConstIdx_, hamDist);
//            return (Integer)hamDist.getVal();
//        }
//        else
//            throw new UnsupportedOperationException("Code not yet written. Should be same as externalData.hasSameHammingDistPrevBest");
//    }
    
    /**
     * Checks if the child is acceptable in inter-marriage crossover.
     * It is possible that child may not contain any trait of a parent. In this
     * case it is necessary to check for genuineness with this function.
     * If constraint violation is to be checked then <BR>
     * child must not violate any other constraint than its parent's already 
     * violated constraint. <BR>
     * If constraint satisfaction is to be checked then <BR>
     * child must satisfy same or more constraints.
     * @param child
     * @return 
     */
    public boolean isMyChild(Chromosome child){
        boolean result;
        
        if(tmpSortBy == BY_VIOLATIONS || tmpSortBy == BY_FITNESS){ //child must not violate any other constraint than its parent's already violated constraint.
            result = this.violations_.containsAll(child.violations_);
//            if(result){
//                for (int d = 0; d < position_.size(); d++) {
//                    if(position_.get(d) != child.position_.get(d)){
//                        result = false;
//                        break;
//                    }
//                }                    
//            }
        }else if(tmpSortBy == BY_SATISFACTIONS){ //child must satisfy same or more constraints.
            result = child.satisfactions_.containsAll(this.satisfactions_);
        }else{
            throw new UnsupportedOperationException("Incorrect Ranking Type Defined.");
        }   
        
        return result;
    }
    
    
    /**
     * The current (this) object is supposed to be a child
     * @param parent
     * @return 
     */
    public boolean myParent(Chromosome parent){
        boolean result;
        int vios[] = new int[userInput_.totalConstraints];
        ArrayList<Integer> parentSatisfactions = new ArrayList<Integer>();
        
        
        if(this.violations_.containsAll(parent.violations_) && 
                parent.violations_.containsAll(this.violations_)) ///same violations
            return true;
        
        for (int i = 0; i < parent.violations_.size(); i++) {
            vios[parent.violations_.get(i)] = 1;
        }
        
        for (int i = 0; i < vios.length; i++) {
            if(vios[i]==0){
                parentSatisfactions.add(i);
            }            
        }
        
        result = true;
        if(this.violations_.containsAll(parentSatisfactions)){
            result = false;
        }
        
        return result;
    } 
        
    
    
    
    /**
     * used in Nqueen dyanmic constraints...
     */
    public void NqueenCleanFitnessHistory(){
        //for nqueen
        if(fitness_.size()==2){
            fitness_.set(1, 0.0);
        }
    }
    
    public void swapNrefreshFitness(int []p1, int []p2, boolean ... bUpdateBest) throws SolutionFoundException{
        if(this == CspProcess.getBestSoFarCOP()){
            System.out.println("Are you sure you want to use this method for bestSoFarCOP. Read the warning in its javadoc.");
            throw new UnsupportedOperationException("refreshFitness should not be used for bestSoFarCOP");
        }
        swapNrefreshObjectiveFunction(p1, p2, bUpdateBest);        
    }
    
    private void swapNrefreshObjectiveFunction(int []p1, int []p2, boolean ... bUpdateBest) throws SolutionFoundException{
    //DO NOT DELETE THESE LINES
    //<<
        //totalEvals++;
        totalRefEvals += this.vals_.size();
        double prevRank;
        double curRank;
        double prevHardVios;
        double curHardVios;
        prevRank = this.getFitnessVal(0); //this.getRank();
        prevHardVios = this.getFitnessVal(1);
    //>>

    //TODO... Call your objective function here....
    //Start<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    try{
        if(externalData_ != null)
            externalData_.objectiveFnSwapNrefresh(satisfactions_, vals_, fitness_, valVsConstIdx_, tabuVios, p1, p2); 
        else
            CCSPfns.objFn(1, vals_, fitness_, violations_, userInput_, CspProcess.maxCSPval);
        
//        totalHardVios = fitness_.get(1).intValue();

        boolean bupdate = true;
        if(bUpdateBest.length > 0){
            if(!bUpdateBest[0]){
                bupdate = false;
            }
        }
        
        
        if(this.isPartialSolution()){ 
            if(CspProcess.getBestSoFarCOP() != null && bupdate)
                if(this.isMorePromisingThanBestCOP()){
                    CspProcess.setBestSoFarCOP((Chromosome)this.clone());
                }
        }else{
            if(CspProcess.getBestSoFarCSP() != null)
                if(this.isMorePromisingThanBestCSP()){
                    //CspProcess.bestSoFar.setVals((ArrayList<Double>)vals_.clone(), maxCSPval);
                    CspProcess.setBestSoFarCSP((Chromosome)this.clone());
                }
        }
        
    }catch (SolutionFoundException sfe){
        CspProcess.setBestSoFarCOP((Chromosome)this.clone());
        throw sfe;
    }
    //End>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    //DO NOT DELETE THESE LINES
    //<<
        curRank = this.getFitnessVal(0);//this.getRank();
        curHardVios = this.getFitnessVal(1);
        
        if(MyMath.roundN(curHardVios - prevHardVios, CspProcess.FIT_DP_LIMIT) < 0.0){
            noProgressCounter=0;
            goodnessAge++;
        }else if(MyMath.roundN(curHardVios - prevHardVios, CspProcess.FIT_DP_LIMIT) > 0.0){
            noProgressCounter++;
        }else{
            if(MyMath.roundN(curRank - prevRank, CspProcess.FIT_DP_LIMIT)>=0.0 ){ //worse or same => stagnant
                noProgressCounter++;
            }else{
                noProgressCounter=0;
                goodnessAge++;
            }
        }
        if(noProgressCounter == 0){ //improved...
            this.prevBest.update();
        }
        if(isStagnant(CspProcess.NO_PROGRESS_LIMIT)){
            goodnessAge = 0;
        }
    //>>    
    }
    
    public void reverseNoProgress(){
        noProgressCounter--;
        if(noProgressCounter<0){
            noProgressCounter = 0;
        }
    }
    
    public void reportNoProgress(){
        noProgressCounter++;
         if(isStagnant(CspProcess.NO_PROGRESS_LIMIT)){
            goodnessAge = 0;
        }
    }
    
    /**
     * This won't ensure generating feasible solution. It only refreshes the 
     * related component (like valVsConstIdx_) of a chromosome. If the input is infeasible it will remain
     * infeasible after calling this function.<BR>
     * <B>WARNING</B> Be careful in using this method for {@link CspProcess.#bestSoFarCOP} as it may become
 CSP if its transition period has expired d.e. {@link CspProcess.#getCurAcceptedConstraints(int) } will 
     * have new (incremented) value.
     * @param maxCSPval
     * @param bUpdateBest - updates BestSoFarCOP if true, otherwise ignores
     * @throws SolutionFoundException 
     */
    public void refreshFitness(boolean ... bUpdateBest ) throws SolutionFoundException{
        if(this == CspProcess.getBestSoFarCOP()){
            System.out.println("Are you sure you want to use this method for bestSoFarCOP. Read the warning in its javadoc.");
            throw new UnsupportedOperationException("refreshFitness should not be used for bestSoFarCOP");
        }
        refreshObjectiveFunction(bUpdateBest);        
    }
    
    /**
     * It remove duplicate values - It is problem dependant.
     */
//    private void repairVal(){
//        Set<Double> s = new LinkedHashSet<Double>(this.vals_);
//        this.vals_ = new ArrayList<Double>(s);      
//    }
    
    /**
     * This is tricky one if we deal with both numeric and ordinal data<BR>
     * - for numeric data vals are features/dimensions where the total dimension 
     * remain constant. as in my circle example. val size will remain 2.<BR>
     * - for ordinal data vals are just constraint representation. it can keep
     * on growing for any size (in some problems up to verticleIdx satisfaction)
     * @param vals - 
     * <BR> - for numeric data - val is dimension/feature value.
     * <BR> - for ordinal data - val is constraint representation.
     */
    public boolean appendVal(final double vals, boolean ... bUpdateBest) throws SolutionFoundException{
        this.vals_.add(vals);
        int lastIdx = this.vals_.size() - 1;
        boolean isAppended = false;
         
        if(this.userInput_.dataType.contains("Double")){
            if(this.vals_.size() == this.userInput_.totalDecisionVars){ //its always the case
                resetObjectiveFunction();
                isAppended = true; //refresh above.
            }
            
        }else{
            if(vals_.size()>=1){ //its always the case
//                for (int d = 0; d < lastIdx; d++) {
//                    if(violationChk(d,lastIdx)>0){//violated
//                        this.vals_.remove(lastIdx);
//                        break;
//                    }
//                }
//                refreshRank();
                
                isAppended = appendObjectiveFunction(bUpdateBest); 
            }
        }
        
        return isAppended;
    }

    /**
     * Updates the vals with the new vals_ from argument
     * @param vals_ to be assigned to chromosome
     */
    public void setVals(ArrayList<Double> vals) throws SolutionFoundException{
        this.vals_ = new ArrayList<Double>();
        this.satisfactions_ = new ArrayList<ArrayList>();
        this.violations_ = new ArrayList<Integer>();
        this.fitness_ = new ArrayList<Double>();
        this.valVsConstIdx_ = new Idx2D[userInput_.totalConstraints];
        
        for (int i = 0; i < valVsConstIdx_.length; i++) {
            valVsConstIdx_[i] = new Idx2D();
        }

        if (vals == null){
            //vals = new ArrayList<Double>();
            return;
        }
        if(vals.size()>=1){
            for (int i = 0; i < vals.size(); i++) {
                this.appendVal(vals.get(i), true);
            }
//            resetObjectiveFunction();
        }
    }

    public void replaceVal(int idx, Double val) throws SolutionFoundException, Exception{ 
        if(userInput_.dataType.contains("Integer")){
            remove(idx);            
            appendVal(val);
            
        }else{
            this.vals_.set(idx, val); //can be removed anywhere
            resetObjectiveFunction();
        }
    }
    
    public void remove(final int valIdx) throws Exception{
        //DO NOT DELETE THESE LINES
        //<<
            //totalEvals++;
            totalRefEvals += vals_.size();
            double prevRank;
            double curRank;
            double prevHardVios;
            double curHardVios;
            prevRank = this.getFitnessVal(0); //this.getRank();
            prevHardVios = this.getFitnessVal(1);
        //>>

        if(externalData_ != null){
            externalData_.objectiveFnRemove(vals_, fitness_, false, satisfactions_, valVsConstIdx_, valIdx, tabuVios); 
//            totalHardVios = fitness_.get(1).intValue();
        }
        else
            throw new UnsupportedOperationException("Not supported!\n");        
        
        //DO NOT DELETE THESE LINES
        //<<
            curRank = this.getFitnessVal(0);//this.getRank();
            curHardVios = this.getFitnessVal(1);
        
            if(MyMath.roundN(curHardVios - prevHardVios, CspProcess.FIT_DP_LIMIT) < 0.0){
                noProgressCounter=0;
                goodnessAge++;
            }else if(MyMath.roundN(curHardVios - prevHardVios, CspProcess.FIT_DP_LIMIT) > 0.0){
                noProgressCounter++;
            }else{
                if(MyMath.roundN(curRank - prevRank, CspProcess.FIT_DP_LIMIT)>=0.0 ){ //worse or same => stagnant
                    noProgressCounter++;
                }else{
                    noProgressCounter=0;
                    goodnessAge++;
                }
            }
            if(noProgressCounter == 0){ //improved...
                this.prevBest.update();
            }
                        
            if(isStagnant(CspProcess.NO_PROGRESS_LIMIT)){
                goodnessAge = 0;
            }
        //>>
    }
    
    /**
     * keep first half intact and
     * @param c 
     */
    public void twoPointCrossoverWith(Chromosome c, boolean forceCompleteSol) throws SolutionFoundException{
        int fullSz = this.satisfactions_.size();
        int halfSz = this.satisfactions_.size()/2;
//        ArrayList<Integer> failed = new ArrayList<Integer>();
        noGoods.clear();
                
        for (int i = halfSz; i < fullSz; i++) {
            this.satisfactions_.get(i).clear();            
        }
        int val;
        for (int i = halfSz; i < fullSz; i++) {
            for (int j = 0; j < c.satisfactions_.get(i).size(); j++) {
                val = ((Integer)c.satisfactions_.get(i).get(j)).intValue();
                try {
                    if(!appendVal(val,false)){
                        noGoods.add(val);
                    }
                } catch (SolutionFoundException ex) {
                    System.err.println(ex);
                }
            }           
        }
        for (int i = halfSz; i < fullSz; i++) { 
            if(this.isPartialSolution()){
                break;
            }
            for (int j = 0; j < this.satisfactions_.get(i).size(); j++) {
                val = ((Integer)this.satisfactions_.get(i).get(j)).intValue();
                try {
                    if(!appendVal(val,false)){
                        noGoods.add(val);
                    }
                } catch (SolutionFoundException ex) {
                    System.err.println(ex);
                }
            }           
        }
        
        Element takenVal;
        if(forceCompleteSol){
            satisfactions_.get(MyRandom.randperm(0, satisfactions_.size()).get(0)).addAll(noGoods);
            for (Integer i : noGoods) {
                vals_.add(i*1.0);
            }
            for (Integer i : noGoods) {
                val = i;
                takenVal = new Element(val, -1);
                //move the takenVal to "somewhere else".
                mutationKempe(0, 0, false, false, 1, takenVal, false, true);
            }                
        }
        try {
            refreshObjectiveFunction(false);
        } catch (SolutionFoundException ex) {
            Logger.getLogger(Chromosome.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }

    /**
     * Because of the nature of satisfaction structure (2 dimensional), simple
     * crossover is not applicable. Because we need a diverse solution and 
     * diverse solution in this case relies on diverse satisfaction array rather
     * than val array which is just constraint satisfaction holder without
     * any patter. We must keep creating diverse pattern.
     * @param percent 
     */
    public void restructure(double percent, boolean fromLeft, double maxCSPval) throws SolutionFoundException{
        ArrayList<Integer> ind = new ArrayList<Integer>();
        int temp = -1;
        for (ArrayList<Integer> grp : satisfactions_.subList(0, Math.min(satisfactions_.size(), userInput_.totalConstraints))) {
            temp++;
            if(grp.size()>0){
                ind.add(temp);
            }
        }
        
        this.noGoods.clear();//are you sure????
        if(fromLeft){
            for (int i = 0; i < Math.floor(ind.size()*percent); i++) {
                satisfactions_.set(ind.get(i), new ArrayList<Integer>());
            }
        }else{
            for (int i = ind.size()-1; i >= Math.ceil(ind.size()*(1-percent)); i--) {
                satisfactions_.set(ind.get(i), new ArrayList<Integer>());
            }
        }
        
//        for (int grp : MyRandom.randperm(0,satisfactions_.size()-1).subList(0, (int)Math.ceil(satisfactions_.size()*percent))) {
//            satisfactions_.set(grp, new ArrayList<Integer>());            
//        }
        
        vals_.clear();                                
        valVsConstIdx_ = new Idx2D[userInput_.totalConstraints];
        for (int i = 0; i < valVsConstIdx_.length; i++) {
            valVsConstIdx_[i] = new Idx2D(); //col = -1; position = -1;
        }

        Idx2D idx2D;
        int col = -1;
        int position;
        try{
        for (ArrayList<Integer> grp : satisfactions_.subList(0, Math.min(satisfactions_.size(), userInput_.totalConstraints))) {
            col++;
            position = -1;
            for (Integer i : grp) {
                position++;
                vals_.add(i*1.0);
                idx2D = new Idx2D();
                idx2D.col = col;
                idx2D.position = position;
                valVsConstIdx_[i] = idx2D; 
            }
        }
        }catch(Exception e){
            e.printStackTrace();
        }

        refreshFitness();
        
    }
    
    /**
     * returns the value specified by the index
     * @param index the index of the decision value
     * @return the value of specified index of the decision value array
     */
    public Double getVals(int index){
        return new Double(this.vals_.get(index));
    }

    /**
     * returns the value array;
     * @return the value array
     */
    public ArrayList<Double> getValsCopy() {                
        return new ArrayList<Double>(this.vals_); //to protect vals_
    }

    public ArrayList<Double> getVals() {                
        return this.vals_; //to protect vals_
    }

        /**
     * Function returns if it is feasible in terms of satisfying all the hard constraints
     * @return 
     */
    public int getHardConstraintViosCurrentlyNotUsed(){                
        int c1,c2;
//        ArrayList<Integer> vios = new ArrayList<Integer>();
        int vios=0;
        for (int i = 0; i < satisfactions_.size(); i++) {
            for (int j = 0; j < satisfactions_.get(i).size()-1; j++) {
                for (int k = j+1; k < satisfactions_.get(i).size(); k++) {
                    c1 = (Integer)satisfactions_.get(i).get(j);
                    c2 = (Integer)satisfactions_.get(i).get(k);
                    if(externalData_.isViolated(c1,c2)){
//                        vios.add(c2);                                    
                        vios++;
                    }
                }
            }            
        }
        
        return vios;
    }
    /**
     * This function is like a "repair" function. If Easily repaired then return true with repaired
     * solution otherwise return unrepaired + modified solution
     * Input can be feasible or infeasible + partial or full.
     * Function returns if it is feasible in terms of satisfying all the hard constraints
     * @return 
     */
    public boolean cleanHardConstraintVios(final boolean forceCompleteSol) throws SolutionFoundException{                
        int c1,c2;  
        boolean retVal = false;
        ArrayList<Integer> vios = new ArrayList<Integer>();
        ArrayList<Integer> failed = new ArrayList<Integer>();
        try{
        
        for (int i = 0; i < satisfactions_.size(); i++) {
            for (int j = 0; j < satisfactions_.get(i).size()-1; j++) {
                for (int k = j+1; k < satisfactions_.get(i).size(); k++) {
                    c1 = (Integer)satisfactions_.get(i).get(j);
                    c2 = (Integer)satisfactions_.get(i).get(k);
                    if(externalData_.isViolated(c1,c2)){
                        vios.add(c2);
                        satisfactions_.get(i).remove(k);
                        vals_.remove(new Double(c2*1.0));
                        k--;
                    }
                }
            }            
        }
       
//        refreshFitness(false); //don't update best because it is now "dirty" - it is partial not full
        
        
        
        for (int i = 0; i < vios.size(); i++) {
            if(!appendVal(vios.get(i),false)){
                failed.add(vios.get(i));
            }
        }        
        if(vals_.size() == userInput_.totalConstraints){
            retVal =  true;
        }else{
            if(forceCompleteSol){
                satisfactions_.get(MyRandom.randperm(0, satisfactions_.size()).get(0)).addAll(failed);
                for (Integer i : failed) {
                    vals_.add(i*1.0);
                }
            }
            retVal =  false;
        }
        
        refreshFitness(false);
        
        }catch(Exception e){
            e.printStackTrace();
        }
        return retVal;
    }
    
    public void completeSolution() throws SolutionFoundException{          
////        ArrayList<Double> v = (ArrayList<Double>)vals_.clone();
        Collections.sort(vals_);
        
        ArrayList<Double> missingVals = new ArrayList<Double>();         
        
        int idx=0; 
        boolean lastValAdded = false;
        if(vals_.get(vals_.size()-1)!=userInput_.totalDecisionVars-1){
            vals_.add(1.0*userInput_.totalDecisionVars-1);
            lastValAdded = true;
        }
        for (int i = 0; i < userInput_.totalDecisionVars; i++) {
            if(i != vals_.get(idx)){
                missingVals.add(i*1.0);
            }else{
                idx++;
            }            
        }
        if(lastValAdded){
            missingVals.add(1.0*userInput_.totalDecisionVars-1);
        }
        
        idx = 0;
        for (int i = 0; i < missingVals.size(); i++) {
            satisfactions_.get(idx++%satisfactions_.size()).add(missingVals.get(i).intValue());
        }
        refreshFitness(false);
    }
    
    /**
     * Checks to see if there is any violation
     * if rank is 0, it has no violation.
     * @return true or false for violation
     */
    public boolean isSolution(){
        boolean bsol = false;        
  
        if(!isValid()){
            return false;
        }
        
        Chromosome.tmpSortBy = Chromosome.BY_SATISFACTIONS;
        if(getRank() == 0 && vals_.size() == userInput_.total__updatedConstraints)
            bsol = true;
        
        Chromosome.tmpSortBy = userInput_.solutionBy;
        if(getRank() == 0 && vals_.size() == userInput_.total__updatedConstraints)
            bsol = true;

        if(getFitnessVal(0)==0 && externalData_ != null && vals_.size() == userInput_.total__updatedConstraints)
            bsol = true;
        
        if(violations_.size() == 0 && externalData_ == null){
            bsol = true;
            if(CspProcess.bInTransition || CspProcess.dynamicConstraintNo < userInput_.totalConstraints - userInput_.totalDecisionVars){
//                System.out.println("\n... current transition sol reached...\n...\n...\n...\n");
                bsol = false;
            }
        }                
                    
        return bsol;            
    }
    
    public boolean isPartialSolution(){
        boolean bsol = false; 
        
        if(!isValid()){
            return false;
        }
        
        int sz = 0;
//        for (ArrayList<Integer> grp: satisfactions_) {
//            sz += grp.size();
//        }
        sz = vals_.size();
        
        if(sz >=  CspProcess.getCurAcceptedConstraints(userInput_.total__updatedConstraints)){
            bsol = true;
        }        
        
        return bsol;
    }

    /**
     * Checks to see if the input chromosome has the same violation as the
     * current object.
     * @param from the chomosome with whom comparision is to be made.
     * @return true or false for same violation
     */
    public boolean hasSameRankComponent(Chromosome from){
        boolean bsame =false;        
        if(tmpSortBy == BY_VIOLATIONS || externalData_ == null){
            if(this.violations_.size() == this.userInput_.totalConstraints)
                bsame = false;
            else            
                bsame = this.violations_.containsAll(from.violations_) || from.violations_.containsAll(this.violations_); //the higher the violation the lower the rank (ascending order from 0...maxval)
        }else if(tmpSortBy == BY_SATISFACTIONS){
            if(this.satisfactions_.isEmpty() || from.satisfactions_.isEmpty())
                bsame = false;
            else
                bsame = this.satisfactions_.containsAll(from.satisfactions_) || from.satisfactions_.containsAll(this.satisfactions_);
//        }else if(tempSortBy == BY_FITNESS){
//            //fitness based on satisfaction or violation?
//            if(!this.satisfactions_.isEmpty()){ //using satisfaction
//                if(this.satisfactions_.isEmpty() || from.satisfactions_.isEmpty())
//                    bsame = false;
//                else
//                    bsame = this.satisfactions_.containsAll(from.satisfactions_) || from.satisfactions_.containsAll(this.satisfactions_);
//            }else if(!this.violations_.isEmpty()){//using violation
//                if(this.isSolution())//this.violations_.size() == this.userInput_.totalConstraints)
//                    bsame = true;
//                else            
//                    bsame = this.violations_.containsAll(from.violations_) || from.violations_.containsAll(this.violations_); //the higher the violation the lower the rank (ascending order from 0...maxval)
//            }else if (this.isSolution() || from.isSolution()){
//                bsame = true;
//                //throw new UnsupportedOperationException("Not able to determine what ranking type is used by fitness.");
//                //assumption is violation is empty hence isSolution
//            }
        }else{
            throw new UnsupportedOperationException("Incorrect Ranking Type Defined.");
        }
        return bsame;
    }

    
    public boolean forceFindSolution(double maxCSPval) throws SolutionFoundException{
        
        if(externalData_ != null){
            if(externalData_.getForcedCSPsol(satisfactions_, false)){
                refreshFitness();
                //refreshValVsConstIdx();
                return true;
            }else
                return false;
        
        }else
            return false;
    }  
 
    public void RAkempeInfluenceWith(final Chromosome c, int degreeOfInfluence){        
        if(externalData_ != null){
            try {
                externalData_.RAinfluenceKempe(vals_, fitness_, this.satisfactions_, 
                        valVsConstIdx_,noGoods, tabuVios, c.satisfactions_, degreeOfInfluence);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
         }else{
             
         }
    }
    
    public void RAfullInfluenceWith(final Chromosome c, int degreeOfInfluence){        
        if(externalData_ != null){
            try {
                externalData_.RAinfluenceFull(vals_, fitness_, this.satisfactions_, 
                        valVsConstIdx_, tabuVios, c.satisfactions_, degreeOfInfluence);
//                this.refreshFitness();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
         }else{
             
         }
    }
    /**
     * This won't ensure generating feasible solution. It only refreshes the 
     * related component (like valVsConstIdx_) of a chromosome. If the input is infeasible it will remain
     * infeasible after calling this function.
     * @param maxCSPval
     * @param bUpdateBest
     * @throws SolutionFoundException 
     */
    private void refreshObjectiveFunction(boolean ... bUpdateBest) throws SolutionFoundException{
    //DO NOT DELETE THESE LINES
    //<<
        //totalEvals++;
        totalRefEvals += this.vals_.size();
        double prevRank;
        double curRank;
        double prevHardVios;
        double curHardVios;
        prevRank = this.getFitnessVal(0); //this.getRank();
        prevHardVios = this.getFitnessVal(1);
    //>>

    //TODO... Call your objective function here....
    //Start<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    try{
        if(externalData_ != null)
            externalData_.objectiveFnRefresh(satisfactions_, fitness_, vals_, valVsConstIdx_, tabuVios, false); 

        else
            CCSPfns.objFn(1, vals_, fitness_, violations_, userInput_, CspProcess.maxCSPval);
        
//        totalHardVios = fitness_.get(1).intValue();

        boolean bupdate = true;
        if(bUpdateBest.length > 0){
            if(!bUpdateBest[0]){
                bupdate = false;
            }
        }
        
        
        if(this.isPartialSolution()){
            if(CspProcess.getBestSoFarCOP() != null && bupdate)
                if(this.isMorePromisingThanBestCOP()){
                    CspProcess.setBestSoFarCOP((Chromosome)this.clone());
                }
        }else{
            if(CspProcess.getBestSoFarCSP() != null)
                if(this.isMorePromisingThanBestCSP()){
                    CspProcess.setBestSoFarCSP((Chromosome)this.clone());
                }
        }
        
    }catch (SolutionFoundException sfe){
        CspProcess.setBestSoFarCOP((Chromosome)this.clone());
        throw sfe;
    }
    //End>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    //DO NOT DELETE THESE LINES
    //<<
        curRank = this.getFitnessVal(0);//this.getRank();
        curHardVios = this.getFitnessVal(1);
        
        if(MyMath.roundN(curHardVios - prevHardVios, CspProcess.FIT_DP_LIMIT) < 0.0){
            noProgressCounter=0;
            goodnessAge++;
        }else if(MyMath.roundN(curHardVios - prevHardVios, CspProcess.FIT_DP_LIMIT) > 0.0){
            noProgressCounter++;
        }else{
            if(MyMath.roundN(curRank - prevRank, CspProcess.FIT_DP_LIMIT)>=0.0 ){ //worse or same => stagnant
                noProgressCounter++;
            }else{
                noProgressCounter=0;
                goodnessAge++;
            }
        }
        if(noProgressCounter == 0){ //improved...
            this.prevBest.update();
        }
        
        if(isStagnant(CspProcess.NO_PROGRESS_LIMIT)){
            goodnessAge = 0;
        }
    //>>    
    }
    
    
    public void mutationCluster(final int times, boolean ... bUpdateBest) throws SolutionFoundException{
    //DO NOT DELETE THESE LINES
    //<<
        //totalEvals++;
        totalRefEvals += this.vals_.size();
        double prevRank;
        double curRank;
        double prevHardVios;
        double curHardVios;
        prevRank = this.getFitnessVal(0); //this.getRank();
        prevHardVios = this.getTotalHardVios();
    //>>

    //TODO... Call your objective function here....
    //Start<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    try{
        if(!this.isSolution()){
            return;
        }
        
        if(externalData_ != null)
            for (int i = 0; i < times; i++) {
                externalData_.mutationCluster(satisfactions_, vals_, fitness_, valVsConstIdx_, tabuVios);
            }            
        else
            CCSPfns.objFn(1, vals_, fitness_, violations_, userInput_, CspProcess.maxCSPval);
        
//        totalHardVios = fitness_.get(1).intValue();

        boolean bupdate = true;
        if(bUpdateBest.length > 0){
            if(!bUpdateBest[0]){
                bupdate = false;
            }
        }
        
        
        if(this.isPartialSolution()){
            if(CspProcess.getBestSoFarCOP() != null && bupdate)
                if(this.isMorePromisingThanBestCOP()){
                    CspProcess.setBestSoFarCOP((Chromosome)this.clone());
                }
        }else{
            if(CspProcess.getBestSoFarCSP() != null)
                if(this.isValid() && this.getFitnessVal(0) < CspProcess.getBestSoFarCSP().getFitnessVal(0)){
                    //CspProcess.bestSoFar.setVals((ArrayList<Double>)vals_.clone(), maxCSPval);
                    CspProcess.setBestSoFarCSP((Chromosome)this.clone());
                }
        }
        
    }catch (SolutionFoundException sfe){ 
        CspProcess.setBestSoFarCOP((Chromosome)this.clone());
        throw sfe;
    }
    //End>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    //DO NOT DELETE THESE LINES
    //<<
        curRank = this.getFitnessVal(0);//this.getRank();
        curHardVios = this.getTotalHardVios();
        
        if(MyMath.roundN(curHardVios - prevHardVios, CspProcess.FIT_DP_LIMIT) < 0.0){
            noProgressCounter=0;
            goodnessAge++;
        }else if(MyMath.roundN(curHardVios - prevHardVios, CspProcess.FIT_DP_LIMIT) > 0.0){
            noProgressCounter++;
        }else{
            if(MyMath.roundN(curRank - prevRank, CspProcess.FIT_DP_LIMIT)>=0.0 ){ //worse or same => stagnant
                noProgressCounter++;
            }else{
                noProgressCounter=0;
                goodnessAge++;
            }
        }
        if(noProgressCounter == 0){ //improved...
            this.prevBest.update();
        }
        if(isStagnant(CspProcess.NO_PROGRESS_LIMIT)){
            goodnessAge = 0;
        }
    //>>    
    }
    
    
    /**
     * mutation means changes within the chromosomes. If the input is valid then
     * the output is certainly valid.
     * @param Aper
     * @param Bper
     * @param atCorner
     * @param useAppend
     * @param n
     * @param valTo - destination of the value (index and value) is provided
     * @param isDynamic
     * @param bUpdateBest
     * @throws SolutionFoundException 
     */
    public void mutationKempe(final double Aper, final double Bper, final boolean atCorner, 
    final boolean useAppend, final int n, final Element valTo, final boolean isDynamic, boolean ... bUpdateBest) throws SolutionFoundException{
    //DO NOT DELETE THESE LINES
    //<<
        //totalEvals++;
        totalRefEvals += this.vals_.size();
        double prevRank;
        double curRank;
        double prevHardVios;
        double curHardVios;
        prevRank = this.getFitnessVal(0); //this.getRank();
        prevHardVios = this.getTotalHardVios();
    //>>

    //TODO... Call your objective function here....
    //Start<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    try{
        if(externalData_ != null)
            externalData_.kempe(satisfactions_, vals_, valVsConstIdx_, fitness_, noGoods, 
                tabuVios, Aper,  Bper, atCorner, useAppend, n, valTo, isDynamic);
        else
            CCSPfns.objFn(1, vals_, fitness_, violations_, userInput_, CspProcess.maxCSPval);
        
//        totalHardVios = fitness_.get(1).intValue();

        boolean bupdate = true;
        if(bUpdateBest.length > 0){
            if(!bUpdateBest[0]){
                bupdate = false;
            }
        }
        
        
        if(this.isPartialSolution()){
            if(CspProcess.getBestSoFarCOP() != null && bupdate)
                if(this.isMorePromisingThanBestCOP()){
                    CspProcess.setBestSoFarCOP((Chromosome)this.clone());
                }
        }else{
            if(CspProcess.getBestSoFarCSP() != null)
                if(this.isValid() && this.getFitnessVal(0) < CspProcess.getBestSoFarCSP().getFitnessVal(0)){
                    //CspProcess.bestSoFar.setVals((ArrayList<Double>)vals_.clone(), maxCSPval);
                    CspProcess.setBestSoFarCSP((Chromosome)this.clone());
                }
        }
        
    }catch (SolutionFoundException sfe){
        CspProcess.setBestSoFarCOP((Chromosome)this.clone());
        throw sfe;
    }
    //End>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    //DO NOT DELETE THESE LINES
    //<<
        curRank = this.getFitnessVal(0);//this.getRank();
        curHardVios = this.getTotalHardVios();
        
        if(MyMath.roundN(curHardVios - prevHardVios, CspProcess.FIT_DP_LIMIT) < 0.0){
            noProgressCounter=0;
            goodnessAge++;
        }else if(MyMath.roundN(curHardVios - prevHardVios, CspProcess.FIT_DP_LIMIT) > 0.0){
            noProgressCounter++;
        }else{
            if(MyMath.roundN(curRank - prevRank, CspProcess.FIT_DP_LIMIT)>=0.0 ){ //worse or same => stagnant
                noProgressCounter++;
            }else{
                noProgressCounter=0;
                goodnessAge++;
            }
        }
        if(noProgressCounter == 0){ //improved...
            this.prevBest.update();
        }
        if(isStagnant(CspProcess.NO_PROGRESS_LIMIT)){
            goodnessAge = 0;
        }
//        if(isStagnant()){
//            goodnessAge = 0;
//        }
    //>>    
    }

    
    /**
     * NOT TESTED... TEST IT FIRST BEFORE USING IT
     */
    private void resetObjectiveFunction() throws SolutionFoundException{
        //DO NOT DELETE THESE LINES
        //<<
            //totalEvals++;
        totalRefEvals += vals_.size();
            double prevRank;
            double curRank;
            double prevHardVios;
            double curHardVios;
            prevRank = this.getFitnessVal(0); //this.getRank();
            prevHardVios = this.getFitnessVal(1);
        //>>

        //TODO... Call your objective function here....
        //Start<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        try{
            if(externalData_ != null)
                externalData_.objectiveFnReset(vals_, fitness_, satisfactions_, noGoods, valVsConstIdx_, tabuVios); 
            else
                CCSPfns.objFn(1, vals_, fitness_, violations_, userInput_, CspProcess.maxCSPval);  
            
//            totalHardVios = fitness_.get(1).intValue();

            if(this.isPartialSolution()){                
                if(CspProcess.getBestSoFarCOP() != null)
                    if(this.isMorePromisingThanBestCOP()){
                        CspProcess.setBestSoFarCOP((Chromosome)this.clone());
                    }
            }else{
                if(CspProcess.getBestSoFarCSP() != null)
                    if(this.isMorePromisingThanBestCSP()){
                        //CspProcess.bestSoFar.setVals((ArrayList<Double>)vals_.clone(), maxCSPval);
                        CspProcess.setBestSoFarCSP((Chromosome)this.clone());
                    }
            }
        }catch (SolutionFoundException sfe){
            CspProcess.setBestSoFarCOP((Chromosome)this.clone());
            throw sfe;
        }
        //End>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
                
        //DO NOT DELETE THESE LINES
        //<<
            curRank = this.getFitnessVal(0);//this.getRank();
            curHardVios = this.getFitnessVal(1);  
            
            if(MyMath.roundN(curHardVios - prevHardVios, CspProcess.FIT_DP_LIMIT) < 0.0){
                noProgressCounter=0;
                goodnessAge++;
            }else if(MyMath.roundN(curHardVios - prevHardVios, CspProcess.FIT_DP_LIMIT) > 0.0){
                noProgressCounter++;
            }else{
                if(MyMath.roundN(curRank - prevRank, CspProcess.FIT_DP_LIMIT)>=0.0 ){ //worse or same => stagnant
                    noProgressCounter++;
                }else{
                    noProgressCounter=0;
                    goodnessAge++;
                }
            }
            if(noProgressCounter == 0){ //improved...
                this.prevBest.update();
            }
            if(isStagnant(CspProcess.NO_PROGRESS_LIMIT)){
                goodnessAge = 0;
            }
        //>>
    }
    
    public void debugPrint(){
        System.out.println("testing<<<<<<<<<<<<<");
        System.out.println("val: " + vals_.size());
        System.out.println("noGood: "+ noGoods.size());
//        int sz = 0;
//        for (ArrayList<Double> al: affinity_) {
//            sz+=al.size();
//        }
//        System.out.println("affinity " + sz);
        System.out.println("valvscons: " + valVsConstIdx_.length);
        System.out.println(">>>>>>>>>>>>>>>>>>>>>");
    }
    
    
    /**
     * NOT TESTED.... TEST IT PROPERLY BEFORE USE
     */
    private boolean appendObjectiveFunction(boolean ... bUpdateBest) throws SolutionFoundException{
        //totalEvals++;
        totalAppEvals += vals_.size();
        //DO NOT DELETE THESE LINES
        //<<
            double prevRank;
            double curRank;
            double prevHardVios;
            double curHardVios;
            prevRank = this.getFitnessVal(0); //this.getRank();
            prevHardVios = this.getFitnessVal(1);
            boolean upgraded = this.isPartialSolution();
        //>>  
          
        boolean bupdate = true;
        if(bUpdateBest.length > 0){
            if(!bUpdateBest[0]){
                bupdate = false;
            }
        } 
            
        //TODO... Call your objective function here....
        //Start<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
            boolean isAppended;
        try{
            if(externalData_ != null){
                isAppended = externalData_.objectiveFnAppend(vals_, fitness_, satisfactions_, noGoods, valVsConstIdx_, tabuVios); 
            }
            else{
                CCSPfns.objFn(1, vals_, fitness_, violations_, userInput_, CspProcess.maxCSPval);
                isAppended = true; //refresh above.
            }
            
            if(this.isPartialSolution()){
//                refreshFitness(bUpdateBest);
                upgraded = !upgraded;
                if(CspProcess.getBestSoFarCOP() != null && bupdate){
                    if(this.isMorePromisingThanBestCOP()){ 
                        CspProcess.setBestSoFarCOP((Chromosome)this.clone());
                    }
                }
            }else{
                if(CspProcess.getBestSoFarCSP() != null){
                    if(this.isMorePromisingThanBestCSP()){
                        CspProcess.setBestSoFarCSP((Chromosome)this.clone());
                    }
                }
            }

        }catch (SolutionFoundException sfe){
            CspProcess.setBestSoFarCOP((Chromosome)this.clone());
            throw sfe;
        }
        //End>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
       
        //DO NOT DELETE THESE LINES
        //<<
            curRank = this.getFitnessVal(0);//this.getRank();
            curHardVios = this.getFitnessVal(1);
        
            if(MyMath.roundN(curHardVios - prevHardVios, CspProcess.FIT_DP_LIMIT) < 0.0){
                noProgressCounter=0;
                goodnessAge++;
            }else if(MyMath.roundN(curHardVios - prevHardVios, CspProcess.FIT_DP_LIMIT) > 0.0){
                noProgressCounter++;
            }else{
                if(CspProcess.getBestSoFarCOP()!=null){
                    if(CspProcess.getBestSoFarCOP().isPartialSolution()){
                        if(MyMath.roundN(curRank - prevRank, CspProcess.FIT_DP_LIMIT)>=0.0 ){ //worse or same => stagnant
                            noProgressCounter++;
                        }else{
                            noProgressCounter=0;
                            goodnessAge++;
                        }
                    }else{
                        noProgressCounter++;
                    }
                }
            }
            if(noProgressCounter == 0){ //improved...
                this.prevBest.update();
            }
            
            if(isStagnant(CspProcess.NO_PROGRESS_LIMIT)){
                goodnessAge = 0;
            }
            
            if(upgraded){
                noProgressCounter = 0;
            }
        //>>
        return isAppended;
    }

    /**
     * checks if marriage is compatible in a sense that two chromosomes should
     * have at least two opposite positions which can help in finding the point
     * of intersection. for example {-1,-1, 0 1} and {-1, 1, 0 , -1} has opposite
     * positions at 2nd and 4th place.
     * 
     * @param c another couple
     * @return 
     */
    public boolean  isMarriageCompatible(Chromosome c){        
        int count = 0;
        boolean result = false;       
        
        CspProcess.bringCloserRatio = 0.5;
        
        if(this.hasSameRankComponent(c)){ // && this.getRankComponents().size() < userInput_.totalConstraints-userInput_.totalDecisionVars){
            CspProcess.bringCloserRatio = 0.10;
            result = true;  
//            result = false;
        }else{ 
            CspProcess.bringCloserRatio = 0.5;
            result = true;
        }
        
        return  result;
    }

    public void useImmunity(int curGen, boolean considerConsecutiveGens) {
        if(considerConsecutiveGens){
            if(RAimmunityOnGen+1==curGen || RAimmunity >= RA_MAX_IMMUNITY){//used in previous generation
                RAimmunity--;
            }
        }else{
            RAimmunity--;
        }

        if(RAimmunity < 0){
            RAimmunity = 0;
        }else{
            RAimmunityOnGen = curGen;
        }
    }

    public int getImmunity() {
        return RAimmunity;
    }
    
    public void resetImmmunity(){
        RAimmunity = RA_MAX_IMMUNITY;
    }
    
    
    

    
//     
//     private int violationChk(int d, int j){
//         return externalData_.isViolated(vals_.get(d).intValue(), vals_.get(j).intValue(),Math.abs(d-j));
//         
//     }
     

    
    
    @Override
    public String toString() {
        String sp = "  ";
        String print = "\n"+sp+"{";
        String lineStart = "\n"+sp+sp;
        print += lineStart+"vals("+ vals_.size()+ "):, " + vals_.toString();
        print += lineStart+"Fitness (bweighted-"+userInput_.bWeighted+"):," + getFitnessVal(0).toString();
        if(externalData_ != null)
            print += lineStart+"Functional value: " + this.externalData_.getFunctionalVal(getFitnessVal(0));
        if(violations_.isEmpty())
            print += lineStart + "Violations:, (No Violation)";
        else
            print += lineStart +"Violations:," + violations_.toString();
        if(!satisfactions_.isEmpty())
            print += lineStart+"Satisfaction:" + satisfactions_.toString();
        
//        if(!affinity_.isEmpty())
//            print += lineStart+"(Un)Affinity: " + affinity_.toString();
        
        print += lineStart+ "Hard Vios: " + getTotalHardVios();
        
//        print += lineStart+"valsVsConstraintIdx:";
//        for (int d = 0; d < valVsConstIdx_.length; d++) {
//            if(!valVsConstIdx_[d].isEmpty())
//                print+= "(" + d + ")"+ valVsConstIdx_[d].toString()+"; ";            
//        }
        
//        print += lineStart+"NoGoods:" + noGoods.toString();
        print += lineStart+"Rank:," + getRank()+"/[0:"+(userInput_.totalConstraints-1)+"]";
//        print += lineStart+"pStart:" + externalData_.getpStart();
        print += "\n"+sp+"}\n";
        return print;
    }

    /**
     * ro value determines the rank of novelty. The higher value the better.
     * @param chrome
     * @return
     * @throws MyException 
     */
    public double getDoubleRoValue(ArrayList<Chromosome> pop, int knearest) throws MyException{
        double ro;
        int maxViolation = this.userInput_.totalConstraints;
        ArrayList<Integer> validChromosomesIdx = new ArrayList<Integer>();
        Double []dist;
        int tempKnearest;
        
//        if(chrome.getRank() == maxViolation-1){
//            return -1.0;
//        }
        
        if (pop.isEmpty()){
            throw new MyException("No chromosme population", "Variable Initialization Error",JOptionPane.ERROR_MESSAGE);
        }
 
        for (int i = 0; i < pop.size(); i++) {
            if (pop.get(i).getRank() != maxViolation)
                validChromosomesIdx.add(i);            
        }

        dist = new Double[validChromosomesIdx.size()];
        for (int i = 0; i < validChromosomesIdx.size(); i++) {
            dist[i] = MyMath.norm(new ArrayList<Double>(this.getVals()), 
                    new ArrayList<Double>(pop.get(validChromosomesIdx.get(i)).getVals()), MyMath.DIST_EUCLEADIAN);
//            NOTE: I am using "SQUARE of distance" instead of just distance
//            because I will be using variance for ro_min.
            dist[i] = Math.pow(dist[i], 2);

        }
        Arrays.sort(dist);        
        // x1 itself is included in this set which should have the value 0.
        if(dist.length<=knearest){ //////@Danger code............................
            tempKnearest = dist.length-1;
        }else{
            tempKnearest = knearest;
        }
        ro = Math.pow(1.0/tempKnearest, 2) * MyMath.sum(dist, 0, tempKnearest);//Note: should not be knearest-1 as x1 itself is also included

        ro = MyMath.roundN(ro, 0);
        this.tempRo = ro;
        return ro;
    }

    
    /**
     * This method determines &rho; value for nominal data types. the higher the
     * the value the better the quality of the solution.<BR>
     * <B>Precaution:</B> it is computationally very expensive. 
     * @param chrome
     * @return
     * @throws MyException 
     */
    public double getIntegerRoValue(ArrayList<Chromosome> pop, int knearest) throws MyException{
        double ro;
        ArrayList<Integer> validChromosomesIdx = new ArrayList<Integer>();
        Double []dist;
        int tempKnearest;
        
        if (pop.isEmpty()){
            throw new MyException("No chromosme population", "Variable Initialization Error",JOptionPane.ERROR_MESSAGE);
        }
 
        for (int i = 0; i < pop.size(); i++) {
            if (pop.get(i).isValid()) // getRank() != this.userInput_.total__updatedConstraints)
                validChromosomesIdx.add(i);            
        }

        dist = new Double[validChromosomesIdx.size()];
        for (int i = 0; i < validChromosomesIdx.size(); i++) {
            dist[i] = 1.0*MyMath.norm2D(MyMath.DIST_HAMMING_PATTERN, satisfactions_, pop.get(validChromosomesIdx.get(i)).satisfactions_,null,-1);
                this.refreshValVsConstIdx();
                pop.get(validChromosomesIdx.get(i)).refreshValVsConstIdx();
//                    MyMath.norm(vals_, pop.get(validChromosomesIdx.get(d)).vals_,
//                    MyMath.DIST_HAMMING_PATTERN);
//            NOTE: I am using "SQUARE of distance" instead of just distance
//            because I will be using variance for ro_min.
            dist[i] = Math.pow(dist[i], 2);

        }
        Arrays.sort(dist);        
        // x1 itself is included in this set which should have the value 0.
        if(dist.length<=knearest){ //////@Danger code............................
            tempKnearest = dist.length-1;
        }else{
            tempKnearest = knearest;
        }
        ro = (1.0/tempKnearest) * MyMath.sum(dist, 0, tempKnearest);//Note: should not be knearest-1 as x1 itself is also included
        
//        ro = MyMath.roundN(ro, 0); //to reduce so much variations...
        this.tempRo = ro;
        return ro;                     
    }
    
    

    @Override
    public int compareTo(Object obj){
        if (!(obj instanceof Chromosome)) {
            throw new ClassCastException("Not a Chromosome");
        }
        Chromosome c = (Chromosome) obj;
        
        if(this.tmpSortBy != c.tmpSortBy){
            throw new UnsupportedOperationException("Found different sort types.");
        }
////        if(!fitness_.isEmpty())
////            return (int)(this.getFitnessVal(0) - c.getFitnessVal(0)); ////////////???????????????????????????????
////        else
        
        int totalConstainThis=0;
        int totalConstainC=0;
        int retVal;
        
//        if(this.tmpSortBy == Chromosome.BY_DISCOURAGE){
//            for (Integer pv : discourageVios) {
//                if(this.violations_.contains(pv)){
//                    totalConstainThis++;
//                }
//                if(c.violations_.contains(pv)){
//                    totalConstainC++;
//                }
//            }
//            if(totalConstainThis - totalConstainC>0){ //totalConstainThis  is worse
//                return 1;
//            }else if(totalConstainThis - totalConstainC<0){
//                return -1;
//            }else{
//                this.tmpSortBy = BY_VIOLATIONS;
//                c.tmpSortBy = BY_VIOLATIONS;
//                if(this.getRank() - c.getRank()>0){ //this is worse than c
//                    retVal =  1;
//                }else if(this.getRank() - c.getRank()<0){
//                    retVal =  -1;
//                }else{
//                    retVal =  0;
//                }
//                this.tmpSortBy = Chromosome.BY_DISCOURAGE;
//                c.tmpSortBy = Chromosome.BY_DISCOURAGE;
//                return retVal;
//            }
//        }
        
        if(this.tmpSortBy == Chromosome.BY_VIOLATIONS 
                || this.tmpSortBy == Chromosome.BY_SATISFACTIONS
                || this.tmpSortBy == Chromosome.BY_FITNESS
                || this.tmpSortBy == Chromosome.BY_HARD_CONSTRAINT_VIOS
//                || this.tmpSortBy == Chromosome.BY_HAMMING_DIST
                || this.tmpSortBy == Chromosome.BY_GOODNESS_AGE
                || this.tmpSortBy == Chromosome.BY_RHO) //good to bad - less to more
            //return this.getRank() - c.getRank();
        {
            if(this.getRank() - c.getRank()>0){
                return 1;
            }else if(this.getRank() - c.getRank()<0){
                return -1;
            }else{
                return 0;
            }
        }
//        else if (this.tempSortBy == Chromosome.BY_FITNESS){
//            return (int)(this.getFitnessVal(0) - c.getFitnessVal(0));
//        }
//        else if(this.tmpSortBy == Chromosome.BY_IMMUNITY){
//            return this.immunity - c.immunity;
//        }
//        else if(this.tmpSortBy == Chromosome.BY_RHO){ //good to bad - more to less
//            if(this.tempRo - c.tempRo > 0){
//                return -1;
//            }else if(this.tempRo - c.tempRo<0){
//                return 1;
//            }else{
//                return 0;
//            }
//        }
        else //can also be used for "preferenced fitness_ values" - not implemented yet.
            throw new UnsupportedOperationException("Not supported yet.");
        
    }

    @Override
    public Object clone() {
        Chromosome chromosome;
        try {
            chromosome = (Chromosome) super.clone();
            // nope... do not clone thses....
            //chromosome.externalData_ = (ExternalData) this.externalData_.clone();
            //chromosome.userInput_ = (UserInput) this.userInput_.clone();

////            lTabu = lTabu; //to clone or not to clone....????
            Chromosome.PrevBest pb = chromosome.new PrevBest();
            pb.gen = prevBest.gen;
            pb.prevBestFit = prevBest.prevBestFit;            
            chromosome.prevBest = pb;
            
            chromosome.vals_ = (ArrayList<Double>)vals_.clone(); 
//            chromosome.affinity_ = new ArrayList<ArrayList<Double>>();
//            for (int d = 0; d < affinity_.size(); d++) {
//                chromosome.affinity_.add((ArrayList<Double>)affinity_.get(d).clone()); 
//            }
            
            chromosome.valVsConstIdx_ = new Idx2D[valVsConstIdx_.length];
            for (int i = 0; i < valVsConstIdx_.length; i++) {
                chromosome.valVsConstIdx_[i] = (Idx2D)valVsConstIdx_[i].clone();
            }          
            chromosome.noGoods = (ArrayList<Integer>)noGoods.clone();
            chromosome.fitness_ = (ArrayList<Double>)fitness_.clone();
            chromosome.violations_ = (ArrayList<Integer>)violations_.clone();
            chromosome.satisfactions_ = new ArrayList<ArrayList>();            
            for (int i = 0; i < satisfactions_.size(); i++) {
                chromosome.satisfactions_.add((ArrayList)satisfactions_.get(i).clone()); 
            }

            chromosome.tabuVios = (ByRef)tabuVios.clone();
            
            return chromosome;

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        } catch (NullPointerException npe){
            npe.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Chromosome)) {
            throw new ClassCastException("Not a Chromosome");
        }
        Chromosome c = (Chromosome) obj;
        boolean iseq;

        if(this.tmpSortBy == Chromosome.BY_VIOLATIONS){// satisfactions_.isEmpty()){ //using violations...
            iseq = this.violations_.equals(c.violations_);
        }else if (this.tmpSortBy == Chromosome.BY_SATISFACTIONS){
            iseq = this.satisfactions_.equals(c.satisfactions_);            
        }else{
            throw new UnsupportedOperationException("Unsupported tmpSortBy encountered!");
        }
       
        return iseq;        
    }
    
    
}

// <editor-fold defaultstate="collapsed" desc="ObjectiveFunction Code">

/// for resetObjFunction
//<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<     
                    //Time tabling problme
                    //<<
            //            ArrayList<Double> satisfyCheck;
            //            int count;
            //            satisfyCheck = vals_;
            //
            //            for (Double d : vals_) {
            //                count = 0;
            //                for (Double j : satisfyCheck) {
            //                    if(externalData_.isViolated(d.intValue(), j.intValue())){
            //                        satisfyCheck = new ArrayList<Double>(satisfyCheck.subList(0, count));
            //                        break;
            //                    }else{
            //                        ;
            //                    }
            //                    count++;
            //                }
            //            }
            //            vals_ = satisfyCheck;
                    //>>

                    //MAP COLORING PROBLEM
                    //<<
            //            updateObjectiveFunctionVars();
                    //>>

                    //NQueen Problem
                    //<<
            //        this.fitness_.clear();
            //        this.fitness_.add(Double.NaN);
            //        this.sortBy = Chromosome.BY_SATISFACTIONS;
            //        this.satisfactions_.clear();
            //
            //        repairVal();
            //
            //        for (int d = 0; d < vals_.size(); d++) {
            //            for (int j = d+1; j < vals_.size(); j++) {
            //                 if(d!=j){
            //                    if(externalData_.isViolated(vals_.get(d).intValue(), vals_.get(j).intValue(),Math.abs(d-j))){
            //                        vals_.remove(j);
            //                        j--;
            //                        
            //                    }else{
            //                        ;
            //                    }
            //                }
            //            }
            //        }
            //
            //        for (Double v : vals_) {
            //            satisfactions_.add(v.intValue());
            //        }
                    //>>


                    //<<My CIRCLEs Test Problem
            //        this.fitness_ = new ArrayList<Double>();
            //        this.sortBy = Chromosome.BY_VIOLATIONS; //its a MUST
            //
            //        if(this.vals_.size() != this.userInput_.totalDecisionVars){
            //            return;
            //        }
            //
            //        this.fitness_.add( Math.sqrt(4- Math.pow((Double)this.vals_.get(0)-2,2))+2 );
            //        this.fitness_.add( -Math.sqrt(4- Math.pow((Double)this.vals_.get(0)-2,2))+2 );
            //
            //        this.fitness_.add( Math.sqrt(1- Math.pow((Double)this.vals_.get(0)-4.98,2)) +2);
            //        this.fitness_.add( -Math.sqrt(1- Math.pow((Double)this.vals_.get(0)-4.98,2)) +2);
            //
            //        this.fitness_.add( Math.sqrt(1- Math.pow((Double)this.vals_.get(0)-4.5,2)) +1);
            //        this.fitness_.add( -Math.sqrt(1- Math.pow((Double)this.vals_.get(0)-4.5,2)) +1);
            //
            //        ArrayList<Double> x = this.vals_;
            //        ArrayList<Double> f = this.fitness_;
            //
            //        this.violations_.clear();
            //
            //        if((x.get(1) > f.get(0) || x.get(1) < f.get(1) || x.get(0)>4)      ||    (x.get(1) < f.get(1) || x.get(1) > f.get(0) || x.get(0)>4 || x.get(0)<0))
            //            this.violations_.add(1);
            //
            //        if((x.get(1) > f.get(2) || x.get(1) < f.get(3) || x.get(0)<3.98)    ||    (x.get(1) < f.get(3) || x.get(1) > f.get(2) || x.get(0)<3.98 || x.get(0)>5.98))
            //            this.violations_.add(2);
            //
            //        if((x.get(1) > f.get(4) || x.get(1) < f.get(5) || x.get(0)<3.5)    ||    (x.get(1) < f.get(5) || x.get(1) > f.get(4) || x.get(0)<3.5 || x.get(0)>5.5))
            //            this.violations_.add(3);
                    //>>
            
//resetObjFunction
//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>




//for updateObjFunction<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

                //resetObjectiveFunctionVars();

                //<< Nqueen problem
        //        this.fitness_.clear();
        //        this.fitness_.add(Double.NaN);
        //        this.sortBy = Chromosome.BY_SATISFACTIONS;
        //        this.satisfactions_.clear();
        //
        //        repairVal();
        //
        //        int d;
        //        for (int j = vals_.size()-1; j >=0; j--) {
        //            d = vals_.size()-1;
        //            if(d!=j){
        //                if(externalData_.isViolated(vals_.get(d).intValue(), vals_.get(j).intValue(), d-j)){
        //                    vals_.remove(d);
        //                    break;
        //                }
        //            }
        //        }
        //
        //
        //        for (Double v : vals_) {
        //            satisfactions_.add(v.intValue());
        //        }                    
                //>>


            //<< NqueenII            
        //        this.fitness_.clear();
        //        this.fitness_.add(Double.NaN);
        //        this.sortBy = Chromosome.BY_SATISFACTIONS;
        //        this.satisfactions_.clear();
        //
        //        repairVal();
        //
        //        for (int d = 0; d < vals_.size(); d++) {
        //            for (int j = d+1; j < vals_.size(); j++) {
        //                 if(d!=j){
        //                    if(externalData_.isViolated(vals_.get(d).intValue(), vals_.get(j).intValue(),Math.abs(d-j))){
        //                        vals_.remove(j);
        //                        j--;
        //                    }else{
        //                        ;
        //                    }
        //                }
        //            }
        //        }
        //
        //        for (Double v : vals_) {
        //            satisfactions_.add(v.intValue());
        //        }                    
            //>>


                //<< Graph coloring problem
        //        this.fitness_.clear();
        //        this.fitness_.add(Double.NaN);
        //        this.sortBy = Chromosome.BY_SATISFACTIONS;
        //        this.satisfactions_.clear();
        //
        //        repairVal();
        //
        //        for (int d = 0; d < vals_.size(); d++) {
        //            for (int j = 0; j < vals_.size(); j++) {
        //                 if(d!=j){
        //                    if(externalData_.isViolated(vals_.get(d), vals_.get(j))){
        //                        vals_.remove(j);
        //                        j--;
        //                    }else{
        //                        ;
        //                    }
        //                }
        //            }
        //        }
        //
        //        for (Double v : vals_) {
        //            satisfactions_.add(v.intValue());
        //        }
                //>>


                //<<Time Tabling Problem
        //        this.fitness_.clear();
        //        this.fitness_.add(Double.NaN); // fitness is not needed.
        //        this.sortBy = Chromosome.BY_SATISFACTIONS;
        //
        //        Integer element;
        //        boolean bviolated;
        //
        //        try {
        //            if(satisfactions_.isEmpty()){
        //                if(vals_.size() == 1){
        //                    satisfactions_.add(vals_.get(0).intValue());
        //                    return;
        //                }else{
        //                    throw new Exception("Incompatible values. Check satisfaction value and vals...");
        //                }
        //            }else{ //Only check the last element ..... it will be fast
        //                element = vals_.get(vals_.size()-1).intValue();//last element
        //
        //                bviolated = false;
        //                for (int d = 0; d < satisfactions_.size(); d++) {
        //                    if(externalData_.isViolated(satisfactions_.get(d).intValue(), element)){
        //                        bviolated = true;
        //                        break;
        //                    }
        //                }
        //
        //                if(!bviolated){
        //                    //shift the element from last position to first position
        //                    this.vals_.add(0,element.doubleValue());//insert to first -NOTE - order doesn't matter in this particular problem
        //                    this.vals_.remove(this.vals_.size()-1);//then remove from last
        //                    this.satisfactions_.add(0, element);
        //                }
        //            }
        //        } catch (Exception e) {
        //            e.printStackTrace();
        //            Application.getInstance().exit();
        //        }


                //>>



//updateObjFunction>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


//    private void objFunction_NQueenII(){          
//        this.fitness_.clear();
//        this.fitness_.add(Double.NaN);
//        this.tempSortBy = Chromosome.BY_SATISFACTIONS;
//        this.satisfactions_.clear();
//
//        repairVal();
//
//        for (int d = 0; d < vals_.size(); d++) {
//            for (int j = d+1; j < vals_.size(); j++) {
//                 if(d!=j){
//                    if(violationChk(d,j)>0){
//                        vals_.remove(j);
//                        j--;
//                    }else{
//                        ;
//                    }
//                }
//            }
//        }
//
//        refreshRank();
//    }
    
    
//     private void objFunction_graphColoring(){          
//        this.fitness_.clear();
//        this.fitness_.add(Double.NaN);
//        this.tempSortBy = Chromosome.BY_SATISFACTIONS;
//        this.satisfactions_.clear();
//        
//        repairVal();        
//        
//        for (int d = 0; d < vals_.size(); d++) {
//            for (int j = d+1; j < vals_.size(); j++) { 
//                 if(d!=j){
//                    if(externalData_.isViolated(vals_.get(d), vals_.get(j),-1)>0){
//                        vals_.remove(j);
//                        j--;
//                        //break;
//                    }else{
//                        ;
//                    }
//                }
//            }
//        }
//
//        refreshRank();        
//     }

//    private void objFunction_StackedGymnasticHalls(){
//        
//        this.fitness_ = new ArrayList<Double>();
//        this.tempSortBy = Chromosome.BY_VIOLATIONS; //its a MUST
//
//        //no fitness function.
//
//        this.violations_.clear();
//        Double x[] = new Double[this.userInput_.totalConstraints];
//        this.vals_.toArray(x);
//        
//        
//        //# Constants
//        double Pi = 3.14159265358979323846;
//        double ZeroPlus = Math.pow(10.0,-12.0);
//
//        //# by dynamics experts
//        double E_steel	= 220000.0;	
//
//        //# by civil engineer
//        double R_f	= 2.0;		
//        double S	= 1.65;	
//
//        //# additional
//        double h_t	= 0.5;		
//        double L_z = 11.5;	
//        ////
//        ////
//        //////#-------------------------------------------------------------------------------
//        //////# 12 variables
//        //////#-------------------------------------------------------------------------------
//        double E = x[0]; //		>=2	,<=10	;#2	# beam spacing [m]
//        double H_s1	= x[1]; //>=5	,<=8	;#7	# height of lower gym [m]
//        double H_s2	=x[2]; //>=5	,<=8	;#7	# height of upper gym [m]
//        double L; //		>=10	,<=50	;#27.4	# span [m]
//        double c = x[3]; //		>=200	,<=500	;#200	# half cover plate width [mm]
//        double d = x[4]; //		>=20	,<=40	;#20	# web thickness [mm]
//        double h_a = x[5]; //		>=800	,<=4000	;#4000	# beam height [mm]
//        double k1; //		>=1	,<=1e8	;#1e7	# modal stiffness [N/m]
//        double m; //		>=1	,<=1e6	;#1000	# modal weight [kg]
//        double P = x[6]; //		>=0	,<=10	;#5	# depth of foundation [m]
//        double q_concrete; //	>=200	,<=2000	;#200	# self weight [kg/m]
//        double t = x[7];// 		>=20	,<=100	;#20	# half cover plate thickness [mm]
//
//
//
//
//        //#ArchitectC01:	
//        L = 27.4;
//        //DynamicsC00:	
//        q_concrete = 200 * Math.pow(E,0.6);
//        //DynamicsC01:	
//        m = q_concrete * E * L / 2.0;
//        //DynamicsC02:	
//        k1 = E_steel/Math.pow(L,3.0)/Math.pow(10.0,6.0)*(3.37*Math.pow(h_a,3.0)*d+42.84*Math.pow(h_a,2.0)*t*c);
//
//
//        //# Constraints by client
//        //ClientC00:	
//        if(H_s1 < 5.5 + ZeroPlus)	//# needed for certain sports
//            this.violations_.add(1);
//        //ClientC01:
//        if(H_s2 < 5.5 + ZeroPlus)	//# needed for certain sports
//            this.violations_.add(2);
//        //ClientC02:	
//        if(E < 2.5 + ZeroPlus)	//# needed to mount sports facilities
//            this.violations_.add(3);
//
//        //# Constraints by contractor
//        //ContractorC00:
//        if(4.5*t > c)
//            this.violations_.add(4);
//        //ContractorC01:
//        if(c > 8.5*t)
//            this.violations_.add(5);
//        //ContractorC02:
//        if(40*d > h_a)
//            this.violations_.add(6);
//        //ContractorC03:
//        if(h_a > 100*d)
//            this.violations_.add(7);
//
//        //# Constraints by dynamics expert
//
//        //DynamicsC03:	
//        if(Math.pow((k1/m),0.5) < 8 * 2 * Pi)
//            this.violations_.add(8);
//
//        //# Constraints by civil engineer
//        //CivilC00:
//        if(0.5 * h_a * d * 235 / Math.pow(3,0.5) < 1000 * L * Math.pow(E,8.0/5)*R_f*S)
//            this.violations_.add(9);
//        //CivilC01:	
//        if(235*t*c*h_a/500.0 < 250 * L * Math.pow(E,8.0/5)*R_f*S)
//            this.violations_.add(10);
//
//        //# Constraints by Geologist
//        //GeologistC00:	
//        if(P > 5 - ZeroPlus)
//            this.violations_.add(11);
//
//        //# Constraints by architect
//        //ArchitectC00:
//        if(H_s1 + H_s2 + h_a/1000.0 + h_t > L_z + P - ZeroPlus)
//            this.violations_.add(12);
//    }
//    
//
//     private void objFunction_TrussDesign01(){
//        
//        this.fitness_ = new ArrayList<Double>();
//        this.tempSortBy = Chromosome.BY_VIOLATIONS; //its a MUST
//
//        //no fitness function.
//
//        this.violations_.clear();
//        Double x[] = new Double[this.userInput_.totalConstraints];
//        this.vals_.toArray(x);
//        
//        //Constants-------------------------------------------------------
//        double E = 210 * Math.pow(10.0, 6.0); //# (Unit = kN/m2, Youngs modulus of steel)
//        double T = 235000.0; //# (Unit = kN/m2, yield stress of steel)
//        double A = 0.25; //# Area of cross section of truss members
//        double r = 0.5; //# Radius of gyration of the cross section of truss members
//        double Pi = 4*Math.atan(1.0);
//        double ZeroPlus = 0.00000001; //# sufficiently small positive number
//
//        double P = 400.0; //#var H >=5, <=10;
//        double H = 6.0; //#var H >=5, <=10;height of truss ???
//        double L = 10.0;
//        
//        double x1, y1;
//        x1 = x[0];
//        y1 = x[1];
//
//        if((P/Math.tan(Math.atan((H-y1)/(L-x1)))) > T*A - ZeroPlus)
//            this.violations_.add(1);
//
//        if(((P*L/H)*Math.tan(Math.atan(y1/x1))) > T*A - ZeroPlus)
//            this.violations_.add(2);
//
//        if((P/Math.sin(Math.atan((H-y1)/(L-x1)))) > (Math.pow(Pi,2)*E/((Math.pow(Math.pow((L-x1)*(L-x1)+(H-y1)*(H-y1),0.5)/r,2)*A - ZeroPlus))))
//            this.violations_.add(3);
//
//
//        if((P/Math.sin(Math.atan((H-y1)/(L-x1)))) > T*A - ZeroPlus)
//            this.violations_.add(4);
//
//        if((P*L/H)/Math.cos(Math.atan(y1/x1)) > Pi*Pi*E/(Math.pow(   Math.pow(x1*x1+y1*y1,0.5)/r,  2)*A - ZeroPlus))
//            this.violations_.add(5);
//
//        if(((P*L/H)/Math.cos(Math.atan(y1/x1))) > T*A - ZeroPlus)
//            this.violations_.add(6);
//
//        if(Math.abs((P*L/H - P/Math.tan(Math.atan((H-y1)/(L-x1))))/Math.cos(Math.atan((H-y1)/x1))) > T*A - ZeroPlus)
//            this.violations_.add(7);
//
//        if ((P*L/H - P/Math.tan(Math.atan((H-y1)/(L-x1))))/Math.cos(Math.atan((H-y1)/x1)) <= 0){
//            if(-((P*L/H - P/Math.tan(Math.atan((H-y1)/(L-x1))))/Math.cos(Math.atan((H-y1)/x1))) > Pi*Pi*E/(Math.pow(   Math.pow(  (x1*x1+(H-y1)*(H-y1)),  0.5)  /r,   2)*A - ZeroPlus))
//                this.violations_.add(8);
//        }
//
//        if(x1 > L - ZeroPlus)
//            this.violations_.add(9);
//
//        if(y1 > H - ZeroPlus)
//            this.violations_.add(10);
//        
//     }

//     private void objFunction_bangleCircle(){
//        this.fitness_ = new ArrayList<Double>();
//        this.tempSortBy = Chromosome.BY_VIOLATIONS; //its a MUST
//
//        this.fitness_.add(this.vals_.get(0));
//        this.fitness_.add((1+this.vals_.get(1))/this.vals_.get(0));
//
//        ArrayList<Double> x = this.vals_;
//        ArrayList<Double> f = this.fitness_;
//
//        this.violations_.clear();
//        this.constFx_.clear();
//        this.position_.clear();
//        
//        //should bes...
//        double br = 5.0;
//        double sr = 4.9;
//        double outerBoundary = 30.0;
//        
//        this.constFx_.add(x.get(1)*x.get(1) + x.get(0)*x.get(0)-br*br); //=0
//        this.constFx_.add(x.get(1)*x.get(1) + x.get(0)*x.get(0)-sr*sr); //=0
//        this.constFx_.add(x.get(1)*x.get(1) + Math.pow(x.get(0)+2*br-1.0,2)-br*br); //=0
//        this.constFx_.add(x.get(1)*x.get(1) + Math.pow(x.get(0)+2*br-1.0,2)-sr*sr); //=0
//        
//        this.constFx_.add(x.get(1)-outerBoundary);
//        this.constFx_.add(x.get(1)+outerBoundary);
//        this.constFx_.add(x.get(0)-outerBoundary);
//        this.constFx_.add(x.get(0)+outerBoundary);
//        
//     
//        if(constFx_.size() != this.userInput_.totalConstraints){
//            System.err.println("constFx size is not same as total constraints");
//            Application.getInstance().exit();
//        }
//        
//        for (Double cf : this.constFx_) {
//            if(cf<0){
//                position_.add(-1);
//            }else if (cf>0){
//                position_.add(1);
//            }else{
//                position_.add(0);
//            }            
//        }
//   
//        if(this.constFx_.get(0)>0){
//            this.violations_.add(1);
//        }
//
//        if(this.constFx_.get(1)<0){
//            this.violations_.add(2);
//        }
//        
//        if(this.constFx_.get(2)>0){
//            this.violations_.add(3);
//        }
//
//        if(this.constFx_.get(3)<0){
//            this.violations_.add(4);
//        }
//       
//        //for outer boundaries.
//        if(this.constFx_.get(4)>0){
//            this.violations_.add(5);
//        } 
//        if(this.constFx_.get(5)<0){
//            this.violations_.add(6);
//        }
//        if(this.constFx_.get(6)>0){
//            this.violations_.add(7);
//        }
//        if(this.constFx_.get(7)<0){
//            this.violations_.add(8);
//        } 
//        
//    }
//     
//    
//    private void objFunction_broyden10(){
//        this.fitness_ = new ArrayList<Double>();
//        this.tempSortBy = Chromosome.BY_VIOLATIONS; //its a MUST
//        
//        //no fitness function
//        
//        //ArrayList<Double> x = this.vals_;
//        Double x[] = new Double[this.userInput_.totalConstraints];
//        this.vals_.toArray(x);
//        
//        this.violations_.clear();
//        
//        if(x[0]*(2+5*x[0]*x[0]) + 1 - (x[0]*(1+x[0]) + x[1]*(1+x[1])) != 0){
//            this.violations_.add(1);
//        }
//                
//        if(x[1]*(2+5*x[1]*x[1]) + 1 - (x[0]*(1+x[0]) + x[2]*(1+x[2])) != 0){
//            this.violations_.add(2);
//        }
//        
//        if(x[2]*(2+5*x[2]*x[2]) + 1 - (x[0]*(1+x[0]) + x[1]*(1+x[1]) + x[3]*(1+x[3])) != 0){
//            this.violations_.add(3);
//        }
//        
//        if(x[3]*(2+5*x[3]*x[3]) + 1 - (x[0]*(1+x[0]) + x[1]*(1+x[1]) + x[2]*(1+x[2]) + x[4]*(1+x[4])) != 0){
//            this.violations_.add(4);
//        }
//        
//        if(x[4]*(2+5*x[4]*x[4]) + 1 - (x[0]*(1+x[0]) + x[1]*(1+x[1]) + x[2]*(1+x[2]) + x[3]*(1+x[3])+ x[5]*(1+x[5])) != 0){
//            this.violations_.add(5);
//        }
//
//        if(x[5]*(2+5*x[5]*x[5]) + 1 - (x[0]*(1+x[0]) + x[1]*(1+x[1]) + x[2]*(1+x[2]) + x[3]*(1+x[3])+ x[4]*(1+x[4]) + x[6]*(1+x[6])) != 0){
//            this.violations_.add(6);
//        }
//
//        if(x[6]*(2+5*x[6]*x[6]) + 1 - (x[1]*(1+x[1]) + x[2]*(1+x[2]) + x[3]*(1+x[3]) + x[4]*(1+x[4])+ x[5]*(1+x[5]) + x[7]*(1+x[7])) != 0){
//            this.violations_.add(7);
//        }
//
//        if(x[7]*(2+5*x[7]*x[7]) + 1 - (x[2]*(1+x[2]) + x[3]*(1+x[3]) + x[4]*(1+x[4]) + x[5]*(1+x[5])+ x[6]*(1+x[6]) + x[8]*(1+x[8])) != 0){
//            this.violations_.add(8);
//        }
//
//        if(x[8]*(2+5*x[8]*x[8]) + 1 - (x[3]*(1+x[3]) + x[4]*(1+x[4]) + x[5]*(1+x[5]) + x[6]*(1+x[6])+ x[7]*(1+x[7]) + x[9]*(1+x[9])) != 0){
//            this.violations_.add(9);
//        }
//
//        if(x[9]*(2+5*x[9]*x[9]) + 1 - (x[4]*(1+x[4]) + x[5]*(1+x[5]) + x[6]*(1+x[6]) + x[7]*(1+x[7])+ x[8]*(1+x[8])) != 0){
//            this.violations_.add(10);
//        }
//        
//    }
//    
//    
//    private void objFunction_biggsc4(){
//    //<< deb problem
//
//        this.fitness_ = new ArrayList<Double>();
//        this.tempSortBy = Chromosome.BY_VIOLATIONS; //its a MUST
//
//        //no fitness function.
//
//        this.violations_.clear();
//        this.constFx_.clear();
//        this.position_.clear();
//        
//        ArrayList<Double> x = this.vals_;
//        double best_val_found = -24.375;
//	double eps = 1; 
//        
//        this.constFx_.add(best_val_found + eps - (-x.get(0)*x.get(2)-x.get(1)*x.get(3)));
//        this.constFx_.add(x.get(0)+x.get(1) -2.5);
//        this.constFx_.add(x.get(0)+x.get(2) -2.5); //=0
//        this.constFx_.add(x.get(0)+x.get(3) -2.5);
//        this.constFx_.add(x.get(1)+x.get(2) -2.0);
//        this.constFx_.add(x.get(1)+x.get(3) -2.0);
//        this.constFx_.add(x.get(2)+x.get(3) -1.5);
//        this.constFx_.add(x.get(0)+x.get(1)+x.get(2)+x.get(3)-5.0);    
//        
//        if(constFx_.size() != this.userInput_.totalConstraints){
//            System.err.println("constFx size is not same as total constraints");
//            Application.getInstance().exit();
//        }
//        
//        for (Double cf : this.constFx_) {
//            if(cf<0){
//                position_.add(-1);
//            }else if (cf>0){
//                position_.add(1);
//            }else{
//                position_.add(0);
//            }            
//        }
//
//        if(this.constFx_.get(0)<0){
//            this.violations_.add(1);
//        }
//        if(this.constFx_.get(1)<0|| this.constFx_.get(1)> 5){
//            this.violations_.add(2);
//        }
//        if(this.constFx_.get(2)<0|| this.constFx_.get(2)> 5){
//            this.violations_.add(3);
//        }
//        if(this.constFx_.get(3)<0|| this.constFx_.get(3)> 5){
//            this.violations_.add(4);
//        }
//        if(this.constFx_.get(4)<0|| this.constFx_.get(4)> 5){
//            this.violations_.add(5);
//        }
//        if(this.constFx_.get(5)<0|| this.constFx_.get(5)> 5){
//            this.violations_.add(6);
//        }
//        if(this.constFx_.get(6)<0|| this.constFx_.get(6)> 5){
//            this.violations_.add(7);
//        }
//        if(this.constFx_.get(7)<0){
//            this.violations_.add(8);
//        }
//    }
//    
//    
//    private void objFunction_h74mod(){
//        
//        this.fitness_ = new ArrayList<Double>();
//        this.tempSortBy = Chromosome.BY_VIOLATIONS; //its a MUST
//
//        //no fitness function.
//
//        this.violations_.clear();
//        Double x[] = new Double[this.userInput_.totalConstraints];
//        this.vals_.toArray(x);
//        
//        double a = 0.55;  
//        double c1 = Math.pow(2.0/3.0,-6);
//
////	cons1 {d in 1..2}: x[d] >= 0;
////cons2 {d in 1..2}: x[d] <= 1200;
////cons3 {d in 3..4}: x[d] >= -a;
////cons4 {d in 3..4}: x[d] <= a;
//
//
//    if(x[0] * (3 + Math.pow(1.0,-6.0)*x[0]*x[0]) + x[1] * (2 + Math.pow(c1*x[1],2.0)) > 5126.49811 + 51.2649811);
//	this.violations_.add(1);
//
//    if(x[3] - x[2] + a <0)
//	this.violations_.add(2);
//
//    if(x[2] - x[3] + a <0)
//	this.violations_.add(3);
//
//    if(1000 * Math.sin(-x[2] - 0.25) + 1000 * Math.sin(-x[3]-0.25) + 894.8 - x[0] != 0)
//	this.violations_.add(4);	
//
//    if(1000 * Math.sin(x[2] - 0.25) + 1000 * Math.sin(x[2]-x[3]-0.25) + 894.8 - x[1] != 0)
//	this.violations_.add(5);
//
//    if(1000 * Math.sin(x[3] - 0.25) + 1000 * Math.sin(x[3]-x[2]-0.25) + 1294.8 != 0)
//	this.violations_.add(6);
//    }

//    
//    
    
//    private void objFunction_test(){
//        
//        this.fitness_ = new ArrayList<Double>();
//        this.sortBy = Chromosome.BY_VIOLATIONS; //its a MUST
//
//        this.fitness_.add(this.vals_.get(0));
//        this.fitness_.add((1+this.vals_.get(1))/this.vals_.get(0));
//
//        ArrayList<Double> x = this.vals_;
//        ArrayList<Double> f = this.fitness_;
//
//        this.violations_.clear();
//        this.constFx_.clear();
//        this.position_.clear();
//        
//        double best_val_found = 0.2415051288;
//	double eps = 1;
//	double dt = 0.001;
//        double s2 = Math.sqrt(2.0);
//        //var
////        x {1..5} >= 0, <= 4;
//                //cons = 5
//		//var = 5
//        this.constFx_.add( best_val_found + eps - (2*x.get(0)*(x.get(0)-x.get(1)-1) + 1 + Math.pow(x.get(1),2) + Math.pow(x.get(2) - 1, 2) + Math.pow(x.get(3) - 1.0,4.0) + Math.pow(x.get(4) - 1.0,6.0)));
//        this.constFx_.add(dt - (Math.pow(x.get(0),2) * x.get(3) + Math.sin(x.get(3) - x.get(4)) - 2.0 * s2)); //=0
//        this.constFx_.add(dt + Math.pow(x.get(0),2) * x.get(3) + Math.sin(x.get(3) - x.get(4)) - 2.0 * s2); //=0
//        this.constFx_.add(dt - (x.get(1) + Math.pow(x.get(2)*1.0,4.0)*Math.pow(x.get(3),2) - 8 - s2)); //=0
//        this.constFx_.add(dt +  x.get(1) + Math.pow(x.get(2)*1.0,4.0)*Math.pow(x.get(3),2) - 8 - s2); //=0
//
//        if(constFx_.size() != this.userInput_.totalConstraints){
//            System.err.println("constFx size is not same as total constraints");
//            Application.getInstance().exit();
//        }
//        
//        for (Double cf : this.constFx_) {
//            if(cf<0){
//                position_.add(-1);
//            }else if (cf>0){
//                position_.add(1);
//            }else{
//                position_.add(0);
//            }            
//        }
//
//        
//        if(this.constFx_.get(0)<0){
//            this.violations_.add(1);
//        }
//
//        if(this.constFx_.get(1)<0){
//            this.violations_.add(2);
//        }
//        
//        if(this.constFx_.get(2)<0){
//            this.violations_.add(3);
//        }
//
//        if(this.constFx_.get(3)<0){
//            this.violations_.add(4);
//        }
//        if(this.constFx_.get(4)<0){
//            this.violations_.add(5);
//        }
//       
//        
//    }
 


    //<<My CIRCLEs Test Problem
//    private void objFunction_circles(){
//        this.fitness_ = new ArrayList<Double>();
//        this.tempSortBy = Chromosome.BY_VIOLATIONS; //its a MUST
//
//        this.fitness_.add(this.vals_.get(0));
//        this.fitness_.add((1+this.vals_.get(1))/this.vals_.get(0));
//
//        ArrayList<Double> x = this.vals_;
//        ArrayList<Double> f = this.fitness_;
//
//        this.violations_.clear();
//        this.constFx_.clear();
//        this.position_.clear();       
//        
//        //should bes...
//        double r1 = 2.0;
//        double r2 = 1.0;
//        double r3 = 1.0;
//        double outerBoundary = 10.0;
//        
//        this.constFx_.add((x.get(1)-2)*(x.get(1)-2) + (x.get(0)-2)*(x.get(0)-2)-r1*r1); //=0
//        this.constFx_.add((x.get(1)-2)*(x.get(1)-2) + Math.pow(x.get(0)-4.90, 2)-r2*r2); //=0
//        this.constFx_.add((x.get(1)-1)*(x.get(1)-1) + (x.get(0)-4.50)*(x.get(0)-4.50)-r3*r3); //=0
//        this.constFx_.add(x.get(1)-outerBoundary);
//        this.constFx_.add(x.get(1)+outerBoundary);
//        this.constFx_.add(x.get(0)-outerBoundary);
//        this.constFx_.add(x.get(0)+outerBoundary);
//        //this.constFx_.add(x.get(1) - x.get(0));
//        
//        if(constFx_.size() != this.userInput_.totalConstraints){
//            System.err.println("constFx size is not same as total constraints");
//            Application.getInstance().exit();
//        }
//        
//        for (Double cf : this.constFx_) {
//            if(cf<0){
//                position_.add(-1);
//            }else if (cf>0){
//                position_.add(1);
//            }else{
//                position_.add(0);
//            }            
//        }
//   
//        //for circles
//        if(this.constFx_.get(0)>0){
//            this.violations_.add(1);
//        }
//
//        if(this.constFx_.get(1)>0){
//            this.violations_.add(2);
//        }
//        
//        if(this.constFx_.get(2)>0){
//            this.violations_.add(3);
//        } 
//        
//        
//        //for outer boundaries.
//        if(this.constFx_.get(3)>0){
//            this.violations_.add(4);
//        } 
//        if(this.constFx_.get(4)<0){
//            this.violations_.add(5);
//        }
//        if(this.constFx_.get(5)>0){
//            this.violations_.add(6);
//        }
//        if(this.constFx_.get(6)<0){
//            this.violations_.add(7);
//        } 
//        
//        
////////        if(this.constFx_.get(7)>0){
////////            this.violations_.add(8);
////////        } 
//    }
//    
//    
//    
//    
//    
//    
//    private void objFunction_circumscribedCircle(){
//        this.fitness_ = new ArrayList<Double>();
//        this.tempSortBy = Chromosome.BY_VIOLATIONS; //its a MUST
//
//        this.fitness_.add(this.vals_.get(0));
//        this.fitness_.add((1+this.vals_.get(1))/this.vals_.get(0));
//
//        ArrayList<Double> x = this.vals_;
//        ArrayList<Double> f = this.fitness_;
//
//        this.violations_.clear();
//        this.constFx_.clear();
//        this.position_.clear();
//        
//        //should bes...
//        double br = 4.0;
//        double sr = 0.1;
//        
//        this.constFx_.add(x.get(1)*x.get(1) + x.get(0)*x.get(0)-br*br); //=0
//        this.constFx_.add(x.get(1)*x.get(1) + x.get(0)*x.get(0)-sr*sr); //=0
//        
//        if(constFx_.size() != this.userInput_.totalConstraints){
//            System.err.println("constFx size is not same as total constraints");
//            Application.getInstance().exit();
//        }
//        
//        for (Double cf : this.constFx_) {
//            if(cf<0){
//                position_.add(-1);
//            }else if (cf>0){
//                position_.add(1);
//            }else{
//                position_.add(0);
//            }            
//        }
//   
//        if(this.constFx_.get(0)>0){
//            this.violations_.add(1);
//        }
//
//        if(this.constFx_.get(1)>0){
//            this.violations_.add(2);
//        }
//       
//    //>> deb problem
//    }
//    private void objFunction_deb(){
//    //<< deb problem
//
//        this.fitness_ = new ArrayList<Double>();
//        this.tempSortBy = Chromosome.BY_VIOLATIONS; //its a MUST
//
//        this.fitness_.add(this.vals_.get(0));
//        this.fitness_.add((1+this.vals_.get(1))/this.vals_.get(0));
//
//        ArrayList<Double> x = this.vals_;
//        ArrayList<Double> f = this.fitness_;
//
//        this.violations_.clear();
//        this.constFx_.clear();
//        this.position_.clear();
//        
//        //should bes...
//        this.constFx_.add(x.get(1) + 9*x.get(0)-6.0); //=0
////        this.constFx_.add(x.get(1) + 9*x.get(0)-6.1); //=0
//        this.constFx_.add(-x.get(1) + 9*x.get(0)-1.0); //=0
////        this.constFx_.add(-x.get(1) + 9*x.get(0)-1.1); //=0
//        this.constFx_.add(x.get(0)-1); //=0
//        this.constFx_.add(x.get(0)); //=0
//        
//        if(constFx_.size() != this.userInput_.totalConstraints){
//            System.err.println("constFx size is not same as total constraints");
//            Application.getInstance().exit();
//        }
//        
//        for (Double cf : this.constFx_) {
//            if(cf<0){
//                position_.add(-1);
//            }else if (cf>0){
//                position_.add(1);
//            }else{
//                position_.add(0);
//            }            
//        }
//        
//        //if(x.get(1) + 5.3*x.get(0) < 6){
//        if(this.constFx_.get(0) < 0){
//            this.violations_.add(1);
//        }
//
//        //if(-x.get(1) + 1.8*x.get(0) < 1){
//        if(this.constFx_.get(1) < 0){
//            this.violations_.add(2);
//        }
//
//        if(this.constFx_.get(2) > 0){
//            this.violations_.add(3);
//        }
//        
//        if(this.constFx_.get(3) < 0){
//            this.violations_.add(4);
//        }
//
////        if(this.constFx_.get(0)<0){
////            this.violations_.add(1);
////        }
////
////        if(this.constFx_.get(1)>0){
////            this.violations_.add(2);
////        }
////
////        if(this.constFx_.get(2)<0){
////            this.violations_.add(3);
////        }
////        
////        if(this.constFx_.get(3)>0){
////            this.violations_.add(4);
////        }
//    //>> deb problem
//    }
//    
//    
//     private void objFunction_imod(){
//        this.fitness_ = new ArrayList<Double>();
//        this.tempSortBy = Chromosome.BY_VIOLATIONS; //its a MUST
//
//        this.fitness_.add(this.vals_.get(0));
//        this.fitness_.add((1+this.vals_.get(1))/this.vals_.get(0));
//
//        ArrayList<Double> x = this.vals_;
//        ArrayList<Double> f = this.fitness_;
//
//        this.violations_.clear();
//        this.constFx_.clear();
//        this.position_.clear();  
//        double dt = 0.1;
//        
//        //vars = 8
//        //const = 8
//        
//       
//        
//        this.constFx_.add(x.get(0) - 0.25428722 - 0.18324757*x.get(3)*x.get(2)*x.get(8));
//        this.constFx_.add(x.get(1) - 0.37842197 - 0.16275449*x.get(0)*x.get(9)*x.get(5));
//        this.constFx_.add(x.get(2) - 0.27162577 - 0.16955071*x.get(0)*x.get(1)*x.get(9));
//        this.constFx_.add(x.get(3) - 0.19807914 - 0.15585316*x.get(6)*x.get(0)*x.get(5));
//        this.constFx_.add(x.get(4) - 0.44166728 - 0.19950920*x.get(6)*x.get(5)*x.get(2));
//        this.constFx_.add(x.get(5) - 0.14654113 - 0.18922793*x.get(7)*x.get(4)*x.get(9));
//        this.constFx_.add(x.get(6) - 0.42937161 - 0.21180484*x.get(1)*x.get(4)*x.get(7));
//        this.constFx_.add(x.get(7) - 0.07056438 - 0.17081208*x.get(0)*x.get(6)*x.get(5));
//        this.constFx_.add(x.get(8) - 0.34504906 - 0.19612740*x.get(9)*x.get(5)*x.get(7));
//        this.constFx_.add(x.get(9) - 0.42651102 - 0.21466544*x.get(3)*x.get(7)*x.get(0));
//
//
//
//
//        
//        if(constFx_.size() != this.userInput_.totalConstraints){
//            System.err.println("constFx size is not same as total constraints");
//            Application.getInstance().exit();
//        }
//        
//        for (Double cf : this.constFx_) {
//            if(cf<0){
//                position_.add(-1);
//            }else if (cf>0){
//                position_.add(1);
//            }else{
//                position_.add(0);
//            }            
//        }
//        
//        
//        if(this.constFx_.get(0)>dt || this.constFx_.get(0)< -dt){
//            this.violations_.add(1);
//        }
//
//        if(this.constFx_.get(1)>dt|| this.constFx_.get(1)< -dt){
//            this.violations_.add(2);
//        }
//
//        if(this.constFx_.get(2)> dt || this.constFx_.get(2)<-dt){
//            this.violations_.add(3);
//        }
//        
//        if(this.constFx_.get(3)>dt || this.constFx_.get(3)< -dt){
//            this.violations_.add(4);
//        }
//
//        if(this.constFx_.get(4)>dt|| this.constFx_.get(4)< -dt){
//            this.violations_.add(5);
//        }
//
//        if(this.constFx_.get(5)> dt || this.constFx_.get(5)<-dt){
//            this.violations_.add(6);
//        }
//                
//        if(this.constFx_.get(6)>dt || this.constFx_.get(6)< -dt){
//            this.violations_.add(7);
//        }  
//        
//        if(this.constFx_.get(7)>dt || this.constFx_.get(7)< -dt){
//            this.violations_.add(8);
//        }  
//         
//        if(this.constFx_.get(8)>dt || this.constFx_.get(8)< -dt){
//            this.violations_.add(9);
//        }  
//          
//        if(this.constFx_.get(9)>dt || this.constFx_.get(9)< -dt){
//            this.violations_.add(10);
//        }             
//     }
//       private void objFunction_test(){
//        
//        this.fitness_ = new ArrayList<Double>();
//        this.tempSortBy = Chromosome.BY_VIOLATIONS; //its a MUST
//
//        this.fitness_.add(this.vals_.get(0));
//        this.fitness_.add((1+this.vals_.get(1))/this.vals_.get(0));
//
//        ArrayList<Double> x = this.vals_;
//        ArrayList<Double> f = this.fitness_;
//
//        this.violations_.clear();
//        this.constFx_.clear();
//        this.position_.clear();
//        
//        double best_val_found = 0.2415051288;
//	double eps = 1;
//	double dt = 0.1;
//        double s2 = Math.sqrt(2.0);
//        //var
////        x {1..5} >= 0, <= 4;
//                //cons = 5
//		//var = 5
//        this.constFx_.add( best_val_found + eps - (2*x.get(0)*(x.get(0)-x.get(1)-1) + 1 + Math.pow(x.get(1),2) + Math.pow(x.get(2) - 1, 2) + Math.pow(x.get(3) - 1.0,4.0) + Math.pow(x.get(4) - 1.0,6.0)));
//        this.constFx_.add(Math.pow(x.get(0),2) * x.get(3) + Math.sin(x.get(3) - x.get(4)) - 2.0 * s2); //=0
//        this.constFx_.add(x.get(1) + Math.pow(x.get(2)*1.0,4.0)*Math.pow(x.get(3),2) - 8 - s2); //=0
//        		
//
//        if(constFx_.size() != this.userInput_.totalConstraints){
//            System.err.println("constFx size is not same as total constraints");
//            Application.getInstance().exit();
//        }
//        
//        for (Double cf : this.constFx_) {
//            if(cf<0){
//                position_.add(-1);
//            }else if (cf>0){
//                position_.add(1);
//            }else{
//                position_.add(0);
//            }            
//        }
//
//        
//        if(this.constFx_.get(0)<0){
//            this.violations_.add(1);
//        }
//
//        if(this.constFx_.get(1)>dt|| this.constFx_.get(1)< -dt){
//            this.violations_.add(2);
//        }
//
//        if(this.constFx_.get(2)> dt || this.constFx_.get(2)<-dt){
//            this.violations_.add(3);
//        }
//       
//        
//    }


// </editor-fold>