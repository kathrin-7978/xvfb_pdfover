package at.asit.pdfover.gui.ui;

import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.Main;
import at.asit.pdfover.gui.workflow.StateMachine;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.junit.jupiter.api.BeforeEach;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AbstractSignatureUiTest {

    private static Thread uiThread;
    private static Shell shell;
    private static StateMachine sm;
    private SWTBotShell swtbs;
    private SWTBot bot;

    private static File inputFile = new File("src/test/resources/TestFile.pdf");
    protected String str(String k) { return Messages.getString(k); }

    @BeforeEach
    public final void setupUITest() throws InterruptedException, BrokenBarrierException {
        final CyclicBarrier swtBarrier = new CyclicBarrier(2);

        Display display = Display.getDefault();
        if (display == null) {
            display = new Display();
        }

        if (uiThread == null) {
            Display finalDisplay = display;
            uiThread = new Thread(() -> {
                shell = new Shell(finalDisplay);
                sm = Main.setup(new String[]{inputFile.getAbsolutePath()});
                shell = sm.getMainShell();
                try {
                    swtBarrier.await();
                    sm.start();
                    // Event loop for the display
                    while (!shell.isDisposed()) {
                        if (!finalDisplay.readAndDispatch()) {
                            finalDisplay.sleep();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    finalDisplay.dispose();
                }
            });
            uiThread.setDaemon(true);
            uiThread.start();
        }
        swtBarrier.await();

        bot = new SWTBot(shell);
        swtbs = bot.activeShell();
        swtbs.activate();
    }


    @Test
    public void buttonClickTest() throws Exception {
        ICondition widgetExists = new WidgetExitsCondition(str("mobileBKU.numb3r"));
        bot.waitUntil(widgetExists, 20000);
        assertTrue(widgetExists.test(), "The widget should exist after test setup.");
    }
}