package dk.invixo.java.mapping.audit.log;

import com.sap.aii.mapping.api.AbstractTransformation;
import com.sap.aii.mapping.api.StreamTransformationException;
import com.sap.aii.mapping.api.TransformationInput;
import com.sap.aii.mapping.api.TransformationOutput;
import com.sap.engine.interfaces.messaging.api.MessageDirection;
import com.sap.engine.interfaces.messaging.api.MessageKey;
import com.sap.engine.interfaces.messaging.api.PublicAPIAccessFactory;
import com.sap.engine.interfaces.messaging.api.auditlog.AuditAccess;
import com.sap.engine.interfaces.messaging.api.auditlog.AuditLogStatus;
import com.sap.engine.interfaces.messaging.api.exception.MessagingException;

public class Main extends AbstractTransformation {
	
	private static final String DASH = "-";

	@Override
	public void transform(TransformationInput in, TransformationOutput out) throws StreamTransformationException {
		// Retrieve message ID from input header (com.sap.aii.mapping.api.InputHeader)
		// of transformation input (com.sap.aii.mapping.api.TransformationInput).
		String msgID = in.getInputHeader().getMessageId();
		String msgUUID = generateMessageUUID(msgID);
		// Construct message key (com.sap.engine.interfaces.messaging.api.MessageKey)
		// for retrieved message ID and outbound message direction (com.sap.engine.interfaces.messaging.api.MessageDirection).
		MessageKey msgKey = new MessageKey(msgUUID, MessageDirection.OUTBOUND);
		
		try {
		     // Retrieve instance of audit log accessor (com.sap.engine.interfaces.messaging.api.auditlog.AuditAccess)
		     // using public API access factory (com.sap.engine.interfaces.messaging.api.PublicAPIAccessFactory).
		     AuditAccess msgAuditAccessor = PublicAPIAccessFactory.getPublicAPIAccess().getAuditAccess();
		     // Add new audit log entry with status ‘Success’ to the audit log of the message using the constructed message key.
		     msgAuditAccessor.addAuditLogEntry(msgKey, AuditLogStatus.SUCCESS, "Mapping started!");
		     msgAuditAccessor.addAuditLogEntry(msgKey, AuditLogStatus.SUCCESS, "#MessageID: " + msgID);
		     msgAuditAccessor.addAuditLogEntry(msgKey, AuditLogStatus.SUCCESS, "#MsgUUID: " + msgUUID);
		     msgAuditAccessor.addAuditLogEntry(msgKey, AuditLogStatus.SUCCESS, "#MsgKey: " + msgKey);
		     msgAuditAccessor.addAuditLogEntry(msgKey, AuditLogStatus.SUCCESS, "Mapping done!");
		     
		     } catch (MessagingException e) {
		          // Exception handling logic.
		          e.printStackTrace();
		     }
		
	}

	private String generateMessageUUID(String msgID) {
		// Convert message ID to UUID format (in compliance to RFC 4122).
		// UUID format is used by Advanced Adapter Engine for identifiers of processed messages.
		// For the sake of simplicity, conversion is done manually - alternatively, specific libraries can be used for this.
		
		String uuidTimeLow = msgID.substring(0, 8);
		String uuidTimeMid = msgID.substring(8, 12);
		String uuidTimeHighAndVersion = msgID.substring(12, 16);
		String uuidClockSeqAndReserved = msgID.substring(16, 18);
		String uuidClockSeqLow = msgID.substring(18, 20);
		String uuidNode = msgID.substring(20, 32);
		
		String msgUUID 	= uuidTimeLow + DASH 
						+ uuidTimeMid + DASH 
						+ uuidTimeHighAndVersion + DASH
						+ uuidClockSeqAndReserved + uuidClockSeqLow + DASH 
						+ uuidNode;
		
		return msgUUID;
	}

}
