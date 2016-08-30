/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.math;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.vcell.util.Compare;
import org.vcell.util.Matchable;
import org.vcell.util.Issue.IssueSource;

import cbit.vcell.math.Variable.Domain;
import cbit.vcell.parser.Discontinuity;
import cbit.vcell.parser.DivideByZeroException;
import cbit.vcell.parser.Expression;
import cbit.vcell.parser.ExpressionBindingException;
import cbit.vcell.parser.ExpressionException;
import cbit.vcell.parser.SymbolTable;
/**
 * This class was generated by a SmartGuide.
 * 
 */
public abstract class Equation implements Serializable, MathObject, Matchable, IssueSource {
	private   Expression rateExp = null;
	protected Expression initialExp = null;
	protected Expression exactExp = null;
	private Variable var = null;
	
	public final static int UNKNOWN_SOLUTION = 0;
	public final static int EXACT_SOLUTION = 1;
	
	protected int solutionType = UNKNOWN_SOLUTION;
/**
 * This method was created by a SmartGuide.
 * @param var cbit.vcell.math.Variable
 */
protected Equation (Variable var) {
	this.var = var;
}
/**
 * This method was created by a SmartGuide.
 */
protected Equation(Variable var, Expression initialExp, Expression rateExp) {
	this.var = var;
	this.rateExp = rateExp;
	this.initialExp = initialExp;
/*
	try {
		rateExp = rateExp.flatten();
		if (rateExp.evaluateConstant() == 0){
			bConstant = true;
		}
	}catch (Exception e){
	}	
*/
}

/**
 * This method was created by a SmartGuide.
 * @param symbolTable cbit.vcell.parser.SymbolTable
 * @exception java.lang.Exception The exception description.
 */
public void bind(SymbolTable symbolTable) throws ExpressionBindingException {
	MathDescription mathDesc = null;
	if (symbolTable instanceof MathSymbolTable){
		mathDesc = ((MathSymbolTable)symbolTable).getMathDescription();
	}else if (symbolTable instanceof MathDescription){
		mathDesc = (MathDescription)symbolTable;
	}else{
		throw new RuntimeException("unexpected SymbolTable type '"+symbolTable.getClass().getName()+"', expecting MathDescription or SimulationSymbolTable");
	}
	Enumeration<Expression> enum1 = getExpressions(mathDesc).elements();
	while (enum1.hasMoreElements()){
		Expression exp = enum1.nextElement();
		exp.bindExpression(symbolTable);
	}	
}
/**
 * This method was created in VisualAge.
 * @return boolean
 * @param object java.lang.Object
 */
public abstract boolean compareEqual(Matchable object);
/**
 * This method was created in VisualAge.
 * @return boolean
 * @param object java.lang.Object
 */
protected boolean compareEqual0(Equation equ) {
	if (!Compare.isEqualOrNull(rateExp,equ.rateExp)){
		return false;
	}
	if (!Compare.isEqualOrNull(initialExp,equ.initialExp)){
		return false;
	}
	if (!Compare.isEqualOrNull(exactExp,equ.exactExp)){
		return false;
	}
	if (!Compare.isEqualOrNull(var,equ.var)){
		return false;
	}
	if (solutionType != equ.solutionType){
		return false;
	}
	return true;
}
/**
 * Insert the method's description here.
 * Creation date: (10/10/2002 10:31:03 AM)
 * @param sim cbit.vcell.solver.Simulation
 */
abstract void flatten(MathSymbolTable simSymbolTable, boolean bRoundCoefficients) throws ExpressionException, MathException;
/**
 * Insert the method's description here.
 * Creation date: (10/10/2002 10:31:03 AM)
 * @param sim cbit.vcell.solver.Simulation
 */
final void flatten0(MathSymbolTable simSymbolTable, boolean bRoundCoefficients) throws ExpressionException, MathException {

	rateExp = getFlattenedExpression(simSymbolTable,rateExp,bRoundCoefficients);
	initialExp = getFlattenedExpression(simSymbolTable,initialExp,bRoundCoefficients);
	exactExp = getFlattenedExpression(simSymbolTable,exactExp,bRoundCoefficients);
}
/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.parser.Expression
 */
public Expression getExactSolution() {
	return exactExp;
}
/**
 * This method was created by a SmartGuide.
 * @return java.util.Vector
 */
public abstract Vector<Expression> getExpressions(MathDescription mathDesc);
/**
 * Insert the method's description here.
 * Creation date: (10/10/2002 10:31:03 AM)
 * @param sim cbit.vcell.solver.Simulation
 */
public final static Expression getFlattenedExpression(MathSymbolTable simSymbolTable, Expression exp, boolean bRoundCoefficients) throws ExpressionException, MathException {
	if (exp == null){
		return null;
	}
	
	exp.bindExpression(simSymbolTable);
	exp = simSymbolTable.substituteFunctions(exp);
	try {
		exp = exp.flatten();
	}catch (DivideByZeroException e){
		System.out.println(e.getMessage());
		throw e;
	}
	if (bRoundCoefficients){
		exp.roundToFloat();  /// warning looses precision on literal coefficients (doesn't follow bound expressions)
	}

	return exp.flatten();

}
/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.parser.Expression
 * @exception java.lang.Exception The exception description.
 */
public final Expression getFlattenedRateExpression(MathSymbolTable simSymbolTable) throws ExpressionException, MathException {
	return getFlattenedExpression(simSymbolTable, new Expression(getRateExpression()),false);
}
/**
 * This method was created by a SmartGuide.
 * @return double
 */
public Expression getInitialExpression() {
	return initialExp;
}
/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.parser.Expression
 * @exception java.lang.Exception The exception description.
 */
public Expression getRateExpression() {
	return rateExp;
}
/**
 * This method was created by a SmartGuide.
 * @return java.util.Enumeration
 */
public final Enumeration<Variable> getRequiredVariables(MathSymbolTable simSymbolTable) throws Exception {
	Vector<Variable> requiredVarList = new Vector<Variable>();
	Vector<Expression> expList = getExpressions(simSymbolTable.getMathDescription());
	for (int i=0;i<expList.size();i++){
		Expression exp = expList.elementAt(i);
		if (exp != null){
			exp = new Expression(exp);
			exp.bindExpression(simSymbolTable);
			exp = simSymbolTable.substituteFunctions(exp);
			String identifiers[] = exp.getSymbols();
			if (identifiers != null){
				for (int j=0;j<identifiers.length;j++){
					Variable var = simSymbolTable.getVariable(identifiers[j]);
					if (var == null){
						var = ReservedMathSymbolEntries.getReservedVariableEntry(identifiers[j]);
					}
					if (var==null){
						throw new Exception("unresolved symbol "+identifiers[j]+" in expression "+exp);
					}	
					if (!requiredVarList.contains(var)){
						requiredVarList.addElement(var);
					}	
				}	
			}
		}		
	}	
	return requiredVarList.elements();
}
/**
 * This method was created by a SmartGuide.
 * @return int
 */
public int getSolutionType() {
	return solutionType;
}
/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.parser.Expression
 */
public abstract Enumeration<Expression> getTotalExpressions() throws ExpressionException;
/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.parser.Expression
 */
public Expression getTotalInitialExpression() throws ExpressionException {
	//
	// initial condition can be from initialExp or from Exact or Constructed Solution
	//
	Expression lvalueExp = new Expression(getVariable().getName()+"_initial;");
	Expression iexp = null;
	if (initialExp != null){
		iexp = new Expression(initialExp);
	}else if (exactExp != null){
		iexp = new Expression(exactExp);
	}else{
		iexp = null;
	}				
	Expression expt = new Expression("t;");
	Expression exp0 = new Expression(0.0);
	iexp.substituteInPlace(expt,exp0);
	Expression rvalueExp = iexp;
	Expression totalExp = Expression.assign(lvalueExp,rvalueExp);
	totalExp.bindExpression(null);
	totalExp.flatten();
	return totalExp;
}
/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.parser.Expression
 */
public Expression getTotalSolutionExpression() throws ExpressionException {
	if (exactExp!=null){
		//
		// Exact Solution was specified
		//
		Expression lvalueExp = new Expression(getVariable().getName()+"_exact;");
		Expression rvalueExp = new Expression(exactExp);
		Expression totalExp = Expression.assign(lvalueExp,rvalueExp);
		totalExp.bindExpression(null);
		return totalExp.flatten();
	}else{
		return null;
	}			
}
/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.math.Variable
 */
public Variable getVariable() {
	return var;
}
/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public abstract String getVCML();

/**
 * Insert the method's description here.
 * Creation date: (7/6/00 9:38:12 AM)
 * @param constructedExpression cbit.vcell.parser.Expression
 */
public void setExactSolution(Expression exactExpression) {
	this.exactExp = exactExpression;
	this.solutionType = EXACT_SOLUTION;
}
/**
 * This method was created by a SmartGuide.
 * @param initialExpression cbit.vcell.parser.Expression
 */
public void setInitialExpression(Expression initialExpression) {
	this.initialExp = initialExpression;
	this.solutionType = UNKNOWN_SOLUTION;
}
/**
 * This method was created by a SmartGuide.
 * @param rateExpression cbit.vcell.parser.Expression
 */
public void setRateExpression(Expression rateExpression) {
	this.rateExp = rateExpression;
}
/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public String toString() {
	return getClass().toString()+"  "+"d["+var.getName()+"]/dt = "+rateExp;
}

public final void getDiscontinuities(MathSymbolTable simSymbolTable, Vector<Discontinuity> discontinuities) throws ExpressionException, MathException {
	Vector<Expression> expressions = getExpressions(simSymbolTable.getMathDescription());
	for (Expression exp : expressions) {
		Expression exp1 = new Expression(exp);
		exp1.bindExpression(simSymbolTable);
		exp1 = simSymbolTable.substituteFunctions(exp1).flatten();
		Vector<Discontinuity> v = exp1.getDiscontinuities();
		discontinuities.addAll(v);
	}
}

public boolean hasDiscontinuities(MathDescription mathDesc) throws ExpressionException {
	Vector<Expression> expressions = getExpressions(mathDesc);
	for (Expression exp : expressions) {
		if (exp.getDiscontinuities().size() > 0) {
			return true;
		}
	}
	return false;	
}

public abstract void checkValid(MathDescription mathDesc, SubDomain subDomain) throws MathException, ExpressionException;

protected void checkInitialCondition(MathDescription mathDesc) throws ExpressionException, MathException {
	Enumeration<Variable> varEnum = MathUtilities.getRequiredVariables(getInitialExpression(), mathDesc);
	while (varEnum.hasMoreElements()) {
		Variable refVar = varEnum.nextElement();
		if (refVar instanceof VolVariable
			|| refVar instanceof VolumeRegionVariable
			|| refVar instanceof MemVariable
			|| refVar instanceof MembraneRegionVariable
			|| refVar instanceof VolumeParticleVariable
			|| refVar instanceof MembraneParticleVariable) {
			throw new MathException("Initial condition for variable '" + getVariable().getName() + "' references variable '"+ refVar.getName() +"'. Initial conditions cannot reference variables.");
		}
	}
}
	
protected void checkValid_Volume(MathDescription mathDesc, List<Expression> expList, CompartmentSubDomain compartmentSubDomain) throws MathException, ExpressionException {
	for (Expression exp : expList) {
		if (exp == null) {
			return;
		}
		Domain volumeDomain = new Domain(compartmentSubDomain);
		Enumeration<Variable> varEnum = MathUtilities.getRequiredVariables(getInitialExpression(), mathDesc);
		while (varEnum.hasMoreElements()) {
			Variable refVar = varEnum.nextElement();
			if (refVar != null && refVar.getDomain()!=null){
				// if variable defined on this surface or the appropriate volume domain, then it is ok.
				if (refVar.getDomain().compareEqual(volumeDomain)){
					continue;
				}
			}
			if ((refVar instanceof MemVariable
					|| refVar instanceof MembraneRandomVariable
					|| refVar instanceof MembraneParticleVariable
					|| refVar instanceof MembraneRegionVariable)) {
				String varType = "membrane";
				if (refVar instanceof MembraneRandomVariable) {
					varType = "membrane random";
				} else if (refVar instanceof MembraneRegionVariable) {
					varType = "membrane region";
				} else if (refVar instanceof MembraneParticleVariable) {
					varType = "membrane particle";
				}
				throw new MathException("Equation for volume variable '" + getVariable().getName() + "' references " + varType
						+ " variable '" + refVar.getName() + "'.");
			}
		}
	}
} 

protected void checkValid_Membrane(MathDescription mathDesc, List<Expression> expList, MembraneSubDomain membraneSubDomain) throws MathException, ExpressionException {
	for (Expression exp : expList) {
		if (exp == null) {
			return;
		}
		Domain surfaceDomain = new Domain(membraneSubDomain);
		Domain volume1Domain = new Domain(membraneSubDomain.getInsideCompartment());
		Domain volume2Domain = new Domain(membraneSubDomain.getOutsideCompartment());
		Enumeration<Variable> varEnum = MathUtilities.getRequiredVariables(getInitialExpression(), mathDesc);
		while (varEnum.hasMoreElements()) {
			Variable refVar = varEnum.nextElement();
			if (refVar!=null && refVar.getDomain()!=null){
				// if variable defined on this surface or the appropriate volume domain, then it is ok.
				if (refVar.getDomain().compareEqual(surfaceDomain) || refVar.getDomain().compareEqual(volume1Domain) || refVar.getDomain().compareEqual(volume2Domain)){
					continue;
				}
			}
			String refVarName = refVar.getName();
			if (refVar instanceof VolumeRandomVariable) {
				throw new MathException("Equation for membrane variable '" + getVariable().getName() + "' references volume random variable '" + refVarName + "'.");
			}
			if (refVar instanceof VolumeParticleVariable) {
				throw new MathException("Equation for membrane variable '" + getVariable().getName() + "' references volume particle variable '" + refVarName + "'.");
			}
			if (refVar instanceof VolVariable
					|| refVar instanceof VolumeRegionVariable) {
				String varType = "volume";
				if (refVar instanceof VolumeRegionVariable) {
					varType = "volume region";
				}
				throw new MathException("Equation for membrane variable '" + getVariable().getName() + "' references " +
						varType + " variable '" + refVarName + "'. Please use " +
						refVarName + "_INSIDE or " + refVarName + "_OUTSIDE to denote " + varType + " variable " + refVarName  + "'s solution at the membrane");
			}
		}
	}
}

protected void checkValid_PointSubDomain(MathDescription mathDesc, List<Expression> expList, PointSubDomain pointSubDomain) throws MathException, ExpressionException {
	for (Expression exp : expList) {
		if (exp == null) {
			return;
		}
		Domain domain = new Domain(pointSubDomain);
		Enumeration<Variable> varEnum = MathUtilities.getRequiredVariables(getInitialExpression(), mathDesc);
		while (varEnum.hasMoreElements()) {
			Variable refVar = varEnum.nextElement();
			if (refVar!=null && refVar.getDomain()!=null){
				// if variable defined on this surface or the appropriate volume domain, then it is ok.
				if (!refVar.getDomain().compareEqual(domain)){
					throw new MathException("Equation for point variable '" + getVariable().getName() + "' references variable not defined on this point, not yet supported (variable is '" + refVar.getName() + "'.");
				}
			}
		}
	}
}

public void refreshDependencies(MathDescription mathDesc) {
}

}
