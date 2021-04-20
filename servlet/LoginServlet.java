package com.laioffer.jupiter.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.jupiter.db.MySQLConnection;
import com.laioffer.jupiter.db.MySQLException;
import com.laioffer.jupiter.entity.LoginRequestBody;
import com.laioffer.jupiter.entity.LoginResponseBody;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //Read user data from the request body

        //ObjectMapper mapper = new ObjectMapper();
        //LoginRequestBody body = mapper.readValue(request.getReader(), LoginRequestBody.class);
        LoginRequestBody body = ServletUtil.readRequestBody(LoginRequestBody.class, request);

        if (body == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        String username;
        MySQLConnection connection = null;
        try {
            //Verify if the user ID and password are correct
            connection = new MySQLConnection();
            String userId = body.getUserId();
            String password = ServletUtil.encryptPassword(body.getUserId(), body.getPassword());
            username = connection.verifyLogin(userId, password);
        } catch (MySQLException e) {
            e.printStackTrace();
            throw new ServletException(e);
        } finally {
            connection.close();
        }
        //create a new session, put userId as an attribute into the session object
        // and set the expiration time to 600 seconds.
        if (!username.isEmpty()) {
            HttpSession session = request.getSession();
            session.setAttribute("user_id", body.getUserId());
            session.setMaxInactiveInterval(600);
            LoginResponseBody loginResponseBody = new LoginResponseBody(body.getUserId(), username);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().print(new ObjectMapper().writeValueAsString(loginResponseBody));

            //response.addHeader("sessionid", session.getId());//Tomcat已实现把sessonId加到response header里，不需要自己实现
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}