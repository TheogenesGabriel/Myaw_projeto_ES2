# 🐾 Myow — Sistema de Gestão de Clínica Veterinária

Aplicação Desktop desenvolvida em **Java 17** e **JavaFX**, com persistência em **Banco de Dados Relacional SQLite (via JDBC)**, concebida para a disciplina de **Engenharia de Software II**.

---

## 📌 Visão Geral do Sistema

O **Myow** é uma solução desktop completa para informatização e gestão de clínicas veterinárias, cobrindo o ciclo de atendimento desde a recepção, cadastro de tutores e animais, agendamento de consultas, atendimento clínico com consumo de medicamentos, controle rigoroso de estoque e faturamento no caixa, além de inteligência operacional com relatórios exportáveis.

A aplicação opera de forma **100% local e offline**, não dependendo de navegadores, serviços em nuvem, Node.js ou APIs externas.

---

## 🏛️ Arquitetura do Software

O projeto adota uma arquitetura em camadas bem definida, promovendo alta coesão, baixo acoplamento e separação estrita de responsabilidades:

```
┌─────────────────────────────────────────────────────────────┐
│                    CAMADA DE APRESENTAÇÃO                   │
│          Interface Gráfica Desktop em JavaFX 17             │
│   (LoginView, DashboardView, TutoresView, PacientesView...) │
└──────────────────────────────┬──────────────────────────────┘
                               │ Chamadas de Ação
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                      CAMADA DE SERVIÇO                      │
│                  Regras de Negócio e RBAC                   │
│   (AuthService, TutorService, PacienteService, Estoque...)  │
└──────────────────────────────┬──────────────────────────────┘
                               │ Operações CRUD / Consultas
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                 CAMADA DE ACESSO A DADOS (DAO)              │
│                PreparedStatements & Transações              │
│  (UsuarioDAO, TutorDAO, PacienteDAO, ConsultaDAO, etc.)     │
└──────────────────────────────┬──────────────────────────────┘
                               │ JDBC (sqlite-jdbc)
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                    BANCO DE DADOS RELACIONAL                │
│                 SQLite Local (database/myow.db)             │
│        Chaves Estrangeiras, Índices, Constraints de Checagem│
└─────────────────────────────────────────────────────────────┘
```

### Principais Componentes:
- **`com.myow.model`**: Entidades de domínio (`Usuario`, `Tutor`, `Paciente`, `Consulta`, `Atendimento`, `ItemEstoque`, `Faturamento`).
- **`com.myow.database`**: `DatabaseManager` (Singleton) gerencia a conexão JDBC, criação automática das tabelas a partir de `schema.sql` e carga inicial `seed.sql`.
- **`com.myow.dao`**: Classes DAO com `PreparedStatement`, consultas parametrizadas, prevenção a SQL Injection e suporte a transações atômicas (`commit`/`rollback`).
- **`com.myow.service`**: Regras de negócio, cálculo de subtotais, validação de unicidade de CPF, bloqueio temporário após tentativas de login, validações de estoque e controle de acesso baseado em papéis (RBAC).
- **`com.myow.view`**: Interfaces gráficas interativas construídas puramente em JavaFX, com componentes visuais modernos, responsivos e acessíveis.

---

## 🗄️ Banco de Dados Relacional (SQLite)

O banco de dados relacional utiliza o arquivo local `database/myow.db`, estruturado com 8 tabelas normalizadas e integridade referencial estrita (`PRAGMA foreign_keys = ON;`):

1. **`usuarios`**: Gestão de colaboradores, senhas, papéis (RBAC), tentativas de login consecutivas e dados para recuperação de senha.
2. **`tutores`**: Cadastro de responsáveis por animais com validação de unicidade de CPF.
3. **`pacientes`**: Cadastro dos animais, com chave estrangeira para `tutores(id)` e dados clínicos prévios.
4. **`consultas`**: Agendamentos com verificação de conflito de agenda (médico veterinário x dia x horário).
5. **`itens_estoque`**: Controle de farmácia e materiais, lotes, validade e controle de nível mínimo.
6. **`atendimentos`**: Registro do prontuário veterinário (diagnóstico, procedimentos, prescrições) vinculado à consulta.
7. **`atendimento_itens`**: Medicamentos e insumos baixados automaticamente do estoque durante o atendimento.
8. **`faturamentos`**: Registros de recebimentos de caixa (dinheiro, cartão, PIX), cálculo de troco e emissão de comprovantes.

---

## 📋 Os 12 Casos de Uso Atendidos

| Caso de Uso | Descrição | Regras de Negócio e Segurança |
|---|---|---|
| **UC01 — Realizar Login** | Autenticação no sistema com perfil de acesso | Bloqueio temporário de 5 min após 3 tentativas incorretas; fluxo local "Esqueci minha senha" com pergunta de segurança. |
| **UC02 — Cadastrar Tutor** | Inclusão de tutores responsáveis | Validação obrigatória de dados e garantia de unicidade de CPF via banco e service. |
| **UC03 — Cadastrar Paciente** | Registro de animais de estimação | Vinculação com tutor existente e detecção de duplicidade (mesmo nome sob o mesmo tutor). |
| **UC04 — Agendar Consulta** | Marcação de consultas e retornos | Bloqueio automático de marcação em conflito de horário para o mesmo veterinário. |
| **UC05 — Cadastrar Item no Estoque** | Entrada de insumos e medicamentos | Controle de lote, validade, valor e alerta visual imediato de estoque baixo. |
| **UC06 — Atualizar Estoque** | Ajuste manual e conferência física | Registro de entradas/saídas com recálculo de saldo em tempo real. |
| **UC07 — Iniciar Consulta** | Acolhimento do paciente na recepção | Transição do estado da consulta de `AGENDADA` para `AGUARDANDO_ATENDIMENTO`. |
| **UC08 — Registrar Atendimento Clínico** | Consulta veterinária completa | Diagnóstico, procedimentos, prescrições e baixa automática dos insumos/remédios no estoque. |
| **UC09 — Consultar Prontuário** | Histórico clínico cronológico | Visualização de todos os atendimentos passados de cada animal com filtro de busca. |
| **UC10 — Registrar Faturamento** | Operação de caixa e recebimento | Suporte a Dinheiro, Cartão e PIX, conferência de valor recebido, troco e quitação do atendimento. |
| **UC11 — Gerar Relatórios** | Inteligência gerencial e exportação | Filtros por período, KPIs de ticket médio e **exportação em PDF (OpenPDF) e Excel (Apache POI)**. |
| **UC12 — Gerenciar Funcionários** | Gestão de equipe e perfis | Exclusivo para administradores; cadastro, edição e inativação segura de contas. |

---

## 🔐 Controle de Acesso Baseado em Perfis (RBAC)

O controle de acesso é aplicado de forma dupla: na interface gráfica e diretamente na camada de serviços:

* **ADMINISTRADOR**: Acesso irrestrito a todos os módulos, incluindo relatórios estratégicos e gestão de funcionários.
* **VETERINARIO**: Acesso restrito ao atendimento clínico, emissão de prescrições, prontuários de pacientes e consulta de insumos médicos. Bloqueado para faturamento e gestão de colaboradores.
* **FUNCIONARIO (Recepção/Caixa)**: Gestão de tutores, pacientes, agendamentos, estoque de suprimentos e recebimentos de caixa. Bloqueado para registrar atendimentos clínicos, prontuários restritos e gerenciar colaboradores.

---

## 🚀 Como Compilar e Executar

### Pré-requisitos
- **Java JDK 17+** instalado e configurado nas variáveis de ambiente (`JAVA_HOME`).
- **Apache Maven 3.8+**.

### 1. Compilar o Projeto e Executar os Testes Automatizados
```bash
mvn clean test
```
*Todos os 8 testes de integração (banco SQLite, login, bloqueio, recuperação de senha, agendamentos, estoque, relatórios e RBAC) serão executados com sucesso.*

### 2. Executar a Aplicação Desktop JavaFX
```bash
mvn javafx:run
```
Ou compile o pacote JAR executável:
```bash
mvn clean package
java -jar target/myow-clinica-veterinaria-1.0.0.jar
```

---

## 🔑 Credenciais Padrão de Demonstração (SQLite)

O banco de dados é inicializado automaticamente com os seguintes usuários para testes:

| Perfil | Login | Senha Padrão | Colaborador |
|---|---|---|---|
| **ADMINISTRADOR** | `admin` | `admin123` | Lucas Nogueira (Admin Geral) |
| **VETERINÁRIO** | `camila.vet` | `123456` | Dra. Camila Silveira |
| **FUNCIONÁRIO** | `bia.recepcao` | `123456` | Beatriz Costa (Recepcionista) |

*Dica de Recuperação de Senha:* A resposta padrão para a pergunta de segurança (*"Qual o nome da clínica?"*) é **`myow`**.
