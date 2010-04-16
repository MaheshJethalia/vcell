package cbit.vcell.solver.ode.gui;
import java.awt.Component;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;

import org.vcell.util.BeanUtils;

import cbit.plot.Plot2D;
import cbit.plot.PlotData;
import cbit.plot.SingleXPlot2D;
import cbit.vcell.mapping.MathMapping;
import cbit.vcell.math.Constant;
import cbit.vcell.model.ReservedSymbol;
import cbit.vcell.parser.Expression;
import cbit.vcell.parser.ExpressionBindingException;
import cbit.vcell.parser.ExpressionException;
import cbit.vcell.parser.SymbolTable;
import cbit.vcell.parser.SymbolTableEntry;
import cbit.vcell.simdata.SimDataConstants;
import cbit.vcell.solver.ode.FunctionColumnDescription;
import cbit.vcell.solver.ode.ODESolverResultSet;
import cbit.vcell.util.ColumnDescription;

/**
 * Insert the type's description here.  What we want to do with this
 * is to pass in an ODESolverResultSet which contains everything needed
 * for this panel to run.  This necessitates being able to get variable
 * and sensitivity parameter info from the result set.  So, we need to
 * add that kind of interface to ODESolverResultSet.
 * Creation date: (8/13/2000 3:15:43 PM)
 * @author: John Wagner the Great
 */
/**
 * Amended March 12, 2007 to generate plot2D(instead of SingleXPlot2D) when multiple trials
 * are conducted, the plot2D is used to display the histogram.
 **/
public class ODESolverPlotSpecificationPanel extends JPanel {
	private JCheckBox ivjLogSensCheckbox = null;
	private JLabel ivjMaxLabel = null;
	private JLabel ivjMinLabel = null;
	private JPanel ivjSensitivityParameterPanel = null;
	private JLabel ivjXAxisLabel = null;
	private JList ivjYAxisChoice = null;
	private JLabel ivjYAxisLabel = null;
	private JLabel ivjCurLabel = null;
	private JSlider ivjSensitivityParameterSlider = null;
	private JScrollPane ivjJScrollPaneYAxis = null;
	private JLabel ivjJLabelSensitivityParameter = null;
	private JPanel ivjJPanelSensitivity = null;
	private ODESolverResultSet fieldOdeSolverResultSet = null;
	private DefaultListModel ivjDefaultListModelY = null;
	IvjEventHandler ivjEventHandler = new IvjEventHandler();
	private int[] plottableColumnIndices = new int[0];
	private String[] plottableNames = new String[0];
	private int fieldXIndex = -1;
	private int[] fieldYIndices = new int[0];
	private Plot2D fieldPlot2D = null;
	private java.lang.String[] resultSetColumnNames = null;
	private DefaultComboBoxModel ivjComboBoxModelX = null;
	private JComboBox ivjXAxisComboBox = null;
	private boolean ivjConnPtoP2Aligning = false;
	private ODESolverResultSet ivjodeSolverResultSet1 = null;
	
	private static ImageIcon function_icon = null;
	
	class IvjEventHandler implements java.awt.event.ActionListener, java.awt.event.ItemListener, java.beans.PropertyChangeListener, javax.swing.event.ChangeListener, javax.swing.event.ListSelectionListener {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (e.getSource() == ODESolverPlotSpecificationPanel.this.getLogSensCheckbox()) 
				connEtoC10(e);
		};
		public void itemStateChanged(java.awt.event.ItemEvent e) {
			if (e.getSource() == ODESolverPlotSpecificationPanel.this.getXAxisComboBox()) 
				connEtoC6(e);
		};
		public void propertyChange(java.beans.PropertyChangeEvent evt) {
			if (evt.getSource() == ODESolverPlotSpecificationPanel.this && (evt.getPropertyName().equals("odeSolverResultSet")))
			{
				connEtoC1(evt);
			}
			if (evt.getSource() == ODESolverPlotSpecificationPanel.this && (evt.getPropertyName().equals("xIndex"))) 
				connEtoC3(evt);
			if (evt.getSource() == ODESolverPlotSpecificationPanel.this && (evt.getPropertyName().equals("YIndices"))) 
				connEtoC4(evt);
			if (evt.getSource() == ODESolverPlotSpecificationPanel.this && (evt.getPropertyName().equals("odeSolverResultSet"))) 
				connPtoP2SetTarget();
			if (evt.getSource() == ODESolverPlotSpecificationPanel.this.getodeSolverResultSet1() && (evt.getPropertyName().equals("columnDescriptions"))) 
				connEtoC7(evt);
		};
		public void stateChanged(javax.swing.event.ChangeEvent e) {
			if (e.getSource() == ODESolverPlotSpecificationPanel.this.getSensitivityParameterSlider()) 
				connEtoC12(e);
		};
		public void valueChanged(javax.swing.event.ListSelectionEvent e) {
			if (e.getSource() == ODESolverPlotSpecificationPanel.this.getYAxisChoice()) 
				connEtoC2(e);
		};
	};
	private SymbolTable fieldSymbolTable = null;

/**
 * ODESolverPlotSpecificationPanel constructor comment.
 */
public ODESolverPlotSpecificationPanel() {
	super();
	initialize();
}

/**
 * connEtoC1:  (ODESolverPlotSpecificationPanel.odeSolverResultSet --> ODESolverPlotSpecificationPanel.setPlottableColumns(Lcbit.vcell.solver.ode.ODESolverResultSet;)V)
 * @param arg1 java.beans.PropertyChangeEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC1(java.beans.PropertyChangeEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.updateResultSet(this.getOdeSolverResultSet());
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}

/**
 * connEtoC10:  (LogSensCheckbox.action.actionPerformed(java.awt.event.ActionEvent) --> ODESolverPlotSpecificationPanel.regeneratePlot2D()V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC10(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.regeneratePlot2D();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connEtoC11:  (odeSolverResultSet1.this --> ODESolverPlotSpecificationPanel.initializeLogSensCheckBox()V)
 * @param value cbit.vcell.solver.ode.ODESolverResultSet
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC11(ODESolverResultSet value) {
	try {
		// user code begin {1}
		// user code end
		this.initializeLogSensCheckBox();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connEtoC12:  (SensitivityParameterSlider.change.stateChanged(javax.swing.event.ChangeEvent) --> ODESolverPlotSpecificationPanel.regeneratePlot2D()V)
 * @param arg1 javax.swing.event.ChangeEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC12(javax.swing.event.ChangeEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.regeneratePlot2D();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connEtoC13:  (odeSolverResultSet1.this --> ODESolverPlotSpecificationPanel.updateResultSet(Lcbit.vcell.solver.ode.ODESolverResultSet;)V)
 * @param value cbit.vcell.solver.ode.ODESolverResultSet
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC13(ODESolverResultSet value) {
	try {
		// user code begin {1}
		// user code end
		this.updateResultSet(getodeSolverResultSet1());
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connEtoC2:  (YAxisChoice.listSelection.valueChanged(javax.swing.event.ListSelectionEvent) --> ODESolverPlotSpecificationPanel.setYNamesFromList()V)
 * @param arg1 javax.swing.event.ListSelectionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC2(javax.swing.event.ListSelectionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.setYIndicesFromList();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}

/**
 * connEtoC3:  (ODESolverPlotSpecificationPanel.yIndices --> ODESolverPlotSpecificationPanel.refreshPlotData()V)
 * @param arg1 java.beans.PropertyChangeEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC3(java.beans.PropertyChangeEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.regeneratePlot2D();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}

/**
 * connEtoC4:  (ODESolverPlotSpecificationPanel.YIndices --> ODESolverPlotSpecificationPanel.refreshPlotData()V)
 * @param arg1 java.beans.PropertyChangeEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC4(java.beans.PropertyChangeEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		refreshVisiblePlots(getPlot2D());
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}

/**
 * connEtoC6:  (XAxisComboBox.item.itemStateChanged(java.awt.event.ItemEvent) --> ODESolverPlotSpecificationPanel.setXIndex(I)V)
 * @param arg1 java.awt.event.ItemEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC6(java.awt.event.ItemEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.setXIndex(getXAxisComboBox().getSelectedIndex());
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connEtoC7:  (odeSolverResultSet1.columnDescriptions --> ODESolverPlotSpecificationPanel.updateResultSet(Lcbit.vcell.solver.ode.ODESolverResultSet;)V)
 * @param arg1 java.beans.PropertyChangeEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC7(java.beans.PropertyChangeEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		if ((getodeSolverResultSet1() != null)) {
			this.updateResultSet(getodeSolverResultSet1());
		}
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connEtoC9:  (odeSolverResultSet1.this --> ODESolverPlotSpecificationPanel.enableLogSensitivity(Lcbit.vcell.solver.ode.ODESolverResultSet;)V)
 * @param value cbit.vcell.solver.ode.ODESolverResultSet
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC9(ODESolverResultSet value) {
	try {
		// user code begin {1}
		// user code end
		this.enableLogSensitivity();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}

/**
 * connPtoP1SetTarget:  (DefaultListModelY.this <--> YAxisChoice.model)
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connPtoP1SetTarget() {
	/* Set the target from the source */
	try {
		getYAxisChoice().setModel(getDefaultListModelY());
		// user code begin {1}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connPtoP2SetSource:  (ODESolverPlotSpecificationPanel.odeSolverResultSet <--> odeSolverResultSet1.this)
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connPtoP2SetSource() {
	/* Set the source from the target */
	try {
		if (ivjConnPtoP2Aligning == false) {
			// user code begin {1}
			// user code end
			ivjConnPtoP2Aligning = true;
			if ((getodeSolverResultSet1() != null)) {
				this.setOdeSolverResultSet(getodeSolverResultSet1());
			}
			// user code begin {2}
			// user code end
			ivjConnPtoP2Aligning = false;
		}
	} catch (java.lang.Throwable ivjExc) {
		ivjConnPtoP2Aligning = false;
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connPtoP2SetTarget:  (ODESolverPlotSpecificationPanel.odeSolverResultSet <--> odeSolverResultSet1.this)
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connPtoP2SetTarget() {
	/* Set the target from the source */
	try {
		if (ivjConnPtoP2Aligning == false) {
			// user code begin {1}
			// user code end
			ivjConnPtoP2Aligning = true;
			setodeSolverResultSet1(this.getOdeSolverResultSet());
			// user code begin {2}
			// user code end
			ivjConnPtoP2Aligning = false;
		}
	} catch (java.lang.Throwable ivjExc) {
		ivjConnPtoP2Aligning = false;
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * connPtoP3SetTarget:  (ComboBoxModelX.this <--> XAxisComboBox.model)
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connPtoP3SetTarget() {
	/* Set the target from the source */
	try {
		getXAxisComboBox().setModel(getComboBoxModelX());
		// user code begin {1}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}


/**
 * Method to enable the log sensitivity checkbox and slider depending on whether sensitivity analysis is enabled.
 */
private void enableLogSensitivity() throws ExpressionException {

	if (getSensitivityParameter() == null) {
		getLogSensCheckbox().setVisible(false);
		getSensitivityParameterPanel().setVisible(false);
		getJLabelSensitivityParameter().setVisible(false);
	}
}


/**
 * Return the ComboBoxModelX property value.
 * @return javax.swing.DefaultComboBoxModel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.DefaultComboBoxModel getComboBoxModelX() {
	if (ivjComboBoxModelX == null) {
		try {
			ivjComboBoxModelX = new javax.swing.DefaultComboBoxModel();
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjComboBoxModelX;
}


/**
 * Return the ConstantTextField property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getCurLabel() {
	if (ivjCurLabel == null) {
		try {
			ivjCurLabel = new javax.swing.JLabel();
			ivjCurLabel.setName("CurLabel");
			ivjCurLabel.setText("Value");
			ivjCurLabel.setBackground(java.awt.Color.lightGray);
			ivjCurLabel.setForeground(java.awt.Color.black);
			ivjCurLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjCurLabel;
}

/**
 * Return the DefaultListModelY property value.
 * @return javax.swing.DefaultListModel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.DefaultListModel getDefaultListModelY() {
	if (ivjDefaultListModelY == null) {
		try {
			ivjDefaultListModelY = new javax.swing.DefaultListModel();
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjDefaultListModelY;
}


/**
 * Return the JLabelSensitivityParameter property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabelSensitivityParameter() {
	if (ivjJLabelSensitivityParameter == null) {
		try {
			ivjJLabelSensitivityParameter = new javax.swing.JLabel();
			ivjJLabelSensitivityParameter.setName("JLabelSensitivityParameter");
			ivjJLabelSensitivityParameter.setText(" ");
			ivjJLabelSensitivityParameter.setForeground(java.awt.Color.black);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJLabelSensitivityParameter;
}

/**
 * Return the JPanel property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getJPanelSensitivity() {
	if (ivjJPanelSensitivity == null) {
		try {
			ivjJPanelSensitivity = new javax.swing.JPanel();
			ivjJPanelSensitivity.setName("JPanelSensitivity");
			ivjJPanelSensitivity.setLayout(new java.awt.GridBagLayout());

			java.awt.GridBagConstraints constraintsLogSensCheckbox = new java.awt.GridBagConstraints();
			constraintsLogSensCheckbox.gridx = 0; constraintsLogSensCheckbox.gridy = 0;
			constraintsLogSensCheckbox.gridwidth = 3;
			constraintsLogSensCheckbox.fill = java.awt.GridBagConstraints.HORIZONTAL;
			constraintsLogSensCheckbox.insets = new java.awt.Insets(4, 4, 4, 4);
			getJPanelSensitivity().add(getLogSensCheckbox(), constraintsLogSensCheckbox);

			java.awt.GridBagConstraints constraintsSensitivityParameterPanel = new java.awt.GridBagConstraints();
			constraintsSensitivityParameterPanel.gridx = 0; constraintsSensitivityParameterPanel.gridy = 2;
			constraintsSensitivityParameterPanel.gridwidth = 3;
			constraintsSensitivityParameterPanel.fill = java.awt.GridBagConstraints.BOTH;
			constraintsSensitivityParameterPanel.weightx = 2.0;
			constraintsSensitivityParameterPanel.weighty = 1.0;
			constraintsSensitivityParameterPanel.insets = new java.awt.Insets(4, 4, 4, 4);
			getJPanelSensitivity().add(getSensitivityParameterPanel(), constraintsSensitivityParameterPanel);

			java.awt.GridBagConstraints constraintsJLabelSensitivityParameter = new java.awt.GridBagConstraints();
			constraintsJLabelSensitivityParameter.gridx = 0; constraintsJLabelSensitivityParameter.gridy = 1;
			constraintsJLabelSensitivityParameter.gridwidth = 3;
			constraintsJLabelSensitivityParameter.fill = java.awt.GridBagConstraints.HORIZONTAL;
			constraintsJLabelSensitivityParameter.insets = new java.awt.Insets(4, 4, 4, 4);
			getJPanelSensitivity().add(getJLabelSensitivityParameter(), constraintsJLabelSensitivityParameter);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJPanelSensitivity;
}

/**
 * Return the JScrollPaneYAxis property value.
 * @return javax.swing.JScrollPane
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JScrollPane getJScrollPaneYAxis() {
	if (ivjJScrollPaneYAxis == null) {
		try {
			ivjJScrollPaneYAxis = new javax.swing.JScrollPane();
			ivjJScrollPaneYAxis.setName("JScrollPaneYAxis");
			getJScrollPaneYAxis().setViewportView(getYAxisChoice());
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJScrollPaneYAxis;
}


/**
 * Return the LogSensCheckbox property value.
 * @return javax.swing.JCheckBox
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JCheckBox getLogSensCheckbox() {
	if (ivjLogSensCheckbox == null) {
		try {
			ivjLogSensCheckbox = new javax.swing.JCheckBox();
			ivjLogSensCheckbox.setName("LogSensCheckbox");
			ivjLogSensCheckbox.setText("Log Sensitivity");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjLogSensCheckbox;
}

/**
 * Return the MaxLabel property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getMaxLabel() {
	if (ivjMaxLabel == null) {
		try {
			ivjMaxLabel = new javax.swing.JLabel();
			ivjMaxLabel.setName("MaxLabel");
			ivjMaxLabel.setText("1");
			ivjMaxLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
			ivjMaxLabel.setForeground(java.awt.Color.black);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjMaxLabel;
}

/**
 * Return the MinLabel property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getMinLabel() {
	if (ivjMinLabel == null) {
		try {
			ivjMinLabel = new javax.swing.JLabel();
			ivjMinLabel.setName("MinLabel");
			ivjMinLabel.setText("0");
			ivjMinLabel.setBackground(java.awt.Color.lightGray);
			ivjMinLabel.setForeground(java.awt.Color.black);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjMinLabel;
}

/**
 * Gets the odeSolverResultSet property (cbit.vcell.solver.ode.ODESolverResultSet) value.
 * @return The odeSolverResultSet property value.
 * @see #setOdeSolverResultSet
 */
public ODESolverResultSet getOdeSolverResultSet() {
	return fieldOdeSolverResultSet;
}


/**
 * Return the odeSolverResultSet1 property value.
 * @return cbit.vcell.solver.ode.ODESolverResultSet
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private ODESolverResultSet getodeSolverResultSet1() {
	// user code begin {1}
	// user code end
	return ivjodeSolverResultSet1;
}


/**
 * Insert the method's description here.
 * Creation date: (2/8/2001 5:27:49 PM)
 * @return int[]
 */
private int[] getPlottableColumnIndices() {
	return plottableColumnIndices;
}


/**
 * Insert the method's description here.
 * Creation date: (2/8/2001 5:27:49 PM)
 * @return java.lang.String[]
 */
private java.lang.String[] getPlottableNames() {
	return plottableNames;
}


/**
 * Insert the method's description here.
 * Creation date: (5/18/2001 10:42:41 PM)
 * @return java.lang.String[]
 */
private java.lang.String[] getResultSetColumnNames() {
	return resultSetColumnNames;
}

/**
 // Method to obtain the sensitivity parameter (if applicable). The method checks the column description names in the
 // result set to find any column description that begins with the substring "sens" and contains the substring "wrt_".
 // If there is, then the last portion of that column description name is the parameter name. The sensitivity parameter
 // is also stored as a function column description in the result set (as a constant function). The value is extracted
 // from the result set, and a new Constant is created (with the name and value of the parameter) and returned. If no
 // column description starts with the substring "sens" or if the column for the parameter does not exist in the result
 // set, the method returns null.
 */
private Constant getSensitivityParameter() throws ExpressionException {
	String sensParamName = "";
	FunctionColumnDescription fcds[] = getOdeSolverResultSet().getFunctionColumnDescriptions();

	// Check for any column description name that starts with the substring "sens" and contains "wrt_".
	for (int i = 0; i < fcds.length; i++){
		if (fcds[i].getName().startsWith("sens_")) {
			int c = fcds[i].getName().indexOf("wrt_");
			sensParamName = fcds[i].getName().substring(c+4);
			if (!sensParamName.equals(null) || !sensParamName.equals("")) {
				break;
			} 
		}
	}

	double sensParamValue = 0.0;

	if (sensParamName.equals("")) {
		return null;
	}

	// If the sens param column exists in the result set, create a Constant and return it, else return null.
	if (getOdeSolverResultSet().findColumn(sensParamName) > -1) {
		double[] tempValues = getOdeSolverResultSet().extractColumn(getOdeSolverResultSet().findColumn(sensParamName));
		sensParamValue = tempValues[1];
	} else {
		// System.out.println("REUSULT SET DOES NOT HAVE SENSITIVITY ANALYSIS");
		return null;
	}

	Constant sensParam = new Constant(sensParamName, new Expression(sensParamValue));
	return sensParam;
}


/**
 * Return the SensitivityParameterPanel property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getSensitivityParameterPanel() {
	if (ivjSensitivityParameterPanel == null) {
		try {
			ivjSensitivityParameterPanel = new javax.swing.JPanel();
			ivjSensitivityParameterPanel.setName("SensitivityParameterPanel");
			ivjSensitivityParameterPanel.setLayout(new java.awt.GridBagLayout());

			java.awt.GridBagConstraints constraintsMinLabel = new java.awt.GridBagConstraints();
			constraintsMinLabel.gridx = 0; constraintsMinLabel.gridy = 1;
			getSensitivityParameterPanel().add(getMinLabel(), constraintsMinLabel);

			java.awt.GridBagConstraints constraintsMaxLabel = new java.awt.GridBagConstraints();
			constraintsMaxLabel.gridx = 2; constraintsMaxLabel.gridy = 1;
			constraintsMaxLabel.fill = java.awt.GridBagConstraints.HORIZONTAL;
			constraintsMaxLabel.anchor = java.awt.GridBagConstraints.EAST;
			getSensitivityParameterPanel().add(getMaxLabel(), constraintsMaxLabel);

			java.awt.GridBagConstraints constraintsCurLabel = new java.awt.GridBagConstraints();
			constraintsCurLabel.gridx = 1; constraintsCurLabel.gridy = 1;
			constraintsCurLabel.fill = java.awt.GridBagConstraints.HORIZONTAL;
			constraintsCurLabel.weightx = 1.0;
			getSensitivityParameterPanel().add(getCurLabel(), constraintsCurLabel);

			java.awt.GridBagConstraints constraintsSensitivityParameterSlider = new java.awt.GridBagConstraints();
			constraintsSensitivityParameterSlider.gridx = 0; constraintsSensitivityParameterSlider.gridy = 0;
			constraintsSensitivityParameterSlider.gridwidth = 3;
			constraintsSensitivityParameterSlider.fill = java.awt.GridBagConstraints.HORIZONTAL;
			constraintsSensitivityParameterSlider.weightx = 1.0;
			getSensitivityParameterPanel().add(getSensitivityParameterSlider(), constraintsSensitivityParameterSlider);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjSensitivityParameterPanel;
}

/**
 * Return the ConstantScrollbar property value.
 * @return javax.swing.JSlider
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JSlider getSensitivityParameterSlider() {
	if (ivjSensitivityParameterSlider == null) {
		try {
			ivjSensitivityParameterSlider = new javax.swing.JSlider();
			ivjSensitivityParameterSlider.setName("SensitivityParameterSlider");
			ivjSensitivityParameterSlider.setPaintLabels(false);
			ivjSensitivityParameterSlider.setPaintTicks(true);
			ivjSensitivityParameterSlider.setValue(25);
			ivjSensitivityParameterSlider.setPreferredSize(new java.awt.Dimension(200, 16));
			ivjSensitivityParameterSlider.setMaximum(50);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjSensitivityParameterSlider;
}

/**
 * Comment
 */
private double[] getSensValues(ColumnDescription colDesc) throws ExpressionException {
	if (getSensitivityParameter() != null) {
		double sens[] = null;
		String[] rsetColNames = getPlottableNames();
		int sensIndex = -1;
		
		for (int j = 0; j < rsetColNames.length; j++) {
			if (rsetColNames[j].equals("sens_"+colDesc.getName()+"_wrt_"+getSensitivityParameter().getName())) {
				sensIndex = j;
			}
		}
		if (sensIndex > -1) {
			sens = getOdeSolverResultSet().extractColumn(getOdeSolverResultSet().findColumn(rsetColNames[sensIndex]));
		}

		return sens;
	} else {
		return null;
	}
}

/**
 * Gets the symbolTable property (cbit.vcell.parser.SymbolTable) value.
 * @return The symbolTable property value.
 * @see #setSymbolTable
 */
public SymbolTable getSymbolTable() {
	return fieldSymbolTable;
}


/**
 * Return the XAxisComboBox property value.
 * @return javax.swing.JComboBox
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
public javax.swing.JComboBox getXAxisComboBox() {
	if (ivjXAxisComboBox == null) {
		try {
			ivjXAxisComboBox = new javax.swing.JComboBox();
			ivjXAxisComboBox.setName("XAxisComboBox");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjXAxisComboBox;
}


/**
 * Return the XAxisLabel property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getXAxisLabel() {
	if (ivjXAxisLabel == null) {
		try {
			ivjXAxisLabel = new javax.swing.JLabel();
			ivjXAxisLabel.setName("XAxisLabel");
			ivjXAxisLabel.setText("X Axis:");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjXAxisLabel;
}

/**
 * Gets the xIndex property (int) value.
 * @return The xIndex property value.
 * @see #setXIndex
 */
public int getXIndex() {
	return fieldXIndex;
}


/**
 * Return the YAxisChoice property value.
 * @return javax.swing.JList
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JList getYAxisChoice() {
	if (ivjYAxisChoice == null) {
		try {
			ivjYAxisChoice = new javax.swing.JList();
			ivjYAxisChoice.setName("YAxisChoice");
			ivjYAxisChoice.setBounds(0, 0, 160, 120);
			ivjYAxisChoice.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjYAxisChoice;
}

/**
 * Return the YAxisLabel property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getYAxisLabel() {
	if (ivjYAxisLabel == null) {
		try {
			ivjYAxisLabel = new javax.swing.JLabel();
			ivjYAxisLabel.setName("YAxisLabel");
			ivjYAxisLabel.setText("Y Axis:");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjYAxisLabel;
}

/**
 * Gets the yIndices property (int[]) value.
 * @return The yIndices property value.
 * @see #setYIndices
 */
private int[] getYIndices() {
	return fieldYIndices;
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
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initConnections() throws java.lang.Exception {
	// user code begin {1}
	// user code end
	getYAxisChoice().addListSelectionListener(ivjEventHandler);
	this.addPropertyChangeListener(ivjEventHandler);
	getXAxisComboBox().addItemListener(ivjEventHandler);
	getLogSensCheckbox().addActionListener(ivjEventHandler);
	getSensitivityParameterSlider().addChangeListener(ivjEventHandler);
	connPtoP1SetTarget();
	connPtoP3SetTarget();
	connPtoP2SetTarget();
	
	getYAxisChoice().setCellRenderer(new DefaultListCellRenderer() {
		
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
			if (getOdeSolverResultSet() == null) {
				return this;
			}
			
			String varName = (String)value;
			int col = getOdeSolverResultSet().findColumn(varName);
			ColumnDescription cd = getOdeSolverResultSet().getColumnDescriptions(col);
			if (cd instanceof FunctionColumnDescription) {
				if (((FunctionColumnDescription)cd).getIsUserDefined()) {
					if (function_icon == null) {
						function_icon = new ImageIcon(getClass().getResource("/icons/function_icon.png"));
					}
					setIcon(function_icon);
				}
			}
			return this;
		}
	});
}

/**
 * Initialize the class.
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initialize() {
	try {
		// user code begin {1}
		// user code end
		setName("ODESolverPlotSpecificationPanel");
		setPreferredSize(new java.awt.Dimension(150, 600));
		setLayout(new java.awt.GridBagLayout());
		setSize(248, 604);
		setMinimumSize(new java.awt.Dimension(125, 300));

		java.awt.GridBagConstraints constraintsXAxisLabel = new java.awt.GridBagConstraints();
		constraintsXAxisLabel.gridx = 0; constraintsXAxisLabel.gridy = 0;
		constraintsXAxisLabel.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getXAxisLabel(), constraintsXAxisLabel);

		java.awt.GridBagConstraints constraintsYAxisLabel = new java.awt.GridBagConstraints();
		constraintsYAxisLabel.gridx = 0; constraintsYAxisLabel.gridy = 2;
		constraintsYAxisLabel.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getYAxisLabel(), constraintsYAxisLabel);

		java.awt.GridBagConstraints constraintsJPanelSensitivity = new java.awt.GridBagConstraints();
		constraintsJPanelSensitivity.gridx = 0; constraintsJPanelSensitivity.gridy = 5;
		constraintsJPanelSensitivity.gridheight = 2;
		constraintsJPanelSensitivity.fill = java.awt.GridBagConstraints.BOTH;
		constraintsJPanelSensitivity.weightx = 1.0;
		add(getJPanelSensitivity(), constraintsJPanelSensitivity);

		java.awt.GridBagConstraints constraintsJScrollPaneYAxis = new java.awt.GridBagConstraints();
		constraintsJScrollPaneYAxis.gridx = 0; constraintsJScrollPaneYAxis.gridy = 3;
		constraintsJScrollPaneYAxis.fill = java.awt.GridBagConstraints.BOTH;
		constraintsJScrollPaneYAxis.weightx = 1.0;
		constraintsJScrollPaneYAxis.weighty = 1.0;
		constraintsJScrollPaneYAxis.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getJScrollPaneYAxis(), constraintsJScrollPaneYAxis);

		java.awt.GridBagConstraints constraintsXAxisComboBox = new java.awt.GridBagConstraints();
		constraintsXAxisComboBox.gridx = 0; constraintsXAxisComboBox.gridy = 1;
		constraintsXAxisComboBox.fill = java.awt.GridBagConstraints.HORIZONTAL;
		constraintsXAxisComboBox.weightx = 1.0;
		constraintsXAxisComboBox.insets = new java.awt.Insets(4, 4, 4, 4);
		add(getXAxisComboBox(), constraintsXAxisComboBox);

		initConnections();
	} catch (java.lang.Throwable ivjExc) {
		handleException(ivjExc);
	}
	// user code begin {2}
	// user code end
}

/**
 * Comment
 */
private void initializeLogSensCheckBox() throws ExpressionException{
	if (getSensitivityParameter() != null) {
		getLogSensCheckbox().setSelected(true);
	} else {
		getLogSensCheckbox().setSelected(false);
	}
}


/**
 * main entrypoint - starts the part when it is run as an application
 * @param args java.lang.String[]
 */
public static void main(java.lang.String[] args) {
	try {
		javax.swing.JFrame frame = new javax.swing.JFrame();
		ODESolverPlotSpecificationPanel aODESolverPlotSpecificationPanel;
		aODESolverPlotSpecificationPanel = new ODESolverPlotSpecificationPanel();
		frame.setContentPane(aODESolverPlotSpecificationPanel);
		frame.setSize(aODESolverPlotSpecificationPanel.getSize());
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				System.exit(0);
			};
		});
		frame.setVisible(true);
		java.awt.Insets insets = frame.getInsets();
		frame.setSize(frame.getWidth() + insets.left + insets.right, frame.getHeight() + insets.top + insets.bottom);
		frame.setVisible(true);
		
		String[] list = {"name1", "name2", "name3", "name4", "name5"};
		aODESolverPlotSpecificationPanel.getDefaultListModelY().removeAllElements();
		for (int i=0;i<list.length;i++) {
			aODESolverPlotSpecificationPanel.getDefaultListModelY().addElement(list[i]);
		}
		
	} catch (Throwable exception) {
		System.err.println("Exception occurred in main() of javax.swing.JPanel");
		exception.printStackTrace(System.out);
	}
}

/**
 * Comment
 */
private void refreshVisiblePlots(Plot2D plot2D) {
	if (plot2D == null) {
		return;
	} else {
		boolean[] visiblePlots = new boolean[plot2D.getNumberOfPlots()];
		for (int i=0;i<getYIndices().length;i++) {
			visiblePlots[getYIndices()[i]] = true;
		}
		if((getOdeSolverResultSet()!= null) && getOdeSolverResultSet().isMultiTrialData()) {
			plot2D.setVisiblePlots(visiblePlots,true);
		}
		else {
			plot2D.setVisiblePlots(visiblePlots,false);
		}
	}
}


/**
 * Comment
 */
private void regeneratePlot2D() throws ExpressionException {
	if (getOdeSolverResultSet() == null) 
	{
		return;
	} 
	if(!getOdeSolverResultSet().isMultiTrialData())
	{
		if(getXAxisComboBox().getSelectedIndex() < 0)
		{
			return;
		}
		else 
		{
			double[] xData = getOdeSolverResultSet().extractColumn(getPlottableColumnIndices()[getXIndex()]);
			double[][] allData = new double[getPlottableColumnIndices().length + 1][xData.length];
			String[] yNames = new String[getPlottableColumnIndices().length];
			allData[0] = xData;
			double[] yData = new double[xData.length];
	
			double currParamValue = 0.0;
			double deltaParamValue = 0.0;
			// Extrapolation calculations!
			if (getSensitivityParameter() != null) {
				int val = getSensitivityParameterSlider().getValue();
				double nominalParamValue = getSensitivityParameter().getConstantValue();
				double pMax = nominalParamValue*1.1;
				double pMin = nominalParamValue*0.9;
				int iMax = getSensitivityParameterSlider().getMaximum();
				int iMin = getSensitivityParameterSlider().getMinimum();
				double slope = (pMax-pMin)/(iMax-iMin);
				currParamValue = slope*val + pMin;
				deltaParamValue = currParamValue - nominalParamValue;
	
				getMaxLabel().setText(Double.toString(pMax));
				getMinLabel().setText(Double.toString(pMin));
				getCurLabel().setText(Double.toString(currParamValue));
			}
			
			if (!getLogSensCheckbox().getModel().isSelected()) {
				// When log sensitivity check box is not selected.
				for (int i=0;i<allData.length-1;i++) {
					// If sensitivity analysis is enabled, extrapolate values for State vars and non-sensitivity functions
					if (getSensitivityParameter() != null) {
						ColumnDescription cd = getOdeSolverResultSet().getColumnDescriptions(getPlottableColumnIndices()[i]);
						double sens[] = getSensValues(cd);
						yData = getOdeSolverResultSet().extractColumn(getOdeSolverResultSet().findColumn(cd.getName()));
						// sens array != null for non-sensitivity state vars and functions, so extrapolate
						if (sens != null) {
							for (int j = 0; j < sens.length; j++) {
								if (Math.abs(yData[j]) > 1e-6) {
									// away from zero, exponential extrapolation
									allData[i+1][j] = yData[j] * Math.exp(deltaParamValue * sens[j]/ yData[j] );
								} else {
									// around zero - linear extrapolation
									allData[i+1][j] = yData[j] + sens[j] * deltaParamValue;
								}						
							} 
						// sens array == null for sensitivity state vars and functions, so don't change their original values
						} else {
							allData[i+1] = getOdeSolverResultSet().extractColumn(getPlottableColumnIndices()[i]);
						} 
					} else {
						// No sensitivity analysis case, so do not alter the original values for any variable or function
						allData[i+1] = getOdeSolverResultSet().extractColumn(getPlottableColumnIndices()[i]);
					}
					yNames[i] = getPlottableNames()[i];
				}
			} else {
				// When log sensitivity checkbox is selected.
	
				// Get sensitivity parameter and its value to compute log sensitivity
				Constant sensParam = getSensitivityParameter();
				double sensParamValue = sensParam.getConstantValue();
				getJLabelSensitivityParameter().setText("Sensitivity wrt Parameter "+sensParam.getName());
	
				//
				// For each column (State vars and functions) in the result set, find the corresponding sensitivity var column
				// in the result set (a value > -1). If the sensitivity var column does not exist (for user defined functions or
				// sensitivity variables themselves), the column number returned is -1, so do not change that data column.
				//
				String[] rsetColNames = getPlottableNames();
				for (int i=0;i<allData.length-1;i++) {
					// Finding sensitivity var column for each column in result set.
					ColumnDescription cd = getOdeSolverResultSet().getColumnDescriptions(getPlottableColumnIndices()[i]);
					int sensIndex = -1;
					for (int j = 0; j < rsetColNames.length; j++) {
						if (rsetColNames[j].equals("sens_"+cd.getName()+"_wrt_"+sensParam.getName())) {
							sensIndex = j;
						}
					}
					yData = getOdeSolverResultSet().extractColumn(getOdeSolverResultSet().findColumn(cd.getName()));
					// If sensitivity var exists, compute log sensitivity
					if (sensIndex > -1) {
						double[] sens = getOdeSolverResultSet().extractColumn(getOdeSolverResultSet().findColumn(rsetColNames[sensIndex]));
						for (int k = 0; k < yData.length; k++) {
							// Extrapolated statevars and functions
							if (Math.abs(yData[k]) > 1e-6) {
								// away from zero, exponential extrapolation
								allData[i+1][k] = yData[k] * Math.exp(deltaParamValue * sens[k]/ yData[k] );
							} else {
								// around zero - linear extrapolation
								allData[i+1][k] = yData[k] + sens[k] * deltaParamValue;
							}						
							// Log sensitivity for the state variables and functions
							double logSens = 0.0;  // default if floating point problems
							if (Math.abs(yData[k]) > 0){
								double tempLogSens = sens[k] * sensParamValue / yData[k];
								if (tempLogSens != Double.NEGATIVE_INFINITY &&
									tempLogSens != Double.POSITIVE_INFINITY &&
									tempLogSens != Double.NaN) {
										
									logSens = tempLogSens;
								}
							}
							allData[sensIndex+1][k] = logSens;
						}
					// If sensitivity var does not exist, retain  original value of column (var or function).
					} else {
						if (!cd.getName().startsWith("sens_")) {
							allData[i+1] = yData;
						}
					}
					yNames[i] = getPlottableNames()[i];
				}
			}
				
			String title = "";
			String xLabel = getPlottableNames()[getXIndex()];
			String yLabel = "";
	
			if (yNames.length == 1) {
				yLabel = yNames[0];
			}
			// Update Sensitivity parameter label depending on whether Log sensitivity check box is checked or not.
			if (!getLogSensCheckbox().getModel().isSelected()) {
				getJLabelSensitivityParameter().setText("");
			}
	
			SymbolTableEntry[] symbolTableEntries = null;
			if(getSymbolTable() != null && yNames != null && yNames.length > 0){
				symbolTableEntries = new SymbolTableEntry[yNames.length];
				for(int i=0;i<yNames.length;i+= 1){
					try{
						symbolTableEntries[i] = getSymbolTable().getEntry(yNames[i]);
					}catch(ExpressionBindingException e){
						//Do Nothing
					}
				}
				
			}
			SingleXPlot2D plot2D = new SingleXPlot2D(symbolTableEntries,xLabel, yNames, allData, new String[] {title, xLabel, yLabel});
			refreshVisiblePlots(plot2D);
			//here fire "singleXPlot2D" event, ODEDataViewer's event handler listens to it.
			setPlot2D(plot2D);
		}
	}// end of none MultitrialData
	else // multitrial data
	{
		//a column of data get from ODESolverRestultSet, which is actually the results for a specific variable during multiple trials
		double[] rowData = new double[getOdeSolverResultSet().getRowCount()];
		int[] plottableColumnIndices =  getPlottableColumnIndices();
		PlotData[] plotData = new PlotData[plottableColumnIndices.length];
		String[] yNames = getPlottableNames();
		
		for (int i=0; i<plottableColumnIndices.length; i++)
		{
			ColumnDescription cd = getOdeSolverResultSet().getColumnDescriptions(getPlottableColumnIndices()[i]);
			rowData = getOdeSolverResultSet().extractColumn(getOdeSolverResultSet().findColumn(cd.getName()));
			Point2D[] histogram = generateHistogram(rowData);
			double[] x = new double[histogram.length];
			double[] y = new double[histogram.length];
			for (int j=0; j<histogram.length; j++)
			{
				x[j]= histogram[j].getX();
		        y[j]= histogram[j].getY();
		    }
			plotData[i] =  new PlotData(x,y);
		}
		
		SymbolTableEntry[] symbolTableEntries = null;
		if(getSymbolTable() != null && yNames != null && yNames.length > 0){
			symbolTableEntries = new SymbolTableEntry[yNames.length];
			for(int i=0;i<yNames.length;i+= 1){
				try{
					symbolTableEntries[i] = getSymbolTable().getEntry(yNames[i]);
				}catch(ExpressionBindingException e){
					e.printStackTrace();
				}
			}
			
		}
		
		String title = "Probability Distribution of Species";
		String xLabel = "Number of Particles";
		String yLabel = "";
		
		Plot2D plot2D = new Plot2D(symbolTableEntries, yNames, plotData, new String[] {title, xLabel, yLabel});
		refreshVisiblePlots(plot2D);
		setPlot2D(plot2D);
	}
}


/**
 * Sets the odeSolverResultSet property (cbit.vcell.solver.ode.ODESolverResultSet) value.
 * @param odeSolverResultSet The new value for the property.
 * @see #getOdeSolverResultSet
 */
public void setOdeSolverResultSet(ODESolverResultSet odeSolverResultSet) {
	ODESolverResultSet oldValue = fieldOdeSolverResultSet;
	fieldOdeSolverResultSet = odeSolverResultSet;
	if (odeSolverResultSet==null){
		setPlot2D(null);
		return;
	}
	firePropertyChange("odeSolverResultSet", oldValue, odeSolverResultSet);
}


/**
 * Set the odeSolverResultSet1 to a new value.
 * @param newValue cbit.vcell.solver.ode.ODESolverResultSet
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void setodeSolverResultSet1(ODESolverResultSet newValue) {
	if (ivjodeSolverResultSet1 != newValue) {
		try {
			ODESolverResultSet oldValue = getodeSolverResultSet1();
			/* Stop listening for events from the current object */
			if (ivjodeSolverResultSet1 != null) {
				ivjodeSolverResultSet1.removePropertyChangeListener(ivjEventHandler);
			}
			ivjodeSolverResultSet1 = newValue;

			/* Listen for events from the new object */
			if (ivjodeSolverResultSet1 != null) {
				ivjodeSolverResultSet1.addPropertyChangeListener(ivjEventHandler);
			}
			connPtoP2SetSource();
			connEtoC9(ivjodeSolverResultSet1);
			connEtoC11(ivjodeSolverResultSet1);
			connEtoC13(ivjodeSolverResultSet1);
			firePropertyChange("odeSolverResultSet", oldValue, newValue);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	};
	// user code begin {3}
	// user code end
}

/**
 * Insert the method's description here.
 * Creation date: (2/8/2001 5:27:49 PM)
 * @param newPlottableColumnIndices int[]
 */
private void setPlottableColumnIndices(ArrayList<Integer> newPlottableColumnIndices) {
	plottableColumnIndices = new int[newPlottableColumnIndices.size()];
	for (int i = 0; i < newPlottableColumnIndices.size(); i++) {
		plottableColumnIndices[i] = newPlottableColumnIndices.get(i);
	}
}


/**
 * Insert the method's description here.
 * Creation date: (2/8/2001 5:27:49 PM)
 * @param newPlottableNames java.lang.String[]
 */
private void setPlottableNames(java.lang.String[] newPlottableNames) {
	plottableNames = newPlottableNames;
}


/**
 * Insert the method's description here.
 * Creation date: (5/18/2001 10:42:41 PM)
 * @param newResultSetColumnNames java.lang.String[]
 */
private void setResultSetColumnNames(java.lang.String[] newResultSetColumnNames) {
	resultSetColumnNames = newResultSetColumnNames;
}

/**
 * Sets the symbolTable property (cbit.vcell.parser.SymbolTable) value.
 * @param symbolTable The new value for the property.
 * @see #getSymbolTable
 */
public void setSymbolTable(SymbolTable symbolTable) {
	SymbolTable oldValue = fieldSymbolTable;
	fieldSymbolTable = symbolTable;
	firePropertyChange("symbolTable", oldValue, symbolTable);
}


/**
 * Sets the xIndex property (int) value.
 * @param xIndex The new value for the property.
 * @see #getXIndex
 */
private void setXIndex(int xIndex) {
	int oldValue = fieldXIndex;
	fieldXIndex = xIndex;
	firePropertyChange("xIndex", new Integer(oldValue), new Integer(xIndex));
}


/**
 * Sets the yIndices property (int[]) value.
 * @param yIndices The new value for the property.
 * @see #getYIndices
 */
private void setYIndices(int[] yIndices) {
	int[] oldValue = fieldYIndices;
	fieldYIndices = yIndices;
	firePropertyChange("YIndices", oldValue, yIndices);
}


/**
 * Comment
 */
private synchronized void setYIndicesFromList() {
/*
 * ONE SHOULDN'T HAVE TO DEAL WITH THIS CRAP !!
 *	
 * this is a workaround for Swing double event firing (which would result in double plotData building)
 */
	boolean different = false;
	// we build the new array
	int[] indices = new int[getYAxisChoice().getSelectedIndices().length];
	for (int i=0;i<getYAxisChoice().getSelectedIndices().length;i++) {
		indices[i] = getYAxisChoice().getSelectedIndices()[i];
		// while building, we also check the stored (maybe new) value
		if (i < fieldYIndices.length) {
			different = fieldYIndices[i] != indices[i];
		} else {
			// old array is smaller size
			different = true;
		}
	}
	// while equal so far, old could be larger
	if (fieldYIndices.length > indices.length) {
		different = true;
	}
	if (different) {
		setYIndices(indices);
	} else {
		// do nothing, we are dealing with the duplicate event
	}
}


/**
 * Insert the method's description here.
 * Creation date: (2/8/2001 4:56:15 PM)
 * @param cbit.vcell.solver.ode.ODESolverResultSet
 */
private void sortIndices(final ODESolverResultSet odeSolverResultSet, ArrayList<Integer> indices) {
	Collections.sort(indices, new Comparator<Integer>() {

		public int compare(Integer o1, Integer o2) {
			ColumnDescription columnDescriptionI = odeSolverResultSet.getColumnDescriptions(o1);
	        ColumnDescription columnDescriptionJ = odeSolverResultSet.getColumnDescriptions(o2);
	        String nameI = columnDescriptionI.getName();
			String nameJ = columnDescriptionJ.getName();
			return nameI.compareTo(nameJ);
		}
	});
}


/**
 * Insert the method's description here.
 * Creation date: (2/8/2001 4:56:15 PM)
 * @param cbit.vcell.solver.ode.ODESolverResultSet
 */
private synchronized void updateChoices(ODESolverResultSet odeSolverResultSet) throws ExpressionException {
	if (odeSolverResultSet == null) {
		return;
	}
	ArrayList<Integer> variableIndices = new ArrayList<Integer>();
	ArrayList<Integer> sensitivityIndices = new ArrayList<Integer>();
	int timeIndex = -1;
	
    for (int i = 0; i < odeSolverResultSet.getColumnDescriptionsCount(); i++) {
        ColumnDescription cd = odeSolverResultSet.getColumnDescriptions(i);
        //If the column is "TrialNo" from multiple trials, we don't put the column "TrialNo" in. amended March 12th, 2007
        //If the column is "_initConnt" generated when using concentration as initial condition, we dont' put the function in list. amended again in August, 2008.
        if (cd.getName().equals(ReservedSymbol.TIME.getName())) {
        	timeIndex = i;
        } else if (cd.getParameterName() == null) {
        	if (!cd.getName().equals(SimDataConstants.HISTOGRAM_INDEX_NAME) && !cd.getName().contains(MathMapping.MATH_FUNC_SUFFIX_SPECIES_INIT_COUNT)) {
        		variableIndices.add(i);
        	}
        } else {
        	sensitivityIndices.add(i);
        }
    }    
    sortIndices(odeSolverResultSet, variableIndices);
    sortIndices(odeSolverResultSet, sensitivityIndices); 
    
    //  Hack this here, Later we can use an array utility...
    ArrayList<Integer> sortedIndices = new ArrayList<Integer>();
    if (timeIndex >= 0) {
	    sortedIndices.add(timeIndex); // add time first
	}
    
    boolean bMultiTrailData = odeSolverResultSet.isMultiTrialData();
    
    sortedIndices.addAll(variableIndices);
    if(!bMultiTrailData)
    {
    	sortedIndices.addAll(sensitivityIndices);
    }
    //  End hack
    setPlottableColumnIndices(sortedIndices);
    // now store their names
    String[] names = new String[sortedIndices.size()];
    for (int i = 0; i < sortedIndices.size(); i++) {
        ColumnDescription column = odeSolverResultSet.getColumnDescriptions(sortedIndices.get(i));
        names[i] = column.getDisplayName();
    }
    setPlottableNames(names);
   
    // finally, update widgets
    getComboBoxModelX().removeAllElements();
    getDefaultListModelY().removeAllElements();
    for (int i = 0; i < sortedIndices.size(); i++) {
    	// Don't put anything in X Axis, if the results of multifple trials are being displayed.
    	if(!bMultiTrailData) {
    		getComboBoxModelX().addElement(names[i]);
    	}
        getDefaultListModelY().addElement(names[i]);
    }
    
    if (sortedIndices.size() > 0) {
    	//Don't put anything in X Axis, if the results of multifple trials are being displayed.
    	if(!bMultiTrailData) {
    		getXAxisComboBox().setSelectedIndex(0);
    	}
        getYAxisChoice().setSelectedIndex(sortedIndices.size() > 1 ? 1 : 0);
    }
    regeneratePlot2D();
}


/**
 * Comment
 */
private void updateResultSet(ODESolverResultSet odeSolverResultSet) throws ExpressionException {
	String[] columnNames = new String[odeSolverResultSet.getColumnDescriptionsCount()];

	for (int i = 0; i < columnNames.length; i++){
		columnNames[i] = odeSolverResultSet.getColumnDescriptions(i).getDisplayName();
	}
	if (BeanUtils.arrayEquals(columnNames, getResultSetColumnNames())) {
		// same stuff, maybe more/different data - keep axis choices
		regeneratePlot2D();
	} else {
		// axis choices are different, so update everything
		setResultSetColumnNames(columnNames);
		updateChoices(odeSolverResultSet);
	}
}

public Plot2D getPlot2D() {
	return fieldPlot2D;
}

public void setPlot2D(Plot2D plot2D) {
	//amended March 29, 2007. To fire event to ODEDataViewer.
	Plot2D oldValue = this.fieldPlot2D;
	this.fieldPlot2D = plot2D;
	firePropertyChange("Plot2D", oldValue, plot2D);
}
/*
 * To get a hash table with keys as possible results for a specific variable after certain time period
 * and the values as the frequency. It is sorted ascendantly.
 */
private Point2D[] generateHistogram(double[] rawData)
{
	Hashtable<Integer,Integer> temp = new Hashtable<Integer,Integer>();
	//sum the results for a specific variable after multiple trials.
	for(int i=0;i<rawData.length;i++)
	{
		int val = ((int)Math.round(rawData[i]));
		if(temp.get(new Integer(val))!= null)
		{
			int v = temp.get(new Integer(val)).intValue();
			temp.put(new Integer(val), new Integer(v+1));
		}
		else temp.put(new Integer(val), new Integer(1));
	}
	//sort the hashtable ascendantly and also calculate the frequency in terms of percentage.
	Vector<Integer> keys = new Vector<Integer>(temp.keySet());
	Collections.sort(keys);
	Point2D[] result = new Point2D[keys.size()];
	for (int i=0; i<keys.size(); i++)
	{
        Integer key = keys.elementAt(i);
        Double valperc = new Double(((double)temp.get(key).intValue())/((double)rawData.length));
        result[i] = new Point2D.Double(key,valperc);
    }
	return result;
}

public String[] getSelectedVariableNames() {
	Object[] selectedValues = getYAxisChoice().getSelectedValues();
	String[] selectedNames = new String[selectedValues.length];
	for (int i = 0; i < selectedNames.length; i++) {
		selectedNames[i] = selectedValues[i].toString();
	}
	return selectedNames;
}
}