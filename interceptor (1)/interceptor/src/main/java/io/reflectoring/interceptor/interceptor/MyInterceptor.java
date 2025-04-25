package io.reflectoring.interceptor.interceptor;

import io.reflectoring.interceptor.entity.RequestLog;
import io.reflectoring.interceptor.repository.RequestLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class MyInterceptor implements HandlerInterceptor {

    private final Map<String, Long> inProgress = new ConcurrentHashMap<>();
    private final long LOCK_TIMEOUT_MS = 30_00; // 3 seconds

    @Autowired
    private RequestLogRepository requestLogRepository;

    public MyInterceptor() {
        // Background thread to clean expired locks
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            inProgress.entrySet().removeIf(entry -> (now - entry.getValue()) > LOCK_TIMEOUT_MS);
        }, 10, 10, TimeUnit.SECONDS);
    }
    private void saveLog(HttpServletRequest request, String stage) {
        RequestLog log = new RequestLog();
        log.setUri(request.getRequestURI());
        log.setMethod(request.getMethod());
        log.setStage(stage);
        requestLogRepository.save(log);
    }



    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        if (!request.getMethod().equalsIgnoreCase("POST")) {
            saveLog(request, "preHandle");
            return true;
        }

        String path = request.getRequestURI();
        String[] parts = path.split("/");
        String id = parts[parts.length - 1];

        long now = System.currentTimeMillis();

        // If not present or expired, allow and put
        if (inProgress.containsKey(id) && (now - inProgress.get(id)) < LOCK_TIMEOUT_MS) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Request already in progress for id: " + id);
            return false;
        }

        inProgress.put(id, now);
        saveLog(request, "preHandle");
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) throws Exception {
        saveLog(request, "postHandle");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        if (ex != null) {
            System.out.println("❗ Exception: " + ex.getMessage());
        }
        saveLog(request, "afterCompletion");
    }
}
