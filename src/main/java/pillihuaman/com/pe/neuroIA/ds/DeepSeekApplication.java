package pillihuaman.com.pe.neuroIA.ds;

public class DeepSeekApplication {

    public static void main(String[] args) {
        try {
            var apiKey = System.getenv("DEEPSEEK_API_KEY");
            if (apiKey == null) {
                throw new IllegalStateException("DEEPSEEK_API_KEY environment variable not set");
            }

            var client = new DeepSeekClient(new DeepSeekModels.Config(apiKey, null));
            var response = client.chat("deepseek-reasoner", "How many r's are in the word Strawberry");
            if (!response.choices().isEmpty()) {
                var message = response.choices().get(0).message();
                System.out.println("Response: " + message.content());
                System.out.println("Total tokens used: " + response.usage().totalTokens());
            }

        } catch (DeepSeekException e) {
            System.err.println("Error calling DeepSeek API: " + e.getMessage());
            e.printStackTrace();
        }
    }
}