package com.anluy.datapig;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * ${DESCRIPTION}
 *
 * @author hc.zeng
 * @create 2018-06-21 15:10
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/api/admin/websocketServer").setAllowedOrigins("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic","/user");//定义两个主题;topic:群发；user:一对一
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");//user:一对一,需存在于上面的SimpleBroker中
    }
}
