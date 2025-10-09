package com.example.evshop.util;

import android.util.Base64;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

public final class JwtUtils {
    private JwtUtils() {}

    public static JsonObject parsePayload(String jwt) {
        try {
            if (jwt == null) return null;
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) return null;
            byte[] decoded = Base64.decode(parts[1],
                    Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
            String json = new String(decoded, StandardCharsets.UTF_8);
            return JsonParser.parseString(json).getAsJsonObject();
        } catch (Throwable t) {
            return null;
        }
    }

    public static String getDisplayName(String jwt) {
        JsonObject p = parsePayload(jwt);
        if (p == null) return null;

        // 1) Ưu tiên FullName (đúng với payload bạn gửi)
        String v = getString(p, "FullName");
        if (!v.isEmpty()) return v;

        // 2) Các biến thể tên thường gặp
        String[] directKeys = {"fullName","name","given_name","preferred_username","unique_name"};
        for (String k : directKeys) {
            v = getString(p, k);
            if (!v.isEmpty()) return v;
        }

        // 3) Quét claim có namespace (…/name, …/unique_name, …/givenname…)
        for (Map.Entry<String, JsonElement> e : p.entrySet()) {
            String key = e.getKey();
            String simple = key.contains("/") ? key.substring(key.lastIndexOf('/') + 1) : key;
            simple = simple.toLowerCase(Locale.ROOT);
            if (simple.equals("name") || simple.equals("unique_name") || simple.equals("given_name") || simple.equals("fullname")) {
                String val = safeTrim(e.getValue().isJsonNull() ? null : e.getValue().getAsString());
                if (!val.isEmpty()) return val;
            }
        }

        // 4) Dùng Email (lấy phần trước @)
        v = getString(p, "Email");
        if (v.isEmpty()) v = getString(p, "email");
        if (!v.isEmpty()) {
            int at = v.indexOf('@');
            return at > 0 ? v.substring(0, at) : v;
        }

        // 5) Cuối cùng: id/subject nếu muốn (ít thân thiện)
        v = getString(p, "sub");
        if (!v.isEmpty()) return v;
        v = getString(p, "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier");
        if (!v.isEmpty()) return v;

        return null;
    }

    private static String getString(JsonObject o, String key) {
        if (o.has(key) && !o.get(key).isJsonNull()) {
            return safeTrim(o.get(key).getAsString());
        }
        return "";
    }

    private static String safeTrim(String s) { return s == null ? "" : s.trim(); }
}