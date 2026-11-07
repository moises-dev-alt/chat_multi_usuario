# Chat Multiusuario com Historico

Projeto de Programacao Orientada a Objetos em Java: servidor TCP concorrente,
grupos de conversa, interface visual Swing, assistente de IA e historico persistido com JDBC.

## Recursos

- Interface visual feita somente com Java Swing.
- Varios usuarios conectados ao mesmo servidor local.
- Criacao de grupos e convites para pessoas conectadas.
- Historico separado por grupo, salvo no banco H2.
- Aba exclusiva para conversar com uma IA por OpenAI ou Ollama local.

## Requisitos e dependencias

- **Java 17 ou superior**: necessario para executar o projeto. Ja esta instalado neste computador.
- **Maven 3.9 ou superior**: necessario para compilar. Neste computador ha uma copia portatil em `.tools`; em outro computador, instale o Maven e adicione-o ao `PATH`.
- **H2 2.3.232**: biblioteca JDBC usada como banco de dados local. E baixada automaticamente na primeira compilacao e fica em `.m2repo`.
- **JUnit 5**: usado somente pelos testes automatizados; tambem e baixado na primeira compilacao.

Nao e necessario instalar plugin de IDE, MySQL, XAMPP ou outro servidor de banco de dados. Na primeira execucao de `compilar-e-testar.bat`, e preciso estar conectado a internet para baixar H2 e JUnit. Depois disso, o projeto pode ser executado sem internet. As pastas `.tools` e `.m2repo` sao locais e nao fazem parte do codigo enviado ao GitHub.

## Como executar no Windows

### 1. Compilar e testar

Na pasta do projeto, execute uma vez:

```powershell
.\compilar-e-testar.bat
```

O resultado esperado ao final e `BUILD SUCCESS`. Esse passo tambem baixa as bibliotecas necessarias.

### 2. Iniciar o servidor

Abra um terminal PowerShell na pasta do projeto e execute:

```powershell
.\iniciar-servidor.bat
```

Mantenha esse terminal aberto. A mensagem esperada e:

```text
Servidor iniciado na porta 5000.
```

### 3. Iniciar os clientes visuais

Abra tres outros terminais PowerShell na mesma pasta. Em cada um, execute:

```powershell
.\iniciar-cliente.bat
```

Informe um nome diferente em cada janela. Elas se conectam automaticamente ao mesmo servidor local (`localhost:5000`), portanto nao e necessario digitar endereco.

### 4. Criar e usar grupos

1. Na aba **Conversas**, clique em **+ Criar grupo** e informe um nome.
2. Clique em **Convidar pessoa**.
3. Digite exatamente o nome de outro usuario que ja esteja conectado.
4. O usuario convidado recebe uma mensagem e escolhe se deseja entrar no grupo.

As mensagens e o historico ficam visiveis somente para quem selecionou o mesmo grupo.

## Assistente IA

Abra a aba **Assistente IA** e clique em **Configurar IA**. A chave informada e usada apenas durante a execucao e nao e gravada no projeto.

### Opcao A: OpenAI

1. Crie uma chave em [OpenAI API Keys](https://platform.openai.com/api-keys).
2. Adicione creditos no faturamento da API, caso sua conta ainda nao tenha saldo.
3. Use os valores:

```text
Endpoint: https://api.openai.com/v1/chat/completions
Chave: sua chave da OpenAI
Modelo: gpt-4o-mini
```

> A assinatura do ChatGPT e o faturamento da API sao separados. O erro `credit_balance_exhausted` indica que e necessario adicionar creditos na conta da API.

### Opcao B: Ollama local (sem chave da OpenAI)

1. Instale o Ollama pelo site [ollama.com/download/windows](https://ollama.com/download/windows) ou, no PowerShell, execute:

```powershell
irm https://ollama.com/install.ps1 | iex
```

2. Feche e abra um novo PowerShell, depois baixe/inicie um modelo:

```powershell
ollama run llama3.2
```

3. Na configuracao da IA no aplicativo, use:

```text
Endpoint: http://localhost:11434/v1/chat/completions
Chave: deixe vazia
Modelo: llama3.2
```

Se aparecer `Connection refused: getsockopt`, verifique se o Ollama esta instalado e em execucao.

Para evitar digitar a chave em cada execucao, tambem e possivel definir a variavel de ambiente `OPENAI_API_KEY` antes de abrir o cliente.

> No PowerShell, o prefixo `.\` e obrigatorio para executar arquivos da pasta atual.

## Usar em mais de um computador

O atalho do cliente usa `localhost`, portanto todos os clientes no **mesmo computador** entram automaticamente no mesmo chat. Para usar em computadores diferentes da mesma rede, execute o servidor em uma maquina e inicie o cliente pelo terminal informando o IP dela:

```powershell
java -cp "target\classes;.m2repo\com\h2database\h2\2.3.232\h2-2.3.232.jar" br.edu.unicesumar.chat.client.ClienteVisual 192.168.0.10 5000
```

Substitua `192.168.0.10` pelo IP do computador que esta executando `iniciar-servidor.bat`. Talvez seja necessario liberar a porta 5000 no Firewall do Windows.

## Banco de dados

O banco H2 e criado automaticamente em `data/chatdb.mv.db`. A estrutura da tabela esta em `src/main/resources/schema.sql`.

## Testes

O comando `compilar-e-testar.bat` executa os 8 testes unitarios. Eles tambem podem ser executados pelo Maven portatil:

```powershell
.\.tools\apache-maven-3.9.11\bin\mvn.cmd -Dmaven.repo.local=".m2repo" test
```

## Protocolo simples

Ao conectar, o cliente envia seu nome na primeira linha. Cada linha posterior e uma
mensagem. O servidor envia linhas formatadas como `[dd/MM/yyyy HH:mm:ss] Nome: texto`. Os comandos internos `/grupo Nome` e `/convidar Nome` sao enviados pela interface visual.

