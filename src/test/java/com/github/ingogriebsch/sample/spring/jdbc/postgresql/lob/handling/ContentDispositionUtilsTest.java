package com.github.ingogriebsch.sample.spring.jdbc.postgresql.lob.handling;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ContentDispositionUtilsTest {

    @Test(expected = NullPointerException.class)
    public void attachement_should_throw_exception_if_input_is_null() {
        ContentDispositionUtils.attachment(null);
    }

    @Test
    public void attachement_should_return_string_containing_filename() {
        String filename = "test.txt";
        assertThat(ContentDispositionUtils.attachment(filename)).contains(filename);
    }

    @Test(expected = NullPointerException.class)
    public void inline_should_throw_exception_if_input_is_null() {
        ContentDispositionUtils.inline(null);
    }

    @Test
    public void inline_should_return_string_containing_filename() {
        String filename = "test.txt";
        assertThat(ContentDispositionUtils.inline(filename)).contains(filename);
    }
}
