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

package de.zbit.sbml.layout.y;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.CubicBezier;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LineSegment;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;

import y.base.Node;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.NodeRealizer;
import de.zbit.graph.sbgn.ReactionNodeRealizer;
import de.zbit.sbml.layout.AbstractLayoutBuilder;
import de.zbit.sbml.layout.Catalysis;
import de.zbit.sbml.layout.Compartment;
import de.zbit.sbml.layout.Consumption;
import de.zbit.sbml.layout.Inhibition;
import de.zbit.sbml.layout.Macromolecule;
import de.zbit.sbml.layout.Production;
import de.zbit.sbml.layout.SBGNArc;
import de.zbit.sbml.layout.SBGNNode;
import de.zbit.sbml.layout.SimpleChemical;
import de.zbit.sbml.layout.SourceSink;
import de.zbit.sbml.layout.UnspecifiedNode;
import de.zbit.util.progressbar.AbstractProgressBar;
import de.zbit.util.progressbar.ProgressListener;

/**
 * @author Jakob Matthes
 * @version $Rev$
 */
public class YLayoutBuilder extends AbstractLayoutBuilder<Graph2D,NodeRealizer,EdgeRealizer> {

	private static Logger logger = Logger.getLogger(YLayoutBuilder.class.toString());

	/**
	 * A YFiles Graph2D is the product of this builder.
	 */
	private Graph2D graph;
	
	/**
	 * List keeping track of ProgressListeners.
	 */
	private List<ProgressListener> progressListeners = new LinkedList<ProgressListener>();

	/**
	 * Indicates whether graph generation has finished or not.
	 */
	private boolean terminated = false;

	/**
	 * Maps SBML identifiers to yFiles nodes.
	 */
	private Map<String, Node> id2node = new HashMap<String, Node>();
	
	/**
	 * Set to hold all text glyphs which label a specific node.
	 */
	Set<TextGlyph> labelTextGlyphs;

	/**
	 * Method to initialize the graph2d structure.
	 * 
	 * @param layout
	 * @see de.zbit.sbml.layout.LayoutBuilder#builderStart(org.sbml.jsbml.ext.layout.Layout)
	 */
	@Override
	public void builderStart(Layout layout) {
		graph = new Graph2D();
		labelTextGlyphs = new HashSet<TextGlyph>();
		// TODO for all p in progressListeners: progress.setNumberOfTotalCalls(xyz);
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutBuilder#addProgressListener(de.zbit.util.progressbar.AbstractProgressBar)
	 */
	@Override
	public void addProgressListener(AbstractProgressBar progress) {
		progressListeners.add(progress);
	}

	/**
	 * Method to add the compartment glyph representation to the graph.
	 * @see de.zbit.sbml.layout.LayoutBuilder#buildCompartment(org.sbml.jsbml.ext.layout.CompartmentGlyph)
	 */
	@Override
	public void buildCompartment(CompartmentGlyph compartmentGlyph) {
		// TODO compartmentGlpyh.getSBOTerm() returns -1
		SBGNNode<NodeRealizer> node = getSBGNNode(SBO.getCompartment());
		
		BoundingBox boundingBox = compartmentGlyph.getBoundingBox();
		Point point = boundingBox.getPosition();
		Dimensions dimension = boundingBox.getDimensions();
		double x, y, z, width, height, depth;
		x = point.getX();
		y = point.getY();
		z = point.getZ();
		width = dimension.getWidth();
		height = dimension.getHeight();
		depth = dimension.getDepth();

		NodeRealizer nodeRealizer = node.draw(x, y, z, width, height, depth);
		Node ynode = graph.createNode();
		graph.setRealizer(ynode, nodeRealizer);
		
		logger.info(String.format("building compartment glyph id=%s\n\tbounding box=%s",
				compartmentGlyph.getId(), nodeRealizer.getBoundingBox()));
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutBuilder#buildEntityPoolNode(org.sbml.jsbml.ext.layout.SpeciesGlyph, boolean)
	 */
	@Override
	public void buildEntityPoolNode(SpeciesGlyph speciesGlyph,
			boolean cloneMarker) {
		SBGNNode<NodeRealizer> node = getSBGNNode(speciesGlyph.getSBOTerm());
		
		if (cloneMarker) {
			node.setCloneMarker();
		}
		
		BoundingBox boundingBox = speciesGlyph.getBoundingBox();
		Point point = boundingBox.getPosition();
		Dimensions dimension = boundingBox.getDimensions();
		double x, y, z, width, height, depth;
		x = point.getX();
		y = point.getY();
		z = point.getZ();
		width = dimension.getWidth();
		height = dimension.getHeight();
		depth = dimension.getDepth();

		NodeRealizer nodeRealizer = node.draw(x, y, z, width, height, depth);
		
		logger.info(String.format("building EPN element id=%s sbo=%d (%s)\n\tbounding box=%s",
				speciesGlyph.getId(), speciesGlyph.getSBOTerm(), SBO.convertSBO2Alias(speciesGlyph.getSBOTerm()),
				nodeRealizer.getBoundingBox()));
		
		Node ynode = graph.createNode();
		graph.setRealizer(ynode, nodeRealizer);
		id2node.put(speciesGlyph.getId(), ynode);
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutBuilder#buildConnectingArc(org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph)
	 */
	@Override
	public void buildConnectingArc(SpeciesReferenceGlyph speciesReferenceGlyph) {
		// TODO
		logger.info(String.format("building reference glyph id=%s", speciesReferenceGlyph.getId()));
		logger.info(speciesReferenceGlyph.toString());
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutBuilder#buildConnectingArc(org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph, org.sbml.jsbml.ext.layout.ReactionGlyph)
	 */
	@Override
	public void buildConnectingArc(SpeciesReferenceGlyph srg, ReactionGlyph rg) {
		Node processNode = id2node.get(rg.getId());
		Node speciesGlyphNode = id2node.get(srg.getSpeciesGlyph());
		assert processNode != null;
		assert speciesGlyphNode != null;
		
		SBGNArc<EdgeRealizer> arc;
		if (srg.isSetSBOTerm()) {
			arc = getSBGNArc(srg.getSBOTerm());
		} else {
			arc = getSBGNArc(srg.getSpeciesReferenceRole());
		}
		
		EdgeRealizer edgeRealizer = arc.draw(srg.getCurve());
		
		graph.createEdge(processNode, speciesGlyphNode, edgeRealizer);
		
		logger.info(String.format("create edge between %s and %s", srg.getId(), rg.getId()));
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutBuilder#buildLineSegment(org.sbml.jsbml.ext.layout.LineSegment)
	 */
	@Override
	public void buildLineSegment(LineSegment lineSegment) {
		// TODO
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutBuilder#buildCubicBezier(org.sbml.jsbml.ext.layout.CubicBezier)
	 */
	@Override
	public void buildCubicBezier(CubicBezier cubicBezier) {
		// TODO
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutBuilder#buildProcessNode(org.sbml.jsbml.ext.layout.ReactionGlyph, double)
	 */
	@Override
	public void buildProcessNode(ReactionGlyph reactionGlyph, double rotationAngle) {
		// check if process node has already been built
		Node maybeProcesNode = id2node.get(reactionGlyph.getId());
		if (maybeProcesNode == null) {
			ReactionNodeRealizer reactionNodeRealizer = new ReactionNodeRealizer();
			Node processNode = graph.createNode(reactionNodeRealizer);
			id2node.put(reactionGlyph.getId(), processNode);
		}
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutBuilder#buildTextGlyph(org.sbml.jsbml.ext.layout.TextGlyph)
	 */
	@Override
	public void buildTextGlyph(TextGlyph textGlyph) {
		String text = "";
		
		if (textGlyph.isSetText() &&
				!textGlyph.isSetGraphicalObject() &&
				!textGlyph.isSetOriginOfText()) {
			// independent text
			BoundingBox boundingBox = textGlyph.getBoundingBox();
			Point point = boundingBox.getPosition();
			Dimensions dimensions = boundingBox.getDimensions();
			double x, y, width, height;
			x = point.getX();
			y = point.getY();
			width = dimensions.getWidth();
			height = dimensions.getHeight();
			
			text = textGlyph.getText();
			logger.info(String.format("building text glyph element id=%s\n\tindependent text text='%s'",
					textGlyph.getId(), text));
			// TODO how to display independent text in yFiles graph?
			Node ynode = graph.createNode();
			NodeRealizer nr = graph.getRealizer(ynode);
			nr.setLabelText(text);
			nr.setFrame(x, y, width, height);
		}
		else if (textGlyph.isSetGraphicalObject() &&
				textGlyph.isSetOriginOfText()) {
			// label for a graphical object
			// label text glyphs are collected and built as a last step of the builder
			labelTextGlyphs.add(textGlyph);
		}
		else {
			logger.info(String.format("illegal text glyph id=%s",
					 textGlyph.getId()));
		}
	}

	/**
	 * Realizes a text glyph as a label of an already existing node.
	 * @param textGlyph
	 */
	private void buildTextGlyphAsLabel(TextGlyph textGlyph) {
		Node origin = id2node.get(textGlyph.getGraphicalObject());
		NodeRealizer originRealizer = graph.getRealizer(origin);

		String text;
		if (textGlyph.isSetText()) {
			text = textGlyph.getText();
			logger.info(String.format("building text glyph element id=%s\n\torigin text overridden text='%s'",
					textGlyph.getId(), text));
		}
		else {
			NamedSBase namedSBase = textGlyph.getOriginOfTextInstance();
			text = namedSBase.getName();
			logger.info(String.format("building text glyph element id=%s\n\ttext from origin id=%s text='%s'",
					textGlyph.getId(), namedSBase.getId(), text));
		}
		
		// use NodeLabel to specify position of text
		originRealizer.setLabelText(text);
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutBuilder#builderEnd()
	 */
	@Override
	public void builderEnd() {
		// build label text glyphs
		for (TextGlyph textGlyph : labelTextGlyphs) {
			buildTextGlyphAsLabel(textGlyph);
		}
		
		terminated = true;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutBuilder#getProduct()
	 */
	@Override
	public Graph2D getProduct() {
		return graph;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutBuilder#isProductReady()
	 */
	@Override
	public boolean isProductReady() {
		return terminated;
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createMacromolecule()
	 */
	@Override
	public Macromolecule<NodeRealizer> createMacromolecule() {
		return new YMacromolecule();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createSourceSink()
	 */
	@Override
	public SourceSink<NodeRealizer> createSourceSink() {
		return new YSourceSink();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createUnspecifiedNode()
	 */
	@Override
	public UnspecifiedNode<NodeRealizer> createUnspecifiedNode() {
		return new YUnspecifiedNode();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createSimpleChemical()
	 */
	@Override
	public SimpleChemical<NodeRealizer> createSimpleChemical() {
		return new YSimpleChemical();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createCompartment()
	 */
	@Override
	public Compartment<NodeRealizer> createCompartment() {
		return new YCompartment();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createProduction()
	 */
	@Override
	public Production<EdgeRealizer> createProduction() {
		return new YProduction();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createConsumption()
	 */
	@Override
	public Consumption<EdgeRealizer> createConsumption() {
		return new YConsumption();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createCatalysis()
	 */
	@Override
	public Catalysis<EdgeRealizer> createCatalysis() {
		return new YCatalysis();
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.layout.LayoutFactory#createInhibition()
	 */
	@Override
	public Inhibition<EdgeRealizer> createInhibition() {
		return new YInhibition();
	}

}