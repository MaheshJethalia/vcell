package cbit.vcell.client.desktop.biomodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.vcell.sybil.models.miriam.MIRIAMQualifier;

import cbit.vcell.biomodel.BioModel;
import cbit.vcell.biomodel.meta.MiriamManager.MiriamRefGroup;
import cbit.vcell.biomodel.meta.MiriamManager.MiriamResource;
import cbit.vcell.biomodel.meta.VCMetaData.AnnotationEvent;
import cbit.vcell.biomodel.meta.VCMetaData.AnnotationEventListener;
import cbit.vcell.client.GuiConstants;
import cbit.vcell.client.desktop.biomodel.BioModelEditor.SelectionEvent;
import cbit.vcell.data.DataSymbol;
import cbit.vcell.desktop.BioModelNode;
import cbit.vcell.document.SimulationOwner;
import cbit.vcell.mapping.BioEvent;
import cbit.vcell.mapping.SimulationContext;
import cbit.vcell.math.AnnotatedFunction;
import cbit.vcell.math.OutputFunctionContext;
import cbit.vcell.model.Feature;
import cbit.vcell.model.Membrane;
import cbit.vcell.model.Model;
import cbit.vcell.model.Model.ModelParameter;
import cbit.vcell.model.ReactionParticipant;
import cbit.vcell.model.ReactionStep;
import cbit.vcell.model.SpeciesContext;
import cbit.vcell.model.Structure;
import cbit.vcell.modelopt.AnalysisTask;
import cbit.vcell.solver.Simulation;
import cbit.vcell.xml.gui.MiriamTreeModel;

@SuppressWarnings("serial")
public class BioModelEditorTreeModel extends DefaultTreeModel
	implements java.beans.PropertyChangeListener, TreeExpansionListener, AnnotationEventListener, TreeSelectionListener {

	private static final String PROPERTY_NAME_BIO_MODEL = "bioModel";
	private BioModel bioModel = null;
	private BioModelNode rootNode = null;
	private JTree ownerTree = null;
	private transient java.beans.PropertyChangeSupport propertyChange;
	private BioModelNode selectedBioModelNode = null;
	
	public static class BioModelEditorTreeFolderNode {
		private BioModelEditorTreeFolderClass folderClass;
		private String name;
		private boolean bFirstLevel;
		boolean bSupported = true;
		
		public BioModelEditorTreeFolderNode(BioModelEditorTreeFolderClass c, String name) {
			this(c, name, false);
		}
		public BioModelEditorTreeFolderNode(BioModelEditorTreeFolderClass c, String name, boolean bFirstLevel) {
			this.folderClass = c;
			this.name = name;
			this.bFirstLevel = bFirstLevel;
		}		
		public boolean isSupported() {
			return bSupported;
		}		
		public void setSupported(boolean bSupported) {
			this.bSupported = bSupported;
		}
		public final String getName() {
			return name;
		}
		public final BioModelEditorTreeFolderClass getFolderClass() {
			return folderClass;
		}
		public boolean isFirstLevel() {
			return bFirstLevel;
		}
	}
	
	public enum BioModelEditorTreeFolderClass {
		MODEL_NODE,	
		APPLICATTIONS_NODE,		

		STRUCTURES_NODE,
		SPECIES_NODE,		
		REACTIONS_NODE,
		GLOBAL_PARAMETER_NODE,
		
		MATHEMATICS_NODE,
		ANALYSIS_NODE,
		
		GEOMETRY_NODE,
		STRUCTURE_MAPPING_NODE,
		INITIAL_CONDITIONS_NODE,		
		APP_REACTIONS_NODE,
		EVENTS_NODE,
		ELECTRICAL_MAPPING_NODE,
		DATA_SYMBOLS_NODE,
		
		SIMULATIONS_NODE,
		OUTPUT_FUNCTIONS_NODE,
		
		MICROSCOPE_MEASUREMENT_NODE;		
	}
	
	enum ModelNodeID {		
		STRUCTURES_NODE,
		SPECIES_NODE,
		REACTIONS_NODE,
		GLOBAL_PARAMETER_NODE,
	}
	
	enum ApplicationNodeID {		
		SPECIFICATIONS_NODE,
		MATHEMATICS_NODE,
		RUN_SIMULATIONS_NODE,
		ANALYSIS_NODE,
	}
	
	enum SpecificationNodeID {
		GEOMETRY_NODE,
		STRUCTURE_MAPPING_NODE,
		INITIAL_CONDITIONS_NODE,
		APP_REACTIONS_NODE,
		EVENTS_NODE,
		ELECTRICAL_MAPPING_NODE,
		DATA_SYMBOLS_NODE,	
	}
	
	enum RunSimulationsNodeID {
		SIMULATIONS_NODE,
		OUTPUT_FUNCTIONS_NODE,
	}
	
	// first Level
	private BioModelEditorTreeFolderNode bioModelChildFolderNodes[] = {
			new BioModelEditorTreeFolderNode(BioModelEditorTreeFolderClass.MODEL_NODE, "Biological Model", true),
			new BioModelEditorTreeFolderNode(BioModelEditorTreeFolderClass.APPLICATTIONS_NODE, "Applications", true),
		};
	private BioModelNode modelNode = new BioModelNode(bioModelChildFolderNodes[0], true);
	private BioModelNode applicationsNode = new BioModelNode(bioModelChildFolderNodes[1], true);	
	private BioModelNode  bioModelChildNodes[] = {
			modelNode,
			applicationsNode,
	};
	List<BioModelNode> annotationNodes = new ArrayList<BioModelNode>();
	List<BioModelNode> applicationsChildNodes = new ArrayList<BioModelNode>();

	// Model	
	private BioModelEditorTreeFolderNode modelChildFolderNodes[] = {			
			new BioModelEditorTreeFolderNode(BioModelEditorTreeFolderClass.STRUCTURES_NODE, "Structures", true),
			new BioModelEditorTreeFolderNode(BioModelEditorTreeFolderClass.SPECIES_NODE, "Species", true),
			new BioModelEditorTreeFolderNode(BioModelEditorTreeFolderClass.REACTIONS_NODE, "Reactions", true),			
			new BioModelEditorTreeFolderNode(BioModelEditorTreeFolderClass.GLOBAL_PARAMETER_NODE, "Global Parameters", true),			
		};	
	private BioModelNode structuresNode = new BioModelNode(modelChildFolderNodes[0], true); 
	private BioModelNode speciesNode = new BioModelNode(modelChildFolderNodes[1], true); 
	private BioModelNode reactionsNode = new BioModelNode(modelChildFolderNodes[2], true); 
	private BioModelNode globalParametersNode = new BioModelNode(modelChildFolderNodes[3], true); 
	private BioModelNode modelChildNodes[] = new BioModelNode[] {
			structuresNode,
			speciesNode,
			reactionsNode,
			globalParametersNode,
	};
		
	public BioModelEditorTreeModel(JTree tree) {
		super(new BioModelNode("empty",true),true);
		addPropertyChangeListener(this);
		
		rootNode = (BioModelNode)root;
		this.ownerTree = tree;
		for (BioModelNode bioModeNode : bioModelChildNodes) {
			rootNode.add(bioModeNode);
		}
		for (BioModelNode bioModeNode : modelChildNodes) {
			modelNode.add(bioModeNode);
		}
	}
	
	public void setBioModel(BioModel newValue) {
		BioModel oldValue = this.bioModel;
		this.bioModel = newValue;
		firePropertyChange(PROPERTY_NAME_BIO_MODEL, oldValue, newValue);
	}
	
	private void populateRoot() {
		if (bioModel == null){
			return;
		}
		rootNode.setUserObject(bioModel);
		populateAnnotationNode();
		modelNode.setUserObject(bioModel.getModel());
		populateModelNode(modelNode);
		populateApplicationsNode();
		nodeStructureChanged(rootNode);
		ownerTree.expandPath(new TreePath(applicationsNode.getPath()));
		if (selectedBioModelNode == null) {
			ownerTree.setSelectionPath(new TreePath(structuresNode.getPath()));
		} else {
			restoreTreeExpansion();
		}
	}

	private void populateAnnotationNode() {
		for (BioModelNode node : annotationNodes) {
			rootNode.remove(node);
		}
		annotationNodes.clear();
		
		int childIndex = 0;
		BioModelNode newChild = new BioModelNode(bioModel.getVCMetaData(), false);
		rootNode.insert(newChild, childIndex ++);
		annotationNodes.add(newChild);

		Set<MiriamRefGroup> isDescribedByAnnotation = bioModel.getVCMetaData().getMiriamManager().getMiriamRefGroups(bioModel, MIRIAMQualifier.MODEL_isDescribedBy);
		for (MiriamRefGroup refGroup : isDescribedByAnnotation){
			for (MiriamResource miriamResources : refGroup.getMiriamRefs()){
				newChild = new MiriamTreeModel.LinkNode(MIRIAMQualifier.MODEL_isDescribedBy, miriamResources);
				rootNode.insert(newChild, childIndex ++);
				annotationNodes.add(newChild);
			}
		}
		Set<MiriamRefGroup> isAnnotation = bioModel.getVCMetaData().getMiriamManager().getMiriamRefGroups(bioModel, MIRIAMQualifier.MODEL_is);
		for (MiriamRefGroup refGroup : isAnnotation){
			for (MiriamResource miriamResources : refGroup.getMiriamRefs()){
				newChild = new MiriamTreeModel.LinkNode(MIRIAMQualifier.MODEL_is, miriamResources);
				rootNode.insert(newChild, childIndex ++);
				annotationNodes.add(newChild);
			}
		}

	}

	private void populateModelNode(BioModelNode argNode) {
		Model model = bioModel.getModel();		
		if (argNode == modelNode || argNode == modelChildNodes[ModelNodeID.STRUCTURES_NODE.ordinal()]) {
			BioModelNode popNode = modelChildNodes[ModelNodeID.STRUCTURES_NODE.ordinal()];
			popNode.removeAllChildren();
		    Structure[] structures = model.getStructures().clone();
		    if(structures.length > 0) {
		    	Arrays.sort(structures, new Comparator<Structure>() {
					public int compare(Structure o1, Structure o2) {
						if (o1 instanceof Feature && o2 instanceof Membrane) {
							return -1;
						}
						if (o1 instanceof Membrane && o2 instanceof Feature) {
							return 1;
						}
						return o1.getName().compareToIgnoreCase(o2.getName());
					}
				});
		    	for (Structure structure : structures) {
		    		BioModelNode node = new BioModelNode(structure, false);
		    		popNode.add(node);
		    	}
		    }
		}
	    
		if (argNode == modelNode || argNode == modelChildNodes[ModelNodeID.SPECIES_NODE.ordinal()]) {	
			BioModelNode popNode = modelChildNodes[ModelNodeID.SPECIES_NODE.ordinal()];
		    popNode.removeAllChildren();
		    SpeciesContext[] speciesContexts = model.getSpeciesContexts().clone();
		    if(speciesContexts.length > 0) {
		    	Arrays.sort(speciesContexts, new Comparator<SpeciesContext>() {
		    		public int compare(SpeciesContext o1, SpeciesContext o2) {
		    			return o1.getName().compareToIgnoreCase(o2.getName());
		    		}
		    	});
		    	for (SpeciesContext sc : speciesContexts) {
		    		BioModelNode node = new BioModelNode(sc, false);
		    		popNode.add(node);
		    	}
		    }
		}
	    
		if (argNode == modelNode || argNode == modelChildNodes[ModelNodeID.REACTIONS_NODE.ordinal()]) {
			BioModelNode popNode = modelChildNodes[ModelNodeID.REACTIONS_NODE.ordinal()];
			popNode.removeAllChildren();
			ReactionStep[] reactionSteps = model.getReactionSteps().clone();
		    if(reactionSteps.length > 0) {
		    	Arrays.sort(reactionSteps, new Comparator<ReactionStep>() {
					public int compare(ReactionStep o1, ReactionStep o2) {
						return o1.getName().compareToIgnoreCase(o2.getName());
					}
				});
		    	for (ReactionStep rs : reactionSteps) {
		    		BioModelNode node = new BioModelNode(rs, false);;
		    		popNode.add(node);
		    	}
		    }
		}
		
		if (argNode == modelNode || argNode == modelChildNodes[ModelNodeID.GLOBAL_PARAMETER_NODE.ordinal()]) {
			BioModelNode popNode = modelChildNodes[ModelNodeID.GLOBAL_PARAMETER_NODE.ordinal()];
			popNode.removeAllChildren();
		    ModelParameter[] modelParameters = model.getModelParameters().clone();
		    if (modelParameters.length > 0) {
		    	Arrays.sort(modelParameters, new Comparator<ModelParameter>() {
					public int compare(ModelParameter o1, ModelParameter o2) {
						return o1.getName().compareToIgnoreCase(o2.getName());
					}
				});
		    	for (ModelParameter mp : modelParameters) {
		    		BioModelNode node = new BioModelNode(mp, false);
		    		popNode.add(node);
		    	}
		    }
		}
		
		nodeStructureChanged(argNode); 
		restoreTreeExpansion();
	}
	
	private void populateApplicationsNode() {
		for (BioModelNode node : applicationsChildNodes) {
			applicationsNode.remove(node);
		}
		applicationsChildNodes.clear();
		SimulationContext[] simulationContexts = bioModel.getSimulationContexts();
		if (simulationContexts != null && simulationContexts.length > 0) {
			simulationContexts = simulationContexts.clone();
			Arrays.sort(simulationContexts, new Comparator<SimulationContext>() {
				public int compare(SimulationContext o1, SimulationContext o2) {
					return o1.getName().compareToIgnoreCase(o2.getName());
				}
			});
			for (SimulationContext simulationContext : simulationContexts) {
				BioModelNode appNode = new BioModelNode(simulationContext, true);
				applicationsNode.add(appNode);
				applicationsChildNodes.add(appNode);
	
				BioModelNode geometryNode = new BioModelNode(new BioModelEditorTreeFolderNode(BioModelEditorTreeFolderClass.GEOMETRY_NODE, "Geometry"), false);
				BioModelNode structureMappingNode = new BioModelNode(new BioModelEditorTreeFolderNode(BioModelEditorTreeFolderClass.STRUCTURE_MAPPING_NODE, "Structure Mapping"), false);
				BioModelNode initialConditionNode = new BioModelNode(new BioModelEditorTreeFolderNode(BioModelEditorTreeFolderClass.INITIAL_CONDITIONS_NODE, "Initial Conditions"), true); 
				BioModelNode reactionsNode = new BioModelNode(new BioModelEditorTreeFolderNode(BioModelEditorTreeFolderClass.APP_REACTIONS_NODE, "Reactions"), true);
				BioModelNode eventsNode = new BioModelNode(new BioModelEditorTreeFolderNode(BioModelEditorTreeFolderClass.EVENTS_NODE, "Events"), true);
				BioModelNode electricalNode = new BioModelNode(new BioModelEditorTreeFolderNode(BioModelEditorTreeFolderClass.ELECTRICAL_MAPPING_NODE, "Electrical"), false);
				BioModelNode dataSymbolNode = new BioModelNode(new BioModelEditorTreeFolderNode(BioModelEditorTreeFolderClass.DATA_SYMBOLS_NODE, "Data Symbols"), true);
				BioModelNode microscopeMeasurmentNode = new BioModelNode(new BioModelEditorTreeFolderNode(BioModelEditorTreeFolderClass.MICROSCOPE_MEASUREMENT_NODE, "Microscope Measurements"), true);
				BioModelNode mathematicsNode = new BioModelNode(new BioModelEditorTreeFolderNode(BioModelEditorTreeFolderClass.MATHEMATICS_NODE, "View Math"), false); 
				BioModelNode analysisNode = new BioModelNode(new BioModelEditorTreeFolderNode(BioModelEditorTreeFolderClass.ANALYSIS_NODE, "Parameter Estimations"), true); 
				BioModelNode simulationsNode = new BioModelNode(new BioModelEditorTreeFolderNode(BioModelEditorTreeFolderClass.SIMULATIONS_NODE, "Simulations"), true);
				BioModelNode outputFunctionsNode = new BioModelNode(new BioModelEditorTreeFolderNode(BioModelEditorTreeFolderClass.OUTPUT_FUNCTIONS_NODE, "Output Functions"), true);
				
				BioModelNode[] applicationChildNodes = new BioModelNode[] {
						geometryNode,
						structureMappingNode,
						initialConditionNode,
						reactionsNode,
						eventsNode,
						electricalNode,
						dataSymbolNode,
						microscopeMeasurmentNode,
						mathematicsNode,
						analysisNode,
						simulationsNode,
						outputFunctionsNode,
				};
				for (BioModelNode node : applicationChildNodes) {
					appNode.add(node);
				}
				populateApplicationNode(appNode, BioModelEditorTreeFolderClass.INITIAL_CONDITIONS_NODE);
				populateApplicationNode(appNode, BioModelEditorTreeFolderClass.APP_REACTIONS_NODE);
				populateApplicationNode(appNode, BioModelEditorTreeFolderClass.EVENTS_NODE);
				populateApplicationNode(appNode, BioModelEditorTreeFolderClass.DATA_SYMBOLS_NODE);
				populateApplicationNode(appNode, BioModelEditorTreeFolderClass.MICROSCOPE_MEASUREMENT_NODE);
				populateApplicationNode(appNode, BioModelEditorTreeFolderClass.SIMULATIONS_NODE);
				populateApplicationNode(appNode, BioModelEditorTreeFolderClass.OUTPUT_FUNCTIONS_NODE);
				populateApplicationNode(appNode, BioModelEditorTreeFolderClass.ANALYSIS_NODE);
			}
		}
		nodeStructureChanged(applicationsNode);
		restoreTreeExpansion();
	}

	private BioModelNode findApplicationChildNode(BioModelNode appNode, BioModelEditorTreeFolderClass folderClass) {
		for (int i = 0; i < appNode.getChildCount(); i ++) {
			BioModelNode node = (BioModelNode) appNode.getChildAt(i);
			Object userObject = node.getUserObject();
			if (userObject instanceof BioModelEditorTreeFolderNode && ((BioModelEditorTreeFolderNode)userObject).getFolderClass() == folderClass) {
				return node;
			}
			if (node.getAllowsChildren()) {
				BioModelNode node1 = findApplicationChildNode(node, folderClass);
				if (node1 != null) {
					return node1;
				}
			}
		}
		return null;
	}
	
	private void populateApplicationNode(SimulationOwner simulationContext, BioModelEditorTreeFolderClass folderClass) {
		for (BioModelNode node : applicationsChildNodes) {
			Object userObject = node.getUserObject();
			if (userObject instanceof SimulationContext && userObject == simulationContext) {
				populateApplicationNode(node, folderClass);
				break;
			}
		}
	}
	
	private void populateApplicationNode(BioModelNode appNode, BioModelEditorTreeFolderClass folderClass) {
		if (!(appNode.getUserObject() instanceof SimulationContext)) {
			throw new RuntimeException("Application node's user Object must be an instance of SimulationContext");
		}
		SimulationContext simulationContext = (SimulationContext)appNode.getUserObject();
		BioModelNode popNode = findApplicationChildNode(appNode, folderClass);
		popNode.removeAllChildren();
		switch (folderClass) {
		case INITIAL_CONDITIONS_NODE: {
		    SpeciesContext[] speciesContexts = simulationContext.getModel().getSpeciesContexts().clone();
		    if(speciesContexts.length > 0) {
		    	Arrays.sort(speciesContexts, new Comparator<SpeciesContext>() {
					public int compare(SpeciesContext o1, SpeciesContext o2) {
						return o1.getName().compareToIgnoreCase(o2.getName());
					}
				});
		    	for (SpeciesContext sc : speciesContexts) {
		    		BioModelNode node = new BioModelNode(sc, false);
		    		popNode.add(node);
		    	}
		    }
		    break;
		}
		case APP_REACTIONS_NODE: {
		    ReactionStep[] reactionSteps = simulationContext.getModel().getReactionSteps().clone();
		    if(reactionSteps.length != 0) {
		    	Arrays.sort(reactionSteps, new Comparator<ReactionStep>() {
					public int compare(ReactionStep o1, ReactionStep o2) {
						return o1.getName().compareToIgnoreCase(o2.getName());
					}
				});
		    	for (ReactionStep rs : reactionSteps) {
		    		BioModelNode node = new BioModelNode(rs, false);;
		    		popNode.add(node);
		    	}
		    }
		    break;
		}
		case EVENTS_NODE: {
			if ((simulationContext.getGeometry().getDimension() > 0) || simulationContext.isStoch()) {
				BioModelNode parentNode = (BioModelNode) popNode.getParent();
				parentNode.remove(popNode);				
				((BioModelEditorTreeFolderNode)popNode.getUserObject()).setSupported(false);
			} else {
				((BioModelEditorTreeFolderNode)popNode.getUserObject()).setSupported(true);
				if (simulationContext.getBioEvents() != null) {
				    BioEvent[] bioEvents = simulationContext.getBioEvents().clone();
				    if(bioEvents.length != 0) {
				    	Arrays.sort(bioEvents, new Comparator<BioEvent>() {
							public int compare(BioEvent o1, BioEvent o2) {
								return o1.getName().compareToIgnoreCase(o2.getName());
							}
						});
				    	for (BioEvent bevnt : bioEvents) {
				    		BioModelNode node = new BioModelNode(bevnt, false);
				    		popNode.add(node);
				    	}
				    }
				}
			}
			break;
		}
		case DATA_SYMBOLS_NODE: {
		    DataSymbol[] dataSymbol = simulationContext.getDataContext().getDataSymbols().clone();
		    if(dataSymbol.length > 0) {
		    	Arrays.sort(dataSymbol, new Comparator<DataSymbol>() {
					public int compare(DataSymbol o1, DataSymbol o2) {
						return o1.getName().compareToIgnoreCase(o2.getName());
					}
				});
		    	for (DataSymbol sc : dataSymbol) {
		    		BioModelNode node = new BioModelNode(sc, false);
		    		popNode.add(node);
		    	}
		    }
		    break;
		}		
		case SIMULATIONS_NODE: {		
		    Simulation[] simulations = simulationContext.getSimulations().clone();
		    if(simulations.length > 0) {
		    	Arrays.sort(simulations, new Comparator<Simulation>() {
					public int compare(Simulation o1, Simulation o2) {
						return o1.getName().compareToIgnoreCase(o2.getName());
					}
				});
		    	for (Simulation sc : simulations) {
		    		BioModelNode node = new BioModelNode(sc, false);
		    		popNode.add(node);
		    	}
		    }
		    break;
		}			
		case OUTPUT_FUNCTIONS_NODE: {
		    ArrayList<AnnotatedFunction> outputFunctions = new ArrayList<AnnotatedFunction>(simulationContext.getOutputFunctionContext().getOutputFunctionsList());
		    if(outputFunctions.size() != 0) {
		    	Collections.sort(outputFunctions, new Comparator<AnnotatedFunction>() {
					public int compare(AnnotatedFunction o1, AnnotatedFunction o2) {
						return o1.getName().compareToIgnoreCase(o2.getName());
					}
				});
		    	for (AnnotatedFunction outputFunction : outputFunctions) {
		    		BioModelNode node = new BioModelNode(outputFunction, false);
		    		popNode.add(node);
		    	}
		    }
		    break;
		}
		case ANALYSIS_NODE: {
			AnalysisTask[] analysisTasks = simulationContext.getAnalysisTasks();
			if (analysisTasks != null && analysisTasks.length > 0) {
				analysisTasks = analysisTasks.clone();
				Arrays.sort(analysisTasks, new Comparator<AnalysisTask>() {
					public int compare(AnalysisTask o1, AnalysisTask o2) {
						return o1.getName().compareToIgnoreCase(o2.getName());
					}
				});
				for (AnalysisTask analysisTask : analysisTasks) {
					BioModelNode node = new BioModelNode(analysisTask, false);
					popNode.add(node);
				}
			}
			break;
		}
		}
		nodeStructureChanged(popNode);
	}
	
	private void refreshListeners(BioModel oldValue, BioModel newValue){
//		getSimulationContext().getDataContext().removePropertyChangeListener(this);
//		getSimulationContext().getDataContext().addPropertyChangeListener(this);
		if (oldValue != null) {	
			oldValue.removePropertyChangeListener(this);
			oldValue.getVCMetaData().removeAnnotationEventListener(this);
			oldValue.getModel().removePropertyChangeListener(this);
			for (Structure structure : oldValue.getModel().getStructures()){
				structure.removePropertyChangeListener(this);
			}
			for (SpeciesContext speciesContext : oldValue.getModel().getSpeciesContexts()) {
				speciesContext.removePropertyChangeListener(this);
			}
			for (ReactionStep reactionStep : oldValue.getModel().getReactionSteps()){
				reactionStep.removePropertyChangeListener(this);
				reactionStep.getKinetics().removePropertyChangeListener(this);
				for (ReactionParticipant reactionParticipant : reactionStep.getReactionParticipants()) {
					reactionParticipant.removePropertyChangeListener(this);
				}
			}			
			for (ModelParameter modelParameter : oldValue.getModel().getModelParameters()) {
				modelParameter.removePropertyChangeListener(this);
			}
			for (SimulationContext simulationContext : oldValue.getSimulationContexts()) {
				simulationContext.removePropertyChangeListener(this);
				simulationContext.getOutputFunctionContext().removePropertyChangeListener(this);
			}
		}
		if (newValue != null) {
			newValue.addPropertyChangeListener(this);
			newValue.getVCMetaData().addAnnotationEventListener(this);
			newValue.getModel().addPropertyChangeListener(this);
			for (Structure structure : newValue.getModel().getStructures()){
				structure.addPropertyChangeListener(this);
			}
			for (SpeciesContext speciesContext : newValue.getModel().getSpeciesContexts()) {
				speciesContext.addPropertyChangeListener(this);
			}
			for (ReactionStep reactionStep : newValue.getModel().getReactionSteps()){
				reactionStep.getKinetics().addPropertyChangeListener(this);
				reactionStep.addPropertyChangeListener(this);
				for (ReactionParticipant reactionParticipant : reactionStep.getReactionParticipants()) {
					reactionParticipant.addPropertyChangeListener(this);
				}
			}	
			for (ModelParameter modelParameter : newValue.getModel().getModelParameters()) {
				modelParameter.addPropertyChangeListener(this);
			}
			for (SimulationContext simulationContext : newValue.getSimulationContexts()) {
				simulationContext.addPropertyChangeListener(this);
				simulationContext.getOutputFunctionContext().addPropertyChangeListener(this);
			}
		}
	}
	
	public void propertyChange(java.beans.PropertyChangeEvent evt) {
		try {
			Object source = evt.getSource();
			String propertyName = evt.getPropertyName();
			
			if (source == this && propertyName.equals(PROPERTY_NAME_BIO_MODEL)) {
				populateRoot();
			    refreshListeners((BioModel) evt.getOldValue(), (BioModel) evt.getNewValue());
			} else if (propertyName.equals("name")){
				nodeChanged(rootNode);
			} else if (source == bioModel) {
				if (propertyName.equals(BioModel.PROPERTY_NAME_SIMULATION_CONTEXTS)) {
					populateApplicationsNode();
				}
			} else if (source == bioModel.getModel()) {
				if (propertyName.equals(Model.PROPERTY_NAME_SPECIES_CONTEXTS)) {
					populateModelNode(modelChildNodes[ModelNodeID.SPECIES_NODE.ordinal()]);
					SpeciesContext oldValue[] = (SpeciesContext[])evt.getOldValue();
					if (oldValue != null){
						for (SpeciesContext sc : oldValue){
							sc.removePropertyChangeListener(this);
						}
					}
					SpeciesContext newValue[] = (SpeciesContext[])evt.getNewValue();
					if (newValue != null){
						for (SpeciesContext sc : newValue){
							sc.addPropertyChangeListener(this);
						}
					}
				} else if (propertyName.equals(Model.PROPERTY_NAME_STRUCTURES)) {
					populateModelNode(modelChildNodes[ModelNodeID.STRUCTURES_NODE.ordinal()]);
					Structure oldValue[] = (Structure[])evt.getOldValue();
					if (oldValue != null){
						for (Structure s : oldValue){
							s.removePropertyChangeListener(this);
						}
					}
					Structure newValue[] = (Structure[])evt.getNewValue();
					if (newValue != null){
						for (Structure s : newValue){
							s.addPropertyChangeListener(this);
						}
					}
				} else if (propertyName.equals(Model.PROPERTY_NAME_REACTION_STEPS)) {
					populateModelNode(modelChildNodes[ModelNodeID.REACTIONS_NODE.ordinal()]);
					ReactionStep oldValue[] = (ReactionStep[])evt.getOldValue();
					if (oldValue != null){
						for (ReactionStep rs : oldValue){
							rs.removePropertyChangeListener(this);
						}
					}
					ReactionStep newValue[] = (ReactionStep[])evt.getNewValue();
					if (newValue != null){
						for (ReactionStep rs : newValue){
							rs.addPropertyChangeListener(this);
						}
					}
				} else if (propertyName.equals(Model.PROPERTY_NAME_MODEL_PARAMETERS)) {
					populateModelNode(modelChildNodes[ModelNodeID.GLOBAL_PARAMETER_NODE.ordinal()]);
					ModelParameter oldValue[] = (ModelParameter[])evt.getOldValue();
					if (oldValue != null){
						for (ModelParameter rs : oldValue){
							rs.removePropertyChangeListener(this);
						}
					}
					ModelParameter newValue[] = (ModelParameter[])evt.getNewValue();
					if (newValue != null){
						for (ModelParameter rs : newValue){
							rs.addPropertyChangeListener(this);
						}
					}
				}
			} else if (source instanceof SimulationContext) {
				if (propertyName.equals(GuiConstants.PROPERTY_NAME_SIMULATIONS)) {
					populateApplicationNode((SimulationContext)source, BioModelEditorTreeFolderClass.SIMULATIONS_NODE);
				} else if (propertyName.equals(SimulationContext.PROPERTY_NAME_BIOEVENTS)) {
						populateApplicationNode((SimulationContext)source, BioModelEditorTreeFolderClass.EVENTS_NODE);
				} else if (propertyName.equals(SimulationContext.PROPERTY_NAME_ANALYSIS_TASKS)) {
					populateApplicationNode((SimulationContext)source, BioModelEditorTreeFolderClass.ANALYSIS_NODE);
				}
			} else if (source instanceof OutputFunctionContext) {
				populateApplicationNode(((OutputFunctionContext)source).getSimulationOwner(), BioModelEditorTreeFolderClass.OUTPUT_FUNCTIONS_NODE);
			}
		} catch (Exception e){
			e.printStackTrace(System.out);
		}
	}

	public void treeCollapsed(TreeExpansionEvent e) {
//		if (e.getSource() == ownerTree) {
//			TreePath path = e.getPath();
//			BioModelNode lastComp = (BioModelNode) path.getLastPathComponent();
//			expandedObject.remove(lastComp);
//		}
	}
	
	public void treeExpanded(TreeExpansionEvent e) {
//		if (e.getSource() == ownerTree) {
//			TreePath path = e.getPath();
//			BioModelNode lastComp = (BioModelNode) path.getLastPathComponent();
//			expandedObject.add(lastComp);
//		}
	}
	
	public void restoreTreeExpansion() {
		if (selectedBioModelNode != null) {
			while (true) {
				if (getIndexOfChild(rootNode, selectedBioModelNode) > 0) {
					break;
				}
				selectedBioModelNode = (BioModelNode) selectedBioModelNode.getParent();
			}
			ownerTree.setSelectionPath(new TreePath(selectedBioModelNode.getPath()));
		}
	}
	
	public void select(SelectionEvent newValue) {
		if (newValue == null || newValue.getSelectedObject() == null) {
			return;
		}
		BioModelNode nodeToSearch = null;
		if (newValue.getSelectedContainer() != null) {
			nodeToSearch = rootNode.findNodeByUserObject(newValue.getSelectedContainer());			
		}
		if (nodeToSearch == null) {
			nodeToSearch = rootNode;
		}
		BioModelNode leaf = nodeToSearch.findNodeByUserObject(newValue.getSelectedObject());
		TreePath path = new TreePath(leaf.getPath());
		if (!ownerTree.isVisible(path)) {
			path = path.getParentPath();
		}
		if (path != null && !ownerTree.isPathSelected(path)) {
			ownerTree.setSelectionPath(path);
			ownerTree.scrollPathToVisible(path);
		}
	}

	public void annotationChanged(AnnotationEvent annotationEvent) {
		nodeChanged(rootNode);
	}

	public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
		getPropertyChange().addPropertyChangeListener(listener);
	}

	public void firePropertyChange(java.lang.String propertyName, java.lang.Object oldValue, java.lang.Object newValue) {
		getPropertyChange().firePropertyChange(propertyName, oldValue, newValue);
	}

	public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
		getPropertyChange().removePropertyChangeListener(listener);
	}
	
	private java.beans.PropertyChangeSupport getPropertyChange() {
		if (propertyChange == null) {
			propertyChange = new java.beans.PropertyChangeSupport(this);
		};
		return propertyChange;
	}
	
	public void valueChanged(javax.swing.event.TreeSelectionEvent e) {
		if (e.getSource() == ownerTree) {
			try {
				Object node = ownerTree.getLastSelectedPathComponent();;
				if (node == null || !(node instanceof BioModelNode)) {
					restoreTreeExpansion();
				} else {
					selectedBioModelNode = (BioModelNode) node;
				}				
			} catch (Exception ex){
				ex.printStackTrace(System.out);
			}
		}
	}
}
