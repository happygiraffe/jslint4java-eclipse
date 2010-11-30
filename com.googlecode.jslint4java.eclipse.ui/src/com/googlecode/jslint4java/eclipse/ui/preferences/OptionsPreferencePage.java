package com.googlecode.jslint4java.eclipse.ui.preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.googlecode.jslint4java.Option;
import com.googlecode.jslint4java.StringArray;
import com.googlecode.jslint4java.eclipse.ui.JSLintUIPlugin;

/**
 * This class represents a preference page that is contributed to the Preferences dialog. By
 * subclassing <samp>FieldEditorPreferencePage</samp>, we can use the field support built into JFace
 * that allows us to create a page that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that
 * belongs to the main plug-in class. That way, preferences can be accessed directly via the
 * preference store.
 * <p>TODO: trigger a rebuild on change.
 */
public class OptionsPreferencePage extends FieldEditorPreferencePage implements
        IWorkbenchPreferencePage {

    /** Sort boolean options first. Within an option type, sort according to the regular order. */
    private static final class BooleanFirstOptionComparator implements Comparator<Option> {
        public int compare(Option o1, Option o2) {
            if (o1.getType() == Boolean.class) {
                if (o2.getType() == Boolean.class) {
                    return o1.compareTo(o2); // Usual enum order
                } else {
                    return -1; // Bool comes first.
                }
            } else {
                if (o2.getType() == Boolean.class) {
                    return 1; // Bool comes first.
                } else {
                    return o1.compareTo(o2);
                }
            }
        }
    }

    public OptionsPreferencePage() {
        super(GRID);
        setPreferenceStore(JSLintUIPlugin.getDefault().getPreferenceStore());
    }

    /**
     * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to
     * manipulate various types of preferences. Each field editor knows how to save and restore
     * itself.
     */
    @Override
    public void createFieldEditors() {
        List<Option> optionsList = new ArrayList<Option>(Arrays.asList(Option.values()));
        Collections.sort(optionsList, new BooleanFirstOptionComparator());
        for (Option o : optionsList) {
            FieldEditor fe = makeFieldEditor(o);
            if (fe != null) {
                addField(fe);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {
    }

    private BooleanFieldEditor makeBooleanFieldEditor(Option o) {
        return new BooleanFieldEditor(o.getLowerName(), o.getDescription(), getFieldEditorParent());
    }

    /** Nasty. */
    private FieldEditor makeFieldEditor(Option o) {
        Class<?> optionType = o.getType();
        if (optionType == Boolean.class) {
            return makeBooleanFieldEditor(o);
        } else if (optionType == Integer.class) {
            return makeIntegerFieldEditor(o);
        } else if (optionType == StringArray.class) {
            return makeStringArrayFieldEditor(o);
        } else {
            return null;
        }
    }

    private IntegerFieldEditor makeIntegerFieldEditor(Option o) {
        return new IntegerFieldEditor(o.getLowerName(), o.getDescription(), getFieldEditorParent());
    }

    private StringFieldEditor makeStringArrayFieldEditor(Option o) {
        return new StringFieldEditor(o.getLowerName(), o.getDescription(), getFieldEditorParent());
    }

}