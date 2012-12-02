/*******************************************************************************
 * Copyright (c) 2006 David Sciamma. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors to Java2UMLWizard
 *   David Sciamma - initial API and implementation 
 *   Urs Zeidler   - Added two importing options, model imports, profile import
 *   Thomas Szadel - Allow to limit the details of the import
 * Contributors to AspectJ2UMLWizard
 *   Jeffrey Koch  - Ported from Java2UMLWizard, tweaked to use AspectJ2UML code.
 *                   I Am Not A Lawyer, but since this is a modified and renamed
 *                   version of Java2UMLWizard.java, I think its copyright 
 *                   follows it.
 ******************************************************************************/

package edu.utdallas.fdaf.aspectj.reverse.internal.wizards;

import java.io.IOException;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.uml2.uml.VisibilityKind;

import edu.utdallas.fdaf.aspectj.reverse.AspectJReporter;
import edu.utdallas.fdaf.aspectj.reverse.BuildNeededException;
import edu.utdallas.fdaf.aspectj.reverse.internal.Activator;
import edu.utdallas.fdaf.model.AbstractionEngine;
import edu.utdallas.fdaf.model.CreateModelException;
import edu.utdallas.fdaf.model.ModelPrologLoadException;



/**
 * This is a sample new wizard. Its role is to create a new file resource in the provided container. If the container
 * resource (a folder or a project) is selected in the workspace when the wizard is opened, it will accept it as the
 * target container. The wizard creates one file with the extension "uml2". If a sample multi-page editor (also
 * available as a template) is registered for the same extension, it will be able to open it.
 */

public class AspectJ2UMLWizard extends Wizard implements INewWizard
{
	private AspectJ2UMLWizardPage page;

	private ISelection selection;

	private List<IJavaElement> javaElements;

	/**
	 * Constructor for Java2UML2Wizard.
	 */
	public AspectJ2UMLWizard()
	{
		super();
		setNeedsProgressMonitor(true);
	}

	/**
	 * Adding the page to the wizard.
	 */

	@Override
	public void addPages()
	{
		page = new AspectJ2UMLWizardPage(selection);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We will create an operation and run it using
	 * wizard as execution context.
	 */
	@Override
	public boolean performFinish()
	{
		final String containerName = page.getContainerName();
		final String fileName = page.getFileName();
		final VisibilityKind visibility = page.getVisibilityKind();
		final String[] importList = page.getImportList();
		final String modelName = page.getModelName();
		IRunnableWithProgress op = new IRunnableWithProgress()
		{
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException
			{
				try
				{
					
					doFinish(containerName, fileName, importList, monitor, modelName, visibility);
				}
				catch (CoreException e)
				{
					throw new InvocationTargetException(e);
				} catch (IOException e) {
					String title = "Error";
					String message = "An IO error occured during UML model generation.\n"
						+ "See error logs for more details.";
					MessageDialog.openError(getShell(), title, message);
				} catch (final BuildNeededException e) {
					final String title = "Error";
					//bala
					 new Thread(new Runnable() {
					      public void run() {
					         while (true) {
					            try { Thread.sleep(1000); } catch (Exception e) { }
					            Display.getDefault().asyncExec(new Runnable() {
					               public void run() {
					            	
					               }
					            });
					         }
					      }
					   }).start();
					
				/*	//bala
					MessageDialog.openError(getShell(), title, "Need to build project.\n"
							+ e.getMessage());
					//bala*/
				} catch (ModelPrologLoadException e) {
					String title = "Error";
					String message = "Unable to load Prolog rules for model generation.\n"
						+ "See error logs for more details.";
					MessageDialog.openError(getShell(), title, message);
					e.printStackTrace();
				} catch (CreateModelException e) {
					String title = "Error";
					String message = "Abstraction engine unable to convert program facts into model.\n"
						+ "See error logs for more details.";
					MessageDialog.openError(getShell(), title, message);
					e.printStackTrace();
				}
				finally
				{
					monitor.done();
				}
			}
		};
		try
		{
			getContainer().run(true, false, op);
		}
		catch (InterruptedException e)
		{
			return false;
		}
		catch (InvocationTargetException e)
		{
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * The worker method. It will find the container, create the file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 * 
	 * @param importList
	 * @param modelName
	 * @param visibility The max visibiltiy to import.
	 * @throws BuildNeededException 
	 * @throws IOException 
	 * @throws CreateModelException 
	 * @throws ModelPrologLoadException 
	 */

	@SuppressWarnings("unchecked")
	private void doFinish(String containerName, String fileName, String[] importList, IProgressMonitor monitor, String modelName, VisibilityKind visibility) throws CoreException, IOException, BuildNeededException, ModelPrologLoadException, CreateModelException
	{
		// create a sample file
		
		monitor.beginTask("Creating " + fileName, 2);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		
		
		
		//---------------
		if (!resource.exists() || !(resource instanceof IContainer))
		{
			throwCoreException("Container \"" + containerName + "\" does not exist.");
		}
		IContainer container = (IContainer) resource;
		final IFile progFactsIfile = container.getFile(new Path(fileName));

		String archFileName = archifyFileName(fileName);
		final IFile archIfile = container.getFile(new Path(archFileName));

		//----------------
		
		if (javaElements == null)
		{
			throwCoreException("No Java or AspectJ elements selected.");
		}

		//		AbstractJava2UMLConverter converter = null;

		//-----------------
		AspectJReporter reporter = new AspectJReporter();
		

		reporter.report(javaElements, progFactsIfile);

		//------
		AbstractionEngine.generateModel(progFactsIfile, archIfile);
		
		archIfile.refreshLocal(IResource.DEPTH_ZERO, null);

	}

	private String archifyFileName(String fileName) {
		// TODO Auto-generated method stub
		String filePrefix;
		if (fileName.endsWith(".uml")) {
			filePrefix = fileName.substring(0, fileName.length()-4);
		} else {
			filePrefix = fileName;
		}
		return filePrefix + "-arch.uml";
	}

	/**
	 * We will accept the selection in the workbench to see if we can initialize from it.
	 * 
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection sel)
	{
		this.selection = sel;
	}

	private void throwCoreException(String message) throws CoreException
	{
		IStatus status = new Status(IStatus.ERROR, Activator.getId(), IStatus.OK, message, null);
		throw new CoreException(status);
	}

	/**
	 * Get the Java Element to be reverse engineered
	 * 
	 * @return the element to transform
	 */
	public List <IJavaElement> getJavaElements()
	{
		return javaElements;
	}

	/**
	 * Get the Java Element to be reverse engineered
	 * 
	 * @param elt the element to transform
	 */
	public void setJavaElements(List <IJavaElement> elts)
	{
		javaElements = elts;
	}
}