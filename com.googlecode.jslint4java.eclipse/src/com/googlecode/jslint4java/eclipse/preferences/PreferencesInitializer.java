package com.googlecode.jslint4java.eclipse.preferences;

import java.util.EnumSet;
import java.util.Set;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import com.googlecode.jslint4java.Option;
import com.googlecode.jslint4java.eclipse.JSLintPlugin;

/**
 * Set up the default preferences. By default,we enable:
 * <ul>
 * <li> {@link Option#EQEQ}
 * <li> {@link Option#UNDEF}
 * <li> {@link Option#WHITE}
 * </ul>
 * <p>
 * And assign default values to:
 * <ul>
 * <li> {@link Option#INDENT}
 * <li> {@link Option#MAXERR}
 * </ul>
 */
public class PreferencesInitializer extends AbstractPreferenceInitializer {

    private static final int DEFAULT_INDENT = 4;
    private static final int DEFAULT_MAXERR = 50;

    private final Set<Option> defaultEnable = EnumSet.of(Option.EQEQ, Option.UNDEF, Option.WHITE);

    @Override
    public void initializeDefaultPreferences() {
        IEclipsePreferences node = DefaultScope.INSTANCE.getNode(JSLintPlugin.PLUGIN_ID);
        for (Option o : defaultEnable) {
            node.putBoolean(o.getLowerName(), true);
        }
        // Hand code these.
        node.putInt(Option.INDENT.getLowerName(), DEFAULT_INDENT);
        node.putInt(Option.MAXERR.getLowerName(), DEFAULT_MAXERR);
    }

}
