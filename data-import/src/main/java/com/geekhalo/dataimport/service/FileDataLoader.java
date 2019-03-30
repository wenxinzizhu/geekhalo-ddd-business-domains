package com.geekhalo.dataimport.service;

import java.io.File;

/**
 * Created by taoli on 2019/1/1.
 */
public interface FileDataLoader {
    void load(File file, LineCallback callback);

    interface LineCallback {
        void onData(String line);
    }
}
