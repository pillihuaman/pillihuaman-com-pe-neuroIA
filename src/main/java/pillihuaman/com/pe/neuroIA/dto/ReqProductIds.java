package pillihuaman.com.pe.neuroIA.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReqProductIds {
    private List<String> productIds;
    private Boolean status; // <-- Nuevo campo
}