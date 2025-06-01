package med.voll.api.integration.controller;

import med.voll.api.domain.endereco.DadosEndereco;
import med.voll.api.domain.medico.DadosCadastroMedico;
import med.voll.api.domain.medico.Especialidade;
import med.voll.api.domain.usuario.DadosAutenticacao;
import med.voll.api.domain.usuario.Usuario;
import med.voll.api.domain.usuario.UsuarioRepository;
import med.voll.api.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class MedicoControllerIT extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String token;

    private final String BASE_URL = "/medicos";

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();

        var rawPassword = "123456";
        var encodedPassword = passwordEncoder.encode(rawPassword);
        var usuario = new Usuario(null, "ana.souza@voll.med", encodedPassword);
        usuarioRepository.save(usuario);

        var auth = new DadosAutenticacao("ana.souza@voll.med", "123456");
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        var request = new HttpEntity<>(auth, headers);
        var response = restTemplate.postForEntity("/login", request, String.class);

        var tokenRaw = response.getBody();
        Assertions.assertNotNull(tokenRaw);
        token = tokenRaw.substring(10, tokenRaw.length() - 2);
    }

    private DadosEndereco retornaEndereco() {
        return new DadosEndereco(
                "Rua das Flores", "Bairro Exemplo", "01000000",
                "São Paulo", "SP", "Apto 123", "123"
        );
    }

    private DadosCadastroMedico retornaDadosMedicos(DadosEndereco endereco) {
        return new DadosCadastroMedico(
                "João da Silva", "joao.medico@voll.med", "1199999999",
                "53454", Especialidade.CARDIOLOGIA, endereco
        );
    }

    @Test
    void deveCadastrarMedicoComSucesso() {
        final var endereco = retornaEndereco();
        final var dadosMedico  = retornaDadosMedicos(endereco);

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        var request = new HttpEntity<>(dadosMedico, headers);
        var response = restTemplate.postForEntity(URI.create(BASE_URL), request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).isNotNull();
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void naoDeveCadastrarMedicoComDadosInvalidos() {
        var endereco = retornaEndereco();
        var dadosMedicoInvalido = new DadosCadastroMedico(
                "João da Silva",
                "joao.medico@voll.med",
                "1199999999",
                "123", // CRM inválido
                Especialidade.CARDIOLOGIA,
                endereco
        );

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        var request = new HttpEntity<>(dadosMedicoInvalido, headers);
        var response = restTemplate.postForEntity(URI.create(BASE_URL), request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
    }
}