package com.googlecode.jslint4java.eclipse.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

import com.googlecode.jslint4java.eclipse.JSLintPlugin;

/**
 * This nature indicates that a project supports validation through JSLint.
 */
public class JSLintNature implements IProjectNature {

    /** ID of this project nature. */
    public static final String NATURE_ID = JSLintPlugin.PLUGIN_ID + ".jsLintNature";

    /**
     * Add the JSLint build commands to a project.  If it already has them, no change will be made.
     */
    public static void addCommands(IProject project) throws CoreException {
        List<ICommand> cmds = getCommands(project);
        for (ICommand cmd : cmds) {
            if (cmd.getBuilderName().equals(JSLintBuilder.BUILDER_ID)) {
                return;
            }
        }

        cmds.add(newCommand(project, JSLintBuilder.BUILDER_ID));
        setCommands(project, cmds);
    }

    /** Return a (mutable) list of commands (builders) in a project. */
    private static List<ICommand> getCommands(IProject project) throws CoreException {
        IProjectDescription desc = project.getDescription();
        return new ArrayList<ICommand>(Arrays.asList(desc.getBuildSpec()));
    }

    /** Make a new command (builder). */
    private static ICommand newCommand(IProject project, String builderName) throws CoreException {
        ICommand cmd = project.getDescription().newCommand();
        cmd.setBuilderName(builderName);
        return cmd;
    }

    /** Remove the JSLint commands from a project. */
    public static void removeFrom(IProject project) throws CoreException {
        List<ICommand> cmds = getCommands(project);
        // Separate detection from removal to avoid a ConcurrentModificationException.
        Set<ICommand> togo = new HashSet<ICommand>();
        for (ICommand cmd : cmds) {
            if (cmd.getBuilderName().equals(JSLintBuilder.BUILDER_ID)) {
                togo.add(cmd);
            }
        }
        cmds.removeAll(togo);
        setCommands(project, cmds);
    }

    /** Set the list of commands (builders) for a project. */
    private static void setCommands(IProject project, List<ICommand> commands) throws CoreException {
        IProjectDescription desc = project.getDescription();
        desc.setBuildSpec(commands.toArray(new ICommand[commands.size()]));
        project.setDescription(desc, null);
    }

    private IProject project;

    /**
     * Add the necessary builder commands for this nature.
     */
    public void configure() throws CoreException {
        addCommands(project);
    }

    /**
     * Remove the JSLint builder commands.
     */
    public void deconfigure() throws CoreException {
        removeFrom(project);
    }

    public IProject getProject() {
        return project;
    }

    public void setProject(IProject project) {
        this.project = project;
    }

}
