/*
 * Created on 15.09.2005
 */
package org.bardsoftware.impl.eclipsito;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

public abstract class InternalJobImpl {
    protected static final JobManagerImpl ourManager = JobManagerImpl.getInstance();
    private IProgressMonitor myMonitor;
    private String myName;
    private int myTicks;

    /**
     * Volatile because it is usually set via a Worker thread and is read via a 
     * client thread. 
     */
    private volatile IStatus result;

    protected abstract IStatus run(IProgressMonitor progressMonitor);

    public InternalJobImpl(String name) {
        assert name != null : "Job name is null";
        myName = name;
    }
    
    public void doSchedule() {
        ourManager.doSchedule(this);
    }
    
    public void setProgressGroup(IProgressMonitor monitor, int ticks) {
        myMonitor = monitor;
        myTicks = ticks;
    }
    
    int getTicks() {
        return myTicks;
    }

    protected String getName() {
        return myName;
    }
    
    IProgressMonitor getProgressMonitor() {
        return myMonitor;
    }
    
    protected boolean cancel() {
    	ourManager.cancel(this);
        return true;
    }
    
    protected void setResult(IStatus result) {
        this.result = result;
    }

    protected IStatus getResult() {
        return result;
    }
}
