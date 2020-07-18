package com.github.ingogriebsch.sample.spring.jdbc.postgresql.lob.handling;

import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebMvcConfiguration {

    @Bean
    public DocumentResourceHttpMessageConverter
        documentResourceHttpMessageConverter(@NonNull DocumentRepository documentRepository) {
        return new DocumentResourceHttpMessageConverter(documentRepository);
    }

}
