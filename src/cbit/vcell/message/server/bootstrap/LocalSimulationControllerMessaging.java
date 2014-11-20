/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.message.server.bootstrap;
import java.rmi.ConnectException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.vcell.util.DataAccessException;
import org.vcell.util.SessionLog;
import org.vcell.util.document.KeyValue;
import org.vcell.util.document.UserLoginInfo;
import java.rmi.server.UnicastRemoteObject;
import cbit.vcell.message.VCMessageSession;
import cbit.vcell.solver.VCSimulationIdentifier;

/**
 * Insert the type's description here.
 * Creation date: (2/4/2003 11:08:14 PM)
 * @author: Jim Schaff
 */
public class LocalSimulationControllerMessaging extends java.rmi.server.UnicastRemoteObject implements cbit.vcell.server.SimulationController {
	private org.vcell.util.SessionLog fieldSessionLog = null;
	private RpcSimServerProxy simServerProxy = null;
    private boolean bClosed = false;

/**
 * MessagingSimulationController constructor comment.
 * @exception java.rmi.RemoteException The exception description.
 */
public LocalSimulationControllerMessaging(UserLoginInfo userLoginInfo, VCMessageSession vcMessageSession, SessionLog log, int rmiPort) throws java.rmi.RemoteException {
	super(rmiPort);
	this.fieldSessionLog = log;
	simServerProxy = new RpcSimServerProxy(userLoginInfo, vcMessageSession, fieldSessionLog);
}


/**
 * This method was created by a SmartGuide.
 * @exception java.rmi.RemoteException The exception description.
 */
public void startSimulation(VCSimulationIdentifier vcSimID, int numSimulationScanJobs) throws RemoteException {
	fieldSessionLog.print("LocalSimulationControllerMessaging.startSimulation(" + vcSimID + ")");
	checkClosed();
	simServerProxy.startSimulation(vcSimID,numSimulationScanJobs);
}


/**
 * This method was created by a SmartGuide.
 * @exception java.rmi.RemoteException The exception description.
 */
public void stopSimulation(VCSimulationIdentifier vcSimID) throws RemoteException{
	fieldSessionLog.print("LocalSimulationControllerMessaging.stopSimulation(" + vcSimID + ")");
    checkClosed();
	simServerProxy.stopSimulation(vcSimID);
}

private void checkClosed() throws RemoteException {
	if (bClosed){
		fieldSessionLog.print("LocalSimulationControllerMessaging closed");
		throw new ConnectException("LocalSimulationControllerMessaging closed, please reconnect");
	}
}

public void close() {
	//try {
		bClosed = true;
	//	UnicastRemoteObject.unexportObject(this, true);
	//} catch (NoSuchObjectException e) {
	//	e.printStackTrace();
	//}
}
}
