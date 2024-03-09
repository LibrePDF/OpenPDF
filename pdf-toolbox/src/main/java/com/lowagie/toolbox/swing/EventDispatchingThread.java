/*
 * $Id: EventDispatchingThread.java 3271 2008-04-18 20:39:42Z xlv $
 * Copyright (c) 2007 Bruno Lowagie
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * If we ever move to Java 6, we should use the SwingWorker class
 * (included in the JDK) instead of this custum Event Dispatching
 * code.
 */

package com.lowagie.toolbox.swing;

import javax.swing.SwingUtilities;

/**
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public abstract class EventDispatchingThread {

    /**
     * The value of an object constructed by the construct() method.
     */
    private Object value;
    /**
     * A wrapper for the tread that executes a time-consuming task.
     */
    private ThreadWrapper thread;

    /**
     * Starts a thread. Executes the time-consuming task in the construct method; finally calls the finish().
     */
    public EventDispatchingThread() {
        final Runnable doFinished = this::finished;

        Runnable doConstruct = () -> {
            try {
                value = construct();
            } finally {
                thread.clear();
            }
            SwingUtilities.invokeLater(doFinished);
        };
        Thread t = new Thread(doConstruct);
        thread = new ThreadWrapper(t);
    }

    /**
     * Implement this class; the time-consuming task will go here.
     *
     * @return Object
     */
    public abstract Object construct();

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
     * Called on the event dispatching thread once the construct method has finished its task.
     */
    public void finished() {
    }

    /**
     * Returns the value created by the construct method.
     *
     * @return the value created by the construct method or null if the task was interrupted before it was finished.
     */
    public Object get() {
        while (true) {
            Thread t = thread.get();
            if (t == null) {
                return value;
            }
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // propagate
                return null;
            }
        }
    }

    /**
     * Inner class that holds the reference to the thread.
     */
    private static class ThreadWrapper {

        private Thread thread;

        ThreadWrapper(Thread t) {
            thread = t;
        }

        synchronized Thread get() {
            return thread;
        }

        synchronized void clear() {
            thread = null;
        }
    }
}
