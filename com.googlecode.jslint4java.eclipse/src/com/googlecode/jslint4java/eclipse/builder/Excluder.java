package com.googlecode.jslint4java.eclipse.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;

import com.googlecode.jslint4java.eclipse.JSLintPlugin;

/**
 * Maintain the list of paths to exclude from linting. Will be automatically
 * updated when the prefs value changes.
 */
public class Excluder {
    public static final String EXCLUDE_PATH_REGEXES_PREFERENCE = "exclude_path_regexes";

    private final List<Pattern> excludes = new ArrayList<Pattern>();

    public Excluder() {
        readPref();
        monitor();
    }

    /** Return the list of patterns to exclude from linting. */
    public List<Pattern> getExcludes() {
        return new ArrayList<Pattern>(excludes);
    }

    /** Should {@code file} be excluded from linting? */
    public boolean isExcluded(IFile file) {
        String filePath = file.getFullPath().toString();
        if (!excludes.isEmpty()) {
            for (Pattern p : excludes) {
                if (p.matcher(filePath).find()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void monitor() {
        IEclipsePreferences scope = new InstanceScope().getNode(JSLintPlugin.PLUGIN_ID);
        scope.addPreferenceChangeListener(new IPreferenceChangeListener() {
            public void preferenceChange(PreferenceChangeEvent ev) {
                if (ev.getKey().equals(EXCLUDE_PATH_REGEXES_PREFERENCE)) {
                    parsePref((String) ev.getNewValue());
                }
            }
        });
    }

    private void parsePref(String pref) {
        excludes.clear();
        if (pref.isEmpty()) {
            return;
        }
        for (String path : pref.split(",")) {
            excludes.add(Pattern.compile(path));
        }

    }

    private void readPref() {
        IPreferencesService prefs = Platform.getPreferencesService();
        parsePref(prefs.getString(JSLintPlugin.PLUGIN_ID, EXCLUDE_PATH_REGEXES_PREFERENCE, "", null));
    }
}
