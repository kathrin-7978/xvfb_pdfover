package at.asit.pdfover.gui.tests;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.commons.Constants;
import at.asit.pdfover.commons.Messages;
import at.asit.pdfover.commons.Profile;
import at.asit.pdfover.gui.Main;
import at.asit.pdfover.gui.workflow.StateMachine;
import at.asit.pdfover.gui.workflow.states.ConfigurationUIState;
import org.apache.commons.io.FileUtils;

public abstract class AbstractSignatureUITest {

    private static Thread uiThread;
    private static Shell shell;
    private static StateMachine sm;
    private SWTBotShell swtbs;
    private SWTBot bot;

    private static File inputFile = new File("src/test/resources/TestFile.pdf");
    private static String outputDir = inputFile.getAbsoluteFile().getParent();
    private String postFix = "_superSigned";
    private Profile currentProfile;

    private static final Logger logger = LoggerFactory
            .getLogger(AbstractSignatureUITest.class);

    SignaturePositionTestProvider provider = new SignaturePositionTestProvider();
    private static Path tmpDir;

    @BeforeAll
    public static void createTempDir() throws IOException {
        deleteTempDir();
        tmpDir = Files.createTempDirectory(Paths.get(inputFile.getAbsoluteFile().getParent()), "output_");
        tmpDir.toFile().deleteOnExit();
        outputDir = tmpDir.toString();
    }

    private static void deleteTempDir() throws IOException {
        String root = inputFile.getAbsoluteFile().getParent();
        File dir = new File(root);
        for (File f : dir.listFiles()) {
            if (f.getName().startsWith("output_")) {
                FileUtils.deleteDirectory(f);
            }
        }
    }

    /**
     * workaround for setup until
     * file selection works automatically
     *
     * @throws BrokenBarrierException
     * @throws InterruptedException
     * @throws IOException
     */

    @BeforeEach
    public final void setup() throws BrokenBarrierException, InterruptedException, IOException {
        String[] args = null;
        boolean setup = true;
        while (setup) {
            args = new String[]{};
            logger.info("setup initial UI config");
            setupSWTBot(args);
            setAdvancedUIConfig();
            closeShell();
            setup = false;
        }
        args = new String[]{inputFile.getAbsolutePath()};
        setupSWTBot(args);
        }

    public final void setupSWTBot(String[] cmdLineArgs) throws InterruptedException, BrokenBarrierException, IOException {
        final CyclicBarrier swtBarrier = new CyclicBarrier(2);

        if (uiThread == null) {
            uiThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    sm = Main.setup(cmdLineArgs);
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
        // synchronize with the thread opening the shell
        swtBarrier.await();

        bot =  new SWTBot(shell);
        swtbs = bot.activeShell();
        swtbs.activate();
    }

    @AfterEach
    public void reset() throws InterruptedException, IOException {
        logger.info("reset config");
        resetAdvancedUIConfig();
        deleteOutputFile();
        closeShell();
    }

    public void closeShell() throws InterruptedException, IOException {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                shell.close();
            }
        });
        uiThread.join();
        uiThread = null;
    }

    protected String str(String k) { return Messages.getString(k); }

    protected void setCredentials() throws InterruptedException, IOException, BrokenBarrierException {
        try {
            bot.textWithLabel(str("mobileBKU.number")).setText("testuser");
            bot.textWithLabel(str("mobileBKU.password")).setText("testuser-password");
            bot.button(str("common.Ok")).click();
            bot.sleep(20000);
        }
        catch (WidgetNotFoundException wnf) {
            bot.button(str("common.Cancel")).click();
            fail(wnf.getMessage());
        }

        File output = new File(getPathOutputFile());
        if(!output.exists()) {
            bot.button(str("common.Cancel")).click();
        }
        assertTrue(output.exists(), "Received signed PDF");
    }

    private void deleteOutputFile() {
        if (getPathOutputFile() != null) {
            File outputFile = new File(getPathOutputFile());
            outputFile.delete();
            assertTrue(!outputFile.exists());
            logger.info("Deleted output file");
        }
    }

    protected void setBaseConfig(Profile profile) throws InterruptedException, IOException {
        try {
            bot.button(str("common.Cancel")).click();
            bot.sleep(2000);
            sm.jumpToState(new ConfigurationUIState(sm));
            //bot.sleep(2000);

            switch(profile) {
            case AMTSSIGNATURBLOCK:
                bot.comboBoxInGroup(str("simple_config.SigProfile_Title"))
                        .setSelection(str("simple_config.AMTSSIGNATURBLOCK"));
                currentProfile = profile;
                break;
            case SIGNATURBLOCK_SMALL:
                bot.comboBoxInGroup(str("simple_config.SigProfile_Title"))
                        .setSelection(str("simple_config.SIGNATURBLOCK_SMALL"));
                currentProfile = profile;
                break;
            case BASE_LOGO:
                bot.comboBoxInGroup(str("simple_config.SigProfile_Title"))
                        .setSelection(str("simple_config.BASE_LOGO"));
                currentProfile = profile;
                break;
            case INVISIBLE:
                bot.comboBoxInGroup(str("simple_config.SigProfile_Title"))
                        .setSelection(str("simple_config.INVISIBLE"));
                currentProfile = profile;
                break;
            default:
                break;
            }

            bot.button(str("common.Save")).setFocus();
            bot.button(str("common.Save")).click();
            bot.button(str("bku_selection.mobile")).click();

        } catch (WidgetNotFoundException wnf) {
            bot.button(str("common.Cancel")).setFocus();
            bot.button(str("common.Cancel")).click();
            bot.button(str("bku_selection.mobile")).click();
            bot.button(str("common.Cancel")).click();
            fail(wnf.getMessage());
        }
        logger.info("Current signature profile: " + currentProfile);
        }

    protected void testSignature(boolean negative) throws IOException, InterruptedException {
        String outputFile = getPathOutputFile();
        assertNotNull(currentProfile);
        assertNotNull(outputFile);
        provider.checkSignaturePosition(currentProfile, negative, getPathOutputFile());
    }

    private String getPathOutputFile() {
        String inputFileName = inputFile.getName();
        String pathOutputFile = inputFileName
                .substring(0, inputFileName.lastIndexOf('.'))
                .concat(postFix)
                .concat(".pdf");
        pathOutputFile = outputDir.concat("\\")
                .concat(pathOutputFile);
        assertNotNull(pathOutputFile);
        return pathOutputFile;
    }

    protected void setAdvancedUIConfig() throws InterruptedException, IOException {
        try {
            bot.sleep(2000);
            sm.jumpToState(new ConfigurationUIState(sm));
            //bot.sleep(2000);
            bot.tabItem(str("config.Advanced")).activate();
            if (!bot.checkBox(str("advanced_config.AutoPosition")).isChecked()) {
                bot.checkBox(str("advanced_config.AutoPosition")).click();
            }
            //bot.sleep(2000);
            bot.textWithLabel(str("advanced_config.OutputFolder")).setFocus();
            bot.textWithLabel(str("advanced_config.OutputFolder")).setText(outputDir);
            //bot.sleep(2000);
            bot.textWithLabel(str("AdvancedConfigurationComposite.lblSaveFilePostFix.text")).setFocus();
            bot.textWithLabel(str("AdvancedConfigurationComposite.lblSaveFilePostFix.text")).setText(postFix);
            //bot.sleep(2000);
            bot.button(str("common.Save")).setFocus();
            bot.button(str("common.Save")).click();
        }
        catch (WidgetNotFoundException wnf) {
            bot.button(str("common.Cancel")).setFocus();
            bot.button(str("common.Cancel")).click();
            fail(wnf.getMessage());
        }
    }

    protected void resetAdvancedUIConfig() {
        try {
            bot.sleep(2000);
            sm.jumpToState(new ConfigurationUIState(sm));
            bot.sleep(2000);
            bot.tabItem(str("config.Advanced")).activate();
            if (bot.checkBox(str("advanced_config.AutoPosition")).isChecked()) {
                bot.checkBox(str("advanced_config.AutoPosition")).click();
            }
//            bot.sleep(2000);
            bot.textWithLabel(str("advanced_config.OutputFolder")).setFocus();
            bot.textWithLabel(str("advanced_config.OutputFolder")).setText("");
//            bot.sleep(2000);
            bot.textWithLabel(str("AdvancedConfigurationComposite.lblSaveFilePostFix.text")).setFocus();
            bot.textWithLabel(str("AdvancedConfigurationComposite.lblSaveFilePostFix.text")).setText(Constants.DEFAULT_POSTFIX);
//            bot.sleep(2000);
            bot.button(str("common.Save")).setFocus();
            bot.button(str("common.Save")).click();
        }
        catch (WidgetNotFoundException wnf) {
            bot.button(str("common.Cancel")).setFocus();
            bot.button(str("common.Cancel")).click();
        }
    }
}
