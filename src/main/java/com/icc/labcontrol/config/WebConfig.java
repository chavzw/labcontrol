package com.icc.labcontrol.config;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

/**
 * URLs limpias:
 *   /usuarios  -> static/usuarios.html
 *   /procesos  -> static/procesos.html
 * Usa forward servlet directo (más robusto que view controllers para recursos estáticos
 * en Spring MVC 7 cuando no hay Thymeleaf ni otro ViewResolver configurado).
 */
@Controller
public class WebConfig {

    @GetMapping({"/usuarios", "/usuarios/"})
    public void usuarios(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        RequestDispatcher rd = req.getRequestDispatcher("/usuarios.html");
        rd.forward(req, res);
    }

    @GetMapping({"/procesos", "/procesos/"})
    public void procesos(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        RequestDispatcher rd = req.getRequestDispatcher("/procesos.html");
        rd.forward(req, res);
    }
}
