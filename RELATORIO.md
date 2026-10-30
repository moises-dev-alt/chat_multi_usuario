# Relatorio - Chat Multiusuario com Historico

## Arquitetura

O sistema foi separado em quatro pacotes. `model` possui as entidades imutaveis
`Usuario` e `Mensagem`; `persistence` contem apenas o acesso JDBC em
`MensagemRepository`; `server` concentra a escuta, o broadcast e o tratamento de
uma conexao por `ClienteHandler`; e `client` oferece um cliente de console.

O banco escolhido foi H2 em modo arquivo. Ele permite que o trabalho rode localmente
sem instalar um servidor de banco separado, mas o acesso continua sendo JDBC com SQL,
`PreparedStatement` e transacoes por operacao. O script `schema.sql` descreve a tabela.

## Concorrencia

Cada socket aceito pelo `Servidor` e entregue a uma tarefa `ClienteHandler` no
`ExecutorService`, sem bloquear a aceitacao de novas conexoes. A lista de clientes
ativos e uma `CopyOnWriteArrayList`, adequada para muitos broadcasts e poucas entradas
ou saidas. Cada `ClienteHandler` sincroniza sua propria escrita no socket, impedindo que
duas threads misturem caracteres na mesma mensagem.

O servidor persiste a mensagem antes do broadcast. Na conexao, envia um snapshot das
ultimas 20 mensagens antes de incluir o cliente na lista de destinatarios; assim o novo
cliente nao recebe mensagens duplicadas do historico. Falhas ou desconexoes sao tratadas
no `finally`, removendo somente aquele cliente e mantendo o servidor ativo.

## Testes

Os testes unitarios cobrem validacao de usuario e mensagem, formatacao, ordenacao e
limite de historico, alem da regra que impede o envio de texto vazio. Eles nao dependem
de sockets nem do banco de dados.

