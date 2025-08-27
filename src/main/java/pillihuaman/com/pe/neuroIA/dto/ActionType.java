package pillihuaman.com.pe.neuroIA.dto;

public enum ActionType {
    API_CALL,      // El orquestador debe llamar a otro microservicio.
    INTERNAL_LINK, // El orquestador debe generar un link para el frontend.
    START_WORKFLOW // El orquestador debe iniciar un proceso de negocio.
}
