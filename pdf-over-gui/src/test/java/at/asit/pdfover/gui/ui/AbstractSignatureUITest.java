package at.asit.pdfover.gui.ui;

import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.Main;
import at.asit.pdfover.gui.workflow.StateMachine;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;


public class AbstractSignatureUITest {

    private SWTBot bot;
    private static Thread uiThread;
    private static Shell shell;
    private static StateMachine sm;
    private SWTBotShell swtbs;

    private static final File inputFile = new File("src/test/resources/TestFile.pdf");
    protected String str(String k) { return Messages.getString(k); }

    @BeforeEach
    public final void setupUITest() throws InterruptedException, BrokenBarrierException {
        final CyclicBarrier swtBarrier = new CyclicBarrier(2);

        if (uiThread == null) {
            uiThread = new Thread(() -> {
              //  Display.getDefault().syncExec(() -> {
                    sm = Main.setup(new String[]{inputFile.getAbsolutePath()});
                    shell = sm.getMainShell();
                    try {
                        swtBarrier.await();
                        sm.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
              //  });
            });
            uiThread.setDaemon(true);
            uiThread.start();
        }
        swtBarrier.await();

        // Create SWTBot on the main UI thread
        Display.getDefault().syncExec(() -> {
            bot = new SWTBot(shell);
            swtbs = bot.activeShell();
            swtbs.activate();
        });
    }

    @Test
    public void buttonClick() {

        ICondition widgetExists = new WidgetExitsCondition(str("mobileBKU.number"));
        bot.waitUntil(widgetExists, 20000);
    }

    @AfterEach
    public void tearDown() throws InterruptedException {
        closeShell();
    }

    public void closeShell() throws InterruptedException {
        Display.getDefault().syncExec(() -> shell.close());
        uiThread.join();
        uiThread = null;
    }
}


