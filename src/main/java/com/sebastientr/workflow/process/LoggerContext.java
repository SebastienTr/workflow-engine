package com.sebastientr.workflow.process;

import java.util.HashMap;
import java.util.Map;

public final class LoggerContext {
    private LoggerContext() {
        throw new IllegalStateException("Utility class");
    }

    private static final ThreadLocal<HashMap<String, Object>> THREAD_LOCAL = new ThreadLocal<>();

    public static Object get(String key) {
        return THREAD_LOCAL.get() == null ? null : THREAD_LOCAL.get().get(key);
    }

    public static void set(String key, Object value) {
        if (THREAD_LOCAL.get() == null) {
            THREAD_LOCAL.set(new HashMap<>());
        }

        THREAD_LOCAL.get().put(key, value);
    }

    public static void addAll(Map<String, Object> map) {
        if (THREAD_LOCAL.get() == null) {
            THREAD_LOCAL.set(new HashMap<>());
        }

        (THREAD_LOCAL.get()).putAll(map);
    }

    public static Map<String, Object> getAll() {
        return THREAD_LOCAL.get();
    }

    public static void clear() {
        THREAD_LOCAL.get().clear();
    }

    public static void remove(String key) {
        (THREAD_LOCAL.get()).remove(key);
    }

    public static void unload() {
        THREAD_LOCAL.remove();
    }
}