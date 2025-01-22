package at.asit.pdfover.gui.tests;

import java.io.IOException;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import at.asit.pdfover.commons.Profile;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SignatureUITest extends AbstractSignatureUITest{

	@Order(1)
	@Test
	public void createRefFiles() throws IOException {
		setCredentials();
		testSignature(false, true);
	}

	@Order(2)
	@Test
	public void testSignaturAutoPosition() throws IOException {
		setCredentials();
		testSignature(false, false);
	}

	/*
	@Order(3)
	@ParameterizedTest
	@EnumSource(Profile.class)
	public void testSignaturAutoPositionNegative(Profile profile) throws IOException {
		setCredentials();
		testSignature(true, false);
	}

	 */
}