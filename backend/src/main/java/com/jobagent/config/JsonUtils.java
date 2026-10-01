package com.jobagent.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String toJson(Object obj) {
        if (obj == null) return "[]";
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            return "[]";
        }
    }

    public static List<String> toStringList(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return MAPPER.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
