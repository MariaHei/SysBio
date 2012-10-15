/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the SysBio API library.
 *
 * Copyright (C) 2009-2012 by the University of Tuebingen, Germany.
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

import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.Curve;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.Position;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;

/**
 * Interface for defining an algorithm to create bounding boxes, dimensions and
 * points missing in the layout
 * 
 * @author Mirjam Gutekunst
 * @version $Rev: 142 $
 */
public interface LayoutAlgorithm {
	
	/**
	 * method to create a {@link Dimensions} for the layout if it was missing in
	 * the layout model
	 * 
	 * @return a {@link Dimensions}
	 */
	public Dimensions createLayoutDimension();
	
	/**
	 * method to create a {@link BoundingBox} for a {@link CompartmentGlyph} if
	 * it was missing in the layout model, should invoke
	 * {@link LayoutAlgorithm#createCompartmentGlyphDimension(CompartmentGlyph)}
	 * and {@link LayoutAlgorithm#createCompartmentGlyphPosition(CompartmentGlyph)}
	 * 
	 * @param previousCompartmentGlyph
	 * 			the last {@link CompartmentGlyph} that had been drawn before
	 *          the {@link CompartmentGlyph} that is missing a {@link BoundingBox}
	 * @return a {@link BoundingBox}
	 */
	public BoundingBox createCompartmentGlyphBoundingBox(CompartmentGlyph previousCompartmentGlyph);
	
	/**
	 * method to create a {@link Dimensions} for a {@link CompartmentGlyph} if
	 * it was missing in the layout model
	 * 
	 * @param previousCompartmentGlyph
	 *			the last {@link CompartmentGlyph} that had been drawn before
	 *          the {@link CompartmentGlyph} that is missing a {@link Dimensions}
	 * @return a {@link Dimensions}
	 */
	public Dimensions createCompartmentGlyphDimension(CompartmentGlyph previousCompartmentGlyph);
	
	/**
	 * method to create a {@link Position} for a {@link CompartmentGlyph} if it
	 * was missing in the layout model
	 * 
	 * @param previousCompartmentGlyph
	 *			the last {@link CompartmentGlyph} that had been drawn before
	 *          the {@link CompartmentGlyph} that is missing a {@link Position}
	 * @return a {@link Position}
	 */
	public Position createCompartmentGlyphPosition(CompartmentGlyph previousCompartmentGlyph);
	
	/**
	 * method to create a {@link BoundingBox} for a {@link SpeciesGlyph} if it
	 * was missing in the layout model, should invoke {@link LayoutAlgorithm#createSpeciesGlyphDimension()}
	 * and {@link LayoutAlgorithm#createSpeciesGlyphPosition(SpeciesGlyph)}
	 * 
	 * @param speciesGlyph for which the {@link BoundingBox} is missing
	 * @return a {@link BoundingBox}
	 */
	public BoundingBox createSpeciesGlyphBoundingBox(SpeciesGlyph speciesGlyph);
	
	/**
	 * method to create a {@link BoundingBox} for a {@link SpeciesGlyph} if it
	 * was missing in the layout model and if the {@link SpeciesGlyph} can be
	 * associated with a {@link SpeciesReferenceGlyph}, should invoke
	 * {@link LayoutAlgorithm#createSpeciesGlyphDimension()} and
	 * {@link LayoutAlgorithm#createSpeciesGlyphPosition(SpeciesGlyph, SpeciesReferenceGlyph)}
	 * 
	 * @param speciesGlyph for which the {@link BoundingBox} is missing
	 * @param speciesReferenceGlyph that points to the given {@link SpeciesGlyph}
	 * @return a {@link BoundingBox}
	 */
	public BoundingBox createSpeciesGlyphBoundingBox(SpeciesGlyph speciesGlyph,
										SpeciesReferenceGlyph speciesReferenceGlyph);
	
	/**
	 * method to create a {@link Dimensions} for a {@link SpeciesGlyph} if it
	 * was missing in the layout model
	 * 
	 * @return a {@link Dimensions}
	 */
	public Dimensions createSpeciesGlyphDimension();
	
	/**
	 * method to create a {@link Position} for a {@link SpeciesGlyph} if it was
	 * missing in the layout model
	 * 
	 * @param speciesGlyph for which the {@link Position} is missing
	 * @return a {@link Position}
	 */
	public Position createSpeciesGlyphPosition(SpeciesGlyph speciesGlyph);
	
	/**
	 * method to create a {@link Position} for a {@link SpeciesGlyph} if it
	 * was missing in the layout model and if the {@link SpeciesGlyph} can be
	 * associated with a {@link SpeciesReferenceGlyph}
	 * 
	 * @param speciesGlyph for which the {@link Position} is missing
	 * @param speciesReferenceGlyph that points to the given {@link SpeciesGlyph}
	 * @return a {@link Position}
	 */
	public Position createSpeciesGlyphPosition(SpeciesGlyph speciesGlyph,
								SpeciesReferenceGlyph specieReferenceGlyph);
	
	/**
	 * method to create the {@link Curve} of a {@link SpeciesReferenceGlyph},
	 * starting or ending at the given {@link ReactionGlyph}
	 * 
	 * @param reactionGlyph at which the {@link Curve} starts or ends
	 * @param speciesReferenceGlyph for which the {@link Curve} is missing
	 * @return a {@link Curve}
	 */
	public Curve createCurve(ReactionGlyph reactionGlyph,
						SpeciesReferenceGlyph speciesReferenceGlyph);
	
	/**
	 * method to create a {@link BoundingBox} for a {@link TextGlyph} if it was
	 * missing in the layout model, should invoke
	 * {@link LayoutAlgorithm#createTextGlyphDimension(TextGlyph)} and
	 * {@link LayoutAlgorithm#createTextGlyphPosition(TextGlyph)}
	 * 
	 * @param textGlyph for which the {@link BoundingBox} is missing
	 * @return a {@link BoundingBox}
	 */
	public BoundingBox createTextGlyphBoundingBox(TextGlyph textGlyph);
	
	/**
	 * method to create a {@link Dimensions} for a {@link TextGlyph} if it was
	 * missing in the layout model
	 * 
	 * @param textGlyph for which the {@link Dimensions} is missing
	 * @return a {@link Dimensions}
	 */
	public Dimensions createTextGlyphDimension(TextGlyph textGlyph);
	
	/**
	 * method to create a {@link Position} for a {@link TextGlyph} if it was
	 * missing in the layout model
	 * 
	 * @param textGlyph for which the {@link Position} is missing
	 * @return a {@link Position}
	 */
	public Position createTextGlyphPosition(TextGlyph textGlyph);
	
	/**
	 * method to create a {@link BoundingBox} for a {@link ReactionGlyph} if it
	 * was missing in the layout model should invoke
	 * {@link LayoutAlgorithm#createReactionGlyphDimension(ReactionGlyph)} and
	 * {@link LayoutAlgorithm#createReactionGlyphPosition(ReactionGlyph)}
	 * 
	 * @param reactionGlyph for which the {@link BoundingBox} is missing
	 * @return a {@link BoundingBox}
	 */
	public BoundingBox createReactionGlyphBoundingBox(ReactionGlyph reactionGlyph);
	
	/**
	 * method to create a {@link Dimensions} for a {@link ReactionGlyph} if it
	 * was missing in the layout model
	 * 
	 * @param reactionGlyph for which the {@link Dimensions} is missing
	 * @return a {@link Dimensions}
	 */
	public Dimensions createReactionGlyphDimension(ReactionGlyph reactionGlyph);
	
	/**
	 * method to create a {@link Position} for a {@link ReactionGlyph} if it was
	 * missing in the layout model
	 * 
	 * @param reactionGlyph for which the {@link Position} is missing
	 * @return a {@link Position}
	 */
	public Position createReactionGlyphPosition(ReactionGlyph reactionGlyph);
	
	/**
	 * method to create a {@link BoundingBox} for a {@link SpeciesReferenceGlyph}
	 * if it was missing in the layout model,
	 * should invoke
	 * {@link LayoutAlgorithm#createSpeciesReferenceGlyphDimension(ReactionGlyph reactionGlyph, SpeciesReferenceGlyph speciesReferenceGlyph)}
	 * and
	 * {@link LayoutAlgorithm#createSpeciesReferenceGlyphPosition(ReactionGlyph reactionGlyph, SpeciesReferenceGlyph speciesReferenceGlyph)}
	 * 
	 * @param reactionGlyph
	 *            from which the {@link SpeciesReferenceGlyph} is drawn
	 * @param speciesReferenceGlyph
	 *            for which the {@link BoundingBox} is missing
	 * @return a {@link BoundingBox}
	 */
	public BoundingBox createSpeciesReferenceGlyphBoundingBox(
			ReactionGlyph reactionGlyph,
			SpeciesReferenceGlyph speciesReferenceGlyph);
	
	/**
	 * method to create a {@link Dimensions} for a {@link SpeciesReferenceGlyph}
	 * if it was missing in the layout model
	 * 
	 * @param reactionGlyph from which the {@link SpeciesReferenceGlyph} is drawn
	 * @param speciesReferenceGlyph for which the {@link Dimensions} is missing
	 * @return a {@link Dimensions}
	 */
	public Dimensions createSpeciesReferenceGlyphDimension(
			ReactionGlyph reactionGlyph,
			SpeciesReferenceGlyph speciesReferenceGlyph);
	
	/**
	 * method to create a {@link Position} for a {@link SpeciesReferenceGlyph}
	 * if it was missing in the layout model
	 * 
	 * @param reactionGlyph
	 *            from which the {@link SpeciesReferenceGlyph} is drawn
	 * @param speciesReferenceGlyph
	 *            for which the {@link Position} is missing
	 * @return a {@link Position}
	 */
	public Position createSpeciesReferenceGlyphPosition(
			ReactionGlyph reactionGlyph,
			SpeciesReferenceGlyph speciesReferenceGlyph);
	
	/**
	 * @param layout the {@link Layout} to be set for this LayoutAlgorithm
	 */
	public void setLayout(Layout layout);
	
	/**
	 * @return the {@link Layout} set for this LayoutAlgorithm;
	 */
	public Layout getLayout();
	
	/**
	 * @return true if a {@link Layout} is set for this LayoutAlgorithm, else false
	 */
	public boolean isSetLayout();
	
	/**
	 * method to calculate the rotation angle of the lines from the reaction box,
	 * in degrees
	 * 
	 * @param reactionGlyp for which the rotation angle shall be callculated
	 * @return the rotation angle in degrees
	 */
	public double calculateReactionGlyphRotationAngle(ReactionGlyph reactionGlyph);
	
}