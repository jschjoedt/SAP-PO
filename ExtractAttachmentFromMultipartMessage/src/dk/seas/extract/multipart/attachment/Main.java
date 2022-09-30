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

	private static final String CONTENT_DISPOSITION = "attachment";
	private static final String CONTENT_MAINPAYLOAD_DESCRIPTION = "MainDocument";
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

		MimeMultipart mmp = extractMulitipartFromMessage(is, "multipart/related");

		DataHandler dh = extractBodyPartFromMultipart(mmp, CONTENT_DISPOSITION, CONTENT_MAINPAYLOAD_DESCRIPTION);

		if (EMBEDDED_MULTIPART_MESSAGE) {
			// Extract PDF document from MainDocument (Embedded multipart in Mainpayload)
			mmp = extractMulitipartFromMessage(dh.getInputStream(), "application/pdf");
			dh = extractBodyPartFromMultipart(mmp, CONTENT_DISPOSITION, "N/A");
		}
		
		writeAttachmentToDisk(TEST_DATA_PATH, f.getName(), dh);

	}


	private static File[] getFiles(String filePath) {
		File folder = new File(filePath);

		folder.listFiles();

		return folder.listFiles();
	}


	private static void writeAttachmentToDisk(String outputFolder, String originalFileName, DataHandler dh) throws IOException {
		File f = new File(outputFolder + originalFileName + " - " + dh.getName());

		FileOutputStream fos = new FileOutputStream(f);

		dh.writeTo(fos);

		System.out.println("Attachment extracted from payload: " + f.getAbsolutePath());

	}


	private static MimeMultipart extractMulitipartFromMessage(InputStream payloadFile, String contentType)
			throws IOException, MessagingException {
		ByteArrayDataSource ds = new ByteArrayDataSource(payloadFile, contentType);
		MimeMultipart mmp = new MimeMultipart(ds);
		return mmp;
	}


	private static DataHandler extractBodyPartFromMultipart(MimeMultipart mmp, String contentDisposition, String contentDescription) throws MessagingException, IOException {
		DataHandler dh = null;

		for (int i = 0; i < mmp.getCount(); i++) {
			BodyPart bp = mmp.getBodyPart(i);
			if (bp.getDisposition().equals(contentDisposition) && getContentDescription(bp).equals(contentDescription)) {
				System.out.println("Exctracted for further processing: " + bp.getDisposition() + " : " + bp.getContentType());
				dh = bp.getDataHandler();
			}
		}

		return dh;
	}


	private static String getContentDescription(BodyPart bp) throws MessagingException {
		String contentDescription = null;
		
			contentDescription = bp.getDescription();
			
			if (contentDescription == null) {
				// No content description is found in current BodyPart, return not applicable
				contentDescription = "N/A";
			}
				
		return contentDescription;
	}
}