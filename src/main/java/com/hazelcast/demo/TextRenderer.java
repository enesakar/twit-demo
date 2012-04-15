package com.hazelcast.demo;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * @mdogan 4/15/12
 */
public class TextRenderer extends JTextArea implements TableCellRenderer {

    private final static Font font = new Font("Helvetica", Font.PLAIN, 12);

    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value,
                                                   final boolean isSelected, final boolean hasFocus,
                                                   final int row, final int column) {

        this.setFont(font);
        this.setWrapStyleWord(true);
        this.setLineWrap(true);
        this.setText((String) value);
        return this;
    }
}
