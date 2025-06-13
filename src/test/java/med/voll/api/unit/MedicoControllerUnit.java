package med.voll.api.unit;

import med.voll.api.controller.MedicoController;
import med.voll.api.domain.endereco.DadosEndereco;
import med.voll.api.domain.medico.*;
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

    private DadosEndereco retornaEndereco() {
        return new DadosEndereco(
                "Rua das Flores", "Bairro Exemplo", "01000000",
                "São Paulo", "SP", "Apto 123", "123"
        );
    }

    @Test
    void deveCadastrarMedicoComSucesso() {
        var endereco = retornaEndereco();
        var dados = new DadosCadastroMedico("João da Silva", "joao@voll.med", "11999999999", "12345", Especialidade.CARDIOLOGIA, endereco);

        when(repository.existsByCrm("12345")).thenReturn(false);
        when(repository.existsByEmail("joao@voll.med")).thenReturn(false);

        ArgumentCaptor<Medico> captor = ArgumentCaptor.forClass(Medico.class);
        when(repository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        UriComponentsBuilder uriBuilder = mock(UriComponentsBuilder.class);
        org.springframework.web.util.UriComponents uriComponents = mock(org.springframework.web.util.UriComponents.class);

        when(uriBuilder.path("/medicos/{id}")).thenReturn(uriBuilder);
        when(uriBuilder.buildAndExpand((Map<Long, ?>) any())).thenReturn(uriComponents);
        when(uriComponents.toUri()).thenReturn(URI.create("/medicos/1"));

        var response = controller.cadastrar(dados, uriBuilder);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).isEqualTo(URI.create("/medicos/1"));
        assertThat(response.getBody()).isInstanceOf(DadosDetalhamentoMedico.class);

        Medico medicoSalvo = captor.getValue();
        assertThat(medicoSalvo.getNome()).isEqualTo("João da Silva");
    }

}
