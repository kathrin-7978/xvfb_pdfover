package at.asit.pdfover.gui.utils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SimpleUITest {

    private SWTBot bot;
    private Shell shell;

    @BeforeEach
    public void setup() {
        Display display = new Display();
        shell = new Shell(display);
        shell.setText("Simple Test Shell");
        shell.setSize(300, 200);

        Button button = new Button(shell, SWT.PUSH);
        button.setText("Click Me");
        button.setBounds(100, 80, 100, 30);
        button.addListener(SWT.Selection, event -> {
            System.out.println("Button clicked in test!");
        });
        shell.open();

        bot = new SWTBot(shell);
    }

    @Test
    public void testButtonClick() {
        assertTrue(bot.button("Click Me").isVisible());
    }

    @AfterEach
    public void tearDown() {
        shell.close();
        Display.getDefault().dispose();
    }
}