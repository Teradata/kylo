# Manual Install

## On Host

    scp -P2222 -i ~/.ssh/id_rsa activemq.properties root@127.0.0.1:~
    
## On Guest

    cp activemq.properties /opt/kylo/kylo-services/conf
    chown kylo:users /opt/kylo/kylo-services/conf/activemq.properties
    chmod 755 /opt/kylo/kylo-services/conf/activemq.properties

    vi /opt/nifi/ext-config/config.properties
        spring.profiles.active=jms-activemq
        jms.activemq.broker.url=tcp://localhost:61616


    
 