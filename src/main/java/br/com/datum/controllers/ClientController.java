package br.com.datum.controllers;

import br.com.datum.data.dto.ClienteDTO;
import br.com.datum.data.dto.CustomerScoreDTO;
import br.com.datum.services.ClienteServices;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class ClientController {

    private ClienteServices clienteServices;

    public ClientController(ClienteServices clienteServices) {
        this.clienteServices = clienteServices;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ClienteDTO> findAll(@RequestParam(value = "status", required = false) String status){
        if (status != null) {
            return clienteServices.search(null, status);
        }
        return clienteServices.findAll();
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ClienteDTO> search(@RequestParam(value = "name", required = false) String name){
        return clienteServices.search(name, null);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ClienteDTO findById(@PathVariable("id") Long id){
        return clienteServices.findById(id);
    }

    @GetMapping(value = "/{id}/score", produces = MediaType.APPLICATION_JSON_VALUE)
    public CustomerScoreDTO getScore(@PathVariable("id") Long id){
        return clienteServices.getScore(id);
    }

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ClienteDTO create(@Valid @RequestBody ClienteDTO clienteDTO){
        return clienteServices.create(clienteDTO);
    }

    @PutMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ClienteDTO update(@PathVariable("id") Long id, @Valid @RequestBody ClienteDTO clienteDTO){
        return clienteServices.update(id, clienteDTO);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id){
        clienteServices.delete(id);
        return ResponseEntity.noContent().build();
    }
}
