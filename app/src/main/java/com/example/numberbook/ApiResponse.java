package com.example.numberbook;

// Commentaire LEMGHILI Mohammed Amine: represente la reponse JSON envoyee par le backend PHP.
public class ApiResponse {
    private boolean success;
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
