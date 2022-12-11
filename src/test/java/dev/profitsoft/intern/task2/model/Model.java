package dev.profitsoft.intern.task2.model;

import dev.profitsoft.intern.task2.annotation.Property;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
public class Model {

    private String stringProperty;

    @Property(name = "numberProperty")
    private int myNumber;

    private Integer integerNumber;

    @Property(format = "dd.MM.yyyy HH:mm")
    private Instant timeProperty;

    @Property(name = "prefix.anotherStr")
    private String anotherName;

    @Property(name = "timeAnother")
    private Instant time;

}
