package cbit.vcell.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.vcell.model.rbm.MolecularComponent;
import org.vcell.model.rbm.MolecularComponentPattern;
import org.vcell.model.rbm.MolecularType;
import org.vcell.model.rbm.MolecularTypePattern;
import org.vcell.util.Displayable;
import org.vcell.util.Issue;
import org.vcell.util.Issue.Severity;
import org.vcell.util.IssueContext;

import cbit.vcell.graph.AbstractComponentShape.IssueListProvider;
import cbit.vcell.graph.LargeShapeCanvas.DisplayMode;
import cbit.vcell.model.Model.RbmModelContainer;
import cbit.vcell.model.RbmObservable;
import cbit.vcell.model.ReactionRule;
import cbit.vcell.model.SpeciesContext;
import cbit.vcell.model.Structure;

public class MolecularTypeLargeShape extends IssueManagerContainer implements LargeShape, HighlightableShapeInterface {
	
	public static final Color colorTable[] = new Color[] {
		Color.red,
		Color.cyan,
		Color.magenta,
		Color.orange,
		Color.pink,
		Color.green,
		Color.blue,
	};
	
	private static final int BaseWidth = 25;
	private static final int BaseHeight = 30;
	private static final int CornerArc = 30;
	private static final int CompartmentOffset = 13;	// the first molecule in the species pattern is wider to provide space to draw the compartment

	private final int baseWidth;
	private final int baseHeight;
	private final int cornerArc;
	private final int compartmentOffset;

	
	private boolean pattern;			// we draw component or we draw component pattern (and perhaps a bond)
	private int xPos = 0;
	private int yPos = 0;
	private int width;
	private final int height;
	private int positionInPattern = 0;	// index of molecule within a species pattern (if applicable)

	private Structure structure = null;
	final LargeShapeCanvas shapePanel;
	
	private final String name;
	private final MolecularType mt;
	private final MolecularTypePattern mtp;
	private final Displayable owner;	// the topmost entity to which this shape belongs (a rule, an observable, a molecular type, etc)
	private boolean bMatchesSignature = false;
	
	List <MolecularComponentLargeShape> componentShapes = new ArrayList<MolecularComponentLargeShape>();
		
	private static int calculateBaseWidth(LargeShapeCanvas shapePanel) {
		if(shapePanel == null) {
			return BaseWidth;
		} else {
			int Ratio = 2;	// arbitrary factor, to be determined
			int zoomFactor = shapePanel.getZoomFactor() * Ratio;	// negative if going smaller
			return BaseWidth + zoomFactor;
		}
	}
	private static int calculateBasHeight(LargeShapeCanvas shapePanel) {
		if(shapePanel == null) {
			return BaseHeight;
		} else {
			int Ratio = 3;
			int zoomFactor = shapePanel.getZoomFactor() * Ratio;
			return BaseHeight + zoomFactor;
		}
	}
	private static int calculateCornerArc(LargeShapeCanvas shapePanel) {
		if(shapePanel == null) {
			return CornerArc;
		} else {
			int Ratio = 3;
			int zoomFactor = shapePanel.getZoomFactor() * Ratio;
			return CornerArc + zoomFactor;
		}
	}
	private static int calculateCompartmentOffset(LargeShapeCanvas shapePanel) {
		if(shapePanel == null) {
			return CompartmentOffset;
		} else {
			int Ratio = 1;
			int zoomFactor = shapePanel.getZoomFactor() * Ratio;
			return CompartmentOffset + zoomFactor;
		}
	}
	// how much space we reserve for the anchor rectangle at the right end of the molecule
	private static int calculateAnchorWidth(LargeShapeCanvas shapePanel) {
		// the size of the anchor rectangle is about the base width of a component
		int anchorOffset = MolecularComponentLargeShape.calculateBaseWidth(shapePanel)-2;
		return anchorOffset;
	}	
	
	// this is only called for plain species context (no pattern)
	public MolecularTypeLargeShape(int xPos, int yPos, LargeShapeCanvas shapePanel, 
			Displayable owner, IssueListProvider issueListProvider) {
		super(issueListProvider);
		
		this.owner = owner;		// null owner means we want to display a red circle (meaning error)
		this.pattern = false;
		this.mt = null;
		this.mtp = null;
		this.xPos = xPos;
		this.yPos = yPos;
		this.shapePanel = shapePanel;
		this.name = "";

		baseWidth = calculateBaseWidth(shapePanel);
		baseHeight = calculateBasHeight(shapePanel);
		cornerArc = calculateCornerArc(shapePanel);
		compartmentOffset = calculateCompartmentOffset(shapePanel);		// unused here
		
		width = baseWidth+4;	// width is ignored, we write no name inside, we'll just draw a roundish green shape
		height = baseHeight + MolecularComponentLargeShape.calculateBasHeight(shapePanel) / 2;
	}
	// this is only called for molecular type
	public MolecularTypeLargeShape(int xPos, int yPos, MolecularType mt, LargeShapeCanvas shapePanel, 
			Displayable owner, IssueListProvider issueListProvider) {
		super(issueListProvider);
		
		this.owner = owner;
		this.pattern = false;
		this.mt = mt;
		this.mtp = null;
		this.xPos = xPos;
		this.yPos = yPos;
		this.shapePanel = shapePanel;
		
		baseWidth = calculateBaseWidth(shapePanel);
		baseHeight = calculateBasHeight(shapePanel);
		cornerArc = calculateCornerArc(shapePanel);
		compartmentOffset = calculateCompartmentOffset(shapePanel);
		
		width = baseWidth;
		// adjustment for name width and for the width of the components
		// TODO: properly calculate the width based on the font and size of each letter
		int numComponents = mt.getComponentList().size();
		int offsetFromRight = 0;		// total width of all components, based on the length of their names
		// if there's anchoring info to show, reserve some extra space at the end
		if(mt != null && !mt.isAnchorAll()) {
			int anchorOffset = calculateAnchorWidth(shapePanel)-4;
			offsetFromRight += anchorOffset;
		}
		for(int i=numComponents-1; i >=0; i--) {
			MolecularComponent mc = getMolecularType().getComponentList().get(i);
			// WARNING! we create temporary component shapes whose coordinates are invented, we use them only to compute 
			// the width of the molecular type shape; only after that is known we can finally compute the exact coordinates
			// of the components
			MolecularComponentLargeShape mlcls = new MolecularComponentLargeShape(100, 50, mc, shapePanel, owner, issueListProvider);
			offsetFromRight += mlcls.getWidth() + MolecularComponentLargeShape.calculateComponentSeparation(shapePanel);
		}
		name = adjustForSize();
		width += offsetFromRight;			// adjusted for # of components
		width += getStringWidth(name);		// adjust for the length of the name of the molecular type
		height = baseHeight + MolecularComponentLargeShape.calculateBasHeight(shapePanel) / 2;

		int fixedPart = xPos + width;
		offsetFromRight = 10;
		// if there's anchoring info to show, reserve some extra space at the end
		if(mt != null && !mt.isAnchorAll()) {
			int anchorOffset = calculateAnchorWidth(shapePanel)-4;
			offsetFromRight += anchorOffset;
		}
		for(int i=numComponents-1; i >=0; i--) {
			int rightPos = fixedPart - offsetFromRight;		// we compute distance from right end
			int y = yPos + height - MolecularComponentLargeShape.calculateBasHeight(shapePanel);
			// now that we know the dimensions of the molecular type shape we create the component shapes
			MolecularComponent mc = mt.getComponentList().get(i);
			MolecularComponentLargeShape mlcls = new MolecularComponentLargeShape(rightPos, y, mc, shapePanel, owner, issueListProvider);
			offsetFromRight += mlcls.getWidth() + MolecularComponentLargeShape.calculateComponentSeparation(shapePanel);
			componentShapes.add(0, mlcls);
		}
	}
	// called for species patterns (rules, species, observables)
	public MolecularTypeLargeShape(int xPos, int yPos, MolecularTypePattern mtp, LargeShapeCanvas shapePanel, 
			Displayable owner, int positionInPattern, Structure structure, IssueListProvider issueListProvider) {
		super(issueListProvider);
		
		this.owner = owner;
		this.pattern = true;
		this.mt = mtp.getMolecularType();
		this.mtp = mtp;
		this.xPos = xPos;
		this.yPos = yPos;
		this.shapePanel = shapePanel;
		this.positionInPattern = positionInPattern;
		this.structure = structure;

		baseWidth = calculateBaseWidth(shapePanel);
		baseHeight = calculateBasHeight(shapePanel);
		cornerArc = calculateCornerArc(shapePanel);
		compartmentOffset = calculateCompartmentOffset(shapePanel);

		// we adjust the width of the first molecule in the species pattern to make space for the compartment depiction
		width = baseWidth;
		if(positionInPattern == 0) {
			width += compartmentOffset;
		}
		
		int numComponents = mt.getComponentList().size();	// components
		int offsetFromRight = 0;		// total width of all components, based on the length of their names
		// if there's anchoring info to show, reserve some extra space at the end
		if(structure != null && mt != null && !mt.isAnchorAll() && mt.getAnchors().size()>0) {
			int anchorOffset = calculateAnchorWidth(shapePanel)-4;
			offsetFromRight += anchorOffset;
		}

		for(int i=numComponents-1; i >=0; i--) {
//			MolecularComponentPattern mcp = mtp.getComponentPatternList().get(i);
			MolecularComponent mc = mt.getComponentList().get(i);
			MolecularComponentPattern mcp = mtp.getMolecularComponentPattern(mc);
			MolecularComponentLargeShape mlcls = new MolecularComponentLargeShape(100, 50, mcp, shapePanel, owner, issueListProvider);
			offsetFromRight += mlcls.getWidth() + MolecularComponentLargeShape.calculateComponentSeparation(shapePanel);
		}
		name = adjustForSize();
		width += offsetFromRight;				// adjusted for # of components
		width += getStringWidth(name);			// adjust for the length of the name of the molecular type
		height = baseHeight + MolecularComponentLargeShape.calculateBasHeight(shapePanel) / 2;
		
		int fixedPart = xPos + width;
		offsetFromRight = 10;
		if(structure != null && mt != null && !mt.isAnchorAll() && mt.getAnchors().size()>0) {
			int anchorOffset = calculateAnchorWidth(shapePanel)-4;		// -2
			offsetFromRight += anchorOffset;
		}
		for(int i=numComponents-1; i >=0; i--) {
			int rightPos = fixedPart - offsetFromRight;		// we compute distance from right end
			int y = yPos + height - MolecularComponentLargeShape.calculateBasHeight(shapePanel);

//			MolecularComponentPattern mcp = mtp.getComponentPatternList().get(i);
			MolecularComponent mc = mt.getComponentList().get(i);
			MolecularComponentPattern mcp = mtp.getMolecularComponentPattern(mc);
			MolecularComponentLargeShape mlcls = new MolecularComponentLargeShape(rightPos, y, mcp, shapePanel, owner, issueListProvider);
			offsetFromRight += mlcls.getWidth() + MolecularComponentLargeShape.calculateComponentSeparation(shapePanel);
			componentShapes.add(0, mlcls);
		}
	}
	
	public MolecularType getMolecularType() {
		return mt;
	}

	private String adjustForSize() {
		if(shapePanel.getZoomFactor() < LargeShapeCanvas.SmallestZoomFactorWithText) {
			// when we zoom to very small shapes we must stop writing the text
			return "  ";
		}
		// we truncate to 12 characters any name longer than 12 characters
		// we keep the first 7 letters, then 2 points, then the last 3 letters
		String s = null;
		if(mt == null) {
			if(owner instanceof SpeciesContext) {
				s = owner.getDisplayName();
			} else if(owner instanceof RbmObservable) {
				s = owner.getDisplayName();
			} else {
				s = "?";
			}
		} else {
			s = mt.getDisplayName();
		}
		int len = s.length();
		if(len > 12) {
			return(s.substring(0,7) + ".." + s.substring(len-3, len));
		} else {
			return(s);
		}
	}
	
	private int getStringWidth(String s) {
		Graphics gc = shapePanel.getGraphics();
		Font font = gc.getFont().deriveFont(Font.BOLD);
		FontMetrics fm = gc.getFontMetrics(font);
		int stringWidth = fm.stringWidth(s);
//		AffineTransform at = font.getTransform();
//		FontRenderContext frc = new FontRenderContext(at,true,true);
//		int textwidth = (int)(font.getStringBounds(s, frc).getWidth());
		return stringWidth;
	}

	@Override
	public void setX(int xPos){ 
		this.xPos = xPos;
	}
	@Override
	public int getX(){
		return xPos;
	}
	@Override
	public void setY(int yPos){
		this.yPos = yPos;
	}
	@Override
	public int getY(){
		return yPos;
	}
	@Override
	public int getWidth(){
		return width;
	} 
	@Override
	public int getHeight(){
		return height;
	}
	@Override
	public Rectangle getLabelOutline() {
		Graphics gc = shapePanel.getGraphics();
		Font font = gc.getFont().deriveFont(Font.BOLD);
		FontMetrics fm = gc.getFontMetrics(font);
		int stringWidth = fm.stringWidth(getFullName());
		Rectangle labelOutline = new Rectangle(xPos+8, yPos+7, stringWidth+11, fm.getHeight()+5);
		return labelOutline;
	}
	@Override
	public Font getLabelFont() {
		Graphics gc = shapePanel.getGraphics();
		Font font = gc.getFont().deriveFont(Font.BOLD);
		return font;
	}
	@Override
	public String getFullName() {
		if(mt != null) {
			return mt.getDisplayName();
		} else {
			return "?";
		}
	}

	public final boolean getPattern() {
		return pattern;
	}
	public final MolecularTypePattern getMolecularTypePattern() {
		return mtp;
	}
	public MolecularComponentLargeShape getComponentShape(int index) {
		return componentShapes.get(index);
	}
	public MolecularComponentLargeShape getShape(MolecularComponentPattern mcpTo) {
		for(MolecularComponentLargeShape mcs : componentShapes) {
			MolecularComponentPattern mcpThis = mcs.getMolecularComponentPattern();
			if(mcpThis == mcpTo) {
				return mcs;
			}
		}
		return null;
	}

	@Override
	public boolean contains(PointLocationInShapeContext locationContext) {
		
		// first we check if the point is inside a subcomponent of "this"
		for(MolecularComponentLargeShape mcs : componentShapes) {
			boolean found = mcs.contains(locationContext);
			if(found) {
				// since point is inside one of our components it's also inside "this"
				locationContext.mts = this;
				return true;	// if the point is inside a MolecularComponentLargeShape there's no need to check others
			}
		}
		if(owner instanceof SpeciesContext && !((SpeciesContext)owner).hasSpeciesPattern()) {
			// special case: clicked inside plain species, we only want to allow the user to add a species pattern
			// we'll behave as if the user clicked outside the shape
			return false;
		}
		// even if the point it's not inside one of our subcomponents it may still be inside "this"
		RoundRectangle2D rect = new RoundRectangle2D.Float(xPos, yPos, width, baseHeight, cornerArc, cornerArc);
		if(rect.contains(locationContext.point)) {
			locationContext.mts = this;
			return true;
		}
		return false;		// locationContext.mts remains null;
	}
	
	public void setMatchesSignature(boolean bMatchesSignature) {
		this.bMatchesSignature = bMatchesSignature;
		for(MolecularComponentLargeShape mcls : componentShapes) {
			mcls.setMatchesSignature(bMatchesSignature);
		}
	}
	// ----------------------------------------------------------------------------------------
	public String getAnchorsHTML() {
		String str = "";
		if(mt != null && !mt.isAnchorAll() && mt.getAnchors().size()>0) {
			str += "<html>";
			boolean first = true;
			for(Structure s : mt.getAnchors()) {
				if(!first) {
					str += "<br>";
				}
				first = false;
				str += s.getName();
			}
			str += "</html>";
		}
		return str;
	}
	public Rectangle getAnchorRectangleRight() {
		if(mt != null && !mt.isAnchorAll()) {
			int SHIFT = 2;	// the rectangle is shifted a few pixels to the left of the end of the molecule
							// (looks better that way)
			int compWidth = calculateAnchorWidth(shapePanel);
			int x = xPos + width - compWidth-SHIFT;
			int y = yPos-4;
			int compHeight = 59;
			Rectangle r2d = new Rectangle(x, y, compWidth, compHeight);
			return r2d;
		}
		return null;
	}
	public Rectangle getAnchorHotspot() {		// mouse over this rectangle should display the anchor tooltip
		if(mt != null && !mt.isAnchorAll() && mt.getAnchors().size()>0) {
			Rectangle r = getAnchorRectangleRight();	// position and dimension based on anchor gray rectangle

			Rectangle r2d = new Rectangle(r.x+1, yPos+baseHeight+1, r.width-2, r.height-baseHeight-6);
			return r2d;
		}
		return null;
	}

	public void paintNarrowCompartmentRight(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		Color colorOld = g2.getColor();
		Paint paintOld = g2.getPaint();
		
		int z = shapePanel.getZoomFactor();
		Rectangle r = getAnchorRectangleRight();
		Rectangle2D border = new Rectangle2D.Double(r.x, r.y, r.width, r.height);
		Color darker = getDefaultColor(Color.gray);	// a bit darker for border
		g2.setColor(darker);
		g2.draw(border);
		
		Color lighter;
		if(owner instanceof MolecularType) {
			if(mt.getAnchors().size()==0) {
				lighter = MolecularComponentLargeShape.componentBad;
			} else {
				// molecules may be anchored to multiple structures, 
				// so we leave it white since it may be a mix of membranes and compartments
				lighter = Color.white;
			}
		} else {	// non molecular type, we make sure before calling this that structure is not null
			boolean found = false;
			for(Structure struct : mt.getAnchors()) {
				if(structure.getName().equals(struct.getName())) {
					found = true;
					break;
				}
			}
			if(!found) {	// molecule can't be in the structure where the species pattern is
				lighter = MolecularComponentLargeShape.componentBad;
			} else {
				if(structure.getTypeName().equals(Structure.TYPE_NAME_MEMBRANE)) {
					lighter = new Color(192, 192, 192);		// 192	208
				} else {
					lighter = new Color(240, 240, 240);		// 244
				}
			}
		}
		Rectangle2D filling = new Rectangle2D.Double(r.x, r.y+1, r.width, r.height-1);
		g2.setPaint(lighter);
		g2.fill(filling);
		
	    g2.setPaint(paintOld);
		g2.setColor(colorOld);
	}
	
	@Override
	public void paintSelf(Graphics g) {
		paintSpecies(g);
	}
	// --------------------------------------------------------------------------------------
	private void paintSpecies(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		Font fontOld = g2.getFont();
		Color colorOld = g2.getColor();
		Stroke strokeOld = g2.getStroke();
		Color primaryColor = null;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		if(mt == null && mtp == null) {		// plain species context
			Color exterior;
			if(owner == null) {						// for plain species, we just draw a circle based on height!!! we ignore width!!!
				exterior = Color.red.darker();		// error
			} else {
				if(!shapePanel.isShowMoleculeColor()) {
					exterior = Color.GRAY;
				} else {
					exterior = Color.green.darker().darker();		// plain species
				}
			}
			Point2D center = new Point2D.Float(xPos+baseHeight/3, yPos+baseHeight/3);
			float radius = baseHeight*0.5f;
			Point2D focus = new Point2D.Float(xPos+baseHeight/3-1, yPos+baseHeight/3-1);
			float[] dist = {0.1f, 1.0f};
			Color[] colors = {Color.white, exterior};
			RadialGradientPaint p = new RadialGradientPaint(center, radius, focus, dist, colors, CycleMethod.NO_CYCLE);
			g2.setPaint(p);
			Ellipse2D circle = new Ellipse2D.Double(xPos, yPos, baseHeight, baseHeight);
			g2.fill(circle);
			Ellipse2D circle2 = new Ellipse2D.Double(xPos-1, yPos-1, baseHeight+0.7, baseHeight+0.7);
			g2.setPaint(getDefaultColor(Color.DARK_GRAY));
			int z = shapePanel.getZoomFactor();
			g2.setStroke(new BasicStroke(2.0f+0.14f*z));
			g2.draw(circle2);
			
			if(owner == null) {
				Font font = fontOld.deriveFont(Font.BOLD);
				g.setFont(font);
				g.setColor(Color.red.darker().darker());
				g2.drawString("Error parsing generated species!", xPos+baseHeight+10, yPos+baseHeight-9);
			}
			g2.setFont(fontOld);
			g2.setColor(colorOld);
			g2.setStroke(strokeOld);
			return;
		} else {							// molecular type, species pattern, observable
			if(mt == null || mt.getModel() == null) {
				primaryColor = Color.blue.darker().darker();
			} else {
				if(shapePanel.getDisplayMode()==DisplayMode.participantSignatures) {
					if(!shapePanel.isShowMoleculeColor()) {
						primaryColor = AbstractComponentShape.componentVeryLightGray;
					} else {
						RbmModelContainer rbmmc = mt.getModel().getRbmModelContainer();
						List<MolecularType> mtList = rbmmc.getMolecularTypeList();
						int index = mtList.indexOf(mt);
						index = index%7;
						primaryColor = colorTable[index].darker().darker();
					}
					if(shapePanel.isShowDifferencesOnly()) {
						ReactionRule reactionRule = (ReactionRule)owner;
						switch (shapePanel.hasNoMatch(reactionRule.getName(), mtp)){
						case CHANGED:
							primaryColor = AbstractComponentShape.deepOrange;
							break;
						case UNCHANGED:		// keep whatever color we set above
							break;
						case ANALYSISFAILED:
							ArrayList<Issue> issueList = new ArrayList<Issue>();
							reactionRule.gatherIssues(new IssueContext(), issueList);
							boolean bRuleHasErrorIssues = false;
							for (Issue issue : issueList){
								if (issue.getSeverity() == Severity.ERROR){
									bRuleHasErrorIssues = true;
									break;
								}
							}
							if (bRuleHasErrorIssues) {
								primaryColor = AbstractComponentShape.componentHidden;
							}else{
								System.err.println("ReactionRule Analysis failed, but there are not Error Issues with ReactionRule "+reactionRule.getName());
								primaryColor = Color.red.darker();
							}
							break;
						default:
							break;
						}
					}
				} else if(shapePanel.getDisplayMode()==DisplayMode.rules) {
					if(!shapePanel.isShowMoleculeColor()) {
						primaryColor = AbstractComponentShape.componentVeryLightGray;
					} else {
						RbmModelContainer rbmmc = mt.getModel().getRbmModelContainer();
						List<MolecularType> mtList = rbmmc.getMolecularTypeList();
						int index = mtList.indexOf(mt);
						index = index%7;
						primaryColor = colorTable[index].darker().darker();
					}
					// if we show difference, we apply that now and override the color
					if(shapePanel.isShowDifferencesOnly()) {
						ReactionRule reactionRule = (ReactionRule)owner;
						switch (shapePanel.hasNoMatch(mtp)){
						case CHANGED:
							primaryColor = AbstractComponentShape.deepOrange;
							break;
						case UNCHANGED:		// keep whatever color we set above
							break;
						case ANALYSISFAILED:
							ArrayList<Issue> issueList = new ArrayList<Issue>();
							reactionRule.gatherIssues(new IssueContext(), issueList);
							boolean bRuleHasErrorIssues = false;
							for (Issue issue : issueList){
								if (issue.getSeverity() == Severity.ERROR){
									bRuleHasErrorIssues = true;
									break;
								}
							}
							if (bRuleHasErrorIssues) {
								primaryColor = AbstractComponentShape.componentHidden;
							}else{
								System.err.println("ReactionRule Analysis failed, but there are not Error Issues with ReactionRule "+reactionRule.getName());
								primaryColor = Color.red.darker();
							}
							break;
						default:
							break;
						}
					}
				} else {
					// TODO: do we ever get here legitimately? if not throw an exception!
					RbmModelContainer rbmmc = mt.getModel().getRbmModelContainer();
					List<MolecularType> mtList = rbmmc.getMolecularTypeList();
					int index = mtList.indexOf(mt);
					index = index%7;
					if(!shapePanel.isShowMoleculeColor()) {
						primaryColor = AbstractComponentShape.componentVeryLightGray;
					} else {
						primaryColor = isHighlighted() == true ? Color.white : colorTable[index].darker().darker();
					}
					if(hasErrorIssues(owner, mt)) {
						primaryColor = isHighlighted() ? Color.white : Color.red;
					}
				}
			}
		}
		
		// paint the structure contour if applicable (only for anchored molecules!)
		if(structure != null && mt != null && !mt.isAnchorAll() && mt.getAnchors().size()>0) {
			paintNarrowCompartmentRight(g);
		} else if(owner instanceof MolecularType && !mt.isAnchorAll()) {
			paintNarrowCompartmentRight(g);
		}
		
		// paint the shape of the molecule and fill it with color
		GradientPaint p = new GradientPaint(xPos, yPos, primaryColor, xPos, yPos + baseHeight/2, Color.WHITE, true);
		g2.setPaint(p);

		RoundRectangle2D rect = new RoundRectangle2D.Float(xPos, yPos, width, baseHeight, cornerArc, cornerArc);
		g2.fill(rect);

		RoundRectangle2D inner = new RoundRectangle2D.Float(xPos+1, yPos+1, width-2, baseHeight-2, cornerArc-3, cornerArc-3);
		if(isHighlighted()) {
			if(hasErrorIssues(owner, mt)) {
				g2.setPaint(Color.red);
			} else {
				g2.setPaint(getDefaultColor(Color.BLACK));
			}
			g2.draw(inner);
			if(hasErrorIssues(owner, mt)) {
				g2.setPaint(Color.red);
			} else {
				g2.setPaint(getDefaultColor(Color.BLACK));
			}
			g2.draw(rect);
		} else {
			if(hasErrorIssues(owner, mt)) {
				g2.setPaint(Color.red.darker());
			} else {
				g2.setPaint(getDefaultColor(Color.GRAY));
			}
			g2.draw(inner);
			if(hasErrorIssues(owner, mt)) {
				g2.setPaint(Color.red.darker());
			} else {
				g2.setPaint(getDefaultColor(Color.DARK_GRAY));
			}
			g2.draw(rect);
		}

		// paint the anchor glyph
		Rectangle r = getAnchorHotspot();
		if(r != null) {
//			g2.drawRect(r.x, r.y, r.width, r.height);	// anchor tooltip hotspot area 
			int z = shapePanel.getZoomFactor();
			int w = r.width;
			int x = r.x + w/2;
			int y = r.y;
			int h = 12+z/2;
			h = z<-2 ? h-1 : h;
			h = z<-4 ? h-1 : h;
			
			Line2D line = new Line2D.Float(x, y, x, y+h);
			g2.setPaint(getDefaultColor(Color.RED.darker().darker()));
			g2.setStroke(new BasicStroke(2.6f+0.13f*z));
			g2.draw(line);
			
			// TODO: adjust the arc at deep zoom!
			double a1 = z<-3 ? 245 : 240;
			double a2 = z<-3 ? 52 : 59;	// 60
			Arc2D arc = new Arc2D.Double(x-h, y-h, 2*h, 2*h, 
					a1, a2, Arc2D.OPEN);
			g2.setStroke(new BasicStroke(2.6f+0.20f*z));
			g2.draw(arc);
			g2.setPaint(colorOld);
			g2.setStroke(strokeOld);
		}
		
		
//		Rectangle r = getAnchorRectangle();
//		if(r != null) {
//			// icon looking like an anchor
//			int z = shapePanel.getZoomFactor();
//			int w = r.width;
//			int x = r.x + w/2;
//			int y = r.y;
//			int yb = y + r.height;	// y at bottom
////			g2.drawRect(r.x, r.y, r.width, r.height);		// hotspot for anchor tooltips
//			Line2D line = new Line2D.Float(x, y, x, yb);
//			g2.setPaint(getDefaultColor(Color.RED.darker().darker()));
//			g2.setStroke(new BasicStroke(2.1f+0.13f*z));
//			g2.draw(line);
//			int h = r.height + 1;
//			h = z < -3 ? h-1 : h;
//			Arc2D arc = new Arc2D.Double(x-w/2, y, w, h, 
//					210, 120, Arc2D.OPEN);
//			g2.setStroke(new BasicStroke(2.1f+0.20f*z));
//			g2.draw(arc);
//			g2.setPaint(colorOld);
//			g2.setStroke(strokeOld);
//		}
		
		// the text inside the molecule shape 
		if(mt == null && mtp == null) {		// plain species context
			 // don't write any text inside
		} else {							// molecular type, species pattern
			Graphics gc = shapePanel.getGraphics();
			Font font = deriveMoleculeFontBold(g, shapePanel);
			g.setFont(font);
			g.setColor(getDefaultColor(Color.BLACK));	// font color
			int fontSize = font.getSize();
			
			int textX = xPos + 11;
			int textY =  yPos + baseHeight - (baseHeight - fontSize)/2;
			g2.drawString(name, textX, textY);
			
			if(owner instanceof ReactionRule && mtp != null && mtp.hasExplicitParticipantMatch()) {
				int z = shapePanel.getZoomFactor();
				if(z >= LargeShapeCanvas.SmallestZoomFactorWithText) {	// hide the matching too when we don't display the name
					FontMetrics fm = gc.getFontMetrics(font);
					int stringWidth = fm.stringWidth(name);
					Font smallerFont = font.deriveFont(font.getSize() * 0.8F);
					g.setFont(smallerFont);
					g2.drawString(mtp.getParticipantMatchLabel(), textX + stringWidth + 2, textY + 2);
				}
			}
		}
		g.setFont(fontOld);
		g.setColor(colorOld);
		g2.setStroke(strokeOld);
		
		for(MolecularComponentLargeShape mcls : componentShapes) {	// paint the components
			mcls.paintSelf(g);
		}
		g2.setFont(fontOld);
		g2.setColor(colorOld);
		g2.setStroke(strokeOld);
	}
	
	private Color getDefaultColor(Color defaultCandidate) {
		if(shapePanel == null) {
			return defaultCandidate;
		}
		return shapePanel.isEditable() ? defaultCandidate : LargeShapeCanvas.uneditableShape;
	}
	
	public static Font deriveMoleculeFontBold(Graphics gc, LargeShapeCanvas shapePanel) {
		Font fontOld = gc.getFont();
		int bh = calculateBasHeight(shapePanel);
		Font font = fontOld.deriveFont((float) (bh*7/17)).deriveFont(Font.BOLD);
		return font;
	}


	public static void paintDummy(Graphics g, int xPos, int yPos, LargeShapeCanvas shapePanel) {
		Graphics2D g2 = (Graphics2D)g;
		Color colorOld = g2.getColor();
		Stroke strokeOld = g2.getStroke();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		final float dash1[] = { 6.0f };
		final BasicStroke dashed = new BasicStroke(2.0f,BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
		g2.setStroke(dashed);
		int w = getDummyWidth(shapePanel);
		int h = calculateBasHeight(shapePanel);
		int c = calculateCornerArc(shapePanel);
		RoundRectangle2D rect = new RoundRectangle2D.Float(xPos, yPos, w, h, c, c);
		//RoundRectangle2D inner = new RoundRectangle2D.Float(xPos+1, yPos+1, w-2, h-2, c-3, c-3);
		g2.setPaint(Color.LIGHT_GRAY);
		//g2.draw(inner);
		g2.setPaint(Color.LIGHT_GRAY);
		g2.draw(rect);
		g2.setColor(colorOld);
		g2.setStroke(strokeOld);
	}
	public static int getDummyWidth(LargeShapeCanvas shapePanel) {
		return calculateBaseWidth(shapePanel)+30;
	}
	
	@Override
	public void setHighlight(boolean b, boolean param) {
		// param is being ignored
		if(owner instanceof RbmObservable) {
			shapePanel.setMolecularTypePattern(b ? mtp : null);
		} else if(owner instanceof MolecularType) {
			shapePanel.setMolecularType(b ? mt : null);
		} else if(owner instanceof SpeciesContext) {
			if(mtp != null) {
				shapePanel.setMolecularTypePattern(b ? mtp : null);	// plain species don't have sp, nor mtp
			}
		} else if(owner instanceof ReactionRule) {
			shapePanel.setMolecularTypePattern(b ? mtp : null);
		} else {
			System.out.println("Unexpected owner: " + owner);
		}
	}
	@Override
	public boolean isHighlighted() {
		if(owner instanceof RbmObservable) {
			return shapePanel.isHighlighted(mtp);
		} else if(owner instanceof MolecularType) {
			return shapePanel.isHighlighted(mt);
		} else if(owner instanceof SpeciesContext) {
			if(mtp != null) {
				return shapePanel.isHighlighted(mtp);
			} else {
				return false;
			}
		} else if(owner instanceof ReactionRule) {
			// we don't highlight when we display single row (view only, no edit), hence no selection
			if(shapePanel == null) {
				return false;
			}
			if(shapePanel.getDisplayMode()==DisplayMode.rules && shapePanel.isViewSingleRow()) {
				return false;
			}

			return shapePanel.isHighlighted(mtp);
		} else {
			System.out.println("Unexpected owner: " + owner);
		}
		return false;
	}
	@Override
	public void turnHighlightOffRecursive(Graphics g) {
		boolean oldHighlight = isHighlighted();
		setHighlight(false, false);
		if(oldHighlight == true) {
			paintSelf(g);			// paint self not highlighted if previously highlighted
		}
		for(MolecularComponentLargeShape mcls : componentShapes) {
			mcls.turnHighlightOffRecursive(g);
		}
	}
	public void flash(String matchKey) {
		if(!(owner instanceof ReactionRule)) {
			return;
		}
		if(mtp != null && mtp.hasExplicitParticipantMatch() && mtp.getParticipantMatchLabel().equals(matchKey)) {
			Graphics g = shapePanel.getGraphics();
			Graphics2D g2 = (Graphics2D)g;
			Font fontOld = g2.getFont();
			Color colorOld = g2.getColor();
			
			Color color = (Color.red).darker();
			Font font = deriveMoleculeFontBold(g, shapePanel);
			Font smallerFont = font.deriveFont(font.getSize() * 0.8F);
			g.setFont(smallerFont);
			FontMetrics fm = g.getFontMetrics(font);
			int stringWidth = fm.stringWidth(name);
			int textX = xPos + 11;
			int textY =  yPos + baseHeight - (baseHeight - smallerFont.getSize())/2;
			g.setColor(color);
			g2.drawString(mtp.getParticipantMatchLabel(), textX + stringWidth + 2, textY + 2);
			
			g.setFont(fontOld);
			g.setColor(colorOld);
		}
	}

	@Override
	public String getDisplayName() {
		throw new UnsupportedOperationException();
	}
	@Override
	public String getDisplayType() {
		return MolecularType.typeName;
	}

}
