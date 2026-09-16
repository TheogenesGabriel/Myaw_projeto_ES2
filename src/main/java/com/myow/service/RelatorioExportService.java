package com.myow.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.myow.model.Faturamento;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class RelatorioExportService {

    public OperationResult<File> exportarParaPdf(
            File arquivoDestino,
            String dataInicio,
            String dataFim,
            Map<String, Object> resumo,
            List<Faturamento> registros
    ) {
        try {
            if (arquivoDestino.getParentFile() != null && !arquivoDestino.getParentFile().exists()) {
                arquivoDestino.getParentFile().mkdirs();
            }

            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, new FileOutputStream(arquivoDestino));
            document.open();

            // Fontes
            Font fonteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(6, 78, 59));
            Font fonteSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 11, new Color(75, 85, 99));
            Font fonteSecao = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(31, 41, 55));
            Font fonteCabecalho = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font fonteDados = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(17, 24, 39));
            Font fonteDestaque = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(5, 150, 105));

            // Cabeçalho do Relatório
            Paragraph pTitulo = new Paragraph("Myow — Gestão de Clínica Veterinária", fonteTitulo);
            pTitulo.setAlignment(Element.ALIGN_CENTER);
            document.add(pTitulo);

            Paragraph pSub = new Paragraph("Demonstrativo Financeiro e Operacional de Faturamento", fonteSubtitulo);
            pSub.setAlignment(Element.ALIGN_CENTER);
            pSub.setSpacingAfter(15);
            document.add(pSub);

            // Informações do Período e Emissão
            String periodoTexto = "Período: " + 
                ((dataInicio != null && !dataInicio.isEmpty()) ? dataInicio : "Início") + 
                " até " + 
                ((dataFim != null && !dataFim.isEmpty()) ? dataFim : "Hoje");

            String emissaoTexto = "Gerado em: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            PdfPCell cellPeriodo = new PdfPCell(new Phrase(periodoTexto, fonteSubtitulo));
            cellPeriodo.setBorder(Rectangle.NO_BORDER);
            PdfPCell cellEmissao = new PdfPCell(new Phrase(emissaoTexto, fonteSubtitulo));
            cellEmissao.setBorder(Rectangle.NO_BORDER);
            cellEmissao.setHorizontalAlignment(Element.ALIGN_RIGHT);
            metaTable.addCell(cellPeriodo);
            metaTable.addCell(cellEmissao);
            metaTable.setSpacingAfter(15);
            document.add(metaTable);

            // Quadro Resumo Financeiro
            Paragraph pResumo = new Paragraph("1. Resumo dos Indicadores", fonteSecao);
            pResumo.setSpacingAfter(8);
            document.add(pResumo);

            PdfPTable kpiTable = new PdfPTable(4);
            kpiTable.setWidthPercentage(100);
            Color corHeaderKpi = new Color(5, 150, 105);

            adicionarCelulaHeader(kpiTable, "Faturamento Total", corHeaderKpi, fonteCabecalho);
            adicionarCelulaHeader(kpiTable, "Atendimentos", corHeaderKpi, fonteCabecalho);
            adicionarCelulaHeader(kpiTable, "Ticket Médio", corHeaderKpi, fonteCabecalho);
            adicionarCelulaHeader(kpiTable, "Distribuição Pgto", corHeaderKpi, fonteCabecalho);

            double total = (double) resumo.getOrDefault("total", 0.0);
            int qtd = (int) resumo.getOrDefault("quantidade", 0);
            double ticketMedio = (double) resumo.getOrDefault("ticketMedio", 0.0);
            double din = (double) resumo.getOrDefault("totalDinheiro", 0.0);
            double car = (double) resumo.getOrDefault("totalCartao", 0.0);
            double pix = (double) resumo.getOrDefault("totalPix", 0.0);

            adicionarCelulaValor(kpiTable, String.format("R$ %.2f", total), fonteDestaque);
            adicionarCelulaValor(kpiTable, String.valueOf(qtd), fonteDados);
            adicionarCelulaValor(kpiTable, String.format("R$ %.2f", ticketMedio), fonteDados);
            String dist = String.format("Din: R$ %.2f\nCart: R$ %.2f\nPix: R$ %.2f", din, car, pix);
            adicionarCelulaValor(kpiTable, dist, fonteDados);
            kpiTable.setSpacingAfter(20);
            document.add(kpiTable);

            // Tabela Detalhada de Faturamentos
            Paragraph pRegistros = new Paragraph("2. Registros de Faturamento no Período", fonteSecao);
            pRegistros.setSpacingAfter(8);
            document.add(pRegistros);

            PdfPTable table = new PdfPTable(new float[]{16f, 12f, 18f, 18f, 16f, 20f});
            table.setWidthPercentage(100);
            Color corHeader = new Color(31, 41, 55);

            adicionarCelulaHeader(table, "Data / Hora", corHeader, fonteCabecalho);
            adicionarCelulaHeader(table, "ID Atend.", corHeader, fonteCabecalho);
            adicionarCelulaHeader(table, "ID Paciente", corHeader, fonteCabecalho);
            adicionarCelulaHeader(table, "ID Tutor", corHeader, fonteCabecalho);
            adicionarCelulaHeader(table, "Forma Pgto", corHeader, fonteCabecalho);
            adicionarCelulaHeader(table, "Valor Total", corHeader, fonteCabecalho);

            boolean zebrado = false;
            Color corFundoZebrado = new Color(243, 244, 246);

            for (Faturamento f : registros) {
                Color fundo = zebrado ? corFundoZebrado : Color.WHITE;
                zebrado = !zebrado;

                adicionarLinhaTabela(table, f.getDataPagamento() + " " + f.getHoraPagamento(), fundo, fonteDados, Element.ALIGN_LEFT);
                adicionarLinhaTabela(table, f.getAtendimentoId(), fundo, fonteDados, Element.ALIGN_LEFT);
                adicionarLinhaTabela(table, f.getPacienteId(), fundo, fonteDados, Element.ALIGN_LEFT);
                adicionarLinhaTabela(table, f.getTutorId(), fundo, fonteDados, Element.ALIGN_LEFT);
                adicionarLinhaTabela(table, f.getFormaPagamento().name(), fundo, fonteDados, Element.ALIGN_CENTER);
                adicionarLinhaTabela(table, String.format("R$ %.2f", f.getValorTotal()), fundo, fonteDestaque, Element.ALIGN_RIGHT);
            }

            document.add(table);
            document.close();

            return OperationResult.ok("Relatório em PDF gerado com sucesso em:\n" + arquivoDestino.getAbsolutePath(), arquivoDestino);
        } catch (Exception e) {
            e.printStackTrace();
            return OperationResult.error("Falha ao exportar relatório em PDF: " + e.getMessage());
        }
    }

    public OperationResult<File> exportarParaExcel(
            File arquivoDestino,
            String dataInicio,
            String dataFim,
            Map<String, Object> resumo,
            List<Faturamento> registros
    ) {
        try (Workbook workbook = new XSSFWorkbook()) {
            if (arquivoDestino.getParentFile() != null && !arquivoDestino.getParentFile().exists()) {
                arquivoDestino.getParentFile().mkdirs();
            }

            // Estilos
            org.apache.poi.ss.usermodel.Font fonteBold = workbook.createFont();
            fonteBold.setBold(true);

            org.apache.poi.ss.usermodel.Font fonteHeader = workbook.createFont();
            fonteHeader.setBold(true);
            fonteHeader.setColor(IndexedColors.WHITE.getIndex());

            CellStyle estiloHeader = workbook.createCellStyle();
            estiloHeader.setFont(fonteHeader);
            estiloHeader.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
            estiloHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            estiloHeader.setAlignment(HorizontalAlignment.CENTER);

            CellStyle estiloMoeda = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            estiloMoeda.setDataFormat(format.getFormat("R$ #,##0.00"));

            CellStyle estiloHeaderResumo = workbook.createCellStyle();
            estiloHeaderResumo.setFont(fonteBold);

            // --- ABA 1: RESUMO FINANCEIRO ---
            Sheet abaResumo = workbook.createSheet("Resumo Geral");
            int r = 0;

            Row rowTitulo = abaResumo.createRow(r++);
            Cell cellTitulo = rowTitulo.createCell(0);
            cellTitulo.setCellValue("MYOW - RELATÓRIO FINANCEIRO E OPERACIONAL");
            cellTitulo.setCellStyle(estiloHeaderResumo);

            Row rowPeriodo = abaResumo.createRow(r++);
            rowPeriodo.createCell(0).setCellValue("Período Analisado: " + 
                    ((dataInicio != null && !dataInicio.isEmpty()) ? dataInicio : "Início") + " até " + 
                    ((dataFim != null && !dataFim.isEmpty()) ? dataFim : "Hoje"));

            Row rowEmissao = abaResumo.createRow(r++);
            rowEmissao.createCell(0).setCellValue("Data de Emissão: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            r++; // linha vazia

            adicionarLinhaResumoExcel(abaResumo, r++, "Faturamento Total (R$)", (double) resumo.getOrDefault("total", 0.0), estiloMoeda);
            adicionarLinhaResumoExcel(abaResumo, r++, "Quantidade de Atendimentos", (int) resumo.getOrDefault("quantidade", 0), null);
            adicionarLinhaResumoExcel(abaResumo, r++, "Ticket Médio (R$)", (double) resumo.getOrDefault("ticketMedio", 0.0), estiloMoeda);
            adicionarLinhaResumoExcel(abaResumo, r++, "Total em Dinheiro (R$)", (double) resumo.getOrDefault("totalDinheiro", 0.0), estiloMoeda);
            adicionarLinhaResumoExcel(abaResumo, r++, "Total em Cartão (R$)", (double) resumo.getOrDefault("totalCartao", 0.0), estiloMoeda);
            adicionarLinhaResumoExcel(abaResumo, r++, "Total em PIX (R$)", (double) resumo.getOrDefault("totalPix", 0.0), estiloMoeda);

            abaResumo.autoSizeColumn(0);
            abaResumo.autoSizeColumn(1);

            // --- ABA 2: REGISTROS DETALHADOS ---
            Sheet abaDetalhes = workbook.createSheet("Detalhamento Faturamento");
            String[] colunas = {"ID Faturamento", "Data", "Hora", "ID Atendimento", "ID Consulta", "ID Paciente", "ID Tutor", "Forma Pagamento", "Valor Total (R$)", "Valor Recebido (R$)", "Troco (R$)", "Operador"};

            Row rowHeader = abaDetalhes.createRow(0);
            for (int i = 0; i < colunas.length; i++) {
                Cell cell = rowHeader.createCell(i);
                cell.setCellValue(colunas[i]);
                cell.setCellStyle(estiloHeader);
            }

            int rowIdx = 1;
            for (Faturamento f : registros) {
                Row row = abaDetalhes.createRow(rowIdx++);
                row.createCell(0).setCellValue(f.getId());
                row.createCell(1).setCellValue(f.getDataPagamento());
                row.createCell(2).setCellValue(f.getHoraPagamento());
                row.createCell(3).setCellValue(f.getAtendimentoId());
                row.createCell(4).setCellValue(f.getConsultaId());
                row.createCell(5).setCellValue(f.getPacienteId());
                row.createCell(6).setCellValue(f.getTutorId());
                row.createCell(7).setCellValue(f.getFormaPagamento().name());

                Cell cValor = row.createCell(8);
                cValor.setCellValue(f.getValorTotal());
                cValor.setCellStyle(estiloMoeda);

                if (f.getValorRecebido() != null) {
                    Cell cRecebido = row.createCell(9);
                    cRecebido.setCellValue(f.getValorRecebido());
                    cRecebido.setCellStyle(estiloMoeda);
                } else {
                    row.createCell(9).setCellValue("-");
                }

                if (f.getTroco() != null) {
                    Cell cTroco = row.createCell(10);
                    cTroco.setCellValue(f.getTroco());
                    cTroco.setCellStyle(estiloMoeda);
                } else {
                    row.createCell(10).setCellValue("-");
                }

                row.createCell(11).setCellValue(f.getOperadorId());
            }

            for (int i = 0; i < colunas.length; i++) {
                abaDetalhes.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(arquivoDestino)) {
                workbook.write(fos);
            }

            return OperationResult.ok("Relatório em Excel (.xlsx) gerado com sucesso em:\n" + arquivoDestino.getAbsolutePath(), arquivoDestino);
        } catch (Exception e) {
            e.printStackTrace();
            return OperationResult.error("Falha ao exportar relatório em Excel: " + e.getMessage());
        }
    }

    private void adicionarLinhaResumoExcel(Sheet sheet, int rowNum, String rotulo, Object valor, CellStyle estiloMoeda) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(rotulo);
        Cell cValor = row.createCell(1);
        if (valor instanceof Double d) {
            cValor.setCellValue(d);
            if (estiloMoeda != null) cValor.setCellStyle(estiloMoeda);
        } else if (valor instanceof Integer i) {
            cValor.setCellValue(i);
        } else {
            cValor.setCellValue(String.valueOf(valor));
        }
    }

    private void adicionarCelulaHeader(PdfPTable table, String texto, Color bgColor, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void adicionarCelulaValor(PdfPTable table, String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(8);
        table.addCell(cell);
    }

    private void adicionarLinhaTabela(PdfPTable table, String texto, Color bgColor, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(align);
        cell.setPadding(5);
        table.addCell(cell);
    }
}
