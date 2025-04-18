package pillihuaman.com.pe.neuroIA.Help;

public enum RoutingKey {

	LOG_TABLE("log_table"), 
	LOG_SERVICE("log_service"), 
	PRINT("print");

	private String key;

	private RoutingKey(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
}
