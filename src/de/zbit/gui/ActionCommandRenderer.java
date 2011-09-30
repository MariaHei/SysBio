/*
 * $Id:  ActionCommandComboBoxModel.java 16:18:36 wrzodek $
 * $URL: ActionCommandComboBoxModel.java $
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2011 by the University of Tuebingen, Germany.
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

import java.awt.Component;
import java.io.Serializable;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * A {@link ComboBoxModel} that displays the names and ToolTips
 * of {@link ActionCommand}s.
 * <p>It furthermore displays {@link Component} directly
 * as components and does not generate a {@link JLabel} with
 * the {@link Component#toString()} method.
 * <p>As last feature, Classes are displayed with
 * {@link Class#getSimpleName()}.
 * 
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class ActionCommandRenderer extends JLabel implements ListCellRenderer, TableCellRenderer, Serializable {
  private static final long serialVersionUID = 6825133145583461124L;
  
  /**
   * If this is <code>TRUE</code>, each <code>value</code> of type
   * {@link Class} will get {@link Class#getName()} as ToolTip.
   */
  public static boolean setToolTipToFullClassNameForClasses=true;
  
  /**
   * Initialize when required.
   */
  private TableCellRenderer defaultTableRenderer = null;
  
  /**
   * Initialize when required.
   */
  private ListCellRenderer defaultListRenderer = null;
  
  
  /* (non-Javadoc)
   * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
   */
  @Override
  public Component getTableCellRendererComponent(JTable table, Object value,
    boolean isSelected, boolean hasFocus, int row, int column) {
    
    // Get properties
    String label = value.toString();
    String toolTip = null;
    Component c = null;
    if (value instanceof Component) {
      c = (Component) value;
    } else if (value instanceof ActionCommand) {
      label = ((ActionCommand)value).getName();
      toolTip = ((ActionCommand)value).getToolTip();
    } else if (value instanceof Class<?>) {
      label = ((Class<?>)value).getSimpleName();
      if (setToolTipToFullClassNameForClasses) {
        toolTip = ((Class<?>)value).getName();
      }
    }
    
    // Generate component
    if (c==null) {
      if (defaultTableRenderer==null) {
        defaultTableRenderer = new DefaultTableCellRenderer();
      }
      c = defaultTableRenderer.getTableCellRendererComponent(table, label, isSelected, hasFocus, row, column);
    }
    if (toolTip!=null && toolTip.length()>0 && (c instanceof JComponent)) {
      ((JComponent)c).setToolTipText(toolTip);
    }
    
    return c;
  }
  
  /* (non-Javadoc)
   * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
   */
  @Override
  public Component getListCellRendererComponent(JList list, Object value,
    int index, boolean isSelected, boolean cellHasFocus) {
    
    // Get properties
    String label = value.toString();
    String toolTip = null;
    Component c = null;
    if (value instanceof Component) {
      c = (Component) value;
    } else if (value instanceof ActionCommand) {
      label = ((ActionCommand)value).getName();
      toolTip = ((ActionCommand)value).getToolTip();
    } else if (value instanceof Class<?>) {
      label = ((Class<?>)value).getSimpleName();
      if (setToolTipToFullClassNameForClasses) {
        toolTip = ((Class<?>)value).getName();
      }
    }
    
    
    // Generate component
    if (c==null) {
      /*
       * Get the systems default renderer.
       */
      if (defaultListRenderer==null) {
        // new DefaultListCellRenderer(); Is not necessarily the default!
        // even UIManager.get("List.cellRenderer"); returns a different value!
        try {
          defaultListRenderer = new JComboBox().getRenderer();
          if (defaultListRenderer==null) {
            defaultListRenderer = (ListCellRenderer) UIManager.get("List.cellRenderer");
          }
        } catch (Throwable t){t.printStackTrace();}
        if (defaultListRenderer==null) {
          defaultListRenderer = new DefaultListCellRenderer();;
        }
      }
      //-------------------
      
      c = defaultListRenderer.getListCellRendererComponent(list, label, index, isSelected, cellHasFocus);
    }
    if (toolTip!=null && toolTip.length()>0 && (c instanceof JComponent)) {
      ((JComponent)c).setToolTipText(toolTip);
    }
    
    return c;
  }
  
  
  
  
}