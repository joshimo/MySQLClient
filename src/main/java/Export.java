/**
 * Created by y.golota on 16.03.2017.
 */

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import javax.swing.JOptionPane;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.Date;

public class Export {

    static void SaveInstrumentsToXLS(String filename, Vector<Vector<String>> dataTable, Vector<String> datatTableHeader) {

        try (FileOutputStream stream = new FileOutputStream(new File (filename))) {

            HSSFWorkbook Book = new HSSFWorkbook();

            HSSFFont header_font = Book.createFont();
            header_font.setBold(true);
            header_font.setFontHeight((short) 225);

            HSSFFont value_font = Book.createFont();
            value_font.setItalic(true);
            value_font.setFontHeight((short) 200);

            HSSFCellStyle style_header = Book.createCellStyle();
            style_header.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
            style_header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style_header.setAlignment(HorizontalAlignment.CENTER);
            style_header.setVerticalAlignment(VerticalAlignment.CENTER);
            style_header.setFont(header_font);
            style_header.setBorderLeft(BorderStyle.THICK);
            style_header.setBorderRight(BorderStyle.THICK);
            style_header.setBorderTop(BorderStyle.THICK);
            style_header.setBorderBottom(BorderStyle.THICK);

            HSSFCellStyle style_value = Book.createCellStyle();
            style_value.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
            style_value.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style_value.setAlignment(HorizontalAlignment.CENTER);
            style_value.setVerticalAlignment(VerticalAlignment.CENTER);
            style_value.setFont(header_font);
            style_value.setBorderLeft(BorderStyle.THIN);
            style_value.setBorderRight(BorderStyle.THIN);
            style_value.setBorderTop(BorderStyle.THIN);
            style_value.setBorderBottom(BorderStyle.THIN);


            HSSFSheet i_list = Book.createSheet("DataBase Query Result");
            int cnt = 0;
            Date date = new Date();
            //i_list.setColumnWidth(0, 1500);
            //i_list.setColumnWidth(1, 5000);
            //i_list.setColumnWidth(2, 10000);
            i_list.createRow(0).setHeight((short) 300);
            i_list.addMergedRegion(new CellRangeAddress(0,0,0,10));
            i_list.getRow(0).createCell(0).setCellStyle(style_header);
            i_list.getRow(0).getCell(0).setCellValue("Query Result");
            i_list.createRow(1);
            i_list.addMergedRegion(new CellRangeAddress(1,1,0,10));
            i_list.getRow(1).createCell(0).setCellStyle(style_header);
            i_list.getRow(1).getCell(0).setCellValue(date.toGMTString());

            Book.write(stream);
            stream.flush();
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
