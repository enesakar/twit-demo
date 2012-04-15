package com.hazelcast.demo;

import com.hazelcast.nio.DataSerializable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @mdogan 4/15/12
 */
public class Twit implements DataSerializable {

    public String username;
    public String text;

    public Twit() {
    }

    public Twit(final String username, final String text) {
        this.username = username;
        this.text = text;
    }

    @Override
    public void writeData(final DataOutput out) throws IOException {
        out.writeUTF(username);
        out.writeUTF(text);
    }

    @Override
    public void readData(final DataInput in) throws IOException {
        username = in.readUTF();
        text = in.readUTF();
    }
}
