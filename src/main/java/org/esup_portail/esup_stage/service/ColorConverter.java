package org.esup_portail.esup_stage.service;

import jakarta.persistence.Entity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorConverter {
    public static String convertHslToRgb(String html) {
        String pattern = "color:hsl\\((\\d+),\\s*(\\d+)%?,\\s*(\\d+)%?\\)";
        Pattern colorPattern = Pattern.compile(pattern);
        Matcher matcher = colorPattern.matcher(html);
        StringBuffer stringBuffer = new StringBuffer();

        while (matcher.find()) {
            int h = Integer.parseInt(matcher.group(1));
            int s = Integer.parseInt(matcher.group(2));
            int l = Integer.parseInt(matcher.group(3));
            String rgbColor = hslToRgb(h, s, l);
            matcher.appendReplacement(stringBuffer, "color:" + rgbColor);
        }
        matcher.appendTail(stringBuffer);

        return stringBuffer.toString();
    }

    public static String hslToRgb(int h, int s, int l) {
        double hue = h / 360.0;
        double saturation = s / 100.0;
        double lightness = l / 100.0;

        double c = (1 - Math.abs(2 * lightness - 1)) * saturation;
        double x = c * (1 - Math.abs((hue * 6) % 2 - 1));
        double m = lightness - c / 2;

        double r, g, b;
        if (hue >= 0 && hue < 1.0 / 6.0) {
            r = c;
            g = x;
            b = 0;
        } else if (hue >= 1.0 / 6.0 && hue < 2.0 / 6.0) {
            r = x;
            g = c;
            b = 0;
        } else if (hue >= 2.0 / 6.0 && hue < 3.0 / 6.0) {
            r = 0;
            g = c;
            b = x;
        } else if (hue >= 3.0 / 6.0 && hue < 4.0 / 6.0) {
            r = 0;
            g = x;
            b = c;
        } else if (hue >= 4.0 / 6.0 && hue < 5.0 / 6.0) {
            r = x;
            g = 0;
            b = c;
        } else {
            r = c;
            g = 0;
            b = x;
        }

        int red = (int) ((r + m) * 255);
        int green = (int) ((g + m) * 255);
        int blue = (int) ((b + m) * 255);

        return String.format("rgb(%d, %d, %d)", red, green, blue);
    }

}
