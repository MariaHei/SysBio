/*
 * $Id: UncertainProcessNode.java 15:44:17 Meike Aichele$
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SysBio/trunk/src/de/zbit/sbml/layout/UncertainProcessNode.java $
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2013 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package de.zbit.sbml.layout;

import org.sbml.jsbml.ext.layout.Point;

/**
 * @author Meike Aichele
 *
 */
public abstract class UncertainProcessNode<T> implements SBGNNode<T> {

	// uncertain nodes do not have a clone marker
	private final boolean cloneMarker = false;
	
	private Point pointOfContactToProduct;
	
	private Point pointOfContactToSubstrate;
	
	@Override
	public abstract T draw(double x, double y, double z, double width, double height,
			double depth);

	@Override
	public void setCloneMarker() {
		// do nothing because process nodes have no clone marker
	}

	@Override
	public boolean isSetCloneMarker() {
		return cloneMarker;
	}

	/**
	 * @param pointOfContactToSubstrate the pointOfContactToSubstrate to set
	 */
	public void setPointOfContactToSubstrate(Point pointOfContactToSubstrate) {
		this.pointOfContactToSubstrate = pointOfContactToSubstrate;
	}

	/**
	 * @return the pointOfContactToSubstrate
	 */
	public Point getPointOfContactToSubstrate() {
		return pointOfContactToSubstrate;
	}

	/**
	 * @param pointOfContactToProduct the pointOfContactToProduct to set
	 */
	public void setPointOfContactToProduct(Point pointOfContactToProduct) {
		this.pointOfContactToProduct = pointOfContactToProduct;
	}

	/**
	 * @return the pointOfContactToProduct
	 */
	public Point getPointOfContactToProduct() {
		return pointOfContactToProduct;
	}
	
	/**
	 * @return the lineWidth
	 */
	public abstract double getLineWidth();

	/**
	 * @param lineWidth the lineWidth to set
	 */
	public abstract void setLineWidth(double lineWidth);
	
}
