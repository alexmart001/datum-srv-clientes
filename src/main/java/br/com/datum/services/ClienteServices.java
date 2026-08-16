package br.com.datum.services;

import br.com.datum.client.score.ScoreClient;
import br.com.datum.data.dto.ClienteDTO;
import br.com.datum.data.dto.CustomerScoreDTO;
import br.com.datum.exception.ResourceNotFoundException;
import br.com.datum.mapper.ClienteMapper;
import br.com.datum.messaging.CustomerEventPublisher;
import br.com.datum.model.Cliente;
import br.com.datum.repository.ClienteRepository;
import br.com.datum.serializer.StatusConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ClienteServices {

    private final AtomicLong counter = new AtomicLong();
    private final Logger logger = LoggerFactory.getLogger(ClienteServices.class);

    ClienteRepository clienteRepository;

    ClienteMapper clienteMapper;

    CustomerEventPublisher customerEventPublisher;

    ScoreClient scoreClient;

    public ClienteServices(ClienteRepository clienteRepository, ClienteMapper clienteMapper,
                            CustomerEventPublisher customerEventPublisher, ScoreClient scoreClient) {
        this.clienteRepository = clienteRepository;
        this.clienteMapper = clienteMapper;
        this.customerEventPublisher = customerEventPublisher;
        this.scoreClient = scoreClient;
    }

    public List<ClienteDTO> findAll(){
        logger.info("Finding all Clientes!");
        return clienteRepository.findAll().stream()
                .map(clienteMapper::toDTO)
                .toList();
    }

    public List<ClienteDTO> search(String name, String status){
        logger.info("Searching Clientes by name: {} and status: {}", name, status);

        Specification<Cliente> spec = Specification.where(null);

        if (StringUtils.hasText(name)) {
            String likePattern = "%" + name.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("nome")), likePattern));
        }

        if (StringUtils.hasText(status)) {
            boolean statusValue = StatusConverter.toBoolean(status);
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), statusValue));
        }

        return clienteRepository.findAll(spec).stream()
                .map(clienteMapper::toDTO)
                .toList();
    }

    public ClienteDTO findById(Long id){
        logger.info("Finding Cliente by id: {}", id);

        var cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente not found!"));

        return clienteMapper.toDTO(cliente);
    }

    public ClienteDTO create(ClienteDTO clienteDTO){
        logger.info("Creating Cliente: {}", clienteDTO);

        var cliente = clienteMapper.toEntity(clienteDTO);
        cliente = clienteRepository.save(cliente);

        customerEventPublisher.publishCustomerCreated(cliente.getId());

        return clienteMapper.toDTO(cliente);
    }

    public ClienteDTO update(Long id, ClienteDTO clienteDTO){
        logger.info("Updating Cliente: {}", clienteDTO);

        var cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente not found!"));

        cliente.setNome(clienteDTO.getNome());
        cliente.setCpf(clienteDTO.getCpf());
        cliente.setEmail(clienteDTO.getEmail());
        cliente.setStatus(clienteDTO.isStatus());

        cliente = clienteRepository.save(cliente);

        return clienteMapper.toDTO(cliente);
    }

    /**
     * Altera apenas o status do cliente, preservando os demais campos.
     * Usado pelo consumidor de mensagens CUSTOMER_STATUS_CHANGE
     * (CustomerStatusChangeListener) - diferente de update(), que exige o
     * payload completo do cliente.
     */
    public ClienteDTO updateStatus(Long id, boolean status){
        logger.info("Updating status do Cliente id={} para status={}", id, status);

        var cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente not found!"));

        cliente.setStatus(status);
        cliente = clienteRepository.save(cliente);

        return clienteMapper.toDTO(cliente);
    }

    /**
     * Consulta o score do cliente junto ao serviço externo
     * datum-srv-score-cliente (GET /scores/{cpf}), a partir do CPF já
     * cadastrado localmente.
     */
    public CustomerScoreDTO getScore(Long id){
        logger.info("Consultando score do Cliente id={}", id);

        var cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente not found!"));

        var scoreResponse = scoreClient.consultarScore(cliente.getCpf());

        return new CustomerScoreDTO(cliente.getId(), scoreResponse.cpf(), scoreResponse.score(), scoreResponse.classification());
    }

    public void delete(Long id){
        logger.info("Deleting Cliente by id: {}", id);

        var cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente not found!"));

        clienteRepository.delete(cliente);
    }
}
