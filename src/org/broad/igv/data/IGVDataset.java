/*
 * Copyright (c) 2007-2012 The Broad Institute, Inc.
 * SOFTWARE COPYRIGHT NOTICE
 * This software and its documentation are the copyright of the Broad Institute, Inc. All rights are reserved.
 *
 * This software is supplied without any warranty or guaranteed support whatsoever. The Broad Institute is not responsible for its use, misuse, or functionality.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL),
 * Version 2.1 which is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broad.igv.data;

import org.broad.igv.feature.genome.Genome;
import org.broad.igv.track.TrackProperties;
import org.broad.igv.track.TrackType;
import org.broad.igv.util.ObjectCache;
import org.broad.igv.util.ResourceLocator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jrobinso
 */
public class IGVDataset implements Dataset {

    private String name;

    private TrackType type = TrackType.OTHER;

    private boolean logNormalized;
    private String[] dataHeadings;
    private Map<String, ChromosomeSummary> chromosomeSummaries = new LinkedHashMap();
    private GenomeSummaryData genomeSummary;
    private IGVDatasetParser parser;
    private ObjectCache<String, ChromosomeData> chromsomeDataCache = new ObjectCache(30);
    private float dataMin;
    private float dataMax;
    TrackProperties trackProperties = new TrackProperties();
    private Map<String, Integer> longestFeatureMap;

    public IGVDataset(ResourceLocator locator, Genome genome) {

        parser = new IGVDatasetParser(locator, genome);

        List<ChromosomeSummary> summaries = parser.scan(this);

        if (summaries == null || summaries.size() == 0)
            throw new RuntimeException("Could not find any chromosomes in the dataset on the genome(" + genome.getId() + ")");

        for (ChromosomeSummary summary : summaries) {
            chromosomeSummaries.put(summary.getName(), summary);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setGenomeSummary(GenomeSummaryData genomeSummary) {
        this.genomeSummary = genomeSummary;
    }

    public void setTrackType(TrackType type) {
        this.type = type;
    }

    public TrackType getType() {
        return type;
    }

    public String[] getChromosomes() {
        return chromosomeSummaries.keySet().toArray(new String[]{});
    }

    public void setDataHeadings(String[] dataHeadings) {
        this.dataHeadings = dataHeadings;
    }

    public String[] getTrackNames() {
        return dataHeadings;
    }

    public int[] getStartLocations(String chr) {
        ChromosomeData cd = getChromosomeData(chr);
        return cd == null ? null : cd.getStartLocations();
    }

    public int[] getEndLocations(String chr) {
        ChromosomeData cd = getChromosomeData(chr);
        return cd == null ? null : cd.getEndLocations();
    }

    public String[] getFeatureNames(String chr) {
        ChromosomeData cd = getChromosomeData(chr);
        return cd == null ? null : cd.getProbes();
    }

    public float[] getData(String heading, String chr) {
        ChromosomeData cd = getChromosomeData(chr);
        return cd == null ? null : cd.getData(heading);
    }

    /**
     * Get the data for all samples (tracks) for the given chromosome.
     * <p/>
     * This method is synchronized to insure that the data for a chromosome
     * is only loaded once.
     *
     * @param chr
     * @return
     */
    private synchronized ChromosomeData getChromosomeData(String chr) {
        ChromosomeData cd = chromsomeDataCache.get(chr);
        if (cd == null) {
            ChromosomeSummary sum = chromosomeSummaries.get(chr);
            if (sum == null) {
                //todo -- throw exception
                return null;
            }
            //synchronized (sum) {

            cd = parser.loadChromosomeData(sum, dataHeadings);
            chromsomeDataCache.put(chr, cd);
            //}

        }
        return cd;
    }

    public GenomeSummaryData getGenomeSummary() {
        return genomeSummary;
    }

    public void setLogNormalized(boolean logNormalized) {
        this.logNormalized = logNormalized;
    }

    public boolean isLogNormalized() {
        return logNormalized;
    }

    public float getDataMin() {
        return dataMin;
    }

    public float getDataMax() {
        return dataMax;
    }

    public void setDataMin(float dataMin) {
        this.dataMin = dataMin;
    }

    public void setDataMax(float dataMax) {
        this.dataMax = dataMax;
    }

    public TrackProperties getTrackProperties() {
        return trackProperties;
    }


    public Integer getLongestFeature(String chr) {
        return longestFeatureMap == null ? 1000 :
                longestFeatureMap.containsKey(chr) ? longestFeatureMap.get(chr) : 1;
    }

    public void setLongestFeatureMap(Map<String, Integer> longestFeatureMap) {
        this.longestFeatureMap = longestFeatureMap;
    }
}
