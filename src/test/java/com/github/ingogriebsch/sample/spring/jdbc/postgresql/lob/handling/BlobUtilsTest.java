package com.github.ingogriebsch.sample.spring.jdbc.postgresql.lob.handling;

import java.sql.Blob;
import java.sql.SQLException;

import com.github.ingogriebsch.sample.spring.jdbc.postgresql.lob.handling.BlobUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BlobUtilsTest {

    @Test
    public void free_should_succeed_if_blob_is_null() {
        BlobUtils.free(null);
    }

    @Test
    public void free_should_call_free_on_blob_if_blob_is_given() throws Exception {
        Blob blob = Mockito.mock(Blob.class);
        BlobUtils.free(blob);
        Mockito.verify(blob).free();
    }

    @Test(expected = Exception.class)
    public void free_should_throw_any_exception_if_calling_free_results_in_a_thrown_exception() throws Exception {
        Blob blob = Mockito.mock(Blob.class);
        Mockito.doThrow(new SQLException()).when(blob).free();
        BlobUtils.free(blob);
    }

    @Test
    public void freeQuietly_should_succeed_if_blob_is_null() {
        BlobUtils.freeQuietly(null);
    }

    @Test
    public void freeQuietly_should_call_free_on_blob_if_blob_is_given() throws Exception {
        Blob blob = Mockito.mock(Blob.class);
        BlobUtils.freeQuietly(blob);
        Mockito.verify(blob).free();
    }

    @Test
    public void freeQuietly_should_succeed_if_calling_free_results_in_a_thrown_exception() throws Exception {
        Blob blob = Mockito.mock(Blob.class);
        Mockito.doThrow(new SQLException()).when(blob).free();
        BlobUtils.freeQuietly(blob);
    }
}
