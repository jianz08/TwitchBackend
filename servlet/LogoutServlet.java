package com.laioffer.jupiter.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "LogoutServlet", urlPatterns = {"/logout"})
public class LogoutServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //Destroy the session since the user is logged out

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        //清除浏览器cookie里的sessionId to null
        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setPath("/");//删除根目录下的cookie,相当于localhost:8080/jupiter
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
