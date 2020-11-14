package utils.compress.gzip;

import utils.compress.Compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipCompress implements Compress {

    private static final int BUFFER_SIZE = 1024 * 4;

    @Override
    public byte[] compress(byte[] bytes) {
        if(bytes == null) throw new NullPointerException("bytes is null");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOutputStream =  new GZIPOutputStream(out)) {
            gzipOutputStream.write(bytes);
            gzipOutputStream.flush();
            gzipOutputStream.finish();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("gzip compress error: ", e);
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        if(bytes == null) throw new NullPointerException("bytes is null");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int n;
            while((n = gzipInputStream.read(buffer)) > -1) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("gzip decompress error: ", e);
        }
    }
}
