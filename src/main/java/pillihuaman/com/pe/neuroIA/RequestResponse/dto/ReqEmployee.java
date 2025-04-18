package pillihuaman.com.pe.neuroIA.RequestResponse.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class ReqEmployee {

    private Integer page;
    private Integer pagesize;
    private String id;
    private String name;
    private String lastName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy", timezone = "GMT-5")
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date startDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy", timezone = "GMT-5")
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date finishDate;
    private Integer totalHours;
    private String department;
    private Double salaryMonth;
    private Double salaryHours;
    private String typeDocument;
    private String document;

}
