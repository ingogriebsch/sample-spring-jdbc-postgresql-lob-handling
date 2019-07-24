package com.github.ingogriebsch.sample.spring.jdbc.postgresql.lob.handling;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = PRIVATE)
public final class ContentDispositionUtils {

    public static String attachment(@NonNull String filename) {
        return contentDisposition("attachement", filename);
    }

    public static String inline(@NonNull String filename) {
        return contentDisposition("inline", filename);
    }

    private static String contentDisposition(String name, String filename) {
        return new StringBuilder("form-data; name=\"").append(name).append('\"').append("; filename=\"").append(filename)
            .append('\"').toString();
    }
}
