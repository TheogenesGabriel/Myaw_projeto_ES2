# Myaw - Sistema Veterinário

Projeto desenvolvido para a disciplina de Engenharia de Software 2 da Universidade Federal do Vale do São Francisco (Univasf).

## 📋 Sobre o projeto

O **Myow** é um sistema desktop para clínicas veterinárias, com interface gráfica feita em **JavaFX**. Atualmente o sistema permite:

- **Cadastro de Tutores**: nome completo, CPF, telefone, e-mail e endereço.
- **Cadastro de Pacientes (pets)**: nome, espécie, raça, idade, sexo e histórico médico, com vínculo a um tutor.

O projeto está em fase inicial de desenvolvimento: os cadastros já validam campos obrigatórios e exibem mensagens de sucesso/erro na interface, mas a persistência em banco de dados ainda não foi implementada (os dados são apenas criados em memória).

## 🛠️ Tecnologias

- **Java 11**
- **JavaFX 13** (interface gráfica)
- **Maven** (gerenciador de dependências e build)

## 📁 Estrutura do projeto

```
sistema-myow/
├── pom.xml                          # Configuração do Maven
└── src/main/
    ├── java/com/myow/
    │   ├── App.java                 # Ponto de entrada da aplicação
    │   ├── Tutor.java               # Modelo do Tutor
    │   ├── Paciente.java            # Modelo do Paciente (pet)
    │   ├── TutorController.java     # Lógica da tela de cadastro de tutor
    │   ├── PacienteController.java  # Lógica da tela de cadastro de paciente
    │   ├── PrimaryController.java
    │   └── SecondaryController.java
    └── resources/com/myow/
        ├── TelaCadastroTutor.fxml
        ├── TelaCadastroPaciente.fxml
        ├── primary.fxml / secondary.fxml
        └── global1-style.css
```

## ▶️ Como rodar o projeto na sua máquina

### Pré-requisitos

- **JDK 11** instalado
- **Maven** instalado e configurado no `PATH`

Verifique se estão prontos:
```bash
java -version
mvn -version
```

### Passo a passo

1. Clone o repositório:
   ```bash
   git clone https://github.com/TheogenesGabriel/Myaw_projeto_ES2.git
   cd Myaw_projeto_ES2/sistema-myow
   ```

2. Rode a aplicação com o plugin do JavaFX:
   ```bash
   mvn clean javafx:run
   ```

Isso vai compilar o projeto e abrir a janela do sistema (tela de cadastro de tutor).

## 👥 Contexto acadêmico

Este projeto é um trabalho da disciplina de Engenharia de Software 2 (Univasf), com foco em aplicar conceitos de POO, levantamento de requisitos e casos de uso (ex.: UC02 - Cadastrar Tutor, UC03 - Cadastrar Paciente) no desenvolvimento de um sistema real.
