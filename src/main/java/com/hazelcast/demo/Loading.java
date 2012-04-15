package com.hazelcast.demo;

import javax.swing.*;
import java.awt.*;

/**
 * @mdogan 4/15/12
 */
public class Loading extends JDialog {

    private final ImageIcon image;
    private final JLabel loading;
    private final JPanel panel;

    protected final JFrame main;

    public Loading(JFrame main, final String message) {
        super(main);
        this.main = main;
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        setUndecorated(true);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setSize(200, 75);

        getContentPane().setLayout(new BorderLayout());
        panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 3));
        getContentPane().add(panel);

        image = new ImageIcon(getClass().getClassLoader().getResource("loading.gif"));
        loading = new JLabel(image);
        loading.setFont(Font.getFont(Font.SANS_SERIF));
        loading.setSize(panel.getSize());
        loading.setText(message);
        panel.add(loading, BorderLayout.CENTER);
    }

    public void showPanel() {
        Point point = main.getLocation();
        Dimension size = main.getSize();
        int x = point.x + size.width/2 - getWidth()/2;
        int y = point.y + size.height/2 - getHeight()/2;
        setLocation(x, y);
        setVisible(true);
    }

    public void hidePanel() {
        setVisible(false);
    }
}
