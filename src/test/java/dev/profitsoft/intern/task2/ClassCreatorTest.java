package dev.profitsoft.intern.task2;

import dev.profitsoft.intern.task2.exception.PropertyNotFoundException;
import dev.profitsoft.intern.task2.model.Model;
import dev.profitsoft.intern.task2.model.Model2;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ClassCreatorTest {

    @Test
    public void loadFromProperties() {
        Model model = ClassCreator.loadFromProperties(Model.class, getPath());

        assertThat(model.getStringProperty())
                .isEqualTo("value1");
        assertThat(model.getMyNumber())
                .isEqualTo(10);
        assertThat(model.getTimeProperty())
                .isEqualTo(createInstant("29.11.2022 18:30", "dd.MM.yyyy HH:mm"));
    }

    private Path getPath() {
        try {
            return Path.of(getClass().getClassLoader().getResource("attributes.properties").toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private Instant createInstant(String value, String pattern) {
        return LocalDateTime.parse(value, DateTimeFormatter.ofPattern(pattern))
                .toInstant(ZoneOffset.UTC);
    }

    @Test
    public void loadFromProperties_null() {
        assertThatThrownBy(() -> ClassCreator.loadFromProperties(null, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void loadFromProperties_nullClass() {
        assertThatThrownBy(() -> ClassCreator.loadFromProperties(null, getPath()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void loadFromProperties_nullProperties() {
        assertThatThrownBy(() -> ClassCreator.loadFromProperties(Model.class, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void loadFromProperties_parseString() {
        Model model = ClassCreator.loadFromProperties(Model.class, getPath());

        assertThat(model.getStringProperty())
                .isEqualTo("value1");
    }

    @Test
    public void loadFromProperties_parseInt() {
        Model model = ClassCreator.loadFromProperties(Model.class, getPath());

        assertThat(model.getMyNumber())
                .isEqualTo(10);
    }

    @Test
    public void loadFromProperties_parseInteger() {
        Model model = ClassCreator.loadFromProperties(Model.class, getPath());

        assertThat(model.getIntegerNumber())
                .isEqualTo(1024);
    }

    @Test
    public void loadFromProperties_parseInstant() {
        Model model = ClassCreator.loadFromProperties(Model.class, getPath());

        assertThat(model.getTimeProperty())
                .isEqualTo(createInstant("29.11.2022 18:30", "dd.MM.yyyy HH:mm"));
    }

    @Test
    public void loadFromProperties_AnotherPropertyName() {
        Model model = ClassCreator.loadFromProperties(Model.class, getPath());

        assertThat(model.getAnotherName())
                .isEqualTo("anotherValue");
    }

    @Test
    public void loadFromProperties_DefaultFormatInstant() {
        Model model = ClassCreator.loadFromProperties(Model.class, getPath());

        assertThat(model.getTime())
                .isEqualTo(createInstant("10.08.2022 12:20:13", "dd.MM.yyyy HH:mm:ss"));
    }

    @Test
    public void loadFromProperties_PropertyNotFound() {
        assertThatThrownBy(() -> ClassCreator.loadFromProperties(Model2.class, getPath()))
                .isInstanceOf(PropertyNotFoundException.class)
                .hasMessage("Property 'name' not found in properties file for field 'strProperty'");
    }

    @Test
    public void loadFromProperties_SkipField() {
        Model model = ClassCreator.loadFromProperties(Model.class, getPath());

        assertThat(model.getSkipString())
                .isNull();
    }

}