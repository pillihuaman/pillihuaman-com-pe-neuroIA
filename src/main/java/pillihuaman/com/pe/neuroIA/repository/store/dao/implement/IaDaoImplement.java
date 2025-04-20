package pillihuaman.com.pe.neuroIA.repository.store.dao.implement;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import pillihuaman.com.pe.neuroIA.Help.Constante;
import pillihuaman.com.pe.neuroIA.RequestResponse.dto.ReqStore;
import pillihuaman.com.pe.neuroIA.repository.AzureAbstractMongoRepositoryImpl;
import pillihuaman.com.pe.neuroIA.repository.store.Ia;
import pillihuaman.com.pe.neuroIA.repository.store.dao.IaDAO;

import java.util.List;
@Component
@Repository
public class IaDaoImplement extends AzureAbstractMongoRepositoryImpl<Ia> implements IaDAO {
    IaDaoImplement() {
        DS_WRITE = Constante.DW;
        COLLECTION = Constante.COLLECTION_STORE;
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
