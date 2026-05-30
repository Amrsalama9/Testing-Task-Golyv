# Test Cases – Cairo to Marsa Alam Trip Planner

Scenario: A user opens Google, searches for flights from Cairo to Marsa Alam, checks the weather there, then looks for restaurants nearby and sorts them by rating.

---

## Flights – Google Flights UI

**TC_FL_001**
Open https://www.google.com/travel/flights and check the page loads properly.
Expected: the page title says "Flights" and the search form is visible.
Priority: P1

**TC_FL_002**
Click the origin field and type "Cairo". Check the autocomplete dropdown shows up.
Expected: Cairo International Airport (CAI) appears as an option in the dropdown.
Priority: P1

**TC_FL_003**
After setting Cairo as origin, click destination and type "Marsa Alam".
Expected: Marsa Alam (RMF) shows up in the suggestions.
Priority: P1

**TC_FL_004**
Set origin = Cairo, destination = Marsa Alam, click Search.
Expected: At least one flight result appears. The search shouldn't come back empty.
Priority: P1

**TC_FL_005**
After the search, check the page header or title.
Expected: Both cities are mentioned somewhere — either the full names or the airport codes CAI and RMF. If neither appears, the results page isn't confirming the route correctly.
Priority: P2

**TC_FL_006**
Look at the first result card on the results page.
Expected: The airline name is visible. It shouldn't be blank or cut off.
Priority: P2

**TC_FL_007**
Look at the first result card — check the price.
Expected: A price is shown. Not "N/A", not empty. Doesn't matter what currency as long as something is there.
Priority: P2

**TC_FL_008**
After submitting the Cairo → Marsa Alam search, check if any "no results" or "no flights found" message appears.
Expected: That message should NOT appear. If it does, either the route is wrong or there's a data issue.
Priority: P1

---

## Weather – Google Search UI

**TC_WX_001**
Go to google.com, type "Marsa Alam weather" in the search box, hit Enter.
Expected: Results page loads. The URL should have something related to the query. Basic smoke test.
Priority: P1

**TC_WX_002**
After the weather search, look for the weather card that Google usually shows at the top.
Expected: The card is visible — it shows temperature, condition, and location. If it doesn't appear at all, something is off with the search or the widget failed to load.
Priority: P1

**TC_WX_003**
Check the location label inside the weather widget.
Expected: It says "Marsa Alam" — not Cairo, not some other city. This is important because if Google returns weather for the wrong location the whole thing is misleading.
Priority: P1

**TC_WX_004**
Read the temperature number from the weather card.
Expected: There's an actual number there, not a dash or empty space.
Priority: P1

**TC_WX_005**
Check the weather condition text in the card (usually something like "Sunny", "Partly cloudy", etc.).
Expected: Some condition text is visible. Doesn't need to match a specific value since weather changes, but it shouldn't be blank.
Priority: P2

**TC_WX_006**
Take the temperature number from the widget and check if it makes sense for Marsa Alam.
Expected: Between 5°C and 55°C. The city is on the Red Sea coast in Egypt — it never freezes and the record highs are around 48°C. If the number is outside this range, it's almost certainly wrong data being returned.
Priority: P2

---

## Restaurants – Google Search UI

**TC_RS_001**
Go to google.com, search for "restaurant near Marsa Alam".
Expected: A results page loads. Doesn't need to be perfect yet, just checking the search goes through.
Priority: P1

**TC_RS_002**
On the results page, look for the local restaurant cards (the map pack / local panel Google usually shows for this kind of search).
Expected: At least one restaurant card is there. If the panel is completely empty, either Google returned no local results or the locator needs updating.
Priority: P1

**TC_RS_003**
Check if there's a sort or filter button above the restaurant results.
Expected: Some kind of sort control is visible. Google typically shows "Relevance", "Distance", "Highest rated" options here.
Priority: P2

**TC_RS_004**
Click the sort button and select "Highest rated".
Expected: The page updates and still shows restaurants. It shouldn't crash or go blank.
Priority: P1

**TC_RS_005**
After sorting by highest rated, compare the rating of the first card versus the last visible card.
Expected: The first one's rating is equal to or higher than the last one. If the last card has a higher rating than the first, the sort isn't working.
Priority: P1

**TC_RS_006**
Go through each visible restaurant card and check there's actually text in it.
Expected: Every card has a name or some visible content. An empty card would be a rendering bug.
Priority: P2

---

## Flights – Amadeus API

Base URL: https://test.api.amadeus.com/v2

**TC_API_FL_001**
POST to the Amadeus token endpoint with valid client_id and client_secret.
Expected: HTTP 200 back with an access_token in the response. Without this, none of the other Amadeus tests can run.
Priority: P1

**TC_API_FL_002**
GET /shopping/flight-offers with originLocationCode=CAI, destinationLocationCode=RMF, a departure date 30 days from now, 1 adult.
Expected: HTTP 200. If it's 400 or 401, something is wrong with the request or the token.
Priority: P1

**TC_API_FL_003**
Same request as above — parse the response body and check the data array.
Expected: data array exists and has at least one item. An empty array means no flights found which shouldn't happen for this route.
Priority: P1

**TC_API_FL_004**
Loop through all offers in the data array and check each one has an id field.
Expected: No offer is missing its id. If one is null, the response structure is broken.
Priority: P2

**TC_API_FL_005**
Check the first segment of the first itinerary in the first offer.
Expected: departure.iataCode is "CAI". If it's something else, the origin filtering isn't working.
Priority: P1

**TC_API_FL_006**
Check the last segment of the first itinerary (could be a direct flight or the last leg of a connection).
Expected: arrival.iataCode is "RMF". Same reasoning as above — destination must match what we searched for.
Priority: P1

**TC_API_FL_007**
Check the price object on the first offer.
Expected: price.total is there and has a value. An offer without a price is useless.
Priority: P1

**TC_API_FL_008**
Take the price.total value and try to parse it as a number.
Expected: It parses without errors. The Amadeus API returns prices as strings like "450.00" — if it's something like "N/A" or has unexpected characters, that's a data issue.
Priority: P2

**TC_API_FL_009**
Measure how long the /flight-offers request takes from start to finish.
Expected: Under 5 seconds. The Amadeus test environment can be slow, but anything beyond 5s would be a problem in production.
Priority: P2

**TC_API_FL_010**
Check the top-level "dictionaries" section of the response (it contains carrier names, aircraft types etc.).
Expected: The dictionaries object is present. Not critical but it's part of the contract and should be there.
Priority: P3

---

## Weather – OpenWeatherMap API

Base URL: https://api.openweathermap.org/data/2.5

**TC_API_WX_001**
GET /weather with q=Marsa Alam,EG, units=metric, and a valid API key.
Expected: HTTP 200. That's the starting point.
Priority: P1

**TC_API_WX_002**
Check the "name" field in the response.
Expected: Contains "Marsa" somewhere. OWM sometimes returns slightly different names so checking for the full exact string can be brittle, but at minimum it should reference Marsa Alam not a completely different city.
Priority: P1

**TC_API_WX_003**
Check main.temp in the response.
Expected: It's there and it's a number. If the field is missing entirely, the response is malformed.
Priority: P1

**TC_API_WX_004**
Check the actual temperature value.
Expected: Between 5 and 55 degrees Celsius. Same reasoning as the UI check — Marsa Alam doesn't go below 5°C and the API shouldn't return garbage data.
Priority: P2

**TC_API_WX_005**
Check weather[0].description.
Expected: Some text is there — "clear sky", "few clouds", something. Blank means the weather condition array is empty which shouldn't happen.
Priority: P2

**TC_API_WX_006**
Check coord.lat and coord.lon in the response.
Expected: lat is roughly 24–26°N and lon is roughly 33–37°E. Marsa Alam is at around 25.07N, 34.89E — if the coordinates are wildly different, OWM matched the wrong city.
Priority: P2

**TC_API_WX_007**
Check wind.speed.
Expected: A number that's 0 or higher. Negative wind speed makes no sense physically.
Priority: P3

**TC_API_WX_008**
Time the request from sending to receiving the response.
Expected: Under 3 seconds. Weather APIs should be fast.
Priority: P2

**TC_API_WX_009**
Send the same request but with a fake/wrong API key.
Expected: HTTP 401. The API should reject invalid keys, not return data.
Priority: P1

**TC_API_WX_010**
Send a request with a city name that definitely doesn't exist (something like "XXXXXXXXXCITY").
Expected: HTTP 404. If OWM returns 200 with made-up data for a nonexistent city, that's a problem.
Priority: P1

---

## Restaurants – Google Places API

Base URL: https://maps.googleapis.com/maps/api/place

**TC_API_RS_001**
GET /textsearch/json with query="restaurant near Marsa Alam", type=restaurant, and a valid key.
Expected: HTTP 200.
Priority: P1

**TC_API_RS_002**
Check the status field in the response body.
Expected: "OK". If it's ZERO_RESULTS or REQUEST_DENIED, something is wrong with the query or the key.
Priority: P1

**TC_API_RS_003**
Check the results array.
Expected: At least one restaurant in there. Marsa Alam has restaurants, so an empty results array would mean the query isn't working.
Priority: P1

**TC_API_RS_004**
Go through each item in results and check the name field.
Expected: Every result has a name. A restaurant without a name in the response is a data integrity issue.
Priority: P1

**TC_API_RS_005**
For each result, check geometry.location.
Expected: Both lat and lng are present. Without coordinates, the result is not very useful and suggests the response structure changed.
Priority: P2

**TC_API_RS_006**
Check the actual lat/lng values against the expected location.
Expected: lat is between 23 and 27, lng is between 33 and 37. If coordinates are totally off (like showing a restaurant in Europe), the query matched the wrong location.
Priority: P2

**TC_API_RS_007**
Check place_id on each result.
Expected: Every result has a place_id. It's used for follow-up API calls so it needs to be there.
Priority: P2

**TC_API_RS_008**
For results that have a rating field, check the value.
Expected: Between 1.0 and 5.0. Google Places ratings are always in that range — anything outside it would be corrupted data.
Priority: P2

**TC_API_RS_009**
Do a nearby search around the Marsa Alam coordinates (25.0671, 34.8923) with rankby=prominence and check the order.
Expected: The first result should have a rating >= the second. Prominence-based sorting should put better-rated places first.
Priority: P1

**TC_API_RS_010**
Time the textsearch request.
Expected: Under 4 seconds. Google's APIs are generally fast but 4s gives some breathing room.
Priority: P2

**TC_API_RS_011**
Send the request with a clearly wrong API key.
Expected: status in the response body is REQUEST_DENIED or INVALID_REQUEST. The API should not return real data with a bad key.
Priority: P1

**TC_API_RS_012**
For each result, check the types array.
Expected: At least one of the types should be food-related — "restaurant", "food", "cafe", "meal_takeaway", or "bar". If none of those show up, Google may have returned something that's not actually a restaurant.
Priority: P2
