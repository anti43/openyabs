package mpv5.utils.export;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ExportAddressesTest {

    @Test
    void combinesAndValidatesCopyAddresses() {
        assertEquals("archiv@muster.example;chef@muster.example",
                Export.validAddresses("archiv@muster.example", " chef@muster.example ; kein-mail ;archiv@muster.example", null));
        assertEquals("", Export.validAddresses(null, "", " ; "));
    }
}
