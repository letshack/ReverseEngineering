/*******************************************************************************
 * Copyright (c) 2009 Jeffrey Koch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jeffrey Koch - initial API and implementation.  I Am Not A Lawyer, but if I read 
 *    http://www.eclipse.org/legal/legalfaq.php, http://www.eclipse.org/legal/eplfaq.php, 
 *    and the EPL correctly, I think I have the copyright for this code since I'm
 *    initial implementer of this module even though it extends Java2UMLConverter which
 *    was copyrighted by its initial implementer (David Sciamma).
 *******************************************************************************/ 
package edu.utdallas.fdaf.aspectj.reverse;

/**
 * Exception thrown if the AspectJ model hasn't been created because 
 * the project hasn't been successfully built yet.
 * @author Jeffrey Koch
 *
 */
public class BuildNeededException extends Exception {

	/**
	 * Passes string as part of thrown exception.
	 * @param message
	 */
	public BuildNeededException(String message) {
		super(message);
	}

}
