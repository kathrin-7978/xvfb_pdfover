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

import iaik.x509.X509Certificate;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.signator.BKUs;
import at.asit.pdfover.signator.DocumentSource;
import at.asit.pdfover.signator.Emblem;
import at.asit.pdfover.signator.SignatureDimension;
import at.asit.pdfover.signator.SignaturePosition;
import at.gv.egiz.pdfas.lib.api.Configuration;
import at.gv.egiz.pdfas.lib.api.PdfAs;
import at.gv.egiz.pdfas.lib.api.PdfAsFactory;
import at.gv.egiz.pdfas.lib.api.sign.SignParameter;
import at.asit.pdfover.commons.Profile;

/**
 * Implementation of SignatureParameter for PDF-AS 4 Library
 */
public class PdfAs4SignatureParameter {
    /**
     * SLF4J Logger instance
     **/
    static final Logger log = LoggerFactory.getLogger(PdfAs4SignatureParameter.class);
    
    /**
     * this is set by CliArguments.InvisibleProfile
     * TODO: this is a no good, very bad, hack
     */
    public static boolean PROFILE_VISIBILITY = true;

    /** The Signature Position */
	public SignaturePosition signaturePosition = null;

	/** The Signature language */
	public String signatureLanguage = null;

	/** The key identifier */
	public String keyIdentifier = null;

	/** The input document */
	public DocumentSource inputDocument = null;

	/** Holds the emblem */
	public Emblem emblem;

	/** Whether to use PDF/A compatibility */
	public boolean enablePDFACompat;

	/** The signature device */
	public BKUs signatureDevice;

	/** Whether so look for placeholder signatures or not. */
	public boolean searchForPlaceholderSignatures = false;

    private HashMap<String, String> genericProperties = new HashMap<String, String>();

    /**
     * This parameters are defining the signature block size
     */
    private int sig_w = 229;
    private int sig_h = 77;

    public String signatureProfileName = Profile.getDefaultProfile();

	// TODO why is this stored separately?
    public SignatureDimension getPlaceholderDimension() {
        return new SignatureDimension(this.sig_w, this.sig_h);
    }

    public Image getPlaceholder() {
        String sigProfile = getPdfAsSignatureProfileId();

        String sigEmblem = (this.emblem == null ? null : this.emblem.getFileName());
        String sigNote = getProperty("SIG_NOTE");

        try {
            X509Certificate cert = new X509Certificate(PdfAs4SignatureParameter.class.getResourceAsStream("/example.cer"));
            PdfAs pdfas = PdfAs4Helper.getPdfAs();
            Configuration conf = pdfas.getConfiguration();
            if (sigEmblem != null && !sigEmblem.trim().equals("")) {
                conf.setValue("sig_obj." + sigProfile + ".value.SIG_LABEL", sigEmblem);
            }
            if (sigNote != null) {
                conf.setValue("sig_obj." + sigProfile + ".value.SIG_NOTE", sigNote);
            }
            SignParameter param = PdfAsFactory
                    .createSignParameter(conf, null, null);
            param.setSignatureProfileId(sigProfile);
            Image img = pdfas.generateVisibleSignaturePreview(param, cert, 72 * 4);
            this.sig_w = img.getWidth(null) / 4;
            this.sig_h = img.getHeight(null) / 4;

            return img;
        } catch (Exception e) {
            log.error("Failed to get signature placeholder", e);
            return new BufferedImage(getPlaceholderDimension().getWidth(),
                    getPlaceholderDimension().getHeight(),
                    BufferedImage.TYPE_INT_RGB);
        }
    }

    // TODO review this
    public void setProperty(String key, String value) {
        this.genericProperties.put(key, value);
    }

    public String getProperty(String key) {
        return this.genericProperties.get(key);
    }

    /**
     * Gets the Signature Position String for PDF-AS
     *
     * @return Signature Position String
     */
    public String getPdfAsSignaturePosition() {
        SignaturePosition in_pos = this.signaturePosition;

        if (in_pos.useAutoPositioning())
			return "p:auto;x:auto;y:auto";

		if (in_pos.getPage() < 1)
		{
			return String.format(
					(Locale) null,
					"p:new;x:%f;y:%f", in_pos.getX(), in_pos.getY());
		} else {
			return String.format(
					(Locale) null,
					"p:%d;x:%f;y:%f", in_pos.getPage(), in_pos.getX(), in_pos.getY());
		}
    }

    /**
     * Get the Signature Profile ID for this set of parameters
     *
     * @return the Signature Profile ID
     */
    public String getPdfAsSignatureProfileId() {

        //Add Signature Param here//
        String profileId;

        if (!PROFILE_VISIBILITY) {
            log.debug("Profile visibility was set to false");
            return Profile.INVISIBLE.name();
        }

        Profile profile = Profile.getProfile(this.signatureProfileName);
        switch (profile) {
            case BASE_LOGO:
            case INVISIBLE:
                return this.signatureProfileName;
            case AMTSSIGNATURBLOCK:
                profileId = this.signatureProfileName;
                profileId += getLangProfilePart(this.signatureLanguage);
                profileId += "_RECOMMENDED";
                return profileId;
            default:
                profileId = this.signatureProfileName;
                profileId += getLangProfilePart(this.signatureLanguage);
                break;
        }

        if (getProperty("SIG_NOTE") != null)
            profileId += "_NOTE";

        if (this.enablePDFACompat)
            profileId += "_PDFA";

        log.debug("Profile ID: {}", profileId);
        return profileId;
    }

    private static String getLangProfilePart(String lang) {
        return ("en".equals(lang)) ? "_EN" : "_DE";
    }
}



