//@VCellHelper vh
//@ImageJ ij


import java.awt.Dimension;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.vcell.imagej.helper.VCellHelper;
import org.vcell.imagej.helper.VCellHelper.BasicStackDimensions;
import org.vcell.imagej.helper.VCellHelper.IJData
import org.vcell.imagej.helper.VCellHelper.IJDataList
import org.vcell.imagej.helper.VCellHelper.IJGeom;
import org.vcell.imagej.helper.VCellHelper.IJSimStatus;
import org.vcell.imagej.helper.VCellHelper.VCellModelSearchResults;

import io.scif.img.SCIFIOImgPlus;
import net.imagej.DefaultDataset;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.display.ImageDisplay;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IterableRandomAccessibleInterval;



exampleFilesDir = new File("/home/vcell/Downloads/packImageJExample");
	
    	try {
	    	if(exampleFilesDir.getName().equals("DownloadedExamplesDir")){
	    		ij.IJ.showMessage("You must set the 'exampleFilesDir' variable to the location of the downloaded example files")
	    		return;
	    	}
	    	if(!exampleFilesDir.exists()){
	    		ij.IJ.showMessage("Couldn't find directory "+exampleFilesDir.getAbsoutePath()+".  hint: don't use '\' in directory path")
	    		return;
	    	}
	    	if(!exampleFilesDir.isDirectory()){
	    		ij.IJ.showMessage("Selected item is not a directory "+exampleFilesDir.getAbsoutePath())
	    		return;
	    	}
//			VCellHelper vh = new VCellHelper();
//			ImageJ ij = new ImageJ();
//			ij.launch(new String[0]);
			
			ExampleDatasets exampleDatasets = openExample(ij, exampleFilesDir);
			int ANALYZE_BEGIN_TIMEINDEX = 3;
			int ANALYZE_END_TIMEINDEX = 8;
	    	int ANALYZE_COUNT = ANALYZE_END_TIMEINDEX - ANALYZE_BEGIN_TIMEINDEX +1;
	    	int xIndex = exampleDatasets.experimentalData.dimensionIndex(Axes.X);
	    	int xsize = (int)exampleDatasets.experimentalData.dimension(xIndex);
	    	int yIndex = exampleDatasets.experimentalData.dimensionIndex(Axes.Y);
	    	int ysize = (int)exampleDatasets.experimentalData.dimension(yIndex);
			FinalInterval analyzeOrigInterval = FinalInterval.createMinSize([0,0,ANALYZE_BEGIN_TIMEINDEX, xsize,ysize,ANALYZE_COUNT] as long[]);//xyzt:origin and xyzt:size
	    	int assumedZIndex = -1;
	    	for (int i = 0; i < exampleDatasets.segmentedGeom.numDimensions(); i++) {
				if(exampleDatasets.segmentedGeom.dimensionIndex(Axes.X)!=i && exampleDatasets.segmentedGeom.dimensionIndex(Axes.Y)!=i) {
					assumedZIndex = i;
					break;
				}
			}
	    	int zsize = (assumedZIndex == -1?1:(int)exampleDatasets.segmentedGeom.dimension(assumedZIndex));
	    	HashMap<Integer, String> segmentedGeomValuesMapSubvolumeName = new HashMap();
	    	segmentedGeomValuesMapSubvolumeName.put(0, "cyt");
	    	segmentedGeomValuesMapSubvolumeName.put(255, "Nuc");
			VCellHelper.IJGeom overrideGeom = createGeometry(exampleDatasets.segmentedGeom, xIndex, yIndex, assumedZIndex, xsize, ysize, zsize, segmentedGeomValuesMapSubvolumeName);
	    	double[] diffRates = [	    			
			1.25,
//			1.3,
//			1.4,
//			1.45,
//			1.5,
//			1.55,
//			1.6
			] as double[];
	    	double[] mse = new double[diffRates.length];
			for (int i = 0; i < diffRates.length; i++) {
				String simulationCacheKey = getSimulationCacheKey(vh);
				println(simulationCacheKey)
				IJSimStatus ijSimStatus = runFrapSolver(vh, diffRates[i],simulationCacheKey,overrideGeom);
				String simulationDataCacheKey = waitForSolverGetCacheForData(vh, ijSimStatus);
				SCIFIOImgPlus<DoubleType> annotatedZProjectedSimPostBleachData = zProjectNormalizeSimData(vh, "Sim Data "+diffRates[i], simulationDataCacheKey, "rf", 0, ijSimStatus.getJobIndex(), ANALYZE_BEGIN_TIMEINDEX, ANALYZE_END_TIMEINDEX);
				showAndZoom(ij, "Sim Data "+diffRates[i], annotatedZProjectedSimPostBleachData, 4);
				mse[i] = calcMSE(ij, annotatedZProjectedSimPostBleachData,exampleDatasets,analyzeOrigInterval);
			}
	    	for(int i=0;i<mse.length;i++){
	    		println("diffRate="+diffRates[i]+" MSE="+(mse[i]));
	    		ij.IJ.log("diffRate="+diffRates[i]+" MSE="+(mse[i]))
	    	}

		} catch (Exception e) {
			e.printStackTrace();
		}

	def String getSimulationCacheKey(VCellHelper vh) throws Exception{
    	// User can search (with simple wildcard *=any) for VCell Model by:
		// model type {biomodel,mathmodel,quickrun}
		// user name
		// model name 
		// application name (if searching for BioModels, not applicable for MathModels)
		// simulation name
		// integer Geometry dimension 0-ODE (1,2,3)-PDE
		// math type 'Deterministic' or 'Stochastic'
    	SimpleDateFormat easyDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	//VCellHelper.VCellModelSearch search = new VCellHelper.VCellModelSearch(VCellHelper.ModelType.bm, "tutorial", "Tutorial_FRAPbinding", "Spatial", "FRAP binding",null,null);
    	VCellHelper.VCellModelSearch search = new VCellHelper.VCellModelSearch(VCellHelper.ModelType.bm, "tutorial", "Tutorial_FRAPbinding", "Spatial", "FRAP binding",2,"Deterministic");
		// version time range (all VCell Models saved on the VCell server have a last date 'saved')
    	VCellHelper.VCellModelVersionTimeRange vcellModelVersionTimeRange = new VCellHelper.VCellModelVersionTimeRange(easyDate.parse("2015-06-01 00:00:00"), easyDate.parse("2025-01-01 00:00:00"));
		//Search VCell database for models matching search parameters
    	ArrayList<VCellModelSearchResults> vcellModelSearchResults = vh.getSearchedModelSimCacheKey(true, search,vcellModelVersionTimeRange);
		//This search should only find 1
    	if(vcellModelSearchResults.size() != 1) {
			ij.IJ.showMessage("Expecting 1 search result for "+search.getModelName()+" but got "+vcellModelSearchResults.size()+", make sure model is open in VCell client");
			throw new Exception("Expecting only 1 model from search results");
		}

    	String cacheKey = vcellModelSearchResults.get(0).getCacheKey();
    	try{
    		//make sure original sim result is open
    		//getTimePointData(String simulationDataCacheKey,String variableName,VARTYPE_POSTPROC varTypePostProcess,int[] timePointIndexes,int simulationJobIndex)
    		tIndexes = [0] as int[]
			VCellHelper.IJDataList ijDataList = vh.getTimePointData(cacheKey, null, VCellHelper.VARTYPE_POSTPROC.NotPostProcess,tIndexes, 0);
    	}catch(Exception e){
    		ij.IJ.showMessage("Make sure Application '"+search.getApplicationName()+"' -> Simulation '"+search.getSimulationName()+"' results viewer is open");
    		e.printStackTrace()
    		println(e.getMessage())
    		throw e
    	}

    	return cacheKey;

	}
	def IJSimStatus runFrapSolver(VCellHelper vh,double diffusionRate,String cacheKey,VCellHelper.IJGeom overrideGeom) throws Exception{
    	//Override Model/Simulation parameter values (user must have knowledge of chosen model to know what parameter names to override)
    	HashMap<String,String> simulationParameterOverrides = new HashMap<>();
    	simulationParameterOverrides.put("rf_diffusionRate", ""+diffusionRate);
    	HashMap<String, String> speciesContextInitialConditionsOverrides = new HashMap<>();
    	String laserArea = "((x >=  "+43+") && (x <= "+47+") && (y >=  "+22+") && (y <= "+26+"))";
    	speciesContextInitialConditionsOverrides.put("Laser", laserArea);
    	double newEndTime = 10.0;
    	//Start Frap simulation
    	IJSimStatus iJSimStatus = vh.startVCellSolver(Long.parseLong(cacheKey), overrideGeom, simulationParameterOverrides, speciesContextInitialConditionsOverrides,newEndTime);
		return iJSimStatus;
	}
	def String waitForSolverGetCacheForData(VCellHelper vh,IJSimStatus iJSimStatus) throws Exception{
    	//Wait for solver to finish
    	System.out.println(iJSimStatus.toString());
    	ij.IJ.log(iJSimStatus.toString())
    	String simulationJobId = iJSimStatus.simJobId;
    	while(true) {
    		Thread.sleep(5000);
        	iJSimStatus =  vh.getSolverStatus(simulationJobId);
        	System.out.println(iJSimStatus.toString());
        	ij.IJ.log(iJSimStatus.toString())
        	String statusName = iJSimStatus.statusName.toLowerCase();
        	if(statusName.equals("finished") || statusName.equals("stopped") || statusName.equals("aborted")) {
        		break;
        	}
    	}
    	return vh.getSimulationDataCacheKey(iJSimStatus.simJobId);
	}
	def SCIFIOImgPlus<DoubleType> zProjectNormalizeSimData(VCellHelper vh,
		String newImgPlusName,String simulationDataCachekey,String varToAnalyze,int preBleachTimeIndex,int solverStatus_jobIndex,int ANALYZE_BEGIN_TIMEINDEX,int ANALYZE_END_TIMEINDEX) throws Exception{
		
		int ANALYZE_COUNT = ANALYZE_END_TIMEINDEX - ANALYZE_BEGIN_TIMEINDEX+1;
	    // Get the SIMULATION pre-bleach timepoint data for normalizing
	    VCellHelper.IJDataList ijDataList = vh.getTimePointData(simulationDataCachekey, varToAnalyze, [preBleachTimeIndex] as int[], solverStatus_jobIndex);
		int xysize = ijDataList.ijData[0].stackInfo.xsize*ijDataList.ijData[0].stackInfo.ysize;
		double[] bleachedTimeStack = new double[ANALYZE_COUNT*xysize];
//	    checkSizes(timePointData.getBasicStackDimensions(), xsize, xysize, zsize);
//    	URL dataUrl = new URL("http://localhost:"+vh.findVCellApiServerPort()+"/"+"getdata"+"?"+"cachekey"+"="+cachekey+"&"+"varname"+"="+varToAnalyze+"&"+"timeindex"+"="+(int)(0)+"&"+"jobid="+ijSolverStatus.getJobIndex());
	    double[] zProjectedSimNormalizer = getNormalizedZProjected(ijDataList,null);
//showAndZoom(ij, "Sim Data PreBleach", ArrayImgs.doubles(simNormalizer, xsize,ysize), 4);
	    
	    // Get the SIMULATION post-bleach data to analyze
	    for(int timeIndex = ANALYZE_BEGIN_TIMEINDEX;timeIndex<=ANALYZE_END_TIMEINDEX;timeIndex++){
	    	ijDataList = vh.getTimePointData(simulationDataCachekey, varToAnalyze, [timeIndex] as int[], solverStatus_jobIndex);
//	    	checkSizes(timePointData.getBasicStackDimensions(), xsize, xysize, zsize);
//	    	dataUrl = new URL("http://localhost:"+vh.findVCellApiServerPort()+"/"+"getdata"+"?"+"cachekey"+"="+cachekey+"&"+"varname"+"="+varToAnalyze+"&"+"timeindex"+"="+(int)timeIndex+"&"+"jobid="+ijSolverStatus.getJobIndex());
	 		double[] data = getNormalizedZProjected(ijDataList,zProjectedSimNormalizer);
		    System.arraycopy(data, 0, bleachedTimeStack, (timeIndex-ANALYZE_BEGIN_TIMEINDEX)*xysize, data.length);
    	}

    	//Turn VCell data into iterableinterval
//    	FinalInterval zProjectedSimDataSize = FinalInterval.createMinSize(new long[] {0,0,0, xsize,ysize,ANALYZE_COUNT});//xyzt:origin and xyzt:size
		ArrayImg<DoubleType, DoubleArray> simImgs = ArrayImgs.doubles(bleachedTimeStack, [ijDataList.ijData[0].stackInfo.xsize,ijDataList.ijData[0].stackInfo.ysize,ANALYZE_COUNT] as long[]);
		SCIFIOImgPlus<DoubleType> annotatedZProjectedSimPostBleachData = new SCIFIOImgPlus<>(simImgs,newImgPlusName,[Axes.X,Axes.Y,Axes.TIME] as AxisType[]);
//    	RandomAccessibleInterval<DoubleType> simExtracted = ij.op().transform().crop(annotatedSimData, simExtractInterval);   	
//showAndZoom(ij, "Sim Data "+diffusionRate, annotatedZProjectedSimPostBleachData, 4);
		return annotatedZProjectedSimPostBleachData;
	}
    def double[] getNormalizedZProjected(VCellHelper.IJDataList ijDataList,double[] normalizer) throws Exception{
		VCellHelper.BasicStackDimensions basicStackDimensions = ijDataList.ijData[0].stackInfo;//VCellHelper.getDimensions(nodes.item(0));
 		//Sum pixel values in Z direction to match experimental data (open pinhole confocal, essentially brightfield)
		int xysize = basicStackDimensions.xsize*basicStackDimensions.ysize;
		double[] normalizedData = new double[xysize];
		for (int i = 0; i < ijDataList.ijData[0].getDoubleData().length; i++) {
			normalizedData[(i%xysize)]+= ijDataList.ijData[0].getDoubleData()[i];
		}
		if(normalizer != null) {
			for (int i = 0; i < normalizedData.length; i++) {
				if(normalizedData[i] != 0) {
					normalizedData[i] = (normalizedData[i]+Double.MIN_VALUE)/(normalizer[i]+Double.MIN_VALUE);
				}
			}
		}
		return normalizedData;
    }
//    public static void checkSizes(BasicStackDimensions basicStackDimensions,int xsize,int ysize,int zsize) throws Exception{
//		if(basicStackDimensions.xsize != xsize || basicStackDimensions.ysize != ysize || basicStackDimensions.zsize != zsize) {
//			throw new Exception("One or more sim data xyz dimensions="+basicStackDimensions.xsize+","+basicStackDimensions.ysize+","+basicStackDimensions.zsize+" does not match expected xyz sizes="+xsize+","+ysize+","+zsize);
//		}
//    }

    def double calcMSE(ImageJ ij,SCIFIOImgPlus<DoubleType> annotatedZProjectedSimPostBleachData,ExampleDatasets exampleDatasets,FinalInterval analyzeOrigInterval) throws Exception{
		//Calculate mean-squared-error as example, normalize experimental data by dividing by a prebleach image
		RandomAccessibleInterval<? extends RealType<?>> zProjectedExperimentalPostBleach = ij.op().transform().crop(exampleDatasets.experimentalData, analyzeOrigInterval);
    	Cursor<DoubleType> zProjectedSimPostBleachDataCursor = IterableRandomAccessibleInterval.create(annotatedZProjectedSimPostBleachData).localizingCursor();
    	Cursor<? extends RealType<?>> zProjectedExpPostBleachDataCursor = IterableRandomAccessibleInterval.create(zProjectedExperimentalPostBleach).localizingCursor();
    	IterableRandomAccessibleInterval<? extends RealType<?>> zProjectedPreBleachInterval = IterableRandomAccessibleInterval.create(exampleDatasets.preBleachImage);
    	BigDecimal sumSquaredDiff = new BigDecimal(0);
    	int[] analysisPosition = new int[exampleDatasets.analysisROI.numDimensions()];
    	int[] preBleachPosition = new int[zProjectedPreBleachInterval.numDimensions()];
    	int[] experimentalPosition = new int[zProjectedExperimentalPostBleach.numDimensions()];
    	int[] simulationPosition = new int[annotatedZProjectedSimPostBleachData.numDimensions()];
    	int numValsInMask = 0;
    	while(zProjectedSimPostBleachDataCursor.hasNext()){
    		Cursor<UnsignedByteType> analysisCursor = exampleDatasets.analysisROI.localizingCursor();
			Cursor<? extends RealType<?>> preBleachNormalizingCursor = zProjectedPreBleachInterval.localizingCursor();
        	while(analysisCursor.hasNext()){
        		boolean maskBit = !(analysisCursor.next().get() == 0);
        		DoubleType simPostBleachVal = zProjectedSimPostBleachDataCursor.next();
        		RealType<?> expPostBleachVal = zProjectedExpPostBleachDataCursor.next();
        		RealType<?> preBleachNormalizingVal = preBleachNormalizingCursor.next();
        		if(!maskBit) {//skip areas where analysis mask is 0
        			continue;
        		}
        		//Get the position of all the cursors and check they are in sync
        		analysisCursor.localize(analysisPosition);
        		preBleachNormalizingCursor.localize(preBleachPosition);
        		zProjectedExpPostBleachDataCursor.localize(experimentalPosition);
        		zProjectedSimPostBleachDataCursor.localize(simulationPosition);
//        		System.out.println("analysis="+Arrays.toString(analysisPosition)+" prebleach="+Arrays.toString(preBleachPosition)+" exp="+Arrays.toString(experimentalPosition)+" sim="+Arrays.toString(simulationPosition));
        		if(!Arrays.equals(analysisPosition, preBleachPosition) || !Arrays.equals(experimentalPosition, simulationPosition)){
        			throw new Exception("Cursor positions not equal");
        		}
        		if(analysisPosition[0] != experimentalPosition[0] || analysisPosition[1] != experimentalPosition[1]) {//check xy are same
        			throw new Exception("XY Cursor position not equal for analysisROI mask and experimental data");
        		}
        		double normalizedExpDataVal = 0;
        		if(expPostBleachVal.getRealDouble() != 0) {
        			normalizedExpDataVal = (expPostBleachVal.getRealDouble())/(preBleachNormalizingVal.getRealDouble()+Double.MIN_VALUE);
        		}
    			double diff = simPostBleachVal.get()-(normalizedExpDataVal);
    			sumSquaredDiff = sumSquaredDiff.add(new BigDecimal(Math.pow(diff,2.0)));
    			numValsInMask++;
        	}
    	}
		return sumSquaredDiff.divide(new BigDecimal(numValsInMask),8, RoundingMode.HALF_UP).doubleValue();

    }
    public class ExampleDatasets{
    	public ImgPlus<? extends RealType<?>> experimentalData;
    	public ImgPlus<? extends RealType<?>> preBleachImage;
    	public ImgPlus<UnsignedByteType> segmentedGeom;
    	public ImgPlus<UnsignedByteType> analysisROI;
		public ExampleDatasets(ImgPlus<? extends RealType<?>> experimentalData,
				ImgPlus<? extends RealType<?>> preBleachImage, ImgPlus<UnsignedByteType> segmentedGeom,
				ImgPlus<UnsignedByteType> analysisROI) {
			super();
			this.experimentalData = experimentalData;
			this.preBleachImage = preBleachImage;
			this.segmentedGeom = segmentedGeom;
			this.analysisROI = analysisROI;
		}
    }
    def ExampleDatasets openExample(ImageJ ij,File exampleDataDir) throws Exception{
    	final String[] exampleFiles = ["Experimental.zip","Seg Geom_3d.zip","PreBleach.zip","Analysis ROI.zip"] as String[];
//    	final String[] exampleFiles = new String[] {"testDiff_1_2.zip","Seg Geom_3d.zip","testPreBleach.zip","Analysis ROI.zip"};// for test
    	
    	
//    	List<Display<?>> displays0 = ij.display().getDisplays();
//    	for (Display<?> display : displays0) {
//			System.out.println("----------disp "+display.getName()+" "+display.getIdentifier());
//		}
    	List<ImageDisplay> knownImageDisplays = ij.imageDisplay().getImageDisplays();
//    	for (ImageDisplay imageDisplay : imageDisplays0) {
//    		System.out.println("----------imgdisp "+imageDisplay.getName()+" "+imageDisplay.getIdentifier());
//		}

    	ImgPlus<? extends RealType<?>> experimentalData = null;// 2D
    	ImgPlus<UnsignedByteType> segmentedGeom = null;// 2D or 3D (if 3D will be z-project for analysis withg 2D experimental data)
    	ImgPlus<? extends RealType<?>> preBleachImage = null;// 2D
    	ImgPlus<UnsignedByteType> analysisROI = null; // 2D
    	
    	// Open example images if they are not already open
    	for (int exampleFilesIndex = 0; exampleFilesIndex < exampleFiles.length; exampleFilesIndex++) {
    		DefaultDataset exampleFileDataset = null;
    		
    		for (ImageDisplay imageDisplay : knownImageDisplays) {
    			if(imageDisplay.getName().equals(exampleFiles[exampleFilesIndex])) {
    				exampleFileDataset = (DefaultDataset) imageDisplay.getActiveView().getData();
    				break;
    			}
    		}
			if(exampleFileDataset == null) {
				exampleFileDataset = (DefaultDataset)ij.io().open(new File(exampleDataDir,exampleFiles[exampleFilesIndex]).getAbsolutePath());
//				ij.ui().show(exampleFileDataset);
				showAndZoom(ij, exampleFiles[exampleFilesIndex], exampleFileDataset, 4);
			}
			// Print names of axes (x,y,z,time,channel,unknown,...)
			System.out.print(exampleFileDataset.getName()+": ");
	    	for (int dimensionIndex = 0; dimensionIndex < exampleFileDataset.numDimensions(); dimensionIndex++) {
	    		System.out.print((exampleFileDataset.axis(dimensionIndex)!=null?exampleFileDataset.axis(dimensionIndex).type().toString()+" ":"null "));
	    	}
	    	System.out.println();

	    	// Assign variables (defaultDataset corresponds to open 'exampleFiles' element
			switch (exampleFilesIndex) {
			case 0:
				experimentalData = exampleFileDataset.getImgPlus();
				break;
			case 1:
				segmentedGeom = (ImgPlus<UnsignedByteType>) exampleFileDataset.getImgPlus();
				break;
			case 2:
				preBleachImage = exampleFileDataset.getImgPlus();
				break;
			case 3:
				analysisROI = (ImgPlus<UnsignedByteType>) exampleFileDataset.getImgPlus();
				break;
			}
		}
    	return new ExampleDatasets(experimentalData, preBleachImage, segmentedGeom, analysisROI);
    }
    //Haven't yet found a way to reliably zoom image display programatically so it may display small despite the following code
    def void showAndZoom(ImageJ ij,String displayName,Object thingToDisplay,double zoomFactor) throws Exception{
//    	if(!displayName.startsWith("Sim Data")) {
//    		return;
//    	}
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				ij.ui().show(displayName,thingToDisplay);
		    	//Find display and set zoom, resize window
		    	List<ImageDisplay> knownImageDisplays = ij.imageDisplay().getImageDisplays();
		    	boolean bvisible = false;
				while (!bvisible) {
					for (ImageDisplay imageDisplay : knownImageDisplays) {
						if (imageDisplay.getName().equals(displayName)) {
							if (imageDisplay.isVisible(imageDisplay.getActiveView())) {
								bvisible = true;
								break;
							}
						}
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
//		    	int vw = ij.imageDisplay().getActiveImageDisplay().getCanvas().getViewportWidth();
//		    	int vh = ij.imageDisplay().getActiveImageDisplay().getCanvas().getViewportHeight();
//		    	System.out.println(" -----byname="+ij.display().getDisplay(displayName));
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
				    	try {
							ij.imageDisplay().getActiveImageDisplay().getCanvas().setZoom(zoomFactor);
							if(ij.display().getDisplay(displayName) != null && ij.ui().getDisplayViewer(ij.display().getDisplay(displayName)) instanceof JFrame) {
								double vw = ij.imageDisplay().getActiveImageDisplay().dimension(ij.imageDisplay().getActiveImageDisplay().dimensionIndex(Axes.X))*zoomFactor;
								double vh = ij.imageDisplay().getActiveImageDisplay().dimension(ij.imageDisplay().getActiveImageDisplay().dimensionIndex(Axes.Y))*zoomFactor;
								((JFrame)ij.ui().getDisplayViewer(ij.display().getDisplay(displayName)).getWindow()).setSize(new Dimension((int)vw+50, (int)vh+150));
							}
						} catch (Exception e) {
							e.printStackTrace();
						}				
					}
				});

//		    	ij.ui().getDisplayViewer(ij.display().getDisplay(displayName)).getPanel().redoLayout();
//		    	List<Display<?>> displays = ij.display().getDisplays();
//		    	for (Display<?> display : displays) {
//					System.out.println(display+" -----byname="+ij.display().getDisplay(displayName));
//				}
				
			}
		}).start();

    }

    def IJGeom createGeometry(ImgPlus<UnsignedByteType> nonZProjectedSegmentedGeom,int xIndex,int yIndex,int assumedZIndex,int xsize,int ysize,int zsize,HashMap<Integer, String> segmentedGeomValuesMapSubvolumeName) throws Exception{
    	//Create byte array that defines vcell subvolumes ids (handles)
    	byte[] vcellSubvolumeHandles = new byte[(int)Intervals.numElements(nonZProjectedSegmentedGeom)];
    	Cursor<UnsignedByteType> nonZProjectedSegmentedGeomCursor = nonZProjectedSegmentedGeom.localizingCursor();
    	while(nonZProjectedSegmentedGeomCursor.hasNext()){
    		UnsignedByteType segmentedGeomValue = (UnsignedByteType) nonZProjectedSegmentedGeomCursor.next();
    		int currentZ = (assumedZIndex==-1?0:nonZProjectedSegmentedGeomCursor.getIntPosition(assumedZIndex));
    		int currXYPixelIndex = nonZProjectedSegmentedGeomCursor.getIntPosition(yIndex)*xsize + nonZProjectedSegmentedGeomCursor.getIntPosition(xIndex);
    		int currXYZPixelIndex = currXYPixelIndex + (assumedZIndex==-1?0:currentZ*(xsize*ysize));
    		vcellSubvolumeHandles[currXYZPixelIndex] = (byte)segmentedGeomValue.get();
    	}
    	//Define new Geometry with domain equal to xyz pixel size for ease of use
    	String[] subvolumeNames = segmentedGeomValuesMapSubvolumeName.values().toArray(new String[0]);
    	Integer[] subvolumePixelValues = segmentedGeomValuesMapSubvolumeName.keySet().toArray(new Integer[0]);
    	double[] origin = [0,0,0] as double[];
    	double[] extent = [xsize,ysize,zsize] as double[];
    	IJGeom ijGeom = new IJGeom(subvolumeNames, subvolumePixelValues, xsize, ysize, zsize, origin, extent, vcellSubvolumeHandles);
    	return ijGeom;
    }

