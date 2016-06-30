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
package at.asit.pdfover.gui.composites;

// Imports
import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.Constants;
import at.asit.pdfover.gui.utils.Messages;
import at.asit.pdfover.gui.workflow.states.State;

/**
 * Composite for input document selection
 */
public class DataSourceSelectComposite extends StateComposite {

	/**
	 * Open the input document selection dialog
	 */
	public void openFileDialog() {
		FileDialog dialog = new FileDialog(
				DataSourceSelectComposite.this.getShell(), SWT.OPEN);
		dialog.setFilterExtensions(new String[] { "*.pdf", "*" }); //$NON-NLS-1$ //$NON-NLS-2$
		dialog.setFilterNames(new String[] {
				Messages.getString("common.PDFExtension_Description"),  //$NON-NLS-1$
				Messages.getString("common.AllExtension_Description") }); //$NON-NLS-1$
		String fileName = dialog.open();
		File file = null;
		if (fileName != null) {
			file = new File(fileName);
			if (file.exists()) {
				DataSourceSelectComposite.this.setSelected(file);
			}
		}
	}

	/**
	 * Selection adapter for file browsing
	 */
	private final class FileBrowseDialogListener extends SelectionAdapter {
		/**
		 * Empty constructor
		 */
		public FileBrowseDialogListener() {
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			openFileDialog();
		}
	}

	/**
	 * SLF4J Logger instance
	 **/
	static final Logger log = LoggerFactory
			.getLogger(DataSourceSelectComposite.class);

	/**
	 * Set this value through the setter method!!
	 */
	private File selected = null;

	/**
	 * Sets the selected file and calls update to the workflow
	 * 
	 * @param selected
	 */
	protected void setSelected(File selected) {
		this.selected = selected;
		this.state.updateStateMachine();
	}

	/**
	 * Gets the selected file
	 * 
	 * @return the selected file
	 */
	public File getSelected() {
		return this.selected;
	}

	void MarkDragEnter() {
		this.backgroundColor = this.activeBackground;
		this.borderColor = this.activeBorder;
		this.redrawDrop();
	}
	
	void MarkDragLeave() {
		this.backgroundColor = this.inactiveBackground;
		this.borderColor = this.inactiveBorder;
		this.redrawDrop();
	}
	
	void redrawDrop() {
		this.lbl_drag.setBackground(this.backgroundColor);
		this.lbl_drag2.setBackground(this.backgroundColor);
		this.btn_open.setBackground(this.backgroundColor);
		this.drop_area.redraw();
		this.drop_area.layout(true, true);
	}
	
	Color activeBackground;
	Color inactiveBackground;
	Color inactiveBorder;
	Color activeBorder;
	Color borderColor;
	Color backgroundColor;
	
	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 * @param state
	 */
	public DataSourceSelectComposite(Composite parent, int style, State state) {
		super(parent, style, state);

		this.activeBackground = Constants.MAINBAR_ACTIVE_BACK_LIGHT;
		this.inactiveBackground = this.getBackground();//Constants.MAINBAR_INACTIVE_BACK;
		this.inactiveBorder = Constants.MAINBAR_ACTIVE_BACK_LIGHT;
		this.activeBorder = Constants.MAINBAR_ACTIVE_BACK_DARK;
		this.backgroundColor = this.inactiveBackground;
		this.borderColor = Constants.DROP_BORDER_COLOR;
		
		this.setLayout(new FormLayout());

		// Color back = new Color(Display.getCurrent(), 77, 190, 250);

		this.drop_area = new Composite(this, SWT.RESIZE);
		FormData fd_drop_area = new FormData();
		fd_drop_area.left = new FormAttachment(0, 30);
		fd_drop_area.right = new FormAttachment(100, -30);
		fd_drop_area.top = new FormAttachment(0, 30);
		fd_drop_area.bottom = new FormAttachment(100, -30);
		this.drop_area.setLayoutData(fd_drop_area);
		this.drop_area.setLayout(new FormLayout());
		
		this.drop_area.addPaintListener(new PaintListener() {
			
			@Override
			public void paintControl(PaintEvent e) {
				Rectangle clientArea = DataSourceSelectComposite.this
						.drop_area.getClientArea();
				
				//e.gc.setForeground(new Color(getDisplay(),0x6B, 0xA5, 0xD9));
				e.gc.setForeground(DataSourceSelectComposite.this.borderColor);
				e.gc.setLineWidth(3);
				e.gc.setLineStyle(SWT.LINE_DASH);
				e.gc.setBackground(DataSourceSelectComposite.this.backgroundColor);
				e.gc.fillRoundRectangle(clientArea.x, 
						clientArea.y, clientArea.width - 2, clientArea.height - 2, 
						10, 10);
				e.gc.drawRoundRectangle(clientArea.x, 
						clientArea.y, clientArea.width - 2, clientArea.height - 2, 
						10, 10);
			}
		});
		
		DropTarget dnd_target = new DropTarget(this.drop_area, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
		final FileTransfer fileTransfer = FileTransfer.getInstance();
		Transfer[] types = new Transfer[] { fileTransfer };
		dnd_target.setTransfer(types);

		dnd_target.addDropListener(new DropTargetAdapter() {
			@Override
			public void drop(DropTargetEvent event) {
				if (fileTransfer.isSupportedType(event.currentDataType)) {
					if (event.data == null) {
						log.error("Dropped file name was null"); //$NON-NLS-1$
						return;
					}
					String[] files = (String[]) event.data;
					if (files.length > 0) {
						// Only taking first file ...
						File file = new File(files[0]);
						if (!file.exists()) {
							log.error(String.format(Messages.getString("error.FileNotExist"), files[0])); //$NON-NLS-1$
							return;
						}
						DataSourceSelectComposite.this.setSelected(file);
					}
				}
			}

			@Override
			public void dragOperationChanged(DropTargetEvent event) {
				event.detail = DND.DROP_COPY;
			}

			@Override
			public void dragEnter(DropTargetEvent event) {
				// only accept transferable files
				for (int i = 0; i < event.dataTypes.length; i++) {
					if (fileTransfer.isSupportedType(event.dataTypes[i])) {
						event.currentDataType = event.dataTypes[i];
						event.detail = DND.DROP_COPY;
						MarkDragEnter();
						return;
					}
				}
				event.detail = DND.DROP_NONE;
			}

			/* (non-Javadoc)
			 * @see org.eclipse.swt.dnd.DropTargetAdapter#dragLeave(org.eclipse.swt.dnd.DropTargetEvent)
			 */
			@Override
			public void dragLeave(DropTargetEvent event) {
				MarkDragLeave();
				super.dragLeave(event);
			}
		});

		this.lbl_drag2 = new Label(this.drop_area, SWT.NONE | SWT.RESIZE );
		
		this.lbl_drag = new Label(this.drop_area, SWT.NONE | SWT.RESIZE );
		this.fd_lbl_drag = new FormData();
		this.fd_lbl_drag.left = new FormAttachment(0, 10);
		this.fd_lbl_drag.right = new FormAttachment(100, -10);
		//this.fd_lbl_drag.top = new FormAttachment(40, -10);
		this.fd_lbl_drag.bottom = new FormAttachment(this.lbl_drag2, -10);
		this.lbl_drag.setLayoutData(this.fd_lbl_drag);
		FontData[] fD = this.lbl_drag.getFont().getFontData();
		fD[0].setHeight(Constants.TEXT_SIZE_BIG);
		this.lbl_drag.setFont(new Font(Display.getCurrent(), fD[0]));
		this.lbl_drag.setText(Messages.getString("dataSourceSelection.DropLabel")); //$NON-NLS-1$
		this.lbl_drag.setAlignment(SWT.CENTER);
		
		
		this.fd_lbl_drag2 = new FormData();
		this.fd_lbl_drag2.left = new FormAttachment(0, 10);
		this.fd_lbl_drag2.right = new FormAttachment(100, -10);
		this.fd_lbl_drag2.top = new FormAttachment(50, -10);
		// fd_lbl_drag.bottom = new FormAttachment(100, -10);
		this.lbl_drag2.setLayoutData(this.fd_lbl_drag2);
		FontData[] fD2 = this.lbl_drag2.getFont().getFontData();
		fD2[0].setHeight(Constants.TEXT_SIZE_NORMAL);
		this.lbl_drag2.setFont(new Font(Display.getCurrent(), fD2[0]));
		this.lbl_drag2.setText(Messages
				.getString("dataSourceSelection.DropLabel2")); //$NON-NLS-1$
		this.lbl_drag2.setAlignment(SWT.CENTER);
		
		this.btn_open = new Button(this.drop_area, SWT.NATIVE | SWT.RESIZE);
		this.btn_open.setText(Messages.getString("dataSourceSelection.browse")); //$NON-NLS-1$
		
		FontData[] fD_open = this.btn_open.getFont().getFontData();
		fD_open[0].setHeight(Constants.TEXT_SIZE_BUTTON);
		this.btn_open.setFont(new Font(Display.getCurrent(), fD_open[0]));
		
		/*
		lbl_drag.addListener(SWT.Resize, new Listener() {

			@Override
			public void handleEvent(Event event) {
				DataSourceSelectComposite.this.fd_lbl_drag.top = new FormAttachment(
						50, -1 * (lbl_drag.getSize().y / 2));
				DataSourceSelectComposite.this.fd_lbl_drag.left = new FormAttachment(
						50, -1 * (lbl_drag.getSize().x / 2));
				
				Point size = btn_open.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				DataSourceSelectComposite.this.fd_btn_open.top = new FormAttachment(
						50, (lbl_drag.getSize().y / 2) + 10);
				DataSourceSelectComposite.this.fd_btn_open.left = new FormAttachment(
						50, -1 * (size.x / 2));
				DataSourceSelectComposite.this.fd_btn_open.right = new FormAttachment(
						50, (size.x / 2));
				DataSourceSelectComposite.this.fd_btn_open.bottom = new FormAttachment(
						50, (lbl_drag.getSize().y / 2) + 10 + size.y);
			}
		});
		*/
		// lbl_drag.setBackground(back);

		this.fd_btn_open = new FormData();
		this.fd_btn_open.left = new FormAttachment(this.lbl_drag2, 0, SWT.CENTER);
		this.fd_btn_open.top = new FormAttachment(this.lbl_drag2, 10);
		this.btn_open.setLayoutData(this.fd_btn_open);

		// btn_open.setBackground(back);
		this.btn_open.addSelectionListener(new FileBrowseDialogListener());
		this.drop_area.pack();
		this.redrawDrop();
	}

	Composite drop_area;

	FormData fd_lbl_drag;
	FormData fd_lbl_drag2;

	FormData fd_btn_open;

	private Label lbl_drag2;

	private Label lbl_drag;

	private Button btn_open;

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.asit.pdfover.gui.components.StateComposite#doLayout()
	 */
	@Override
	public void doLayout() {
		this.layout(true, true);
		this.drop_area.layout(true, true);
	}

	/* (non-Javadoc)
	 * @see at.asit.pdfover.gui.composites.StateComposite#reloadResources()
	 */
	@Override
	public void reloadResources() {
		this.lbl_drag.setText(Messages.getString("dataSourceSelection.DropLabel")); //$NON-NLS-1$
		this.btn_open.setText(Messages.getString("dataSourceSelection.browse")); //$NON-NLS-1$
		this.lbl_drag2.setText(Messages.getString("dataSourceSelection.DropLabel2")); //$NON-NLS-1$
	}
}
