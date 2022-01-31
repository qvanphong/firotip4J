package tech.qvanphong.firotipbot.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = "firo")
@Getter
@Setter
public class FiroProperties {
    @NestedConfigurationProperty
    private RPCProperties rpc;
    private double txFee;

}
