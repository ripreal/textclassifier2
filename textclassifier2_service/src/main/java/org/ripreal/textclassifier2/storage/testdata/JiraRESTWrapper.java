package org.ripreal.textclassifier2.storage.testdata;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.ripreal.textclassifier2.JiraBasicAuthClient;
import org.ripreal.textclassifier2.JiraIssueReader;
import org.ripreal.textclassifier2.PropertiesClient;
import org.ripreal.textclassifier2.model.ClassifiableFactory;
import org.ripreal.textclassifier2.model.ClassifiableText;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
@Slf4j
public class JiraRESTWrapper {

    @NonNull
    private final ClassifiableFactory textFactory;

    public JiraRESTWrapper(ClassifiableFactory factory) {
        textFactory = factory;
    }

    @Bean
    public List<ClassifiableText> listOfClassifiableTexts() {
        JiraBasicAuthClient client = null;
        try {
            client = new JiraBasicAuthClient(
                textFactory, new PropertiesClient());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (JiraIssueReader reader = client.reader(100)) {
            while (reader.next()) {
                log.info(reader.getResult().toString());
            }
        }
        catch(Exception e) {
            log.error("parse error", e);
        }
        return null;
    }

}
