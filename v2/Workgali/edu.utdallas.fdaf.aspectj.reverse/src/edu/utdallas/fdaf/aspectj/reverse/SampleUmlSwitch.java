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

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.InterfaceRealization;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.TypedElement;
import org.eclipse.uml2.uml.util.UMLSwitch;

/**
 * <p>This demonstrates one way to use a <code>UMLSwitch</code> to parse a UML model. 
 * <p>Each <code>UMLSwitch</code> has several <code>case<b>foo</b>()</code> methods, where
 * <b>foo</b> is each possible UML node type (<code>caseElement</code>, <code>casePackage</code>, 
 * and so on).  
 * <p><code>UMLSwitch</code> generally works like this:
 * <ul>
 * <li>Create a <code>UMLSwitch</code>, and use <code>doSwitch()</code> to pass a UML node
 * (<code>EObject</code>) to it.
 * <li><code>doSwitch()</code> runs the appropriate <code>case<b>foo</b>()</code> for 
 * the node's type.  If that <code>case<b>foo</b>()</code> method returns <code>null</code>,
 * <code>doSwitch()</code> runs more general <code>case<b>foo</b>()</code> methods until
 * it finds a non-<code>null</code> result.
 * </ul>
 * <p>The default <code>case<b>foo</b>()</code> implementations return <code>null</code>, so 
 * we'd have to write <code>case<b>foo</b>()</code> methods for the node types we want.
 * For this sample class, I wrote some methods that simply returned the name of
 * simple nodes and the name and type of typed nodes; for more complicated nodes, I also 
 * recursively called <code>doSwitch()</code>
 * for the node's subnodes.  (See the Javadocs for each method for the specifics.)
 * @see org.eclipse.uml2.uml.util.UMLSwitch<T>
 * @author Jeff Koch
 *
 */
public class SampleUmlSwitch extends UMLSwitch<String> {

	//I put each node on a separate line.  "newLine" is easier to read and type 
	//than "System.getProperty("line.separator")" :-)
	final private String newLine = System.getProperty("line.separator");

	//If a node uses one of these Stereotypes, it's flagged as aspect-related.
	private Stereotype aspectStereotype;
	private Stereotype staticCrossCuttingFeatureStereotype;
	private Stereotype pointCutStereotype;
	private Stereotype adviceStereotype;

	/**
	 * Initializes the UMLSwitch, giving it the AspectJ profile used by the UML model.
	 * The switch needs the AspectJ profile so it can identify the stereotypes used
	 * (Aspect, Advice, and so forth). 
	 * @param ajProfile UML model's AspectJ profile
	 */
	public SampleUmlSwitch(Profile ajProfile) {
		super();
		//Retrieve the aspect-related stereotypes used by the AspectJ profile.
		aspectStereotype = ajProfile.getOwnedStereotype("Aspect");
		staticCrossCuttingFeatureStereotype = ajProfile.getOwnedStereotype("StaticCrossCuttingFeature");
		pointCutStereotype = ajProfile.getOwnedStereotype("PointCut");
		adviceStereotype = ajProfile.getOwnedStereotype("Advice");
	}

	/**
	 * Returns the name of the Package and the elements it owns.
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#casePackage(org.eclipse.uml2.uml.Package)
	 * @param object Package 
	 * @return Name of the package
	 */
	@Override
	public String casePackage(Package object) {

		return nodeHeader(object, "Package") + getNameAndOwnedElements(object) + nodeTrailer(object);
	}

	/**
	 * This usually closes a node's description with a closing bracket...however, if
	 * this happens to be an Advice or StaticCrossCuttingFeature, it will include the 
	 * advisedElement or onType tag, respectively.
	 * @param object Node currently being checked.
	 * @return See above.
	 */
	private String nodeTrailer(Element object) {
		StringBuilder trailer = new StringBuilder("");
		trailer.append(getStereotypeTag(object, adviceStereotype, "advisedElement"));
		trailer.append(getStereotypeTag(object, staticCrossCuttingFeatureStereotype, "onType"));
		return trailer + "}";
	}

	/**
	 * If the node has the specified property defined for the specified stereotype, returns
	 * a new line followed by the property.  If not, returns an empty string.
	 * @param object Node to be checked.
	 * @param stereotype Stereotype to be checked.
	 * @param propertyName Property to be checked.
	 * @return  See above.
	 */
	private String getStereotypeTag(Element object, Stereotype stereotype, String propertyName) {
		if (object.hasValue(stereotype, propertyName)) {
			StringBuilder result = new StringBuilder(newLine + propertyName + ": {");
			Object tag = object.getValue(stereotype, propertyName);
			if (tag instanceof EList<?>) {
				EList<Object> tagList = (EList<Object>)tag;
				for (Object listItem : tagList) {
					if (listItem instanceof NamedElement) {
						NamedElement ne = (NamedElement)listItem;
						result.append(newLine + ne.getName());
					} else if (listItem instanceof EObject) {
						result.append(newLine + listItem.toString());
					}
				}
			}
			return result.toString() + newLine + "}";
		} else
			return "";
	}

	/**
	 * <p>Returns the name of the NamedElement and its owned elements.  Several
	 * nodes fall into this category; for instance, Packages usually have Classes,
	 * Classes and Interfaces usually have Properties and Operations, and so forth.
	 * <p>I refactored all of that "print name and process the owned elements" code here. 
	 * @param object the NamedElement
	 * @return String with the name of the NamedElement and its owned elements
	 * on separate lines.
	 */
	private String getNameAndOwnedElements(NamedElement object) {
		return getNameAndOwnedStuff(object, object.getOwnedElements());
	}

	/**
	 * <p>Returns the name of the NamedElement and the "stuff" it owns. 
	 * <p>I refactored this code when it became obvious that I was doing similar things
	 * with several node types--Classes, Interfaces, and Packages own other Elements, 
	 * and Operations own Parameters, for instance--and I wanted to handle those things
	 * the same way.
	 * @param object NamedElement being checked.
	 * @param ownedList List of the "stuff" owned by the NamedElement.
	 * @return Multi-line string w/ name of NamedElement and the stuff it owns.
	 */
	private String getNameAndOwnedStuff(NamedElement object, EList ownedList) {
		StringBuilder reply = new StringBuilder(object.getName() + " {");
		for (Object ownedElement : ownedList) {
			if (ownedElement instanceof Element) {
				reply.append(newLine + this.doSwitch((Element)ownedElement));
			}
		}
		return reply.toString();
	}


	/**
	 * Default "if all else fails" handler for nodes that aren't covered elsewhere.
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#defaultCase(org.eclipse.emf.ecore.EObject)
	 * @param object UML node being checked
	 * @return "No handler defined" message for the node. 
	 */
	@Override
	public String defaultCase(EObject object) {
		return "No handler defined for " + object.toString();
	}

	/**
	 * Returns the name of this NamedElement and the elements it owns.  Most UML node types 
	 * implement NamedElement, so this method tends to catch most nodes that aren't covered
	 * elsewhere.
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseNamedElement(org.eclipse.uml2.uml.NamedElement)
	 * @param object
	 * @return
	 */
	@Override
	public String caseNamedElement(NamedElement object) {
		return nodeHeader(object, "Named Element") + getNameAndOwnedElements(object) 
		+ nodeTrailer(object);
	}

	/**
	 * Returns the name of this Class and the elements it owns (usually Properties and
	 * Operations).
	 * 
	 * 
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseClass(org.eclipse.uml2.uml.Class)
	 * @param object
	 * @return
	 */
	@Override
	public String caseClass(Class object) {

		return nodeHeader(object, "Class") + getNameAndOwnedElements(object)
		+ nodeTrailer(object);
	}

	/**
	 * Returns the aspect-related type if applicable ("<<Aspect>>", "<<Advice>>" etc.) 
	 * followed by the typeString, followed by a space.
	 * @param object UML node being checked.
	 * @param typeString type of UML node ("Package", "Class", etc.).
	 * @return See above
	 */
	private String nodeHeader(Element object, String typeString) {
		StringBuilder aspectType = new StringBuilder("");
		if (object.isStereotypeApplied(aspectStereotype)) {
			aspectType.append("<<Aspect>> ");
		} else if (object.isStereotypeApplied(staticCrossCuttingFeatureStereotype)) {
			aspectType.append("<<StaticCrossCuttingFeature>> ");
		} else if (object.isStereotypeApplied(pointCutStereotype)) {
			aspectType.append("<<PointCut>> ");
		} else if (object.isStereotypeApplied(adviceStereotype)) {
			aspectType.append("<<Advice>> ");
		} 
		return aspectType + typeString + " ";
	}

	/**
	 * Returns the name of this Interface and the elements it owns (usually Properties and
	 * Operations).
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseInterface(org.eclipse.uml2.uml.Interface)
	 * @param object
	 * @return
	 */
	@Override
	public String caseInterface(Interface object) {
		return nodeHeader(object, "Interface") + getNameAndOwnedElements(object)
		+ nodeTrailer(object);
	}

	/**
	 * Returns the name of this Operation and its parameters.
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseOperation(org.eclipse.uml2.uml.Operation)
	 * @param object
	 * @return
	 */
	@Override
	public String caseOperation(Operation object) {
		return nodeHeader(object, "Operation") 
		+ getNameAndOwnedStuff(object,object.getOwnedParameters())
		+ nodeTrailer(object);
		//		StringBuilder reply = new StringBuilder("Operation " + object.getName() + " {");
		//		for (Parameter parm : object.getOwnedParameters()) {
		//			reply.append(newLine + this.doSwitch(parm));			
		//		}
		//		return reply.toString() + "}";
	}

	/**
	 * Returns the name and type of this Property.
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseProperty(org.eclipse.uml2.uml.Property)
	 * @param object
	 * @return
	 */
	@Override
	public String caseProperty(Property object) {
		return nodeHeader(object, "Property") + getNameAndType(object) + nodeTrailer(object);
	}

	/**
	 * Returns the name and type of this Parameter.
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseParameter(org.eclipse.uml2.uml.Parameter)
	 * @param object
	 * @return
	 */
	@Override
	public String caseParameter(Parameter object) {
		return nodeHeader(object, "Parameter") + getNameAndType(object) + nodeTrailer(object);
	}

	/**
	 * Returns the name and type of this TypedElement.  TypedElements include Properties
	 * and Parameters.
	 * @param object
	 * @return
	 */
	private String getNameAndType(TypedElement object) {
		Type theType = object.getType();
		if (theType != null) {
			return object.getName() + " {(" + doSwitch(theType) + ")";
		} else {
			return object.getName() + " {(null)";
		}
	}

	/**
	 * Returns the Interface realized by this Interface Realization (i.e. 
	 * <code>implements IInterface</code>).
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseInterfaceRealization(org.eclipse.uml2.uml.InterfaceRealization)
	 * @param object
	 * @return
	 */
	@Override
	public String caseInterfaceRealization(InterfaceRealization object) {
		return "Interface Realization for " + object.getName();
	}

	/**
	 * Returns the name of this Type.
	 * @see org.eclipse.uml2.uml.util.UMLSwitch#caseType(org.eclipse.uml2.uml.Type)
	 * @param object
	 * @return
	 */
	@Override
	public String caseType(Type object) {
		return "Type " + object.getName();
	}

}
