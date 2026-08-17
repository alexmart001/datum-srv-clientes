package br.com.datum.client.score.dto;

public record ExternalScoreResponse(String cpf, int score, String classification) {
}
