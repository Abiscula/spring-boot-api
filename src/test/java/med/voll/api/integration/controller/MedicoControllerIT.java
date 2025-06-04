package med.voll.api.integration.controller;

import med.voll.api.domain.endereco.DadosEndereco;
import med.voll.api.domain.medico.*;
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
    private MedicoRepository medicoRepository;

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
        var response = restTemplate.exchange("/login", HttpMethod.POST, request, String.class);

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


    @Test
    void deveCadastrarMedicoComSucesso() {
        final var endereco = retornaEndereco();
        final var dadosMedico  =  new DadosCadastroMedico(
                "João da Silva", "joao.medico@voll.med", "1199999999",
                "53454", Especialidade.CARDIOLOGIA, endereco
        );;

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        var request = new HttpEntity<>(dadosMedico, headers);
        var response = restTemplate.exchange(URI.create(BASE_URL), HttpMethod.POST, request, String.class);

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
        var response = restTemplate.exchange(URI.create(BASE_URL), HttpMethod.POST, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void naoDeveCadastrarMedicoQueJaExisteNoBanco() {
        final var endereco = retornaEndereco();
        var dadosMedico  =  new DadosCadastroMedico(
                "Jose da Silva", "jose.medico@voll.med", "1199999999",
                "53455", Especialidade.DERMATOLOGIA, endereco
        );;
        var medico = new Medico(dadosMedico);
        medicoRepository.save(medico);

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        var request = new HttpEntity<>(dadosMedico, headers);

        var response = restTemplate.exchange(BASE_URL, HttpMethod.POST, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody())
                .satisfiesAnyOf(
                        body -> assertThat(body).contains("Já existe um médico com esse e-mail."),
                        body -> assertThat(body).contains("Já existe um médico com esse CRM.")
                );
    }

    @Test
    void deveAtualizarMedicoComSucesso() {
        final var endereco = retornaEndereco();
        var dadosMedico  =  new DadosCadastroMedico(
                "Jose da Silva", "jose.medico@voll.med", "1199999999",
                "53455", Especialidade.DERMATOLOGIA, endereco
        );;
        var medico = new Medico(dadosMedico);
        medicoRepository.save(medico);

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        var dadosAtualizacao = new DadosAtualizacaoMedico(
                medico.getId(),
                null,
                "11988888888", // novo telefone
                null
        );

        var request = new HttpEntity<>(dadosAtualizacao, headers);
        var response = restTemplate.exchange(BASE_URL, HttpMethod.PUT, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }
}