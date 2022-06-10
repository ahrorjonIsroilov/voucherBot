package ent.service;

import ent.entity.Transaction;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class ExcelExporterService implements BaseService {
    private final XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private final List<Transaction> listTransactions;

    public ExcelExporterService(List<Transaction> listTransactions) {
        this.listTransactions = listTransactions;
        workbook = new XSSFWorkbook();
    }


    private void writeHeaderLine() {
        sheet = workbook.createSheet("Transactions");

        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(16);
        style.setFont(font);

        createCell(row, 0, "Transaction ID", style);
        createCell(row, 1, "Seller name", style);
        createCell(row, 2, "Employee name", style);
        createCell(row, 3, "Employee ID", style);
        createCell(row, 4, "Amount", style);
        createCell(row, 5, "Transaction time", style);

    }

    private void createCell(Row row, int columnCount, Object value, CellStyle style) {
        sheet.autoSizeColumn(columnCount);
        Cell cell = row.createCell(columnCount);
        if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Date)
            cell.setCellValue(new SimpleDateFormat("MM-dd-yyyy HH:mm").format((Date) value));
        else if (value instanceof Long)
            cell.setCellValue((Long) value);
        cell.setCellStyle(style);
    }

    private void writeDataLines() {
        int rowCount = 1;
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);
        style.setFont(font);

        for (Transaction user : listTransactions) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row, columnCount++, user.getId(), style);
            createCell(row, columnCount++, user.getSellerName(), style);
            createCell(row, columnCount++, user.getEmployeeName(), style);
            createCell(row, columnCount++, user.getEmployeeId(), style);
            createCell(row, columnCount++, user.getAmount(), style);
            createCell(row, columnCount++, user.getTransactionTime(), style);
        }
    }

    public void export(String path) {
        writeHeaderLine();
        writeDataLines();
        try (OutputStream writer = new FileOutputStream(path)) {
            workbook.write(writer);
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
