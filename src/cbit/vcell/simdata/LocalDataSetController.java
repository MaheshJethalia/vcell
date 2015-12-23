/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.simdata;
import java.rmi.RemoteException;

import org.vcell.util.DataAccessException;
import org.vcell.util.SessionLog;
import org.vcell.util.document.TimeSeriesJobSpec;
import org.vcell.util.document.User;
import org.vcell.util.document.VCDataIdentifier;
import org.vcell.vis.io.VtuFileContainer;
import org.vcell.vis.io.VtuVarInfo;

import cbit.rmi.event.ExportEvent;
import cbit.vcell.export.server.ExportServiceImpl;
import cbit.vcell.field.io.FieldDataFileOperationResults;
import cbit.vcell.field.io.FieldDataFileOperationSpec;
import cbit.vcell.server.DataSetController;
import cbit.vcell.server.LocalVCellConnection;
import cbit.vcell.solver.AnnotatedFunction;
import cbit.vcell.solvers.CartesianMesh;
/**
 * This interface was generated by a SmartGuide.
 * 
 */
public class LocalDataSetController implements DataSetController {
	private LocalVCellConnection vcConn = null;
	private SessionLog log = null;
	private User user = null;
	private DataServerImpl dataServerImpl = null;

/**
 * This method was created by a SmartGuide.
 */
public LocalDataSetController (LocalVCellConnection argvcConn, SessionLog log, DataSetControllerImpl dsControllerImpl, ExportServiceImpl exportServiceImpl, User user) {
	this.vcConn = argvcConn;
	this.user = user;
	this.log = log;
	dataServerImpl = new DataServerImpl(log, dsControllerImpl, exportServiceImpl);	
}


public FieldDataFileOperationResults fieldDataFileOperation(FieldDataFileOperationSpec fieldDataFileOperationSpec) throws DataAccessException {
	return dataServerImpl.fieldDataFileOperation(user,fieldDataFileOperationSpec);
}


/**
 * This method was created by a SmartGuide.
 * @return java.lang.String[]
 */
public DataIdentifier[] getDataIdentifiers(OutputContext outputContext, VCDataIdentifier vcdID) throws DataAccessException {
	return dataServerImpl.getDataIdentifiers(outputContext, user, vcdID);
}


/**
 * This method was created by a SmartGuide.
 * @return double[]
 */
public double[] getDataSetTimes(VCDataIdentifier vcdID) throws DataAccessException {
	return dataServerImpl.getDataSetTimes(user, vcdID);
}


/**
 * Insert the method's description here.
 * Creation date: (10/11/00 1:11:04 PM)
 * @param function cbit.vcell.math.Function
 * @exception org.vcell.util.DataAccessException The exception description.
 * @exception java.rmi.RemoteException The exception description.
 */
public AnnotatedFunction[] getFunctions(OutputContext outputContext,VCDataIdentifier vcdID) throws org.vcell.util.DataAccessException {
	return dataServerImpl.getFunctions(outputContext,user, vcdID);
}


/**
 * This method was created by a SmartGuide.
 * @return cbit.plot.PlotData
 * @param variable java.lang.String
 * @param time double
 * @param spatialSelection cbit.vcell.simdata.gui.SpatialSelection
 * @exception java.rmi.RemoteException The exception description.
 */
public cbit.plot.PlotData getLineScan(OutputContext outputContext, VCDataIdentifier vcdID, String varName, double time, SpatialSelection spatialSelection) throws DataAccessException {
	return dataServerImpl.getLineScan(outputContext, user, vcdID,varName,time,spatialSelection);
}


/**
 * This method was created in VisualAge.
 * @return CartesianMesh
 */
public CartesianMesh getMesh(VCDataIdentifier vcdID) throws DataAccessException {
	return dataServerImpl.getMesh(user, vcdID);
}


/**
 * Insert the method's description here.
 * Creation date: (1/14/00 11:20:51 AM)
 * @return cbit.vcell.export.data.ODESimData
 * @exception org.vcell.util.DataAccessException The exception description.
 * @exception java.rmi.RemoteException The exception description.
 */
public cbit.vcell.solver.ode.ODESimData getODEData(VCDataIdentifier vcdID) throws DataAccessException {
	return dataServerImpl.getODEData(user, vcdID);
}


/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.server.DataSet
 * @param time double
 */
public ParticleDataBlock getParticleDataBlock(VCDataIdentifier vcdID, double time) throws DataAccessException {
	return dataServerImpl.getParticleDataBlock(user, vcdID, time);
}


/**
 * This method was created in VisualAge.
 * @return boolean
 */
public DataOperationResults doDataOperation(DataOperation dataOperation) throws DataAccessException {
	return dataServerImpl.doDataOperation(user, dataOperation);
}

/**
 * This method was created in VisualAge.
 * @return boolean
 */
public boolean getParticleDataExists(VCDataIdentifier vcdID) throws DataAccessException {
	return dataServerImpl.getParticleDataExists(user, vcdID);
}


/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.server.DataSet
 * @param time double
 */
public SimDataBlock getSimDataBlock(OutputContext outputContext, VCDataIdentifier vcdID, String var, double time) throws DataAccessException {
	return dataServerImpl.getSimDataBlock(outputContext, user, vcdID,var,time);
}


/**
 * This method was created by a SmartGuide.
 * @return double[]
 * @param varName java.lang.String
 * @param x int
 * @param y int
 * @param z int
 */
public org.vcell.util.document.TimeSeriesJobResults getTimeSeriesValues(OutputContext outputContext, VCDataIdentifier vcdID, TimeSeriesJobSpec timeSeriesJobSpec) throws DataAccessException {
	return dataServerImpl.getTimeSeriesValues(outputContext, user,vcdID,timeSeriesJobSpec);
}


/**
 * Insert the method's description here.
 * Creation date: (3/30/2001 11:11:52 AM)
 * @param exportSpecs cbit.vcell.export.server.ExportSpecs
 * @exception org.vcell.util.DataAccessException The exception description.
 */
public ExportEvent makeRemoteFile(OutputContext outputContext,cbit.vcell.export.server.ExportSpecs exportSpecs) throws DataAccessException {
	return dataServerImpl.makeRemoteFile(outputContext,user, exportSpecs);

	/*
	log.print("LocalDataSetController.makeRemoteFile(" + exportSpecs.getVCDataIdentifier() + ")");
	try {
		ExportEvent exportEvent = exportServiceImpl.makeRemoteFile(getUser(), this, exportSpecs);
		if (exportEvent != null && exportEvent.getEventTypeID() == MessageEvent.EXPORT_COMPLETE) {
			// updates database with export metadata
			if (exportSpecs.getVCDataIdentifier() instanceof SimulationInfo){
				vcConn.getResultSetCrawler().updateExportData(user, (SimulationInfo)(exportSpecs.getVCDataIdentifier()), exportEvent);
			}else{
				log.alert("LocalDataSetController.makeRemoteFile(), invoked for data '"+exportSpecs.getVCDataIdentifier()+"' not indexed using ResultSetCrawler");
			}
		}
		return exportEvent;
	} catch (Throwable e) {
		log.exception(e);
		throw new DataAccessException(e.getMessage());
	}*/
}


@Override
public DataSetMetadata getDataSetMetadata(VCDataIdentifier vcdataID) throws DataAccessException {
	return dataServerImpl.getDataSetMetadata(user, vcdataID);
}


@Override
public DataSetTimeSeries getDataSetTimeSeries(VCDataIdentifier vcdataID, String[] variableNames) throws DataAccessException {
	return dataServerImpl.getDataSetTimeSeries(user, vcdataID, variableNames);
}


@Override
public VtuFileContainer getEmptyVtuMeshFiles(VCDataIdentifier vcdataID) throws DataAccessException {
	return dataServerImpl.getEmptyVtuMeshFiles(user, vcdataID);
}

@Override
public double[] getVtuMeshData(OutputContext outputContext, VCDataIdentifier vcdataID, VtuVarInfo var, double time) throws DataAccessException {
	return dataServerImpl.getVtuMeshData(user, outputContext, vcdataID, var, time);
}


@Override
public VtuVarInfo[] getVtuVarInfos(OutputContext outputContext, VCDataIdentifier vcdataID) throws DataAccessException {
	return dataServerImpl.getVtuVarInfos(user, outputContext, vcdataID);
}

}
