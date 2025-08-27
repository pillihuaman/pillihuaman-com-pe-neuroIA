package pillihuaman.com.pe.neuroIA.Help;

public enum IntentType {

    // --- BÚSQUEDA DE INFORMACIÓN (El usuario quiere ver/saber algo) ---
    FIND_PRODUCT,      // Intención específica de encontrar productos. Es el antiguo PRODUCT_SEARCH.
    FIND_CLIENT,       // Intención específica de encontrar un cliente. Es el antiguo CLIENT_SEARCH.
    FIND_ORDER,        // NUEVO: "buscar mi pedido 12345"
    FIND_INVOICE,      // NUEVO: "dónde está mi factura de mayo"
    FIND_SUPPORT_TICKET, // NUEVO: "ver el estado de mi ticket de ayuda"

    // --- ACCIÓN DIRECTA (El usuario quiere hacer algo) ---
    NAVIGATE_TO,       // Intención de ir a una página. Es el antiguo NAVIGATION.
    CREATE_ENTITY,     // NUEVO: "crear nuevo producto", "registrar un cliente" -> más genérico que NAVIGATION.
    START_PROCESS,     // NUEVO: "quiero devolver un producto", "iniciar una cotización"

    // --- PREGUNTAS (El usuario tiene una duda) ---
    GENERAL_FAQ,       // Preguntas generales sobre la empresa. Es el antiguo SUPPORT_QUERY.
    PRODUCT_INQUIRY,   // NUEVO: Preguntas específicas sobre un producto. "¿esta camisa es de algodón?", "¿tienen talla M?"
    POLICY_INQUIRY,    // NUEVO: Preguntas sobre políticas. "¿cuál es su política de devoluciones?"

    // --- COMANDOS (El usuario da una orden) ---
    ADD_TO_CART,       // NUEVO: Futuro. "añade la camisa azul talla M al carrito"
    REQUEST_CALL,      // NUEVO: "que un asesor me llame"

    // --- CATCH-ALL ---
    UNKNOWN            // La intención no pudo ser determinada con confianza.
}