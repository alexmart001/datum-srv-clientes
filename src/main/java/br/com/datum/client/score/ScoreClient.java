package br.com.datum.client.score;

import br.com.datum.client.score.dto.ExternalScoreResponse;
import br.com.datum.exception.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class ScoreClient {

    private static final Logger logger = LoggerFactory.getLogger(ScoreClient.class);

    private final RestClient scoreRestClient;

    public ScoreClient(RestClient scoreRestClient) {
        this.scoreRestClient = scoreRestClient;
    }

    public ExternalScoreResponse consultarScore(String cpf) {
        try {
            return scoreRestClient.get()
                    .uri("/scores/{cpf}", cpf)
                    .retrieve()
                    .body(ExternalScoreResponse.class);
        } catch (RestClientResponseException ex) {
            logger.error("Serviço de score retornou erro {} para cpf={}: {}",
                    ex.getStatusCode(), cpf, ex.getResponseBodyAsString());
            throw new ExternalServiceException(
                    "Serviço de score retornou erro (" + ex.getStatusCode() + ") ao consultar o CPF informado", ex);
        } catch (RestClientException ex) {
            logger.error("Falha ao chamar o serviço de score para cpf={}: {}", cpf, ex.getMessage());
            throw new ExternalServiceException("Serviço de score indisponível no momento", ex);
        }
    }
}
