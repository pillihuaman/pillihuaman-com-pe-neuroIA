package pillihuaman.com.pe.neuroIA.repository;

import com.mongodb.client.MongoCollection;
import org.bson.conversions.Bson;

import java.util.List;

public interface BaseMongoRepository<T> {

	/**
	 * Devuelve todos los elementos de una colecci�n
	 *
	 * @return
	 */
	List<T> findAll();

	/**
	 * Devuelve todos los elementos de una colecci�n
	 *
	 * @return
	 */
	List<T> findAllByQuery(Bson query);

	/**
	 * Devuelve todos los elementos de una coleccion ingresando por el indice especificado
	 *
	 * @return
	 */

	List<T> findAllWithHintByQuery(Bson query, Bson index);

	/**
	 * Devuelve un elemento de una colecci�n por un filtro especifico
	 *
	 * @param query
	 * @return
	 */
	T findOneById(Bson query);

	/**
	 * Buscar el document por un criterio y especificando un indice
	 * @param query
	 * @param index
	 * @return
	 */

	T findOneWithHintByQuery(Bson query, Bson index);

	/**
	 * Grabar el Document enviado
	 *
	 * @param document
	 */
	T save(T document);

	/**
	 * Grabar el Document enviado
	 *
	 * @param document
	 */
//	Document save(Document document);

	/**
	 * Inserta un document en la colecci�n
	 *

	 */
	void insertCollection(T document);

	/**
	 * Obtiene una colecci�n con un tipo espec�fico
	 *
	 * @param <T>
	 * @param colname
	 * @param classType
	 * @return
	 */
	<T> MongoCollection<T> getCollection(String colname, Class<T> classType);

	/**
	 * Inserta una lista de documentos en la colecci�n
	 *
	 * @param <T>
	 * @param colname
	 * @param classType
	 * @param listObjects
	 * @return
	 */
	<T> MongoCollection<T> insertCollection(String colname, Class<T> classType, List<T> listObjects);

	/**
	 * Actualiza elementos de una colecci�n tipada
	 *
	 * @param <T>
	 * @param colname
	 * @param classType
	 * @param filter
	 * @param query
	 * @return
	 */
	<T> MongoCollection<T> updateCollection(String colname, Class<T> classType, Bson filter, Bson query);

	/**
	 * Actualiza elementos de una colecci�n sin tipo (document)
	 *
	 * @param colname
	 * @param filter
	 * @param query
	 * @return
	 */
	T updateCollection(String colname, Bson filter, Bson query);

	/**
	 * Inserta un document en la colecci�n
	 * @param document
	 */
	void insertOne(T document);

	/**
	 * Inserta un documento y retorna el resultado
	 * @param document
	 * @return
	 */
	T insert(T document);


	/**
	 * Actualiza un document en la colecci�n
	 * @param filter
	 * @param document
	 */
	void updateOne(Bson filter, T document);

	/**
	 * Obtiene siguiente correlativo desde una sequencia de mongodb
	 * @param collectionName
	 * @param sequenceName
	 * @return
	 */
	//String getNextSequence(String collectionName, String sequenceName);

	/**
	 * Almacena un Java POJO en GridFS asociando un nombre.
	 * @param <T> objeto del tipo T que se almacenar&aacute; en GridFS
	 * @param gridFsFileName nombre del objeto almacenado en GridFS
	 * @return id identificardor del objeto almacenado en GridFS
	 * @throws Exception
	 */
}