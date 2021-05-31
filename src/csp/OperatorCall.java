/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package csp;

import csp.CspProcess.Operators;

/**
 *
 * @author sharma_au
 */
public class OperatorCall{
    public Operators currentOperator;
    public double sumDiff;
    public int affectedChromes;
    public int improvedChromes;
    public double bestBeforeOpStarts;
    public double diversity;

    public OperatorCall(){
        currentOperator = Operators.badOperator;
        sumDiff = 0;
        affectedChromes = 0;
        improvedChromes = 0;
        bestBeforeOpStarts = CspProcess.getBestSoFarCOP().getFitnessVal(0);        
        diversity = 0;
    }
}
