package tp.gestion_cleints;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.io.FileOutputStream;
import java.util.List;
import java.util.ResourceBundle;

public class PdfExporter {

    public static void exportClients(List<Client> clients, AdminInfo adminInfo, String filePath, ResourceBundle bundle)
            throws Exception {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));

        document.open();

        // 1. Admin Header (Top Right or Left)
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 8, java.awt.Color.GRAY);

        if (adminInfo != null && adminInfo.getRaisonSociale() != null) {
            Paragraph companyName = new Paragraph(adminInfo.getRaisonSociale().toUpperCase(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, java.awt.Color.DARK_GRAY));
            companyName.setAlignment(Element.ALIGN_LEFT);
            document.add(companyName);

            StringBuilder adminDetails = new StringBuilder();
            if (adminInfo.getAdresse() != null)
                adminDetails.append(adminInfo.getAdresse()).append("\n");
            if (adminInfo.getVille() != null)
                adminDetails.append(adminInfo.getVille());

            Paragraph contactParam = new Paragraph(adminDetails.toString(), smallFont);
            contactParam.setAlignment(Element.ALIGN_LEFT);
            document.add(contactParam);

            // Legal Info Line
            StringBuilder legal = new StringBuilder();
            if (adminInfo.getIce() != null)
                legal.append("ICE: ").append(adminInfo.getIce()).append("  ");
            if (adminInfo.getRc() != null)
                legal.append("RC: ").append(adminInfo.getRc()).append("  ");
            if (adminInfo.getIdentifiantTva() != null)
                legal.append("IF: ").append(adminInfo.getIdentifiantTva());

            if (legal.length() > 0) {
                Paragraph legalParam = new Paragraph(legal.toString(), smallFont);
                legalParam.setAlignment(Element.ALIGN_LEFT);
                document.add(legalParam);
            }

            document.add(new Paragraph(" ")); // Spacer
            document.add(new com.lowagie.text.pdf.draw.LineSeparator(0.5f, 100, java.awt.Color.LIGHT_GRAY,
                    Element.ALIGN_CENTER, -1));
            document.add(new Paragraph(" ")); // Spacer
        }

        // 2. Report Title
        Paragraph title = new Paragraph(bundle.getString("pdf.title"), titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // 3. Table
        PdfPTable table = new PdfPTable(6); // Raison Sociale, Email, Ville, ICE, HT, TTC
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        float[] columnWidths = { 25f, 20f, 15f, 15f, 12f, 13f }; // Adjust widths
        table.setWidths(columnWidths);

        // Headers
        addTableHeader(table, "Raison Sociale");
        addTableHeader(table, "Email");
        addTableHeader(table, "Ville");
        addTableHeader(table, "I.C.E.");
        addTableHeader(table, "Total HT");
        addTableHeader(table, "Total TTC");

        // Rows
        String currency = bundle.getString("currency");
        for (Client client : clients) {
            table.addCell(new Phrase(client.getRaisonSociale(), normalFont));
            table.addCell(new Phrase(client.getEmail(), normalFont));
            table.addCell(new Phrase(client.getVille(), normalFont));
            table.addCell(new Phrase(client.getIce(), normalFont));
            table.addCell(new Phrase(String.format("%.2f %s", client.getFixedTotalAmount(), currency), normalFont));
            table.addCell(new Phrase(String.format("%.2f %s", client.getTtc(), currency), normalFont));
        }

        document.add(table);

        // Footer timestamp
        Paragraph footer = new Paragraph("Généré le: " + new java.util.Date().toString(), smallFont);
        footer.setAlignment(Element.ALIGN_RIGHT);
        footer.setSpacingBefore(20);
        document.add(footer);

        document.close();
    }

    public static void exportTransactionReceipt(Transaction t, Client c, AdminInfo adminInfo, String filePath,
            ResourceBundle bundle)
            throws Exception {
        Document document = new Document(PageSize.A5);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, java.awt.Color.DARK_GRAY);
        Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 9, java.awt.Color.GRAY);
        String currency = bundle.getString("currency");

        // Brand/Company Header
        if (adminInfo != null && adminInfo.getRaisonSociale() != null) {
            Paragraph companyName = new Paragraph(adminInfo.getRaisonSociale().toUpperCase(), titleFont);
            companyName.setAlignment(Element.ALIGN_RIGHT);
            document.add(companyName);

            // Admin Details (Address, Phone, etc.)
            StringBuilder adminDetails = new StringBuilder();
            if (adminInfo.getAdresse() != null)
                adminDetails.append(adminInfo.getAdresse()).append("\n");
            if (adminInfo.getVille() != null)
                adminDetails.append(adminInfo.getVille()).append("\n");
            if (adminInfo.getPhone() != null)
                adminDetails.append("Tél: ").append(adminInfo.getPhone()).append(" ");
            if (adminInfo.getEmail() != null)
                adminDetails.append("Email: ").append(adminInfo.getEmail());

            Paragraph adminContact = new Paragraph(adminDetails.toString(), smallFont);
            adminContact.setAlignment(Element.ALIGN_RIGHT);
            adminContact.setSpacingAfter(5);
            document.add(adminContact);
        } else {
            // Fallback
            Paragraph companyName = new Paragraph("GESTION CLIENT SYSTEM", titleFont);
            companyName.setAlignment(Element.ALIGN_RIGHT);
            document.add(companyName);
        }

        Paragraph receiptTitle = new Paragraph("REÇU DE PAIEMENT", subTitleFont);
        receiptTitle.setAlignment(Element.ALIGN_LEFT);
        receiptTitle.setSpacingBefore(-15);
        document.add(receiptTitle);

        if (t.getReceiptNumber() != null && !t.getReceiptNumber().trim().isEmpty()) {
            Paragraph receiptNo = new Paragraph("N°: " + t.getReceiptNumber(), subTitleFont);
            receiptNo.setAlignment(Element.ALIGN_LEFT);
            document.add(receiptNo);
        }

        document.add(new Paragraph("________________________________________________", smallFont));

        // Client Info Block
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingBefore(10);
        infoTable.setSpacingAfter(20);
        infoTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.addElement(new Phrase("Client: " + c.getRaisonSociale(), subTitleFont));
        leftCell.addElement(new Phrase("Nom: " + c.getNomPrenom(), normalFont));
        leftCell.addElement(new Phrase("Adresse: " + c.getAdresse() + ", " + c.getVille(), normalFont));

        // Admin Legal Info (ICE, RC, etc)
        if (adminInfo != null) {
            Chunk legalChunk = new Chunk("\n\nVendor Info:\n", smallFont);
            legalChunk.setUnderline(0.1f, -2f);
            leftCell.addElement(legalChunk);
            if (adminInfo.getIce() != null)
                leftCell.addElement(new Phrase("ICE: " + adminInfo.getIce(), smallFont));
            if (adminInfo.getRc() != null)
                leftCell.addElement(new Phrase("RC: " + adminInfo.getRc(), smallFont));
            if (adminInfo.getTp() != null)
                leftCell.addElement(new Phrase("TP: " + adminInfo.getTp(), smallFont));
            if (adminInfo.getIdentifiantTva() != null)
                leftCell.addElement(new Phrase("IF: " + adminInfo.getIdentifiantTva(), smallFont));
        }

        infoTable.addCell(leftCell);

        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(new Phrase("Date: " + t.getDate(), normalFont));
        rightCell.addElement(new Phrase("Client ICE: " + (c.getIce() != null ? c.getIce() : "-"), normalFont));
        rightCell.addElement(new Phrase("Régime: " + (c.getRegimeTva() != null ? c.getRegimeTva() : "-"), normalFont));
        infoTable.addCell(rightCell);

        document.add(infoTable);

        // Transaction Table
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        float[] widths = { 70f, 30f };
        table.setWidths(widths);

        addTableHeader(table, "Description du Paiement");
        addTableHeader(table, "Montant");

        PdfPCell desCell = new PdfPCell(new Phrase("Versement: " + t.getNotes(), normalFont));
        desCell.setPadding(10);
        table.addCell(desCell);

        PdfPCell amtCell = new PdfPCell(new Phrase(String.format("%.2f %s", t.getAmount(), currency), subTitleFont));
        amtCell.setPadding(10);
        amtCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(amtCell);

        document.add(table);

        // Footer
        Paragraph footer = new Paragraph("\nMerci pour votre confiance.", smallFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(30);
        document.add(footer);

        document.close();
    }

    public static void exportTransactionStatement(Client c, List<Transaction> transactions, AdminInfo adminInfo,
            String filePath,
            ResourceBundle bundle) throws Exception {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new java.awt.Color(44, 62, 80));
        Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 8, java.awt.Color.GRAY);
        String currency = bundle.getString("currency");

        // Admin Header
        if (adminInfo != null && adminInfo.getRaisonSociale() != null) {
            Paragraph adminHeader = new Paragraph(adminInfo.getRaisonSociale().toUpperCase(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, java.awt.Color.GRAY));
            adminHeader.setAlignment(Element.ALIGN_LEFT);
            document.add(adminHeader);

            String details = "";
            if (adminInfo.getAdresse() != null)
                details += adminInfo.getAdresse() + ", ";
            if (adminInfo.getVille() != null)
                details += adminInfo.getVille();
            if (!details.isEmpty())
                document.add(new Paragraph(details, smallFont));

            String legal = "";
            if (adminInfo.getIce() != null)
                legal += "ICE: " + adminInfo.getIce() + "  ";
            if (adminInfo.getRc() != null)
                legal += "RC: " + adminInfo.getRc() + "  ";
            if (adminInfo.getIdentifiantTva() != null)
                legal += "IF: " + adminInfo.getIdentifiantTva();
            if (!legal.isEmpty())
                document.add(new Paragraph(legal, smallFont));

            document.add(new Paragraph(
                    "_______________________________________________________________________________", smallFont));
            document.add(new Chunk("\n"));
        }

        Paragraph title = new Paragraph("RELEVÉ DE COMPTE CLIENT", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Detailed Header
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell clientCell = new PdfPCell();
        clientCell.setBorder(Rectangle.NO_BORDER);
        clientCell.addElement(new Phrase("CLIENT:", smallFont));
        clientCell.addElement(new Phrase(c.getRaisonSociale(), subTitleFont));
        clientCell.addElement(new Phrase(c.getAdresse(), normalFont));
        clientCell.addElement(new Phrase(c.getVille(), normalFont));
        clientCell.addElement(new Phrase("ICE: " + c.getIce(), normalFont));
        headerTable.addCell(clientCell);

        PdfPCell summaryCell = new PdfPCell();
        summaryCell.setBorder(Rectangle.NO_BORDER);
        summaryCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        summaryCell
                .addElement(new Phrase("Date du relevé: " + new java.sql.Date(System.currentTimeMillis()), normalFont));
        headerTable.addCell(summaryCell);

        document.add(headerTable);
        document.add(new Chunk("\n"));

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        addTableHeader(table, "Date");
        addTableHeader(table, "Désignation / Notes");
        addTableHeader(table, "Montant");

        double total = 0;
        for (Transaction t : transactions) {
            table.addCell(new Phrase(t.getDate(), normalFont));
            String designation = t.getNotes();
            if (t.getReceiptNumber() != null && !t.getReceiptNumber().isEmpty()) {
                designation = "(N° " + t.getReceiptNumber() + ") " + designation;
            }
            table.addCell(new Phrase(designation, normalFont));
            PdfPCell mCell = new PdfPCell(new Phrase(String.format("%.2f %s", t.getAmount(), currency), normalFont));
            mCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(mCell);
            total += t.getAmount();
        }
        document.add(table);

        Paragraph summary = new Paragraph("\nTOTAL PAYÉ À CE JOUR: " + String.format("%.2f %s", total, currency),
                subTitleFont);
        summary.setAlignment(Element.ALIGN_RIGHT);
        document.add(summary);

        document.close();
    }

    private static void addTableHeader(PdfPTable table, String columnTitle) {
        PdfPCell header = new PdfPCell();
        header.setBackgroundColor(new java.awt.Color(52, 152, 219)); // Premium blue
        header.setPadding(8);
        header.setBorderWidth(1);
        header.setPhrase(
                new Phrase(columnTitle, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, java.awt.Color.WHITE)));
        table.addCell(header);
    }
}
