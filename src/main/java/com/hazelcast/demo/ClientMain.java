package com.hazelcast.demo;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.EntryAdapter;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.IMap;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * @mdogan 4/15/12
 */
public class ClientMain extends JFrame {

    private JPanel main = new JPanel();
    private JTextArea text;
    private JTable table;
    private DefaultTableModel tableModel = new DemoTableModel();
    private Map<String, Icon> icons = new HashMap<String, Icon>();
    private volatile String username;
    private volatile HazelcastClient client;

    public ClientMain() {
        init();
    }

    private void init() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.updateComponentTreeUI(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(new Dimension(550, 585));
        setResizable(false);
        setTitle("Hazelcast Twitter Demo Client");

        getContentPane().add(main);
        main.setLayout(null);

        text = new JTextArea();
        text.setLineWrap(true);
        text.setBorder(BorderFactory.createEtchedBorder());
        text.setBounds(22, 466, 431, 73);
        text.addKeyListener(new KeyAdapter() {
            public void keyPressed(final KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    sendTwit();
                }
            }
        });
        main.add(text);

        JButton send = new JButton("Tweet");
        send.setBounds(465, 510, 71, 29);
        send.addActionListener(new SendActionListener());
        main.add(send);

        table = new DemoTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEtchedBorder());
        scrollPane.setBounds(22, 60, 514, 390);
        table.setFillsViewportHeight(true);
        table.setPreferredScrollableViewportSize(table.getPreferredSize());
        table.setRowHeight(65);
        main.add(scrollPane);

        tableModel.addColumn("");
        tableModel.addColumn("");
        tableModel.addColumn("");
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(10);
        table.getColumnModel().getColumn(2).setPreferredWidth(450);

        table.setDefaultRenderer(String.class, new TextRenderer());

        setLocationRelativeTo(null);
    }

    void setUser(final String name) {
        try {
            client = HazelcastClient.newHazelcastClient(null);
            IMap<Long, Twit> twits = client.getMap("twits");
            twits.addEntryListener(new EntryAdapter<Long, Twit>() {
                public void entryAdded(final EntryEvent<Long, Twit> e) {
                    addTwit(e.getValue());
                }
            }, true);
            Icon icon = ImageLoader.load(name);
            JLabel picture = new JLabel(icon);
            picture.setSize(48, 48);
            picture.setLocation(22, 5);
            main.add(picture);
            icons.put(name, icon);

            JLabel user = new JLabel('@' + name);
            user.setSize(200, 50);
            user.setLocation(102, 5);
            main.add(user);
            this.username = name;
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    void addTwit(final Twit twit) {
        if (!icons.containsKey(twit.username)) {
            Icon icon = ImageLoader.load(twit.username);
            icons.put(twit.username, icon);
        }
        tableModel.insertRow(0, new Object[]{icons.get(twit.username), "", twit.text});
    }

    private class SendActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            sendTwit();
        }
    }

    void sendTwit() {
        final String message = text.getText();
        text.setText("");
        long id = client.getAtomicNumber("twit").getAndAdd(1);
        client.getMap("twits").put(id,
                                   new Twit(username, '@'+ username + '\n' + message));
    }

    private class DemoTable extends JTable {



        public DemoTable(final TableModel dm) {
            super(dm);
        }

        public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
            final Component c = super.prepareRenderer(renderer, row, column);
//            if (!isRowSelected(row)) {
//                c.setBackground(row % 2 == 0 ? getBackground() : Color.LIGHT_GRAY);
//            }
            ((JComponent) c).setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
            return c;
        }
    }

    private class DemoTableModel extends DefaultTableModel {

        public Class<?> getColumnClass(int column) {
            return column == 0 ? Icon.class : String.class;
        }

        public boolean isCellEditable(final int row, final int column) {
            return false;
        }
    }

    public static void main(String[] args) {
        ClientMain main = new ClientMain();
        final String name = JOptionPane
                .showInputDialog(main, "Login to Twitter:", "Login", JOptionPane.INFORMATION_MESSAGE);
        if (name != null && name.trim().length() > 1) {
            main.setUser(name);
            main.setVisible(true);
        } else {
            System.exit(0);
        }
    }
}
