package com.yang.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

import javax.validation.constraints.NotNull;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/webSocket/{userId}")
@Component
@Slf4j
@EqualsAndHashCode
@EnableWebSocket
public class WebSocket {

    private final String websocketOfflineSend = "WEBSOCKET_OFFLINE_SEND";

    private static RedisTemplate redisTemplate;

    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        WebSocket.redisTemplate = redisTemplate;
    }
//    private RedisTemplate<String, String> redisTemplate = (RedisTemplate<String, String>) SpringHelper.getBean("redisTemplate");

    private Session session;

    private static CopyOnWriteArraySet<WebSocket> webSockets = new CopyOnWriteArraySet<>();

    private static Map<String, Session> sessionPool = new HashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam(value = "userId") String userId) {
        try {
            this.session = session;
            webSockets.add(this);
            sessionPool.put(userId, session);
            log.info("【websocket消息】有新的连接，总数为:" + webSockets.size() + ";userId:" + userId);
            Boolean aBoolean = redisTemplate.opsForHash().hasKey(websocketOfflineSend, userId);
            if (aBoolean) {
                String string = Objects.requireNonNull(redisTemplate.opsForHash().get(websocketOfflineSend, userId)).toString();
                JSONArray objects = JSONObject.parseArray(string);
                if (objects.size() > 0) {
                    for (Object object : objects) {
                        this.sendOneMessage(userId, (String) object);
                    }
                }
                //消息已读 销毁该条消息
                Long delete = redisTemplate.opsForHash().delete(websocketOfflineSend, userId);
                log.info("userId = " + userId + "的离线消息已读，销毁status = " + delete);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @OnClose
    public void onClose() {
        try {
            webSockets.remove(this);
            log.info("【websocket消息】连接断开，总数为:" + webSockets.size());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @OnMessage
    public void onMessage(String message) {
        log.info("【websocket消息】收到客户端消息:" + message);
        session.getAsyncRemote().sendText(message);
    }

    /**
     * 广播消息
     *
     * @param message 消息
     */
    public void sendAllMessage(String message) {
        log.info("【websocket消息】广播消息:" + message);
        for (WebSocket webSocket : webSockets) {
            try {
                if (webSocket.session.isOpen()) {
                    webSocket.session.getAsyncRemote().sendText(message);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 单点消息 推送单人
     *
     * @param userId  用户id
     * @param message 消息
     */
    public void sendOneMessage(String userId, String message) {
        getSession(message, userId);
    }

    /**
     * 单点消息(多人) / 支持离线消息发送
     *
     * @param userIds 用户id列表
     * @param message 消息
     */
    public void sendMoreMessage(@NotNull String[] userIds, String message) {
        for (String userId : userIds) {
            getSession(message, userId);
        }
    }

    private void getSession(String message, String userId) {
        Session session = sessionPool.get(userId);
        if (session != null && session.isOpen()) {
            synchronized (session) {
                try {
                    log.info("【websocket消息】 单点消息:" + message);
                    session.getBasicRemote().sendText(message);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        } else {
            // 离线数据存储
            List<String> oldList = new ArrayList<>();
            List<String> newList = new ArrayList<>();
            if (redisTemplate.opsForHash().hasKey(websocketOfflineSend, userId)) {
                String string = Objects.requireNonNull(redisTemplate.opsForHash().get(websocketOfflineSend, userId)).toString();
                JSONArray objects = JSONObject.parseArray(string);
                if (objects.size() > 0) {
                    objects.forEach(x -> {
                        newList.add(x.toString());
                    });
                }
                newList.add(message);
                String jsonString = JSON.toJSONString(newList);
                redisTemplate.opsForHash().put(websocketOfflineSend, userId, jsonString);
            } else {
                oldList.add(message);
                String string = JSON.toJSONString(oldList);
                redisTemplate.opsForHash().put(websocketOfflineSend, userId, string);
            }
        }
    }
}
