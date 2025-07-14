package com.lowagie.toolbox.arguments;

import com.lowagie.toolbox.AbstractTool;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public abstract class AbstractArgument implements ActionListener, PropertyChangeListener {

    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    /**
     * value of the argument.
     */
    protected Object value = null;
    /**
     * short name for the argument.
     */
    protected String name;
    /**
     * reference to the internal frame
     */
    protected AbstractTool tool;
    /**
     * describes the argument.
     */
    protected String description;

    public AbstractArgument() {
    }

    public AbstractArgument(AbstractTool tool, String name, String description, Object value) {
        this.tool = tool;
        this.name = name;
        this.description = description;
        this.value = value;
    }

    protected synchronized void firePropertyChange(PropertyChangeEvent evt) {
        pcs.firePropertyChange(evt);
    }

    public synchronized void removePropertyChangeListener(
            PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    /**
     * @return Returns the value.
     */
    public Object getValue() {
        return value;
    }

    /**
     * @param value The value to set.
     */
    public void setValue(Object value) {
        Object oldvalue = this.value;
        this.value = value;
        tool.valueHasChanged(this);
        this.firePropertyChange(new PropertyChangeEvent(this, name, oldvalue,
                this.value));
    }

    public void setValue(Object value, String propertyname) {
        Object oldvalue = this.value;
        this.value = value;
        tool.valueHasChanged(this);
        this.firePropertyChange(new PropertyChangeEvent(this, propertyname,
                oldvalue, this.value));
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Give you a String that can be used in a usage description.
     *
     * @return a String
     */
    public String getUsage() {
        StringBuilder buf = new StringBuilder("  ");
        buf.append(name);
        buf.append(" -  ");
        buf.append(description);
        buf.append('\n');
        return buf.toString();
    }

    public AbstractTool getTool() {
        return tool;
    }

    public void setTool(AbstractTool tool) {
        this.tool = tool;
    }

    /**
     * Gets the argument as an object.
     *
     * @return an object
     * @throws InstantiationException if the specified key cannot be compared with the keys currently in the map
     */
    public Object getArgument() throws InstantiationException {
        if (value == null) {
            return null;
        }
        return value;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        System.out.println("AbstractArgument PropertyChange");
    }

    public abstract void actionPerformed(ActionEvent e);

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    public String toString() {
        return getValue().toString();
    }

}
