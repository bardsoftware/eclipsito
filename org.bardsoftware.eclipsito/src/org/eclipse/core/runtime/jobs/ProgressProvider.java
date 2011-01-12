/*
 * Created on 26.10.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.core.runtime.jobs;

import org.eclipse.core.runtime.IProgressMonitor;

public abstract class ProgressProvider {
	/**
	 * Provides a new progress monitor instance to be used by the given job.
	 * This method is called prior to running any job that does not belong to a
	 * progress group. The returned monitor will be supplied to the job's
	 * <code>run</code> method.
	 * 
	 * @see #createProgressGroup()
	 * @see Job#setProgressGroup(IProgressMonitor, int)
	 * @param job the job to create a progress monitor for
	 * @return a progress monitor, or <code>null</code> if no progress monitoring 
	 * is needed.
	 */
	public abstract IProgressMonitor createMonitor(Job job);

	/**
	 * Returns a progress monitor that can be used to provide
	 * aggregated progress feedback on a set of running jobs.
	 * This method implements <code>IJobManager.createProgressGroup</code>,
	 * and must obey all rules specified in that contract.
	 * <p>
	 * This default implementation returns a new
	 * <code>NullProgressMonitor</code>  Subclasses may override.
	 * 
	 * @see IJobManager#createProgressGroup()
	 * @return a progress monitor
	 */
	public abstract IProgressMonitor createProgressGroup();

	/**
	 * Returns a progress monitor that can be used by a running job
	 * to report progress in the context of a progress group. This method
	 * implements <code>Job.setProgressGroup</code>.  One of the
	 * two <code>createMonitor</code> methods will be invoked
	 * prior to each execution of a job, depending on whether a progress
	 * group was specified for the job.
	 * <p>
	 * The provided monitor must be a monitor returned by the method
	 * <code>createProgressGroup</code>.  This method is responsible
	 * for asserting this and throwing an appropriate runtime exception
	 * if an invalid monitor is provided.
	 * <p>
	 * This default implementation returns a new
	 * <code>SubProgressMonitor</code>.  Subclasses may override.
	 * 
	 * @see IJobManager#createProgressGroup()
	 * @see Job#setProgressGroup(IProgressMonitor, int)
	 * @param job the job to create a progress monitor for
	 * @param group the progress monitor group that this job belongs to
	 * @param ticks the number of ticks of work for the progress monitor
	 * @return a progress monitor, or <code>null</code> if no progress monitoring 
	 * is needed.
	 */
	public abstract IProgressMonitor createMonitor(Job job, IProgressMonitor group, int ticks);

	/**
	 * Returns a progress monitor to use when none has been provided
	 * by the client running the job.  
	 * <p>
	 * This default implementation returns a new
	 * <code>NullProgressMonitor</code>  Subclasses may override.
	 * 
	 * @return a progress monitor
	 */
	public abstract IProgressMonitor getDefaultMonitor();
}
