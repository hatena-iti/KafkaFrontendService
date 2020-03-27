package jp.co.iti.kafkafrontendservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

/**
 * Kafka 関連の各種設定を担当するクラス
 * 参考：
 * https://asbnotebook.com/2020/01/05/synchronous-request-reply-using-apache-kafka-spring-boot/
 */
@Configuration
public class KafkaConfig {
    // レスポンスを受けるための Consumer に指定するグループID
    @Value("${kafka.group.id}")
    private String groupId;

    // レスポンスデータが格納される topic 名
    @Value("${kafka.reply.topic}")
    private String replyTopic;

    // 同期的な Request-Reply 形式でのメッセージ送受信を実現するための KafkaTemplate オブジェクト
    @Bean
    public ReplyingKafkaTemplate<Long, String, String> replyingKafkaTemplate(ProducerFactory<Long, String> pf,
                                                                             ConcurrentKafkaListenerContainerFactory<Long, String> factory) {
        ConcurrentMessageListenerContainer<Long, String> replyContainer = factory.createContainer(replyTopic);
        replyContainer.getContainerProperties().setMissingTopicsFatal(false);
        replyContainer.getContainerProperties().setGroupId(groupId);
        return new ReplyingKafkaTemplate<>(pf, replyContainer);
    }
}
