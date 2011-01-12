/*
 * Created on 15.09.2005
 */
package org.eclipse.core.runtime.jobs;

import org.bardsoftware.impl.eclipsito.InternalJobImpl;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

public abstract class Job extends InternalJobImpl {
    /**
     * Returns the job manager.
     * 
     * @return the job manager
     * @since org.eclipse.core.jobs 3.2
     */
    public static final IJobManager getJobManager() {
        return ourManager;
    }
    
    public Job(String name) {
        
    }

    /**
     * Executes this job.  Returns the result of the execution.
     * <p>
     * The provided monitor can be used to report progress and respond to 
     * cancellation.  If the progress monitor has been canceled, the job
     * should finish its execution at the earliest convenience and return a result
     * status of severity {@link IStatus#CANCEL}.  The singleton
     * cancel status {@link Status#CANCEL_STATUS} can be used for
     * this purpose.  The monitor is only valid for the duration of the invocation
     * of this method.
     * <p>
     * This method must not be called directly by clients.  Clients should call
     * <code>schedule</code>, which will in turn cause this method to be called.
     * <p>
     * Jobs can optionally finish their execution asynchronously (in another thread) by 
     * returning a result status of {@link #ASYNC_FINISH}.  Jobs that finish
     * asynchronously <b>must</b> specify the execution thread by calling
     * <code>setThread</code>, and must indicate when they are finished by calling
     * the method <code>done</code>.
     * 
     * @param monitor the monitor to be used for reporting progress and
     * responding to cancelation. The monitor is never <code>null</code>
     * @return resulting status of the run. The result must not be <code>null</code>
     * @see #ASYNC_FINISH
     * @see #done(IStatus)
     */
    protected abstract IStatus run(IProgressMonitor monitor);

    /**
     * Schedules this job to be run.  The job is added to a queue of waiting
     * jobs, and will be run when it arrives at the beginning of the queue.
     * <p>
     * This is a convenience method, fully equivalent to 
     * <code>schedule(0L)</code>.
     * </p>
     * @see #schedule(long)
     */
    public final void schedule() {
        super.doSchedule();
    }
    
    public void join() throws InterruptedException {
        
    }
    
	/**
	 * Returns whether this job belongs to the given family.  Job families are
	 * represented as objects that are not interpreted or specified in any way
	 * by the job manager.  Thus, a job can choose to belong to any number of
	 * families.
	 * <p>
	 * Clients may override this method.  This default implementation always returns
	 * <code>false</code>.  Overriding implementations must return <code>false</code>
	 * for families they do not recognize.
	 * </p>
	 * 
	 * @param family the job family identifier
	 * @return <code>true</code> if this job belongs to the given family, and 
	 * <code>false</code> otherwise.
	 */
	public boolean belongsTo(Object family) {
		return false;
	}
    
    public final boolean cancel() {
        return super.cancel();
    }    
    
}
