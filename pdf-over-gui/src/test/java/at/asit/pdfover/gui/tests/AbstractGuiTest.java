package at.asit.pdfover.gui.tests;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.Main;
import at.asit.pdfover.gui.workflow.StateMachine;

public abstract class AbstractGuiTest  {

    private static Thread uiThread;
    private static Shell shell;
    private static StateMachine sm;
    private final static CyclicBarrier swtBarrier = new CyclicBarrier(2);
    private SWTBot bot;
	
    @BeforeAll
    public static synchronized void setupApp() {
        if (uiThread == null) {
            uiThread = new Thread(new Runnable() {
                @Override
	            public void run() {
                    sm = Main.setup(new String[]{"./src/test/java/at/asit/pdfover/gui/tests/TestFile.pdf"});
                    shell = sm.getMainShell();
                    try {
                        // wait for the test setup
                        swtBarrier.await();
                        sm.start();
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

    protected String str(String k) { return Messages.getString(k); }
    protected void dragNDropPdfAndSign() {
        SWTBotShell swtbs = bot.activeShell();
        swtbs.activate();

        bot.toggleButton(str("positioning.newPage")).click();
        bot.toggleButton(str("positioning.removeNewPage")).click();
        bot.button(str("positioning.sign")).click();
    }

}
