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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationship;
import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.core.model.AJRelationshipType;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.StructuredClassifier;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLFactory;
import org.topcased.java.reverse.Java2UMLConverter;

/**
 * Extends Topcased's Java2UMLConverter to convert AspectJ and Java to UML.
 * 
 * @author Jeff Koch
 *
 */
public class AspectJ2UMLConverter extends Java2UMLConverter {

	AspectJUmlProfile profile;
	AJProjectModelFacade ajModel;
	/**
	 * This contains the map between IJavaElements and UML elements.
	 * I wrapped the map inside this (and intentionally called it "Info"
	 * instead of "Map") so I could override the normal Map behavior of
	 * allowing the Value for a Key to be changed.  (That's also why
	 * I called the method addNodeToInfo instead of the normal Map-like
	 * name of "add()".)
	 * 
	 * @author Jeff Koch
	 *
	 */
	private class Aj2UmlInfo {
		Map<String, NamedElement> modelMap;

		/**
		 * 
		 */
		protected Aj2UmlInfo() {
			modelMap = new HashMap<String, NamedElement>();

		}

		/**
		 * If no UML node has been associated with the IJavaElement, associates this NamedElement
		 * with the IJavaElement.
		 * @param jElement element from the AspectJ/Java Model
		 * @param umlType element from the UML Model
		 */
		void addNodeToInfo(IJavaElement jElement, NamedElement umlNode) {
			String typeHandle = jElement.getHandleIdentifier();
			if (!modelMap.containsKey(typeHandle)) {
				modelMap.put(typeHandle, umlNode);
				//				System.err.println("Map update: " + typeHandle + " to " + umlNode.getName());
			}
		}

		NamedElement get(String key) {
			//			return modelMap.get(key);
			//Stop when we find a match, or when we run out of string.
			NamedElement umlElement = null;
			//			StringBuilder curKey = new StringBuilder(key);
			//			while ((umlElement == null) && (curKey.length() > 0)) {
			//				umlElement = modelMap.get(curKey.toString());
			//				if (umlElement == null) {
			//					curKey.deleteCharAt(curKey.length()-1);
			//				}
			//			}
			for (StringBuilder curKey = new StringBuilder(key); 
			(umlElement == null) && (curKey.length() > 0); 
			curKey.deleteCharAt(curKey.length()-1)) {
				umlElement = modelMap.get(curKey.toString());
			}	



			return umlElement;
		}

	}


	private Aj2UmlInfo a2uInfo;  

	/**
	 * @see edu.utdallas.fdaf.org.topcased.java.reverse.AbstractJava2UMLConverter#convert(org.eclipse.jdt.core.IJavaElement, org.eclipse.emf.ecore.resource.Resource)
	 * @param javaElement
	 * @param pEmfResource
	 * @return
	 * @throws CoreException
	 */
	@Override
	public Package convert(IJavaElement javaElement, Resource pEmfResource)
	throws CoreException {
		// TODO Auto-generated method stub
		return super.convert(javaElement, pEmfResource);
	}






	/**
	 * Initialise the created types and populated them with operation properties and inner types.
	 * 
	 * This code modifies Java2UMLConverter.initializeTypes() to handle AspectJ elements.
	 */
	@Override
	protected void initializeTypes(Package packageObject, IPackageFragment fragment) throws JavaModelException

	{
		ajModel = AJProjectModelFactory.getInstance().getModelForJavaElement(fragment);
		for (IJavaElement javaElement : fragment.getChildren())
		{
			if (javaElement instanceof ICompilationUnit)
			{
				ICompilationUnit unit = (ICompilationUnit) javaElement;
				//				AJParseTree tree = new AJParseTree(unit);
				for (IType type : unit.getAllTypes())
				{
					String typeName = type.getElementName();
					NamedElement element = packageObject.getOwnedMember(typeName);
					if (element == null && type.getParent() != null)
					{
						element = findInnerType(packageObject, type, typeName);
					}

					if (element instanceof Interface)
					{
						Interface interfaceObject = (Interface) element;

						// Super interfaces
						for (String interfaceName : type.getSuperInterfaceNames())
						{
							Type interfaceType = findType(packageObject, interfaceName);
							if (interfaceType == null && type.getParent() != null)
							{
								interfaceType = findInnerType(packageObject, type, typeName);
							}

							if (interfaceType != null && interfaceType instanceof Classifier)
							{
								interfaceObject.createGeneralization((Classifier) interfaceType);
							}
						}

						// Inner objects
						createProperties(type, interfaceObject);
						createOperations(type, interfaceObject);
					}
					else if (element instanceof Enumeration)
					{
						Enumeration enumeration = (Enumeration) element;

						for (String interfaceName : type.getSuperInterfaceNames())
						{
							Type interfaceType = findType(packageObject, interfaceName);
							if (interfaceType == null)
							{
								interfaceType = findGenericType(packageObject, interfaceName);
							}

							if (interfaceType == null && type.getParent() != null)
							{
								interfaceType = findInnerType(packageObject, type, typeName);
							}

							if (interfaceType != null && interfaceType instanceof Interface)
							{
								Classifier classifier = (Classifier) interfaceType;
								if (enumeration.getGeneralization(classifier) == null)
								{
									enumeration.createGeneralization(classifier);
								}
							}
						}
						// Inner objects
						createProperties(type, enumeration);
						createOperations(type, enumeration);

					}
					else if (element instanceof Class)
					{
						Class classObject = (Class) element;
						// Super Interfaces
						for (String interfaceName : type.getSuperInterfaceNames())
						{
							Type interfaceType = findType(packageObject, interfaceName);
							if (interfaceType == null)
							{
								interfaceType = findGenericType(packageObject, interfaceName);
							}

							if (interfaceType == null && type.getParent() != null)
							{
								interfaceType = findInnerType(packageObject, type, typeName);
							}

							if (interfaceType != null && interfaceType instanceof Interface)
							{
								Interface interf = (Interface) interfaceType;

								if (classObject.getInterfaceRealization(interfaceName, interf) == null)
								{
									classObject.createInterfaceRealization(interfaceName, interf);
								}
							}
						}

						// Generalization
						String superClassName = type.getSuperclassName();
						Type classType = findType(packageObject, superClassName);
						if (classType == null && type.getParent() != null)
						{
							classType = findInnerType(packageObject, type, typeName);
						}

						if (classType == null)
						{
							classType = findOrCreateType(packageObject, superClassName);
						}

						if (classType != null && classType instanceof Classifier)
						{
							Classifier classifier = (Classifier) classType;
							// Create generalization only if it is needed
							if (classObject.getGeneralization(classifier) == null)
							{
								classObject.createGeneralization(classifier);
							}
						}
						/*
						 * Eventually, here's what I need to do.  I need to find the 
						 * ProgramElement that corresponds to the class; if it's an
						 * Aspect, then plug in the Aspect stereotype. (For now, I'll
						 * hand-wave it by sticking in a comment to syserr.)
						 * 
						 * Don't know if I'll try to handle the intertype stuff here,
						 * or if I'll push that down into createProperties/createOperations.
						 * Probably the former, which means I wouldn't call createProperties
						 * or createOperations at all for Aspects.
						 */
						IProgramElement ajElement = javaElementToProgramElement(type);
						if (ajElement.getKind().toString().equals("aspect")) {
							System.err.println("aspect found:  javaElement " + typeName 
									+ " (handle " + type.getHandleIdentifier() 
									+ "), programElement " + ajElement.getName()
									+ ", UML object = " + classObject.getQualifiedName());
							Stereotype aspectST = profile.getStereotype("Aspect");
							try {
								classObject.applyStereotype(aspectST);
							} catch (Exception e) {
								System.err.println("Exception attempting to apply Aspect stereotype to " + typeName + ":");
								e.printStackTrace();
							}
							//Extract elements from the Program Element
							createAjFeatures(type, ajElement, classObject);




						} else {

							// Inner objects
							createProperties(type, classObject);
							createOperations(type, classObject);
						}
					}
				}
			}
		}
		addAspectRelationships(ajModel, packageObject, fragment);
	}

	private IProgramElement javaElementToProgramElement(IType type) {
		return ajModel.javaElementToProgramElement(type);
	}

	private IJavaElement programElementToJavaElement(IProgramElement element) {
		return ajModel.programElementToJavaElement(element);
	}

	/**
	 * Converts an AspectJ (IProgramElement) handle to its Java (IJavaElement)
	 * counterpart.
	 * @param ajHandle AspectJ Model (IProgramElement) handle
	 * @return the Java Model (IJavaElement) counterpart to ajHandle
	 */
	private String ajHandleToJavaHandle(String ajHandle) {
		IJavaElement jElement = ajModel.programElementToJavaElement(ajHandle);
		return jElement.getHandleIdentifier();

	}


	/**
	 * Creates the features (StaticCrossCuttingFeatures, Advices, and PointCuts) for
	 * the aspect. 
	 * @param jType Java Model node for the aspect.
	 * @param ajElement ProgramElement for the aspect.
	 * @param classifier UML node for the aspect
	 * @throws JavaModelException 
	 */
	private void createAjFeatures(IType jType, IProgramElement ajElement, Class classifier) throws JavaModelException {

		List children = ajElement.getChildren();
		for (Object object : children) {
			if (object instanceof IProgramElement) {
				IProgramElement child = (IProgramElement) object;
				printChildInfo(ajElement, child);
				String childType = child.getKind().toString();
				if ((childType.equals("inter-type field")) || (childType.equals("inter-type constructor"))) {
					Property intertypeField = addAspectProperty(classifier,
							child);
					applyStereotype(intertypeField, "StaticCrossCuttingFeature");
				} else if (childType.equals("inter-type method")) {
					Operation intertypeMethod = addAspectOperation(classifier,
							child);
					applyStereotype(intertypeMethod, "StaticCrossCuttingFeature");

				} else if (childType.equals("inter-type parent")) {
					//TODO handle inter-type parent
				} else if (childType.equals("pointcut")) {
					/*
					 * Evermann defines Pointcut as an abstract stereotype 
					 * that's extended by specific pointcut types 
					 * (AdviceExecutionPointCut, OperationPointCut,
					 * PointCutPointCut, etc).  IProgramElement doesn't
					 * specific pointcut types, so for now I'm making Pointcut
					 * non-abstract and lumping all pointcuts under that
					 * stereotype.
					 */
					Property pointcutProperty = addAspectProperty(classifier, child);
					applyStereotype(pointcutProperty, "PointCut");
				} else if (childType.equals("advice")) {
					/*
					 * Advice is a behavioral feature (Operation in our case).
					 */
					Operation adviceOp = addAspectOperation(classifier,
							child);
					applyStereotype(adviceOp, "Advice");
					//Now set adviceExecution: before/after/around
					setAdviceExecution(child, adviceOp);

				}
			}
		}
	}






	/**
	 * Sets the adviceExecution (before, after, around) for a newly created advice
	 * @param modelAdvice the advice as it appears in the AspectJ/Java Model
	 * @param umlAdvice the advice as it appears in the UML model
	 */
	private void setAdviceExecution(IProgramElement modelAdvice, Operation umlAdvice) {

		Stereotype stereotype = profile.getStereotype("Advice");

		String literalName;
		String adviceType = modelAdvice.getName();
		if (adviceType.contains("before")) {
			literalName = "BeforeAdvice";
		} else if (adviceType.contains("after")) {
			literalName = "AfterAdvice";
		} else {
			literalName = "AroundAdvice";
		}

		EnumerationLiteral literal = profile.getEnumLiteral("AdviceExecutionType", literalName);

		umlAdvice.setValue(stereotype, "adviceExecution", literal);
	}


	/**
	 * Applies a {@link Stereotype} to an {@link Element}.
	 * @param elementObject UML Element that needs the Stereotype
	 * @param stereotypeName Name of the Stereotype to be applied.
	 */
	private void applyStereotype(Element elementObject, String stereotypeName) {
		Stereotype st = profile.getStereotype(stereotypeName);
		/*
		 * OK, so I'm being lazy by printing to syserr instead of throwing
		 * a proper exception myself.  OTOH, this should (in theory) only
		 * happen in development, so this is a heads-up that I typo'd the
		 * stereotype name, and it'll show up right before elementObject.applyStereotype
		 * triggers the exception because st is null.
		 */
		if (st == null) {
			System.err.println("Couldn't find stereotype for " + stereotypeName);
		}
		try {
			elementObject.applyStereotype(st);
		} catch (Exception e) {
			System.err.println("Exception thrown while attempting to apply stereotype " 
					+ stereotypeName + " to Element " + elementObject.toString());
			e.printStackTrace();
		}
	}






	/**
	 * Creates a UML Property for the IProgramElement and adds it to the UML aspect.
	 * 
	 * The Property could be any aspect-related "property"--intertype fields,
	 * pointcuts, or the like.  The caller is responsible for adding the
	 * appropriate stereotype to the Property.
	 * 
	 * @param classifier UML Class representing an aspect
	 * @param child IProgramElement representing the property
	 * @return UML Property representing the property
	 * @throws JavaModelException
	 */
	private Property addAspectProperty(Class classifier, IProgramElement child)
	throws JavaModelException {
		//JWK:  Don't look for an existing aspect property
		//		Property propertyObject = findProperty(classifier, child.getName());
		//		if (propertyObject != null)
		//		{
		//			// Just update
		//			attachJavadoc(propertyObject, child);
		//		}
		//		else
		//		{
		//			// Creates a new one
		//			propertyObject = createProperty(classifier, child);
		//			classifier.getOwnedAttributes().add(propertyObject);
		//
		//			//processAnnotations(classifier.getNamespace(), type, propertyObject);
		//		}
		// Creates a new one
		Property propertyObject = createProperty(classifier, child);
		classifier.getOwnedAttributes().add(propertyObject);
		a2uInfo.addNodeToInfo(ajModel.programElementToJavaElement(child), propertyObject);

		return propertyObject;
	}

	//
	//	future aspect-related stuff:
	//		public static final Kind DECLARE_PARENTS = new Kind("declare parents");
	//		public static final Kind DECLARE_WARNING = new Kind("declare warning");
	//		public static final Kind DECLARE_ERROR = new Kind("declare error");
	//		public static final Kind DECLARE_SOFT = new Kind("declare soft");
	//		public static final Kind DECLARE_PRECEDENCE = new Kind("declare precedence");
	//				 * 
	//				 * 
	//				 * "Inside types" and other stuff I might implment later:
	//		public static final Kind CLASS = new Kind("class");
	//		public static final Kind INTERFACE = new Kind("interface");
	//		public static final Kind ASPECT = new Kind("aspect");
	//
	//	TBD:
	//		public static final Kind ENUM = new Kind("enum");
	//		public static final Kind ENUM_VALUE = new Kind("enumvalue");
	//		public static final Kind ANNOTATION = new Kind("annotation");
	//		public static final Kind INITIALIZER = new Kind("initializer");
	//		public static final Kind CONSTRUCTOR = new Kind("constructor");
	//		public static final Kind METHOD = new Kind("method");
	//		public static final Kind FIELD = new Kind("field");
	//		public static final Kind CODE = new Kind("code");
	//		public static final Kind ERROR = new Kind("error");
	//		public static final Kind DECLARE_ANNOTATION_AT_CONSTRUCTOR = new Kind("declare @constructor");
	//		public static final Kind DECLARE_ANNOTATION_AT_FIELD = new Kind("declare @field");
	//		public static final Kind DECLARE_ANNOTATION_AT_METHOD = new Kind("declare @method");
	//		public static final Kind DECLARE_ANNOTATION_AT_TYPE = new Kind("declare @type");
	//		public static final Kind SOURCE_FOLDER = new Kind("source folder");
	//		public static final Kind PACKAGE_DECLARATION = new Kind("package declaration");
	//				 */
	//




	/**
	 * Creates a UML Operation based on an IProgramElement, and
	 * adds it to the aspect Class.  This "operation" could be an intertype
	 * method, advice, or the like; it's up to the calling module to add the
	 * appropriate stereotype to the Operation.
	 * 
	 * @param classifier UML Class representing an aspect.
	 * @param child IProgramElement representing some operation.  This "operation"
	 * could be an intertype method, advice, or the like.
	 * @return the UML Operation created 
	 * @throws JavaModelException
	 */
	private Operation addAspectOperation(Class classifier, IProgramElement child)
	throws JavaModelException {
		/*
		 * JWK:  I was looking for an existing (UML) operation that
		 * matched the IProgramElement, but that messes up when I have
		 * identical aspect operations; for instance, one of our test systems
		 * have two identical advices in a row.  I'll see what happens
		 * if I disable that check.
		 */
		//		Operation operationObject = findOperation(classifier, child);
		//		if (operationObject != null)
		//		{
		//			// Just update the javadoc
		//			attachJavadoc(operationObject, child);
		//		}
		//		else
		//		{
		//			// Creates a new one
		//			operationObject = createOperation(classifier, child);
		//			classifier.getOwnedOperations().add(operationObject);
		//
		//		}
		Operation operationObject = createOperation(classifier, child);
		classifier.getOwnedOperations().add(operationObject);
		//	                processAnnotations(classifier.getNamespace(), child, operationObject);
		a2uInfo.addNodeToInfo(ajModel.programElementToJavaElement(child), operationObject);
		return operationObject;
	}

	private Operation createOperation(Class classifier, IProgramElement child) throws JavaModelException {

		//	    protected Operation createOperation(Element element, IMethod method) throws JavaModelException
		//	    {
		String methodName = child.getName();

		Operation operationObject = UMLFactory.eINSTANCE.createOperation();
		operationObject.setName(methodName);

		update(operationObject, child.getRawModifiers());

		int flags = child.getRawModifiers();
		operationObject.setIsAbstract(Flags.isAbstract(flags));
		operationObject.setIsStatic(Flags.isStatic(flags));

		attachJavadoc(operationObject, child);

		createParameters(classifier, operationObject, child);

		//JWK:  For now, I'm assuming that the child will exist and won't be a Constructor of some sort.
		//		if (child.exists() && !child.isConstructor())
		//		{
		// TEMP		createTemplateParameters(child, operationObject);

		//		String returnTypeSig = child.getReturnType();
		//
		//		String returnTypeWithoutArray = Signature.getElementType(returnTypeSig);
		//		String returnTypeName = Signature.toString(Signature.getTypeErasure(returnTypeWithoutArray));
		String returnTypeName = child.getCorrespondingType(true);
		Type returnType = findTemplateParameter(classifier, returnTypeName);
		if (returnType == null)
		{
			returnType = findTemplateParameter(operationObject, returnTypeName);
		}

		if (returnType == null)
		{
			returnType = findOrCreateType(classifier.getNearestPackage(), returnTypeName);
		}

		// Type returnType = findOrCreateType(element.getNearestPackage(),
		// returnTypeName);
		if (returnType != null)
		{
			operationObject.createReturnResult("return", returnType);
		}
		//		}

		return operationObject;

	}

	//	private void createTemplateParameters(IProgramElement child,
	//			Operation operationObject) {
	////	    private void createTemplateParameters(IMethod method, Operation operationObject) throws JavaModelException
	////	    {
	//	        ITypeParameter[] typeParameters = child.getTypeParameters();
	//	        for (ITypeParameter typeParameter : typeParameters)
	//	        {
	//	            TemplateSignature templateSignature = operationObject.getOwnedTemplateSignature();
	//	            // FIXME See how to update the template signature
	//	            if (templateSignature == null)
	//	            {
	//	                templateSignature = operationObject.createOwnedTemplateSignature();
	//	                ClassifierTemplateParameter classifierTemplateParameter = UMLFactory.eINSTANCE.createClassifierTemplateParameter();
	//	                templateSignature.getOwnedParameters().add(classifierTemplateParameter);
	//	                ParameterableElement parameteredElement = classifierTemplateParameter.createOwnedParameteredElement(UMLPackage.Literals.CLASS);
	//	                if (parameteredElement instanceof Class)
	//	                {
	//	                    Class clazz = (Class) parameteredElement;
	//	                    clazz.setName(typeParameter.getElementName());
	//	                }// end of if (parameteredElement instanceof Class)
	//	            }
	//	        }
	//	    }


	private void createParameters(Class classifier, Operation operationObject,
			IProgramElement child) {
		//	    protected void createParameters(Element element, Operation operation, IMethod method) throws JavaModelException
		//	    {
		List<?> paramNameList = child.getParameterNames();
		//If there weren't any parm names, don't create any parms
		if (paramNameList == null) {
			return;
		}
		Object[] paramNames = paramNameList.toArray();
		Object[]  paramTypeSigs = child.getParameterTypes().toArray();
		/*
		 * child.getParameterNames is List<String>, but child.getParameterNames is
		 * actually List<char[]> or something equally bizarre.  Which is why the
		 * typeSigObj-to-typeSig conversion is so weird. 
		 */
		for (int i = 0; i < paramNames.length; i++)
		{
			Object nameObj = paramNames[i];
			Object typeSigObj = paramTypeSigs[i];

			if (nameObj instanceof String) {

				String name = (String)nameObj;
				char[] typeSigArr = (char[])typeSigObj;
				String typeName = String.valueOf(typeSigArr);
				//				String typeSig = String.valueOf(typeSigArr);
				//
				Parameter parameter = UMLFactory.eINSTANCE.createParameter();
				parameter.setName(name);
				//
				//				String typeWithoutArray = Signature.getElementType(typeSig);
				//				String typeName = Signature.toString(Signature.getTypeErasure(typeWithoutArray));

				Type paramType = findTemplateParameter(classifier, typeName);
				if (paramType == null)
				{
					paramType = findOrCreateType(classifier.getNearestPackage(), typeName);
				}
				if (paramType != null)
				{
					parameter.setType(paramType);
				}

				operationObject.getOwnedParameters().add(parameter);
			}

			//	            processAnnotations(classifier.getNearestPackage(), child, parameter);
		}

	}

	private Operation findOperation(StructuredClassifier classifier,
			IProgramElement child) {
		//	    protected Operation findOperation(Classifier classifier, IMethod method)
		//	    {
		for (Operation op : classifier.getOperations())
		{
			if (op.getOwner().equals(classifier) && op.getName().equals(child.getName()))
			{
				//	                try
				//	                {
				List lParamNames = child.getParameterNames();
				//				if ((lParamNames == null) || (lParamNames.size() == 1)) 
				//				{
				//					if (op.getOwnedParameters().size() == 1)
				//					{
				//						return op;
				//					}
				//				}
				if (lParamNames == null) 
				{
					if (op.getOwnedParameters().size() == 0)
					{
						return op;
					}
				}
				else if (lParamNames.size() == 1) 
				{
					if (op.getOwnedParameters().size() == 1)
					{
						return op;
					}
				}
				else
				{
					Object[] aParamNames = lParamNames.toArray();
					int i = 0;
					int paramCount = 0;
					for (Parameter param : op.getOwnedParameters())
					{
						if (!param.getName().equals("return"))
						{
							if (i < aParamNames.length && param.getName().equals(aParamNames[i].toString()))
							{
								paramCount++;
							}
							i++;
						}
					}
					if (paramCount == aParamNames.length)
					{
						return op;
					}
				}
				//	                }
				//	                catch (JavaModelException e)
				//	                {
				//	                    e.printStackTrace();
				//	                }
			}
		}
		return null;

	}

	/**
	 * @param propertyObject
	 * @param child
	 * @throws JavaModelException
	 */
	private void attachJavadoc(Element elementObject, IProgramElement child)
	throws JavaModelException {
		IJavaElement javaChild = programElementToJavaElement(child);
		if (javaChild instanceof IMember) {

			attachJavadoc(elementObject, (IMember)javaChild);
		}
	}




	private Property createProperty(Element element,
			IProgramElement field) throws JavaModelException {

		String fieldName = field.getName();
		//		String fieldTypeSig = field.getCorrespondingType();
		//
		//		String fieldTypeWithoutArray = Signature.getElementType(fieldTypeSig);
		//		String fieldTypeName = Signature.toString(Signature.getTypeErasure(fieldTypeWithoutArray));
		String fieldTypeName = field.getCorrespondingType(true);
		Type fieldType = findTemplateParameter(element, fieldTypeName);
		if (fieldType == null)
		{
			fieldType = findOrCreateType(element.getNearestPackage(), fieldTypeName);
		}

		Property propertyObject = UMLFactory.eINSTANCE.createProperty();
		//	        if (isGenericType(fieldType))
		//	        {
		//	            // TODO : find the explicit type and define a template parameter
		//	            // extractGenericTypesFromCurrentSignature(fieldTypeSig);
		//	        }
		propertyObject.setName(fieldName);
		if (fieldType != null)
		{
			propertyObject.setType(fieldType);
		}
		attachJavadoc(propertyObject, field);

		//BOOKMARK
		//JWK:  I'm hoping that getRawModifiers() = getFlags for IField.
		update(propertyObject, field.getRawModifiers());


		//		// No need to check the null (done with instanceof which returns always false)
		//		if (field.getConstant() instanceof String)
		//		{
		//			LiteralString defaultValue = (LiteralString) propertyObject.createDefaultValue("", propertyObject.getType(), UMLFactory.eINSTANCE.createLiteralString().eClass());
		//			defaultValue.setValue((String) field.getConstant());
		//		}
		//		else if (field.getConstant() instanceof Integer)
		//		{
		//			LiteralInteger defaultValue = (LiteralInteger) propertyObject.createDefaultValue("", propertyObject.getType(), UMLFactory.eINSTANCE.createLiteralInteger().eClass());
		//			defaultValue.setValue((Integer) field.getConstant());
		//		}
		//		else if (field.getConstant() instanceof Boolean)
		//		{
		//			LiteralBoolean defaultValue = (LiteralBoolean) propertyObject.createDefaultValue("", propertyObject.getType(), UMLFactory.eINSTANCE.createLiteralBoolean().eClass());
		//			defaultValue.setValue((Boolean) field.getConstant());
		//		}

		return propertyObject;



	}

	/**
	 * @param ajElement
	 * @param child
	 */
	private void printChildInfo(IProgramElement ajElement, IProgramElement child) {
		String childType = child.getKind().toString();
		String childHandle = child.getHandleIdentifier();
		IJavaElement jChild = programElementToJavaElement(child);
		String jChildHandle = jChild.getHandleIdentifier();
		//BEGIN TEMPORARY STUFF
		System.err.println(ajElement.getName() + " " + childType + " " + child.getName());
		//		System.out.println("   bytecode name:" + child.getBytecodeName());
		//		System.out.println("   bytecode sig:" + child.getBytecodeSignature());
		//		System.out.println("   corresponding type:" + child.getCorrespondingType(false));
		//		System.out.println("   fully qual'd corresponding type:" + child.getCorrespondingType(true));
		//		System.out.println("   declaring type:" + child.getDeclaringType());
		//		System.out.println("   details:" + child.getDetails());
		//		System.out.println("   formal comment:" + child.getFormalComment());
		System.err.println("   Program Element handle:" + childHandle);
		System.err.println("   Java Element handle:" + jChildHandle);
		//		System.out.println("   package name:" + child.getPackageName());
		//		System.out.println("   raw modifiers:" + child.getRawModifiers());
		//		System.out.println("   source signature:" + child.getSourceSignature());
		//		printList(child.getParameterNames(), "parm names");
		//		printList(child.getParameterSignatures(), "parm sigs");
		//		printList(child.getParameterSignaturesSourceRefs(), "parm sig source refs");
		//		printList(child.getParameterTypes(), "parm types");
		//		printList(child.getChildren(), "children");
		//END TEMPORARY STUFF
	}

	/**
	 * @param theList
	 * @param header
	 */
	private void printList(List theList, String header) {
		if (theList == null) {
			System.err.println("   " + header + ": NULL");
		} else {
			System.err.println("   " + header + ":");
			if (theList.size() == 0) {
				System.err.println("      Empty set");
			} else {
				for (Object object2 : theList) {
					if (object2 instanceof String) {
						System.err.println("      " + (String)object2);
					} else {
						System.err.println("      " + object2.toString());
					}
				}
			}
		}
	}

	/**
	 * @see org.topcased.java.reverse.Java2UMLConverter#initializeModel(org.eclipse.uml2.uml.Package)
	 * @param model
	 * @throws CoreException
	 */
	@Override
	protected void initializeModel(Package model) throws CoreException {

		profile = new AspectJUmlProfile();
		model.applyProfile(profile.getAspectJProfile());
		a2uInfo = new Aj2UmlInfo();

	}






	/**
	 * @see org.topcased.java.reverse.Java2UMLConverter#createTypeInPackage(org.eclipse.uml2.uml.Namespace, org.eclipse.jdt.core.IType)
	 * @param packageObject
	 * @param type
	 * @return
	 * @throws JavaModelException
	 */
	@Override
	protected Classifier createTypeInPackage(Namespace packageObject, IType type)
	throws JavaModelException {
		Classifier umlType = super.createTypeInPackage(packageObject, type);
		a2uInfo.addNodeToInfo(type, umlType);
		return umlType;
	}


	/**
	 * @see org.topcased.java.reverse.AbstractJava2UMLConverter#createOperation(org.eclipse.uml2.uml.Element, org.eclipse.jdt.core.IMethod)
	 * @param element
	 * @param method
	 * @return
	 * @throws JavaModelException
	 */
	@Override
	protected Operation createOperation(Element element, IMethod method)
	throws JavaModelException {
		Operation umlOp = super.createOperation(element, method);
		a2uInfo.addNodeToInfo(method, umlOp);
		return umlOp;
	}






	/**
	 * @see org.topcased.java.reverse.AbstractJava2UMLConverter#createProperty(org.eclipse.uml2.uml.Element, org.eclipse.jdt.core.IField)
	 * @param element
	 * @param field
	 * @return
	 * @throws JavaModelException
	 */
	@Override
	protected Property createProperty(Element element, IField field)
	throws JavaModelException {
		Property umlProp = super.createProperty(element, field);
		a2uInfo.addNodeToInfo(field, umlProp);
		return umlProp;
	}



	/**
	 * @see org.topcased.java.reverse.AbstractJava2UMLConverter#findOrCreatePackage(org.eclipse.uml2.uml.Package, org.eclipse.jdt.core.IPackageFragment)
	 * @param model
	 * @param fragment
	 * @return
	 */
	@Override
	protected Package findOrCreatePackage(Package model,
			IPackageFragment fragment) {
		Package umlPackage =  super.findOrCreatePackage(model, fragment);
		a2uInfo.addNodeToInfo(fragment, umlPackage);
		return umlPackage;
	}

	/**
	 * Adds the model's aspect-related relationships to the Package.
	 * 
	 * @param model the AJ Model facade
	 * @param packageObject the UML Package
	 * @param fragment the AspectJ/Java model package associated with packageObject
	 */
	void addAspectRelationships(AJProjectModelFacade model, Package packageObject, IPackageFragment fragment) {
		// You can narrow this to include on certain kinds of relationships
		AJRelationshipType[] relsTypes = AJRelationshipManager.getAllRelationshipTypes();

		// check first to see if there is a model
		// will return false if there has not yet been a successful build of this project
		if (model.hasModel()) {

			// all relationships for project
			// can also query for relationships on individual elements or compilation units
			List<IRelationship> rels = model.getRelationshipsForProject(relsTypes);
			for (IRelationship rel : rels) {
				// Source is an IProgramElement handle, which refers to a piece of the program in AspectJ's World
				String sourceJHandle = ajHandleToJavaHandle(rel.getSourceHandle());
				NamedElement sourceElement = a2uInfo.get(sourceJHandle);


				for (String targetHandle : rel.getTargets()) {

					String targetJHandle = ajHandleToJavaHandle(targetHandle);
					NamedElement targetElement = a2uInfo.get(targetJHandle);
					try {
						System.err.println("Calling addAspectRelationship - rel = " + rel.getName());
						System.err.println("==>Source:  jHandle=" + sourceJHandle 
								+ ", sourceElement=" + sourceElement.getQualifiedName() 
								+ " (class " + sourceElement.getClass().getName() + ")");
						System.err.println("==>Target:  jHandle=" + targetJHandle 
								+ ", targetElement=" + targetElement.getQualifiedName()
								+ " (class " + targetElement.getClass().getName() + ")");

						addAspectRelationship(rel, sourceElement, targetElement);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						System.err.println("Exception thrown by addAspectRelationship");
						System.err.println("***rel = " + rel.getName());
						System.err.println("***Source:  jHandle=" + sourceJHandle + ", sourceElement=" + sourceElement.getQualifiedName());
						System.err.println("***Target:  jHandle=" + targetJHandle + ", targetElement=" + targetElement.getQualifiedName());
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Adds an aspect-related relationship from the sourceElement to the targetElement.
	 * The relationship is modeled as a attribute to the sourceElement's stereotype, 
	 * using the targetElement as the attribute's value.
	 * 
	 * @param rel the aspect-related relationship from the AspectJ/Java Model
	 * @param sourceElement UML Element containing the stereotype w/ the relationship
	 * @param targetElement UML Element to be used as the relationship's value
	 */
	private void addAspectRelationship(IRelationship rel,
			NamedElement sourceElement, NamedElement targetElement) {
		/*
		 * 					 * If rel.getName() is "advises"
		 * then make sure sourceElement is Advice, and set up sourceElement.advisedElement
		 * else if rel.getName() is "declared on"
		 * then make sure sourceElement is StaticCrossCuttingFeature, and set up sourceElement.onType
		 * 

		 */
		/*
		 * I suspect the "add" property would do something like this:
		 *  * get the current property value
		 *  * (if there isn't one, create a new empty list of whatevers)
		 *  * add the targetElement
		 *  * set the property to the newly-revised list
		 */
		if (rel.getName().equals("advises")) {
			addStereotypeProperty("Advice", "advisedElement", sourceElement, targetElement);
		} else if (rel.getName().equals("declared on")) {
			System.err.println("StaticCrossCuttingFeature " + sourceElement.getQualifiedName() + " declared on " + targetElement.getQualifiedName());
			addStereotypeProperty("StaticCrossCuttingFeature", "onType", sourceElement, targetElement);
		}

	}

	/**
	 * Adds targetElement as the specified property for the specified stereotype, provided
	 * that the stereotype has been applied to the sourceElement.  Does nothing if the 
	 * specified stereotype hasn't been applied to the sourceElement.
	 *  
	 * @param stereotypeName Name of the stereotype
	 * @param propertyName Name of the property to be set
	 * @param sourceElement Element to which property should be applied 
	 * @param targetElement New value of the property
	 */
	private void addStereotypeProperty(String stereotypeName, String propertyName,
			NamedElement sourceElement, NamedElement targetElement) {

		Stereotype stereotype = profile.getStereotype(stereotypeName);
		if (sourceElement.isStereotypeApplied(stereotype)) {
			Object propertyObj = sourceElement.getValue(stereotype, propertyName);
			List<NamedElement> propertyList;
			if (propertyObj instanceof List) {
				propertyList = (List<NamedElement>)propertyObj;
			} else {
				propertyList = new ArrayList<NamedElement>();
			}
			propertyList.add(targetElement);

			//			sourceElement.setValue(stereotype, propertyName, propertyList);
		}



	}






	private String printInfoOn(IProgramElement element) {
		String info = printNameTypeOf(element);
		IProgramElement parent = element.getParent();
		if (parent != null) {
			info += ": child of " + printInfoOn(parent);
		} else {
			info += ": childless";
		}
		return info;
	}

	/**
	 * Returns the name and type of an IJavaElement.
	 * @param element IJavaElement being examined
	 * @return <i>element-name</i>(<i>element-type</i>)
	 */
	private String printNameTypeOf(IProgramElement element) {
		return element.getName() + "(" + element.getKind().toString() + ")[" + element.getHandleIdentifier() + "]";
	}


}
