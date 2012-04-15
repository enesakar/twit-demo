package com.hazelcast.demo;

import com.hazelcast.config.Config;
import com.hazelcast.config.EntryListenerConfig;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.*;
import com.hazelcast.demo.TwitIconLoader.Callback;
import com.hazelcast.impl.GroupProperties;
import com.hazelcast.partition.MigrationEvent;
import com.hazelcast.partition.MigrationListener;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @mdogan 4/15/12
 */
public class ServerMain extends JFrame {

    private JPanel main = new JPanel();
    private JTable table;
    private DefaultTableModel tableModel = new DemoTableModel();
    private Map<String, Icon> icons = new HashMap<String, Icon>();
    private volatile IMap<Long, Twit> twits;

    public ServerMain() {
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
        setSize(new Dimension(500, 585));
        setResizable(false);
        setTitle("Hazelcast Twitter Demo Server");

        getContentPane().add(main);
        main.setLayout(null);

        table = new DemoTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEtchedBorder());
        scrollPane.setBounds(22, 10, 464, 550);
        table.setFillsViewportHeight(true);
        table.setPreferredScrollableViewportSize(table.getPreferredSize());
        table.setRowHeight(50);
        main.add(scrollPane);

        tableModel.addColumn("");
        tableModel.addColumn("");
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(410);

        setLocationRelativeTo(null);
    }

    private class DemoTable extends JTable {

        public DemoTable(final TableModel dm) {
            super(dm);
        }

        public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
            final Component c = super.prepareRenderer(renderer, row, column);
            if (!isRowSelected(row)) {
                c.setBackground(row % 2 == 0 ? getBackground() : Color.LIGHT_GRAY);
            }
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

    void setHazelcastInstance(HazelcastInstance hz) {
        twits = hz.getMap("twits");
    }

    void loadLocalTwitsIfRequired(MigrationEvent event) {
        if (event.getNewOwner().localMember() || event.getOldOwner().localMember()) {
            loadLocalTwits();
        }
    }

    synchronized void loadLocalTwits() {
        if (twits == null) return;
        final java.util.List<Long> keys = new LinkedList<Long>(twits.localKeySet());
        Collections.sort(keys);
        System.err.println("Loading local twits..." + keys);
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    tableModel.setRowCount(0);
                    tableModel.fireTableDataChanged();
                    for (Long key : keys) {
                        addTwitInternal(twits.get(key));
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void addTwit(final Twit twit) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    addTwitInternal(twit);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addTwitInternal(final Twit twit) {
        if (icons.containsKey(twit.username)) {
            tableModel.insertRow(0, new Object[]{icons.get(twit.username), twit.text});
        } else {
            try {
                TwitIconLoader.loadIcon(twit.username, new Callback() {
                    public void call(final Icon icon) {
                        icons.put(twit.username, icon);
                        tableModel.insertRow(0, new Object[]{icons.get(twit.username), twit.text});
                    }

                    public void onError(final Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        final ServerMain main = new ServerMain();
        main.setVisible(true);
        Config config = new XmlConfigBuilder().build();
        config.setProperty(GroupProperties.PROP_PARTITION_MIGRATION_INTERVAL, "0");
        config.getMapConfig("twits").addEntryListenerConfig(new EntryListenerConfig(new EntryAdapter<Long, Twit>() {
            public void entryAdded(final EntryEvent<Long, Twit> e) {
                Twit twit = e.getValue();
                main.addTwit(twit);
            }
        }, true, true));

        config.addListenerConfig(new ListenerConfig(new MembershipListener() {
            public void memberAdded(final MembershipEvent e) {
            }

            public void memberRemoved(final MembershipEvent e) {
                main.loadLocalTwits();
            }
        }));

        config.addListenerConfig(new ListenerConfig(new MigrationListener() {
            public void migrationStarted(final MigrationEvent e) {
            }

            public void migrationCompleted(final MigrationEvent e) {
                main.loadLocalTwitsIfRequired(e);
            }
        }));

        final HazelcastInstance hz = Hazelcast.init(config);
        main.setHazelcastInstance(hz);
        main.loadLocalTwits();
    }
}
