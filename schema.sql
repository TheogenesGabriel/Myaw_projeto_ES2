-- Script de Criação do Banco de Dados SQLite (clinica_vet.db)

CREATE TABLE IF NOT EXISTS tutores (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    cpf TEXT UNIQUE NOT NULL,
    telefone TEXT NOT NULL,
    email TEXT NOT NULL,
    endereco TEXT NOT NULL,
    data_cadastro DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pacientes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tutor_id INTEGER NOT NULL,
    nome TEXT NOT NULL,
    especie TEXT NOT NULL,
    raca TEXT NOT NULL,
    idade INTEGER NOT NULL,
    sexo TEXT NOT NULL,
    historico_medico TEXT,
    data_cadastro DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tutor_id) REFERENCES tutores(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS consultas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    paciente_id INTEGER NOT NULL,
    veterinario TEXT NOT NULL,
    data_consulta TEXT NOT NULL,
    horario TEXT NOT NULL,
    motivo TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'AGENDADA', -- AGENDADA, REALIZADA, CANCELADA
    observacoes TEXT,
    motivo_cancelamento TEXT,
    data_agendamento DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (paciente_id) REFERENCES pacientes(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS atendimentos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    consulta_id INTEGER UNIQUE NOT NULL,
    peso_kg REAL,
    temperatura_c REAL,
    frequencia_cardiaca INTEGER,
    diagnostico TEXT NOT NULL,
    procedimentos_realizados TEXT NOT NULL,
    prescricao_medicamentosa TEXT NOT NULL,
    recomendacoes TEXT,
    data_atendimento DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (consulta_id) REFERENCES consultas(id) ON DELETE CASCADE
);

-- Inserção de Dados Iniciais de Teste
INSERT OR IGNORE INTO tutores (id, nome, cpf, telefone, email, endereco) VALUES
(1, 'Carlos Silva', '123.456.789-00', '(11) 98765-4321', 'carlos.silva@email.com', 'Rua das Flores, 120 - São Paulo/SP'),
(2, 'Mariana Souza', '987.654.321-11', '(11) 97654-3210', 'mariana.souza@email.com', 'Av. Paulista, 1500 - São Paulo/SP');

INSERT OR IGNORE INTO pacientes (id, tutor_id, nome, especie, raca, idade, sexo, historico_medico) VALUES
(1, 1, 'Thor', 'Canina', 'Golden Retriever', 4, 'Macho', 'Alergia a pulgas e picadas de inseto. Vacinação V10 em dia.'),
(2, 2, 'Luna', 'Felina', 'Siamês', 2, 'Fêmea', 'Castrada aos 6 meses. Sem histórico de doenças crônicas.');

INSERT OR IGNORE INTO consultas (id, paciente_id, veterinario, data_consulta, horario, motivo, status, observacoes) VALUES
(1, 1, 'Dra. Camila Torres (CRMV-SP 18.442)', '2026-09-20', '09:30', 'Check-up de rotina e aplicação de reforço da vacina antirrábica', 'AGENDADA', 'Paciente dócil'),
(2, 2, 'Dr. Lucas Mendes (CRMV-SP 22.810)', '2026-09-21', '14:00', 'Avaliação de coceira persistente na orelha direita', 'AGENDADA', 'Fazer otoscopia');
