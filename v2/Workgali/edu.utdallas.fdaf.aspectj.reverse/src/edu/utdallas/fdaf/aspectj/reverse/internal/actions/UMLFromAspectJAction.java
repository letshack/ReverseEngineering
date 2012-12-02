/*******************************************************************************
 * Copyright (c) 2005 Anyware Technologies.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors to UMLFromJavaAction:
 *    David Sciamma (Anyware Technologies) - initial API and implementation
 * Contributors to UMLFromAspectJAction:
 *    Jeffrey Koch  - ported from UMLFromJavaAction, tweaked for AspectJ2Java.  
 *                   I Am Not A Lawyer, but since this is a modified and renamed
 *                   version of UMLFromJavaAction.java, I think its copyright 
 *                   follows it.
 *******************************************************************************/

package edu.utdallas.fdaf.aspectj.reverse.internal.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWizard;

import edu.utdallas.fdaf.aspectj.reverse.internal.Activator;
import edu.utdallas.fdaf.aspectj.reverse.internal.wizards.AspectJ2UMLWizard;


/**
 * This action launch the convertion wizard.
 * 
 * @author <a href="david.sciamma@anyware-tech.com">David Sciamma</a>
 */
public class UMLFromAspectJAction implements IObjectActionDelegate
{

	private List<IJavaElement> javaElements;

	private IWorkbenchWindow window;

	/**
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction,
	 *      org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart)
	{
		window = targetPart.getSite().getWorkbenchWindow();
	}

	    /**
	     * Return the container associated with the selected IJavaElement
	     * @return the initial container
	     */
	    protected IContainer getInitialContainer()
	    {
	        IContainer container = null;
	        if (javaElements.size() > 0)

	        {
	        	IJavaElement javaElement = javaElements.get(0);
	            try
	            {
	                container = (IContainer) javaElement.getCorrespondingResource();
	            }
	            catch (JavaModelException e)
	            {
	                Activator.log(e);
	//                ReversePlugin.log(e);
	            }
	        }
	        
	        return container;
	    }

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action)
	{
		if (window == null)
		{
			return;
		}

		Shell shell = window.getShell();

		
//		AspectJReporter reporter = new AspectJReporter();
//		try {
//			File umlFile = reporter.report(javaElements);
//			MessageDialog.openInformation(shell, "UML Model Created", "UML model created as " + umlFile.getAbsolutePath() + ".");
//			File modelFile = AbstractionEngine.generateModel(umlFile);
//			MessageDialog.openInformation(shell, "Architecture Model Created", "Architecture model created as " + modelFile.getAbsolutePath() + ".");
//		} catch (IOException e) {
//			String title = "Error";
//			String message = "An IO error occured during UML model generation.\n"
//				+ "See error logs for more details.";
//			MessageDialog.openError(shell, title, message);
//		} catch (CoreException e) {
//			String title = "Error";
//			String message = "An error occured during UML model generation.\n"
//				+ "See error logs for more details.";
//
//			ErrorDialog.openError(shell, title, message, e.getStatus());
//		} catch (BuildNeededException e) {
//			String title = "Error";
//			MessageDialog.openError(shell, title, "Need to build project.\n"
//					+ e.getMessage());
//		} catch (ModelPrologLoadException e) {
//			String title = "Error";
//			String message = "Unable to load Prolog rules for model generation.\n"
//				+ "See error logs for more details.";
//			MessageDialog.openError(shell, title, message);
//			e.printStackTrace();
//		} catch (CreateModelException e) {
//			String title = "Error";
//			String message = "Abstraction engine unable to convert program facts into model.\n"
//				+ "See error logs for more details.";
//			MessageDialog.openError(shell, title, message);
//			e.printStackTrace();
//		}


		        try
		        {
		            Wizard wizard = createWizard();
		            if (wizard instanceof IWorkbenchWizard)
		            {
		                ((IWorkbenchWizard) wizard).init(getWorkbench(), new StructuredSelection(getInitialContainer()));
		            }
		
		            WizardDialog dialog = new WizardDialog(shell, wizard);
		            dialog.create();
		            int result = dialog.open();
		            postWizard(result);
		        }
		        catch (CoreException e)
		        {
		            String title = "Error";
		            String message = "An error occured during wizard execution.\n"
		                    + "See error logs for more details.";
		
		            ErrorDialog.openError(shell, title, message, e.getStatus());
		        }
	}

	/**
	 * Returns the Eclipse workbench
	 * @return the workbench
	 */
	protected IWorkbench getWorkbench()
	{
		return Activator.getDefault().getWorkbench();
		//        return ReversePlugin.getDefault().getWorkbench();
	}

	/**
	 * Create the wizard
	 * @return the wizard that creates the reverse engineered model
	 * @throws CoreException if an error occured during wizard
	 */
	protected Wizard createWizard() throws CoreException
	{
		AspectJ2UMLWizard wizard = new AspectJ2UMLWizard();
		//        Java2UMLWizard wizard = new Java2UMLWizard();
		wizard.setJavaElements(javaElements);
		return wizard;
	}

	/**
	 * Execute the post action after the wizard
	 * @param code the return code of the wizard
	 * @return <code>true</code> if the action is executed
	 */
	protected boolean postWizard(int code)
	{
		if (code == Window.OK)
		{
			// TODO open the editor
			return true;
		}
		return false;
	}


	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection)
	{
		javaElements = new ArrayList<IJavaElement>();
		if (selection instanceof IStructuredSelection)
		{
			for (Iterator iterator = ((IStructuredSelection) selection).iterator(); iterator.hasNext();) {
				Object selectedObj = iterator.next();
				if (selectedObj instanceof IJavaElement)
				{
					javaElements.add((IJavaElement) selectedObj);
				}
			}
		}

		action.setEnabled(!javaElements.isEmpty());
	}

}
