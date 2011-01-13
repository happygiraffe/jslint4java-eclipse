package com.googlecode.jslint4java.eclipse.ui.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.googlecode.jslint4java.Option;
import com.googlecode.jslint4java.eclipse.ui.JSLintUIPlugin;

/**
 * This class represents a preference page that is contributed to the Preferences dialog. By
 * subclassing <samp>FieldEditorPreferencePage</samp>, we can use the field support built into JFace
 * that allows us to create a page that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that
 * belongs to the main plug-in class. That way, preferences can be accessed directly via the
 * preference store.
 * <p>
 * TODO: trigger a rebuild on change.
 */
public class OptionsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    private CheckboxTableViewer checkboxViewer;

    private final List<FieldEditor> fieldEditors = new ArrayList<FieldEditor>();

    public OptionsPreferencePage() {
        super("jslint4java");
        setPreferenceStore(JSLintUIPlugin.getDefault().getPreferenceStore());
    }

    private void createBooleansArea(Composite main) {
        Font mainFont = main.getFont();
        Composite booleansParent = new Composite(main, SWT.NONE);
        booleansParent.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout decoratorsLayout = new GridLayout();
        decoratorsLayout.marginWidth = 0;
        decoratorsLayout.marginHeight = 0;
        booleansParent.setLayout(decoratorsLayout);
        booleansParent.setFont(mainFont);

        Label decoratorsLabel = new Label(booleansParent, SWT.NONE);
        decoratorsLabel.setText("Toggleable options:");
        decoratorsLabel.setFont(mainFont);

        checkboxViewer = CheckboxTableViewer.newCheckList(booleansParent, SWT.SINGLE | SWT.TOP
                | SWT.BORDER);
        checkboxViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
        checkboxViewer.getTable().setFont(booleansParent.getFont());
        checkboxViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return labelForOption((Option) element);
            }
        });
        checkboxViewer.getTable().setFont(mainFont);

        checkboxViewer.setContentProvider(new IStructuredContentProvider() {
            public void dispose() {
                // Nothing to do on dispose
            }

            // Make an entry for each option
            public Object[] getElements(Object inputElement) {
                @SuppressWarnings("unchecked")
                List<Option> elements = (List<Option>) inputElement;
                return elements.toArray();
            }

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }
        });

    }

    @Override
    protected Control createContents(Composite parent) {
        Font font = parent.getFont();

        Composite main = new Composite(parent, SWT.NULL);
        main.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        main.setLayout(layout);
        main.setFont(font);

        createBooleansArea(main);
        createOtherPrefsArea(main);
        populateBooleansArea();
        populateOtherPrefsArea();

        return main;
    }

    private void createOtherPrefsArea(Composite main) {
        Composite parent = new Composite(main, SWT.NONE);
        parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        parent.setLayout(new GridLayout());

        addIntegerFieldEditor(parent, Option.INDENT);
        addIntegerFieldEditor(parent, Option.MAXERR);
        addIntegerFieldEditor(parent, Option.MAXLEN);
        addStringFieldEditor(parent, Option.PREDEF);
    }

    private void addIntegerFieldEditor(Composite parent, Option o) {
        addFieldEditor(new IntegerFieldEditor(nameOfPref(o), o.getDescription(), parent));
    }

    private void addFieldEditor(FieldEditor fieldEditor) {
        fieldEditor.setPage(this);
        fieldEditor.setPreferenceStore(getPreferenceStore());
        fieldEditors.add(fieldEditor);
    }

    private void addStringFieldEditor(Composite parent, Option o) {
        addFieldEditor(new StringFieldEditor(nameOfPref(o), o.getDescription(), parent));
    }

    private void populateOtherPrefsArea() {
        for (FieldEditor fieldEditor : fieldEditors) {
            fieldEditor.load();
        }
    }

    public void init(IWorkbench workbench) {
    }

    private String labelForOption(Option o) {
        return String.format("%s [%s]", o.getDescription(), o.getLowerName());
    }

    private void populateBooleansArea() {
        List<Option> options = booleanOptions();
        checkboxViewer.setInput(options);
        for (Option option : options) {
            checkboxViewer.setChecked(option, loadBooleanPref(option));
        }
    }

    /** Read the value of a boolean pref. */
    private boolean loadBooleanPref(Option option) {
        return getPreferenceStore().getBoolean(nameOfPref(option));
    }

    /** The preference name an option should use. */
    private String nameOfPref(Option option) {
        return option.getLowerName();
    }

    private List<Option> booleanOptions() {
        List<Option> options = new ArrayList<Option>();
        for (Option o : Option.values()) {
            if (o.getType() == Boolean.class) {
                options.add(o);
            }
        }
        return options;
    }

    @Override
    public boolean performOk() {
        if (super.performOk()) {
            storeBooleanPrefs();
            storeOtherPrefs();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
        performBooleanDefaults();
        performOtherDefaults();
    }

    private void performOtherDefaults() {
        for (FieldEditor fieldEditor : fieldEditors) {
            fieldEditor.loadDefault();
        }
    }

    private void performBooleanDefaults() {
        for (Option o : booleanOptions()) {
            boolean enabled = getPreferenceStore().getDefaultBoolean(nameOfPref(o));
            checkboxViewer.setChecked(o, enabled);
        }
    }

    private void storeOtherPrefs() {
        for (FieldEditor fieldEditor : fieldEditors) {
            fieldEditor.store();
        }
    }

    private void storeBooleanPrefs() {
        for (Option option : booleanOptions()) {
            storeBooleanPref(option, checkboxViewer.getChecked(option));
        }
    }

    private void storeBooleanPref(Option option, boolean enabled) {
        getPreferenceStore().setValue(nameOfPref(option), enabled);
    }

}