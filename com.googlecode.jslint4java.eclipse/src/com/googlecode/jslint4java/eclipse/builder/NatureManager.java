package com.googlecode.jslint4java.eclipse.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.googlecode.jslint4java.eclipse.JSLintLog;

/**
 * Manage adding and removing the nature from a project.
 */
public class NatureManager {

    /** Return a mutable list of natures on this project. */
    private List<String> getNatures(IProject project) throws CoreException {
        IProjectDescription desc = project.getDescription();
        return new ArrayList<String>(Arrays.asList(desc.getNatureIds()));
    }

    /** Apply this list of natures to a project. */
    private void setNatures(IProject project, List<String> natures) throws CoreException {
        IProjectDescription desc = project.getDescription();
        desc.setNatureIds(natures.toArray(new String[natures.size()]));
        project.setDescription(desc, null);
    }

    /** Remove all markers we have made for this project. */
    public void removeMarkers(IProject project) {
        try {
            project.deleteMarkers(JSLintBuilder.MARKER_TYPE, true, IResource.DEPTH_INFINITE);
        } catch (CoreException e) {
            JSLintLog.error(e);
        }
    }

    /** Enable or disable the JSLint nature on a project. */
    public void toggleNature(IProject project) {
        try {
            List<String> natures = getNatures(project);
            if (natures.contains(JSLintNature.NATURE_ID)) {
                // Remove the nature.
                natures.remove(JSLintNature.NATURE_ID);
                removeMarkers(project);
            } else {
                // Add the nature.
                natures.add(JSLintNature.NATURE_ID);
            }
            setNatures(project, natures);
        } catch (CoreException e) {
            JSLintLog.error(e);
        }
    }

}
