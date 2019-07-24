package com.github.ingogriebsch.sample.spring.jdbc.postgresql.lob.handling;

import static java.util.UUID.randomUUID;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class DocumentController {

    static final String PATH_FIND_ALL = "/api/documents";
    static final String PATH_FIND_ONE = "/api/documents/{id}";
    static final String PATH_UPLOAD = "/api/documents";
    static final String PATH_DOWNLOAD = "/api/documents/{id}/content";
    static final String PATH_DELETE = "/api/documents/{id}";
    static final String REQUEST_PART_SOURCE = "source";

    @NonNull
    private final DocumentRepository documentRepository;

    @GetMapping(path = PATH_FIND_ALL, produces = APPLICATION_JSON_UTF8_VALUE)
    public List<Document> findAll() {
        return documentRepository.findAll();
    }

    @GetMapping(path = PATH_FIND_ONE, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Document> findOne(@PathVariable String id) {
        Document document = documentRepository.findOne(id);
        return document != null ? ok(document) : notFound().build();
    }

    @PostMapping(path = PATH_UPLOAD, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Document> upload(@RequestPart(name = REQUEST_PART_SOURCE) MultipartFile source) throws Exception {
        Document template =
            new Document(randomUUID().toString(), source.getOriginalFilename(), source.getContentType(), source.getSize());

        Document document;
        try (InputStream content = new BufferedInputStream(source.getInputStream())) {
            document = documentRepository.save(template, content);
        }
        return status(CREATED).body(document);
    }

    @GetMapping(path = PATH_DOWNLOAD, produces = ALL_VALUE)
    public ResponseEntity<DocumentResource> download(@PathVariable String id) throws Exception {
        if (!documentRepository.exists(id)) {
            return notFound().build();
        }
        return ResponseEntity.ok(new DocumentResource(id));
    }

    @DeleteMapping(path = PATH_DELETE)
    public ResponseEntity<?> delete(@PathVariable String id) {
        return documentRepository.deleteIfExists(id) ? ok().build() : notFound().build();
    }
}
