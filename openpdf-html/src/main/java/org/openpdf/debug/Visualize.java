package org.openpdf.debug;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Visualize {

    static private double t(double value, int min, int zoom, int border) {
        return (value - min) * zoom + border;
    }

    static public BufferedImage shape(Shape shape, int zoom, int pointSize) {
        PathIterator it = shape.getPathIterator(null);
        List<Double> real = new ArrayList<>();
        List<Double> control = new ArrayList<>();
        while (!it.isDone()) {
            double[] a = new double[6];
            int tpe = it.currentSegment(a);
            switch (tpe) {
                case PathIterator.SEG_MOVETO:
                case PathIterator.SEG_LINETO:
                    real.add(a[0]);
                    real.add(a[1]);
                    break;
                case PathIterator.SEG_QUADTO:
                    control.add(a[0]);
                    control.add(a[1]);
                    real.add(a[2]);
                    real.add(a[3]);
                case PathIterator.SEG_CUBICTO:
                    control.add(a[0]);
                    control.add(a[1]);
                    control.add(a[2]);
                    control.add(a[3]);
                    real.add(a[4]);
                    real.add(a[5]);
                case PathIterator.SEG_CLOSE:
            }
            it.next();
        }
        List<Double> all = new ArrayList<>(real);
        all.addAll(control);

        double minXF = all.get(0), minYF = all.get(1);
        double maxXF = minXF, maxYF = minYF;

        for (int i = 0; i < all.size(); i += 2) {
            double x = all.get(i), y = all.get(i + 1);
            if (x < minXF) minXF = x;
            if (x > maxXF) maxXF = x;
            if (y < minYF) minYF = y;
            if (y > maxYF) maxYF = y;
        }
        int minX = (int) Math.floor(minXF), maxX = (int) Math.ceil(maxXF);
        int minY = (int) Math.floor(minYF), maxY = (int) Math.ceil(maxYF);

        int border = zoom * 2 + 5;
        int width = (maxX - minX) * zoom + border * 2;
        int height = (maxY - minY) * zoom + border * 2;

        BufferedImage bim = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bim.createGraphics();

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        AffineTransform saveAT = g2d.getTransform();

        g2d.translate(border, border);
        g2d.scale(zoom, zoom);
        g2d.translate(-minX, -minY);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fill(shape);

        g2d.setTransform(saveAT);


        g2d.setColor(Color.GREEN);
        // draw the rows
        for (int x = 0; x <= maxX - minX; x++) {
            int xx = x * zoom + border;
            g2d.drawLine(xx, border, xx, height - border);
        }
        for (int y =0; y <= maxY - minY; y++) {
            int yy = y * zoom + border;
            g2d.drawLine(border, yy, width - border, yy);
        }

        int halfPoint = pointSize / 2;
        g2d.setColor(Color.RED);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        for (int c = 0; c < control.size(); c += 2) {
            double x = control.get(c), y = control.get(c + 1);
            double cx = t(x, minX, zoom, border), cy = t(y, minY, zoom, border);
            g2d.fillRect((int)cx - halfPoint, (int)cy - halfPoint, pointSize, pointSize);
            g2d.drawString(String.format("%3$d: %1$.3f, %2$.3f", x, y, c / 2), (int)cx,(int)cy + pointSize + 32);
        }

        g2d.setColor(Color.BLUE);
        for (int c = 0; c < real.size(); c += 2) {
            double x = real.get(c), y = real.get(c + 1);
            double cx = t(x, minX, zoom, border), cy = t(y, minY, zoom, border);
            g2d.fillRect((int)cx - halfPoint, (int)cy - halfPoint, pointSize, pointSize);
            g2d.drawString(String.format("%3$d: %1$.3f, %2$.3f", x, y, c / 2), (int)cx,(int)cy + pointSize + 15);
        }

        g2d.dispose();
        return bim;
  }
}
