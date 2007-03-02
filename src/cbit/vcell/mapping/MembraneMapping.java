package cbit.vcell.mapping;

/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import cbit.vcell.model.*;
import cbit.vcell.geometry.*;
import cbit.vcell.math.VCML;
import cbit.vcell.math.Function;
import cbit.vcell.parser.*;
import java.util.*;
import cbit.util.*;
import cbit.vcell.units.VCUnitDefinition;
/**
 * This class was generated by a SmartGuide.
 * 
 */
public class MembraneMapping extends StructureMapping implements java.beans.VetoableChangeListener {
	//private Expression surfaceToVolumeExpression = new Expression(1.0);
	//private Expression volumeFractionExpression = new Expression(0.2);

	protected transient java.beans.PropertyChangeSupport propertyChange;
	//private double fieldSpecificCapacitance_pF_squm = 0.01;   // cell membrane is typically 1uF/sqcm (from Hille page 9) = 0.01pF/squm
	//private cbit.vcell.parser.Expression fieldInitialVoltage = new Expression(0.0);
	private boolean fieldCalculateVoltage = false;

	public static boolean bFluxCorrectionBugMode = false;
	public static boolean bFluxCorrectionBugModeExercised = false;
	
	static {
		System.out.println("MembraneMapping.getTotalVolumeCorrection(): volume correction for a resolved membrane is meaningless, but maybe a 1.0 would be harmless");
	};
/**
 * MembraneMapping constructor comment.
 * @param membrane cbit.vcell.model.Membrane
 * @param geoContext cbit.vcell.mapping.GeometryContext
 * @exception java.lang.Exception The exception description.
 */
public MembraneMapping(MembraneMapping membraneMapping, SimulationContext argSimulationContext) {
	super(membraneMapping, argSimulationContext);
	fieldCalculateVoltage = membraneMapping.fieldCalculateVoltage;

	addVetoableChangeListener(this);
}
/**
 * MembraneMapping constructor comment.
 * @param membrane cbit.vcell.model.Membrane
 * @param geoContext cbit.vcell.mapping.GeometryContext
 * @exception java.lang.Exception The exception description.
 */
public MembraneMapping(cbit.vcell.model.Membrane membrane, SimulationContext argSimulationContext) {
	super(membrane, argSimulationContext);
	try {
		setParameters(new StructureMappingParameter[] {
						new StructureMappingParameter(getInitialVoltageName(), new Expression(0.0), ROLE_InitialVoltage,VCUnitDefinition.UNIT_mV),
						new StructureMappingParameter(DefaultNames[ROLE_SpecificCapacitance], new Expression(1.0), ROLE_SpecificCapacitance,VCUnitDefinition.UNIT_pF_per_um2),
						new StructureMappingParameter(DefaultNames[ROLE_SurfaceToVolumeRatio], /*new Expression(0)*/ null, ROLE_SurfaceToVolumeRatio,VCUnitDefinition.UNIT_per_um),
						new StructureMappingParameter(DefaultNames[ROLE_VolumeFraction], /*new Expression(0)*/ null, ROLE_VolumeFraction,VCUnitDefinition.UNIT_DIMENSIONLESS),
						new StructureMappingParameter(DefaultNames[ROLE_Size], /*new Expression(0)*/ null, ROLE_Size,VCUnitDefinition.UNIT_um2)
		});
	}catch (java.beans.PropertyVetoException e){
		e.printStackTrace(System.out);
		throw new RuntimeException(e.getMessage());
	}
	addVetoableChangeListener(this);
}
/**
 * The addPropertyChangeListener method was generated to support the propertyChange field.
 */
public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
	getPropertyChange().addPropertyChangeListener(listener);
}
/**
 * The addPropertyChangeListener method was generated to support the propertyChange field.
 */
public synchronized void addPropertyChangeListener(String propertyName, java.beans.PropertyChangeListener listener) {
	getPropertyChange().addPropertyChangeListener(propertyName, listener);
}
/**
 * This method was created in VisualAge.
 * @return boolean
 * @param obj java.lang.Object
 */
public boolean compareEqual(Matchable obj) {

	MembraneMapping mm = null;
	if (!(obj instanceof MembraneMapping)){
		return false;
	}
	mm = (MembraneMapping)obj;

	if (!compareEqual0(mm)){
		return false;
	}

	//if (!Compare.isEqual(surfaceToVolumeExpression,mm.surfaceToVolumeExpression)){
		//return false;
	//}

	//if (!Compare.isEqual(volumeFractionExpression,mm.volumeFractionExpression)){
		//return false;
	//}
	
	//if (!Compare.isEqual(fieldInitialVoltage,mm.fieldInitialVoltage)){
		//return false;
	//}

	//if (fieldSpecificCapacitance_pF_squm != mm.fieldSpecificCapacitance_pF_squm){
		//return false;
	//}

	if (fieldCalculateVoltage != mm.fieldCalculateVoltage){
		return false;
	}
	return true;
}
/**
 * The firePropertyChange method was generated to support the propertyChange field.
 */
public void firePropertyChange(java.beans.PropertyChangeEvent evt) {
	getPropertyChange().firePropertyChange(evt);
}
/**
 * The firePropertyChange method was generated to support the propertyChange field.
 */
public void firePropertyChange(String propertyName, int oldValue, int newValue) {
	getPropertyChange().firePropertyChange(propertyName, oldValue, newValue);
}
/**
 * The firePropertyChange method was generated to support the propertyChange field.
 */
public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
	getPropertyChange().firePropertyChange(propertyName, oldValue, newValue);
}
/**
 * The firePropertyChange method was generated to support the propertyChange field.
 */
public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
	getPropertyChange().firePropertyChange(propertyName, oldValue, newValue);
}
/**
 * Gets the calculateVoltage property (boolean) value.
 * @return The calculateVoltage property value.
 * @see #setCalculateVoltage
 */
public boolean getCalculateVoltage() {
	return fieldCalculateVoltage;
}
/**
 * This method was created by a SmartGuide.
 * @return double
 */
private String getInitialVoltageName() {
	return TokenMangler.fixToken(getMembrane().getMembraneVoltage().getName())+"_init";
}
/**
 * Gets the initialVoltage property (cbit.vcell.parser.Expression) value.
 * @return The initialVoltage property value.
 * @see #setInitialVoltage
 */
public StructureMappingParameter getInitialVoltageParameter() {
	return getParameterFromRole(ROLE_InitialVoltage);
}
/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.model.Membrane
 */
public Membrane getMembrane() {
	return (Membrane)getStructure();
}
/**
 * Accessor for the propertyChange field.
 */
protected java.beans.PropertyChangeSupport getPropertyChange() {
	if (propertyChange == null) {
		propertyChange = new java.beans.PropertyChangeSupport(this);
	};
	return propertyChange;
}
/**
 * This method was created in VisualAge.
 * @return boolean
 */
public boolean getResolved(SimulationContext simulationContext) {
	if (simulationContext!=null && simulationContext.getGeometryContext()!=null && getMembrane()!=null && getMembrane().getInsideFeature()!=null){
		FeatureMapping sm = (FeatureMapping)simulationContext.getGeometryContext().getStructureMapping(getMembrane().getInsideFeature());
		if (sm!=null){
			return sm.getResolved();
		}
	}
	return false;
}
/**
 * Gets the specificCapacitance_pF_squm property (double) value.
 * @return The specificCapacitance_pF_squm property value.
 * @see #setSpecificCapacitance_pF_squm
 */
public StructureMappingParameter getSpecificCapacitanceParameter() {
	return getParameterFromRole(ROLE_SpecificCapacitance);
}
/**
 * This method was created by a SmartGuide.
 * @return double
 */
public StructureMappingParameter getSurfaceToVolumeParameter() {
	return getParameterFromRole(ROLE_SurfaceToVolumeRatio);
}
/**
 * This method was created in VisualAge.
 * @return cbit.vcell.parser.Expression
 */
public Expression getTotalVolumeCorrection(SimulationContext simulationContext) throws ExpressionException {
	if (simulationContext==null){
		throw new RuntimeException("MembraneMapping.getTotalVolumeCorrection(): simulationContext is null");
	}
	
	if (getResolved(simulationContext)) {
		return new Expression(1.0);
//		throw new Exception("volume correction for a resolved membrane is meaningless, but maybe a 1.0 would be harmless");
	} else {
		//
		// volume correction with respect to entire area enclosed by parent's membrane
		//
		//   SurfToVolRatio * VolFract * KMOLE 
		//
		Expression exp = new Expression(simulationContext.getNameScope().getSymbolName(getSurfaceToVolumeParameter())+
			                            "*"+simulationContext.getNameScope().getSymbolName(getVolumeFractionParameter())+
			                            "*"+ReservedSymbol.KMOLE.getName());
		//
		// for all parent volumes (that have distributed membranes), multiply each volume fraction
		//
		Membrane membrane = getMembrane().getOutsideFeature().getMembrane();
		while (membrane!=null){
			MembraneMapping memMapping = (MembraneMapping)simulationContext.getGeometryContext().getStructureMapping(membrane);
			if (memMapping.getResolved(simulationContext)==false){
				exp = Expression.mult(exp,new Expression(simulationContext.getNameScope().getSymbolName(memMapping.getVolumeFractionParameter())));
			}else{
				break;
			}
			membrane = membrane.getOutsideFeature().getMembrane();
		}
		return exp;
	}
}
/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public String getVCML() throws Exception {
	StringBuffer buffer = new StringBuffer();
	buffer.append("\t"+VCMODL.MembraneMapping+" "+getMembrane().getName()+" {\n");
//	buffer.append("\t\t"+VCML.SubVolume+" "+getSubVolume().getName()+"\n");
//	buffer.append("\t\t"+VCMODL.Resolved+" "+isResolved()+"\n");
	//
	// write FluxCorrections
	//
	buffer.append("\t\t"+VCMODL.CalculateVoltage+" "+getCalculateVoltage()+"\n");
	buffer.append("\t\t"+VCMODL.SpecificCapacitance+" "+getSpecificCapacitanceParameter()+"\n");
	buffer.append("\t\t"+VCMODL.InitialVoltage+" "+getInitialVoltageParameter()+";\n");
	buffer.append("\t\t"+VCMODL.SurfaceToVolume+" "+getSurfaceToVolumeParameter()+";\n");
	buffer.append("\t\t"+VCMODL.VolumeFraction+" "+getVolumeFractionParameter()+";\n");
	buffer.append("\t}\n");
	return buffer.toString();		
}
/**
 * This method was created by a SmartGuide.
 * @return double
 */
public StructureMappingParameter getVolumeFractionParameter() {
	return getParameterFromRole(ROLE_VolumeFraction);
}
/**
 * The hasListeners method was generated to support the propertyChange field.
 */
public synchronized boolean hasListeners(String propertyName) {
	return getPropertyChange().hasListeners(propertyName);
}
/**
 * Insert the method's description here.
 * Creation date: (2/19/2002 1:08:12 PM)
 */
public void refreshDependencies() {
	super.refreshDependencies();
	addVetoableChangeListener(this);
}
/**
 * The removePropertyChangeListener method was generated to support the propertyChange field.
 */
public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
	getPropertyChange().removePropertyChangeListener(listener);
}
/**
 * The removePropertyChangeListener method was generated to support the propertyChange field.
 */
public synchronized void removePropertyChangeListener(String propertyName, java.beans.PropertyChangeListener listener) {
	getPropertyChange().removePropertyChangeListener(propertyName, listener);
}
/**
 * Sets the calculateVoltage property (boolean) value.
 * @param calculateVoltage The new value for the property.
 * @see #getCalculateVoltage
 */
public void setCalculateVoltage(boolean calculateVoltage) {
	boolean oldValue = fieldCalculateVoltage;
	fieldCalculateVoltage = calculateVoltage;
	firePropertyChange("calculateVoltage", new Boolean(oldValue), new Boolean(calculateVoltage));
}
/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public String toString() {
	return getClass().getName()+"@"+Integer.toHexString(hashCode())+" "+getMembrane().getName();
}
	/**
	 * This method gets called when a constrained property is changed.
	 *
	 * @param     evt a <code>PropertyChangeEvent</code> object describing the
	 *   	      event source and the property that has changed.
	 * @exception PropertyVetoException if the recipient wishes the property
	 *              change to be rolled back.
	 */
public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
	if (evt.getPropertyName().equals("specificCapacitance_pF_squm")){
		Double newValue = (Double)evt.getNewValue();
		if (newValue.doubleValue()<0.0){
			throw new java.beans.PropertyVetoException("specificCapacitance (pF/squm) cannot be negative, value="+newValue.doubleValue(),evt);
		}
	}
	if (evt.getPropertyName().equals("initialVoltage")){
		if (evt.getNewValue()==null){
			throw new java.beans.PropertyVetoException("initialVoltage cannot be 'null'",evt);
		}
	}
}
}
