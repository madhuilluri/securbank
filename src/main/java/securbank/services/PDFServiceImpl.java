package securbank.services;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import securbank.models.Account;
import securbank.models.Transaction;
import securbank.models.User;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

@Service("pdfService")
@Transactional
public class PDFServiceImpl implements PDFService {

	private final Font TIME_ROMAN = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
	private final Font TIME_ROMAN_SMALL = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);

	int transactionCount = 20;
	
	@Override
	public ByteArrayOutputStream convertPDFToByteArrayOutputStream(String fileName) {

		InputStream inputStream = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {

			inputStream = new FileInputStream(fileName);
			byte[] buffer = new byte[1024];
			baos = new ByteArrayOutputStream();

			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				baos.write(buffer, 0, bytesRead);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return baos;
	}

	@Override
	public Document createStatementPDF(String file, User user) {
		Document document = null;

		try {
			document = new Document();
			PdfWriter.getInstance(document, new FileOutputStream(file));
			document.open();

			addStatementMetaData(document);

			addStatementTitlePage(document, user);

			createStatementTable(document, user);

			document.close();

		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return document;
	}

	private void addStatementMetaData(Document document) {
		document.addTitle("Generate PDF report");
		document.addSubject("Generate PDF report");
		document.addAuthor("Cardinal Bank System");
		document.addCreator("Cardinal Bank System");
	}

	private void addStatementTitlePage(Document document, User user) throws DocumentException {

		Paragraph preface = new Paragraph();
		creteEmptyLine(preface, 1);
		preface.add(new Paragraph("Account Statement Report", TIME_ROMAN));
		preface.setAlignment(Element.ALIGN_CENTER);

		creteEmptyLine(preface, 1);
		preface.add(new Paragraph("Name: ", TIME_ROMAN_SMALL) + user.getFirstName() + " " + user.getLastName());

		creteEmptyLine(preface, 1);
		preface.add(new Paragraph("Account details: ", TIME_ROMAN_SMALL)
				+ String.valueOf(getCheckingAccount(user).getAccountNumber()));

		creteEmptyLine(preface, 1);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
		preface.add(new Paragraph("Report created on: " + simpleDateFormat.format(new Date()), TIME_ROMAN_SMALL));
		document.add(preface);

	}

	private void creteEmptyLine(Paragraph paragraph, int number) {
		for (int i = 0; i < number; i++) {
			paragraph.add(new Paragraph(" "));
		}
	}

	private Account getCheckingAccount(User user) {
		for (Account acc : user.getAccounts()) {
			System.out.println("Type:  " + acc.getType());
			if ("checking".equals(acc.getType())) {
				System.out.println("Account number inisde if loop ");
				return acc;
			}
		}
		return null;
	}

	private void createStatementTable(Document document, User user) throws DocumentException {
		Paragraph paragraph = new Paragraph();
		creteEmptyLine(paragraph, 2);
		document.add(paragraph);
		PdfPTable table = new PdfPTable(3);

		PdfPCell c1 = new PdfPCell(new Phrase("Date"));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("Withdrawal/Deposit"));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("Balance"));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(c1);
		table.setHeaderRows(1);

		int i = 0;
		for (Transaction trans : getCheckingAccount(user).getTransactions()) {
			if (i < transactionCount) {
				table.setWidthPercentage(100);
				table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
				table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);

				table.addCell(trans.getCreatedOn().toString());
				table.addCell(trans.getType());
				table.addCell(String.valueOf(trans.getAmount()));
			}
			i++;
		}

		document.add(table);
	}

}
