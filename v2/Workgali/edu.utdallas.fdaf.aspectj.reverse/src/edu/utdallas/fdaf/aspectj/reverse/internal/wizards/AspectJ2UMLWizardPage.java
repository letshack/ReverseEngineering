/*******************************************************************************
 * Copyright (c) 2006 David Sciamma. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors to Java2UMLWizardPage:
 *   David Sciamma - initial API and implementation 
 *   Urs Zeidler   - Added some widget to support the new features, model imports
 *                   and profile import 
  * Contributors to AspectJ2UMLWizardPage:
 *   Jeffrey Koch  - Ported from Java2UMLWizardPage, tweaked to use AspectJ2UML code.
 *                   I Am Not A Lawyer, but since this is a modified and renamed
 *                   version of Java2UMLWizardPage.java, I think its copyright 
 *                   follows it.
 ******************************************************************************/

package edu.utdallas.fdaf.aspectj.reverse.internal.wizards;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.ResourceSelectionDialog;
import org.eclipse.uml2.uml.VisibilityKind;

/**
 * The "New" wizard page allows setting the container for the new file as well as the file name. The page will only
 * accept file name without the extension OR with the extension that matches the expected one (uml2).
 */

public class AspectJ2UMLWizardPage extends WizardPage
{
    private Text containerText;

    private Text fileText;

    private Text modelText;

    private ISelection selection;

    private Group groupModel;

    private List listModelImports;

    private Composite compositebtn;
    
    private Combo restrictVisCombo;

    private Button buttonadd;

    private Button buttondel;

    private Button buttonclear;

    private boolean importProfile = false;

    /**
     * Constructor for SampleNewWizardPage.
     * 
     * @param selection Elements currently selected.
     */
    public AspectJ2UMLWizardPage(ISelection selection)
    {
        super("wizardPage");
        setTitle("UML Model File");
        setDescription("This wizard creates a new file with *.uml extension from a AspectJ/Java Reverse Engineering process.");
        this.selection = selection;
    }

    /**
     * @see IDialogPage#createControl(Composite)
     */
    @Override
	public void createControl(Composite parent)
    {
        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 3;
        layout.verticalSpacing = 9;
        Label label = new Label(container, SWT.NULL);
        label.setText("&Container:");

        containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        containerText.setLayoutData(gd);
        containerText.addModifyListener(new ModifyListener()
        {
            @Override
			public void modifyText(ModifyEvent e)
            {
                dialogChanged();
            }
        });

        Button button = new Button(container, SWT.PUSH);
        button.setText("Browse...");
        button.addSelectionListener(new SelectionAdapter()
        {
            @Override
			public void widgetSelected(SelectionEvent e)
            {
                handleBrowse();
            }
        });
        label = new Label(container, SWT.NULL);
        label.setText("&File name:");

        fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        fileText.setLayoutData(gd);
        fileText.addModifyListener(new ModifyListener()
        {
            @Override
			public void modifyText(ModifyEvent e)
            {
                dialogChanged();
            }
        });
        label = new Label(container, SWT.NULL);

        label = new Label(container, SWT.NULL);
        label.setText("&Model name:");
        modelText = new Text(container, SWT.BORDER | SWT.SINGLE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        modelText.setLayoutData(gd);
        modelText.addModifyListener(new ModifyListener()
        {
            @Override
			public void modifyText(ModifyEvent e)
            {
                dialogChanged();
            }
        });
        label = new Label(container, SWT.NULL);

        // Patch TSL: limit the import with the visibility 
        label = new Label(container, SWT.NULL);
        label.setText("&Limit Visibility:");
        restrictVisCombo = new Combo(container, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
        restrictVisCombo.add(VisibilityKind.PUBLIC_LITERAL.getLiteral());
        restrictVisCombo.add(VisibilityKind.PACKAGE_LITERAL.getLiteral());
        restrictVisCombo.add(VisibilityKind.PROTECTED_LITERAL.getLiteral());
        restrictVisCombo.add(VisibilityKind.PRIVATE_LITERAL.getLiteral());
        restrictVisCombo.select(0); // By default, get only public content
        
        gd = new GridData(GridData.FILL_HORIZONTAL);
        restrictVisCombo.setLayoutData(gd);
        restrictVisCombo.addModifyListener(new ModifyListener()
        {
            @Override
			public void modifyText(ModifyEvent e)
            {
                dialogChanged();
            }
        });
        label = new Label(container, SWT.NULL);

        
        Button button2 = new Button(container, SWT.RADIO);
        button2.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
        button2.setText("import profile");
        button2.addSelectionListener(new SelectionAdapter()
        {
            @Override
			public void widgetSelected(SelectionEvent e)
            {
                importProfile = true;
            }
        });

        Button button3 = new Button(container, SWT.RADIO);
        button3.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        button3.setText("import model");
        button3.setSelection(true);
        button3.addSelectionListener(new SelectionAdapter()
        {
            @Override
			public void widgetSelected(SelectionEvent e)
            {
                importProfile = false;
            }
        });
        label = new Label(container, SWT.NULL);

        GridLayout gridLayout1 = new GridLayout();
        gridLayout1.numColumns = 2; // Generated
        GridData gridData5 = new GridData();
        gridData5.grabExcessHorizontalSpace = false; // Generated
        gridData5.grabExcessVerticalSpace = true;
        gridData5.verticalSpan = 20; // Generated
        gridData5.horizontalAlignment = GridData.FILL; // Generated
        gridData5.verticalAlignment = GridData.FILL; // Generated
        gridData5.horizontalSpan = 10; // Generated
        groupModel = new Group(container, SWT.NONE);
        groupModel.setText("Model imports"); // Generated
        groupModel.setLayoutData(gridData5); // Generated
        groupModel.setLayout(gridLayout1); // Generated

        GridData gridData7 = new GridData();
        gridData7.grabExcessHorizontalSpace = true; // Generated
        gridData7.grabExcessVerticalSpace = true;
        gridData7.verticalSpan = 20; // Generated
        gridData7.horizontalAlignment = GridData.FILL; // Generated
        gridData7.verticalAlignment = GridData.FILL; // Generated
        gridData7.horizontalSpan = 10; // Generated
        listModelImports = new List(groupModel, SWT.MULTI | SWT.BORDER);
        listModelImports.setLayoutData(gridData7); // Generated

        RowLayout rowLayout = new RowLayout();
        rowLayout.type = SWT.HORIZONTAL; // Generated
        rowLayout.justify = false; // Generated
        rowLayout.pack = false; // Generated
        rowLayout.fill = false; // Generated
        GridData gridData10 = new GridData();
        gridData10.horizontalAlignment = GridData.FILL; // Generated
        gridData10.horizontalSpan = 2; // Generated
        // gridData10.verticalAlignment = GridData.CENTER; // Generated
        compositebtn = new Composite(groupModel, SWT.NONE);
        compositebtn.setLayoutData(gridData10); // Generated
        compositebtn.setLayout(rowLayout); // Generated
        buttonadd = new Button(compositebtn, SWT.NONE);
        buttonadd.setText("add"); // Generated
        buttonadd.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
        {
            @Override
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e)
            {
                handleAddImport();
            }
        });
        buttondel = new Button(compositebtn, SWT.NONE);
        buttondel.setText("del"); // Generated
        buttondel.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
        {
            @Override
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e)
            {
                String[] selection2 = listModelImports.getSelection();
                for (String string : selection2)
                {
                    listModelImports.remove(string);
                }
            }
        });

        buttonclear = new Button(compositebtn, SWT.NONE);
        buttonclear.setText("clear"); // Generated
        buttonclear.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter()
        {
            @Override
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e)
            {
                listModelImports.setItems(new String[] {});
            }
        });

        initialize();
        dialogChanged();
        setControl(container);
    }

    /**
     * Tests if the current workbench selection is a suitable container to use.
     */

    private void initialize()
    {
        if (selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection)
        {
            IStructuredSelection ssel = (IStructuredSelection) selection;
            if (ssel.size() > 1)
            {
                return;
            }
            Object obj = ssel.getFirstElement();
            if (obj instanceof IResource)
            {
                IContainer container;
                if (obj instanceof IContainer)
                {
                    container = (IContainer) obj;
                }
                else
                {
                    container = ((IResource) obj).getParent();
                }
                containerText.setText(container.getFullPath().toString());
            }
        }
        fileText.setText("model.uml");
    }

    private void handleAddImport()
    {
        IContainer cont = ResourcesPlugin.getWorkspace().getRoot();

        ResourceSelectionDialog dialog = new ResourceSelectionDialog(getShell(), cont, "select Model to Import");

        // ResourceListSelectionDialog dialog = new ResourceListSelectionDialog(
        // getShell(), cont, 1);
        // dialog.open();

        if (dialog.open() == IDialogConstants.OK_ID)
        {
            // dialog.setBlockOnOpen(true);
            Object[] obj = dialog.getResult();
            if (obj == null || obj.length < 1)
            {
                return;
            }
            for (int i = 0; i < obj.length; i++)
            {
                Object object = obj[i];
                if (object instanceof IResource)
                {
                    String filename = ((IResource) obj[i]).getFullPath().toString();
                    listModelImports.add(filename);

                }

            }
        }
    }

    /**
     * Uses the standard container selection dialog to choose the new value for the container field.
     */

    private void handleBrowse()
    {
        ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), ResourcesPlugin.getWorkspace().getRoot(), false, "Select new file container");
        if (dialog.open() == Window.OK)
        {
            Object[] result = dialog.getResult();
            if (result.length == 1)
            {
                containerText.setText(((Path) result[0]).toString());
            }
        }
    }

    /**
     * Ensures that both text fields are set.
     */

    private void dialogChanged()
    {
        IResource container = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(getContainerName()));
        String fileName = getFileName();

        if (getContainerName().length() == 0)
        {
            updateStatus("File container must be specified");
            return;
        }
        if (container == null || (container.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0)
        {
            updateStatus("File container must exist");
            return;
        }
        if (!container.isAccessible())
        {
            updateStatus("Project must be writable");
            return;
        }
        if (fileName.length() == 0)
        {
            updateStatus("File name must be specified");
            return;
        }
        if (fileName.replace('\\', '/').indexOf('/', 1) > 0)
        {
            updateStatus("File name must be valid");
            return;
        }
        int dotLoc = fileName.lastIndexOf('.');
        if (dotLoc != -1)
        {
            String ext = fileName.substring(dotLoc + 1);
            if (!ext.equalsIgnoreCase("uml"))
            {
                updateStatus("File extension must be \"uml\"");
                return;
            }
        }
        updateStatus(null);
    }

    private void updateStatus(String message)
    {
        setErrorMessage(message);
        setPageComplete(message == null);
    }

    /**
     * returns the container text
     * 
     * @return Container text
     */
    public String getContainerName()
    {
        return containerText.getText();
    }

    /**
     * returns the filename
     * 
     * @return File name
     */
    public String getFileName()
    {
        return fileText.getText();
    }

    /**
     * returns the list of imports
     * 
     * @return List of imports
     */
    public String[] getImportList()
    {
        return listModelImports.getItems();
    }

    /**
     * return if a profile should be imported
     * 
     * @return <code>true</code> if a profile should be imported.
     */
    public boolean getImportProfile()
    {
        return importProfile;
    }
    
    /**
     * Return the visibility that limits the import.
     * @return The visibility
     */
    public VisibilityKind getVisibilityKind(){
    	switch(restrictVisCombo.getSelectionIndex()){
    	case 1:
    		return VisibilityKind.PACKAGE_LITERAL;
    	case 2:
    		return VisibilityKind.PROTECTED_LITERAL;
    	case 3:
    		return VisibilityKind.PRIVATE_LITERAL;
    		default:
        		return VisibilityKind.PUBLIC_LITERAL;
    	}
    }

    /**
     * returns the model names
     * 
     * @return Model name
     */
    public String getModelName()
    {
        return modelText.getText();
    }
}
