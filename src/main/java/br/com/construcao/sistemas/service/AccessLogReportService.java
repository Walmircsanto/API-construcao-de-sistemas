package br.com.construcao.sistemas.service;

import br.com.construcao.sistemas.controller.dto.response.acesslog.AccessLogReportDTO;
import br.com.construcao.sistemas.model.AuditLog;
import br.com.construcao.sistemas.repository.AccessLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccessLogReportService {

    private final AccessLogRepository repository;

    public byte[] generateReport(LocalDateTime startDate, LocalDateTime endDate, String format) {
        try {
            log.info("Iniciando geração de relatório - Período: {} até {}, Formato: {}", startDate, endDate, format);

            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("A data inicial não pode ser posterior à data final");
            }

            List<AuditLog> logs = repository.findByDataCriacaoBetweenOrderByDataCriacaoDesc(startDate, endDate);
            log.info("Encontrados {} registros no período", logs.size());

            if (logs.isEmpty()) {
                log.warn("Nenhum registro encontrado para o período informado");
                throw new IllegalArgumentException("Nenhum registro encontrado para o período informado");
            }

            List<AccessLogReportDTO> data = logs.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            InputStream reportStream = getClass().getResourceAsStream("/reports/access_log_report.jrxml");

            if (reportStream == null) {
                log.error("Template do relatório não encontrado no caminho: /reports/access_log_report.jrxml");
                throw new IllegalStateException("Template do relatório não encontrado. Verifique se o arquivo access_log_report.jrxml está em src/main/resources/reports/");
            }

            log.debug("Template carregado com sucesso, compilando...");
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
            log.debug("Template compilado com sucesso");

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("START_DATE", startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            parameters.put("END_DATE", endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(data);

            log.debug("Preenchendo relatório com dados...");
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            log.debug("Relatório preenchido com sucesso");

            byte[] reportBytes = exportReport(jasperPrint, format);
            log.info("Relatório gerado com sucesso. Tamanho: {} bytes", reportBytes.length);

            return reportBytes;

        } catch (IllegalArgumentException e) {
            log.error("Erro de validação ao gerar relatório: {}", e.getMessage());
            throw e;

        } catch (IllegalStateException e) {
            log.error("Erro de estado ao gerar relatório: {}", e.getMessage());
            throw e;

        } catch (JRException e) {
            log.error("Erro do JasperReports ao gerar relatório", e);
            throw new RuntimeException("Erro ao processar o template do relatório: " + e.getMessage(), e);

        } catch (Exception e) {
            log.error("Erro inesperado ao gerar relatório", e);
            throw new RuntimeException("Erro inesperado ao gerar relatório: " + e.getMessage(), e);
        }
    }

    public byte[] generateReportWithFilters(
            LocalDateTime startDate,
            LocalDateTime endDate,
            String userEmail,
            String method,
            Integer statusCode,
            String format) {

        try {
            log.info("Iniciando geração de relatório com filtros - User: {}, Method: {}, Status: {}",
                    userEmail, method, statusCode);

            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("A data inicial não pode ser posterior à data final");
            }

            List<AuditLog> logs = repository.findByFilters(startDate, endDate, userEmail, method, statusCode);
            log.info("Encontrados {} registros com os filtros aplicados", logs.size());

            if (logs.isEmpty()) {
                log.warn("Nenhum registro encontrado para os filtros informados");
                throw new IllegalArgumentException("Nenhum registro encontrado para os filtros informados");
            }

            List<AccessLogReportDTO> data = logs.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            InputStream reportStream = getClass().getResourceAsStream("/reports/access_log_report.jrxml");

            if (reportStream == null) {
                log.error("Template do relatório não encontrado no caminho: /reports/access_log_report.jrxml");
                throw new IllegalStateException("Template do relatório não encontrado. Verifique se o arquivo access_log_report.jrxml está em src/main/resources/reports/");
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("START_DATE", startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            parameters.put("END_DATE", endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(data);

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            byte[] reportBytes = exportReport(jasperPrint, format);
            log.info("Relatório com filtros gerado com sucesso. Tamanho: {} bytes", reportBytes.length);

            return reportBytes;

        } catch (IllegalArgumentException e) {
            log.error("Erro de validação ao gerar relatório com filtros: {}", e.getMessage());
            throw e;

        } catch (IllegalStateException e) {
            log.error("Erro de estado ao gerar relatório com filtros: {}", e.getMessage());
            throw e;

        } catch (JRException e) {
            log.error("Erro do JasperReports ao gerar relatório com filtros", e);
            throw new RuntimeException("Erro ao processar o template do relatório: " + e.getMessage(), e);

        } catch (Exception e) {
            log.error("Erro inesperado ao gerar relatório com filtros", e);
            throw new RuntimeException("Erro inesperado ao gerar relatório: " + e.getMessage(), e);
        }
    }

    private byte[] exportReport(JasperPrint jasperPrint, String format) {
        try {
            log.debug("Exportando relatório no formato: {}", format);

            return switch (format.toLowerCase()) {
                case "pdf" -> {
                    log.debug("Exportando para PDF...");
                    yield JasperExportManager.exportReportToPdf(jasperPrint);
                }
                case "xlsx" -> {
                    log.debug("Exportando para Excel...");
                    yield exportToExcel(jasperPrint);
                }
                case "csv" -> {
                    log.debug("Exportando para CSV...");
                    yield exportToCsv(jasperPrint);
                }
                default -> {
                    log.error("Formato não suportado: {}", format);
                    throw new IllegalArgumentException("Formato não suportado: " + format + ". Use: pdf, xlsx ou csv");
                }
            };

        } catch (JRException e) {
            log.error("Erro ao exportar relatório no formato {}", format, e);
            throw new RuntimeException("Erro ao exportar relatório no formato " + format + ": " + e.getMessage(), e);
        }
    }

    private byte[] exportToExcel(JasperPrint jasperPrint) throws JRException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

            SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
            configuration.setDetectCellType(true);
            configuration.setCollapseRowSpan(false);
            configuration.setWhitePageBackground(false);
            configuration.setRemoveEmptySpaceBetweenRows(true);

            exporter.setConfiguration(configuration);
            exporter.exportReport();

            return outputStream.toByteArray();

        } catch (JRException e) {
            log.error("Erro específico ao exportar para Excel", e);
            throw e;
        }
    }

    private byte[] exportToCsv(JasperPrint jasperPrint) throws JRException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            JRCsvExporter exporter = new JRCsvExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleWriterExporterOutput(outputStream));

            SimpleCsvExporterConfiguration configuration = new SimpleCsvExporterConfiguration();
            configuration.setFieldDelimiter(";");

            exporter.setConfiguration(configuration);
            exporter.exportReport();

            return outputStream.toByteArray();

        } catch (JRException e) {
            log.error("Erro específico ao exportar para CSV", e);
            throw e;
        }
    }

    private AccessLogReportDTO convertToDTO(AuditLog log) {
        try {
            return new AccessLogReportDTO(
                    log.getId(),
                    log.getMethod(),
                    log.getPath(),
                    log.getStatusCode(),
                    log.getUserEmail(),
                    log.getIp(),
                    log.getUserAgent(),
                    log.getResponseTimeMs(),
                    log.getDataCriacao()
            );
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar registro de log ID: " + log.getId(), e);
        }
    }
}
