/*
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see <https://www.gnu.org/licenses/>.
 */

package ai.datamaker.generator;

import ai.datamaker.exception.DatasetSerializationException;
import ai.datamaker.model.Dataset;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.field.type.ArrayField;
import ai.datamaker.model.field.type.ComplexField;
import ai.datamaker.model.field.type.ImageField;
import com.google.common.collect.Lists;
import com.itextpdf.text.Anchor;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class PdfGenerator implements DataGenerator {

    static final PropertyConfig PDF_GENERATOR_DOCUMENT_TITLE =
            new PropertyConfig("pdf.generator.document.title",
                               "Document title",
                               PropertyConfig.ValueType.EXPRESSION,
                               "#dataset.name",
                               Collections.emptyList());

    static final PropertyConfig PDF_GENERATOR_DOCUMENT_SUBJECT=
            new PropertyConfig("pdf.generator.document.subject",
                               "Document subject",
                               PropertyConfig.ValueType.EXPRESSION,
                               "",
                               Collections.emptyList());

    static final PropertyConfig PDF_GENERATOR_DOCUMENT_KEYWORDS =
            new PropertyConfig("pdf.generator.document.keywords",
                               "Keywords",
                               PropertyConfig.ValueType.STRING,
                               "",
                               Collections.emptyList());

    static final PropertyConfig PDF_GENERATOR_DOCUMENT_AUTHOR =
            new PropertyConfig("pdf.generator.document.author",
                               "Document author",
                               PropertyConfig.ValueType.STRING,
                               "",
                               Collections.emptyList());

    static final PropertyConfig PDF_GENERATOR_DOCUMENT_CREATOR =
            new PropertyConfig("pdf.generator.document.creator",
                               "Document creator",
                               PropertyConfig.ValueType.STRING,
                               "",
                               Collections.emptyList());

    static final PropertyConfig PDF_GENERATOR_TEMPLATE =
            new PropertyConfig("pdf.generator.template",
                               "Template",
                               PropertyConfig.ValueType.STRING,
                               "",
                               Collections.emptyList());

    static final PropertyConfig PDF_GENERATOR_OUTPUT_TABLE =
            new PropertyConfig("pdf.generator.output.table",
                               "Output data in a table",
                               PropertyConfig.ValueType.BOOLEAN,
                               false,
                               Collections.emptyList());

    static final PropertyConfig PDF_GENERATOR_FONT =
            new PropertyConfig("pdf.generator.font",
                               "Font",
                               PropertyConfig.ValueType.STRING,
                               "TIMES_ROMAN",
                               Arrays.stream(Font.FontFamily.values()).map(Font.FontFamily::toString).collect(Collectors.toList()));

    static final PropertyConfig PDF_GENERATOR_NUMBER_PAGES =
            new PropertyConfig("pdf.generator.pages",
                               "Number of pages",
                               PropertyConfig.ValueType.NUMERIC,
                               "",
                               Collections.emptyList());

    @Override
    public void generate(Dataset dataset, OutputStream outputStream) throws Exception {
        generate(dataset, outputStream, JobConfig.EMPTY);
    }

    @Override
    public void generate(Dataset dataset, OutputStream outputStream, JobConfig config) throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();
        addMetaData(document, dataset, config);
        addTitlePage(document, dataset, config);
        addContent(document, dataset, config);
        document.close();
    }

    @Override
    public FormatType getDataType() {
        return FormatType.PDF;
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(PDF_GENERATOR_TEMPLATE,
                                  PDF_GENERATOR_NUMBER_PAGES,
                                  PDF_GENERATOR_FONT,
                                  PDF_GENERATOR_OUTPUT_TABLE,
                                  PDF_GENERATOR_DOCUMENT_AUTHOR,
                                  PDF_GENERATOR_DOCUMENT_CREATOR,
                                  PDF_GENERATOR_DOCUMENT_TITLE,
                                  PDF_GENERATOR_DOCUMENT_SUBJECT,
                                  PDF_GENERATOR_DOCUMENT_KEYWORDS);
    }

    private Font getCatFont(JobConfig config) {
        return new Font(Font.FontFamily.valueOf((String) config.getConfigProperty(PDF_GENERATOR_FONT)),
                        18,
                        Font.BOLD);
    }

    private Font getSubFont(JobConfig config) {
        return new Font(Font.FontFamily.valueOf((String) config.getConfigProperty(PDF_GENERATOR_FONT)),
                        16,
                        Font.BOLD);
    }

    private Font getSmallBond(JobConfig config) {
        return new Font(Font.FontFamily.valueOf((String) config.getConfigProperty(PDF_GENERATOR_FONT)),
                        12,
                        Font.BOLD);
    }

    private Font getRedFont(JobConfig config) {
        return new Font(Font.FontFamily.valueOf((String) config.getConfigProperty(PDF_GENERATOR_FONT)),
                        12,
                        Font.NORMAL,
                        BaseColor.RED);
    }

    private void addMetaData(Document document, Dataset dataset, JobConfig jobConfig) {
        document.addTitle((String) jobConfig.getConfigProperty(PDF_GENERATOR_DOCUMENT_TITLE));
        document.addSubject((String) jobConfig.getConfigProperty(PDF_GENERATOR_DOCUMENT_SUBJECT));
        document.addKeywords((String) jobConfig.getConfigProperty(PDF_GENERATOR_DOCUMENT_KEYWORDS));
        document.addAuthor((String) jobConfig.getConfigProperty(PDF_GENERATOR_DOCUMENT_AUTHOR));
        document.addCreator((String) jobConfig.getConfigProperty(PDF_GENERATOR_DOCUMENT_CREATOR));
    }

    private void addTitlePage(Document document, Dataset dataset, JobConfig jobConfig) throws DocumentException {
        Paragraph preface = new Paragraph();
        // We add one empty line
        addEmptyLine(preface, 1);
        // Lets write a big header
        String title = (String) jobConfig.getConfigProperty(PDF_GENERATOR_DOCUMENT_TITLE);
        document.addTitle(title);

        preface.add(new Paragraph(title, getCatFont(jobConfig)));

        addEmptyLine(preface, 1);
        // Will create: Report generated by: _name, _date
        preface.add(new Paragraph(
                "Report generated by: " + System.getProperty("user.name") + ", " + new Date(),
                getSmallBond(jobConfig)));
        addEmptyLine(preface, 3);
//        preface.add(new Paragraph(
//                "This document describes something which is very important ",
//                getSmallBond(jobConfig)));

        addEmptyLine(preface, 8);

//        preface.add(new Paragraph(
//                "This document is a preliminary version and not subject to your license agreement or any other agreement with vogella.com ;-).",
//                getRedFont(jobConfig)));

        document.add(preface);
        // Start a new page
        document.newPage();
    }

    private void addContent(Document document, Dataset dataset, JobConfig jobConfig) throws Exception {

        if ((boolean) jobConfig.getConfigProperty(PDF_GENERATOR_OUTPUT_TABLE)) {
            createTable(document, dataset, jobConfig);
        } else {

            final int[] chapterIndex = new int[]{1};

            dataset.processAllValues(fv -> {
                Anchor anchor = new Anchor(dataset.getName(), getCatFont(jobConfig));
                anchor.setName(dataset.getName());

                Chapter catPart = new Chapter(new Paragraph(anchor), chapterIndex[0]++);

                fv.forEach(value -> {
                   Paragraph subPara = new Paragraph(value.getField().getName(), getSubFont(jobConfig));
                   Section subCatPart = catPart.addSection(subPara);

                   if (value.getField() instanceof ImageField) {
                       try {
                           int indentation = 0;
                           Image image = Image.getInstance((byte[])value.getValue());
                           float scaler = ((document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin() - indentation) / image.getWidth()) * 100;
                           image.scalePercent(scaler);
                           subCatPart.add(image);
                       } catch (BadElementException | IOException e) {
                           throw new DatasetSerializationException("Error while inserting image", e, dataset);
                       }

                   } else if (value.getField() instanceof ArrayField) {
                       List<Object> objects = (List<Object>) value.getValue();
                       com.itextpdf.text.List list = new com.itextpdf.text.List(true, false, 10);
                       objects.forEach(o -> list.add(new ListItem(o.toString())));

                       subCatPart.add(list);

                   } else if (value.getField() instanceof ComplexField) {
                       // not supported...
                   } else {
                       subCatPart.add(new Paragraph(value.getValue().toString()));
                   }
               }
                );
                try {
                    document.add(catPart);
                } catch (DocumentException e) {
                    throw new DatasetSerializationException("Error while creating pdf", e, dataset);
                }
            });
        }

    }

    private void createTable(Document document, Dataset dataset, JobConfig jobConfig) throws DocumentException {
        Anchor anchor = new Anchor(dataset.getName(), getCatFont(jobConfig));
        anchor.setName(dataset.getName());

        Paragraph title = new Paragraph(anchor);
        Chapter catPart = new Chapter(title, 1);

        addEmptyLine(title, 3);

        PdfPTable table = new PdfPTable(dataset.getFields().size());

        dataset.getFields().forEach(f -> {
            PdfPCell c1 = new PdfPCell(new Phrase(f.getName(), getSubFont(jobConfig)));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);
        });

        dataset.processAllValues(fv -> {

            fv.forEach(value -> {

                   if (value.getField() instanceof ImageField) {
                       try {
                           int indentation = 0;
                           Image image = Image.getInstance((byte[])value.getValue());
                           float scaler = ((document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin() - indentation) / image.getWidth()) * 100;
                           image.scalePercent(scaler);
                           table.addCell(image);
                       } catch (BadElementException | IOException e) {
                           throw new DatasetSerializationException("Error while inserting image", e, dataset);
                       }

                   } else if (value.getField() instanceof ArrayField) {
                       List<Object> objects = (List<Object>) value.getValue();
                       com.itextpdf.text.List list = new com.itextpdf.text.List(true, false, 10);
                       objects.forEach(o -> table.addCell(new ListItem(o.toString())));

                   } else if (value.getField() instanceof ComplexField) {
                       // not supported...
                   } else {
                       table.addCell(value.getValue().toString());
                   }
               }
            );
        });

        catPart.add(table);
        document.add(catPart);

        // t.setBorderColor(BaseColor.GRAY);
        // t.setPadding(4);
        // t.setSpacing(4);
        // t.setBorderWidth(1);
    }

    private static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }
}
