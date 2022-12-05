package dev.profitsoft.intern.task1.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@JacksonXmlRootElement(localName = "fines_statistic")
@Data
@AllArgsConstructor
public class FinesStatistic {

    @JacksonXmlElementWrapper(localName = "fines")
    @JacksonXmlProperty(localName = "fine")
    private List<Fine> finesStatistic;
}
