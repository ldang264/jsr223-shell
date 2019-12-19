package jsr223.shell.util;

import java.io.*;
import java.util.Scanner;

public class IOUtil {

    public static String toString(Reader reader) {
        Scanner s = new Scanner(reader).useDelimiter("\\A");
        return s.hasNext() ? s.next() : null;
    }

    public static void pipe(Reader from, Writer to) throws IOException {
        char[] buff = new char[1024];
        int n = from.read(buff);
        while (n != -1) {
            to.write(buff, 0, n);
            to.flush();
            n = from.read(buff);
        }
        from.close();
    }

    public static void pipe(String from, Writer to) throws IOException {
        pipe(new StringReader(from), to);
    }

    public static void closeSilently(OutputStream os) {
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                ;
            }
        }
    }

}
