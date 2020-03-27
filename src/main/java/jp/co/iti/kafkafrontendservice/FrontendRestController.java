package jp.co.iti.kafkafrontendservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping(value = "/kafka")
public class FrontendRestController {
    //
    private static final Logger logger = LoggerFactory.getLogger(FrontendRestController.class);

    // リクエストデータが格納される topic 名
    @Value("${kafka.request.topic}")
    private String requestTopic;

    @Autowired
    private ReplyingKafkaTemplate<Long, String, String> replyingKafkaTemplate;

    // Gradle タスクの application > bootRun で起動した場合、
    // http://localhost:8080/kafka/lesson?message=ABCDE
    // という URL で GET リクエストした場合に、当メソッドが呼び出される
    @GetMapping(value = "/lesson")
    public ResponseEntity<String> lesson(@RequestParam(value = "message", defaultValue = "defalut") String message)
            throws ExecutionException, InterruptedException, JsonProcessingException {

        // リクエストデータを作成
        Request request = new Request(System.currentTimeMillis(), message);
        // JSON 文字列化
        ObjectMapper mapper = new ObjectMapper();
        String requestValue = mapper.writeValueAsString(request);

        logger.info("#### FrontendRestController lesson. request: " + requestValue);

        // topic に格納するレコードを作成
        ProducerRecord<Long, String> record =
                new ProducerRecord<>(requestTopic, null, null, requestValue);
        // リクエスト用 topic へのレコード push と、レスポンス用 topic からのレコード pull を実行
        RequestReplyFuture<Long, String, String> future =
                replyingKafkaTemplate.sendAndReceive(record);
        ConsumerRecord<Long, String> consumerRecord = future.get();

        Response response = new Response(request,
                consumerRecord.key(), consumerRecord.value(),
                consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset());
        String responseValue = mapper.writeValueAsString(response);
        logger.info("#### FrontendRestController lesson. response: " + responseValue);

        // 受け取ったレスポンスデータを REST API のレスポンスボディに設定して返す
        return new ResponseEntity<>(responseValue, HttpStatus.OK);
    }
}
