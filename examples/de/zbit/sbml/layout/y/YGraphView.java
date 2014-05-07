/*
 * $Id: YGraphView.java 1064 2012-10-29 15:46:01Z jmatthes $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SysBio/trunk/test/de/zbit/sbml/layout/y/YGraphView.java $
 * ---------------------------------------------------------------------
 * This file is part of SBML Editor.
 *
 * Copyright (C) 2012-2014 by the University of Tuebingen, Germany.
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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutConstants;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.util.StringTools;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.io.ImageIoOutputHandler;
import y.layout.organic.SmartOrganicLayouter;
import y.view.DefaultGraph2DRenderer;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;
import y.view.Graph2DView;
import y.view.Graph2DViewMouseWheelZoomListener;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import yext.svg.io.SVGIOHandler;
import de.zbit.graph.RestrictedEditMode;
import de.zbit.graph.io.Graph2Dwriter;
import de.zbit.gui.GUITools;
import de.zbit.io.OpenedFile;
import de.zbit.sbml.gui.SBMLReadingTask;
import de.zbit.sbml.layout.GlyphCreator;
import de.zbit.sbml.layout.LayoutDirector;
import de.zbit.util.logging.LogUtil;

/**
 * Simple GUI to display a Graph2DView.
 * 
 * It renders an {@link SBMLDocument} (from command line arg0, or falling back to a
 * default) with {@link LayoutDirector} using {@link YLayoutBuilder} and the TikZLayoutAlgorithm
 * (YLayoutAlgorithm is not yet functional).
 * 
 * @author Jakob Matthes
 * @author Andreas Dr&auml;ger
 * @version $Rev: 1064 $
 */
public class YGraphView implements PropertyChangeListener {

  /**
   * Initial dimensions  of the window.
   */
  private static final int WINDOW_WIDTH = 960;
  private static final int WINDOW_HEIGHT = 720;

  /**
   * Graph2D instance generated by LayoutDirector.
   */
  private Graph2D product;

  /**
   * SBML document from which to create the graph.
   */
  private SBMLDocument document;

  private static String out;

  private static Logger logger = Logger.getLogger(YGraphView.class.getName());

  /**
   * @param args
   */
  public static void main(final String[] args) {
    LogUtil.initializeLogging(YGraphView.class.getPackage().toString());
    final File in = new File(args[0]);
    out = args[1];
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      /* (non-Javadoc)
       * @see java.lang.Runnable#run()
       */
      @Override
      public void run() {
        try {
          new YGraphView(in);
        } catch (Throwable e) {
          e.printStackTrace();
        }
      }
    });

    // Do-nothing-loop because in MacOS the application terminates suddenly.
    if (GUITools.isMacOSX()) {
      for (int i = 0; i < 1E12; i++);
    }
  }

  /**
   * 
   */
  public YGraphView() {
  }

  /**
   * @param inputFile File to display
   * @throws Throwable
   */
  public YGraphView(File inputFile) throws Throwable {
    this();
    setSBMLDocument(SBMLReader.read(inputFile));
    //    SBMLReadingTask readingTask = new SBMLReadingTask(inputFile, null, this);
    //    readingTask.execute();
  }

  /**
   * @param doc SBMLDocument to display
   */
  public YGraphView(SBMLDocument doc) {
    this();
    setSBMLDocument(doc);
  }

  /* (non-Javadoc)
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getPropertyName().equals(SBMLReadingTask.SBML_READING_SUCCESSFULLY_DONE)) {
      @SuppressWarnings("unchecked")
      OpenedFile<SBMLDocument> openedFile = (OpenedFile<SBMLDocument>) evt.getNewValue();
      SBMLDocument doc = openedFile.getDocument();
      setSBMLDocument(doc);
    }
  }

  /**
   * 
   * @param doc
   */
  private void setSBMLDocument(SBMLDocument doc) {
    if (doc == null) {
      logger.warning("No SBML document given.");
      System.exit(1);
    }

    this.document = doc;

    Model model = doc.getModel();
    LayoutModelPlugin ext = (LayoutModelPlugin) model.getExtension(LayoutConstants.getNamespaceURI(doc.getLevel(), doc.getVersion()));

    // Generate glyphs for SBML documents without layout information
    if (ext == null) {
      logger.info("Model does not contain layouts, creating glyphs for every object.");
      (new GlyphCreator(model)).create();
      ext = (LayoutModelPlugin) model.getExtension(LayoutConstants.getNamespaceURI(doc.getLevel(), doc.getVersion()));
    }

    // Display option pane to choose specific layout if multiple layouts are available
    int layoutIndex = 0;
    if (ext.getLayoutCount() > 1) {
      String layouts[] = new String[ext.getLayoutCount()];
      for (int i = 0; i < ext.getLayoutCount(); i++) {
        Layout layout = ext.getLayout(i);
        layouts[i] = layout.isSetName() ? layout.getName() : layout.getId();
      }
      layoutIndex = JOptionPane.showOptionDialog(null,
        "Select the layout to be displayed", "Layout selection",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
        layouts, layouts[0]);
      if (layoutIndex < 0) {
        System.exit(0);
      }
    }

    // Run LayoutDirector and create product
    LayoutDirector<ILayoutGraph> director =
        new LayoutDirector<ILayoutGraph>(doc, new YLayoutBuilder(), new YLayoutAlgorithm());
    director.setLayoutIndex(layoutIndex);
    director.run();
    product = director.getProduct().getGraph2D();

    SmartOrganicLayouter sol = new SmartOrganicLayouter();
    sol.setCompactness(.2);

    new Graph2DLayoutExecutor(Graph2DLayoutExecutor.BUFFERED).doLayout(product, sol);
    // experimental or debug features
    //    writeModifiedModel(System.getProperty("user.dir")+"/out.xml");
    writeSVGImage(out);
    //dumpGraph();

    displayGraph2DView();
  }

  /**
   * Create a window showing the graph view.
   */
  private void displayGraph2DView() {
    // Create a viewer for the graph
    Graph2DView view = new Graph2DView(product);
    DefaultGraph2DRenderer dgr = new DefaultGraph2DRenderer();
    dgr.setDrawEdgesFirst(true);
    view.setGraph2DRenderer(dgr);
    Rectangle box = view.getGraph2D().getBoundingBox();
    Dimension dim = box.getSize();
    view.setSize(dim);
    // view.zoomToArea(box.getX() - 10, box.getY() - 10, box.getWidth() + 20, box.getHeight() + 20);
    Dimension minimumSize = new Dimension(
      (int) Math.max(view.getMinimumSize().getWidth(), 100),
      (int) Math.max(view.getMinimumSize().getHeight(), WINDOW_HEIGHT/2d));
    view.setMinimumSize(minimumSize);
    view.setPreferredSize(new Dimension(100, (int) Math.max(WINDOW_HEIGHT * 0.6d, 50d)));
    view.setOpaque(false);

    view.setGraph2DRenderer(new DefaultGraph2DRenderer() {
      @Override
      protected int getLayer(Graph2D graph, Node node) {
        return 0;
      }
      @Override
      protected int getLayer(Graph2D graph, Edge edge) {
        return 1;
      }
    });

    ((DefaultGraph2DRenderer) view.getGraph2DRenderer()).setLayeredPainting(true);
    view.getCanvasComponent().addMouseWheelListener(new Graph2DViewMouseWheelZoomListener());
    try {
      view.fitContent(true);
    } catch (Throwable t) {
      // Not really a problem
    }
    RestrictedEditMode.addOverviewAndNavigation(view);
    view.addViewMode(new RestrictedEditMode());
    view.setFitContentOnResize(true);

    // Create and show window
    JFrame frame = new JFrame();
    frame.setTitle(YGraphView.class.getSimpleName());
    frame.add(view);
    frame.setMinimumSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
  }

  /**
   * Write svg image file
   * 
   * @param outFile path of the output file
   */
  private void writeSVGImage(String outFile) {
    SVGIOHandler svgio = new SVGIOHandler();
    SVGDOMEnhancerForHierarchy svgEFH = new SVGDOMEnhancerForHierarchy(document.getModel());
    svgEFH.setDrawEdgesFirst(false);
    svgio.setSVGGraph2DRenderer(svgEFH);

    try {
      svgio.write(product, outFile);
      logger.info(MessageFormat.format("Image written to ''{0}''.", outFile));
    } catch (IOException e) {
      logger.warning("Could not write image: ImageWriter not available.");
    }
  }

  /**
   * Write image file (png) of the graph.
   * 
   * @param outFile path of the output file
   */
  private void writeImage(String outFile) {
    Iterator<javax.imageio.ImageWriter> iterator =
        javax.imageio.ImageIO.getImageWritersBySuffix("png");
    javax.imageio.ImageWriter imageWriter =
        iterator.hasNext() ? iterator.next() : null;

        if (imageWriter != null) {
          Graph2Dwriter graph2Dwriter =
              new Graph2Dwriter(new ImageIoOutputHandler(imageWriter));
          graph2Dwriter.writeToFile(product, outFile);
          logger.info(MessageFormat.format("Image written to ''{0}''.", outFile));
        }
        else {
          logger.warning("Could not write image: ImageWriter not available.");
        }
  }

  /**
   * Write the modified SBML model to a file.
   * 
   * @param document document to write
   * @param outFile
   * @throws XMLStreamException
   * @throws IOException
   */
  private void writeModifiedModel(String outFile) {
    try {
      SBMLWriter.write(document, new File(outFile), ' ', (short) 2);
    } catch (SBMLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (XMLStreamException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    logger.info(MessageFormat.format("Modified model written to ''{0}''.", outFile));
  }

  /**
   * Write a textual representation of the graph (all nodes and edges
   * including realizier information) to standard out for debugging purposes.
   */
  private void dumpGraph() {
    NodeCursor nodeCursor = product.nodes();
    System.out.println("Nodes:");
    for (; nodeCursor.ok(); nodeCursor.next()) {
      Node n = (Node) nodeCursor.current();
      NodeRealizer nr = product.getRealizer(n);
      NodeLabel nodeLabel = nr.getLabel();
      System.out.println(n);
      System.out.println("  " + product.getRealizer(n));
      System.out.println(nodeLabel.toString());
    }
    System.out.println("Edges:");
    EdgeCursor edgeCursor = product.edges();
    for (; edgeCursor.ok(); edgeCursor.next()) {
      Edge e = (Edge) edgeCursor.current();
      System.out.println(e);
      System.out.println("  " + prettyPrintEdgeRealizer(product.getRealizer(e)));
    }
  }

  /**
   * @param realizer
   * @return a textual representation of an edge realizer
   */
  private String prettyPrintEdgeRealizer(EdgeRealizer r) {
    return StringTools.concat(r.getClass().getSimpleName(),
      " [sourcePoint=", r.getSourcePoint(),
      ", targetPoint=", r.getTargetPoint(),
        "]").toString();
  }

}
