package pillihuaman.com.pe.neuroIA.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary; // <-- IMPORTANTE
import pillihuaman.com.pe.neuroIA.Help.Constante;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    // Nombres de todas las cachés que usará este microservicio
    public static final String SIGNED_URL_CACHE = "signedUrlCache";
    public static final String FILE_METADATA_CACHE = "findAllByProductIds";
    public static final String PROMPT_TEMPLATE_CACHE = "promptTemplateCache";

    @Bean("neuroIaCacheManager") // Le damos un nombre único y descriptivo
    @Primary // <-- ¡CRUCIAL! Le dice a Spring que este es el CacheManager principal.
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // Le enseñamos al gestor los nombres de TODAS las cachés que debe manejar.
        cacheManager.setCacheNames(List.of(SIGNED_URL_CACHE, FILE_METADATA_CACHE));

        // Configura el comportamiento por defecto para ambas cachés.
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(Constante.LIFE_TIME_IMG_AWS - 1, TimeUnit.MINUTES)
                .maximumSize(10000)); // Máximo de 10,000 entradas combinadas

        return cacheManager;
    }
}