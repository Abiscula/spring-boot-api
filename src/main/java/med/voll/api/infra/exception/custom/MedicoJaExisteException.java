package med.voll.api.infra.exception.custom;

public class MedicoJaExisteException extends RuntimeException {
    public MedicoJaExisteException(String mensagem) {
        super(mensagem);
    }
}
