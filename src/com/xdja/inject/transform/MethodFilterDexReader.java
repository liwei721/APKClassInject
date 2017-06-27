package com.xdja.inject.transform;

import com.googlecode.d2j.reader.DexFileReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;

/**
 * Created by zlw on 2017/6/27.
 */
public class MethodFilterDexReader extends DexFileReader {
    public MethodFilterDexReader(ByteBuffer in) {
        super(in);
    }

    public MethodFilterDexReader(byte[] data) {
        super(data);
    }

    public MethodFilterDexReader(File file) throws IOException {
        super(file);
    }

    public MethodFilterDexReader(Path file) throws IOException {
        super(file);
    }

    public MethodFilterDexReader(InputStream is) throws IOException {
        super(is);
    }

    @Override
    public void accept(com.googlecode.d2j.visitors.DexFileVisitor dv) {
        super.accept(dv);
    }

    @Override
    public List<String> getClassNames() {
        return super.getClassNames();
    }

    @Override
    public void accept(com.googlecode.d2j.visitors.DexFileVisitor dv, int config) {
        super.accept(dv, config);
    }

    @Override
    public void accept(com.googlecode.d2j.visitors.DexFileVisitor dv, int classIdx, int config) {
        super.accept(dv, classIdx, config);
    }
}
