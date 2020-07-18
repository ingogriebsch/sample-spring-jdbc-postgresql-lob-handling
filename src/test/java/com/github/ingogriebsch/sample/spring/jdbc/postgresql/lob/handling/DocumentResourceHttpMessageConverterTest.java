package com.github.ingogriebsch.sample.spring.jdbc.postgresql.lob.handling;

import static java.util.UUID.randomUUID;

import static com.github.ingogriebsch.sample.spring.jdbc.postgresql.lob.handling.ContentDispositionUtils.attachment;
import static org.apache.commons.lang3.RandomUtils.nextBytes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.MediaType.ALL;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.http.MediaType.parseMediaType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotWritableException;

@RunWith(MockitoJUnitRunner.class)
public class DocumentResourceHttpMessageConverterTest {

    @Test
    public void canRead_should_return_false() {
        DocumentRepository documentRepository = mock(DocumentRepository.class);
        DocumentResourceHttpMessageConverter converter = new DocumentResourceHttpMessageConverter(documentRepository);
        assertThat(converter.canRead(DocumentResource.class, APPLICATION_OCTET_STREAM)).isFalse();
    }

    @Test
    public void canWrite_should_return_true_on_matching_input() {
        DocumentRepository documentRepository = mock(DocumentRepository.class);
        DocumentResourceHttpMessageConverter converter = new DocumentResourceHttpMessageConverter(documentRepository);
        assertThat(converter.canWrite(DocumentResource.class, APPLICATION_OCTET_STREAM)).isTrue();
    }

    @Test
    public void canWrite_should_return_false_on_unrelated_input() {
        DocumentRepository documentRepository = mock(DocumentRepository.class);
        DocumentResourceHttpMessageConverter converter = new DocumentResourceHttpMessageConverter(documentRepository);
        assertThat(converter.canWrite(String.class, TEXT_PLAIN)).isFalse();
    }

    @Test
    public void getSupportedMediaTypes_should_return_matching_media_type() {
        DocumentRepository documentRepository = mock(DocumentRepository.class);
        DocumentResourceHttpMessageConverter converter = new DocumentResourceHttpMessageConverter(documentRepository);
        assertThat(converter.getSupportedMediaTypes()).containsExactly(ALL);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void read_should_throw_unsupported_operation_exception() throws Exception {
        DocumentRepository documentRepository = mock(DocumentRepository.class);
        DocumentResourceHttpMessageConverter converter = new DocumentResourceHttpMessageConverter(documentRepository);
        converter.read(DocumentResource.class, mock(HttpInputMessage.class));
    }

    @Test(expected = NullPointerException.class)
    public void write_should_throw_exception_if_input_is_null() throws Exception {
        DocumentRepository documentRepository = mock(DocumentRepository.class);
        DocumentResourceHttpMessageConverter converter = new DocumentResourceHttpMessageConverter(documentRepository);
        converter.write(null, null, null);
    }

    @Test(expected = HttpMessageNotWritableException.class)
    public void obtainDocument_should_throw_exception_if_document_resource_is_not_available() throws Exception {
        String id = UUID.randomUUID().toString();

        DocumentRepository documentRepository = mock(DocumentRepository.class);
        given(documentRepository.findOne(id)).willReturn(null);

        DocumentResourceHttpMessageConverter.obtainDocument(new DocumentResource(id), documentRepository);
    }

    @Test
    public void obtainDocument_should_return_matching_document() throws Exception {
        String id = randomUUID().toString();
        Document document = new Document(id, "test.txt", TEXT_PLAIN_VALUE, 100L);

        DocumentRepository documentRepository = mock(DocumentRepository.class);
        given(documentRepository.findOne(id)).willReturn(document);

        assertThat(DocumentResourceHttpMessageConverter.obtainDocument(new DocumentResource(id), documentRepository))
            .isEqualTo(document);
    }

    @Test
    public void setHeaders_should_set_headers_matching_the_document_properties() throws Exception {
        Document document = new Document(randomUUID().toString(), "test.txt", TEXT_PLAIN_VALUE, 100L);

        HttpHeaders httpHeaders = new HttpHeaders();
        HttpOutputMessage httpOutputMessage = mock(HttpOutputMessage.class);
        given(httpOutputMessage.getHeaders()).willReturn(httpHeaders);

        DocumentResourceHttpMessageConverter.setHeaders(document, httpOutputMessage);
        assertThat(httpHeaders.getContentLength()).isEqualTo(document.getContentLength());
        assertThat(httpHeaders.getContentType()).isEqualTo(parseMediaType(document.getContentType()));
        assertThat(httpHeaders.get(CONTENT_DISPOSITION)).containsExactly(attachment(document.getFilename()));
    }

    @Test(expected = IOException.class)
    public void writeContent_should_throw_exception_if_content_is_not_accessible() throws Exception {
        Blob blob = mock(Blob.class);
        given(blob.getBinaryStream()).willThrow(new SQLException());

        Document document = new Document(randomUUID().toString(), "test.txt", TEXT_PLAIN_VALUE, 100L, blob);
        HttpOutputMessage httpOutputMessage = mock(HttpOutputMessage.class);

        DocumentResourceHttpMessageConverter.writeContent(document, httpOutputMessage);
    }

    @Test(expected = IOException.class)
    public void writeContent_should_throw_exception_if_content_is_not_freeable() throws Exception {
        Blob blob = mock(Blob.class);
        doReturn(new ByteArrayInputStream(new byte[0])).when(blob).getBinaryStream();
        doThrow(new SQLException()).when(blob).free();

        Document document = new Document(randomUUID().toString(), "test.txt", TEXT_PLAIN_VALUE, 100L, blob);
        HttpOutputMessage httpOutputMessage = mock(HttpOutputMessage.class);

        DocumentResourceHttpMessageConverter.writeContent(document, httpOutputMessage);
    }

    @Test
    public void writeContent_should_free_content() throws Exception {
        Blob blob = mock(Blob.class);
        given(blob.getBinaryStream()).willReturn(new ByteArrayInputStream(new byte[0]));

        Document document = new Document(randomUUID().toString(), "test.txt", TEXT_PLAIN_VALUE, 100L, blob);

        HttpOutputMessage httpOutputMessage = mock(HttpOutputMessage.class);
        given(httpOutputMessage.getBody()).willReturn(new ByteArrayOutputStream());

        DocumentResourceHttpMessageConverter.writeContent(document, httpOutputMessage);
        verify(blob).free();
    }

    @Test
    public void write_should_transfer_document_to_output_message() throws Exception {
        byte[] content = nextBytes(100);
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(content)) {
            Blob blob = mock(Blob.class);
            given(blob.getBinaryStream()).willReturn(inputStream);

            String id = UUID.randomUUID().toString();
            Document document = new Document(id, "test.txt", TEXT_PLAIN_VALUE, 100L, blob);

            DocumentRepository documentRepository = mock(DocumentRepository.class);
            given(documentRepository.findOne(id)).willReturn(document);

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                HttpHeaders httpHeaders = new HttpHeaders();
                HttpOutputMessage httpOutputMessage = mock(HttpOutputMessage.class);
                given(httpOutputMessage.getHeaders()).willReturn(httpHeaders);
                given(httpOutputMessage.getBody()).willReturn(outputStream);

                DocumentResourceHttpMessageConverter converter = new DocumentResourceHttpMessageConverter(documentRepository);
                converter.write(new DocumentResource(id), null, httpOutputMessage);

                assertThat(httpOutputMessage.getBody()).isInstanceOf(ByteArrayOutputStream.class);
                assertThat(((ByteArrayOutputStream) httpOutputMessage.getBody()).toByteArray()).isEqualTo(content);

                assertThat(httpHeaders.getContentLength()).isEqualTo(document.getContentLength());
                assertThat(httpHeaders.getContentType()).isEqualTo(parseMediaType(document.getContentType()));
                assertThat(httpHeaders.get(CONTENT_DISPOSITION)).containsExactly(attachment(document.getFilename()));
            }
        }
    }
}
