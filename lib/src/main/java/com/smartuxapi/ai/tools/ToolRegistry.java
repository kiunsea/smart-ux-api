package com.smartuxapi.ai.tools;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tool 정의 등록소. 이름 기준 유일성 유지, 등록 순서 보존.
 *
 * @since 0.8.0
 */
public final class ToolRegistry {

    private final Map<String, ToolDefinition> byName = new LinkedHashMap<>();

    /**
     * 등록. 동일 이름이 있으면 교체된다.
     */
    public ToolRegistry register(ToolDefinition def) {
        if (def == null) throw new IllegalArgumentException("def is required");
        byName.put(def.getName(), def);
        return this;
    }

    public void unregister(String name) {
        if (name != null) byName.remove(name);
    }

    public ToolDefinition get(String name) {
        if (name == null) return null;
        return byName.get(name);
    }

    /**
     * 등록 순서를 유지한 불변 뷰.
     */
    public Collection<ToolDefinition> all() {
        return Collections.unmodifiableCollection(byName.values());
    }

    public int size() { return byName.size(); }
    public boolean isEmpty() { return byName.isEmpty(); }
    public boolean contains(String name) { return byName.containsKey(name); }

    /**
     * 모든 tool 제거 (테스트/리셋 용).
     */
    public void clear() { byName.clear(); }
}
