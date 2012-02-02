package com.googlecode.jslint4java.eclipse.ui;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;

import com.googlecode.jslint4java.eclipse.JSLintPlugin;

/**
 * The activator class controls the plug-in life cycle
 */
public class JSLintUIPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "com.googlecode.jslint4java.eclipse.ui"; //$NON-NLS-1$

    // The shared instance
    private static JSLintUIPlugin plugin;

    private IPreferenceStore preferenceStore;

    /**
     * The constructor
     */
    public JSLintUIPlugin() {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static JSLintUIPlugin getDefault() {
        return plugin;
    }

    /**
     * Override the default implementation in order to store prefs in the correct scope.
     *
     * @see JSLintPlugin#PLUGIN_ID
     */
    @Override
    public IPreferenceStore getPreferenceStore() {
        // Create the preference store lazily.
        if (preferenceStore == null) {
            preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, JSLintPlugin.PLUGIN_ID);
        }
        return preferenceStore;
    }
}
