package net.happygiraffe.jslint.builder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import net.happygiraffe.jslint.Activator;
import net.happygiraffe.jslint.Issue;
import net.happygiraffe.jslint.JSLint;
import net.happygiraffe.jslint.JSLintLog;
import net.happygiraffe.jslint.Option;

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

    public static final String BUILDER_ID = "net.happygiraffe.jslint.jsLintBuilder";

    // NB! Must match plugin.xml declaration.
    private static final String MARKER_TYPE = Activator.PLUGIN_ID
            + ".javaScriptLintProblem";

    private JSLint lint;

    private void addMarker(IFile file, Issue issue) {
        try {
            IMarker marker = file.createMarker(MARKER_TYPE);
            marker.setAttribute(IMarker.MESSAGE, issue.getReason());
            marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
            marker.setAttribute(IMarker.LINE_NUMBER, issue.getLine() + 1);
            // JSLintLog.logInfo("Added marker for " + issue);
        } catch (CoreException e) {
            JSLintLog.logError(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
     *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected IProject[] build(final int kind, Map args,
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

    void checkJavaScript(IResource resource) {
        if (!(resource instanceof IFile))
            return;

        IFile file = (IFile) resource;
        if (!file.getName().endsWith(".js"))
            return;

        JSLintLog.logInfo("Checking file " + resource.getFullPath());

        // Clear out any existing problems.
        deleteMarkers(file);

        BufferedReader reader = null;
        try {
            JSLint lint = getJSLint();
            applyPrefs(lint);
            reader = new BufferedReader(new InputStreamReader(file
                    .getContents()));
            List<Issue> issues = lint.lint(file.getFullPath().toString(),
                    reader);
            for (Issue issue : issues) {
                addMarker(file, issue);
            }
        } catch (IOException e) {
            JSLintLog.logError(e);
        } catch (CoreException e) {
            JSLintLog.logError(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    JSLintLog.logError(e);
                }
            }
        }
    }

    // Apply the global prefs to this JSLint object.
    private void applyPrefs(JSLint lint) {
        lint.resetOptions();
        IPreferencesService prefs = Platform.getPreferencesService();
        for (Option o : Option.values()) {
            boolean value = prefs.getBoolean(Activator.PLUGIN_ID, o.getLowerName(), false, null);
            if (value) {
                lint.addOption(o);
            }
        }
    }

    private JSLint getJSLint() throws IOException {
        if (lint == null) {
            lint = new JSLint();
        }
        return lint;
    }

    private void deleteMarkers(IFile file) {
        try {
            file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
        } catch (CoreException e) {
            JSLintLog.logError(e);
        }
    }

    protected void fullBuild(final IProgressMonitor monitor)
            throws CoreException {
        try {
            getProject().accept(new JSLintResourceVisitor());
        } catch (CoreException e) {
            JSLintLog.logError(e);
        }
    }

    protected void incrementalBuild(IResourceDelta delta,
            IProgressMonitor monitor) throws CoreException {
        // the visitor does the work.
        delta.accept(new JSLintDeltaVisitor());
    }
}
