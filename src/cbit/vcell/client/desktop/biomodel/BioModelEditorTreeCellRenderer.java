package cbit.vcell.client.desktop.biomodel;

/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import java.awt.Component;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;

import cbit.vcell.biomodel.BioModel;
import cbit.vcell.desktop.BioModelNode;
 
@SuppressWarnings("serial")
public class BioModelEditorTreeCellRenderer extends DocumentEditorTreeCellRenderer  {
//	private BioModel bioModel = null;
	private Icon bioModelIcon = null;
	
	public BioModelEditorTreeCellRenderer(JTree tree) {
		super(tree);
		try {
			bioModelIcon = new ImageIcon(getClass().getResource("/images/bioModel_16x16.gif"));
		} catch (Exception ex) {
			ex.printStackTrace(System.out);
		}
	}

	public Component getTreeCellRendererComponent(
                        JTree tree,
                        Object value,
                        boolean sel,
                        boolean expanded,
                        boolean leaf,
                        int row,
                        boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		Font font = regularFont;
		Icon icon = null;
    	String labelText = null;
    	String toolTipPrefix = "";
    	String toolTipSuffix = "";
    	boolean bChange = false;
    	if (value instanceof BioModelNode) {
	        BioModelNode node = (BioModelNode)value;
	        Object userObj = node.getUserObject();
	    	if (userObj instanceof BioModel) {
	    		bChange = true;
	    		font = boldFont;
	    		icon = bioModelIcon;
	    		labelText = ((BioModel)userObj).getName();
	    		toolTipPrefix = "BioModel: ";
//	    	} else if (userObj instanceof VCMetaData) {
//	    		bChange = true;
//	    		if (bioModel != null) {
//	    			VCMetaData vcMetaData = (VCMetaData)userObj;
//	    			labelText = vcMetaData.getFreeTextAnnotation(bioModel);
//	    		}
//	        	if (labelText == null || labelText.length() == 0) {
//	        		labelText = "(click to edit notes)";
//	        	}
	        }
		}
    	if (bChange) {
	    	setIcon(icon);
	    	setFont(font);
	    	setText(labelText);
	    	if (toolTipSuffix.length() == 0) {
				toolTipSuffix = labelText;
			}
	    	setToolTipText(toolTipPrefix + toolTipSuffix);
    	}
        return this;
    }
}
