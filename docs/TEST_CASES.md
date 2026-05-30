# Test Cases – Trip Planner QA Suite

**Scenario:** A user planning a trip from Cairo to Marsa Alam using Google Search and Google Flights.

---

## 1. UI – Google Flights: Cairo → Marsa Alam

| ID | Title | Steps | Expected Result | Priority | Type |
|---|---|---|---|---|---|
| TC_FL_001 | Flights page loads successfully | Open `google.com/travel/flights` | Page title contains "Flights"; page is accessible | P1 | Smoke |
| TC_FL_002 | Origin field accepts "Cairo" | Open Flights, click origin, type "Cairo" | Autocomplete appears, "Cairo" is selectable | P1 | Functional |
| TC_FL_003 | Destination field accepts "Marsa Alam" | After origin set, click destination, type "Marsa Alam" | "Marsa Alam" appears in autocomplete | P1 | Functional |
| TC_FL_004 | Search returns at least one result | Fill Cairo → Marsa Alam, click Search | At least one flight card is displayed | P1 | Functional |
| TC_FL_005 | Route label references both cities | Submit search, check page title/header | Title or route label contains "Cairo" and "Marsa Alam" (or their IATA codes) | P2 | Functional |
| TC_FL_006 | Result cards show airline names | Submit search, inspect first result | Airline name is non-blank on the card | P2 | Functional |
| TC_FL_007 | Result cards show prices | Submit search, inspect first result | Price is shown and is not "N/A" | P2 | Functional |
| TC_FL_008 | No "no flights found" banner shown | Submit search | The "no flights" error message is absent | P1 | Negative |

---

## 2. UI – Google Search: Marsa Alam Weather

| ID | Title | Steps | Expected Result | Priority | Type |
|---|---|---|---|---|---|
| TC_WX_001 | Searching "Marsa Alam weather" opens SERP | Open Google, type "Marsa Alam weather", press Enter | SERP loads; URL contains "marsa" | P1 | Smoke |
| TC_WX_002 | Weather widget is visible | Perform weather search | Google's weather card widget is rendered on the page | P1 | Functional |
| TC_WX_003 | Widget location label shows "Marsa Alam" | Observe the location label in the weather widget | Text contains "Marsa Alam" (case-insensitive) | P1 | Functional |
| TC_WX_004 | Widget displays a temperature value | Read temperature from widget | Temperature field is non-blank | P1 | Functional |
| TC_WX_005 | Widget displays a weather condition | Read condition from widget | Condition string (e.g., "Sunny", "Clear") is non-blank | P2 | Functional |
| TC_WX_006 | Temperature is in a plausible range | Parse numeric temperature | Value is between 5°C and 55°C | P2 | Data Validation |

---

## 3. UI – Google Search: Restaurants near Marsa Alam

| ID | Title | Steps | Expected Result | Priority | Type |
|---|---|---|---|---|---|
| TC_RS_001 | Searching "restaurant near Marsa Alam" opens SERP | Open Google, search the query | SERP loads, URL reflects the query | P1 | Smoke |
| TC_RS_002 | At least one restaurant card is present | Observe local results panel | Minimum one restaurant card is rendered | P1 | Functional |
| TC_RS_003 | Sort-by control is visible | Observe top of local panel | A sort/filter button is present | P2 | Functional |
| TC_RS_004 | Clicking "Highest rated" does not break the page | Click Sort → Highest rated | Page reloads/updates; at least one card remains | P1 | Functional |
| TC_RS_005 | After sorting, first card rating ≥ last card rating | Sort by Highest rated, compare ratings | Rating is in descending order | P1 | Sorting / Data |
| TC_RS_006 | All result cards have a visible name | Inspect each card | Card text is non-blank for every card | P2 | Data Validation |

---

## 4. API – Amadeus Flight Offers

**Endpoint:** `GET https://test.api.amadeus.com/v2/shopping/flight-offers`

| ID | Title | Expected Result | Priority | Type |
|---|---|---|---|---|
| TC_API_FL_001 | OAuth token request returns HTTP 200 | Token endpoint responds 200 with `access_token` | P1 | Auth |
| TC_API_FL_002 | Flight offers endpoint returns HTTP 200 | `GET /flight-offers?origin=CAI&destination=RMF` → 200 | P1 | Contract |
| TC_API_FL_003 | `data` array contains at least one offer | `data` is present and non-empty | P1 | Contract |
| TC_API_FL_004 | Each offer has an `id` field | No null IDs in `data[]` | P2 | Schema |
| TC_API_FL_005 | First segment departure IATA = CAI | `data[0].itineraries[0].segments[0].departure.iataCode == "CAI"` | P1 | Data |
| TC_API_FL_006 | Last segment arrival IATA = RMF | Final segment arrival IATA matches `RMF` | P1 | Data |
| TC_API_FL_007 | Each offer has `price.total` | `price.total` is present and non-blank | P1 | Schema |
| TC_API_FL_008 | `price.total` is a valid numeric string | `Double.parseDouble(price.total)` does not throw | P2 | Data |
| TC_API_FL_009 | Response time < 5000 ms | Measured response time under threshold | P2 | Performance |
| TC_API_FL_010 | `dictionaries` meta section is present | `dictionaries` object exists in response body | P3 | Contract |

---

## 5. API – OpenWeatherMap Current Weather

**Endpoint:** `GET https://api.openweathermap.org/data/2.5/weather`

| ID | Title | Expected Result | Priority | Type |
|---|---|---|---|---|
| TC_API_WX_001 | Valid request returns HTTP 200 | `?q=Marsa Alam,EG&appid=<key>` → 200 | P1 | Contract |
| TC_API_WX_002 | `name` field references Marsa Alam | Response `name` contains "Marsa" | P1 | Data |
| TC_API_WX_003 | `main.temp` is present | Field exists and is numeric | P1 | Schema |
| TC_API_WX_004 | Temperature is in realistic range | Value is between 5°C and 55°C | P2 | Data Validation |
| TC_API_WX_005 | `weather[0].description` is non-blank | Condition string is present | P2 | Schema |
| TC_API_WX_006 | Coordinates match Marsa Alam location | `coord.lat` ≈ 24–26°N, `coord.lon` ≈ 33–37°E | P2 | Geo Validation |
| TC_API_WX_007 | `wind.speed` is non-negative | Wind speed ≥ 0 | P3 | Data |
| TC_API_WX_008 | Response time < 3000 ms | Measured time under threshold | P2 | Performance |
| TC_API_WX_009 | Invalid key returns HTTP 401 | `appid=INVALID_KEY` → 401 | P1 | Negative / Security |
| TC_API_WX_010 | Non-existent city returns HTTP 404 | `q=XXXXXXXXXX_NOT_A_CITY` → 404 | P1 | Negative |

---

## 6. API – Google Places Text Search (Restaurants)

**Endpoint:** `GET https://maps.googleapis.com/maps/api/place/textsearch/json`

| ID | Title | Expected Result | Priority | Type |
|---|---|---|---|---|
| TC_API_RS_001 | Valid request returns HTTP 200 | `?query=restaurant near Marsa Alam&key=<key>` → 200 | P1 | Contract |
| TC_API_RS_002 | `status` field equals "OK" | Response `status == "OK"` | P1 | Contract |
| TC_API_RS_003 | `results` array is non-empty | At least one restaurant returned | P1 | Data |
| TC_API_RS_004 | Each result has a non-blank `name` | `results[i].name` present for all i | P1 | Schema |
| TC_API_RS_005 | Each result has `geometry.location` | `lat` and `lng` present | P2 | Schema |
| TC_API_RS_006 | Coordinates are within Marsa Alam region | `lat` ≈ 23–27°N, `lng` ≈ 33–37°E | P2 | Geo Validation |
| TC_API_RS_007 | Each result has a `place_id` | `place_id` non-blank for all results | P2 | Schema |
| TC_API_RS_008 | Rating (when present) is 1.0–5.0 | Rating values within valid range | P2 | Data |
| TC_API_RS_009 | Nearby search sorted by rating is descending | First result rating ≥ second result rating | P1 | Sorting |
| TC_API_RS_010 | Response time < 4000 ms | Measured time under threshold | P2 | Performance |
| TC_API_RS_011 | Invalid key returns error status | `status` is `REQUEST_DENIED` or `INVALID_REQUEST` | P1 | Negative / Security |
| TC_API_RS_012 | Result types include a food-related category | `types` includes restaurant, food, cafe, etc. | P2 | Data |

---

## Test Count Summary

| Suite | UI Tests | API Tests | Total |
|---|---|---|---|
| Flights (Cairo → Marsa Alam) | 8 | 10 | 18 |
| Weather (Marsa Alam) | 6 | 10 | 16 |
| Restaurants (near Marsa Alam) | 6 | 12 | 18 |
| **Total** | **20** | **32** | **52** |
