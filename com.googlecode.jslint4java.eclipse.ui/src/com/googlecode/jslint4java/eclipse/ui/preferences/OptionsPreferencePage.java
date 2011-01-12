package com.googlecode.jslint4java.eclipse.ui.preferences;

import java.util.Arrays;
import java.util.Collections;

import org.eclipse.jface.preference.PreferencePage;
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

    public OptionsPreferencePage() {
        super("jslint4java");
        setPreferenceStore(JSLintUIPlugin.getDefault().getPreferenceStore());
    }

    public void init(IWorkbench workbench) {
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

        Label info = new Label(main, SWT.NONE);
        info.setText("nothing to see here");
        info.setFont(font);

        createBooleansArea(main);

        return main;
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
                return ((Option) element).getDescription();
            }
        });
        checkboxViewer.getTable().setFont(mainFont);

        checkboxViewer.setContentProvider(new IStructuredContentProvider() {
            public void dispose() {
                // Nothing to do on dispose
            }

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }

            public Object[] getElements(Object inputElement) {
                // Make an entry for each option
                Option[] elements = (Option[]) inputElement;
                Option[] results = new Option[elements.length];
                System.arraycopy(elements, 0, results, 0, elements.length);
                Collections.sort(Arrays.asList(results));
                return results;
            }
        });

    }

}