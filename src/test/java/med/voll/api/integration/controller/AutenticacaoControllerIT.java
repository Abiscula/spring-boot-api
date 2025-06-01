package med.voll.api.integration.controller;

import med.voll.api.domain.usuario.Usuario;
import med.voll.api.domain.usuario.UsuarioRepository;
import med.voll.api.domain.usuario.DadosAutenticacao;
import med.voll.api.infra.security.DadosTokenJWT;
import med.voll.api.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;


import static org.assertj.core.api.Assertions.assertThat;

class AutenticacaoControllerIT extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
        String rawPassword = "123456";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        Usuario usuario = new Usuario(null, "ana.souza@voll.med", encodedPassword);
        usuarioRepository.save(usuario);
    }

    @Test
    void deveAutenticarEReceberToken() {
        DadosAutenticacao dados = new DadosAutenticacao("ana.souza@voll.med", "123456");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DadosAutenticacao> request = new HttpEntity<>(dados, headers);

        ResponseEntity<DadosTokenJWT> response = restTemplate.postForEntity("/login", request, DadosTokenJWT.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().token()).isNotBlank();
    }

}