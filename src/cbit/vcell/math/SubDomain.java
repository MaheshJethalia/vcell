package cbit.vcell.math;
/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import java.util.*;
import java.io.*;
import cbit.util.*;
import cbit.vcell.parser.Expression;
/**
 * This class was generated by a SmartGuide.
 * 
 */
public abstract class SubDomain implements Serializable, Matchable {
	private String name = null;
	private Vector equationList = new Vector();
	private FastSystem fastSystem = null;
	private Vector listOfJumpProcesses = null;
	private Vector listOfVarIniConditions = null;

/**
 * This method was created by a SmartGuide.
 * @param name java.lang.String
 */
protected SubDomain (String name) {
	this.name = name;
	listOfJumpProcesses = new Vector();
	listOfVarIniConditions = new Vector();
}


/**
 * This method was created by a SmartGuide.
 * @param equation cbit.vcell.math.Equation
 */
public void addEquation(Equation equation) throws MathException {
	if (getEquation(equation.getVariable()) != null){
		throw new MathException("equation for variable "+equation.getVariable().getName()+" already exists");
	}
	equationList.addElement(equation);	
}


/**
 * Insert the method's description here.
 * Creation date: (6/26/2006 5:28:25 PM)
 */
public void addJumpProcess() {}


/**
 * Append a new process to the jump process list if it is still not in the list.
 * Creation date: (6/26/2006 5:28:25 PM)
 */
public void addJumpProcess(JumpProcess newJumpProcess) throws MathException
{
	if(getJumpProcess(newJumpProcess.getName())!=null)
		throw new MathException("JumpProcess "+newJumpProcess.getName()+" already exists");
	listOfJumpProcesses.add(newJumpProcess);
}


/**
 * Append a new variable initial condition to the list if the variable is not in the list.
 * Creation date: (6/27/2006 10:02:29 AM)
 * @param newVarIniCondition cbit.vcell.math.VarIniCondition
 */
public void addVarIniCondition(VarIniCondition newVarIniCondition) throws MathException
{
	if(getVarIniCondition(newVarIniCondition.getVar().getName())!=null)
		throw new MathException("Initial condition regarding variable: "+newVarIniCondition.getVar().getName()+" already exists");
	listOfVarIniConditions.add(newVarIniCondition);
}


/**
 * This method was created in VisualAge.
 * @return boolean
 * @param object java.lang.Object
 */
protected boolean compareEqual0(Object object) {
	
	SubDomain subDomain = null;
	if (object == null){
		return false;
	}
	if (!(object instanceof SubDomain)){
		return false;
	}else{
		subDomain = (SubDomain)object;
	}

	//
	// compare name
	//
	if (!Compare.isEqualOrNull(name,subDomain.name)){
		return false;
	}
	//
	// compare equations
	//
	if (!Compare.isEqual(equationList,subDomain.equationList)){
		return false;
	}
	//
	// compare fastSystem
	//
	if (!Compare.isEqualOrNull(fastSystem,subDomain.fastSystem)){
		return false;
	}
	//
	// compare jumpProcesses
	//
	if ((listOfJumpProcesses != null) && (subDomain.listOfJumpProcesses != null))
	{
		JumpProcess jumpProcesses1[] = (JumpProcess[]) listOfJumpProcesses.toArray(new JumpProcess[0]);
		JumpProcess jumpProcesses2[] = (JumpProcess[]) subDomain.listOfJumpProcesses.toArray(new JumpProcess[0]);
		
		if (!Compare.isEqualOrNull(jumpProcesses1, jumpProcesses2)){ //call isEqualOrNull(Matchable[], Matchable[]) function
			return false;
		}
	}
	else return false;
	//
	// compare varIniConditions
	//
	if ((listOfVarIniConditions != null) && (subDomain.listOfVarIniConditions != null))
	{
		VarIniCondition varIniConditions1[] = (VarIniCondition[]) listOfVarIniConditions.toArray(new VarIniCondition[0]);
		VarIniCondition varIniConditions2[] = (VarIniCondition[]) subDomain.listOfVarIniConditions.toArray(new VarIniCondition[0]);
		if (!Compare.isEqualOrNull(varIniConditions1,varIniConditions2)){
			return false;
		}
	
	}
	else return false;
		
	return true;
}


/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.math.Equation
 * @param variable cbit.vcell.math.Variable
 */
public Equation getEquation(Variable variable) {
	Enumeration enum1 = equationList.elements();
	while (enum1.hasMoreElements()){
		Equation equ = (Equation)enum1.nextElement();
		if (equ.getVariable().getName().equals(variable.getName())){
			return equ;
		}
	}
	return null;
}


/**
 * This method was created by a SmartGuide.
 * @return java.util.Enumeration
 */
public java.util.Enumeration getEquations() {
	return equationList.elements();
}


/**
 * This method was created in VisualAge.
 * @return cbit.vcell.math.FastSystem
 */
public FastSystem getFastSystem() {
	return fastSystem;
}


/**
 * Get a jump process from the list by it's index.
 * Creation date: (6/27/2006 10:23:01 AM)
 * @return cbit.vcell.math.JumpProcess
 * @param index int
 */
public JumpProcess getJumpProcess(int index)
{
	if(index<listOfJumpProcesses.size())
		return (JumpProcess)listOfJumpProcesses.elementAt(index);
	return null;
}


/**
 * Get a jump process from process list by it's name.
 * Creation date: (6/27/2006 10:36:11 AM)
 * @return cbit.vcell.math.JumpProcess
 * @param processName java.lang.String
 */
public JumpProcess getJumpProcess(String processName) {
	for(int i=0; i<listOfJumpProcesses.size(); i++)
	{
		if(((JumpProcess)listOfJumpProcesses.elementAt(i)).getName().compareTo(processName)==0)
			return 	(JumpProcess)listOfJumpProcesses.elementAt(i);
	}
	return null;
}


/**
 * Return the reference of the jump process list.
 * Creation date: (6/27/2006 3:05:52 PM)
 * @return java.util.Vector
 */
public Vector getJumpProcesses() {
	return listOfJumpProcesses;
}


/**
 * Return the reference of the jump process list.
 * Creation date: (6/27/2006 3:05:52 PM)
 * @return java.util.Vector
 */
public Vector getListOfJumpProcesses() {
	return listOfJumpProcesses;
}


/**
 * Return the reference of the variable initial condition list.
 * Creation date: (6/27/2006 3:07:26 PM)
 * @return java.util.Vector
 */
public Vector getListOfVarIniConditions() {
	return listOfVarIniConditions;
}


/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public String getName() {
	return name;
}


/**
 * Get a variable initial condition from the list by it's index.
 * Creation date: (6/27/2006 10:40:07 AM)
 * @return cbit.vcell.math.VarIniCondition
 * @param index int
 */
public VarIniCondition getVarIniCondition(int index) 
{
	if(index<listOfVarIniConditions.size())
		return (VarIniCondition)listOfVarIniConditions.elementAt(index);
	return null;
}


/**
 * Get a variable initial condition from list by varaible's name.
 * Creation date: (6/27/2006 10:42:24 AM)
 * @return cbit.vcell.math.VarIniCondition
 * @param varName java.lang.String
 */
public VarIniCondition getVarIniCondition(String varName) 
{
	for(int i=0; i<listOfVarIniConditions.size(); i++)
	{
		if(((VarIniCondition)listOfVarIniConditions.elementAt(i)).getVar().getName().compareTo(varName)==0)
			return (VarIniCondition)listOfVarIniConditions.elementAt(i);
	}
	return null;
}


/**
 * Return the reference of the variable initial condition list.
 * Creation date: (6/27/2006 3:07:26 PM)
 * @return java.util.Vector
 */
public Vector getVarIniConditions() {
	return listOfVarIniConditions;
}


/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public abstract String getVCML(int spatialDimension);


/**
 * Remove 
 * Creation date: (6/26/2006 5:39:50 PM)
 */
public void removeJumpProcess() {}


/**
 * Remove a jump process from jump process list by it's index in the list.
 * Creation date: (6/26/2006 5:39:50 PM)
 */
public void removeJumpProcess(int index)
{
	if(index<listOfJumpProcesses.size())
		listOfJumpProcesses.remove(index);
}


/**
 * Remove a jump process from jump process list by it's index in the list.
 * Creation date: (6/26/2006 5:39:50 PM)
 */
public void removeJumpProcess(String procName)
{
	for(int i=0; i<listOfJumpProcesses.size(); i++)
	{
		if(((JumpProcess)listOfJumpProcesses.elementAt(i)).getName().compareTo(procName)==0)
			listOfJumpProcesses.remove(i) ;
	}
}


/**
 * Remove a variable initial conditino from list by it's index.
 * Creation date: (6/27/2006 10:04:45 AM)
 * @param index int
 */
public void removeVarIniCondition(int index)
{
	if(index<listOfVarIniConditions.size())
		listOfVarIniConditions.remove(index);
}


/**
 * Remove a variable initial condition from list by it's name in the list.
 * Creation date: (6/27/2006 10:06:44 AM)
 * @param varName java.lang.String
 */
public void removeVarIniCondition(String varName)
{
	for (int i=0; i<listOfVarIniConditions.size(); i++)
	{
		if(((VarIniCondition)listOfVarIniConditions.elementAt(i)).getVar().getName().compareTo(varName)==0)
			listOfVarIniConditions.remove(i);
	}	
}


/**
 * This method was created in VisualAge.
 * @param equation cbit.vcell.math.Equation
 */
public void replaceEquation(Equation equation) throws MathException {
	Equation currentEquation = getEquation(equation.getVariable());
	if (currentEquation!=null){
		equationList.removeElement(currentEquation);
	}
	addEquation(equation);
}


/**
 * This method was created in VisualAge.
 * @param fastSystem cbit.vcell.math.FastSystem
 * @exception java.lang.Exception The exception description.
 */
public void setFastSystem(FastSystem fastSystem) {
	this.fastSystem = fastSystem;
}


/**
 * This method was created in VisualAge.
 * @return java.lang.String
 */
public String toString() {
	return getClass().getName()+"@"+Integer.toHexString(hashCode())+": "+getName();
}


/**
 * Insert the method's description here.
 * Creation date: (10/12/2002 3:30:10 PM)
 */
public void trimTrivialEquations(MathDescription mathDesc) {
	for (int i = 0; i < equationList.size(); i++){
		Equation equ = (Equation)equationList.elementAt(i);
		Vector expressionList = equ.getExpressions(mathDesc);
		boolean bNonZeroExists = false;
		for (int j = 0; j < expressionList.size(); j++){
			Expression exp = (Expression)expressionList.elementAt(j);
			if (!exp.isZero()){
				bNonZeroExists = true;
				break;
			}
		}
		if (!bNonZeroExists){
			equationList.remove(i);
			i--;
		}
	}
}
}