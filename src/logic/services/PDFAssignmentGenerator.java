package logic.services;

import logic.daos.RepresentativeDAO;
import logic.logicclasses.Project;
import logic.logicclasses.Student;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class PDFAssignmentGenerator {

    private static final float MARGIN = 72;
    private static final float LEADING = 14;
    private static final float FONT_SIZE_REGULAR = 12;
    private static final float FONT_SIZE_SMALL = 10;
    private static final float FOOTER_THRESHOLD = 150;

    public static byte[] generateAssignmentPDF(Student student, Project project) throws IOException, SQLException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            float yPosition = PDRectangle.LETTER.getHeight() - MARGIN;
            contentStream.setLeading(LEADING);

            try {
                writeHeader(contentStream, fontBold, fontRegular, project, yPosition);
                yPosition = writeAddress(contentStream, fontRegular, yPosition);
                yPosition = writeCurrentDate(contentStream, fontRegular, yPosition);
                yPosition = writeRepresentativeInfo(contentStream, fontBold, fontRegular, project, yPosition);

                contentStream = writeMainContent(contentStream, fontRegular, student, project, yPosition, document);

                contentStream = writeFooter(contentStream, fontBold, yPosition, document);

            } finally {
                contentStream.close();
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    private static void writeHeader(PDPageContentStream contentStream, PDType1Font fontBold,
                                    PDType1Font fontRegular, Project project, float yPosition) throws IOException {
        contentStream.beginText();
        contentStream.setFont(fontBold, FONT_SIZE_REGULAR);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Facultad de Estadística e Informática");
        contentStream.endText();
    }

    private static float writeAddress(PDPageContentStream contentStream, PDType1Font font,
                                      float yPosition) throws IOException {
        String[] addressLines = {
                "Dirección",
                "Av. Xalapa esq. Ávila Camacho",
                "S/N",
                "Col. Obrero Campesina",
                "CP 91020",
                "Xalapa de Enríquez",
                "Veracruz, México"
        };

        yPosition -= LEADING;

        for (String line : addressLines) {
            contentStream.beginText();
            contentStream.setFont(font, FONT_SIZE_SMALL);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText(line);
            contentStream.endText();
            yPosition -= LEADING;
        }

        return yPosition - LEADING;
    }

    private static float writeCurrentDate(PDPageContentStream contentStream, PDType1Font font,
                                          float yPosition) throws IOException {
        String currentDate = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES")));

        contentStream.beginText();
        contentStream.setFont(font, FONT_SIZE_REGULAR);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Xalapa-Enríquez, Veracruz, a " + currentDate);
        contentStream.endText();

        return yPosition - (LEADING * 2);
    }

    private static float writeRepresentativeInfo(PDPageContentStream contentStream, PDType1Font fontBold,
                                                 PDType1Font fontRegular, Project project, float yPosition)
            throws IOException, SQLException {
        RepresentativeDAO repDAO = new RepresentativeDAO();

        contentStream.beginText();
        contentStream.setFont(fontBold, FONT_SIZE_REGULAR);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(repDAO.getRepresentativeNameByProjectId(project.getIdProyect()));
        contentStream.endText();
        yPosition -= LEADING;

        String[] institutionLines = {
                "FACULTAD DE ESTADÍSTICA E INFORMATICA",
                "AVENIDA XALAPA CASI ESQUINA CON AVENIDA MANUEL AVILA CAMACHO"
        };

        for (String line : institutionLines) {
            contentStream.beginText();
            contentStream.setFont(fontRegular, FONT_SIZE_REGULAR);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText(line);
            contentStream.endText();
            yPosition -= LEADING;
        }

        return yPosition - LEADING;
    }

    private static PDPageContentStream writeMainContent(PDPageContentStream contentStream, PDType1Font font,
                                                        Student student, Project project, float yPosition, PDDocument document) throws IOException {
        String[] paragraphs = {
                "En atención a su solicitud expresada a la Coordinación de Prácticas Profesionales de la",
                "Licenciatura en Ingeniería de Software, hacemos de su conocimiento que el C. " +
                        student.getFullName().toUpperCase() + ", estudiante de la Licenciatura con matrícula " +
                        student.getEnrollment() + ", ha sido asignado al proyecto de " +
                        project.getTitle().toUpperCase() + ", a su digno cargo a partir del " +
                        getFormattedStartDate(LocalDate.now().format(
                                DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES")))) +
                        " del presente hasta cubrir 420 HORAS. Cabe mencionar que el estudiante cuenta con la formación " +
                        "y el perfil para las actividades a desempeñar.",
                "",
                "Anexo a este documento usted encontrará una copia del horario de las experiencias " +
                        "educativas que el estudiante asignado se encuentra cursando para que sea respetado y " +
                        "tomado en cuenta al momento de establecer el horario de realización de sus Prácticas " +
                        "Profesionales. Por otra parte, le solicito de la manera más atenta, haga llegar a la " +
                        "brevedad con el estudiante, el oficio de aceptación así como el plan de trabajo " +
                        "detallado del estudiante, además el horario que cubrirá. Deberá indicar además, la " +
                        "forma en que se registrará la evidencia de asistencia y número de horas cubiertas. " +
                        "Es importante mencionar que el estudiante deberá presentar mensualmente un reporte " +
                        "de avances de sus prácticas. Este reporte de avances puede entregarse hasta con una " +
                        "semana de atraso por lo que le solicito de la manera más atenta sean elaborados y " +
                        "avalados (incluyendo sello si aplica) de manera oportuna para su entrega al académico " +
                        "responsable de la experiencia de Prácticas de Ingeniería de Software. En relación con " +
                        "lo anterior, es importante que en el oficio de aceptación proporcione el nombre de la " +
                        "persona que supervisará y avalará en su dependencia la prestación de las prácticas " +
                        "profesionales así como número telefónico, extensión (cuando aplique) y correo " +
                        "electrónico. Lo anterior con el fin de contar con el canal de comunicación que permita " +
                        "dar seguimiento al desempeño del estudiante.",
                "",
                "Le informo que las Prácticas de Ingeniería de Software forman parte de la currícula " +
                        "de la Licenciatura en Ingeniería de Software, por lo cual es necesaria su evaluación " +
                        "y de ahí la necesidad de realizar el seguimiento correspondiente. Es por ello que, " +
                        "durante el semestre, el coordinador de Prácticas de Ingeniería de Software realizará " +
                        "al menos un seguimiento de las actividades del estudiante por lo que será necesario " +
                        "mostrar evidencias de la asistencia del estudiante, así como de sus actividades. Este " +
                        "seguimiento podrá ser vía correo electrónico, teléfono o incluso mediante una visita " +
                        "a sus oficinas, por lo que le solicito de la manera más atenta, proporcione las " +
                        "facilidades requeridas en su caso.",
                "",
                "Sin más por el momento, agradezco su atención al presente reiterándome a sus " +
                        "apreciables órdenes."
        };

        contentStream.setFont(font, FONT_SIZE_REGULAR);
        float width = PDRectangle.LETTER.getWidth() - 2 * MARGIN;

        for (String paragraph : paragraphs) {
            if (paragraph.isEmpty()) {
                yPosition -= LEADING;
                continue;
            }

            for (String line : splitTextIntoLines(paragraph, font, FONT_SIZE_REGULAR, width)) {
                if (yPosition < MARGIN) {
                    contentStream.close();
                    PDPage newPage = new PDPage(PDRectangle.LETTER);
                    document.addPage(newPage);
                    contentStream = new PDPageContentStream(document, newPage);
                    contentStream.setLeading(LEADING);
                    contentStream.setFont(font, FONT_SIZE_REGULAR);
                    yPosition = PDRectangle.LETTER.getHeight() - MARGIN;
                }

                contentStream.beginText();
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText(line);
                contentStream.endText();
                yPosition -= LEADING;
            }
        }

        return contentStream;
    }

    private static PDPageContentStream writeFooter(PDPageContentStream contentStream, PDType1Font fontBold,
                                                   float yPosition, PDDocument document) throws IOException {
        if (yPosition < FOOTER_THRESHOLD) {
            contentStream.close();
            PDPage newPage = new PDPage(PDRectangle.LETTER);
            document.addPage(newPage);
            contentStream = new PDPageContentStream(document, newPage);
            yPosition = PDRectangle.LETTER.getHeight() - MARGIN;
        }

        contentStream.beginText();
        contentStream.setFont(fontBold, FONT_SIZE_REGULAR);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Dr. Ángel Juan Sánchez García");
        contentStream.endText();
        yPosition -= LEADING;

        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Coordinador de Servicio Social y Prácticas Profesionales");
        contentStream.endText();

        return contentStream;
    }

    private static String getFormattedStartDate(String currentDate) {
        String[] parts = currentDate.split(" ");
        return parts[0] + " DE " + parts[2].toUpperCase();
    }

    private static String[] splitTextIntoLines(String text, PDType1Font font, float fontSize, float width)
            throws IOException {
        StringBuilder builder = new StringBuilder();
        String[] words = text.split(" ");
        String line = "";

        for (String word : words) {
            if (line.isEmpty()) {
                line = word;
            } else {
                String testLine = line + " " + word;
                float testWidth = font.getStringWidth(testLine) / 1000 * fontSize;
                if (testWidth < width) {
                    line = testLine;
                } else {
                    builder.append(line).append("\n");
                    line = word;
                }
            }
        }
        builder.append(line);

        return builder.toString().split("\n");
    }
}