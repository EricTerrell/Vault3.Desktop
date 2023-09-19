/*
  Vault 3
  (C) Copyright 2023, Eric Bergman-Terrell
  
  This file is part of Vault 3.

  Vault 3 is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  Vault 3 is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with Vault 3.  If not, see <http://www.gnu.org/licenses/>.
*/

package mainPackage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

public class Email {
	private static final String PROTOCOL_SMTP = "smtp";
	
	public static void send(String serverAddress, boolean authenticate, String userName, String password, String toAddresses, String fromAddress, String subject, String body, List<String> photoPaths, int maxResolution) throws MessagingException, IOException {
		Properties props = new Properties();
	    props.put("mail.smtp.host", serverAddress);
	    
	    PreferenceStore preferenceStore = Globals.getPreferenceStore();
	    
	    int port = preferenceStore.getInt(PreferenceKeys.EmailSMTPPort);
	    
	    if (port > 0) {
	    	props.put("mail.smtp.port", port);
	    }
	    
	    props.put("mail.from", fromAddress);
	    
	    if (authenticate) {
	    	props.put("mail.smtp.auth", "true");
	    }
	    
	    if (preferenceStore.getBoolean(PreferenceKeys.EmailSSL)) {
	    	props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
	    }
	    
	    Session session = Session.getInstance(props, null);

	    Globals.getLogger().info("send email");
	    Globals.getLogger().info(String.format("Server Address: %s Authenticate: %s User Name: %s To Addresses: %s From Address: %s, Subject: %s", serverAddress, authenticate, userName, toAddresses, fromAddress, subject));
	    
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom();
        msg.setRecipients(Message.RecipientType.TO, toAddresses);
        msg.setSubject(subject);
        msg.setSentDate(new Date());
        
        Multipart mp = new MimeMultipart();
        
        BodyPart textPart = new MimeBodyPart();
        textPart.setText(body);
        mp.addBodyPart(textPart);
        
        Globals.getLogger().info(String.format("textPart: %s", body));
        
        List<File> tempFiles = new ArrayList<>();
        
        if (photoPaths != null) {
	        for (String photoPath : photoPaths) {
	    		Globals.getLogger().info(String.format("Attachment: %s", photoPath));
	    		
		        BodyPart attachmentPart = new MimeBodyPart();
		        
		        attachmentPart.setFileName(photoPath);
		        
	        	// Scale the image to the specified maximum resolution, and convert to JPEG before sending. If the photo
	        	// is not converted to JPEG format it may not be visible to the recipient.
	        	Image originalImage = null, resizedImage = null;
	        	
	        	try {
			        originalImage = GraphicsUtils.loadImage(photoPath);
			        
			        if (maxResolution != -1) {
			        	resizedImage = GraphicsUtils.resize(maxResolution, originalImage);
			        }
			        else {
				        resizedImage = GraphicsUtils.loadImage(photoPath);
			        }
			        
			        ImageLoader imageLoader =  new ImageLoader();
			        imageLoader.data = new ImageData[] { resizedImage.getImageData() };
			        
			        String prefix = String.format("%s_TempPhoto_", StringLiterals.ProgramName);
			        File tempFile = File.createTempFile(prefix, ".jpeg");
			        
			        Globals.getLogger().info(String.format("Email.send, scaling image and saving it to %s", tempFile.getAbsolutePath()));
			        
			        imageLoader.save(tempFile.getAbsolutePath(), SWT.IMAGE_JPEG);
			        
			        tempFiles.add(tempFile);
			        
			        DataSource dataSource = new FileDataSource(tempFile.getAbsolutePath());
			        attachmentPart.setDataHandler(new DataHandler(dataSource));
	        	}
	        	finally {
	        		if (originalImage != null) {
	        			originalImage.dispose();
	        		}
	        		
	        		if (resizedImage != null) {
	        			resizedImage.dispose();
	        		}
	        	}
		        
		        mp.addBodyPart(attachmentPart);
	        }
        }

        msg.setContent(mp);

        try {
	        if (authenticate) {
	        	Transport transport = session.getTransport(PROTOCOL_SMTP);
	        	transport.connect(serverAddress, userName, password);
	        	transport.sendMessage(msg, msg.getAllRecipients());
	        }
	        else {
	    		Transport.send(msg);
	        }
        }
    	finally {
    		for (File tempFile : tempFiles) {
    			try {
    				if (tempFile.exists()) {
        				Globals.getLogger().info(String.format("Email.send, deleting temporary file %s", tempFile.getAbsolutePath()));
    					tempFile.delete();
    				}
    			}
    			catch (Throwable ex) {
    				Globals.getLogger().info(String.format("Email.send, cannot delete temporary file %s", tempFile.getAbsolutePath()));
    			}
    		}
    	}
	}

	public static void send(String serverAddress, boolean authenticate, String userName, String password, String toAddresses, String fromAddress, String subject, String body) throws MessagingException, IOException {
		send(serverAddress, authenticate, userName, password, toAddresses, fromAddress, subject, body, null, 0);
	}
}
