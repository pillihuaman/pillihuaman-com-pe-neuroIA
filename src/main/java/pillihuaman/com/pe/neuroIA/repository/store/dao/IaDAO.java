package pillihuaman.com.pe.neuroIA.repository.store.dao;

import pillihuaman.com.pe.lib.common.MyJsonWebToken;
import pillihuaman.com.pe.neuroIA.RequestResponse.dto.ReqStore;
import pillihuaman.com.pe.neuroIA.RequestResponse.RespStore;
import pillihuaman.com.pe.neuroIA.repository.BaseMongoRepository;
import pillihuaman.com.pe.neuroIA.repository.store.Ia;

import java.util.List;

public interface IaDAO extends BaseMongoRepository<Ia> {

    /**WW
     * Lists stores based on specific request criteria.
     *
     * @param reqStore The request criteria for filtering stores.
     * @return A list of stores matching the criteria.
     */
    List<Ia> getIAResponse(ReqStore reqStore);


}
