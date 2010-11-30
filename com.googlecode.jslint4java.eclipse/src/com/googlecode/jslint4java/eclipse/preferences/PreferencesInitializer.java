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
 * <li> {@link Option#EQEQEQ}
 * <li> {@link Option#UNDEF}
 * <li> {@link Option#WHITE}
 * <li>
 */
public class PreferencesInitializer extends AbstractPreferenceInitializer {

    private final Set<Option> defaultEnable = EnumSet.of(Option.EQEQEQ, Option.UNDEF, Option.WHITE);

    @Override
    public void initializeDefaultPreferences() {
        IEclipsePreferences node = new DefaultScope().getNode(JSLintPlugin.PLUGIN_ID);
        for (Option o : defaultEnable) {
            node.putBoolean(o.getLowerName(), true);
        }
    }

}
