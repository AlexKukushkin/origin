#TM JMS JNDI properties
#java.naming.provider.url=tcp\://localhost\:61617
#java.naming.factory.initial=org.apache.activemq.jndi.ActiveMQInitialContextFactory

# Default Configuration for IBM Websphere MQ
#java.naming.provider.url = localhost:1418\/SYSTEM.AUTO.SVRCONN
#java.naming.factory.initial = com.ibm.mq.jms.context.WMQInitialContextFactory

java.naming.provider.url=localhost\:1414/SYSTEM.DEF.SVRCONN
java.naming.factory.initial=com.ibm.mq.jms.context.WMQInitialContextFactory
