/*
 * Copyright 2012 by A-SIT, Secure Information Technology Center Austria
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package at.asit.pdfover.gui.bku.mobile;

// Imports
import org.apache.commons.httpclient.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.workflow.config.ConfigProvider;

/**
 * A-Trust MobileBKUStatus implementation
 */
public class ATrustStatus extends MobileBKUStatus {
	/**
	 * SLF4J Logger instance
	 **/
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory
			.getLogger(ATrustStatus.class);

	/** Maximum number of TAN tries */
	public static final int MOBILE_MAX_TAN_TRIES = 3;

	public String viewState;
	public String eventValidation;
	public String qrCodeURL = null;
	private boolean tanField = false;
	private boolean isAPPTan = false;
	public String viewStateGenerator; 
	public String dynAttrPhoneNumber; 
	public String dynAttrPassword; 
	public String dynAttrBtnId; 
	public String dynAttrTan; 
	public String dynAttrSignButton; 
	public boolean isSMSTan = false;

	/**
	 * Constructor
	 * @param provider the ConfigProvider
	 */
	public ATrustStatus(ConfigProvider provider) {
		this.phoneNumber = provider.getDefaultMobileNumber();
		this.mobilePassword = provider.getDefaultMobilePassword();
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.workflow.states.mobilebku.MobileBKUStatus#getMaxTanTries()
	 */
	@Override
	public int getMaxTanTries() {
		return MOBILE_MAX_TAN_TRIES;
	}
	
	/**
	 * @param tanField
	 */
	public void setTanField(String tanField) {
		this.tanField = tanField.equals("input_tan"); //$NON-NLS-1$
	}
	
	/**
	 * @return boolean if response contained tan field
	 */
	public boolean getTanField() {
		return this.tanField;
	}
	
	/**
	 * @param tanString the tan string from the response
	 */
	public void setIsAPPTan(String tanString) {
		this.isAPPTan = !tanString.toLowerCase().contains("sms"); //$NON-NLS-1$
	}
	
	/**
	 * @return true if the user receives the tan via app
	 */
	public boolean getIsAPPTan() {
		return this.isAPPTan;
	}

	@Override
	public String ensureSessionID(String url) {
		return url;
	}
}
