package com.smartuxapi.scenario;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 시나리오 JSON 파일을 {@link ScenarioData} 로 로드.
 *
 * @since lib 0.9.5
 */
public final class ScenarioDataLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ScenarioDataLoader() {}

    public static ScenarioData load(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IOException("scenario file not found: " + (file == null ? "null" : file.getAbsolutePath()));
        }
        return MAPPER.readValue(file, ScenarioData.class);
    }

    public static ScenarioData load(Path path) throws IOException {
        return load(path.toFile());
    }

    /**
     * 클래스패스 리소스에서 로드 — 테스트 fixture 용.
     *
     * @param resourcePath 예: {@code "scenarios/sample.json"}
     */
    public static ScenarioData loadFromClasspath(String resourcePath) throws IOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try (InputStream in = cl.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IOException("classpath resource not found: " + resourcePath);
            }
            return MAPPER.readValue(in, ScenarioData.class);
        }
    }

    /**
     * 디렉터리 내 모든 {@code *.json} 파일을 시간 순 (파일명 오름차순) 으로 로드.
     */
    public static List<ScenarioData> loadDirectory(Path dir) throws IOException {
        if (dir == null || !Files.isDirectory(dir)) {
            throw new IOException("not a directory: " + dir);
        }
        List<ScenarioData> result = new ArrayList<>();
        try (Stream<Path> stream = Files.list(dir)) {
            stream
                .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".json"))
                .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                .forEach(p -> {
                    try {
                        result.add(load(p));
                    } catch (IOException e) {
                        throw new RuntimeException("failed to load: " + p, e);
                    }
                });
        }
        return result;
    }
}
