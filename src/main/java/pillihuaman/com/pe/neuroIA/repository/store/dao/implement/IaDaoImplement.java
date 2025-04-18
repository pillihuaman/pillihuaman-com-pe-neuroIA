package pillihuaman.com.pe.neuroIA.repository.store.dao.implement;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import pillihuaman.com.pe.lib.common.AuditEntity;
import pillihuaman.com.pe.lib.common.MyJsonWebToken;
import pillihuaman.com.pe.neuroIA.Help.Constantes;
import pillihuaman.com.pe.neuroIA.RequestResponse.RespStore;
import pillihuaman.com.pe.neuroIA.RequestResponse.dto.ReqStore;
import pillihuaman.com.pe.neuroIA.repository.AzureAbstractMongoRepositoryImpl;
import pillihuaman.com.pe.neuroIA.repository.store.Ia;
import pillihuaman.com.pe.neuroIA.repository.store.dao.IaDAO;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
@Component
@Repository
public class IaDaoImplement extends AzureAbstractMongoRepositoryImpl<Ia> implements IaDAO {
    IaDaoImplement() {
        DS_WRITE = Constantes.DW;
        COLLECTION = Constantes.COLLECTION_STORE;
    }

    @Override
    public Class<Ia> provideEntityClass() {
        return Ia.class;
    }


    @Override
    public List<Ia> getIAResponse(ReqStore reqStore) {
        return List.of();
    }
}
