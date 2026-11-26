package br.dev.bootcamp.orcamento;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/voz")
@Profile("voz")
class VozController {
    private final TranscriptionModel transcricao;
    private final TextToSpeechModel voz;
    private final ChatClient chat;
    private final Orcamento orcamento;

    VozController(TranscriptionModel transcricao, TextToSpeechModel voz, ChatClient.Builder chat,
                  Orcamento orcamento) {
        this.transcricao = transcricao;
        this.voz = voz;
        this.orcamento = orcamento;
        this.chat = chat.defaultSystem("""
                Interprete comandos de orçamento em português brasileiro.
                Use o recurso de consulta para responder sobre gastos confirmados.
                Para registrar um gasto, apenas proponha os campos. Informe que a
                pessoa precisa confirmar antes de registrar. Não invente valores.
                Deixe o campo proposta nulo para consultas e comandos ambíguos.
                """).build();
    }

    public record Interpretacao(String mensagem, Lancamento proposta) {}
    record Resultado(String transcricao, String mensagem, Lancamento proposta, UUID id) {}

    @PostMapping(value = "/comandos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Resultado interpretar(@RequestParam("audio") MultipartFile audio) {
        if (audio.isEmpty() || audio.getContentType() == null || !audio.getContentType().startsWith("audio/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie um arquivo de áudio.");
        }
        String texto = transcricao.transcribe(audio.getResource());
        if (texto == null || texto.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "O áudio não contém fala reconhecível.");
        }
        Interpretacao resultado = chat.prompt().user(texto).tools(new Consultas(orcamento))
                .call().entity(Interpretacao.class);
        if (resultado == null || resultado.mensagem() == null || resultado.mensagem().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "O modelo retornou uma resposta vazia.");
        }
        UUID id = resultado.proposta() == null ? null : orcamento.propor(resultado.proposta());
        return new Resultado(texto, resultado.mensagem(), resultado.proposta(), id);
    }

    @PostMapping(value = "/narracoes", produces = "audio/mpeg")
    byte[] narrar(@RequestBody Map<String, String> entrada) {
        String mensagem = entrada.get("mensagem");
        if (mensagem == null || mensagem.isBlank() || mensagem.length() > 800) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe uma mensagem com até 800 caracteres.");
        }
        return voz.call(mensagem);
    }

    public static final class Consultas {
        private final Orcamento orcamento;

        Consultas(Orcamento orcamento) {
            this.orcamento = orcamento;
        }

        @Tool(description = "Consulta o total, em reais, dos gastos já confirmados de uma categoria")
        public BigDecimal totalDaCategoria(String categoria) {
            return orcamento.total(categoria);
        }
    }
}
