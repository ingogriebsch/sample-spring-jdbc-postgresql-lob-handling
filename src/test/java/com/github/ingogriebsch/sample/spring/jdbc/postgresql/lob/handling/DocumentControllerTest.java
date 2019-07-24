package com.github.ingogriebsch.sample.spring.jdbc.postgresql.lob.handling;

import static java.util.UUID.randomUUID;

import static com.github.ingogriebsch.sample.spring.jdbc.postgresql.lob.handling.ContentDispositionUtils.attachment;
import static com.github.ingogriebsch.sample.spring.jdbc.postgresql.lob.handling.DocumentController.PATH_DELETE;
import static com.github.ingogriebsch.sample.spring.jdbc.postgresql.lob.handling.DocumentController.PATH_DOWNLOAD;
import static com.github.ingogriebsch.sample.spring.jdbc.postgresql.lob.handling.DocumentController.PATH_FIND_ALL;
import static com.github.ingogriebsch.sample.spring.jdbc.postgresql.lob.handling.DocumentController.PATH_FIND_ONE;
import static com.github.ingogriebsch.sample.spring.jdbc.postgresql.lob.handling.DocumentController.PATH_UPLOAD;
import static com.github.ingogriebsch.sample.spring.jdbc.postgresql.lob.handling.DocumentController.REQUEST_PART_SOURCE;
import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.core.io.ResourceLoader.CLASSPATH_URL_PREFIX;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_LENGTH;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Blob;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@Import(WebMvcConfiguration.class)
@RunWith(SpringRunner.class)
@WebMvcTest(DocumentController.class)
public class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResourceLoader resourceLoader;

    @MockBean
    private DocumentRepository documentRepository;

    @Test
    public void findAll_should_return_matching_list() throws Exception {
        given(documentRepository.findAll()).willReturn(newArrayList());

        ResultActions actions = mockMvc.perform(get(PATH_FIND_ALL).accept(APPLICATION_JSON_UTF8));
        actions.andExpect(status().isOk());

        verify(documentRepository).findAll();
        verifyNoMoreInteractions(documentRepository);
    }

    @Test
    public void findOne_should_return_ok_if_document_is_known() throws Exception {
        String id = randomUUID().toString();
        given(documentRepository.findOne(id)).willReturn(new Document(id, "test.txt", TEXT_PLAIN_VALUE, 100L));

        ResultActions actions = mockMvc.perform(get(PATH_FIND_ONE, id).accept(APPLICATION_JSON_UTF8));
        actions.andExpect(status().isOk());

        verify(documentRepository).findOne(id);
        verifyNoMoreInteractions(documentRepository);
    }

    @Test
    public void findOne_should_return_not_found_if_document_is_not_known() throws Exception {
        String id = randomUUID().toString();
        given(documentRepository.findOne(id)).willReturn(null);

        ResultActions actions = mockMvc.perform(get(PATH_FIND_ONE, id).accept(APPLICATION_JSON_UTF8));
        actions.andExpect(status().isNotFound());

        verify(documentRepository).findOne(id);
        verifyNoMoreInteractions(documentRepository);
    }

    @Test
    public void upload_should_save_document_if_source_is_given() throws Exception {
        Resource resource = resourceLoader.getResource(CLASSPATH_URL_PREFIX + "/documents/simple-txt-file.txt");
        MockMultipartFile source =
            new MockMultipartFile(REQUEST_PART_SOURCE, resource.getFilename(), TEXT_PLAIN_VALUE, resource.getInputStream());
        Document document =
            new Document(UUID.randomUUID().toString(), source.getOriginalFilename(), source.getContentType(), source.getSize());

        given(documentRepository.save(any(), any())).willReturn(document);

        ResultActions actions = mockMvc.perform(fileUpload(PATH_UPLOAD).file(source).accept(APPLICATION_JSON_UTF8));
        actions.andExpect(status().isCreated());

        actions.andExpect(jsonPath("$.id").value(document.getId()));
        actions.andExpect(jsonPath("$.filename").value(document.getFilename()));
        actions.andExpect(jsonPath("$.contentType").value(document.getContentType()));
        actions.andExpect(jsonPath("$.contentLength").value(document.getContentLength()));
        actions.andExpect(jsonPath("$.content").doesNotExist());

        verify(documentRepository).save(any(), any());
        verifyNoMoreInteractions(documentRepository);
    }

    @Test
    public void upload_should_save_document_if_source_is_not_given() throws Exception {
        ResultActions actions = mockMvc.perform(fileUpload(PATH_UPLOAD).accept(APPLICATION_JSON_UTF8));
        actions.andExpect(status().isBadRequest());
        verifyNoMoreInteractions(documentRepository);
    }

    @Test
    public void download_should_return_not_found_if_document_is_not_known() throws Exception {
        String id = randomUUID().toString();
        given(documentRepository.exists(id)).willReturn(false);

        ResultActions actions = mockMvc.perform(get(PATH_DOWNLOAD, id));
        actions.andExpect(status().isNotFound());

        verify(documentRepository).exists(id);
        verifyNoMoreInteractions(documentRepository);
    }

    @Test
    public void download_should_return_content_if_document_is_known() throws Exception {
        String documentId = randomUUID().toString();
        Resource resource = resourceLoader.getResource(CLASSPATH_URL_PREFIX + "/documents/simple-txt-file.txt");

        Blob blob = mock(Blob.class);
        given(blob.getBinaryStream()).willReturn(resource.getInputStream());

        Document document = new Document(documentId, resource.getFilename(), TEXT_PLAIN_VALUE, resource.contentLength(), blob);

        given(documentRepository.exists(documentId)).willReturn(true);
        given(documentRepository.findOne(documentId)).willReturn(document);

        ResultActions actions = mockMvc.perform(get(PATH_DOWNLOAD, documentId));
        actions.andExpect(status().isOk());

        actions.andExpect(header().longValue(CONTENT_LENGTH, document.getContentLength()));
        actions.andExpect(header().string(CONTENT_TYPE, document.getContentType()));
        actions.andExpect(header().string(CONTENT_DISPOSITION, attachment(document.getFilename())));

        verify(documentRepository).exists(documentId);
        verify(documentRepository).findOne(documentId);
        verifyNoMoreInteractions(documentRepository);
    }

    @Test
    public void delete_should_return_not_found_if_document_is_not_known() throws Exception {
        String id = randomUUID().toString();
        given(documentRepository.deleteIfExists(id)).willReturn(false);

        ResultActions actions = mockMvc.perform(delete(PATH_DELETE, id));
        actions.andExpect(status().isNotFound());

        verify(documentRepository).deleteIfExists(id);
        verifyNoMoreInteractions(documentRepository);
    }

    @Test
    public void delete_should_return_ok_if_document_is_known() throws Exception {
        String id = randomUUID().toString();
        given(documentRepository.deleteIfExists(id)).willReturn(true);

        ResultActions actions = mockMvc.perform(delete(PATH_DELETE, id));
        actions.andExpect(status().isOk());

        verify(documentRepository).deleteIfExists(id);
        verifyNoMoreInteractions(documentRepository);
    }
}
