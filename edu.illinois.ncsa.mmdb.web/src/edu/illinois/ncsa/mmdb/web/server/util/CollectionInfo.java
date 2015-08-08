package edu.illinois.ncsa.mmdb.web.server.util;

import java.util.HashSet;
import java.util.Set;

public class CollectionInfo {
    private Long              size           = 0L;
    private int               numDatasets    = 0;
    private long              maxDatasetSize = 0L;
    private int               numCollections = 0;
    private int               maxDepth       = 0;
    private final Set<String> mimetypeSet    = new HashSet<String>();

    public Long getSize() {
        return size;
    }

    public int getNumDatasets() {
        return numDatasets;
    }

    public void incrementNumDatasets(int numDatasets) {
        this.numDatasets += numDatasets;
    }

    public long getMaxDatasetSize() {
        return maxDatasetSize;
    }

    public void setMaxDatasetSize(long maxDatasetSize) {
        this.maxDatasetSize = maxDatasetSize;
    }

    public int getNumCollections() {
        return numCollections;
    }

    public void incrementNumCollections(int numCollections) {
        this.numCollections += numCollections;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public Set<String> getMimetypeSet() {
        return mimetypeSet;
    }

    public void addMimetype(String mimetype) {
        this.mimetypeSet.add(mimetype);
    }

    public void increaseSize(long size) {
        this.size += size;

    }

    public void addMimetypes(Set<String> mimetypeSet2) {
        mimetypeSet.addAll(mimetypeSet2);

    }

}