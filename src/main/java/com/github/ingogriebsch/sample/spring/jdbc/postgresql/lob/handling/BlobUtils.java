package com.github.ingogriebsch.sample.spring.jdbc.postgresql.lob.handling;

import static lombok.AccessLevel.PRIVATE;

import java.sql.Blob;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

// FIXME Only necessary if we use a Blob for the content.
@NoArgsConstructor(access = PRIVATE)
public final class BlobUtils {

    @SneakyThrows
    public static void free(Blob blob) {
        if (blob == null) {
            return;
        }
        blob.free();
    }

    public static void freeQuietly(Blob blob) {
        try {
            free(blob);
        } catch (Exception e) {
            // nothing to do
        }
    }
}
