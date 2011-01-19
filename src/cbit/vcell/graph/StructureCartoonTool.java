package cbit.vcell.graph;

/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
 �*/
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JFileChooser;
import org.vcell.util.BeanUtils;
import org.vcell.util.SimpleFilenameFilter;
import org.vcell.util.gui.DialogUtils;
import org.vcell.util.gui.SimpleTransferable;
import org.vcell.util.gui.VCFileChooser;

import cbit.gui.graph.GraphModel;
import cbit.gui.graph.Shape;
import cbit.gui.graph.actions.CartoonToolEditActions;
import cbit.gui.graph.actions.CartoonToolMiscActions;
import cbit.gui.graph.actions.CartoonToolSaveAsImageActions;
import cbit.vcell.client.DocumentWindowManager;
import cbit.vcell.client.UserMessage;
import cbit.vcell.client.server.ClientServerManager;
import cbit.vcell.client.server.UserPreferences;
import cbit.vcell.desktop.VCellTransferable;
import cbit.vcell.graph.structures.AllStructureSuite;
import cbit.vcell.graph.structures.MembraneStructureSuite;
import cbit.vcell.graph.structures.SingleStructureSuite;
import cbit.vcell.graph.structures.StructureSuite;
import cbit.vcell.model.Feature;
import cbit.vcell.model.Membrane;
import cbit.vcell.model.Model;
import cbit.vcell.model.ReactionStep;
import cbit.vcell.model.Species;
import cbit.vcell.model.SpeciesContext;
import cbit.vcell.model.Structure;
import cbit.vcell.model.gui.ModelParametersDialog;
import cbit.vcell.model.gui.ReactionCartoonEditorDialog;
import cbit.vcell.publish.ITextWriter;

/**
 * This class was generated by a SmartGuide.
 * 
 */
public class StructureCartoonTool extends BioCartoonTool implements PropertyChangeListener {
	//
	private StructureCartoon structureCartoon = null;
	//
	// public boolean bMoving = false;
	// public Shape movingShape = null;
	// private Point movingPointWorld = null;
	// private Point movingOffsetWorld = null;
	private Mode mode = null;
	private Hashtable<String, ReactionCartoonEditorDialog> reactionEditorHash = 
		new Hashtable<String, ReactionCartoonEditorDialog>();
	private ModelParametersDialog modelParametersDialog = null;

	/**
	 * @param canvas
	 *            cbit.vcell.graph.CartoonCanvas
	 * @param cartoon
	 *            cbit.vcell.graph.StructureCartoon
	 * @param buttonGroup
	 *            cbit.gui.ButtonGroupCivilized
	 */
	public StructureCartoonTool() {
		super();
	}

	/**
	 * Insert the method's description here. Creation date: (6/21/2005 5:41:56
	 * PM)
	 * 
	 * @param bDispose
	 *            boolean
	 */
	private void disposeReactionCartoonEditorDialog(
			ReactionCartoonEditorDialog rced) {

		DocumentWindowManager.close(rced, getJDesktopPane());
		// rced.dispose();

		//
		// The following added to force ReactionCartoon to cleanup its listeners
		// otherwise
		// 1. ReactionCartoon could not be garbage collected. Many "phantom"
		// copies would accumulate from showing ReactionCartoonEditorPanel
		// 2. Erroneous (deleted,renamed) object references in "phantom"
		// ReactionCartoons would interact with
		// listener callbacks and throw exceptions.
		//
		rced.cleanupOnClose();
	}

	/**
	 * Insert the method's description here. Creation date: (1/31/2003 4:57:53
	 * PM)
	 * 
	 * @param e
	 *            java.lang.Exception
	 */
	private void generateErrorDialog(Exception e, int x, int y) {

		System.out.println("CartoonTool.mouseClicked: uncaught exception");
		e.printStackTrace(System.out);
		Point canvasLoc = getGraphPane().getLocationOnScreen();
		canvasLoc.x += x;
		canvasLoc.y += y;
		DialogUtils.showErrorDialog(getGraphPane(), e.getMessage(), e);
	}

	/**
	 * Insert the method's description here. Creation date: (9/9/2002 10:26:00
	 * AM)
	 * 
	 * @return cbit.vcell.graph.GraphModel
	 */
	@Override
	public GraphModel getGraphModel() {
		return structureCartoon;
	}

	/**
	 * Insert the method's description here. Creation date: (9/9/2002 10:26:00
	 * AM)
	 * 
	 * @return cbit.vcell.graph.GraphModel
	 */
	public StructureCartoon getStructureCartoon() {
		return (StructureCartoon) getGraphModel();
	}

	/**
	 * Insert the method's description here. Creation date: (9/17/2002 3:56:54
	 * PM)
	 * 
	 * @param shape
	 *            cbit.vcell.graph.Shape
	 * @param menuAction
	 *            java.lang.String
	 */
	@Override
	protected void menuAction(Shape shape, String menuAction) {
		//
		if (shape == null) {
			return;
		}
		//
		if (menuAction.equals(CartoonToolEditActions.Copy.MENU_ACTION)) {
			if (shape instanceof SpeciesContextShape) {
				Species species = ((SpeciesContextShape) shape).getSpeciesContext().getSpecies();
				VCellTransferable.sendToClipboard(species);
			}
		} else if (menuAction.equals(CartoonToolEditActions.Paste.MENU_ACTION)
				|| menuAction.equals(CartoonToolEditActions.PasteNew.MENU_ACTION)) {
			if (shape instanceof StructureShape) {
				Species species = (Species) SimpleTransferable
				.getFromClipboard(VCellTransferable.SPECIES_FLAVOR);
				IdentityHashMap<Species, Species> speciesHash = new IdentityHashMap<Species, Species>();
				if (species != null) {
					boolean bPasteNew = menuAction.equals(CartoonToolEditActions.PasteNew.MENU_ACTION);
					pasteSpecies(getGraphPane(), species, getStructureCartoon().getModel(), 
							((StructureShape) shape).getStructure(), bPasteNew, speciesHash, null);
				}
			}
		} else if (menuAction.equals(CartoonToolMiscActions.Properties.MENU_ACTION)) {
			if (shape instanceof FeatureShape) {
				//
				// showFeaturePropertyDialog is invoked in two modes:
				//
				// 1) parent!=null and child==null
				// upon ok, it adds a new feature to the supplied parent.
				//
				// 2) parent==null and child!=null
				// upon ok, edits the feature name
				//
//				showFeaturePropertiesDialog(getGraphPane(),
//						(getStructureCartoon().getModel() == null ? null
//								: getStructureCartoon().getModel()), null,
//								((FeatureShape) shape).getFeature());
			} else if (shape instanceof MembraneShape) {
//				showMembranePropertiesDialog(getGraphPane(),
//						((MembraneShape) shape).getMembrane());
			} else if (shape instanceof SpeciesContextShape) {
//				showEditSpeciesDialog(getGraphPane(), getStructureCartoon().getModel(), 
//						((SpeciesContextShape) shape).getSpeciesContext());
			}
		} else if (menuAction.equals(CartoonToolMiscActions.AddGlobalParameter.MENU_ACTION)) {
			if (shape instanceof FeatureShape || shape instanceof MembraneShape) {
				Point locationOnScreen = shape.getSpaceManager().getAbsLoc();
				Point graphPaneLocation = getGraphPane().getLocationOnScreen();
				locationOnScreen.translate(graphPaneLocation.x,
						graphPaneLocation.y);
				showCreateGlobalParamDialog(getGraphPane(),
						getStructureCartoon().getModel(), locationOnScreen);
			}

		} else if (menuAction.equals(CartoonToolMiscActions.AddSpecies.MENU_ACTION)) {
			if (shape instanceof StructureShape) {
				SpeciesContext speciesContext = getStructureCartoon().getModel().createSpeciesContext(((StructureShape) shape).getStructure());
				getGraphModel().select(speciesContext);
//				showCreateSpeciesContextDialog(getGraphPane(),
//						getStructureCartoon().getModel(),
//						((StructureShape) shape).getStructure(), null);
			}

		} else if (menuAction.equals(CartoonToolMiscActions.AddFeature.MENU_ACTION)) {
			try {
				if (shape instanceof FeatureShape) {
					final Feature feature = getStructureCartoon().getModel().createFeature(((FeatureShape) shape).getFeature());
					getGraphModel().select(feature);
//					showFeaturePropertiesDialog(getGraphPane(),
//							(getStructureCartoon().getModel() == null ? null
//									: getStructureCartoon().getModel()),
//									((FeatureShape) shape).getFeature(), null);
				}
			} catch (Exception e) {
				generateErrorDialog(e, 0, 0);
			}

		} else if (menuAction.equals(CartoonToolEditActions.Delete.MENU_ACTION)
				|| menuAction.equals(CartoonToolEditActions.Cut.MENU_ACTION)) {
			try {
				if (shape instanceof FeatureShape
						&& menuAction.equals(CartoonToolEditActions.Delete.MENU_ACTION)) {
					getStructureCartoon().getModel().removeFeature(
							((FeatureShape) shape).getFeature());
				} else if (shape instanceof SpeciesContextShape) {
					getStructureCartoon().getModel().removeSpeciesContext(
							((SpeciesContextShape) shape).getSpeciesContext());
					if (menuAction.equals(CartoonToolEditActions.Cut.MENU_ACTION)) {
						VCellTransferable.sendToClipboard(((SpeciesContextShape) shape).getSpeciesContext().getSpecies());
					}
				}
			} catch (Throwable e) {
				DialogUtils.showErrorDialog(getGraphPane(), e.getMessage(), e);
			}

		} else if (menuAction.equals(CartoonToolMiscActions.Reactions.MENU_ACTION)) {
			if (shape instanceof StructureShape) {
				showReactionCartoonEditorPanel((StructureShape) shape);
			}

		} else if (menuAction.equals(CartoonToolMiscActions.ReactionsSlices.MENU_ACTION)) {
			if (shape instanceof StructureShape) {
				showReactionSlicesCartoonEditorPanel((StructureShape) shape);
			}

		} else if (menuAction.equals(CartoonToolSaveAsImageActions.HighRes.MENU_ACTION)
				|| menuAction.equals(CartoonToolSaveAsImageActions.MedRes.MENU_ACTION)
				|| menuAction.equals(CartoonToolSaveAsImageActions.LowRes.MENU_ACTION)) {
			try {
				String resType = null;
				if (menuAction.equals(CartoonToolSaveAsImageActions.HighRes.MENU_ACTION)) {
					resType = ITextWriter.HIGH_RESOLUTION;
				} else if (menuAction.equals(CartoonToolSaveAsImageActions.MedRes.MENU_ACTION)) {
					resType = ITextWriter.MEDIUM_RESOLUTION;
				} else if (menuAction.equals(CartoonToolSaveAsImageActions.LowRes.MENU_ACTION)) {
					resType = ITextWriter.LOW_RESOLUTION;
				}
				if (shape instanceof StructureShape) {
					showSaveStructureImageDialog(((StructureShape) shape).getModel(), resType);
				}
			} catch (Exception e) {
				DialogUtils.showErrorDialog(getGraphPane(), e.getMessage(), e);
			}
		} else if (menuAction.equals(CartoonToolEditActions.Move.MENU_ACTION)) {
			if (shape instanceof FeatureShape) {
				showMoveDialog((FeatureShape) shape);
			}
		} else {
			//
			// default action is to ignore
			//
			System.out.println("unsupported menu action '" + menuAction
					+ "' on shape '" + shape + "'");
		}

	}

	@Override
	public void mouseClicked(java.awt.event.MouseEvent event) {
		//
		if (getStructureCartoon() == null) {
			return;
		}
		//
		try {
			// Point worldPoint = screenToWorld(event.getPoint());
			// Shape pickedShape = getStructureCartoon().pickWorld(worldPoint);
			//
			// if right mouse button, then do popup menu
			//
			if ((event.getModifiers() & (InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK)) != 0) {
				return;
			}
			switch (mode) {
			case SELECT: {
				if (event.getClickCount() == 2) {
					Shape selectedShape = getStructureCartoon().getSelectedShape();
					if (selectedShape != null) {
						menuAction(selectedShape, CartoonToolMiscActions.Properties.MENU_ACTION);
						// if(selectedShape instanceof SpeciesContextShape){
						// showEditSpeciesDialog(((SpeciesContextShape)selectedShape).getSpeciesContext(),worldPoint);
						// }else if(selectedShape instanceof
						// SimpleReactionShape){
						// showSimpleReactionPropertiesDialog((SimpleReactionShape)selectedShape,worldPoint);
						// }else if(selectedShape instanceof FluxReactionShape){
						// showFluxReactionPropertiesDialog((FluxReactionShape)selectedShape,worldPoint);
						// }else if(selectedShape instanceof ProductShape){
						// showProductPropertiesDialog((ProductShape)selectedShape,worldPoint);
						// }else if(selectedShape instanceof ReactantShape){
						// showReactantPropertiesDialog((ReactantShape)selectedShape,worldPoint);
						// }
						// selectedShape.showPropertiesDialog(desktop,
						// selectedShape.getLocationOnScreen());
					}
				}
				// Shape selectedShape =
				// getStructureCartoon().getSelectedShape();

				// if (event.getClickCount()==2){
				// selectEvent(event.getX(),event.getY());
				// if (selectedShape != null){
				// selectedShape.showPropertiesDialog(desktop,
				// selectedShape.getLocationOnScreen());
				// }
				// }else{
				// selectEventFromWorld(worldPoint);
				// }
				break;
			}
			case FEATURE: {
				menuAction(getStructureCartoon().getSelectedShape(),
						CartoonToolMiscActions.AddFeature.MENU_ACTION);
				// createFeature(pickedShape);
				// String newFeatureName =
				// getStructureCartoon().getModel().getFreeFeatureName();
				// String newMembraneName =
				// getStructureCartoon().getModel().getFreeMembraneName();
				// if (pickedShape instanceof FeatureShape){
				// getStructureCartoon().getModel().addFeature(newFeatureName,((FeatureShape)pickedShape).getFeature(),newMembraneName);
				// }else if (pickedShape instanceof MembraneShape){
				// throw new
				// Exception("cannot add new structure (compartment) to a membrane");
				// }else if (pickedShape==null){
				// getStructureCartoon().getModel().addFeature(newFeatureName,null,null);
				// }
				setMode(Mode.SELECT);
				// Feature feature =
				// (Feature)getStructureCartoon().getModel().getStructure(newFeatureName);
				// Shape shape =
				// getStructureCartoon().getShapeFromModelObject(feature);
				// showFeaturePropertiesDialog((FeatureShape)shape,shape.getLocationOnScreen());
				break;
			}
			case SPECIES: {
				menuAction(getStructureCartoon().getSelectedShape(),
						CartoonToolMiscActions.AddSpecies.MENU_ACTION);
				// if (pickedShape instanceof StructureShape){
				// showCreateSpeciesContextDialog(getStructureCartoon().getModel(),((StructureShape)pickedShape).getStructure(),pickedShape.getLocationOnScreen(getGraphPane().getLocationOnScreen()));
				// }
			}
			default:
				break;
			}
		} catch (Exception e) {
			generateErrorDialog(e, event.getX(), event.getY());
			// System.out.println("CartoonTool.mouseClicked: uncaught exception");
			// e.printStackTrace(System.out);
			// Point canvasLoc = graphPane.getLocationOnScreen();
			// canvasLoc.x += event.getX();
			// canvasLoc.y += event.getY();
			// javax.swing.JOptionPane.showMessageDialog(cbit.vcell.desktop.controls.ClientDisplayManager.getClientDisplayManager().getMainClientWindow(),
			// e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
		}
	}


	@Override
	public void mouseDragged(MouseEvent event) {
	}

	@Override
	public void mousePressed(MouseEvent event) {
		//
		if (getStructureCartoon() == null) {
			return;
		}
		try {
			int x = event.getX();
			int y = event.getY();
			java.awt.Point worldPoint = screenToWorld(x, y);
			// Always select with MousePress
			if (mode == Mode.SELECT
					|| (event.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
				selectEventFromWorld(worldPoint);
			}
			//
			// If mouse popupMenu event, popup menu
			if (event.isPopupTrigger() && mode == Mode.SELECT) {
				popupMenu(getStructureCartoon().getSelectedShape(), event.getX(), 
						event.getY());
				return;
			}
		} catch (Exception e) {
			System.out.println("StructureCartoonTool.mousePressed: uncaught exception");
			e.printStackTrace(System.out);
		}
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		try {
			// Picked shape
			int x = event.getX();
			int y = event.getY();
			java.awt.Point worldPoint = screenToWorld(x, y);
			// Shape pickedShape = getStructureCartoon().pickWorld(worldPoint);
			//
			// if mouse popupMenu event, popup menu
			//
			if (event.isPopupTrigger() &&
					// !bMoving &&
					mode == Mode.SELECT) {
				// selectEventFromWorld(worldPoint);
				Shape pickedShape = getStructureCartoon().pickWorld(worldPoint);
				if (pickedShape == getStructureCartoon().getSelectedShape()) {
					popupMenu(getStructureCartoon().getSelectedShape(),
							event.getX(), event.getY());
				}
				return;
			}
			//
			// if ((event.getModifiers() & (MouseEvent.BUTTON2_MASK |
			// MouseEvent.BUTTON3_MASK)) != 0){
			// return;
			// }
			// //
			// // else select or move
			// //
			// getGraphPane().setCursor(Cursor.getDefaultCursor());
			// if (mode==SELECT_MODE){
			// if (bMoving){
			// if (!(movingShape instanceof MembraneShape)){
			// throw new
			// Exception("expected movingShape to be of type MembraneShape");
			// }
			// Shape newParent = getStructureCartoon().pickWorld(worldPoint);
			// if (newParent!=null){
			// // make sure that new parent is a featureShape other than the one
			// that is being moved
			// if (newParent!=movingShape &&
			// newParent instanceof FeatureShape &&
			// newParent.getParent()!=movingShape &&
			// !movingShape.isDescendant(newParent)) {
			// FeatureShape fs = (FeatureShape)newParent;
			// ((MembraneShape)movingShape).getMembrane().setOutsideFeature(fs.getFeature());
			// getStructureCartoon().refreshAll();
			// }
			// }
			// getStructureCartoon().notifyChangeEvent();
			// // dragFix.queueEvent(null);
			// getGraphPane().repaint();
			// }
			// }
			// //
			// bMoving=false;
			// movingShape=null;
			// //
		} catch (Exception e) {
			System.out.println("CartoonTool.mouseReleased: uncaught exception");
			e.printStackTrace(System.out);
		}

	}

	public void propertyChange(PropertyChangeEvent evt) {

		if ((evt.getSource() == (StructureCartoon) getGraphModel() && evt
				.getPropertyName().equals("model"))
				|| (evt.getSource() == ((StructureCartoon) getGraphModel()).getModel() 
						&& evt.getPropertyName().equals(
						"structures"))) {
			// The Model (or the Structures therein) associated with our
			// StructureCartoon have changed
			// dispose of all ReactionCartoonEditors because they have
			// references to
			// the wrong Model or the structures don't match the ContainerShape
			// heirarchy
			Iterator<ReactionCartoonEditorDialog> iter = 
				reactionEditorHash.values().iterator();
			while (iter.hasNext()) {
				disposeReactionCartoonEditorDialog(iter.next());
			}
			reactionEditorHash.clear();
			// clear the model in ModelParametersPanel and ProblemsPanel thro'
			// ModelParametersDialog
			if (modelParametersDialog != null) {
				modelParametersDialog.cleanupOnClose();
			}

			if (evt.getSource() == (StructureCartoon) getGraphModel()
					&& evt.getPropertyName().equals("model")) {
				// configure Model listeners for structure changes
				if (evt.getOldValue() != null) {
					((Model) evt.getOldValue()).removePropertyChangeListener(this);
				}
				if (evt.getNewValue() != null) {
					((Model) evt.getNewValue()).removePropertyChangeListener(this);
					((Model) evt.getNewValue()).addPropertyChangeListener(this);
				}
				if (modelParametersDialog != null
						&& modelParametersDialog.getModelParametersPanel() != null
						&& (modelParametersDialog.getModelParametersPanel().getModel() 
								!= ((StructureCartoon) getGraphModel())
								.getModel())) {
					modelParametersDialog.init(((StructureCartoon) 
							getGraphModel()).getModel());
				}
			}

		}

		// if(evt.getSource() ==
		// ((StructureCartoon)getGraphModel()).getModel()){
		// if(evt.getPropertyName().equals("structures")){
		// //The Structures in the Model associated with our
		// //StructureCartoon have changed (removed or heirarchy changed)
		// //If any existing ReactionCartoonEditor is associated with a changed
		// Structure
		// //then dispose of that Editor
		// Iterator iter = reactionEditorHash.keySet().iterator();
		// while(iter.hasNext()){
		// Structure recpStruct = (Structure)iter.next();
		// Structure modelStruct =
		// ((Model)evt.getSource()).getStructure(recpStruct.getName());
		// if(modelStruct == null ||
		// modelStruct != recpStruct ||
		// modelStruct.getParentStructure() != recpStruct.getParentStructure()
		// ||
		// (modelStruct instanceof Membrane &&
		// ((Membrane)modelStruct).getInsideFeature() !=
		// ((Membrane)recpStruct).getInsideFeature()) ||
		// (modelStruct instanceof Membrane &&
		// ((Membrane)modelStruct).getOutsideFeature() !=
		// ((Membrane)recpStruct).getOutsideFeature())
		// ){
		// disposeReactionCgetStructureCartoonartoonEditorDialog((ReactionCartoonEditorDialog)reactionEditorHash.get(recpStruct));
		// reactionEditorHash.remove(recpStruct);
		// }
		// }
		// }
		// }

	}


	private void selectEventFromWorld(Point worldPoint) {
		//
		if(getStructureCartoon() == null){return;}
		// Only 1 thing at a time can be selected in StructureCartoon
		Shape selectedShape = getStructureCartoon().getSelectedShape();
		Shape pickedShape = getStructureCartoon().pickWorld(worldPoint);
		//	
		if (selectedShape == pickedShape) {
			return;
		}
		getStructureCartoon().clearSelection();
		if (pickedShape == null) {
			return;
		}
		//
		getStructureCartoon().selectShape(pickedShape);
	}

	public void setGraphModel(StructureCartoon newCartoon) {
		if (structureCartoon != null) {
			structureCartoon.removePropertyChangeListener(this);
			if (structureCartoon.getModel() != null) {
				structureCartoon.getModel().removePropertyChangeListener(this);
			}
		}
		structureCartoon = newCartoon;
		if (structureCartoon != null) {
			structureCartoon.addPropertyChangeListener(this);// Listen for BioModel change
			if (structureCartoon.getModel() != null) {
				structureCartoon.getModel().addPropertyChangeListener(this);
				// Listen for structure change
			}
		}
	}

	@Override
	public boolean shapeHasMenuAction(Shape shape, String menuAction) {
		// all structures (features and membranes) can edit properties and reactions
		if (shape instanceof StructureShape) {
			if (menuAction.equals(CartoonToolMiscActions.Properties.MENU_ACTION)
					|| menuAction.equals(CartoonToolMiscActions.Reactions.MENU_ACTION)
					|| menuAction.equals(CartoonToolMiscActions.ReactionsSlices.MENU_ACTION)
					|| menuAction.equals(CartoonToolEditActions.Paste.MENU_ACTION)
					|| menuAction.equals(CartoonToolEditActions.PasteNew.MENU_ACTION)
					|| menuAction.equals(CartoonToolMiscActions.AddSpecies.MENU_ACTION)
					|| menuAction.equals(CartoonToolMiscActions.AddGlobalParameter.MENU_ACTION)
					|| menuAction.equals(CartoonToolMiscActions.ShowParameters.MENU_ACTION)
					|| menuAction.equals(CartoonToolSaveAsImageActions.HighRes.MENU_ACTION)
					|| menuAction.equals(CartoonToolSaveAsImageActions.MedRes.MENU_ACTION)
					|| menuAction.equals(CartoonToolSaveAsImageActions.LowRes.MENU_ACTION)) {
				return true;
			}
		}
		// only features should be deleted (not membranes).
		if (shape instanceof FeatureShape) {
			if (menuAction.equals(CartoonToolEditActions.Delete.MENU_ACTION)
					|| menuAction.equals(CartoonToolMiscActions.AddFeature.MENU_ACTION)
					|| menuAction.equals(CartoonToolEditActions.Move.MENU_ACTION)) {
				return true;
			}
		}
		// speciesContext's may be deleted or edited or species edited or
		// copied.
		if (shape instanceof SpeciesContextShape) {
			if (menuAction.equals(CartoonToolEditActions.Cut.MENU_ACTION)
					|| menuAction.equals(CartoonToolMiscActions.Properties.MENU_ACTION)
					|| menuAction.equals(CartoonToolEditActions.Copy.MENU_ACTION)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean shapeHasMenuActionEnabled(Shape shape, String menuAction) {
		// Paste if there is a species on the system clipboard and (it doesn't
		// exist in structure || you are PASTE_NEW)
		if (shape instanceof StructureShape) {
			boolean bPasteNew = menuAction.equals(CartoonToolEditActions.PasteNew.MENU_ACTION);
			boolean bPaste = menuAction.equals(CartoonToolEditActions.Paste.MENU_ACTION);
			if (bPaste || bPasteNew) {
				Species species = 
					(Species) SimpleTransferable.getFromClipboard(VCellTransferable.SPECIES_FLAVOR);
				if (species == null) {
					return false;
				}
				if (getStructureCartoon().getModel().contains(species)) {
					if (getStructureCartoon().getModel().getSpeciesContext(
							species, ((StructureShape) shape).getStructure()) != null) {
						return bPasteNew ? true : false;
					} else {
						return bPasteNew ? false : true;
					}
				} else {
					return bPasteNew ? false : true;
				}
			}
		}
		// Is Move valid
		if (shape instanceof FeatureShape) {
			if (menuAction.equals(CartoonToolEditActions.Move.MENU_ACTION)) {
				Feature[] featureArr = 
					getStructureCartoon().getModel().getValidDestinationsForMovingFeature(
							((FeatureShape) shape).getFeature());
				if (featureArr == null || featureArr.length == 0) {
					return false;
				}
			}
		}
		return true;
	}

	private void showMoveDialog(FeatureShape featureShape) {
		try {
			Feature[] validDestinations = 
				featureShape.getModel().getValidDestinationsForMovingFeature(featureShape.getFeature());
			if (validDestinations == null || validDestinations.length == 0) {
				DialogUtils.showErrorDialog(getGraphPane(),
						"No valid Destinations for Feature '"
						+ featureShape.getFeature().getName() + "'");
			} else {
				String[] destinations = new String[validDestinations.length];
				for (int i = 0; i < validDestinations.length; i += 1) {
					destinations[i] = validDestinations[i].getName();
				}
				String featureSelection = (String) DialogUtils.showListDialog(
						getJDesktopPane(), destinations,
						"Select destination for Moving Feature '"
						+ featureShape.getFeature().getName() + "'");

				if (featureSelection == null) {
					return;
				}

				Feature finalDestination = (Feature) 
				featureShape.getModel().getStructure(featureSelection);
				if (finalDestination == null) {
					DialogUtils.showErrorDialog(getGraphPane(),
							"Feature with name " + featureSelection
							+ " not found in model");
					return;
				}
				SpeciesContext[] neededSCArr = 
					featureShape.getModel().getSpeciesContextsNeededByMovingMembrane(
							featureShape.getFeature().getMembrane());
				if (neededSCArr != null) {
					String message = "";
					for (int i = 0; i < neededSCArr.length; i += 1) {
						if (featureShape.getModel().getSpeciesContext(
								neededSCArr[i].getSpecies(), finalDestination) == null) {
							if (message.length() > 0) {
								message += "\n";
							}
							message += "     "
								+ neededSCArr[i].getSpecies()
								.getCommonName();
						}
					}
					if (message.length() > 0) {
						String result = DialogUtils.showWarningDialog(getJDesktopPane(),
								"The following species must be copied to destination '"
								+ finalDestination.getName()
								+ "'\n"
								+ "because reactions of moving Membrane'"
								+ featureShape.getFeature().getMembrane().getName()
								+ "' need them\n" + message,
								new String[] { "OK", "Cancel" }, "OK");
						if (result.equals("Cancel")) {
							return;
						}
					}
				}

				Feature parentOfMoving = 
					(Feature) featureShape.getFeature().getMembrane().getParentStructure();
				featureShape.getModel().moveFeature(featureShape.getFeature(),
						finalDestination);

				// Ask if cleanup wanted on SpeciesContexts of parent of moving
				// membrane that have no other references
				Vector<SpeciesContext> cleanupWantedV = new Vector<SpeciesContext>();
				if (neededSCArr != null && neededSCArr.length > 0) {
					for (int i = 0; i < neededSCArr.length; i += 1) {
						boolean bFound = false;
						ReactionStep[] reactionArr = featureShape.getModel().getReactionSteps();
						if (reactionArr != null) {
							for (int j = 0; j < reactionArr.length; j += 1) {
								if ((reactionArr[j].getStructure() == parentOfMoving || 
										reactionArr[j].getStructure() == parentOfMoving.getMembrane())
										&& reactionArr[j].countNumReactionParticipants(neededSCArr[i]) > 0) {
									bFound = true;
									break;
								}
							}
						}
						if (!bFound) {
							cleanupWantedV.add(neededSCArr[i]);
						}
					}
				}
				if (cleanupWantedV.size() > 0) {
					String message = "";
					for (int i = 0; i < cleanupWantedV.size(); i += 1) {
						if (message.length() > 0) {
							message += "\n";
						}
						message += "     "
							+ (cleanupWantedV.elementAt(i)).getName();
					}
					if (message.length() > 0) {
						String result = DialogUtils.showWarningDialog(
								getJDesktopPane(),
								"The following Species from Feature '"
								+ parentOfMoving.getName()
								+ "' that were used by\n"
								+ "moved membrane '"
								+ featureShape.getFeature().getMembrane().getName()
								+ "' no longer have references\n"
								+ "Should the Species be deleted?\n"
								+ message, new String[] {
									UserMessage.OPTION_YES,
									UserMessage.OPTION_NO },
									UserMessage.OPTION_NO);
						if (result.equals(UserMessage.OPTION_YES)) {
							for (int i = 0; i < cleanupWantedV.size(); i += 1) {
								try {
									featureShape.getModel().removeSpeciesContext(
											cleanupWantedV.elementAt(i));
								} catch (Throwable e) {
									System.out.println(e.getClass() + " "
											+ e.getMessage());
								}
							}
						}
					}
				}
			}
		} catch (Throwable e) {
			DialogUtils.showErrorDialog(getGraphPane(), e.getMessage(), e);
		}
	}
	
	public void showReactionCartoonEditorPanel(
			final StructureShape structureShape) {
		if (getGraphModel() == null || getDocumentManager() == null
				|| getJDesktopPane() == null) {
			return;
		}
		//
		// See propertyChange method for related code that closes
		// ReactionEditorCartoonDialog when appropriate
		//
		Structure structure = structureShape.getStructure();
		StructureSuite layout;
		if(structure instanceof Membrane) {
			layout = new MembraneStructureSuite(((Membrane) structure));
		} else {
			layout = new SingleStructureSuite(structure);
		}
		StructureSuite structureSuite = layout;
		ReactionCartoonEditorDialog rced = reactionEditorHash.get(structureSuite.getTitle());
		if (rced == null) {
			final ReactionCartoonEditorDialog reactionCartoonEditorDialog = 
				new ReactionCartoonEditorDialog();
			reactionCartoonEditorDialog.setIconifiable(true);
			reactionCartoonEditorDialog.init(structureShape.getModel(),
					structureSuite, getDocumentManager());
			reactionEditorHash.put(structureSuite.getTitle(), reactionCartoonEditorDialog);
			rced = reactionCartoonEditorDialog;
			rced.setLocation(rced.getLocation().x + reactionEditorHash.size() * 15, 
					rced.getLocation().y + reactionEditorHash.size() * 15);
		}
		BeanUtils.centerOnComponent(rced, getJDesktopPane());
		DocumentWindowManager.showFrame(rced, getJDesktopPane());
	}

	public void showReactionSlicesCartoonEditorPanel(
			final StructureShape structureShape) {
		if (getGraphModel() == null || getDocumentManager() == null
				|| getJDesktopPane() == null) {
			return;
		}
		//
		// See propertyChange method for related code that closes
		// ReactionEditorCartoonDialog when appropriate
		//
		StructureSuite structureSuite = 
			new AllStructureSuite(getStructureCartoon());
		ReactionCartoonEditorDialog rced = reactionEditorHash.get(structureSuite.getTitle());
		if (rced == null) {
			final ReactionCartoonEditorDialog reactionCartoonEditorDialog = 
				new ReactionCartoonEditorDialog();
			reactionCartoonEditorDialog.setIconifiable(true);
			reactionCartoonEditorDialog.init(structureShape.getModel(),
				structureSuite, getDocumentManager());
			reactionEditorHash.put(structureSuite.getTitle(), reactionCartoonEditorDialog);
			rced = reactionCartoonEditorDialog;
			rced.setLocation(rced.getLocation().x + reactionEditorHash.size() * 15, 
					rced.getLocation().y + reactionEditorHash.size() * 15);
		}
		BeanUtils.centerOnComponent(rced, getJDesktopPane());
		DocumentWindowManager.showFrame(rced, getJDesktopPane());
	}

	// TO DO: allow user preferences for directory selection.
	public void showSaveStructureImageDialog(Model model, String resLevel)
	throws Exception {
		if (model == null) { // or throw exception?
			System.err.println("Insufficient params for generating structures image.");
			return;
		}
		if (resLevel == null) { // default resolution.
			resLevel = ITextWriter.HIGH_RESOLUTION;
		}
		System.out.println("Processing save as Image request for: "
				+ model.getName() + "(" + resLevel + ")");
		// set file filter
		SimpleFilenameFilter gifFilter = new SimpleFilenameFilter("gif");
		ClientServerManager csm =
			(ClientServerManager) getDocumentManager().getSessionManager();
		UserPreferences userPref = csm.getUserPreferences();
		String defaultPath = userPref.getGenPref(UserPreferences.GENERAL_LAST_PATH_USED);
		VCFileChooser fileChooser = new VCFileChooser(defaultPath);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.addChoosableFileFilter(gifFilter);
		final java.io.File defaultFile = new java.io.File(model.getName() + ".gif");
		fileChooser.setSelectedFile(defaultFile);
		fileChooser.setDialogTitle("Save Image As...");
		// a hack to fix the jdk 1.2 problem (?) of losing the selected file
		// name once the user changes the directory.
		class FileChooserFix implements java.beans.PropertyChangeListener {
			public void propertyChange(java.beans.PropertyChangeEvent ev) {
				JFileChooser chooser = (JFileChooser) ev.getSource();
				if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(ev.getPropertyName())) {
					// java.io.File directory = (java.io.File)ev.getNewValue();
					chooser.setSelectedFile(defaultFile);
				}
			}
		}
		fileChooser.addPropertyChangeListener(new FileChooserFix());
		// process user input
		if (fileChooser.showSaveDialog(getDialogOwner(getGraphPane())) == 
			JFileChooser.APPROVE_OPTION) {
			java.io.File selectedFile = fileChooser.getSelectedFile();
			if (selectedFile != null) {
				if (selectedFile.exists()) {
					int question = javax.swing.JOptionPane.showConfirmDialog(
							getDialogOwner(getGraphPane()), "Overwrite file: "
							+ selectedFile.getPath() + "?");
					if (question == javax.swing.JOptionPane.NO_OPTION
							|| question == javax.swing.JOptionPane.CANCEL_OPTION) {
						return;
					}
				}
				// System.out.println("Saving reactions image to file: " +
				// selectedFile.toString());
				getDocumentManager().generateStructureImage(model, resLevel,
						new FileOutputStream(selectedFile));
				// reset the user preference for the default path, if needed.
				String newPath = selectedFile.getParent();
				if (!newPath.equals(defaultPath)) {
					userPref.setGenPref(UserPreferences.GENERAL_LAST_PATH_USED, newPath);
				}
			} else {
				// throw
				// cbit.vcell.client.task.UserCancelException.CANCEL_FILE_SELECTION;
			}
		} else {
			// throw
			// cbit.vcell.client.task.UserCancelException.CANCEL_FILE_SELECTION;
			// //best available
		}
	}

	@Override
	public void updateMode(Mode newMode) {
		if (newMode == mode) {
			return;
		}
		// bMoving = false;
		// movingShape = null;
//		if (getStructureCartoon() != null) {
//			getStructureCartoon().clearSelection();
//		}
		this.mode = newMode;
		if (getGraphPane() != null) {
			switch (mode) {
			case FEATURE: {
				getGraphPane().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				break;
			}
			case SPECIES: {
				getGraphPane().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				break;
			}
			case SELECT: {
				getGraphPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				break;
			}
			default: {
				System.out.println("ERROR: mode " + newMode + "not defined");
				break;
			}
			}
		}
		return;
	}
}