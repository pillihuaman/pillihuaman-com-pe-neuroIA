package pillihuaman.com.pe.neuroIA.RequestResponse.dto;


import pillihuaman.com.pe.neuroIA.RequestResponse.Message;

import java.util.ArrayList;
import java.util.List;

public class ChatRequest {
    private String model;
    private List<Message> messages;
    private int n = 1; // Número de respuestas (opcional)
    private double temperature = 1.0; // Controla la aleatoriedad (opcional)

    public ChatRequest(String model, String prompt) {
        this.model = model;
        this.messages = new ArrayList<>();
        this.messages.add(new Message("user", prompt));
    }

    // Getters y Setters
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
}
