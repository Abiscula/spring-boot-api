package med.voll.api.integration.repository;

import med.voll.api.domain.endereco.DadosEndereco;
import med.voll.api.domain.medico.DadosCadastroMedico;
import med.voll.api.domain.medico.Especialidade;
import med.voll.api.domain.medico.Medico;
import med.voll.api.domain.medico.MedicoRepository;
import med.voll.api.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import static org.assertj.core.api.Assertions.assertThat;


class MedicoRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private MedicoRepository medicoRepository;

    private DadosEndereco retornaEndereco() {
        return new DadosEndereco(
                "Rua das Flores", "Bairro Exemplo", "01000000",
                "São Paulo", "SP", "Apto 123", "123"
        );
    }

    @Test
    void deveRetornarApenasMedicosAtivos() {
        var endereco = retornaEndereco();
        var medico1  =  new DadosCadastroMedico(
                "Jose da Silva", "jose.medico@voll.med", "1199999999",
                "53455", Especialidade.DERMATOLOGIA, endereco
        );
        var medico2  =  new DadosCadastroMedico(
                "Maria da Silva", "maria.medico@voll.med", "2199999999",
                "53456", Especialidade.GINECOLOGIA, endereco
        );
        var medicoAtivo = new Medico(medico1);
        var medicoInativo = new Medico(medico2);
        medicoInativo.excluir();

        medicoRepository.save(medicoAtivo);
        medicoRepository.save(medicoInativo);

        var resultado = medicoRepository.findAllByAtivoTrue(Pageable.unpaged());

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getNome()).isEqualTo("Jose da Silva");
    }
}
