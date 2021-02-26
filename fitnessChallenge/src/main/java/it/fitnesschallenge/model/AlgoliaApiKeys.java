package it.fitnesschallenge.model;

public class AlgoliaApiKeys {

    private String clientCode;
    private String apiKey;

    AlgoliaApiKeys() {
        // Necessario per la deserializzazione da firebase.
    }

    public String getClientCode() {
        return clientCode;
    }

    public void setClientCode(String clientCode) {
        this.clientCode = clientCode;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
