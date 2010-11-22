package com.googlecode.jslint4java.eclipse.builder;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.googlecode.jslint4java.eclipse.JSLintLog;

public class ToggleNatureAction implements IObjectActionDelegate {

    private ISelection selection;

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        if (selection instanceof IStructuredSelection) {
            for (Iterator<?> it = ((IStructuredSelection) selection).iterator(); it
                    .hasNext();) {
                Object element = it.next();
                IProject project = null;
                if (element instanceof IProject) {
                    project = (IProject) element;
                } else if (element instanceof IAdaptable) {
                    project = (IProject) ((IAdaptable) element)
                            .getAdapter(IProject.class);
                }
                if (project != null) {
                    toggleNature(project);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        this.selection = selection;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction,
     *      org.eclipse.ui.IWorkbenchPart)
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    }

    /**
     * Toggles sample nature on a project
     *
     * @param project
     *                to have sample nature added or removed
     */
    private void toggleNature(IProject project) {
        try {
            IProjectDescription description = project.getDescription();
            List<String> natures = new ArrayList<String>(Arrays.asList(description.getNatureIds()));
            if (natures.contains(JSLintNature.NATURE_ID)) {
                // Remove the nature.
                natures.remove(JSLintNature.NATURE_ID);
            } else {
                // Add the nature.
                natures.add(JSLintNature.NATURE_ID);
            }
            description.setNatureIds(natures.toArray(new String[natures.size()]));
            project.setDescription(description, null);
        } catch (CoreException e) {
            JSLintLog.logError(e);
        }
    }

}
