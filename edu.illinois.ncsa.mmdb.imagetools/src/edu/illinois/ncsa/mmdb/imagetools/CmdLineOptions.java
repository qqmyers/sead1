package edu.illinois.ncsa.mmdb.imagetools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

public class CmdLineOptions {
    @Option(name = "--tilesize", usage = "Size of each tile.")
    public int        tilesize = 256;

    @Option(name = "--overlap", usage = "Overlap between tiles.")
    public int        overlap  = 1;

    @Option(name = "--type", usage = "Image type of the output image.")
    public String     type     = "jpg";

    @Option(name = "--output", usage = "Folder where the pyramid will be placed.")
    public File       output   = new File("pyramid");

    @Option(name = "--smooth", usage = "Average the value of the pixels when scaling the image.")
    public boolean    smooth   = false;

    @Argument(usage = "Files to be processed.")
    public List<File> files    = new ArrayList<File>();
}
