package com.hazelcast.demo;

import javax.swing.*;
import java.net.URL;

/**
 * @mdogan 4/15/12
 */
public class ImageLoader {

    public static Icon load(String name) {
        URL url = ImageLoader.class.getClassLoader().getResource(name + ".png");
        if (url == null) {
            url = ImageLoader.class.getClassLoader().getResource("default.png");
        }
        return new ImageIcon(url);
    }
}
