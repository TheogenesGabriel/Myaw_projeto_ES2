# Sistema de Gerenciamento de Clínica Veterinária (JavaFX + SQLite)

Aplicação desktop desenvolvida em **JavaFX 21** e persistência local com **SQLite**, implementando rigorosamente a especificação do projeto e os casos de uso solicitados.

---

## 📋 Casos de Uso Implementados

1. **Login do Sistema**: Autenticação restrita (`admin` / `admin`).
2. **UC02 - Cadastrar Tutor**: Cadastro completo de tutores (Nome, CPF com máscara/validação, Telefone, E-mail e Endereço).
3. **UC03 - Cadastrar Paciente (Animal)**: Registro do animal vinculado ao seu tutor (Nome, Espécie, Raça, Idade, Sexo e Histórico).
4. **UC04 - Agendar Consulta**: Seleção do paciente, médico veterinário, data e horário com validação de conflitos.
5. **UC05 - Alterar Consulta**: Reagendamento de data, horário ou profissional de consultas agendadas.
6. **UC06 - Cancelar Consulta**: Cancelamento com registro obrigatório de justificativa e liberação da vaga.
7. **UC07 – Registrar Atendimento Médico**: Preenchimento do prontuário eletrônico (anamnese, sinais vitais, diagnóstico clínico, procedimentos realizados e prescrição medicamentosa).

---

## 🛠️ Tecnologias Utilizadas

- **Linguagem**: Java 17+ (ou Java 21)
- **Interface Gráfica**: JavaFX (FXML + CSS personalizado com paleta escura `#012f27` e acentos `#00c49f`)
- **Persistência**: SQLite 3 com driver JDBC oficial (`sqlite-jdbc`)
- **Gerenciador de Dependências**: Apache Maven

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
- **Java JDK 17 ou superior** instalado e configurado no `PATH` (`JAVA_HOME`).
- **Apache Maven** instalado (`mvn -version`).

### 1. Executando via Linha de Comando (Maven)
Abra o terminal na pasta raiz do projeto (`javafx-clinica`) e execute:

```bash
mvn clean compile javafx:run
```

O Maven baixará automaticamente o JavaFX e o driver do SQLite, criará o banco de dados `clinica_vet.db` automaticamente caso não exista e abrirá a tela de Login do sistema!

---

### 2. Abrindo no IntelliJ IDEA
1. Abra o IntelliJ IDEA.
2. Selecione **File > Open** e escolha a pasta `javafx-clinica`.
3. O IntelliJ detectará o `pom.xml` e carregará as dependências do Maven automaticamente.
4. Na aba lateral do Maven, navegue em: `Plugins > javafx > javafx:run` e dê dois cliques, ou execute diretamente a classe `br.com.clinica.MainApp`.

---

### 3. Abrindo no Eclipse ou NetBeans
1. No Eclipse: **File > Import > Existing Maven Projects** e selecione a pasta.
2. Clique com o botão direito no projeto > **Run As > Maven build...** e informe o goal: `javafx:run`.

---

## 🔐 Credenciais de Acesso
- **Usuário**: `admin`
- **Senha**: `admin`
