/*******************************************************************************
 * Copyright (c) 2009 Jeffrey Koch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jeffrey Koch - initial API and implementation.
 *******************************************************************************/ 

package edu.utdallas.fdaf.aspectj.reverse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.VisibilityKind;

import edu.utdallas.fdaf.aspectj.reverse.internal.Activator;
import org.topcased.java.reverse.AbstractJava2UMLConverter;
//import edu.utdallas.fdaf.org.topcased.java.reverse.JavaAnnotations2UMLConverter;

/**
 * 
 * @author Jeffrey Koch
 *
 */
public class AspectJReporter {
	/**
	 * Given a list of {@link IJavaElement}s, returns a File that contains a UML model of
	 * the IJavaElements.
	 * 
	 * Currently processes only the {@link org.eclipse.jdt.core.IJavaProject}s and 
	 * {@link org.eclipse.jdt.core.IPackageFragment}s
	 * in the list.
	 * 
	 * @param javaElements List of IJavaElements
	 * @return File containing the UML model
	 * @throws IOException if unable to create the UML model file
	 * @throws CoreException 
	 * @throws BuildNeededException if AspectJ model isn't available because project hasn't been successfully built 
	 */
	public void report(List<IJavaElement> javaElements, IFile progFactsXmiFile) throws IOException, CoreException, BuildNeededException  {
		
		for (IJavaElement elt : javaElements) {
			AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForJavaElement(elt);
		
			
			
			if (!model.hasModel()) {
			//	javax.swing.JOptionPane.showMessageDialog(null, "fdjklsafhldskaf");
				throw new BuildNeededException("Project " + model.getProject().getName() + " hasn't been built yet.");
			}
		}
		
		
		AbstractJava2UMLConverter converter = new AspectJ2UMLConverter();

		
		
        String[] importList = {"platform:/plugin/edu.utdallas.fdaf.aspectj.reverse/AspectJ.profile.uml"};

		converter.setImportList(importList);
		converter.setModelName("UML Model");
		converter.setVisibility(VisibilityKind.PRIVATE_LITERAL);
		// Create a resource set
		//
		ResourceSet resourceSet = new ResourceSetImpl();

		// Get the URI of the model file.
		//
//		File tempFile = File.createTempFile("AjUmlModel", ".uml");
//		String tempFileString = progFactsXmiFile.getAbsolutePath();
//		System.out.println("Program Facts XMI file: " + tempFileString);
		
		URI tempFileURI = URI.createPlatformResourceURI(progFactsXmiFile.getFullPath().toString(), false);
//		URI tempFileURI = URI.createFileURI(tempFileString);

		//
		// Create a resource for this file.
		//
		Resource emfResource = resourceSet.createResource(tempFileURI);

		//Run through each of the javaElements and converts each one--add to model.
		for (IJavaElement javaElement : javaElements) {
			Package model = null;
			model = converter.convert(javaElement, emfResource);

			// Add the initial model object to the contents.
			//
			if (model != null)
			{
				emfResource.getContents().add(model);
			}

		}


		// When we're done, save the contents of the resource to the file system
		// system.
		//
		Map options = new HashMap();
		try
		{
			emfResource.save(options);
			
			//JWK:  Temporarily inserting file reader here.
//			SampleUmlReader umlReader = new SampleUmlReader(tempFile);
//			umlReader.extractInfo();
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			IStatus status = new Status(IStatus.ERROR, Activator.getId(), IStatus.OK, "An error occured during saving resource", ioe);
			throw new CoreException(status);
		}

		return;

	}

}
