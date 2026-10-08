package de.hfu;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.util.HtmlUtils;
import de.hfu.service.MessageService;
import de.hfu.model.Message;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/messages-.html")
public class MessageListServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html");
        res.setCharacterEncoding("UTF-8");

        WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(req.getServletContext());
        MessageService messageService = (MessageService) applicationContext.getBean("messageService");

        PrintWriter out = res.getWriter();

        out.append("<!DOCTYPE html><html><head><title>Nachrichtenliste</title></head><body><h1>Nachrichtenliste</h1><ul>");

        List<Message> messages = messageService.findAllMessages();
        for (Message message : messages) {
            out.append("<li>Message: " + HtmlUtils.htmlEscape(message.getText()) +
                    "<br>User: " + HtmlUtils.htmlEscape(message.getUser().getUsername()) +
                    "<br>Date: " + message.getDate() +
                    "</li>");
        }

        out.append("</ul></body></html>");

    }
}
