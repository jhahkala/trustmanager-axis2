To use the testapp, you'll need to do following steps:
1. setup tomcat
2. setup trustmanager (libs to tomcat6/common [bcprov, log4j, commons-logging, trustmanager, trustmanager-tomcat], copy and edit server.xml.template to tomcat6/conf, copy log4j-trustmanager.properties to tomcat6/conf)
3. deploy axis2.war
4. edit axis2.xml in axis2/WEB-INF/conf/ by commenting out the http transport and add https transport 
   <transportReceiver name="https" class="org.apache.axis2.transport.http.AxisServletListener">
    <parameter name="port">8443</parameter>
</transportReceiver>
<!--    <transportReceiver name="http"
                       class="org.apache.axis2.transport.http.AxisServletListener"/>
-->
5. deploy the EchoService.aar via the web admin interface
6. connect to https://localhost:8443/axis2/services/EchoService