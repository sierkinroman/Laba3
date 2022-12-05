package dev.profitsoft.intern.task1.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Fine {

    @JsonProperty("date_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateTime;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("type")
    @JacksonXmlProperty(localName = "type", isAttribute = true)
    private FineType type;

    @JsonProperty("fine_amount")
    @JacksonXmlProperty(localName = "fine_amount", isAttribute = true)
    private BigDecimal fineAmount;

    public Fine(FineType type, BigDecimal fineAmount) {
        this.dateTime = null;
        this.firstName = null;
        this.lastName = null;
        this.type = type;
        this.fineAmount = fineAmount;
    }

}
