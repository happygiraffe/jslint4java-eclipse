package com.googlecode.jslint4java.eclipse.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.googlecode.jslint4java.Option;
import com.googlecode.jslint4java.StringArray;
import com.googlecode.jslint4java.eclipse.ui.Activator;

/**
 * This class represents a preference page that is contributed to the Preferences dialog. By
 * subclassing <samp>FieldEditorPreferencePage</samp>, we can use the field support built into JFace
 * that allows us to create a page that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that
 * belongs to the main plug-in class. That way, preferences can be accessed directly via the
 * preference store.
 */

public class OptionsPreferencePage extends FieldEditorPreferencePage implements
        IWorkbenchPreferencePage {

    public OptionsPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    /**
     * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to
     * manipulate various types of preferences. Each field editor knows how to save and restore
     * itself.
     */
    @Override
    public void createFieldEditors() {
        for (Option o : Option.values()) {
            FieldEditor ed = null;
            if (o.getType() == Boolean.class) {
                ed = new BooleanFieldEditor(o.getLowerName(), o.getDescription(), getFieldEditorParent());
            } else if (o.getType() == Integer.class ) {
                ed = new IntegerFieldEditor(o.getLowerName(), o.getDescription(), getFieldEditorParent());
            } else if (o.getType() == StringArray.class) {
                ed = new StringFieldEditor(o.getLowerName(), o.getDescription(), getFieldEditorParent());
            }
            if (ed != null) {
                addField(ed);
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

}