/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2016 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.gui;

import static de.zbit.util.Utils.getMessage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.ItemSelectable;
import java.awt.LayoutManager;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;

import de.zbit.gui.actioncommand.ActionCommandRenderer;
import de.zbit.gui.csv.CSVReaderOptionPanel;
import de.zbit.gui.layout.LayoutHelper;
import de.zbit.gui.layout.VerticalLayout;
import de.zbit.gui.prefs.JComponentForOption;
import de.zbit.io.csv.CSVReader;
import de.zbit.util.ArrayUtils;
import de.zbit.util.Reflect;
import de.zbit.util.ResourceManager;
import de.zbit.util.StringUtil;
import de.zbit.util.Utils;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.SBPreferences;

/**
 * A generic component with a label.
 * 
 * @author Roland Keller
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class JLabeledComponent extends JPanel implements JComponentForOption, ItemSelectable {
  
  /**
   * 
   */
  protected static ResourceBundle bundle = ResourceManager.getBundle(StringUtil.RESOURCE_LOCATION_FOR_LABELS);
  
  /**
   * 
   */
  protected static String exampleError=bundle.getString("INVALID_COLUMN");
  
  /**
   * 
   */
  public static final Insets insets = new Insets(1,3,1,3);
  /**
   * A {@link Logger} for this class.
   */
  public static final transient Logger logger = Logger.getLogger(JLabeledComponent.class.getName());
  
  /**
   * 
   */
  protected static String noOptionChoosen=bundle.getString("NOT_AVAILABLE");
  /**
   * 
   */
  protected static String noSelection="n/a";
  /**
   * Generated serial version identifier.
   */
  protected static final long serialVersionUID = -9026612128266336630L;
  /**
   * Number of columns to define the width of generated textfields.
   */
  private final static int TEXTFIELD_COLUMNS = 20;
  /**
   * Helper Method for GridBagConstrains.
   */
  public static void addComponent(Container container, Component component, int gridx, int gridy,
    int gridwidth, int gridheight, int anchor, int fill) {
    GridBagConstraints gbc = new GridBagConstraints(gridx, gridy, gridwidth, gridheight, 1.0, 1.0,
      anchor, fill, insets, 0, 0);
    container.add(component, gbc);
  }
  /**
   * 
   * @param lh
   * @param jc
   */
  public static void addSelectorsToLayout(LayoutHelper lh, JLabeledComponent jc) {
    addSelectorsToLayout(lh, jc, false);
  }
  /**
   * 
   * @param lh
   * @param jc
   * @param addSpace
   */
  public static void addSelectorsToLayout(LayoutHelper lh,
    JLabeledComponent jc, boolean addSpace) {
    int x = 0;
    lh.ensurePointerIsAtBeginningOfARow();
    
    lh.add(jc.label, (x++), lh.getRow(), 1, 1, 0d, 0d);
    lh.add(new JPanel(), (x++), lh.getRow(), 1, 1, 0d, 0d);
    lh.add(jc.colChooser, (x++), 1, 1, 0d, 0d);
    if (addSpace) {
      lh.add(new JPanel(), 0, (x++), 1, 0d, 0d);
    }
  }
  
  /**
   * create a {@link JSpinner} that respects the properties
   * of the given option. I.e. the range, initalVale, Numeric
   * datatype (Integer, Double, etc.),...
   * @param <T>
   * @param option
   * @param initialValue might be {@code null}.
   * @return
   */
  @SuppressWarnings({ "rawtypes" })
  public static <T extends Number> JSpinner buildJSpinner(Option<T> option, T initialValue) {
    
    // Try to get min and max (optional parameters, might be null)
    T minimum = option.isSetRangeSpecification() ? option.getRange().getMinimum() : null;
    T maximum = option.isSetRangeSpecification() ? option.getRange().getMaximum() : null;
    
    // Get an initial value (required parameter)
    if (initialValue == null) {
      initialValue = option.getDefaultValue();
    }
    if (initialValue == null) {
      initialValue = (minimum != null) ? minimum : maximum;
    }
    if (initialValue == null) {
      initialValue = option.parseOrCast("0");
    }
    
    // Get the step size (required parameter)
    T stepSize = null;
    if ((minimum != null) && (maximum != null)) {
      // Initially define 100 steps between minimum and maximum.
      double d = (maximum.doubleValue() - minimum.doubleValue()) / 100;
      try {
        // 0.95 is INTEGER parsed to "0" => round before doing that
        //stepSize = option.parseOrCast(d);
        if (Utils.isInteger(option.getRequiredType())) {
          d = Math.ceil(d);
        }
        stepSize = option.parseOrCast(d);
      } catch (Throwable exc) {
        logger.finest(getMessage(exc));
      }
    }
    if ((stepSize == null) || (stepSize.doubleValue() == 0d)) {
      stepSize = option.parseOrCast("1");
    }
    
    // Create a number model with these values
    SpinnerModel myModel = new SpinnerNumberModel((Number) initialValue, (Comparable) minimum, (Comparable) maximum, (Number) stepSize);
    JSpinner spinner = new JSpinner(myModel);
    
    // Set width
    if (spinner.getEditor() instanceof JSpinner.DefaultEditor) {
      ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setColumns(TEXTFIELD_COLUMNS);
    }
    
    // Show smaller numbers if data type is double
    if (Double.class.isAssignableFrom(initialValue.getClass())) {
      spinner.setEditor(new JSpinner.NumberEditor(spinner, "#." + StringUtil.replicateCharacter("#", TEXTFIELD_COLUMNS - 2)));
      JSpinner.NumberEditor editor = (JSpinner.NumberEditor) spinner.getEditor();
      DecimalFormat format = editor.getFormat();
      format.setMaximumFractionDigits(TEXTFIELD_COLUMNS - 2);
      editor.getTextField().setHorizontalAlignment(SwingConstants.RIGHT);
    }
    
    if (option.isSetDescription()) {
      spinner.setToolTipText(option.getDescription());
    }
    
    return spinner;
  }
  /**
   * 
   * @param model
   * @return
   */
  protected static JComponent getColumnChooser(ComboBoxModel model) {
    return getColumnChooser(model, -1, false, null, true);
  }
  /**
   * 
   * @param model
   * @param defaultValue
   * @param required
   * @param l
   * @param acceptOnlyIntegers
   * @return
   */
  protected static JComponent getColumnChooser(ComboBoxModel model, int defaultValue, boolean required, ActionListener l, boolean acceptOnlyIntegers) {
    return getColumnChooser(model, defaultValue, required, l, acceptOnlyIntegers, false);
  }
  
  /**
   * 
   * @param model
   * @param defaultValue
   * @param required
   * @param l
   * @param acceptOnlyIntegers
   * @param secret is it legal do display the value to the user? For instance, password field
   * @return
   * @see #getColumnChooser(ComboBoxModel, int, boolean, ActionListener, boolean)
   */
  public static JComponent getColumnChooser(ComboBoxModel model,
    int defaultValue, boolean required, ActionListener l,
    boolean acceptOnlyIntegers, boolean secret) {
    JComponent ret;
    
    if ((model != null) && (model.getSize() > 0)) {
      JComboBox cb = new JComboBox(model);
      // XXX: Feature possibility: remove the selected element in all other ComboBoxes.
      if (l != null) {
        cb.addActionListener(l);
      }
      
      int def = defaultValue + (required ? 0 : 1);
      if ((def < 0) || (def >= model.getSize())) {
        def = 0;
      }
      cb.setSelectedIndex(def);
      // Configure an improved renderer that allows more customization.
      cb.setRenderer(new ActionCommandRenderer());
      ret = cb;
    } else {
      if (defaultValue < 0) {
        defaultValue=0;
      }
      JTextField tf;
      if (acceptOnlyIntegers) {
        tf =CSVReaderOptionPanel.buildIntegerBox(Integer.toString(defaultValue), l);
      } else {
        if (secret) {
          tf = new JPasswordField(defaultValue);
        } else {
          tf = new JTextField(defaultValue);
        }
        tf.setColumns(TEXTFIELD_COLUMNS);
        tf.addActionListener(l);
      }
      ret = tf;
    }
    
    return ret;
  }
  /**
   * Sequentially searches a string in an unsorted array.
   * @param array
   * @param toSearch
   * @return index if found, -1 else.
   */
  protected static int indexOf(Object[] array, Object toSearch) {
    for (int i=0; i<array.length; i++) {
      if (array[i].equals(toSearch)) {
        return i;
      }
    }
    return -1;
  }
  
  /**
   * Set the static string to use in optional combo boxes if no
   * column has been selected.
   * @param s
   */
  public static void setStringComboBoxNoOptionChoosen(String s) {
    noOptionChoosen = s;
  }
  /**
   * Set the static string to use if the user selects a column
   * which is missing in the given preview.
   * @param s
   */
  public static void setStringExampleError(String s) {
    exampleError = s;
  }
  /**
   * Set the static string to use for the preview in optional combo
   * boxes if no column has been selected.
   * @param s
   */
  public static void setStringExampleNoOptionChoosen(String s) {
    noSelection = s;
  }
  
  /**
   * Show a dialog with multiple {@link JLabeledComponent}s. Each component
   * has the label given in {@code fields} and the selections from the
   * {@code suggestions} variable.
   * @param parent the parent to which this dialog is modal
   * @param title  title for this dialog
   * @param fields labels for {@link JLabeledComponent}s
   * @param suggestions available choices for {@link JLabeledComponent}s (same indices as fields)
   * @param fixedSuggestions if true, only choices from the given selections arrays are allowed.
   * If false, users may enter custom strings.
   * @return array with selected values for each field
   */
  public static String[] showDialog(Component parent, String title, String[] fields, String[][] suggestions, boolean fixedSuggestions) {
    
    // Initialize the panel
    final JPanel c = new JPanel(new VerticalLayout());
    for (int i=0; i<fields.length; i++) {
      JLabeledComponent l = new JLabeledComponent(fields[i],true, suggestions[i]);
      if (!fixedSuggestions) {
        l.setEditHeaderAllowed(true);
      }
      // Make a flexible layout
      l.setLayout(new FlowLayout());
      l.setPreferredSize(null);
      c.add(l);
    }
    c.setBorder(new TitledBorder(title));
    
    // Show as Dialog
    int button = GUITools.showAsDialog(parent, c, title, true);
    
    // OK Pressed?
    boolean okPressed = (button==JOptionPane.OK_OPTION);
    if (!okPressed) {
      return null;
    } else {
      String[] ret = new String[fields.length];
      for (int i=0; i<fields.length; i++) {
        ret[i] = ((JLabeledComponent)c.getComponent(i)).getSelectedItem().toString();
      }
      return ret;
    }
  }
  
  /**
   * This should always be true. Just if you want to use this
   * class not for "Choosing columns" but other stuff, you may
   * want to change this behavior.
   */
  protected boolean acceptOnlyIntegers = true;
  
  /**
   * 
   */
  protected JComponent colChooser;
  
  /**
   * If true, the user may edit the given header strings.
   */
  private boolean editHeaderAlllowed = false;
  
  /**
   * 
   */
  protected Object[] headers = null;
  
  /**
   * Every integer added here corresponds to one column number. If an
   * integer is added, the column will be hidden in all ColumnChoosers.
   */
  protected List<Integer> hideColumns = new ArrayList<Integer>();
  
  /**
   * 
   */
  protected JLabel label;
  
  /**
   * 
   */
  protected ComboBoxModel model = null;
  
  /**
   * Only necessary for using this class in Combination with
   * {@link SBPreferences} and {@link Option}s.
   */
  protected Option<?> option = null;
  
  /**
   * 
   */
  protected boolean required;
  
  /**
   * If unsorted, the columns appear as they appear in the file. Else,
   * they are sorted alphabetically.
   */
  protected boolean sortHeaders = false;
  
  // Label options
  /**
   * 
   */
  protected String titel;
  
  // Column selector options
  /**
   * Use a JTextField instead of JComboBoxes
   */
  protected boolean useJTextField = false;
  
  private boolean secret = false;
  
  /**
   * Creates a new column chooser which let's the user choose
   * columns with {@link JTextField}s if no columnHeaders will be given.
   * @param title - Label caption for this column chooser
   * @param fieldIsRequired - If not required, this class will add
   * a NoOptionChoosen String at the start of the box.
   */
  public JLabeledComponent(String title, boolean fieldIsRequired) {
    this(title, fieldIsRequired, (Object[])null);
  }
  
  /**
   * @param title
   * @param fieldIsRequired
   * @param b
   */
  public JLabeledComponent(String title, boolean fieldIsRequired, boolean acceptOnlyIntegers) {
    this(title,fieldIsRequired);
    setAcceptOnlyIntegers(acceptOnlyIntegers);
  }
  
  /**
   * 
   * @param title
   * @param fieldIsRequired
   * @param columnHeaders
   */
  public JLabeledComponent(String title, boolean fieldIsRequired,
    Collection<?> columnHeaders) {
    this(title, fieldIsRequired, columnHeaders.toArray());
  }
  
  /**
   * @param title
   * @param fieldIsRequired
   * @param colorChooserWithPreview
   */
  public JLabeledComponent(String title, boolean fieldIsRequired,
    JComponent c) {
    this (title, fieldIsRequired, new Object[] {c});
    GUITools.replaceComponent(colChooser, c);
    colChooser = c;
    label.setLabelFor(c);
  }
  
  /**
   * Creates a new column chooser panel with the given headers.
   * @param title - Label caption for this column chooser
   * @param fieldIsRequired - If not required, this class will add
   * a NoOptionChoosen String at the start of the box.
   * @param columnHeaders - Column Headers
   */
  public JLabeledComponent(String title, boolean fieldIsRequired, Object[] columnHeaders) {
    this(title, fieldIsRequired, columnHeaders, false);
  }
  
  public JLabeledComponent(String title, boolean fieldIsRequired,
    Object[] columnHeaders, boolean secret) {
    super();
    initGUI();
    
    setTitle(title);
    setName(title);
    // setHeaders handles this variable.. so set it directly.
    required = fieldIsRequired;
    setHeaders(columnHeaders);
    
    label.setLabelFor(colChooser);
    this.secret  = secret;
  }
  
  /**
   * Add any action Listener to the combo box.
   * Be careful: The action listeners are erased when the selector
   * changes (by setting new headers or refreshing).
   * @param l
   */
  public void addActionListener(ActionListener l) {
    if (colChooser instanceof JComboBox) {
      ((JComboBox) colChooser).addActionListener(l);
    } else if (colChooser instanceof JTextField) {
      ((JTextField) colChooser).addActionListener(l);
    } else {
      Reflect.invokeIfContains(colChooser, "addActionListener", ActionListener.class, l);
    }
  }
  
  /**
   * Adds a listener to this {@link #colChooser}, if the
   * current {@link #colChooser} supports {@link ChangeListener}s.
   * 
   * @param listener the {@code ChangeListener} to add
   */
  public void addChangeListener(ChangeListener listener) {
    // For JSpinners
    Reflect.invokeIfContains(colChooser, "addChangeListener", ChangeListener.class, listener);
  }
  
  /**
   * 
   * @param il
   */
  @Override
  public synchronized void addItemListener(ItemListener il) {
    JComponent comp = getColumnChooser();
    if (comp instanceof JComboBox) {
      ((JComboBox) comp).addItemListener(il);
    } else {
      // otherwise not possible!
      Reflect.invokeIfContains(colChooser, "addItemListener", ItemListener.class, il);
    }
  }
  
  /*
   * (non-Javadoc)
   * @see java.awt.Component#addKeyListener(java.awt.event.KeyListener)
   */
  @Override
  public synchronized void addKeyListener(KeyListener l) {
    super.addKeyListener(l);
    if (colChooser != null) {
      colChooser.addKeyListener(l);
    }
  }
  
  /**
   * 
   * @param newHeader
   * @return
   */
  protected ComboBoxModel buildComboBoxModel(Object[] newHeader) {
    if ((newHeader == null) || (newHeader.length < 1)) {
      return null;
    }
    
    // Create a list, hiding all unwanted elements
    Vector<Object> modelHeaders = new Vector<Object>();
    Map<String, Object> headersToDisplay = new HashMap<String, Object>();
    for (int i = 0; i < newHeader.length; i++) {
      if (hideColumns.contains(i)) {
        continue;
      }
      headersToDisplay.put(newHeader[i].toString(), newHeader[i]);
      modelHeaders.add(newHeader[i]);
    }
    
    // Sort eventually
    if (sortHeaders) {
      ArrayList<String> keys = new ArrayList<String>(headersToDisplay.keySet());
      Collections.sort(keys);
      for (int i = 0; i < keys.size(); i++) {
        modelHeaders.set(i, headersToDisplay.get(keys.get(i)));
      }
    }
    
    // If not required, add noOptionChoosen
    if (!required) {
      modelHeaders.add(0, noOptionChoosen);
    }
    
    // Build the model
    return new DefaultComboBoxModel(modelHeaders);
  }
  
  /**
   * Returns the actual column chooser object, which is either
   * a {@link JComboBox} or a {@link JTextField}.
   * @return
   */
  public JComponent getColumnChooser() {
    return colChooser;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.prefs.JComponentForOption#getCurrentValue()
   */
  @Override
  public Object getCurrentValue() {
    return getSelectedItem();
  }
  
  /**
   * @return the current {@link #headers}.
   */
  public Object[] getHeaders() {
    return headers;
  }
  
  /**
   * @return the {@link JTextComponent} associated with the current {@link #getColumnChooser()}.
   */
  public JTextComponent getJTextComponent() {
    if (colChooser == null) {
      return null;
    }
    if (colChooser instanceof JComboBox) {
      return (JTextComponent) ((JComboBox) colChooser).getEditor().getEditorComponent();
    } else if (colChooser instanceof JTextComponent) {
      return ((JTextComponent) colChooser);
    } else {
      return null; // Unknown
    }
  }
  
  
  /**
   * 
   * @return
   */
  public JLabel getLabel() {
    return label;
  }
  
  /*
   * (non-Javadoc)
   * @see java.awt.Component#getName()
   */
  @Override
  public String getName() {
    return super.getName();
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.prefs.JComponentForOption#getOption()
   */
  @Override
  public Option<?> getOption() {
    return option;
  }
  
  /**
   * Returns the selected item (Usually a header string).
   * @return
   */
  public Object getSelectedItem() {
    if (colChooser instanceof JComboBox) {
      return ((JComboBox)colChooser).getSelectedItem();
    } else if (colChooser instanceof JTextComponent) {
      String s = ((JTextComponent)colChooser).getText();
      return s;
    } else if (colChooser instanceof ColorChooserWithPreview) {
      return colChooser.getBackground();
    } else if (colChooser instanceof JSpinner) {
      return ((JSpinner) colChooser).getValue();
    } else {
      logger.severe(String.format(
        "Please implement getSelectedItem() for %s in JLabeledComponent.",
        colChooser.getClass().toString()));
      return null;
    }
  }
  
  /* (non-Javadoc)
   * @see java.awt.ItemSelectable#getSelectedObjects()
   */
  @Override
  public Object[] getSelectedObjects() {
    return ArrayUtils.toArray(getSelectedItem());
  }
  
  /**
   * Returns the selected index. Accounts automatically for sorting and
   * required or optional.
   * 
   * @return integer between -1 and headers.length.
   */
  public int getSelectedValue() {
    if (colChooser == null) {
      return -1;
    }
    if (colChooser instanceof JComboBox) {
      if (!sortHeaders) {
        if (required) {
          return ((JComboBox) colChooser).getSelectedIndex();
        } else {
          return ((JComboBox) colChooser).getSelectedIndex() - 1;
        }
      } else {
        return indexOf(headers, getSelectedItem().toString());
      }
    } else if (colChooser instanceof JTextComponent) {
      String s = ((JTextComponent)colChooser).getText().trim();
      
      if (CSVReader.isNumber(s, true)) {
        return Integer.parseInt(s);
      } else {
        return -1;
      }
    } else {
      return -1; // Unknown
    }
  }
  
  /**
   * 
   */
  protected void initGUI() {
    setPreferredSize(new Dimension(400, 25));
    //layout = new GridBagLayout();
    GridLayout layout = new GridLayout(1,3,10,2);
    setLayout(layout);
  }
  
  /**
   * @return acceptOnlyIntegers
   */
  public boolean isAcceptOnlyIntegers() {
    return acceptOnlyIntegers;
  }
  
  /**
   * @return if this is a required column chooser. If not required
   * this class will add a NoOptionChoosen String at the start of the box.
   */
  public boolean isRequired() {
    return required;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.prefs.JComponentForOption#isSetOption()
   */
  @Override
  public boolean isSetOption() {
    return option!=null;
  }
  
  /**
   * Should only be called when the layout changed.
   */
  protected void layoutElements() {
    LayoutManager l = getLayout();
    
    if (label!=null) {
      l.removeLayoutComponent(label);
    }
    if (colChooser!=null) {
      l.removeLayoutComponent(colChooser);
    }
    if (l instanceof BorderLayout) {
      BorderLayout c = (BorderLayout) l;
      if (label!=null) {
        c.addLayoutComponent(label, BorderLayout.WEST);
      }
      if (colChooser!=null) {
        c.addLayoutComponent(colChooser, BorderLayout.CENTER);
      }
      
    } else if (l instanceof GridBagLayout) {
      GridBagLayout c = (GridBagLayout) l;
      
      GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0);
      
      if (label!=null) {
        gbc.gridx=0;
        c.addLayoutComponent(label, gbc);
      }
      if (colChooser!=null) {
        gbc.gridx=1;
        c.addLayoutComponent(colChooser, gbc);
      }
      
    } else {
      if (label!=null) {
        l.addLayoutComponent("Titel", label);
      }
      if (colChooser!=null) {
        l.addLayoutComponent("ColChooser", colChooser);
      }
    }
  }
  
  /**
   * Refresh the whole panel.
   */
  public void refresh() {
    refreshLabel();
    refreshSelector();
    
    validateRepaint();
  }
  
  /**
   * Builds the ComboBoxModel based on the current header,
   * refreshs the selector with this model and validates/
   * repaints the panel.
   */
  protected void refreshAndRepaint() {
    model = buildComboBoxModel(headers);
    
    refreshSelector(true);
    validateRepaint();
  }
  
  /**
   * 
   */
  protected void refreshLabel() {
    // Create it
    if (label==null) {
      label = new JLabel(titel);
      if (getLayout() instanceof GridBagLayout) {
        addComponent(this, label, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL);
      } else if (getLayout() instanceof BorderLayout) {
        add(label, BorderLayout.WEST);
      } else {
        add(label); // e.g. GridLayout
      }
    } else {
      label.setText(titel);
    }
  }
  
  /**
   * 
   */
  protected void refreshSelector() {
    refreshSelector(false);
  }
  
  /**
   * @param onlySetNewModel if true and the colChooser is an already initialized
   * {@link JComboBox}, this method will not create a new box, but simply change the model
   * of the exiting one.
   */
  protected void refreshSelector(boolean onlySetNewModel) {
    // Remember last selection
    int id = colChooser != null ? getSelectedValue() : -1;
    
    // Build column chooser or only change existing model
    if (onlySetNewModel && (model != null) && (colChooser != null) && (colChooser instanceof JComboBox)) {
      ((JComboBox) colChooser).setModel(model);
    } else {
      // Note: replacing an existing colChooser will also remove all existing listeners!
      if (colChooser != null) {
        remove(colChooser);
      }
      colChooser = getColumnChooser(useJTextField ? null : model, -1, required, null, acceptOnlyIntegers, secret);
      
      // Add to layout
      if (getLayout() instanceof GridBagLayout) {
        addComponent(this, colChooser, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL);
      } else if (getLayout() instanceof BorderLayout) {
        add(colChooser, BorderLayout.CENTER);
      } else {
        add(colChooser); // e.g. GridLayout
      }
    }
    
    // Set Properties
    if (colChooser instanceof JComboBox) {
      ((JComboBox) colChooser).setEditable(editHeaderAlllowed);
    }
    colChooser.setToolTipText(getToolTipText());
    
    // Try to restore old selection
    if (id >= 0) {
      setSelectedValue(id);
    }
  }
  
  /**
   * Removes a {@code ChangeListener} from this {@link #colChooser}.
   *
   * @param listener the {@code ChangeListener} to remove
   */
  public void removeChangeListener(ChangeListener listener) {
    // For JSpinners
    Reflect.invokeIfContains(colChooser, "removeChangeListener", ChangeListener.class, listener);
  }
  
  /* (non-Javadoc)
   * @see java.awt.ItemSelectable#removeItemListener(java.awt.event.ItemListener)
   */
  @Override
  public void removeItemListener(ItemListener l) {
    JComponent comp = getColumnChooser();
    if (comp instanceof JComboBox) {
      ((JComboBox) comp).removeItemListener(l);
    } else {
      // otherwise not possible!
      Reflect.invokeIfContains(colChooser, "removeItemListener", ItemListener.class, l);
    }
  }
  
  /**
   * This should always be true. Just if you want to use this
   * class not for "Choosing columns" but other stuff, you may
   * want to change this behaviour.
   * 
   * @param acceptOnlyIntegers
   */
  public void setAcceptOnlyIntegers(boolean acceptOnlyIntegers) {
    if (this.acceptOnlyIntegers != acceptOnlyIntegers) {
      this.acceptOnlyIntegers = acceptOnlyIntegers;
      refreshSelector();
    }
  }
  
  /**
   * Set the default value of the column chooser.
   * Does account for required or optional settings
   * (adds 1 for optional).
   * Also automaticaly accounts for sorting.
   * 
   * @param i - index number
   */
  public void setDefaultValue(int i) {
    if (!sortHeaders) {
      if (!required) {
        i+=1;
      }
      if (i<0) {
        i=0;
      }
      if (model!=null && i>=model.getSize()) {
        i = model.getSize()-1;
      }
      
      if (model!=null && i<model.getSize()) {
        model.setSelectedItem(model.getElementAt(i));
      }
    } else {
      // Search for position of the given item.
      if (i<0 || i>=headers.length) {
        return;
      }
      for (int j=0; j<model.getSize(); j++) {
        if (model.getElementAt(j).equals(headers[i])) {
          i=j;
          break;
        }
      }
    }
    setSelectedValue(i);
  }
  
  /**
   * Set the default value of the column chooser.
   * @param s set the default index to this value's index.
   */
  public void setDefaultValue(Object defaultV) {
    if (model!=null) {
      model.setSelectedItem(defaultV);
    } else {
      setDefaultValue(defaultV.toString());
    }
  }
  
  /**
   * Set the default value of the column chooser.
   * @param s set the default index to this value's index.
   */
  public void setDefaultValue(String s) {
    if (model!=null) {
      model.setSelectedItem(s);
    } else {
      if (CSVReader.isNumber(s, true)) {
        setDefaultValue(Integer.parseInt(s));
      } else if (colChooser instanceof JTextComponent) {
        ((JTextComponent) colChooser).setText(s);
      }
    }
  }
  
  /**
   * @param b if true, the given {@link #headers} may be edited
   * and customized by the user!
   */
  public void setEditHeaderAllowed(boolean b) {
    editHeaderAlllowed=b;
    if (colChooser instanceof JComboBox) {
      ((JComboBox) colChooser).setEditable(b);
    } else {
      Reflect.invokeIfContains(colChooser, "setEditable", Boolean.class, b);
    }
  }
  
  /* (non-Javadoc)
   * @see javax.swing.JComponent#setEnabled(boolean)
   */
  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    if (label!=null) {
      label.setEnabled(enabled);
    }
    if (colChooser!=null) {
      colChooser.setEnabled(enabled);
    }
  }
  
  /**
   * @see #setHeaders(Object[])
   * @param header
   */
  public void setHeaders(Collection<?> header) {
    setHeaders(header.toArray(), 0);
  }
  
  /**
   * Set the headers to display in the combo box.
   * This function will fill the combo box with
   * "Column 1", "Column 2",...
   * @param numberOfColumns
   */
  public void setHeaders(int numberOfColumns) {
    setHeaders (null, numberOfColumns);
  }
  
  /**
   * Set the headers to display in the combo box. This function will fill empty
   * fields in the combo box with "(Column i)". if header is null, behaves like
   * {@link #setHeaders(int)}.
   * 
   * @param header
   */
  public void setHeaders(Object[] header) {
    setHeaders(header, 0);
  }
  
  /**
   * Set the headers to display in the combo box.
   * This function will fill empty fields in the combo box
   * with "(Column i)".
   * If numberOfColumns is greater than header.length, it will
   * extend the header to the numberOfColumns filling it wil
   * "(Column i)".
   * @param header
   * @param numberOfColumns
   */
  public void setHeaders(Object[] header, int numberOfColumns) {
    // Set the header
    Object[] newHeader = header;
    int maxSize = newHeader != null ? newHeader.length:numberOfColumns;
    maxSize = Math.max(numberOfColumns, maxSize);
    newHeader = new Object[maxSize];
    if (header != null) {
      System.arraycopy(header, 0, newHeader, 0, header.length);
    }
    String column = bundle.getString("COLUMN");
    for (int i = 0; i < newHeader.length; i++) {
      // Completely empty array
      if (header == null) {
        newHeader[i] = column + (i + 1);
      }
      // Just fill missing gaps
      else if ((newHeader[i] == null) || (newHeader[i].toString().trim().length() < 1)) {
        newHeader[i] = '(' + column + (i + 1) + ')';
      }
    }
    
    // Remember required headers and build model
    headers = newHeader;
    refreshAndRepaint();
  }
  
  /**
   * Set the visibility of a column. This will hide the
   * header of the column with the given number, so the user can't
   * choose that column.
   * Try to set multiple columns at once, because calling this class
   * will also lead to refreshing and repainting the column chooser.
   * @param columnNumber - from 0 to n.
   * @param visible - true or false. Default: true (visible).
   */
  public void setHeaderVisible(int columnNumber, boolean visible) {
    setHeaderVisible(new int[]{columnNumber}, visible);
  }
  
  /**
   * Set the visibility of certain columns. This will hide the
   * header of the column with the given number, so the user can't
   * choose that column.
   * Try to set multiple columns at once, because calling this class
   * will also lead to refreshing and repainting the column chooser.
   * @param columnNumbers - from 0 to n.
   * @param visible - true or false. Default: true (visible).
   */
  public void setHeaderVisible(int[] columnNumbers, boolean visible) {
    
    // Internally, there is just one array with columns to hide.
    // The array is changed here to reflect the desired visibilities.
    boolean performedChanges=false;
    for (int i=0; i<columnNumbers.length; i++) {
      int pos = hideColumns.indexOf(columnNumbers[i]);
      if (pos>=0) {
        if (visible) {
          hideColumns.remove(pos);
          performedChanges=true;
        }
      } else {
        if (!visible) {
          hideColumns.add(columnNumbers[i]);
          performedChanges=true;
        }
      }
    }
    
    // If changed, change active ColumnChoosers
    if (performedChanges) {
      refreshAndRepaint();
    }
  }
  
  /*
   * (non-Javadoc)
   * @see java.awt.Container#setLayout(java.awt.LayoutManager)
   */
  @Override
  public void setLayout(LayoutManager manager) {
    super.setLayout(manager);
    layoutElements();
  }
  
  /*
   * (non-Javadoc)
   * @see java.awt.Component#setName(java.lang.String)
   */
  @Override
  public void setName(String name) {
    super.setName(name);
    if (colChooser != null) {
      colChooser.setName(name);
    }
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.prefs.JComponentForOption#setOption(de.zbit.util.prefs.Option)
   */
  @Override
  public void setOption(Option<?> option) {
    this.option=option;
  }
  
  /**
   * @param required - If not required, this class will add
   * a NoOptionChoosen String at the start of the box.
   */
  public void setRequired(boolean required) {
    if (this.required != required) {
      this.required = required;
      model = buildComboBoxModel(headers);
      refreshSelector();
      validateRepaint();
    }
  }
  
  /**
   * Set the selected item to the given one.
   * @param string
   */
  public void setSelectedItem(Object string) {
    if (colChooser instanceof JComboBox) {
      ((JComboBox) colChooser).setSelectedItem(string);
    } else if (colChooser instanceof JTextComponent) {
      int pos = indexOf(headers, string.toString());
      if (pos < 0) {
        if (CSVReader.isNumber(string.toString(), false)) {
          ((JTextComponent) colChooser).setText(string.toString());
        }
      } else {
        ((JTextComponent) colChooser).setText(Integer.toString(pos));
      }
    } else if (colChooser instanceof ColorChooserWithPreview) {
      if (string instanceof java.awt.Color) {
        ((ColorChooserWithPreview) colChooser).setColor((Color)string);
      }
    } else {
      logger.warning(String.format(
        "Please implement setSelectedItem for %s.", colChooser.getClass().toString()));
    }
  }
  
  /**
   * Set the selected index to i. Does NOT account for required or
   * optional (add 1 for optional) or sortation. Use {@link #setDefaultValue(String)}
   * or {@link #setDefaultValue(int))} to account for that.
   * @param i
   */
  public void setSelectedValue(int i) {
    if (colChooser instanceof JComboBox) {
      if ((i >= 0) && (i < ((JComboBox) colChooser).getModel().getSize())) {
        ((JComboBox) colChooser).setSelectedIndex(i);
      }
    } else if (colChooser instanceof JTextComponent) {
      ((JTextComponent) colChooser).setText(Integer.toString(i));
    } else {
      logger.warning(String.format(
        "Cannot set selected integer value on %s",
        colChooser.getClass().toString()));
    }
  }
  
  /**
   * Decide, wether you want the headers to appear sorted, or not.
   * @param sort - True: headers appear sorted. False: headers appear
   * in the same ordering as they appear in the file.
   */
  public void setSortHeaders(boolean sort) {
    if (sort!=sortHeaders) {
      // Keep the selection
      Object item = getSelectedItem();
      
      sortHeaders = sort;
      refreshAndRepaint();
      
      // Restore selection.
      setSelectedItem(item);
    }
  }
  
  /**
   * Sets the title (the label caption) for this column chooser.
   * @param title
   */
  public void setTitle(String title) {
    titel = title;
    refreshLabel();
    validateRepaint();
  }
  
  
  /*
   * (non-Javadoc)
   * @see javax.swing.JComponent#setToolTipText(java.lang.String)
   */
  @Override
  public void setToolTipText(String s) {
    super.setToolTipText(s);
    if (colChooser!=null) {
      colChooser.setToolTipText(s);
    }
  }
  
  /**
   * Do you want to use a {@link JTextField} (true) or
   * a {@link JComboBox} (false) to choose the column?
   * Default: {@link JComboBox}(false).
   * @param useTextField
   */
  public void setUseJTextField(boolean useTextField) {
    useJTextField = useTextField;
    refreshSelector();
    validateRepaint();
  }
  
  /**
   * 
   */
  protected void validateRepaint() {
    validate();
    repaint();
  }
  
}
