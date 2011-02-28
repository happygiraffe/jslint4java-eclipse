package com.googlecode.jslint4java.eclipse.builder;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.regex.Pattern;

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
import com.googlecode.jslint4java.eclipse.JSLintLog;
import com.googlecode.jslint4java.eclipse.JSLintPlugin;

/**
 * A “Builder” which invokes {@link JSLint} on each affected JavaScript in the project.
 */
public class JSLintBuilder extends IncrementalProjectBuilder {

    private class JSLintDeltaVisitor implements IResourceDeltaVisitor {
        private final IProgressMonitor monitor;

        public JSLintDeltaVisitor(IProgressMonitor monitor) {
            this.monitor = monitor;
        }

        public boolean visit(IResourceDelta delta) throws CoreException {
            IResource resource = delta.getResource();
            switch (delta.getKind()) {
            case IResourceDelta.ADDED:
                // handle added resource
                logProgress(monitor, resource);
                checkJavaScript(resource);
                break;
            case IResourceDelta.REMOVED:
                // handle removed resource
                break;
            case IResourceDelta.CHANGED:
                // handle changed resource
                logProgress(monitor, resource);
                checkJavaScript(resource);
                break;
            }
            // return true to continue visiting children.
            return true;
        }
    }

    private class JSLintResourceVisitor implements IResourceVisitor {
        private final IProgressMonitor monitor;

        public JSLintResourceVisitor(IProgressMonitor monitor) {
            this.monitor = monitor;
        }

        public boolean visit(IResource resource) {
            logProgress(monitor, resource);
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
        if (!shouldLint(file)) {
            return;
        }

        // Clear out any existing problems.
        deleteMarkers(file);

        BufferedReader reader = null;
        try {
            JSLint lint = lintProvider.getJsLint();
            // TODO: this should react to changes in the prefs pane instead.
            reader = new BufferedReader(new InputStreamReader(file
                    .getContents(), file.getCharset()));
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
            close(reader);
        }
    }

    private boolean shouldLint(IFile file) {
        return isJavaScript(file) && !excluded(file);
    }

    /**
     * Is {@code file} explicitly excluded? Check against a list of regexes in
     * the <i>exclude_path_regexes</i> preference.
     */
    // TODO: precompute the regex list.
    private boolean excluded(IFile file) {
        String filePath = file.getFullPath().toString();
        IPreferencesService prefs = Platform.getPreferencesService();
        String excludePaths = prefs.getString(JSLintPlugin.PLUGIN_ID, "exclude_path_regexes", "", null);
        if (excludePaths.length() > 0) {
            for (String path : excludePaths.split(",")) {
                Pattern p = Pattern.compile(path);
                if (p.matcher(filePath).find()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isJavaScript(IFile file) {
        return file.getName().endsWith(".js");
    }

    private void close(Closeable close) {
        if (close == null) {
            return;
        }
        try {
            close.close();
        } catch (IOException e) {
        }
    }

    private void deleteMarkers(IFile file) {
        try {
            file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
        } catch (CoreException e) {
            JSLintLog.error(e);
        }
    }

    private void fullBuild(final IProgressMonitor monitor) throws CoreException {
        try {
            startProgress(monitor);
            getProject().accept(new JSLintResourceVisitor(monitor));
        } catch (CoreException e) {
            JSLintLog.error(e);
        } finally {
            monitor.done();
        }
    }

    private void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor)
            throws CoreException {
        try {
            startProgress(monitor);
            delta.accept(new JSLintDeltaVisitor(monitor));
        } finally {
            monitor.done();
        }
    }

    private void startProgress(IProgressMonitor monitor) {
        monitor.beginTask("jslint4java", IProgressMonitor.UNKNOWN);
    }

    private void logProgress(IProgressMonitor monitor, IResource resource) {
        monitor.subTask("Linting " + resource.getName());
    }
}
