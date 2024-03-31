package com.lowagie.toolbox;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Properties;

public class Frame extends JFrame {
    private final JDesktopPane desktop;
    private final ArrayList<AbstractTool> toolarray = new ArrayList<>();

    /**
     * The list of tools in the toolbox.
     */
    private Properties toolmap = new Properties();
    /**
     * x-coordinate of the location of a new internal frame.
     */
    private int locationX = 0;
    /**
     * y-coordinate of the location of a new internal frame.
     */
    private int locationY = 0;
    public Frame(){
        desktop = new JDesktopPane();
    }

    public void centerFrame(java.awt.Frame f) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = f.getSize();
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        f.setLocation((screenSize.width - frameSize.width) / 2,
                (screenSize.height - frameSize.height) / 2);
    }

    public AbstractTool createFrame(String name) throws InstantiationException,
            IllegalAccessException, ClassNotFoundException,
            PropertyVetoException {
        AbstractTool ti = null;
        String classname = (String) toolmap.get(name);
        ti = (AbstractTool) Class.forName(classname).newInstance();
        toolarray.add(ti);
        JInternalFrame f = ti.getInternalFrame();
        f.setLocation(locationX, locationY);
        locationX += 25;
        if (locationX > this.getWidth() + 50) {
            locationX = 0;
        }
        locationY += 25;
        if (locationY > this.getHeight() + 50) {
            locationY = 0;
        }
        f.setVisible(true);
        desktop.add(f);
        f.setSelected(true);
        return ti;
    }

}
