/*
 * $Id: BackgroundTask.java 3146 2008-02-20 18:10:07Z blowagie $
 *
 * Copyright 2007 Bruno Lowagie.
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

package com.lowagie.rups.model;

import javax.swing.SwingUtilities;

/**
 * Allows you to perform long lasting tasks in background.
 * If we ever move to Java 6, we should use the SwingWorker class
 * (included in the JDK) instead of this custom Event Dispatching
 * code.
 */

public abstract class BackgroundTask {

	/**
     * Inner class that holds the reference to the thread.
     */
    private static class ThreadWrapper {
        private Thread thread;
        ThreadWrapper(Thread t) { thread = t; }
        synchronized Thread get() { return thread; }
        synchronized void clear() { thread = null; }
    }

	/** A wrapper for the tread that executes a time-consuming task. */
    private ThreadWrapper thread;

    /**
     * Starts a thread.
     * Executes the time-consuming task in the construct method;
     * finally calls the finish().
     */
    public BackgroundTask() {
        final Runnable doFinished = new Runnable() {
           public void run() { finished(); }
        };

        Runnable doConstruct = new Runnable() {
            public void run() {
                try {
                	doTask();
                }
                finally {
                    thread.clear();
                }
                SwingUtilities.invokeLater(doFinished);
            }
        };
        Thread t = new Thread(doConstruct);
        thread = new ThreadWrapper(t);
    }

    /**
     * Implement this class; the time-consuming task will go here.
     */
    public abstract void doTask();

    /**
     * Starts the thread.
     */
    public void start() {
        Thread t = thread.get();
        if (t != null) {
            t.start();
        }
    }

    /**
     * Forces the thread to stop what it's doing.
     */
    public void interrupt() {
        Thread t = thread.get();
        if (t != null) {
            t.interrupt();
        }
        thread.clear();
    }

    /**
     * Called on the event dispatching thread once the
     * construct method has finished its task.
     */
    public void finished() {
    }
}
