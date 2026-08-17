package br.com.datum.data.dto;

public record CustomerScoreDTO(Long customerId, String cpf, int score, String classification) {
}
