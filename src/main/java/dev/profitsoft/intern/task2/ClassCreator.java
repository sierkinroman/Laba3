package dev.profitsoft.intern.task2;

import dev.profitsoft.intern.task2.annotation.Property;
import dev.profitsoft.intern.task2.exception.PropertyNotFoundException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Properties;

public class ClassCreator {

    public static <T> T loadFromProperties(Class<T> clazz, Path propertiesPath) {
        Properties properties = loadProperties(propertiesPath);
        T newInstance = createInstance(clazz);

        for (Field field : clazz.getDeclaredFields()) {
            if (!isSkipField(field, properties)) {
                processField(newInstance, field, properties);
            }
        }

        return newInstance;
    }

    private static Properties loadProperties(Path propertiesPath) {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(propertiesPath.toFile())) {
            properties.load(fis);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("file '" + propertiesPath.toFile().getName() + "' not found", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

    private static <T> T createInstance(Class<T> clazz) {
        T newInstance;
        try {
            newInstance = clazz.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Class '" + clazz.getName() + "' has no default constructor", e);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return newInstance;
    }

    private static boolean isSkipField(Field field, Properties properties) {
        return !field.isAnnotationPresent(Property.class) && !properties.containsKey(field.getName());
    }

    private static <T> void processField(T instance, Field field, Properties properties) {
        String fieldName = getFieldName(field);
        checkIsPropertyPresent(properties, fieldName, field);

        Class<?> type = field.getType();
        if (type == String.class) {
            setField(field, instance, properties.getProperty(fieldName));
        } else if (type == int.class || type == Integer.class) {
            setField(field, instance, parseInt(properties, fieldName));
        } else if (type == Instant.class) {
            setField(field, instance, parseInstant(field, properties, fieldName));
        }
    }

    private static String getFieldName(Field field) {
        if (field.isAnnotationPresent(Property.class)) {
            String propertyName = field.getAnnotation(Property.class).name();
            if (!propertyName.isBlank()) {
                return propertyName;
            }
        }

        return field.getName();
    }

    private static void checkIsPropertyPresent(Properties properties, String property, Field field) {
        if (!properties.containsKey(property)) {
            throw new PropertyNotFoundException(
                    String.format("Property '%s' not found in properties file for field '%s'", property, field.getName()));
        }
    }

    private static <T> void setField(Field field, T instance, Object value) {
        field.setAccessible(true);
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static int parseInt(Properties properties, String fieldName) {
        String propertyValue = properties.getProperty(fieldName);
        try {
            return Integer.parseInt(propertyValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    String.format("Property '%s' with value '%s' can't parse to int", fieldName, propertyValue), e);
        }
    }

    private static Instant parseInstant(Field field, Properties properties, String fieldName) {
        String pattern;

        if (field.isAnnotationPresent(Property.class)) {
            pattern = field.getAnnotation(Property.class).format();
        } else {
            Method method;
            try {
                method = Property.class.getDeclaredMethod("format");
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            pattern = (String) method.getDefaultValue();
        }

        DateTimeFormatter formatter;
        try {
            formatter = DateTimeFormatter.ofPattern(pattern);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(String.format("Incorrect pattern '%s' on field '%s'", pattern, fieldName), e);
        }

        String propertyValue = properties.getProperty(fieldName);
        try {
           return LocalDateTime.parse(propertyValue, formatter).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    String.format("Property '%s' with value '%s' can't parse with pattern '%s'", fieldName, propertyValue, pattern), e);
        }
    }

}
