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
package at.asit.pdfover.signer.pdfas;

//Imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import at.asit.pdfover.signator.Signer;
import at.asit.pdfover.signator.SignerFactory;

/**
 * Factory class for PDF-AS Signer
 */
public class PDFASSignerFactory extends SignerFactory {

	/**
	 * SFL4J Logger instance
	 **/
	private static final Logger log = LoggerFactory.getLogger(PDFASSignerFactory.class);
	
	@Override
	public Signer createSigner() {
		return new PDFASSigner();
	}
}
