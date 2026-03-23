package org.esup_portail.esup_stage.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class LogTailerService {

    private static final long POLL_INTERVAL_MS = 500L;
    private static final long SSE_RETRY_MS = 3000L;
    private static final int READ_BUFFER_SIZE = 8192;
    private static final Pattern LOG_PATTERN = Pattern.compile(
            "^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}) \\[([^\\]]+)]\\s+([A-Z]+)\\s+([^\\s]+)\\s+-\\s+(.*)$"
    );

    @Value("${logging.file.name:logs/esup-stage.log}")
    private String logFileName;

    private final Map<UUID, TailSession> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4, new TailThreadFactory());

    public UUID startStreaming(SseEmitter emitter, int historyLines) {
        UUID sessionId = UUID.randomUUID();
        TailSession session = new TailSession(emitter, resolveLogFilePath());
        sessions.put(sessionId, session);

        try {
            sendHistory(session, historyLines);
            session.position = currentFileSize(session.logFilePath);
            session.future = scheduler.scheduleWithFixedDelay(
                    () -> poll(sessionId),
                    POLL_INTERVAL_MS,
                    POLL_INTERVAL_MS,
                    TimeUnit.MILLISECONDS
            );
            return sessionId;
        } catch (Exception ex) {
            removeSession(sessionId);
            if (isClientDisconnectException(ex)) {
                log.debug("Log stream {} closed during startup: {}", sessionId, ex.getMessage());
                return sessionId;
            }
            throw new IllegalStateException("Unable to start log stream", ex);
        }
    }

    public void stopStreaming(UUID sessionId) {
        removeSession(sessionId);
    }

    @PreDestroy
    public void shutdown() {
        for (UUID sessionId : new ArrayList<>(sessions.keySet())) {
            removeSession(sessionId);
        }
        scheduler.shutdownNow();
    }

    private void poll(UUID sessionId) {
        TailSession session = sessions.get(sessionId);
        if (session == null) {
            return;
        }

        synchronized (session.monitor) {
            if (!sessions.containsKey(sessionId)) {
                return;
            }

            try {
                long fileSize = currentFileSize(session.logFilePath);
                if (fileSize < session.position) {
                    // Rotation/truncate: restart from beginning of the new file.
                    closeReader(session);
                    session.pendingLineBuffer.reset();
                    session.position = 0L;
                }

                if (fileSize == session.position) {
                    return;
                }

                openReaderIfNeeded(session);
                if (session.reader == null) {
                    return;
                }

                session.reader.seek(session.position);
                byte[] chunk = new byte[READ_BUFFER_SIZE];
                int readCount;

                while ((readCount = session.reader.read(chunk)) > 0) {
                    processChunk(session, chunk, readCount);
                }

                session.position = session.reader.getFilePointer();
            } catch (Exception ex) {
                boolean clientDisconnect = isClientDisconnectException(ex);
                if (clientDisconnect) {
                    log.debug("Log stream {} client disconnected: {}", sessionId, ex.getMessage());
                } else {
                    log.warn("Log stream {} stopped after error: {}", sessionId, ex.getMessage());
                }
                removeSession(sessionId);
                if (!clientDisconnect) {
                    try {
                        session.emitter.completeWithError(ex);
                    } catch (Exception ignore) {
                        // Emitter can already be completed by the container.
                    }
                }
            }
        }
    }

    private void processChunk(TailSession session, byte[] chunk, int readCount) throws IOException {
        int segmentStart = 0;

        for (int i = 0; i < readCount; i++) {
            if (chunk[i] == '\n') {
                int segmentLength = i - segmentStart;
                if (segmentLength > 0) {
                    session.pendingLineBuffer.write(chunk, segmentStart, segmentLength);
                }

                emitBufferedLine(session);
                segmentStart = i + 1;
            }
        }

        if (segmentStart < readCount) {
            session.pendingLineBuffer.write(chunk, segmentStart, readCount - segmentStart);
        }
    }

    private void emitBufferedLine(TailSession session) throws IOException {
        byte[] bytes = session.pendingLineBuffer.toByteArray();
        session.pendingLineBuffer.reset();

        String line = new String(bytes, StandardCharsets.UTF_8);
        if (line.endsWith("\r")) {
            line = line.substring(0, line.length() - 1);
        }

        LogLineEvent payload = parseLine(line, false);
        session.emitter.send(SseEmitter.event().reconnectTime(SSE_RETRY_MS).data(payload));
    }

    private void sendHistory(TailSession session, int historyLines) throws IOException {
        if (historyLines <= 0 || !Files.exists(session.logFilePath)) {
            return;
        }

        List<String> lines = new ArrayList<>(historyLines);
        try (ReversedLinesFileReader reader = new ReversedLinesFileReader(session.logFilePath.toFile(), StandardCharsets.UTF_8)) {
            String line;
            while (lines.size() < historyLines && (line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        Collections.reverse(lines);
        for (String line : lines) {
            LogLineEvent payload = parseLine(line, true);
            session.emitter.send(SseEmitter.event().reconnectTime(SSE_RETRY_MS).data(payload));
        }
    }

    private void openReaderIfNeeded(TailSession session) throws IOException {
        if (session.reader != null || !Files.exists(session.logFilePath)) {
            return;
        }

        session.reader = new RandomAccessFile(session.logFilePath.toFile(), "r");
        if (session.position > 0L) {
            session.reader.seek(session.position);
        }
    }

    private void closeReader(TailSession session) {
        if (session.reader == null) {
            return;
        }
        try {
            session.reader.close();
        } catch (IOException e) {
            log.debug("Unable to close log reader cleanly: {}", e.getMessage());
        } finally {
            session.reader = null;
        }
    }

    private void removeSession(UUID sessionId) {
        TailSession session = sessions.remove(sessionId);
        if (session == null) {
            return;
        }

        synchronized (session.monitor) {
            if (session.future != null) {
                session.future.cancel(true);
            }
            closeReader(session);
            session.pendingLineBuffer.reset();
        }
    }

    private long currentFileSize(Path filePath) throws IOException {
        if (!Files.exists(filePath)) {
            return 0L;
        }
        return Files.size(filePath);
    }

    private Path resolveLogFilePath() {
        return Path.of(logFileName).toAbsolutePath().normalize();
    }

    private boolean isClientDisconnectException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null) {
                String normalized = message.toLowerCase(Locale.ROOT);
                if (normalized.contains("broken pipe")
                        || normalized.contains("connection reset by peer")
                        || normalized.contains("forcibly closed by the remote host")
                        || normalized.contains("une connexion etablie a ete abandonnee")
                        || normalized.contains("servletoutputstream failed to flush")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

    private LogLineEvent parseLine(String rawLine, boolean historical) {
        Matcher matcher = LOG_PATTERN.matcher(rawLine);
        if (matcher.matches()) {
            return new LogLineEvent(
                    matcher.group(1),
                    matcher.group(2),
                    matcher.group(3),
                    matcher.group(4),
                    matcher.group(5),
                    rawLine,
                    historical
            );
        }

        return new LogLineEvent(null, null, "UNKNOWN", null, rawLine, rawLine, historical);
    }

    public record LogLineEvent(
            String timestamp,
            String thread,
            String level,
            String logger,
            String message,
            String raw,
            boolean historical
    ) {
    }

    private static final class TailSession {
        private final SseEmitter emitter;
        private final Path logFilePath;
        private final Object monitor = new Object();
        private final ByteArrayOutputStream pendingLineBuffer = new ByteArrayOutputStream();
        private RandomAccessFile reader;
        private ScheduledFuture<?> future;
        private long position = 0L;

        private TailSession(SseEmitter emitter, Path logFilePath) {
            this.emitter = emitter;
            this.logFilePath = logFilePath;
        }
    }

    private static final class TailThreadFactory implements ThreadFactory {
        private final AtomicInteger threadIndex = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "log-tailer-" + threadIndex.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    }
}
