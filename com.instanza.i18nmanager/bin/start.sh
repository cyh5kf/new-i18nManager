rm -f tpid

nohup /usr/local/jdk1.8/jre/bin/java -jar 'jars/com.instanza.i18nmanager-0.0.1-SNAPSHOT.jar' --spring.config.location=config/application.properties > ./logs/i18nmanager.log 2>&1 &

echo $! > tpid

echo Start Success!