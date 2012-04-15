package com.hazelcast.demo;

import javax.swing.*;
import java.net.URL;

/**
 * @mdogan 4/15/12
 */
public class TwitIconLoader {

    public static void loadIcon(final String name, final Callback callback) throws Exception {
        final URL url = new URL("https://api.twitter.com/1/users/profile_image?screen_name="
                                + name + "&size=normal");
        final SwingWorker sw = new SwingWorker<ImageIcon, Void>() {
            protected ImageIcon doInBackground() throws Exception {
                return new ImageIcon(url);
            }

            protected void done() {
                if (callback != null) {
                    try {
                        callback.call(get());
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onError(e);
                    }
                }
            }
        };
        sw.execute();
    }

    public static interface Callback {
        public void call(Icon icon);
        public void onError(Exception e);
    }
}
