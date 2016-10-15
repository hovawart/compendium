package com.nobodyelses.data.commands;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import junit.framework.TestCase;

import org.junit.Test;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.ElementTags;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class TestReporting extends TestCase {

    @Test
    public void test() throws FileNotFoundException, DocumentException {
        Document document = new Document(PageSize.A4.rotate());
        FileOutputStream output = new FileOutputStream("test.pdf");
        PdfWriter.getInstance(document, output);

        document.open();

        Paragraph title = new Paragraph("Test Title");
        title.setAlignment(ElementTags.ALIGN_CENTER);
        title.setSpacingAfter(50);
        document.add(title);

        PdfPTable table = new PdfPTable(4);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.setWidthPercentage(100);

        table.addCell(getReportCell("col1"));
        table.addCell(getReportCell("col2"));
        table.addCell(getReportCell("col3"));
        table.addCell(getReportCell("col4"));
        document.add(table);

        document.close();
    }

    private PdfPCell getReportCell(String phrase) {
        Phrase p = new Phrase(phrase);
        p.getFont().setSize(10);

        PdfPCell cell = new PdfPCell(p);
        cell.setBorderWidth(0);

        return cell;
    }
}
