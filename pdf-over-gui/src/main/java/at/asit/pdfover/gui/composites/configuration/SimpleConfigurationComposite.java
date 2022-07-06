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
package at.asit.pdfover.gui.composites.configuration;

// Imports
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.commons.Profile;
import at.asit.pdfover.gui.composites.StateComposite;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.gui.controls.ErrorMarker;
import at.asit.pdfover.gui.exceptions.InvalidEmblemFile;
import at.asit.pdfover.gui.utils.ImageConverter;
import at.asit.pdfover.gui.workflow.config.ConfigManipulator;
import at.asit.pdfover.gui.workflow.config.ConfigurationContainer;
import at.asit.pdfover.gui.workflow.config.PersistentConfigProvider;
import at.asit.pdfover.gui.workflow.states.State;
import at.asit.pdfover.signator.CachedFileNameEmblem;
import at.asit.pdfover.signator.SignatureParameter;
import at.asit.pdfover.signator.SignaturePosition;

/**
 *
 */
public class SimpleConfigurationComposite extends ConfigurationCompositeBase {

	/**
	 * SLF4J Logger instance
	 **/
	static final Logger log = LoggerFactory
			.getLogger(SimpleConfigurationComposite.class);

	private Group grpHandySignatur;
	private Label lblMobileNumber;
	protected Text txtMobileNumber;
	protected ErrorMarker txtMobileNumberErrorMarker;

	private Group grpLogo;
	private Canvas cLogo;
	private Label lblDropLogo;
	protected Button btnClearImage;
	private Button btnBrowseLogo;
	protected Canvas cSigPreview;

	private Group grpSignatureNote;
	private Label lblSignatureNote;
	protected Text txtSignatureNote;
	private Button btnSignatureNoteDefault;

	protected final Group grpSignatureLang;
	protected final Combo cmbSignatureLang;

	protected String logoFile = null;
	protected Image sigPreview = null;
	protected Image logo = null;

	protected final Group grpSignatureProfile;
	protected final Combo cmbSignatureProfiles;




	/**
	 * @param parent
	 * @param style
	 * @param state
	 * @param container
	 */
	public SimpleConfigurationComposite(
			org.eclipse.swt.widgets.Composite parent, int style, State state,
			ConfigurationContainer container) {
		super(parent, style, state, container);
		setLayout(new FormLayout());

		this.grpHandySignatur = new Group(this, SWT.NONE | SWT.RESIZE);
		StateComposite.anchor(grpHandySignatur).right(100,-5).left(0,5).top(0,5).set();
		grpHandySignatur.setLayout(new GridLayout(2, false));
		StateComposite.setFontHeight(grpHandySignatur, Constants.TEXT_SIZE_NORMAL);

		this.lblMobileNumber = new Label(grpHandySignatur, SWT.NONE | SWT.RESIZE);
		this.lblMobileNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		StateComposite.setFontHeight(lblMobileNumber, Constants.TEXT_SIZE_NORMAL);

		Composite compMobileNumerContainer = new Composite(this.grpHandySignatur, SWT.NONE);
		compMobileNumerContainer.setLayout(new FormLayout());
		compMobileNumerContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		this.txtMobileNumber = new Text(compMobileNumerContainer, SWT.BORDER | SWT.RESIZE);
		StateComposite.anchor(txtMobileNumber).left(0,5).right(100,-42).top(0).set();
		StateComposite.setFontHeight(txtMobileNumber, Constants.TEXT_SIZE_NORMAL);

		this.txtMobileNumberErrorMarker = new ErrorMarker(compMobileNumerContainer, SWT.NONE, "");
		this.txtMobileNumberErrorMarker.setVisible(false);
		StateComposite.anchor(txtMobileNumberErrorMarker).left(100,-32).right(100).top(0).bottom(0,32).set();

		this.txtMobileNumber.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_RETURN) {
				processNumberChanged();
			}
		});

		this.txtMobileNumber.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				processNumberChanged();
			}
		});

		this.grpSignatureProfile = new Group(this, SWT.NONE);
		StateComposite.anchor(grpSignatureProfile).right(100,-5).left(0,5).top(grpHandySignatur, 5).set();
		this.grpSignatureProfile.setLayout(new FormLayout());
		StateComposite.setFontHeight(grpSignatureProfile, Constants.TEXT_SIZE_NORMAL);

		this.cmbSignatureProfiles = new Combo(this.grpSignatureProfile, SWT.READ_ONLY);
		StateComposite.anchor(cmbSignatureProfiles).left(0,10).right(100,-10).top(0,10).bottom(100,-10).set();
		StateComposite.setFontHeight(cmbSignatureProfiles, Constants.TEXT_SIZE_NORMAL);
		this.cmbSignatureProfiles.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Profile current = SimpleConfigurationComposite.this.configurationContainer.getSignatureProfile();
				int index = SimpleConfigurationComposite.this.cmbSignatureProfiles.getSelectionIndex();
				Profile selected = Profile.values()[index];
				if (!current.equals(selected)) {
					preformProfileSelectionChanged(selected);
				}
			}
		});

		this.grpLogo = new Group(this, SWT.NONE);
		StateComposite.anchor(grpLogo).left(0,5).right(100,-5).top(grpSignatureProfile, 5).set();
		this.grpLogo.setLayout(new FormLayout());
		StateComposite.setFontHeight(grpLogo, Constants.TEXT_SIZE_NORMAL);

		Composite containerComposite = new Composite(this.grpLogo, SWT.NONE);
		StateComposite.anchor(containerComposite).left(0).right(100).top(0).bottom(100).set();
		containerComposite.setLayout(new FormLayout());

		final Composite controlComposite = new Composite(containerComposite, SWT.NONE);
		StateComposite.anchor(controlComposite).left(0,20).right(0,300).top(0,20).bottom(100,-20).set();
		controlComposite.setLayout(new FormLayout());
		controlComposite.addPaintListener(e -> {
			e.gc.setForeground(Constants.DROP_BORDER_COLOR);
			e.gc.setLineWidth(3);
			e.gc.setLineStyle(SWT.LINE_DASH);
			Point size = controlComposite.getSize();
			e.gc.drawRoundRectangle(0, 0, size.x - 2, size.y - 2,
					10, 10);
		});

		this.cSigPreview = new Canvas(containerComposite, SWT.RESIZE);
		StateComposite.anchor(cSigPreview).left(controlComposite, 20).right(100,-20).top(0,20).bottom(100,-20).set();
		StateComposite.setFontHeight(cSigPreview, Constants.TEXT_SIZE_NORMAL);
		this.cSigPreview.addPaintListener(e -> imagePaintControl(e, SimpleConfigurationComposite.this.sigPreview));

		this.btnBrowseLogo = new Button(controlComposite, SWT.NONE);
		StateComposite.anchor(btnBrowseLogo).bottom(100,-20).right(100,-20).set();
		StateComposite.setFontHeight(btnBrowseLogo, Constants.TEXT_SIZE_BUTTON);

		this.lblDropLogo = new Label(controlComposite, SWT.NATIVE | SWT.CENTER);
		StateComposite.anchor(lblDropLogo).left(0,20).right(100,-20).bottom(btnBrowseLogo,-20).set();

		this.cLogo = new Canvas(controlComposite, SWT.NONE);
		StateComposite.anchor(cLogo).left(0,20).right(100,-20).top(0,20).bottom(lblDropLogo,-20).height(40).width(40).set();
		this.cLogo.addPaintListener(e -> imagePaintControl(e, SimpleConfigurationComposite.this.logo));

		this.btnClearImage = new Button(controlComposite, SWT.NATIVE);
		StateComposite.anchor(btnClearImage).bottom(100,-20).right(btnBrowseLogo, -10).set();
		StateComposite.setFontHeight(btnClearImage, Constants.TEXT_SIZE_BUTTON);

		DropTarget dnd_target = new DropTarget(controlComposite, DND.DROP_DEFAULT | DND.DROP_COPY);
		final FileTransfer fileTransfer = FileTransfer.getInstance();
		Transfer[] types = new Transfer[] { fileTransfer };
		dnd_target.setTransfer(types);

		dnd_target.addDropListener(new DropTargetAdapter() {
			@Override
			public void drop(DropTargetEvent event) {
				if (fileTransfer.isSupportedType(event.currentDataType)) {
					String[] files = (String[]) event.data;
					if (files.length > 0) {
						// Only taking first file ...
						File file = new File(files[0]);
						if (!file.exists()) {
							log.error("File: {} does not exist!", files[0]); ////
							return;
						}
						processEmblemChanged(file.getAbsolutePath());
					}
				}
			}

			@Override
			public void dragOperationChanged(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					if ((event.operations & DND.DROP_COPY) != 0) {
						event.detail = DND.DROP_COPY;
					} else {
						event.detail = DND.DROP_NONE;
					}
				}
			}

			@Override
			public void dragEnter(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					if ((event.operations & DND.DROP_COPY) != 0) {
						event.detail = DND.DROP_COPY;
					} else {
						event.detail = DND.DROP_NONE;
					}
				}
				// Only drop one item!
				if (event.dataTypes.length > 1) {
					event.detail = DND.DROP_NONE;
					return;
				}
				// will accept text but prefer to have files dropped
				for (int i = 0; i < event.dataTypes.length; i++) {
					if (fileTransfer.isSupportedType(event.dataTypes[i])) {
						event.currentDataType = event.dataTypes[i];
						// files should only be copied
						if (event.detail != DND.DROP_COPY) {
							event.detail = DND.DROP_NONE;
						}
						break;
					}
				}
			}
		});

		this.btnClearImage.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				SimpleConfigurationComposite.this.processEmblemChanged(null);
			}
		});
		this.btnBrowseLogo.addSelectionListener(new ImageFileBrowser());


		this.grpSignatureLang = new Group(this, SWT.NONE);
		StateComposite.anchor(grpSignatureLang).right(100,-5).top(grpLogo, 5).left(0,5).set();
		this.grpSignatureLang.setLayout(new FormLayout());
		StateComposite.setFontHeight(grpSignatureLang, Constants.TEXT_SIZE_NORMAL);

		this.cmbSignatureLang = new Combo(this.grpSignatureLang, SWT.READ_ONLY);
		StateComposite.anchor(cmbSignatureLang).left(0,10).right(100,-10).top(0,10).bottom(100,-10).set();
		StateComposite.setFontHeight(cmbSignatureLang, Constants.TEXT_SIZE_NORMAL);
		this.cmbSignatureLang.setItems(Arrays.stream(Constants.SUPPORTED_LOCALES).map(l -> l.getDisplayLanguage()).toArray(String[]::new));

		this.cmbSignatureLang.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Locale currentLocale = SimpleConfigurationComposite.this.configurationContainer
						.getSignatureLocale();
				Locale selectedLocale = Constants.
						SUPPORTED_LOCALES[SimpleConfigurationComposite.this.cmbSignatureLang
						                  .getSelectionIndex()];
				if (!currentLocale.equals(selectedLocale)) {
					performSignatureLangSelectionChanged(selectedLocale, currentLocale);
				}
			}
		});

		this.grpSignatureNote = new Group(this, SWT.NONE);
		StateComposite.anchor(grpSignatureNote).right(100,-5).top(grpSignatureLang,5).left(0,5).set();
		this.grpSignatureNote.setLayout(new GridLayout(2, false));
		StateComposite.setFontHeight(grpSignatureNote, Constants.TEXT_SIZE_NORMAL);

		this.lblSignatureNote = new Label(this.grpSignatureNote, SWT.NONE);
		do { /* grid positioning */
			GridData gd_lblSignatureNote = new GridData(SWT.LEFT, SWT.CENTER,
					false, false, 1, 1);
			gd_lblSignatureNote.widthHint = 66;
			this.lblSignatureNote.setLayoutData(gd_lblSignatureNote);
			this.lblSignatureNote.setBounds(0, 0, 57, 15);
		} while (false);
		StateComposite.setFontHeight(lblSignatureNote, Constants.TEXT_SIZE_NORMAL);

		Composite compSignatureNoteContainer = new Composite(this.grpSignatureNote, SWT.NONE);
		compSignatureNoteContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		compSignatureNoteContainer.setLayout(new FormLayout());

		this.txtSignatureNote = new Text(compSignatureNoteContainer, SWT.BORDER);
		StateComposite.anchor(txtSignatureNote).top(0,0).left(0,5).right(100,-42).set();
		StateComposite.setFontHeight(txtSignatureNote, Constants.TEXT_SIZE_NORMAL);

		this.txtSignatureNote.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				processSignatureNoteChanged();
			}
		});

		this.txtSignatureNote.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_RETURN) {
				processSignatureNoteChanged();
			}
		});

		Composite compSignatureNoteButtonContainer = new Composite(this.grpSignatureNote, SWT.NONE);
		compSignatureNoteButtonContainer.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));
		compSignatureNoteButtonContainer.setLayout(new FormLayout());

		this.btnSignatureNoteDefault = new Button(compSignatureNoteButtonContainer, SWT.NONE);
		StateComposite.anchor(btnSignatureNoteDefault).top(0,0).right(100,-42).set();
		StateComposite.setFontHeight(btnSignatureNoteDefault, Constants.TEXT_SIZE_BUTTON);
		this.btnSignatureNoteDefault.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SimpleConfigurationComposite.this.txtSignatureNote.setText(getDefaultSignatureBlockNoteTextFor(null, null));
			}
		});

		// Load localized strings
		reloadResources();
	}

	static void imagePaintControl(PaintEvent e, Image i) {
		if (i == null)
			return;

		Rectangle r = i.getBounds();
		int srcW = r.width;
		int srcH = r.height;
		Point p = ((Control) e.widget).getSize();
		float dstW = p.x;
		float dstH = p.y;

		float scale = dstW / srcW;
		if (srcH * scale > dstH)
			scale = dstH / srcH;

		float w = srcW * scale;
		float h = srcH * scale;

		int x = (int) ((dstW / 2) - (w / 2));
		int y = (int) ((dstH / 2) - (h / 2));
		e.gc.drawImage(i, 0, 0, srcW, srcH, x, y, (int) w, (int) h);
	}

	/**
	 *
	 */
	private final class ImageFileBrowser extends SelectionAdapter {
		/**
		 *
		 */
		public ImageFileBrowser() {
			// Nothing to do
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			FileDialog dialog = new FileDialog(
					SimpleConfigurationComposite.this.getShell(), SWT.OPEN);
			dialog.setFilterExtensions(new String[] {
					"*.jpg;*.png;*.gif", "*.jpg", "*.png", "*.gif", "*" });
			dialog.setFilterNames(new String[] {
					Messages.getString("common.ImageExtension_Description"),
					Messages.getString("common.JPGExtension_Description"),
					Messages.getString("common.PNGExtension_Description"),
					Messages.getString("common.GIFExtension_Description"),
					Messages.getString("common.AllExtension_Description") });
			String fileName = dialog.open();
			File file = null;
			if (fileName != null) {
				file = new File(fileName);
				if (file.exists()) {
					processEmblemChanged(fileName);
				}
			}
		}
	}

	private void setEmblemFileInternal(final String filename, boolean force)
			throws Exception {
		if (!force && this.configurationContainer.getEmblem() != null) {
			if (this.configurationContainer.getEmblem().equals(filename)) {
				return; // Ignore ...
			}
		}

		this.configurationContainer.setEmblem(filename);
		this.setVisibleImage();
		this.doLayout();
	}

	void setVisibleImage() {
		String image = this.configurationContainer.getEmblem();
		ImageData img = null;
		ImageData logo = null;

		try {
			if (this.signer != null) {
				SignatureParameter param = this.signer.getPDFSigner().newParameter();
				if(this.configurationContainer.getSignatureNote() != null && !this.configurationContainer.getSignatureNote().isEmpty()) {
					param.setProperty("SIG_NOTE", this.configurationContainer.getSignatureNote());
				}

				param.setSignatureLanguage(this.configurationContainer.getSignatureLocale().getLanguage());
				param.setSignaturePdfACompat(this.configurationContainer.getSignaturePdfACompat());
				if (image != null && !image.trim().isEmpty()) {
					logo = new ImageData(image);
					param.setEmblem(new CachedFileNameEmblem(image));
				}
				//TODO deactivated the placeholder preview
				//TODO display accurate placeholder preview -> now its only standard placeholder shown
				//img = SignaturePlaceholderCache.getSWTPlaceholder(param);
			}
		} catch (Exception e) {
			log.error("Failed to load image for display...", e);
		}

		if (img != null) {
			this.sigPreview = new Image(this.getDisplay(), img);
		} else {
			this.sigPreview = null;
		}

		if (logo != null) {
			try {
				File imgFile = new File(image);
				this.logo = new Image(this.getDisplay(),
						ImageConverter.convertToSWT(CachedFileNameEmblem.fixImage(
								ImageIO.read(imgFile), imgFile)));
			} catch (IOException e) {
				log.error("Error reading image", e);
			}
		} else {
			this.logo = null;
		}

		this.cSigPreview.redraw();
		this.cLogo.redraw();
	}

	void processEmblemChanged(String filename) {
		try {
			setEmblemFileInternal(filename, false);
		} catch (Exception ex) {
			log.error("processEmblemChanged: ", ex);
			ErrorDialog dialog = new ErrorDialog(
					getShell(),
					Messages.getString("error.FailedToLoadEmblem"), BUTTONS.OK);
			dialog.open();
		}
	}

	void processNumberChanged() {
		try {
			this.txtMobileNumberErrorMarker.setVisible(false);
			plainMobileNumberSetter();
		} catch (Exception ex) {
			this.txtMobileNumberErrorMarker.setVisible(true);
			this.txtMobileNumberErrorMarker.setToolTipText(Messages
					.getString("error.InvalidPhoneNumber"));
			log.error("processNumberChanged: ", ex);
			this.redraw();
			this.doLayout();
		}
	}

	int getLocaleElementIndex(Locale locale) {
		for (int i = 0; i < Constants.SUPPORTED_LOCALES.length; i++) {
			if (Constants.SUPPORTED_LOCALES[i].equals(locale)) {
				log.debug("Locale: {} IDX: {}",locale, i);
				return i;
			}
		}

		log.warn("NO Locale match for {}", locale);
		return 0;
	}

	void performSignatureLangSelectionChanged(Locale selected, Locale previous) {
		log.debug("Selected Sign Locale: {}", selected);
		this.configurationContainer.setSignatureLocale(selected);
		this.cmbSignatureLang.select(this.getLocaleElementIndex(selected));

		if ((previous != null) && (txtSignatureNote.getText().equals(getDefaultSignatureBlockNoteTextFor(null, previous))))
			txtSignatureNote.setText(getDefaultSignatureBlockNoteTextFor(null, selected));
	}



    void preformProfileSelectionChanged(Profile newProfile) {
		log.debug("Signature Profile {} was selected", newProfile.name());
		Profile oldProfile = this.configurationContainer.getSignatureProfile();
    	this.configurationContainer.setSignatureProfile(newProfile);
    	this.cmbSignatureProfiles.select(newProfile.ordinal());

    	if (newProfile.equals(Profile.AMTSSIGNATURBLOCK) || newProfile.equals(Profile.INVISIBLE)){
			this.configurationContainer.setDefaultSignaturePosition(new SignaturePosition());
		}
    	setSignatureProfileSetting();
		if (txtSignatureNote.getText().equals(getDefaultSignatureBlockNoteTextFor(oldProfile, null)))
			txtSignatureNote.setText(getDefaultSignatureBlockNoteTextFor(newProfile, null));
	}

	String getDefaultSignatureBlockNoteTextFor(Profile profile, Locale locale){
		if (profile == null)
			profile = configurationContainer.getSignatureProfile();
		if (locale == null)
			locale = configurationContainer.getSignatureLocale();
		return profile.getDefaultSignatureBlockNote(locale);
	}

	void setSignatureProfileSetting(){
		if (this.signer == null){
			log.debug("In setSignatureProfileSettings: Signer was null");
			return;
		}
		try {
			SignatureParameter param = this.signer.getPDFSigner().newParameter();
			param.setSignatureProfile(this.configurationContainer.getSignatureProfile().name());

		} catch (Exception e){
			log.warn("Cannot save signature profile {}", e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * at.asit.pdfover.gui.composites.BaseConfigurationComposite#signerChanged()
	 */
	@Override
	protected void signerChanged() {
		this.setVisibleImage();
		this.setSignatureProfileSetting();
	}

	private void plainMobileNumberSetter() {
		String number = this.txtMobileNumber.getText();
		this.configurationContainer.setMobileNumber(number);
		number = this.configurationContainer.getMobileNumber();
		if (number == null) {
			this.txtMobileNumber.setText("");
			return;
		}
		this.txtMobileNumber.setText(number);
	}

	void processSignatureNoteChanged() {
		String note = this.txtSignatureNote.getText();
		this.configurationContainer.setSignatureNote(note);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see at.asit.pdfover.gui.composites.StateComposite#doLayout()
	 */
	@Override
	public void doLayout() {
		layout(true, true);
	}


	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.BaseConfigurationComposite#initConfiguration(at.asit.pdfover.gui.workflow.config.PersistentConfigProvider)
	 */
	@Override
	public void initConfiguration(PersistentConfigProvider provider) {
		this.configurationContainer.setMobileNumber(
				provider.getDefaultMobileNumberPersistent());

		try {
			this.configurationContainer.setEmblem(
					provider.getDefaultEmblemPersistent());
		} catch (InvalidEmblemFile e) {
			log.error("Failed to set emblem!", e);
		}

		this.configurationContainer.setSignatureLocale(
				provider.getSignatureLocale());

		this.configurationContainer.setSignatureNote(
				provider.getSignatureNote());
	}

	/*
	 * (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.BaseConfigurationComposite#loadConfiguration
	 * ()
	 */
	@Override
	public void loadConfiguration() {
		// Initialize form fields from configuration Container
		String number = this.configurationContainer.getMobileNumber();
		if (number != null) {
			this.txtMobileNumber.setText(number);
		}

		String emblemFile = this.configurationContainer.getEmblem();
		if (emblemFile != null && !emblemFile.trim().isEmpty()) {
			this.logoFile = emblemFile;
			try {
				setEmblemFileInternal(emblemFile, true);
				this.btnClearImage.setSelection(true);
			} catch (Exception e1) {
				log.error("Failed to load emblem: ", e1);
				ErrorDialog dialog = new ErrorDialog(
						getShell(),
						Messages.getString("error.FailedToLoadEmblem"), BUTTONS.OK);
				dialog.open();
			}
		}

		String note = this.configurationContainer.getSignatureNote();

		if (note != null) {
			this.txtSignatureNote.setText(note);
		}

		this.setVisibleImage();

		this.performSignatureLangSelectionChanged(this.configurationContainer.getSignatureLocale(), null);

		this.preformProfileSelectionChanged(this.configurationContainer.getSignatureProfile());

	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.BaseConfigurationComposite#storeConfiguration(at.asit.pdfover.gui.workflow.config.ConfigManipulator, at.asit.pdfover.gui.workflow.config.PersistentConfigProvider)
	 */
	@Override
	public void storeConfiguration(ConfigManipulator store,
			PersistentConfigProvider provider) {
		store.setDefaultMobileNumber(this.configurationContainer.getMobileNumber());

		store.setDefaultEmblem(this.configurationContainer.getEmblem());

		store.setSignatureLocale(this.configurationContainer.getSignatureLocale());

		store.setSignatureNote(this.configurationContainer.getSignatureNote());

		store.setSignatureProfile(this.configurationContainer.getSignatureProfile().name());


	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * at.asit.pdfover.gui.composites.BaseConfigurationComposite#validateSettings
	 * ()
	 */
	@Override
	public void validateSettings(int resumeFrom) throws Exception {
		switch (resumeFrom) {
		case 0:
			this.plainMobileNumberSetter();
			// Fall through
		case 1:
			this.processSignatureNoteChanged();
			break;
		default:
				//Fall through
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see at.asit.pdfover.gui.composites.StateComposite#reloadResources()
	 */
	@Override
	public void reloadResources() {
		this.grpHandySignatur.setText(Messages.getString("simple_config.MobileBKU_Title"));
		this.lblMobileNumber.setText(Messages.getString("simple_config.PhoneNumber"));
		this.txtMobileNumber.setToolTipText(Messages.getString("simple_config.ExampleNumber_ToolTip"));
		this.txtMobileNumber.setMessage(Messages.getString("simple_config.ExampleNumber"));

		this.grpLogo.setText(Messages.getString("simple_config.Emblem_Title"));
		this.lblDropLogo.setText(Messages.getString("simple_config.EmblemEmpty"));
		this.btnClearImage.setText(Messages.getString("simple_config.ClearEmblem"));
		this.btnBrowseLogo.setText(Messages.getString("common.browse"));
		this.grpSignatureNote.setText(Messages.getString("simple_config.Note_Title"));
		this.lblSignatureNote.setText(Messages.getString("simple_config.Note"));
		this.txtSignatureNote.setToolTipText(Messages.getString("simple_config.Note_Tooltip"));
		this.btnSignatureNoteDefault.setText(Messages.getString("simple_config.Note_SetDefault"));

		this.grpSignatureLang.setText(Messages.getString("simple_config.SigBlockLang_Title"));
		this.cmbSignatureLang.setToolTipText(Messages.getString("simple_config.SigBlockLang_ToolTip"));

		this.grpSignatureProfile.setText(Messages.getString("simple_config.SigProfile_Title"));
		this.cmbSignatureProfiles.setItems(Arrays.stream(Profile.values()).map(v -> Messages.getString("simple_config."+v.name())).toArray(String[]::new));
	}
}
