# Mock Servers

Three lightweight Python HTTP servers that simulate the external APIs used in the test suite. Use these when you do not have real API keys, or when you want the API tests to run in CI without hitting real endpoints.

Ports:
- 9081 - OpenWeatherMap /data/2.5/weather
- 9082 - Google Places /v1/places:searchText and /v1/places:searchNearby
- 9083 - Amadeus /v1/security/oauth2/token and /v2/shopping/flight-offers

Start them:

```bash
python mock-server/mock_servers.py
```

Then run API tests against the mocks:

```bash
mvn test -Papi-mock
```

The responses match the real APIs exactly - same field names, same structure, same HTTP status codes for error cases. The OWM mock returns weather for Marsa Alam at 32.4 Celsius with clear sky conditions. The Places mock returns 3 restaurants sorted by rating descending. The Amadeus mock returns 2 flight offers: a direct EgyptAir flight and a SmartWings connection via Hurghada.

Error simulation:
- OWM: pass appid=INVALID_KEY_xxx to get a 401; pass a city with xxxx in the name to get a 404
- Places: pass an invalid value in X-Goog-Api-Key header to get a 403
- Amadeus: call flight-offers without a Bearer token to get a 401

Requires Python 3.8 or later, no external dependencies.
