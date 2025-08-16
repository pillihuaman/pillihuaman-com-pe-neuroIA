package pillihuaman.com.pe.neuroIA.repository;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;


public abstract class AzureAbstractMongoRepositoryImpl<T> implements BaseMongoRepository<T> {
    private static final Logger log = LoggerFactory.getLogger(AzureAbstractMongoRepositoryImpl.class);
    protected String COLLECTION;
    protected String DS_WRITE;
    protected String DS_READ;

    private MongoClient mongoClient;
    private MongoDatabase database;

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    /**
     * Método llamado después de que Spring inyecta los valores.
     */
    @PostConstruct
    private void initMongo() {
        CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(mongoUri))
                .codecRegistry(codecRegistry)  // <-- Aplicamos el CodecRegistry aquí
                .build();

        this.mongoClient = MongoClients.create(settings);
        this.database = mongoClient.getDatabase(databaseName).withCodecRegistry(codecRegistry); // <-- También aquí
        log.info("✅ Conexión con MongoDB inicializada para la base de datos: {}", databaseName);
    }

    /**
     * Cada subclase debe proporcionar su clase de entidad.
     */
    public abstract Class<T> provideEntityClass();

    /**
     * Obtiene la colección asegurándose de que `collectionName` no sea `null`.
     */
    @Override
    public <T> MongoCollection<T> getCollection(String colname, Class<T> classType) {
        return database.getCollection(colname, classType);
    }

    /**
     * Encuentra todos los documentos en una colección.
     */
    @Override
    public List<T> findAll() {
        MongoCollection<T> collection = getCollection(COLLECTION, provideEntityClass());
        return collection.find().into(new ArrayList<>());
    }

    /**
     * Encuentra documentos con una consulta específica.
     */
    @Override
    public List<T> findAllByQuery(Bson query) {
        MongoCollection<T> collection = getCollection(COLLECTION, provideEntityClass());
        return collection.find(query).into(new ArrayList<>());
    }

    /**
     * Encuentra un documento por ID.
     */
    @Override
    public T findOneById(Bson query) {
        MongoCollection<T> collection = getCollection(COLLECTION, provideEntityClass());
        return collection.find(query).first();
    }

    /**
     * Guarda un nuevo documento.
     */
    @Override
    public T save(T document) {
        MongoCollection<T> collection = getCollection(COLLECTION, provideEntityClass());
        collection.insertOne(document);
        return document;
    }

    /**
     * Actualiza un documento existente.
     */
    @Override
    public void updateOne(Bson filter, T document) {
        MongoCollection<T> collection = getCollection(COLLECTION, provideEntityClass());
        collection.replaceOne(filter, document);
    }

    /**
     * Elimina un documento basado en un filtro.
     */
    public void deleteOne(Bson filter) {
        MongoCollection<T> collection = getCollection(COLLECTION, provideEntityClass());
        collection.deleteOne(filter);
    }

    /**
     * Cierra la conexión con MongoDB.
     */
    public void closeConnection() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    /**
     * Encuentra documentos con una consulta específica y un índice de sugerencia.
     */
    @Override
    public List<T> findAllWithHintByQuery(Bson query, Bson index) {
        MongoCollection<T> collection = getCollection(COLLECTION, provideEntityClass());
        return collection.find(query).hint(index).into(new ArrayList<>());
    }

    /**
     * Encuentra un documento con una consulta específica y un índice de sugerencia.
     */
    @Override
    public T findOneWithHintByQuery(Bson query, Bson index) {
        MongoCollection<T> collection = getCollection(COLLECTION, provideEntityClass());
        return collection.find(query).hint(index).first();
    }

    /**
     * Inserta un documento en la colección.
     */
    @Override
    public void insertCollection(T document) {
        MongoCollection<T> collection = getCollection(COLLECTION, provideEntityClass());
        collection.insertOne(document);
    }

    /**
     * Inserta un documento y devuelve el resultado.
     */
    @Override
    public T insert(T document) {
        MongoCollection<T> collection = getCollection(COLLECTION, provideEntityClass());
        collection.insertOne(document);
        return document;
    }

    /**
     * Inserta una lista de documentos en la colección.
     */
    @Override
    public <T> MongoCollection<T> insertCollection(String colname, Class<T> classType, List<T> listObjects) {
        MongoCollection<T> collection = getCollection(colname, classType);
        collection.insertMany(listObjects);
        return collection;
    }

    /**
     * Actualiza documentos en la colección.
     */
    @Override
    public <T> MongoCollection<T> updateCollection(String colname, Class<T> classType, Bson filter, Bson query) {
        MongoCollection<T> collection = getCollection(colname, classType);
        collection.updateMany(filter, query);
        return collection;
    }

    /**
     * Actualiza documentos en la colección sin tipo.
     */
    @Override
    public T updateCollection(String colname, Bson filter, Bson query) {
        MongoCollection<T> collection = getCollection(colname, provideEntityClass());
        collection.updateMany(filter, query);
        return null;
    }

    /**
     * Inserta un documento en la colección.
     */
    @Override
    public void insertOne(T document) {
        MongoCollection<T> collection = getCollection(COLLECTION, provideEntityClass());
        collection.insertOne(document);
    }

    /**
     * Obtiene la siguiente secuencia de una colección en MongoDB.
     */
   /* @Override
    public String getNextSequence(String collectionName, String sequenceName) {
        MongoCollection<org.bson.Document> collection = database.getCollection(collectionName);
        Document update = new Document("$inc", new Document("seq", 1));
        Document options = new Document("returnDocument", "after");
        Document result = collection.findOneAndUpdate(new Document("_id", sequenceName), update, options);
        return result != null ? result.get("seq").toString() : "1";
    }*/
    /**
     * Guarda o actualiza un documento basado en su ID.
     */

    protected void printAllDataFromCollection(String collectionName) {
        if (this.database == null) {
            log.error("🔥 La base de datos no está inicializada. No se puede imprimir la información.");
            return;
        }
        try {
            MongoCollection<Document> collection = this.database.getCollection(collectionName, Document.class);

            log.info("========================================================================================");
            log.info("IMPRIMIENDO INFORMACIÓN DE LA COLECCIÓN");
            log.info("========================================================================================");
            log.info("✅ Base de Datos: {}", this.database.getName());
            log.info("✅ Colección    : {}", collectionName);
            log.info("----------------------------------------------------------------------------------------");

            long documentCount = collection.countDocuments();

            if (documentCount == 0) {
                log.warn("🟡 La colección '{}' está vacía. No hay documentos para mostrar.", collectionName);
            } else {
                log.info("⬇️  Encontrados {} documentos en la colección. Mostrando todos: ⬇️", documentCount);
                FindIterable<Document> documents = collection.find();

                // Iteramos e imprimimos cada documento en formato JSON
                documents.forEach((Consumer<Document>) doc -> {
                    log.info(doc.toJson());
                });

                log.info("----------------------------------------------------------------------------------------");
                log.info("✅ Impresión de la colección '{}' finalizada.", collectionName);
            }
            log.info("========================================================================================");

        } catch (Exception e) {
            log.error("🔥 Error al intentar imprimir la información de la colección '{}': {}", collectionName, e.getMessage(), e);
        }
    }

}
