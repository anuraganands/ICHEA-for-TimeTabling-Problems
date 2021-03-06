/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package user_code;

import csp.ByRef;
import csp.Chromosome;
import csp.CspProcess;
import csp.Element;
import csp.ExternalData;
import csp.Idx2D;
import csp.MyException;
import csp.MyMath;
import csp.MyRandom;
import csp.SolutionFoundException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.StringTokenizer;
import org.jdesktop.application.Application;


/**
 *
 * @author s425770
 */
// It should be singleton object
    
    
public class TimeTableData extends ExternalData{
    private ArrayList<ArrayList<Integer>> courses_; //10 courses_ 0-9
    /**
     * Matrix shows total conflicting students for each course.
     * size is [COURSE_SIZE] x [COURSE_SIZE];
     */
    private int conflictMatrix_[][];
    private ArrayList<Element> conflictOrder; //it contains sorted order of courses from most constrained to least constrained.
    int coeffConflictVsCourse[]; 
    private int maxSlotGroups;//horizontal size - maximum grouped constraint satisfaction allowed
    private int givenMaxSlotGroups;
    private boolean bReadAllFromFile;
    int STUDENT_SIZE;
    int COURSE_SIZE;
    boolean bdebug;
    final int MAX_DIST = 5;

    @Override
    public void alterCSPsizeBy(int inc) {
        maxSlotGroups+=inc; // it seems these are useless... as when the system it running it only uses satisfaction.size()
//        if(maxSlotGroups>givenMaxSlotGroups)
//            maxSlotGroups = givenMaxSlotGroups;
    }
//
//    @Override
//    public void decrementCSPsizeBy(int inc) {
////        throw new UnsupportedOperationException("Not supported yet.");
//        maxSlotGroups-=inc;
////        if(maxSlotGroups<1)
////            maxSlotGroups = 1;
//    }

    @Override
    public int getCSPsize() {
        return maxSlotGroups;
    }

    
    
    public TimeTableData(String absFileName, String fileName, int populaion, int generation, int curPref, 
            int prevPref, boolean saveChromes, int solutionBy, Class t, int hardConstViosTolerance) 
            throws InstantiationException, IllegalAccessException, MyException {
        super(absFileName, fileName, populaion, generation, curPref, prevPref, saveChromes, solutionBy, t, hardConstViosTolerance);        
        maxSlotGroups = 0;
        initializeCounter = 0;
        this.courses_ = new ArrayList<ArrayList<Integer>>();
        
        bReadAllFromFile = false;
        conflictOrder = new ArrayList<Element>();
        bdebug = false;
        
        this.readData();

    }

    
    
    @Override
    public void readData() throws MyException {
        //Students ID starting from 0 to max val
        //Courses ID starting frmo 0 to max val

        ArrayList<Integer> arrayList;
        
        ArrayList<Integer> students1;
        ArrayList<Integer> students2;
        int conflictStudent;
                
        Scanner dataFile;
        try {
            dataFile = new Scanner(new File(fileName_));        
        
            while (dataFile.hasNext("#")){                
                System.out.println("got #####");
                dataFile.nextLine(); //ignore comments  
            }

            STUDENT_SIZE = dataFile.nextInt();
            dataFile.nextLine(); //ignore comments

            COURSE_SIZE = dataFile.nextInt();
            dataFile.nextLine(); //ignore

            givenMaxSlotGroups = dataFile.nextInt();
            maxSlotGroups = givenMaxSlotGroups;
            dataFile.nextLine(); //ignore             

            for (int i = 0; i < COURSE_SIZE; i++) {
                arrayList = new ArrayList<Integer>();
                courses_.add(arrayList);  
            }

            StringTokenizer str;
            Integer tempCourse;
            int tempStudentID = 0;
            int sz;
            String tempStr;

            //<<For evalutation purpose only
            ArrayList<ArrayList<Integer>> studentsTmp = new ArrayList<ArrayList<Integer>>();
            for (int i = 0; i < STUDENT_SIZE; i++) {
                arrayList = new ArrayList<Integer>();
                studentsTmp.add(arrayList);  
            }
            //>>
            
            while(dataFile.hasNext()){    
                tempStr = dataFile.nextLine();
                str = new StringTokenizer(tempStr," ");
                sz = str.countTokens();
                for (int i = 0; i < sz; i++){

                    tempStr = str.nextElement().toString();                
                    tempCourse = Integer.valueOf(tempStr);
                    courses_.get(tempCourse-1).add(tempStudentID);
                    //<<For evalutation purpose only
                    studentsTmp.get(tempStudentID).add(tempCourse-1);
                    //>>
                }  
                if(sz>0)
                    tempStudentID++;                
            }
            
            //<<For evalutation purpose only
            for (int i = 0; i < STUDENT_SIZE; i++) {                 
                for (int j = 0; j < studentsTmp.get(i).size(); j++) {
                    System.out.print("s"+(i+1)+ " ");
                    System.out.println((studentsTmp.get(i).get(j)+1)+" "); 
                }
            }
            //>>
            
        
            //userInput_.fileData = true;
            userInput_.totalConstraints = courses_.size();
            userInput_.total__updatedConstraints = userInput_.totalConstraints;
            userInput_.totalDecisionVars = userInput_.totalConstraints;
            userInput_.totalObjectives = 1;
            userInput_.bHasConstraintPreferences = false;
            userInput_.bWeighted = true; 
            for (int i = 0; i < userInput_.totalDecisionVars; i++) {
                userInput_.minVals.add(0.0);
                userInput_.maxVals.add(userInput_.totalConstraints*1.0-1);            
            }  

            userInput_.validateData();
            
            System.out.flush();
            userInput_.domainVals = new  ArrayList<ArrayList<Double>>();
            for (int i = 0; i < userInput_.totalDecisionVars; i++) {
                userInput_.domainVals.add(new ArrayList<Double>());
                for (Double j = userInput_.minVals.get(i); j <= userInput_.maxVals.get(i); j++) {
                    userInput_.domainVals.get(i).add(j);
                }
            }        
            userInput_.doMutation = true;
            immunitySize = 0;//userInput_.totalDecisionVars; //this immunity is different from immunity used in chromosome.
        
            conflictMatrix_ = new int[COURSE_SIZE][COURSE_SIZE];
            coeffConflictVsCourse = new int[COURSE_SIZE];
            for (int i = 0; i < courses_.size(); i++) {
                students1 = courses_.get(i);
                for (int j = i; j < courses_.size(); j++) {
                    if(i == j){
                        conflictMatrix_[i][j] = students1.size();
                    }
                    students2 = courses_.get(j);     
                    conflictStudent = 0;
                    for (Integer astudent : students1) {
                        if(students2.contains(astudent)){
                            conflictStudent++;
                        }
                    }
                    conflictMatrix_[i][j] = conflictStudent;
                    conflictMatrix_[j][i] = conflictStudent;
                }
            }
            
            int total;
            for (int i = 0; i < COURSE_SIZE; i++) {
                total = 0;
                for (int j = 0; j < COURSE_SIZE; j++) {
                    if(conflictMatrix_[i][j]>0)
                        total += 1; // this conflict is including self... means conflict with courses + 1                   
                }
                
                conflictOrder.add(new Element(total*1.0, i));
            }  
            //Element.sortOrder = Element.DESCENDING;
            Collections.sort(conflictOrder,Collections.reverseOrder());
            
            //<<randomize
//            int changeSz = (int)(0.2*conflictOrder.size());
//            ArrayList<Integer> rnd = new ArrayList<Integer>(MyRandom.randperm(0, conflictOrder.size()).subList(0,changeSz ));
//            Collections.sort(rnd);            
//            ArrayList<Element> changedCrs = new ArrayList<Element>();
//            for (int v = rnd.size()-1; v == 0; v--) { //last one first
//                changedCrs.add(conflictOrder.remove(rnd.get(v).intValue()));
//            }            
//            for (int v = 0; v < changedCrs.size(); v++) {
//                conflictOrder.add(changedCrs.get(v));
//            }
            //>>
            
            for (int wt = 0; wt < conflictOrder.size(); wt++) {
                coeffConflictVsCourse[conflictOrder.get(wt).idx] = conflictOrder.size() - wt;                
            }  
            
            

        } catch (FileNotFoundException fnfe) {
            System.err.println("Data file not found.");
            Application.getInstance().exit();
        }
        //debug();
    }

    /**
     * 
     * @param courseIdx just provide index as conflictMatrix is built using sorted indices
     * @return 
     */
    @Override
    public ArrayList<Integer> getCluster(final ArrayList<Integer> column) {
        ArrayList<Integer> cluster = new ArrayList<Integer>();
        ArrayList<Integer> newCluster = new ArrayList<Integer>();
        int sz;
        sz = conflictMatrix_.length;
        for (int i = 0; i < sz; i++) {
            cluster.add(i);
        }
        int course;
        
        for (int i = 0; i < column.size(); i++) {
            course = column.get(i);
            for (int j : cluster) {
                if(!isViolated(course, j)){
                    newCluster.add(j);
                }
            }
//            cluster.clear();
//            cluster.addAll(newCluster);
//            newCluster.clear();            
            cluster = newCluster;
            newCluster = new ArrayList<Integer>();
        }
        
        return cluster;
    }
    
    
    
    
//    private static int sequentialSearch(ArrayList arr, final Object val){                 
//        return arr.indexOf(val);
//    }
//    
//    private int sequentialSearch(ArrayList<Integer> arr, final int val){
//        int idx = -1;
//        for (int v = 0; v < arr.size(); v++) {
//            if(arr.get(v) == val){
//                idx = v;
//                break;
//            }
//        }
//        return idx;
//    }
    @Override
    public void RAinitialize (final ArrayList<ArrayList> sat, final ArrayList<Double> fitness, 
            ArrayList<Double> vals, Idx2D[] valVsConstIdx_, ByRef tabuVios){//(final ArrayList<ArrayList> sat, final ArrayList<Double> vals, final ArrayList<Double> fitness, final ArrayList<ArrayList<Double>> aff){
        int minGrpSz = userInput_.totalConstraints/maxSlotGroups;
        int additionalGrpSz = userInput_.totalConstraints - minGrpSz*maxSlotGroups;
        
        ArrayList<Integer> rnd = MyRandom.randperm(0, userInput_.totalConstraints);
        
        sat.clear();
        vals.clear();
        int count = 0;
        int course;
        
        for (int i = 0; i < maxSlotGroups; i++){
            sat.add(new ArrayList<Integer>());
            
            for (int j = 0; j < minGrpSz; j++){
                course = rnd.get(count++);
                sat.get(i).add(course);
                vals.add(course*1.0);
            }                     
        }        
        for (int i = 0; i < additionalGrpSz; i++) {
            course = rnd.get(count++);
            sat.get(i).add(course);
            vals.add(course*1.0);
        }
        
//        RAupdateFitness(sat, fitness,vals, valVsConstIdx_, tabuVios);
        objectiveFnRefresh(sat, fitness,vals, valVsConstIdx_, tabuVios, false);
    }
    
//    @Override
//    public void RAupdateFitness(final ArrayList<ArrayList> sat, final ArrayList<Double> fitness, 
//            ArrayList<Double> vals, Idx2D[] valVsConstIdx_, ByRef tabuViolations){
//////        boolean isValid = true;
//////        int totalHardConstVios = 0;
//////        fitness.clear();
//////        int course1, course2;
//////        //check hard constraint violations
//////        for (int i = 0; i < sat.size(); i++) {
//////            for (int j = 0; j < sat.get(i).size(); j++) {
//////                for (int k = j+1; k < sat.get(i).size(); k++) {
//////                    course1 = (Integer)sat.get(i).get(j);
//////                    course2 = (Integer)sat.get(i).get(k);
//////                    if(isViolated(course1, course2)){
//////                        totalHardConstVios++;
//////                        isValid = false;
//////                        fitness.add(Double.MAX_VALUE);
//////                        fitness.add(userInput_.totalConstraints*1.0);//not right
//////                        break;
//////                    }
//////                }
//////                if(!isValid)
//////                    break;
//////            }
//////            if(!isValid)
//////                break;
//////        }
//////        
//////        if(isValid){
//            objectiveFnRefresh(sat, fitness, vals, valVsConstIdx_, tabuViolations);            
//////        }        
//    }
    
    /**
     * WRONG!!!!! This is more "expensive" than {@link TimeTableData.#RAinfluenceFull(java.util.ArrayList, java.util.ArrayList, int) }
     * @param influencee
     * @param influencer
     * @param degree_of_influence 
     */
//    @Override
//    public void RAinfluencePartial(final ArrayList<ArrayList> influencee, final ArrayList<ArrayList> influencer, int degree_of_influence) {
//        ArrayList<Integer> horizontalIdx = new ArrayList<Integer>(MyRandom.randperm(0, maxSlotGroups-1).subList(0, degree_of_influence));
//        int hIdx;//horizontal index
//        int influencerIdx,  influencerVal, influenceeIdx;
//        ArrayList<Integer> tmp;
//        
//        for (int v = 0; v < degree_of_influence; v++) {
//            hIdx = horizontalIdx.get(v);
//            influencerIdx = -1;
//            influencerVal = -1;
//            if(influencer.get(hIdx).size()>0){
//                influencerIdx = MyRandom.randperm(0, influencer.get(hIdx).size()-1).get(0); //pick a course randomly.                               
//                tmp = getCluster(influencee.get(hIdx));
//                if(tmp.isEmpty()){ //do full influence
//                   ; //you can change to "abort influence" by provide influencerIdx = -1. as well
//                }else{
//                    influencerVal = tmp.get(MyRandom.randperm(0, tmp.size()-1).get(0));//returns val 
//                    for (int j = 0; j < maxSlotGroups; j++) {
//                        influencerIdx = influencer.get(j).indexOf(influencerVal);
//                        if(influencerIdx>=0){//must return index as it is assumed that influencer is a CSP solution
//                            hIdx = j;
//                            break;
//                        }
//                    }                   
//                }
//                influencerVal = (Integer)influencer.get(hIdx).get(influencerIdx);//course val from NEW hIdx and NEW influcerIdx
//            }
//            
//            
//            if(influencerIdx>=0){
//                for (int j = 0; j < maxSlotGroups; j++) {
//                    influenceeIdx = influencee.get(j).indexOf(influencerVal);
//                    if(influenceeIdx>=0){
//                        influencee.get(j).remove(influenceeIdx);//verticle indx
//                        break;
//                    }
//                }
//                if(influencee.get(hIdx).size()<influencerIdx+1){ //size = index+1
//                    influencee.get(hIdx).add(influencerVal);//put on top. atleast it will be in the best possible location
//                }else{                    
//                    influencee.get(hIdx).add(influencerIdx, influencerVal);
//                }                
//            }            
//        }
//    }
//    
    

    /**
     * This is more "expensive" than {@link TimeTableData.#RAinfluenceFull(java.util.ArrayList, java.util.ArrayList, int) }
     * @param influencee
     * @param influencer
     * @param degree_of_influence 
     */
    @Override
    public void RAinfluencePartial(final ArrayList<ArrayList> influencee, final ArrayList<ArrayList> influencer, int degree_of_influence) {
        MyRandom rnd = new MyRandom();
        ArrayList<Integer> horizontalIdx = new ArrayList<Integer>(rnd.randComb(0, maxSlotGroups-1, degree_of_influence));
        int hIdx;//horizontal index
        int influencerIdx,  influencerVal, influenceeIdx;
        ArrayList<Integer> tmp;
        
        for (int i = 0; i < degree_of_influence; i++) {
            hIdx = horizontalIdx.get(i);
            influencerIdx = -1;
            influencerVal = -1;
            if(influencer.get(hIdx).size()>0){
                influencerIdx = MyRandom.randperm(0, influencer.get(hIdx).size()).get(0); //pick a course randomly.
                influencerVal = (Integer)influencer.get(hIdx).get(influencerIdx);//course val
            }
            
            if(influencerIdx>=0){
                //do partial influence into same influencerID
                tmp = getCluster(influencer.get(hIdx));
                if(tmp.isEmpty()){ //do full influence
                   influencerVal = influencerVal; //you can change to "abort influence" by provide influencerIdx = -1. as well
                }else{
//                    for (int j = 0; j < tmp.size(); j++) {
//                        
//                    }
                    influencerVal = tmp.get(MyRandom.randperm(0, tmp.size()).get(0));
                }
                for (int j = 0; j < maxSlotGroups; j++) {
                    influenceeIdx = influencee.get(j).indexOf(influencerVal);
                    if(influenceeIdx>=0){
                        influencee.get(j).remove(influenceeIdx);//verticle indx
                        break;
                    }
                }
                if(influencee.get(hIdx).size()<influencerIdx+1){ //size = index+1
                    influencee.get(hIdx).add(influencerVal);//put on top. atleast it will be in the best possible location
                }else{                    
                    influencee.get(hIdx).add(influencerIdx, influencerVal);
                }                
            }            
        }   
    }
    
    @Override
    public void RAinfluenceKempe(final ArrayList<Double> valsInfluencee, final ArrayList<Double> fitnessInfluencee,
    final ArrayList<ArrayList> satInfluencee, final Idx2D[] valVsConstIdxInfluencee, final ArrayList<Integer> noGoodInfluencee,
    final ByRef tabuVios, final ArrayList<ArrayList> satInfluencer, final int degree_of_influence) throws Exception{
        MyRandom rnd = new MyRandom();
        //very slow man for big data.... buuu...
        ArrayList<Integer> horizontalIdx = new ArrayList<Integer>(rnd.randComb(0, maxSlotGroups-1, degree_of_influence));

        
        int hIdx;//horizontal index
        int influencerIdx,  influencerVal, influenceeIdx;
        
        //kempe vars
        int Tj;
        Element valTo;
        
        for (int i = 0; i < degree_of_influence; i++) {
            hIdx = horizontalIdx.get(i); // (int)(Math.random()*maxSlotGroups); //can have repetition
            influencerIdx = -1;
            influencerVal = -1;
            if(satInfluencer.get(hIdx).size()>0){
                influencerIdx = MyRandom.randperm(0, satInfluencer.get(hIdx).size()).get(0); //pick a course randomly.
                influencerVal = (Integer)satInfluencer.get(hIdx).get(influencerIdx);//course val
            }

            if(influencerIdx>=0){                
//                influenceeIdx = valsInfluencee.indexOf(new Double(influencerVal)); 
                Tj = hIdx;//This is  influen"CER" s column.
//                valTo = new Element(influenceeIdx, Tj); //carryies influenceeidx and influencer Idx Tj. valTo -> Tj valTo will go to Tj
                valTo = new Element(influencerVal, Tj);
                //valTo: idx refers - TO, val refers - From.
                
                kempe(satInfluencee, valsInfluencee, valVsConstIdxInfluencee, fitnessInfluencee, 
                    noGoodInfluencee,tabuVios, 0, 0, false, false, 1, valTo, false);
            }
        }
                   
        tabuVios.setValue(tabuConsViolation(valsInfluencee, satInfluencee,valVsConstIdxInfluencee)); 
    }
    
  
/**
     * Do not need to call <code>{@link TimeTableData.#objectiveFnRefresh(java.util.ArrayList, 
     * java.util.ArrayList, java.util.ArrayList, csp.Idx2D[], csp.ByRef, boolean)}</code>
     * or <code>{@link TimeTableData.#objectiveFnReset(java.util.ArrayList, java.util.ArrayList, 
     * java.util.ArrayList, java.util.ArrayList, csp.Idx2D[], csp.ByRef) }</code>
     * after calling this method.
     * @param valsInfluencee
     * @param fitnessInfluencee
     * @param satInfluencee
     * @param valVsConstIdxInfluencee
     * @param tabuVios
     * @param satInfluencer
     * @param degree_of_influence
     * @throws Exception 
     */
    @Override
    public void RAinfluenceFull(final ArrayList<Double> valsInfluencee, final ArrayList<Double> fitnessInfluencee,
    final ArrayList<ArrayList> satInfluencee, final Idx2D[] valVsConstIdxInfluencee, final ByRef tabuVios,
    final ArrayList<ArrayList> satInfluencer, final int degree_of_influence) throws Exception{
        MyRandom rnd = new MyRandom();
        //very slow man for big data.... buuu...
        ArrayList<Integer> horizontalIdx = new ArrayList<Integer>(rnd.randComb(0, maxSlotGroups-1, degree_of_influence));
//        ArrayList<Integer> horizontalIdx =new ArrayList<Integer>();
//        horizontalIdx.add((int)(Math.random()*maxSlotGroups));        
        
        
        int hIdx;//horizontal index
        int influencerIdx,  influencerVal, influenceeIdx;
        
        
        for (int i = 0; i < degree_of_influence; i++) {

            hIdx = horizontalIdx.get(i); // (int)(Math.random()*maxSlotGroups); //can have repetition
            influencerIdx = -1;
            influencerVal = -1;
            if(satInfluencer.get(hIdx).size()>0){
                influencerIdx = MyRandom.randperm(0, satInfluencer.get(hIdx).size()).get(0); //pick a course randomly.
                influencerVal = (Integer)satInfluencer.get(hIdx).get(influencerIdx);//course val
            }

            if(influencerIdx>=0){                
                influenceeIdx = valsInfluencee.indexOf(new Double(influencerVal)); 

                objectiveFnRemove(valsInfluencee, fitnessInfluencee, true, satInfluencee, 
                    valVsConstIdxInfluencee, influenceeIdx,tabuVios);  
                                
                //insert
                if(influencerIdx<satInfluencee.get(hIdx).size()){
                    satInfluencee.get(hIdx).add(influencerIdx, influencerVal);
                    valVsConstIdxInfluencee[influencerVal].col = hIdx;
                    valVsConstIdxInfluencee[influencerVal].position = influencerIdx;
                    
                    for (int j = influencerIdx+1;j<satInfluencee.get(hIdx).size(); j++) {                        
                        valVsConstIdxInfluencee[(Integer)satInfluencee.get(hIdx).get(j)].position++;
                    }
                }else{//add
                    satInfluencee.get(hIdx).add(influencerVal);
                    influencerIdx = satInfluencee.get(hIdx).size()-1;//last index
                    valVsConstIdxInfluencee[influencerVal].col = hIdx;
                    valVsConstIdxInfluencee[influencerVal].position = influencerIdx;
                }
                
                valsInfluencee.add(influencerVal*1.0);
                
                Double localFit[];
//                double fit, hardVios;
                localFit = getValidIndividualPref(influencerIdx, hIdx, satInfluencee); //first one removed above
                            
                for (int j = 0; j < localFit.length; j++) {
                    fitnessInfluencee.set(j, fitnessInfluencee.get(j)+localFit[j]);
                }
//                ?? check if hard vio then where???? update with kemp call from mutaion
//                fit = fitnessInfluencee.get(0) + localFit[0];
//                hardVios = getTotalHardVios(fitnessInfluencee) + localFit[1];
//                fitnessInfluencee.set(0, fit);
//                fitnessInfluencee.set(fitnessInfluencee.size()-1, hardVios);
            }   
        } 
        tabuVios.setValue(tabuConsViolation(valsInfluencee, satInfluencee,valVsConstIdxInfluencee));        
    }

    

    
//    @Override
//    public ArrayList<Double> negateVal(ArrayList<Double> vals) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

    @Override
    public int popConstLimit(int amt) {           
        int constTo, consFrom = -1;
        boolean bRemoved;
        if(!constLimits.isEmpty())
            constTo = constLimits.get(constLimits.size()-1);//not required actually
        
        bRemoved = false;
        for (int i = 0; i < amt; i++) {
            if(constLimits.isEmpty()){
                constLimits.add(0);
                break;
            }else{
                bRemoved = true;
                constLimits.remove(constLimits.size()-1);
            }             
        } 
        
        if(bRemoved){
            if(constLimits.isEmpty()){
                constLimits.add(0);
                consFrom = 0;
            }else{
                consFrom = constLimits.get(constLimits.size()-1)-1;
            }
        }
        return consFrom;
    }
    
    @Override
    public void cleanCSP(ArrayList<ArrayList> sat, int threshold){
        ArrayList<Integer> prohibited = new ArrayList<Integer>();
        for (int i = threshold; i < conflictOrder.size(); i++) {
            prohibited.add(conflictOrder.get(i).idx);
        }

        for (int i = 0; i < sat.size(); i++) {
            for (int j = 0; j < sat.get(i).size(); j++) {
                if(prohibited.contains((Integer)sat.get(i).get(j))){
                    sat.get(i).remove(j);
                    j--;
                }                
            }            
        }
        
    }       
    
    @Override
    public ArrayList<Chromosome> initializeExternalChrmosomes(final int population, boolean bRestart) throws SolutionFoundException{ //, final boolean bInitialStage) {
        Chromosome chrome;
        ArrayList<Chromosome> newChromes = new ArrayList<Chromosome>();
        int modVal;
        //int prevPref;
        
        if(userInput_ == null)
            throw new UnsupportedOperationException("User input not initialized");
    
        if(!bReadAllFromFile && this.getPrevPref() >= 0){
            try {           
                FileInputStream fis = new FileInputStream("partial_solutions_pref_" + this.getPrevPref() + ".ichea");
                ObjectInputStream ois = new ObjectInputStream(fis);
                newChromes = (ArrayList<Chromosome>)ois.readObject();
                
                this.immunitySize = newChromes.get(0).getVals().size(); //I hope only satisfaction is used...
                this.pStart = newChromes.get(0).getExternalData().getpStart();
                
                for (Chromosome nc : newChromes) {
                    nc.setExternalData(this);
                }
                ois.close();
            }
            catch(Exception e) {
                System.out.println("Exception during deserialization: " + e);
                System.exit(0);
            }

            bReadAllFromFile = true; // rest are discarded ... as in next call to initializeExternalChrmosomes data will not be read from a file
            if(newChromes.size()>population){
                newChromes = (ArrayList<Chromosome>)newChromes.subList(0, population);
                return newChromes;
            }
        }

                
        modVal = CspProcess.getCurAcceptedConstraints(userInput_.totalConstraints);
        int lastIdx = constLimits.size()-1;
        if(modVal != constLimits.get(lastIdx)){
            if(!bRestart)
                initializeCounter = constLimits.get(lastIdx);//new vals AFTER the previous parial sol
            constLimits.add(modVal);
            lastIdx++;
//            initializeCounter = 0;
        }
//        refreshConstLimits();
//        int lastIdx = constLimits.size()-1;
        
        int prevIdx = constLimits.size()-2;
        if(bRestart)
            prevIdx = 0;
        
        modVal = constLimits.get(lastIdx)- constLimits.get(prevIdx); //with last 2 limits
        int loc = -1;
        
        for (int i = 0; newChromes.size() < population; i++) { 
            chrome = new Chromosome(userInput_.solutionBy, this); 
  
            if(loc == Integer.MAX_VALUE){
                loc = 0;
            }
            
            if(CspProcess.isSolution(CspProcess.getBestSoFarCOP()) || CspProcess.changeSpaceMode){
                modVal = constLimits.get(lastIdx);
                loc = (initializeCounter++ % modVal);
                
            }else{
                loc = constLimits.get(prevIdx) + (initializeCounter++ % modVal);
            }            
//            loc = (initializeCounter++) % constLimits.get(lastIdx);
            
            
            loc = conflictOrder.get(loc).idx;
            
            chrome.appendVal(loc);
            newChromes.add(chrome);                     
        }
                  
        if(bdebug){
            userInput_.bWeighted = true;
            ArrayList<Double> fit = new ArrayList<Double>();
//            ArrayList<ArrayList<Double>> aff = new ArrayList<ArrayList<Double>>();
            Idx2D[] valVsConstIdx_ = new Idx2D[userInput_.totalConstraints];
            debug(newChromes.get(0).getSatisfaction(), fit, valVsConstIdx_, new ByRef(new Integer(0)), false);  
            System.out.println("The input solution is: " + newChromes.get(0));
            System.out.println("The soulution is: "+fit.get(0)/STUDENT_SIZE);
            Application.getInstance().exit();
        }

        return newChromes;
    }

    /**
     * Refreshes the constLimits array which is generally changed by {@link CspProcess#alterAcceptedConstRatio(boolean, boolean) }
     * or from GUI.
     * @return Size of constlimit to hel
     */
    @Override
    public void refreshConstLimits() {
        int modVal = CspProcess.getCurAcceptedConstraints(userInput_.totalConstraints);
        int lastIdx = constLimits.size()-1;
        if(modVal > constLimits.get(lastIdx)){
            constLimits.add(modVal);
            lastIdx++;//?
            initializeCounter = 0;
        }else if(modVal<constLimits.get(lastIdx)){            
            while(modVal>=constLimits.get(lastIdx)){
                constLimits.remove(lastIdx);
                lastIdx--;
            }
        }
    }

    @Override
    public void refreshValVsConstIdx(final Idx2D[] valVsConstIdx_, final ArrayList<ArrayList> sat) {      
        int col;
        int pos;

//        valVsConstIdx_ = new Idx2D[userInput_.totalConstraints];
        
        for (int j = 0; j < valVsConstIdx_.length; j++) {
            valVsConstIdx_[j] = new Idx2D();
        }

        col = -1;
        for (ArrayList<Integer> list : sat) {
            col++;
            pos = -1;
            for (Integer val : list) {
                pos++;
                valVsConstIdx_[val].col = col;
                valVsConstIdx_[val].position = pos;
            }
        }
    }
    
    
    
        /**
     * NOTE: input must be a full solution otherwise won't work or throw exception.
     * @param sat
     * @param vals
     * @param fitness_
     * @param valVsConstIdx_
     * @param tabuViolations
     * @return
     * @throws Exception 
     */
    @Override
    public boolean mutationCluster(final ArrayList<ArrayList> sat, final ArrayList<Double> vals, ArrayList<Double> fitness_, 
    final Idx2D[] valVsConstIdx_, final ByRef tabuViolations) throws SolutionFoundException{
        
        int posFromH = -1;//Horizontal and Verticle positions respectively
    
        Integer clusterVal = -1; //MUST use "Integer" because using remove(Object) below.
        ArrayList<Integer> Hrand;
        Hrand = MyRandom.randperm(0, sat.size());
        boolean bFound = false;
        ArrayList<Integer> tmpColumn;
        MyRandom rnd = new MyRandom();
        
//        double curFit = fitness_.get(0);
//        double hardVios = getTotalHardVios(fitness_);
        Double []localFit1;
        Double []localFit2;
      
        for (int i = 0; i < sat.size(); i++) {
            posFromH = Hrand.get(i);
            if(sat.get(posFromH).isEmpty()){
                continue;
            }
            for (int j = 0; j < sat.get(posFromH).size(); j++) {
                tmpColumn = getCluster(sat.get(posFromH));
                if(tmpColumn.size()>0){ //values exist other than valFrom
                    clusterVal = tmpColumn.get(rnd.randVal(0, tmpColumn.size()-1));
                    bFound = true;
                    break;
                }
            }
            if(bFound){
                break;
            }
        }
        
        if(!bFound){
            return bFound;
        }
                   
        bFound = false;

        
        //fitness before
        int pos;
       
        for (int col = 0; col < sat.size(); col++) {
            if(col == posFromH){
                continue;
            }
            pos = sat.get(col).indexOf(clusterVal);
            if(pos >=0 ){
                //set valvsIndx
                localFit1 = getValidIndividualPref(pos, col, sat); //both avail  
                if(!sat.get(col).remove(clusterVal)){ 
                    throw new SolutionFoundException("Error! how come???");
                }
                 
                for (int i = pos; i < sat.get(col).size(); i++) {
                    valVsConstIdx_[(Integer)sat.get(col).get(i)].position = i;
                }
                
                sat.get(posFromH).add(clusterVal);
                valVsConstIdx_[clusterVal].col = posFromH;
                valVsConstIdx_[clusterVal].position = sat.get(posFromH).size()-1;  
                
                localFit2 = getValidIndividualPref(sat.get(posFromH).size()-1, posFromH, sat); //both avail 
                //set valvsindx
                             
                bFound = true;  
                
                for (int j = 0; j < localFit1.length; j++) {
                    fitness_.set(j, fitness_.get(j)-localFit1[j]);
                }
                for (int j = 0; j < localFit2.length; j++) {
                    fitness_.set(j, fitness_.get(j)+localFit2[j]);
                }
                break;
            }
        }          
        
        tabuViolations.setValue(tabuConsViolation(vals, sat, valVsConstIdx_));
        
        return bFound;
    }
    
    
        /**
     * NOTE: Assumes valid input because it discards hard violation constraints.
     * it is used for full solutions only
     * version.1: Does not solve hard constraint violation.<BR> 
     * <B>NOTE</B>: if input {@code sat} has hard constraint violation then
     * after kempe it is not necessary the solution will become feasible. It 
     * depends on the data.
     * version. 2: has high chance of getting feasible solution from infeasible swapping.
     * @param sat
     * @param vals
     * @param valVsConstIdx
     * @param fitness_
     * @param noGood
     * @param tabuViolations
     * @param Apercent usually 0.05 - low value shows high individual constrained course.
     * @param Bpercent usually 0.20 - high value shows low individual constraint. 
     * @param inCorner
     * @param useAppend
     * @param n
     * @param valFromAndTj has two components: (val, idx). <B>NOTE:</B> val is a value
     * in the {@code sat} somewhere in Ti. idx is the location (Tj) where it is supposed to go.
     * Don't think that val's corresponding index is idx. This is NOT the case
     * it is credentials for expected destination.
     * @param isDynamic
     * @throws SolutionFoundException 
     */
    @Override
    public void kempe(final ArrayList<ArrayList> sat, final ArrayList<Double> vals,
    final Idx2D[] valVsConstIdx, final ArrayList<Double> fitness_, final ArrayList<Integer> noGood, 
    final ByRef tabuViolations,final double Apercent, final double Bpercent, final boolean inCorner, 
    final boolean useAppend, final int n, Element valFromAndTj, final boolean isDynamic) throws SolutionFoundException{
        ArrayList hardViosJ = new ArrayList();
        ArrayList hardViosI = new ArrayList();
        boolean takenValHasConflicts = false;
        boolean fixedVal = false;
        
        int Ti = -1;
        int Tj = -1;
        int takenVal=-1;
        
        
        if(valFromAndTj == null){
            takenVal = -1;                       
        }else{
            takenVal = (int)valFromAndTj.val;
            Ti = valVsConstIdx[takenVal].col;
            if(valFromAndTj.idx>=0){
                Tj = valFromAndTj.idx;
            }
            fixedVal = true;
        }
        
        
        ArrayList<Integer> rnd= MyRandom.randperm(0, sat.size());
        
        if(Ti<0)
            Ti = rnd.get(0);
        
        if(Tj<0)
            Tj = rnd.get(1);
        
        if(Tj == Ti){
            Tj = (Ti+sat.size()/2)%sat.size();
        }
        //Steps1: Ti and Tj selected, randomly or from user input (generally when
        //boundary values are needed) - but never used
        
        if(!fixedVal){          
            if(sat.get(Ti).isEmpty()){
                return;
            }else{
                takenVal = MyRandom.randperm(0, sat.get(Ti).size()).get(0); //pick an index
                takenVal = (Integer)sat.get(Ti).get(takenVal); //now get a value
            }
        }
        
        //step 2: get a value randomly in Ti
        
        if(Apercent>Bpercent){
            throw new UnsupportedOperationException("Apercent should be less than Bpercent");
        }

        if(Apercent >0){
            int val, tmpFit;
            ArrayList<Element> fit = new ArrayList<Element>();
            
            for (int i = 0; i < sat.size(); i++) {
                for (int j = 0; j < sat.get(i).size(); j++) {
                    val = (Integer)sat.get(i).get(j);
                    tmpFit = getValidIndividualPref(j, i, sat)[0].intValue();
                    fit.add(new Element(tmpFit, val));
                }
            }
            
            if(fit.size()<3){
                return;
            }
            
            //Element.sortOrder = Element.DESCENDING;
            Collections.sort(fit, Collections.reverseOrder());
            
            MyRandom rndobj = new MyRandom();
 
            int takenIdx = -1;
            if(!fixedVal){
                takenIdx = rndobj.randVal(0, (int)(Apercent*(fit.size()-1)));
                takenVal = fit.get(takenIdx).idx; //its index is the value of val
                Ti = valVsConstIdx[takenVal].col;
            
            
                //now get Tj from Bpersent
                int from = Math.min(takenIdx+1,fit.size()-2); // assumed fit.size()>2
                ArrayList<Integer> valIdx = MyRandom.randperm(from, Math.max(from+1,(int)(Bpercent*(fit.size()))));
                Tj = Ti;
                for (int i = 0; i < valIdx.size(); i++) {
                    Tj = valIdx.get(i);
                    Tj = fit.get(Tj).idx; //its index is the value of val
                    Tj = valVsConstIdx[Tj].col;   
                    if(Ti != Tj){
                        break;
                    }
                }
            }
        }      
            //Step 2: Pick Ti based on fitness.
            
        if(inCorner){
            int from, to; // Ti is from, Tj is to
            if(Math.random()<0.5){
                from = 0;
                to = MAX_DIST*1/3;
            }else{
                from = (sat.size()-1) - MAX_DIST*1/3;
                to = sat.size();
            }
            ArrayList<Integer> valIdx = MyRandom.randperm(from,to);
            for (int i = 0; i < valIdx.size(); i++) {
                if(valIdx.get(i) != Ti){
                    Tj = valIdx.get(i);
                    break;
                }
            }
        }
        //select Tj based on fitness or if corner is selected.

        if(Tj == Ti){
            Tj = (Ti+sat.size()/2)%sat.size();
        }

             

        ArrayList<Integer> toBeRemoved = new ArrayList<Integer>();
        
        if(n<0){
            System.err.println("No longer works for negative n val.");
        }
        
        if(n == 0)
            return;
         
        if(n>=1){ // if you want more values to be removed but it will only happen if
            ArrayList<Integer> rIdx = MyRandom.randperm(0, sat.get(Ti).size());
                
            int maxPos = Math.min(n, sat.get(Ti).size()-1);
            int tVal;
            int firstTakenVal = takenVal;

            for (int m = 0; m < maxPos; m++) {                                
                for (int jIdx = 0; jIdx < sat.get(Tj).size(); jIdx++) {  
                    if(hardConstraintViolated(takenVal,(Integer)sat.get(Tj).get(jIdx))){
                        takenValHasConflicts = true;
                        break;
                    }
                }       

                if(!takenValHasConflicts){                          
                    Double [] tmp = getValidIndividualPref(valVsConstIdx[takenVal].position, 
                        valVsConstIdx[takenVal].col, sat);

                    for (int j = 0; j < tmp.length; j++) {
                        fitness_.set(j, fitness_.get(j)-tmp[j]);
                    }

                    // if NO conflict then just exchange easily
                    sat.get(Ti).remove(valVsConstIdx[takenVal].position);
                    sat.get(Tj).add(takenVal);
                    rIdx = MyRandom.randperm(0, sat.get(Ti).size()); //do it later...
//                    m = -1;
//                    m--;
                    
                    for (int i = valVsConstIdx[takenVal].position; i < sat.get(Ti).size(); i++) {
                        valVsConstIdx[(Integer)sat.get(Ti).get(i)].position = i;
                    }
                    valVsConstIdx[takenVal].col = Tj;
                    valVsConstIdx[takenVal].position = sat.get(Tj).size()-1;

                    double curFit, curHardVios;
                    tmp = getValidIndividualPref(valVsConstIdx[takenVal].position, 
                        valVsConstIdx[takenVal].col, sat);

                    for (int j = 0; j < tmp.length; j++) {
                        fitness_.set(j, fitness_.get(j)+tmp[j]);
                    }

                    //return;
                }else{//cannot remove easily from sat. do the following kempe thing
                    hardViosI.add(takenVal);//taken values only MARKED
                    toBeRemoved.add(valVsConstIdx[takenVal].position); //sat is still not updated... This is in Ith column which needs to be moved to Jth column.
                    
                    //hardViosI misnomer here. hardViosI is only used to indicate how many
                    //values from column "I" will be transfered to "J".
                    //it is NOT that hardViosI is violations in I.
                    if(hardViosI.size()>=n){
                        break;
                    }
//                    tVal = (Integer)sat.get(Ti).get(rIdx.get(i)); 
//                    if(tVal == takenVal)
//                        continue;
//                    hardViosI.add(tVal); // don't worry if it is not hardvio it will be removed below. MISNOMER....
//                    toBeRemoved.add(valVsConstIdx[tVal].position);
                }
//                if(m>Math.min(n, sat.get(Ti).size()-1)){
//                    break;
//                }
                
                takenVal = (Integer)sat.get(Ti).get(rIdx.remove(0)); 
                if(takenVal == firstTakenVal){
                    if(rIdx.size()>0)
                        takenVal = (Integer)sat.get(Ti).get(rIdx.remove(0)); 
                    else
                        break;
                }
                        
            }
        }
        //Now in case of conflict do the following....
            
           
            
//            sat.get(Ti).remove(valVsConstIdx[takenVal].position);
                    
            
            Collections.sort(toBeRemoved);            

            for (int i = toBeRemoved.size()-1; i>=0; i--) {
                sat.get(Ti).remove(toBeRemoved.get(i).intValue());                
            }
   
//        if(useAppend){
//            boolean violated;           
//            
//            for (int i = 0; i < hardViosI.size() && i >= 0; i++) {
////                for (int s = 0; s < sat.size(); s++) {
//                 for (int s : MyRandom.randperm(0,sat.size()-1)) {
//                    if(s == Ti){// || s == Tj){
//                        continue;
//                    }
//                    violated = false;
//                    for (int k = 0; k < sat.get(s).size(); k++) {
//                        if(hardConstraintViolated((Integer)hardViosI.get(i),(Integer)sat.get(s).get(k))){
//                            violated = true;
//                            break;
//                        }
//                    }
//                    if(!violated){
//                        sat.get(s).add(hardViosI.get(i));
//                        hardViosI.remove(i--);
//                        break;
//                    }
//                }
//            }
//        }
               
        ArrayList conflicts = new ArrayList();
        
        int idx = 0;
        conflicts.addAll(hardViosI);
        hardViosI = new ArrayList();
        final int []Ts = {Ti, Tj};
        
        //sat: all vioations has been removed just above. Now...
        while(true){
            if(idx == 0){
                hardViosI.addAll(conflicts);
            }else{
                hardViosJ.addAll(conflicts);
            }
            
            idx = (idx+1)%2;
            conflicts = columnConflicts(sat, conflicts, Ts[idx], Ts[(idx+1)%2]);
 
            if(conflicts.isEmpty()){
                break;
            }
        }

        //now swap
        sat.get(Ti).addAll(hardViosJ);
        sat.get(Tj).addAll(hardViosI);
        
        double prevV, curV;
        prevV = getTotalHardVios(fitness_);
        objectiveFnRefresh(sat, fitness_, vals, valVsConstIdx, tabuViolations, isDynamic); 
        curV = getTotalHardVios(fitness_);
        if(curV>prevV){ //but can still have hard costraint violation but a violated column is just swapped from I to J.
            System.err.println("bad robot.............");
        }        
    }
    
    public double getTotalHardVios(final ArrayList<Double> fitness_) {
        if(userInput_.bWeighted)
            return fitness_.get(1);
        else
            return fitness_.get(fitness_.size()-1);
    }  
    
    /**
     * This function is only for a given column. If that column has conflict 
     * from the given values then the conflict causing values will be reported
     * and removed so that new values can be included in that column.
     * kempe: force in new values from {@code conflictsFrom} into {@code sat}
     * if there is any conflict then remove it from {@code sat} and put them 
     * into the returned arraylist {@code conflictsTo}
     * @param sat
     * @param conflictsFrom
     * @param idxTo
     * @return 
     */
    private ArrayList columnConflicts(final ArrayList<ArrayList> sat, final ArrayList conflictsFrom, final int idxTo, final int idxFrom){
        Object oi, oj, ok;
        ArrayList conflictsTo = new ArrayList();
        boolean bViolated = true;
        
        ArrayList<Integer> rIdx = MyRandom.randperm(0, sat.size());
        
        for (int iIdx = 0; iIdx < conflictsFrom.size(); iIdx++) {
            oi = conflictsFrom.get(iIdx);
           
            for (int jIdx = 0; jIdx < sat.get(idxTo).size(); jIdx++) {
                oj = sat.get(idxTo).get(jIdx);
                if(hardConstraintViolated((Integer)oi, (Integer)oj)){
                    
                    bViolated = true;
//                    for (int i = 0; i < sat.size(); i++){ //try randomize it!
                    if(CspProcess.getBestSoFarCOP().isSolution()){ //new added feature call it diffused-kempe
                        for (Integer i : rIdx) {
                            if(i == idxTo || i == idxFrom)
                                continue;
                            bViolated = false;
                            for (int j = 0; j < sat.get(i).size(); j++){
                                ok = sat.get(i).get(j);
                                if(hardConstraintViolated((Integer)ok, (Integer)oj)){   
                                    bViolated = true;
                                    break;
                                }
                            }
                            if(!bViolated){//not violated in any member in a column
                                sat.get(i).add((Integer)oj); //this can be accepted in one of the column  
                                sat.get(idxTo).remove(jIdx);
                                jIdx--;
                                break;
                            }
                        }
                        if(!bViolated){ //oi is removed, get the new one
                            continue;
                        } 
                    }

                    //take out those who are causing conflict(s)
                    conflictsTo.add(oj);
                    sat.get(idxTo).remove(jIdx);
                    jIdx--;
                }
            }
        }        
        return conflictsTo;
    }
    
    
    @Override
    public ArrayList<Double> getNoGoodsPartialCSP(ArrayList<Double> vals) {        
        vals = new ArrayList<Double>(vals);
        ArrayList<Double> noGoods = new ArrayList<Double>(); 
                
        if (vals.size() < 1){
            return noGoods;
        }
        
        
        int expectedVal;
        int lastIdx = constLimits.size()-1;
        
        ArrayList<Integer> partialCSPidx = new ArrayList<Integer>();
        
        //PLEASE CONSIDER THIS
        int maxVals = Math.max(CspProcess.getBestSoFarCSP().getVals().size(), CspProcess.getBestSoFarCOP().getVals().size());
        maxVals = Math.max(maxVals, constLimits.get(lastIdx));
        
        for (int i = 0; i < maxVals; i++) { //NOTE it is for CSP I think that's why < sign and not  <=
            partialCSPidx.add(conflictOrder.get(i).idx);//All possible valid values....
        }
        
        Collections.sort(vals);
        Collections.sort(partialCSPidx);
        
        int tmpVal;
        int e = 0;
        try{
        for (int v = 0; v < vals.size(); v++,e++) {  
            expectedVal = partialCSPidx.get(e);
            tmpVal = vals.get(v).intValue();
            if(tmpVal != expectedVal){
                while(expectedVal != tmpVal){
                    noGoods.add(expectedVal*1.0);
                    expectedVal = partialCSPidx.get(++e);
                }
            }           
        }
        }catch(IndexOutOfBoundsException ex){
            ex.printStackTrace();
            System.out.println(vals);
            System.out.println(partialCSPidx);
            System.out.println(noGoods);
            System.out.println(constLimits);
            System.out.println(CspProcess.getCurAcceptedConstraints(userInput_.totalConstraints));
        }
        
        return noGoods;
        
    }

    
    /**
     * Check violation in 2 given courses_. It checks if there is overlap of 2
     * or more students.
     * @param obj1 - First course index - note index starts from 0
     * @param obj2 - Second course index - note index starts from 0
     * @return if there is overlap of 2 or more students that means there is
     * a violation and true is returned, otherwise false is returned.
     */

    
    private int penaltyFn(int dist){
        int weight = Integer.MAX_VALUE;
        if(dist == 0){
            weight = 32; //Integer.MAX_VALUE; //16;???????????? BADDDDDDDDDD
        }else if (dist == 1){
            weight = 16; //8;
        }else if (dist == 2){
            weight = 8;//4;
        }else if (dist == 3){
            weight = 4;//2;
        }else if (dist == 4){
            weight = 2; //1;
        }else if(dist > 4){
            weight = 1;//0;
        }else{
            throw new UnsupportedOperationException("dist must be > 0.");
        }
        return weight;
    }
    
    /**
     * 
     * @param dist maximum value of dist = levels - 1;
     * @return 
     */
    private int prefFn(int dist){
        int levels = 5;
        int pref = Integer.MAX_VALUE;
        
        if(dist > 0){
            pref = levels - dist;
            if(pref < 0)
                pref = 0;
        }else{
            ;
            //throw new UnsupportedOperationException("dist cannot be more than max level defined.");
        }
        
        return pref;
    }

    @Override
    public void printProblemSpecificSol(ArrayList<ArrayList> sat) {
        int TTarrangement[][] = new int[COURSE_SIZE][2];        
        
        int counter = 1;
        for (int i = 0; i < sat.size(); i++) {
            for (int j = 0; j < sat.get(i).size(); j++) {
                TTarrangement[(Integer)sat.get(i).get(j)][0] = (Integer)sat.get(i).get(j)+1;
                TTarrangement[(Integer)sat.get(i).get(j)][1] = i;
            }
        }
        
//        for (int v = 0; v < conflictMatrix_.length; v++) {
//            for (int j = 0; j < conflictMatrix_.length; j++) {
//                System.out.print(conflictMatrix_[v][j]+" ");
//            }
//            System.out.println();
//        }
        
        for (int i = 0; i < TTarrangement.length; i++) {            
            System.out.println(TTarrangement[i][0] + " " + TTarrangement[i][1]);            
        }
    }

    
    
    /**
     * 
     * @param fnVal it is prefFn or penaltyFn
     * @param satisfiedCons - total satisifactions
     * @return fitness value
     */
//    private double curOjbectiveFnUpdate(final int maxPref, final int prefVal, final int satisfiedCons){
//        //prefVal = (prefVal+1)*(userInput_.totalConstraints - satisfiedCons+1)-1;
//        //return prefVal*1.0;
//        /// f(Lenght, pref) = f(k,p) = (2.MaxP+1)^(L-l).(p+1) 
//        double val = Math.pow(2.0*maxPref+1,userInput_.totalConstraints*1.0-satisfiedCons)*(prefVal + 1.0);
//        //return val/Math.pow(2.0*maxPref, userInput_.totalConstraints/2.0);
//        return val;
//    }
    

    /**
     * <B>WARNING:</B> use this method very carefully as it updates previous value of fitness
     * @param penalty current preference = penalty*effected_students
     * @param fitness
     * @return 
     */
    @Override
    public double updateFitnessValWeightBased(final int penalty, ArrayList<Double> fitness){        
        //double ifitness = curStudentSize*penaltyFn(dist); 
        final int L = userInput_.totalConstraints;
        final double totalWorstCaseVal;
        final double individualWorstCaseVal;
        final double maxPenalty = penaltyFn(0);
        
                
        totalWorstCaseVal = 1.0*L*(L-1)*STUDENT_SIZE*maxPenalty;
        individualWorstCaseVal = 1.0*STUDENT_SIZE*(L-1)*maxPenalty;
        
        double curFitness;
        
        if(fitness.isEmpty()){
            fitness.add(totalWorstCaseVal);                 
        }  

        curFitness = fitness.get(0)-individualWorstCaseVal;
        curFitness+= penalty;    
        
        if(fitness.get(0)<0 || curFitness < 0){
            System.out.println("eee kaisey....");
        }  

        fitness.set(0,curFitness); //one just added          
        return fitness.get(0);        
    }
    private double curOjbectiveFnUpdate(final int D, final int p, final int constrainedWt, ArrayList<Double> fitness){
//        if(!CspProcess.bOptimizationMode)
//            throw new UnsupportedOperationException("System should be in Optimization mode!!!"); //need to check why???
            //I think it can be workable for non-optimization mode as well.
        if(userInput_.bWeighted){
            return updateFitnessValWeightBased(p, fitness);
        }else{
            return fitnessValPrefBased(D, p, constrainedWt, fitness);
        }
    }

    @Override
    protected int maxPref() { 
        int D = prefFn(0);
        if(userInput_.bWeighted){
            //return penaltyFn(0);
            //return 2*D*STUDENT_SIZE*penaltyFn(0);
            return Integer.MAX_VALUE;   //??? it should be 2*D*Student_SIze*penalty(0)???         
        }else{
            return 5;//prefFn(0);
        }
    }

    @Override
    protected void objectiveFnRefresh(ArrayList<ArrayList> constraints, ArrayList<Double> fitness_, 
    ArrayList<Double> vals, final Idx2D[] valVsConstIdx_, final ByRef tabuViolations, boolean forcePartialSolFit) {
        
        debug(constraints, fitness_, valVsConstIdx_, tabuViolations, forcePartialSolFit);
        
        vals.clear();
        for (ArrayList grp : constraints) {
            for (Object obj : grp) {
                vals.add(1.0*((Integer)obj));
            }
        }
        
        Idx2D idx2D;
        int col;
        int pos;

        for (int j = 0; j < valVsConstIdx_.length; j++) {
            valVsConstIdx_[j] = new Idx2D();
        }

        col = -1;
        for (ArrayList<Integer> list : constraints) {
            col++;
            pos = -1;
            for (Integer val : list) {
                pos++;
                valVsConstIdx_[val].col = col;
                valVsConstIdx_[val].position = pos;
            }
        }
        
//        tabuViolations.setValue(tabuConsViolation(vals, constraints,valVsConstIdx_));
//        
//        if(isPartialCSPSol(vals.size())){ //CspProcess.bOptimizationMode){
//            fitness_.set(0, fitness_.get(0)); //???
//        }else{
//            if(fitness_.isEmpty()){
//                fitness_.add(userInput_.totalConstraints*1.0);
//            }
//            fitness_.set(0, userInput_.totalConstraints - vals.size()*1.0); //overwrite
//        }           
    }

    
    
    
    /**
     * Appends <i>only</i> feasible vals. Infeasible one is discarded.
     * @param vals - NOTE vals must be unique.
     * @param fitness - fitness[0] = fitness val, fitness[1] = Sum of preferences
     * fitness[2] = worst preference encountered so far.
     * @param chromeConstraints 
     */
    @Override
    protected boolean objectiveFnAppend(final ArrayList<Double> vals, final ArrayList<Double> fitness, 
    final ArrayList<ArrayList> chromeConstraints, final ArrayList<Integer> noGood,
    final Idx2D[] valVsConstIdx_, final ByRef tabuViolations) throws SolutionFoundException{
        //All values will be accepted..
        ArrayList<Integer> tempAL;
        ArrayList<Integer> Prefs = new ArrayList<Integer>();
        ArrayList<Integer> conflictStudents = new ArrayList<Integer>();
//        ArrayList<Integer> possibleLoc = new ArrayList<Integer>();
        final int course = (int)Math.round(vals.get(vals.size()-1));
        
        int MAX_PREF_WT;
        int MIN_PREF_WT;       
        int totalConflictStudents;
        int worstPref = 0;
        boolean bPlaceFound = false;
        int tempDist;  
        int hardConstraintViolated = 0;
        Idx2D idx2D;
        int minIdx = -1;


        MIN_PREF_WT = 0;
        MAX_PREF_WT = 5; //prefFn(0);


        if(vals.size() == 0){
            return false;
        }
        
        
       //-wt think eela nikalek pari... 
        //expensive bhi hai aur ... setfn to deal kare hai... lekin appendfn nahi.... 
        
        if(vals.subList(0, vals.size()-1).contains(course*1.0)){
            vals.remove(vals.size() - 1);//last element
            //fitness and satisfactions not changed.
            return false;
        }
        
        if(vals.size() == 1){
            chromeConstraints.clear();
            for (int i = 0; i < maxSlotGroups; i++) {
                chromeConstraints.add(new ArrayList<Integer>());  
//                affinity_.add(new ArrayList<Double>());
            }
//            int ridx = 0;
            int ridx = MyRandom.randperm(0, maxSlotGroups).get(0);
            
            chromeConstraints.get(ridx).add(vals.get(0).intValue());
            
            idx2D = new Idx2D();
            idx2D.col = ridx;
            idx2D.position = 0;            
            valVsConstIdx_[vals.get(0).intValue()] = idx2D;
            
            bPlaceFound = true;
        }

        if(!bPlaceFound){
            for (int i = 0; i < chromeConstraints.size(); i++) { //maxslots
                tempAL = chromeConstraints.get(i);            
                totalConflictStudents = 0;
                for (int j = 0; j < tempAL.size(); j++) {
                    totalConflictStudents += conflictMatrix_[course][tempAL.get(j)];                
                }              
                conflictStudents.add(totalConflictStudents);            
            }           
            
            for (int i = 0; i < chromeConstraints.size(); i++) {
                Prefs.add(Integer.MAX_VALUE);
            }
            
            for (int i : MyRandom.randperm(0,chromeConstraints.size())) {
            //check surrounding
            //for (int wt = 0; wt < chromeConstraints.size(); wt++) {
                worstPref = 0; //best one
                hardConstraintViolated = 0;
                for (int j = i - MAX_DIST; j <= i+MAX_DIST; j++) {
                    if(j<0 || j > chromeConstraints.size()-1){
                        continue;
                    }
                    if(conflictStudents.get(j)>0){ //if there is conflict
                        tempDist = Math.abs(i-j);
                        if(tempDist == 0){ //&& conflictStudents.get(j) > 0 ... see above
                            worstPref = Integer.MAX_VALUE;
                            hardConstraintViolated++;
                            break;
                        }
                        
                        if(userInput_.bWeighted){ // fitness based on penalty weight function
                            worstPref+=penaltyFn(tempDist)*conflictStudents.get(j);
//                            if(penaltyFn(tempDist)*conflictStudents.get(j)>worstPref)
//                                worstPref = penaltyFn(tempDist)*conflictStudents.get(j);
                        }else{ // fitness based on preference                    
                            if(prefFn(tempDist)>worstPref)
                                worstPref = prefFn(tempDist);
                        }   
                    }
                }
                
                //itry placing Integer.MAX_VALUE in place of getCurPref() below....
                
//                if(worstPref <= getCurPref()){
                if(hardConstraintViolated<=0){ //only feasible
                    chromeConstraints.get(i).add(course);
//                    affinity_.get(v).add(worstPref*1.0);
                    idx2D = new Idx2D();
                    idx2D.col = i;
                    idx2D.position = chromeConstraints.get(i).size()-1;
                    valVsConstIdx_[course] = idx2D;
                    bPlaceFound = true;
                    break;
                }else{
                    if(worstPref<this.nextPrefLimit){
                        this.nextPrefLimit = worstPref;
                    }
                }
            }
        }
        
//        int sz = 0;        
//        for (ArrayList consGroup : chromeConstraints) {
//            sz+= consGroup.size();
//        }
                
        
        if(bPlaceFound){            
//            curOjbectiveFnUpdate(MAX_PREF_WT,worstPref, coeffConflictVsCourse[course], fitness);   
////////            //debug(chromeConstraints, fitness);
            noGood.remove(course*1.0);
        }else{ 
            if(hardConstraintViolated == chromeConstraints.size()){//NOGOOD solution eee 
                noGood.add(course);//last element
                
                if(noGood.subList(0, noGood.size()-1).contains(course*1.0)){
                    noGood.remove(noGood.size() - 1);//last element
                    //fitness and satisfactions not changed.
                }
            }
            vals.remove(vals.size() - 1);//last element
        }
        
        
//        tabuViolations.setValue(tabuConsViolation(vals));
        
//        if(isPartialCSPSol(vals.size())){//CspProcess.bOptimizationMode){
            tabuViolations.setValue(tabuConsViolation(vals, chromeConstraints,valVsConstIdx_));
            debug(chromeConstraints, fitness, valVsConstIdx_, tabuViolations, false);
            fitness.set(0, fitness.get(0)); 
//        }else{           
//            if(fitness.isEmpty()){
//                fitness.add(userInput_.totalConstraints*1.0);
//                fitness.add(0.0);//total hard constraint is assumed to be 0 - who wants to refresh it :(
//            }
//            tabuViolations.setValue(0);
//            fitness.set(0, userInput_.totalConstraints - vals.size()*1.0);   
//        } 
        
        return bPlaceFound;

    }

    
    @Override
    protected void objectiveFnAppendLocalSearch(final ArrayList<Double> vals, final ArrayList<Double> fitness, 
    final ArrayList<ArrayList> chromeConstraints, final ArrayList<Double> noGood,
    final Idx2D[] valVsConstIdx_, ByRef tabuViolations) throws SolutionFoundException{
        //All values will be accepted..
        ArrayList<Integer> tempAL;
        ArrayList<Integer> Prefs = new ArrayList<Integer>();
        ArrayList<Integer> conflictStudents = new ArrayList<Integer>();
//        ArrayList<Integer> possibleLoc = new ArrayList<Integer>();
        final int course = vals.get(vals.size()-1).intValue();
        int MAX_PREF_WT;
        int MIN_PREF_WT;       
        int totalConflictStudents;
        int worstPref = 0;
        boolean bPlaceFound = false;
        int tempDist;  
        int hardConstraintViolated = 0;
        Idx2D idx2D;
        int minIdx = -1;


        MIN_PREF_WT = 0;
        MAX_PREF_WT = 5; //prefFn(0);


        if(vals.size() == 0)
            return;
        
        
       //-wt think eela nikalek pari... 
        //expensive bhi hai aur ... setfn to deal kare hai... lekin appendfn nahi.... 
        
        if(vals.subList(0, vals.size()-1).contains(course*1.0)){
            vals.remove(vals.size() - 1);//last element
            //fitness and satisfactions not changed.
            return;
        }
        
        if(vals.size() == 1){
            chromeConstraints.clear();
            for (int i = 0; i < maxSlotGroups; i++) {
                chromeConstraints.add(new ArrayList<Integer>());  
//                affinity_.add(new ArrayList<Double>());
            }
//            int ridx = 0;
            int ridx = MyRandom.randperm(0, maxSlotGroups).get(0);
            
            chromeConstraints.get(ridx).add(vals.get(0).intValue());
            
            idx2D = new Idx2D();
            idx2D.col = ridx;
            idx2D.position = 0;            
            valVsConstIdx_[vals.get(0).intValue()] = idx2D;
            
            bPlaceFound = true;
        }

        if(!bPlaceFound){
            for (int i = 0; i < chromeConstraints.size(); i++) { //maxslots
                tempAL = chromeConstraints.get(i);            
                totalConflictStudents = 0;
                for (int j = 0; j < tempAL.size(); j++) {
                    totalConflictStudents += conflictMatrix_[course][tempAL.get(j)];                
                }              
                conflictStudents.add(totalConflictStudents);            
            }           
            
            for (int i = 0; i < chromeConstraints.size(); i++) {
                Prefs.add(Integer.MAX_VALUE);
            }
            
            
            ArrayList<Element> bestLocs = new ArrayList<Element>();
            Element tmpEle;
            
            for (int i : MyRandom.randperm(0,chromeConstraints.size())) {
            //check surrounding
            //for (int wt = 0; wt < chromeConstraints.size(); wt++) {
                worstPref = 0; //best one
                hardConstraintViolated = 0;
                for (int j = i - MAX_DIST; j <= i+MAX_DIST; j++) {
                    if(j<0 || j > chromeConstraints.size()-1){
                        continue;
                    }
                    if(conflictStudents.get(j)>0){ //if there is conflict
                        tempDist = Math.abs(i-j);
                        if(tempDist == 0){ //&& conflictStudents.get(j) > 0 ... see above
                            worstPref = Integer.MAX_VALUE;
                            hardConstraintViolated++;
                            break;
                        }
                        
                        if(userInput_.bWeighted){ // fitness based on penalty weight function
                            worstPref+=penaltyFn(tempDist)*conflictStudents.get(j);
//                            if(penaltyFn(tempDist)*conflictStudents.get(j)>worstPref)
//                                worstPref = penaltyFn(tempDist)*conflictStudents.get(j);
                        }else{ // fitness based on preference                    
                            if(prefFn(tempDist)>worstPref)
                                worstPref = prefFn(tempDist);
                        }   
                    }
                }
                
                //itry placing Integer.MAX_VALUE in place of getCurPref() below....
                
//                if(worstPref <= getCurPref()){
                if(hardConstraintViolated<=0){
                    //<< Local search
                    tmpEle = new Element(worstPref,i); //val, idx
                    bestLocs.add(tmpEle);
                    //>>
                    //<<
//                    chromeConstraints.get(v).add(course);
//                    //affinity_.get(v).add(worstPref*1.0);
//                    idx2D = new Idx2D();
//                    idx2D.col = v;
//                    idx2D.position = chromeConstraints.get(v).size()-1;
//                    valVsConstIdx_[course] = idx2D;
//                    bPlaceFound = true;
//                    break;
                    //>>
                }else{
                    if(worstPref<this.nextPrefLimit){
                        this.nextPrefLimit = worstPref;
                    }
                }
            }
            if(!bestLocs.isEmpty()){  
                //Element.sortOrder = Element.ASCENDING;
                Collections.sort(bestLocs);
                tmpEle = bestLocs.get(0);
                chromeConstraints.get(tmpEle.idx).add(course);
                //affinity_.get(v).add(worstPref*1.0);
                idx2D = new Idx2D();
                idx2D.col = tmpEle.idx;
                idx2D.position = chromeConstraints.get(tmpEle.idx).size()-1;
                valVsConstIdx_[course] = idx2D;
                bPlaceFound = true;
            }
            
        }
                     
        
        if(bPlaceFound){            
//            curOjbectiveFnUpdate(MAX_PREF_WT,worstPref, coeffConflictVsCourse[course], fitness);   
////////            //debug(chromeConstraints, fitness);
            noGood.remove(course*1.0);
        }else{ 
            if(hardConstraintViolated == chromeConstraints.size()){//NOGOOD solution eee 
                noGood.add(course*1.0);//last element
                
                if(noGood.subList(0, noGood.size()-1).contains(course*1.0)){
                    noGood.remove(noGood.size() - 1);//last element
                    //fitness and satisfactions not changed.
                }
            }
            vals.remove(vals.size() - 1);//last element
        }
        
        
        tabuViolations.setValue(tabuConsViolation(vals, chromeConstraints,valVsConstIdx_));
        
        if(isPartialCSPSol(vals.size())){
            debug(chromeConstraints, fitness, valVsConstIdx_, tabuViolations, false);
            fitness.set(0, fitness.get(0)); 
        }else{           
            if(fitness.isEmpty()){
                fitness.add(userInput_.totalConstraints*1.0);
            }
            fitness.set(0, userInput_.totalConstraints - vals.size()*1.0);     
        }        
    }
    
    /**
     * 
     * @param chromeConstraints
     * @return return 1 means no violation, more than 1 refers to violations
     */
//   private int tabuConsViolation(final ArrayList<ArrayList> chromeConstraints){
//               //<<cater dynamic constraints........................
//            if(chromeConstraints.isEmpty() || CspProcess.dynamicConstraints.isEmpty()){
//                return 1;
//            }
//
//            double hammingDist = -1;        
//            ArrayList intChromCons = new ArrayList();
//
//            for (int v = 0; v < chromeConstraints.size(); v++) {
//                for (int j = 0; j < chromeConstraints.get(v).size(); j++) {                
//                    intChromCons.add(Double.parseDouble(chromeConstraints.get(v).get(j).toString()));                
//                }            
//            }
//
//            final int maxHamDist = (int)(CspProcess.getCurAcceptedConstraints(userInput_.totalConstraints)/2);
//            ArrayList tabuConsList;
//            int tabuVios = 1;
//
//            for (int v = 0; v < Math.min(maxDynamicConstraints,CspProcess.dynamicConstraints.size()); v++) {  
//                tabuConsList = new ArrayList();
//                for (int j = 0; j < CspProcess.dynamicConstraints.get(v).size(); j++) {
//                    for (int k = 0; k < CspProcess.dynamicConstraints.get(v).get(j).size(); k++) {
//                        tabuConsList.add(Double.parseDouble(CspProcess.dynamicConstraints.get(v).get(j).get(k).toString()));                    
//                    }                
//                }
//
//                hammingDist = MyMath.norm(intChromCons,tabuConsList,MyMath.DIST_HAMMING, maxHamDist);            
//
//                if(hammingDist<=maxHamDist){ //0.2*userInput_.totalConstraints){//why 20%????
//                    tabuVios++; //violated                
//                }
//            }
//        //>>.......................................
//        return tabuVios;
//   } 
//    
    /**
     * <B>NOTE:</B> Technically this is not fully correct as the commented one
     * given in {@link TimeTableData class}. It must use constraint satisfaction
     * arraylist instead of vals arraylist.
     * @param vals values of a given chromosome
     * @return return 1 means no violation, more than 1 refers to violations
    */
    private int tabuConsViolation(final ArrayList<Double> vals, final ArrayList<ArrayList> sat, final Idx2D[] valVsConstIdx_){
//        //<<cater dynamic constraints........................
//        if(vals.isEmpty() || CspProcess.dynamicConstraints.isEmpty()){
//            return 0;
//        }
//
//        double hammingDist = -1;                    
//
//        final int maxHamDist = (int)(CspProcess.getCurAcceptedConstraints(userInput_.totalConstraints)*localBestComparision);
//        int tabuVios = 0;
//        ArrayList<Number> v1 = new ArrayList<Number>();
//        ArrayList<Number> v2 = new ArrayList<Number>();
//
//        for (int i = 0; i < sat.size(); i++) {
//            Collections.sort(sat.get(i));
//            v1.addAll(sat.get(i));
//        }
//
//        for (int i = 0; i < Math.min(maxDynamicConstraints,CspProcess.dynamicConstraints.size()); i++) {  
//            v2 = new ArrayList<Number>();
//            for (int j = 0; j < CspProcess.dynamicConstraints.get(i).size(); j++) {
//                v2.addAll(CspProcess.dynamicConstraints.get(i).get(j));
//            }
//
//            hammingDist = MyMath.norm(v1, v2 ,MyMath.DIST_HAMMING, maxHamDist+1,true);            
//
//            if(hammingDist<=maxHamDist){ //0.2*userInput_.totalConstraints){//why 20%????
//                tabuVios++; //violated                
//            }
//        }
//        refreshValVsConstIdx(valVsConstIdx_, sat);
//        return tabuVios; 
        //>>.......................................
        
      
        //<< hamming dist with prevBestsofar
        if(vals.isEmpty() || CspProcess.dynamicConstraints.isEmpty() || !CspProcess.resolvingLocalOptimalMode()){
           return 0; 
        }
            
        int tabuVios = 0;  
//        ByRef hamDist = new ByRef(0);
//        ByRef BRmaxHamDist = new ByRef(getMaxHamDist());
        int dist;
        
        for (int i = 0; i < Math.min(maxDynamicConstraints,CspProcess.dynamicConstraints.size()); i++) {  
//            if(hasSameHammingDist(CspProcess.dynamicConstraints.get(i), sat, valVsConstIdx_)){
//                tabuVios++;
//            } 
            dist = MyMath.norm2D(MyMath.DIST_HAMMING_PATTERN, CspProcess.dynamicConstraints.get(i), sat, null, getMaxHamDist());
//            hasSameHammingDist(CspProcess.dynamicConstraints.get(i), sat, valVsConstIdx_, hamDist, BRmaxHamDist);
//            if((Integer)hamDist.getVal()<=super.getMaxHamDist()){
            if(dist<super.getMaxHamDist()){ //MUST < if use <= then > ones are also included since > ones are reduced to =. that's how hammingDist fun is used.
                tabuVios++;
            }
            
        }
        
        return tabuVios;
        //>>
   } 
 
    /**
     * <B>NOTE:</B> input parameters {@code tabuSat} and {@code sat} must be sorted 
     * columnwise before calling this method.
     * @param tabuSat
     * @param sat
     * @param valVsConstIdx_
     * @param hamDist
     * @return 
     */
    @Override
    public boolean hasSameHammingDist(final ArrayList<ArrayList> tabuSat, final ArrayList<ArrayList> sat, 
    final Idx2D[] valVsConstIdx_, ByRef ... hamDist){
        
        boolean ret;
        
        if(tabuSat == null){
            ret = false;
            return ret;
        }
        
        if(tabuSat.isEmpty()){
            ret = false;
            return ret;
        }
      
        int hammingDist = -2;
        int maxHamDist = -1;
        if(hamDist.length >= 1){
            hammingDist = 0;
            if(hamDist.length == 2){
                maxHamDist = (Integer)hamDist[1].getVal();
            }
        }
//        for (int i = 0; i < tabuSat.size(); i++) {
//            Collections.sort(sat.get(i)); //must call refresh() later
//            for (int j = 0; j < Math.min(tabuSat.get(i).size(), sat.get(i).size()); j++) {
////                if((Integer)tabuSat.get(i).get(j) == -1){
////                    continue;
////                }              
//                if((Integer)sat.get(i).get(j) != (Integer)tabuSat.get(i).get(j)){
//                    refreshValVsConstIdx(valVsConstIdx_, sat);
//                    if(hammingDist>=0){
//                        hammingDist++;
//                    }else{
//                        return false;
//                    }
//                    if(hammingDist>=maxHamDist && maxHamDist > 0){ //very far...  
//                        i = tabuSat.size();//to break outer loop.
//                        return false;
//                    }
//                }
//            }
//        }
        
        int common;
        int tabuPtr;
        int satPtr;
        ret = true; // now check for difference
        for (int i = 0; i < tabuSat.size(); i++) { 
            tabuPtr = 0;
            satPtr = 0;
            common = 0;
            while(tabuPtr<tabuSat.get(i).size() && satPtr<sat.get(i).size()){
                while(tabuPtr<tabuSat.get(i).size() && (Integer)tabuSat.get(i).get(tabuPtr) < (Integer)sat.get(i).get(satPtr)){
                    tabuPtr++;
                }

                if(tabuPtr>=tabuSat.get(i).size()){
                    break;
                }
                if(((Integer)tabuSat.get(i).get(tabuPtr)).intValue() == ((Integer)sat.get(i).get(satPtr)).intValue()){
                    common++;
                    tabuPtr++;
                    satPtr++;
                }
                if(tabuPtr>=tabuSat.get(i).size() || satPtr >= sat.get(i).size()){
                    break;
                }
                while(satPtr<sat.get(i).size() && (Integer)sat.get(i).get(satPtr)<(Integer)tabuSat.get(i).get(tabuPtr)){
                    satPtr++;
                }
                
                if(satPtr>= sat.get(i).size()){
                    break;
                }

                if(((Integer)tabuSat.get(i).get(tabuPtr)).intValue() == ((Integer)sat.get(i).get(satPtr)).intValue()){
                    common++;
                    tabuPtr++;
                    satPtr++;
                }
            }
            if(hammingDist>=0){//using hammin dist
                hammingDist += tabuSat.get(i).size()+sat.get(i).size() - 2*common;
            }else{
                int tmpHammingDist = tabuSat.get(i).size()+sat.get(i).size() - 2*common;
                if(tmpHammingDist != 0){ //not same
                    ret = false;
//                    i = tabuSat.size();//break outer loop
                    break;
                }
            }
            if(hammingDist>=maxHamDist && maxHamDist > 0){ //very far...   
                ret = false;
                break;
            }
        }
               
        refreshValVsConstIdx(valVsConstIdx_, sat);
        hamDist[0].setValue(hammingDist);
        return ret;
    }
    
    
    
    @Override
    protected boolean isHighlyConstrained(Object obj) {
        int course = (Integer)obj;
        
        if(coeffConflictVsCourse[course]>=userInput_.totalConstraints-maxSlotGroups/4){//top 5
            return true;
        }else
            return false;
    }

    
    
    @Override
    protected boolean isViolated(final Object obj1, final Object obj2, final Object... additionalInfo) {
        return hardConstraintViolated((Integer)obj1, (Integer)obj2);
    }
    
    
    
    private boolean hardConstraintViolated(final int course1, final int course2){
        if(conflictMatrix_[course1][course2]==0){
            return false;
        }else{
            return true;
        }
    }

//    @Override
//    public ArrayList<ArrayList<Double>> refreshIndividualAffinity(ArrayList<ArrayList> sat) {
//        int course_col, course_pos, totalConflictStudents;
//        double pref;
//        ArrayList<Integer> conflictStudents = new ArrayList<Integer>();
//        ArrayList<Integer> tempAL;
//        ArrayList<ArrayList<Double>> affinity = new ArrayList<ArrayList<Double>>();
//        for (int i = 0; i < maxSlotGroups; i++) {                
//            affinity.add(new ArrayList<Double>());
//        }
//        
//        course_pos = -1;
//        pref = 0; 
//        course_col = -1;
//        for (ArrayList<Integer> courseList : sat) {
//            course_col++; //first one is 0.
//            course_pos = -1;
//            for (Integer course : courseList) {
//                course_pos++;
//                conflictStudents = new ArrayList<Integer>();
//                for (int i = 0; i < sat.size(); i++) { //maxslots can save time here by reducing range to MAX_DIST only ....
//                    tempAL = sat.get(i);                //but it used for easy calculation below with regards to columnIds
//                                        
//                    totalConflictStudents = 0;
//                    for (int j=0; j < tempAL.size(); j++) {
//                        if(course!=tempAL.get(j)){
//                            totalConflictStudents += conflictMatrix_[course][tempAL.get(j)];  
//                        }
//                    }                  
//                    conflictStudents.add(totalConflictStudents); 
//                }
//
//                
//                pref = 0;              
//                for (int j = course_col-MAX_DIST; j <= course_col+MAX_DIST; j++) {
//                    if(j<0 || j > sat.size()-1){
//                        continue;
//                    }
//                    
//                    if(conflictStudents.get(j)>0){ //if there is conflict, for double check becuase we are starting with same column course_col
//                        if(userInput_.bWeighted){   
//                            pref+=1.0*penaltyFn(Math.abs(j-course_col))*conflictStudents.get(j);                        
//                        }else{
//                            if(prefFn(j-course_col)>(int)pref)
//                                pref = prefFn(Math.abs(j-course_col));
//                                //can use break here, because the cur pref will be maximum.
//                        }
//                    }
//                }
//                affinity.get(course_col).add(pref);
//            }
//        }
//        return affinity;
//    }
    
    
    private void debug(final ArrayList<ArrayList> sat, final ArrayList<Double> fitness, 
            final Idx2D[] valVsConstIdx, ByRef tabuVios, boolean forcePartialSolFit){

        fitness.clear();
//        affinity.clear();        
//        for (int v = 0; v < maxSlotGroups; v++) {                
//            affinity.add(new ArrayList<Double>());
//            for (int j = 0; j < sat.get(v).size(); j++) {                
//                affinity.get(v).add(-1.0);
//            }
//        }
        int sz = 0;
        for (ArrayList<Integer> grp: sat) {
            sz += grp.size();            
        }
        

        
        //actually conflictStudents[] size should only be MAX_PREF_WT. may be this guy used for easy calc.
        int [] conflictStudentsTotal = new int[sat.size()]; //auto initialized to 0
        int grpHardVios; //total courses causing hard violations in a column 
        
        ArrayList<Integer> tempAL;       
        ArrayList<Double> vals = new ArrayList<Double>();
        int totalConflictStudents;               
        double pref; 
        double prefHardVios; 

        int course_col;
        final int MAX_PREF_WT = 5; //prefFn(0);
               
        int course_pos = -1;
        pref = 0; 
        prefHardVios = 0;
        course_col = -1;
        int curTotalElements = 0;
        int dist;
        int totalHardConstVios = 0;
        for (ArrayList<Integer> courseList : sat) {
            course_col++; //first one is 0.
            course_pos = -1;
            grpHardVios = 0;
            
            for (Integer course : courseList) {
                vals.add(course*1.0);
                curTotalElements++;
                course_pos++;
                conflictStudentsTotal = new int[sat.size()];//default all 0
                
                prefHardVios = 0;
                for (int i = course_pos+1; i < courseList.size(); i++) {
                    if(conflictMatrix_[course][courseList.get(i)]>0){//at least one student
                        grpHardVios++;
                        if(userInput_.bWeighted){                             
                            prefHardVios+=1.0*penaltyFn(0)*conflictMatrix_[course][courseList.get(i)]; //dist = 0                             
                        }else{
                            if(prefFn(0)>(int)prefHardVios)
                                prefHardVios = prefFn(0);
                                //can use break here, because the cur pref will be maximum.
                        }
                    } 
                }
                
                for (int i = course_col; i <= course_col+MAX_DIST; i++) { //maxslots can save time here by reducing range to MAX_DIST only ....
                    if(i>sat.size()-1){
                        break;
                    }
                    tempAL = sat.get(i);                //but it used for easy calculation below with regards to columnIds                                        
                    totalConflictStudents = 0;
                    for (int j=0; j < tempAL.size(); j++) {
                        if(course!=(Integer)(tempAL.get(j)).intValue()){
                            totalConflictStudents += conflictMatrix_[course][tempAL.get(j)];  
                        }
                    }                  
                    conflictStudentsTotal[i] = totalConflictStudents; 
                }
                
                pref = 0;                                
                for (int i = course_col; i <= course_col+MAX_DIST; i++) {
                    if(i<0 || i > sat.size()-1){
                        continue;
                    }
                    dist = i-course_col;
                    if(conflictStudentsTotal[i]>0){ //if there is conflict, for double check becuase we are starting with same column course_col
                        if(dist == 0){ //hard constraint violation
                            continue; //hard vios are calculated separately above
////                            totalHardConstVios++;
                        }
                        if(userInput_.bWeighted){                             
                            pref+=1.0*penaltyFn(dist)*conflictStudentsTotal[i];                             
                        }else{
                            if(prefFn(dist)>(int)pref)
                                pref = prefFn(dist);
                                //can use break here, because the cur pref will be maximum.
                        }
                    }
                }
                
                
//                tabu????
                tabuVios.setValue(tabuConsViolation(vals, sat,valVsConstIdx));                     

//                if(isPartialCSPSol(sz) || forcePartialSolFit){//CspProcess.bOptimizationMode){
                    curOjbectiveFnUpdate(MAX_PREF_WT,(int)(pref+prefHardVios), coeffConflictVsCourse[course], fitness);            
//                }else{           
//                    if(fitness.isEmpty()){
//                        fitness.add(userInput_.totalConstraints*1.0);
//                    }
//                    fitness.set(0, userInput_.totalConstraints - curTotalElements*1.0);//can use sz as well     
//                }
            } 
            //hard vios accumulate per column
            totalHardConstVios += grpHardVios;
        }
        
        if(fitness.isEmpty()){
            fitness.add(Double.MAX_VALUE);
        }
        
        
        fitness.add(totalHardConstVios*1.0); //fitness[1]
    }

    public void objectiveFnSwapNrefresh(final ArrayList<ArrayList> sat, final ArrayList<Double> vals, ArrayList<Double> fitness_, 
        final Idx2D[] valVsConstIdx_, final ByRef tabuViolations, int[] p1, int[] p2){
        int val1 = (Integer)sat.get(p1[0]).get(p1[1]);
        int val2 = (Integer)sat.get(p2[0]).get(p2[1]);
        
        double curFit;
        double hardVios;
        
        //fitness before
        Double []localFit1;
        Double []localFit2;
        localFit1 = getValidIndividualPref(p1[1], p1[0], sat); //both avail  
        sat.get(p1[0]).remove(p1[1]);
        localFit2 = getValidIndividualPref(p2[1], p2[0], sat); //first one removed
        curFit = fitness_.get(0) - localFit1[0] - localFit2[0];
        hardVios = fitness_.get(1)- localFit1[1] - localFit2[1];

        //swapped to each others same position.
        sat.get(p1[0]).add(p1[1], val2);
        sat.get(p2[0]).set(p2[1], val1);
        
        
        //<<refresh valsvsids
        valVsConstIdx_[val1] = new Idx2D(p2[0], p2[1]);
        valVsConstIdx_[val2] = new Idx2D(p1[0], p1[1]);//top one....
        //>>>>>>>>
        
        
        
        
        //fitness after
        localFit1 = getValidIndividualPref(p1[1], p1[0], sat);   
        sat.get(p1[0]).remove(p1[1]);
        localFit2 = getValidIndividualPref(p2[1], p2[0], sat); 
        sat.get(p1[0]).add(p1[1],val2);//now p1 holds val2
        curFit = curFit + localFit1[0] + localFit2[0];
        hardVios = hardVios + localFit1[1] + localFit2[1];
        
//        System.out.println("later fit: " + curFit + ", " + localFit1[0] +", " + localFit2[0]);
        
        fitness_.clear();
        fitness_.add(curFit);
        fitness_.add(hardVios);
        
//        refreshValVsConstIdx(valVsConstIdx_, sat); //its been done on top. 
        
        tabuViolations.setValue(tabuConsViolation(vals, sat, valVsConstIdx_));
        
//        if(!isPartialCSPSol(vals.size())){           
//            if(fitness_.isEmpty()){
//                fitness_.add(userInput_.totalConstraints*1.0);
//                fitness_.add(0.0);//total hard constraint is assumed to be 0 - who wants to refresh it :(
//            }
//            tabuViolations.setValue(0);
//            fitness_.set(0, userInput_.totalConstraints - vals.size()*1.0);   
//        } 
    }
    
    /**
     * 
     * @param pos
     * @param col
     * @param sat
     * @return current fitness value in the form of array of size 2. The first
     * index is the fitness value and the second index is hard constraint violations.
     */
    private Double[] getValidIndividualPref(final int pos, final int col, final ArrayList<ArrayList> sat){
        int[] conflictStudents = new int[sat.size()];
        final int course = (Integer)sat.get(col).get(pos);
        ArrayList<Integer> tempAL;
        int hardConstraintViolated = 0;
        int totalConflictStudents = 0;
        int viosList[] = new int[MAX_DIST+1]; //starting from 0-MAX_DIST = MAX_DIST+1
        for (int i = 0; i < viosList.length; i++) {
            viosList[i] = 0;
        }
         
        for (int i = col-MAX_DIST; i <= col+MAX_DIST; i++) { //maxslots can save time here by reducing range to MAX_DIST only ....            
            if(i<0){
                continue;
            }
            if(i > sat.size()-1){
                break;
            }
            tempAL = sat.get(i);                //but it used for easy calculation below with regards to columnIds                                        
            totalConflictStudents = 0;
            for (int j=0; j < tempAL.size(); j++) {
                if(course!=(Integer)(tempAL.get(j)).intValue()){
                    totalConflictStudents += conflictMatrix_[course][tempAL.get(j)];
                    if(conflictMatrix_[course][tempAL.get(j)]>0 && !userInput_.bWeighted){
                        viosList[prefFn(Math.abs(col-i))]++;
                    }
                }
            }                  
            conflictStudents[i] = totalConflictStudents; //total conflict studetns in EACH GROUP
        }
        
        tempAL = sat.get(col);
        for (int i = 0; i < tempAL.size(); i++) {
            if(course != tempAL.get(i).intValue() && conflictMatrix_[course][tempAL.get(i)]>0){
                hardConstraintViolated++;
            }
        }
        
        int worstPref = 0; //best one

       
        int curPref;
        
        int tempDist;
        for (int i = col - MAX_DIST; i <= col+MAX_DIST; i++) {
            if(i < 0){
                continue;
            }
            if(i > sat.size()-1){
               break; 
            }
            if(conflictStudents[i]>0){ //if there is conflict
                tempDist = Math.abs(col-i);

                if(userInput_.bWeighted){ // fitness based on penalty weight function
                    worstPref+=penaltyFn(tempDist)*conflictStudents[i];
                }else{ // fitness based on preference                    
//                    curPref = prefFn(tempDist);
//                    if(curPref>=0){
//                        viosList[curPref]++; 
//                        if(curPref>worstPref)
//                            worstPref = curPref;
//                    }   
                }
            }
        }
        
        
        
        
        Double[] fit; 
  
        
        //<<
        if(userInput_.bWeighted){
            fit = new Double[2];
            fit[0] = worstPref*1.0;
            fit[1] = hardConstraintViolated*1.0;
        }else{
            fit = new Double[viosList.length+1];
            fit[0] = super.getIndividualPrefFit(MAX_DIST, viosList);
            for (int i = 0; i < viosList.length; i++) {
                fit[i+1] = viosList[i]*1.0;
            }
        }
        //>>      
        
        
        return fit;
    }
    
    
    /**
     * Gives only valid functional value. It does not take account for tabu related
     * extra weights. 
     * @param fitnessVal
     * @return 
     */
    @Override
    public double getFunctionalVal(double fitnessVal) {
        return fitnessVal/STUDENT_SIZE;
    }
    
    
    
    /**
     * NOTE: It only works if the partial solution/preferences is NOT taken into account.
     * this is bad..... don't use it.
     * @param vals
     * @param fitness
     * @param chromeConstraints
     * @param noGood
     * @param valVsConstIdx 
     */
    @Override
    protected void objectiveFnReset(final ArrayList<Double> vals, final ArrayList<Double> fitness, 
    final ArrayList<ArrayList> chromeConstraints, final ArrayList<Integer> noGood, 
    final Idx2D[] valVsConstIdx, ByRef tabuViolations) throws SolutionFoundException{        
        //the following works if the partial solution/preferences is not taken into account.
        
        objectiveFnRefresh(chromeConstraints, fitness, vals, valVsConstIdx, tabuViolations, false);
    }

    @Override
    protected void objectiveFnRemove(final ArrayList<Double> vals, final ArrayList<Double> fitness_, 
    final boolean forcePartialFitSol, final ArrayList<ArrayList> constraints, final Idx2D[] valVsConstIdx, final int idx, 
    ByRef tabuViolations) 
    throws Exception{
        
        int course_col;
        int course_pos;
        Idx2D idx2D;
        int val = vals.get(idx).intValue();
        double hardVios;
        double curFit;
        Double []localFit;
        
        try {     
            course_col = valVsConstIdx[val].col;
            course_pos = valVsConstIdx[val].position;

        //<<Adjust instead of refresh. <<<<<<<<< for fast calculation
            hardVios = 0;
            localFit = getValidIndividualPref(course_pos, course_col, constraints); //both avail  
            hardVios = fitness_.get(1) - localFit[1];
            curFit = fitness_.get(0) - localFit[0];
            
            vals.remove(idx);
            valVsConstIdx[val] = new Idx2D(); //col = -1; position = -1;
            constraints.get(course_col).remove(course_pos);
            
            for (int i = course_pos; i < constraints.get(course_col).size(); i++) {
                valVsConstIdx[(Integer)constraints.get(course_col).get(i)].position--;                
            }     
     
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Removal Failure!");
        }
        
        tabuViolations.setValue(tabuConsViolation(vals, constraints,valVsConstIdx));
            
//        if(isPartialCSPSol(vals.size()) || forcePartialFitSol){//CspProcess.bOptimizationMode){
//            if(fitness_.isEmpty()){
//                fitness_.add(Double.POSITIVE_INFINITY);
//            }
            fitness_.set(0, curFit);
//        }else{
//            if(fitness_.isEmpty()){
//                fitness_.add(userInput_.totalConstraints*1.0);
//            }
//            fitness_.set(0, userInput_.totalConstraints - vals.size()*1.0); 
//        }  
        fitness_.set(1, hardVios);
    }
    
//    @Override
//    protected void objectiveFnRemove(final ArrayList<Double> vals, final ArrayList<Double> fitness_, 
//    final ArrayList<ArrayList> constraints, final Idx2D[] valVsConstIdx, final Object value, 
//    ByRef tabuViolations) 
//    throws Exception{
//        
//        int course_col;
//        int course_pos;
//        Idx2D idx2D;
//        int val;
//        if(value instanceof Double){
//            val = ((Double)value).intValue();
//        }else if(value instanceof Integer){
//            val = (Integer)value;
//        }else{
//            throw new UnsupportedOperationException("incorrect input parameter type");
//        }
//
//        
//        double hardVios;
//        double curFit;
//        Double []localFit;
//        
//        try {     
//            course_col = valVsConstIdx[val].col;
//            course_pos = valVsConstIdx[val].position;
//
//        //<<Adjust instead of refresh. <<<<<<<<< for fast calculation
//            hardVios = 0;
//            localFit = getValidIndividualPref(course_pos, course_col, constraints); //both avail  
//            hardVios = fitness_.get(1) - localFit[1];
//            curFit = fitness_.get(0) - localFit[0];
//            
//            vals.remove(new Double(val));
//            valVsConstIdx[val] = new Idx2D(); //col = -1; position = -1;
//            constraints.get(course_col).remove(course_pos);
//            
//            for (int i = course_pos; i < constraints.get(course_col).size(); i++) {
//                valVsConstIdx[(Integer)constraints.get(course_col).get(i)].position--;                
//            }     
//     
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new Exception("Removal Failure!");
//        }
//        
//        tabuViolations.setValue(tabuConsViolation(vals, constraints,valVsConstIdx));
//            
//        if(isPartialCSPSol(vals.size())){//CspProcess.bOptimizationMode){
////            if(fitness_.isEmpty()){
////                fitness_.add(Double.POSITIVE_INFINITY);
////            }
//            fitness_.set(0, curFit);
//        }else{
//            if(fitness_.isEmpty()){
//                fitness_.add(userInput_.totalConstraints*1.0);
//            }
//            fitness_.set(0, userInput_.totalConstraints - vals.size()*1.0); 
//        }  
//        fitness_.set(1, hardVios);
//    }
//    
    
//    @Override
//    protected void objectiveFnRemove(final ArrayList<Double> vals, final ArrayList<Double> fitness_, 
//        final ArrayList<ArrayList> constraints, final Idx2D[] valVsConstIdx, final int idx, 
//        ByRef tabuViolations) 
//    throws Exception{
//        
//        int course_col;
//        int course_pos;
//        Idx2D idx2D;
//        int val = vals.get(idx).intValue();
//        
//        try {
//           
//            course_col = valVsConstIdx[val].col;
//            course_pos = valVsConstIdx[val].position;
//            vals.remove(idx);
//            valVsConstIdx[val] = new Idx2D(); //col = -1; position = -1;
//            constraints.get(course_col).remove(course_pos);
//            
//            for (int i = course_pos; i < constraints.get(course_col).size(); i++) {
//                valVsConstIdx[(Integer)constraints.get(course_col).get(i)].position--;                
//            }                                  
//                
//
//        //<<Adjust instead of refresh. <<<<<<<<< for fast calculation
//            double hardVios;
//            Double []localFit;
//
//            hardVios = 0;
//            if(fitness_.get(1)>0){ //already has hard vios. Check if this one is causing.
//                localFit = getValidIndividualPref(course_pos, course_col, constraints); //both avail  
//                constraints.get(course_col).remove(course_pos);
//                hardVios = fitness_.get(1) - localFit[1];
//            }
//            fitness_.clear();
//            fitness_.add(userInput_.totalConstraints - vals.size()*1.0);
//            fitness_.add(hardVios);
//
//            tabuViolations.setValue(tabuConsViolation(vals, constraints, valVsConstIdx));    
//        //>>>>>>>>>>>>>>>
//        
//        
////            debug(constraints, fitness_, valVsConstIdx, tabuViolations);
//            
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new Exception("Removal Failure!");
//        }
//        
//        tabuViolations.setValue(tabuConsViolation(vals, constraints,valVsConstIdx));
//            
//        if(isPartialCSPSol(vals.size())){//CspProcess.bOptimizationMode){
////            if(fitness_.isEmpty()){
////                fitness_.add(Double.POSITIVE_INFINITY);
////            }
//            fitness_.set(0, fitness_.get(0));
//        }else{
//            if(fitness_.isEmpty()){
//                fitness_.add(userInput_.totalConstraints*1.0);
//            }
//            fitness_.set(0, userInput_.totalConstraints - vals.size()*1.0); 
//        }        
//    }

    private boolean isPartialCSPSol(int valSize){
        return (valSize >= CspProcess.getCurAcceptedConstraints(userInput_.totalConstraints)); // && CspProcess.bOptimizationMode;
    }
    
    private boolean isCSPSol(int valSize){
        return (valSize == userInput_.totalConstraints); // && CspProcess.bOptimizationMode;
    }
    
    @Override
    protected int getConstraintID(Double val) {
        return val.intValue();
    }



     /**
     * Clone defined for ExternalData is ONLY SHALLOW CLONE.
     * @return Object.clone();
     * @throws CloneNotSupportedException
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone(); //Not making copy of file data
    }  
    
    
       /**
     * Don't try to use fitness value here becuase it is corrupted...
     * @param chromeConstraints
     * @param bShowProgress
     * @return 
     */
//    @Override
    public boolean getForcedCSPsol(ArrayList<ArrayList> chromeConstraints, boolean bShowProgress) {
        throw new UnsupportedOperationException("Not supported yet.");
//            ArrayList<Double> vals = new ArrayList<Double>();
//            ArrayList<Double> fitness = new ArrayList<Double>(); //NOTE fitness will be very corrupted.... 
//            double tmpVal;
//            int tmpIdx;
//            
//            chromeConstraints.clear();
//            ArrayList<Double> noGood = new ArrayList<Double>();
//            Idx2D[] valVsConstIdx_ = new Idx2D[userInput_.totalConstraints];
//            
//            for (int wt = 0; wt < maxSlotGroups; wt++) {
//                chromeConstraints.add(new ArrayList<Integer>());            
//            }
//            
//            int bs, as;
//            Double badval;
//            Integer temp;
//            int valSize;
//            int ConstSize;
//            
//            ArrayList<Integer> idxList = MyRandom.randperm(0,conflictOrder.size()-1);
//            
//            for (int v = 0; v < conflictOrder.size(); v++) {
//            //for(int v = 0; v<idxList.size();v++){
//                tmpIdx = v;//idxList.get(v);
//                if(!fitness.isEmpty()){
//                    fitness.add(Integer.MAX_VALUE*1.0);
//                    fitness.set(0, Integer.MAX_VALUE*1.0);// fitness is already corrupted....
//                }
//                tmpVal = conflictOrder.get(tmpIdx).horizontalIdx*1.0;
//                vals.add(tmpVal);
//                if(vals.subList(0, vals.size()-1).contains(tmpVal)){
//                    vals.remove(vals.size() - 1);
//                    continue;
//                }
//                
//                bs = vals.size();
//                ObjectiveFnAppend(vals, fitness, chromeConstraints, noGood, valVsConstIdx_);
//                as = vals.size();
//                
//                if(as < bs){//not added
//                    badval = tmpVal;
//                    v = -1; //start again... but these will be ignored by append fn
//                    
//                    int j = MyRandom.randperm(0, chromeConstraints.size()-1).get(0);
//                    for (int k = 0; k < chromeConstraints.get(j).size(); k++) {
//                        //System.out.println(chromeConstraints.get(j).get(k));
//                        temp = (Integer)chromeConstraints.get(j).get(k);
//                        if(isViolated(temp, badval.intValue())){
//                            chromeConstraints.get(j).remove(k);
//                            vals.remove(temp*1.0);
//                            k--;
//                        }
//                    } 
//                    vals.add(badval);
//                    ObjectiveFnAppend(vals, fitness, chromeConstraints, noGood, valVsConstIdx_);
//                } 
//                
//                if(v%5 == 0 && bShowProgress){
//                    System.out.println(chromeConstraints+"\n\n");
//                }
//            }
//            valSize = vals.size();
//            ConstSize = 0;
//            for (ArrayList grp : chromeConstraints) {
//                for (Object obj : grp) {
//                    ConstSize++;
//                }
//            }
//            fitness.clear(); //note fitness val is corrupted...
//            if(ConstSize == valSize && valSize == userInput_.totalConstraints){
//                return true;
//            }else
//                return false;
    }

        /**
     * Don't try to use fitness value here becuase it is corrupted...
     * @param chromeConstraints
     * @param bShowProgress
     * @return 
     */
////    @Override
////    public void tryForcedCSPsolUpdate(final ArrayList<Double> vals, final ArrayList<Double> fitness, 
////    final ArrayList<ArrayList> chromeConstraints, final ArrayList<Double> noGood,
////    final Idx2D[] valVsConstIdx_,  boolean bShowProgress){
////        //ArrayList<Double> vals = new ArrayList<Double>();
////        //ArrayList<Double> fitness = new ArrayList<Double>(); //NOTE fitness will be very corrupted.... 
////        double tmpVal;
////        int tmpIdx;
////
////        ArrayList<Double> localNoGoods = new ArrayList<Double>();
////        double randNoGood;
////        int randListIdx;
////        ArrayList<Integer> randList;
////
////        
////        for (int v = 0; v < userInput_.totalConstraints; v++) {
////            localNoGoods.add(v*1.0);
////        }
////        
////        for (int v = 0; v < vals.size(); v++) {   
////            localNoGoods.remove(vals.get(v));        
////        }
////        
////        if(localNoGoods.isEmpty()){
////            return;
////        }
////
////        randNoGood = localNoGoods.get(MyRandom.randperm(0, localNoGoods.size()-1).get(0));
////
////        //It is assumed that all groups are conflicted with localNoGoods... duh... that's the definition of localNoGoods...
////        randListIdx = MyRandom.randperm(0, maxSlotGroups-1).get(0);
////        randList = chromeConstraints.get(randListIdx); //reference to list.
////
////
////        for (int j = 0; j < randList.size(); j++) {
////            if(isViolated(randList.get(j).intValue(), (int)randNoGood)){
////                valVsConstIdx_[randList.get(j)] = new Idx2D();
////                randList.remove(j);
////                j--;                
////            }
////        }
////        randList.add((int)randNoGood);
////        
////        chromeConstraints.set(randListIdx, randList);
////        
////        ObjectiveFnRefresh(chromeConstraints, fitness, vals, valVsConstIdx_);
////        
//////        for (int v = 0; v < randList.size(); v++) {
//////            valVsConstIdx_[randList.get(v)].position = v;            
//////        }
////        
////        //ObjectiveFnAppend(vals, fitness, chromeConstraints, noGood, valVsConstIdx_);
////           
////    }
    
}
