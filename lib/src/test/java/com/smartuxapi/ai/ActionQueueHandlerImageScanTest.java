package com.smartuxapi.ai;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.smartuxapi.ai.vision.ImageScanInfo;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ActionQueueHandler — 이미지 스캔 정보 통합 테스트")
class ActionQueueHandlerImageScanTest {

    private ActionQueueHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ActionQueueHandler();
    }

    @Test
    @DisplayName("초기 상태 — 스캔 정보 없음, isCurrentViewInfo()=false")
    void testInitial() {
        assertTrue(handler.getImageScanInfoList().isEmpty());
        assertFalse(handler.isCurrentViewInfo());
    }

    @Test
    @DisplayName("addImageScanInfo — 정보가 등록되고 isCurrentViewInfo()=true 가 됨")
    void testAddSingle() {
        ImageScanInfo info = new ImageScanInfo("https://x/1.png", "Login");
        handler.addImageScanInfo(info);

        List<ImageScanInfo> list = handler.getImageScanInfoList();
        assertEquals(1, list.size());
        assertSame(info, list.get(0));
        assertTrue(handler.isCurrentViewInfo(), "스캔정보만 있어도 뷰 정보로 간주되어야 한다");
        assertTrue(handler.isViewInfoChanged());
    }

    @Test
    @DisplayName("dedupe — 동일 imageUrl 재주입 시 최신값으로 대체, 리스트 크기는 유지")
    void testDedupe() {
        ImageScanInfo first = new ImageScanInfo("u1", "OLD");
        ImageScanInfo updated = new ImageScanInfo("u1", "NEW");
        handler.addImageScanInfo(first);
        handler.addImageScanInfo(updated);

        List<ImageScanInfo> list = handler.getImageScanInfoList();
        assertEquals(1, list.size(), "동일 URL 은 하나만 유지되어야 한다");
        assertEquals("NEW", list.get(0).getExtractedText());
    }

    @Test
    @DisplayName("서로 다른 URL 은 모두 보관 (삽입 순서 유지)")
    void testMultipleDifferentUrls() {
        handler.addImageScanInfo(new ImageScanInfo("u1", "A"));
        handler.addImageScanInfo(new ImageScanInfo("u2", "B"));
        handler.addImageScanInfo(new ImageScanInfo("u3", "C"));

        List<ImageScanInfo> list = handler.getImageScanInfoList();
        assertEquals(3, list.size());
        assertEquals("u1", list.get(0).getImageUrl());
        assertEquals("u2", list.get(1).getImageUrl());
        assertEquals("u3", list.get(2).getImageUrl());
    }

    @Test
    @DisplayName("addImageScanInfoList — 각 항목 dedupe 적용")
    void testAddList() {
        handler.addImageScanInfo(new ImageScanInfo("u1", "OLD"));
        handler.addImageScanInfoList(Arrays.asList(
                new ImageScanInfo("u1", "NEW"),
                new ImageScanInfo("u2", "B")
        ));

        List<ImageScanInfo> list = handler.getImageScanInfoList();
        assertEquals(2, list.size());
        assertEquals("NEW", list.get(0).getExtractedText());
        assertEquals("u2", list.get(1).getImageUrl());
    }

    @Test
    @DisplayName("null 입력은 무시")
    void testNullInputsIgnored() {
        handler.addImageScanInfo(null);
        handler.addImageScanInfoList(null);
        assertTrue(handler.getImageScanInfoList().isEmpty());
    }

    @Test
    @DisplayName("getImageScanInfoList 는 읽기 전용")
    void testUnmodifiable() {
        handler.addImageScanInfo(new ImageScanInfo("u", "t"));
        List<ImageScanInfo> list = handler.getImageScanInfoList();
        assertThrows(UnsupportedOperationException.class,
                () -> list.add(new ImageScanInfo("x", "y")));
    }

    @Test
    @DisplayName("clearImageScanInfo — 전체 삭제 + viewInfoChanged 갱신")
    void testClear() {
        handler.addImageScanInfo(new ImageScanInfo("u", "t"));
        handler.markViewInfoAsSent(); // viewInfoChanged = false
        handler.clearImageScanInfo();

        assertTrue(handler.getImageScanInfoList().isEmpty());
        assertTrue(handler.isViewInfoChanged(), "clear 는 변경 플래그를 세팅해야 한다");
    }

    @Test
    @DisplayName("clearCurrentViewInfo — 스캔 정보도 함께 초기화")
    void testClearCurrentAlsoClearsScan() throws Exception {
        handler.setCurrentViewInfo("{\"a\":1}");
        handler.addImageScanInfo(new ImageScanInfo("u", "t"));

        handler.clearCurrentViewInfo();

        assertTrue(handler.getImageScanInfoList().isEmpty());
        assertFalse(handler.isCurrentViewInfo());
    }
}
