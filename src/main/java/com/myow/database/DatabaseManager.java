package com.myow.database;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class DatabaseManager {
    private static final String DB_DIR = "database";
    private static final String DB_FILE = "database/myow.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;

    private static DatabaseManager instance;

    private DatabaseManager() {
        initDatabase();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    private void initDatabase() {
        try {
            File dir = new File(DB_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            try (Connection conn = getConnection()) {
                createTables(conn);
                seedInitialDataIfEmpty(conn);
            }
        } catch (Exception e) {
            System.err.println("Erro ao inicializar banco de dados: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables(Connection conn) throws SQLException {
        String schema = 
            "CREATE TABLE IF NOT EXISTS usuarios (" +
            "    id VARCHAR(50) PRIMARY KEY," +
            "    nome VARCHAR(100) NOT NULL," +
            "    cpf VARCHAR(20) UNIQUE NOT NULL," +
            "    cargo VARCHAR(100) NOT NULL," +
            "    perfil VARCHAR(30) NOT NULL CHECK(perfil IN ('ADMINISTRADOR', 'VETERINARIO', 'FUNCIONARIO'))," +
            "    login VARCHAR(50) UNIQUE NOT NULL," +
            "    senha VARCHAR(255) NOT NULL," +
            "    status VARCHAR(20) NOT NULL DEFAULT 'Ativo' CHECK(status IN ('Ativo', 'Inativo'))," +
            "    telefone VARCHAR(30)," +
            "    tentativas_login INT NOT NULL DEFAULT 0," +
            "    bloqueado_ate VARCHAR(40)," +
            "    pergunta_seguranca VARCHAR(200)," +
            "    resposta_seguranca VARCHAR(200)" +
            ");" +

            "CREATE TABLE IF NOT EXISTS tutores (" +
            "    id VARCHAR(50) PRIMARY KEY," +
            "    nome_completo VARCHAR(150) NOT NULL," +
            "    cpf VARCHAR(20) UNIQUE NOT NULL," +
            "    telefone VARCHAR(30) NOT NULL," +
            "    email VARCHAR(100)," +
            "    endereco VARCHAR(200) NOT NULL," +
            "    data_cadastro VARCHAR(30) NOT NULL" +
            ");" +

            "CREATE TABLE IF NOT EXISTS pacientes (" +
            "    id VARCHAR(50) PRIMARY KEY," +
            "    nome VARCHAR(100) NOT NULL," +
            "    especie VARCHAR(50) NOT NULL," +
            "    raca VARCHAR(50) NOT NULL," +
            "    idade INT NOT NULL CHECK(idade >= 0)," +
            "    sexo VARCHAR(20) NOT NULL CHECK(sexo IN ('Macho', 'Fêmea'))," +
            "    tutor_id VARCHAR(50) NOT NULL," +
            "    historico_medico TEXT," +
            "    data_cadastro VARCHAR(30) NOT NULL," +
            "    FOREIGN KEY (tutor_id) REFERENCES tutores(id) ON DELETE RESTRICT" +
            ");" +

            "CREATE TABLE IF NOT EXISTS consultas (" +
            "    id VARCHAR(50) PRIMARY KEY," +
            "    data VARCHAR(20) NOT NULL," +
            "    horario VARCHAR(10) NOT NULL," +
            "    paciente_id VARCHAR(50) NOT NULL," +
            "    tutor_id VARCHAR(50) NOT NULL," +
            "    veterinario_id VARCHAR(50) NOT NULL," +
            "    status VARCHAR(30) NOT NULL CHECK(status IN ('AGENDADA', 'AGUARDANDO_ATENDIMENTO', 'EM_ANDAMENTO', 'REALIZADA', 'CANCELADA'))," +
            "    motivo TEXT NOT NULL," +
            "    observacoes TEXT," +
            "    data_agendamento VARCHAR(30) NOT NULL," +
            "    FOREIGN KEY (paciente_id) REFERENCES pacientes(id) ON DELETE RESTRICT," +
            "    FOREIGN KEY (tutor_id) REFERENCES tutores(id) ON DELETE RESTRICT," +
            "    FOREIGN KEY (veterinario_id) REFERENCES usuarios(id) ON DELETE RESTRICT" +
            ");" +

            "CREATE TABLE IF NOT EXISTS itens_estoque (" +
            "    id VARCHAR(50) PRIMARY KEY," +
            "    nome VARCHAR(150) NOT NULL," +
            "    lote VARCHAR(50) NOT NULL," +
            "    categoria VARCHAR(50) NOT NULL," +
            "    quantidade INT NOT NULL CHECK(quantidade >= 0)," +
            "    estoque_minimo INT NOT NULL CHECK(estoque_minimo >= 0)," +
            "    validade VARCHAR(20) NOT NULL," +
            "    valor_unitario REAL NOT NULL CHECK(valor_unitario >= 0)," +
            "    unidade VARCHAR(20) NOT NULL," +
            "    UNIQUE(nome, lote)" +
            ");" +

            "CREATE TABLE IF NOT EXISTS atendimentos (" +
            "    id VARCHAR(50) PRIMARY KEY," +
            "    consulta_id VARCHAR(50) UNIQUE NOT NULL," +
            "    paciente_id VARCHAR(50) NOT NULL," +
            "    tutor_id VARCHAR(50) NOT NULL," +
            "    veterinario_id VARCHAR(50) NOT NULL," +
            "    data VARCHAR(20) NOT NULL," +
            "    hora VARCHAR(10) NOT NULL," +
            "    diagnostico TEXT NOT NULL," +
            "    procedimentos TEXT NOT NULL," +
            "    prescricoes TEXT," +
            "    observacoes_clinicas TEXT," +
            "    valor_procedimentos REAL NOT NULL DEFAULT 0.0 CHECK(valor_procedimentos >= 0)," +
            "    valor_total REAL NOT NULL DEFAULT 0.0 CHECK(valor_total >= 0)," +
            "    status VARCHAR(30) NOT NULL DEFAULT 'Concluído' CHECK(status IN ('Concluído', 'Faturado'))," +
            "    FOREIGN KEY (consulta_id) REFERENCES consultas(id) ON DELETE RESTRICT," +
            "    FOREIGN KEY (paciente_id) REFERENCES pacientes(id) ON DELETE RESTRICT," +
            "    FOREIGN KEY (tutor_id) REFERENCES tutores(id) ON DELETE RESTRICT," +
            "    FOREIGN KEY (veterinario_id) REFERENCES usuarios(id) ON DELETE RESTRICT" +
            ");" +

            "CREATE TABLE IF NOT EXISTS atendimento_itens (" +
            "    id VARCHAR(50) PRIMARY KEY," +
            "    atendimento_id VARCHAR(50) NOT NULL," +
            "    item_estoque_id VARCHAR(50) NOT NULL," +
            "    nome_item VARCHAR(150) NOT NULL," +
            "    quantidade INT NOT NULL CHECK(quantidade > 0)," +
            "    valor_unitario REAL NOT NULL CHECK(valor_unitario >= 0)," +
            "    subtotal REAL NOT NULL CHECK(subtotal >= 0)," +
            "    FOREIGN KEY (atendimento_id) REFERENCES atendimentos(id) ON DELETE CASCADE," +
            "    FOREIGN KEY (item_estoque_id) REFERENCES itens_estoque(id) ON DELETE RESTRICT" +
            ");" +

            "CREATE TABLE IF NOT EXISTS faturamentos (" +
            "    id VARCHAR(50) PRIMARY KEY," +
            "    atendimento_id VARCHAR(50) UNIQUE NOT NULL," +
            "    consulta_id VARCHAR(50) NOT NULL," +
            "    paciente_id VARCHAR(50) NOT NULL," +
            "    tutor_id VARCHAR(50) NOT NULL," +
            "    valor_total REAL NOT NULL CHECK(valor_total >= 0)," +
            "    forma_pagamento VARCHAR(30) NOT NULL CHECK(forma_pagamento IN ('DINHEIRO', 'CARTAO', 'PIX'))," +
            "    valor_recebido REAL," +
            "    troco REAL," +
            "    data_pagamento VARCHAR(20) NOT NULL," +
            "    hora_pagamento VARCHAR(10) NOT NULL," +
            "    operador_id VARCHAR(50) NOT NULL," +
            "    FOREIGN KEY (atendimento_id) REFERENCES atendimentos(id) ON DELETE RESTRICT," +
            "    FOREIGN KEY (consulta_id) REFERENCES consultas(id) ON DELETE RESTRICT," +
            "    FOREIGN KEY (paciente_id) REFERENCES pacientes(id) ON DELETE RESTRICT," +
            "    FOREIGN KEY (tutor_id) REFERENCES tutores(id) ON DELETE RESTRICT," +
            "    FOREIGN KEY (operador_id) REFERENCES usuarios(id) ON DELETE RESTRICT" +
            ");";

        try (Statement stmt = conn.createStatement()) {
            for (String sql : schema.split(";")) {
                if (!sql.trim().isEmpty()) {
                    stmt.execute(sql.trim());
                }
            }
        }
    }

    private void seedInitialDataIfEmpty(Connection conn) throws SQLException {
        try (Statement checkStmt = conn.createStatement();
             ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) FROM usuarios")) {
            if (rs.next() && rs.getInt(1) > 0) {
                return; // Já possui dados cadastrados
            }
        }

        // Carrega dados padrão
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT OR IGNORE INTO usuarios (id, nome, cpf, cargo, perfil, login, senha, status, telefone, tentativas_login, bloqueado_ate, pergunta_seguranca, resposta_seguranca) VALUES " +
                "('usr_1', 'Dra. Camila Silveira', '111.222.333-44', 'Médica Veterinária Chefe', 'VETERINARIO', 'camila.vet', '123456', 'Ativo', '(11) 98765-4321', 0, NULL, 'Qual o nome da clínica?', 'myow'), " +
                "('usr_2', 'Lucas Nogueira', '222.333.444-55', 'Administrador do Sistema', 'ADMINISTRADOR', 'admin', 'admin123', 'Ativo', '(11) 97654-3210', 0, NULL, 'Qual o nome da clínica?', 'myow'), " +
                "('usr_3', 'Beatriz Costa', '333.444.555-66', 'Recepcionista', 'FUNCIONARIO', 'bia.recepcao', '123456', 'Ativo', '(11) 96543-2109', 0, NULL, 'Qual o nome da clínica?', 'myow'), " +
                "('usr_4', 'Dr. Roberto Martins', '444.555.666-77', 'Médico Veterinário Cirurgião', 'VETERINARIO', 'roberto.vet', '123456', 'Ativo', '(11) 95432-1098', 0, NULL, 'Qual o nome da clínica?', 'myow');");

            stmt.execute("INSERT OR IGNORE INTO tutores (id, nome_completo, cpf, telefone, email, endereco, data_cadastro) VALUES " +
                "('tut_1', 'Mariana Souza', '123.456.789-00', '(11) 99887-1122', 'mariana.souza@email.com', 'Rua das Flores, 120 - SP', '2025-01-10'), " +
                "('tut_2', 'Carlos Eduardo Lima', '234.567.890-11', '(11) 98765-2233', 'carlos.lima@email.com', 'Av. Paulista, 1500 - SP', '2025-01-15'), " +
                "('tut_3', 'Fernanda Rocha', '345.678.901-22', '(11) 97654-3344', 'fernanda.rocha@email.com', 'Rua Augusta, 450 - SP', '2025-02-01');");

            stmt.execute("INSERT OR IGNORE INTO pacientes (id, nome, especie, raca, idade, sexo, tutor_id, historico_medico, data_cadastro) VALUES " +
                "('pac_1', 'Thor', 'Canina', 'Golden Retriever', 4, 'Macho', 'tut_1', 'Vacinação V10 em dia. Histórico de dermatite atópica sazonal.', '2025-01-10'), " +
                "('pac_2', 'Luna', 'Felina', 'Siamês', 2, 'Fêmea', 'tut_1', 'Castrada. Vermifugação recente em janeiro/2025.', '2025-01-12'), " +
                "('pac_3', 'Bob', 'Canina', 'Bulldog Francês', 5, 'Macho', 'tut_2', 'Histórico de sensibilidade respiratória.', '2025-01-15'), " +
                "('pac_4', 'Mel', 'Felina', 'Persa', 3, 'Fêmea', 'tut_3', 'Sem histórico de alergias conhecidas. Vacinas completas.', '2025-02-01');");

            stmt.execute("INSERT OR IGNORE INTO itens_estoque (id, nome, lote, categoria, quantidade, estoque_minimo, validade, valor_unitario, unidade) VALUES " +
                "('est_1', 'Amoxicilina + Clavulanato 250mg', 'LOT-2025-A1', 'Medicamento', 18, 5, '2026-12-31', 45.00, 'caixa'), " +
                "('est_2', 'Dipirona Gotas 500mg/ml', 'LOT-2025-D2', 'Medicamento', 4, 6, '2026-08-15', 18.50, 'frasco'), " +
                "('est_3', 'Vacina Nobivac V10', 'LOT-2025-V3', 'Vacina', 12, 5, '2025-11-20', 75.00, 'dose'), " +
                "('est_4', 'Meloxicam 0,5% Injetável', 'LOT-2025-M4', 'Medicamento', 7, 3, '2026-05-10', 62.00, 'frasco'), " +
                "('est_5', 'Seringa Descartável 3ml c/ Agulha', 'LOT-2025-S5', 'Material', 150, 30, '2028-01-01', 1.20, 'unidade'), " +
                "('est_6', 'Luva Cirúrgica Estéril 7.5', 'LOT-2025-L6', 'Material', 50, 15, '2027-04-30', 3.50, 'par');");

            stmt.execute("INSERT OR IGNORE INTO consultas (id, data, horario, paciente_id, tutor_id, veterinario_id, status, motivo, observacoes, data_agendamento) VALUES " +
                "('cons_1', '2026-09-15', '09:00', 'pac_1', 'tut_1', 'usr_1', 'REALIZADA', 'Consulta de rotina e aplicação de vacina', 'Paciente calmo.', '2026-09-15'), " +
                "('cons_2', '2026-09-15', '10:30', 'pac_3', 'tut_2', 'usr_1', 'AGUARDANDO_ATENDIMENTO', 'Respiração ofegante e tosse seca', 'Prioridade intermediária.', '2026-09-15'), " +
                "('cons_3', '2026-09-15', '14:00', 'pac_2', 'tut_1', 'usr_4', 'AGENDADA', 'Avaliação odontológica profilática', '', '2026-09-15'), " +
                "('cons_4', '2026-09-15', '15:30', 'pac_4', 'tut_3', 'usr_1', 'AGENDADA', 'Vermifugação e check-up semestral', '', '2026-09-15');");

            stmt.execute("INSERT OR IGNORE INTO atendimentos (id, consulta_id, paciente_id, tutor_id, veterinario_id, data, hora, diagnostico, procedimentos, prescricoes, observacoes_clinicas, valor_procedimentos, valor_total, status) VALUES " +
                "('atend_1', 'cons_1', 'pac_1', 'tut_1', 'usr_1', '2026-09-15', '09:40', 'Exame clínico satisfatório. Mucosas normocoradas.', 'Exame físico e aplicação de Vacina V10.', 'Repouso relativo nas próximas 24h.', 'Tutor orientado sobre calendário anual.', 120.00, 196.20, 'Faturado');");

            stmt.execute("INSERT OR IGNORE INTO atendimento_itens (id, atendimento_id, item_estoque_id, nome_item, quantidade, valor_unitario, subtotal) VALUES " +
                "('ait_1', 'atend_1', 'est_3', 'Vacina Nobivac V10', 1, 75.00, 75.00), " +
                "('ait_2', 'atend_1', 'est_5', 'Seringa Descartável 3ml c/ Agulha', 1, 1.20, 1.20);");

            stmt.execute("INSERT OR IGNORE INTO faturamentos (id, atendimento_id, consulta_id, paciente_id, tutor_id, valor_total, forma_pagamento, valor_recebido, troco, data_pagamento, hora_pagamento, operador_id) VALUES " +
                "('fat_1', 'atend_1', 'cons_1', 'pac_1', 'tut_1', 196.20, 'CARTAO', NULL, NULL, '2026-09-15', '09:50', 'usr_3');");
        }
    }
}
