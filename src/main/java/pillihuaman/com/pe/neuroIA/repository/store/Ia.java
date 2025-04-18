package pillihuaman.com.pe.neuroIA.repository.store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import pillihuaman.com.pe.lib.common.AuditEntity;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Document(collection = "store")
public class Ia {
    @Id
    private ObjectId id; // ID único de la tienda, MongoDB lo genera automáticamente
    private String storeId; // Identificador único de la tienda (ST-001)
    private String name; // Nombre de la tienda
    private String address; // Dirección de la tienda
    private String country; // País de la tienda
    private String email; // Correo electrónico de contacto
    private String phone; // Teléfono de contacto
    private String status; // Estado de la tienda (activo, inactivo, etc.)
    private String ownerName; // Nombre del dueño de la tienda
    private AuditEntity audit;
}
