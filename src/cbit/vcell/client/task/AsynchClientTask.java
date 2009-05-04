package cbit.vcell.client.task;

import java.util.Hashtable;


import org.vcell.util.UserCancelException;
/**
 * Insert the type's description here.
 * Creation date: (5/31/2004 6:09:07 PM)
 * @author: Ion Moraru
 */
public abstract class AsynchClientTask {
	public final static int TASKTYPE_NONSWING_BLOCKING = 1;
	public final static int TASKTYPE_SWING_NONBLOCKING = 2;
	public final static int TASKTYPE_SWING_BLOCKING = 3;

/**
 * Insert the method's description here.
 * Creation date: (5/31/2004 7:18:33 PM)
 * @return boolean
 */
	private boolean bSkipIfAbort = true;
	private boolean bSkipIfCancel = true;
	private int taskType = TASKTYPE_NONSWING_BLOCKING;
	private String taskName = "";
	
	public AsynchClientTask(String name, int taskType) {
		this(name, taskType, true, true);
	}
	
	public AsynchClientTask(String name, int taskType, boolean skipIfAbort, boolean skipIfCancel) {
		this.taskName = name;
		this.taskType = taskType;
		this.bSkipIfAbort = skipIfAbort;
		this.bSkipIfCancel = skipIfCancel;
	}
	
	public final boolean skipIfAbort() {
		return bSkipIfAbort;
	}

	/**
	 * Insert the method's description here.
	 * Creation date: (5/31/2004 7:18:33 PM)
	 * @return boolean
	 */
	public boolean skipIfCancel(UserCancelException exc) {
		return bSkipIfCancel;
	}
	
	public final int getTaskType() {
		return taskType;
	}

	public final String getTaskName() {
		return taskName;
	}
	
	public abstract void run(Hashtable<String, Object> hashTable) throws Exception;
}