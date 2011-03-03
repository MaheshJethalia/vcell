package cbit.vcell.solver;
import org.vcell.util.Matchable;

import cbit.vcell.math.VCML;

/**
 * Insert the type's description here.
 * Creation date: (9/6/2005 3:09:27 PM)
 * @author: Jim Schaff
 */
@SuppressWarnings("serial")
public class UniformOutputTimeSpec extends OutputTimeSpec {
	private double fieldOutputTimeStep;

/**
 * UniformOutputTimeSpec constructor comment.
 */
public UniformOutputTimeSpec(double arg_outputTimeStep) {
	super();
	fieldOutputTimeStep = arg_outputTimeStep;
}


/**
 * Checks for internal representation of objects, not keys from database
 * @return boolean
 * @param obj java.lang.Object
 */
public boolean compareEqual(Matchable obj) {
	if (this == obj) {
		return (true);
	}
	
	if (obj != null && obj instanceof UniformOutputTimeSpec) {
		UniformOutputTimeSpec uot = (UniformOutputTimeSpec)obj;
		if (uot.fieldOutputTimeStep == fieldOutputTimeStep) {
			return true;
		}
	}
	
	return false;
}


/**
 * Insert the method's description here.
 * Creation date: (9/7/2005 8:45:54 AM)
 * @return double
 */
public double getOutputTimeStep() {
	return fieldOutputTimeStep;
}

/**
 * Set a new unifromOutputTimeSpec.
 * It is initialized in SolverDescription and default value is 0.1.
 * @return null. Amended June 2nd, 2007
 */
public void setOuputTimeStep(double arg_outputTimeStep)
{
	fieldOutputTimeStep = arg_outputTimeStep;
}

/**
 * Insert the method's description here.
 * Creation date: (9/7/2005 3:43:26 PM)
 * @return java.lang.String
 */
public java.lang.String getDescription() {
	return "every " + fieldOutputTimeStep + "s";
}


/**
 * Insert the method's description here.
 * Creation date: (9/7/2005 8:56:49 AM)
 * @return java.lang.String
 */
public java.lang.String getVCML() {
	//
	// write format as follows:
	//
	//   OutputOptions {
	//		OutputTimeStep 10
	//   }
	//
	//	
	StringBuffer buffer = new StringBuffer();
	
	buffer.append(VCML.OutputOptions + " " + VCML.BeginBlock + "\n");
	
	buffer.append("    " + VCML.OutputTimeStep + " " + fieldOutputTimeStep + "\n");	

	buffer.append(VCML.EndBlock + "\n");

	return buffer.toString();
}


/**
 * Insert the method's description here.
 * Creation date: (9/7/2005 1:53:56 PM)
 * @return boolean
 */
public boolean isDefault() {
	return false;
}


/**
 * Insert the method's description here.
 * Creation date: (9/7/2005 1:53:56 PM)
 * @return boolean
 */
public boolean isExplicit() {
	return false;
}


/**
 * Insert the method's description here.
 * Creation date: (9/7/2005 1:53:56 PM)
 * @return boolean
 */
public boolean isUniform() {
	return true;
}


@Override
public String toString() {
	return String.valueOf(fieldOutputTimeStep);
}


}