"""
Local mock API servers for the Trip Planner QA suite.

Simulates 3 external APIs on localhost so the API tests run without
needing real credentials. Each server replicates the real API response
structure exactly - same field names, same HTTP status codes, same error behavior.

Ports:
  9081 - OpenWeatherMap  /data/2.5/weather
  9082 - Google Places   /v1/places:searchText  /v1/places:searchNearby
  9083 - Amadeus         /v1/security/oauth2/token  /v2/shopping/flight-offers

Usage:
  python mock_servers.py

The tests pick up the mock URLs automatically when you run with the
mock profile: mvn test -Papi-mock
"""

import json
import threading
import time
from http.server import HTTPServer, BaseHTTPRequestHandler
from urllib.parse import urlparse, parse_qs

OWM_PORT     = 9081
PLACES_PORT  = 9082
AMADEUS_PORT = 9083


# Mock response payloads
# These match the real API response structures field for field.

OWM_WEATHER_MARSA_ALAM = {
    "coord": {"lon": 34.8923, "lat": 25.0671},
    "weather": [{"id": 800, "main": "Clear", "description": "clear sky", "icon": "01d"}],
    "base": "stations",
    "main": {
        "temp": 32.4,
        "feels_like": 35.1,
        "temp_min": 29.8,
        "temp_max": 34.2,
        "pressure": 1013,
        "humidity": 58
    },
    "visibility": 10000,
    "wind": {"speed": 4.2, "deg": 340},
    "clouds": {"all": 0},
    "dt": 1716998400,
    "sys": {"type": 2, "id": 2091, "country": "EG", "sunrise": 1716953100, "sunset": 1716999600},
    "timezone": 10800,
    "id": 352733,
    "name": "Marsa Alam",
    "cod": 200
}

OWM_UNAUTHORIZED = {
    "cod": 401,
    "message": "Invalid API key. Please see https://openweathermap.org/faq#error401 for more info."
}

OWM_CITY_NOT_FOUND = {
    "cod": "404",
    "message": "city not found"
}

PLACES_TEXT_SEARCH_RESULT = {
    "places": [
        {
            "id": "ChIJabc123xyz",
            "displayName": {"text": "Red Sea Restaurant", "languageCode": "en"},
            "formattedAddress": "Marsa Alam Port Road, Marsa Alam, Red Sea Governorate, Egypt",
            "location": {"latitude": 25.0612, "longitude": 34.8901},
            "rating": 4.5,
            "types": ["restaurant", "food", "point_of_interest", "establishment"],
            "businessStatus": "OPERATIONAL"
        },
        {
            "id": "ChIJdef456uvw",
            "displayName": {"text": "Coral Bay Seafood", "languageCode": "en"},
            "formattedAddress": "Marsa Alam Village, Marsa Alam, Egypt",
            "location": {"latitude": 25.0589, "longitude": 34.8876},
            "rating": 4.2,
            "types": ["restaurant", "meal_takeaway", "food", "establishment"],
            "businessStatus": "OPERATIONAL"
        },
        {
            "id": "ChIJghi789rst",
            "displayName": {"text": "Desert Rose Cafe", "languageCode": "en"},
            "formattedAddress": "Sharm El Luli Road, Marsa Alam, Egypt",
            "location": {"latitude": 25.0543, "longitude": 34.8832},
            "rating": 3.9,
            "types": ["cafe", "food", "point_of_interest", "establishment"],
            "businessStatus": "OPERATIONAL"
        }
    ]
}

PLACES_UNAUTHORIZED = {
    "error": {
        "code": 403,
        "message": "The provided API key is invalid.",
        "status": "PERMISSION_DENIED"
    }
}

AMADEUS_TOKEN_RESPONSE = {
    "type": "amadeusOAuth2Token",
    "username": "test@example.com",
    "application_name": "TripPlannerQA",
    "client_id": "mock_client_id",
    "token_type": "Bearer",
    "access_token": "mock_token_abc123xyz456",
    "expires_in": 1799,
    "state": "approved",
    "scope": ""
}

AMADEUS_FLIGHT_OFFERS = {
    "meta": {
        "count": 2,
        "links": {"self": f"http://localhost:{AMADEUS_PORT}/v2/shopping/flight-offers"}
    },
    "data": [
        {
            "type": "flight-offer",
            "id": "1",
            "source": "GDS",
            "instantTicketingRequired": False,
            "nonHomogeneous": False,
            "oneWay": False,
            "lastTicketingDate": "2025-07-15",
            "numberOfBookableSeats": 4,
            "itineraries": [
                {
                    "duration": "PT1H30M",
                    "segments": [
                        {
                            "departure": {"iataCode": "CAI", "terminal": "1", "at": "2025-07-15T08:00:00"},
                            "arrival":   {"iataCode": "RMF", "terminal": "1", "at": "2025-07-15T09:30:00"},
                            "carrierCode": "MS",
                            "number": "400",
                            "aircraft": {"code": "738"},
                            "operating": {"carrierCode": "MS"},
                            "duration": "PT1H30M",
                            "id": "1",
                            "numberOfStops": 0
                        }
                    ]
                }
            ],
            "price": {
                "currency": "EGP",
                "total": "2450.00",
                "base": "2100.00",
                "fees": [{"amount": "0.00", "type": "SUPPLIER"}],
                "grandTotal": "2450.00"
            },
            "pricingOptions": {"fareType": ["PUBLISHED"], "includedCheckedBagsOnly": True},
            "validatingAirlineCodes": ["MS"],
            "travelerPricings": [
                {
                    "travelerId": "1",
                    "fareOption": "STANDARD",
                    "travelerType": "ADULT",
                    "price": {"currency": "EGP", "total": "2450.00", "base": "2100.00"},
                    "fareDetailsBySegment": [
                        {
                            "segmentId": "1",
                            "cabin": "ECONOMY",
                            "fareBasis": "YOWEGB",
                            "class": "Y",
                            "includedCheckedBags": {"weight": 23, "weightUnit": "KG"}
                        }
                    ]
                }
            ]
        },
        {
            "type": "flight-offer",
            "id": "2",
            "source": "GDS",
            "instantTicketingRequired": False,
            "nonHomogeneous": False,
            "oneWay": False,
            "lastTicketingDate": "2025-07-15",
            "numberOfBookableSeats": 9,
            "itineraries": [
                {
                    "duration": "PT3H10M",
                    "segments": [
                        {
                            "departure": {"iataCode": "CAI", "terminal": "1", "at": "2025-07-15T10:00:00"},
                            "arrival":   {"iataCode": "HRG", "at": "2025-07-15T11:00:00"},
                            "carrierCode": "HQ",
                            "number": "102",
                            "aircraft": {"code": "320"},
                            "duration": "PT1H00M",
                            "id": "2",
                            "numberOfStops": 0
                        },
                        {
                            "departure": {"iataCode": "HRG", "at": "2025-07-15T12:10:00"},
                            "arrival":   {"iataCode": "RMF", "at": "2025-07-15T13:10:00"},
                            "carrierCode": "HQ",
                            "number": "103",
                            "aircraft": {"code": "320"},
                            "duration": "PT1H00M",
                            "id": "3",
                            "numberOfStops": 0
                        }
                    ]
                }
            ],
            "price": {
                "currency": "EGP",
                "total": "1890.00",
                "base": "1600.00",
                "fees": [{"amount": "0.00", "type": "SUPPLIER"}],
                "grandTotal": "1890.00"
            },
            "pricingOptions": {"fareType": ["PUBLISHED"], "includedCheckedBagsOnly": False},
            "validatingAirlineCodes": ["HQ"],
            "travelerPricings": [
                {
                    "travelerId": "1",
                    "fareOption": "STANDARD",
                    "travelerType": "ADULT",
                    "price": {"currency": "EGP", "total": "1890.00", "base": "1600.00"},
                    "fareDetailsBySegment": [
                        {"segmentId": "2", "cabin": "ECONOMY", "fareBasis": "YOWEGB", "class": "Y"},
                        {"segmentId": "3", "cabin": "ECONOMY", "fareBasis": "YOWEGB", "class": "Y"}
                    ]
                }
            ]
        }
    ],
    "dictionaries": {
        "locations": {
            "CAI": {"cityCode": "CAI", "countryCode": "EG"},
            "RMF": {"cityCode": "RMF", "countryCode": "EG"},
            "HRG": {"cityCode": "HRG", "countryCode": "EG"}
        },
        "aircraft": {"738": "BOEING 737-800", "320": "AIRBUS A320"},
        "currencies": {"EGP": "EGYPTIAN POUND"},
        "carriers": {"MS": "EGYPTAIR", "HQ": "SMARTWINGS"}
    }
}


# Request handlers

def send_json(handler, status, body):
    data = json.dumps(body).encode()
    handler.send_response(status)
    handler.send_header("Content-Type", "application/json")
    handler.send_header("Content-Length", str(len(data)))
    handler.end_headers()
    handler.wfile.write(data)


class OWMHandler(BaseHTTPRequestHandler):
    """
    Simulates OpenWeatherMap /data/2.5/weather.
    - Valid appid + any city name: returns 200 with Marsa Alam weather data
    - appid contains invalid or is empty: returns 401
    - city name contains xxxx or not_a: returns 404
    """
    def do_GET(self):
        params = parse_qs(urlparse(self.path).query)
        appid  = params.get("appid", [""])[0]
        city   = params.get("q",     [""])[0].lower()

        if not appid or "invalid" in appid.lower():
            send_json(self, 401, OWM_UNAUTHORIZED)
        elif "xxxx" in city or "not_a" in city:
            send_json(self, 404, OWM_CITY_NOT_FOUND)
        else:
            send_json(self, 200, OWM_WEATHER_MARSA_ALAM)

    def log_message(self, fmt, *args):
        print(f"[OWM:{OWM_PORT}] {fmt % args}")


class PlacesHandler(BaseHTTPRequestHandler):
    """
    Simulates Google Places API v1.
    - Valid X-Goog-Api-Key: returns 200 with restaurant results
    - Key contains invalid or is empty: returns 403
    Handles both /places:searchText and /places:searchNearby.
    """
    def do_POST(self):
        key    = self.headers.get("X-Goog-Api-Key", "")
        length = int(self.headers.get("Content-Length", 0))
        self.rfile.read(length)  # consume body

        if not key or "invalid" in key.lower():
            send_json(self, 403, PLACES_UNAUTHORIZED)
        else:
            send_json(self, 200, PLACES_TEXT_SEARCH_RESULT)

    def log_message(self, fmt, *args):
        print(f"[Places:{PLACES_PORT}] {fmt % args}")


class AmadeusHandler(BaseHTTPRequestHandler):
    """
    Simulates Amadeus test API.
    POST /v1/security/oauth2/token - returns access token if client_id and secret are present
    GET /v2/shopping/flight-offers - returns CAI to RMF offers if valid Bearer token
    """
    def do_POST(self):
        length = int(self.headers.get("Content-Length", 0))
        body   = self.rfile.read(length).decode()

        if "oauth2/token" in self.path:
            if "client_id" in body and "client_secret" in body:
                send_json(self, 200, AMADEUS_TOKEN_RESPONSE)
            else:
                send_json(self, 401, {"error": "invalid_client", "error_description": "Client credentials are invalid"})
        else:
            send_json(self, 404, {})

    def do_GET(self):
        auth = self.headers.get("Authorization", "")
        if "flight-offers" in self.path and "Bearer" in auth:
            send_json(self, 200, AMADEUS_FLIGHT_OFFERS)
        else:
            send_json(self, 401, {"errors": [{"status": 401, "code": 38190, "title": "Invalid access token"}]})

    def log_message(self, fmt, *args):
        print(f"[Amadeus:{AMADEUS_PORT}] {fmt % args}")


# Startup

if __name__ == "__main__":
    configs = [
        (OWM_PORT,     OWMHandler,     "OpenWeatherMap"),
        (PLACES_PORT,  PlacesHandler,  "Google Places"),
        (AMADEUS_PORT, AmadeusHandler, "Amadeus"),
    ]

    for port, handler, name in configs:
        server = HTTPServer(("", port), handler)
        server.allow_reuse_address = True
        thread = threading.Thread(target=server.serve_forever, daemon=True)
        thread.start()
        print(f"[{name}] mock server running on http://localhost:{port}")

    print("\nAll mock servers started. Ctrl+C to stop.\n")

    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print("\nStopped.")
