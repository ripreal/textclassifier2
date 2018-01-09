package org.ripreal.textclassifier2.data.reactive;

import org.ripreal.textclassifier2.model.ClassifiableText;
import org.ripreal.textclassifier2.model.VocabularyWord;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface VocabularyWordRepo extends ReactiveMongoRepository<VocabularyWord, String> {

}
