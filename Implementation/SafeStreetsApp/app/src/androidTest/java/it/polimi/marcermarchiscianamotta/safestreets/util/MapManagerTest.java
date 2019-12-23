package it.polimi.marcermarchiscianamotta.safestreets.util;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MapManagerTest {

	private Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
	private MapManager mapManager = new MapManager();

	@Test
	public void getCityFromLocation_normalInput_correctOutput() {
		double latitude = 45.984517;
		double longitude = 12.702901;
		String result = MapManager.getAddressFromLocation(appContext, latitude, longitude);
		assertEquals("Cordenons",result);
	}
}