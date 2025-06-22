package med.voll.api.unit;

import med.voll.api.controller.MedicoController;
import med.voll.api.domain.endereco.DadosEndereco;
import med.voll.api.domain.medico.*;
import med.voll.api.infra.exception.custom.MedicoJaExisteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
public class MedicoControllerUnit {

    @InjectMocks
    private MedicoController controller;

    @Mock
    private MedicoRepository repository;

    @Mock
    private UriComponentsBuilder uriBuilder;

    private Medico medico;

    private DadosCadastroMedico dados;

    private DadosEndereco retornaEndereco() {
        return new DadosEndereco(
                "Rua das Flores", "Bairro Exemplo", "01000000",
                "São Paulo", "SP", "Apto 123", "123"
        );
    }

    private DadosCadastroMedico retornaDadosCadastroMedico() {
        var endereco = retornaEndereco();

        return new DadosCadastroMedico("João da Silva", "joao@voll.med",
                "11999999999", "12345", Especialidade.CARDIOLOGIA, endereco);

    }

    @BeforeEach
    public void setUp() {
        dados = retornaDadosCadastroMedico();
        medico = new Medico(dados);
    }

    @Test
    void deveRecusarCadastroSeCrmJaExistir() {
        when(repository.existsByCrm("12345")).thenReturn(true);

        var exception = assertThrows(MedicoJaExisteException.class, () -> {
            controller.cadastrar(dados, uriBuilder);
        });

        assertThat(exception.getMessage()).contains("Já existe um médico com esse CRM.");
    }

    @Test
    void deveCadastrarMedicoComSucesso() {
        when(repository.existsByCrm(medico.getCrm())).thenReturn(false);
        when(repository.existsByEmail(medico.getEmail())).thenReturn(false);

        ArgumentCaptor<Medico> captor = ArgumentCaptor.forClass(Medico.class);
        when(repository.save(captor.capture())).thenAnswer(invocation ->
                invocation.getArgument(0));

        UriComponentsBuilder uriBuilder = mock(UriComponentsBuilder.class);
        var uriComponentsMock = mock(org.springframework.web.util.UriComponents.class);

        when(uriBuilder.path("/medicos/{id}")).thenReturn(uriBuilder);
        when(uriBuilder.buildAndExpand(Optional.ofNullable(any()))).thenReturn(uriComponentsMock);
        when(uriComponentsMock.toUri()).thenReturn(URI.create("/medicos/1"));

        var response = controller.cadastrar(dados, uriBuilder);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).isEqualTo(URI.create("/medicos/1"));
    }

    @Test
    void deveRetornarOMedicoPorIdComSucesso() {
        when(repository.getReferenceById(anyLong()))
                .thenReturn(medico);

        var response = controller.detalhar(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(DadosDetalhamentoMedico.class);
        assertNotNull(response.getBody());
        assertThat(((DadosDetalhamentoMedico) response.getBody())
                .nome()).isEqualTo("João da Silva");
        assertThat(((DadosDetalhamentoMedico) response.getBody())
                .crm()).isEqualTo("12345");
    }

}
