/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.client.desktop.biomodel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.vcell.model.rbm.ComponentStateDefinition;
import org.vcell.model.rbm.ComponentStatePattern;
import org.vcell.model.rbm.MolecularComponent;
import org.vcell.model.rbm.MolecularComponentPattern;
import org.vcell.model.rbm.MolecularType;
import org.vcell.model.rbm.MolecularTypePattern;
import org.vcell.model.rbm.SeedSpecies;
import org.vcell.model.rbm.SpeciesPattern;
import org.vcell.model.rbm.MolecularComponentPattern.BondType;
import org.vcell.model.rbm.SpeciesPattern.Bond;
import org.vcell.pathway.BioPaxObject;
import org.vcell.pathway.Entity;
import org.vcell.relationship.RelationshipObject;
import org.vcell.sybil.models.miriam.MIRIAMQualifier;
import org.vcell.sybil.models.miriam.MIRIAMRef.URNParseFailureException;
import org.vcell.sybil.util.http.pathwaycommons.search.XRef;
import org.vcell.sybil.util.http.uniprot.UniProtConstants;
import org.vcell.sybil.util.miriam.XRefToURN;
import org.vcell.util.Compare;
import org.vcell.util.Displayable;
import org.vcell.util.gui.DialogUtils;
import org.vcell.util.gui.GuiUtils;

import uk.ac.ebi.www.miriamws.main.MiriamWebServices.MiriamProvider;
import uk.ac.ebi.www.miriamws.main.MiriamWebServices.MiriamProviderServiceLocator;
import cbit.vcell.biomodel.BioModel;
import cbit.vcell.biomodel.meta.MiriamManager;
import cbit.vcell.biomodel.meta.MiriamManager.MiriamRefGroup;
import cbit.vcell.biomodel.meta.MiriamManager.MiriamResource;
import cbit.vcell.biomodel.meta.VCMetaData;
import cbit.vcell.client.PopupGenerator;
import cbit.vcell.client.desktop.biomodel.DocumentEditorTreeModel.DocumentEditorTreeFolderClass;
import cbit.vcell.client.desktop.biomodel.RbmDefaultTreeModel.SpeciesPatternLocal;
import cbit.vcell.client.desktop.biomodel.SelectionManager.ActiveView;
import cbit.vcell.client.desktop.biomodel.SelectionManager.ActiveViewID;
import cbit.vcell.client.task.AsynchClientTask;
import cbit.vcell.client.task.ClientTaskDispatcher;
import cbit.vcell.desktop.BioModelNode;
import cbit.vcell.graph.LargeShape;
import cbit.vcell.graph.SpeciesPatternShape;
import cbit.vcell.graph.SpeciesTypeLargeShape;
import cbit.vcell.model.RbmObservable;
import cbit.vcell.model.SpeciesContext;
import cbit.vcell.model.ReactionRule.ReactionRuleParticipantType;
import cbit.vcell.model.common.VCellErrorMessages;
/**
 * Insert the type's description here.
 * Creation date: (2/3/2003 2:07:01 PM)
 * @author: Frank Morgan
 */
@SuppressWarnings("serial")
public class SpeciesPropertiesPanel extends DocumentEditorSubPanel {
	private SpeciesContext fieldSpeciesContext = null;
	private BioModel bioModel = null;
	
	private JTextArea annotationTextArea;
	private JScrollPane linkedPOScrollPane;
	private JEditorPane PCLinkValueEditorPane = null;
	private JTextField nameTextField = null;
	
	private JTree speciesPropertiesTree = null;
	private SpeciesPropertiesTreeModel speciesPropertiesTreeModel = null;
	private JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	private JTree rightClickSourceTree = null;
	
	private EventHandler eventHandler = new EventHandler();

	private JPopupMenu popupMenu;
	private JMenu addMenu;
	private JMenuItem deleteMenuItem;	
	private JMenuItem renameMenuItem;
	private JMenuItem editMenuItem;
	private JCheckBox showDetailsCheckBox;

	private SpeciesPatternShape sps;
	private JPanel shapePanel = null;
	
	public class BioModelNodeEditableTree extends JTree {
		@Override
		public boolean isPathEditable(TreePath path) {
			Object object = path.getLastPathComponent();
			return object instanceof BioModelNode;
		}
	}
	
	public void saveSelectedXRef(final XRef selectedXRef, final MIRIAMQualifier miriamQualifier) {
		AsynchClientTask task1 = new AsynchClientTask("retrieving metadata", AsynchClientTask.TASKTYPE_NONSWING_BLOCKING) {
			@Override
			public void run(Hashtable<String, Object> hashTable) throws Exception {
				String urn = XRefToURN.createURN(selectedXRef.db(), selectedXRef.id());
				try {
					MiriamManager miriamManager = bioModel.getModel().getVcMetaData().getMiriamManager();
					MiriamResource resource = miriamManager.createMiriamResource(urn);
					String urnstr = resource.getMiriamURN(); 
					if (urnstr != null && urnstr.toLowerCase().contains("uniprot")) {
						String prettyName = UniProtConstants.getNameFromID(urnstr);	
						if (prettyName != null) {
							miriamManager.setPrettyName(resource, prettyName);
						}
					}

					Set<MiriamResource> miriamResources = new HashSet<MiriamResource>();
					miriamResources.add(resource);
					miriamManager.addMiriamRefGroup(getSpeciesContext().getSpecies(), miriamQualifier, miriamResources);
					
					MiriamProviderServiceLocator providerLocator = new MiriamProviderServiceLocator();
					MiriamProvider provider = providerLocator.getMiriamWebServices();
					
					String pcLink = resource.getMiriamURN();
					if (pcLink != null && pcLink.length() > 0) {
						String[] locations = provider.getLocations(pcLink);
						if (locations != null){
							for(String url : locations) {
								try {
									miriamManager.addStoredCrossReferencedLink(resource, new URL(url));
								} catch (MalformedURLException e) {
									e.printStackTrace(System.out);
								}
							}
						}
					}
				} catch (URNParseFailureException e) {
					e.printStackTrace();
					DialogUtils.showErrorDialog(SpeciesPropertiesPanel.this, e.getMessage());
				}
			}
		};
		AsynchClientTask task2 = new AsynchClientTask("displaying metadata", AsynchClientTask.TASKTYPE_SWING_BLOCKING) {
			@Override
			public void run(Hashtable<String, Object> hashTable) throws Exception {
				updatePCLink();
			}
		};
		ClientTaskDispatcher.dispatch(this, new Hashtable<String, Object>(), new AsynchClientTask[] { task1, task2 });
	}

	private void updatePCLink() {
		try {
			StringBuffer buffer = new StringBuffer("<html>");
			MiriamManager miriamManager = bioModel.getModel().getVcMetaData().getMiriamManager();
			Map<MiriamRefGroup,MIRIAMQualifier> refGroups = miriamManager.getAllMiriamRefGroups(getSpeciesContext().getSpecies());
			if (refGroups != null && refGroups.size()>0) {
				for (MiriamRefGroup refGroup : refGroups.keySet()){
					Set<MiriamResource> miriamResources = refGroup.getMiriamRefs();
					for (MiriamResource resource : miriamResources){
						String urn = resource.getMiriamURN();
						String preferredName = ""; 
						if (urn != null && urn.length() > 0) {
							String prettyName = miriamManager.getPrettyName(resource);
							if (prettyName != null) {
								preferredName = "[" + prettyName + "]";	
							}
							String prettyResourceName = urn.replaceFirst("urn:miriam:", "");
							buffer.append("&#x95;&nbsp;" + prettyResourceName + "&nbsp;<b>" + preferredName + "</b><br>");
							List<URL> linkURLs = miriamManager.getStoredCrossReferencedLinks(resource);
							for (URL url : linkURLs) {
								buffer.append("&nbsp;&nbsp;&nbsp;&nbsp;-&nbsp;<a href=\"" + url.toString() + "\">" + url.toString() + "</a><br>");
							}
						}
					}
				}
			}
			buffer.append("</html>");
			getPCLinkValueEditorPane().setText(buffer.toString());
			getPCLinkValueEditorPane().setCaretPosition(0);
		} catch (Exception ex) {
			ex.printStackTrace(System.out);
			DialogUtils.showErrorDialog(this, ex.getMessage());
		}

	}
	
	private class EventHandler extends MouseAdapter implements java.awt.event.ActionListener, HyperlinkListener, FocusListener, PropertyChangeListener, TreeSelectionListener,
	TreeWillExpandListener
	{
		public void actionPerformed(java.awt.event.ActionEvent e) {
			Object source = e.getSource();
			if (source == nameTextField) {
				changeName();
//			} else if (e.getSource() == getAddSpeciesPatternMenuItem()) {
//				addSpeciesPattern();
			} else if (source == getDeleteMenuItem()) {
				delete();
			} else if (source == getRenameMenuItem()) {
				speciesPropertiesTree.startEditingAtPath(speciesPropertiesTree.getSelectionPath());
			} else if (source == getAddMenu()) {
				addNew();
			} else if (source == getEditMenuItem()) {
				speciesPropertiesTree.startEditingAtPath(speciesPropertiesTree.getSelectionPath());
			} else if (source == showDetailsCheckBox) {
				speciesPropertiesTreeModel.setShowDetails(showDetailsCheckBox.isSelected());
			}
		
		};
		@Override
		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType() == EventType.ACTIVATED) {
				URL link = e.getURL();
				if (link != null) {
					DialogUtils.browserLauncher(SpeciesPropertiesPanel.this, link.toExternalForm(), "failed to launch", false);
				}
			}
		}
		public void focusGained(FocusEvent e) {
		}
		public void focusLost(FocusEvent e) {
			if (e.getSource() == annotationTextArea) {
				changeFreeTextAnnotation();
			} else if (e.getSource() == nameTextField) {
				changeName();
			} 
		}
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getSource() == fieldSpeciesContext) {
				updateInterface();
			}
		}
		@Override
		public void valueChanged(TreeSelectionEvent e) {
		}
		@Override
		public void mousePressed(MouseEvent e) {
			if (!e.isConsumed() && e.getSource() == speciesPropertiesTree) {
				showPopupMenu(e);
			}
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			if (!e.isConsumed() && e.getSource() == speciesPropertiesTree) {
				showPopupMenu(e);
			}			
		}
		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			super.mouseExited(e);
			if(e.getSource() == annotationTextArea){
				changeFreeTextAnnotation();
			}
		}
		@Override
		public void treeWillExpand(TreeExpansionEvent e) throws ExpandVetoException {
			boolean veto = false;
			if (veto) {
				throw new ExpandVetoException(e);
			}
		}
		@Override
		public void treeWillCollapse(TreeExpansionEvent e) throws ExpandVetoException {
			JTree tree = (JTree) e.getSource();
			TreePath path = e.getPath();
			boolean veto = false;
			if(path.getParentPath() == null) {
				veto = true;
			}
			if (veto) {
				throw new ExpandVetoException(e);
			}
		}
	}


/**
 * EditSpeciesDialog constructor comment.
 */
public SpeciesPropertiesPanel() {
	super();
	initialize();
}

/**
 * Gets the speciesContext property (cbit.vcell.model.SpeciesContext) value.
 * @return The speciesContext property value.
 * @see #setSpeciesContext
 */
public SpeciesContext getSpeciesContext() {
	return fieldSpeciesContext;
}

/**
 * Called whenever the part throws an exception.
 * @param exception java.lang.Throwable
 */
private void handleException(java.lang.Throwable exception) {

	/* Uncomment the following lines to print uncaught exceptions to stdout */
	System.out.println("--------- UNCAUGHT EXCEPTION ---------");
	exception.printStackTrace(System.out);
}

/**
 * Initializes connections
 * @exception java.lang.Exception The exception description.
 */
private void initConnections() throws java.lang.Exception {
	annotationTextArea.addFocusListener(eventHandler);
	annotationTextArea.addMouseListener(eventHandler);
	nameTextField.addFocusListener(eventHandler);
	getPCLinkValueEditorPane().addHyperlinkListener(eventHandler);
}

/**
 * Initialize the class.
 */
private void initialize() {
	try {
		shapePanel = new JPanel() {		// glyph (shape) panel
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				if(sps != null) {
					sps.paintSelf(g);
				}
			}
		};
		shapePanel.setBackground(Color.white);		
//		Dimension ms = new Dimension(350, 80);
//		shapePanel.setMinimumSize(ms);
				
		// ----------------------------------------------------------------------------------
		JPanel leftPanel = new JPanel();
		GridBagLayout mgr = new GridBagLayout();
		mgr.rowHeights = new int[] { 100,100 };
		leftPanel.setLayout(mgr);
		leftPanel.setBackground(Color.white);
		
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new GridBagLayout());
		rightPanel.setBackground(Color.white);		
		
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(350);
		splitPane.setResizeWeight(0.9);
		splitPane.setLeftComponent(leftPanel);
		splitPane.setRightComponent(rightPanel);
				
		speciesPropertiesTree = new BioModelNodeEditableTree();
		speciesPropertiesTreeModel = new SpeciesPropertiesTreeModel(speciesPropertiesTree);
//		speciesPropertiesTreeModel.setBioModel(bioModel);
		speciesPropertiesTree.setModel(speciesPropertiesTreeModel);
		RbmSpeciesContextTreeCellRenderer crsc = new RbmSpeciesContextTreeCellRenderer();
		speciesPropertiesTree.setCellRenderer(crsc);
		DisabledTreeCellEditor dtce =  new DisabledTreeCellEditor(speciesPropertiesTree, (crsc));
		speciesPropertiesTree.setCellEditor(dtce);
		speciesPropertiesTree.setEditable(false);
		
//		speciesPropertiesTree.setCellRenderer(new RbmTreeCellRenderer());
//		speciesPropertiesTree.setCellEditor(new RbmTreeCellEditor(speciesPropertiesTree));
		int rowHeight = speciesPropertiesTree.getRowHeight();
		if (rowHeight < 10) { 
			rowHeight = 20; 
		}
		speciesPropertiesTree.setRowHeight(rowHeight + 5);
		speciesPropertiesTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		ToolTipManager.sharedInstance().registerComponent(speciesPropertiesTree);
		speciesPropertiesTree.addTreeSelectionListener(eventHandler);
		speciesPropertiesTree.addTreeWillExpandListener(eventHandler);
		speciesPropertiesTree.addMouseListener(eventHandler);
		speciesPropertiesTree.setLargeModel(true);
		speciesPropertiesTree.setRootVisible(true);
		
		
		
		JPanel generalPanel = new JPanel();
		generalPanel.setLayout(new GridBagLayout());
		Dimension size = new Dimension(100, 150);
		generalPanel.setMinimumSize(size);

		nameTextField = new JTextField();
		nameTextField.setEditable(false);
		nameTextField.addActionListener(eventHandler);
		
		int gridy = 0;
		GridBagConstraints gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 0; 
		gbc.gridy = gridy;
		gbc.insets = new Insets(0, 4, 4, 4);
		gbc.anchor = GridBagConstraints.LINE_END;		
		JLabel label = new JLabel("Species Name");
		generalPanel.add(label, gbc);
		
		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 1; 
		gbc.gridy = gridy;
		gbc.weightx = 1.0;
		gbc.fill = java.awt.GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 4, 4, 4);
		gbc.anchor = GridBagConstraints.LINE_START;		
		generalPanel.add(nameTextField, gbc);
		
		gridy ++;
		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 0; 
		gbc.gridy = gridy;
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.FIRST_LINE_END;
		generalPanel.add(new JLabel("Linked Pathway Object(s)"), gbc);

		linkedPOScrollPane = new JScrollPane();
		
		gbc = new java.awt.GridBagConstraints();
		gbc.weightx = 1.0;
		gbc.weighty = 0.1;
		gbc.gridx = 1; 
		gbc.gridy = gridy;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = java.awt.GridBagConstraints.BOTH;
		gbc.insets = new Insets(4, 4, 4, 4);
		generalPanel.add(linkedPOScrollPane, gbc);
		
		gridy ++;
		gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 0; 
		gbc.gridy = gridy;
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.FIRST_LINE_END;
		generalPanel.add(new JLabel("Annotation"), gbc);

		annotationTextArea = new javax.swing.JTextArea("", 1, 30);
		annotationTextArea.setLineWrap(true);
		annotationTextArea.setWrapStyleWord(true);
		annotationTextArea.setEditable(false);
		javax.swing.JScrollPane jsp = new javax.swing.JScrollPane(annotationTextArea);
		
		gbc = new java.awt.GridBagConstraints();
		gbc.weightx = 1.0;
		gbc.weighty = 0.1;
		gbc.gridx = 1; 
		gbc.gridy = gridy;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = java.awt.GridBagConstraints.BOTH;
		gbc.insets = new Insets(4, 4, 4, 4);
		generalPanel.add(jsp, gbc);
	
		int gridy2 = 0;
		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.gridx = 0;
		gbc2.gridy = gridy2;
		gbc2.weightx = 1.0;
		gbc2.weighty = 1.0;
		gbc2.fill = GridBagConstraints.BOTH;
		rightPanel.add(new JScrollPane(speciesPropertiesTree), gbc2);

		
		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.gridx = 0;
        gbc1.gridy = 0;
        gbc1.gridwidth = 1;
        gbc1.weightx = 1;
        gbc1.weighty = 1;
        gbc1.fill = GridBagConstraints.BOTH;
        leftPanel.add(generalPanel, gbc1);

//		gbc1 = new GridBagConstraints();
        gbc1.gridx = 0;
        gbc1.gridy = 1;
        gbc1.weightx = 1;
        gbc1.weighty = 0.1;
        gbc1.fill = GridBagConstraints.BOTH;
        leftPanel.add(shapePanel, gbc1); 
		
//		Dimension minimumSize = new Dimension(100, 150);		//provide minimum sizes for the two components in the split pane
//		splitPane.setMinimumSize(minimumSize);
//		leftPanel.setMinimumSize(minimumSize);
//		rightPanel.setMinimumSize(minimumSize);
		
		
		setName("SpeciesEditorPanel");
		setLayout(new BorderLayout());
		add(splitPane, BorderLayout.CENTER);
		setBackground(Color.white);
		
		initConnections();
		
	} catch (java.lang.Throwable ivjExc) {
		handleException(ivjExc);
	}
}

/**
 * Comment
 */
private void changeFreeTextAnnotation() {
	try{
		if (getSpeciesContext() == null) {
			return;
		}
		// set text from annotationTextField in free text annotation for species in vcMetaData (from model)
		if(bioModel.getModel() != null && bioModel.getModel().getVcMetaData() != null){
			VCMetaData vcMetaData = bioModel.getModel().getVcMetaData();
			String textAreaStr = (annotationTextArea.getText() == null || annotationTextArea.getText().length()==0?null:annotationTextArea.getText());
			if(!Compare.isEqualOrNull(vcMetaData.getFreeTextAnnotation(getSpeciesContext().getSpecies()),textAreaStr)){
				vcMetaData.setFreeTextAnnotation(getSpeciesContext().getSpecies(), textAreaStr);	
			}
		}
	} catch(Exception e){
		e.printStackTrace(System.out);
		PopupGenerator.showErrorDialog(this,"Edit Species Error\n"+e.getMessage(), e);
	}
}

// wei's code
private String listLinkedPathwayObjects(){
	if (getSpeciesContext() == null) {
		return "no selected species";
	}
	if(bioModel == null || bioModel.getModel() == null){
		return "no biomodel";
	}
	JPanel panel = new JPanel();
	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	String linkedPOlist = "";
	for(RelationshipObject relObject : bioModel.getRelationshipModel().getRelationshipObjects(getSpeciesContext())){
		final BioPaxObject bpObject = relObject.getBioPaxObject();
		if(bpObject instanceof Entity){
			JLabel label = new JLabel("<html><u>" + ((Entity)bpObject).getName().get(0) + "</u></html>");
			label.setForeground(Color.blue);
			label.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2) {
						selectionManager.followHyperlink(new ActiveView(null,DocumentEditorTreeFolderClass.PATHWAY_DIAGRAM_NODE, ActiveViewID.pathway_diagram),new Object[]{bpObject});
					}
				}
			});
			panel.add(label);
		}
		
	}
	linkedPOScrollPane.setViewportView(panel);
	return linkedPOlist;
}
// done

public synchronized void addActionListener(ActionListener l) {
    listenerList.add(ActionListener.class, l);
}

public void setBioModel(BioModel newValue) {
	if (bioModel == newValue) {
		return;
	}
	bioModel = newValue;
	speciesPropertiesTreeModel.setBioModel(bioModel);
}


/**
 * Sets the speciesContext property (cbit.vcell.model.SpeciesContext) value.
 * @param newValue The new value for the property.
 * @see #getSpeciesContext
 */
void setSpeciesContext(SpeciesContext newValue) {
	if (fieldSpeciesContext == newValue) {
		return;
	}
	SpeciesContext oldValue = fieldSpeciesContext;
	if (oldValue != null) {
		oldValue.removePropertyChangeListener(eventHandler);
	}
	// commit the changes before switch to another species
	changeName();
	changeFreeTextAnnotation();
	
	fieldSpeciesContext = newValue;
	if (newValue != null) {
		newValue.addPropertyChangeListener(eventHandler);
	}
	speciesPropertiesTreeModel.setSpeciesContext(fieldSpeciesContext);
	updateInterface();
}

/**
 * Comment
 */
private void updateInterface() {
	boolean bNonNullSpeciesContext = fieldSpeciesContext != null && bioModel != null;
	annotationTextArea.setEditable(bNonNullSpeciesContext);
	nameTextField.setEditable(bNonNullSpeciesContext);
	if (bNonNullSpeciesContext) {
		nameTextField.setText(getSpeciesContext().getName());
		annotationTextArea.setText(bioModel.getModel().getVcMetaData().getFreeTextAnnotation(getSpeciesContext().getSpecies()));
		updatePCLink();		
	} else {
		annotationTextArea.setText(null);
		getPCLinkValueEditorPane().setText(null);
		nameTextField.setText(null);
	}
	listLinkedPathwayObjects();
	if(fieldSpeciesContext!= null && fieldSpeciesContext.getSpeciesPattern() != null) {
		SpeciesPattern sp = fieldSpeciesContext.getSpeciesPattern();
		Graphics panelContext = shapePanel.getGraphics();
		sps = new SpeciesPatternShape(20, 5, fieldSpeciesContext, sp, panelContext);
		shapePanel.repaint();
	}
}

	private JEditorPane getPCLinkValueEditorPane() {
		if (PCLinkValueEditorPane == null) {
			PCLinkValueEditorPane = new JEditorPane();
			PCLinkValueEditorPane.setContentType("text/html");
			PCLinkValueEditorPane.setEditable(false);
			PCLinkValueEditorPane.setBackground(UIManager.getColor("TextField.inactiveBackground"));
			PCLinkValueEditorPane.setText(null);
		}
		return PCLinkValueEditorPane;
	}

	private void changeName() {
		if (fieldSpeciesContext == null) {
			return;
		}
		String newName = nameTextField.getText();
		if (newName == null || newName.length() == 0) {
			nameTextField.setText(getSpeciesContext().getName());
			return;
		}
		if (newName.equals(fieldSpeciesContext.getName())) {
			return;
		}
		try {
			getSpeciesContext().setName(newName);
		} catch (PropertyVetoException e1) {
			e1.printStackTrace();
			DialogUtils.showErrorDialog(SpeciesPropertiesPanel.this, e1.getMessage());
		}
	}

	@Override
	protected void onSelectedObjectsChange(Object[] selectedObjects) {
		if (selectedObjects == null || selectedObjects.length != 1) {
			return;
		}
		if (selectedObjects[0] instanceof SpeciesContext) {
			setSpeciesContext((SpeciesContext) selectedObjects[0]);
		} else {
			setSpeciesContext(null);
		}		
	}
	
	@Override
	public void setSelectionManager(SelectionManager selectionManager) {
		super.setSelectionManager(selectionManager);
	}
	
	//
	// --- speciesPropertiesTree stuff
	//
	private void selectClickPath(MouseEvent e) {
		Point mousePoint = e.getPoint();
		TreePath clickPath = speciesPropertiesTree.getPathForLocation(mousePoint.x, mousePoint.y);
	    if (clickPath == null) {
	    	return; 
	    }
		Object rightClickNode = clickPath.getLastPathComponent();
		if (rightClickNode == null || !(rightClickNode instanceof BioModelNode)) {
			return;
		}
		TreePath[] selectedPaths = speciesPropertiesTree.getSelectionPaths();
		if (selectedPaths == null || selectedPaths.length == 0) {
			return;
		} 
		boolean bFound = false;
		for (TreePath tp : selectedPaths) {
			if (tp.equals(clickPath)) {
				bFound = true;
				break;
			}
		}
		if (!bFound) {
			speciesPropertiesTree.setSelectionPath(clickPath);
		}
	}
	
	private JMenu getAddMenu() {
		if (addMenu == null) {
			addMenu = new JMenu("Add");
			addMenu.addActionListener(eventHandler);
		}
		return addMenu;
	}
	
	private JMenuItem getRenameMenuItem() {
		if (renameMenuItem == null) {
			renameMenuItem = new JMenuItem("Rename");
			renameMenuItem.addActionListener(eventHandler);
		}
		return renameMenuItem;
	}
	
	private JMenuItem getDeleteMenuItem() {
		if (deleteMenuItem == null) {
			deleteMenuItem = new JMenuItem("Delete");
			deleteMenuItem.addActionListener(eventHandler);
		}
		return deleteMenuItem;
	}
	
	private JMenuItem getEditMenuItem() {
		if (editMenuItem == null) {
			editMenuItem = new JMenuItem("Edit");
			editMenuItem.addActionListener(eventHandler);
		}
		return editMenuItem;
	}

	public void addNew() {
		Object obj = speciesPropertiesTree.getLastSelectedPathComponent();
		if (obj == null || !(obj instanceof BioModelNode)) {
			return;
		}
		BioModelNode selectedNode = (BioModelNode) obj;
		Object selectedUserObject = selectedNode.getUserObject();
		if (selectedUserObject == fieldSpeciesContext){
//			MolecularComponent molecularComponent = molecularType.createMolecularComponent();
//			molecularType.addMolecularComponent(molecularComponent);
//			speciesPropertiesTree.startEditingAtPath(speciesPropertiesTreeModel.findObjectPath(null, molecularComponent));
//			System.out.println("Functionality not implemented yet");
			throw new RuntimeException("Functionality not implemented yet");
			
		} else if (selectedUserObject instanceof MolecularComponent){
			MolecularComponent molecularComponent = (MolecularComponent) selectedUserObject;
			// TODO: anything to do about ComponentStatePattern ???
			ComponentStateDefinition componentStateDefinition = molecularComponent.createComponentStateDefinition();
			molecularComponent.addComponentStateDefinition(componentStateDefinition);
			speciesPropertiesTree.startEditingAtPath(speciesPropertiesTreeModel.findObjectPath(null, componentStateDefinition));
		}	
	}

//	public void addSpeciesPattern() {
//		SpeciesPattern sp = new SpeciesPattern();
//		fieldSpeciesContext.setSpeciesPattern(sp);
//		final TreePath path = speciesPropertiesTreeModel.findObjectPath(null, fieldSpeciesContext);
//		speciesPropertiesTree.setSelectionPath(path);
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {				
//				speciesPropertiesTree.scrollPathToVisible(path);
//			}
//		});
//	}

	public void delete() {
		Object obj = speciesPropertiesTree.getLastSelectedPathComponent();
		if (obj == null || !(obj instanceof BioModelNode)) {
			return;
		}
		BioModelNode selectedNode = (BioModelNode) obj;
		TreeNode parent = selectedNode.getParent();
		if (!(parent instanceof BioModelNode)) {
			return;
		}
		BioModelNode parentNode = (BioModelNode) parent;
		Object selectedUserObject = selectedNode.getUserObject();
		if(selectedUserObject instanceof MolecularTypePattern) {
			System.out.println("deleting molecular type pattern");
			MolecularTypePattern mtp = (MolecularTypePattern) selectedUserObject;
			SpeciesPattern sp = fieldSpeciesContext.getSpeciesPattern();
			sp.removeMolecularTypePattern(mtp);
			if(sp.getMolecularTypePatterns().isEmpty()) {
				fieldSpeciesContext.setSpeciesPattern(null);
			} else {
				sp.resolveBonds();
			}
			final TreePath path = speciesPropertiesTreeModel.findObjectPath(null, fieldSpeciesContext);
			speciesPropertiesTree.setSelectionPath(path);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					speciesPropertiesTreeModel.populateTree();
					speciesPropertiesTree.scrollPathToVisible(path);	// this doesn't seem to work ?
				}
			});
// We should not delete components of species, we always must have them all
//		} else if(selectedUserObject instanceof MolecularComponentPattern) {
//			MolecularComponentPattern mcp = (MolecularComponentPattern) selectedUserObject;
//			Object parentUserObject = parentNode.getUserObject();
//			MolecularTypePattern mtp = (MolecularTypePattern)parentUserObject;
//			mtp.removeMolecularComponentPattern(mcp);
//			System.out.println("deleting MolecularComponentPattern " + mcp.getMolecularComponent().getName());
//			parent = parentNode.getParent();
//			parentNode = (BioModelNode) parent;
//			parentUserObject = parentNode.getUserObject();
//			SpeciesContext sc = (SpeciesContext)parentUserObject;
//			SpeciesPattern sp = sc.getSpeciesPattern();
//			if(!sp.getMolecularTypePatterns().isEmpty()) {
//				sp.resolveBonds();
//			}
//			final TreePath path = speciesPropertiesTreeModel.findObjectPath(null, sc);
//			speciesPropertiesTree.setSelectionPath(path);
//			SwingUtilities.invokeLater(new Runnable() {
//				public void run() {
//					speciesPropertiesTreeModel.populateTree();
//					speciesPropertiesTree.scrollPathToVisible(path);	// this doesn't seem to work ?
//				}
//			});
		} else {
			System.out.println("deleting " + selectedUserObject.toString());
		}
	}

	private void showPopupMenu(MouseEvent e) {
		if (!e.isPopupTrigger()) {
			return;
		}
		if (popupMenu == null) {
			popupMenu = new JPopupMenu();			
		}		
		if (popupMenu.isShowing()) {
			return;
		}
		
		boolean bDelete = false;
		boolean bAdd = false;
		boolean bEdit = false;
		boolean bRename = false;
		popupMenu.removeAll();
		Point mousePoint = e.getPoint();

		// TODO: may need to implement own version of this, like in MolecularTypePropertiesPanel
		GuiUtils.selectClickTreePath(speciesPropertiesTree, e);
		
		TreePath clickPath = speciesPropertiesTree.getPathForLocation(mousePoint.x, mousePoint.y);
	    if (clickPath == null) {
	    	popupMenu.add(getAddMenu());
	    	
			for (final MolecularType mt : bioModel.getModel().getRbmModelContainer().getMolecularTypeList()) {
				JMenuItem menuItem = new JMenuItem(mt.getName());
				getAddMenu().add(menuItem);
				menuItem.addActionListener(new ActionListener() {
					
					public void actionPerformed(ActionEvent e) {
						MolecularTypePattern molecularTypePattern = new MolecularTypePattern(mt);
						fieldSpeciesContext.getSpeciesPattern().addMolecularTypePattern(molecularTypePattern);
						final TreePath path = speciesPropertiesTreeModel.findObjectPath(null, molecularTypePattern);
						speciesPropertiesTree.setSelectionPath(path);
						SwingUtilities.invokeLater(new Runnable() {
							
							public void run() {				
								speciesPropertiesTree.scrollPathToVisible(path);
							}
						});
					}
				});
			}
	    	popupMenu.show((Component) e.getSource(), mousePoint.x, mousePoint.y);
	    	return;
	    }
		
		TreePath[] selectedPaths = speciesPropertiesTree.getSelectionPaths();
		if (selectedPaths == null) {
			return;
		}
		for (TreePath tp : selectedPaths) {
			Object obj = tp.getLastPathComponent();
			if (obj == null || !(obj instanceof BioModelNode)) {
				continue;
			}
			
			BioModelNode selectedNode = (BioModelNode) obj;
			final Object userObject = selectedNode.getUserObject();
			if (userObject instanceof SpeciesContext) {
				final SpeciesContext sc = (SpeciesContext)userObject;
				getAddMenu().setText(VCellErrorMessages.SpecifySpeciesTypes);
				getAddMenu().removeAll();
				for (final MolecularType mt : bioModel.getModel().getRbmModelContainer().getMolecularTypeList()) {
					JMenuItem menuItem = new JMenuItem(mt.getName());
					getAddMenu().add(menuItem);
					menuItem.addActionListener(new ActionListener() {
						
						public void actionPerformed(ActionEvent e) {
							MolecularTypePattern molecularTypePattern = new MolecularTypePattern(mt);
							for(MolecularComponentPattern mcp : molecularTypePattern.getComponentPatternList()) {
								mcp.setBondType(BondType.None);
							}
							if(sc.getSpeciesPattern() == null) {
								SpeciesPattern sp = new SpeciesPattern();
								sc.setSpeciesPattern(sp);
							}
							sc.getSpeciesPattern().addMolecularTypePattern(molecularTypePattern);
							final TreePath path = speciesPropertiesTreeModel.findObjectPath(null, sc);
							speciesPropertiesTree.setSelectionPath(path);
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									speciesPropertiesTreeModel.populateTree();
									speciesPropertiesTree.scrollPathToVisible(path);
								}
							});
						}
					});
				}
				bAdd = true;
				bDelete = true;
			} else if (userObject instanceof MolecularTypePattern) {
				bDelete = true;
			} else if (userObject instanceof MolecularComponentPattern) {
				manageComponentPattern(speciesPropertiesTreeModel, speciesPropertiesTree, selectedNode, userObject);
				bDelete = false;	// can't delete components, we need to always have them all
			} else if (userObject instanceof MolecularComponent) {
				getEditMenuItem().setText("Edit Pattern");
				bEdit = true;
			} else {
				System.out.println("Unexpected object type: " + userObject);
			}
		}
		// everything can be renamed
		if (bRename) {
			popupMenu.add(getRenameMenuItem());
		}
		if (bDelete) {
			popupMenu.add(getDeleteMenuItem());
		}
		if (bEdit) {
			popupMenu.add(getEditMenuItem());
		}
		if (bAdd) {
			popupMenu.add(new JSeparator());
			popupMenu.add(getAddMenu());
		}
		popupMenu.show(speciesPropertiesTree, mousePoint.x, mousePoint.y);
	}

	public void manageComponentPattern(final SpeciesPropertiesTreeModel treeModel, final JTree tree,
			BioModelNode selectedNode, final Object selectedObject) {
		popupMenu.removeAll();
		final MolecularComponentPattern mcp = (MolecularComponentPattern)selectedObject;
		final MolecularComponent mc = mcp.getMolecularComponent();
		//
		// --- State
		//
		if(mc.getComponentStateDefinitions().size() != 0) {
			JMenu editStateMenu = new JMenu();
			editStateMenu.setText("Edit State");
			editStateMenu.removeAll();
			List<String> itemList = new ArrayList<String>();
			// Any is not an option for species			
//			itemList.add("Any");
			for (final ComponentStateDefinition csd : mc.getComponentStateDefinitions()) {
				String name = csd.getName();
				itemList.add(name);
			}
			for(String name : itemList) {
				JMenuItem menuItem = new JMenuItem(name);
				editStateMenu.add(menuItem);
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						String name = e.getActionCommand();
						if(name.equals("Any")) {
							ComponentStatePattern csp = new ComponentStatePattern();
							mcp.setComponentStatePattern(csp);
						} else {
							String csdName = e.getActionCommand();
							ComponentStateDefinition csd = mcp.getMolecularComponent().getComponentStateDefinition(csdName);
							if(csd == null) {
								throw new RuntimeException("Missing ComponentStateDefinition " + csdName + " for Component " + mcp.getMolecularComponent().getName());
							}
							ComponentStatePattern csp = new ComponentStatePattern(csd);
							mcp.setComponentStatePattern(csp);
						}
					}
				});
			}
			popupMenu.add(editStateMenu);
		}
		//
		// --- Bonds
		//						
		final MolecularTypePattern mtp;
		final SpeciesPattern sp;
		BioModelNode parentNode = (BioModelNode) selectedNode.getParent();
		Object parentObject = parentNode == null ? null : parentNode.getUserObject();
		if(parentObject != null && parentObject instanceof MolecularTypePattern) {
			mtp = (MolecularTypePattern)parentObject;
			parentNode = (BioModelNode) parentNode.getParent();
			parentObject = parentNode == null ? null : parentNode.getUserObject();
			if(parentObject != null && parentObject instanceof SpeciesContext) {
				sp = ((SpeciesContext)parentObject).getSpeciesPattern();
				// equivalent with using fieldSpeciesContext.getSpeciesPattern()
			} else {
				sp = null;
			}
		} else {
			mtp = null;
			sp = null;
		}
		
		JMenu editBondMenu = new JMenu();
		editBondMenu.setText("Edit Bond");
		editBondMenu.removeAll();
		final Map<String, Bond> itemMap = new LinkedHashMap<String, Bond>();
		
		final String noneString = "<html><b>" + BondType.None.symbol + "</b> " + BondType.None.name() + "</html>";
		// possible and exists are not options here
//		final String existsString = "<html><b>" + BondType.Exists.symbol + "</b> " + BondType.Exists.name() + "</html>";
//		final String possibleString = "<html><b>" + BondType.Possible.symbol + "</b> " + BondType.Possible.name() + "</html>";
		itemMap.put(noneString, null);
//		itemMap.put(existsString, null);
//		itemMap.put(possibleString, null);
		if(mtp != null && sp != null) {
			List<Bond> bondPartnerChoices = sp.getAllBondPartnerChoices(mtp, mc);
			for(Bond b : bondPartnerChoices) {
				if(b.equals(mcp.getBond())) {
					continue;	// if the mcp has a bond already we don't offer it
				}
				int index = 0;
				if(mcp.getBondType() == BondType.Specified) {
					index = mcp.getBondId();
				} else {
					index = sp.nextBondId();
				}
				itemMap.put(b.toHtmlStringLong(sp, index), b);
			}
		}
		for(String name : itemMap.keySet()) {
			JMenuItem menuItem = new JMenuItem(name);
			editBondMenu.add(menuItem);
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String name = e.getActionCommand();
					BondType btBefore = mcp.getBondType();
					if(name.equals(noneString)) {
						if(btBefore == BondType.Specified) {	// specified -> not specified
							// change the partner to none
							mcp.getBond().molecularComponentPattern.setBondType(BondType.None);
							mcp.getBond().molecularComponentPattern.setBond(null);
						}
						mcp.setBondType(BondType.None);
						mcp.setBond(null);
						treeModel.populateTree();
//					} else if(name.equals(existsString)) {
//						if(btBefore == BondType.Specified) {	// specified -> not specified
//							// change the partner to possible
//							mcp.getBond().molecularComponentPattern.setBondType(BondType.Possible);
//							mcp.getBond().molecularComponentPattern.setBond(null);
//						}
//						mcp.setBondType(BondType.Exists);
//						mcp.setBond(null);
//						treeModel.populateTree();
//					} else if(name.equals(possibleString)) {
//						if(btBefore == BondType.Specified) {	// specified -> not specified
//							// change the partner to possible
//							mcp.getBond().molecularComponentPattern.setBondType(BondType.Possible);
//							mcp.getBond().molecularComponentPattern.setBond(null);
//						}
//						mcp.setBondType(BondType.Possible);
//						mcp.setBond(null);
//						treeModel.populateTree();
					} else {
						if (btBefore != BondType.Specified) {
							// if we go from a non-specified to a specified we need to find the next available
							// bond id, so that we can choose the color for displaying the bond
							// a bad bond id, like -1, will crash badly when trying to choose the color
							int bondId = sp.nextBondId();
							mcp.setBondId(bondId);
						} else {
							// specified -> specified
							// change the old partner to none, continue using the bond id
							mcp.getBond().molecularComponentPattern.setBondType(BondType.None);
							mcp.getBond().molecularComponentPattern.setBond(null);
						}
						mcp.setBondType(BondType.Specified);
						Bond b = itemMap.get(name);
						mcp.setBond(b);
						mcp.getBond().molecularComponentPattern.setBondId(mcp.getBondId());
						sp.resolveBonds();

						final TreePath path = treeModel.findObjectPath(null, mcp);
						treeModel.populateTree();
						tree.setSelectionPath(path);
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {				
								tree.scrollPathToVisible(path);
							}
						});
					}

				}
			});
		}
		popupMenu.add(editBondMenu);
	}

}
