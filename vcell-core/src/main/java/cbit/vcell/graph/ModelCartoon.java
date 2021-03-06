/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.graph;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.vcell.relationship.RelationshipEvent;
import org.vcell.relationship.RelationshipListener;
import org.vcell.relationship.RelationshipModel;
import org.vcell.relationship.RelationshipObject;

import cbit.gui.graph.GraphModel;
import cbit.gui.graph.Shape;
import cbit.vcell.model.BioModelEntityObject;
import cbit.vcell.model.Model;
import cbit.vcell.model.Structure;
/**
 * This class was generated by a SmartGuide.
 * 
 */
public abstract class ModelCartoon extends GraphModel 
implements java.beans.PropertyChangeListener, Model.Owner, RelationshipListener {
	public static final String PROPERTY_NAME_MODEL = "model";
	private Model fieldModel = null;

	/**
	 * Insert the method's description here.
	 * Creation date: (5/13/2003 9:46:54 AM)
	 * @return cbit.vcell.model.Model
	 */
	public Model getModel() {
		return fieldModel;
	}


	/**
	 * Insert the method's description here.
	 * Creation date: (5/13/2003 9:46:54 AM)
	 * @param newFieldModel cbit.vcell.model.Model
	 */
	public void setModel(Model newFieldModel) {
		Model oldModel = fieldModel;
		if (oldModel != null){
			oldModel.removePropertyChangeListener(this);
		}
		fieldModel = newFieldModel;
		if(fieldModel != null){
			fieldModel.removePropertyChangeListener(this);
			fieldModel.addPropertyChangeListener(this);
		}
		refreshAll();
		firePropertyChange(PROPERTY_NAME_MODEL, oldModel, newFieldModel);
	}
	
	@Override
	public void searchText(String text) {
		String lowerCaseText = text.toLowerCase();
		Set<Object> selectedObjectsNew = new HashSet<Object>();
		for(Map.Entry<Object, Shape> entry : objectShapeMap.entrySet()) {
			Object object = entry.getKey();
			Shape shape = entry.getValue();
			if(!(object instanceof Structure) && text != null && text.length() != 0 && shape.getLabel() != null && shape.getLabel().toLowerCase().contains(lowerCaseText)) {
				selectedObjectsNew.add(object);
			}
		}
		setSelectedObjects(selectedObjectsNew.toArray());
	}
	
	public void relationshipChanged(RelationshipEvent event) {
		RelationshipObject relationshipObject = event.getRelationshipObject();
		if(event.getOperationType() == event.ADDED){ 
			Shape shape = getShapeFromModelObject(relationshipObject.getBioModelEntityObject());
			if(shape instanceof SpeciesContextShape) {
				SpeciesContextShape scShape = (SpeciesContextShape) shape;
				scShape.setLinkText("L");
			} else if(shape instanceof SimpleReactionShape) {
				SimpleReactionShape srShape = (SimpleReactionShape) shape;
				srShape.setLinkText("L");
			} else if(shape instanceof ReactionRuleDiagramShape) {
				ReactionRuleDiagramShape srShape = (ReactionRuleDiagramShape) shape;
				srShape.setLinkText("L");
			}
		} else if(event.getOperationType() == event.REMOVED){
			Shape shape = getShapeFromModelObject(relationshipObject.getBioModelEntityObject());
			if(shape instanceof SpeciesContextShape) {
				SpeciesContextShape scShape = (SpeciesContextShape) shape;
				scShape.setLinkText("");
				// if the BioModelEntity Object is still linked with other BioPax objects, we add the "L" shape back
				if(((RelationshipModel)event.getSource()).getRelationshipObjects(relationshipObject.getBioModelEntityObject()).size() > 0){
					scShape.setLinkText("L");
				}
			} else if(shape instanceof SimpleReactionShape) {
				SimpleReactionShape srShape = (SimpleReactionShape) shape;
				srShape.setLinkText("");
				if(((RelationshipModel)event.getSource()).getRelationshipObjects(relationshipObject.getBioModelEntityObject()).size() > 0){
					srShape.setLinkText("L");
				}
			} else if(shape instanceof ReactionRuleDiagramShape) {
				ReactionRuleDiagramShape srShape = (ReactionRuleDiagramShape) shape;
				srShape.setLinkText("");
				if(((RelationshipModel)event.getSource()).getRelationshipObjects(relationshipObject.getBioModelEntityObject()).size() > 0){
					srShape.setLinkText("L");
				}
			}				
		}
	}

	public final void refreshRelationshipInfo(RelationshipModel relationshipModel) {
		for(RelationshipObject relationship : relationshipModel.getRelationshipObjects()) {
			BioModelEntityObject bioModelEntity = relationship.getBioModelEntityObject();
			Shape shape = getShapeFromModelObject(bioModelEntity);
			if(shape instanceof SpeciesContextShape) {
				SpeciesContextShape scShape = (SpeciesContextShape) shape;
				scShape.setLinkText("L");
			} else if(shape instanceof SimpleReactionShape) {
				SimpleReactionShape srShape = (SimpleReactionShape) shape;
				srShape.setLinkText("L");
			} else if(shape instanceof ReactionRuleDiagramShape) {
				ReactionRuleDiagramShape srShape = (ReactionRuleDiagramShape) shape;
				srShape.setLinkText("L");
			}		
		}
	}
	
}
