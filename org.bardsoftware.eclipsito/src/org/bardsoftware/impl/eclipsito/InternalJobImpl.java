/*
 * Created on 15.09.2005
 */
package org.bardsoftware.impl.eclipsito;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

public abstract class InternalJobImpl {
    private static final JobManagerImpl ourManager = JobManagerImpl.getInstance();
    private IProgressMonitor myMonitor;
    private int myTicks;

    protected abstract IStatus run(IProgressMonitor progressMonitor);

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
    
    IProgressMonitor getProgressMonitor() {
        return myMonitor;
    }
    
    protected boolean cancel() {
    	ourManager.cancel(this);
        return true;
    }
    
}
