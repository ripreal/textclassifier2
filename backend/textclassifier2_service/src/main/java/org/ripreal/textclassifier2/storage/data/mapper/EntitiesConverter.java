package org.ripreal.textclassifier2.storage.data.mapper;

import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.ripreal.textclassifier2.model.VocabularyWord;
import org.ripreal.textclassifier2.storage.data.entities.MongoCharacteristic;
import org.ripreal.textclassifier2.storage.data.entities.MongoCharacteristicValue;
import org.ripreal.textclassifier2.storage.data.entities.MongoClassifiableText;
import org.ripreal.textclassifier2.storage.data.entities.MongoVocabularyWord;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EntitiesConverter {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(EntitiesConverter.class);

    static public List<MongoClassifiableText> castToMongoTexts(Iterable<MongoClassifiableText> texts) {
        List<MongoClassifiableText> mongoTexts = new ArrayList<>();
        texts.forEach(item -> mongoTexts.add(((MongoClassifiableText) item)));
        return mongoTexts;
    }

    static public Set<MongoCharacteristic> castToMongoCharacteristic(Iterable<MongoCharacteristic> characteristics) {
        Set<MongoCharacteristic> mongoItems = new HashSet<>();
        characteristics.forEach(item -> mongoItems.add(((MongoCharacteristic) item)));
        return mongoItems;
    }

    static public Set<MongoCharacteristicValue> castToMongoCharValues(Iterable<MongoCharacteristicValue> charVals) {
        Set<MongoCharacteristicValue> mongoItems = new HashSet<>();
        charVals.forEach(item -> mongoItems.add(((MongoCharacteristicValue) item)));
        return mongoItems;
    }

    static public Set<MongoVocabularyWord> castToMongoVocabulary(Iterable<VocabularyWord> vocab) {
        Set<MongoVocabularyWord> mongoItems = new HashSet<>();
        vocab.forEach(item -> mongoItems.add(((MongoVocabularyWord) item)));
        return mongoItems;
    }

    static  <T> void convertTo(JsonNode node, ObjectCodec codec, T data) throws IOException {
        Object convertedValue = null;
        for (Field field : data.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(DeserializableField.class)) {
                if (Set.class.equals(field.getType()))
                    try {
                        convertedValue = node.get(field.getName())
                            .traverse(codec).readValueAs(new TypeReference<Set<MongoCharacteristicValue>>() {});
                    }
                    catch (Exception e) {
                        convertedValue = node.get(field.getName())
                            .traverse(codec).readValueAs(new TypeReference<Set<?>>() {});
                    }
                else
                    convertedValue = node.get(field.getName()).traverse(codec).readValueAs(boxType(field.getType()));
                setValue(field, data, convertedValue);
            }
        }
    }

    static private <T> Class<?> boxType(Class<T> type) {
        if (type == boolean.class)
            return Boolean.class;
        else if (type == byte.class)
            return Boolean.class;
        else if (type == char.class)
            return Character.class;
        else if (type == float.class)
            return Float.class;
        else if (type == int.class)
            return Integer.class;
        else if (type == long.class)
            return Long.class;
        else if (type == short.class)
            return Short.class;
        else if (type == double.class)
            return double.class;
        else
            return type;
    }

    static private <T> void setValue(Field field, T object, Object value){
        for(Method method : object.getClass().getMethods()) {
            if (isSetter(method, field)) {
                try {
                    method.invoke(object, value);
                    return;
                } catch (IllegalAccessException | InvocationTargetException  e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    static private boolean isSetter(Method method, Field field) {

        boolean nameConforms = method.getName().toUpperCase().startsWith("SET")
                            && method.getName().toUpperCase().endsWith(field.getName().toUpperCase());

        if (!nameConforms) return false;
        if (method.getParameterTypes().length != 1) return false;
        return true;
    }
}
