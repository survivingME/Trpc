package sample.api;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Calresult implements Serializable {
    private String message;
    private Long result;
}
