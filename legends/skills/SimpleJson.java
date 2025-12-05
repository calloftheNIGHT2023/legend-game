package legends.skills;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Very small JSON-ish parser tailored to our simple skills file format.
 * Supports an array of objects with string keys and string/number values.
 */
public class SimpleJson {
    public static List<Map<String, String>> parseArray(Path p) throws IOException {
        String s = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
        int idx = 0;
        int len = s.length();
        // skip whitespace
        idx = skipWhitespace(s, idx);
        if (idx >= len || s.charAt(idx) != '[') throw new IOException("expected '[' at start");
        idx++;
        List<Map<String, String>> result = new ArrayList<>();
        while (true) {
            idx = skipWhitespace(s, idx);
            if (idx >= len) break;
            if (s.charAt(idx) == ']') { idx++; break; }
            if (s.charAt(idx) == '{') {
                // parse object
                int start = idx;
                int brace = 0;
                while (idx < len) {
                    char c = s.charAt(idx);
                    if (c == '{') brace++;
                    else if (c == '}') { brace--; if (brace == 0) { idx++; break; } }
                    idx++;
                }
                String objText = s.substring(start, idx);
                Map<String, String> map = parseObject(objText);
                result.add(map);
                idx = skipWhitespace(s, idx);
                if (idx < len && s.charAt(idx) == ',') idx++;
                continue;
            }
            // unexpected char
            idx++;
        }
        return result;
    }

    public static List<Map<String, String>> parseArrayFromString(String s) throws IOException {
        int idx = 0;
        int len = s.length();
        idx = skipWhitespace(s, idx);
        if (idx >= len || s.charAt(idx) != '[') throw new IOException("expected '[' at start");
        idx++;
        List<Map<String, String>> result = new ArrayList<>();
        while (true) {
            idx = skipWhitespace(s, idx);
            if (idx >= len) break;
            if (s.charAt(idx) == ']') { idx++; break; }
            if (s.charAt(idx) == '{') {
                int start = idx;
                int brace = 0;
                while (idx < len) {
                    char c = s.charAt(idx);
                    if (c == '{') brace++;
                    else if (c == '}') { brace--; if (brace == 0) { idx++; break; } }
                    idx++;
                }
                String objText = s.substring(start, idx);
                Map<String, String> map = parseObject(objText);
                result.add(map);
                idx = skipWhitespace(s, idx);
                if (idx < len && s.charAt(idx) == ',') idx++;
                continue;
            }
            idx++;
        }
        return result;
    }

    private static Map<String, String> parseObject(String txt) throws IOException {
        Map<String, String> m = new HashMap<>();
        int i = 0, n = txt.length();
        if (i < n && txt.charAt(i) == '{') i++; else throw new IOException("expected '{' in object");
        while (i < n) {
            i = skipWhitespace(txt, i);
            if (i < n && txt.charAt(i) == '}') break;
            // parse key
            if (i >= n || txt.charAt(i) != '"') throw new IOException("expected '" + '"' + " for key");
            int kstart = ++i;
            while (i < n && txt.charAt(i) != '"') i++;
            if (i >= n) throw new IOException("unterminated key");
            String key = txt.substring(kstart, i);
            i++;
            i = skipWhitespace(txt, i);
            if (i >= n || txt.charAt(i) != ':') throw new IOException("expected ':' after key");
            i++;
            i = skipWhitespace(txt, i);
            // parse value (string or number)
            if (i < n && txt.charAt(i) == '"') {
                i++;
                StringBuilder sb = new StringBuilder();
                while (i < n) {
                    char c = txt.charAt(i);
                    if (c == '\\') {
                        if (i + 1 < n) { sb.append(txt.charAt(i+1)); i += 2; continue; }
                        else throw new IOException("unterminated escape");
                    }
                    if (c == '"') break;
                    sb.append(c); i++;
                }
                if (i >= n) throw new IOException("unterminated string value");
                String val = sb.toString();
                i++; // skip closing quote
                m.put(key, val);
            } else {
                // number or bareword
                int vstart = i;
                while (i < n) {
                    char c = txt.charAt(i);
                    if (c == ',' || c == '\n' || c == '\r' || c == '}' ) break;
                    i++;
                }
                String val = txt.substring(vstart, i).trim();
                m.put(key, val);
            }
            // skip to next or end
            i = skipWhitespace(txt, i);
            if (i < n && txt.charAt(i) == ',') { i++; continue; }
        }
        return m;
    }

    private static int skipWhitespace(String s, int i) {
        int n = s.length();
        while (i < n) {
            char c = s.charAt(i);
            if (!Character.isWhitespace(c)) break;
            i++;
        }
        return i;
    }
}
