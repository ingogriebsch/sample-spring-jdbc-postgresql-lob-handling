package com.github.ingogriebsch.sample.spring.jdbc.postgresql.lob.handling;

import static java.lang.String.format;
import static java.util.Arrays.asList;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_LENGTH;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.ALL;
import static org.springframework.http.MediaType.parseMediaType;
import static org.springframework.util.StreamUtils.copy;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.transaction.annotation.Transactional;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DocumentResourceHttpMessageConverter implements HttpMessageConverter<DocumentResource> {

    @NonNull
    private final DocumentRepository documentRepository;

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return DocumentResource.class.isAssignableFrom(clazz);
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return asList(new MediaType[] { ALL });
    }

    @Override
    public DocumentResource read(Class<? extends DocumentResource> clazz, HttpInputMessage inputMessage)
        throws IOException, HttpMessageNotReadableException {
        throw new UnsupportedOperationException("Operation read is not supported through this converter!");
    }

    @Override
    @Transactional
    public void write(@NonNull DocumentResource documentResource, MediaType contentType, @NonNull HttpOutputMessage outputMessage)
        throws IOException, HttpMessageNotWritableException {
        Document document = obtainDocument(documentResource, documentRepository);
        setHeaders(document, outputMessage);
        writeContent(document, outputMessage);
    }

    static Document obtainDocument(DocumentResource documentResource, DocumentRepository documentRepository) {
        Document document = documentRepository.findOne(documentResource.getId());
        if (document == null) {
            throw new HttpMessageNotWritableException(format("Document resource '%s' is not available!", documentResource));
        }
        return document;
    }

    static void writeContent(Document document, HttpOutputMessage outputMessage) throws IOException {
        Blob content = document.getContent();
        try {
            try (InputStream inputStream = new BufferedInputStream(content.getBinaryStream())) {
                copy(inputStream, outputMessage.getBody());
            }
        } catch (SQLException e) {
            throw new IOException(e);
        } finally {
            try {
                content.free();
            } catch (SQLException e) {
                throw new IOException(e);
            }
        }
    }

    static void setHeaders(Document document, HttpOutputMessage outputMessage) {
        HttpHeaders headers = outputMessage.getHeaders();
        headers.set(CONTENT_LENGTH, "" + document.getContentLength());
        headers.set(CONTENT_TYPE, parseMediaType(document.getContentType()).toString());
        headers.set(CONTENT_DISPOSITION, ContentDispositionUtils.attachment(document.getFilename()));
    }

}
