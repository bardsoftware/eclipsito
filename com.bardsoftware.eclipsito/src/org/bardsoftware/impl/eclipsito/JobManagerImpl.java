/*
 * Created on 15.09.2005
 */
package org.bardsoftware.impl.eclipsito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.ProgressProvider;

public class JobManagerImpl implements IJobManager {
    private static final  JobManagerImpl ourInstance = new JobManagerImpl();
    private Map myGroup_Jobs = new HashMap();
	private ProgressProvider myProgressProvider;
    private Semaphor mySemaphor = new Semaphor();

    public static JobManagerImpl getInstance() {
        return ourInstance;
    }

    public void doSchedule(final InternalJobImpl job) {
        Object group = job.getProgressMonitor();
        System.err.println("scheduling job="+job+" group="+group);
        getJobs(group).scheduleJob(job);
    }

	public void cancel(InternalJobImpl job) {
        GroupWorker worker = (GroupWorker) myGroup_Jobs.get(job.getProgressMonitor());
        assert worker!=null;
        worker.cancelJobsFrom(job);
        job.getProgressMonitor().setCanceled(true);
	}

    protected boolean isCanceled(IProgressMonitor group) {
        return group.isCanceled();
    }


    private GroupWorker getJobs(Object group) {
        GroupWorker worker = (GroupWorker) myGroup_Jobs.get(group);
        if (worker==null) {
        	worker = new GroupWorker((IProgressMonitor) group);
            myGroup_Jobs.put(worker.getProgressMonitor(), worker);
        }
        assert worker!=null;
        return worker;
    }

    public IProgressMonitor createProgressGroup() {
        IProgressMonitor monitor = myProgressProvider==null ? new ConsoleProgressMonitor() : myProgressProvider.createProgressGroup();
        //GroupWorker worker = new GroupWorker(monitor);
        //myGroup_Jobs.put(worker.getProgressMonitor(), worker);
        //return worker.getProgressMonitor();
        return monitor;
    }

	public void cancel(Object family) {
		System.err.println("\n\n!!!!!!!!!!! canceling family="+family+"\n\n");
		synchronized (mySemaphor) {
			mySemaphor.setClosed(true);
		}
		for (Iterator jobLists = myGroup_Jobs.values().iterator(); jobLists.hasNext();) {
			GroupWorker nextWorker = (GroupWorker) jobLists.next();
			nextWorker.cancelJobFamily(family);
		}
		synchronized (mySemaphor) {
			mySemaphor.setClosed(false);
			mySemaphor.notifyAll();
		}
	}

	private static class Semaphor {
		private boolean isClosed;
		boolean isClosed() {
			return isClosed;
		}
		void setClosed(boolean closed) {
			isClosed = closed;
		}
	}

    private static class ConsoleProgressMonitor implements IProgressMonitor {

        private String myTaskName;
        private int myTotalWork;
        private boolean isCanceled;
        private int myWorked;

        public void beginTask(String name, int totalWork) {
            myTaskName = name;
            myTotalWork = totalWork;
        }

        public void done() {
            System.out.println("["+myTaskName+"] done");
        }

        public void internalWorked(double work) {
        }

        public boolean isCanceled() {
            return isCanceled;
        }

        public void setCanceled(boolean value) {
            isCanceled = value;
            System.out.println("["+myTaskName+"] canceled");
        }

        public void setTaskName(String name) {
        }

        public void subTask(String name) {
        }

        public void worked(int work) {
            myWorked+=work;
            System.out.println("["+myTaskName+"] "+(myWorked*100/myTotalWork)+"%");
        }

    }

	public void setProgressProvider(ProgressProvider progressProvider) {
		myProgressProvider = progressProvider;
	}

	private class GroupWorker implements Runnable {
		Thread myThread;
		LinkedList myJobs = new LinkedList();
		private IProgressMonitor myProgressMonitor;
		public void run() {
			while (true) {
				synchronized(JobManagerImpl.this.mySemaphor) {
					if (JobManagerImpl.this.mySemaphor.isClosed()) {
						try {
							JobManagerImpl.this.mySemaphor.wait();
						} catch (InterruptedException e) {
							cleanJobs();
						}
					}
				}
				synchronized (myJobs) {
					if (myJobs.isEmpty()) {
						try {
							myJobs.wait();
						} catch (InterruptedException e) {
							cleanJobs();
						}
					}
					else {
						InternalJobImpl next = (InternalJobImpl) myJobs.removeFirst();
						IStatus result = next.run(myProgressMonitor);
                        next.setResult(result);
		                if (result.isOK()) {
		                }
		                else {
		                	cleanJobs();
		                }
					}
				}
			}
		}

		public void cancelJobFamily(Object family) {
			for (Iterator jobs = myJobs.iterator();jobs.hasNext();) {
				Job nextJob = (Job)jobs.next();
				System.err.println("next job="+nextJob);
				if (nextJob.belongsTo(family)) {
					jobs.remove();
				}
			}
		}

		public void cancelJobsFrom(InternalJobImpl job) {
			boolean remove = false;
			for (Iterator jobs = myJobs.iterator(); jobs.hasNext();) {
				InternalJobImpl nextJob = (InternalJobImpl) jobs.next();
				if (nextJob==job) {
					remove = true;
				}
				if (remove) {
					jobs.remove();
				}
			}
		}

		public IProgressMonitor getProgressMonitor() {
			return myProgressMonitor;
		}

		void scheduleJob(InternalJobImpl job) {
			System.err.println("scheduling job="+job);
			synchronized (myJobs) {
				myJobs.add(job);
				myJobs.notify();
			}
		}
		private void cleanJobs() {
			System.err.println("cleaning jobs...");
			myJobs.clear();
			myGroup_Jobs.remove(myProgressMonitor);
		}
		GroupWorker(IProgressMonitor progressDelegate) {
			myThread = new Thread(this);
			myProgressMonitor = progressDelegate;
			myThread.start();
		}

		void start() {
		}
		/*
		private class ProgressMonitorImpl implements IProgressMonitor {
			private final IProgressMonitor myProgressDelegate;

			ProgressMonitorImpl(IProgressMonitor progressDelegate) {
				myProgressDelegate = progressDelegate;
			}

			public void beginTask(String name, int totalWork) {
				myProgressDelegate.beginTask(name, totalWork);
			}

			public void done() {
				System.err.println("done!");
				myProgressDelegate.done();
				myThread.interrupt();
			}

			public void internalWorked(double work) {
				myProgressDelegate.internalWorked(work);
			}

			public boolean isCanceled() {
				return myProgressDelegate.isCanceled();
			}

			public void setCanceled(boolean value) {
				myProgressDelegate.setCanceled(value);
				if (value) {
					myThread.interrupt();
				}
			}

			public void setTaskName(String name) {
				myProgressDelegate.setTaskName(name);
			}

			public void subTask(String name) {
				myProgressDelegate.subTask(name);
			}

			public void worked(int work) {
				myProgressDelegate.worked(work);
			}

		}
		*/
	}

}
