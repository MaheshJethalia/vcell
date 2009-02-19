package cbit.vcell.math;
/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import java.util.*;

import cbit.util.CommentStringTokenizer;
import cbit.vcell.parser.*;
/**
 * This class was generated by a SmartGuide.
 * 
 */
public class OdeEquation extends Equation {
/**
 * OdeEquation constructor comment.
 * @param subDomain cbit.vcell.math.SubDomain
 * @param var cbit.cell.math.Variable
 * @param initialExp cbit.vcell.parser.Expression
 * @param rateExp cbit.vcell.parser.Expression
 */
public OdeEquation(Variable var, Expression initialExp, Expression rateExp) {
	super(var, initialExp, rateExp);
}


/**
 * Insert the method's description here.
 * Creation date: (9/4/2003 12:32:19 PM)
 * @return boolean
 * @param object cbit.util.Matchable
 */
public boolean compareEqual(cbit.util.Matchable object) {
	OdeEquation equ = null;
	if (!(object instanceof OdeEquation)){
		return false;
	}else{
		equ = (OdeEquation)object;
	}
	if (!compareEqual0(equ)){
		return false;
	}
	return true;
}


/**
 * Insert the method's description here.
 * Creation date: (10/10/2002 10:41:10 AM)
 * @param sim cbit.vcell.solver.Simulation
 */
void flatten(cbit.vcell.solver.Simulation sim, boolean bRoundCoefficients) throws cbit.vcell.parser.ExpressionException, MathException {
	super.flatten0(sim,bRoundCoefficients);
}


/**
 * This method was created by a SmartGuide.
 * @return java.util.Vector
 */
protected Vector<Expression> getExpressions(MathDescription mathDesc){
	Vector<Expression> list = new Vector<Expression>();
	
	if (getRateExpression()!=null)		list.addElement(getRateExpression());
	if (getInitialExpression()!=null)	list.addElement(getInitialExpression());
	if (getExactSolution()!=null)		list.addElement(getExactSolution());
	return list;
}


/**
 * This method was created by a SmartGuide.
 * @return java.util.Enumeration
 */
public Enumeration<Expression> getTotalExpressions() throws ExpressionException {
	Vector<Expression> vector = new Vector<Expression>();
	vector.addElement(getTotalRateExpression());
	vector.addElement(getTotalInitialExpression());
	Expression solutionExp = getTotalSolutionExpression();
	if (solutionExp!=null){
		vector.addElement(solutionExp);
	}	
	return vector.elements();
}


/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.parser.Expression
 */
private Expression getTotalRateExpression() throws ExpressionException {
	Expression lvalueExp = Expression.derivative("t",new Expression(getVariable().getName()+";"));
//	Expression lvalueExp = new Expression("d/dt*"+getVariable().getName()+";");
	Expression rvalueExp = new Expression(new Expression(getRateExpression()));
	Expression totalExp = Expression.assign(lvalueExp,rvalueExp);
	totalExp.bindExpression(null);
	totalExp.flatten();
	return totalExp;
}


/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public String getVCML() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("\t"+VCML.OdeEquation+" "+getVariable().getName()+" {\n");
	if (getRateExpression() != null){
		buffer.append("\t\t"+VCML.Rate+"\t"+getRateExpression().infix()+";\n");
	}else{
		buffer.append("\t\t"+VCML.Rate+"\t"+"0.0;\n");
	}
	if (initialExp != null){
		buffer.append("\t\t"+VCML.Initial+"\t "+initialExp.infix()+";\n");
	}
	switch (solutionType){
		case UNKNOWN_SOLUTION:{
			if (initialExp == null){
				buffer.append("\t\t"+VCML.Initial+"\t "+"0.0;\n");
			}
			break;
		}
		case EXACT_SOLUTION:{
			buffer.append("\t\t"+VCML.Exact+"\t"+exactExp.infix()+";\n");
			break;
		}
	}				
		
	buffer.append("\t}\n");
	return buffer.toString();		
}


/**
 * This method was created by a SmartGuide.
 * @param tokens java.util.StringTokenizer
 * @exception java.lang.Exception The exception description.
 */
public void read(CommentStringTokenizer tokens) throws MathFormatException, ExpressionException {
	String token = null;
	token = tokens.nextToken();
	if (!token.equalsIgnoreCase(VCML.BeginBlock)){
		throw new MathFormatException("unexpected token "+token+" expecting "+VCML.BeginBlock);
	}			
	while (tokens.hasMoreTokens()){
		token = tokens.nextToken();
		if (token.equalsIgnoreCase(VCML.EndBlock)){
			break;
		}			
		if (token.equalsIgnoreCase(VCML.Initial)){
			initialExp = new Expression(tokens);
			continue;
		}
		if (token.equalsIgnoreCase(VCML.Rate)){
			Expression exp = new Expression(tokens);
			setRateExpression(exp);
			continue;
		}
		if (token.equalsIgnoreCase(VCML.Exact)){
			exactExp = new Expression(tokens);
			solutionType = EXACT_SOLUTION;
			continue;
		}
		throw new MathFormatException("unexpected identifier "+token);
	}	
		
}
}