package com.googlecode.jslint4java.eclipse;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class JSLintLog {

    // TODO How to get debug logging?
    
    public static void logInfo(String message) {
        log(IStatus.INFO, IStatus.OK, message, null);
    }

    public static void logError(Throwable t) {
        log(IStatus.ERROR, IStatus.OK, "Unexpected exception", t);
    }

    public static void logError(String message, Throwable t) {
        log(IStatus.ERROR, IStatus.OK, message, t);
    }

    public static void log(int severity, int code, String message, Throwable t) {
        log(createStatus(severity, code, message, t));
    }

    public static IStatus createStatus(int severity, int code, String message,
            Throwable t) {
        return new Status(severity, Activator.PLUGIN_ID, code, message, t);
    }

    public static void log(IStatus status) {
        Activator.getDefault().getLog().log(status);
    }
}
