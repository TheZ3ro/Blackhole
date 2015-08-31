package org.thezero.multipart;

        import java.util.HashMap;
        import java.util.Map;

public class ValueParser {
    private char chars[];
    private int pos=0;

    public static Map<String,String> parse(String s) {
        return new ValueParser(s).parse();
    }

    private ValueParser(char chars[]) {
        this.chars = chars;
    }

    private ValueParser(String s) {
        this(s.toCharArray());
    }

    private Map<String,String> parse() {
        Map<String,String> result = new HashMap<String,String>();
        while (pos < chars.length) {
            skipSpaces();
            if (pos >= chars.length) {
                break;
            }
            StringBuilder buf = new StringBuilder();
            int nameEnd = 0;
            char c = ' ';
            while (pos < chars.length) {
                c = chars[pos++];
                if (c == '=' || c == ';') {
                    break;
                }
                if (c == '"') {
                    quotedString(buf);
                    nameEnd = buf.length();
                } else {
                    buf.append(c);
                    if (!Character.isWhitespace(c)) {
                        nameEnd = buf.length();
                    }
                }
            }

            String name = buf.substring(0, nameEnd).toLowerCase();
            skipSpaces();
            if (c != '=') {
                result.put("", name);
            } else {
                buf.setLength(0);
                int valEnd = 0;
                while (pos < chars.length) {
                    c = chars[pos++];
                    if (c == ';') {
                        break;
                    }
                    if (c == '"') {
                        quotedString(buf);
                        valEnd = buf.length();
                    } else {
                        buf.append(c);
                        if (!Character.isWhitespace(c)) {
                            valEnd = buf.length();
                        }
                    }
                }
                String value = buf.substring(0, valEnd);
                result.put(name.toLowerCase(), value);
            }
        }
        return result;
    }

    private void skipSpaces() {
        while (pos < chars.length && Character.isWhitespace(chars[pos])) {
            ++pos;
        }
    }

    private void quotedString(StringBuilder buf) {
        while (pos < chars.length) {
            char c = chars[pos++];
            if (c == '"') {
                break;
            } else if (c == '\\' && pos < chars.length) {
                c = chars[pos++];
            }
            buf.append(c);
        }
    }
}
