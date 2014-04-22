package com.continuuity.gateway.router;

import com.continuuity.common.conf.Constants;
import com.continuuity.gateway.GatewayFastTestsSuite;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *  To test the RouterPathLookup regular expression tests.
 */
public class RouterPathTest {

  private static RouterPathLookup pathLookup;
  private final String VERSION = "HTTP/1.1";

  @BeforeClass
  public static void init() throws Exception {
    pathLookup = GatewayFastTestsSuite.getInjector().getInstance(RouterPathLookup.class);
  }

  @Test
  public void testRouterFlowPathLookUp() throws Exception {
    String flowPath = "/v2//apps/ResponseCodeAnalytics/flows/LogAnalyticsFlow/status";
    HttpRequest httpRequest = new DefaultHttpRequest(new HttpVersion(VERSION), new HttpMethod("GET"), flowPath);
    String result = pathLookup.getRoutingPath(flowPath, httpRequest);
    Assert.assertEquals(Constants.Service.APP_FABRIC_HTTP, result);
  }

  @Test
  public void testRouterWorkFlowPathLookUp() throws Exception {
    String procPath = "/v2/apps/PurchaseHistory/workflows/PurchaseHistoryWorkflow/status";
    HttpRequest httpRequest = new DefaultHttpRequest(new HttpVersion(VERSION), new HttpMethod("GET"), procPath);
    String result = pathLookup.getRoutingPath(procPath, httpRequest);
    Assert.assertEquals(Constants.Service.APP_FABRIC_HTTP,  result);
  }

  @Test
  public void testRouterProcedurePathLookUp() throws Exception {
    String procPath = "/v2//apps/ResponseCodeAnalytics/procedures/StatusCodeProcedure/status";
    HttpRequest httpRequest = new DefaultHttpRequest(new HttpVersion(VERSION), new HttpMethod("GET"), procPath);
    String result = pathLookup.getRoutingPath(procPath, httpRequest);
    Assert.assertEquals(Constants.Service.APP_FABRIC_HTTP,  result);
  }

  @Test
  public void testRouterDeployPathLookUp() throws Exception {
    String procPath = "/v2//apps/";
    HttpRequest httpRequest = new DefaultHttpRequest(new HttpVersion(VERSION), new HttpMethod("GET"), procPath);
    String result = pathLookup.getRoutingPath(procPath, httpRequest);
    Assert.assertEquals(Constants.Service.APP_FABRIC_HTTP,  result);
  }

  @Test
  public void testRouterFlowletInstancesLookUp() throws Exception {
    String procPath = "/v2//apps/WordCount/flows/WordCountFlow/flowlets/StreamSource/instances";
    HttpRequest httpRequest = new DefaultHttpRequest(new HttpVersion(VERSION), new HttpMethod("GET"), procPath);
    String result = pathLookup.getRoutingPath(procPath, httpRequest);
    Assert.assertEquals(Constants.Service.APP_FABRIC_HTTP,  result);
  }

}
