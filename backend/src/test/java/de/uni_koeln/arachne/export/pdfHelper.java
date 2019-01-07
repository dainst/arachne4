package de.uni_koeln.arachne.export;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;

class pdfHelper {
    static String stripText(byte[] pdf, Integer page) throws IOException {

        final PDDocument document = PDDocument.load(pdf);

        final AccessPermission ap = document.getCurrentAccessPermission();
        if (!ap.canExtractContent())  {
            throw new IOException("You do not have permission to extract text");
        }

        final PDFTextStripper stripper = new PDFTextStripper();

        stripper.setSortByPosition(true);

        stripper.setStartPage(page);
        stripper.setEndPage(page);

        return stripper.getText(document).trim();
    }

    static Integer countPages(byte[] pdf) throws IOException {
        final PDDocument document = PDDocument.load(pdf);
        return document.getNumberOfPages();
    }
}
