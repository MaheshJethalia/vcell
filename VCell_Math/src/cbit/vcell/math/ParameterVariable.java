package cbit.vcell.math;

/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import edu.uchc.vcell.expression.internal.*;
/**
 * This class was generated by a SmartGuide.
 * 
 */
public class ParameterVariable extends Variable {
/**
 * Constant constructor comment.
 * @param name java.lang.String
 */
public ParameterVariable(String name) {
	super(name);
	if (name==null){
		throw new IllegalArgumentException("name is null");
	}
}
/**
 * This method was created in VisualAge.
 * @return boolean
 * @param obj Matchable
 */
public boolean compareEqual(cbit.util.Matchable obj) {
	if (!(obj instanceof ParameterVariable)){
		return false;
	}
	if (!compareEqual0(obj)){
		return false;
	}

	return true;
}
/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public String getVCML() {
	return "Parameter  "+getName();
}
/**
 * This method was created in VisualAge.
 * @return java.lang.String
 */
public String toString() {
	return "Parameter("+hashCode()+") <"+getName()+">";
}
}
