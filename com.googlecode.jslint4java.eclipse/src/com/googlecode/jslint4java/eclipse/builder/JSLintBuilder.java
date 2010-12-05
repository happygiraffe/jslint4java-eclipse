package com.googlecode.jslint4java.eclipse.builder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;

import com.googlecode.jslint4java.Issue;
import com.googlecode.jslint4java.JSLint;
import com.googlecode.jslint4java.JSLintResult;
import com.googlecode.jslint4java.Option;
import com.googlecode.jslint4java.eclipse.JSLintLog;
import com.googlecode.jslint4java.eclipse.JSLintPlugin;

public class JSLintBuilder extends IncrementalProjectBuilder {

    class JSLintDeltaVisitor implements IResourceDeltaVisitor {
        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
         */
        public boolean visit(IResourceDelta delta) throws CoreException {
            IResource resource = delta.getResource();
            switch (delta.getKind()) {
            case IResourceDelta.ADDED:
                // handle added resource
                checkJavaScript(resource);
                break;
            case IResourceDelta.REMOVED:
                // handle removed resource
                break;
            case IResourceDelta.CHANGED:
                // handle changed resource
                checkJavaScript(resource);
                break;
            }
            // return true to continue visiting children.
            return true;
        }
    }

    class JSLintResourceVisitor implements IResourceVisitor {
        public boolean visit(IResource resource) {
            checkJavaScript(resource);
            // return true to continue visiting children.
            return true;
        }
    }

    // NB! Must match plugin.xml declaration.
    public static final String BUILDER_ID = JSLintPlugin.PLUGIN_ID + ".jsLintBuilder";

    // NB! Must match plugin.xml declaration.
    public static final String MARKER_TYPE = JSLintPlugin.PLUGIN_ID
            + ".javaScriptLintProblem";

    private final JSLintProvider lintProvider = new JSLintProvider();

    public JSLintBuilder() {
        lintProvider.init();
    }

    private void addMarker(IFile file, Issue issue) {
        try {
            IMarker m = file.createMarker(MARKER_TYPE);
            if (m.exists()) {
                m.setAttribute(IMarker.MESSAGE, issue.getReason());
                m.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
                m.setAttribute(IMarker.LINE_NUMBER, issue.getLine());
                m.setAttribute(IMarker.SOURCE_ID, "jslint4java");
            }
            // JSLintLog.logInfo("Added marker for " + issue);
        } catch (CoreException e) {
            JSLintLog.error(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
     *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected IProject[] build(final int kind, @SuppressWarnings("rawtypes") Map args,
            IProgressMonitor monitor) throws CoreException {
        ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException {
                if (kind == FULL_BUILD) {
                    fullBuild(monitor);
                } else {
                    IResourceDelta delta = getDelta(getProject());
                    if (delta == null) {
                        fullBuild(monitor);
                    } else {
                        incrementalBuild(delta, monitor);
                    }
                }
            }
        }, monitor);
        return null;
    }

    private void checkJavaScript(IResource resource) {
        if (!(resource instanceof IFile)) {
            return;
        }

        IFile file = (IFile) resource;
        if (!file.getName().endsWith(".js")) {
            return;
        }

        JSLintLog.info("Checking file " + resource.getFullPath());

        // Clear out any existing problems.
        deleteMarkers(file);

        BufferedReader reader = null;
        try {
            JSLint lint = lintProvider.getJsLint();
            // TODO: this should react to changes in the prefs pane instead.
            applyPrefs(lint);
            reader = new BufferedReader(new InputStreamReader(file
                    .getContents()));
            JSLintResult result = lint.lint(file.getFullPath().toString(),
                    reader);
            for (Issue issue : result.getIssues()) {
                addMarker(file, issue);
            }
        } catch (IOException e) {
            JSLintLog.error(e);
        } catch (CoreException e) {
            JSLintLog.error(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    JSLintLog.error(e);
                }
            }
        }
    }

    // Apply the global prefs to this JSLint object.
    private void applyPrefs(JSLint lint) {
        lint.resetOptions();
        IPreferencesService prefs = Platform.getPreferencesService();
        for (Option o : Option.values()) {
            if (o.getType() == Boolean.class) {
                boolean value = prefs.getBoolean(JSLintPlugin.PLUGIN_ID, o.getLowerName(), false,
                        null);
                if (value) {
                    lint.addOption(o);
                }
            }
            // TODO: implement other option types.
        }
    }

    private void deleteMarkers(IFile file) {
        try {
            file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
        } catch (CoreException e) {
            JSLintLog.error(e);
        }
    }

    protected void fullBuild(final IProgressMonitor monitor)
            throws CoreException {
        try {
            getProject().accept(new JSLintResourceVisitor());
        } catch (CoreException e) {
            JSLintLog.error(e);
        }
    }

    protected void incrementalBuild(IResourceDelta delta,
            IProgressMonitor monitor) throws CoreException {
        // the visitor does the work.
        delta.accept(new JSLintDeltaVisitor());
    }
}
