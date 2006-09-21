package cbit.vcell.solvers;
/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import cbit.gui.PropertyLoader;
import cbit.util.*;
import java.util.*;

import cbit.vcell.simulation.*;
import cbit.vcell.math.*;

/**
 * This class was generated by a SmartGuide.
 * 
 */
public class CppClassCoderSimulation extends CppClassCoder {
	private SimulationJob simulationJob = null;
	private String baseDataName = null;

/**
 * VarContextCppCoder constructor comment.
 * @param name java.lang.String
 */
protected CppClassCoderSimulation(CppCoderVCell cppCoderVCell, SimulationJob argSimulationJob, String baseDataName) 
{
	super(cppCoderVCell,"UserSimulation", "Simulation");
	this.simulationJob = argSimulationJob;
	this.baseDataName = baseDataName;
}


/**
 * This method was created by a SmartGuide.
 * @param out java.io.PrintWriter
 */
protected void writeConstructor(java.io.PrintWriter out) throws Exception {
	out.println(getClassName()+"::"+getClassName()+"(CartesianMesh *mesh)");
	out.println(": Simulation(mesh)");
	out.println("{");
	out.println("VolumeRegionVariable		*volumeRegionVar;");
	out.println("MembraneRegionVariable		*membraneRegionVar;");
	out.println("VolumeVariable    *volumeVar;");
	out.println("MembraneVariable  *membraneVar;");
	out.println("ContourVariable   *contourVar;");
	out.println("// ImplicitPDESolver *pdeSolver;");
	out.println("PdeSolverDiana    *pdeSolver;");
	out.println("ODESolver         *odeSolver;");
	out.println("SparseLinearSolver    *slSolver;");
	out.println("EqnBuilder        *builder;");
	out.println("SparseMatrixEqnBuilder        *smbuilder;");
	out.println("long sizeX = mesh->getNumVolumeX();");
	out.println("long sizeY = mesh->getNumVolumeY();");
	out.println("long sizeZ = mesh->getNumVolumeZ();");
	out.println("int numSolveRegions;");
	out.println("int *solveRegions;");
	out.println("int numVolumeRegions = mesh->getNumVolumeRegions();");
	out.println("int i;");
	out.println("int regionCount;");
	out.println("");	

	out.println("\tint symmflg = 1;    // define symmflg = 0 (general) or 1 (symmetric)");

	Simulation simulation = simulationJob.getWorkingSim();
	Variable variables[] = simulation.getVariables();
	for (int i=0;i<variables.length;i++){
	  	Variable var = (Variable)variables[i];
	  	String units;
	  	if (var instanceof VolVariable){
	  		units = "uM";
	  		VolVariable volVar = (VolVariable)var;
	  		out.println("   volumeVar = new VolumeVariable(sizeX,sizeY,sizeZ,\""+volVar.getName()+"\",\""+units+"\");");
	  		
	  		//
	  		// need to specify which SubDomains should be solved for
	  		//
	  		Vector listOfSubDomains = new Vector();
	  		int totalNumCompartments = 0;
	  		StringBuffer compartmentNames = new StringBuffer();
	  		Enumeration subDomainEnum = simulation.getMathDescription().getSubDomains();
	  		while (subDomainEnum.hasMoreElements()){
		  		SubDomain subDomain = (SubDomain)subDomainEnum.nextElement();
		  		if (subDomain instanceof CompartmentSubDomain){
			  		CompartmentSubDomain compartmentSubDomain = (CompartmentSubDomain)subDomain;
			  		totalNumCompartments++;
			  		if (subDomain.getEquation(var) != null){
				  		listOfSubDomains.add(compartmentSubDomain);
				  		int handle = simulation.getMathDescription().getHandle(compartmentSubDomain);
				  		compartmentNames.append(compartmentSubDomain.getName()+"("+handle+") ");
			  		}
		  		}
	  		}
	  		if (totalNumCompartments == listOfSubDomains.size()){
		  		//
		  		// every compartments has an equation, set numSolveRegions accordingly
		  		//
		  		out.println("    // solving for all regions");
		  		out.println("    numSolveRegions = 0;  // flag specifying to solve for all regions");
		  		out.println("    solveRegions = NULL;");
	  		}else{
		  		//
		  		// only solve for some compartments
		  		//
			  	out.println("   // solving for only regions belonging to ("+compartmentNames.toString()+"), first 'numSolveRegions' elements used");
			  	out.println("   solveRegions = new int[numVolumeRegions];");
		  		
		  		//
		  		//  build list of regions belonging to the required SubDomains
		  		//
				out.println("   regionCount = 0;");
		  		out.println("   for (i = 0; i < numVolumeRegions; i++){");
		  		out.println("      VolumeRegion *volRegion = mesh->getVolumeRegion(i);");
			  	for (int j = 0; j < listOfSubDomains.size(); j++){
					CompartmentSubDomain compartmentSubDomain = (CompartmentSubDomain)listOfSubDomains.elementAt(j);
				  	int handle = simulation.getMathDescription().getHandle(compartmentSubDomain);
					out.println("      if (volRegion->getFeature()->getHandle() == (FeatureHandle)(0xff & "+handle+")){  // test if this region is same as '"+compartmentSubDomain.getName()+"'");
					out.println("          solveRegions[regionCount++] = volRegion->getId();");
					out.println("      }");
				}
				out.println("   }");
		  		out.println("   numSolveRegions = regionCount;");
	  		}
	  		
	  		if (simulation.getMathDescription().isPDE(volVar)){
		  		if (simulation.getMathDescription().hasVelocity(volVar)) { // Convection
		  			out.println("   // pdeSolver = new ImplicitPDESolver(volumeVar,mesh,"+simulation.hasTimeVaryingDiffusionOrAdvection(volVar)+");");
		  			out.println("   // pdeSolver = new PdeSolverDiana(volumeVar,mesh"+simulation.hasTimeVaryingDiffusionOrAdvection(volVar)+");");
		  			out.println("   // pdeSolver = new PdeSolverDiana(volumeVar,mesh,numSolveRegions,solveRegions,"+simulation.hasTimeVaryingDiffusionOrAdvection(volVar)+");");
		  			out.println("\tsymmflg = 0;    // define symmflg = 0 (general) or 1 (symmetric)");
		  			out.println("\tpdeSolver = new PdeSolverDiana(volumeVar,mesh,symmflg,numSolveRegions,solveRegions,"+simulation.hasTimeVaryingDiffusionOrAdvection(volVar)+");");
		  			out.println("\tbuilder = new EqnBuilderReactionDiffusionConvection(volumeVar,mesh,pdeSolver);");
		  			out.println("\tpdeSolver->setEqnBuilder(builder);");
		  			out.println("\taddSolver(pdeSolver);");
	  			} else {
		  			out.println("   // pdeSolver = new ImplicitPDESolver(volumeVar,mesh,"+simulation.hasTimeVaryingDiffusionOrAdvection(volVar)+");");
		  			out.println("   // pdeSolver = new PdeSolverDiana(volumeVar,mesh"+simulation.hasTimeVaryingDiffusionOrAdvection(volVar)+");");
		  			out.println("   // pdeSolver = new PdeSolverDiana(volumeVar,mesh,numSolveRegions,solveRegions,"+simulation.hasTimeVaryingDiffusionOrAdvection(volVar)+");");
		  			out.println("\tsymmflg = 1;    // define symmflg = 0 (general) or 1 (symmetric)");
		  			out.println("\tpdeSolver = new PdeSolverDiana(volumeVar,mesh,symmflg,numSolveRegions,solveRegions,"+simulation.hasTimeVaryingDiffusionOrAdvection(volVar)+");");
		  			out.println("\tbuilder = new EqnBuilderReactionDiffusion(volumeVar,mesh,pdeSolver);");	  			
		  			out.println("\tpdeSolver->setEqnBuilder(builder);");
		  			out.println("\taddSolver(pdeSolver);");
	  			}
	  		}else{
	  			out.println("   //odeSolver = new ODESolver(volumeVar,mesh);");
	  			out.println("   odeSolver = new ODESolver(volumeVar,mesh,numSolveRegions,solveRegions);");
	  			out.println("   builder = new EqnBuilderReactionForward(volumeVar,mesh,odeSolver);");
	  			out.println("   odeSolver->setEqnBuilder(builder);");
	  			out.println("   addSolver(odeSolver);");
	  		}		
	  		out.println("   addVariable(volumeVar);");
	  		out.println("");
	  	}else if (var instanceof MemVariable) { // membraneVariable
		  	units = "molecules/squm";
	  		MemVariable memVar = (MemVariable)var;
		  	if (simulation.getMathDescription().isPDE(memVar)) {
		  		out.println("\tmembraneVar = new MembraneVariable(mesh->getNumMembraneElements(),\""+memVar.getName()+"\",\""+units+"\");");
		  		out.println("\tsmbuilder = new MembraneEqnBuilderDiffusion(membraneVar,mesh);");
	  			out.println("\tslSolver = new SparseLinearSolver(membraneVar,smbuilder);");	  			
	  			out.println("\taddSolver(slSolver);");
		  		out.println("\taddVariable(membraneVar);");
		  	} else {		  		
		  		out.println("   // solving for all regions");
		  		out.println("   numSolveRegions = 0;  // flag specifying to solve for all regions");
		  		out.println("   solveRegions = NULL;");
		  		out.println("   membraneVar = new MembraneVariable(mesh->getNumMembraneElements(),\""+memVar.getName()+"\",\""+units+"\");");
	  			out.println("   odeSolver = new ODESolver(membraneVar,mesh,numSolveRegions,solveRegions);");
	  			out.println("   builder = new MembraneEqnBuilderForward(membraneVar,mesh,odeSolver);");
	  			out.println("   odeSolver->setEqnBuilder(builder);");
	  			out.println("   addSolver(odeSolver);");
		  		out.println("   addVariable(membraneVar);");
		  	}
	  	}else if (var instanceof FilamentVariable) { // contourVariable
	  		units = "molecules/um";
	  		FilamentVariable filamentVar = (FilamentVariable)var;
	  		out.println("   // solving for all regions");
	  		out.println("   numSolveRegions = 0;  // flag specifying to solve for all regions");
	  		out.println("   solveRegions = NULL;");
	  		out.println("   contourVar = new ContourVariable(mesh->getNumMembraneElements(),\""+filamentVar.getName()+"\",\""+units+"\");");
  			out.println("   odeSolver = new ODESolver(contourVar,mesh,numSolveRegions,solveRegions);");
  			out.println("   builder = new ContourEqnBuilderForward(contourVar,mesh,odeSolver);");
  			out.println("   odeSolver->setEqnBuilder(builder);");
  			out.println("   addSolver(odeSolver);");
	  		out.println("   addVariable(contourVar);");
	  	}else if (var instanceof VolumeRegionVariable) { // volumeRegionVariable
	  		units = "uM";
	  		VolumeRegionVariable volumeRegionVar = (VolumeRegionVariable)var;
	  		out.println("   // solving for all regions");
	  		out.println("   numSolveRegions = 0;  // flag specifying to solve for all regions");
	  		out.println("   solveRegions = NULL;");
	  		out.println("   volumeRegionVar = new VolumeRegionVariable(mesh->getNumVolumeRegions(),\""+volumeRegionVar.getName()+"\",\""+units+"\");");
  			out.println("   odeSolver = new ODESolver(volumeRegionVar,mesh,numSolveRegions,solveRegions);");
  			out.println("   builder = new VolumeRegionEqnBuilder(volumeRegionVar,mesh,odeSolver);");
  			out.println("   odeSolver->setEqnBuilder(builder);");
  			out.println("   addSolver(odeSolver);");
	  		out.println("   addVariable(volumeRegionVar);");
	  	}else if (var instanceof MembraneRegionVariable) { // membraneRegionVariable
	  		units = "molecules/um^2";
	  		MembraneRegionVariable membraneRegionVar = (MembraneRegionVariable)var;
	  		out.println("   // solving for all regions");
	  		out.println("   numSolveRegions = 0;  // flag specifying to solve for all regions");
	  		out.println("   solveRegions = NULL;");
	  		out.println("   membraneRegionVar = new MembraneRegionVariable(mesh->getNumMembraneRegions(),\""+membraneRegionVar.getName()+"\",\""+units+"\");");
  			out.println("   odeSolver = new ODESolver(membraneRegionVar,mesh,numSolveRegions,solveRegions);");
  			out.println("   builder = new MembraneRegionEqnBuilder(membraneRegionVar,mesh,odeSolver);");
  			out.println("   odeSolver->setEqnBuilder(builder);");
  			out.println("   addSolver(odeSolver);");
	  		out.println("   addVariable(membraneRegionVar);");
	  	}	
	}		  	
	out.println("}");
}


/**
 * This method was created by a SmartGuide.
 * @param printWriter java.io.PrintWriter
 */
public void writeDeclaration(java.io.PrintWriter out) {
	out.println("//---------------------------------------------");
	out.println("//  class " + getClassName());
	out.println("//---------------------------------------------");

	out.println("class " + getClassName() + " : public " + getParentClassName());
	out.println("{");
	out.println(" public:");
	out.println("   "+getClassName() + "(CartesianMesh *mesh);");
	out.println("};");
}


/**
 * This method was created by a SmartGuide.
 * @param out java.io.PrintWriter
 */
protected void writeGetSimTool(java.io.PrintWriter out) throws Exception {

	Simulation simulation = simulationJob.getWorkingSim();
	SolverTaskDescription taskDesc = simulation.getSolverTaskDescription();
	if (taskDesc==null){
		throw new Exception("task description not defined");
	}	

	out.println("");
	out.println("SimTool *getSimTool()");
	out.println("{");
	out.println("");
	ISize meshSampling = simulation.getMeshSpecification().getSamplingSize();
//	char fs = File.separatorChar;
//	String baseDataName = "SIMULATION" + fs + mathDesc.getName() + fs + "UserData" ;
	StringBuffer newBaseDataName = new StringBuffer();
	for (int i=0;i<baseDataName.length();i++){
		if (baseDataName.charAt(i) == '\\'){
			newBaseDataName.append(baseDataName.charAt(i));
			newBaseDataName.append(baseDataName.charAt(i));
		}else{
			newBaseDataName.append(baseDataName.charAt(i));
		}
	}
	out.println("   Simulation *sim = NULL;");
	out.println("   VCellModel *model = NULL;");
	out.println("   CartesianMesh *mesh = NULL;");
	out.println("   SimTool *pSimTool = new SimTool(\"Simulate\");");
	out.println("   int numX = "+meshSampling.getX()+";");
	out.println("   int numY = "+meshSampling.getY()+";");
	out.println("   int numZ = "+meshSampling.getZ()+";");
	out.println("   theApplication = new App();");
	out.println("   try {");
	out.println("      model = new UserVCellModel();");
	out.println("      assert(model);");
	out.println("      theApplication->setModel(model);");
	out.println("      SimulationMessaging::getInstVar()->setWorkerEvent(new WorkerEvent(JOB_STARTING, \"initializing mesh...\"));");
	out.println("      mesh = new CartesianMesh(\"" + newBaseDataName + ".vcg" + "\");");
	out.println("      SimulationMessaging::getInstVar()->setWorkerEvent(new WorkerEvent(JOB_STARTING, \"mesh initialized\"));");
	out.println("      assert(mesh);");
	out.println("      sim = new UserSimulation(mesh);");
	out.println("      assert(sim);");
	out.println("      theApplication->setSimulation(sim);");
	out.println();
	out.println("      sim->initSimulation();");
	out.println("      pSimTool->setup();");
	out.println("      pSimTool->setBaseFilename(\""+newBaseDataName.toString()+"\");");
	out.println("	   pSimTool->loadFinal();   // initializes to the latest file if it exists");
	out.println("");
	out.println("      pSimTool->setPeriodSec("+taskDesc.getTimeStep().getDefaultTimeStep()+");");
	out.println("      pSimTool->setEndTimeSec("+taskDesc.getTimeBounds().getEndingTime()+");");

	if (taskDesc.getOutputTimeSpec().isDefault()){
		out.println("      pSimTool->setStoreMultiple("+((DefaultOutputTimeSpec)taskDesc.getOutputTimeSpec()).getKeepEvery()+");");
	}else{
		throw new RuntimeException("unexpected OutputTime specification type :"+taskDesc.getOutputTimeSpec().getClass().getName());
	}
	out.println("      pSimTool->setStoreEnable(TRUE);");
	out.println("      pSimTool->setFileCompress(FALSE);");
	
	out.println("");
	out.println("   }catch (char *exStr){");
	out.println("      char* title = \"Exception in initialization: \";");
	out.println("      char* msg = new char[strlen(title) + strlen(exStr) + 1];");
	out.println("      strcpy(msg, title);");
	out.println("      strcat(msg, exStr);");
	out.println("      throw msg;");
	out.println("   }catch (...){");
	out.println("      throw \"Unknown Exception in initialization \";");
	out.println("   }");
	out.println("");
	out.println("   return pSimTool;");
	out.println("");
	out.println("}");
}


/**
 * This method was created by a SmartGuide.
 * @param printWriter java.io.PrintWriter
 */
public void writeImplementation(java.io.PrintWriter out) throws Exception {
	out.println("//---------------------------------------------");
	out.println("//  main routine");
	out.println("//---------------------------------------------");
	writeMain(out);
	out.println("");
	writeGetSimTool(out);
	out.println("");
	out.println("//---------------------------------------------");
	out.println("//  class " + getClassName());
	out.println("//---------------------------------------------");
	writeConstructor(out);
	out.println("");
}


/**
 * This method was created by a SmartGuide.
 * @param out java.io.PrintWriter
 */
protected void writeMain(java.io.PrintWriter out) throws Exception {

	Simulation simulation = simulationJob.getWorkingSim();
	SolverTaskDescription taskDesc = simulation.getSolverTaskDescription();
	if (taskDesc==null){
		throw new Exception("task description not defined");
	}	

	out.println("#ifndef VCELL_CORBA");
	out.println("//-------------------------------------------");
	out.println("//   BATCH (NON-CORBA) IMPLEMENTATION");
	out.println("//-------------------------------------------");
	out.println("");
	out.println("#ifdef VCELL_MPI");
	out.println("#include <mpi.h>");
	out.println("#endif");
	out.println("");

	out.println("void main(int argc, char *argv[])");
	out.println("{");
		
	out.println("");
	out.println("#ifdef VCELL_MPI");
	out.println("\tint ierr = MPI_Init(&argc,&argv);");
	out.println("\tassert(ierr == MPI_SUCCESS);");
	out.println("#endif");
	out.println("");
	out.println("");

	// Fei Changes Begin
	out.println("\ttry {");
	out.println("\t\tif (argc == 1) { // no messaging");
	out.println("\t\t\tSimulationMessaging::create();");
	out.println("\t\t} else {");
	out.println("\t\t\tchar* broker = \"" + PropertyLoader.getRequiredProperty(PropertyLoader.jmsURL) + "\";");
    out.println("\t\t\tchar *smqusername = \"" + PropertyLoader.getRequiredProperty(PropertyLoader.jmsUser) + "\";");
    out.println("\t\t\tchar *password = \"" + PropertyLoader.getRequiredProperty(PropertyLoader.jmsPassword) + "\";");
    out.println("\t\t\tchar *qname = \"" + PropertyLoader.getRequiredProperty(PropertyLoader.jmsWorkerEventQueue) + "\";");  
	out.println("\t\t\tchar* tname = \"" + PropertyLoader.getRequiredProperty(PropertyLoader.jmsServiceControlTopic) + "\";");
	out.println("\t\t\tchar* vcusername = \"" + simulation.getVersion().getOwner().getName() + "\";");
	out.println("\t\t\tjint simKey = " + simulation.getVersion().getVersionKey() + ";");
	out.println("\t\t\tjint jobIndex = " + simulationJob.getJobIndex() + ";");
	out.println("\t\t\tjint taskID = atoi(argv[1]);");
	out.println("\t\t\tSimulationMessaging::create(broker, smqusername, password, qname, tname, vcusername, simKey, jobIndex, taskID);");
	out.println("\t\t}");
	out.println("\t\tSimulationMessaging::getInstVar()->start(); // start the thread");

	out.println("\t\tSimTool *pSimTool = getSimTool();");
	if (taskDesc.getTaskType() == SolverTaskDescription.TASK_UNSTEADY){
		out.println("\t\tpSimTool->start();");
	}else{
		out.println("\t\tpSimTool->startSteady("+taskDesc.getErrorTolerance().getAbsoluteErrorTolerance()+","+taskDesc.getTimeBounds().getEndingTime()+");");
	}		

	out.println("\t\t\t//cerr << \"Simulation Complete in Main() ... \" << endl;");
	out.println("\t\tif (!SimTool::bStopSimulation) {");	
	out.println("\t\t\tSimulationMessaging::getInstVar()->waitUntilFinished();");
	out.println("\t\t}");
	out.println("\t}catch (char *exStr){");
	out.println("\t\t//cerr<< \"Exception while running ... \" << exStr << endl;");
	out.println("\t\tchar msg1[80];");
	out.println("\t\tstrcpy(msg1, \"Exception while running ... \");");
	out.println("\t\tchar* msg = new char[strlen(msg1) + strlen(exStr) + 1];");
	out.println("\t\tstrcpy(msg, msg1);");
	out.println("\t\tstrcat(msg, exStr);");
	out.println("\t\tif (!SimTool::bStopSimulation) {");
	out.println("\t\t\tSimulationMessaging::getInstVar()->setWorkerEvent(new WorkerEvent(JOB_FAILURE, msg));");
	out.println("\t\t\tSimulationMessaging::getInstVar()->waitUntilFinished();");
	out.println("\t\t}");
	out.println("\t\tdelete SimulationMessaging::getInstVar();");
    out.println("\t\texit(-1);");
   	out.println("\t}catch (...){");
    out.println("\t\t//cerr << \"Unknown Exception while running ... \" << endl;");
    out.println("\t\tif (!SimTool::bStopSimulation) {");   	
 	out.println("\t\t\tSimulationMessaging::getInstVar()->setWorkerEvent(new WorkerEvent(JOB_FAILURE, \"Unknown Exception while running ... \"));");
	out.println("\t\t\tSimulationMessaging::getInstVar()->waitUntilFinished();");
	out.println("\t\t}");
	out.println("\t\tdelete SimulationMessaging::getInstVar();");
    out.println("\t\texit(-1);");
	out.println("\t}");

	out.println("#ifdef VCELL_MPI");
	out.println("\tMPI_Finalize();");
	out.println("#endif");

	out.println("\tdelete SimulationMessaging::getInstVar();");
	out.println("\texit(0);");
	out.println("}");
   	
	//out.println("   try {");
	//out.println("          SimTool *pSimTool = getSimTool();");
	//if (taskDesc.getTaskType() == SolverTaskDescription.TASK_UNSTEADY){
		//out.println("      pSimTool->start();");
	//}else{
		//out.println("      pSimTool->startSteady("+taskDesc.getErrorTolerance().getAbsoluteErrorTolerance()+","+taskDesc.getTimeBounds().getEndingTime()+");");
	//}		
	//out.println("      cerr << \"Simulation Complete in Main() ... \" << endl;");
	//out.println("   }catch (char *exStr){");
	//out.println("      cerr << \"Exception while running ... \" << exStr << endl;");
	//out.println("      exit(-1);");
	//out.println("   }catch (...){");
	//out.println("      cerr << \"Unknown Exception while running ... \" << endl;");
	//out.println("      exit(-1);");
	//out.println("   }");
	//out.println("");
	
	//out.println("#ifdef VCELL_MPI");
	//out.println("   MPI_Finalize();");
	//out.println("#endif");
	
	//out.println("   exit(0);");
	//out.println("}");
	
	// Fei Changes End
	
	out.println("#else  // end not VCELL_CORBA");
	out.println("//-------------------------------------------");
	out.println("//   CORBA IMPLEMENTATION");
	out.println("//-------------------------------------------");
	out.println("#include <OB/CORBA.h>");
	out.println("#include <OB/Util.h>");
	out.println("");
	out.println("#include <Simulation_impl.h>");
	out.println("");
	out.println("#include <stdlib.h>");
	out.println("#include <errno.h>");
	out.println("");
	out.println("#ifdef HAVE_FSTREAM");
	out.println("#   include <fstream>");
	out.println("#else");
	out.println("#   include <fstream.h>");
	out.println("#endif");
	out.println("");
	out.println("int main(int argc, char* argv[], char*[])");
	out.println("{");
	out.println("    try");
	out.println("    {");
	out.println("	//");
	out.println("	// Create ORB and BOA");
	out.println("	//");
	out.println("	CORBA_ORB_var orb = CORBA_ORB_init(argc, argv);");
	out.println("	CORBA_BOA_var boa = orb -> BOA_init(argc, argv);");
	out.println("	");
	out.println("	orb->conc_model(CORBA_ORB::ConcModelThreaded);");
	out.println("	boa->conc_model(CORBA_BOA::ConcModelThreadPool);");
	out.println("	boa->conc_model_thread_pool(4);");
	out.println("	");
	out.println("	//");
	out.println("	// Create implementation object");
	out.println("	//");
	out.println("	mathService_Simulation_var p = new Simulation_impl(getSimTool());");
	out.println("	");
	out.println("	//");
	out.println("	// Save reference");
	out.println("	//");
	out.println("	CORBA_String_var s = orb -> object_to_string(p);");
	out.println("	");
	out.println("	const char* refFile = \"Simulation.ref\";");
	out.println("	ofstream out(refFile);");
	out.println("	if(out.fail())");
	out.println("	{");
	out.println("	    cerr << argv[0] << \": can't open `\" << refFile << \"': \"");
	out.println("		 << strerror(errno) << endl;");
	out.println("	    return 1;");
	out.println("	}");
	out.println("	");
	out.println("	out << s << endl;");
	out.println("	out.close();");
	out.println("	");
	out.println("	//");
	out.println("	// Run implementation");
	out.println("	//");
	out.println("	boa -> impl_is_ready(CORBA_ImplementationDef::_nil());");
	out.println("    }");
	out.println("    catch(CORBA_SystemException& ex)");
	out.println("    {");
	out.println("	OBPrintException(ex);");
	out.println("	return 1;");
	out.println("    }");
	out.println("");
	out.println("    return 0;");
	out.println("}");
	out.println("");
	out.println("#endif // end VCELL_CORBA");
	out.println("");
}
}