package com.smartuxapi.scenario;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ScenarioDataLoader 단위 테스트")
class ScenarioDataLoaderTest {

    @Test
    @DisplayName("classpath fixture 로드 — 2턴 시나리오")
    void testLoadFromClasspath() throws Exception {
        ScenarioData data = ScenarioDataLoader.loadFromClasspath("scenarios/sample-2turn.json");
        assertNotNull(data);
        assertEquals(1, data.getSchemaVersion());
        assertEquals("fixture-2turn", data.getSessionId());
        assertEquals("chatgpt", data.getAiModel());
        assertEquals(2, data.getTurnCount());
        assertEquals(2, data.turnSize());

        ScenarioTurn t1 = data.getTurns().get(0);
        assertEquals(1, t1.getTurnNo());
        assertEquals("주문하기 눌러줘", t1.getUserPrompt());
        assertNotNull(t1.getActionQueue());
        assertEquals("click",
                t1.getActionQueue().get("actions").get(0).get("type").asText());

        ScenarioTurn t2 = data.getTurns().get(1);
        assertNull(t2.getUiInfo());
        assertEquals("cancel",
                t2.getActionQueue().get("actions").get(0).get("target").asText());
    }

    @Test
    @DisplayName("loadFromClasspath — 미존재 리소스 IOException")
    void testMissingResource() {
        assertThrows(IOException.class,
                () -> ScenarioDataLoader.loadFromClasspath("nonexistent.json"));
    }

    @Test
    @DisplayName("load(File) — 미존재 파일 IOException")
    void testMissingFile() {
        assertThrows(IOException.class,
                () -> ScenarioDataLoader.load((java.io.File) null));
    }

    @Test
    @DisplayName("loadDirectory — 다수 파일 정렬 순 로드 + 비-JSON 무시")
    void testLoadDirectory(@TempDir Path dir) throws Exception {
        // sample fixture 를 임시 디렉터리에 2번 복사 + non-JSON 파일 추가
        ScenarioData base = ScenarioDataLoader.loadFromClasspath("scenarios/sample-2turn.json");
        com.fasterxml.jackson.databind.ObjectMapper m = new com.fasterxml.jackson.databind.ObjectMapper();

        Path a = dir.resolve("a-scenario.json");
        Path b = dir.resolve("b-scenario.json");
        Path c = dir.resolve("c-not-json.txt");
        m.writeValue(a.toFile(), base);
        m.writeValue(b.toFile(), base);
        Files.write(c, "ignored".getBytes());

        java.util.List<ScenarioData> loaded = ScenarioDataLoader.loadDirectory(dir);
        assertEquals(2, loaded.size(), "non-json 파일은 무시");
        assertEquals(2, loaded.get(0).turnSize());
    }
}
