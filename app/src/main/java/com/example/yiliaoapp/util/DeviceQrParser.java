package com.example.yiliaoapp.util;

import org.json.JSONObject;

/**
 * 支持二维码内容：
 * 1) JSON：{"name":"..","code":"..","dept":".."}（可用 deviceName/deviceCode/department 等别名）
 * 2) 竖线分隔：设备名称|设备编号|科室
 * 3) 纯文本：仅填入设备编号，其余手动补全
 */
public final class DeviceQrParser {

    public static final class Parsed {
        public final String name;
        public final String code;
        public final String dept;

        public Parsed(String name, String code, String dept) {
            this.name = nz(name);
            this.code = nz(code);
            this.dept = nz(dept);
        }

        public boolean isComplete() {
            return !name.isEmpty() && !code.isEmpty() && !dept.isEmpty();
        }
    }

    private DeviceQrParser() {
    }

    public static Parsed parse(String raw) {
        if (raw == null) {
            return new Parsed("", "", "");
        }
        String t = raw.trim();
        if (t.isEmpty()) {
            return new Parsed("", "", "");
        }
        if (t.startsWith("{")) {
            Parsed fromJson = tryJson(t);
            if (fromJson != null) {
                return fromJson;
            }
        }
        if (t.contains("|")) {
            String[] p = t.split("\\|", 3);
            String n = p.length > 0 ? p[0].trim() : "";
            String c = p.length > 1 ? p[1].trim() : "";
            String d = p.length > 2 ? p[2].trim() : "";
            return new Parsed(n, c, d);
        }
        return new Parsed("", t, "");
    }

    private static Parsed tryJson(String t) {
        try {
            JSONObject o = new JSONObject(t);
            String name = firstKey(o, "name", "deviceName", "n");
            String code = firstKey(o, "code", "deviceCode", "deviceId", "id");
            String dept = firstKey(o, "dept", "department", "d");
            return new Parsed(name, code, dept);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String firstKey(JSONObject o, String... keys) {
        for (String k : keys) {
            if (!o.has(k) || o.isNull(k)) {
                continue;
            }
            String s = o.optString(k, "").trim();
            if (!s.isEmpty()) {
                return s;
            }
        }
        return "";
    }

    private static String nz(String s) {
        return s == null ? "" : s.trim();
    }
}
