/*
 * $Header: /cvsroot/xanswer/projects/org.bardsoftware.eclipsito/src/org/osgi/framework/BundleException.java,v 1.1 2005/02/21 16:59:06 akarjakina Exp $
 *
 * Copyright (c) The Open Services Gateway Initiative (2000-2001).
 * All Rights Reserved.
 *
 * Implementation of certain elements of the Open Services Gateway Initiative
 * (OSGI) Specification may be subject to third party intellectual property
 * rights, including without limitation, patent rights (such a third party may
 * or may not be a member of OSGi). OSGi is not responsible and shall not be
 * held responsible in any manner for identifying or failing to identify any or
 * all such third party intellectual property rights.
 *
 * This document and the information contained herein are provided on an "AS
 * IS" basis and OSGI DISCLAIMS ALL WARRANTIES, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO ANY WARRANTY THAT THE USE OF THE INFORMATION HEREIN WILL
 * NOT INFRINGE ANY RIGHTS AND ANY IMPLIED WARRANTIES OF MERCHANTABILITY OR
 * FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL OSGI BE LIABLE FOR ANY
 * LOSS OF PROFITS, LOSS OF BUSINESS, LOSS OF USE OF DATA, INTERRUPTION OF
 * BUSINESS, OR FOR DIRECT, INDIRECT, SPECIAL OR EXEMPLARY, INCIDENTIAL,
 * PUNITIVE OR CONSEQUENTIAL DAMAGES OF ANY KIND IN CONNECTION WITH THIS
 * DOCUMENT OR THE INFORMATION CONTAINED HEREIN, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH LOSS OR DAMAGE.
 *
 * All Company, brand and product names may be trademarks that are the sole
 * property of their respective owners. All rights reserved.
 */

package org.osgi.framework;

/**
 * A Framework exception used to indicate that a bundle lifecycle problem occurred.
 *
 * <p><tt>BundleException</tt> object is created by the Framework to denote an exception condition
 * in the lifecycle of a bundle.
 * <tt>BundleException</tt>s should not be created by bundle developers.
 *
 * <p> This exception is updated to conform to the general purpose
 * exception chaining mechanism.
 *
 * @version $Revision: 1.1 $
 */

public class BundleException extends Exception
{
    /**
     * Nested exception.
     */
    private transient Throwable cause;

    /**
     * Creates a <tt>BundleException</tt> that wraps another exception.
     *
     * @param msg The associated message.
     * @param cause The cause of this exception.
     */
    public BundleException(String msg, Throwable cause)
    {
        super(msg);
        this.cause = cause;
    }

    /**
     * Creates a <tt>BundleException</tt> object with the specified message.
     *
     * @param msg The message.
     */
    public BundleException(String msg)
    {
        super(msg);
        this.cause = null;
    }

    /**
     * Returns any nested exceptions included in this exception.
     *
     * <p>This method predates the general purpose exception chaining mechanism.
     * The {@link #getCause()} method is now the preferred means of
     * obtaining this information.
     *
     * @return The nested exception; <tt>null</tt> if there is
     * no nested exception.
     */
    public Throwable getNestedException()
    {
        return cause;
    }

    //================================================================
	// Post R3 Addenda. EXPERIMENTAL.
	//================================================================

    /**
     * Returns the cause of this exception or <tt>null</tt> if no
     * cause was specified when this exception was created.
     *
     * @return  The cause of this exception or <tt>null</tt> if no
     * cause was specified.
     * @since 1.3 <b>EXPERIMENTAL</b>
     */
    public Throwable getCause() {
        return cause;
    }

    /**
     * The cause of this exception can only be set when constructed.
     *
     * @throws IllegalStateException
     * This method will always throw an <tt>IllegalStateException</tt>
     * since the cause of this exception can only be set when constructed.
     * @since 1.3 <b>EXPERIMENTAL</b>
     */
    public Throwable initCause(Throwable cause) {
    	throw new IllegalStateException();
    }
}
