package org.thezero.multipart;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class Part {
    private Map<String,List<String>> headers;
    private InputStream body;

    Part(Map<String,List<String>> headers, InputStream body) {
        this.headers = headers;
        this.body = body;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public List<String> getValues(String name) {
        return headers.get(name.toLowerCase());
    }

    public String getFirstValue(String name) {
        List<String> values = getValues(name);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }

    public InputStream getBody() {
        return body;
    }

    public void close() throws IOException {
        body.close();
    }
}

