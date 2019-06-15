/*
 * Created on 15.09.2005
 */
package org.eclipse.core.runtime.jobs;

import org.eclipse.core.runtime.IProgressMonitor;

public interface IJobManager {

    /**
     * Returns a progress monitor that can be used to provide
     * aggregated progress feedback on a set of running jobs. A user
     * interface will typically group all jobs in a progress group together,
     * providing progress feedback for individual jobs as well as aggregrated
     * progress for the entire group.  Jobs in the group may be run sequentially,
     * in parallel, or some combination of the two.
     * <p>
     * Recommended usage (this snippet runs two jobs in sequence in a
     * single progress group):
     * <pre>
     *    Job parseJob, compileJob;
     *    IProgressMonitor pm = Platform.getJobManager().createProgressGroup();
     *    try {
     *       pm.beginTask("Building", 10);
     *       parseJob.setProgressGroup(pm, 5);
     *       parseJob.schedule();
     *       compileJob.setProgressGroup(pm, 5);
     *       compileJob.schedule();
     *       parseJob.join();
     *       compileJob.join();
     *    } finally {
     *       pm.done();
     *    }
     * </pre>
     *
     * @see Job#setProgressGroup(IProgressMonitor, int)
     * @see IProgressMonitor
     * @return a progress monitor
     */
    public IProgressMonitor createProgressGroup();

    public void setProgressProvider(ProgressProvider progressProvider);

	/**
	 * Cancels all jobs in the given job family.  Jobs in the family that are currently waiting
	 * will be removed from the queue.  Sleeping jobs will be discarded without having
	 * a chance to wake up.  Currently executing jobs will be asked to cancel but there
	 * is no guarantee that they will do so.
	 *
	 * @param family the job family to cancel, or <code>null</code> to cancel all jobs
	 * @see Job#belongsTo(Object)
	 */
	public void cancel(Object family);
}
