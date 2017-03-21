/**
 * Created by y.golota on 16.03.2017.
 */

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import javax.swing.JOptionPane;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.Date;

class Export {

    private boolean addRequestListing = false;
    private String requestString;

    public void setRequestString(String requestString) {
        this.requestString = requestString;
    }

    public void setAddRequestListing(boolean addRequestListing) {
        this.addRequestListing = addRequestListing;
    }

    void SaveInstrumentsToXLS(String filename, Vector<Vector<String>> dataTable, Vector<String> dataTableHeader) {

        try (FileOutputStream stream = new FileOutputStream(new File (filename))) {

            HSSFWorkbook Book = new HSSFWorkbook();

            HSSFFont header_font = Book.createFont();
            header_font.setBold(true);
            header_font.setFontHeight((short) 320);

            HSSFFont subheader_font = Book.createFont();
            subheader_font.setBold(true);
            subheader_font.setItalic(true);
            subheader_font.setFontHeight((short) 220);

            HSSFFont value_font = Book.createFont();
            value_font.setFontHeight((short) 200);

            HSSFCellStyle style_header = Book.createCellStyle();
            style_header.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
            style_header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style_header.setAlignment(HorizontalAlignment.CENTER);
            style_header.setVerticalAlignment(VerticalAlignment.CENTER);
            style_header.setFont(header_font);

            HSSFCellStyle style_subheader = Book.createCellStyle();
            style_subheader.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
            style_subheader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style_subheader.setAlignment(HorizontalAlignment.LEFT);
            style_subheader.setVerticalAlignment(VerticalAlignment.CENTER);
            style_subheader.setFont(subheader_font);

            HSSFCellStyle style_TableHeader = Book.createCellStyle();
            style_TableHeader.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
            style_TableHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style_TableHeader.setAlignment(HorizontalAlignment.CENTER);
            style_TableHeader.setVerticalAlignment(VerticalAlignment.CENTER);
            style_TableHeader.setFont(subheader_font);
            style_TableHeader.setBorderLeft(BorderStyle.THICK);
            style_TableHeader.setBorderRight(BorderStyle.THICK);
            style_TableHeader.setBorderTop(BorderStyle.THICK);
            style_TableHeader.setBorderBottom(BorderStyle.THICK);

            HSSFCellStyle style_TableCellValue = Book.createCellStyle();
            style_TableCellValue.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
            style_TableCellValue.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style_TableCellValue.setAlignment(HorizontalAlignment.CENTER);
            style_TableCellValue.setVerticalAlignment(VerticalAlignment.CENTER);
            style_TableCellValue.setFont(value_font);
            style_TableCellValue.setBorderLeft(BorderStyle.THICK);
            style_TableCellValue.setBorderRight(BorderStyle.THICK);
            style_TableCellValue.setBorderTop(BorderStyle.THIN);
            style_TableCellValue.setBorderBottom(BorderStyle.THIN);

            HSSFSheet i_list = Book.createSheet("DataBase Query Result");
            int cellCounter = 0;
            int rowCounter = 0;
            Date date = new Date();

            i_list.createRow(rowCounter).setHeight((short) 500);
            i_list.addMergedRegion(new CellRangeAddress(rowCounter, rowCounter, 0, dataTableHeader.size() - 1));
            i_list.getRow(rowCounter).createCell(cellCounter).setCellStyle(style_header);
            i_list.getRow(rowCounter).getCell(cellCounter).setCellValue("Database Query Result");

            rowCounter ++;
            i_list.createRow(rowCounter).setHeight((short) 450);
            i_list.addMergedRegion(new CellRangeAddress(rowCounter, rowCounter, 0, dataTableHeader.size() - 1));
            i_list.getRow(rowCounter).createCell(cellCounter).setCellStyle(style_subheader);
            i_list.getRow(rowCounter).getCell(cellCounter).setCellValue("Generated at " + date.toGMTString());

            if (addRequestListing) {
                rowCounter ++;
                i_list.createRow(rowCounter).setHeight((short) 450);
                i_list.addMergedRegion(new CellRangeAddress(rowCounter, rowCounter, 0, dataTableHeader.size() - 1));
                i_list.getRow(rowCounter).createCell(cellCounter).setCellStyle(style_subheader);
                i_list.getRow(rowCounter).getCell(cellCounter).setCellValue("Request listing: " + requestString);
            }

            rowCounter ++;
            i_list.createRow(rowCounter).setHeight((short) 400);
            for (int i = 0; i < dataTableHeader.size(); i ++) {
                String cellValue = dataTableHeader.get(i);
                int cellWidth = cellValue.length() * 450;
                i_list.setColumnWidth(i, cellWidth);
                i_list.getRow(rowCounter).createCell(i).setCellStyle(style_TableHeader);
                i_list.getRow(rowCounter).getCell(i).setCellValue(cellValue);
            }


            for (int i = 0; i < dataTable.size(); i ++) {
                rowCounter ++;
                Vector<String> currentRow = dataTable.get(i);
                i_list.createRow(rowCounter).setHeight((short) 300);
                for (int cell = 0; cell < currentRow.size(); cell ++) {
                    try {
                        String cellValue = currentRow.get(cell);
                        if (cellValue == null) cellValue = "null";
                        int cellWidth = cellValue.length() * 320;
                        if (cellWidth > i_list.getColumnWidth(cell)) i_list.setColumnWidth(cell, cellWidth);
                        i_list.getRow(rowCounter).createCell(cell).setCellStyle(style_TableCellValue);
                        i_list.getRow(rowCounter).getCell(cell).setCellValue(cellValue);
                    }
                    catch (NullPointerException npe) { }
                }
            }

            Book.write(stream);
            stream.close();
        }
        catch(FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "File " + filename.split("/")[filename.split("/").length - 1] + " not found or still opened by another application!");
        }
        catch(IOException e) {
            JOptionPane.showMessageDialog(null, "IO Exception!");
        }
    }
}