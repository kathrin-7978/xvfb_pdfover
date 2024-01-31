package at.asit.pdfover.gui.tests;

import java.io.File;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.states.PositioningState;

public abstract class AbstractGuiTest  {

	private static Thread uiThread;
	private static Shell shell;
	private static StateMachine sm = new StateMachine(new String[0]);
	private final static CyclicBarrier swtBarrier = new CyclicBarrier(2);
	private SWTBot bot;
	
	@BeforeAll
	public static synchronized void setupApp() {
		if (uiThread == null) {
			uiThread = new Thread(new Runnable() {

				@Override
				public void run() {	
					shell = sm.getMainShell();
					try {	
						while (true) {
							// wait for the test setup
							swtBarrier.await();
							sm.start();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			uiThread.setDaemon(true);
			uiThread.start();
		}
	}

	@BeforeEach
	public final void setupSWTBot() throws InterruptedException, BrokenBarrierException {
		// synchronize with the thread opening the shell
		swtBarrier.await();		
		bot = new SWTBot(shell);
	}

	@AfterEach
	public void closeShell() throws InterruptedException {
		// close the shell
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				shell.close();
			}
		});
	}

	protected void dragNDropPdfAndSign() {
		SWTBotShell swtbs = bot.activeShell();
		swtbs.activate();
		
		sm.jumpToState(new PositioningState(sm));
	    String fileName = ".\\src\\test\\java\\at\\asit\\pdfover\\gui\\tests\\TestFile.pdf";
		File documentPath = new File(fileName);
		sm.status.document = documentPath;

		bot.toggleButton().click();
		bot.toggleButton("&Neue Seite rückgängig").click();
		bot.button("&Signieren").click();

	}

}
