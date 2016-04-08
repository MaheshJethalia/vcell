/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.bionetgen;
import java.io.Serializable;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.vcell.model.rbm.RbmUtils;
import org.vcell.util.Matchable;
import org.vcell.util.Pair;

import cbit.vcell.parser.Expression;
/**
 * Insert the type's description here.
 * Creation date: (1/13/2006 5:28:44 PM)
 * @author: Jim Schaff
 */
public abstract class BNGSpecies implements Matchable, Serializable {

	//
	// Storing the index of a species in the network file generated by BioNetGen. 
	// This index is required for filling out reactions listed in the network file.
	//
	private int networkFileIndex = 0;			
	private String name = null;
	private Expression concentration = null;

public BNGSpecies(String argName, Expression argConc, int argNetwkFileIndx) {
	super();
	name = argName;
	concentration = argConc;
	networkFileIndex = argNetwkFileIndx;
}

public boolean compareEqual(org.vcell.util.Matchable object) {
	if (this == object) {
		return (true);
	}
	if (object != null && object instanceof BNGSpecies) {
		BNGSpecies species = (BNGSpecies) object;
		//
		// check for true equality
		//
		if (!org.vcell.util.Compare.isEqual(getName(),species.getName())){
			return false;
		}
		if (!org.vcell.util.Compare.isEqualOrNull(getConcentration(),species.getConcentration())){
			return false;
		}
		return true;
	}
	return false;
}

public Expression getConcentration() {
	return concentration;
}
public String getName() {
	return name;			// may contain compartment information
}
public String extractName() {
	if(!(name.contains(":") && name.contains("@"))) {
		return name;
	} else {
		String shortName = name.substring(name.indexOf(":")+1);
		return shortName;
	}
}
public boolean hasCompartment() {
	if(!(name.contains(":") && name.contains("@"))) {
		return false;
	}
	return true;
}
public java.lang.String extractCompartment() {
	if(!(name.contains(":") && name.contains("@"))) {
		return null;
	} else {
		String compartmentName = name.substring(1, name.indexOf(":"));
		return compartmentName;
	}
}

public int getNetworkFileIndex() {
	return networkFileIndex;
}

public abstract boolean isWellDefined();
public abstract BNGSpecies[] parseBNGSpeciesName();

public void setConcentration(Expression newConc) {
	concentration = newConc;
}
public void setName(java.lang.String newName) {
	name = newName;
}

public String toBnglString() {
	return new String(getNetworkFileIndex() + " " + getName() + " " + getConcentration().infix());
}
public String toString() {
	return new String(getNetworkFileIndex() + ";\t" + getName() + ";\t" + getConcentration().infix());
}
public String toStringMedium() {
	return new String(extractName() + " " + getConcentration().infix());
}
public String toStringShort() {
	return new String(getName());
}

public static Pair<List<BNGSpecies>, List<BNGSpecies>> diff(List<BNGSpecies> older, BNGSpecies[] newer) {
	List<BNGSpecies> removed = new ArrayList<>();
	List<BNGSpecies> added = new ArrayList<>();
	
	// whatever is present in 'older' list and is missing in 'newer' list - means that it was removed
	boolean found;
	for(BNGSpecies o : older) {
		found = false;
		for(BNGSpecies n : newer) {
			if(o.getNetworkFileIndex() == n.getNetworkFileIndex()) {
				found = true;
				break;
			}
		}
		if(!found) {
//			System.out.println(o);
//			System.out.println(n);
			removed.add(o);
		}
	}
	// whatever is present in 'newer' list and is missing in 'older' list - means that it was added
	for(BNGSpecies n : newer) {
		found = false;
		for(BNGSpecies o : older) {
			if(n.getNetworkFileIndex() == o.getNetworkFileIndex()) {
				found = true;
				break;
			}
		}
		if(!found) {
			added.add(n);
		}
	}
	Pair<List<BNGSpecies>, List<BNGSpecies>> p = new Pair<>(removed, added);
	return p;
}


//@Override
//public boolean equals(Object thatObject) {
//	if(!(thatObject instanceof BNGSpecies)) {
//		return false;
//	}
//	BNGSpecies that = (BNGSpecies)thatObject;
//
//	
//	
//	return true;
//}

//private List<String> extractComponentPatterns(String mtp) {
//	String input = mtp.substring(mtp.indexOf("(")+1);
//	input = input.substring(0, input.indexOf(")"));
//	System.out.println(input);
//	
//	List<String> mcpList = new ArrayList<> ();
//	String delimiters = "'";
//	StringTokenizer tokenizer = new StringTokenizer(input, delimiters);
//	String token = new String("");
//
//	while (tokenizer.hasMoreTokens()) {
//		token = tokenizer.nextToken();
//		token = token.trim();
//		mcpList.add(token);
//	}
//	Collections.sort(mcpList);
//	return mcpList;
//}
}
