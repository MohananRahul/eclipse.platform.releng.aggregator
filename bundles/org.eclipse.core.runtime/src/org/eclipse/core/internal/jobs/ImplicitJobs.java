/**********************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.jobs;

import java.util.*;
import org.eclipse.core.internal.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Implicit jobs are jobs that are running by virtue of a JobManager.begin/end
 * pair. They act like normal jobs, except they are tied to an arbitrary thread
 * of the client's choosing, and they can be nested.
 */
class ImplicitJobs {
	/**
	 * Cached of unused instance that can be reused 
	 */
	private ThreadJob jobCache = null;
	protected JobManager manager;

	/**
	 * Maps (Rule ->ThreadJob), suspended scheduling rule to the thread job
	 * that represents that suspended rule
	 */
	private final Map suspendedJobs = new HashMap(20);

	/**
	 * Maps (Thread->ThreadJob), threads to the currently running job for that
	 * thread.
	 */
	private final Map threadJobs = new HashMap(20);

	ImplicitJobs(JobManager manager) {
		this.manager = manager;
	}

	/* (Non-javadoc) 
	 * @see IJobManager#beginRule 
	 */
	void begin(ISchedulingRule rule, IProgressMonitor monitor, boolean suspend) {
		if (JobManager.DEBUG_BEGIN_END)
			JobManager.debug("Begin rule: " + rule); //$NON-NLS-1$
		final Thread currentThread = Thread.currentThread();
		ThreadJob threadJob;
		synchronized (this) {
			threadJob = findThreadJob(currentThread, rule);
			if (threadJob != null) {
				//nested rule, just push on stack and return
				threadJob.push(rule);
				return;
			}
			//no need to schedule a thread job for a null rule
			if (rule == null)
				return;
			//create a thread job for this thread, use the rule from the real job if it has one
			Job realJob = manager.currentJob();
			if (realJob != null && realJob.getRule() != null)
				threadJob = newThreadJob(realJob.getRule());
			else {
				threadJob = newThreadJob(rule);
				threadJob.acquireRule = true;
			}
			//indicate if it is a system job to ensure isBlocking works correctly
			threadJob.setRealJob(realJob);
			threadJob.setThread(currentThread);
			threadJob.push(rule);
		}
		try {
			//join the thread job outside sync block
			if (threadJob.acquireRule) {
				//no need to reaquire any locks because the thread did not wait to get this lock
				if (manager.runNow(threadJob))
					manager.getLockManager().addLockThread(Thread.currentThread(), rule);
				else
					threadJob.joinRun(monitor);
			}
		} finally {
			//remember this thread job  - only do this
			//after the rule is acquired because it is ok for this thread to acquire
			//and release other rules while waiting.
			synchronized (this) {
				if (suspend) 
					suspendedJobs.put(rule, threadJob);
				else
					threadJobs.put(currentThread, threadJob);
			}
		}
	}

	/**
	 * Finds and returns the ThreadJob associated with this thread and rule. Returns
	 * <code>null</code> if no related ThreadJob can be found
	 * 
	 * Note: Callers of this method must synchronize on the receiver while invoking
	 * this method.
	 * 
	 * @param thread The thread to find a ThreadJob for
	 * @param rule The rule associated with the ThreadJob
	 * @return The ThreadJob for the given thread and rule, or <code>null</code>
	 */
	private ThreadJob findThreadJob(final Thread thread, ISchedulingRule rule) {
		//first check for a suspended job rule that contains the rule to begin
		if (suspendedJobs.size() > 0) {
			for (Iterator it = suspendedJobs.keySet().iterator(); it.hasNext();) {
				ISchedulingRule suspendedRule = (ISchedulingRule)it.next();
				if (suspendedRule.contains(rule))
					return (ThreadJob) suspendedJobs.get(suspendedRule);
			}
		}
		//next check for a thread job for this thread
		return (ThreadJob) threadJobs.get(thread);
	}

	/* (Non-javadoc) 
	 * @see IJobManager#endRule 
	 */
	synchronized void end(ISchedulingRule rule) {
		if (JobManager.DEBUG_BEGIN_END)
			JobManager.debug("End rule: " + rule); //$NON-NLS-1$
		final Thread currentThread = Thread.currentThread();
		ThreadJob threadJob = findThreadJob(currentThread, rule);
		if (threadJob == null)
			Assert.isLegal(rule == null, "endRule without matching beginRule: " + rule); //$NON-NLS-1$
		else if (threadJob.pop(rule)) {
			//clean up when last rule scope exits
			if (suspendedJobs.remove(rule) == null)
				threadJobs.remove(currentThread);
			else if (JobManager.DEBUG_BEGIN_END)
				JobManager.debug("Resume rule: " + rule); //$NON-NLS-1$
			//if this job had a rule, then we are essentially releasing a lock
			//note it is safe to do this even if the acquire was aborted
			if (threadJob.acquireRule)
				manager.getLockManager().removeLockThread(Thread.currentThread(), threadJob.getRule());
			//if the job was started, we need to notify job manager to end it
			if (threadJob.isRunning())
				manager.endJob(threadJob, Status.OK_STATUS, false);
			recycle(threadJob);
		}
	}

	/**
	 * Returns the currently running implicit job for the given thread, or null
	 * if there currently isn't one.
	 */
	Job jobForThread(Thread thread) {
		return (Job) threadJobs.get(thread);
	}

	/**
	 * Returns a new or reused ThreadJob instance. 
	 */
	private ThreadJob newThreadJob(ISchedulingRule rule) {
		if (jobCache != null) {
			ThreadJob job = jobCache;
			job.setRule(rule);
			job.acquireRule = job.running = false;
			job.realJob = null;
			jobCache = null;
			return job;
		}
		return new ThreadJob(manager, rule);
	}

	/**
	 * Indicates that a thread job is no longer in use and can be reused. 
	 */
	private void recycle(ThreadJob job) {
		if (jobCache == null && job.recycle())
			jobCache = job;
	}

	/**
	 * Implements IJobManager#resume(ISchedulingRule)
	 * @param rule
	 */
	void resume(ISchedulingRule rule) {
		//resume happens as a consequence of freeing the last rule in the stack
		end(rule);
	}

	/**
	 * Implements IJobManager#suspend(ISchedulingRule, IProgressMonitor)
	 * @param rule
	 * @param monitor
	 */
	void suspend(ISchedulingRule rule, IProgressMonitor monitor) {
		if (JobManager.DEBUG_BEGIN_END)
			JobManager.debug("Suspend rule: " + rule); //$NON-NLS-1$
		//the suspend job will be remembered once the rule is acquired
		begin(rule, monitor, true);
	}
}