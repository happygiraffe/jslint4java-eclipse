package com.googlecode.jslint4java.eclipse.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.googlecode.jslint4java.eclipse.JSLintPlugin;

public class OptionsPropertyPage extends OptionsPreferencePage implements IWorkbenchPropertyPage {

    private IAdaptable element;

    public OptionsPropertyPage() {
        // This will set up the scope as "instance."
        super();
    }

    public IAdaptable getElement() {
        return element;
    }

    public void setElement(IAdaptable element) {
        this.element = element;
        // Now that we have a project, we can reset the scope to be project specific.
        IProject project = (IProject) element.getAdapter(IProject.class);
        if (project != null) {
            IScopeContext scope = new ProjectScope(project);
            setPreferenceStore(new ScopedPreferenceStore(scope, JSLintPlugin.PLUGIN_ID));
        }
    }

}
