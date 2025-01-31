package at.asit.pdfover.gui.tests;

import java.io.File;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.gui.Main;
import at.asit.pdfover.gui.workflow.StateMachine;

import static org.junit.jupiter.api.Assertions.*;

public class SignatureUITest {

    private static Thread uiThread;
    private static Shell shell;
    private static StateMachine sm;
    private SWTBot bot;

    private static final File inputFile = new File("src/test/resources/TestFile.pdf");

    protected String str(String k) {
        return Messages.getString(k);
    }

    @BeforeEach
    public final void setupUITest() throws InterruptedException, BrokenBarrierException {
        final CyclicBarrier swtBarrier = new CyclicBarrier(2);

        if (uiThread == null) {
            uiThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        swtBarrier.await();
                        Display.getDefault().syncExec(() -> {
                            sm = Main.setup(new String[]{inputFile.getAbsolutePath()});
                            shell = sm.getMainShell();
                            sm.start();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            uiThread.setDaemon(true);
            uiThread.start();
        }
        swtBarrier.await();

        Display.getDefault().syncExec(() -> {
            bot = new SWTBot(shell);
        });
    }

    @AfterEach
    public void reset() throws InterruptedException {
        closeShell();
    }

    public void closeShell() throws InterruptedException {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                shell.close();
            }
        });
        uiThread.join();
        uiThread = null;
    }

    @Test
    protected void setCredentials() {
        try {
            ICondition widgetExists = new WidgetExitsCondition(str("mobileBKU.number"));
            bot.waitUntil(widgetExists, 20000);
            bot.textWithLabel(str("mobileBKU.number")).setText("TestUser-1902503362");
        } catch (WidgetNotFoundException wnf) {
            bot.button(str("common.Cancel")).click();
            fail(wnf.getMessage());
        }
    }
}