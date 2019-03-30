package com.geekhalo.tinyurl.controller;

import com.geekhalo.tinyurl.application.TinyUrlApplication;
import com.geekhalo.tinyurl.application.TinyUrlApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;


@Controller
public class RedirectController {

    @Autowired
    private TinyUrlApplication tinyUrlApplication;


    @GetMapping("s/{code}")
    public ModelAndView redirect(@PathVariable String code){
        String url = getTargetUrl(code);
        // 使用 RedirectView，进行请求重定向
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(url);
        return new ModelAndView(redirectView);
    }

    private String getTargetUrl(String code) {
        return this.tinyUrlApplication.getTargetUrl(code);
    }

//
//    @RequestMapping("{code}")
//    public void redirect(@PathVariable String code, HttpServletResponse response) throws IOException {
//        String url = getTargetUrl(code);
//        // 调用 sendRedirect 方法，进行请求重定向
//        response.sendRedirect(url);
//    }

}
