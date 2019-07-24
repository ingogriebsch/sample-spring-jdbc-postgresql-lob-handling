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

import static com.google.common.collect.Lists.newArrayList;
import static org.springframework.transaction.annotation.Propagation.MANDATORY;

import java.io.InputStream;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class DocumentRepository {

    @Transactional(readOnly = true)
    public List<Document> findAll() {
        // FIXME implement me... :)
        return newArrayList();
    }

    @Transactional(readOnly = true)
    public Document findOne(String id) {
        // FIXME implement me... :)
        return null;
    }

    @Transactional(readOnly = true)
    public boolean exists(String id) {
        // FIXME implement me... :)
        return false;
    }

    @Transactional
    public Document save(Document template, InputStream source) throws Exception {
        // FIXME implement me... :)
        return null;
    }

    @Transactional
    public boolean deleteIfExists(String id) {
        // FIXME implement me... :)
        return false;
    }

    @Transactional(propagation = MANDATORY)
    public void deleteContent(String id) {
        // FIXME implement me... :)
    }

}
