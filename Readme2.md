		      WireMock Manual

WireMock is distributed in two flavours - a standard JAR containing just WireMock, and a standalone fat JAR containing WireMock plus all its dependencies.
The standalone JAR’s dependencies are shaded i.e. they are hidden in alternative packages.

How to Start WireMock?
Run the below command in command prompt/Powershell
java -jar WireMockTransformer-3.4.0-all.jar –extensions org.discover.sv.transformer.ParameterizedTransformer --https-port 52013 --port 8081

--https-port  ---> 52013 - https Port to run WireMock
--port 8081  ---> http Port run WireMock

How to add Virtual Service in WireMock?

Add a .json file inside the mapping folder in below WireMock format

{
    "priority": 1,
	"request": {
		"urlPath": "URL Path",
		"method": "POST/GET/PUT/DELETE",
		"bodyPatterns": [
			{
				"contains": "asysdebit2"
			}
	},
	"response": {
		"status": Response Status Ex: 200, 404, 500,
		"jsonBody": {
			Response Json
		},
		"headers": {
			Response headers
		}
	}
}

Request Matching

Match the incoming request body, header or query parameter.

{
  "request" : {
    "urlPath" : "/everything",
    "method" : "ANY",
    "headers" : {
      "Accept" : {
        "contains" : "xml"
      }
    },
    "queryParameters" : {
      "search_term" : {
        "equalTo" : "WireMock"
      }
    },
    "cookies" : {
      "session" : {
        "matches" : ".*12345.*"
      }
    },
    "bodyPatterns" : [ {
      "equalToXml" : "<search-results />"
    }, {
      "matchesXPath" : "//search-results"
    } ],
    "basicAuthCredentials" : {
      "username" : "jeff@example.com",
      "password" : "jeffteenjefftyjeff"
    }
  },
  "response" : {
    "status" : 200
  }
}

For more examples please refer http://wiremock.org/docs/request-matching/


How to make Response Dynamic?

Getting the response value from Request (From JSON Request body)

 Parametrize response field $(username)
 Example: 
1. $(username) - It takes the Value from Username field in request.
2.$( request.username) - It takes value from request.username in request json.

Example Wire Mock JSON:

{
	"request": {
		"urlPath": "/api/auth/oob/answer",
		"method": "POST"
	},
	"response": {
		"status": 200,
		"jsonBody": {
			"bindDevice": $(username),
			"challengeResult": "success"
		},
		"headers": {
			"Content-Type": "application/json"
		}
	}
}

Random Integer in Response

Parametrize response json $(!RandomInteger)
Example WireMock JSON:

{
	"request": {
		"urlPath": "/api/auth/oob/answer",
		"method": "POST"
	},
	"response": {
		"status": 200,
		"jsonBody": {
			"bindDevice": $(!RandomInteger),
			"challengeResult": "success"
		},
		"headers": {
			"Content-Type": "application/json"
		}
	}
}




Date in Response$(!Today)


Parametrize response json with $(!Today)
Example WireMock JSON:

{
	"request": {
		"urlPath": "/api/auth/oob/answer",
		"method": "POST"
	},
	"response": {
		"status": 200,
		"jsonBody": {
			"bindDevice": $(!Today),
			"challengeResult": "success"
		},
		"headers": {
			"Content-Type": "application/json"
		}
	}
}

To increase or decrease the date from current date use $(!Today+no of days) or $(!Today-no of days) respectively. 


How to include environment variables in Response?

To add Env values in response follow the below steps.

1. Add Variables from property files
Add Property value in Environment.properties files
URL=https://virt-bankmobile.mapi.discoverbank.com 

2. Parametrize WireMock JSON with $(env.)

Example WireMock Json:

{
	"request": {
		"urlPath": "/api/auth/oob/answer",
		"method": "POST"
	},
	"response": {
		"status": 200,
		"jsonBody": {
			"bindDevice": $(env.URL),
			"challengeResult": "success"
		},
		"headers": {
			"Content-Type": "application/json"
		}
	}
}


How to include Static Files( Images ,Pdfs etc)?
Place the static files in __files folder with respective folder structure
