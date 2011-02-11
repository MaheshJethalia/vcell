package org.vcell.relationship;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.vcell.pathway.BioPaxObject;
import cbit.vcell.model.BioModelEntityObject;


public class RelationshipModel {
	private HashSet<RelationshipObject> relationshipObjects = new HashSet<RelationshipObject>();
	protected transient ArrayList<RelationshipListener> aRelationshipListeners = new ArrayList<RelationshipListener>();

	public Set<RelationshipObject> getRelationshipObjects(){
		return relationshipObjects;
	}
	
	public void setRelationshipObjects(HashSet<RelationshipObject> relationshipObjects){
		this.relationshipObjects = relationshipObjects;
	}
	
	public void merge(RelationshipModel relationshipModel) {
		relationshipObjects.addAll(relationshipModel.relationshipObjects);
		fireRelationshipChanged(new RelationshipEvent(this, RelationshipEvent.CHANGED));
	}
	
	private ArrayList<RelationshipListener> getRelationshipListeners(){
		if (aRelationshipListeners == null) {
			aRelationshipListeners = new ArrayList<RelationshipListener>();
		}
		return aRelationshipListeners;
	}
	protected void fireRelationshipChanged(RelationshipEvent event) {
		for (RelationshipListener l : getRelationshipListeners()){
			l.relationshipChanged(event);
		}
	}

	public void addRelationShipListener(RelationshipListener listener) {
		getRelationshipListeners().add(listener);
	}

	public void removeRelationShipListener(RelationshipListener listener) {
		getRelationshipListeners().remove(listener);
	}

	public RelationshipObject addRelationshipObject(RelationshipObject relationshipObject){
		if (relationshipObject==null){
			throw new RuntimeException("added a null object to relationship model");
		}
		if(!contains(relationshipObject)){
			getRelationshipObjects().add(relationshipObject);
			fireRelationshipChanged(new RelationshipEvent(this,RelationshipEvent.CHANGED));
		}
		return relationshipObject;
	}
	
	public RelationshipObject removeRelationshipObject(RelationshipObject relationshipObject){
		if (relationshipObject==null){
			throw new RuntimeException("remove a null object to relationship model");
		}
		if(!contains(relationshipObject)){
			getRelationshipObjects().remove(relationshipObject);
			fireRelationshipChanged(new RelationshipEvent(this,RelationshipEvent.CHANGED));
		}
		return relationshipObject;
	}
	
	public void addRelationshipObjects(HashSet<RelationshipObject> reObjects){
		if (reObjects==null){
			throw new RuntimeException("added a null object to relationship model");
		}
		for(RelationshipObject reObject : reObjects)
			getRelationshipObjects().add(reObject);

		fireRelationshipChanged(new RelationshipEvent(this,RelationshipEvent.CHANGED));
		return;
	}

	public HashSet<RelationshipObject> getRelationshipObjects(BioModelEntityObject bioModelObject){
		HashSet<RelationshipObject> associatedReObjects = new HashSet<RelationshipObject>();
		for(RelationshipObject reObject : getRelationshipObjects()){
			if(reObject.getBioModelEntityObject().equals(bioModelObject))
				associatedReObjects.add(reObject);
		}
		return associatedReObjects;
	}
	
	public HashSet<RelationshipObject> getRelationshipObjects(BioPaxObject bioPaxObject){
		HashSet<RelationshipObject> associatedReObjects = new HashSet<RelationshipObject>();
		for(RelationshipObject reObject : getRelationshipObjects()){
			if(reObject.getBioPaxObject().equals(bioPaxObject))
				associatedReObjects.add(reObject);
		}
		return associatedReObjects;
	}
	
	// to check whether the RelationshipObject has been added
	public boolean contains(RelationshipObject reObject){
		return relationshipObjects.contains(reObject);
	}
}
