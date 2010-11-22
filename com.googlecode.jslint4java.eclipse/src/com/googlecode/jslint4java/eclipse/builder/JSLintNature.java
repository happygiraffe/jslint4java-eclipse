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

import com.googlecode.jslint4java.eclipse.Activator;

/**
 * This nature indicates that a project supports validation through JSLint.
 */
public class JSLintNature implements IProjectNature {

    /**
     * ID of this project nature
     */
    public static final String NATURE_ID = Activator.PLUGIN_ID + ".jsLintNature";

    private IProject project;

    /**
     * Add the necessary builder commands for this nature.
     */
    public void configure() throws CoreException {
        List<ICommand> cmds = getCommands();
        for (ICommand cmd : cmds) {
            if (cmd.getBuilderName().equals(JSLintBuilder.BUILDER_ID)) {
                return;
            }
        }

        cmds.add(newCommand(JSLintBuilder.BUILDER_ID));
        setCommands(cmds);
    }

    /**
     * Remove the JSLint builder commands.
     */
    public void deconfigure() throws CoreException {
        List<ICommand> cmds = getCommands();
        // Separate detection from removal to avoid a ConcurrentModificationException.
        Set<ICommand> togo = new HashSet<ICommand>();
        for (ICommand cmd : cmds) {
            if (cmd.getBuilderName().equals(JSLintBuilder.BUILDER_ID)) {
                togo.add(cmd);
            }
        }
        cmds.removeAll(togo);
        setCommands(cmds);
    }

    /** Return a (mutable) list of commands (builders) in the current project. */
    private List<ICommand> getCommands() throws CoreException {
        IProjectDescription desc = project.getDescription();
        return new ArrayList<ICommand>(Arrays.asList(desc.getBuildSpec()));
    }

    public IProject getProject() {
        return project;
    }

    /** Make a new command (builder)*/
    private ICommand newCommand(String builderName) throws CoreException {
        ICommand cmd = project.getDescription().newCommand();
        cmd.setBuilderName(builderName);
        return cmd;
    }

    /** Set the list of commands (builders) for this project. */
    private void setCommands(List<ICommand> commands) throws CoreException {
        IProjectDescription desc = project.getDescription();
        desc.setBuildSpec(commands.toArray(new ICommand[commands.size()]));
        project.setDescription(desc, null);
    }

    public void setProject(IProject project) {
        this.project = project;
    }

}
