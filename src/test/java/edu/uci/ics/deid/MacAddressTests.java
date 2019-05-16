package edu.uci.ics.deid;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.uci.ics.deid.model.MacAddress;

class MacAddressTests {

	@Test
	@DisplayName("MAC address String To Long")
	void macStringToLong() {
        long macLongResult = 18838586676582L;
		MacAddress macAddr = new MacAddress("11 22 33 44 55 66");
		assertEquals(macLongResult, macAddr.getMacAddrLong(), "Mac Address (String):11 22 33 44 55 66 should be equal to 18838586676582 in decimal");
	}

    @Test
	@DisplayName("MAC address Long To String")
	void macLongToString() {
		MacAddress macAddr = new MacAddress(18838586676582L);
		assertEquals("11 22 33 44 55 66", macAddr.getMacAddrStr(), "Mac Address (String):11 22 33 44 55 66 should be equal to 18838586676582 in decimal");
	}
}