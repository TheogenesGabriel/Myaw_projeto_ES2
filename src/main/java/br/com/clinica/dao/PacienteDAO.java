package br.com.clinica.dao;

import br.com.clinica.database.DatabaseConnection;
import br.com.clinica.model.Paciente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PacienteDAO {

    public void cadastrar(Paciente paciente) throws SQLException {
        String sql = "INSERT INTO pacientes (tutor_id, nome, especie, raca, idade, sexo, historico_medico) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, paciente.getTutorId());
            stmt.setString(2, paciente.getNome());
            stmt.setString(3, paciente.getEspecie());
            stmt.setString(4, paciente.getRaca());
            stmt.setInt(5, paciente.getIdade());
            stmt.setString(6, paciente.getSexo());
            stmt.setString(7, paciente.getHistoricoMedico());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    paciente.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public List<Paciente> listarTodos() throws SQLException {
        List<Paciente> lista = new ArrayList<>();
        String sql = """
            SELECT p.*, t.nome as tutor_nome 
            FROM pacientes p
            JOIN tutores t ON p.tutor_id = t.id
            ORDER BY p.nome ASC
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Paciente p = new Paciente(
                        rs.getInt("id"),
                        rs.getInt("tutor_id"),
                        rs.getString("nome"),
                        rs.getString("especie"),
                        rs.getString("raca"),
                        rs.getInt("idade"),
                        rs.getString("sexo"),
                        rs.getString("historico_medico")
                );
                p.setTutorNome(rs.getString("tutor_nome"));
                lista.add(p);
            }
        }
        return lista;
    }

    public List<Paciente> listarPorTutor(int tutorId) throws SQLException {
        List<Paciente> lista = new ArrayList<>();
        String sql = "SELECT * FROM pacientes WHERE tutor_id = ? ORDER BY nome ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tutorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Paciente p = new Paciente(
                            rs.getInt("id"),
                            rs.getInt("tutor_id"),
                            rs.getString("nome"),
                            rs.getString("especie"),
                            rs.getString("raca"),
                            rs.getInt("idade"),
                            rs.getString("sexo"),
                            rs.getString("historico_medico")
                    );
                    lista.add(p);
                }
            }
        }
        return lista;
    }
}
