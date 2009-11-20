package edu.illinois.ncsa.mmdb.imagetools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public class DeepZoomPyramid {
    private static Log log = LogFactory.getLog(DeepZoomPyramid.class);

    public static void main(String[] args) throws Exception {
        CmdLineOptions options = new CmdLineOptions();
        CmdLineParser parser = new CmdLineParser(options);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException exc) {
            parser.printUsage(System.out);
            throw (new IOException("Could not parse arguments.", exc));
        }

        if (options.files.size() == 0) {
            parser.printUsage(System.out);
            throw new IOException("No files to process.");
        }

        for (File input : options.files) {
            makePyramid(input, options.output, options.tilesize, options.overlap, options.type, options.smooth);
        }
    }

    public static File makePyramid(File input, File folder, int tilesize, int overlap, String imageformat, boolean smooth) throws IOException {
        // make sure folder exists
        folder.mkdirs();

        // load the image
        BufferedImage img = ImageIO.read(input);
        if (img == null) {
            throw (new IOException("Could not load image."));
        }
        log.info("image loaded : " + img.toString());

        // get the width/height and maximum
        int w = img.getWidth();
        int h = img.getHeight();
        int m = (w > h) ? w : h;

        // compute number of levels until 1x1 pixel
        int maxlevel = (int) Math.ceil(Math.log(m) / Math.log(2));

        // create a unique id for the pyramid
        String name = input.getName();

        // write the xml file
        File xml = new File(folder, name + ".xml");
        PrintStream ps = new PrintStream(xml);
        ps.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        ps.print(String.format("<Image TileSize=\"%d\" Overlap=\"%d\" Format=\"%s\" ", tilesize, overlap, imageformat));
        ps.println("ServerFormat=\"Default\" xmlns=\"http://schemas.microsoft.com/deepzoom/2009\">");
        ps.println(String.format("<Size Width=\"%d\" Height=\"%d\" />", w, h));
        ps.println("</Image>");
        ps.close();

        // write the html file
        File html = new File(folder, name + ".html");
        ps = new PrintStream(html);
        ps.println("<html>");
        ps.println("<title>" + input.getName() + "</title>");
        ps.println("<script type=\"text/javascript\" src=\"http://seadragon.com/ajax/embed.js\"></script>");
        ps.println("<body>");
        ps.println("<script type=\"text/javascript\">");
        ps.println("Seadragon.Config.debug = true;");
        ps.println("Seadragon.Config.imagePath = \"img/\";");
        ps.println(String.format("Seadragon.embed(\"%dpx\", \"%dpx\", \"%s.xml\", %d, %d, %d, %d, \"%s\");", 400, 300, name, w, h, tilesize, overlap, imageformat));
        ps.println("</script>");
        ps.println(String.format("Image Size : %d x %d", w, h));
        ps.println("</body>");
        ps.println("</html>");
        ps.close();

        // now process image
        // level 0 is 1x1 pix, level maxlevel is full image
        for (int l = maxlevel; l >= 0; l--) {
            log.info("creating level : " + l);

            // create the folder
            File parent = new File(folder, name + "_files/" + l);
            parent.mkdirs();

            int r = 0;
            int y = 0;
            for (;;) {
                int c = 0;
                int x = 0;

                // compute the tilesize vertical
                int th = tilesize + overlap;
                if (r != 0) {
                    th += overlap;
                }
                th = Math.min(th, h - y);

                // write all images in a row
                for (;;) {
                    // compute the tilesize horizantal
                    int tw = tilesize + overlap;
                    if (c != 0) {
                        tw += overlap;
                    }
                    tw = Math.min(tw, w - x);

                    // images are named col_row
                    BufferedImage subimg = img.getSubimage(x, y, tw, th);
                    ImageIO.write(subimg, imageformat, new File(parent, String.format("%d_%d.jpg", c, r)));
                    c++;

                    if (x + tilesize >= w) {
                        break;
                    }
                    x += tilesize - overlap;
                }

                // next row of images
                r++;

                if (y + tilesize >= h) {
                    break;
                }
                y += tilesize - overlap;
            }

            // scale the image
            img = scaleImage(img, smooth);
            w = img.getWidth();
            h = img.getHeight();
        }

        return html;
    }

    /**
     * Compute the scaled image. This function is more effective than the build
     * in java functions. The function will half the image in width and height.
     * If smooth is true it will compute the average of the 2x2 area.
     * 
     * @param img
     *            the original image.
     * @param smooth
     *            set to true if the output image should smooth the image,
     *            otherwise the upper left pixel is used.
     * @return the scaled down image.
     */
    private static BufferedImage scaleImage(BufferedImage img, boolean smooth) {
        int w = img.getWidth();
        int h = img.getHeight();

        // scale the image
        int neww = (int) Math.ceil(w / 2.0);
        int newh = (int) Math.ceil(h / 2.0);

        BufferedImage scaledImage = new BufferedImage(neww, newh, BufferedImage.TYPE_INT_RGB);

        int rgb[] = new int[4];
        for (int or = 0, r = 0; r < newh; r++, or += 2) {
            for (int oc = 0, c = 0; c < neww; c++, oc += 2) {
                if (smooth) {
                    // get all 4 pixel values
                    rgb[0] = img.getRGB(oc, or);
                    if (oc + 1 >= w) {
                        if (or + 1 >= h) {
                            rgb[2] = rgb[0];
                        } else {
                            rgb[2] = img.getRGB(oc, or + 1);
                        }
                        rgb[1] = rgb[0];
                        rgb[3] = rgb[2];
                    } else {
                        rgb[1] = img.getRGB(oc + 1, or);
                        if (or + 1 >= h) {
                            rgb[2] = rgb[0];
                            rgb[3] = rgb[1];
                        } else {
                            rgb[2] = img.getRGB(oc, or + 1);
                            rgb[3] = img.getRGB(oc + 1, or + 1);
                        }
                    }

                    // smooth the 4 pixels together
                    int v = 0;
                    for (int b = 0; b < 32; b += 8) {
                        int t = 0;
                        for (int i = 0; i < 4; i++) {
                            t += ((rgb[i] >> b) & 0xff);
                        }
                        v += ((t / 4) << b);
                    }
                    scaledImage.setRGB(c, r, v);

                } else {
                    // use single pixel value, no smoothing
                    scaledImage.setRGB(c, r, img.getRGB(oc, or));
                }
            }
        }

        return scaledImage;
    }

    private static void showMemory() {
        System.gc();
        Runtime r = Runtime.getRuntime();
        log.debug(String.format("Memory : %s/%s", showHuman(r.totalMemory() - r.freeMemory()), showHuman(r.maxMemory())));
    }

    private static String showHuman(long x) {
        if (x < 1e3) {
            return String.format("%d B", x);
        } else if (x < 1e6) {
            return String.format("%5.2f KB", x / 1e3);
        } else if (x < 1e9) {
            return String.format("%5.2f MB", x / 1e6);
        } else if (x < 1e12) {
            return String.format("%5.2f GB", x / 1e9);
        } else {
            return String.format("%d B", x);
        }
    }
}
