/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package csp;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
//import java.io.ObjectOutputStream;
import java.io.ObjectOutputStream;
//import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.UnsupportedDataTypeException;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import matlabcontrol.MatlabConnectionException;
import org.jdesktop.application.Application;
//import sun.awt.windows.ThemeReader;
//import org.jfree.data.category.DefaultCategoryDataset;
//import org.jfree.data.xy.XYDataset;
//import org.jfree.data.xy.XYSeries;
//import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author Anurag
 */
public class CspProcess{
    //static boolean abToHoJaFlag;
    public boolean bMatlabDraw;
    private boolean drawStart;
    private volatile ArrayList<Chromosome> chromosomes_; 
    private ArrayList<Chromosome> poolPromisingChromes;
    private ArrayList<Chromosome> poolSelectedChromes;
    private final int poolPromisingChromesSize = 100;
    private final int poolSelectedChromesSize = 20;
//    private Queue<Chromosome> suspended_;
//    private int suspendedRemoved=0;
//    private int suspendSize;
    //public static Chromosome bestSoFar;
    private static Chromosome bestSoFarCSP;
    private static Chromosome bestSoFarCOP;
    private static Chromosome bestSoFarCOP_PREV;
//    public static ArrayList<ArrayList> hammingDistPrevBest;
    public int minDistPrevToNewBestSoFar;
    public double minFitDiffForResolveLocal;
    private static Chromosome curWorstCOP;
    private static Chromosome bestAchievedCOP;
    private double prevBest_;
    private double curBest_;
    private int stillSameBestCount;
//    private ArrayList<Chromosome> solutions_;
//    private ArrayList<ArrayList<Double>> chromeValues;
    public static UserInput userInput_;
    public static ExternalData externalData_;
    //private int population_;
    //private int generation_;
    private int poolSize_;
    private int tourSize_;
    private int knearest_;
    private String dataType_;
    private Double[] range_;
    private final int MAX_MOVES = 10;
    private final int MUM = 20;
    private final int MU = 20;
    private double MUTATION_RATE = 0.1;
    //private final int ARCHIVE_MAX;
    private final double REPLACE_PERCENT = 0.10; //8% PERCENT of chromosomes replaced by new population
    private final double IMMUNITY_PERCENT = 0.10; 
    private final double PARTIAL_SOL_PERCENT = 0.10; //only for saving into file purpose.
    private MyRandom r_;
//    private ArrayList<ArrayList<Double>> sameBestChromeVals_; //stores top ranked SAME_BEST_VAL_PERCENT % of chromosomes
    private int hasAllSame_; //counter to check if the SAME_BEST_VAL_PERCENT % of chromosomes is same for SAME_BEST_GENERATIONS generations
    private final double SAME_BEST_VAL_PERCENT = 0.5; //top ranked percentage of total chrm population
    public int SAME_BEST_GENERATIONS = 5;//50;//12//measure to SAME_BEST_VAL_PERCENT % of top ranked chromosomes remain same for number of generations.
    private int sameBestGen = 0;
    public int maxTransitionGen = 100;//partial optimization before final CSP solution is found looks like VEGA
    public static int NO_PROGRESS_LIMIT;//limit for no progress made in the NO_PROGRESS_LIMIT generations.
    private boolean bStagnant;
    private int stagnantVisit;
    public static double bringCloserRatio = 0.5;
    public static boolean bOptimizationMode;
    public static double maxCSPval;
    public static ArrayList<ArrayList<ArrayList<Double>>> CSPsols;
    private final double FORCED_PERCENT = 0.75;
    private int transitionGen;    
    public static boolean bInTransition = false;
    public static int dynamicConstraintNo;
    public static ArrayList<ArrayList<ArrayList>> dynamicConstraints = new ArrayList<ArrayList<ArrayList>> ();
//    public static ArrayList<ArrayList<Double>> dynamicConstraints = new ArrayList<ArrayList<Double>>();
    public static int MAX_FUNCTIONAL_CONSTRAINTS;
    private double tabuDist;
    static int MaxComb = 1000;//10; //
    private int MaxHospital = 1; //2; //MaxComb/2;
    public static int negFeasibleRange;
    public static final int FIT_DP_LIMIT = 0; //3//6 //we use 3 for 10E-1 and 6 for 10E-3
    private static double curAcceptedConstRatio;
    public double startAcceptedConstRatio = 0.05; //number of constraints considered in each increment (incremtality)
    private int modeChangedGen = -1;
    private static boolean bTabuMode = false;
    private int hyperN;
    private double ro = 20;
    public int RAcommunitySize = 4;//10
    private ArrayList<Chromosome> RAcommoners_; //from reincarnation algorithm (RA)
    private ArrayList<Chromosome> RAgurusLikely; //from reincarnation algorithm (RA)
    private ArrayList<Chromosome> RAgurus;
    public int RAmaxCommonerAge = 10; //???
    public int RAdegreeOfInfluence;
    private final double RAblindTrust = 0.25;
    private Queue<Chromosome> RAsuspendedQueue;
    private int RAcommonerSize;
    private int RAguruSize;    
    private int RAsuspendRate; 
    private static int tabuGens;
    public static int maxTabuGens = 10;
    public final static int chromHistoryLevel = 10;
    private ArrayList<Integer> tmpChangGuruCount;
//    public int mutationID; //initiallized below
    private final int totalMutationTypes = 2; //4; Check corresponding waitRebirth which depends on this value.
    private ArrayList<Chromosome> inFeasiblePopCOP;
//    private ArrayList<Integer> inFeasiblePopCOP_validCurGen;
//    private ArrayList<Chromosome> inFeasiblePopCSP;
    private int maxInfeasiblePop;
    private volatile Thread myThread;
    private final double beta = 0.25;
    public static boolean changeSpaceMode = false;
    private boolean RAgurusReady = false;
    private boolean RAplay = false;
    private boolean resolveLocalOptExternal = false;
    private int resolveLevel = 0;
    private ArrayList<Chromosome> sols;
    private ArrayList<Chromosome> nonSols; 
    private int maxSolPop;
    private int maxNonSolPop;
    private boolean bAlsoGetPrevConstraints = true;
    
    private static final int SORT_HARDCONSVIOS_THEN_FITNESS = 1;
    private static final int SORT_FITNESS_THEN_NOVELTY = 2;
    private static final int SORT_FITNESS = 3;
    private static final int SORT_SATISFACTION = 4;
    public volatile boolean forcePrintSol = false;
    public double rhoCSP = 5.0;
    public double rhoCOP = 2.0;//1.0;
    public double RAfullInfluencePer = 0.6;
    public static int curGen = 0; //generation
    private static int lastBestGen = 0;
    private boolean bStartOffspringStacking = false;
//    private MyStack<Chromosome> offspringStk;
//    private boolean bSolutionFound = false;
    
//    OperatorsLog []operatorsHistory;
//    ArrayList<OperatorsLog> operatorsHistory;
    OperatorsLog opLogPerGen;
    /**
     * stack of <B>sorted</B> {@link CspProcess.#bestSoFarCOP} to track
     * the local optimal solution.
     */
    MyStack<Chromosome> stkBestSoFar = new MyStack<Chromosome>(5);
    
    public static class Debug{
        public static boolean RAcommonerValidity = false;
        public static boolean COPfitDisp = false;
        public static boolean categoryList = false;
    }
    
    private static JTextArea guiJtextAreaDebugPrint;
    
//    public static int hardConstViosTolerance = 10;
    /**
     *
     * @param userInput
     * @throws MyException
     */
    public CspProcess(UserInput userInput) throws MyException{
        //this();
        this.userInput_ = userInput;
        this.externalData_ = null;

        if(userInput_ == null){
            throw new MyException("No user input provided.", "Incorrect Data",JOptionPane.ERROR_MESSAGE);
        }
        
        initialize();
    } // Toavoid calling this constructor

    public static Chromosome getBestSoFarCOP() {
        return bestSoFarCOP;
    }

    public static final Chromosome getCurWorstCOP(){
        return curWorstCOP;
    }
    
    public static Chromosome getBestSoFarCSP() {
        return bestSoFarCSP;
    }

    public static void setBestSoFarCSP(Chromosome bestSoFarCSP) {
        CspProcess.bestSoFarCSP = bestSoFarCSP;
    }
    
    /**
     * Gets the index of the {@link CspProcess#poolSelectedChromes} which is 
     * farthest to the given chromosome {@code c}
     * @param c - a chromosome in question
     * @param bFarthest - true for farthest, false for closest
     * @return 
     */
    private int distFrom(Chromosome c, boolean bFarthest, ArrayList<Chromosome> pool){
        int delta = -1;
        int dist;
        
        if(bFarthest)
            dist = Integer.MAX_VALUE;//minimum
        else
            dist = Integer.MIN_VALUE;
        
        int clIdx = -1;
        //first get its current path.
        int level = (int)(chromHistoryLevel*Math.random());
        ArrayList<ArrayList> satTrackIdx = c.trackCommonPath(level);
        
        int l = 1;
        while (satTrackIdx == null && l<=level){
            c.trackCommonPath(level-l);
            l++;
        }
//        boolean doSatRefresh = false;
//        if(satTrackIdx == null){ //still null
//            satTrackIdx = c.satisfactions_;
//            doSatRefresh = true;
//        }
      
        //now check current path with pool chromes
        for (int i = 0; i < pool.size(); i++) {
            delta = (int)MyMath.norm2D(MyMath.DIST_COMMON_MATCH, satTrackIdx, pool.get(i).satisfactions_, null, -1);  
            pool.get(i).refreshValVsConstIdx();
            
            if(bFarthest){
                if(delta<dist){ //less DIST_COMMON_MATCH - farther
                    dist = delta;
                    clIdx = i;
                }
            }else{
                if(delta>dist){ //more DIST_COMMON_MATCH - closer
                    dist = delta;
                    clIdx = i;
                }
            }
        }
//        if(doSatRefresh)
//            c.refreshValVsConstIdx(); //if using satisfaction...
        return clIdx;
    }
    
    /**
     * make sure to send a clone()
     * @param bestSoFarCOP 
     */
    public static void setBestSoFarCOP(Chromosome bestSoFarCOP) {
        if(bestSoFarCOP.isMorePromisingThanBestCOP()){
            lastBestGen = curGen;
        }
        
        CspProcess.bestSoFarCOP = bestSoFarCOP;       
    }

    
    
    
    public CspProcess(ExternalData externalData)throws MyException{
        this();
        this.externalData_ = externalData;
        this.userInput_ = this.externalData_.getUserInput();

        if(userInput_ == null || this.externalData_ == null){
            throw new MyException("No user input provided or empty external data.", "Incorrect Data",JOptionPane.ERROR_MESSAGE);
        }

        initialize();
    }

    private CspProcess(){
        ;
    }

    /**
     * private Constructor used only for default initialization
     */
    private void initialize() throws MyException{
        //abToHoJaFlag = false;
        bMatlabDraw = false;
        drawStart = false;
        chromosomes_ = new ArrayList<Chromosome>();
        poolPromisingChromes = new ArrayList<Chromosome>();
        poolSelectedChromes = new ArrayList<Chromosome>();
//        suspended_ = new LinkedList<Chromosome>();
//        solutions_ = new ArrayList<Chromosome>();
        this.tourSize_ = 2; //default value assumed
        this.knearest_ = (int)(0.05*userInput_.population); //default value assumed
        this.r_ = new MyRandom();
        
        this.poolSize_ = userInput_.population/2; //default values assumed
        //ARCHIVE_MAX = userInput_.population/2;
        this.dataType_ = this.userInput_.dataType;

        this.range_ = new Double[userInput_.totalDecisionVars];
        for (int i = 0; i < userInput_.totalDecisionVars; i++) {
            this.range_[i] = 0.5; //double assumed.
        }

        if (this.userInput_.population < 5 || this.userInput_.generation < 1){
            throw new MyException("poulation size should be > 5 and generation should be > 1", "Input Data Error!",JOptionPane.ERROR_MESSAGE);
        }
        hasAllSame_ = 0;
//        sameBestChromeVals_ = null;
        bStagnant = false;
        prevBest_ = Double.POSITIVE_INFINITY;
        curBest_ = Double.POSITIVE_INFINITY;
        stillSameBestCount = 0;
        stagnantVisit = 0;
        bOptimizationMode = false;
        maxCSPval = Double.MAX_VALUE;
        MAX_FUNCTIONAL_CONSTRAINTS = userInput_.totalConstraints - userInput_.totalDecisionVars;
        //for dynammicConstrainNo: 0 means incremental and userInput_.totalConstraints - userInput_.totalDecisionVars
        //means concurrent
        dynamicConstraintNo = 0;//userInput_.totalConstraints - userInput_.totalDecisionVars; //0; 
        negFeasibleRange = 0;
        tabuDist = -1.0;
        startAcceptedConstRatio = 5.0/userInput_.totalConstraints;
        curAcceptedConstRatio = startAcceptedConstRatio;
        NO_PROGRESS_LIMIT = 10; //maxTransitionGen; //Math.max(2,(int)(startAcceptedConstRatio*userInput_.totalConstraints)-0);//-1
        
        CSPsols = new ArrayList<ArrayList<ArrayList<Double>>>();
        CSPsols.add(new ArrayList<ArrayList<Double>>());
        for (int i = 0; i < userInput_.totalConstraints; i++) {
            CSPsols.get(0).add(new ArrayList<Double>());
        }
        transitionGen = 0;    
        tabuGens = 0;              
        
        
        RAcommoners_ = new ArrayList<Chromosome>();
        RAgurusLikely = new ArrayList<Chromosome>();
        RAsuspendedQueue = new LinkedList<Chromosome>();
        RAguruSize = (int)(0.1*userInput_.population);
        tmpChangGuruCount = new ArrayList<Integer>();
        
        for (int i = 0; i < RAguruSize; i++) {
            tmpChangGuruCount.add(0);
        }
        RAcommonerSize = RAcommunitySize*RAguruSize; // generally 50% is COP but DON'T change. additional ones would not be taken into account
        RAsuspendRate = (int)(RAcommonerSize*0.1);
//        RAmaxCommonerAge = Math.max(30,userInput_.totalConstraints/2);        
        RAdegreeOfInfluence = 1; //(int)Math.ceil(userInput_.totalDecisionVars*0.2); //hes-5%, sta-10%, ute - 15%, tre etc - 20% 
       
        inFeasiblePopCOP = new ArrayList<Chromosome>();
//        inFeasiblePopCOP_validCurGen = new ArrayList<Integer>();
//        inFeasiblePopCSP = new ArrayList<Chromosome>();
        maxInfeasiblePop = userInput_.population; //RAcommonerSize;//
//        hyperN = (int)(1.0*userInput_.population);//(int)Math.ceil(100.0*userInput_.population/userInput_.totalConstraints);
//        suspendSize = 1000;//(int)(10*userInput_.population);
        
        
        
//        mutationID = 0;
//        if(mutationID<0)
//            switchToNextMutationType();
        
        sols = new ArrayList<Chromosome>();
        nonSols = new ArrayList<Chromosome>();  
        maxSolPop = userInput_.population/2;
        maxNonSolPop = userInput_.population - maxSolPop;
           
        minDistPrevToNewBestSoFar = (int)(0.2*userInput_.totalConstraints);
        minFitDiffForResolveLocal = 100.0;
        RAgurus = new ArrayList<Chromosome>();
//        offspringStk = new MyStack<Chromosome>(userInput_.population/2); //why? why?...
    }   
   
    public enum Operators{
        badOperator, //earlier it was in last place. May affect operatorLog etc.
        interRaceX,
        mutationGroupSwap,        
        mutationSwapCOP, //bad one...
        mutationSinglePoint,
        mutationInterRaceXCOP,
        RAkarma,
        mutationKemp1,
        mutationKemp2,
        mutationKemp3,
        mutationKemp4,
        mutationKemp5,
        crossoverKempe,
        mutationCluster,
        mutationClusterMulti,
        mutationChangeTrack;

        
        //usage:
        //use Operators.values() to get array of all operator elements
        //To print do this:
        //Operators objOperator;
        //assign objOperator = [Operators.your operator];
        //System.out.println(objOperator)
    }
    

    /**
     * Starts the whole process
     */
    public void start(JProgressBar pb, ByRef RArun, boolean saveChromes, ByRef nextPrefSuggestion, Thread inMyThread, JTextArea debugPrint) throws MyException{
        this.myThread = inMyThread;
        this.guiJtextAreaDebugPrint = debugPrint;
        RArun.setValue(this.RAplay);
        ArrayList<Chromosome> parents;
        ArrayList<Chromosome> offspring;
        ArrayList<Chromosome> temp;
        double startTime = 0.0;
        double endTime = 0.0;
        int totalSaved;
        Draw d = null;  
        
        PrintWriter runHistory = null;
        PrintWriter debugOut = null;
        PrintWriter printOpHistory = null;
        
        boolean bAnalsis = true;
        
        
        try{
            File directory = new File ( "./Test Results RCHC" ) ;
            File OpDirectory = new File ( "./Operator History" ) ;

            File [ ] filesInDir = directory.listFiles(
                new FileFilter() {
                    public boolean accept(File pathname) {
                        return (pathname.getName().startsWith(userInput_.chosenFile+"-") 
                                && pathname.getName().endsWith("_history.csv"));
                    }
                }
            );
        
            File [ ] OpFilesInDir = OpDirectory.listFiles(
                new FileFilter() {
                    public boolean accept(File pathname) {
                        return (pathname.getName().startsWith("OpHistory_"+userInput_.chosenFile+"-") 
                                && pathname.getName().endsWith("_history.csv"));
                    }
                }
            );
        
            
        
            int fileNo = 1;
            
            if(filesInDir.length>0){
                fileNo = filesInDir.length+1;
            }
            
            
//            runHistory = new PrintWriter("FinalResult.csv");
            debugOut = new PrintWriter("debugOut.txt");
            
            runHistory = new PrintWriter(directory.getAbsolutePath() + "/"+userInput_.chosenFile+ "-"+fileNo+"_history.csv");
          
            String opFilePath= OpDirectory.getAbsolutePath() + "/OpHistory_"+userInput_.chosenFile+ "-"+fileNo+"_history.csv";
            System.out.println("Operator file saved as: ");
            System.out.println(opFilePath);
            printOpHistory = new PrintWriter(opFilePath);
            printOpHistory.println(OperatorsLog.getColumnsOrder());
            
            if(bMatlabDraw){
                d = new Draw();                
//                d.draw(matlabPlotBuildConstraints());  
            }                         
            
            initializeChromosome(this.chromosomes_, userInput_.population, curGen);
            setBestSoFarCSP((Chromosome)this.chromosomes_.get(0).clone());
            setBestSoFarCOP((Chromosome)this.chromosomes_.get(0).clone());
            bestSoFarCOP.overrideBestSoFarFitness(Double.MAX_VALUE);
            bestAchievedCOP = (Chromosome)this.bestSoFarCOP.clone(); 
            bestSoFarCOP_PREV = (Chromosome)this.bestSoFarCOP.clone(); 
  
            startTime = System.nanoTime();
            startTime = startTime/Math.pow(10, 9);
//            operatorsHistory = new OperatorsLog[userInput_.generation]; // what if generation changes in GUI??
//            operatorsHistory = new ArrayList<OperatorsLog>();
            
            final int totalOperationGrp = Operators.values().length;
            int usedOpTotal = totalOperationGrp;
            boolean bUseBestOps = false;
            
            Element measureOpSuccess[] = new Element[totalOperationGrp];           
            for (int i = 0; i < measureOpSuccess.length; i++) {
                measureOpSuccess[i] = new Element((int)0, i);                
            }
            
            int opOrder[] = new int[totalOperationGrp];
            for (int i = 0; i < opOrder.length; i++) {
                opOrder[i] = i;                            
            }
            
            Operators theOperator = Operators.badOperator;
            double perChange = 0.0;
            lastBestGen = Integer.MAX_VALUE;
            int curMut=0;
            for (curGen = 1; curGen <= userInput_.generation; curGen++) { 
                //don't need to immediately interrupt the loop                        
                if (myThread.isInterrupted()){                        
                    Thread.interrupted();
                    System.out.println("start up process interrupted...");
                        
                    synchronized(myThread){     
                        System.out.println("about to call wait() for start thread. " +myThread.getState());                            
                        myThread.wait();    
                        System.out.println("wait finished for start thread" +myThread.getState());   
                        System.out.println("RESUME start...");
                    }
                }
                
                opLogPerGen = new OperatorsLog();
                opLogPerGen.gen = curGen;
                opLogPerGen.dynamicConstraintsB4 = "["+bestSoFarCOP.getVals().size()+"/" + userInput_.total__updatedConstraints+"]";
                
                if(getBestSoFarCOP().isSolution()){
                    maxSolPop = (int)(userInput_.population*0.9);
                    maxNonSolPop = userInput_.population - maxSolPop;
                    bAlsoGetPrevConstraints = true;
                }else{
                    bAlsoGetPrevConstraints = false;
                }
                
                
                //check if the cycle is over
//                theOperator = Operators.values()[measureOpSuccess[curMut].idx];
                theOperator = Operators.values()[opOrder[curMut]];
                if(theOperator == Operators.badOperator){// || curMut >= usedOpTotal){ //just last operator
                    if(!bUseBestOps) {               
                        Arrays.sort(measureOpSuccess,Collections.reverseOrder());
                        bUseBestOps = true;
                        usedOpTotal = totalOperationGrp/4;
                        System.out.println("****** In Filtered Operation Mode *******");                       
                        for (int i = 0; i < usedOpTotal; i++) {
                            System.out.println("\t" + Operators.values()[measureOpSuccess[i].idx] + ": " + measureOpSuccess[i].val); 
                        }
                        usedOpTotal++; //for bad operator
                        opOrder[0]=0; // first one is bad operator
                        for (int i = 1; i < usedOpTotal; i++) { //rest of the order remains dirty but not in use. So its ok.
                            opOrder[i] = measureOpSuccess[i-1].idx; 
                            if(opOrder[i] == 0){
                                usedOpTotal = i;
                                break;
                            }                            
                        }
                    }else{                    
                        //Should change the order as in SGD....
                        System.out.println("****** In Normal Operation Mode *******"); 
                        measureOpSuccess = new Element[totalOperationGrp]; 
                        for (int i = 0; i < measureOpSuccess.length; i++) {
                            measureOpSuccess[i] = new Element((int)0, i);                
                        }
                        
                        usedOpTotal = totalOperationGrp;  
                        bUseBestOps = false;
                        
//                        for (int i = 0; i < opOrder.length; i++) {
//                            opOrder[i] = i;                            
//                        }
                        ArrayList<Integer> rndOrder =  MyRandom.randperm(0, opOrder.length);
                        for (int i = 0; i < opOrder.length; i++) {
                            opOrder[i] = rndOrder.get(i);                            
                        }
                        
                    }
                    curMut = 1; //valid ones start from 1
//                    theOperator = Operators.values()[measureOpSuccess[curMut].idx];
                    theOperator = Operators.values()[opOrder[curMut]];

                    //curGen??
                    continue;
                }else{ 
                    
                    //if not improved.
                    System.out.println("%changed: " + perChange);
                    if(lastBestGen < curGen-1){ //perChange < MyMath.expProbablity(curGen, userInput_.generation)){// (1 - MyMath.expProbablity(curGen, userInput_.generation))/2) {//////////change it to only change in sols..................
                        if(bUseBestOps){
                            if(curGen%10 == 0){
                                curMut++;                                
                            }
                        }else{
                            curMut++;                            
                        }                        
                    }else{
                        measureOpSuccess[opOrder[curMut]].val++; // only for Normal Operation
                    }
                    
                    if(curMut >= usedOpTotal){
////                        bUseBestOps = true;
                        curMut = 0; //bad operator to start with.
                        continue;
                    }
                }

                
                
//                for (int i = 0; i < eliteSize; i++) {
//                    elite.add((Chromosome)chromosomes_.get(i).clone());
//                }                
//                suspendedRemoved = 0;
//                CSPsolsDB = new ArrayList<Chromosome>(); 
                
                if(externalData_ != null){        
                    //This is for group swap
                    //This is similar to CLONALG...using hMutes then pattern matching.
//                    totalOperationGrp = Operators.values().length;
//                    int curMut; // = curGen%totalOperationGrp; //+1 is to avoid interraceX call which is not a mutation
//                        int id = 0;

//                    theOperator = Operators.values()[curMut%totalOperationGrp];
                    
                    //ignore following operators
//                    while(theOperator == Operators.badOperator
////                            || theOperator == Operators.interRaceX
//////                            || theOperator == Operators.RAkarma
//////                            || theOperator == Operators.mutationSinglePoint
//////                            || theOperator == Operators.mutationGroupSwap
////                            || theOperator == Operators.mutationKemp1
////                            || theOperator == Operators.mutationKemp3
//////                            || theOperator == Operators.mutationChangeTrack
//                            ){
//                        curMut++;
//                        theOperator = Operators.values()[curMut%totalOperationGrp];
//                    }                    
                           
////                    mutation(Operators.RAkarma, true);
                    perChange = mutation(chromosomes_, true, theOperator, true);
                    mutation(inFeasiblePopCOP,false, theOperator, true);
//                    curMut++;                
                }
                                
//                    
//                    // <editor-fold defaultstate="collapsed" desc="PMX for NQUEEN Code">
//                    //<<PMX one...
//                        final int popSize = userInput_.population;
//                        final int queenNum = userInput_.totalConstraints;
//                        double fitValProp[] = new double[popSize];
//                        
//        
//                        
//                        double fitTot = 0;
//                        for (int jR = 0; jR < popSize; jR++) {
//                            fitTot += ((queenNum * (queenNum - 1)) / 2 ) - chromosomes_.get(jR).getFitnessVal(0);                            
//                        }
//                        
//                        for (int jR = 0; jR < popSize; jR++) {
//                            fitValProp[jR] = (((queenNum * (queenNum - 1)) / 2 ) - chromosomes_.get(jR).getFitnessVal(0))/fitTot;                            
//                        }
//                        
//
//                        //int[][] matingPool = new int [popSize][queenNum];
//                        ArrayList<Chromosome> matingPool = new ArrayList<Chromosome>();
//                        int iL = 0;
//                        boolean done = false;
//                        while (done == false){
//                            double mother = Math.random() * popSize;
//                            int mothersPosition = 0;
//                            double father = Math.random() * popSize;
//                            int fathersPosition = 0;
//                            if(done == false){
//                                for(int jR = 0; jR < popSize; jR++){
//                                    if ((mother > fitValProp[jR])){
//                                        mothersPosition = jR;
//                                        //for (int l = 0; l < queenNum; l++){
//                                            //matingPool[iL][l] = queenPop[mothersPosition][l];
//                                            matingPool.add((Chromosome)chromosomes_.get(mothersPosition).clone());
//                                        //}
//                                        jR = popSize; //use break yaar...
//                                        iL++;
//                                    }
//                                }
//                            }
//                            if(iL >= popSize)
//                                done = true;
//                            if(done == false){
//                                for (int k = 0; k < popSize; k++){
//                                    if ((father > fitValProp[k])){
//                                        fathersPosition = k;
//                                        if(fathersPosition == mothersPosition && mothersPosition != 0){
//                                            fathersPosition = 0;
//                                        }
//                                        if (fathersPosition == mothersPosition && mothersPosition == 0 ){
//                                            fathersPosition = 1;
//                                        }
//                //                        for (int m = 0; m < queenNum; m++){
//                //                            matingPool[iL][m] = queenPop[fathersPosition][m];
//                //                        }
//                                        matingPool.add((Chromosome)chromosomes_.get(fathersPosition).clone());
//                                        k = popSize;
//                                        iL++;
//                                    }
//                                }
//                            }
//                            if(iL >= popSize)
//                                done = true;
//                        //System.out.println("Mom is: " + mothersPosition + "Dad is: " + fathersPosition);
//                        }
//                        chromosomes_.clear();
//                        parents.clear();
//                        for(iL = 0; iL < popSize; iL++){
//                            //for(int jR = 0; jR < queenNum; jR++){
//                                //queenPop[iL][jR] = matingPool[iL][jR];
//                           // }
//                            parents.add(matingPool.get(iL));
//                        }   
//                        
//                        
//                    //>>PMX done....
//                    // </editor-fold>
//              
//                if(getBestSoFarCOP().isSolution())
                    RHCH_removal();//RHCH_removal(false);
                                
                
                double bestB4process;
                double diffFit;
                double diffFitSum;
                int AffectedChrome;
                int improvmentCount;
                double diversity;
        
                bestB4process = getBestSoFarCSP().getFitnessVal(0);
                diffFit = 0;
                diffFitSum = 0;
                improvmentCount = 0;
                AffectedChrome = 0;
                
                
////                     //<<option 2 
//////                    parents = noveltyTournamentSelection(chromosomes_); //select best parents only. very slow progress...
                    parents = chromosomes_; //new ArrayList<Chromosome>(chromosomes_); // no need to make clone....
                    offspring = new ArrayList<Chromosome>();  
                    if(bStagnant/* bestSoFarCOP.isPartialSolution()*/){ //sols.size() < 0.10*maxSolPop && !                       
                        offspring.addAll(interRaceCrossover(parents,true, true));
                    }else{
                        offspring.addAll(interRaceCrossover(parents,false, true));
                    }                
                    //Who am I is not working here... can't even get full csp solution
                    //code removed
                    
                    Chromosome of,p;
                    int parentWho;
                    AffectedChrome = parents.size();
                    for (int i = 0; i < offspring.size(); i++) {
                        of = offspring.get(i);
                        parentWho = of.whoAmI-userInput_.population;
                        if(parentWho<0){
                            continue;//unknown parent. Forced marriage.
                        }
                        p = chromosomes_.get(parentWho);
                        if(of.isMorePromisingThan(p)){
                            if(bAnalsis){
                                diffFit = Math.abs(Math.abs(of.getFitnessVal(0)) - Math.abs(p.getFitnessVal(0)));
                                diffFitSum += diffFit; // This is wrong. Why diff fitness. Should be diff position.
                                improvmentCount++;
                            }                            
                            chromosomes_.set(parentWho, of);
//                            enqueueSuspendedIfQualified(of, p);
                        }
                    }
                    
                    if(bAnalsis){
                        diversity = MyMath.stdDev(getAllChromesCOPfintess()); //wrong. should be based on loc not fitness
                        opLogPerGen.addDataForOpCall(Operators.interRaceX, diffFitSum, AffectedChrome, improvmentCount, bestB4process, diversity);
                    }

                    parents = new ArrayList<Chromosome>();//no longer needed 
                    parents.trimToSize();  
                    
                    //<<crossover for COP
                    parents = chromosomes_; //new ArrayList<Chromosome>(chromosomes_); // no need to make clone....
                    offspring = new ArrayList<Chromosome>();                      
                    offspring.addAll(interRaceCrossover(parents,false, true));
                        
                    inFeasiblePopCOP.addAll(offspring);
                    Chromosome.tmpSortBy = Chromosome.BY_FITNESS;
                    Collections.sort(inFeasiblePopCOP);
                    
                    inFeasiblePopCOP = new ArrayList<Chromosome>(inFeasiblePopCOP.subList(0, Math.min(inFeasiblePopCOP.size(), maxInfeasiblePop)));
//                    final double maxVal = Collections.max(inFeasiblePopCOP).getFitnessVal(0);
//                    final double minVal = Collections.min(inFeasiblePopCOP).getFitnessVal(0);
//
//                    inFeasiblePopCOP = categorizeChromesList(inFeasiblePopCOP, Math.min(inFeasiblePopCOP.size(), 
//                        maxInfeasiblePop), minVal, maxVal, SORT_HARDCONSVIOS_THEN_FITNESS,rhoCOP, null, Debug.categoryList);                                                                                   
                    double curC, nextC;
                    for (int i = 0; i < inFeasiblePopCOP.size()-1; i++) {
                        curC = inFeasiblePopCOP.get(i).getFunctionalVal();
                        nextC = inFeasiblePopCOP.get(i+1).getFunctionalVal();
                        if(MyMath.roundN(curC,4) == MyMath.roundN(nextC,4)){
                            inFeasiblePopCOP.remove(i+1);
                            i--;
                        }
                    }
                    
//////                    for (int i = 0; i < inFeasiblePopCOP.size(); i++) {
//////                        if(!inFeasiblePopCOP.get(i).isPartialSolution()){
//////                            inFeasiblePopCOP.remove(i);
//////                            i--;
//////                        }                    
//////                    }
                    
                    inFeasiblePopCOP.trimToSize();   
                    
                   
//////                    chromosomes_.addAll(inFeasiblePopCOP);
                    
                    parents = new ArrayList<Chromosome>();//no longer needed 
                    parents.trimToSize();
                    offspring = new ArrayList<Chromosome>();
                    offspring.trimToSize();
                    //>>
                                
                
                temp = new ArrayList<Chromosome>();
                cleanChromes();//trial 17july2017
                
                initializeChromosome(temp, userInput_.population-chromosomes_.size(), curGen);
                chromosomes_.addAll(temp);
                
                sortAndReplace(curGen);//sort according to least violation first then on ro value (novelty)    

                
//                if(!bSolutionFound && getBestSoFarCOP().isSolution()){
////                    bSolutionFound = true;  
////                    suspended_.clear();
//                }                
                chromosomes_.trimToSize();
//                inFeasiblePopCOP_validCurGen = new ArrayList<Integer>();
//                inFeasiblePopCOP = new ArrayList<Chromosome>();
//                inFeasiblePopCOP.trimToSize();
                RAgurus = new ArrayList<Chromosome>();
                RAgurus.trimToSize();
                offspring = new ArrayList<Chromosome>();
                
//                CSPsolsDB = new ArrayList<Chromosome>();
//                CSPsolsDB.trimToSize();                
                System.gc();                
//                gc();                                
                
                if(bestAchievedCOP.getFitnessVal(0)>bestSoFarCOP.getFitnessVal(0)){
                    bestAchievedCOP = (Chromosome)bestSoFarCOP.clone();
                }   
                
                //<<--- hamming dist set up/updates
//                double dist;
//                double fitDiff;
//                ArrayList<Integer> v1 = new ArrayList<Integer>();
//                ArrayList<Integer> v2 = new ArrayList<Integer>();
//                for (int i = 0; i < bestSoFarCOP.satisfactions_.size(); i++) {
//                    v1.addAll(bestSoFarCOP.satisfactions_.get(i));
//                }
//                for (int i = 0; i < bestSoFarCOP_PREV.satisfactions_.size(); i++) {
//                    v2.addAll(bestSoFarCOP_PREV.satisfactions_.get(i));
//                }
//                dist = MyMath.norm(v1, v2, MyMath.DIST_HAMMING_ABSOLUTE);
//                fitDiff = Math.abs(bestSoFarCOP.getFitnessVal(0)-bestSoFarCOP_PREV.getFitnessVal(0));
//                
//                if(bestSoFarCOP_PREV.getVals().size() != bestSoFarCOP.getVals().size()){
//                    bestSoFarCOP_PREV = (Chromosome)bestSoFarCOP.clone();
//                    stkBestSoFar.forcePushByDequeue(bestSoFarCOP);                    
//                }
//                if(dist>=minDistPrevToNewBestSoFar){                       
//                    hammingDistPrevBest = retrieveHammingDist(0);                    
//                    if(fitDiff>25){ ///??????????????????????????????????????????????????/generalize man.....
//                        stkBestSoFar.forcePushByDequeue(bestSoFarCOP_PREV);//should have put bestSoFarCOP but putting the clone(). Its safer
//                    }
//                }
//                //>>----
                
                
                double val = bestSoFarCOP.getFitnessVal(0);
                if(bestSoFarCOP.isSolution())
                    val = bestSoFarCOP.getFunctionalVal();
                System.out.println();
//                System.out.println("cur accepted constraints: "+getCurAcceptedConstraints(userInput_.totalConstraints));
                System.out.println(curGen+",best CSP: "+ bestSoFarCSP.getFitnessVal(0) + ", curConsts: (" + bestSoFarCSP.getVals().size() + "/" +  userInput_.total__updatedConstraints + ")");                    
                System.out.println(curGen+",best COP: "+ bestSoFarCOP.getFunctionalVal() + 
                        ", hard vios: " + bestSoFarCOP.getFitnessVal(1)+", curConsts: (" + 
                        bestSoFarCOP.getVals().size()+"/" + userInput_.total__updatedConstraints + ") ");
//                        "Transittion Gen: (" + transitionGen + "/"+maxTransitionGen+")");
          
                System.out.println("Pool Size: " + poolPromisingChromes.size());
                System.out.println("Selected Pool Size: " + poolSelectedChromes.size());
                
                System.out.println("Chromes backtrack history: ");
                for (Chromosome c : chromosomes_.subList(0, 10)) {
                    System.out.print(c.curHistorySize()+", ");
                }
                System.out.println();
                
                if(curGen%100==0){
                    System.out.println("Chrome history ");
                    for (Chromosome c : chromosomes_.subList(0, 10)) {
                        c.printHistory();
                    }
                }
                
                System.out.println("CSP solutions: ");
                int tmp = 0;
                for (Chromosome c : chromosomes_) {
                    if(!c.isPartialSolution()){
                        tmp++;
                        System.out.print(c.vals_.size());
                        if(c.vals_.size()==1)
                            System.out.print(" [" + c.vals_.get(0)+"] ");
                        System.out.print(", ");
                    }
                    if(tmp>=10){
                        break;
                    }
                }
                System.out.println("");
//                System.out.println("suspended size: "+ suspended_.size() + ", removed: " + suspendedRemoved);
//                System.out.print(curGen+",prev Bests: ");
//                for (int i = 0; i < stkBestSoFar.size(); i++) {
//                    System.out.print(stkBestSoFar.forceGet(i).getFitnessVal(0)+", ");
//                }
//                System.out.println("");
//                System.out.println(curGen+",best COP fn: "+ val + ", curConsts: " + userInput_.total__updatedConstraints);
                System.out.println("cur mutation type: "+theOperator);
                
                for (int z = 0; z < 10; z++) {
                //System.out.print(new DecimalFormat  ("#.##").format(chromosomes_.get(iL).getFitnessVal(0)) +", ");
                    System.out.print(MyMath.roundN(chromosomes_.get(z).getFunctionalVal(),4)+ "["+ chromosomes_.get(z).getVals().size() + "], ");                               
                }                 
                
                System.out.println("");
                System.out.println("sols: " + sols.size() + "/" + maxSolPop + ", non sols: " + nonSols.size() + "/"+maxNonSolPop);
                
                System.out.print("infeasibleCOP: " + inFeasiblePopCOP.size()+ " || ");                
                int mn = Math.min(10, inFeasiblePopCOP.size());
                for (int i = 0; i < mn; i++) {
                    System.out.print(MyMath.roundN(inFeasiblePopCOP.get(i).getFunctionalVal(),4)+", ");
                }
                System.out.println("");
                
//                if(!RAgurus.isEmpty())
//                    System.out.print("RA guru: "+ RAgurus.get(0).getFitnessVal(0)+", ");
//                System.out.println("transitionGen: "+transitionGen);
//                System.out.println("tmpChangGuruCount: "+tmpChangGuruCount);
//                System.out.println("current tolerance for hard constraint violation: "+userInput_.hardConstViosTolerance);
//                    //                    runHistory.println(g+","+bestSoFar.getFitnessVal(0)+"," + userInput_.total__updatedConstraints);
                   
                endTime = System.nanoTime();
                endTime = endTime/Math.pow(10, 9);
                endTime = MyMath.roundN(endTime - startTime,2);
                    
                if(opLogPerGen.dataSize()==0)//NO operator called.
                    opLogPerGen.addDataForOpCall();
                
                
//                System.out.println("chromosomes_: "+ chromosomes_.size());
//                System.out.println("suspended_: " + suspended_.size());
//                System.out.println("dynamicConstraints: " + dynamicConstraints.size());
//                System.out.println("RAcommoners_: " + RAcommoners_.size());
//                System.out.println("RAgurusLikely: " + RAgurusLikely.size());
//                System.out.println("RAgurus: " + RAgurus.size());
//                System.out.println("tmpChangGuruCount: " + tmpChangGuruCount.size());
//                System.out.println("operatorsHistory: " + operatorsHistory.size());
//                
                
                
                
                
                
//                operatorsHistory[curGen-1] = opLogPerGen;
//                operatorsHistory.add(opLogPerGen);
//                for (int i = 0; i < operatorsHistory.size(); i++) {
//                    printOpHistory.println(operatorsHistory.get(i));
//                }
                printOpHistory.println(opLogPerGen);           
                System.out.println("processing time: "+endTime+" Sec");
                    
                  
//                System.out.println("<<all chromes..........>>");
//                for (Chromosome c : chromosomes_) {
//                    System.out.print("<"+c.getFitnessVal(0).intValue()+","+c.getVals().size()+">, ");
//                }
//                System.out.println("\n<<.....................>>");
                
     // <editor-fold defaultstate="collapsed" desc="MEMORY TEST (commented out)">  
                
////                <<MEMORY TEST<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
//
//                System.out.println("chrom_ " + chromosomes_.size());
//                System.out.println("suspended: " + suspended_.size());
//                
//                int sz = 0;
//                if(hammingDistPrevBest != null){
//                    for (int i = 0; i < hammingDistPrevBest.size(); i++) {
//                        sz+=hammingDistPrevBest.get(i).size();
//                    }
//                    System.out.println("hammingDist: " + sz);
//                }
//
//                sz = 0;
//                if(chromeValues !=null){
//                    for (int i = 0; i < chromeValues.size(); i++) {
//                        sz+= chromeValues.size();
//                    }
//                    System.out.println("chromevalues: " + sz);
//                }
//                
//                System.out.println("range_: " + range_.length);
//                
//                if(sameBestChromeVals_ != null){
//                    sz = 0;
//                    for (int i = 0; i < sameBestChromeVals_.size(); i++) {
//                        sz+= sameBestChromeVals_.size();
//                    }
//                    System.out.println("samebestchromval: " + sz);
//                }
//                
//                sz = 0;
//                for (int i = 0; i < CSPsols.size(); i++) {
//                    for (int j = 0; j < CSPsols.get(i).size(); j++) {
//                        sz+= CSPsols.get(i).get(j).size();
//                    }
//                    
//                }
//                System.out.println("CSPsols: " + sz);
//                
//    
//                sz = 0;
//                for (int i = 0; i < dynamicConstraints.size(); i++) {
//                    for (int j = 0; j < dynamicConstraints.get(i).size(); j++) {
//                        sz+= dynamicConstraints.get(i).get(j).size();
//                    }
//                    
//                }
//                System.out.println("dynamicConstraints: " + sz);
//                
//                System.out.println("RAcommoner: " + RAcommoners_.size());
//                
//                System.out.println("RAgurusLIkely: " + RAgurusLikely.size());
//
//                System.out.println("RAsuspendedQu: " + RAsuspendedQueue.size());
//    
//                System.out.println("tmpChangeGurucount: " + tmpChangGuruCount.size());
//    
//                System.out.println("infeasiblepopCOP: " + inFeasiblePopCOP.size());
//                
//                System.out.println("infeasible valid: " + inFeasiblePopCOP_validCurGen.size());
//
//                System.out.println("sols: " + sols.size());
//                
//                System.out.println("nonsols: " + nonSols.size());
//
//                System.out.println("parents: " + parents.size()); 
//                System.out.println("offspring: " + offspring.size());
//                System.out.println("temp: " + temp.size());
////                System.out.println("cspsolsDB: " + CSPsolsDB.size());
//                System.out.println("RAgurus: " + RAgurus.size());
//                System.out.println("elite: " + elite.size());
//                System.out.println("v1: " + v1.size());
//                System.out.println("v2: "+ v2.size());
//                
                //>>MEMORY TEST>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
                   
                //</editor-fold>
                
//                if(curGen%1000 == 0 & bestSoFarCOP.isSolution()){
//                    resolveLocalOpt(1);
//                }                
                if(forcePrintSol){
                    forcePrintSol = false;
                    printBasicFinalResults(startTime);
                }
                        
//                    System.out.println("Gen: "+g); //+"best rank: " + bestSoFar.getRank() 
//                            + ", fitness: "+ bestSoFar.getFitnessVal(0)
//                            + ", rank list: " + bestSoFar.getSatisfaction() + ", vals: " + bestSoFar.getValsCopy());
//                    bestSoFar.tempSortBy = userInput_.solutionBy;
//                    System.out.println(bestSoFar);
                    
                if(Debug.COPfitDisp){
                    double valFrom;
                    String msg = "COPFIT Gen(" + curGen+ "): ";
                    for (int z = 0; z < userInput_.population/2; z++) {
                        valFrom = chromosomes_.get(z).getFitnessVal(0);
                        if(bOptimizationMode)
                            valFrom = chromosomes_.get(z).getFunctionalVal();;
                        //System.out.print(new DecimalFormat  ("#.##").format(chromosomes_.get(iL).getFitnessVal(0)) +", ");
                        msg +=MyMath.roundN(valFrom,2)+", "; //+" - " + chromosomes_.get(iL).getRankComponents().size()+", ");// +"["+ chromosomes_.get(iL).vals_.get(0) + "], ");                                   
                    } 
                    printGuiDebug(msg);
                }

                
                if(bMatlabDraw){
                    if(d == null)
                        d = new Draw(); 
                    d.draw(matlabPlotBuildGeneration(curGen));
                }
                
                System.out.flush();
                
                if(pb != null)
                    pb.setValue(pb.getMinimum()+(pb.getMaximum()-pb.getMinimum())*curGen/(userInput_.generation));              
            }            
        }catch(OutOfMemoryError ome){
            ome.printStackTrace();
        }catch(SolutionFoundException SFE){
            System.out.println("\nSolution found at generation " + (curGen));
            System.out.println("Reason: " + SFE.getMessage());
            runHistory.println("\n\nSolution found at generation " + (curGen));
            runHistory.println("Reason: " + SFE.getMessage());
            
            try {
                if(bMatlabDraw){
                    if(d == null)
                        d = new Draw(); 
                    d.draw(matlabPlotBuildGeneration(curGen));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            curGen = userInput_.generation;
            pb.setValue(pb.getMinimum()+(pb.getMaximum()-pb.getMinimum())*curGen/(userInput_.generation));            
        }catch(MyException me){
            me.showMessageBox();
        }catch(UnsupportedDataTypeException udte){
            udte.printStackTrace();
        }catch (MatlabConnectionException mce) {
            mce.printStackTrace();           
        }
        catch(Exception e){
            e.printStackTrace();
            throw new MyException("Exception raised in Start Process", "Check Start Process()",JOptionPane.ERROR_MESSAGE);           
        }finally{
            debugOut.close();
            
////            bestSoFarCOP = bestAchievedCOP;
////            endTime = System.nanoTime();
////            endTime = endTime/Math.pow(10, 9);
////            System.out.println("\nProcess time(Sec): " + MyMath.roundN(endTime - startTime,2));
////            System.out.println("total chromosomes: "+chromosomes_.size());
////            int evals = 0;
////            Chromosome.totalAppEvals = Chromosome.totalAppEvals/userInput_.totalConstraints;// equal sized chromes
////            Chromosome.totalAppEvals = 2*Chromosome.totalAppEvals/(userInput_.totalConstraints+1); //each ICHEA eval is (N+1)/2 times faster than others, hence divided. ICHEA N -> Ohters N*(N+1)/2            
////            Chromosome.totalRefEvals = Chromosome.totalRefEvals/userInput_.totalConstraints;// equal sized chromes            
////            evals = Chromosome.totalAppEvals + Chromosome.totalRefEvals;
////            
////            System.out.println("Total Evaluations: "+ evals);
////            

////            
////            System.err.flush();
////            System.out.flush();
            
            printBasicFinalResults(startTime);

            for (int z = 0; z < userInput_.population; z++) {
                //System.out.print(new DecimalFormat  ("#.##").format(chromosomes_.get(iL).getFitnessVal(0)) +", ");
                System.out.print(MyMath.roundN(chromosomes_.get(z).getFunctionalVal(),4)+", "); //+" - " + chromosomes_.get(iL).getRankComponents().size()+", ");// +"["+ chromosomes_.get(iL).vals_.get(0) + "], ");           
            }            

//            setSolution(); //USAGE????
            
            endTime = System.nanoTime();
            endTime = endTime/Math.pow(10, 9);
            runHistory.println("Process time(Sec): " + MyMath.roundN(endTime - startTime,2));
            runHistory.println("total chromosomes: "+chromosomes_.size());
            runHistory.println("Total Evaluations: "+ getTotalEvals());
            
            if(bestSoFarCOP.isSolution()){//this.solutions_.isEmpty()){                            
//                System.out.println("No Solution Found :( ****************");                
//                System.out.println("best chromosomes\n" + bestSoFarCOP);
                runHistory.println("No Solution Found :( ****************"); 
                runHistory.println("\nbest chromosomes\n" + bestSoFarCOP);
//                System.out.println("val1 size: "+bestSoFar.getValsCopy().size());
            }else{
//                System.out.println("Solution Found");                
//                System.out.println("best chromosomes\n" + bestSoFarCOP);
                runHistory.println("\n\nSolution Found"); 
                runHistory.println("\nbest chromosomes\n" + bestSoFarCOP);
            }  
            
//            if(externalData_ != null){
//                externalData_.printProblemSpecificSol(bestSoFarCOP.satisfactions_);
//            }
            
            runHistory.close();                                    
            printOpHistory.close();
            
            if(externalData_ != null){ //external data is used.  
                nextPrefSuggestion.setValue(String.valueOf(externalData_.getNextPrefLimit()));
                String fileName = "partial_solutions_pref_"+externalData_.getCurPref()+".ichea";
                try {
                    FileOutputStream fos;
                    fos = new FileOutputStream(fileName);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);

                    Set<Chromosome> s = new LinkedHashSet<Chromosome>(chromosomes_);
                    chromosomes_ = new ArrayList<Chromosome>(s); 
                    totalSaved = Math.min(chromosomes_.size(),(int)(PARTIAL_SOL_PERCENT*userInput_.population));
                    
                    chromosomes_ = new ArrayList<Chromosome>(chromosomes_.subList(0, totalSaved));
//                    chromosomes_.add(0, bestSoFarCOP); //nope..... refresing below will cause exception
                    for (Chromosome c : chromosomes_) {
                        try {
                            c.refreshFitness();
                        } catch (SolutionFoundException ex) {
                            ;
                        }
                    }
                    
                    oos.writeObject(chromosomes_);//chromosomes_.subList(0, (int)(PARTIAL_SOL_PERCENT*userInput_.population)));
                    oos.flush();
                    oos.close();
                    System.out.println("["+ totalSaved + "] chromosomes of data successfully Saved to File ["+fileName+"].");
                } catch (FileNotFoundException fnfe) {
                    System.err.println("Serialize Error! File cannot be created.");
                } catch (IOException ioe){
                    ioe.printStackTrace();
                    System.err.println("Serialize Error! Cannot write to the file ["+fileName+"].");
                }                
            }
        }    
    }
    
    
    /**
     * only applicable to solutions.
     */
    
    private void cleanChromes(){        
        Chromosome.tmpSortBy = Chromosome.BY_FITNESS;
        Collections.sort(chromosomes_);//good one first, bad ones later.

        ArrayList<Element> gap = new ArrayList<Element>();
          
        if(chromosomes_.get(0).isSolution() && Math.random()<MyMath.expProbablity(curGen, userInput_.generation)/2.0){
            for (int i = 1; i < chromosomes_.size(); i++) {
                if(!chromosomes_.get(i).isSolution()){
                    break;
                }
                gap.add(new Element(chromosomes_.get(i).getFitnessVal(0)-chromosomes_.get(i-1).getFitnessVal(0),i));
            }
        }else{
            return;
        }
            
        
        //Element.sortOrder = Element.ASCENDING;
        Collections.sort(gap);//Bad ones first, good ones later
        
        //do some cleaning remove 20% bad ones - bad ones are those who are very
        //close to each other "relatively"
        //NOTE: you can use Vector Quantization
        double rmPercent = 1.0; 
        int rmTotal=0;

        rmPercent = 0.1;
        rmTotal = (int)Math.floor(gap.size()*rmPercent);
        
        if(rmTotal == 0){
            return;
        }

        Integer [] rmIdx = new Integer[rmTotal];
        for (int i = 0; i < rmTotal; i++) {
            rmIdx[i] = gap.remove(0).idx;
        }                
        
        Arrays.sort(rmIdx, Collections.reverseOrder());//bigger index first

        for (int r : rmIdx) {
            chromosomes_.remove(r);            
        } 
        chromosomes_.set(rmIdx[0], (Chromosome)getBestSoFarCOP().clone());

        System.out.println(rmTotal + " chromes cleaned!");
    }
    
    
    
    private Chromosome getLocalRAguruFor(final int idx){
        int community = this.RAcommunitySize;
        double bestFit = Double.MAX_VALUE;
        Chromosome bestRAguru = null;
        
        int L = 0;
        L = idx/community+1;
        int lowerLimit = 0;
        int upperLimit;
        if(L>=1){
            lowerLimit = (L-1)*community;
            upperLimit = L*community;
        }else{
            lowerLimit = 0;
            upperLimit = 1*community;
        }
        int printK = -1;
        for (int k = lowerLimit; k < upperLimit; k++) {
            if(chromosomes_.get(k).getFitnessVal(0)<bestFit){
                bestRAguru = chromosomes_.get(k);
                bestFit = chromosomes_.get(k).getFitnessVal(0); 
                printK = k;
            }
        }
        return bestRAguru;
    }
    
    private double mutation(ArrayList<Chromosome> list, boolean bAddToPool, Operators type, final boolean bAnalsis) throws SolutionFoundException{
        ArrayList<Chromosome> hMutes = new ArrayList<Chromosome>();
        ArrayList<Integer> idxMutes = new ArrayList<Integer>();
        ArrayList<ArrayList<Chromosome>> clones;
        Chromosome slotBestChrome;
        Chromosome slotChromeNeighbor;
        boolean hasProgressed;
        double percentageChanged = 0.0;

        ArrayList<Chromosome> allPromisingChromes = new ArrayList<Chromosome>();
        
        double bestB4process;
        double diffFit;
        double diffFitSum;
        int AffectedChrome;
        int improvmentCount;
        double diversity;
        
        if(!getBestSoFarCOP().isPartialSolution()){
            return percentageChanged;
        }
        
        if(list == null || list.size() == 0 || ((type == Operators.RAkarma || type == Operators.mutationCluster) 
                && !getBestSoFarCOP().isSolution())){
            return percentageChanged;
        }
                        
//        ArrayList<Integer> tmpIdx = new ArrayList<Integer>();
        bestB4process = getBestSoFarCOP().getFitnessVal(0);
        diffFit = 0;
        diffFitSum = 0;
        improvmentCount = 0;
        AffectedChrome = 0;        

//        tmpIdx = MyMath.sequence(0, chromosomes_.size()-1);
        
        int v = 0;
        Chromosome tchrome;
        Chromosome guru = getBestSoFarCOP();//null will also work
        if(type == Operators.RAkarma){
            for (Chromosome c : list){ 
                c.clearRAmyGuru();
            }            
        }
        
        
        for (int i = 0; i < list.size(); i++) {
            tchrome = list.get(i);
            if(type == Operators.RAkarma){ //keep only solutions
                if(!tchrome.isSolution()){
                    continue;
                }
                
                tchrome.setRAmyGuru(getLocalRAguruFor(i));
            }
            
            hMutes.add(tchrome); //mutations work with non partial solutions as well.
                                //can see old code as well.
            idxMutes.add(i);
        }
        
//        for (Integer j : tmpIdx) {
//            tchrome = chromosomes_.get(j);            
//            if(type == Operators.RAkarma){ //keep only solutions
//                if(!tchrome.isSolution()){
//                    continue;
//                }
//            }
//            if(v++%RAcommunitySize == 0){
//                if(tchrome.isSolution())
//                    guru = tchrome;//change guru, can have self guru
//            }
//            tchrome.setRAmyGuru(guru); //can have self guru            
//            hMutes.add(tchrome); //mutations work with non partial solutions as well.
//                                //can see old code as well.
//            idxMutes.add(j);             
//        }

        double maxVal=Double.NaN;
        double minVal=Double.NaN;
        if(type == Operators.mutationSinglePoint){
            Chromosome.tmpSortBy = Chromosome.BY_FITNESS;                                
            maxVal = Collections.max(hMutes).getFitnessVal(0);
            minVal = Collections.min(hMutes).getFitnessVal(0);
            Chromosome.tmpSortBy = userInput_.solutionBy;
        }
        
        clones = cclone(hMutes, beta); //many clones  
        
        AffectedChrome = hMutes.size();
        double aff;
        int cardinality;
        int nKempe; 
        cardinality = 1; //(int)(Math.random()*3); //5;
        double r;
        
        int i = 0; //indicates an individual's correct possition in chromosome_
        int nbr = 0; //indicates a defined neighbor - can also have set of neighbors in future
        
        for (ArrayList<Chromosome> al : clones) {            
            slotBestChrome = list.get(idxMutes.get(i));//current one
            nbr = idxMutes.get((i+5)%idxMutes.size());
            slotChromeNeighbor = list.get(nbr);
            hasProgressed = false;
            for (Chromosome ch : al) {
                r = Math.random();
                if(r<0.60){
                    nKempe = 1;
                }else if(r<0.85){
                    nKempe = 2; //1+(int)(r*2);
                }
                else{
                    nKempe = 3; //4+(int)(r*3);
                }
        
                double a = Math.random();
                double b = Math.random();
                if(b<a){
                    double tmp;
                    tmp = a;
                    a = b;
                    b = tmp;
                }
                    
                if(type == Operators.mutationGroupSwap){
                    mutationGroupSwap(ch);
                }else if(type == Operators.mutationSwapCOP){ 
                    mutationSwapCOP(ch);//not using anymore....
                    if(!ch.cleanHardConstraintVios(true)){
                        continue;
                    }
                }else if(type == Operators.mutationSinglePoint){
                    aff = (ch.getFitnessVal(0)-minVal)/maxVal;                                        
                    mutationSinglePoint(ch, aff); //It is valid??? 
                    if(!ch.cleanHardConstraintVios(true)){
                        continue;
                    }
                }else if(type == Operators.mutationInterRaceXCOP){
                    interRaceCrossoverIntegerCOP(ch, 1, false);                    
                }else if(type == Operators.RAkarma){ //RA
                    RAperformKarma(ch.getRAmyGuru(), ch, 0, true);
                }else if (type == Operators.mutationKemp1){ 
                    //kemp parameters [Aper,Bper, atCorner, useAppend, n, valTo, isDynamic, bUpdate]
                    ch.mutationKempe(a, b, true, true, nKempe, null, false,true); //0.05,1.0               
                }else if (type == Operators.mutationKemp2){ 
                    ch.mutationKempe(a, b, false, true, nKempe, null, false,true); //0.0,0.0
                }else if (type == Operators.mutationKemp3){ 
                    ch.mutationKempe(a, b, true, false, nKempe, null, false,true); //0.05,1.0               
                }else if (type == Operators.mutationKemp4){ 
                    ch.mutationKempe(a, b, false, false, nKempe, null, false,true);
                }else if (type == Operators.mutationKemp5){ 
                    ch.mutationKempe(a, b, false, false, nKempe, null, false,true); //0.0,0.0,
                }else if (type == Operators.crossoverKempe){ 
                    crossoverKempe(ch,true, getBestSoFarCOP(), false,cardinality, 1);
                }else if (type == Operators.mutationCluster){ 
                    ch.mutationCluster(1, true);
                }else if (type == Operators.mutationClusterMulti){ 
                    cardinality = 1+(int)(Math.random()*userInput_.totalConstraints/10); //minimum change
                    if(bStagnant){
                        cardinality = userInput_.totalConstraints/2;                        
                    }
                    ch.mutationCluster(cardinality, true);
                }else if (type == Operators.mutationChangeTrack){
                    int level = 1+ (int)(Math.random()*chromHistoryLevel);
                    cardinality = 1+(int)(Math.random()*userInput_.totalConstraints/10); //minimum change
                    if(bStagnant){
                        cardinality = userInput_.totalConstraints/2;;
                        level = (int)(Math.random()*2);
                    }
                    
                    int sz = 0;
                    int val;
                    ArrayList<ArrayList> satTrack = ch.trackCommonPath(level);
                    if(satTrack == null){
                        break;
                    }
                    for (ArrayList s : satTrack) {
                        sz += s.size();
                    }
                    cardinality = Math.min(cardinality, sz);
                    
                    //pick courses need to be changed.
                    ArrayList<Integer> rndIdx = MyRandom.randperm(0,satTrack.size());
                    Element takenVal;
                    int removed = 0;
                    int idx=0;
                    int colSize;
                    while (removed < cardinality) {
                        colSize = satTrack.get(rndIdx.get(idx)).size();
                        if(colSize > 0){
                            val = (Integer)satTrack.get(rndIdx.get(idx)).remove((int)(Math.random()*colSize));//it is index
                            takenVal = new Element(val, -1); 
                            //move the takenVal to "somewhere else".
                            ch.mutationKempe(0, 0, false, false, 1, takenVal, false, true);
                            removed++;
                            if(Math.random()<0.8) //try same idx
                                idx = ++idx%satTrack.size();
                        }else{
                            idx = ++idx%satTrack.size();
                        }      
                    }
                }else{
                    break;
                }
                          
                double errorTolerance = 0.1;
                
                if(ch.isMorePromisingThan(slotBestChrome) || (ch.isPartialSolution() && ch.isStagnant(CspProcess.NO_PROGRESS_LIMIT) && ch.getFunctionalVal()-slotBestChrome.getFunctionalVal()< errorTolerance &&
                        Math.random() < (1-MyMath.expProbablity(curGen, userInput_.generation))/3)
//                        || (type == Operators.RAkarma && Math.random()<0.05 ) //just one clone?? - may get infeasible one
                        ){// || (slotBestChrome.isStagnant(NO_PROGRESS_LIMIT) && !slotBestChrome.isPartialSolution())){                  
                    if(bAnalsis){
                        diffFit = Math.abs(Math.abs(slotBestChrome.getFunctionalVal()) - Math.abs(ch.getFunctionalVal()));
                        diffFitSum += diffFit; // This is wrong. Why diff fitness. Should be diff position.
                        improvmentCount++;
                    }
                    list.set(idxMutes.get(i), ch);//??CSPsolsDB??????????????
                    list.get(idxMutes.get(i)).addToHistory(slotBestChrome);

                    //slotBestChrome = ch;
                    hasProgressed = true;
                    break;
                }else{
//////                    if(bAddToPool){                    
//////                        if(ch.isPartialSolution() && ch.isMorePromisingThan(slotChromeNeighbor)
//////                                && slotBestChrome.isMorePromisingThan(ch)){
//////                            addToPool(ch);
//////                        } 
//////                    }
                }
            }
            if(hasProgressed){// && chromosomes_.get(idxMutes.get(i)).isPartialSolution()){ //note it is to cater for CSP sols becuase isMorePromisingThan(?) has changed for CSPs
                list.get(idxMutes.get(i)).cleanProgressCounter();
                percentageChanged++;
            }else{
                list.get(idxMutes.get(i)).reportNoProgress();
            }
            i++;
        }  

        clones.clear();
                                
        if(bAnalsis){
            diversity = MyMath.stdDev(getAllChromesCOPfintess()); //wrong. should be based on loc not fitness
            opLogPerGen.addDataForOpCall(type, diffFitSum, AffectedChrome, improvmentCount, bestB4process, diversity);                       
        }  
   
        clones = new ArrayList<ArrayList<Chromosome>>();
        clones.trimToSize();
        hMutes = new ArrayList<Chromosome>();
        hMutes.trimToSize();
        
        return percentageChanged/list.size();
        
//        chromosomes_.addAll(allPromisingChromes);
//        Collections.sort(chromosomes_);
//        tmpIdx = MyMath.negExpFnSelection(grouping.get(i).size(), FirstSlotSize, ro, debugPrint);
        
    }
    
    private void addToPool(Chromosome c){
        if(poolPromisingChromes.size()< poolPromisingChromesSize){
            if(c.isPartialSolution()){
                poolPromisingChromes.add(c);
            }
        }
    }
    
    /**
     * 
     * @param X
     * @param withY
     * @param bOpposite - by default should be false
     */
    private void enqueueSuspendedIfQualified(Chromosome X, Chromosome withY, boolean bOpposite){        
//        if(X.isMorePromisingThan(withY)){ //suspend the poorer one
//            if(withY.isSolution() && suspended_.size()<suspendSize)
//                if(bOpposite)
//                    suspended_.add(X);
//                else
//                    suspended_.add(withY);
//        }
    }
    
    /**
     * This is done to respect the diverse solution. Even if the fitness value
     * is low but if a chromosome is in a promising region where fitness is likely
     * to increase we do not delete them unless the parameter {@code deleteOnlyWorstOnes}
     * says so.
     * 
     * @param deleteOnlyWorstOnes 
     */
    private void RHCH_removal(){        
        Chromosome newChrom = null;
        int iteration;
        iteration = 5; 
        double rand;
        
        if(/*getBestSoFarCOP().isSolution() &&*/ curGen%iteration == 0){//bOptimizationMode || bStagnant){                    
            Chromosome tchrom;
            for (int i = 0; i < chromosomes_.size(); i++) { //elite???
                tchrom = chromosomes_.get(i);

                if(tchrom.isStagnant(NO_PROGRESS_LIMIT)){
//                    if(curGen-chromosomes_.get(i).prevBest.getLastBestGeneration()>10){                    
                        if(tchrom.isPartialSolution()){ 
                            rand = Math.random(); //0.3+0.7*Math.random();
                            Chromosome x = inFeasiblePopCOP.get(distFrom(chromosomes_.get(i),false,inFeasiblePopCOP));
                            newChrom = chromosomes_.get(i).removedWithBacktrack(rand,x);
////                            newChrom = chromosomes_.get(i).replaceWithBacktrack(rand, x);
//                            if(newChrom != null){
//                                chromosomes_.get(i).addToHistory(tchrom); //don't lose the current one (good one may be)
//                            }
                        }else{
//                            if(poolSelectedChromes.size()>0)
//                                newChrom = poolSelectedChromes.remove(distFrom(chromosomes_.get(i),false, poolSelectedChromes)); //get close match
                        }
                        if(newChrom != null){                            
                            chromosomes_.set(i, newChrom);
                        }
//                    }                                          
                }
            }                                           
        } 
        
        if(curGen%20==0 && getBestSoFarCOP().isSolution()){
            Chromosome.tmpSortBy = Chromosome.BY_FITNESS;
            Collections.sort(chromosomes_);
            int sols = 0;
            double median;
            ArrayList<Double> fit = new ArrayList<Double>();
            for (Chromosome c : chromosomes_) {                
                if(c.isSolution()){
                    sols++;
                    fit.add(c.getFitnessVal(0));
                }
            }
            
//            median = MyMath.median(fit, true);
            double UpperLimit = MyMath.median(new ArrayList<Double>(fit.subList(fit.size()/2,fit.size() )), false);
            
            Chromosome c,infC;

            int rp=0;
            int iClosest;
            for (int i = 0; i < chromosomes_.size(); i++) {
                c = chromosomes_.get(i);
                if(c.prevBest.getAllTimeBestFitness()>UpperLimit){ //poor
                    if(inFeasiblePopCOP.size() == 0){
                        break;
                    }
                    iClosest = distFrom(chromosomes_.get(i),false, inFeasiblePopCOP);
                    infC = inFeasiblePopCOP.remove(iClosest); //exists in chromosome_
//                     user problem dependent epsilon
                    if( Math.abs(infC.getFitnessVal(0)-c.getFitnessVal(0))<userInput_.epsilon){ //very close
                        i--;
                        continue;
                    }else{
                        inFeasiblePopCOP.add(iClosest,infC);
                    }
                    chromosomes_.set(i, inFeasiblePopCOP.remove(0)); //get the best ones only
                    rp++;
                }
            }
            System.out.println("** cleanup... [Total bad chromes removed is " + rp +"]");
//            final int solsRm = sols/10;
//            int rm = Math.min(solsRm, inFeasiblePopCOP.size()); 
//            for (int i = 0; i < rm; i++) {
//                chromosomes_.set(solsRm+i, inFeasiblePopCOP.remove(0)); //hope infeasiblePopCop is sorted...
//            }
//            System.out.println("** cleanup... [Total bad chromes removed is " + rm +"]");
        }
    }
    
    
    /**
    * This method guarantees that garbage collection is
    * done unlike <code>{@link System#gc()}</code><br>
     * See an example in wikipedia:
     * import java.lang.ref.WeakReference;
     * <pre>
     * <code>
    public class ReferenceTest {
        public static void main(String[] args) throws InterruptedException {
            String a = new String("I'm here"); //a is holding a reference in the caller
            WeakReference r = new WeakReference(new String("I'm here"));//no one is holding a reference in the caller<br> 
            WeakReference sr = new WeakReference(a); 
            System.out.println("before gc: r=" + r.get() + ", static=" + sr.get()); 
            System.gc(); 
            Thread.sleep(100); 
 
            // only r.get() becomes null
            System.out.println("after gc: r=" + r.get() + ", static=" + sr.get()); 
        }
    }
     * </code>
     * </pre>
    */
   public static void gc() {
     WeakReference ref = new WeakReference<Object>(new Object());
     while(ref.get() != null) {
       System.gc();
     }
   }
    
   
   private ArrayList<Double> getAllChromesCOPfintess(){
       ArrayList<Double> fit = new ArrayList<Double>();
       for (Chromosome c : chromosomes_) {
           fit.add(c.getFitnessVal(0));
       }
       return fit;
   }
   
   private void removeStagnantChromes(final ArrayList<Chromosome> org, int keepMinSize){
       for (int i = 0; i < org.size(); i++) {
           if(org.size() <= keepMinSize){
               break;
           }
           if(org.get(i).isStagnant(this.NO_PROGRESS_LIMIT)){
               org.remove(i);
               i--;
           }
       }
       
//       int add = keepMinByAddingNew - org.size();
//       Chromosome c;
//       try{
//       for (int hm = 0; hm < add; hm++) {
//           c = initializeChromosome();
//           org.add(c);
//       }
//       }catch(Exception e){
//           e.printStackTrace();
//       }
   }
   
   
   
   /**
    * 
    * @param size 0 calculates current {@link CspProcess.#hammingDistPrevBest} from
    * current {@link CspProcess.#bestSoFarCOP} to the previous {@link CspProcess.#bestSoFarCOP_PREV}
    * @return 
    */
    private ArrayList<ArrayList> retrieveHammingDist(int size){  
        ArrayList<ArrayList> stuck = new ArrayList<ArrayList>();
        
        //<<calc current hamming distance
        for (int i = 0; i < bestSoFarCOP.satisfactions_.size(); i++) {
            stuck.add(new ArrayList());
        }

        int sz;
        for (int i = 0; i < bestSoFarCOP.satisfactions_.size(); i++) {                        
            Collections.sort(bestSoFarCOP.satisfactions_.get(i));
            Collections.sort(bestSoFarCOP_PREV.satisfactions_.get(i));
            sz = Math.min(bestSoFarCOP.satisfactions_.get(i).size(), bestSoFarCOP_PREV.satisfactions_.get(i).size());
            for (int j = 0; j < sz; j++) {
                if((Integer)bestSoFarCOP.satisfactions_.get(i).get(j) ==
                        bestSoFarCOP_PREV.satisfactions_.get(i).get(j)){ // this is for discrete..
                    stuck.get(i).add((Integer)bestSoFarCOP.satisfactions_.get(i).get(j));                           
                }else{
////                                hammingDistPrevBest.get(i).add(-1);//indicate different .... size should be >=10
                    //not needed in the new version of pattern detection.
                }
            }
////                        //not necessary
////                        for (int j = 0; j < bestSoFarCOP.satisfactions_.get(i).size()-sz; j++) {
////                            hammingDistPrevBest.get(i).add(-1);
////                        }
        }

        bestSoFarCOP.refreshValVsConstIdx(); //DON't USE refreshFitness for bestSoFar if there is new transition
                                            //bestsofarCOP will become CSP.
        bestSoFarCOP_PREV = (Chromosome)bestSoFarCOP.clone();
        //>>............



        if(stkBestSoFar.size() == 0 || size == 0){
            return stuck;
        }
        
        size = Math.max(0, stkBestSoFar.size()- size); //first valid index

        ArrayList<ArrayList> node;
        ArrayList<ArrayList> commonVals = new ArrayList<ArrayList>();
        for (int i = 0; i < bestAchievedCOP.satisfactions_.size(); i++) {
            commonVals.add(new ArrayList());
        }
        
        int stuckPtr;
        int nodePtr;
        for (int s = stkBestSoFar.size()-1; s >= size; s--) { //remember it is a stack
            node = stkBestSoFar.forceGet(s).satisfactions_;
//            for (int j = 0; j < node.size(); j++) {
//                sz = Math.min(node.get(j).size(), stuck.get(j).size());//column
//                for (int k = 0; k < sz; k++) {
//                     if((Integer)node.get(j).get(k) == (Integer)stuck.get(j).get(k)){ // this is for discrete..
//                        commonVals.get(j).add((Integer)node.get(j).get(k));                           
//                    }else{
////                        commonVals.get(j).add(-1);//indicate different .... size should be >=10
//                    }
//                }
//            }
            for (int i = 0; i < node.size(); i++) {
                nodePtr = 0;
                stuckPtr = 0;
                while(nodePtr<node.get(i).size() && stuckPtr<stuck.get(i).size()){
                    while(nodePtr<node.get(i).size() && (Integer)node.get(i).get(nodePtr) < (Integer)stuck.get(i).get(stuckPtr)){
                        nodePtr++;
                    }

                    if(nodePtr>=node.get(i).size()){
                        break;
                    }
                    if(((Integer)node.get(i).get(nodePtr)).intValue() == ((Integer)stuck.get(i).get(stuckPtr)).intValue()){
                        commonVals.get(i).add(node.get(i).get(nodePtr));
                        nodePtr++;
                        stuckPtr++;
                    }
                    if(nodePtr>=node.get(i).size() || stuckPtr >= stuck.get(i).size()){
                        break;
                    }
                    while(stuckPtr<stuck.get(i).size() && (Integer)stuck.get(i).get(stuckPtr)<(Integer)node.get(i).get(nodePtr)){
                        stuckPtr++;
                    }

                    if(stuckPtr>= stuck.get(i).size()){
                        break;
                    }

                    if(((Integer)node.get(i).get(nodePtr)).intValue() == ((Integer)stuck.get(i).get(stuckPtr)).intValue()){
                        commonVals.get(i).add(node.get(i).get(nodePtr));
                        nodePtr++;
                        stuckPtr++;
                    }
                }
            }
            stuck = commonVals;
            commonVals = new ArrayList<ArrayList>();
            for (int j = 0; j < bestAchievedCOP.satisfactions_.size(); j++) {                           
                commonVals.add(new ArrayList());
            }
        }
        return stuck;        
    }
    
    public static void printGuiDebug(String msg){
//        msg = guiJtextAreaDebugPrint.getText() + "\n" + msg;
        guiJtextAreaDebugPrint.append(msg+"\n");
    }
    
    public void setRAguruSize(int sz){
        RAguruSize = sz;
        RAcommonerSize = RAcommunitySize*RAguruSize; // generally 50% is COP but DON'T change. additional ones would not be taken into account
        RAsuspendRate = (int)(RAcommonerSize*0.1);
        
        if(tmpChangGuruCount.size()>RAguruSize){
            tmpChangGuruCount = new ArrayList<Integer>(tmpChangGuruCount.subList(0, RAguruSize));
        }else{
            for (int i = 0; i < RAguruSize-tmpChangGuruCount.size(); i++) {
                tmpChangGuruCount.add(0);
            }            
        }
    }
    
    public int getRAguruSize(){
        return RAguruSize;
    }
    
    private void printBasicFinalResults(double startTime){
        double endTime = 0.0;
        setBestSoFarCOP(bestAchievedCOP);
        endTime = System.nanoTime();
        endTime = endTime/Math.pow(10, 9);
        System.out.println("\nProcess time(Sec): " + MyMath.roundN(endTime - startTime,2));
        System.out.println("total chromosomes: "+chromosomes_.size());
        System.out.println("Total Evaluations: "+ getTotalEvals());
                     
        System.err.flush();
        System.out.flush();
        
//        setSolution();
                        
        if(bestSoFarCOP.isSolution()){//this.solutions_.isEmpty()){                            
            System.out.println("No Solution Found :( ****************");                
            System.out.println("best chromosomes\n" + bestSoFarCOP);
        }else{
            System.out.println("Solution Found");                
            System.out.println("best chromosomes\n" + bestSoFarCOP);
        }  

        if(externalData_ != null){
            externalData_.printProblemSpecificSol(bestSoFarCOP.satisfactions_);
        }
    }
    
    private int getTotalEvals(){
        int evals = 0;
        Chromosome.totalAppEvals = Chromosome.totalAppEvals/userInput_.totalConstraints;// equal sized chromes
        Chromosome.totalAppEvals = 2*Chromosome.totalAppEvals/(userInput_.totalConstraints+1); //each ICHEA eval is (N+1)/2 times faster than others, hence divided. ICHEA N -> Ohters N*(N+1)/2            
        Chromosome.totalRefEvals = Chromosome.totalRefEvals/userInput_.totalConstraints;// equal sized chromes            
        evals = Chromosome.totalAppEvals + Chromosome.totalRefEvals;
        
        return evals;
    }

    /*private String getCurrentMutationDesc(final int ID){
        String desc = "Bad Input";
        if(ID == 0){
            desc = "(0) mutation group swap, mutationSinglePoint(req. affinity) & RAkarma() ";
        }else if (ID == 1){
            desc = "(1) mutationSwapCOP (creates separate inFeasible population) & interRaceCrossoverIntegerCOP (on chromosomes_)";
        }else if (ID == 2){
            desc = "(2) mutationCluster";
        }else if (ID == 3){
            desc = "(3) remove a column randomly (on chromosomes_ & elite)";
        }else if (ID == 4){
            desc = "(4) all possible mutations at once.";
        }
//        else{
//            desc = "(5) not yet defined....";
//        }
        return desc;
    }*/

    
    /**
     * 
     * @param p1
     * @param p2
     * @param n 
     */
    private void twoPointBlindCrossover(ArrayList<ArrayList> p1, ArrayList<ArrayList> p2) throws Exception{               
        if(p1.size() != p2.size()){
            throw new Exception("Both parents should have same size"); //this never happens
        }
        int sz = p1.size();
        
        if(p1.size() != p2.size()){
            System.out.println("error.....");
        }
                   
        ArrayList<Integer> ptr = new ArrayList<Integer>(MyRandom.randperm(0, sz).subList(0, 2));
        Collections.sort(ptr);
        ArrayList<Integer> temp;
        
         for (int i = ptr.get(0); i < ptr.get(1); i++) {
             temp = p2.get(i);
                p2.set(i, p1.get(i));
                p1.set(i, temp);
         } 
         
         //cleaning process
         int onlyOne[] = new int[userInput_.totalDecisionVars];
         int idx; 
         for (int i = 0; i < p1.size(); i++) {
             for (int j = 0; j < p1.get(i).size(); j++) {
                 idx = ((Integer)p1.get(i).get(j)).intValue();
                 if(onlyOne[idx]>0){
                     p1.get(i).remove(j);
                     j--;
                 }else{
                    onlyOne[idx]++;
                 }
             }
        }
         
         //cleaning process
         onlyOne = new int[userInput_.totalDecisionVars];
         for (int i = 0; i < p2.size(); i++) {
             for (int j = 0; j < p2.get(i).size(); j++) {
                 idx = ((Integer)p2.get(i).get(j)).intValue();
                 if(onlyOne[idx]>0){
                     p2.get(i).remove(j);
                     j--;
                 }else{
                    onlyOne[idx]++;
                 }
             }
        }         
    }
    
//    // its like nar-narayan aatma
//    private void reincarnateProcess(ArrayList<Chromosome> chromeList, int curGen){
//
//        if(curGen%10 == 0){//every 10th gen
//            int sz = Math.min(2, chromeList.size());
//            for (int i = 0; i < sz && suspended_.size()<userInput_.population; i++) { //get the best ones
//                suspended_.add((Chromosome)chromeList.get(i).clone());//remove last
//            }                  
//        }
//
//        int idx;
//        int totalRemoved = 0;
//        if(suspended_.size()>2*suspendSize){ //not using sz
//            idx = chromosomes_.size()*1/2;;
//            for (int i = 1; i <= suspendSize; i++) { 
//                chromosomes_.get(idx).useImmunity(curGen, true);
//                if(chromosomes_.get(idx).getImmunity()<=0){//no more immunity
//                    totalRemoved++;
//                    chromosomes_.remove(idx);
//                }
//                idx--;
//            }
//            for (int i = 1; i <= totalRemoved; i++) {
//                suspended_.peek().resetImmmunity();//reset immunity before bringing back to life.
//                chromosomes_.add(suspended_.remove());
//            }
//        }
//        
//    }
//    
    public void changeRAcontrol(boolean play){
        RAplay = play;
        if(RAplay && RAcommoners_.isEmpty())
            RAinitialize(RAcommoners_, RAcommonerSize);
    }
    
    public void changeHardConstraintViolationTolerance(int val){
        try { 
            userInput_.hardConstViosTolerance = val;
            if(bestSoFarCOP.getTotalHardVios()>val){
//                bestSoFarCOP.refreshFitness();
                bestSoFarCOP.overrideBestSoFarFitness(Double.MAX_VALUE);
            }
//            bestSoFarCSP.refreshFitness(maxCSPval);
            for (Chromosome c : chromosomes_){
                c.refreshFitness();            
            }
            
            for (Chromosome c : RAcommoners_) {
                c.RAupdateFitness();
            }
        } catch (SolutionFoundException ex) {
            ex.printStackTrace();
        }
    }
    /**
     * I think it only used to "Decrease" search space where <tt>amt</tt> is ALWAYS POSITIVE.
     * Check the clean section. Once this function is called all the existing 
     * individuals are <I>cleaned</I>.
     * @param amt No of times CSP space reduced (<tt>amt</tt>*<code>{@link CspProcess.#startAcceptedConstRatio}</code>).
     * @param onlyCSP Change only partial solution and leave existing complete 
     * solutions intact.
     */
    public void changeCSPspace(int amt, boolean onlyCSP){
        int threshold;
        threshold = externalData_.popConstLimit(amt);
        System.out.println("");
        
        //this seems correct rather than th follwoing for loop.if accepted ratio is changed regularly by GUI.
////        curAcceptedConstRatio = 1.0*(threshold+1)/userInput_.totalConstraints;
        
        for (int i = 0; i < amt; i++) { //possible bug alter curaccept should be based on threshold above. curaccept*total = poped + 1
            transitionGen = maxTransitionGen; // so that to force alter curAcceptedConstRatio
            alterAcceptedConstRatio(false, true);
        }
        try {
            if(!onlyCSP){
//                externalData_.cleanCSP(bestSoFarCOP.satisfactions_, threshold);
//                bestSoFarCOP.refreshFitness(false);
//                bestSoFarCOP.overrideBestSoFarFitness(Double.MAX_VALUE);
                try{
                    bestSoFarCOP = initializeChromosome();
                    bestSoFarCOP.overrideBestSoFarFitness(Double.MAX_VALUE);
                    bestSoFarCOP_PREV = initializeChromosome();
                    bestSoFarCOP_PREV.overrideBestSoFarFitness(Double.MAX_VALUE);
                }catch (Exception e){
                    e.printStackTrace();
                }                        
            }
//            bestSoFarCOP.overrideBestSoFarFitness(Double.MAX_VALUE);
//            externalData_.cleanCSP(bestSoFarCSP.satisfactions_, threshold);
//            bestSoFarCSP.refreshFitness(false);
            try {
                //            bestSoFarCSP.overrideBestSoFarFitness(userInput_.totalConstraints);
                setBestSoFarCSP(initializeChromosome());
            } catch (Exception ex) {
                Logger.getLogger(CspProcess.class.getName()).log(Level.SEVERE, null, ex);
            }
            for (Chromosome c : chromosomes_) { 
                if(c.isSolution() && onlyCSP){
                    continue;
                }
                
                
                
                externalData_.cleanCSP(c.satisfactions_, threshold);
                c.refreshFitness(true); //c.refreshFitness(false);
            }
        } catch (SolutionFoundException ex) {
            ex.printStackTrace();
        }
        
        if(!onlyCSP){
//            inFeasiblePopCOP.clear();
            RAsuspendedQueue.clear();
//            RAcommoners_.clear();
//            modeChangedGen = -1; // so that RAcommoners_ can be refilled later, otherwise it won't be refilled EVER.
//            suspended_.clear();
            cleanChromHistory(chromosomes_);
            bStartOffspringStacking = false;
        }
    }
    
    
    public void resolveLocalOpt(int level){
        if(!resolvingLocalOptimalMode()){
            resolveLocalOptExternal = true;             
            resolveLevel = level;
        }
    }
    
    public void changePopulationSize(final int newSz){
        if(newSz<userInput_.population){            
            chromosomes_ = new ArrayList<Chromosome>(chromosomes_.subList(0, newSz));
        }else{
            try {
                ArrayList<Chromosome> temp = new ArrayList<Chromosome>();                
                initializeChromosome(temp, newSz-userInput_.population, curGen);
                chromosomes_.addAll(temp);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        userInput_.population = newSz;
        maxInfeasiblePop = newSz;
        maxSolPop = userInput_.population/2;
        maxNonSolPop = userInput_.population - maxSolPop;
    }
    
    /**
     * <B>NOTE</B> This function is only for discrete CSP/COP when change of 
     * the size of current solution space is desired. currently it is handled 
     * by the user.
     * @param amt - amount to which increment or decrement. common inputs are +1
     * or -1
     */
    public void changeSolutionSpaceSize(final int amt){
        int i = 0;//for testing purpose only          
        int threshold=0;
        changeSpaceMode = true;
        externalData_.alterCSPsizeBy(amt);// "-"
        
        try{
            for (Chromosome c : chromosomes_) { 
                if(amt>0){
                    c.satisfactions_.add(new ArrayList()); //Append into "Last" 
                    c.prevBest.update();
                }else{
                    c.satisfactions_.remove(MyRandom.randperm(0, c.satisfactions_.size()).get(0).intValue());
                    c.prevBest.update();
                    bOptimizationMode = false;
//                    externalData_.cleanCSP(c.satisfactions_, threshold);
                }
                
                c.refreshFitness();                    
            } 
            setBestSoFarCOP((Chromosome)chromosomes_.get(0).clone());
            setBestSoFarCSP((Chromosome)chromosomes_.get(userInput_.population-1).clone());
        }
        catch(Exception e){
            System.out.println(i);
            e.printStackTrace();
        }
     
        System.out.println("New CSP size: " + externalData_.getCSPsize());
        cleanChromHistory(chromosomes_);
        poolSelectedChromes.clear();
        poolPromisingChromes.clear();
    }
    
    /**
     * clones selected n chromes according to formula provided by CLONALG algorithm
     * @param nSols
     * @param bEmbed if bEmbed is true then the all nSols are also embedded into returned clone
     * @param beta if beta <= 0 that means only one clone is required.
     * @return 
     */
    private ArrayList<ArrayList<Chromosome>> cclone(ArrayList<Chromosome> nSols, final double beta){
        int cloneSize;
//        final int n = nSols.size();
        final int N = nSols.size()/2; //userInput_.population/2; //max clone for the best one 
        ArrayList<ArrayList<Chromosome>> clones = new ArrayList<ArrayList<Chromosome>>();

        for (int i = 1; i <= nSols.size(); i++) {
//            cloneSize = (int)Math.ceil((beta*N/10)/Math.ceil(i/10)); 
//            cloneSize = (int)Math.ceil(beta*N/i);
//             cloneSize = (int)Math.ceil(beta*N/1); 
//            cloneSize = Math.max(cloneSize, 10);//AT least 5... saah why 5??? how??? 
//            cloneSize = Math.max(cloneSize, 3);
//            cloneSize = 10; /////////////////////////////////////////////////////////////////////
//            
//            if(beta<=0){
//                cloneSize = 1;
//            }
            
            cloneSize = 20; //for all???????????? why??? bad ones knocked out early.... think!!
            
            clones.add(new ArrayList<Chromosome>());
            for (int j = 0; j < cloneSize; j++) {
                clones.get(i-1).add((Chromosome)nSols.get(i-1).clone());
            }
        }
        return clones;
    }
    
    public ArrayList<Chromosome> getChromosomes(){
        return this.chromosomes_;
    }
    
  
    
    /**
     * It is the measure of novely. The larger the ro value the more the novelty
     * in the search space.
     * @param chrome
     * @return
     * @throws MyException
     * @throws UnsupportedDataTypeException 
     */
    double getRoValue(Chromosome chrome) throws MyException, UnsupportedDataTypeException{
        double ro;
        if(this.dataType_.contains("Integer")){
            ro = chrome.getIntegerRoValue(chromosomes_, knearest_);//getIntegerRoValue(chrome);
        }
        else if(this.dataType_.contains("Double")){
            ro = getDoubleRoValue(chrome);
        }else{
            throw new UnsupportedDataTypeException("Only supports Integer and Double data type");
        }
        return ro;
    }
    
    
    
    /**
     * ro value determines the rank of novelty. The higher value the better.
     * @param chrome
     * @return
     * @throws MyException 
     */
    double getDoubleRoValue(Chromosome chrome) throws MyException{
        double ro;
        int maxViolation = this.userInput_.totalConstraints;
        ArrayList<Integer> validChromosomesIdx = new ArrayList<Integer>();
        Double []dist;
        int tempKnearest;
        
//        if(chrome.getRank() == maxViolation-1){
//            return -1.0;
//        }
        
        if (chromosomes_.isEmpty()){
            throw new MyException("No chromosme population", "Variable Initialization Error",JOptionPane.ERROR_MESSAGE);
        }
 
        for (int i = 0; i < chromosomes_.size(); i++) {
            if (chromosomes_.get(i).getRank() != maxViolation)
                validChromosomesIdx.add(i);            
        }

        dist = new Double[validChromosomesIdx.size()];
        for (int i = 0; i < validChromosomesIdx.size(); i++) {
            dist[i] = MyMath.norm(chrome.getVals(),chromosomes_.get(validChromosomesIdx.get(i)).getVals(), MyMath.DIST_EUCLEADIAN);
//            NOTE: I am using "SQUARE of distance" instead of just distance
//            because I will be using variance for ro_min.
            dist[i] = Math.pow(dist[i], 2);

        }
        Arrays.sort(dist);        
        // x1 itself is included in this set which should have the value 0.
        if(dist.length<=knearest_){ //////@Danger code............................
            tempKnearest = dist.length-1;
        }else{
            tempKnearest = knearest_;
        }
        ro = Math.pow(1.0/tempKnearest, 2) * MyMath.sum(dist, 0, tempKnearest);//Note: should not be knearest-1 as x1 itself is also included

        ro = MyMath.roundN(ro, 0);
        return ro;
    }

    
    
    private ArrayList<String> matlabPlotBuildGeneration(int generation) throws Exception{
        ArrayList<String> MatlabCommands = new ArrayList<String>();
        int drawXdata;
        double drawYdata;              
                
        if(!drawStart){
            MatlabCommands.add("hold on;");
            MatlabCommands.add("title('"+userInput_.chosenFile+"');"); 
            MatlabCommands.add("xlabel('Generations');");
            MatlabCommands.add("ylabel('Fitness');");           
            drawStart = true;
        }

//        MatlabCommands.add("title(\'Gen: " + generation + "\');");                


        
        
        if(getBestSoFarCOP().isSolution()){
            
////            MatlabCommands.add("h = gcf;");
////            MatlabCommands.add("axesObjs = get(h, 'Children');");
////            MatlabCommands.add("dataObjs = get(axesObjs, 'Children');");
////            MatlabCommands.add("objTypes = get(dataObjs, 'Type');");
////            MatlabCommands.add("xdata = get(dataObjs, 'XData')");
////            MatlabCommands.add("ydata = get(dataObjs, 'YData')");
////            MatlabCommands.add("disp(xdata);");
            MatlabCommands.add("xlim([1 " + userInput_.generation+"]);"); //this is changeable
            drawXdata = generation; //x axis;
            drawYdata = MyMath.roundN(getBestSoFarCOP().getFunctionalVal(),4);//y axis
            MatlabCommands.add("x=" + drawXdata + ";"); 
            MatlabCommands.add("y=" + drawYdata + ";");
////            MatlabCommands.add("xdata = horzcat(cell2mat(xdata(end)), x)");
////            MatlabCommands.add("disp(xdata);");
////            MatlabCommands.add("ydata = horzcat(cell2mat(ydata(end)), y)");
            MatlabCommands.add("plot(x,y,'.k');");

            MatlabCommands.add("drawnow;");            
        }
                

        return MatlabCommands;
    }
    
    private ArrayList<String> matlabPlotBuildConstraints(){
        ArrayList<String> commands = new ArrayList<String>();
        
                commands.add("hold on;");
        commands.add("x = [-100:0.1:100];");
        commands.add("br = 10.0;");
        commands.add("sr = 9.9;");
        commands.add("y1p = sqrt(br^2 - x.^2);");
        commands.add("y1m = -sqrt(br^2 - x.^2);");
        commands.add("y2p = sqrt(sr^2 - x.^2);");
        commands.add("y2m = -sqrt(sr^2 - x.^2);");
        
        commands.add("y3p = sqrt(br^2 - (x+2*br - 0.1).^2);");
        commands.add("y3m = -sqrt(br^2 - (x+2*br - 0.1).^2);");
        commands.add("y4p = sqrt(sr^2 - (x+2*br - 0.1).^2);");
        commands.add("y4m = -sqrt(sr^2 - (x+2*br - 0.1).^2);");


        commands.add("fig1 = gcf;"); //get current figure or create figure fig1
        commands.add("axes1 = axes('Parent',fig1);"); //Create axes
        
        commands.add("ylim(axes1,[-100 100]);");
        commands.add("box(axes1,'on');");
        commands.add("hold(axes1,'all');");
        commands.add("plot(x,y1p);");
        commands.add("plot(x,y1m,'Parent',axes1);");
        commands.add("plot(x,y2p,'Parent',axes1);");
        commands.add("plot(x,y2m,'Parent',axes1);");
        commands.add("plot(x,y3p,'Parent',axes1);");
        commands.add("plot(x,y3m,'Parent',axes1);");
        commands.add("plot(x,y4p,'Parent',axes1);");
        commands.add("plot(x,y4m,'Parent',axes1);");
                
        
//        commands.add("hold on;");
//        commands.add("X1 = [-100:1:100];");
//        commands.add("YMatrix1 = -9*X1+6.0;");
//        commands.add("YMatrix2 = 9*X1 - 1.0;");
////        commands.add("XMatrix3 = 1;");
////        commands.add("XMatrix4 = 0;");
//        commands.add("YMatrix3 = -9*X1+7.0;");
//        commands.add("YMatrix4 = 9*X1 - 2.0;");
//
//        commands.add("fig1 = gcf;"); //get current figure or create figure fig1
//        commands.add("axes1 = axes('Parent',fig1);"); //Create axes
//        
//        commands.add("ylim(axes1,[-100 100]);");
//        commands.add("box(axes1,'on');");
//        commands.add("hold(axes1,'all');");
//        commands.add("plot(X1,YMatrix1);");
//        commands.add("plot(X1,YMatrix2,'Parent',axes1);");
////        commands.add("plot(XMatrix3,X1,'Parent',axes1);");
////        commands.add("plot(XMatrix4,X1,'Parent',axes1);");
//        commands.add("plot(X1,YMatrix3,'Parent',axes1);");
//        commands.add("plot(X1,YMatrix4,'Parent',axes1);");
        
        return commands;
    }
    
    /**
     * Remove stagnant best values from the population of chromosomes.
     * preprocess - chrm population should be sorted and contains unique
     * chromosomes.
     * @param PERCENT - what PERCENT of chromosomes to be checked.
     * @param sameBestVals - Arraylist of same best val1
     * @param sameGens - same best val1 for how many generations
     * @return
     */
//    private boolean isStagnant(){    
//        boolean allsame;
//        boolean bstagnant;
//        bstagnant = false;
//        allsame = false;
//        ArrayList<ArrayList<Double>> diverse;
//
//        if(sameBestChromeVals_ != null)
//            diverse = (ArrayList<ArrayList<Double>>)sameBestChromeVals_.clone();
//        else{
//            diverse = new ArrayList<ArrayList<Double>>();
//            sameBestChromeVals_ = new ArrayList<ArrayList<Double>>();
//        }
//
//        for (int ofsp = 0; ofsp < (int)(SAME_BEST_VAL_PERCENT*userInput_.population); ofsp++) {
//            diverse.add(chromosomes_.get(ofsp).getValsCopy());
//        }
//        
//        HashSet<ArrayList<Double>> hashSet = new HashSet<ArrayList<Double>>(diverse);
//        diverse = new ArrayList<ArrayList<Double>>(hashSet);
//
//        if(diverse.size() == sameBestChromeVals_.size()){
//            allsame = true;
//        }else{            
//            sameBestChromeVals_ = new ArrayList<ArrayList<Double>>();
//            for (int ofsp = 0; ofsp < (int)(SAME_BEST_VAL_PERCENT*userInput_.population); ofsp++) {
//                sameBestChromeVals_.add(chromosomes_.get(ofsp).getValsCopy());
//            }            
//        }
//
//        if(allsame){
//            hasAllSame_++;
//        }else{
//            hasAllSame_ = 0;
//        }
//
//        if(hasAllSame_ >= SAME_BEST_GENERATIONS){
//            hasAllSame_ = 0;
//            sameBestChromeVals_ = null;
//            bstagnant = true;
//        }
//
//        return bstagnant;
//    }

    private void initializeChromosome(ArrayList<Chromosome> chromosome, final int SIZE, final int gen) throws SolutionFoundException, Exception {
        boolean bInitialStage;
        if(SIZE<=0){
            chromosome = null;
            return;
        }
        
        if (externalData_ != null){
            if(gen<10){
                bInitialStage = true;
            }else{
                bInitialStage = false;
            }            
            chromosome.addAll(externalData_.initializeExternalChrmosomes(SIZE,bAlsoGetPrevConstraints));
        }else{
            initializeChromosomesRandomly(chromosome,SIZE);
        }
    }
    
    private Chromosome initializeChromosome() throws SolutionFoundException, Exception {        
        if (externalData_ != null){               
            return externalData_.initializeExternalChrmosomes(1,bAlsoGetPrevConstraints).get(0);
        }else{
            throw new UnsupportedOperationException("Only applicable for externalData only");
        }
    }

    /**
     * Initializes Chromosomes with random values
     */
    private void initializeChromosomesRandomly(ArrayList<Chromosome> chromosome, final int SIZE) throws SolutionFoundException, Exception{
        Object rand = null;
        Chromosome tempChromosome;
               
        
        for (int i = 0; i < SIZE; i++) {
            tempChromosome = new Chromosome(this.userInput_.solutionBy, this.userInput_);
            for (int j = 0; j < userInput_.totalDecisionVars; j++) {
                if (userInput_.dataType.contains("Integer")){
                    rand = r_.randVal(userInput_.minVals.get(j).intValue(), userInput_.maxVals.get(j).intValue());
                }else if (userInput_.dataType.contains("Double")){
                    if(Math.random()<0.5){
                        rand = r_.randVal((Double)userInput_.minVals.get(j), (Double)userInput_.maxVals.get(j));
                    }else{
                        if(Math.random() < 0.5){
                            rand = userInput_.minVals.get(j);
                        }else{
                            rand = userInput_.maxVals.get(j);
                        }
                    }
                }
                else{
                    System.err.println("Incorrect use of data types");
                    System.exit(1);
                }
                tempChromosome.appendVal((Double)rand);
            }
            chromosome.add(tempChromosome);
        }        
    }

//    private void setFitness(ArrayList<Chromosome> chrm, final int SIZE){
//        for (int ofsp = 0; ofsp < SIZE; ofsp++) {
//            //ObjectiveFunction.definition(chrm.get(ofsp));
//            chrm.get(ofsp).setObjectiveFunctionVars();
//        }
//    }

    /**
     * noveltyTournamentSelection() - Tournament selection based on novelty
     * of the chrm in the population.
     * @return Returns ArrayList<Chromosome> of parent selected population 
     */
    private ArrayList<Chromosome> noveltyTournamentSelection(ArrayList<Chromosome> pop) throws MyException, UnsupportedDataTypeException{
        ArrayList<Chromosome> candidates = new ArrayList<Chromosome>();
        ArrayList<Chromosome> parents = new ArrayList<Chromosome>(); // shoud have this.pool sizse
        ArrayList<Integer> temp;
        double csize0, csize1;
        double ro0, ro1;
        int candidate0dominates;
        
        if(this.tourSize_ != 2){
            throw new MyException("Tour Should be 2", "Inappropriate Tour Size",JOptionPane.ERROR_MESSAGE);
        }
                 
        for (int p = 0; p < this.poolSize_; p++) {
            //select tourSize_ chromosomes k.e 2 chromosomes randomly from the population
            temp = MyRandom.randperm(0, pop.size());
            candidates.clear();
            for (int t = 0; t < this.tourSize_; t++) {                
                candidates.add(pop.get(temp.get(t)));
            }
            temp = null;            

            try{

                    //
                    //move here....

                    //
                    Chromosome.tmpSortBy = Chromosome.BY_FITNESS;
                    csize0 = candidates.get(0).getRank();
                    csize1 = candidates.get(1).getRank();
                    Chromosome.tmpSortBy = userInput_.solutionBy;

                    if (csize0 < csize1){ // the lower the better
                        parents.add(candidates.get(0));                
                    }
                    else if (csize1 < csize0){
                        parents.add(candidates.get(1));                
                    }
                    else{  
                        //
                        //??here??  
                            //<< you can move it bottom .....
                            ro0 = getRoValue(candidates.get(0)); //do not need to use getRo function, check sortnreplace function if it has already been set in tempRo property.
                            ro1 = getRoValue(candidates.get(1));

                            if (ro0 > ro1 || candidates.get(0).getVals().size() == 1) // the larger the better
                                parents.add(candidates.get(0));
                            else if (ro1 > ro0 || candidates.get(1).getVals().size() == 1)
                                parents.add(candidates.get(1));
                            else{
                            //>>..........................                  
                        //    
                        candidate0dominates = 0;

                        if(candidates.get(0).isStagnant(this.NO_PROGRESS_LIMIT)){
                            parents.add(candidates.get(1));
                        }else if(candidates.get(1).isStagnant(this.NO_PROGRESS_LIMIT)){
                            parents.add(candidates.get(0));
                        }else{
                            temp = MyRandom.randperm(0, 2);
                            parents.add(candidates.get(temp.get(0)));
                        }
                    }                                    
                }
                }catch(Exception e){
                e.printStackTrace();
            }
//            //>>
        }                
        
        return parents;
    }
    
    /**
     * IMPROPER METHOD... NEEDS CORRECTION.... Checks if the solution for CSP has been achieved
     * @return Returns ArrayList<Chromosome> of solution chromosomes.
     */
//    private void setSolution(){
//        //ArrayList<ArrayList<Double>> duplicates = new ArrayList<ArrayList<Double>>();
//        chromeValues = new ArrayList<ArrayList<Double>>();
//        int beforeSize, afterSize;
//                
//        for (Chromosome chromosome : this.chromosomes_) {
//            if(chromosome.isSolution()){ // no violations
//                this.solutions_.add(chromosome);          
//            }
//        } 
//        
//        if(bestSoFarCOP.isSolution()){
//            this.solutions_.add(bestSoFarCOP);
//        }
//        
//    }
    
//    public String printChromeValues(){
//        String str;
//        
//        str = Integer.toString(chromeValues.size()) + "\n";
//        str += Integer.toString(this.userInput_.totalConstraints) + "\n";
//        for (int i = 0; i < chromeValues.size(); i++) {
//            for (int j = 0; j < chromeValues.get(i).size(); j++) {
//                str += chromeValues.get(i).get(j).toString() + " ";                
//            }
//            str += "\n";            
//        }
//        return str;
//    }
    
//    private Chromosome notVals(Chromosome in){
//        Chromosome out;
//        
//        Double[] temp = new Double[userInput_.totalConstraints];
//        for (int iL = 0; iL < temp.length; iL++) {
//            temp[iL] = iL*1.0;            
//        }
//        
//        for (Double d : in.getValsCopy()) {
//            temp[d.intValue()] = -1.0;
//        }
//        
//        ArrayList<Double> notVal = new ArrayList<Double>();
//        
//        
//        for (int iL = 0; iL < temp.length; iL++) {
//            if(temp[iL]!=-1.0){
//                notVal.add(temp[iL]);
//            }       
//        }
//        
//        out = (Chromosome)in.clone();
//        out.setVals(notVal, maxCSPval);
//        return out;
//    }

     /**
     * inter race crossover - offers crossover between 2 different constraint regions only
     * the offspring will have better or same constraint violation than their parents.
     * This process requires 2 parents that produce 2 offspring
     * @param parents list of parents
     * @param bForced forced marriage - make new complementary couple to get full COP solution.
     * currently only works for COP solution - because it is easy to code. <BR>
     * Drawback: append works fine when high constraints are appended first but
     * in this case a non CSP can marry a low constraint early and leftover high
     * constraints are added later (that may not be possible because of high constraint)
     * @return returns offspring
     * @throws MyException
     * @throws UnsupportedDataTypeException
     */
    private ArrayList<Chromosome> interRaceCrossover(final ArrayList<Chromosome> parents, boolean bForced, boolean bIsCSP) throws MyException, UnsupportedDataTypeException,  SolutionFoundException{
        ArrayList<Chromosome> candidates = new ArrayList<Chromosome>(this.tourSize_);
        ArrayList<Chromosome> offspring = new ArrayList<Chromosome>();
        ArrayList<Integer> tempIntAL; 

        //ArrayList<Double> directions;
        //ArrayList<Double> approachDist = new ArrayList<Double>(1);
        
        //double maxDist;
        //double ratio;
        int count;
        
        if(this.tourSize_ != 2){
            throw new MyException("Tour Should be 2", "Inappropriate Tour Size",JOptionPane.ERROR_MESSAGE);
        }
        
        if(parents.isEmpty()){
            System.out.println("Sigh! no parents!");
        }
        
        //reset all whoAmI
        int tmpWho = 0;
        if(chromosomes_ == parents){
            for (Chromosome p : parents) {
                p.whoAmI = tmpWho++;
//                p.reportNoProgress(); // it will be reversed if selected below
            }
        }else{
            for (Chromosome p : parents) {
                p.whoAmI = -1;//cannot figure out
//                p.reportNoProgress(); // it will be reversed if selected below
            }
        }

        //you may change selection criteria eg. se negExp.
        
        for (int i = 0; i < userInput_.population/2; i++) {
            if(Math.random() < 1.0){ // && !(candidates.get(0).isSolution() && candidates.get(1).isSolution())){
                //Randomly pick two parents.
                tempIntAL = MyRandom.randperm(0, parents.size());
                candidates.clear();
                Chromosome parent;

                
                for (int t = 0; t < this.tourSize_; t++) {                
                    parent = (Chromosome)parents.get(tempIntAL.get(t));
//                    parent.reverseNoProgress();
//                    parent.whoAmI = tempIntAL.get(t); //redundant
                    candidates.add(parent);
                }
                
//                force marriage with uncommon ones....
//                diff between CSP and COP vals and get them married [and see the result before accepting it]
//                int rm = 0;  
                int rm = 1;
                if(bForced){
                    //0 is better than 1.
                    if(candidates.get(0).isMorePromisingThan(candidates.get(1))){
//                        rm = 1;
                        rm = 0;
                    }
                    candidates.set(rm, (Chromosome)getBestSoFarCOP().clone());
                    candidates.get(rm).whoAmI = -1;
//                    ArrayList missing= MyMath.getMissingVals(candidates.get(0).getVals(), userInput_.total__updatedConstraints-1);
      
//                    if(missing.size()>0){
//                        ArrayList<Double> newVals = new ArrayList<Double>();
//                        Integer val = (Integer)missing.subList(0, 1).get(0);
//                        newVals.add((double)val);
//                        Chromosome cNew = null;
//                        try {
//                            cNew = initializeChromosome();
//                        } catch (Exception ex) {
//                            System.err.println("Cannot Initialize a new Chromosome.");
//                            Application.getInstance().exit();
//                        }
//                        cNew.setVals(newVals);
//                        candidates.set(rm, cNew);
//////////                        candidates.get(rm).setVals(newVals); 
//                        candidates.get(rm).whoAmI = -1;
//                    }
                }
//////                if(i==0){
//////                    candidates.clear();
//////                    getBestSoFarCOP().whoAmI = -1;
//////                    candidates.add(getBestSoFarCOP());
//////                    getBestSoFarCSP().whoAmI = -1;
//////                    candidates.add(getBestSoFarCSP());
//////                }

                try{
                    //Note here we can make integer and double combined problem
                    //set as well.
                    if(dataType_.contains("Integer")){
                        count = this.tourSize_;
                        //while both parents belong to same Constraint region
                        //Drawback - this will case very few crossover + very few
                        //final solutions. That might affect the optimization
                        //problem where we need many candidate solutions.                 

//                        while(candidates.get(0).hasSameRankComponent(candidates.get(1))){// &&
//                                //candidates.get(0).getRank() == candidates.get(1).getRank()){
//                            candidates.remove(1);
//                            candidates.add(parents.get(tempIntAL.get(count)));
//                            count++;
//                            if(count >= parents.size()){
//                                break;                                    
//                            }
//                        }  
                        
                        while((candidates.get(0).isSolution() && candidates.get(1).isSolution())){
                            candidates.remove(1);
                            parent = parents.get(tempIntAL.get(count));
//                            parent.whoAmI = tempIntAL.get(count);
                            candidates.add(parent);
                            count++;
                            if(count >= parents.size()){
                                break;                                    
                            }
                        }
                        if(bIsCSP)
                            offspring.addAll(interRaceCrossoverIntegerCSP(candidates));//only 1 move
                        else
                            offspring.addAll(twoPointCrossoverCOP(candidates));
                        
                        
                    }else if(dataType_.contains("Double")){
                        //further filter for boundary intersections... 
                        count = this.tourSize_;

                        while(!candidates.get(0).isMarriageCompatible(candidates.get(1))){
                            candidates.remove(1);
                            candidates.add((Chromosome)parents.get(tempIntAL.get(count)).clone());
                            count++;
                            if(count >= parents.size()){                            
                                break;                            
                            }
                        }
                        
                        
                        if(bIsCSP)
                            offspring.addAll(interRaceCrossoverDouble(this.MAX_MOVES, candidates));
                        else
                            throw new UnsupportedOperationException("Not implemented yet!");
						//NOTE: if input var size is very big you may want to use the following instead of top line.
                        //if(userInput_.totalDecisionVars < 10)
                            //offspring.addAll(interRaceCrossoverDoubleStagnant(this.MAX_MOVES, candidates));
                        //else
                            //offspring.addAll(interRaceCrossoverDouble(this.MAX_MOVES, candidates));
                        
                        
                        
                        
                    }else{
                        throw new UnsupportedDataTypeException("Only supports Integer and Double");
                    }         

                }catch (UnsupportedDataTypeException udte) {
                    throw new UnsupportedDataTypeException("Check your data type");
                }
//                catch (MyException me){
//                    me.printMessage();
//                }
            }
        }
        
        return offspring;
    }

    
//    /**
//     * removal of some elements is certain but it is not necessary that the 
//     * chromosome may remain solution for CSP because addition may not be possible.
//     * @param c NOTE: c is modified.
//     * @return
//     * @throws SolutionFoundException 
//     */   
//    private void interRaceCrossoverIntegerCOP(Chromosome c, final double ro, final double aff) throws SolutionFoundException{
//        int L; //left and right indexes
//        int PLD, PLU; //left and right points down and up positions
//        ArrayList<Integer> PL;
//        int iL, jR;
//        boolean bViolated;
//        PL = new ArrayList<Integer>();
//        double alpha; //mutation rate
//        int mutationFreq;
//        
//        alpha = 1-Math.exp(-ro*aff); 
//        final int changeAllowed = 5;
//        mutationFreq = (int)Math.min(Math.floor(alpha*c.getValsCopy().size()),changeAllowed);
//
//        ArrayList<Integer> randlist;
//        ArrayList<Integer> grpList = MyRandom.randperm(0, c.satisfactions_.size()-1);       
//         
//
//        
//        for (int f = 0; f < mutationFreq; f++) {
//            L = grpList.get(f);
//            PL = new ArrayList<Integer>();
//            if(!c.satisfactions_.get(L).isEmpty()){
//                randlist = MyRandom.randperm(0, c.satisfactions_.get(L).size()-1);
//                PLD = 0;
//                PLU = 0;
//                if(randlist.size()>0){
//                    PLD = randlist.get(0);
//                }         
//                if(randlist.size()>1){
//                    PLU = randlist.get(1);
//                    if(PLU<PLD){
//                        PLD = randlist.get(1);
//                        PLU = randlist.get(0);
//                    }
//                }
//                
//                PL.addAll(c.satisfactions_.get(L).subList(PLD, PLU+1));            
//                for (int j = PLD; j <= PLU; j++) {
//                    c.satisfactions_.get(L).remove(PLD);
//                }
//         
//            
//                iL = 0;
//                jR = c.satisfactions_.size()-1;
//        
//                //L->R
//                while(iL < jR){            
//                    iL++;
//                    if(iL == L)
//                        continue;
//                
//                    bViolated = false;
//                    for (Integer valFrom : PL) {
//                        for (Object existingVal : c.satisfactions_.get(iL)) {//Integer
//                            if(externalData_.isViolated(existingVal, valFrom)){
//                                bViolated = true;
//                                break;
//                            }
//                        }
//                        if(bViolated)
//                            break;
//                    }
//
//                    if(!bViolated){
//                        for (int k = 0; k < PL.size(); k++) {                     
//                            c.satisfactions_.get(iL).add(PL.get(k));                 
//                        }  
//                        break;
//                    }                      
//                }
//            }
//        }
//         
//        c.refreshFitness(maxCSPval);
//    }

    
        
    /**
     * I think it is certain that the chromosomes val size will remain intact
     * because removed values are dumped again to same place if they are not
     * reallocated.
     * @param c NOTE: c is modified.
     * @return
     * @throws SolutionFoundException 
     */   
    private void interRaceCrossoverIntegerCOP(Chromosome c, final int maxIteration, final boolean bClustered, Integer ... idx) throws SolutionFoundException{
        int L; //left and right indexes
        int PLD, PLU; //left and right points down and up positions
        ArrayList<Integer> PL;
        boolean bViolated = false;


        ArrayList<Integer> randlistUp = MyRandom.randperm(0, c.satisfactions_.size());                       
        ArrayList<Integer> randlist;
        
        boolean predefinedPara = false;
        
        if(idx.length>0){
            if(idx.length !=2 || maxIteration != 1){
                throw new SolutionFoundException("Error! No solution found");
            }else{
                predefinedPara = true;
            }
        }
        
        ArrayList<Integer> tmpColumn;
        ArrayList<Integer> grpCluster;
//        int tmp;
        for (int m = 0; m < maxIteration; m++) {            
            L = randlistUp.get(m);              
            if(predefinedPara)
                L = idx[0];
            PL = new ArrayList<Integer>();
            
            if(!c.satisfactions_.get(L).isEmpty()){
                if(predefinedPara){
                    PLD = idx[1];
                    PLU = PLD;
                }else{
                    randlist = MyRandom.randperm(0, c.satisfactions_.get(L).size());
                    PLD = 0;
                    PLU = 0;
                    if(randlist.size()>0){
                        PLD = randlist.get(0);
                    }         
                    if(randlist.size()>1){
                        PLU = randlist.get(1);
                        if(PLU<PLD){
                            PLD = randlist.get(1);
                            PLU = randlist.get(0);
                        }
                    }
                }
                
                grpCluster = new ArrayList<Integer>();
                tmpColumn = externalData_.getCluster(c.satisfactions_.get(L)); //note getting cluster for the unchanged column. 
                                                                                    //Need this cluster because refilling it at the end
                                                                                    //after failures.

//                PLU = PLD; ///*****************************************************************************
                PL.addAll(c.satisfactions_.get(L).subList(PLD, PLU+1));            
                for (int i = PLD; i <= PLU; i++) {
                    c.satisfactions_.get(L).remove(PLD);
                }
       
                if(bClustered){
                    //grpCluster = new ArrayList<Integer>();
                    //tmpColumn = externalData_.getCluster(c.satisfactions_.get(L)); //causes duplicates from PL.... note getting cluster after "deletion" above

                    if(!tmpColumn.isEmpty()){                        
                        for (int iL : MyRandom.randperm(0,tmpColumn.size())) {                    
                            grpCluster.add(tmpColumn.get(iL));
                            if(grpCluster.size()>= PL.size()){
                                break;
                            }
                        }
                        c.satisfactions_.get(L).addAll(grpCluster);
                    }
                
                    for (Integer tmp : grpCluster) { //since we hae dupicate values now. delete.
                        for (int i = 0; i < c.satisfactions_.size(); i++) {
                            if(c.satisfactions_.get(i).indexOf(tmp) >=0 && i != L){
                                c.satisfactions_.get(i).remove((Object)tmp);
                                break;//there is at most one/column. so break;
                            }
                        }
                    }
                    
                
                }
                
                //L->R
                for (int iL : MyRandom.randperm(0,c.satisfactions_.size())) {                                    
                    if(iL == L)
                        continue;

                    //<<//Try reinserting removed values stored in PL.
                    bViolated = false;
                    Integer val;
                    for (int i =0; i<PL.size(); i++) {
                        val = PL.get(i);
                        for (Object existingVal : c.satisfactions_.get(iL)) {//Integer
                            if(externalData_.isViolated(existingVal, val)){
                                bViolated = true;
                                break;
                            }
                        }
                        if(!bViolated){
                            c.satisfactions_.get(iL).add(val);
                            PL.remove(i);
                            i--;
                        }
                    }  
                    //>>
                }
                //dump the remainder back.
                c.satisfactions_.get(L).addAll(PL);
            }
        }
        
        c.refreshFitness(true);
    }
    
    /**
     * removal of some elements is certain but it is not necessary that the 
     * chromosome may remain solution for CSP because addition may not be possible.
     * @param c NOTE: c is modified.
     * @return
     * @throws SolutionFoundException 
     */   
    private void interRaceCrossoverIntegerCOPII(Chromosome c, final int maxIteration, Integer ... idx) throws SolutionFoundException{
        int L; //left and right indexes
        int PLD, PLU; //left and right points down and up positions
        ArrayList<Integer> PL;
        boolean bViolated = false;


        ArrayList<Integer> randlistUp = MyRandom.randperm(0, c.satisfactions_.size());                       
        ArrayList<Integer> randlist;
        
        boolean predefinedPara = false;
        
        if(idx.length>0){
            if(idx.length !=2 || maxIteration != 1){
                throw new SolutionFoundException("Error! No solution found");
            }else{
                predefinedPara = true;
            }
        }
        
        for (int m = 0; m < maxIteration; m++) {            
            L = randlistUp.get(m);              
            if(predefinedPara)
                L = idx[0];
            PL = new ArrayList<Integer>();
            
            if(!c.satisfactions_.get(L).isEmpty()){
                if(predefinedPara){
                    PLD = idx[1];
                    PLU = PLD;
                }else{
                    randlist = MyRandom.randperm(0, c.satisfactions_.get(L).size());
                    PLD = 0;
                    PLU = 0;
                    if(randlist.size()>0){
                        PLD = randlist.get(0);
                    }         
                    if(randlist.size()>1){
                        PLU = randlist.get(1);
                        if(PLU<PLD){
                            PLD = randlist.get(1);
                            PLU = randlist.get(0);
                        }
                    }
                }
//                PLU = PLD; ///*****************************************************************************
                PL.addAll(c.satisfactions_.get(L).subList(PLD, PLU+1));            
                for (int i = PLD; i <= PLU; i++) {
                    c.satisfactions_.get(L).remove(PLD);
                }
       
                //L->R
                for (int iL : MyRandom.randperm(0,c.satisfactions_.size())) {                                    
                    if(iL == L)
                        continue;

                    //<<
//                    bViolated = false;
//                    for (Integer val : PL) {
//                        for (Object existingVal : c.satisfactions_.get(iL)) {//Integer
//                            if(externalData_.isViolated(existingVal, val)){
//                                bViolated = true;
//                                break;
//                            }
//                        }
//                        if(bViolated)
//                            break;
//                    }
//
//                    if(!bViolated){
//                        for (int k = 0; k < PL.size(); k++) {                     
//                            c.satisfactions_.get(iL).add(PL.get(k));                 
//                        }  
//                        break;
//                    }  
                    //>>

                    //<<
                    bViolated = false;
                    Integer val;
                    for (int i =0; i<PL.size(); i++) {
                        val = PL.get(i);
                        for (Object existingVal : c.satisfactions_.get(iL)) {//Integer
                            if(externalData_.isViolated(existingVal, val)){
                                bViolated = true;
                                break;
                            }
                        }
                        if(!bViolated){
                            c.satisfactions_.get(iL).add(val);
                            PL.remove(i);
                        }
                    }  
                    //>>
                }
            }
        }
        c.refreshFitness();
    }
    /**
     * removal of some elements is certain but it is not necessary that the 
     * chromosome may remain solution for CSP because addition may not be possible.
     * @param c NOTE: c is modified.
     * @return
     * @throws SolutionFoundException 
     */   
//    private void interRaceCrossoverIntegerCOP(Chromosome c) throws SolutionFoundException{
//        int L; //left and right indexes
//        int PLD, PLU; //left and right points down and up positions
//        ArrayList<Integer> PL;
//        boolean bViolated;
//        PL = new ArrayList<Integer>();
//        double curFitness = c.getFitnessVal(0);
//
//        ArrayList<Integer> randlist = MyRandom.randperm(0, c.satisfactions_.size()-1);
//        L = randlist.get(0);                        
//         
//        if(!c.satisfactions_.get(L).isEmpty()){
//            randlist = MyRandom.randperm(0, c.satisfactions_.get(L).size()-1);
//            PLD = 0;
//            PLU = 0;
//            if(randlist.size()>0){
//                PLD = randlist.get(0);
//            }         
//            if(randlist.size()>1){
//                PLU = randlist.get(1);
//                if(PLU<PLD){
//                    PLD = randlist.get(1);
//                    PLU = randlist.get(0);
//                }
//            }
//
//            PL.addAll(c.satisfactions_.get(L).subList(PLD, PLU+1));            
//            for (int j = PLD; j <= PLU; j++) {
//                c.satisfactions_.get(L).remove(PLD);
//            }
//         
//            for (int j = 0; j < c.satisfactions_.size(); j++) { 
//                if(j==L)
//                    continue;
//                
//                bViolated = false;
//                for (Integer valFrom : PL) {
//                    for (Object existingVal : c.satisfactions_.get(j)) {//Integer
//                        if(externalData_.isViolated(existingVal, valFrom)){
//                            bViolated = true;
//                            break;
//                        }
//                    }
//                    if(bViolated)
//                        break;
//                }
//
//                if(!bViolated){
//                    for (int k = 0; k < PL.size(); k++) {                     
//                        c.satisfactions_.get(j).add(PL.get(k));                 
//                    }  
//                    c.refreshFitness(maxCSPval);
//                    if(c.getFitnessVal(0)<=curFitness)
//                        break;
//                    else{
//                        int firstVal = c.satisfactions_.get(j).size()-PL.size();
//                        int newSize = c.satisfactions_.get(j).size();
//                        for (int j = firstVal; j < newSize; j++) {
//                            c.satisfactions_.get(j).remove(firstVal);
//                        }
//                    }                     
//                }                      
//            }
//            c.refreshFitness(maxCSPval); 
//        }
//               
//        
//    }
//   
    
    private ArrayList<Chromosome> twoPointCrossoverCOP(final ArrayList<Chromosome> candidates) throws SolutionFoundException{
        ArrayList<Chromosome> offspring = new ArrayList<Chromosome>();
        
        if (candidates.size() != 2){
            throw new UnsupportedOperationException("Require only 2 parents");
        }
        Chromosome tempChrome;
//        tmp = (Chromosome)candidates.get(0).clone();
//        candidates.get(0).twoPointCrossoverWith(candidates.get(1), false);
//        candidates.get(1).twoPointCrossoverWith(tmp, false);
        
        for (int j = 0; j < candidates.size(); j++) {
            tempChrome = (Chromosome)candidates.get(j%tourSize_).clone();
            tempChrome.whoAmI = candidates.get(j%tourSize_).whoAmI+userInput_.population;

            tempChrome.twoPointCrossoverWith(candidates.get((j+1)%tourSize_), false);

            offspring.add(tempChrome);
        }
        return offspring;
    }
    /**
     * interRaceCrossoverIntegerCSP - is used only with nominal data types. for integer data
     * use interRaceCrossoverDouble. it virtually moves 2 parents.
     * @param move
     * @param candidates - parents from which offspring are sought.
     * @return returns offspring from given candidate parents
     */
    private ArrayList<Chromosome> interRaceCrossoverIntegerCSP(final ArrayList<Chromosome> candidates) throws SolutionFoundException{
        ArrayList<Chromosome> offspring = new ArrayList<Chromosome>();
        ArrayList<Integer> idx = new ArrayList<Integer>();
        ArrayList<Double> noGoods;
        Chromosome tempChrome;
        int move;
        boolean isSol;
        
        if (candidates.size() != 2){
            throw new UnsupportedOperationException("Require only 2 parents");
        }
        
        isSol = false;
        for (Chromosome can : candidates) {
            if(can.isSolution()){
                isSol = true;
                break;
            }
        }
        
        //Check common values in both candidate parents.
        int [] commonVals = new int[userInput_.totalConstraints]; //all initialized to 0
        int constVal = 1;
        
        
        for (int j = 0; j < candidates.size(); j++) { // checking for duplicate values
            for (double v : candidates.get(j).getVals()) { //Big O order: O(l1) + O(l2) 
                commonVals[(int)v] += constVal;
            }
            constVal = constVal*10;
        }
        //Those commonVals that have value 11 as element value, it means that is common in both parents.
        //otherwise it will have 1 or 10 respectively for both parents.
       
        int prevLength, newLength;
        //Technique 1 - Append chromosomes- multi-offpring (0-n) afrom 2 parents. - 
        //<< Build up structure for satisfaction list
//        if(Math.random() < 0.5){//5 && !isSol){ //!bStagnant){ //obviously Math.random is always [0 1)
//        if(!bOptimizationMode){
        
        double rate = 2.0;
        
//        if(bOptimizationMode)
//            rate = 0.5;
        
//        final int maxSz = (int)Math.ceil(userInput_.population*0.9);
        if(Math.random()<rate){ // && !(candidates.get(0).isSolution() && candidates.get(1).isSolution())){//!isSol){candidates.get(0).getValsCopy().size()<maxSz || candidates.get(1).getValsCopy().size()<maxSz){ //
            constVal = 1;
           
            for (int j = 0; j < candidates.size(); j++) {
                tempChrome = (Chromosome)candidates.get((j+1)%tourSize_).clone();
                tempChrome.whoAmI = candidates.get((j+1)%tourSize_).whoAmI+userInput_.population;
//                prevLength = tempChrome.getVals().size();
                
                for (int i = 0; i < commonVals.length; i++) { //orer - O(n) --- // checking for duplicate values
                    if(commonVals[i]==constVal){ //10+1 = 11 will automatically be discarded because Checking for (10 or 1 only)
                        tempChrome.appendVal(i);//NOTE ofsp want getSatisfaction value but in this case both are same
                    }                    
                }

//                newLength = tempChrome.getVals().size();                
//                if(newLength < prevLength){ //val added
//                    noGoods = tempChrome.findNoGoods();
//                    for (int iL = 0; iL < noGoods.size(); iL++) {                    
//                        tempChrome.appendVal(noGoods.get(iL), maxCSPval);
//                    }
//                }
                offspring.add((Chromosome)tempChrome);
                constVal = constVal*10;
            }
        }
        else{                                    
            //code removed....
        }

        
        return offspring;
    }
   
    private void forceAppendNoGood(Chromosome ch){
//        ArrayList<Double> noGoods;
//        noGoods = ch.findNoGoods();
//                    for (int iL = 0; iL < noGoods.size(); iL++) {                    
//                        tempChrome.appendVal(noGoods.get(iL), maxCSPval);
//                    }
//                }
//                offspring.add((Chromosome)tempChrome);
    }
    
    
    /**
     * highest valid index of CSPsols.
     * It is possible that the index = highest+1 is in buildup process and 
     * not available for usage.
     * 
     * @return highest valid index. -1 means no valid index available
     */
    private int maxIdxAvailCSPsols(){        
        int highestIndx = CSPsols.size()-1; //can be 1 initially.        
        
        for (ArrayList<Double> valGrp: CSPsols.get(highestIndx)) {
            if(valGrp.isEmpty()){
                highestIndx--; //it can become -1
                break;
            }
        }
        
        //last index can get cleared in CCSPfns so we take the second last
        
        if(highestIndx>0){
            highestIndx--;
        }
        
        return highestIndx;
    }
        
    
    /**
     * interRaceCrossoverDouble - can be used for interger or double data types.
     * double crossover reqires 2 parents and generate 2 offspring
     * process: the original genes of parents are moved closer to each other until
     * the better or same ofsp.e. (less or equal violations) is reached. the number
     * of moves is determined by  move parameter
     * @param move - number of maximum moves until the better/same solution is reached
     * @param candidates Two parents
     * @return Two offspring
     * @throws UnsupportedDataTypeException
     */
    
    private ArrayList<Chromosome> interRaceCrossoverDouble(final int move, final ArrayList<Chromosome> candidates) throws SolutionFoundException, UnsupportedDataTypeException{
        ArrayList<Double> delta;
        ArrayList<Double> newDelta;
        ArrayList<Chromosome> offspring = new ArrayList<Chromosome>();
        Chromosome childChrome = null;

        if (candidates.size() != 2){
            throw new UnsupportedOperationException("Require only 2 parents");
        }
      
        Chromosome p1 = null, p2=null; //parent 1 and parent 2;
        int pickedCons = -1;
        int randPickIdx = 0; //0 is always available... worst case can be empty..
        final int highestValidCSPidx = maxIdxAvailCSPsols();
        int k;
        int trials = 0;
        boolean bMoved = false;  
        ArrayList<Double> initDelta;
        ArrayList<Double> neighborDelta;
        ArrayList<Chromosome> hospital = new ArrayList<Chromosome>();

        ArrayList<String> permutes;  
        ArrayList<ArrayList<Double>> combinations;
        double dtemp;
        boolean isInvalid = false;
        
        
        for (int j = 0; j < this.tourSize_; j++) {            
            p1 = candidates.get(j);
            if(highestValidCSPidx >= 0 && Math.random()<FORCED_PERCENT && !p1.isSolution() && p1.getRankComponents().size()>0){                                               
                pickedCons = MyRandom.randperm(0, p1.getRankComponents().size()).get(0);
                pickedCons = p1.getRankComponents().get(pickedCons);
                
                p2 = new Chromosome(this.userInput_.solutionBy,  userInput_);
                randPickIdx = MyRandom.randperm((int)Math.floor(0.5*highestValidCSPidx), highestValidCSPidx+1).get(0);
                try{ //
                    p2.setVals(CSPsols.get(randPickIdx).get(pickedCons));   
                }catch(IndexOutOfBoundsException iobe){ //temp measure... see below for reason
                    //happens when dynmic tabu constraints are introduced, and CSPsols repertoir does not carry the solutions for these new constraints    
                    p2 = null;
                }
                
            }else{
                if(highestValidCSPidx == -1){ //I use then when it is difficult to find CSP
                    for (int i = 0; i < p1.getRankComponents().size(); i++) {
                        pickedCons = p1.getRankComponents().get(i);                        
                        
                        if(CSPsols.get(0).get(pickedCons).isEmpty()){
                            p2 = null;
                            continue;
                        }else{
                            p2 = new Chromosome(this.userInput_.solutionBy,  userInput_);
                            p2.setVals(CSPsols.get(0).get(pickedCons));
                            break;
                        }
                    }
                }                                               
                if(p2 == null)
                    p2 = candidates.get((j+1)%tourSize_);  
            }            
                       
            if(p2 == null)
                p2 = candidates.get((j+1)%tourSize_);  
            
            initDelta = new ArrayList<Double>(MyMath.vectorSubtraction(p2.getVals(), p1.getVals()));
            hospital.clear();
            combinations = MyMath.getXrandomBinPermutes(userInput_.totalDecisionVars, MaxComb);
            isInvalid = false;
            for(ArrayList<Double> dims: combinations){
                isInvalid = false;
                                
                k = 0;

                neighborDelta = MyMath.vectorMultiplication(true, dims, initDelta);
                
                p2 = new Chromosome(this.userInput_.solutionBy, this.userInput_);
                p2.setVals(MyMath.vectorAddition(p1.getVals(), neighborDelta));

                delta = neighborDelta; //MyMath.vectorSubtraction(p2.getValsCopy(), p1.getValsCopy());
                
                ArrayList<Double> prevVals = null, newVals=null;
                int forceFind = 0;
                do{
                    for (k = 1; k <= move; k++){ //) move; k++) {
                        //find which direction to move?
                        childChrome = new Chromosome(this.userInput_.solutionBy, this.userInput_);                                        
                        newDelta = MyMath.constMultiplicationToVector(Math.pow(bringCloserRatio,k), delta); 
                        newVals = MyMath.vectorAddition(p1.getVals(), newDelta);
                        childChrome.setVals(newVals);
                   
                        bMoved = false;

                        if(!childChrome.myParent(p1)){ //there is a gap/black hole so no idx1 searching further.
                            hospital.add(childChrome);
                            bMoved = true;
                            break;
                        }
                        if(childChrome.isSolution() || childChrome.getRank()<=p1.getRank() || bStagnant){
                            hospital.add(childChrome);
                            if(childChrome.isSolution()){
                                setBestSoFarCOP((Chromosome)childChrome.clone());                                    
                                throw new SolutionFoundException("Sol found during crossover...");
                            }
                            bMoved = true;
                            break;
                        }
                        prevVals = newVals;
                    }
                    if(bMoved && k>=1){
                        p1  = childChrome;
                        delta = MyMath.vectorSubtraction(p2.getVals(), p1.getVals());
                    }else{
                        forceFind = 1;
                    }
                    forceFind++;
                }while(forceFind < 1);
            }
            
            if(!hospital.isEmpty()){
                Collections.sort(hospital);                
                offspring.add(hospital.get(0)); //(Chromosome)hospital.get(0).clone());
                hospital.clear();
            }            
        }

        
        return offspring;
    }
    
    
//    private ArrayList<Chromosome> interRaceCrossoverDouble(final int move, final ArrayList<Chromosome> candidates) throws SolutionFoundException, UnsupportedDataTypeException{
//        ArrayList<Double> delta;
//        ArrayList<Double> newDelta;
//        ArrayList<Chromosome> offspring = new ArrayList<Chromosome>(tourSize_);
//        Chromosome childChrome = null;
//
//        if (candidates.size() != 2){
//            throw new UnsupportedOperationException("Require only 2 parents");
//        }
//      
//        Chromosome p1 = null, p2=null; //parent 1 and parent 2;
//        int pickedCons = -1;
//        int randPickIdx = 0; //0 is always available... worst case can be empty..
//        final int highestValidCSPidx = maxIdxAvailCSPsols();
//        
//        int trials = 0;
//        boolean bMoved = false;
//        
//        for (int jR = 0; jR < this.tourSize_; jR++) {
//            //directions = MyAlgorithms.getDirection(candidates.get(jR).getValsCopy(), candidates.get((jR+1)%tourSize_).getValsCopy());
//            //maxDist = MyMath.norm(candidates.get(jR).getValsCopy(), candidates.get((jR+1)%tourSize_).getValsCopy(), MyMath.DIST_EUCLEADIAN);
//        
//            
//            p1 = candidates.get(jR);
//            
//            if(highestValidCSPidx >= 0 && Math.random()<0.5 && !p1.isSolution()){                        
//                pickedCons = MyRandom.randperm(0, p1.getRankComponents().size()-1).get(0);
//                pickedCons = p1.getRankComponents().get(pickedCons);
//                
//                p2 = new Chromosome(this.userInput_.solutionBy,  userInput_);
//                randPickIdx = MyRandom.randperm(0, highestValidCSPidx).get(0);
//                p2.setVals(CSPsols.get(randPickIdx).get(pickedCons), maxCSPval, CSPsols);   
//                
//            }else{
//                if(highestValidCSPidx == -1){ //I use then when it is difficult to find CSP
//                    for (int iL = 0; iL < p1.getRankComponents().size(); iL++) {
//                        pickedCons = p1.getRankComponents().get(iL);
//                        
//                        
//                        if(CSPsols.get(0).get(pickedCons).isEmpty()){
//                            p2 = null;
//                            continue;
//                        }else{
//                            p2 = new Chromosome(this.userInput_.solutionBy,  userInput_);
//                            p2.setVals(CSPsols.get(0).get(pickedCons), maxCSPval, CSPsols);
//                            break;
//                        }
//                    }
//                }                               
//                
//                if(p2 == null)
//                    p2 = (Chromosome)candidates.get((jR+1)%tourSize_).clone();                
//            }            
//            
//
//            delta = MyMath.vectorSubtraction(p2.getValsCopy(), p1.getValsCopy());
//            int k = 0;
//            ArrayList<Double> prevVals = null, newVals=null;
//            int forceFind = 0;
//            do{
//                for (k = 1; k <= move; k++){ //) move; k++) {
//                    //find which direction to move?
//                    childChrome = new Chromosome(this.userInput_.solutionBy, this.userInput_);
//                    //approachDist = Math.pow(ratio,k)*maxDist;                
//                    newDelta = MyMath.constMultiplicationToVector(Math.pow(bringCloserRatio,k), delta); 
//                    newVals = MyMath.vectorAddition(p1.getValsCopy(), newDelta);
//                    childChrome.setVals(newVals, maxCSPval, CSPsols);
//
//                    //vp = new VirusProliferate(movingChrome.vals.toArray(), this.range_);
//
//                    //**************************************************************************************************************//
//                    //NOTE: ofsp changed <= sign to < sign
//                    //It is now giving me less solutions
//                    //It is good or bad......... I don't know.... It only promotes local search.
//                    //if(childChrome.getRank() < p1.getRank()){// || (childChrome.getRank() <= p1.getRank() && move == 1)){                    
//                        bMoved = false;
//                        
//                        if(!childChrome.myParent(p1)){ //there is a gap/black hole so no idx1 searching further.
//                            bMoved = false;
//                            break;
//                        }
//                        if(childChrome.isSolution() || p1.isMyChild(childChrome)){
//                            offspring.add(childChrome); // if using do while then put this line in correct place.
//                            if(childChrome.isSolution()){
//                                ;//throw new SolutionFoundException("Sol found during crossover...");
//                            }
//                            bMoved = true;
//                            break;
//                        }
//                   // }
//
//                    prevVals = newVals;
//
//                }
//                if(bMoved && k>=1){
//                    p1  = childChrome;
//                    delta = MyMath.vectorSubtraction(p2.getValsCopy(), p1.getValsCopy());
////                }else if(bMoved && k>=2){
////                    p1.setVals(prevVals, maxCSPval, CSPsols);
////                    p2.setVals(newVals, maxCSPval, CSPsols);
////                    delta = MyMath.vectorSubtraction(p2.getValsCopy(), p1.getValsCopy());
//                }else{
//                    forceFind = 1;
//                }
//                forceFind++;
//            }while(forceFind < 1);
//            //chk boundary...
//
//            
////            if(!bMoved && trials < 5 && highestValidCSPidx >= 0){
////                trials++;
////                jR--;
////            }
////            
////            if(!bMoved && trials >= 5 && highestValidCSPidx >= 0){
////                trials = 0;
////                candidates.set(++jR, p2);
////            }
//            
//        }
//        return offspring;
//    }



     /**
     * mutationDouble only mutate Doubles. It uses Polynomial Mutation as described in NSGA - II <br>
     * <B>Note</B> that offspring ArrayList is updated here.
     * @param offspring offspring generated after crossover.
     */
    private void mutationDouble(ArrayList<Chromosome> offspring) throws SolutionFoundException{
        int size = offspring.size();
        ArrayList<Integer> randInts;
        Chromosome temp;
        double val;
        double rand;
        double add;
        int muteBits = (int)Math.ceil(0.1*userInput_.totalDecisionVars); //10%
        
        for (int i = 0; i < size; i++) {
            
            try{
                if(Math.random()<1.0/userInput_.totalDecisionVars){
                    randInts = MyRandom.randperm(0, size);
                    temp = offspring.get(randInts.get(0));                   

                    for (int j : MyRandom.randperm(0, userInput_.totalDecisionVars).subList(0, muteBits)){                   
                    //for (int jR = 0; jR < userInput_.totalDecisionVars; jR++) {
                        val = temp.getVals(j);
                        rand = Math.random();
                        if(rand<0.5)
                            add = Math.pow(2.0*rand,1.0/(MUM+1)) -1;
                        else
                            add = 1- Math.pow(2.0*(1-rand),1.0/(MUM+1));

                        val = val+add;

                        if(val>userInput_.maxVals.get(j))
                            val = userInput_.maxVals.get(j);
                        else if(val<userInput_.minVals.get(j))
                            val = userInput_.minVals.get(j);

                        temp.replaceVal(j, val); 
                    }               
                }  
            }catch (SolutionFoundException sfe){
                throw sfe;
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 
     * @param c1
     * @param isMutable1
     * @param c2
     * @param isMutable2
     * @param cardinality
     * @param freq
     * @throws SolutionFoundException 
     */
    private void crossoverKempe(final Chromosome c1, final boolean isMutable1, 
    final Chromosome c2, final boolean isMutable2, final int cardinality, final int freq) 
    throws SolutionFoundException{
        if(!c1.isSolution() || !c2.isSolution()){
            return;
        }
        
        int sz = Math.min(c1.getVals().size(), c2.getVals().size());
        sz = Math.min(sz, cardinality);
        int v1, v2;
        int Tj1, Tj2;
        Element takenVal1, takenVal2;
        
        for (int i = 0; i < sz; i++) {
            v1 = c1.getVals((int)(Math.random()*c1.getVals().size())).intValue();
            v2 = c2.getVals((int)(Math.random()*c2.getVals().size())).intValue();
            
            Tj1 = c2.valVsConstIdx_[v2].col;
            Tj2 = c1.valVsConstIdx_[v1].col;
            
            takenVal1 = new Element(v2, Tj1); //v2 and its corresponding column in sat          
            takenVal2 = new Element(v1, Tj2); //v1 and its corresponding column in sat
            
            if(isMutable1)
                c1.mutationKempe(0, 0, false, false, freq, takenVal1, false, true);
            if(isMutable2)
                c2.mutationKempe(0, 0, false, false, freq, takenVal2, false, true);
        }
    }
    
    /**
     * <B>WARNING:</B> this one can bring "hard constraint violations" into the system.
     * This one has proved to be very good operator tested for TT.
     * @param ch input chromosome
     * @param changeBestSoFar - allow best so far to be changed. Note: it may be infeasible
     * @return
     * @throws SolutionFoundException 
     */
    private boolean mutationSwapCOP(final Chromosome ch) throws SolutionFoundException{
        
        ArrayList<Integer> rnd = MyRandom.randperm(0, ch.satisfactions_.size());
        final int g1 = rnd.get(0); 
        if(ch.satisfactions_.get(g1).isEmpty()){
            return false;
        }
        final int c1 = MyRandom.randperm(0, ch.satisfactions_.get(g1).size()).get(0); 
        
        final int g2 = rnd.get(1);
        if(ch.satisfactions_.get(g2).isEmpty()){
            return false;
        }        
        final int c2 = MyRandom.randperm(0, ch.satisfactions_.get(g2).size()).get(0); 
        
        try{
            
            int []p1 = {g1,c1};
            int []p2 = {g2,c2};  
            ch.swapNrefreshFitness(p1, p2, true);
            
        }catch(IndexOutOfBoundsException iobe){
            throw new SolutionFoundException(iobe.getMessage());
        }
        return true;
    }
    
    /**
     * Currently used to improve partial CSP solutions only
     * @param chrm
     * @param maxSwap
     * @param maxIteration
     * @throws SolutionFoundException 
     */
    private void mutationSwap(Chromosome ch, double maxSwap, final int maxIteration) throws SolutionFoundException{
        ArrayList<Integer> randVal; // = MyRandom.randperm(0, chrm.getValsCopy().size()-1);
        ArrayList<Double> vals = new ArrayList<Double>();
        double val0, val1;
        int lowIdx, hiIdx;
        //double maxSwap = 1; //0.05*chrm.getValsCopy().size();
//        final double maxIteration = 4;
        double bfFitness, afFitness;
        
        if(ch.getVals().size()<2){
            return;
        }
        
        bfFitness = ch.getFitnessVal(0);
        
        for (int j = 0; j < maxIteration; j++) {
            for (int i = 0; i < maxSwap; i++) {
                randVal = MyRandom.randperm(0, ch.getVals().size());

                lowIdx = randVal.get(0);
                hiIdx = randVal.get(1);

                if(lowIdx > hiIdx){
                    lowIdx = randVal.get(1);
                    hiIdx = randVal.get(0);
                }

                val0 = ch.getVals(lowIdx);
                val1 = ch.getVals(hiIdx);

                vals.clear();
                vals.add(val0);
                vals.add(val1);

                try {
                    if(!ch.getVals().isEmpty())
                        ch.remove(hiIdx);
                    if(!ch.getVals().isEmpty())
                        ch.remove(lowIdx);
                    else{
                        j = maxIteration;
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();    
                    System.out.println("uffff... yeh ulfat..");

                }   
            }

            for (Double v : vals) {
                ch.appendVal(v);
            }
        
            afFitness = ch.getFitnessVal(0);
            
            if(afFitness<bfFitness){
                if(afFitness<bfFitness){
                    afFitness = afFitness;
                }
                break;
            }
        }
    }
   
    
    /**
     * remove a gene randomly and <I>try</I> to add it somewhere else.
     * This operation may result in ending up in an <B>non-partial/incomplete</B> solution.
     * @param chrm
     * @param ro the lower the ro value the steeper the exponential curve. that
     * means low ro will generate less clones. Prefered value = 5;
     * @param aff
     * @throws SolutionFoundException 
     */
    private void mutationSinglePoint(final Chromosome ch, double aff) throws SolutionFoundException{
        int idx = -1;
        ArrayList<Integer> randList;
        double alpha; //mutation rate
        int mutationFreq = 0;
        ArrayList<Integer> removedVal = new ArrayList<Integer>();
        Chromosome chbcp = (Chromosome)ch.clone();
        
        try {
        
//        alpha = 1-Math.exp(-ro*aff); 
//        final int changeAllowed = 4;
//        mutationFreq = (int)Math.min(Math.floor(alpha*ch.getVals().size()),changeAllowed);  
//        
//        if(mutationFreq <=0)
//            mutationFreq = 1;
//        else
//            mutationFreq = mutationFreq;
                                                        
        
////////        mutationFreq = Math.min(chrm.getVals().size(), 10);//////////////////????????????????????????????????????
        
        //randList = new ArrayList<Integer>(MyRandom.randperm(0, (int)(ch.getVals().size()*1.0)-1).subList(0, mutationFreq));
        //Collections.sort(randList);
        randList = MyRandom.randperm(0, ch.getVals().size());

        mutationFreq = Math.min(mutationFreq, ch.getVals().size());
                    
        for (int i = 0; i < mutationFreq; i++) {  
            idx = randList.get(mutationFreq-1-i).intValue();//start from last one so that don't have to adjust idx after delete.
            removedVal.add(ch.getVals(idx).intValue());
            ch.remove(idx); 
        }
        
//        for (int i = (int)mutationFreq-1; i >= 0; i--) {  
        for (int i = 0; i < removedVal.size(); i++) {  
            if(getBestSoFarCOP().isSolution())
                ch.appendVal(removedVal.get(i).intValue(), false); 
            else
                ch.appendVal(removedVal.get(i).intValue(), false); //this is in random order internally
        }


        } catch (Exception e) {
            System.out.println("### "+ch.getVals().size());
            System.out.println("### "+removedVal);
            System.out.println("### "+idx);
            System.out.println("### "+mutationFreq);
            System.out.println("### "+chbcp);
            e.printStackTrace();    
            System.out.println("uffff... yeh ulfat..");
            throw new SolutionFoundException(e.getMessage());
        }                
        
    }

    /**
     * At least for Time Tabling problem mutation Group swap will result in 
     * valid solution if the input is valid.
     * @param ch
     * @throws SolutionFoundException 
     */
    private void mutationGroupSwap(final Chromosome ch) throws SolutionFoundException{
        ArrayList<Integer> randVal = MyRandom.randperm(0, ch.satisfactions_.size());
        int Idx0, Idx1;
        
        if(ch.satisfactions_.size()<4){
            throw new SolutionFoundException("Arey cannot apply PMX crossover");
        }
        
//        if(Math.random()<0.1){ 
            ArrayList<Integer> order = new ArrayList<Integer>(randVal.subList(0, 2));
            Collections.sort(order);

            ArrayList<ArrayList<Integer>> temp = new ArrayList<ArrayList<Integer>>();
            for (int i = order.get(1); i < ch.satisfactions_.size(); i++) {
                temp.add(ch.satisfactions_.remove((int)order.get(1))); //right hand side removed.
            }

            for (int i = 0; i <= order.get(0); i++) {
                ch.satisfactions_.add(ch.satisfactions_.remove(0));
            }

            for (int i = 0; i < temp.size(); i++) {
                ch.satisfactions_.add(i, temp.get(i));
            }
//        }
    
//            if(getBestSoFarCOP().isSolution())
                ch.refreshFitness(true);  
//            else
//                ch.refreshFitness(false);
    }
    
    /**
     * METHOD IS NOT TESTED. TEST IT FIRST BEFORE USE.
     * mutationInteger only mutate integers. It uses swap elements technique. 
     * so that it disrupts order more to get new allel values<br>
     * <B>Note:</B> that offspring ArrayList is updated here but rank will remain
     * same because swapping satisfaction value will produce same result. It may
     * only give different results in crossover.
     * @param offspring offspring generated after crossover.
     */
    private void mutationInteger(ArrayList<Chromosome> offspring) throws SolutionFoundException{
        ArrayList<Integer> randDim;
        ArrayList<Integer> randVal;
        Double temp = 0.0;
        int muteBits;
        
        //System.out.println("testing... " + offspring);

        if(userInput_.domainVals == null || userInput_.domainVals.isEmpty()){ //mutation not supported
            return;
        }

        if(externalData_ == null){ //currently works only for external data
            return;
        }        
        
        
        ArrayList<Double> vals;
        ArrayList<Double> noGoods;

        //Technique 2: mutate a given value from available domain value;
        //<<
        for (Chromosome offsp : offspring) {                    
            if(Math.random()<MUTATION_RATE){ //1.0/userInput_.totalDecisionVars){ //>1.0/offspring.get(ofsp).getValsCopy().size() || bStagnant){
                               
//                vals = offsp.getValsCopy();
//                Collections.sort(vals);
//
//                vals.clear();
                noGoods = externalData_.getNoGoodsPartialCSP(offsp.getVals());
                if(noGoods.isEmpty()){
                    continue; //nothing to replace with
                }
                
                if(offsp.getVals().size()<2){ //swapping not possible
                    continue;
                }else{
                    if(userInput_.domainVals == null){
                        continue;
                    }else if(userInput_.domainVals.isEmpty()){
                        continue;
                    }
                    
                    muteBits = 1;
                    if(bStagnant){
                        muteBits = Math.max(1,(int)(offsp.getVals().size()*0.2));
                    }

                    for (int j = 0; j < muteBits && j<noGoods.size(); j++) {
                        
                        randVal = MyRandom.randperm(0,noGoods.size());
                        
                        if(bStagnant){ //Important... must refresh in every iteration....
                            muteBits = Math.max(1,(int)(offsp.getVals().size()*0.2));
                        }

                        //Only deal with valid values...
                        randDim = MyRandom.randperm(0, offsp.getVals().size());
                        
                        if(randDim.get(0) >= offsp.getVals().size()){
                            System.out.println("ee kaisey sake...");
                            System.out.println(offsp.getVals());
                        }
                        try{
                            if(!externalData_.isHighlyConstrained(offsp.getVals(randDim.get(0)).intValue())) //in optimization mode noGoods is empty so automatically this won't be executed.
                                offsp.replaceVal(randDim.get(0),noGoods.get(randVal.get(0)));                        
                        }catch(Exception e){
                            e.printStackTrace();
                            System.out.println("arey??");
                        }
                        
                    }
                             
                }
            }
        }
        //>>
    }

    private void sortAndReplace(int gen) throws Exception, SolutionFoundException{
        if (userInput_.dataType.contains("Integer")){
            noViolationSortAndReplaceInteger(gen); //duplicateSatisfactionSortAndReplace();
        }else if (userInput_.dataType.contains("Double")){
            throw new Exception("Not supported in this version...");
        }
        else{    
            throw new Exception("Incorrect use of data types");
        }
    }
 
    public static int getCurAcceptedConstraints(int totalConstraints){
        int total;
        total = (int)Math.round(curAcceptedConstRatio*totalConstraints);
        return total;
    }
    
//    private void RAselfStudy(final Chromosome chrm){
//        int idx = MyRandom.randperm(0, chrm.satisfactions_.size()-1).get(0);
//        chrm.satisfactions_.add(0,chrm.satisfactions_.remove(idx));
//        chrm.RAupdateFitness();
//    }
    
    /**
     * Precondition: commoner MUST be a full solution irrespective of its validity. 
     * It must have all the elements in its chromosome.
     * @param guru
     * @param commoner
     * @param curAge
     * @param influenceWithBest
     * @throws SolutionFoundException 
     */
     private void RAperformKarma(final Chromosome guru, final Chromosome commoner, final int curAge, 
    final boolean influenceWithBest) throws SolutionFoundException{
        if(externalData_!=null){
//            for (int i = 0; i < RAmaxCommonerAge; i++) {
//                if(i <= RAfullInfluencePer * RAmaxCommonerAge){ //That means age and RAfullInfluencePer is not relevant anymore.
//                    commoner.RAfullInfluenceWith(guru, RAdegreeOfInfluence);
//                    if(influenceWithBest)
//                        commoner.RAfullInfluenceWith(bestSoFarCOP, RAdegreeOfInfluence);
//                }else{
                    commoner.RAkempeInfluenceWith(guru, RAdegreeOfInfluence);
                    if(influenceWithBest)
                        commoner.RAkempeInfluenceWith(bestSoFarCOP, RAdegreeOfInfluence); 
//                }
                
                if(commoner.isMorePromisingThan(guru)){
//                    System.out.println("RA improved - " + i);
                    commoner.ahamBrahmasi();
//                    break;
                } 
//            }
                    
        }
    }
       
    private void RAinitialize(final ArrayList<Chromosome> commoners, final int size){
        Chromosome tempChromosome;
        commoners.clear();
        
        for (int i = 0; i < size; i++) {
            tempChromosome = new Chromosome(this.userInput_.solutionBy, this.externalData_);
            tempChromosome.RAinit();
                //            tempChromosome.isValid = false;
//            tempChromosome.RAupdateFitness();

            commoners.add(tempChromosome);
        }
    }
    
    private Chromosome RAinitialize() throws SolutionFoundException{
        Chromosome commoner;
        commoner = new Chromosome(this.userInput_.solutionBy, this.externalData_);
        commoner.RAinit();
        commoner.RAupdateFitness();
        
        return commoner;
    }
    
//    private Chromosome RAbuildChrome(){
//        Chromosome tempChromosome;
//
//        tempChromosome = new Chromosome(this.userInput_.solutionBy, this.externalData_);
//        tempChromosome.RAinit();
//        tempChromosome.RAupdateFitness();
//
//        return tempChromosome;        
//    }
    
    /**
     * The process:<BR>
     * <ul>
     * <li>
     * clean sols/nonSols
     * </li>
     * <li>
     * reset stagnant counter
     * </li>
     * <li>
     * chromosome_ divided into sols/nonSols -> chromosome_ cleared, sizes of 
     * sols/nonSols changes accordingly
     * </li>
     * <li>
     * setting current mode - CSP or COP
     * </li>
     * <li>
     * reset stagnant counter
     * </li>
     * </ul>
     * 
     * @param gen
     * @throws SolutionFoundException
     * @throws Exception 
     */
    private void noViolationSortAndReplaceInteger(int gen) throws SolutionFoundException, Exception{          
        sols = new ArrayList<Chromosome>();
        nonSols = new ArrayList<Chromosome>(); 
        
        
        
//        //??? why use pool when not using it.....
//        if(poolPromisingChromes.size()>poolPromisingChromesSize/2
//                && poolSelectedChromes.size()<=poolSelectedChromesSize/2){
//            final double maxVal = Collections.max(poolPromisingChromes).getFitnessVal(0);
//            final double minVal = Collections.min(poolPromisingChromes).getFitnessVal(0);
//            
//            poolPromisingChromes = categorizeChromesList(poolPromisingChromes, 
//                    poolSelectedChromesSize-poolSelectedChromes.size(), minVal, maxVal, Chromosome.BY_FITNESS, rhoCOP, null, Debug.categoryList);
//
//            poolSelectedChromes.addAll(poolPromisingChromes);
//            poolPromisingChromes.clear();
//        }
        
        if(bOptimizationMode)
            curBest_ = MyMath.roundN(bestSoFarCOP.getFitnessVal(0),FIT_DP_LIMIT);
        else
            curBest_ = MyMath.roundN(bestSoFarCSP.getFitnessVal(0),FIT_DP_LIMIT);
        
        if(prevBest_ == curBest_){
            stillSameBestCount++;
            sameBestGen++;
        }else{
            stillSameBestCount = 0;
        }          
        
        if(stagnantVisit >= 3 || stillSameBestCount == 0){
            stillSameBestCount = 0;
            bStagnant = false;
            stagnantVisit = 0;
        }        
        
        Chromosome ch; 
        for (int i = 0; i < chromosomes_.size(); i++) {
            ch = chromosomes_.get(i);
            
           if(ch.isPartialSolution()){//ch.getVals().size() == getBestSoFarCOP().getVals().size()){//){
                sols.add(ch);
                chromosomes_.remove(i);
                i--;
            }else{
                nonSols.add(ch);
                chromosomes_.remove(i);
                i--;
            }
        }
  
        chromosomes_.clear();  
        
        int solPop = Math.min(maxSolPop,sols.size());
        int nonSolPop = Math.min(maxNonSolPop,nonSols.size()); //userInput_.population - solPop;
        
        if(solPop < maxSolPop){               
            int ifc = 0;
            while(inFeasiblePopCOP.size()>ifc && !nonSols.isEmpty() && Math.random() < 0.90){ 
                Chromosome c = inFeasiblePopCOP.get(ifc++);
                if(!c.isPartialSolution())
                    continue;
                sols.add(c);
                inFeasiblePopCOP.remove(--ifc);
                nonSols.remove(0);
                solPop++;
                
            }            
            nonSolPop = userInput_.population - solPop;
        }
        
        if(nonSolPop < maxNonSolPop){    
            solPop = maxSolPop;
            while(nonSols.size()<maxNonSolPop){
                nonSols.add(initializeChromosome());
            }
            nonSolPop = maxNonSolPop;
        }
  
        //<<set current mode......
        if(sols.isEmpty()){
            bOptimizationMode = false;             
        }else{
            bOptimizationMode = true;         
            maxCSPval = sols.get(sols.size()-1).getFitnessVal(0); //nooo wrong  will give 0    
        }
        if(bOptimizationMode){
            if(modeChangedGen == -1){ //change once only
                modeChangedGen = gen;
            }                           
        }       
        if (gen == modeChangedGen){
            if(RAplay)
                RAinitialize(RAcommoners_, RAcommonerSize);
        }
        //>>setting current mode......
             
        
        if((stillSameBestCount >= SAME_BEST_GENERATIONS)){
            bStagnant = true;                   
            stagnantVisit++;                                     
        }
        
        if(!sols.isEmpty() && /*mutationID==2 &&*/ bestSoFarCOP.isSolution() && resolveLocalOptExternal){
            System.out.println("<<<<<<<<<<<<<<<<<<Forced Resolve Local Optimal Called>>>>>>>>>>>>>>>");
            resolveLocalOptExternal = false;//IMPORTANT (LINKED WITH GUI) only one attempt.
     
            //<< hamming dist for prevBestsoFar
            if(!bTabuMode){  
                 //adding hamming dist to dynamicConstraint
                addDynamicConstraint(retrieveHammingDist(resolveLevel), Double.NaN); //the best one is stuck into local optimum
                externalData_.setMaxHamDistByProportion(0.15); //15% of the size of HammDist
                bTabuMode = true;
            }

            minFitDiffForResolveLocal /= 2;
            //>>
                            
        }
        
        if(tabuGens >= maxTabuGens){
            tabuGens = 0;
            bTabuMode = false;
            deleteAllDynamicConstraints();                        
        }
        
        if(bTabuMode){
            tabuGens++;
        }

        try{
            Chromosome.tmpSortBy = Chromosome.BY_FITNESS;
//            Collections.sort(sols);
            
            sols = new ArrayList<Chromosome>(sols.subList(0, solPop)); //may contain some non sols as well because of stagnant code ...
            
            Chromosome.tmpSortBy = Chromosome.BY_SATISFACTIONS;
            Collections.sort(nonSols);
//            nonSols = new ArrayList<Chromosome>(nonSols.subList(0, nonSolPop));
                        
        }catch(IndexOutOfBoundsException iob){
            iob.printStackTrace();
        }
        chromosomes_.clear(); //just to be safe;

        //since the sorting code has been removed so you need to sort it here....
        Chromosome.tmpSortBy = Chromosome.BY_FITNESS;
//        Collections.sort(sols);
        chromosomes_.addAll(sols);        
        
//        int dnonsol;
        Chromosome.tmpSortBy = Chromosome.BY_SATISFACTIONS;
        Collections.sort(nonSols);
//        dnonsol = Math.min(10, nonSols.size()-2);

        chromosomes_.addAll(nonSols);
        
        Chromosome.tmpSortBy = Chromosome.BY_FITNESS;
                
        if(chromosomes_.size() != userInput_.population){
            System.err.println("population size error on noViolationSortAndReplace.");
            Application.getInstance().exit();
        }   
        prevBest_ = curBest_;
        
        worstDeath(chromosomes_, false, gen, 10);
        
        boolean bForce = false;
        boolean bGetMoreConstraints = false;

        if(bestSoFarCOP.isPartialSolution()){// && sols.size() >= 1.0*maxSolPop){
            if(sols.size() >= 0.1*maxSolPop){// && sameBestGen >= CspProcess.NO_PROGRESS_LIMIT){ //now its redundant as used above
                sameBestGen = 0;
                bForce = false;
                bGetMoreConstraints = true;                
                alterAcceptedConstRatio(bGetMoreConstraints, bForce);
            }          
        }     
        
    }
    
    private void cleanChromHistory(ArrayList<Chromosome> chromList){
        for (Chromosome c : chromList) {
            c.cleanHistory();
        }
    }
    
    private void alterAcceptedConstRatio(boolean increment, boolean bForceChange){
        transitionGen++;
        boolean bSuccess = false;
        
        if(increment){            
            if((transitionGen >= maxTransitionGen || bForceChange) && curAcceptedConstRatio < 1.0){
                curAcceptedConstRatio += startAcceptedConstRatio;
                transitionGen = 0;
//                mutationID = 0;
                bSuccess = true;
            }
            
            if(transitionGen == 1){
                cleanChromHistory(chromosomes_);
                poolPromisingChromes.clear();
                poolSelectedChromes.clear();
            }
            
            if(curAcceptedConstRatio > 1.0)
                curAcceptedConstRatio = 1.0;
            

            if(bSuccess){
                for (Chromosome c : chromosomes_) {                                        
                    c.cleanProgressCounter();                
                }
            }

        }else{
            if((transitionGen >= maxTransitionGen || bForceChange) && curAcceptedConstRatio > startAcceptedConstRatio){
                curAcceptedConstRatio -= startAcceptedConstRatio;
                transitionGen = 0;
//                mutationID = 0;
                bSuccess = true;
            }
            
            if(transitionGen == 1){
                cleanChromHistory(chromosomes_);
                poolPromisingChromes.clear();
                poolSelectedChromes.clear();
            }
            
            if(curAcceptedConstRatio < startAcceptedConstRatio)
                curAcceptedConstRatio = startAcceptedConstRatio;
        }
        
//        if(bReinitialize){
//            curAcceptedConstRatio = startAcceptedConstRatio;
//            transitionGen = 0;
//            bSuccess = true;
//        }
        
        try {
            if(bSuccess){
                externalData_.refreshConstLimits();
                
                
                for (Chromosome c : chromosomes_) {                
                    c.refreshFitness();            
                }
//                getBestSoFarCOP().refreshFitness(true);
                getBestSoFarCSP().refreshFitness();
                
                
                        
            }
            
        } catch (SolutionFoundException ex) {
            ;
        }
        

        
    }
    
    public static boolean resolvingLocalOptimalMode(){
        if(bTabuMode && tabuGens < maxTabuGens){
            return true;
        }else{
            return false;
        }
    }
    
//    private void switchToNextMutationType(){
//        int initVal = mutationID;
//        try{
//            mutationID = (mutationID+1)%totalMutationTypes; 
//        }catch(ArithmeticException ae){
////
//            
//            mutationID = initVal;
//        }
//    }
    
    /**
     * This is duplication of {@link Chromosome.#isSolution() } created only
     * for easy access.
     * @param c
     * @return 
     */
    public static boolean isSolution(Chromosome c){
        if(c == null){
            return false;
        }else{
            return c.isSolution();
        }
    }
    
    
    /**
     * @param list - (Pass by value). input list will be destroyed. Get the returned list
     * @param listPop
     * @param minVal
     * @param maxVal
     * @param categorizeBy
     * @param a 1 is preferred can use lesser values as well
     * @param ro current test results shows no difference in picking any value
     * @param grpIdx (Pass by Ref) - indices in final list indicating starting indices of groups/slots
     */
     private ArrayList<Chromosome> categorizeChromesList(ArrayList<Chromosome> list, final int listPop, 
    final double minVal, final double maxVal, final int categorizeBy, final double ro, ArrayList<Integer> grpIdx, 
    final boolean debugPrint){
         
        System.out.println("<<catgorizing...>>: " + list.size());
        int slotSize;
        int FirstSlotSize;
        int empty;
        int incompleteSlots;
        int slotAddition;
        int satis;
        ArrayList<ArrayList<Chromosome>> grouping = new ArrayList<ArrayList<Chromosome>>();
        //final int funcionalConstraints = userInput_.total__updatedConstraints; //userInput_.totalConstraints - userInput_.totalDecisionVars+1;        
        int front2[] = new int[2];

        if(list.isEmpty()){
            return list;
        }     
        
        if(categorizeBy == SORT_HARDCONSVIOS_THEN_FITNESS){
            list = sortTwice(Chromosome.BY_HARD_CONSTRAINT_VIOS, Chromosome.BY_FITNESS, list, list.size()); //Math.min(list.size(),listPop*2)); 
            Chromosome.tmpSortBy = Chromosome.BY_FITNESS;
        }else if(categorizeBy == SORT_SATISFACTION){
            Chromosome.tmpSortBy = Chromosome.BY_SATISFACTIONS;
            Collections.sort(list);
//            list = new ArrayList<Chromosome>(list.subList(0, Math.min(list.size(),listPop*2)));
        }else if(categorizeBy == SORT_FITNESS){ //depends on the current Chromosome.tmpSortBy specified by the caller             
            Chromosome.tmpSortBy = Chromosome.BY_FITNESS;
            Collections.sort(list);
//            list = new ArrayList<Chromosome>(list.subList(0, Math.min(list.size(),listPop*2)));
        }else if (categorizeBy == SORT_FITNESS_THEN_NOVELTY){
            list = sortTwice(Chromosome.BY_FITNESS, Chromosome.BY_RHO, list, list.size()); //Math.min(list.size(),listPop*2)); 
//            list = sortFitnessThenNovelty(list, Math.min(list.size(),listPop*2));
            Chromosome.tmpSortBy = Chromosome.BY_FITNESS;
        }
        
         
        final int range = (int)Math.max(maxVal-minVal, 1);
        
        final int slots = (int)Math.min(range,10);//0.2*userInput_.totalConstraints); //BAD* try to make relationship with population man...
        //final int slots = Math.max((int)(0.1*maxVal), 5); //
        final int remainder = (int)Math.ceil(range/slots);
        slotSize = listPop/(slots); //last one for infeasibles as well
        FirstSlotSize = slotSize + (listPop - slotSize*(slots));
        empty = 0;
        incompleteSlots = 0;

        for (int i = 0; i < slots; i++) {//funcionalConstraints
            grouping.add(new ArrayList<Chromosome>());
        }

        satis = -1;                
        //Now this step is not necessary just redundant.... because you are using exponential.... directly apply formula into the whole population
        for(int i = 0; i<list.size(); i++){
            satis = (int)(list.get(i).getRank()- minVal);//getFitnessVal(0).intValue()); //1 to total constraints
            satis = Math.min((int)Math.floor(satis/remainder),slots-1);
            grouping.get(satis).add(list.get(i));
        }

//        for (ArrayList<Chromosome> grp : grouping) {
//            Collections.sort(grp);
//        }

        int fr = 0;                                        
        for (int i = 0; i < grouping.size(); i++) {
            ArrayList<Chromosome> g = grouping.get(i); 

            if(g.size()>0 && fr < 2){
                front2[fr++] = i;
            }                        
        }

        empty = 0;
        incompleteSlots = 0;
        for (int i = 0; i < grouping.size(); i++) {
            if(grouping.get(i).size()<slotSize){
                empty += slotSize-grouping.get(i).size();
                incompleteSlots++;
            }
        }

        slotAddition = empty/(slots-incompleteSlots); //empty slot space has to be distributed to filled/partially filled slots 
        slotSize += slotAddition;
        FirstSlotSize += slotAddition;
        FirstSlotSize += empty - (slots-incompleteSlots)*slotAddition;

        ArrayList<Chromosome> additionals = new ArrayList<Chromosome>();
//        ArrayList<ArrayList<Chromosome>> additionals = new ArrayList<ArrayList<Chromosome>>();
//         for (int i = 0; i < slots; i++) {
//             additionals.add(new ArrayList<Chromosome>());
//         }

        ArrayList<Integer> tmpIdx;
        ArrayList<Chromosome> chTmp;
        int count;
             
        boolean bFirstSlotAdded = false;
        for (int i = 0; i < grouping.size(); i++) {
            if(!bFirstSlotAdded && grouping.get(i).size() >= FirstSlotSize){  
                
                //<<.... 
//                tmpIdx = MyMath.linearFnSelection(grouping.get(i), FirstSlotSize, debugPrint);
                tmpIdx = MyMath.negExpFnSelection(grouping.get(i).size(), FirstSlotSize, ro, debugPrint);
                chTmp = new ArrayList<Chromosome>();

                for (int j = 0; j < tmpIdx.size(); j++) {
                    chTmp.add(grouping.get(i).get(tmpIdx.get(j)));                    
                }                                
                
                if(grouping.get(i).size() > FirstSlotSize ){
                    count = 0;
                    for (int j = 0; j < grouping.get(i).size(); j++) {
                        if(count<tmpIdx.size()){
                            if(j==tmpIdx.get(count).intValue()){
                                count++;
                                continue;
                            }
                        }
//                        additionals.get(i).add(grouping.get(i).get(j));   
                        additionals.add(grouping.get(i).get(j));
                    }    
                }
                grouping.set(i, chTmp);
                //>>...                

                bFirstSlotAdded = true;
                continue;
            }
            
            //<<
                tmpIdx = new ArrayList<Integer>();
                chTmp = new ArrayList<Chromosome>();
                int sz = Math.min(slotSize,grouping.get(i).size());                                
                
//                tmpIdx = MyMath.linearFnSelection(grouping.get(i), sz, debugPrint);
                tmpIdx = MyMath.negExpFnSelection(grouping.get(i).size(), sz, ro, debugPrint);
                
                for (int j = 0; j < tmpIdx.size(); j++) {
                    chTmp.add(grouping.get(i).get(tmpIdx.get(j)));                    
                }
                                
                if(grouping.get(i).size() > slotSize ){
                    count = 0;
                    for (int j = 0; j < grouping.get(i).size(); j++) {
                        if(count<tmpIdx.size()){
                            if(j==tmpIdx.get(count).intValue()){
                                count++;
                                continue;
                            }
                        }
//                        additionals.get(i).add(grouping.get(i).get(j));    
                        additionals.add(grouping.get(i).get(j));
                    }    
                }
                grouping.set(i, chTmp);
            //>>
            
        }

        list.clear(); 
        if(grpIdx == null){
            grpIdx = new ArrayList<Integer>(); //NOTE pass-by-ref distroyed here. Code for grpIdx below are now USELESS                    
        }
        
        grpIdx.add(0);//first index obviously.
        for (int i = 0; i < grouping.size(); i++) {
            ArrayList<Chromosome> g = grouping.get(i); 
            list.addAll(g);
            grpIdx.add(list.size());//next index
        }
        grpIdx.remove(grpIdx.size()-1);//last one is invalid it is size+1. there is no next after size().
        
//        int reqAdditionals = listPop-list.size();
//        for (int i = 0; i < reqAdditionals; i++) {
//            if(additionals.get(i%slots).isEmpty()){
//                reqAdditionals++; // because this i is ignored
//                continue;
//            }
//            list.add(additionals.get(i%slots).remove(0));
//        }
        list.addAll(additionals.subList(0, listPop-list.size())); 

        Chromosome.tmpSortBy = userInput_.solutionBy;  
        
        return list;
    }
    
    
    private void deleteAllDynamicConstraints(){ 
        if(externalData_ != null)
             CspProcess.dynamicConstraints.clear();
        
        //MUST...
//        for (Chromosome c : chromosomes_) {
//            c.cleanFitnessHistory();
//         used in Nqueen
    }
    
    
    
    /**
     * NOTE: 
     * @param center
     * @param radius 
     */
    private void addDynamicConstraint(ArrayList<ArrayList> center, double radius){ 
//    private void addDynamicConstraint(ArrayList<Double> center, double radius){ 
        //negFeasibleRange = 0; no need 
        //dynamicConstraintNo = 0;
        //userInput_.totalConstraints++; done inside externalData_.addTabuConstraint
        //MAX_FUNCTIONAL_CONSTRAINTS = userInput_.totalConstraints - userInput_.totalDecisionVars; don't need here
        
        if(externalData_ != null)
            externalData_.addTabuConstraint(center); //center = local optimal position 
//        else
//            CCSPfns.addTabuConstraint(center, radius);    
        //MUST... i think in Nqueen
//        for (Chromosome c : chromosomes_) {
//            c.cleanFitnessHistory();
//        }
    }
    
    /**
     * NOTE: This function <B>DOES NOT</B> sort the chromosomes inside a ranked group.
     * If it has <I>n</I> ranks/groups, it only tries to give best ranked chromosomes,
     * then the leftovers are <B>ONLY</B> sorted according to fitness.
     * You must sort each ranked groups separately afterwards.
     * @param in
     * @param size
     * @return 
     */
//    private ArrayList<Chromosome> sortFitnessThenNovelty(final ArrayList<Chromosome> in, final int size){        
//        Chromosome.tmpSortBy = Chromosome.BY_FITNESS;
//        Collections.sort(in);
//        
//        ArrayList<Chromosome> out = null;
//        final int minAcceptedRank = (int)in.get(size-1).getRank(); //worst one
//         
//        int safePointer = -1;
//        Chromosome chrome;
//        ArrayList<Chromosome> temp = new ArrayList<Chromosome>();
//        
//        try{
//            if(in.size()<=1 || in.size() <= size){
//                out = in;
//                throw new ExecutionException(null);
//            }
//
//            for (Chromosome chrm : in) {
//                if(chrm.getFitnessVal(0) == minAcceptedRank){ //??? double == double.... X X X
//                    chrome = (Chromosome)chrm;
//                    temp.add(chrome); //exract chromosomes with max accepted violations.
//                }else if(chrm.getFitnessVal(0) < minAcceptedRank){
//                    safePointer++;
//                }else{
//                    break;
//                }
//            }
//
//            if(size <= safePointer+1){
//                out = new ArrayList<Chromosome>(in.subList(0, size));//not get only required sorted ones.  
//            }else{
//                out = new ArrayList<Chromosome>(in.subList(0, safePointer+1));//not get only required sorted ones.                        
//                Chromosome.tmpSortBy = Chromosome.BY_RO; //where are you calculating ro??
//                Collections.sort(temp);
//                out.addAll(temp.subList(0, size-safePointer-1));                
//                out = new ArrayList<Chromosome>(out.subList(0, size));
//            }        
//        }catch(ExecutionException ee){
//            out = out;
//        }
//        catch(Exception e){
//            e.printStackTrace();
//            out = out;
//        }
//
//        Chromosome.tmpSortBy = userInput_.solutionBy;
//        return out;
//    }
//    
    
    
    private ArrayList<Chromosome> sortTwice(int firstSortType, int SecondSortType, final ArrayList<Chromosome> in, final int size){        
        Chromosome.tmpSortBy = firstSortType;
        Collections.sort(in);
        
        ArrayList<Chromosome> out = null;
//        final int minAcceptedRank = (int)in.get(size-1).getRank(); //worst one         
//        int safePointer = -1;
//        Chromosome chrome;
//        ArrayList<Chromosome> temp = new ArrayList<Chromosome>();
        
        ArrayList<ArrayList<Chromosome>> sortedIn = new ArrayList<ArrayList<Chromosome>>();
        double rankLimit;
        
        try{
            if(in.size()<=1){// || in.size() < size){
                out = in;
//                Throwable cause = new Throwable("Incorrect size");                
                throw new ExecutionException(null);
            }

            rankLimit = in.get(0).getRank(); //in.get(0).getTotalHardVios();
            sortedIn.add(new ArrayList<Chromosome>());
            int total = 0;
            for (Chromosome chrm : in) {
                total++;
                if(MyMath.roundN(chrm.getRank()-rankLimit,FIT_DP_LIMIT) != 0.0){               
                    rankLimit=chrm.getRank(); //getTotalHardVios();
                    sortedIn.add(new ArrayList<Chromosome>());
                }
                sortedIn.get(sortedIn.size()-1).add(chrm);
                
//                if(total >= size){
//                    break;
//                }
//                if(chrm.getTotalHardVios() == minAcceptedRank){ //d
//                    chrome = (Chromosome)chrm;
//                    temp.add(chrome); //exract chromosomes with max accepted violations.
//                }else if(chrm.getTotalHardVios() < minAcceptedRank){
//                    safePointer++;
//                }else{
//                    break;
//                }
            }

            Chromosome.tmpSortBy = SecondSortType;
            out = new ArrayList<Chromosome>();
            for (ArrayList<Chromosome> al : sortedIn) {
                Collections.sort(al);
                out.addAll(al);
                if(out.size()>=size){
                    break;
                }
            }
            
            out = new ArrayList<Chromosome>(out.subList(0, size));
            
//            if(size <= safePointer+1){
//                out = new ArrayList<Chromosome>(in.subList(0, size));//not get only required sorted ones.  
//            }else{
//                out = new ArrayList<Chromosome>(in.subList(0, safePointer+1));//not get only required sorted ones.                        
//                Chromosome.tmpSortBy = Chromosome.BY_FITNESS; //userInput_.solutionBy;
//                Collections.sort(temp);
//                out.addAll(temp.subList(0, size-safePointer-1));                
//                out = new ArrayList<Chromosome>(out.subList(0, size));
//            }        
        }catch(ExecutionException ee){
            ;//no problem here...
        }
        catch(Exception e){
            e.printStackTrace();
        }

        Chromosome.tmpSortBy = userInput_.solutionBy; //default one
        return out;
    }
        
    /**
     * 
     * @param pop
     * @param isRA
     * @param gen
     * @param spareSize
     * @param d size of pop to be removed
     * @return
     * @throws SolutionFoundException 
     */
    private void worstDeath(ArrayList<Chromosome> pop, boolean  isRA, int gen, int d) throws SolutionFoundException{
        ArrayList<Chromosome> newRandPop = new ArrayList<Chromosome>();
//        ArrayList<Chromosome> diedPop = new ArrayList<Chromosome>();       

        try {
            if(isRA)
                RAinitialize(newRandPop, d);             
            else
                initializeChromosome(newRandPop, d, gen);
        } catch (Exception e) {
            e.printStackTrace();
            Application.getInstance().exit();
        }   
      

//        for(int i: MyRandom.randperm(spareSize, pop.size()-1).subList(0, d)){
        for (int i = 0; i < d; i++) {
//            suspended_.add(pop.remove(pop.size()-1-i));
            pop.remove(pop.size()-1-i);//last ones...
            pop.add(newRandPop.get(i));
        }
    }
    
    
//    private void noViolationSortAndReplace(int gen) throws Exception{
//        ArrayList<Chromosome> diverse = new ArrayList<Chromosome>();
//        ArrayList<Chromosome> newRandPop = new ArrayList<Chromosome>();
//        Chromosome chrome;
//        double maxAcceptedRank;
//        int safePointer = -1;
//        int d;
//
//
//        for (Chromosome chrm : chromosomes_) {
//            chrm.tempSortBy = Chromosome.BY_FITNESS;
//        }
//        
//        Collections.sort(chromosomes_);//sorted according to violation
//        
//        bestSoFar = chromosomes_.get(0);
//        
//        for (Chromosome chrm : chromosomes_) {
//            if(chrm.isSolution()){
//                throw new SolutionFoundException("All constraints satisfied");
//            }
//        }
//
//
//        int tempSize;
//        ArrayList<Double> tempVals;
//
//        bStagnant = false;
//        if(gen>userInput_.generation*0.05 && gen%10 == 0){
//            bStagnant = true;
//            System.diverse.println("*** diverse -- removed...");
//            tempSize = (int)(1*SAME_BEST_VAL_PERCENT*userInput_.population);        
//            
//            for (int ofsp= 0; ofsp < tempSize; ofsp++) {
//                tempVals = chromosomes_.get(ofsp).negateVals();
//                chromosomes_.get(ofsp).setVals(tempVals);
//            }            
//            
//            Collections.sort(chromosomes_);          
//        }
//
//        maxAcceptedRank = chromosomes_.get(userInput_.population-1).getFitnessVal(0);
//
//        try{                       
//            for (Chromosome chrm : chromosomes_) {
//                if(MyMath.roundN(chrm.getFitnessVal(0),2) == MyMath.roundN(maxAcceptedRank,2)){
//                    chrome = (Chromosome)chrm;
//                    chrome.tempSortBy = userInput_.solutionBy ;
//                    chrome.tempRo = getRoValue(chrm);
//                    diverse.add(chrome); //exract chromosomes with max accepted violations.
//                }else if(MyMath.roundN(chrm.getFitnessVal(0),2) < MyMath.roundN(maxAcceptedRank,2)){
//                    safePointer++;
//                }else{
//                    break;
//                }
//            }
//            chromosomes_ = new ArrayList<Chromosome>(chromosomes_.subList(0, safePointer+1));//not get only required sorted ones.
//
//            for (Chromosome chrm : diverse) {
//                chrm.tempSortBy = Chromosome.BY_RO; //MUST DO before sorting
//            }
//            Collections.sort(diverse);
//            for (Chromosome chrm : diverse) {
//                chrm.tempSortBy = Chromosome.BY_FITNESS; //MUST DO before sorting
//            }
//
//            chromosomes_.addAll(diverse.subList(0, userInput_.population-safePointer-1));
//
//            if(chromosomes_.size() != userInput_.population){
//                System.err.println("population size error on noViolationSortAndReplace.");
//                Application.getInstance().exit();
//            }
//            
//            for (Chromosome c : chromosomes_) {
//                c.tempSortBy = userInput_.solutionBy;
//            }
//
//            randomDeath();
//        }catch(MyException me){
//            me.showMessageBox();
//        }
//        catch(Exception e){
//            throw e;
//        }
//    }        
    
    private boolean XXXforceFindSolution(Chromosome chrom){                
        ArrayList<Double> vals = chrom.getValsCopy();
        ArrayList<Double> noGoods = new ArrayList<Double>();
       
        Collections.sort(vals);
        int expectedVal = 0;
        
        
        //WRONG CODE..... SEE tryForcedCSPsolUpdate IN TT
        //<<
        for (int i = 0; i < vals.size(); i++) {                        
            if(vals.get(i).intValue() != expectedVal){
                for (int j =expectedVal; j < vals.get(i).intValue(); j++) {
                    noGoods.add(j*1.0); 
                } 
                expectedVal = vals.get(i).intValue();
            } 
            expectedVal++;
        }
        
        for (int i = vals.get(vals.size()-1).intValue()+1; i < userInput_.totalDecisionVars; i++) {
            noGoods.add(i*1.0); 
        }
        //>>

        int prevLength;
        int newLenght=-1;
        Double removedVal;
        
        try{                    
            for (Double ng : noGoods) {               
                prevLength = chrom.getValsCopy().size();
                for (int i = 0; i < chrom.getValsCopy().size(); i++) {
                    removedVal = chrom.getVals(i);
                    if(externalData_.isViolated(removedVal.intValue(), ng.intValue()) && !externalData_.isHighlyConstrained(removedVal.intValue())){
                        chrom.replaceVal(i, ng);
                        chrom.appendVal(removedVal);
                    }
                    newLenght = chrom.getValsCopy().size();

                    if(newLenght>prevLength){//successful addition
                        break;
                    }
                    if(newLenght<prevLength){//successful addition
                        System.out.println("keee kaisey sake?????");
                    }
                    
                }            
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        
        if(newLenght == userInput_.totalDecisionVars){
            return true;
        }else{
            return false;
        }
        
    }
    
    
//    private void xxx(int gen) throws Exception{
//        for (Chromosome chrm : chromosomes_) {
//            chrm.tempRo = getRoValue(chrm);
//            chrm.tempSortBy = userInput_.solutionBy ;//Chromosome.BY_VIOLATIONS; //MUST DO before sorting
//        }
//        
//        Collections.sort(chromosomes_);//sorted according to violation/satisfactions
//        
//        bestSoFar = chromosomes_.get(0);
//        curBest_ = bestSoFar.getRank();
//        
//        for (Chromosome chrm : chromosomes_) {        
//            if(chrm.isSolution()){
//                bestSoFar = chrm;
//                bestSoFar.tempSortBy = userInput_.solutionBy;
//                throw new SolutionFoundException("All constraints satisfied");
//            }
//            chrm.tempSortBy = Chromosome.BY_RO;    
//        }
//
//        Collections.sort(chromosomes_);  
//        chromosomes_ = new ArrayList<Chromosome>(chromosomes_.subList(0, userInput_.population));
//        
//        for (Chromosome chrm : chromosomes_) {
//            chrm.tempSortBy = userInput_.solutionBy ;//Chromosome.BY_VIOLATIONS; //MUST DO before sorting
//        }
//    }
    
    
//    private void setUniqueChromosomes(){
////        for (int iL = 0; iL < chromosomes_.size()-1; iL++) {
////            for (int jR = iL+1; jR < chromosomes_.size(); jR++) {
////                if(chromosomes_.get(iL).equals(chromosomes_.get(jR))){
////                    chromosomes_.remove(jR);
////                    jR--;
////                }
////            }
////        }
//        
//        Collections.sort(chromosomes_);
//        
////        for (Chromosome chrm : chromosomes_) {
////            System.out.print(chrm.fitness_.get(0)+", ");
////        }
//        
//        for (int iL = 0; iL < chromosomes_.size()-1; iL++) {
//            if(MyMath.roundN(chromosomes_.get(iL+1).fitness_.get(0),2)
//                    == MyMath.roundN(chromosomes_.get(iL).fitness_.get(0),2)){
//                chromosomes_.remove(iL+1);
//                iL--;
//            }            
//        }
//        
////        System.out.println("\n\n");
////        for (Chromosome chrm : chromosomes_) {
////            System.out.print(chrm.fitness_.get(0)+", ");
////        }
////        System.out.println("\n\n");
//    }
    
    
  
    
// <editor-fold defaultstate="collapsed" desc="Old commented code. May be useful :)">    
    
//////    /**
//////     * Sort based on violation preference. select the best ones and then use
//////     * ro values if same violation is found.
//////     * @throws Exception
//////     */
//////    private void noViolationSortAndReplace(int gen) throws Exception, SolutionFoundException{
//////        ArrayList<Chromosome> diverse = new ArrayList<Chromosome>();
//////        ArrayList<Chromosome> newRandPop = new ArrayList<Chromosome>();
//////        Chromosome chrome;
//////        int maxAcceptedViolation;
//////        int safePointer = -1;
//////        int d;
//////
//////        //Get immunity chromosomes...
//////        
//////        
////////        ArrayList<Chromosome> goodImmuneChrom = new ArrayList<Chromosome>();        
////////        for (Chromosome chrm : chromosomes_) {
////////            chrm.sortBy = Chromosome.BY_IMMUNITY;        
////////        }
////////        Collections.sort(chromosomes_);        
////////        goodImmuneChrom = new ArrayList<Chromosome>(chromosomes_.subList(0, (int)Math.round(this.IMMUNITY_PERCENT*userInput_.population)));
////////        chromosomes_.removeAll(goodImmuneChrom);
//////        
//////        for (Chromosome chrm : chromosomes_) {
//////            chrm.sortBy = userInput_.solutionBy ;//Chromosome.BY_VIOLATIONS; //MUST DO before sorting 
//////            if(chrm.isSolution()){
//////                throw new SolutionFoundException("All constraints satisfied");
//////            }
//////        }
//////                        
//////        Collections.sort(chromosomes_);//sorted according to violation
//////        bestSoFar = chromosomes_.get(0);
//////
//////        int immuneCount = 0;
////////        Chromosome tempChrome;
////////        
////////        chromosomes_.addAll(0, goodImmuneChrom);
////////        immuneCount = goodImmuneChrom.size();
////////        for (Chromosome chrm : chromosomes_) {
////////            chrm.sortBy = userInput_.solutionBy ;//Chromosome.BY_VIOLATIONS; //MUST DO before sorting 
////////        }
////////           
////////        for (Chromosome chrm : chromosomes_) {
////////            if(chrm.isSolution()){
////////                throw new SolutionFoundException("All constraints satisfied");
////////            }
////////        }
////////        
////////        for (int ofsp = 0; ofsp < chromosomes_.size(); ofsp++) {
////////            if(chromosomes_.get(ofsp).getImmunity() > 0){
////////                chromosomes_.get(ofsp).useImmunity();
////////                tempChrome = chromosomes_.remove(ofsp);
////////                chromosomes_.add(0, tempChrome);
////////                immuneCount++;
////////            }
////////            
////////            if(immuneCount>= (int)Math.round(this.IMMUNITY_PERCENT*userInput_.population))
////////                break;            
////////        }
//////    
//////        int tempSize;
//////        ArrayList<Double> tempVals;
//////
//////        bStagnant = false;
//////        if(gen>userInput_.generation*0.05 && gen%10 == 0){
//////            bStagnant = true;
//////            System.diverse.println("*** diverse -- removed...");
//////            tempSize = (int)(1*SAME_BEST_VAL_PERCENT*userInput_.population);
//////
//////            for (int ofsp= 0; ofsp < tempSize; ofsp++) {
//////                tempVals = chromosomes_.get(ofsp).negateVals();
//////                chromosomes_.get(ofsp).setVals(tempVals);
//////            }
//////            Collections.sort(chromosomes_);        
//////        }
//////
//////        maxAcceptedViolation = chromosomes_.get(userInput_.population-1).getRank();
//////
//////        try{  
//////            safePointer = immuneCount-1;
//////            for (int ofsp = immuneCount; ofsp < chromosomes_.size(); ofsp++) {
//////                if(chromosomes_.get(ofsp).getRank()==maxAcceptedViolation){
//////                    chrome = (Chromosome)chromosomes_.get(ofsp);
//////                    chrome.tempRo = getRoValue(chromosomes_.get(ofsp));
//////                    diverse.add(chrome); //exract chromosomes with max accepted violations.
//////                }else if(chromosomes_.get(ofsp).getRank() < maxAcceptedViolation){
//////                    safePointer++;
//////                }else{
//////                    break;
//////                }
//////            }            
//////            
//////            chromosomes_ = new ArrayList<Chromosome>(chromosomes_.subList(0, safePointer+1));//not get only required sorted ones.
//////
//////            for (Chromosome chrm : diverse) {
//////                chrm.sortBy = Chromosome.BY_RO; //MUST DO before sorting
//////            }
//////            Collections.sort(diverse);
//////            for (Chromosome chrm : diverse) {
//////                chrm.sortBy = userInput_.solutionBy; //MUST DO before sorting
//////            }
//////
//////            chromosomes_.addAll(diverse.subList(0, userInput_.population-safePointer-1));
//////
//////            if(chromosomes_.size() != userInput_.population){
//////                System.err.println("population size error on noViolationSortAndReplace.");
//////                Application.getInstance().exit();
//////            }
//////
//////            randomDeath();
//////        }catch(MyException me){
//////            me.showMessageBox();
//////        }
//////        catch(Exception e){
//////            throw e;
//////        }
//////    }

//////    private void getNovelty(Chromosome chrm, ArrayList<Chromosome> archive){
//////        ArrayList<Chromosome> entirePopulation = new ArrayList<Chromosome>();
//////        ArrayList<Double> diverse = new ArrayList<Double>();
//////
//////        double distSqrMean = 0;
//////        double roMin;
//////
//////        entirePopulation.addAll(archive);
//////        entirePopulation.addAll(chromosomes_);
//////
//////        double[] mean = new double[entirePopulation.size()];
//////
//////        if (archive.size()<ARCHIVE_MAX){
//////            //get mean
//////            for (int ofsp = 0; ofsp < userInput_.totalDecisionVars; ofsp++) {
//////                diverse.clear();
//////                for (int k = 0; k < entirePopulation.size(); k++) {
//////                    diverse.add(entirePopulation.get(k).vals.get(ofsp));
//////                }
//////                mean[ofsp] = MyMath.mean(diverse);
//////            }
//////
//////            //now get variance - actually ofsp amusing average mean square distane
//////            for (int ofsp = 0; ofsp < entirePopulation.size(); ofsp++) {
//////                distSqrMean += Math.pow(MyMath.norm(entirePopulation.get(ofsp).vals, diverse),2);
//////            }
//////            distSqrMean = distSqrMean/entirePopulation.size();
////////            roMin  = distSqrMean;
//////            roMin = 0;
//////        }
//////        else{
//////
//////        }
//////    }


    
    
    
    
    
//////    /**
//////     * Seems working 
//////     * 
//////     */
//////    private void duplicateSatisfactionSortAndReplace(){
//////        Chromosome tempChrome;
//////        ArrayList<Double> tempVals;
//////        int newSize;
//////        int tempSize;
//////        ArrayList<Integer> randIdx;
//////
//////        for (Chromosome chrm : chromosomes_) {
//////            chrm.sortBy = userInput_.solutionBy; //MUST DO before sorting
//////        }
////////        for (Chromosome chrm : chromosomes_) {
////////            chrm.sortBy = Chromosome.BY_RO; //MUST DO before sorting
////////            try{
////////            chrm.tempRo = getRoValue(chrm);
////////            }catch (Exception e){
////////                e.printStackTrace();
////////            }
////////        }
//////        
//////        bStagnant = false;
//////        if(isStagnant()){
//////            Collections.sort(chromosomes_);
//////            bStagnant = true;
//////            System.diverse.println("*** diverse -- removed...");
//////            tempSize = (int)(3*SAME_BEST_VAL_PERCENT*userInput_.population);
//////
//////            
//////            for (int ofsp= 0; ofsp < tempSize; ofsp++) {
//////                //chromosomes_.remove(0);        
//////                tempVals = chromosomes_.get(ofsp).negateVals();                
//////                chromosomes_.get(ofsp).setVals(tempVals);
//////            }
//////            
////////            try{
////////                //System.diverse.println("be " + chromosomes_.get(0).getValsCopy());
////////                mutation(new ArrayList<Chromosome>(chromosomes_.subList(0, tempSize)));
////////            }catch(UnsupportedDataTypeException udte){
////////                System.diverse.println(udte.getLocalizedMessage());
////////                Application.getInstance().exit();
////////            }
//////        }
//////
//////       
//////        ArrayList<Chromosome> newRandPop = new ArrayList<Chromosome>();
//////
//////
//////
//////         
//////        if(chromosomes_.size() < userInput_.population){            
//////            newSize = userInput_.population-chromosomes_.size();
//////            //randomly make copy of existing chromosomes...
//////            //randIdx = MyRandom.randperm(0, chromosomes_.size()-1);
//////
//////            try {
//////                initializeChromosomes(newRandPop, newSize);
//////            } catch (Exception e) {
//////                e.printStackTrace();
//////                Application.getInstance().exit();
//////            }
//////            
//////            for (int ofsp = 0; ofsp < newSize; ofsp++) {
//////                chromosomes_.addAll(newRandPop);
//////            }
//////
//////            Collections.sort(chromosomes_);
//////            
//////        }else{            
//////            Collections.sort(chromosomes_);
//////            
//////            for (int ofsp = chromosomes_.size()-1; ofsp >= 0; ofsp--) {
//////                if(chromosomes_.size() == userInput_.population)
//////                    break;
//////
//////                tempChrome = chromosomes_.get(ofsp);            
//////                for (Chromosome chrm : chromosomes_) {
//////                    //NOTE THIS STEP .... you can use the commented one as well.....
//////                    //if(chrm != childChrome && chrm.getRankComponents().containsAll(childChrome.getRankComponents())){
//////                    if(chrm != tempChrome && chrm.getValsCopy().containsAll(tempChrome.getValsCopy())){
//////                        chromosomes_.remove(ofsp);                    
//////                        break;
//////                    }
//////                }
//////            }
//////
//////            if(chromosomes_.size() > userInput_.population){ //still more...
//////                chromosomes_ = new ArrayList<Chromosome>(chromosomes_.subList(0, userInput_.population));//pick best ones.
//////            }
//////        }
////////        int d;
////////        ArrayList<Chromosome> newRandPop = new ArrayList<Chromosome>();
////////        d = (int)Math.round(this.REPLACE_PERCENT*userInput_.population);
////////        
////////        try {
////////            initializeChromosomes(newRandPop, d);  
////////        } catch (Exception e) {
////////            e.printStackTrace();
////////            Application.getInstance().exit();
////////        }
////////
////////        for (int ofsp = 0; ofsp < d; ofsp++) {
////////            chromosomes_.set(userInput_.population-1-ofsp, newRandPop.get(ofsp));
////////        } 
//////            randomDeath();
//////    }
    
// </editor-fold>    
     
} //End of class definition
