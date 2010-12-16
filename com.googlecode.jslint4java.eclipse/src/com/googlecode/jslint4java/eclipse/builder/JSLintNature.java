package com.googlecode.jslint4java.eclipse.builder;


import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

import com.googlecode.jslint4java.eclipse.JSLintPlugin;

/**
 * This nature indicates that a project supports validation through JSLint.
 */
public class JSLintNature implements IProjectNature {

    /** ID of this project nature. */
    public static final String NATURE_ID = JSLintPlugin.PLUGIN_ID + ".jsLintNature";

    private final CommandManager commandManager = new CommandManager();
    private IProject project;

    /**
     * Add the necessary builder commands for this nature.
     */
    public void configure() throws CoreException {
        commandManager.addTo(project);
    }

    /**
     * Remove the JSLint builder commands.
     */
    public void deconfigure() throws CoreException {
        commandManager.removeFrom(project);
    }

    public IProject getProject() {
        return project;
    }

    public void setProject(IProject project) {
        this.project = project;
    }

}
