package tech.qvanphong.firotipbot.properties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RPCProperties {
    private String user;
    private String pwd;
    private int port;
    private String passphrase;

    public RPCProperties() {
    }
}
