package jp.co.iti.kafkafrontendservice;

import lombok.*;

/**
 * リクエストデータ用クラス
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Request {
    //
    private Long index;
    //
    private String message;
}
