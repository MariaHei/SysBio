/*
 * $Id:  GeneIDMapper.java 17:57:03 wrzodek $
 * $URL: GeneIDMapper.java $
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
package de.zbit.mapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.zbit.io.CSVReader;
import de.zbit.util.AbstractProgressBar;
import de.zbit.util.FileDownload;
import de.zbit.util.FileTools;
import de.zbit.util.ProgressBar;
import de.zbit.util.logging.LogUtil;

/**
 * This class downloads or loads (if available) mapping data
 * to map RefSeq IDs (e.g., NM_X) to NCBI Gene IDs (Entrez).
 * 
 * @author buechel
 * @author Clemens Wrzodek
 * @version $Rev$
 */
public class RefSeq2GeneIDMapper extends AbstractMapper<String, Integer> {
  private static final long serialVersionUID = -4951755727304781666L;

  public static final Logger log = Logger.getLogger(RefSeq2GeneIDMapper.class.getName());
  
  /**
   * The Base url to get the directory listing and search for a file "X2geneid.gz"
   */
  private static String downloadBaseURL = "ftp://ftp.ncbi.nih.gov/refseq/release/release-catalog/";
  
  /**
   * The URL where the file should be downloaded, if it is not available.
   */
  private final static String downloadURL = getLatestReleaseMappingFile();
  
  /**
   * The downloaded and cached local mapping file.
   * MUST contain a folder.
   */
  private static String localFile = "res/" + FileTools.getFilename(downloadURL);
  

  
  
  /**
   * Inintializes the mapper from RefSeq to Gene ids. Downloads and reads the mapping
   * file automatically as required.
   * @throws IOException
   */
  public RefSeq2GeneIDMapper() throws IOException {
    this(new ProgressBar(0));
  }
  /**
   * @param progress - a custom progress bar. Can be NULL!
   * @throws IOException
   * @see {@link RefSeq2GeneIDMapper#GeneIDMapper()}
   */
  public RefSeq2GeneIDMapper(AbstractProgressBar progress) throws IOException {
    super(String.class, Integer.class, progress);
    init();
  }
  
  public void test() {
    boolean checkLowerVersionExists=true;
    boolean differentVersionsHaveDifferentTargets=true;
    
    
    for (String key: getMapping().keySet()) {
      int pos = key.indexOf('.');
      if (pos<0) continue;
      int version = Integer.parseInt(key.substring(pos+1));
      
      if (checkLowerVersionExists && version>1) {
        for (int i=version-1; i>0; i--) {
          String newKey = key.subSequence(0, pos+1) + Integer.toString(i);
          if (getMapping().containsKey(newKey)) {
            System.out.println("Lower version exists (z.B. " + key + " => " + newKey);
            checkLowerVersionExists = false;
          }
        }
      }
      
      if (differentVersionsHaveDifferentTargets) {
        for (int i=version-1; i>0; i--) {
          String newKey = key.subSequence(0, pos+1) + Integer.toString(i);
          if (getMapping().containsKey(newKey)) {
            
            Integer oldVersionTarget = getMapping().get(newKey);
            Integer aktVersionTarget = getMapping().get(key);
            
            if ( ((int)oldVersionTarget)!=  ((int)aktVersionTarget) ) {
              
              System.out.println("Different target exists z.B. \n" + key + " => " + aktVersionTarget + "\n" + newKey + " => " + oldVersionTarget);
              differentVersionsHaveDifferentTargets = false;
              
            }
            
          }
        }
      }
      
      if (!differentVersionsHaveDifferentTargets && !checkLowerVersionExists) break;
    }
    
    if (checkLowerVersionExists)
      System.out.println("No Lower version exist in file.");
    if (differentVersionsHaveDifferentTargets)
      System.out.println("No Lower versions have different targerts.");
  }
  
  public static void main (String[] args) throws IOException {
    LogUtil.initializeLogging(Level.FINEST);
    RefSeq2GeneIDMapper mapper = new RefSeq2GeneIDMapper();
    mapper.test();
    
    
  }
  

  public static String getLatestReleaseMappingFile() {
    OutputStream out = new ByteArrayOutputStream();
    String baseUrl = downloadBaseURL;
    try {
      FileDownload.download(baseUrl, out);
    } catch (Exception e) {
      log.log(Level.SEVERE, "Could not get RefSeq file listing.", e);
      out = null;
    }
    if (out == null || !out.toString().contains("\n")) {
      log.severe("Could not get mapping file from refSeq Server.");
      System.exit(1);
    }
    
    for (String file: out.toString().split("\n")) {
      // file is e.g. "-r--r--r--   1 ftp      anonymous 146709259 Mar 11 10:14 RefSeq-release46.catalog.gz"
      // Get real file name
      int pos = file.lastIndexOf(' ');
      if (pos>0) file = file.substring(pos);
      file = file.trim();
     
      // Check if it is the mapping file and return it, if true.
      if (file.endsWith("2geneid.gz")) {
        return baseUrl + (baseUrl.endsWith("/")?"":"/") + file;
      }
      
    }
    
    // we could  not find a mapping file on the RegSeq Server (downloadBaseURL) that ends with "2geneid.gz".
    log.severe("Could not find mapping file on RefSeq Server.");
    System.exit(1);
    
    return null;
  }
  
  
  /**
   * Removes the version number from a RefSeq Identifier.
   * @param refSeq (e.g. NM_23424.1)
   * @return e.g. NM_23424
   */
  public static String trimVersionNumberFromRefSeq(String refSeq) {
    int pos = refSeq.indexOf('.');
    if (pos>0) refSeq = refSeq.substring(0, pos);
    return refSeq;
  }

  /** {@inheritDoc}*/
  @Override
  protected String postProcessSourceID(String source) {
    // get RefSeq ID (trim version number from id)
    source = trimVersionNumberFromRefSeq(source);
    return source;
  }


  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getLocalFile()
   */
  @Override
  public String getLocalFile() {
    return localFile;
  }


  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getMappingName()
   */
  @Override
  public String getMappingName() {
    return "RefSeq2GeneID";
  }


  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getRemoteURL()
   */
  @Override
  public String getRemoteURL() {
    return downloadURL;
  }


  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getSourceColumn()
   */
  @Override
  public int getSourceColumn(CSVReader r) {
    return 2;
  }


  /* (non-Javadoc)
   * @see de.zbit.mapper.AbstractMapper#getTargetColumn()
   */
  @Override
  public int getTargetColumn(CSVReader r) {
    return 1;
  }
  

}