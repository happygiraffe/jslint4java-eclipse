package com.googlecode.jslint4java.eclipse;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class JSLintLog {

    // TODO How to get debug logging?
    
    public static void info(String message) {
        log(IStatus.INFO, IStatus.OK, message, null);
    }

    public static void error(Throwable t) {
        log(IStatus.ERROR, IStatus.OK, "Unexpected exception", t);
    }

    public static void error(String message, Throwable t) {
        log(IStatus.ERROR, IStatus.OK, message, t);
    }

    public static void log(int severity, int code, String message, Throwable t) {
        log(createStatus(severity, code, message, t));
    }

    public static IStatus createStatus(int severity, int code, String message,
            Throwable t) {
        return new Status(severity, JSLintPlugin.PLUGIN_ID, code, message, t);
    }

    public static void log(IStatus status) {
        JSLintPlugin.getDefault().getLog().log(status);
    }
}
