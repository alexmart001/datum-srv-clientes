package br.com.datum.client.score.dto;

/**
 * Espelha o contrato de resposta do serviço externo de score
 * (GET /scores/{cpf}, no datum-srv-score-cliente):
 * {"cpf": "...", "score": 750, "classification": "LOW_RISK"}
 *
 * "classification" é lido como String (não como enum) de propósito: se o
 * serviço externo adicionar um novo valor no futuro, a desserialização
 * aqui não quebra por causa de um valor de enum desconhecido.
 */
public record ExternalScoreResponse(String cpf, int score, String classification) {
}
