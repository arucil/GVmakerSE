package eastsun.jgvm.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class IOUtil {
    public static final byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buffer = new byte[512];
        int size;
        while ((size = in.read(buffer)) >= 0) {
            bout.write(buffer, 0, size);
        }
        return bout.toByteArray();
    }
}
