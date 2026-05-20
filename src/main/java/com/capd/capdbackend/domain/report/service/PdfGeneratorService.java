package com.capd.capdbackend.domain.report.service;

import com.capd.capdbackend.domain.report.entity.ReportEntity;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
@Slf4j
public class PdfGeneratorService {

    public byte[] generateReportPdf(ReportEntity report) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // 한글 폰트 설정
            PdfFont font = PdfFontFactory.createFont(
                    "fonts/NanumGothic-Regular.ttf",
                    "Identity-H",
                    PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            document.setFont(font);

            // 제목
            Paragraph title = new Paragraph("CAPD 주간 보고서")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(20)
                    .setBold()
                    .setMarginBottom(20);
            document.add(title);

            // 기본 정보 테이블
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(20);

            addTableRow(infoTable, "환자명", report.getPatient().getUser().getUserName());
            addTableRow(infoTable, "담당 의사", report.getDoctor().getUser().getUserName());
            addTableRow(infoTable, "보고서 기간",
                    report.getStartDate() + " ~ " + report.getEndDate());
            document.add(infoTable);

            // 투석 데이터 요약 제목
            document.add(new Paragraph("투석 데이터 요약")
                    .setFontSize(14)
                    .setBold()
                    .setMarginBottom(10));

            // 투석 데이터 요약 테이블
            Table dataTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(20);

            addTableRow(dataTable, "체중 변화", report.getWeightSummary());
            addTableRow(dataTable, "혈압 변화", report.getBpSummary());
            addTableRow(dataTable, "혈당 변화", report.getBloodSugarSummary());
            addTableRow(dataTable, "총초여과량", report.getUfSummary());
            addTableRow(dataTable, "이상치 발생", report.getAnomalySummary());
            document.add(dataTable);

            // AI 종합 소견 제목
            document.add(new Paragraph("AI 종합 소견")
                    .setFontSize(14)
                    .setBold()
                    .setMarginBottom(10));

            // AI 종합 소견 내용
            document.add(new Paragraph(report.getDocSummary())
                    .setFontSize(11)
                    .setMarginBottom(20)
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setPadding(10));

            document.close();

            log.info("PDF 생성 완료: reportId={}", report.getReportId());
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("PDF 생성 실패: {}", e.getMessage());
            throw new RuntimeException("PDF 생성 실패: " + e.getMessage());
        }
    }

    // 테이블 행 추가
    private void addTableRow(Table table, String key, String value) {
        table.addCell(new Cell()
                .add(new Paragraph(key).setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setPadding(8));
        table.addCell(new Cell()
                .add(new Paragraph(value != null ? value : "-"))
                .setPadding(8));
    }
}