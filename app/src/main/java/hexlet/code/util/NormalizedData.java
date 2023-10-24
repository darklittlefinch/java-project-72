package hexlet.code.util;

import java.net.URL;

public final class NormalizedURL {
    public static String getNormalizedURL(URL url) {
        String protocol = url.getProtocol();
        String symbols = "://";
        String host = url.getHost();
        String colonBeforePort = url.getPort() == -1 ? "" : ":";
        String port = url.getPort() == -1 ? "" : String.valueOf(url.getPort());

        return protocol + symbols + host + colonBeforePort + port;
    }
}
