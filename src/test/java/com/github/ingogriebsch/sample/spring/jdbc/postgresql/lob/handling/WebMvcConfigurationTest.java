package com.github.ingogriebsch.sample.spring.jdbc.postgresql.lob.handling;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WebMvcConfigurationTest {

    @Test(expected = NullPointerException.class)
    public void documentResourceHttpMessageConverter_should_throw_exception_if_input_is_null() {
        new WebMvcConfiguration().documentResourceHttpMessageConverter(null);
    }

    @Test
    public void documentResourceHttpMessageConverter_should_return_instance_if_input_is_given() {
        DocumentRepository documentRepository = Mockito.mock(DocumentRepository.class);
        assertThat(new WebMvcConfiguration().documentResourceHttpMessageConverter(documentRepository)).isNotNull();
    }
}
