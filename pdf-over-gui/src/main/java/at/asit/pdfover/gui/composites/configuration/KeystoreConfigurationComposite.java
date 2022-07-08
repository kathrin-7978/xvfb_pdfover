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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.gui.composites.StateComposite;
import at.asit.pdfover.gui.controls.Dialog.BUTTONS;
import at.asit.pdfover.gui.controls.ErrorDialog;
import at.asit.pdfover.gui.controls.PasswordInputDialog;
import at.asit.pdfover.gui.exceptions.CantLoadKeystoreException;
import at.asit.pdfover.gui.exceptions.KeystoreAliasDoesntExistException;
import at.asit.pdfover.gui.exceptions.KeystoreAliasNoKeyException;
import at.asit.pdfover.gui.exceptions.KeystoreDoesntExistException;
import at.asit.pdfover.gui.exceptions.KeystoreKeyPasswordException;
import at.asit.pdfover.gui.keystore.KeystoreUtils;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.workflow.config.ConfigurationManager;
import at.asit.pdfover.gui.workflow.config.ConfigurationDataInMemory.KeyStorePassStorageType;
import at.asit.pdfover.gui.workflow.config.ConfigurationDataInMemory;
import at.asit.pdfover.gui.workflow.states.State;

/**
 *
 */
public class KeystoreConfigurationComposite extends ConfigurationCompositeBase {

	/**
	 * SLF4J Logger instance
	 **/
	static final Logger log = LoggerFactory
			.getLogger(KeystoreConfigurationComposite.class);

	private Group grpKeystore;
	private Label lblKeystoreFile;
	Text txtKeystoreFile;
	private Button btnBrowse;
	private Label lblKeystoreType;
	Combo cmbKeystoreType;
	private Button btnLoad;
	private Label lblKeystoreAlias;
	Combo cmbKeystoreAlias;
	private Label lblKeystorePassStoreType;
	Combo cmbKeystorePassStoreType;
	private Label lblKeystoreStorePass;
	Text txtKeystoreStorePass;
	private Label lblKeystoreKeyPass;
	Text txtKeystoreKeyPass;

	private KeyStore ks;

	/**
	 * @param parent
	 * @param style
	 * @param state
	 * @param container
	 */
	public KeystoreConfigurationComposite(
			org.eclipse.swt.widgets.Composite parent, int style, State state,
			ConfigurationDataInMemory container) {
		super(parent, style, state, container);
		setLayout(new FormLayout());

		this.grpKeystore = new Group(this, SWT.NONE | SWT.RESIZE);
		FormLayout layout = new FormLayout();
		layout.marginHeight = 10;
		layout.marginWidth = 5;
		this.grpKeystore.setLayout(layout);

		StateComposite.anchor(grpKeystore).top(0,5).left(0,5).right(100,-5).set();
		StateComposite.setFontHeight(this.grpKeystore, Constants.TEXT_SIZE_NORMAL);

		this.lblKeystoreFile = new Label(this.grpKeystore, SWT.NONE);
		StateComposite.anchor(lblKeystoreFile).top(0).left(0,5).set();
		FormData fd_lblKeystoreFile = new FormData();
		fd_lblKeystoreFile.top = new FormAttachment(0);
		fd_lblKeystoreFile.left = new FormAttachment(0, 5);
		this.lblKeystoreFile.setLayoutData(fd_lblKeystoreFile);
		StateComposite.setFontHeight(lblKeystoreFile, Constants.TEXT_SIZE_NORMAL);

		this.txtKeystoreFile = new Text(grpKeystore, SWT.BORDER);
		this.btnBrowse = new Button(grpKeystore, SWT.NONE);
		StateComposite.setFontHeight(txtKeystoreFile, Constants.TEXT_SIZE_NORMAL);
		StateComposite.setFontHeight(btnBrowse, Constants.TEXT_SIZE_BUTTON);
		StateComposite.anchor(txtKeystoreFile).top(lblKeystoreFile, 5).left(0,15).right(btnBrowse,-5).set();
		StateComposite.anchor(btnBrowse).top(lblKeystoreFile, 5).right(100,-5).set();

		this.lblKeystoreType = new Label(grpKeystore, SWT.NONE);
		StateComposite.anchor(lblKeystoreType).top(txtKeystoreFile, 5).left(0,5).set();
		StateComposite.setFontHeight(lblKeystoreType, Constants.TEXT_SIZE_NORMAL);

		this.btnLoad = new Button(this.grpKeystore, SWT.NONE);
		StateComposite.anchor(btnLoad).top(lblKeystoreType, 5).right(100,-5).set();
		StateComposite.setFontHeight(btnLoad, Constants.TEXT_SIZE_BUTTON);

		this.cmbKeystoreType = new Combo(grpKeystore, SWT.READ_ONLY);
		StateComposite.anchor(cmbKeystoreType).top(lblKeystoreType, 5).left(0,15).right(btnLoad, -5).set();
		StateComposite.setFontHeight(cmbKeystoreType, Constants.TEXT_SIZE_NORMAL);
		StateComposite.disableEventDefault(cmbKeystoreType, SWT.MouseVerticalWheel);

		this.lblKeystoreAlias = new Label(grpKeystore, SWT.NONE);
		StateComposite.anchor(lblKeystoreAlias).top(cmbKeystoreType, 5).left(0, 5).set();
		StateComposite.setFontHeight(lblKeystoreAlias, Constants.TEXT_SIZE_NORMAL);

		this.cmbKeystoreAlias = new Combo(grpKeystore, SWT.NONE);
		StateComposite.anchor(cmbKeystoreAlias).top(lblKeystoreAlias, 5).left(0,15).right(100,-5).set();
		StateComposite.setFontHeight(cmbKeystoreAlias, Constants.TEXT_SIZE_NORMAL);
		StateComposite.disableEventDefault(cmbKeystoreAlias, SWT.MouseVerticalWheel);

		this.lblKeystorePassStoreType = new Label(this.grpKeystore, SWT.NONE);
		StateComposite.anchor(lblKeystorePassStoreType).top(cmbKeystoreAlias, 5).left(0,5).set();
		StateComposite.setFontHeight(lblKeystorePassStoreType, Constants.TEXT_SIZE_NORMAL);

		this.cmbKeystorePassStoreType = new Combo(grpKeystore, SWT.READ_ONLY);
		StateComposite.anchor(cmbKeystorePassStoreType).top(lblKeystorePassStoreType, 5).left(0,15).right(100,-5).set();
		StateComposite.setFontHeight(cmbKeystorePassStoreType, Constants.TEXT_SIZE_NORMAL);
		StateComposite.disableEventDefault(cmbKeystorePassStoreType, SWT.MouseVerticalWheel);

		this.lblKeystoreStorePass = new Label(grpKeystore, SWT.NONE);
		StateComposite.anchor(lblKeystoreStorePass).top(cmbKeystorePassStoreType, 5).left(0,5).set();
		StateComposite.setFontHeight(lblKeystoreStorePass, Constants.TEXT_SIZE_NORMAL);

		this.txtKeystoreStorePass = new Text(grpKeystore, SWT.BORDER | SWT.PASSWORD);
		StateComposite.anchor(txtKeystoreStorePass).right(100, -5).top(lblKeystoreStorePass, 5).left(0,15).set();
		StateComposite.setFontHeight(txtKeystoreStorePass, Constants.TEXT_SIZE_NORMAL);

		this.lblKeystoreKeyPass = new Label(grpKeystore, SWT.NONE);
		StateComposite.anchor(lblKeystoreKeyPass).top(txtKeystoreStorePass, 5).left(0,5).set();
		StateComposite.setFontHeight(lblKeystoreKeyPass, Constants.TEXT_SIZE_NORMAL);

		this.txtKeystoreKeyPass = new Text(grpKeystore, SWT.BORDER | SWT.PASSWORD);
		StateComposite.anchor(txtKeystoreKeyPass).top(lblKeystoreKeyPass, 5).left(0,15).right(100,-5).set();
		StateComposite.setFontHeight(txtKeystoreKeyPass, Constants.TEXT_SIZE_NORMAL);

		this.txtKeystoreFile.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				performKeystoreFileChanged(KeystoreConfigurationComposite.this.txtKeystoreFile.getText());
			}
		});

		this.btnBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(
						KeystoreConfigurationComposite.this.getShell(), SWT.OPEN);
				dialog.setFilterExtensions(new String[] {
						"*.p12;*.pkcs12;*.pfx;*.ks;*.jks", "*.p12;*.pkcs12;*.pfx;", "*.ks;*.jks*.", "*" });
				dialog.setFilterNames(new String[] {
						Messages.getString("common.KeystoreExtension_Description"),
						Messages.getString("common.PKCS12Extension_Description"),
						Messages.getString("common.KSExtension_Description"),
						Messages.getString("common.AllExtension_Description") });
				String fileName = dialog.open();
				File file = null;
				if (fileName != null) {
					file = new File(fileName);
					if (file.exists()) {
						performKeystoreFileChanged(fileName);
					}
				}
			}
		});

		this.cmbKeystoreType.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				performKeystoreTypeChanged(
						KeystoreConfigurationComposite.this.keystoreTypes.get(
								KeystoreConfigurationComposite.this.cmbKeystoreType.getItem(
										KeystoreConfigurationComposite.this.cmbKeystoreType.getSelectionIndex())));
			}
		});

		this.txtKeystoreStorePass.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				performKeystoreStorePassChanged(KeystoreConfigurationComposite.
						this.txtKeystoreStorePass.getText());
			}
			
		});

		this.btnLoad.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				File f = new File(KeystoreConfigurationComposite.this
						.configurationContainer.keystoreFile);
				try {
					loadKeystore(true);
				} catch (KeyStoreException ex) {
					log.error("Error loading keystore", ex);
					showErrorDialog(Messages.getString("error.KeyStore"));
				} catch (FileNotFoundException ex) {
					log.error("Error loading keystore", ex);
					showErrorDialog(String.format(Messages.getString(
							"error.KeyStoreFileNotExist"), f.getName()));
				} catch (NoSuchAlgorithmException ex) {
					log.error("Error loading keystore", ex);
					showErrorDialog(Messages.getString("error.KeyStore"));
				} catch (CertificateException ex) {
					log.error("Error loading keystore", ex);
					showErrorDialog(Messages.getString("error.KeyStore"));
				} catch (IOException ex) {
					log.error("Error loading keystore", ex);
					showErrorDialog(Messages.getString("error.KeyStore"));
				} catch (NullPointerException ex) {
					log.error("Error loading keystore - NPE?", ex);
					showErrorDialog(Messages.getString("error.KeyStore"));
				} catch (UnrecoverableKeyException ex) {
					log.warn("Error loading keystore, invalid password", ex);
					showErrorDialog(Messages.getString("error.KeyStoreStorePass"));
				}
			}
		});

		this.cmbKeystoreAlias.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				performKeystoreAliasChanged(
						KeystoreConfigurationComposite.this.cmbKeystoreAlias.getItem(
								KeystoreConfigurationComposite.this.cmbKeystoreAlias.getSelectionIndex()));
			}
		});
		this.cmbKeystoreAlias.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				performKeystoreAliasChanged(KeystoreConfigurationComposite.this.cmbKeystoreAlias.getText());
			}
		});

		this.cmbKeystorePassStoreType.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				performKeystorePassStorageTypeChanged(
					keystorePassStorageTypeOptions.get(
						KeystoreConfigurationComposite.this.cmbKeystorePassStoreType.getSelectionIndex()
					).getLeft()
				);
			}
		});

		this.txtKeystoreKeyPass.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				performKeystoreKeyPassChanged(KeystoreConfigurationComposite.this.txtKeystoreKeyPass.getText());
			}
		});

		// Load localized strings
		
		reloadResources();
	}

	void showErrorDialog(String error) {
		ErrorDialog e = new ErrorDialog(getShell(), error, BUTTONS.OK);
		e.open();
	}

	void loadKeystore(boolean force) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException {
		this.cmbKeystoreAlias.remove(0, this.cmbKeystoreAlias.getItemCount()-1);

		ConfigurationDataInMemory config = this.configurationContainer;
		this.ks = null;
		String pass = config.keystoreStorePass;
		if (!force && pass == null)
			throw new UnrecoverableKeyException("No password specified");

		while (this.ks == null)
		{
			if (pass == null)
			{
				pass = new PasswordInputDialog(
						getShell(),
						Messages.getString("keystore_config.KeystoreStorePass"),
						Messages.getString("keystore.KeystoreStorePassEntry")).open();
				if (pass == null)
					throw new UnrecoverableKeyException("User cancelled password input");
			}

			try {
				this.ks = KeystoreUtils.tryLoadKeystore(new File(config.keystoreFile), config.keystoreType, pass);
			} catch (UnrecoverableKeyException ex) {
				new ErrorDialog(getShell(), Messages.getString("error.KeyStoreStorePass"), BUTTONS.OK).open();
				pass = null;
			}
		}
		config.keystoreStorePass = pass;

		Enumeration<String> aliases = this.ks.aliases();
		while (aliases.hasMoreElements())
			this.cmbKeystoreAlias.add(aliases.nextElement());
	}

	/**
	 * @param fileName
	 */
	protected void performKeystoreFileChanged(String fileName) {
		log.debug("Selected keystore file: " + fileName);
		this.configurationContainer.keystoreFile = fileName;
		KeystoreConfigurationComposite.this.txtKeystoreFile.setText(fileName);
		int i = fileName.lastIndexOf('.');
		if (i > 0) {
			String ext = fileName.substring(i+1);
			if (
					ext.equalsIgnoreCase("p12") ||
					ext.equalsIgnoreCase("pkcs12") ||
					ext.equalsIgnoreCase("pfx"))
				performKeystoreTypeChanged("PKCS12");
			else if (
					ext.equalsIgnoreCase("ks") ||
					ext.equalsIgnoreCase("jks"))
				performKeystoreTypeChanged("JCEKS");
		}
	}

	/**
	 * @param type
	 */
	protected void performKeystoreTypeChanged(String type) {
		log.debug("Selected keystore type: " + type);
		this.configurationContainer.keystoreType = type;
		for (int i = 0; i < this.cmbKeystoreType.getItemCount(); ++i) {
			if (this.keystoreTypes.get(this.cmbKeystoreType.getItem(i)).equals(type)) {
				this.cmbKeystoreType.select(i);
				break;
			}
		}
	}

	protected void performKeystorePassStorageTypeChanged(KeyStorePassStorageType p) {
		this.configurationContainer.keystorePassStorageType = p;
		for (int i=0; i<keystorePassStorageTypeOptions.size(); ++i)
		{
			if (keystorePassStorageTypeOptions.get(i).getLeft() == p)
			{
				this.cmbKeystorePassStoreType.select(i);
				break;
			}
		}
		
		boolean showPasswordFields = (p == KeyStorePassStorageType.DISK);
		this.lblKeystoreKeyPass.setVisible(showPasswordFields);
		this.txtKeystoreKeyPass.setVisible(showPasswordFields);
		this.lblKeystoreStorePass.setVisible(showPasswordFields);
		this.txtKeystoreStorePass.setVisible(showPasswordFields);
	}

	/**
	 * @param storepass
	 */
	protected void performKeystoreStorePassChanged(String storepass) {
		log.debug("Changed keystore store password");
		this.configurationContainer.keystoreStorePass = storepass;
		if (storepass == null)
			this.txtKeystoreStorePass.setText("");
		else
			this.txtKeystoreStorePass.setText(storepass);
	}

	/**
	 * @param alias
	 */
	protected void performKeystoreAliasChanged(String alias) {
		log.debug("Selected keystore alias: " + alias);
		this.configurationContainer.keystoreAlias = alias;
		this.cmbKeystoreAlias.setText(alias);
	}

	/**
	 * @param keypass
	 */
	protected void performKeystoreKeyPassChanged(String keypass) {
		log.debug("Changed keystore key password");
		this.configurationContainer.keystoreKeyPass = keypass;
		if (keypass == null)
			this.txtKeystoreKeyPass.setText("");
		else
			this.txtKeystoreKeyPass.setText(keypass);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * at.asit.pdfover.gui.composites.BaseConfigurationComposite#signerChanged()
	 */
	@Override
	protected void signerChanged() {
		// Nothing to do here (yet)
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

	@Override
	public void initConfiguration(ConfigurationManager provider) {
		ConfigurationDataInMemory config = this.configurationContainer;
		config.keystoreFile = provider.getKeyStoreFilePersistent();
		config.keystoreType = provider.getKeyStoreTypePersistent();
		config.keystoreAlias = provider.getKeyStoreAliasPersistent();
		config.keystorePassStorageType = provider.getKeyStorePassStorageType();
		config.keystoreStorePass = provider.getKeyStoreStorePassPersistent();
		config.keystoreKeyPass = provider.getKeyStoreKeyPassPersistent();
	}

	/*
	 * (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.BaseConfigurationComposite#loadConfiguration
	 * ()
	 */
	@Override
	public void loadConfiguration() {
		// Initialize form fields from configuration Container
		ConfigurationDataInMemory config = this.configurationContainer;
		
		String ks = config.keystoreFile;
		performKeystoreFileChanged(ks);
		performKeystoreTypeChanged(config.keystoreType);
		performKeystorePassStorageTypeChanged(config.keystorePassStorageType);
		performKeystoreStorePassChanged(config.keystoreStorePass);
		try {
			File ksf = new File(ks);
			if (ksf.exists())
				loadKeystore(false);
		} catch (Exception e) {
			log.info("Failed to load keystore on init", e);
		}
		performKeystoreAliasChanged(config.keystoreAlias);
		performKeystoreKeyPassChanged(config.keystoreKeyPass);
	}

	@Override
	public void storeConfiguration(ConfigurationManager store) {
		ConfigurationDataInMemory config = this.configurationContainer;
		store.setKeyStoreFile(config.keystoreFile);
		store.setKeyStoreType(config.keystoreType);
		store.setKeyStoreAlias(config.keystoreAlias);
		store.setKeyStorePassStorageType(config.keystorePassStorageType);
		if (config.keystorePassStorageType == KeyStorePassStorageType.DISK)
		{
			store.setKeyStoreStorePassPersistent(config.keystoreStorePass);
			store.setKeyStoreKeyPassPersistent(config.keystoreKeyPass);
		}
		else if (config.keystorePassStorageType == KeyStorePassStorageType.MEMORY)
		{
			store.setKeyStoreStorePassOverlay(config.keystoreStorePass);
			store.setKeyStoreKeyPassOverlay(config.keystoreKeyPass);
		}
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
		ConfigurationDataInMemory config = this.configurationContainer;
		switch (resumeFrom) {
			case 0:
				String fname = config.keystoreFile;
				
				if (fname.isEmpty())
					break; //no checks required
					
				File f = new File(fname);
				if (!f.exists() || !f.isFile())
					throw new KeystoreDoesntExistException(f, 4); //skip next checks
				// Fall through
			case 1:
				try {
					loadKeystore(true);
				} catch (Exception e) {
					throw new CantLoadKeystoreException(e, 4); //skip next checks
				}
				// Fall through
			case 2:
			{
				String alias = config.keystoreAlias;
				if (!this.ks.containsAlias(alias))
					throw new KeystoreAliasDoesntExistException(alias, 4); //skip next check
				if (!this.ks.isKeyEntry(alias))
					throw new KeystoreAliasNoKeyException(alias, 4); //skip next check
			}
				// Fall through
			case 3:
				try {
					String alias = config.keystoreAlias;
					String keypass = config.keystoreKeyPass;
					if (keypass != null)
					{ /* if no keypass is specified, this will happen at signature time */
						Key key = null;
						while (key == null)
						{
							if (keypass == null)
							{
								keypass = new PasswordInputDialog(
									getShell(),
									Messages.getString("keystore_config.KeystoreKeyPass"),
									Messages.getString("keystore.KeystoreKeyPassEntry")).open();
								
								if (keypass == null)
									throw new UnrecoverableKeyException("User cancelled password input");
							}

							try {
								key = this.ks.getKey(alias, keypass.toCharArray());
							} catch (UnrecoverableKeyException ex) {
								new ErrorDialog(getShell(), Messages.getString("error.KeyStoreKeyPass"), BUTTONS.OK).open();
								keypass = null;
							}
						}
						config.keystoreKeyPass = keypass;
					}
				} catch (Exception e) {
					throw new KeystoreKeyPasswordException(4);
				}
		}
	}

	
	Map<String, String> keystoreTypes;
	private void reloadKeystoreTypeStrings() {
		this.keystoreTypes = new HashMap<String, String>();
		this.keystoreTypes.put(Messages.getString("keystore_config.KeystoreType_PKCS12"), "PKCS12");
		this.keystoreTypes.put(Messages.getString("keystore_config.KeystoreType_JKS"), "JCEKS");
	}

	Vector<Pair<KeyStorePassStorageType, String>> keystorePassStorageTypeOptions;
	private void reloadKeystorePassStorageTypeStrings() {
		keystorePassStorageTypeOptions = new Vector<Pair<KeyStorePassStorageType, String>>();
		java.util.function.BiConsumer<KeyStorePassStorageType, String> add = (k,v) -> {
			keystorePassStorageTypeOptions.add(new ImmutablePair<KeyStorePassStorageType,String>(k,Messages.getString(v)));
		};
		add.accept(null, "keystore_config.SaveToWhere.None");
		add.accept(KeyStorePassStorageType.MEMORY, "keystore_config.SaveToWhere.Memory");
		add.accept(KeyStorePassStorageType.DISK, "keystore_config.SaveToWhere.Disk");

		int n = keystorePassStorageTypeOptions.size();
		cmbKeystorePassStoreType.setVisibleItemCount(n);
		cmbKeystorePassStoreType.setItems();
		for (int i=0; i<n; ++i)
			cmbKeystorePassStoreType.add(keystorePassStorageTypeOptions.get(i).getRight());
	}
	
	
	@Override
	
	public void reloadResources() {
		this.grpKeystore.setText(Messages.getString("keystore_config.Keystore_Title"));
		this.lblKeystoreFile.setText(Messages.getString("keystore_config.KeystoreFile"));
		this.btnBrowse.setText(Messages.getString("common.browse"));
		this.txtKeystoreFile.setToolTipText(Messages.getString("keystore_config.KeystoreFile_ToolTip"));
		this.lblKeystoreType.setText(Messages.getString("keystore_config.KeystoreType"));
		reloadKeystoreTypeStrings();
		this.lblKeystorePassStoreType.setText(Messages.getString("keystore_config.SaveToWhere.Header"));
		reloadKeystorePassStorageTypeStrings();
		this.cmbKeystoreType.setItems(this.keystoreTypes.keySet().toArray(new String[0]));
		this.lblKeystoreStorePass.setText(Messages.getString("keystore_config.KeystoreStorePass"));
		this.txtKeystoreStorePass.setToolTipText(Messages.getString("keystore_config.KeystoreStorePass_ToolTip"));
		this.btnLoad.setText(Messages.getString("keystore_config.Load"));
		this.btnLoad.setToolTipText(Messages.getString("keystore_config.Load_ToolTip"));
		this.lblKeystoreAlias.setText(Messages.getString("keystore_config.KeystoreAlias"));
		this.lblKeystoreKeyPass.setText(Messages.getString("keystore_config.KeystoreKeyPass"));
		this.txtKeystoreKeyPass.setToolTipText(Messages.getString("keystore_config.KeystoreKeyPass_ToolTip"));
	}
}
