package org.discover.sv.transformer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.BinaryFile;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

public class ParameterizedTransformer extends ResponseDefinitionTransformer {

    private final Pattern interpolationPattern = Pattern.compile("\\$\\(.*?\\)");
    
    private ObjectMapper jsonMapper = new ObjectMapper();
    private ObjectMapper xmlMapper;
    static String  envURL="";
    static Map<String, String> map = new HashMap<String, String>();
    
    
    

    @Override
    public boolean applyGlobally() {
        return false;
    }
   
    	
    
    private String transformResponseBody(Map requestObject, String response) {
        String modifiedResponse = response;

        Matcher matcher = interpolationPattern.matcher(response);
        while (matcher.find()) {
            String group = matcher.group();
            modifiedResponse = modifiedResponse.replace(group, getValue(group, requestObject));
        }
        
       
      
        return modifiedResponse;
    }

    private HttpHeaders transformResponseHeaders(ResponseDefinition responseDefinition, Map requestObject) {
        HttpHeaders headers = new HttpHeaders();
        for(HttpHeader header : responseDefinition.getHeaders().all() ) {
            if(interpolationPattern.matcher(header.firstValue()).find()) {
                headers = headers.plus(new HttpHeader(header.key(), getValue(header.firstValue(), requestObject).toString()));
                System.out.println("Substituting Header " + header + "\n full headers list" + headers);
            }
            else {
                headers = headers.plus(header);
            }
        }

        return headers;
    }

    private CharSequence getValue(String group, Map requestObject) {
    	System.out.println("Substituting key: " + group);

        if(group==null || group.isEmpty()) return "";
        group = group.substring(2, group.length() - 1);
        System.out.println("group" + group);
        if ("!RandomInteger".equalsIgnoreCase(group)) {
            return String.valueOf(new Random().nextInt(2147483647));
        } else if (group.contains("!Today")) {
        	
        	String date ="";
        	Date today = new Date();
        	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        	
        	if("!Today".equalsIgnoreCase(group))
        	{
        	   date = formatter.format(today);
        	}
        	else if(group.charAt(6) == '+')
        	{
        		 Date increment = DateUtils.addDays(today, Integer.parseInt(group.substring(7)));
        		 date = formatter.format(increment);
        		
        	}
        	else if(group.charAt(6) == '-')
        	{
        	 Date decrement = DateUtils.addDays(today,-Integer.parseInt(group.substring(7)));
       		 date = formatter.format(decrement);
        	}
        	else{
        		date = "Please provoide the date in !Today,!Today+noofdays,!Today-noofdays formate";
        	}  
           
            return date;
                     
            //TODO: change to date function 
        } else if (group.contains("!TimeUTC"))
        {
        	String utcdate="";	
        	Date today = new Date();
        	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ss");
        	
        	
        	if("!TimeUTC".equalsIgnoreCase(group))
        	{
        		utcdate = formatter.format(today);
        	}
        	else if(group.charAt(8) == '+')
        	{
        		 Date increment = DateUtils.addDays(today, Integer.parseInt(group.substring(9)));
        		 utcdate = formatter.format(increment);
        		
        	}
        	else if(group.charAt(8) == '-')
        	{
        	 Date decrement = DateUtils.addDays(today,-Integer.parseInt(group.substring(9)));
        	 utcdate = formatter.format(decrement);
        	}
        	else{
        		utcdate = "Please provoide the date in !TimeUTC,!TimeUTC+noofdays,!TimeUTC-noofdays formate";
        	}  
           
        	
            return utcdate; //TODO: change to date function
        } else if (group.startsWith("db.")) {
            return getValueFromDB(group.substring(3));
        } else if (group.startsWith("env.")) {
        	
            return getProperitesValue(group);
        }
      
        else {
            return getValueFromRequestObject(group, requestObject);
        }
    }
    
    
    private String getProperitesValue(String envKey)
    {
    /*	String envValue = "";
    	System.out.println("Getting Envkey" + envKey);
    	try {
			String workingDir = System.getProperty("user.dir");
			Properties prop = new Properties();
			prop.load(new FileInputStream(workingDir+"/Environment.properties"));
			envValue = prop.getProperty(envKey);
			System.out.println("Env value" + envValue);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return envValue;*/
    	
    	/*for (Map.Entry<String, String> entry : map.entrySet())
    	{
    	
    	if(envKey.equalsIgnoreCase(entry.getKey()))
    	{
    		return entry.getValue();
    	}
    	}
    	System.out.println("Your envKey is" + envKey);
    	return "Key not found in Properties";*/
    	
    	String envvalue = System.getenv(envKey) ;
	       System.out.println("Username using system property: "  + envvalue);
	       return envvalue;
    }


    private CharSequence getValueFromRequestObject(String fieldName, Map requestObject) {
        String[] fieldNames = fieldName.split("\\.");
        Object tempObject = requestObject;
        for (String field : fieldNames) {
            if (tempObject instanceof Map) {
                tempObject = ((Map) tempObject).get(field);
            }
        }
        return String.valueOf(tempObject);
    }

    private CharSequence getValueFromDB(String group) {
        //TODO implement mongoDB integration
        System.out.println("DB Key: " + group + ". DB not supported yet");
        return group;
    }

    private boolean hasEmptyBody(ResponseDefinition responseDefinition) {
        return responseDefinition.getBody() == null && responseDefinition.getBodyFileName() == null;
    }

    private String getBody(ResponseDefinition responseDefinition, FileSource fileSource) {
        String body;
        if (responseDefinition.getBody() != null) {
            body = responseDefinition.getBody();
        } else {
            BinaryFile binaryFile = fileSource.getBinaryFileNamed(responseDefinition.getBodyFileName());
            body = new String(binaryFile.readContents(), StandardCharsets.UTF_8);
        }
        return body;
    }

    @Override
    public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition, FileSource fileSource, Parameters parameters) {


       

        Map requestMap = collectRequestDetails(request, parameters);
        

        if (hasEmptyBody(responseDefinition)) {
            return responseDefinition;
        }
        String body = getBody(responseDefinition, fileSource);

        return ResponseDefinitionBuilder
                .like(responseDefinition).but()
                .withBodyFile(null)
                .withBody(transformResponseBody(requestMap, body))
            //    .withHeaders(transformResponseHeaders(responseDefinition, requestMap))
                .build();
    }

    private Map collectRequestDetails(Request request, Parameters parameters) {
        Map object = null;

        String requestBody = request.getBodyAsString();

        System.out.println("RequestBody:  " + requestBody);
        System.out.println("AbsoluteUrl:   " + request.getAbsoluteUrl());

        try {
            object = jsonMapper.readValue(requestBody, Map.class);
        } catch (IOException e) {
            try {
                JacksonXmlModule configuration = new JacksonXmlModule();
                // Set the default value name for xml elements like <user type="String">Dmytro</user>
                configuration.setXMLTextElementName("value");
                xmlMapper = new XmlMapper(configuration);
                object = xmlMapper.readValue(requestBody, Map.class);
            } catch (IOException ex) {
                // Validate is a body has the 'name=value' parameters
                if (StringUtils.isNotEmpty(requestBody) && (requestBody.contains("&") || requestBody.contains("="))) {
                    object = new HashMap();
                    String[] pairedValues = requestBody.split("&");
                    for (String pair : pairedValues) {
                        String[] values = pair.split("=");
                        object.put(values[0], values.length > 1 ? decodeUTF8Value(values[1]) : "");
                    }
                } else if (request.getAbsoluteUrl().split("\\?").length == 2 ){ // Validate query string parameters
                    object = new HashMap();
                    String absoluteUrl = request.getAbsoluteUrl();
                    String[] pairedValues = absoluteUrl.split("\\?")[1].split("&");
                    for (String pair : pairedValues) {
                        String[] values = pair.split("=");
                        object.put(values[0], values.length > 1 ? decodeUTF8Value(values[1]) : "");
                    }
                }
                else if (object==null)
                {
                	object = new HashMap();
                	 for( HttpHeader header : request.getHeaders().all()) {
                         System.out.println(header);
                         object.put(header.key(), header.firstValue());
                     }

                     //collect all the cookies
                     for (String cookieName : request.getCookies().keySet()) {
                         object.put(cookieName, request.getCookies().get(cookieName));
                         System.out.println(cookieName);
                     }
                	
                }
                
                else {
                    System.err.println("[Body parse error] The body doesn't match any of 3 possible formats (JSON, XML, key=value).");
                }
            }
        }

   
        /*     //collect all DB
        if(parameters.containsKey("dbLookUp")) {
            retrieveDbValues(parameters, object);
        } */

        return object;
    }

    private void retrieveDbValues(Parameters parameters, Map object) {
        Map<String, String> dbLookupKeys = (Map<String, String>) parameters.get("dbLookUp");
        String searchCriteria="";
        for(String dbLookupKey : dbLookupKeys.keySet()) {
            String dbLookupValue = dbLookupKeys.get(dbLookupKey);
            Matcher matcher = interpolationPattern.matcher(dbLookupValue);
            if(matcher.find()) {
                dbLookupValue = dbLookupValue.replace(
                        matcher.group(),
                        getValueFromRequestObject(matcher.group().substring(2, matcher.group().length() - 1), object)
                );
            }
            searchCriteria = searchCriteria + "&" + dbLookupKey + "=" + dbLookupValue;
        }

        System.out.println("DB Search criteria : " + searchCriteria);

        //TODO: Call db rest api

        //TODO: Add response to object Map
    }

    private String decodeUTF8Value(String value) {

        String decodedValue = "";
        try {
            decodedValue = URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            System.err.println("[Body parse error] Can't decode one of the request parameter. It should be UTF-8 charset.");
        }

        return decodedValue;
    }
    
 
    @Override
    public String getName() {
        return "param-transformer";
    }
}