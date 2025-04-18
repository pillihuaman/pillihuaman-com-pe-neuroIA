package pillihuaman.com.pe.neuroIA.RequestResponse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqStore {
    private String id;  // Unique store identifier (e.g., ST-001)
    private String name;     // Store name
    private String address;  // Store address
    private String country;  // Country where the store is located
    private String email;    // Contact email
    private String phone;    // Contact phone number
    private String status;   // Store status (active, inactive)
    private String ownerName; // Store owner's name
    private Integer page;
    private Integer pagesize;
}
