package org.discover.sv.transformer;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.standalone.WireMockServerRunner;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class WireMockWrapper {
	
	public static void main(String[] args) throws Exception {
		//WireMockServerRunner.main("{\"--extensions org.discover.sv.transformer.ParameterizedTransformer\"}");
		String[] args1={"--extensions=org.discover.sv.transformer.ParameterizedTransformer"};
		System.out.println("dfsdfsdfsdf"+ args1[0]);
		WireMockServerRunner.main(args1);
	}
}
