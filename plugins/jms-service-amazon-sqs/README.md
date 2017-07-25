# Manual Install

## On Host

    vi ~/.aws/credentials
        [default]
        aws_access_key_id=...
        aws_secret_access_key=...
    
    scp -P2222 -i ~/.ssh/id_rsa -o StrictHostKeyChecking=no ~/.aws/credentials root@127.0.0.1:~/.aws
    scp -P2222 -i ~/.ssh/id_rsa -o StrictHostKeyChecking=no amazon-sqs.properties root@127.0.0.1:~/sqs
    
    
## On Guest

    cp -r ~/.aws /home/kylo/
    chown -R kylo:kylo /home/kylo/.aws 
    chmod -R 700 /home/kylo/.aws
    
    cp -r ~/.aws /home/nifi/
    chown -R nifi:nifi /home/nifi/.aws 
    chmod -R 700 /home/kylo/.aws
        
    cp sqs/amazon-sqs.properties /opt/kylo/kylo-services/conf
    chown kylo:users /opt/kylo/kylo-services/conf/amazon-sqs.properties
    chmod 755 /opt/kylo/kylo-services/conf/amazon-sqs.properties
    
    vi /opt/nifi/ext-config/config.properties
        spring.profiles.active=jms-amazon-sqs
        sqs.region.name=eu-west-1

    
 