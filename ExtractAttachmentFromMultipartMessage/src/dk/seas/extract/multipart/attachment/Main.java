package dk.seas.extract.multipart.attachment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

public class Main {

	private static final String CONTENT_TYPE = "multipart/mixed";
	private static final String CONTENT_DISPOSITION = "attachment";
	private static final String TEST_DATA_PATH = "./testData/";
	private static final String FILE_INPUT_EXTENSION_ACCEPTED = "txt";
	private static final boolean EMBEDDED_MULTIPART_MESSAGE = true;



	public static void main(String[] args) throws FileNotFoundException, IOException, MessagingException {

		File[] files = getFiles(TEST_DATA_PATH);

		for (File file : files) {
			if (file.getName().endsWith(FILE_INPUT_EXTENSION_ACCEPTED)) {
				System.out.println("Now processing: " + file.getName());

				Main.processFile(file);
				}
		}
	}


	private static void processFile(File f) throws IOException, MessagingException {
		InputStream is = new FileInputStream(f);

		MimeMultipart mmp = extractMulitipartFromMessage(is);

		DataHandler dh = extractMainDocumentFromMultipart(mmp);

		if (EMBEDDED_MULTIPART_MESSAGE) {
			// Extract PDF document from MainDocument (Embedded multipart in Mainpayload)
			mmp = extractMulitipartFromMessage(dh.getInputStream());
			dh = extractBodyPartDataFromMultipart(mmp);
		}
		
		writeAttachmentToDisk(f.getName(), dh);

	}


	private static File[] getFiles(String filePath) {
		File folder = new File(filePath);

		folder.listFiles();

		return folder.listFiles();
	}


	private static void writeAttachmentToDisk(String originalFileName, DataHandler dh) throws IOException {
		File f = new File(TEST_DATA_PATH + originalFileName + " - " + dh.getName());

		FileOutputStream fos = new FileOutputStream(f);

		dh.writeTo(fos);

		System.out.println("Attachment extracted from payload: " + f.getAbsolutePath());

	}


	private static MimeMultipart extractMulitipartFromMessage(InputStream payloadFile)
			throws IOException, MessagingException {
		ByteArrayDataSource ds = new ByteArrayDataSource(payloadFile, CONTENT_TYPE);
		MimeMultipart mmp = new MimeMultipart(ds);
		return mmp;
	}


	private static DataHandler extractBodyPartDataFromMultipart(MimeMultipart mmp) throws MessagingException, IOException {
		DataHandler dh = null;

		for (int i = 0; i < mmp.getCount(); i++) {
			BodyPart bp = mmp.getBodyPart(i);
			if (bp.getDisposition().equals(CONTENT_DISPOSITION)) {
				System.out.println("Extracted from 'MainDocument': " + bp.getDisposition() + " : " + bp.getContentType());
				dh = bp.getDataHandler();
			}
		}

		return dh;
	}
	
	
	private static DataHandler extractMainDocumentFromMultipart(MimeMultipart mmp) throws MessagingException, IOException {
		DataHandler dh = null;
		
		// MainDocument is always located in the second part of the soap message, first part is the SoapEnvelope
		BodyPart bp = mmp.getBodyPart(1);
		System.out.println("Extracted from 'Mainpayload': " + bp.getDescription() + " : " + bp.getContentType());
		
		dh = bp.getDataHandler();
		
		return dh;
	}
}