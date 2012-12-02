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

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.UMLPackage;


/**
 * This class contains the AspectJ UML profile, and allows access to specific stereotypes.
 * @author jeff
 *
 */
class AspectJUmlProfile {
	private Profile aspectJProfile;

	public AspectJUmlProfile() {

		URI uri = URI.createPlatformPluginURI("/edu.utdallas.fdaf.aspectj.reverse/AspectJ.profile.uml", false);
		ResourceSet resourceSet = new ResourceSetImpl();

		Package package1 = null;


		Resource resource = resourceSet.getResource(uri, true);

		package1 = (org.eclipse.uml2.uml.Package) EcoreUtil.getObjectByType(resource.getContents(), UMLPackage.eINSTANCE.getPackage());


		aspectJProfile = (Profile) package1;


	}

	/**
	 * @return the aspectJProfile
	 */
	public Profile getAspectJProfile() {
		return aspectJProfile;
	}
	
	public Stereotype getStereotype(String stereotypeName) {
		return aspectJProfile.getOwnedStereotype(stereotypeName);
	}
	
	public EnumerationLiteral getEnumLiteral(String enumName, String literalName) {
		org.eclipse.uml2.uml.Type enumType = aspectJProfile.getOwnedType(enumName);
		Enumeration enumeration = (Enumeration)enumType;
		EnumerationLiteral literal = enumeration.getOwnedLiteral(literalName);
		return literal;
	}

}
