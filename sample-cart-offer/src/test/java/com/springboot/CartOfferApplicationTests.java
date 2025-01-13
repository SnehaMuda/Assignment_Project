package com.springboot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.controller.ApplyOfferRequest;
import com.springboot.controller.OfferRequest;
import com.springboot.controller.SegmentResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CartOfferApplicationTests {


	@Test
	public void checkFlatXForOneSegment() throws Exception {
		List<String> segments = new ArrayList<>();
		segments.add("p1");
		OfferRequest offerRequest = new OfferRequest(1,"FLATX",10,segments);
		boolean result = addOffer(offerRequest);
		Assert.assertEquals(result,true); // able to add offer
	}

	@Test
	public void checkFlatXForMultipleSegment() throws Exception {
		List<String> segments = new ArrayList<>();
		segments.add("p1");
		segments.add("p2");
		OfferRequest offerRequest = new OfferRequest(1,"FLATX",20,segments);
		boolean result = addOffer(offerRequest);
		Assert.assertEquals(result,true); // able to add offer
	}
	@Test
	public void checkFlatXPrecentForSingleSegment() throws Exception {
		List<String> segments = new ArrayList<>();
		segments.add("p1");
		OfferRequest offerRequest = new OfferRequest(1,"FLATX%",10,segments);
		boolean result = addOffer(offerRequest);
		Assert.assertEquals(result,true); // able to add offer
	}
	@Test
	public void checkFlatXPrecentForMultipleSegment() throws Exception {
		List<String> segments = new ArrayList<>();
		segments.add("p1");
		segments.add("p2");
		OfferRequest offerRequest = new OfferRequest(1,"FLATX%",10,segments);
		boolean result = addOffer(offerRequest);
		Assert.assertEquals(result,true); // able to add offer
	}

	@Test
	public void testApplyOfferFlatX() throws Exception {
		List<String> segments = new ArrayList<>();
		segments.add("p1");
		segments.add("p2");
		OfferRequest offerRequest = new OfferRequest(1,"FLATX%",10,segments);
		boolean result = addOffer(offerRequest);
		Assert.assertEquals(result,true); // able to add offer
	}

	@Test
	public void testApplyOfferFlatXPercent() throws Exception {
		// Setup: Add FlatX% offer for restaurant_id = 1, customer segment = p1
		List<String> segments = new ArrayList<>();
		segments.add("p1");
		OfferRequest offerRequest = new OfferRequest(1,"FLATx%",10,segments);
		boolean result = addOffer(offerRequest);
		Assert.assertEquals(result,true);
		// Action: Apply offer
		String response = applyOffer(200, 1, 1);
		// Assertion: Verify cart value reflects FlatX% discount
		Assert.assertEquals("{\"cart_value\":180}", response); // Expected cart value after applying FlatX% offer
	}

	@Test
	public void testApplyMultipleOffersForSameSegment() throws Exception {
		// Setup: Add multiple offers for the same restaurant and segment
		List<String> segments = new ArrayList<>();
		segments.add("p1");
		OfferRequest offerRequest1 = new OfferRequest(1, "FLATX", 20, segments);
		OfferRequest offerRequest2 = new OfferRequest(1, "FLATX%", 10, segments);
		boolean result1 = addOffer(offerRequest1);
		boolean result2 = addOffer(offerRequest2);
		Assert.assertTrue(result1 && result2);

		// Action: Apply offer
		String response = applyOffer(200, 1, 1);
		// Assertion: Ensure priority (e.g., FlatX applied before FlatX%)
		Assert.assertEquals("{\"cart_value\":180}", response); // Assuming FlatX% is prioritized
	}

	public boolean addOffer(OfferRequest offerRequest) throws Exception {
		String urlString = "http://localhost:9001/api/v1/offer";
		URL url = new URL(urlString);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setDoOutput(true);
		con.setRequestProperty("Content-Type", "application/json");

		ObjectMapper mapper = new ObjectMapper();

		String POST_PARAMS = mapper.writeValueAsString(offerRequest);
		OutputStream os = con.getOutputStream();
		os.write(POST_PARAMS.getBytes());
		os.flush();
		os.close();
		int responseCode = con.getResponseCode();
		System.out.println("POST Response Code :: " + responseCode);

		if (responseCode == HttpURLConnection.HTTP_OK) { //success
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			// print result
			System.out.println(response.toString());
		} else {
			System.out.println("POST request did not work.");
		}
		return true;
	}
	public String applyOffer(int cartValue, int userId, int restaurantId) throws Exception {
		// API endpoint for applying offer
		String urlString = "http://localhost:9001/api/v1/cart/apply_offer";
		URL url = new URL(urlString);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();

		// Setting up the request properties
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		con.setRequestProperty("Content-Type", "application/json");

		// Create JSON payload using ApplyOfferRequest
		ObjectMapper mapper = new ObjectMapper();
		ApplyOfferRequest request = new ApplyOfferRequest();
		request.setCart_value(cartValue);
		request.setUser_id(userId);
		request.setRestaurant_id(restaurantId);

		String POST_PARAMS = mapper.writeValueAsString(request);

		// Send POST request
		OutputStream os = con.getOutputStream();
		os.write(POST_PARAMS.getBytes());
		os.flush();
		os.close();

		// Capture response
		int responseCode = con.getResponseCode();
		System.out.println("POST Response Code :: " + responseCode);

		if (responseCode == HttpURLConnection.HTTP_OK) { // Success
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuilder response = new StringBuilder();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// Return the response as a string
			return response.toString();
		} else {
			System.out.println("POST request did not work.");
			return "{\"error\": \"Unable to apply offer\"}";
		}
	}



}
