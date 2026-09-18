package br.com.clinica.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gerenciador de conexão JDBC com o banco de dados SQLite local.
 * Cria o banco e as tabelas automaticamente na primeira execução.
 */
public class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:clinica_vet.db";
    private static Connection connection = null;

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                System.err.println("Driver SQLite JDBC não encontrado: " + e.getMessage());
            }
            connection = DriverManager.getConnection(URL);
            initDatabase();
        }
        return connection;
    }

    private static void initDatabase() {
        try (Statement stmt = connection.createStatement()) {
            // Tabela de Tutores (UC02)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS tutores (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome TEXT NOT NULL,
                    cpf TEXT UNIQUE NOT NULL,
                    telefone TEXT NOT NULL,
                    email TEXT NOT NULL,
                    endereco TEXT NOT NULL,
                    data_cadastro DATETIME DEFAULT CURRENT_TIMESTAMP
                );
            """);

            // Tabela de Pacientes/Pets (UC03)
            stmt.execute("""
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
            """);

            // Tabela de Consultas (UC04, UC05, UC06)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS consultas (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    paciente_id INTEGER NOT NULL,
                    veterinario TEXT NOT NULL,
                    data_consulta TEXT NOT NULL,
                    horario TEXT NOT NULL,
                    motivo TEXT NOT NULL,
                    status TEXT NOT NULL DEFAULT 'AGENDADA',
                    observacoes TEXT,
                    motivo_cancelamento TEXT,
                    data_agendamento DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (paciente_id) REFERENCES pacientes(id) ON DELETE CASCADE
                );
            """);

            // Tabela de Atendimentos Clínicos / Prontuário (UC07)
            stmt.execute("""
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
            """);

            // Inserir dados padrão para teste caso a tabela de tutores esteja vazia
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM tutores");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("""
                    INSERT INTO tutores (nome, cpf, telefone, email, endereco) VALUES
                    ('Carlos Silva', '123.456.789-00', '(11) 98765-4321', 'carlos.silva@email.com', 'Rua das Flores, 120 - SP'),
                    ('Mariana Souza', '987.654.321-11', '(11) 97654-3210', 'mariana.souza@email.com', 'Av. Paulista, 1500 - SP');
                """);

                stmt.execute("""
                    INSERT INTO pacientes (tutor_id, nome, especie, raca, idade, sexo, historico_medico) VALUES
                    (1, 'Thor', 'Canina', 'Golden Retriever', 4, 'Macho', 'Alergia a picadas. Vacinação V10 atualizada.'),
                    (2, 'Luna', 'Felina', 'Siamês', 2, 'Fêmea', 'Castrada. Sem histórico de doenças crônicas.');
                """);

                stmt.execute("""
                    INSERT INTO consultas (paciente_id, veterinario, data_consulta, horario, motivo, status, observacoes) VALUES
                    (1, 'Dra. Camila Torres (CRMV-SP 18.442)', '2026-09-20', '09:30', 'Check-up de rotina e reforço vacinal', 'AGENDADA', 'Cão dócil'),
                    (2, 'Dr. Lucas Mendes (CRMV-SP 22.810)', '2026-09-21', '14:00', 'Avaliação de coceira persistente na orelha', 'AGENDADA', 'Verificar conduto auditivo');
                """);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao inicializar banco de dados SQLite: " + e.getMessage());
        }
    }
}
