package br.com.datum.mapper;

import br.com.datum.data.dto.ClienteDTO;
import br.com.datum.model.Cliente;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class ClienteMapper {

    public ClienteDTO toDTO(Cliente cliente) {
        if (cliente == null) {
            return null;
        }
        ClienteDTO dto = new ClienteDTO();
        BeanUtils.copyProperties(cliente, dto);
        return dto;
    }

    public Cliente toEntity(ClienteDTO dto) {
        if (dto == null) {
            return null;
        }
        Cliente entity = new Cliente();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}
