package com.github.ingogriebsch.sample.spring.jdbc.postgresql.lob.handling;

import com.github.ingogriebsch.sample.spring.jdbc.postgresql.lob.handling.Document;

import org.junit.Test;

public class DocumentTest {

    @Test(expected = NullPointerException.class)
    public void ctor_should_throw_exception_if_input_is_null() {
        new Document(null, null, null, null);
    }
}
