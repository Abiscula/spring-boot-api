package med.voll.api.controller;

import jakarta.validation.Valid;
import med.voll.api.domain.medico.*;
import med.voll.api.infra.exception.custom.MedicoJaExisteException;
import med.voll.api.infra.exception.custom.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("medicos") //url
public class MedicoController {

    @Autowired
    private MedicoRepository repository;

    @PostMapping
    @Transactional
    public ResponseEntity cadastrar(@RequestBody @Valid DadosCadastroMedico dados, UriComponentsBuilder uriBuilder) {
        if (repository.existsByCrm(dados.crm())) {
            throw new MedicoJaExisteException("Já existe um médico com esse CRM.");
        }

        if (repository.existsByEmail(dados.email())) {
            throw new MedicoJaExisteException("Já existe um médico com esse e-mail.");
        }

        var medico = new Medico(dados);
        repository.save(medico);
        var uri = uriBuilder.path("/medicos/{id}").buildAndExpand(medico.getId()).toUri();
        return ResponseEntity.created(uri).body(new DadosDetalhamentoMedico(medico));
    }

    @GetMapping
    public ResponseEntity<Page<DadosListagemMedico>> listar(@PageableDefault(size = 10, sort = {"nome"}) Pageable paginacao) {
        //retornando lista com paginação de DadosListagemMedico (DTO)
        var page = repository.findAllByAtivoTrue(paginacao).map(DadosListagemMedico::new);
        return ResponseEntity.ok(page);
    }

    @PutMapping
    @Transactional
    public ResponseEntity atualizar(@RequestBody @Valid DadosAtualizacaoMedico dados) {
        try {
            var medico = repository.getReferenceById(dados.id());
            medico.atualizarInformacoes(dados);
            return ResponseEntity.ok(new DadosDetalhamentoMedico(medico));
        } catch (Exception e) {
            throw new ResourceNotFoundException("Medico não encontrado");
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity excluir(@PathVariable Long id) {
        try {
            var medico = repository.getReferenceById(id);
            medico.excluir();
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new ResourceNotFoundException("Medico não encontrado");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity detalhar(@PathVariable Long id) {
        try {
            var medico = repository.getReferenceById(id);
            return ResponseEntity.ok(new DadosDetalhamentoMedico(medico));
        } catch (Exception e) {
            throw new ResourceNotFoundException("Medico não encontrado");
        }
    }




}
