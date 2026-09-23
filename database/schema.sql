-- ==========================================================
-- MYOW - SISTEMA DE GESTÃO DE CLÍNICA VETERINÁRIA
-- Script de Criação do Banco de Dados Relacional (SQLite)
-- Arquivo: /database/schema.sql
-- ==========================================================

PRAGMA foreign_keys = ON;

-- 1. TABELA DE USUÁRIOS / FUNCIONÁRIOS
CREATE TABLE IF NOT EXISTS usuarios (
    id VARCHAR(50) PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cpf VARCHAR(20) UNIQUE NOT NULL,
    cargo VARCHAR(100) NOT NULL,
    perfil VARCHAR(30) NOT NULL CHECK(perfil IN ('ADMINISTRADOR', 'VETERINARIO', 'FUNCIONARIO')),
    login VARCHAR(50) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Ativo' CHECK(status IN ('Ativo', 'Inativo')),
    telefone VARCHAR(30),
    tentativas_login INT NOT NULL DEFAULT 0,
    bloqueado_ate VARCHAR(40),
    pergunta_seguranca VARCHAR(200),
    resposta_seguranca VARCHAR(200)
);

-- 2. TABELA DE TUTORES
CREATE TABLE IF NOT EXISTS tutores (
    id VARCHAR(50) PRIMARY KEY,
    nome_completo VARCHAR(150) NOT NULL,
    cpf VARCHAR(20) UNIQUE NOT NULL,
    telefone VARCHAR(30) NOT NULL,
    email VARCHAR(100),
    endereco VARCHAR(200) NOT NULL,
    data_cadastro VARCHAR(30) NOT NULL
);

-- 3. TABELA DE PACIENTES (ANIMAIS)
CREATE TABLE IF NOT EXISTS pacientes (
    id VARCHAR(50) PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    especie VARCHAR(50) NOT NULL,
    raca VARCHAR(50) NOT NULL,
    idade INT NOT NULL CHECK(idade >= 0),
    sexo VARCHAR(20) NOT NULL CHECK(sexo IN ('Macho', 'Fêmea')),
    tutor_id VARCHAR(50) NOT NULL,
    historico_medico TEXT,
    data_cadastro VARCHAR(30) NOT NULL,
    FOREIGN KEY (tutor_id) REFERENCES tutores(id) ON DELETE RESTRICT
);

-- 4. TABELA DE CONSULTAS / AGENDAMENTOS
CREATE TABLE IF NOT EXISTS consultas (
    id VARCHAR(50) PRIMARY KEY,
    data VARCHAR(20) NOT NULL,
    horario VARCHAR(10) NOT NULL,
    paciente_id VARCHAR(50) NOT NULL,
    tutor_id VARCHAR(50) NOT NULL,
    veterinario_id VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL CHECK(status IN ('AGENDADA', 'AGUARDANDO_ATENDIMENTO', 'EM_ANDAMENTO', 'REALIZADA', 'CANCELADA')),
    motivo TEXT NOT NULL,
    observacoes TEXT,
    data_agendamento VARCHAR(30) NOT NULL,
    FOREIGN KEY (paciente_id) REFERENCES pacientes(id) ON DELETE RESTRICT,
    FOREIGN KEY (tutor_id) REFERENCES tutores(id) ON DELETE RESTRICT,
    FOREIGN KEY (veterinario_id) REFERENCES usuarios(id) ON DELETE RESTRICT
);

-- 5. TABELA DE ITENS DE ESTOQUE / FARMÁCIA
CREATE TABLE IF NOT EXISTS itens_estoque (
    id VARCHAR(50) PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    lote VARCHAR(50) NOT NULL,
    categoria VARCHAR(50) NOT NULL,
    quantidade INT NOT NULL CHECK(quantidade >= 0),
    estoque_minimo INT NOT NULL CHECK(estoque_minimo >= 0),
    validade VARCHAR(20) NOT NULL,
    valor_unitario REAL NOT NULL CHECK(valor_unitario >= 0),
    unidade VARCHAR(20) NOT NULL,
    UNIQUE(nome, lote)
);

-- 6. TABELA DE ATENDIMENTOS CLÍNICOS
CREATE TABLE IF NOT EXISTS atendimentos (
    id VARCHAR(50) PRIMARY KEY,
    consulta_id VARCHAR(50) UNIQUE NOT NULL,
    paciente_id VARCHAR(50) NOT NULL,
    tutor_id VARCHAR(50) NOT NULL,
    veterinario_id VARCHAR(50) NOT NULL,
    data VARCHAR(20) NOT NULL,
    hora VARCHAR(10) NOT NULL,
    diagnostico TEXT NOT NULL,
    procedimentos TEXT NOT NULL,
    prescricoes TEXT,
    observacoes_clinicas TEXT,
    valor_procedimentos REAL NOT NULL DEFAULT 0.0 CHECK(valor_procedimentos >= 0),
    valor_total REAL NOT NULL DEFAULT 0.0 CHECK(valor_total >= 0),
    status VARCHAR(30) NOT NULL DEFAULT 'Concluído' CHECK(status IN ('Concluído', 'Faturado')),
    FOREIGN KEY (consulta_id) REFERENCES consultas(id) ON DELETE RESTRICT,
    FOREIGN KEY (paciente_id) REFERENCES pacientes(id) ON DELETE RESTRICT,
    FOREIGN KEY (tutor_id) REFERENCES tutores(id) ON DELETE RESTRICT,
    FOREIGN KEY (veterinario_id) REFERENCES usuarios(id) ON DELETE RESTRICT
);

-- 7. TABELA DE ITENS UTILIZADOS NO ATENDIMENTO (MEDICAMENTOS/INSUMOS)
CREATE TABLE IF NOT EXISTS atendimento_itens (
    id VARCHAR(50) PRIMARY KEY,
    atendimento_id VARCHAR(50) NOT NULL,
    item_estoque_id VARCHAR(50) NOT NULL,
    nome_item VARCHAR(150) NOT NULL,
    quantidade INT NOT NULL CHECK(quantidade > 0),
    valor_unitario REAL NOT NULL CHECK(valor_unitario >= 0),
    subtotal REAL NOT NULL CHECK(subtotal >= 0),
    FOREIGN KEY (atendimento_id) REFERENCES atendimentos(id) ON DELETE CASCADE,
    FOREIGN KEY (item_estoque_id) REFERENCES itens_estoque(id) ON DELETE RESTRICT
);

-- 8. TABELA DE FATURAMENTO / PAGAMENTOS
CREATE TABLE IF NOT EXISTS faturamentos (
    id VARCHAR(50) PRIMARY KEY,
    atendimento_id VARCHAR(50) UNIQUE NOT NULL,
    consulta_id VARCHAR(50) NOT NULL,
    paciente_id VARCHAR(50) NOT NULL,
    tutor_id VARCHAR(50) NOT NULL,
    valor_total REAL NOT NULL CHECK(valor_total >= 0),
    forma_pagamento VARCHAR(30) NOT NULL CHECK(forma_pagamento IN ('DINHEIRO', 'CARTAO', 'PIX')),
    valor_recebido REAL,
    troco REAL,
    data_pagamento VARCHAR(20) NOT NULL,
    hora_pagamento VARCHAR(10) NOT NULL,
    operador_id VARCHAR(50) NOT NULL,
    FOREIGN KEY (atendimento_id) REFERENCES atendimentos(id) ON DELETE RESTRICT,
    FOREIGN KEY (consulta_id) REFERENCES consultas(id) ON DELETE RESTRICT,
    FOREIGN KEY (paciente_id) REFERENCES pacientes(id) ON DELETE RESTRICT,
    FOREIGN KEY (tutor_id) REFERENCES tutores(id) ON DELETE RESTRICT,
    FOREIGN KEY (operador_id) REFERENCES usuarios(id) ON DELETE RESTRICT
);

-- ÍNDICES DE PERFORMANCE E INTEGRIDADE
CREATE INDEX IF NOT EXISTS idx_usuarios_login ON usuarios(login);
CREATE INDEX IF NOT EXISTS idx_tutores_cpf ON tutores(cpf);
CREATE INDEX IF NOT EXISTS idx_pacientes_tutor ON pacientes(tutor_id);
CREATE INDEX IF NOT EXISTS idx_consultas_data_vet ON consultas(data, veterinario_id);
CREATE INDEX IF NOT EXISTS idx_atendimentos_paciente ON atendimentos(paciente_id);
CREATE INDEX IF NOT EXISTS idx_faturamentos_data ON faturamentos(data_pagamento);
