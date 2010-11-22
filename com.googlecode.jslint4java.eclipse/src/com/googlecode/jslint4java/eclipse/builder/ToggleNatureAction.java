package com.googlecode.jslint4java.eclipse.builder;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;

public class ToggleNatureAction implements IActionDelegate {

    private IStructuredSelection selection;

    public void run(IAction action) {
        NatureManager natureManager = new NatureManager();
        for (Object obj : selection.toArray()) {
            IProject project = projectFromSelectedItem(obj);
            if (project != null) {
                natureManager.toggleNature(project);
            }
        }
    }

    /** Convert to a project, or return null. */
    private IProject projectFromSelectedItem(Object obj) {
        if (obj instanceof IProject) {
            return (IProject) obj;
        } else if (obj instanceof IAdaptable) {
            return (IProject) ((IAdaptable) obj).getAdapter(IProject.class);
        } else {
            return null;
        }
    }

    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            this.selection = (IStructuredSelection) selection;
        }
    }

}
