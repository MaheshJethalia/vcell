package cbit.vcell.export;
import java.util.*;
import java.io.*;
import java.rmi.*;

import org.vcell.util.DataAccessException;
import org.vcell.util.VCDataIdentifier;
import org.vcell.util.document.User;

import cbit.vcell.mesh.CartesianMesh;
import cbit.vcell.simdata.*;
import cbit.vcell.export.nrrd.*;
/**
 * Insert the type's description here.
 * Creation date: (4/27/2004 12:53:34 PM)
 * @author: Ion Moraru
 */
public class RasterExporter implements ExportConstants {
	private ExportServiceImpl exportServiceImpl = null;

/**
 * Insert the method's description here.
 * Creation date: (4/27/2004 1:18:37 PM)
 * @param exportServiceImpl cbit.vcell.export.server.ExportServiceImpl
 */
public RasterExporter(ExportServiceImpl exportServiceImpl) {
	this.exportServiceImpl = exportServiceImpl;
}


/**
 * This method was created in VisualAge.
 */
private NrrdInfo[] exportPDEData(long jobID, User user, DataServerImpl dataServerImpl, VCDataIdentifier vcdID, VariableSpecs variableSpecs, TimeSpecs timeSpecs, GeometrySpecs geometrySpecs, RasterSpecs rasterSpecs, String tempDir) 
						throws RemoteException, DataAccessException, IOException {

	CartesianMesh mesh = dataServerImpl.getMesh(user, vcdID);
	String simID = vcdID.getID();
	switch (rasterSpecs.getFormat()) {
		case NRRD_SINGLE: {
			// single info, specifying 5D
			switch (geometrySpecs.getModeID()) {
				case GEOMETRY_FULL: {
					// create the info object
					NrrdInfo nrrdInfo = NrrdInfo.createBasicNrrdInfo(
						5,
						new int[] {mesh.getSizeX(),	mesh.getSizeY(), mesh.getSizeZ(), timeSpecs.getAllTimes().length, variableSpecs.getVariableNames().length},
						"double",
						"raw"
					);
					nrrdInfo.setContent("5D VCData from " + simID);
					nrrdInfo.setCenters(new String[] {"cell", "cell", "cell", "cell", "???"});
					nrrdInfo.setSpacings(new double[] {
						mesh.getExtent().getX() / mesh.getSizeX(),
						mesh.getExtent().getY() / mesh.getSizeY(),
						mesh.getExtent().getZ() / mesh.getSizeZ(),
						Double.NaN,	// timepoints can have irregular intervals
						Double.NaN  // not meaningful for variables
					});
					nrrdInfo.setSeparateHeader(rasterSpecs.isSeparateHeader());
					// make datafile and update info
					String fileID = simID + "_Full_" + timeSpecs.getAllTimes().length + "times_" + variableSpecs.getVariableNames().length + "vars";
					File datafile = new File(tempDir, fileID + "_data.nrrd");
					nrrdInfo.setDatafile(datafile.getName());
					DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(datafile)));
					try {
						for (int i = 0; i < variableSpecs.getVariableNames().length; i++){
							for (int j = 0; j < timeSpecs.getAllTimes().length; j++){
								double[] data = dataServerImpl.getSimDataBlock(user, vcdID, variableSpecs.getVariableNames()[i], timeSpecs.getAllTimes()[j]).getData();
								for (int k = 0; k < data.length; k++){
									out.writeDouble(data[k]);
								}
							}
						}
					} catch (IOException exc) {
						throw new DataAccessException(exc.toString());
					} finally {
						out.close();
					}
					// write out final output
					File headerfile = new File(tempDir, fileID + ".nrrd");
					nrrdInfo = NrrdWriter.writeNRRD(headerfile.getName(), headerfile.getParentFile(), nrrdInfo);
					if (! nrrdInfo.isSeparateHeader()) {
						datafile.delete(); // don't need it anymore, was appended to header
					}
					return new NrrdInfo[] {nrrdInfo};
				}
				default: {
					throw new DataAccessException("NRRD export from slice not yet supported");
				}
			}			
		}
		case NRRD_BY_TIME: {
			switch (geometrySpecs.getModeID()) {
				case GEOMETRY_FULL: {
					Vector nrrdinfoV = new Vector();
					for (int j = 0; j < timeSpecs.getAllTimes().length; j++){
						// create the info object
						NrrdInfo nrrdInfo = NrrdInfo.createBasicNrrdInfo(
							4,
							new int[] {mesh.getSizeX(),	mesh.getSizeY(), mesh.getSizeZ(),variableSpecs.getVariableNames().length},
							"double",
							"raw"
						);
						nrrdinfoV.add(nrrdInfo);
						nrrdInfo.setContent("4D VCData from " + simID);
						nrrdInfo.setCenters(new String[] {"cell", "cell", "cell","???"});
						nrrdInfo.setSpacings(new double[] {
							mesh.getExtent().getX() / mesh.getSizeX(),
							mesh.getExtent().getY() / mesh.getSizeY(),
							mesh.getExtent().getZ() / mesh.getSizeZ(),
							Double.NaN  // not meaningful for variables
						});
						nrrdInfo.setSeparateHeader(rasterSpecs.isSeparateHeader());
						//format time String
						StringBuffer timeSB = new StringBuffer(timeSpecs.getAllTimes()[j]+"");
						int dotIndex = timeSB.toString().indexOf(".");
						if(dotIndex != -1){
							timeSB.replace(dotIndex,dotIndex+1,"_");
							if(dotIndex == 0){
							timeSB.insert(0,"0");
							}
							if(dotIndex == timeSB.length()-1){
							timeSB.append("0");
							}
						}else{
							timeSB.append("_0");
						}
						// make datafile and update info						
						String fileID = simID + "_Full_" + timeSB.toString() + "time_" + variableSpecs.getVariableNames().length + "vars";
						File datafile = new File(tempDir, fileID + "_data.nrrd");
						nrrdInfo.setDatafile(datafile.getName());
						DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(datafile)));
						try {
							for (int i = 0; i < variableSpecs.getVariableNames().length; i++){
								//for (int j = 0; j < timeSpecs.getAllTimes().length; j++){
									double[] data = dataServerImpl.getSimDataBlock(user, vcdID, variableSpecs.getVariableNames()[i], timeSpecs.getAllTimes()[j]).getData();
									for (int k = 0; k < data.length; k++){
										out.writeDouble(data[k]);
									}
								//}
							}
						} catch (IOException exc) {
							throw new DataAccessException(exc.toString());
						} finally {
							out.close();
						}
						// write out final output
						File headerfile = new File(tempDir, fileID + ".nrrd");
						nrrdInfo = NrrdWriter.writeNRRD(headerfile.getName(), headerfile.getParentFile(), nrrdInfo);
						if (! nrrdInfo.isSeparateHeader()) {
							datafile.delete(); // don't need it anymore, was appended to header
						}
					}
					if(nrrdinfoV.size() > 0){
						NrrdInfo[] nrrdinfoArr = new NrrdInfo[nrrdinfoV.size()];
						nrrdinfoV.copyInto(nrrdinfoArr);
						return nrrdinfoArr;
					}
					return null;
				}
				default: {
					throw new DataAccessException("NRRD export from slice not yet supported");
				}
			}			
		}
		case NRRD_BY_VARIABLE : {
			switch (geometrySpecs.getModeID()) {
				case GEOMETRY_FULL: {
					Vector nrrdinfoV = new Vector();
					for (int i = 0; i < variableSpecs.getVariableNames().length; i++){
						// create the info object
						NrrdInfo nrrdInfo = NrrdInfo.createBasicNrrdInfo(
							4,
							new int[] {mesh.getSizeX(),	mesh.getSizeY(), mesh.getSizeZ(), timeSpecs.getAllTimes().length},
							"double",
							"raw"
						);
						nrrdinfoV.add(nrrdInfo);
						nrrdInfo.setContent("5D VCData from " + simID);
						nrrdInfo.setCenters(new String[] {"cell", "cell", "cell", "cell"});
						nrrdInfo.setSpacings(new double[] {
							mesh.getExtent().getX() / mesh.getSizeX(),
							mesh.getExtent().getY() / mesh.getSizeY(),
							mesh.getExtent().getZ() / mesh.getSizeZ(),
							Double.NaN,	// timepoints can have irregular intervals
						});
						nrrdInfo.setSeparateHeader(rasterSpecs.isSeparateHeader());
						// make datafile and update info
						String fileID = simID + "_Full_" + timeSpecs.getAllTimes().length + "times_" + variableSpecs.getVariableNames()[i] + "vars";
						File datafile = new File(tempDir, fileID + "_data.nrrd");
						nrrdInfo.setDatafile(datafile.getName());
						DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(datafile)));
						try {
							//for (int i = 0; i < variableSpecs.getVariableNames().length; i++){
								for (int j = 0; j < timeSpecs.getAllTimes().length; j++){
									double[] data = dataServerImpl.getSimDataBlock(user, vcdID, variableSpecs.getVariableNames()[i], timeSpecs.getAllTimes()[j]).getData();
									for (int k = 0; k < data.length; k++){
										out.writeDouble(data[k]);
									}
								}
							//}
						} catch (IOException exc) {
							throw new DataAccessException(exc.toString());
						} finally {
							out.close();
						}
						// write out final output
						File headerfile = new File(tempDir, fileID + ".nrrd");
						nrrdInfo = NrrdWriter.writeNRRD(headerfile.getName(), headerfile.getParentFile(), nrrdInfo);
						if (! nrrdInfo.isSeparateHeader()) {
							datafile.delete(); // don't need it anymore, was appended to header
						}
					}
					if(nrrdinfoV.size() > 0){
						NrrdInfo[] nrrdinfoArr = new NrrdInfo[nrrdinfoV.size()];
						nrrdinfoV.copyInto(nrrdinfoArr);
						return nrrdinfoArr;
					}
					return null;
				}
				default: {
					throw new DataAccessException("NRRD export from slice not yet supported");
				}
			}			
		}
		default: {
			throw new DataAccessException("Multiple NRRD file export not yet supported");
		}
	}											
}


/**
 * This method was created in VisualAge.
 */
public NrrdInfo[] makeRasterData(JobRequest jobRequest, User user, DataServerImpl dataServerImpl, ExportSpecs exportSpecs, String tempDir) 
						throws RemoteException, DataAccessException, IOException {
	return exportPDEData(
		jobRequest.getJobID(),
		user,
		dataServerImpl,
		exportSpecs.getVCDataIdentifier(),
		exportSpecs.getVariableSpecs(),
		exportSpecs.getTimeSpecs(),
		exportSpecs.getGeometrySpecs(),
		(RasterSpecs)exportSpecs.getFormatSpecificSpecs(),
		tempDir
	);
}
}