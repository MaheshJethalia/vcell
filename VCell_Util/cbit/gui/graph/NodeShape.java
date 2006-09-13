package cbit.gui.graph;

/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import cbit.gui.graph.GraphModel;
import java.awt.*;
import java.util.*;
import javax.swing.*;
/**
 * This class was generated by a SmartGuide.
 * 
 */
public class NodeShape extends cbit.gui.graph.ElipseShape {
	int radius = 8;
	protected Color darkerBackground = null;
	protected int fieldDegree = 0;
	protected cbit.util.graph.Node fieldNode = null;
/**
 * SpeciesShape constructor comment.
 * @param label java.lang.String
 * @param graphModel cbit.vcell.graph.GraphModel
 */
public NodeShape(cbit.util.graph.Node node, GraphModel graphModel, int degree) {
	super(graphModel);
	this.fieldNode = node;
	this.fieldDegree = degree;
	defaultBG = java.awt.Color.green;
	defaultFGselect = java.awt.Color.black;
	backgroundColor = defaultBG;
	darkerBackground = backgroundColor.darker().darker();
	refreshLabel();
}
/**
 * This method was created in VisualAge.
 * @return java.lang.Object
 */
public Object getModelObject() {
	return fieldNode;
}
/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.model.Species
 */
public cbit.util.graph.Node getNode() {
	return fieldNode;
}
/**
 * This method was created by a SmartGuide.
 * @return int
 * @param g java.awt.Graphics
 */
public Dimension getPreferedSize(java.awt.Graphics2D g) {
	java.awt.FontMetrics fm = g.getFontMetrics();
	labelSize.height = fm.getMaxAscent() + fm.getMaxDescent();
	labelSize.width = fm.stringWidth(getLabel());
//	preferedSize.height = radius*2 + labelSize.height;
//	preferedSize.width = Math.max(radius*2,labelSize.width);
	preferedSize.height = radius*2;
	preferedSize.width = radius*2;
	return preferedSize;
}
/**
 * This method was created by a SmartGuide.
 * @return int
 */
public Point getSeparatorDeepCount() {	
	return new Point(0,0);
}
/**
 * This method was created by a SmartGuide.
 * @return int
 * @param g java.awt.Graphics
 */
public void layout() {

//	if (screenSize.width<labelSize.width ||
//		 screenSize.height<labelSize.height){
//		 throw new Exception("screen size smaller than label");
//	} 
	//
	// this is like a row/column layout  (1 column)
	//
	int centerX = screenSize.width/2;
	
	//
	// position label
	//
	labelPos.x = centerX - labelSize.width/2;
	labelPos.y = 0;
}
/**
 * This method was created by a SmartGuide.
 * @param g java.awt.Graphics
 */
public void paint ( java.awt.Graphics2D g, int parentOffsetX, int parentOffsetY ) {

   int absPosX = screenPos.x + parentOffsetX;
   int absPosY = screenPos.y + parentOffsetY;
	//
	boolean isBound = false;
	//
	// draw elipse
	//
	g.setColor(backgroundColor);
	g.fillOval(absPosX+1,absPosY+1+labelPos.y,2*radius-1,2*radius-1);
	g.setColor(forgroundColor);
	g.drawOval(absPosX,absPosY+labelPos.y,2*radius,2*radius);
	//
	// draw label
	//
	java.awt.FontMetrics fm = g.getFontMetrics();
	int textX = labelPos.x + absPosX;
	int textY = labelPos.y + absPosY;
	g.setColor(forgroundColor);
	if (getLabel()!=null && getLabel().length()>0){
		g.drawString(getLabel(),textX,textY);
	}
	return;
}
/**
 * This method was created in VisualAge.
 */
public void refreshLabel() {
	setLabel(getNode().getName());
}
/**
 * This method was created by a SmartGuide.
 * @param newSize java.awt.Dimension
 */
public void resize(Graphics2D g, Dimension newSize) {
	return;
}
}
