package at.asit.pdfover.gui.tests;

import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import at.asit.pdfover.commons.Profile;

public class SignatureUITest extends AbstractSignatureUITest{

    private boolean isNegative = false;

    @ParameterizedTest
    @EnumSource(Profile.class)
    public void testSignaturAutoPosition(Profile profile) throws InterruptedException, IOException, BrokenBarrierException{
        setBaseConfig(profile);
        setCredentials();
        testSignature(isNegative);
    }

    @ParameterizedTest
    @EnumSource(Profile.class)
    public void testSignaturAutoPositionNegative(Profile profile) throws InterruptedException, IOException, BrokenBarrierException{
        isNegative = true;
        setBaseConfig(profile);
        setCredentials();
        testSignature(isNegative);
    }
}
