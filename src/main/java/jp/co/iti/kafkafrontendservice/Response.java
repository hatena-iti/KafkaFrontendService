package jp.co.iti.kafkafrontendservice;

import lombok.*;

/**
 * レスポンスデータ用クラス
 */
@Value
public class Response {
    //
    Request request;
    //
    Long key;
    //
    String value;
    //
    String topic;
    //
    int partition;
    //
    long offset;
}
