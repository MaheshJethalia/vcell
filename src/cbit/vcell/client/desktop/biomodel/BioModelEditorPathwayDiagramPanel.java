package cbit.vcell.client.desktop.biomodel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.vcell.pathway.BioPaxObject;
import org.vcell.pathway.PathwayEvent;
import org.vcell.pathway.PathwayListener;
import org.vcell.pathway.PathwayModel;
import org.vcell.util.gui.DialogUtils;

import cbit.gui.graph.GraphModel;
import cbit.gui.graph.GraphPane;
import cbit.vcell.biomodel.BioModel;
import cbit.vcell.biomodel.meta.VCMetaData.AnnotationEvent;
import cbit.vcell.biomodel.meta.VCMetaData.AnnotationEventListener;
import cbit.vcell.client.desktop.biomodel.pathway.PathwayGraphModel;
import cbit.vcell.client.desktop.biomodel.pathway.PathwayGraphTool;

@SuppressWarnings("serial")
public class BioModelEditorPathwayDiagramPanel extends DocumentEditorSubPanel implements PathwayListener {
	
	private static final Dimension TOOLBAR_BUTTON_SIZE = new Dimension(28, 28);
	private EventHandler eventHandler = new EventHandler();
	private BioModel bioModel = null;
	private GraphPane graphPane;
	private PathwayGraphTool graphCartoonTool;
	private JTextArea sourceTextArea = null;
	private JButton bringItInButton = null; // wei's code
	private ConversionPanel conversionPanel = new ConversionPanel(); // wei's code
	
	private class EventHandler implements ActionListener, ListSelectionListener, AnnotationEventListener, PropertyChangeListener {

		public void actionPerformed(ActionEvent e) {
			// wei's code
			if(e.getSource() == bringItInButton){
				bringItIn();
			}
			// done
		}
		public void valueChanged(ListSelectionEvent e) {
		}
		public void annotationChanged(AnnotationEvent annotationEvent) {
			refreshInterface();
		}
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals(GraphModel.PROPERTY_NAME_SELECTED)) {
				Object[] selectedObjects = (Object[]) evt.getNewValue();
				setSelectedObjects(selectedObjects);
			}			
		}
	}
	
	public BioModelEditorPathwayDiagramPanel() {
		super();
		initialize();
	}
	
	private JButton createToolBarButton(String[] attributes) {
		JButton button = new JButton();
		button.setMaximumSize(TOOLBAR_BUTTON_SIZE);
		button.setPreferredSize(TOOLBAR_BUTTON_SIZE);
		button.setMinimumSize(TOOLBAR_BUTTON_SIZE);
		button.setMargin(new Insets(2, 2, 2, 2));
		button.setToolTipText(attributes[1]);
		button.setIcon(new ImageIcon(getClass().getResource("/sybil/images/" + attributes[3])));
		return button;
	}

	private JToolBar createToolBar(String[][] layouts, int orientation) {
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setBorder(new javax.swing.border.EtchedBorder());
		toolBar.setOrientation(orientation);
		for (int i = 0; i < layouts.length; i ++) {
			toolBar.add(createToolBarButton(layouts[i]));
		}
		return toolBar;
	}
	
	private void initialize() {
		String[][] layouts = new String[][] {
				new String[]{"Select", "Select", "Select parts of the graph", "layout/select.gif"},
				new String[]{"Zoom In", "Zoom In", "Make graph look bigger", "layout/zoomin.gif"},
				new String[]{"Zoom Out", "Zoom Out", "Make graph look smaller", "layout/zoomout.gif"},
				new String[]{"Random", "Random Layout", "Reconfigure graph randomly", "layout/random.gif"},
				new String[]{"Circular", "Circular Layout", "Reconfigure graph circular", "layout/circular.gif"},
				new String[]{"Annealed", "Annealed Layout", "Reconfigure graph by annealing", "layout/annealed.gif"},
				new String[]{"Levelled", "Levelled Layout", "Reconfigure graph in levels", "layout/levelled.gif"},
				new String[]{"Relaxed", "Relaxed Layout", "Reconfigure graph by relaxing", "layout/relaxed.gif"},
				new String[]{"Reactions Only", "Reactions Only", "Show only Reactions",	"layout/level1.gif"},
				new String[]{"Reaction Network", "Reaction Network", "Reaction Network", "layout/level2.gif"},
				new String[]{"Components", "Components", "Reactions, entities and components", "layout/level3.gif"},								
		};

		String[][] interactions = new String[][] {
				new String[]{"Reaction", "Biochemical Reaction", "Biochemical Reaction", 
						"biopax/biochemicalReaction.gif"},
				new String[]{"Transport", "Transport", "Transport", "biopax/transport.gif"},
				new String[]{"Reaction WT", "Biochemical Reaction with Transport", 
						"Biochemical Reaction with Transport", 
						"biopax/transportWithBiochemicalReaction.gif"},
				new String[]{"Entity", "Physical Entity", "Physical Entity", "biopax/entity.gif"},
				new String[]{"Small Molecule", "Small Molecule", "Small Molecule", "biopax/smallMolecule.gif"},
				new String[]{"Protein", "Protein", "Protein", "biopax/protein.gif"},
				new String[]{"Complex", "Complex", "Complex", "biopax/complex.gif"},
				new String[]{"Participant", "Participant", "Physical Entity Participant", "biopax/modification.gif"},
		};
		
		JToolBar layoutToolBar = createToolBar(layouts, javax.swing.SwingConstants.VERTICAL);
		JToolBar nodesToolBar = createToolBar(interactions, javax.swing.SwingConstants.HORIZONTAL);
		sourceTextArea = new JTextArea();		
		graphPane =  new GraphPane();
		graphCartoonTool = new PathwayGraphTool();
		graphCartoonTool.setGraphPane(graphPane);		
		
		// wei's code: add a button for demo. please redesign it for better User interface
		JPanel layoutPanel = new JPanel();
		bringItInButton = new JButton("Bring It In");
		bringItInButton.addActionListener(eventHandler);
		layoutPanel.add(nodesToolBar, BorderLayout.WEST);
		layoutPanel.add(bringItInButton, BorderLayout.EAST);	
		// done
		
		JPanel graphTabPanel = new JPanel(new BorderLayout());
		JScrollPane graphScrollPane = new JScrollPane(graphPane);
		graphScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		graphScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		graphTabPanel.add(graphScrollPane, BorderLayout.CENTER);
		graphTabPanel.add(layoutToolBar, BorderLayout.WEST);
		graphTabPanel.add(layoutPanel, BorderLayout.NORTH);// wei's code
// wei		graphTabPanel.add(nodesToolBar, BorderLayout.NORTH);
		
		JPanel sourceTabPanel = new JPanel(new BorderLayout());
		sourceTabPanel.add(new JScrollPane(sourceTextArea), BorderLayout.CENTER);
		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Diagram", graphTabPanel);
		tabbedPane.addTab("BioPAX Source", sourceTabPanel);
		
		setLayout(new BorderLayout());
		add(tabbedPane, BorderLayout.CENTER);
	}

	@Override
	protected void onSelectedObjectsChange(Object[] selectedObjects) {
	}


	private void refreshInterface() {
		if (bioModel == null) {
			return;
		}
		try {
			PathwayModel pathwayModel = bioModel.getPathwayModel();
			GraphModel oldModel = graphPane.getGraphModel();
			if (oldModel != null) {
				oldModel.removePropertyChangeListener(eventHandler);
			}
			PathwayGraphModel pathwayGraphModel = new PathwayGraphModel();
			pathwayGraphModel.addPropertyChangeListener(eventHandler);
			graphPane.setGraphModel(pathwayGraphModel);
			pathwayGraphModel.setPathwayModel(pathwayModel);
			sourceTextArea.setText("======Summary View========\n\n"+pathwayModel.show(false)+"\n"+"======Detailed View========\n\n"+pathwayModel.show(true)+"\n");
		} catch (Exception ex) {
			ex.printStackTrace();
			DialogUtils.showErrorDialog(this, ex.getMessage());
		}
	}

	public void setBioModel(BioModel bioModel) {
		if (this.bioModel == bioModel) {
			return;
		}
		if (bioModel!=null){
			bioModel.getPathwayModel().removePathwayListener(this);
		}
		this.bioModel = bioModel;
		if (this.bioModel!=null){
			this.bioModel.getPathwayModel().addPathwayListener(this);
		}
		conversionPanel.setBioModel(bioModel);
		refreshInterface();
	}

	public void pathwayChanged(PathwayEvent event) {
		if (bioModel==null){
			sourceTextArea.setText("");
		}else{
			PathwayModel pathwayModel = bioModel.getPathwayModel();
			sourceTextArea.setText("======Summary View========\n\n"+pathwayModel.show(false)+"\n"+"======Detailed View========\n\n"+pathwayModel.show(true)+"\n");
		}
	}
	
	// wei's code
	public Object[] getSelectedBioPaxObjects(){
		return graphCartoonTool.getGraphModel().getSelectedObjects();
	}
	
	private void bringItIn(){
		if(graphCartoonTool.getGraphModel().getSelectedObjects().length < 1){
			return;
		}
		ArrayList<BioPaxObject> selectedObjects = new ArrayList<BioPaxObject>();
		for(int i = 0; i < graphCartoonTool.getGraphModel().getSelectedObjects().length; i++){
			if((graphCartoonTool.getGraphModel().getSelectedObjects()[i]) instanceof BioPaxObject)
				selectedObjects.add((BioPaxObject) (graphCartoonTool.getGraphModel().getSelectedObjects()[i]));			
		}
		if(selectedObjects.size() < 1){
			return;
		}
        //Create and set up the content pane.
        conversionPanel.setOpaque(true); //content panes must be opaque
        conversionPanel.setBioPaxObjects(selectedObjects);
        int returnCode = DialogUtils.showComponentOKCancelDialog(this, conversionPanel, "Convert to BioModel");
		if (returnCode == JOptionPane.OK_OPTION) {
			conversionPanel.bringItIn();
		}
	}
	
	public void setSelectionManager(SelectionManager selectionManager) {
		super.setSelectionManager(selectionManager);
		conversionPanel.setSelectionManager(selectionManager);
	}
	// done
}
