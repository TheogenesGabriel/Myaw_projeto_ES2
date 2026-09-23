-- ==========================================================
-- MYOW - SISTEMA DE GESTÃO DE CLÍNICA VETERINÁRIA
-- Carga Inicial de Dados de Demonstração (Seed)
-- Arquivo: /database/seed.sql
-- ==========================================================

-- Usuários padrão do sistema (Senha inicial: 123456 / admin: admin123)
-- Pergunta de segurança padrão: "Qual o nome da clínica?" -> "myow"
INSERT OR IGNORE INTO usuarios (id, nome, cpf, cargo, perfil, login, senha, status, telefone, tentativas_login, bloqueado_ate, pergunta_seguranca, resposta_seguranca)
VALUES
('usr_1', 'Dra. Camila Silveira', '111.222.333-44', 'Médica Veterinária Chefe', 'VETERINARIO', 'camila.vet', '123456', 'Ativo', '(11) 98765-4321', 0, NULL, 'Qual o nome da clínica?', 'myow'),
('usr_2', 'Lucas Nogueira', '222.333.444-55', 'Administrador do Sistema', 'ADMINISTRADOR', 'admin', 'admin123', 'Ativo', '(11) 97654-3210', 0, NULL, 'Qual o nome da clínica?', 'myow'),
('usr_3', 'Beatriz Costa', '333.444.555-66', 'Recepcionista', 'FUNCIONARIO', 'bia.recepcao', '123456', 'Ativo', '(11) 96543-2109', 0, NULL, 'Qual o nome da clínica?', 'myow'),
('usr_4', 'Dr. Roberto Martins', '444.555.666-77', 'Médico Veterinário Cirurgião', 'VETERINARIO', 'roberto.vet', '123456', 'Ativo', '(11) 95432-1098', 0, NULL, 'Qual o nome da clínica?', 'myow');

-- Tutores iniciais
INSERT OR IGNORE INTO tutores (id, nome_completo, cpf, telefone, email, endereco, data_cadastro)
VALUES
('tut_1', 'Mariana Souza', '123.456.789-00', '(11) 99887-1122', 'mariana.souza@email.com', 'Rua das Flores, 120 - SP', '2025-01-10'),
('tut_2', 'Carlos Eduardo Lima', '234.567.890-11', '(11) 98765-2233', 'carlos.lima@email.com', 'Av. Paulista, 1500 - SP', '2025-01-15'),
('tut_3', 'Fernanda Rocha', '345.678.901-22', '(11) 97654-3344', 'fernanda.rocha@email.com', 'Rua Augusta, 450 - SP', '2025-02-01');

-- Pacientes iniciais
INSERT OR IGNORE INTO pacientes (id, nome, especie, raca, idade, sexo, tutor_id, historico_medico, data_cadastro)
VALUES
('pac_1', 'Thor', 'Canina', 'Golden Retriever', 4, 'Macho', 'tut_1', 'Vacinação V10 em dia. Histórico de dermatite atópica sazonal.', '2025-01-10'),
('pac_2', 'Luna', 'Felina', 'Siamês', 2, 'Fêmea', 'tut_1', 'Castrada. Vermifugação recente em janeiro/2025.', '2025-01-12'),
('pac_3', 'Bob', 'Canina', 'Bulldog Francês', 5, 'Macho', 'tut_2', 'Histórico de sensibilidade respiratória (síndrome braquicefálica).', '2025-01-15'),
('pac_4', 'Mel', 'Felina', 'Persa', 3, 'Fêmea', 'tut_3', 'Sem histórico de alergias conhecidas. Vacinas completas.', '2025-02-01');

-- Estoque e Farmácia inicial
INSERT OR IGNORE INTO itens_estoque (id, nome, lote, categoria, quantidade, estoque_minimo, validade, valor_unitario, unidade)
VALUES
('est_1', 'Amoxicilina + Clavulanato 250mg', 'LOT-2025-A1', 'Medicamento', 18, 5, '2026-12-31', 45.00, 'caixa'),
('est_2', 'Dipirona Gotas 500mg/ml', 'LOT-2025-D2', 'Medicamento', 4, 6, '2026-08-15', 18.50, 'frasco'),
('est_3', 'Vacina Nobivac V10', 'LOT-2025-V3', 'Vacina', 12, 5, '2025-11-20', 75.00, 'dose'),
('est_4', 'Meloxicam 0,5% Injetável', 'LOT-2025-M4', 'Medicamento', 7, 3, '2026-05-10', 62.00, 'frasco'),
('est_5', 'Seringa Descartável 3ml c/ Agulha', 'LOT-2025-S5', 'Material', 150, 30, '2028-01-01', 1.20, 'unidade'),
('est_6', 'Luva Cirúrgica Estéril 7.5', 'LOT-2025-L6', 'Material', 50, 15, '2027-04-30', 3.50, 'par');

-- Consultas iniciais (data atual 2026-09-15)
INSERT OR IGNORE INTO consultas (id, data, horario, paciente_id, tutor_id, veterinario_id, status, motivo, observacoes, data_agendamento)
VALUES
('cons_1', '2026-09-15', '09:00', 'pac_1', 'tut_1', 'usr_1', 'REALIZADA', 'Consulta de rotina e aplicação de vacina', 'Paciente calmo e colaborativo.', '2026-09-15'),
('cons_2', '2026-09-15', '10:30', 'pac_3', 'tut_2', 'usr_1', 'AGUARDANDO_ATENDIMENTO', 'Respiração ofegante e tosse seca', 'Prioridade intermediária.', '2026-09-15'),
('cons_3', '2026-09-15', '14:00', 'pac_2', 'tut_1', 'usr_4', 'AGENDADA', 'Avaliação odontológica profilática', '', '2026-09-15'),
('cons_4', '2026-09-15', '15:30', 'pac_4', 'tut_3', 'usr_1', 'AGENDADA', 'Vermifugação e check-up semestral', '', '2026-09-15');

-- Atendimento realizado para cons_1
INSERT OR IGNORE INTO atendimentos (id, consulta_id, paciente_id, tutor_id, veterinario_id, data, hora, diagnostico, procedimentos, prescricoes, observacoes_clinicas, valor_procedimentos, valor_total, status)
VALUES
('atend_1', 'cons_1', 'pac_1', 'tut_1', 'usr_1', '2026-09-15', '09:40',
 'Exame clínico satisfatório. Mucosas normocoradas, batimentos e temperatura normais.',
 'Exame físico completo, ausculta cardiopulmonar e aplicação de Vacina V10 subcutânea.',
 'Repouso relativo nas próximas 24h. Monitorar eventual inchaço local.',
 'Tutor orientado quanto ao calendário anual de reforço vacinal.',
 120.00, 196.20, 'Faturado');

-- Itens consumidos no atendimento atend_1
INSERT OR IGNORE INTO atendimento_itens (id, atendimento_id, item_estoque_id, nome_item, quantidade, valor_unitario, subtotal)
VALUES
('ait_1', 'atend_1', 'est_3', 'Vacina Nobivac V10', 1, 75.00, 75.00),
('ait_2', 'atend_1', 'est_5', 'Seringa Descartável 3ml c/ Agulha', 1, 1.20, 1.20);

-- Faturamento registrado para atend_1
INSERT OR IGNORE INTO faturamentos (id, atendimento_id, consulta_id, paciente_id, tutor_id, valor_total, forma_pagamento, valor_recebido, troco, data_pagamento, hora_pagamento, operador_id)
VALUES
('fat_1', 'atend_1', 'cons_1', 'pac_1', 'tut_1', 196.20, 'CARTAO', NULL, NULL, '2026-09-15', '09:50', 'usr_3');
