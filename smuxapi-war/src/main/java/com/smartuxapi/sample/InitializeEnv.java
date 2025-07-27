package com.smartuxapi.sample;


import com.smartuxapi.util.PropertiesUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;

@WebServlet(name = "loadConfig", urlPatterns = { "/init" }, loadOnStartup = 0)
public class InitializeEnv extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public void init() {
		PropertiesUtil.USER_PROPERTIES_PATH = this.getServletContext().getRealPath("/")
				+ "WEB-INF/classes/resources/smuxapi.properties";
    }
    
}
