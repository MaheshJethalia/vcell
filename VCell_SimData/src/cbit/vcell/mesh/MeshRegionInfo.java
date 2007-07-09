package cbit.vcell.mesh;
/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import java.util.Vector;
/**
 * Insert the type's description here.
 * Creation date: (7/4/2001 5:04:16 PM)
 * @author: Frank Morgan
 */
public class MeshRegionInfo implements org.vcell.util.Matchable, java.io.Serializable {
	//
	private Vector volumeRegionMapSubvolume = new Vector();
	private Vector membraneRegionMapVolumeRegion = new Vector();
	private byte[] fieldCompressedVolumeElementMapVolumeRegion = null;
	private transient byte[] fieldVolumeElementMapVolumeRegion = null;
	private int[] fieldMembraneElementMapMembraneRegion = null;
	//
	class VolumeRegionMapSubvolume implements java.io.Serializable {
		public int volumeRegionID;
		public int subvolumeID;
		public double volumeRegionVolume;
		
		public VolumeRegionMapSubvolume(int argvolumeRegionID,int argsubvolumeID,double argvolumeRegionVolume){
			volumeRegionID = argvolumeRegionID;
			subvolumeID = argsubvolumeID;
			volumeRegionVolume = argvolumeRegionVolume;
		}
	}

	class MembraneRegionMapVolumeRegion implements java.io.Serializable {
		public int membraneRegionID;
		public int volumeRegionInsideID;
		public int volumeRegionOutsideID;
		public double membraneRegionSurface;
		public MembraneRegionMapVolumeRegion(int argmembraneRegionID,int argvolumeRegionInsideID,int argvolumeRegionOutsideID,double argmembraneRegionSurface){
			membraneRegionID = 		argmembraneRegionID;
			volumeRegionInsideID = 	argvolumeRegionInsideID;
			volumeRegionOutsideID = argvolumeRegionOutsideID;
			membraneRegionSurface = argmembraneRegionSurface;
		}
	}

/**
 * VolumeRegion constructor comment.
 */
public MeshRegionInfo() {
	super();
}


/**
 * Checks for internal representation of objects, not keys from database
 * @return boolean
 * @param obj java.lang.Object
 */
public boolean compareEqual(org.vcell.util.Matchable obj) {
	return false;
}


/**
 * Insert the method's description here.
 * Creation date: (7/4/2001 6:00:49 PM)
 * @return byte[]
 */
private void compress() throws java.io.IOException {
    java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
    java.util.zip.DeflaterOutputStream dos =
        new java.util.zip.DeflaterOutputStream(bos);
    dos.write(
        fieldVolumeElementMapVolumeRegion,
        0,
        fieldVolumeElementMapVolumeRegion.length);
    dos.flush();
    dos.close();
    fieldCompressedVolumeElementMapVolumeRegion = bos.toByteArray();
    bos.close();
}


/**
 * Insert the method's description here.
 * Creation date: (7/4/2001 5:50:56 PM)
 * @param cvemvr byte[]
 */
public byte[] getCompressedVolumeElementMapVolumeRegion() {
    if (fieldCompressedVolumeElementMapVolumeRegion == null) {
        if (fieldVolumeElementMapVolumeRegion != null) {
            try {
                compress();
            } catch (java.io.IOException e) {
                throw new RuntimeException(e.toString());
            }
        } else {
            return null;
        }
    }
    return fieldCompressedVolumeElementMapVolumeRegion;
}


/**
 * Insert the method's description here.
 * Creation date: (7/5/2001 12:25:30 PM)
 * @return int
 * @param membraneElementIndex int
 */
public int getMembraneRegionForMembraneElement(int membraneElementIndex) {
	return fieldMembraneElementMapMembraneRegion[membraneElementIndex];
}


/**
 * Insert the method's description here.
 * Creation date: (8/8/2005 11:33:14 AM)
 * @return java.util.Vector
 */
java.util.Vector getMembraneRegionMapVolumeRegion() {
	return membraneRegionMapVolumeRegion;
}


/**
 * Insert the method's description here.
 * Creation date: (3/3/2002 11:35:24 PM)
 * @return int
 */
public int getNumMembraneRegions() {
	return membraneRegionMapVolumeRegion.size();
}


/**
 * Insert the method's description here.
 * Creation date: (3/3/2002 11:35:24 PM)
 * @return int
 */
public int getNumVolumeRegions() {
	return volumeRegionMapSubvolume.size();
}


/**
 * Insert the method's description here.
 * Creation date: (9/20/2005 4:58:05 PM)
 * @return double
 * @param membraneIndex int
 */
public double getRegionMembraneSurfaceAreaFromMembraneIndex(int membraneIndex) {

	for(int i=0;i<membraneRegionMapVolumeRegion.size();i+= 1){
		MembraneRegionMapVolumeRegion mrmvr = (MembraneRegionMapVolumeRegion)membraneRegionMapVolumeRegion.elementAt(i);
		if(mrmvr.membraneRegionID == getMembraneRegionForMembraneElement(membraneIndex)){
			return mrmvr.membraneRegionSurface;
		}
	}
	
	throw new RuntimeException("Couldn't find surface area from membraneIndex="+membraneIndex);
}


/**
 * Insert the method's description here.
 * Creation date: (8/20/2003 1:39:54 PM)
 * @return int
 * @param volRegion int
 */
public int getSubVolumeIDfromVolRegion(int volRegion) {
	for (int i = 0; i < volumeRegionMapSubvolume.size(); i++){
		MeshRegionInfo.VolumeRegionMapSubvolume	map = (MeshRegionInfo.VolumeRegionMapSubvolume)volumeRegionMapSubvolume.elementAt(volRegion);
		if (map.volumeRegionID == volRegion) {
			return map.subvolumeID;
		}
	}
	throw new RuntimeException("Volume Region "+volRegion+" not found!");
}


/**
 * Insert the method's description here.
 * Creation date: (7/5/2001 1:05:24 PM)
 * @return java.lang.String
 */
public String getVCMLMembraneRegionsMapVolumeRegion() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("\t"+cbit.vcell.math.VCML.MembraneRegionsMapVolumeRegion+" {\n");
	int numMembraneRegionsMapVolumeRegion = membraneRegionMapVolumeRegion.size();
	buffer.append("\t"+numMembraneRegionsMapVolumeRegion+"\n");
	for(int i = 0;i<numMembraneRegionsMapVolumeRegion;i+= 1){
		MembraneRegionMapVolumeRegion mrmvr = (MembraneRegionMapVolumeRegion)membraneRegionMapVolumeRegion.elementAt(i);
		buffer.append("\t\t"+mrmvr.membraneRegionID+" "+mrmvr.volumeRegionInsideID+" "+mrmvr.volumeRegionOutsideID+" "+mrmvr.membraneRegionSurface+"\n");
	}
	buffer.append("\t}\n");

	return buffer.toString();

}


/**
 * Insert the method's description here.
 * Creation date: (7/5/2001 1:05:24 PM)
 * @return java.lang.String
 */
public String getVCMLVolumeElementsMapVolumeRegion(int numX,boolean bCompress) {
	StringBuffer buffer = new StringBuffer();
	buffer.append("\t"+cbit.vcell.math.VCML.VolumeElementsMapVolumeRegion+" {\n");
	int numVolumeElementsMapVolumeRegion = getVolumeElementMapVolumeRegion().length;
	if(!bCompress){
		buffer.append("\t"+numVolumeElementsMapVolumeRegion+" UnCompressed \n");
		for(int i = 0;i<numVolumeElementsMapVolumeRegion;i+= 1){
			if(i%numX == 0){
				buffer.append("\n\t\t");
			}
			buffer.append(fieldVolumeElementMapVolumeRegion[i]+" ");
		}
		buffer.append("\n");
	}else{
		buffer.append("\t"+numVolumeElementsMapVolumeRegion+" Compressed \n");
		String hex = org.vcell.util.Hex.toString(getCompressedVolumeElementMapVolumeRegion());
		int i = 0;
		while(i < hex.length()){
			int len = ((hex.length()-i)<40?hex.length()-i:40);
			buffer.append("\t"+hex.substring(i,i+len)+"\n");
			i+= len;
		}
	}
	buffer.append("\t}\n");

	return buffer.toString();

}


/**
 * Insert the method's description here.
 * Creation date: (7/5/2001 1:05:24 PM)
 * @return java.lang.String
 */
public String getVCMLVolumeRegionMapSubvolume() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("\t"+cbit.vcell.math.VCML.VolumeRegionsMapSubvolume+" {\n");
	int numVolumeRegionMapSubvolume = volumeRegionMapSubvolume.size();
	buffer.append("\t"+numVolumeRegionMapSubvolume+"\n");
	for(int i = 0;i<numVolumeRegionMapSubvolume;i+= 1){
		VolumeRegionMapSubvolume vrms = (VolumeRegionMapSubvolume)volumeRegionMapSubvolume.elementAt(i);
		buffer.append("\t\t"+vrms.volumeRegionID+" "+vrms.subvolumeID+" "+vrms.volumeRegionVolume+"\n");
	}
	buffer.append("\t}\n");

	return buffer.toString();

}


/**
 * Insert the method's description here.
 * Creation date: (7/4/2001 5:50:56 PM)
 * @param cvemvr byte[]
 */
public byte[] getVolumeElementMapVolumeRegion() {
    if (fieldVolumeElementMapVolumeRegion == null) {
        if (fieldCompressedVolumeElementMapVolumeRegion != null) {
            try{
	            fieldVolumeElementMapVolumeRegion = uncompress(fieldCompressedVolumeElementMapVolumeRegion);
            }catch(java.io.IOException e){
	            throw new RuntimeException(e.toString());
            }
        }else{
	        return null;
        }
    }
    return fieldVolumeElementMapVolumeRegion;
}


/**
 * Insert the method's description here.
 * Creation date: (8/8/2005 11:24:31 AM)
 * @return java.util.Vector
 */
java.util.Vector getVolumeRegionMapSubvolume() {
	return volumeRegionMapSubvolume;
}


/**
 * Insert the method's description here.
 * Creation date: (7/5/2001 11:03:19 AM)
 * @param membraneElementMapMembraneRegion byte[]
 */
public void mapMembraneElementsToMembraneRegions(int[] membraneElementMapMembraneRegion) {
	//MembraneElement is implicit in index of membraneElementMapMembraneRegion array
	fieldMembraneElementMapMembraneRegion = membraneElementMapMembraneRegion;
	}


/**
 * Insert the method's description here.
 * Creation date: (7/4/2001 5:07:49 PM)
 * @param volumeRegionID int
 * @param subvolumeID int
 * @param volume double
 */
public void mapMembraneRegionToVolumeRegion(int membraneRegionID, int volumeRegionInsideID, int volumeRegionOutsideID, double membraneRegionSurface) {
	//Integer membraneRegionIDIint = new Integer(membraneRegionID);
	MembraneRegionMapVolumeRegion mrmvr = new MembraneRegionMapVolumeRegion(membraneRegionID,volumeRegionInsideID,volumeRegionOutsideID,membraneRegionSurface);
	//membraneRegionMapVolumeRegion.put(membraneRegionIDIint,mrmvr);
	membraneRegionMapVolumeRegion.add(mrmvr);
	}


/**
 * Insert the method's description here.
 * Creation date: (7/4/2001 5:07:49 PM)
 * @param volumeRegionID int
 * @param subvolumeID int
 * @param volume double
 */
public void mapVolumeRegionToSubvolume(int volumeRegionID, int subvolumeID, double volumeRegionVolume) {
	//Integer volumeRegionIDIint = new Integer(volumeRegionID);
	VolumeRegionMapSubvolume vrms = new VolumeRegionMapSubvolume(volumeRegionID,subvolumeID,volumeRegionVolume);
	//volumeRegionMapSubvolume.put(volumeRegionIDIint,vrms);
	volumeRegionMapSubvolume.add(vrms);
	}


/**
 * Insert the method's description here.
 * Creation date: (7/4/2001 5:50:56 PM)
 * @param cvemvr byte[]
 */
public void setCompressedVolumeElementMapVolumeRegion(byte[] cvemvr) throws java.io.IOException{
    fieldCompressedVolumeElementMapVolumeRegion = cvemvr;
    fieldVolumeElementMapVolumeRegion = uncompress(cvemvr);
}


/**
 * Insert the method's description here.
 * Creation date: (7/4/2001 5:50:56 PM)
 * @param cvemvr byte[]
 */
public void setVolumeElementMapVolumeRegion(byte[] vemvr) {
	fieldVolumeElementMapVolumeRegion = vemvr;
	fieldCompressedVolumeElementMapVolumeRegion = null;
	}


/**
 * Insert the method's description here.
 * Creation date: (7/4/2001 6:00:49 PM)
 * @return byte[]
 */
private byte[] uncompress(byte[] compressedIN) throws java.io.IOException {
    java.io.ByteArrayInputStream bis =
        new java.io.ByteArrayInputStream(compressedIN);
    java.util.zip.InflaterInputStream iis =
        new java.util.zip.InflaterInputStream(bis);
    int temp;
    byte buf[] = new byte[65536];
    java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
    while ((temp = iis.read(buf, 0, buf.length)) != -1) {
        bos.write(buf, 0, temp);
    }
    byte[] tempArr = bos.toByteArray();
    iis.close();
    bos.close();
    return tempArr;
}
}