package br.com.datum.data.dto;

/**
 * Resposta de GET /customers/{id}/score: dados do score do cliente,
 * obtidos junto ao serviço externo de score (datum-srv-score-cliente),
 * enriquecidos com o id do cliente na base local.
 */
public record CustomerScoreDTO(Long customerId, String cpf, int score, String classification) {
}
