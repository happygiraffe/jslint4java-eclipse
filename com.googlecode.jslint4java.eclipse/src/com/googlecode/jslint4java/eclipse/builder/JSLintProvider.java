package com.googlecode.jslint4java.eclipse.builder;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;

import com.googlecode.jslint4java.JSLint;
import com.googlecode.jslint4java.JSLintBuilder;
import com.googlecode.jslint4java.Option;
import com.googlecode.jslint4java.eclipse.JSLintLog;
import com.googlecode.jslint4java.eclipse.JSLintPlugin;

/**
 * Provide a fully configured instance of {@link JSLint} on demand.
 */
public class JSLintProvider {

    private final JSLintBuilder builder = new JSLintBuilder();

    private JSLint jsLint;

    /**
     * Set up a listener for preference changes. This will ensure that the instance of
     * {@link JSLint} that we have is kept in sync with the users choices. We do this by ensuring
     * that a new JSLint will be created and configured on the next request.
     */
    public void init() {
        IEclipsePreferences x = InstanceScope.INSTANCE.getNode(JSLintPlugin.PLUGIN_ID);
        x.addPreferenceChangeListener(new IPreferenceChangeListener() {
            public void preferenceChange(PreferenceChangeEvent ev) {
                jsLint = null;
                JSLintLog.info("pref %s changed; nulling jsLint", ev.getKey());
            }
        });
    }

    /**
     * Return a fully configured instance of JSLint. This should not be cached; each use should call
     * this method.
     */
    public JSLint getJsLint() {
        if (jsLint == null) {
            // TODO: Allow for non-default versions of fulljslint.js.
            jsLint = builder.fromDefault();
            configure();
        }
        return jsLint;
    }

    /** Set up the current instance of JSLint using the current preferences. */
    public void configure() {
        JSLint lint = getJsLint();
        lint.resetOptions();
        IPreferencesService prefs = Platform.getPreferencesService();
        for (Option o : Option.values()) {
            String value = prefs.getString(JSLintPlugin.PLUGIN_ID, o.getLowerName(), null, null);
            if (value != null) {
                lint.addOption(o, value);
            }
        }
    }
}
