package org.esup_portail.esup_stage.service;

import javax.persistence.Entity;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorConverter {
    public static String hslToRgb(float h, float s, float l) {
        float r, g, b;

        if (s == 0) {
            r = g = b = l; // Les tons de gris ont la même valeur pour R, G et B
        } else {
            float q = l < 0.5f ? l * (1 + s) : l + s - l * s;
            float p = 2 * l - q;
            r = hueToRgb(p, q, h + 1f/3f);
            g = hueToRgb(p, q, h);
            b = hueToRgb(p, q, h - 1f/3f);
        }

        int red = Math.round(r * 255);
        int green = Math.round(g * 255);
        int blue = Math.round(b * 255);

        return String.format("#%02X%02X%02X", red, green, blue);
    }
    private static float hueToRgb(float p, float q, float t) {
        if (t < 0) t += 1;
        if (t > 1) t -= 1;
        if (t < 1f/6f) return p + (q - p) * 6 * t;
        if (t < 1f/2f) return q;
        if (t < 2f/3f) return p + (q - p) * (2f/3f - t) * 6;
        return p;
    }

    public static String convertHslToRgb(String html) {
        Pattern pattern = Pattern.compile("hsl\\((\\d+),\\s*(\\d+)%,\\s*(\\d+)%\\)");
        Matcher matcher = pattern.matcher(html);
        StringBuffer stringBuffer = new StringBuffer();

        while (matcher.find()) {
            float h = Float.parseFloat(matcher.group(1));
            float s = Float.parseFloat(matcher.group(2)) / 100f;
            float l = Float.parseFloat(matcher.group(3)) / 100f;

            String rgbColor = hslToRgb(h, s, l);
            matcher.appendReplacement(stringBuffer, rgbColor);
        }
        matcher.appendTail(stringBuffer);

        return stringBuffer.toString();
    }
}
