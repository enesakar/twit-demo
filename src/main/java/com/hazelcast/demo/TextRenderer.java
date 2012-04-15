package com.hazelcast.demo;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * @mdogan 4/15/12
 */
public class TextRenderer extends JTextArea implements TableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value,
                                                   final boolean isSelected, final boolean hasFocus,
                                                   final int row, final int column) {

        this.setText((String)value);
        this.setWrapStyleWord(true);
        this.setLineWrap(true);
        return this;
    }
}
