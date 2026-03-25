package org.esup_portail.esup_stage.service.logs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;

@Service
public class ExportService {

    @Autowired
    private LogFileService fileService;


    public Resource exportSingle(String path) throws IOException {
        return fileService.getResource(path);
    }

    public byte[] exportLogAsCsv(String path) throws IOException {
        Resource resource = fileService.getResource(path);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("line_number,timestamp,level,thread,message");

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream()))) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                ParsedLogLine parsed = parseLine(line);
                pw.printf("%d,%s,%s,%s,\"%s\"%n",
                        lineNum,
                        escapeCsv(parsed.timestamp()),
                        escapeCsv(parsed.level()),
                        escapeCsv(parsed.thread()),
                        escapeCsv(parsed.message()));
            }
        }

        return sw.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    // ── Helpers ──────────────────────────────────────────────────

    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

    /**
     * Tente de parser une ligne de log au format :
     * 2024-01-15 10:23:45.123 [main] INFO  c.e.MyClass - Message
     */
    private ParsedLogLine parseLine(String raw) {
        // Pattern : timestamp level [thread] message
        var timestampPattern = java.util.regex.Pattern.compile(
                "^(\\d{4}-\\d{2}-\\d{2}[T ]\\d{2}:\\d{2}:\\d{2}(?:\\.\\d+)?(?:Z|[+-]\\d{2}:?\\d{2})?)\\s+(.*)");
        var levelPattern = java.util.regex.Pattern.compile(
                "^(ERROR|FATAL|WARN|WARNING|INFO|DEBUG|TRACE)\\s+(.*)");
        var threadPattern = java.util.regex.Pattern.compile(
                "^\\[([^\\]]+)\\]\\s+(.*)");

        String timestamp = "", level = "", thread = "", message = raw;

        var m = timestampPattern.matcher(raw);
        if (m.find()) { timestamp = m.group(1); message = m.group(2); }

        m = levelPattern.matcher(message);
        if (m.find()) { level = m.group(1); message = m.group(2); }

        m = threadPattern.matcher(message);
        if (m.find()) { thread = m.group(1); message = m.group(2); }

        return new ParsedLogLine(timestamp, level, thread, message.trim());
    }

    private record ParsedLogLine(String timestamp, String level, String thread, String message) {}
}
