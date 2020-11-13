package entity;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcServiceProperties {

    private String serviceName;

    /**
     * when an interface has multiple implementation classes, distinguish by group
     */
    private String group;

    /**
     * service version
     */
    private String version;
}
