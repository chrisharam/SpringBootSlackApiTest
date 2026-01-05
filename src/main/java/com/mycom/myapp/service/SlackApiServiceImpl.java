package com.mycom.myapp.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
@Service
public class SlackApiServiceImpl implements SlackApiService{
    private final WebClient webClient;

    @Value("${slack.bot.token}")
    private String slackBotToken;

    @Value("${slack.channel}")
    private String slackChannel;

    // 생성자 DI - webClient에 관함
    public SlackApiServiceImpl() {
        this.webClient = WebClient.builder()
                .baseUrl("https://slack.com/api")
                .defaultHeader("Content-Type", "application/json") // Content-Type 대소문자 구분함
                .build();
    }

    @Override
    public void sendMessage(String message) {
        sendMessageToChannel(slackChannel, message);
    }



    public void sendMessageToChannel(String channel, String message) { // channel, message를 json 형태로 변환해줌
        // 봇을 통해 인증 처리함
        String jsonMessage = String.format( // text block(""" """)을 통해 형식을 가져감
                """
                {
                    "channel" : "%s",
                    "text" : "%s"
                }		
                """,
                channel,
                message
        );

        // 채널, 메시지를 webClient 이용해 보내기, post로 보내야 함
        this.webClient.post()
                .uri("/chat.postMessage") // 기본 제공되는 명령어 느낌, 채팅을 할 수 있도록 해줌
                .header("Authorization", "Bearer " + slackBotToken) // 인증 토큰 넣기
                .bodyValue(jsonMessage)
                .retrieve()
                .bodyToMono(String.class) // 바디를 mono로 처리함 -> 응답을 단일한 String type으로 변환함
                .doOnSuccess(response -> System.out.println("Slack Response : " + response)) // 성공했을 때
                .doOnError(error -> System.out.println("Slack Error : " + error.getMessage())) // 실패했을 때
                .subscribe();

    }
}