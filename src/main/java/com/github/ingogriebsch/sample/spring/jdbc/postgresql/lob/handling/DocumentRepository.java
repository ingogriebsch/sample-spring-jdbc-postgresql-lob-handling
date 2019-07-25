/*
 * Copyright 2019 Ingo Griebsch
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package com.github.ingogriebsch.sample.spring.jdbc.postgresql.lob.handling;

import static org.springframework.transaction.annotation.Propagation.MANDATORY;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import lombok.NonNull;
import lombok.SneakyThrows;

@Repository
public class DocumentRepository {

    @NonNull
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
    public List<Document> findAll() {
        return jdbcTemplate.query("SELECT * FROM public.document", (resultSet, rowNum) -> toDocument(resultSet));
    }

    @Transactional(readOnly = true)
    public Document findOne(String id) {
        return jdbcTemplate.queryForObject("SELECT * FROM public.document WHERE id=?", new Object[] { id },
            (resultSet, rowNum) -> toDocument(resultSet));
    }

    @Transactional(readOnly = true)
    public boolean exists(String id) {
        int count =
            jdbcTemplate.queryForObject("SELECT count(*) FROM public.document WHERE id=?", new Object[] { id }, Integer.class);
        return count > 0;
    }

    @Transactional
    public Document save(Document template, InputStream source) throws Exception {
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO public.document (id, content, content_length, content_type, filename) VALUES (?,?,?,?,?)");
            statement.setString(1, template.getId());
            statement.setBlob(2, source);
            statement.setLong(3, template.getContentLength());
            statement.setString(4, template.getContentType());
            statement.setString(5, template.getFilename());
            return statement;
        });
        return findOne(template.getId());
    }

    @Transactional
    public boolean deleteIfExists(@NonNull String id) {
        if (!exists(id)) {
            return false;
        }
        deleteContent(id);
        int rows = jdbcTemplate.update("DELETE FROM public.document WHERE id = ?", new Object[] { id });
        return rows == 1;
    }

    @Transactional(propagation = MANDATORY)
    public void deleteContent(String id) {
        jdbcTemplate.queryForObject("SELECT lo_unlink(d.content) FROM public.document d WHERE id=?", new Object[] { id },
            Integer.class);
    }

    @SneakyThrows
    private Document toDocument(ResultSet rs) {
        String id = rs.getString("id");
        String fileName = rs.getString("filename");
        String contentType = rs.getString("content_type");
        long contentLength = rs.getLong("content_length");
        Blob content = rs.getBlob("content");
        return new Document(id, fileName, contentType, contentLength, content);
    }
}
