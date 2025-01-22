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

	@Test
	public void simpleUITest1() throws IOException {
		setBaseConfig();
	}

	@Test
	public void simpleUITest2() throws IOException {
		setBaseConfig();
	}

}