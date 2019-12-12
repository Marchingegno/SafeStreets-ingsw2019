package it.polimi.marcermarchiscianamotta.safestreets.util;

import android.Manifest;
import android.content.Context;
import android.location.Address;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;

import static org.junit.Assert.*;

public class MapManagerTest {

	Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
	MapManager mapManager = new MapManager();
	double latitude = 45.984517;
	double longitude = 12.702901;

	@Test
	public void getCityFromLocation_normalInput_correctOutput() {
		String result = mapManager.getCityFromLocation(appContext, latitude, longitude);
		assertEquals("Cordenons",result);
	}
}