/*
 * $Id:  JTabbedPaneCloseListener.java 12:55:13 Sebastian$
 * $URL$
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

import java.util.EventListener;

import de.zbit.gui.JTabbedPaneDraggableAndCloseable.TabCloseEvent;

/**
 * @author Sebastian Nagel
 * @version $Rev$
 * @since 1.4
 */
public interface JTabbedPaneCloseListener extends EventListener {
	
	/**
	 * called if a tab is about to be closed.
	 * return true if tab should be closed, false otherwise
	 * 
	 * @param evt
	 * @return
	 */
	public boolean tabClosing(TabCloseEvent evt);
	
	
	/**
	 * called after the tab is successfully closed
	 * 
	 * @param evt
	 */
	public void tabClosed(TabCloseEvent evt);

}