/*
 * $Id: Renderer.java 643 2011-12-06 14:15:30Z snagel $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SysBio/trunk/src/de/zbit/sbml/gui/Renderer.java $
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

package de.zbit.sbml.gui;

import javax.swing.JComponent;

/**
 * A interface for the latex renderer.
 * 
 * @author Sebastian Nagel
 * @since 1.1
 * @version $Rev: 643 $
 */
public interface EquationRenderer {
	
	/**
	 * 
	 * @return
	 */
	public boolean printNamesIfAvailable();
	
	/**
	 * 
	 * @param equation
	 * @return
	 */
	public JComponent renderEquation(String equation);

}
