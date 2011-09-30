/*
 * $Id$
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

package de.zbit.gui.prefs;

import de.zbit.gui.ActionCommand;

/**
 * @author Florian Mittag
 * @version $Rev$
 */

public class ActionCommandFactory implements ActionCommand {

  protected final String name;
  protected final String tooltip;

  public static ActionCommand create(String name, String tooltip) {
    return new ActionCommandFactory(name, tooltip);
  }
  
  private ActionCommandFactory(String name, String tooltip) {
    this.name = name;
    this.tooltip = tooltip;
  }
  
  public String getName() {
    return name;
  }

  public String getToolTip() {
    return tooltip;
  }
}