package com.github.ingogriebsch.sample.spring.jdbc.postgresql.lob.handling;

import static lombok.AccessLevel.PRIVATE;

import java.sql.Blob;
import java.sql.SQLException;

import lombok.NoArgsConstructor;

// FIXME Only necessary if we use a Blob for the content.
@NoArgsConstructor(access = PRIVATE)
public final class BlobUtils {

    public static void free(Blob blob) {
        if (blob == null) {
            return;
        }

        try {
            blob.free();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void freeQuietly(Blob blob) {
        if (blob == null) {
            return;
        }

        try {
            blob.free();
        } catch (Exception e) {
            // nothing to do
        }
    }
}
