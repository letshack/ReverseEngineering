/*******************************************************************************
 * Copyright (c) 2010 Jeffrey Koch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jeffrey Koch - initial API and implementation. 
 *******************************************************************************/ 
package edu.utdallas.fdaf.aspectj.reverse;

import java.io.File;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * Sample class to figure out how to load a UML file and parse the file's
 * UML model.
 * @author Jeff Koch
 *
 */
public class SampleUmlReader {

	//This will be the loaded UML model.
	private org.eclipse.uml2.uml.Package umlModel;
	//This is the AspectJ profile used by umlModel.
	private org.eclipse.uml2.uml.Profile ajProfile;

	/**
	 * Creates a UML reader for the UML file.
	 * @param umlFile UML File to be read.
	 */
	public SampleUmlReader(File umlFile) {
		ResourceSet resourceSet = new ResourceSetImpl();
		String myFileString = umlFile.getAbsolutePath();
		
		URI myFileURI = URI.createFileURI(myFileString);
		//Note that there are multiple ``URI'' classes.
		try {
			Resource resource = resourceSet.getResource(myFileURI, true);
			umlModel = (org.eclipse.uml2.uml.Package) EcoreUtil.getObjectByType(
				resource.getContents(), UMLPackage.Literals.PACKAGE);
			System.out.println("umlModel = " + umlModel.getName());


			/*
			 * We retrieve the AspectJ profile from our UML model so
			 * we can search the model for aspect-related stereotypes.
			 */
			
			ajProfile = umlModel.getAppliedProfile("AspectJ");
			if (ajProfile != null) {
				System.out.println("ajProfile = " + ajProfile.getName());
			} else {
				System.out.println("ajProfile is null");
			}
		} catch (WrappedException we) {
			System.err.println(we.getMessage());
		}

	}

	/**
	 * Extracts UML information and prints it to stdout.
	 */
	public void extractInfo() {
		System.out.println("extractInfo called on umlModel " + umlModel.getName());
		/*
		 * SampleUmlSwitch does most of the heavy lifting--basically, we give it a 
		 * UML node, and it figures out what to do with it.
		 * 
		 * We pass the AspectJ profile to the UMLSwitch so it can see if any of the
		 * profile's aspect-related stereotypes have been applied to the UML nodes.
		 */
		SampleUmlSwitch umlSwitch = new SampleUmlSwitch(ajProfile);
		System.out.println("Owned Elements:");
		/*
		 * Here, we feed each of the top-level elements in the model (probably the
		 * Packages) to the switch.  The switch will recursively work its way through
		 * the nodes underneath each top-level element.
		 */
		for (Element element : umlModel.getOwnedElements()) {
			String elementInfo = umlSwitch.doSwitch(element);
			if (elementInfo != null) {
				System.out.println(elementInfo);
			}
			
		}
	}

}
