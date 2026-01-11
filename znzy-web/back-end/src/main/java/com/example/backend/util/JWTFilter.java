package com.example.backend.util;

import com.alibaba.fastjson.JSON;
import com.auth0.jwt.interfaces.Claim;
import com.example.backend.entity.RestBean;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;

/**
 * JWT过滤器，拦截 /secure的请求
 */
@Slf4j
@WebFilter(filterName = "JWTFilter", urlPatterns = "/api/*")
public class JWTFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;
        String requestURI = request.getRequestURI();

        response.setCharacterEncoding("UTF-8");
        try{
        //获取 header里的token
        final String token = request.getHeader("authorization");                                                 //-----------------------------------------------------------------------------------------------------
        if (requestURI.startsWith("/api/auth") || requestURI.startsWith("/api/ai")) {
            chain.doFilter(request, response); // 放行
            return;
        }
//-----------------------------------------------------------------------------------------------------
        if ("OPTIONS".equals(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            chain.doFilter(request, response);
        }
        // Except OPTIONS, other request should be checked by JWT
        else {

            if (token == null) {
                response.getWriter().write(JSON.toJSONString(RestBean.failure(401, "未提供token")));
                return;
            }
            //解析token
            Map<String, Claim> userData = JWTUtil.verifyToken(token);
            if (userData == null) {
                response.getWriter().write(JSON.toJSONString(RestBean.failure(401, "token不合法")));
                return;
            }
            Integer id = userData.get("id").asInt();
            String username = userData.get("name").asString();
            String password = userData.get("password").asString();
            String avatar = userData.get("avatar").asString();
            UserContext.setCurrentUser(id, username, avatar);
            //过滤器 拿到用户信息，放到request中

            request.setAttribute("id", id);
            request.setAttribute("name", username);
            request.setAttribute("password", password);
            request.setAttribute("avatar", avatar);
            chain.doFilter(req, res);

        }
        }catch(Exception e){
            log.error("JWTFilter error: {}", e.getMessage());
            response.getWriter().write(JSON.toJSONString(RestBean.failure(500, "服务器内部错误")));
        }finally{
            UserContext.clear();
        }
    }

    @Override
    public void destroy() {
    }
}