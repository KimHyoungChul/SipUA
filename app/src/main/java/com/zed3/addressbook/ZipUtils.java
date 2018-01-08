package com.zed3.addressbook;

import android.util.*;
import java.util.zip.*;
import java.io.*;

public class ZipUtils
{
    public static final String compress(final String p0) throws UnsupportedEncodingException, IOException {
        // TODO
        return "";
    }
    
    public static final String uncompress(final byte[] array) throws IOException {
        Log.i("xxxxxx", "uncompress enter");
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final InflaterOutputStream inflaterOutputStream = new InflaterOutputStream(byteArrayOutputStream);
        inflaterOutputStream.write(array);
        inflaterOutputStream.close();
        return new String(byteArrayOutputStream.toByteArray());
    }
}
