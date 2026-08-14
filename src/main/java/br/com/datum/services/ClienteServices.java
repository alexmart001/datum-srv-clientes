package br.com.datum.services;

import br.com.datum.data.dto.ClienteDTO;
import br.com.datum.exception.ResourceNotFoundException;
import br.com.datum.mapper.ClienteMapper;
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

    public ClienteServices(ClienteRepository clienteRepository, ClienteMapper clienteMapper) {
        this.clienteRepository = clienteRepository;
        this.clienteMapper = clienteMapper;
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

    public void delete(Long id){
        logger.info("Deleting Cliente by id: {}", id);

        var cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente not found!"));

        clienteRepository.delete(cliente);
    }
}
