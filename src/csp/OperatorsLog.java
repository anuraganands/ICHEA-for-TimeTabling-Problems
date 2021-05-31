/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package csp;

import csp.CspProcess.Operators;
import java.util.ArrayList;

/**
 *
 * @author sharma_au
 */
public class OperatorsLog {
    public int gen;
    public boolean bestIsSolutionB4start;
    public String dynamicConstraintsB4;
    
    private ArrayList<OperatorCall> data;
    
    public int dataSize(){
        return data.size();
    }
    
    public void addDataForOpCall(Operators currentOperator, double improvementAvg, int affectedChromes, int improvedChromes,
        double bestBeforeCurGen, double diversity){
                
        OperatorCall tmpOC = new OperatorCall();        
        tmpOC.currentOperator = currentOperator;
        tmpOC.sumDiff = improvementAvg;
        tmpOC.affectedChromes = affectedChromes;
        tmpOC.improvedChromes = improvedChromes;
        tmpOC.bestBeforeOpStarts = bestBeforeCurGen;
        tmpOC.diversity = diversity;
        data.add(tmpOC);
        
    }
    
    /**
     * Add only invalid/empty values.
     * It can be used when no operators are called in a given generation.
     */
    public void addDataForOpCall(){                
        OperatorCall tmpOC = new OperatorCall();        
        tmpOC.currentOperator = Operators.badOperator;
        tmpOC.sumDiff = Double.NaN;
        tmpOC.affectedChromes = -1;
        tmpOC.improvedChromes = -1;
        tmpOC.bestBeforeOpStarts = Double.NaN;
        tmpOC.diversity = Double.NaN;
        data.add(tmpOC);        
    }

    /**
     * if an operator is called more than once per gen then it needs to be catered for
     */
    public OperatorsLog(){
        gen = -1;
        bestIsSolutionB4start = CspProcess.getBestSoFarCOP().isSolution();
        dynamicConstraintsB4 = "0";
        data = new ArrayList<OperatorCall>();     
    } 

    public static String getColumnsOrder(){
        String retVal = "";
        retVal += "gen,bestIsSolutionB4start,dynamicConstraintsB4,";
        retVal += "currentOperator,sumDiff/AC,improvedChromes," +
                "affectedChromes,diversity,bestBeforeOpStarts";
        return retVal;
    }
    
    @Override
    public String toString() {
        String retVal = "";
        
        for (int i = 0; i < data.size(); i++) {  
            retVal +=  gen + "," + bestIsSolutionB4start + "," + dynamicConstraintsB4 + ",";
            retVal +=data.get(i).currentOperator + ","+(data.get(i).sumDiff/data.get(i).affectedChromes) + "," + data.get(i).improvedChromes + ","+
                data.get(i).affectedChromes + ","+data.get(i).diversity + "," + data.get(i).bestBeforeOpStarts;
            if(i<data.size()-1){
                retVal += "\n";
            }
        }
        return retVal;        
    }
}
