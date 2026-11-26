# Orçamento por voz

API de estudos para propor despesas a partir de um comando gravado e consultar totais por categoria. Implementa o desafio de Spring AI do bootcamp Itaú com Java 21, Spring Boot, Spring AI, JDBC e H2.

A melhoria é a **confirmação explícita do lançamento**. O áudio pode gerar uma proposta, mas a despesa só aparece no orçamento quando o cliente confirma seu identificador. A ferramenta registrada no ChatClient consulta gastos já confirmados. Dois pedidos de confirmação para o mesmo identificador registram a despesa uma única vez.

## Executar sem chave

Use Java 21 e Maven. Rode `mvn test` para verificar a validação monetária, a exclusão de propostas dos totais e a confirmação única. Inicie a API com `mvn spring-boot:run`.

Crie e confirme uma proposta fictícia para testar o fluxo:

```shell
curl -X POST http://localhost:8080/lancamentos/propostas \
  -H "Content-Type: application/json" \
  -d '{"descricao":"Almoço","categoria":"alimentação","valor":24.50}'
curl -X POST http://localhost:8080/lancamentos/propostas/UUID-RETORNADO/confirmar
curl "http://localhost:8080/lancamentos?categoria=alimenta%C3%A7%C3%A3o"
```

## Habilitar áudio

Com `OPENAI_API_KEY` no ambiente, inicie com `mvn spring-boot:run -Dspring-boot.run.profiles=voz`. O perfil usa o provedor externo para transcrever e interpretar áudio. Use dados fictícios no exercício. A execução em nuvem depende de chave e disponibilidade de créditos.

```shell
curl -F "audio=@despesa-ficticia.wav;type=audio/wav" http://localhost:8080/voz/comandos
curl -X POST http://localhost:8080/voz/narracoes \
  -H "Content-Type: application/json" \
  -d '{"mensagem":"O lançamento pode ser revisado antes de confirmar."}' -o resposta.mp3
```

O caso de uso separa a interpretação probabilística da decisão de gravar a despesa. A API mantém dados locais no H2 e escuta apenas em loopback. O projeto se baseia no enunciado e no [material de referência da DIO](https://github.com/digitalinnovationone/dio-spring-boot-learning-track/tree/main/05-spring-ai).
