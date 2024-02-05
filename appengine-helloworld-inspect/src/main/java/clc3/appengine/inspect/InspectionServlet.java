package clc3.appengine.inspect;

import com.google.appengine.api.modules.ModulesService;
import com.google.appengine.api.modules.ModulesServiceFactory;
import com.google.appengine.api.utils.SystemProperty;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

public class InspectionServlet extends HttpServlet {
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Properties properties = System.getProperties();

        ModulesService modulesService =  ModulesServiceFactory.getModulesService();
        String instanceId;
        try {
            instanceId = modulesService.getCurrentInstanceId();
        } catch(Exception e){
            instanceId = "null";
        }

        String hostname = modulesService.getVersionHostname(null, null);

        PrintWriter out = resp.getWriter();
        out.print("<h1>Hello App Engine V2!</h1>");
        out.println("<br>");
        out.print("Instance ID : " + instanceId);
        out.println("<br>");
        out.print("Hostname : " + hostname);
        out.println("<br>");
        out.print("Runtime version: " + SystemProperty.version.get());
        out.println("<br>");
        out.print("Java " + properties.get("java.specification.version"));
        out.println("<br>");
        out.print("System: " + getInfo());
        out.println("<br>");
        if (instanceId.equals("0")) {
            out.print("Service is running on a backend instance");
        } else {
            out.print("Service is running on a frontend instance");
        }
        out.println("<br>");

        out.println("---------<br>");
        try {
            String param = req.getParameter("someParam");
            out.print("someParam = " + param);
        } catch (Exception e) {
            e.printStackTrace();
        }
        out.println("<br>");
    }

    public static String getInfo() {
        return "Version: " + System.getProperty("java.version")
                + " OS: " + System.getProperty("os.name")
                + " User: " + System.getProperty("user.name");
    }
}
