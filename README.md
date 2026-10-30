# Chat Multiusuario com Historico

Projeto de Programacao Orientada a Objetos em Java: servidor TCP concorrente,
broadcast de mensagens e historico persistido com JDBC.

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

### 3. Iniciar os clientes

Abra tres outros terminais PowerShell na mesma pasta. Em cada um, execute:

```powershell
.\iniciar-cliente.bat
```

Informe um nome diferente em cada cliente. Envie uma mensagem em um deles e confirme que todos recebem a mensagem. Para sair de um cliente, digite `/sair`. Para encerrar o servidor, use `Ctrl + C` no terminal dele.

> No PowerShell, o prefixo `.\` e obrigatorio para executar arquivos da pasta atual.

## Banco de dados

O banco H2 e criado automaticamente em `data/chatdb.mv.db`. A estrutura da tabela esta em `src/main/resources/schema.sql`.

## Testes

O comando `compilar-e-testar.bat` executa os 8 testes unitarios. Eles tambem podem ser executados pelo Maven portatil:

```powershell
.\.tools\apache-maven-3.9.11\bin\mvn.cmd -Dmaven.repo.local=".m2repo" test
```

## Protocolo simples

Ao conectar, o cliente envia seu nome na primeira linha. Cada linha posterior e uma
mensagem. O servidor envia linhas formatadas como `[dd/MM/yyyy HH:mm:ss] Nome: texto`.

