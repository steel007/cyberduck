package ch.cyberduck.core.aquaticprime;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.test.Depends;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
@Depends(platform = Factory.Platform.Name.mac)
public class ReceiptVerifierTest extends AbstractTestCase {

    @Test
    public void testVerify() throws Exception {
        ReceiptVerifier r = new ReceiptVerifier(new Local("/Applications/Cyberduck.app/Contents/_MASReceipt/receipt"));
        assertTrue(r.verify());
        assertEquals("c42c030b8670", r.getGuid());
    }

    @Test
    public void testVerifyFailure() throws Exception {
        ReceiptVerifier r = new ReceiptVerifier(new Local("test/ch/cyberduck/core/aquaticprime/Info.plist"));
        assertFalse(r.verify());
        assertEquals(null, r.getGuid());
    }
}
