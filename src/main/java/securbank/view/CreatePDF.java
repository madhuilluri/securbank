package securbank.view;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.transaction.Transaction;

import org.springframework.beans.factory.annotation.Autowired;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import securbank.dao.TransactionDao;
import securbank.dao.UserDao;
import securbank.models.User;
import securbank.models.Account;

/**
 * @author Amit Kumar
 *
 */
public class CreatePDF {
	public String TAG = getClass().toString() + " ";
	private static Font TIME_ROMAN = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
	private static Font TIME_ROMAN_SMALL = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);

	static Set<Account> accounts = new HashSet<Account>();
	static Set<securbank.models.Transaction> transactions = new HashSet<securbank.models.Transaction>();
	static int transactionCount = 20;

	/**
	 * @param args
	 */
	public static Document createPDF(String file, User user) {

		Document document = null;

		try {
			document = new Document();
			PdfWriter.getInstance(document, new FileOutputStream(file));
			document.open();

			addMetaData(document);

			addTitlePage(document, user);

			createTable(document);

			document.close();

		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return document;

	}

	private static void addMetaData(Document document) {
		document.addTitle("Generate PDF report");
		document.addSubject("Generate PDF report");
		document.addAuthor("Cardinal Bank System");
		document.addCreator("Cardinal Bank System");
	}

	private static void addTitlePage(Document document, User user) throws DocumentException {

		Paragraph preface = new Paragraph();
		creteEmptyLine(preface, 1);
		preface.add(new Paragraph("Account Statement Report", TIME_ROMAN));
		preface.setAlignment(Element.ALIGN_CENTER);

		creteEmptyLine(preface, 1);
		preface.add(new Paragraph("Name: ", TIME_ROMAN_SMALL) + user.getFirstName() + " " + user.getLastName());

		accounts = user.getAccounts();
		long acc_num = 0;
		// System.out.println("total account: "+accounts.size());
		for (Account acc : accounts) {
			System.out.println("Type:  " + acc.getType());
			if ("checking".equals(acc.getType())) {
				System.out.println("Account number inisde if loop ");
				acc_num = acc.getAccountNumber();
				transactions = acc.getTransactions();

			}
		}

		// System.out.println("Account number "+acc_num);
		creteEmptyLine(preface, 1);
		preface.add(new Paragraph("Account details: ", TIME_ROMAN_SMALL) + String.valueOf(acc_num));

		creteEmptyLine(preface, 1);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
		preface.add(new Paragraph("Report created on: " + simpleDateFormat.format(new Date()), TIME_ROMAN_SMALL));
		document.add(preface);

	}

	private static void creteEmptyLine(Paragraph paragraph, int number) {
		for (int i = 0; i < number; i++) {
			paragraph.add(new Paragraph(" "));
		}
	}

	private static void createTable(Document document) throws DocumentException {
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
		for (securbank.models.Transaction trans : transactions) {
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
