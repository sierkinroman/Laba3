package dev.profitsoft.intern.task2.model;

import dev.profitsoft.intern.task2.annotation.Property;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Model2 {

    @Property(name = "name")
    private String strProperty;

}
