package cbit.vcell.math;
/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import java.util.Enumeration;
import java.util.Vector;

import org.vcell.util.CommentStringTokenizer;
import org.vcell.util.Compare;
import org.vcell.util.Matchable;

import cbit.vcell.parser.ExpressionException;
/**
 * This class was generated by a SmartGuide.
 * 
 */
public class MembraneSubDomain extends SubDomain {
	private Vector<JumpCondition> jumpConditionList = new Vector<JumpCondition>();
	private CompartmentSubDomain insideCompartment = null;
	private CompartmentSubDomain outsideCompartment = null;

	private BoundaryConditionType boundaryConditionTypeXp = BoundaryConditionType.getDIRICHLET();
	private BoundaryConditionType boundaryConditionTypeXm = BoundaryConditionType.getDIRICHLET();
	private BoundaryConditionType boundaryConditionTypeYp = BoundaryConditionType.getDIRICHLET();
	private BoundaryConditionType boundaryConditionTypeYm = BoundaryConditionType.getDIRICHLET();
	private BoundaryConditionType boundaryConditionTypeZp = BoundaryConditionType.getDIRICHLET();
	private BoundaryConditionType boundaryConditionTypeZm = BoundaryConditionType.getDIRICHLET();

/**
 * This method was created by a SmartGuide.
 * @param inside cbit.vcell.math.CompartmentSubDomain
 * @param outside cbit.vcell.math.CompartmentSubDomain
 */
public MembraneSubDomain (CompartmentSubDomain inside, CompartmentSubDomain outside) {
	super(inside.getName()+"_"+outside.getName()+"_membrane");
	this.insideCompartment = inside;
	this.outsideCompartment = outside;
}


/**
 * This method was created by a SmartGuide.
 * @param equation cbit.vcell.math.Equation
 */
public void addEquation(Equation equation) throws MathException {
	if (equation instanceof JumpCondition){
		addJumpCondition((JumpCondition)equation);
	}else{
		super.addEquation(equation);
	}
}


/**
 * This method was created by a SmartGuide.
 * @param equation cbit.vcell.math.Equation
 */
public void addJumpCondition(JumpCondition jumpCondition) throws MathException {
	if (getJumpCondition((VolVariable)jumpCondition.getVariable()) != null){
		throw new MathException("jumpCondition for variable "+jumpCondition.getVariable()+" already exists");
	}
	jumpConditionList.addElement(jumpCondition);	
}


/**
 * This method was created in VisualAge.
 * @return boolean
 * @param object java.lang.Object
 */
public boolean compareEqual(Matchable object) {
	if (!super.compareEqual0(object)){
		return false;
	}
	MembraneSubDomain msd = null;
	if (!(object instanceof MembraneSubDomain)){
		return false;
	}else{
		msd = (MembraneSubDomain)object;
	}
	//
	// compare jumpConditions
	//
	if (!Compare.isEqual(jumpConditionList,msd.jumpConditionList)){
		return false;
	}
	//
	// compare insideCompartment
	//
	if (insideCompartment==null){
		if (msd.insideCompartment!=null){
			return false;
		}
	}else if (!Compare.isEqual(insideCompartment,msd.insideCompartment)){
		return false;
	}
	//
	// compare outsideCompartment
	//
	if (outsideCompartment==null){
		if (msd.outsideCompartment!=null){
			return false;
		}
	}else if (!Compare.isEqual(outsideCompartment,msd.outsideCompartment)){
		return false;
	}
	
	//
	// compare boundaryConditions
	//
	if (!Compare.isEqual(boundaryConditionTypeXp,msd.boundaryConditionTypeXp)){
		return false;
	}
	if (!Compare.isEqual(boundaryConditionTypeXm,msd.boundaryConditionTypeXm)){
		return false;
	}
	if (!Compare.isEqual(boundaryConditionTypeYp,msd.boundaryConditionTypeYp)){
		return false;
	}
	if (!Compare.isEqual(boundaryConditionTypeYm,msd.boundaryConditionTypeYm)){
		return false;
	}
	if (!Compare.isEqual(boundaryConditionTypeZp,msd.boundaryConditionTypeZp)){
		return false;
	}
	if (!Compare.isEqual(boundaryConditionTypeZm,msd.boundaryConditionTypeZm)){
		return false;
	}
	return true;
}


/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public BoundaryConditionType getBoundaryConditionXm() {
	return boundaryConditionTypeXm;
}


/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public BoundaryConditionType getBoundaryConditionXp() {
	return boundaryConditionTypeXp;
}


/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public BoundaryConditionType getBoundaryConditionYm() {
	return boundaryConditionTypeYm;
}


/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public BoundaryConditionType getBoundaryConditionYp() {
	return boundaryConditionTypeYp;
}


/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public BoundaryConditionType getBoundaryConditionZm() {
	return boundaryConditionTypeZm;
}


/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public BoundaryConditionType getBoundaryConditionZp() {
	return boundaryConditionTypeZp;
}


/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.math.CompartmentSubDomain
 */
public CompartmentSubDomain getInsideCompartment() {
	return insideCompartment;
}


/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.math.JumpCondition
 * @param volVar cbit.vcell.math.VolVariable
 * @exception java.lang.Exception The exception description.
 */
public JumpCondition getJumpCondition(VolVariable volVar) {
	Enumeration<JumpCondition> enum1 = jumpConditionList.elements();
	while (enum1.hasMoreElements()){
		JumpCondition jump = enum1.nextElement();
		if (jump.getVariable().getName().equals(volVar.getName())){
			return jump;
		}
	}
	return null;
}


/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.math.JumpCondition
 */
public Enumeration<JumpCondition> getJumpConditions() {
	return jumpConditionList.elements();
}


/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.math.CompartmentSubDomain
 */
public CompartmentSubDomain getOutsideCompartment() {
	return outsideCompartment;
}


/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public String getVCML(int spatialDimension) {
	StringBuffer buffer = new StringBuffer();
	buffer.append(VCML.MembraneSubDomain+" "+insideCompartment.getName()+" "+outsideCompartment.getName()+" {\n");
	if (spatialDimension>=1){
		buffer.append("\t"+VCML.BoundaryXm+"\t "+boundaryConditionTypeXm.toString()+"\n");
		buffer.append("\t"+VCML.BoundaryXp+"\t "+boundaryConditionTypeXp.toString()+"\n");
	}
	if (spatialDimension>=2){
		buffer.append("\t"+VCML.BoundaryYm+"\t "+boundaryConditionTypeYm.toString()+"\n");
		buffer.append("\t"+VCML.BoundaryYp+"\t "+boundaryConditionTypeYp.toString()+"\n");
	}
	if (spatialDimension==3){
		buffer.append("\t"+VCML.BoundaryZm+"\t "+boundaryConditionTypeZm.toString()+"\n");
		buffer.append("\t"+VCML.BoundaryZp+"\t "+boundaryConditionTypeZp.toString()+"\n");
	}
	Enumeration<Equation> enum1 = getEquations();
	while (enum1.hasMoreElements()){
		Equation equ = enum1.nextElement();
		buffer.append(equ.getVCML());
	}	
	Enumeration<JumpCondition> enum2 = getJumpConditions();
	while (enum2.hasMoreElements()){
		JumpCondition jc = enum2.nextElement();
		buffer.append(jc.getVCML());
	}	
	if (getFastSystem()!=null){
		buffer.append(getFastSystem().getVCML());
	}
	buffer.append("}\n");
	return buffer.toString();		
}


/**
 * This method was created by a SmartGuide.
 * @param tokens java.util.StringTokenizer
 * @exception java.lang.Exception The exception description.
 */
public void read(MathDescription mathDesc, CommentStringTokenizer tokens) throws MathException, ExpressionException {
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
		if (token.equalsIgnoreCase(VCML.OdeEquation)){
			token = tokens.nextToken();
			Variable var = mathDesc.getVariable(token);
			if (var == null){
				throw new MathFormatException("variable "+token+" not defined");
			}	
			if (!(var instanceof MemVariable)){
				throw new MathFormatException("variable "+token+" not a "+VCML.MembraneVariable);
			}	
			OdeEquation ode = new OdeEquation((MemVariable)var, null,null);
			ode.read(tokens);
			addEquation(ode);
			continue;
		}			
		if (token.equalsIgnoreCase(VCML.BoundaryXm)){
			String type = tokens.nextToken();
			boundaryConditionTypeXm = new BoundaryConditionType(type);
			continue;
		}			
		if (token.equalsIgnoreCase(VCML.BoundaryXp)){
			String type = tokens.nextToken();
			boundaryConditionTypeXp = new BoundaryConditionType(type);
			continue;
		}			
		if (token.equalsIgnoreCase(VCML.BoundaryYm)){
			String type = tokens.nextToken();
			boundaryConditionTypeYm = new BoundaryConditionType(type);
			continue;
		}			
		if (token.equalsIgnoreCase(VCML.BoundaryYp)){
			String type = tokens.nextToken();
			boundaryConditionTypeYp = new BoundaryConditionType(type);
			continue;
		}			
		if (token.equalsIgnoreCase(VCML.BoundaryZm)){
			String type = tokens.nextToken();
			boundaryConditionTypeZm = new BoundaryConditionType(type);
			continue;
		}			
		if (token.equalsIgnoreCase(VCML.BoundaryZp)){
			String type = tokens.nextToken();
			boundaryConditionTypeZp = new BoundaryConditionType(type);
			continue;
		}			
		if (token.equalsIgnoreCase(VCML.PdeEquation)){
			token = tokens.nextToken();
			Variable var = mathDesc.getVariable(token);
			if (var == null){
				throw new MathFormatException("variable "+token+" not defined");
			}	
			if (!(var instanceof MemVariable)){
				throw new MathFormatException("variable "+token+" not a MembraneVariable");
			}	
			PdeEquation pde = new PdeEquation((MemVariable)var);
			pde.read(tokens);
			addEquation(pde);
			continue;
		}			
		if (token.equalsIgnoreCase(VCML.MembraneRegionEquation)){
			token = tokens.nextToken();
			Variable var = mathDesc.getVariable(token);
			if (var == null){
				throw new MathFormatException("variable "+token+" not defined");
			}	
			if (!(var instanceof MembraneRegionVariable)){
				throw new MathFormatException("variable "+token+" not a "+VCML.MembraneRegionVariable);
			}	
			MembraneRegionEquation mre = new MembraneRegionEquation((MembraneRegionVariable)var, null);
			mre.read(tokens);
			addEquation(mre);
			continue;
		}			
		if (token.equalsIgnoreCase(VCML.JumpCondition)){
			token = tokens.nextToken();
			Variable var = mathDesc.getVariable(token);
			if (var == null){
				throw new MathFormatException("variable "+token+" not defined");
			}	
			if (!(var instanceof VolVariable)){
				throw new MathException("variable "+token+" not a VolumeVariable");
			}	
			JumpCondition jump = new JumpCondition((VolVariable)var);
			jump.read(tokens);
			addJumpCondition(jump);
			continue;
		}			
		if (token.equalsIgnoreCase(VCML.FastSystem)){
			FastSystem fs = new FastSystem(mathDesc);
			fs.read(tokens);
			setFastSystem(fs);
			continue;
		}			
		throw new MathFormatException("unexpected identifier "+token);
	}	
		
}


/**
 * This method was created in VisualAge.
 * @param equation cbit.vcell.math.Equation
 */
public void replaceJumpCondition(JumpCondition jumpCondition) throws MathException {
	JumpCondition currentJumpCondition = getJumpCondition((VolVariable)jumpCondition.getVariable());
	if (currentJumpCondition!=null){
		jumpConditionList.removeElement(currentJumpCondition);
	}
	addJumpCondition(jumpCondition);
}


/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public void setBoundaryConditionXm(BoundaryConditionType bc) {
	boundaryConditionTypeXm = bc;
}


/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public void setBoundaryConditionXp(BoundaryConditionType bc) {
	boundaryConditionTypeXp = bc;
}


/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public void setBoundaryConditionYm(BoundaryConditionType bc) {
	boundaryConditionTypeYm = bc;
}


/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public void setBoundaryConditionYp(BoundaryConditionType bc) {
	boundaryConditionTypeYp = bc;
}


/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public void setBoundaryConditionZm(BoundaryConditionType bc) {
	boundaryConditionTypeZm = bc;
}


/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public void setBoundaryConditionZp(BoundaryConditionType bc) {
	boundaryConditionTypeZp = bc;
}
}