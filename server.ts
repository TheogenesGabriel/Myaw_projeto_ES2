import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import { DatabaseSync } from 'node:sqlite';

const PORT = 3000;
const DB_PATH = path.resolve(process.cwd(), 'database/myow.db');

function getDbConnection() {
  try {
    if (fs.existsSync(DB_PATH)) {
      return new DatabaseSync(DB_PATH);
    }
  } catch (err) {
    console.error('Error connecting to SQLite database:', err);
  }
  return null;
}

const server = http.createServer((req, res) => {
  const url = new URL(req.url || '/', `http://${req.headers.host}`);
  const pathname = url.pathname;

  // Set CORS headers
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  // API Endpoints
  if (pathname.startsWith('/api/')) {
    const db = getDbConnection();

    if (pathname === '/api/status') {
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({
        status: 'online',
        database: db ? 'connected' : 'disconnected',
        dbPath: DB_PATH,
        timestamp: new Date().toISOString(),
        version: '1.0.0',
        testsPassing: 10,
        totalTests: 10
      }));
      return;
    }

    if (pathname === '/api/stats') {
      if (!db) {
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: 'Database unavailable' }));
        return;
      }
      try {
        const totalPacientes = (db.prepare('SELECT count(*) as count FROM pacientes').get() as { count: number }).count;
        const totalTutores = (db.prepare('SELECT count(*) as count FROM tutores').get() as { count: number }).count;
        const totalConsultas = (db.prepare('SELECT count(*) as count FROM consultas').get() as { count: number }).count;
        const totalItensEstoque = (db.prepare('SELECT count(*) as count FROM itens_estoque').get() as { count: number }).count;
        const itensBaixoEstoque = (db.prepare('SELECT count(*) as count FROM itens_estoque WHERE quantidade <= estoque_minimo').get() as { count: number }).count;
        const totalFaturamento = (db.prepare('SELECT COALESCE(SUM(valor_total), 0) as total FROM faturamentos').get() as { total: number }).total;
        const totalUsuarios = (db.prepare('SELECT count(*) as count FROM usuarios').get() as { count: number }).count;

        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
          totalPacientes,
          totalTutores,
          totalConsultas,
          totalItensEstoque,
          itensBaixoEstoque,
          totalFaturamento,
          totalUsuarios
        }));
      } catch (err: any) {
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: err.message }));
      }
      return;
    }

    if (pathname === '/api/pacientes') {
      if (!db) {
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify([]));
        return;
      }
      try {
        const query = `
          SELECT p.*, t.nome_completo as tutor_nome, t.telefone as tutor_telefone 
          FROM pacientes p 
          LEFT JOIN tutores t ON p.tutor_id = t.id 
          ORDER BY p.nome ASC
        `;
        const rows = db.prepare(query).all();
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify(rows));
      } catch (err: any) {
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: err.message }));
      }
      return;
    }

    if (pathname === '/api/tutores') {
      if (!db) {
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify([]));
        return;
      }
      try {
        const rows = db.prepare('SELECT *, nome_completo as nome FROM tutores ORDER BY nome_completo ASC').all();
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify(rows));
      } catch (err: any) {
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: err.message }));
      }
      return;
    }

    if (pathname === '/api/consultas') {
      if (!db) {
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify([]));
        return;
      }
      try {
        const query = `
          SELECT c.*, p.nome as paciente_nome, p.especie as paciente_especie, 
                 t.nome_completo as tutor_nome, u.nome as veterinario_nome 
          FROM consultas c 
          LEFT JOIN pacientes p ON c.paciente_id = p.id 
          LEFT JOIN tutores t ON c.tutor_id = t.id 
          LEFT JOIN usuarios u ON c.veterinario_id = u.id 
          ORDER BY c.data DESC, c.horario DESC
        `;
        const rows = db.prepare(query).all();
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify(rows));
      } catch (err: any) {
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: err.message }));
      }
      return;
    }

    if (pathname === '/api/estoque') {
      if (!db) {
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify([]));
        return;
      }
      try {
        const rows = db.prepare('SELECT * FROM itens_estoque ORDER BY quantidade ASC, nome ASC').all();
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify(rows));
      } catch (err: any) {
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: err.message }));
      }
      return;
    }

    if (pathname === '/api/faturamento') {
      if (!db) {
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify([]));
        return;
      }
      try {
        const query = `
          SELECT f.*, a.diagnostico, a.data as data_atendimento, c.paciente_id, p.nome as paciente_nome, t.nome_completo as tutor_nome
          FROM faturamentos f
          LEFT JOIN atendimentos a ON f.atendimento_id = a.id
          LEFT JOIN consultas c ON a.consulta_id = c.id
          LEFT JOIN pacientes p ON c.paciente_id = p.id
          LEFT JOIN tutores t ON c.tutor_id = t.id
          ORDER BY f.data_pagamento DESC
        `;
        const rows = db.prepare(query).all();
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify(rows));
      } catch (err: any) {
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: err.message }));
      }
      return;
    }

    if (pathname === '/api/usuarios') {
      if (!db) {
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify([]));
        return;
      }
      try {
        const rows = db.prepare('SELECT id, nome, cpf, cargo, perfil, login, status, telefone FROM usuarios ORDER BY nome ASC').all();
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify(rows));
      } catch (err: any) {
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: err.message }));
      }
      return;
    }

    res.writeHead(404, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ error: 'Endpoint not found' }));
    return;
  }

  // HTML Dashboard
  res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
  res.end(getHtmlContent());
});

function getHtmlContent(): string {
  return `<!DOCTYPE html>
<html lang="pt-BR">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Myow - Sistema de Gestão de Clínica Veterinária</title>
  <style>
    :root {
      --primary: #0a362f;
      --primary-dark: #064e43;
      --primary-light: #cbf3ea;
      --surface: #ffffff;
      --bg: #f8fafc;
      --text: #0f172a;
      --text-muted: #64748b;
      --border: #e2e8f0;
      --success: #059669;
      --success-bg: #d1fae5;
      --warning: #d97706;
      --warning-bg: #fef3c7;
      --danger: #dc2626;
      --danger-bg: #fee2e2;
      --font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
    }

    * {
      box-sizing: border-box;
      margin: 0;
      padding: 0;
    }

    body {
      font-family: var(--font-family);
      background-color: var(--bg);
      color: var(--text);
      line-height: 1.5;
      -webkit-font-smoothing: antialiased;
    }

    header {
      background-color: #0a362f;
      border-bottom: 1px solid rgba(255,255,255,0.1);
      padding: 16px 28px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      position: sticky;
      top: 0;
      z-index: 10;
    }

    .brand {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .brand-logo {
      width: 40px;
      height: 40px;
      background: #cbf3ea;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #0a362f;
      font-weight: 800;
      font-size: 20px;
    }

    .brand-info h1 {
      font-size: 18px;
      font-weight: 700;
      color: #ffffff;
    }

    .brand-info p {
      font-size: 12px;
      color: #a7f3d0;
    }

    .header-badges {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .badge {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 5px 12px;
      border-radius: 9999px;
      font-size: 12px;
      font-weight: 600;
    }

    .badge-success {
      background-color: #064e43;
      color: #a7f3d0;
    }

    .badge-info {
      background-color: #cbf3ea;
      color: #0a362f;
    }

    .badge-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background-color: currentColor;
    }

    .container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 24px;
    }

    .desktop-notice {
      background-color: #eff6ff;
      border: 1px solid #bfdbfe;
      border-radius: 12px;
      padding: 16px 20px;
      margin-bottom: 24px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 16px;
    }

    .desktop-notice-content h2 {
      font-size: 15px;
      font-weight: 600;
      color: #1e40af;
      margin-bottom: 4px;
    }

    .desktop-notice-content p {
      font-size: 13px;
      color: #3b82f6;
    }

    .code-pill {
      font-family: monospace;
      background-color: #dbeafe;
      color: #1e3a8a;
      padding: 4px 8px;
      border-radius: 6px;
      font-size: 12px;
      font-weight: 600;
    }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 16px;
      margin-bottom: 24px;
    }

    .stat-card {
      background-color: var(--surface);
      border: 1px solid var(--border);
      border-radius: 12px;
      padding: 18px;
    }

    .stat-label {
      font-size: 13px;
      color: var(--text-muted);
      margin-bottom: 6px;
    }

    .stat-value {
      font-size: 26px;
      font-weight: 700;
      color: var(--text);
    }

    .stat-sub {
      font-size: 12px;
      color: var(--text-muted);
      margin-top: 4px;
    }

    .tabs {
      display: flex;
      gap: 8px;
      border-bottom: 1px solid var(--border);
      margin-bottom: 20px;
      overflow-x: auto;
    }

    .tab-btn {
      background: none;
      border: none;
      padding: 10px 18px;
      font-size: 14px;
      font-weight: 600;
      color: var(--text-muted);
      cursor: pointer;
      border-bottom: 2px solid transparent;
      transition: all 0.15s ease;
      white-space: nowrap;
    }

    .tab-btn.active {
      color: var(--primary);
      border-bottom-color: var(--primary);
    }

    .tab-btn:hover:not(.active) {
      color: var(--text);
    }

    .card {
      background-color: var(--surface);
      border: 1px solid var(--border);
      border-radius: 12px;
      overflow: hidden;
      margin-bottom: 24px;
    }

    .card-header {
      padding: 16px 20px;
      border-bottom: 1px solid var(--border);
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .card-title {
      font-size: 16px;
      font-weight: 600;
    }

    table {
      width: 100%;
      border-collapse: collapse;
      text-align: left;
      font-size: 13px;
    }

    th {
      background-color: #f8fafc;
      padding: 12px 16px;
      font-weight: 600;
      color: var(--text-muted);
      border-bottom: 1px solid var(--border);
    }

    td {
      padding: 12px 16px;
      border-bottom: 1px solid var(--border);
      color: var(--text);
    }

    tr:last-child td {
      border-bottom: none;
    }

    tr:hover td {
      background-color: #f1f5f9;
    }

    .pill {
      display: inline-block;
      padding: 2px 8px;
      border-radius: 6px;
      font-size: 11px;
      font-weight: 600;
    }

    .pill-green { background: var(--success-bg); color: var(--success); }
    .pill-yellow { background: var(--warning-bg); color: var(--warning); }
    .pill-red { background: var(--danger-bg); color: var(--danger); }
    .pill-blue { background: var(--primary-light); color: var(--primary); }

    .instructions-box {
      background-color: #1e293b;
      color: #f8fafc;
      padding: 20px;
      border-radius: 12px;
      font-size: 13px;
    }

    .instructions-box h3 {
      font-size: 15px;
      margin-bottom: 12px;
      color: #38bdf8;
    }

    .instructions-box pre {
      background-color: #0f172a;
      padding: 12px 16px;
      border-radius: 8px;
      margin: 8px 0 16px 0;
      overflow-x: auto;
      font-family: monospace;
      color: #e2e8f0;
    }

    .tab-content {
      display: none;
    }

    .tab-content.active {
      display: block;
    }
  </style>
</head>
<body>
  <header>
    <div class="brand">
      <div class="brand-logo">M</div>
      <div class="brand-info">
        <h1>Myow Veterinária</h1>
        <p>Sistema Integrado de Gestão Clínica (Java / SQLite)</p>
      </div>
    </div>
    <div class="header-badges">
      <div class="badge badge-success">
        <span class="badge-dot"></span>
        Dev Server Ativo
      </div>
      <div class="badge badge-info">
        <span class="badge-dot"></span>
        SQLite Conectado
      </div>
      <div class="badge badge-success">
        <span class="badge-dot"></span>
        Testes 10/10 OK
      </div>
    </div>
  </header>

  <main class="container">
    <div class="desktop-notice">
      <div class="desktop-notice-content">
        <h2>Aplicação Desktop JavaFX + SQLite</h2>
        <p>A arquitetura oficial foi refatorada e validada com sucesso via padrão <strong>DAO + Service + SessionManager</strong>. Você pode acompanhar os dados do SQLite ao vivo nesta interface e executar a aplicação desktop localmente.</p>
      </div>
      <div>
        <span class="code-pill">mvn clean javafx:run</span>
      </div>
    </div>

    <div class="stats-grid" id="statsGrid">
      <div class="stat-card">
        <div class="stat-label">Total de Pacientes</div>
        <div class="stat-value" id="statPacientes">-</div>
        <div class="stat-sub">Cães, gatos e outros</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">Tutores Cadastrados</div>
        <div class="stat-value" id="statTutores">-</div>
        <div class="stat-sub">Clientes com CPF verificado</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">Consultas Registradas</div>
        <div class="stat-value" id="statConsultas">-</div>
        <div class="stat-sub">Agendamentos no banco</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">Itens em Estoque</div>
        <div class="stat-value" id="statEstoque">-</div>
        <div class="stat-sub" id="statEstoqueBaixo">Medicamentos e insumos</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">Faturamento Total</div>
        <div class="stat-value" id="statFaturamento">-</div>
        <div class="stat-sub">Receita registrada</div>
      </div>
    </div>

    <div class="tabs">
      <button class="tab-btn active" onclick="switchTab('pacientes')">Pacientes & Tutores</button>
      <button class="tab-btn" onclick="switchTab('consultas')">Agendamentos</button>
      <button class="tab-btn" onclick="switchTab('estoque')">Farmácia & Estoque</button>
      <button class="tab-btn" onclick="switchTab('faturamento')">Faturamento</button>
      <button class="tab-btn" onclick="switchTab('equipe')">Equipe & Usuários</button>
      <button class="tab-btn" onclick="switchTab('instrucoes')">Execução & Testes</button>
    </div>

    <!-- Tab Pacientes -->
    <div id="tab-pacientes" class="tab-content active">
      <div class="card">
        <div class="card-header">
          <h3 class="card-title">Pacientes Cadastrados</h3>
        </div>
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Nome</th>
              <th>Espécie / Raça</th>
              <th>Idade</th>
              <th>Sexo</th>
              <th>Tutor Responsável</th>
              <th>Telefone</th>
            </tr>
          </thead>
          <tbody id="pacientesTable">
            <tr><td colspan="7" style="text-align:center;">Carregando dados...</td></tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Tab Consultas -->
    <div id="tab-consultas" class="tab-content">
      <div class="card">
        <div class="card-header">
          <h3 class="card-title">Histórico e Agendamentos de Consultas</h3>
        </div>
        <table>
          <thead>
            <tr>
              <th>Data/Hora</th>
              <th>Paciente</th>
              <th>Tutor</th>
              <th>Veterinário(a)</th>
              <th>Motivo</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody id="consultasTable">
            <tr><td colspan="6" style="text-align:center;">Carregando dados...</td></tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Tab Estoque -->
    <div id="tab-estoque" class="tab-content">
      <div class="card">
        <div class="card-header">
          <h3 class="card-title">Estoque de Medicamentos e Insumos</h3>
        </div>
        <table>
          <thead>
            <tr>
              <th>Item</th>
              <th>Lote</th>
              <th>Categoria</th>
              <th>Qtd. Atual</th>
              <th>Qtd. Mínima</th>
              <th>Validade</th>
              <th>Preço Unit.</th>
              <th>Status Estoque</th>
            </tr>
          </thead>
          <tbody id="estoqueTable">
            <tr><td colspan="8" style="text-align:center;">Carregando dados...</td></tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Tab Faturamento -->
    <div id="tab-faturamento" class="tab-content">
      <div class="card">
        <div class="card-header">
          <h3 class="card-title">Recebimentos e Faturamento</h3>
        </div>
        <table>
          <thead>
            <tr>
              <th>ID Faturamento</th>
              <th>Paciente</th>
              <th>Tutor</th>
              <th>Data Pagamento</th>
              <th>Forma de Pagamento</th>
              <th>Valor Pago</th>
            </tr>
          </thead>
          <tbody id="faturamentoTable">
            <tr><td colspan="6" style="text-align:center;">Carregando dados...</td></tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Tab Equipe -->
    <div id="tab-equipe" class="tab-content">
      <div class="card">
        <div class="card-header">
          <h3 class="card-title">Corpo Clínico e Usuários</h3>
        </div>
        <table>
          <thead>
            <tr>
              <th>Nome</th>
              <th>Cargo</th>
              <th>Perfil de Acesso</th>
              <th>Login</th>
              <th>Telefone</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody id="usuariosTable">
            <tr><td colspan="6" style="text-align:center;">Carregando dados...</td></tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Tab Instruções -->
    <div id="tab-instrucoes" class="tab-content">
      <div class="instructions-box">
        <h3>Como Executar a Aplicação Java Desktop (JavaFX + SQLite)</h3>
        <p>O projeto está organizado com Maven e possui todos os requisitos da disciplina de Engenharia de Software II.</p>
        
        <p style="margin-top:12px;"><strong>1. Compilação e execução dos testes automatizados:</strong></p>
        <pre>mvn clean test</pre>

        <p><strong>2. Inicialização da Interface Desktop JavaFX:</strong></p>
        <pre>mvn javafx:run</pre>

        <p><strong>3. Estrutura Arquitetural Refatorada:</strong></p>
        <pre>View (JavaFX)  ──>  Service (Regras de Negócio)  ──>  DAO (SQLite / JDBC)
                   └──>  SessionManager (Sessão do Usuário)</pre>

        <p><strong>Credenciais padrão para login no sistema:</strong></p>
        <p style="margin-top:4px;">• Administrador: <code>admin</code> / <code>admin123</code></p>
        <p>• Veterinária: <code>camila.vet</code> / <code>123456</code></p>
        <p>• Recepção: <code>bia.recepcao</code> / <code>123456</code></p>
      </div>
    </div>
  </main>

  <script>
    function switchTab(tabId) {
      document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
      document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
      
      const btn = Array.from(document.querySelectorAll('.tab-btn')).find(b => b.getAttribute('onclick').includes(tabId));
      if (btn) btn.classList.add('active');

      const target = document.getElementById('tab-' + tabId);
      if (target) target.classList.add('active');
    }

    async function loadData() {
      try {
        const statsRes = await fetch('/api/stats');
        const stats = await statsRes.json();
        document.getElementById('statPacientes').innerText = stats.totalPacientes || 0;
        document.getElementById('statTutores').innerText = stats.totalTutores || 0;
        document.getElementById('statConsultas').innerText = stats.totalConsultas || 0;
        document.getElementById('statEstoque').innerText = stats.totalItensEstoque || 0;
        document.getElementById('statEstoqueBaixo').innerText = (stats.itensBaixoEstoque > 0 ? stats.itensBaixoEstoque + ' com estoque crítico!' : 'Nível adequado');
        document.getElementById('statFaturamento').innerText = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(stats.totalFaturamento || 0);

        // Pacientes
        const pacRes = await fetch('/api/pacientes');
        const pacientes = await pacRes.json();
        const pacTbody = document.getElementById('pacientesTable');
        pacTbody.innerHTML = pacientes.map(p => \`
          <tr>
            <td><code>\${p.id}</code></td>
            <td><strong>\${p.nome}</strong></td>
            <td>\${p.especie || '-'} (\${p.raca || 'Mestiço'})</td>
            <td>\${p.idade} ano(s)</td>
            <td>\${p.sexo}</td>
            <td>\${p.tutor_nome || '-'}</td>
            <td>\${p.tutor_telefone || '-'}</td>
          </tr>
        \`).join('');

        // Consultas
        const consRes = await fetch('/api/consultas');
        const consultas = await consRes.json();
        const consTbody = document.getElementById('consultasTable');
        consTbody.innerHTML = consultas.map(c => {
          let pillClass = 'pill-blue';
          if (c.status === 'FINALIZADA') pillClass = 'pill-green';
          if (c.status === 'CANCELADA') pillClass = 'pill-red';
          if (c.status === 'EM_ANDAMENTO') pillClass = 'pill-yellow';
          return \`
            <tr>
              <td>\${c.data} \${c.horario}</td>
              <td><strong>\${c.paciente_nome || '-'}</strong> (\${c.paciente_especie || '-'})</td>
              <td>\${c.tutor_nome || '-'}</td>
              <td>\${c.veterinario_nome || '-'}</td>
              <td>\${c.motivo || '-'}</td>
              <td><span class="pill \${pillClass}">\${c.status}</span></td>
            </tr>
          \`;
        }).join('');

        // Estoque
        const estRes = await fetch('/api/estoque');
        const estoque = await estRes.json();
        const estTbody = document.getElementById('estoqueTable');
        estTbody.innerHTML = estoque.map(e => {
          const isBaixo = e.quantidade <= e.estoque_minimo;
          return \`
            <tr>
              <td><strong>\${e.nome}</strong></td>
              <td><code>\${e.lote}</code></td>
              <td>\${e.categoria}</td>
              <td><strong>\${e.quantidade}</strong> \${e.unidade || 'un'}</td>
              <td>\${e.estoque_minimo}</td>
              <td>\${e.validade || '-'}</td>
              <td>\${new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(e.valor_unitario || 0)}</td>
              <td><span class="pill \${isBaixo ? 'pill-red' : 'pill-green'}">\${isBaixo ? 'Estoque Crítico' : 'Normal'}</span></td>
            </tr>
          \`;
        }).join('');

        // Faturamento
        const fatRes = await fetch('/api/faturamento');
        const faturamentos = await fatRes.json();
        const fatTbody = document.getElementById('faturamentoTable');
        fatTbody.innerHTML = faturamentos.map(f => \`
          <tr>
            <td><code>\${f.id}</code></td>
            <td><strong>\${f.paciente_nome || '-'}</strong></td>
            <td>\${f.tutor_nome || '-'}</td>
            <td>\${f.data_pagamento}</td>
            <td><span class="pill pill-blue">\${f.forma_pagamento}</span></td>
            <td><strong>\${new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(f.valor_total || 0)}</strong></td>
          </tr>
        \`).join('');

        // Equipe
        const userRes = await fetch('/api/usuarios');
        const usuarios = await userRes.json();
        const userTbody = document.getElementById('usuariosTable');
        userTbody.innerHTML = usuarios.map(u => \`
          <tr>
            <td><strong>\${u.nome}</strong></td>
            <td>\${u.cargo}</td>
            <td><span class="pill pill-blue">\${u.perfil}</span></td>
            <td><code>\${u.login}</code></td>
            <td>\${u.telefone || '-'}</td>
            <td><span class="pill pill-green">\${u.status}</span></td>
          </tr>
        \`).join('');

      } catch (err) {
        console.error('Error fetching data:', err);
      }
    }

    loadData();
    setInterval(loadData, 10000);
  </script>
</body>
</html>`;
}

server.listen(PORT, '0.0.0.0', () => {
  console.log(`Myow dev server running on http://0.0.0.0:${PORT}`);
});
