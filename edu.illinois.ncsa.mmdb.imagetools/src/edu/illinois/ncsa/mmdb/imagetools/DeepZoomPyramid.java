package edu.illinois.ncsa.mmdb.imagetools;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;

import javax.imageio.ImageIO;

public class DeepZoomPyramid {
    public static void main(String[] args) throws Exception {
        System.out.println(makePyramid(new File("test.jpg")));
    }

    public static File makePyramid(File input) throws IOException {
        return makePyramid(input, new File("pyramid"), 256, 1, "jpg");
    }

    public static File makePyramid(File input, File folder, int tilesize, int overlap, String imageformat) throws IOException {
        // make sure folder exists
        folder.mkdirs();

        // load the image
        BufferedImage img = ImageIO.read(input);
        if (img == null) {
            throw (new IOException("Could not load image."));
        }

        // get the width/height and maximum
        int w = img.getWidth();
        int h = img.getHeight();
        int m = (w > h) ? w : h;

        // compute number of levels until 1x1 pixel
        int maxlevel = (int) Math.ceil(Math.log(m) / Math.log(2));

        // create a unique id for the pyramid
        String uuid = UUID.randomUUID().toString();

        // write the xml file
        File xml = new File(folder, uuid + ".xml");
        PrintStream ps = new PrintStream(xml);
        ps.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        ps.print(String.format("<Image TileSize=\"%d\" Overlap=\"%d\" Format=\"%s\" ", tilesize, overlap, imageformat));
        ps.println("ServerFormat=\"Default\" xmlns=\"http://schemas.microsoft.com/deepzoom/2009\">");
        ps.println(String.format("<Size Width=\"%d\" Height=\"%d\" />", w, h));
        ps.println("</Image>");
        ps.close();

        // write the html file
        File html = new File(folder, uuid + ".html");
        ps = new PrintStream(html);
        ps.println("<html>");
        ps.println("<title>" + input.getName() + "</title>");
        ps.println("<script type=\"text/javascript\" src=\"http://seadragon.com/ajax/embed.js\"></script>");
        ps.println("<body>");
        ps.println("<script type=\"text/javascript\">");
        ps.println(String.format("Seadragon.embed(\"%dpx\", \"%dpx\", \"%s.xml\", %d, %d, %d, %d, \"%s\");", 400, 300, uuid, w, h, tilesize, overlap, imageformat));
        ps.println("</script>");
        ps.println("</body>");
        ps.println("</html>");
        ps.close();

        // now process image
        // level 0 is 1x1 pix, level maxlevel is full image
        for (int l = maxlevel; l >= 0; l--) {
            // create the folder
            File parent = new File(folder, uuid + "_files/" + l);
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

                    BufferedImage subimg = img.getSubimage(x, y, tw, th);

                    // images are named col_row
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
            int neww = (int) Math.ceil(w / 2.0);
            int newh = (int) Math.ceil(h / 2.0);

            BufferedImage scaledImage = new BufferedImage(neww, newh, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics2D = scaledImage.createGraphics();
            AffineTransform xform = AffineTransform.getScaleInstance((double) neww / w, (double) newh / h);
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics2D.drawImage(img, xform, null);
            graphics2D.dispose();

            img = scaledImage;
            w = neww;
            h = newh;
        }

        return html;
    }
}
