package cbit.gui.graph;

/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import java.awt.Point;
import java.awt.geom.GeneralPath;

import cbit.gui.graph.GraphModel;
/**
 * This type was created in VisualAge.
 */
public class GraphEdgeShape extends cbit.gui.graph.EdgeShape {
	protected cbit.util.graph.Edge fieldEdge = null;
	protected boolean bArrow = false;
/**
 * ReactionParticipantShape constructor comment.
 * @param label java.lang.String
 * @param graphModel cbit.vcell.graph.GraphModel
 */
public GraphEdgeShape(cbit.util.graph.Edge edge, NodeShape node1Shape, NodeShape node2Shape, GraphModel graphModel, boolean displayArrow) {
	super(node1Shape, node2Shape, graphModel);
	this.fieldEdge = edge;
	this.bArrow = displayArrow;
}
/**
 * This method was created in VisualAge.
 * @return java.lang.Object
 */
public Object getModelObject() {
	return fieldEdge;
}
/**
 * This method was created in VisualAge.
 * @return cbit.vcell.graph.ReactionStepShape
 */
public NodeShape getNode1Shape() {
	return (NodeShape)startShape;
}
/**
 * This method was created in VisualAge.
 * @return cbit.vcell.graph.ReactionStepShape
 */
public NodeShape getNode2Shape() {
	return (NodeShape)endShape;
}
/**
 * This method was created in VisualAge.
 */
public void refreshLabel() {
	setLabel("");
}

public void paint(java.awt.Graphics2D g2D, int parentOffsetX, int parentOffsetY){
	super.paint(g2D,parentOffsetX,parentOffsetY);
	
	//
	// add arrow if directed
	//
	if (bArrow){
		Point startLocation = getNode1Shape().getAttachmentLocation(EdgeShape.ATTACH_CENTER);
		Point endLocation = getNode2Shape().getAttachmentLocation(EdgeShape.ATTACH_CENTER);
		double diffX = endLocation.x-startLocation.x;
		double diffY = endLocation.y-startLocation.y;
		double length = Math.sqrt(diffX*diffX+diffY*diffY);
		double arrowScale = 10/length;
		Point front = new Point((int)(startLocation.x+diffX/2+diffX*arrowScale/2),(int)(startLocation.y+diffY/2+diffY*arrowScale/2));
		Point back = new Point((int)(startLocation.x+diffX/2-diffX*arrowScale/2),(int)(startLocation.y+diffY/2-diffY*arrowScale/2));
		GeneralPath path = getArrow(front, back, 10);
		g2D.fill(path);
	}
}

}
