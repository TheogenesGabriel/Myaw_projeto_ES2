# Myow — Sistema de Gestão de Clínica Veterinária

Aplicação desktop desenvolvida em **Java 17** e **JavaFX**, com persistência em **SQLite via JDBC**, para a disciplina de **Engenharia de Software II**.

## Sobre o projeto

O Myow é um sistema para gestão de clínicas veterinárias. A aplicação permite cadastrar tutores e animais, agendar consultas, registrar atendimentos, consultar prontuários, controlar estoque, registrar pagamentos, gerar relatórios e gerenciar funcionários.

O sistema funciona localmente, sem depender de navegador, serviços em nuvem ou APIs externas.

## Arquitetura

O projeto é dividido em camadas:

```text
JavaFX
   ↓
Services
   ↓
DAOs
   ↓
SQLite / JDBC
```

* **`model`**: entidades do sistema.
* **`view`**: interfaces gráficas em JavaFX.
* **`service`**: regras de negócio e controle de acesso.
* **`dao`**: acesso ao banco de dados.
* **`database`**: gerenciamento da conexão e inicialização do banco.

## Banco de Dados

O banco utilizado é o **SQLite**, armazenado localmente em:

```text
database/myow.db
```

O banco possui 8 tabelas:

* `usuarios`
* `tutores`
* `pacientes`
* `consultas`
* `itens_estoque`
* `atendimentos`
* `atendimento_itens`
* `faturamentos`

O projeto também mantém os scripts `schema.sql` e `seed.sql` na pasta `database`.

## Funcionalidades

O sistema contempla os seguintes casos de uso:

| Código | Caso de Uso                       |
| ------ | --------------------------------- |
| UC01   | Realizar Login                    |
| UC02   | Cadastrar Tutor                   |
| UC03   | Cadastrar Paciente                |
| UC04   | Agendar Consulta                  |
| UC05   | Alterar Consulta                  |
| UC06   | Cancelar Consulta                 |
| UC07   | Registrar Atendimento Médico      |
| UC08   | Consultar Histórico Clínico       |
| UC09   | Gerenciar Estoque e Medicamentos  |
| UC10   | Registrar Pagamento e Faturamento |
| UC11   | Gerar Relatórios                  |
| UC12   | Gerenciar Funcionários            |

Entre as regras implementadas estão validações de cadastro, conflito de horários, controle de estoque, baixa de medicamentos durante o atendimento e controle de acesso por perfil.

## Perfis de acesso

O sistema possui três perfis:

* **Administrador**: acesso aos módulos administrativos e demais funcionalidades.
* **Veterinário**: acesso às funcionalidades relacionadas ao atendimento clínico.
* **Funcionário**: acesso às funcionalidades de recepção, cadastro, agendamento, estoque e faturamento, conforme as permissões definidas pelo sistema.

O controle de acesso é aplicado tanto na interface quanto na camada de serviços.

## Como executar

### Pré-requisitos

* **JDK 17 ou superior**
* **Maven 3.8 ou superior**

Verifique as instalações:

```bash
java -version
mvn -version
```

### 1. Compilar

Na pasta raiz do projeto:

```bash
mvn clean compile
```

O resultado esperado é:

```text
BUILD SUCCESS
```

### 2. Executar os testes

```bash
mvn test
```

O projeto possui **10 testes automatizados**, cobrindo banco de dados, autenticação, recuperação de senha, cadastros, agendamento, estoque, relatórios, RBAC, sessão e geração de IDs.

### 3. Executar a aplicação

```bash
mvn javafx:run
```

## Credenciais para demonstração

| Perfil        | Login          | Senha      |
| ------------- | -------------- | ---------- |
| Administrador | `admin`        | `admin123` |
| Veterinário   | `camila.vet`   | `123456`   |
| Funcionário   | `bia.recepcao` | `123456`   |

Para o fluxo de recuperação de senha, a resposta cadastrada para a pergunta de segurança é:

```text
myow
```
