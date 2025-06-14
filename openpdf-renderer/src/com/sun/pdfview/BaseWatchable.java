/*
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.sun.pdfview;

/**
 * An abstract implementation of the watchable interface, that is extended
 * by the parser and renderer to do their thing.
 */
public abstract class BaseWatchable implements Watchable, Runnable {

    /** the current status, from the list in Watchable */
    private int status = Watchable.UNKNOWN;
    /** a lock for status-related operations */
    private final Object statusLock = new Object();
    /** a lock for parsing operations */
    private final Object parserLock = new Object();
    /** when to stop */
    private Gate gate;
    /** suppress local stack trace on setError. */
    private static boolean SuppressSetErrorStackTrace = false;
    /** the thread we are running in */
    private Thread thread;
    private Exception exception;
    
    // handle exceptions via this class
    private static PDFErrorHandler errorHandler = new PDFErrorHandler(); 

    /** 
     * Creates a new instance of BaseWatchable
     */
    protected BaseWatchable() {
        setStatus(Watchable.NOT_STARTED);
    }

    /**
     * Perform a single iteration of this watchable.  This is the minimum
     * granularity which the go() commands operate over.
     *
     * @return one of three values: <ul>
     *         <li> Watchable.RUNNING if there is still data to be processed
     *         <li> Watchable.NEEDS_DATA if there is no data to be processed but
     *              the execution is not yet complete
     *         <li> Watchable.COMPLETED if the execution is complete
     *  </ul>
     */
    protected abstract int iterate() throws Exception;

    /** 
     * Prepare for a set of iterations.  Called before the first iterate() call
     * in a sequence.  Subclasses should extend this method if they need to do
     * anything to setup.
     */
    protected void setup() {
        // do nothing
    }

    /**
     * Clean up after a set of iterations. Called after iteration has stopped
     * due to completion, manual stopping, or error.
     */
    protected void cleanup() {
        // do nothing
    }

    @Override
	public void run() {
        try {
            Thread.sleep(1);
            // call setup once we started
            if (getStatus() == Watchable.NOT_STARTED) {
                setup();
            }

            setStatus(Watchable.PAUSED);

            synchronized (this.parserLock) {
                while (!isFinished() && getStatus() != Watchable.STOPPED) {
                    if (isExecutable()) {
                        // set the status to running
                        setStatus(Watchable.RUNNING);

                        try {
                            // keep going until the status is no longer running,
                            // our gate tells us to stop, or no-one is watching
                            int laststatus = Watchable.RUNNING;
                            while ((getStatus() == Watchable.RUNNING) && (this.gate == null || !this.gate.iterate())) {
                                // update the status based on this iteration
                                int status = iterate();
                                if (status != laststatus) {
                                    // update status only when necessary, this increases performance
                                    setStatus(status);
                                    laststatus = status;
                                }

                            }

                            // make sure we are paused
                            if (getStatus() == Watchable.RUNNING) {
                                setStatus(Watchable.PAUSED);
                            }
                        } catch (Exception ex) {
                            setError(ex);
                        }
                    } else {
                        // wait for our status to change
                        synchronized (this.statusLock) {
                            if (!isExecutable()) {
                                try {
                                    this.statusLock.wait();
                                } catch (InterruptedException ie) {
                                    // ignore
                                }
                            }
                        }
                    }
                }
            }
            // call cleanup when we are done
            if (getStatus() == Watchable.COMPLETED || getStatus() == Watchable.ERROR) {

                cleanup();
            }
        } catch (InterruptedException e) {
            PDFDebugger.debug("Interrupted.");
        }
        // notify that we are no longer running
        this.thread = null;
    }

    /**
     * Get the status of this watchable
     *
     * @return one of the well-known statuses
     */
    @Override
	public int getStatus() {
        return this.status;
    }

    /**
     * Return whether this watchable has finished.  A watchable is finished
     * when its status is either COMPLETED, STOPPED or ERROR
     */
    public boolean isFinished() {
        int s = getStatus();
        return (s == Watchable.COMPLETED ||
                s == Watchable.ERROR);
    }

    /**
     * return true if this watchable is ready to be executed
     */
    public boolean isExecutable() {
        return ((this.status == Watchable.PAUSED || this.status == Watchable.RUNNING) &&
                (this.gate == null || !this.gate.stop()));
    }

    /**
     * Stop this watchable if it is not already finished.  
	 * Stop will cause all processing to cease,
     * and the watchable to be destroyed.
     */
    @Override
	public void stop() {
    	if (!isFinished()) setStatus(Watchable.STOPPED);
    }

    /**
     * Start this watchable and run in a new thread until it is finished or
     * stopped.
     * Note the watchable may be stopped if go() with a
     * different time is called during execution.
     */
    @Override
	public synchronized void go() {
        this.gate = null;

        execute(false);
    }

    /**
     * Start this watchable and run until it is finished or stopped.
     * Note the watchable may be stopped if go() with a
     * different time is called during execution.
     *
     * @param synchronous if true, run in this thread
     */
    public synchronized void go(boolean synchronous) {
        this.gate = null;

        execute(synchronous);
    }

    /**
     * Start this watchable and run for the given number of steps or until
     * finished or stopped.
     *
     * @param steps the number of steps to run for
     */
    @Override
	public synchronized void go(int steps) {
        this.gate = new Gate();
        this.gate.setStopIterations(steps);

        execute(false);
    }

    /**
     * Start this watchable and run for the given amount of time, or until
     * finished or stopped.
     *
     * @param millis the number of milliseconds to run for
     */
    @Override
	public synchronized void go(long millis) {
        this.gate = new Gate();
        this.gate.setStopTime(millis);

        execute(false);
    }

    /**
     * Wait for this watchable to finish
     */
    public void waitForFinish() {
        synchronized (this.statusLock) {
            while (!isFinished() && getStatus() != Watchable.STOPPED) {
                try {
                    this.statusLock.wait();
                } catch (InterruptedException ex) {
                    // ignore
                }
            }
        }
    }

    /**
     * Start executing this watchable
     *
     * @param synchronous if true, run in this thread
     */
    protected synchronized void execute(boolean synchronous) {
        // see if we're already running
        if (this.thread != null) {
            // we're already running. Make sure we wake up on any change.
            synchronized (this.statusLock) {
                this.statusLock.notifyAll();
            }

            return;
        } else if (isFinished()) {
            // we're all finished
            return;
        }

        // we'return not running. Start up
        if (synchronous) {
            this.thread = Thread.currentThread();
            run();
        } else {
        	this.thread = new Thread(this);
        	this.thread.setName(getClass().getName());
        	//Fix for NPE: Taken from http://java.net/jira/browse/PDF_RENDERER-46
        	synchronized (statusLock) {
        	    Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException( Thread th, Throwable ex )
                    {
                        PDFDebugger.debug( "Uncaught exception: " + ex );
                    }
                };
                thread.setUncaughtExceptionHandler( h );
        		thread.start();
        		try {
        			statusLock.wait();
        		} catch (InterruptedException ex) {
        			// ignore
        		}
        	}
        }
    }

    /**
     * Set the status of this watchable
     */
    protected void setStatus(int status) {
        synchronized (this.statusLock) {
            this.status = status;

            this.statusLock.notifyAll();
        }
    }

    /**
     * return true if we would be suppressing setError stack traces.
     * 
     * @return  boolean
     */
    public static boolean isSuppressSetErrorStackTrace () {
        return SuppressSetErrorStackTrace;
    }

    /**
     * set suppression of stack traces from setError.
     * 
     * @param suppressTrace
     */
    public static void setSuppressSetErrorStackTrace(boolean suppressTrace) {
        SuppressSetErrorStackTrace = suppressTrace;
    }

    /**
     * Set an error on this watchable
     */
    protected void setError(Exception error) {
    	exception = error;
        if (!SuppressSetErrorStackTrace) {
            errorHandler.publishException(error);
        }

        setStatus(Watchable.ERROR);
    }

	public Exception getException() {
		return exception;
	}

    /** A class that lets us give it a target time or number of steps,
     * and will tell us to stop after that much time or that many steps
     */
    static class Gate {

        /** whether this is a time-based (true) or step-based (false) gate */
        private boolean timeBased;
        /** the next gate, whether time or iterations */
        private long nextGate;

        /** set the stop time */
        public void setStopTime(long millisFromNow) {
            this.timeBased = true;
            this.nextGate = System.currentTimeMillis() + millisFromNow;
        }

        /** set the number of iterations until we stop */
        public void setStopIterations(int iterations) {
            this.timeBased = false;
            this.nextGate = iterations;
        }

        /** check whether we should stop.
         */
        public boolean stop() {
            if (this.timeBased) {
                return (System.currentTimeMillis() >= this.nextGate);
            } else {
                return (this.nextGate < 0);
            }
        }

        /** Notify the gate of one iteration.  Returns true if we should
         * stop or false if not
         */
        public boolean iterate() {
            if (!this.timeBased) {
                this.nextGate--;
            }

            return stop();
        }
    }
    
    public static void setErrorHandler(PDFErrorHandler e) {
        errorHandler = e;
    }
    
    public static PDFErrorHandler getErrorHandler(){
        if(errorHandler == null) {
            errorHandler = new PDFErrorHandler();
        }
        return errorHandler;
    }
}