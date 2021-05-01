import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.ImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

public class Defish {
    private static final double AMOUNT = 0.5;
    private static final double QUALITY = 0.8;
    private static final String SUFFIX = "-df-";
    public static void main (String[] args) throws IOException {
        double amount = AMOUNT, quality = QUALITY;
        int cwidth = 0, cheight = 0;
        Set<File> set = new TreeSet<>();
        for (String arg : args) {
            if (arg.startsWith("-a=")) {
                amount = Double.parseDouble(arg.substring(3));
                if (amount < 0) amount = 0;
                else if (amount > 1) amount = 1;
            } else if (arg.startsWith("-c=")) {
                int i;
                for (i = 3; i < arg.length(); i++) {
                    char c = arg.charAt(i);
                    if (!('0' <= c && c <= '9')) break;
                }
                cwidth = Integer.parseInt(arg.substring(3, i));
                cheight = Integer.parseInt(arg.substring(i+1));
            } else if (arg.startsWith("-q=")) {
                quality = Double.parseDouble(arg.substring(3));
                if (quality < 0) quality = 0;
                else if (quality > 1) quality = 1;
            } else {
                getFiles(new File(arg), set);
            }
        }
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality((float)quality);
        for (File rfile : set) {
            System.out.println(rfile);
            BufferedImage image = ImageIO.read(rfile);
            int cw = cwidth, ch = cheight;
            if (cwidth <= 0 || cheight <= 0) {
                cw = image.getWidth();
                ch = image.getHeight();
                double cr = (double) cw / ch;
                if (0.99 * 3 / 2 <= cr && cr <= 1.01 * 3 / 2) {
                    cw = 3;
                    ch = 2;
                } else if (0.99 * 2 / 3 <= cr && cr <= 1.01 * 2 / 3) {
                    cw = 2;
                    ch = 3;
                } else if (0.99 * 16 / 9 <= cr && cr <= 1.01 * 16 / 9) {
                    cw = 16;
                    ch = 9;
                } else if (0.99 *  9 / 16 <= cr && cr <= 1.01 * 9 / 16) {
                    cw = 9;
                    ch = 16;
                } else if (0.99 *  4 / 3 <= cr && cr <= 1.01 * 4 / 3) {
                    cw = 4;
                    ch = 3;
                } else if (0.99 *  3 / 4 <= cr && cr <= 1.01 * 3 / 4) {
                    cw = 3;
                    ch = 4;
                } else if (0.99  <= cr && cr <= 1.01) {
                    cw = 1;
                    ch = 1;
                }
            }
            for ( ; ; ) {
                if (cw % 2 == 0 && ch % 2 == 0) {
                    cw /= 2;
                    ch /= 2;
                } else if (cw % 3 == 0 && ch % 3 == 0) {
                    cw /= 3;
                    ch /= 3;
                } else if (cw % 5 == 0 && ch % 5 == 0) {
                    cw /= 5;
                    ch /= 5;
                } else if (cw % 7 == 0 && ch % 7 == 0) {
                    cw /= 7;
                    ch /= 7;
                } else {
                    break;
                }
            }
            image = convert(image, amount, cw, ch);
            String rname = rfile.getName(), wname;
            String suffix = SUFFIX + (float)amount + "-" + cw + "_" + ch;
            int i = rname.lastIndexOf(".");
            if (i >= 0) {
                wname = rname.substring(0,i) + suffix + rname.substring(i);
            } else {
                wname = rname + suffix;
            }
            File wfile = new File(rfile.getParentFile(), wname);
            ImageOutputStream output = ImageIO.createImageOutputStream(wfile);
            writer.setOutput(output);
            writer.write(null, new IIOImage(image, null, null), param);
            output.flush();
            output.close();
            System.out.println(wfile);
        }
        writer.dispose();
    }
    private static void getFiles(File file, Set<File> set) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    getFiles(files[i], set);
                }
            }
        } else if (file.canRead() &&
                   !file.getName().matches(".*" + SUFFIX +
                                           "[.0-9]+-[0-9]+_[0-9]+.*")) {
            set.add(file);
        }
    }
    private static BufferedImage convert(BufferedImage image1, double amount,
                                         int cwidth, int cheight) {
        int width1 = image1.getWidth(), height1 = image1.getHeight();
        int width2, height2, w1, h1, w2, h2;
        if (width1 >= height1) {
            w1 = width1/2;
            h1 = height1/2;
        } else {
            w1 = height1/2;
            h1 = width1/2;
        }
        int cw2, ch2;
        if (cwidth > cheight) {
            cw2 = cwidth;
            ch2 = cheight;
        } else {
            cw2 = cheight;
            ch2 = cwidth;
        }
        double a, a0 = 0.5, a3 = 1.5, a4 = 12;
        double a1 = (double) h1 / Math.sqrt(w1*w1+h1*h1), a2 = a1 * 1.1;
        if (amount < 0.1) {
            a = a0 + (a3-a0) * (1-0.1);
            a = a + (a4-a) * (0.1-amount) / 0.1;
        } else {
            a = a0 + (a3-a0) * (1-amount);
        }
        double w0 = w1, h0 = h1;
        if (a < a2) { // h1 -> h0 < rw
            h0 = Math.sqrt(w1*w1+h1*h1) * (a0 + (a-a0) * (a1-a0) / (a2-a0));
        }
        double rw = Math.sqrt(w1*w1+h1*h1) * a;
        double ddww = rw*rw - h0*h0;
        if (w0 < rw) {
            w2 = (int) (w0 * Math.sqrt((rw*rw - h0*h0) / (rw*rw - w0*w0)));
            h2 = (int) h0;
        } else {
            w2 = (int) rw;
            h2 = (int) h0;
        }
        if ((double) h2 / w2 <= (double) ch2 / cw2) {
            w2 = h2 * cw2/ch2;
            double s = Math.sqrt(ddww + w2*w2 + h2*h2) / rw;
            w2 = ((int)(w2/s)+3)/4*4;
            h2 = ((int)(h2/s)+3)/4*4;
            ddww = h2*h2 * (rw*rw/h0/h0-1);
        } else {
            h2 = w2 * ch2/cw2;
            double s = Math.sqrt(ddww + w2*w2 + h2*h2) / rw;
            w2 = ((int)(w2/s)+3)/4*4;
            h2 = ((int)(h2/s)+3)/4*4;
            if (w0 < rw) ddww = w2*w2 * (rw*rw/w0/w0-1);
            else ddww = h2*h2 * (rw*rw/h0/h0-1);
        }
        if (width1 > height1) {
            width2 = w2*2;
            height2 = h2*2;
        } else {
            w1 = width1/2;
            h1 = height1/2;
            width2 = h2*2;
            height2 = w2*2;
            w2 = width2/2;
            h2 = height2/2;
        }
        BufferedImage image2 = new BufferedImage(width2, height2,
                                                 image1.getType());
        byte[] ds1 = ((DataBufferByte)image1.getRaster().getDataBuffer()).getData();
        byte[] ds2 = ((DataBufferByte)image2.getRaster().getDataBuffer()).getData();
        for (int x2 = 0; x2 < width2; x2++) {
            for (int y2 = 0; y2 < height2; y2++) {
                double p = rw / Math.sqrt(ddww + ((x2-w2)*(x2-w2) + (y2-h2)*(y2-h2)));
                double x1 = p * (x2-w2) + w1, y1 = p * (y2-h2) + h1;
                double b = 0, g = 0, r = 0;
                for (int x = (int)x1; x <= (int)(x1+1); x++) {
                    for (int y = (int)y1; y <= (int)(y1+1); y++) {
                        double s; // bilinear
                        if (0 <= x && x < width1 && 0 <= y && y < height1 &&
                            (s = (x <= x1 ? x+1-x1 : x1-x+1) *
                             (y <= y1 ? y+1-y1 : y1-y+1)) > 0) {
                            int z1 = (x + y*width1) * 3;
                            b += s * (0xff & ds1[z1]);
                            g += s * (0xff & ds1[z1+1]);
                            r += s * (0xff & ds1[z1+2]);
                        }
                    }
                }
                b = Math.round(b);
                if (b < 0) b = 0;
                else if (b > 0xff) b = 0xff;
                g = Math.round(g);
                if (g < 0) g = 0;
                else if (g > 0xff) g = 0xff;
                r = Math.round(r);
                if (r < 0) r = 0;
                else if (r > 0xff) r = 0xff;
                int z2 = (x2 + y2*width2) * 3;
                ds2[z2] =   (byte) ((int)b & 0xff);
                ds2[z2+1] = (byte) ((int)g & 0xff);
                ds2[z2+2] = (byte) ((int)r & 0xff);
            }
        }
        return image2;
    }
}
