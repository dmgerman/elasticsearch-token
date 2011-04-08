begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.jmx
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|jmx
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|logging
operator|.
name|ESLogger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|network
operator|.
name|NetworkService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|Settings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|transport
operator|.
name|PortsRange
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|InstanceAlreadyExistsException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|MBeanServer
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|ObjectName
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|remote
operator|.
name|JMXConnectorServer
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|remote
operator|.
name|JMXConnectorServerFactory
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|remote
operator|.
name|JMXServiceURL
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|ManagementFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|rmi
operator|.
name|registry
operator|.
name|LocateRegistry
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|CopyOnWriteArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicReference
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|JmxService
specifier|public
class|class
name|JmxService
block|{
DECL|class|SettingsConstants
specifier|public
specifier|static
class|class
name|SettingsConstants
block|{
DECL|field|CREATE_CONNECTOR
specifier|public
specifier|static
specifier|final
name|String
name|CREATE_CONNECTOR
init|=
literal|"jmx.create_connector"
decl_stmt|;
block|}
comment|// we use {jmx.port} without prefix of $ since we don't want it to be resolved as a setting property
DECL|field|JMXRMI_URI_PATTERN
specifier|public
specifier|static
specifier|final
name|String
name|JMXRMI_URI_PATTERN
init|=
literal|"service:jmx:rmi:///jndi/rmi://:{jmx.port}/jmxrmi"
decl_stmt|;
DECL|field|JMXRMI_PUBLISH_URI_PATTERN
specifier|public
specifier|static
specifier|final
name|String
name|JMXRMI_PUBLISH_URI_PATTERN
init|=
literal|"service:jmx:rmi:///jndi/rmi://{jmx.host}:{jmx.port}/jmxrmi"
decl_stmt|;
DECL|field|logger
specifier|private
specifier|final
name|ESLogger
name|logger
decl_stmt|;
DECL|field|settings
specifier|private
specifier|final
name|Settings
name|settings
decl_stmt|;
DECL|field|jmxDomain
specifier|private
specifier|final
name|String
name|jmxDomain
decl_stmt|;
DECL|field|serviceUrl
specifier|private
name|String
name|serviceUrl
decl_stmt|;
DECL|field|publishUrl
specifier|private
name|String
name|publishUrl
decl_stmt|;
DECL|field|mBeanServer
specifier|private
specifier|final
name|MBeanServer
name|mBeanServer
decl_stmt|;
DECL|field|connectorServer
specifier|private
name|JMXConnectorServer
name|connectorServer
decl_stmt|;
DECL|field|constructionMBeans
specifier|private
specifier|final
name|CopyOnWriteArrayList
argument_list|<
name|ResourceDMBean
argument_list|>
name|constructionMBeans
init|=
operator|new
name|CopyOnWriteArrayList
argument_list|<
name|ResourceDMBean
argument_list|>
argument_list|()
decl_stmt|;
DECL|field|registeredMBeans
specifier|private
specifier|final
name|CopyOnWriteArrayList
argument_list|<
name|ResourceDMBean
argument_list|>
name|registeredMBeans
init|=
operator|new
name|CopyOnWriteArrayList
argument_list|<
name|ResourceDMBean
argument_list|>
argument_list|()
decl_stmt|;
DECL|field|nodeDescription
specifier|private
name|String
name|nodeDescription
decl_stmt|;
DECL|field|started
specifier|private
specifier|volatile
name|boolean
name|started
init|=
literal|false
decl_stmt|;
DECL|method|JmxService
specifier|public
name|JmxService
parameter_list|(
name|ESLogger
name|logger
parameter_list|,
specifier|final
name|Settings
name|settings
parameter_list|)
block|{
name|this
operator|.
name|logger
operator|=
name|logger
expr_stmt|;
name|this
operator|.
name|settings
operator|=
name|settings
expr_stmt|;
name|this
operator|.
name|jmxDomain
operator|=
name|settings
operator|.
name|get
argument_list|(
literal|"jmx.domain"
argument_list|,
literal|"org.elasticsearch"
argument_list|)
expr_stmt|;
name|this
operator|.
name|mBeanServer
operator|=
name|ManagementFactory
operator|.
name|getPlatformMBeanServer
argument_list|()
expr_stmt|;
block|}
DECL|method|serviceUrl
specifier|public
name|String
name|serviceUrl
parameter_list|()
block|{
return|return
name|this
operator|.
name|serviceUrl
return|;
block|}
DECL|method|publishUrl
specifier|public
name|String
name|publishUrl
parameter_list|()
block|{
return|return
name|this
operator|.
name|publishUrl
return|;
block|}
DECL|method|connectAndRegister
specifier|public
name|void
name|connectAndRegister
parameter_list|(
name|String
name|nodeDescription
parameter_list|,
specifier|final
name|NetworkService
name|networkService
parameter_list|)
block|{
if|if
condition|(
name|started
condition|)
block|{
return|return;
block|}
name|started
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|nodeDescription
operator|=
name|nodeDescription
expr_stmt|;
if|if
condition|(
name|settings
operator|.
name|getAsBoolean
argument_list|(
name|SettingsConstants
operator|.
name|CREATE_CONNECTOR
argument_list|,
literal|false
argument_list|)
condition|)
block|{
comment|// we are going to create the connector, set the GC interval to a large value
try|try
block|{
if|if
condition|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"sun.rmi.dgc.client.gcInterval"
argument_list|)
operator|==
literal|null
condition|)
name|System
operator|.
name|setProperty
argument_list|(
literal|"sun.rmi.dgc.client.gcInterval"
argument_list|,
literal|"36000000"
argument_list|)
expr_stmt|;
if|if
condition|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"sun.rmi.dgc.server.gcInterval"
argument_list|)
operator|==
literal|null
condition|)
name|System
operator|.
name|setProperty
argument_list|(
literal|"sun.rmi.dgc.server.gcInterval"
argument_list|,
literal|"36000000"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|secExc
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Failed to set sun.rmi.dgc.xxx system properties"
argument_list|,
name|secExc
argument_list|)
expr_stmt|;
block|}
specifier|final
name|String
name|port
init|=
name|settings
operator|.
name|get
argument_list|(
literal|"jmx.port"
argument_list|,
literal|"9400-9500"
argument_list|)
decl_stmt|;
name|PortsRange
name|portsRange
init|=
operator|new
name|PortsRange
argument_list|(
name|port
argument_list|)
decl_stmt|;
specifier|final
name|AtomicReference
argument_list|<
name|Exception
argument_list|>
name|lastException
init|=
operator|new
name|AtomicReference
argument_list|<
name|Exception
argument_list|>
argument_list|()
decl_stmt|;
name|boolean
name|success
init|=
name|portsRange
operator|.
name|iterate
argument_list|(
operator|new
name|PortsRange
operator|.
name|PortCallback
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|onPortNumber
parameter_list|(
name|int
name|portNumber
parameter_list|)
block|{
try|try
block|{
name|LocateRegistry
operator|.
name|createRegistry
argument_list|(
name|portNumber
argument_list|)
expr_stmt|;
name|serviceUrl
operator|=
name|settings
operator|.
name|get
argument_list|(
literal|"jmx.service_url"
argument_list|,
name|JMXRMI_URI_PATTERN
argument_list|)
operator|.
name|replace
argument_list|(
literal|"{jmx.port}"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|portNumber
argument_list|)
argument_list|)
expr_stmt|;
comment|// Create the JMX service URL.
name|JMXServiceURL
name|url
init|=
operator|new
name|JMXServiceURL
argument_list|(
name|serviceUrl
argument_list|)
decl_stmt|;
comment|// Create the connector server now.
name|connectorServer
operator|=
name|JMXConnectorServerFactory
operator|.
name|newJMXConnectorServer
argument_list|(
name|url
argument_list|,
name|settings
operator|.
name|getAsMap
argument_list|()
argument_list|,
name|mBeanServer
argument_list|)
expr_stmt|;
name|connectorServer
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// create the publish url
name|String
name|publishHost
init|=
name|networkService
operator|.
name|resolvePublishHostAddress
argument_list|(
name|settings
operator|.
name|get
argument_list|(
literal|"jmx.publish_host"
argument_list|)
argument_list|)
operator|.
name|getHostAddress
argument_list|()
decl_stmt|;
name|publishUrl
operator|=
name|settings
operator|.
name|get
argument_list|(
literal|"jmx.publish_url"
argument_list|,
name|JMXRMI_PUBLISH_URI_PATTERN
argument_list|)
operator|.
name|replace
argument_list|(
literal|"{jmx.port}"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|portNumber
argument_list|)
argument_list|)
operator|.
name|replace
argument_list|(
literal|"{jmx.host}"
argument_list|,
name|publishHost
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|lastException
operator|.
name|set
argument_list|(
name|e
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
block|}
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|success
condition|)
block|{
throw|throw
operator|new
name|JmxConnectorCreationException
argument_list|(
literal|"Failed to bind to ["
operator|+
name|port
operator|+
literal|"]"
argument_list|,
name|lastException
operator|.
name|get
argument_list|()
argument_list|)
throw|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"bound_address {{}}, publish_address {{}}"
argument_list|,
name|serviceUrl
argument_list|,
name|publishUrl
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|ResourceDMBean
name|resource
range|:
name|constructionMBeans
control|)
block|{
name|register
argument_list|(
name|resource
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|registerMBean
specifier|public
name|void
name|registerMBean
parameter_list|(
name|Object
name|instance
parameter_list|)
block|{
name|ResourceDMBean
name|resourceDMBean
init|=
operator|new
name|ResourceDMBean
argument_list|(
name|instance
argument_list|,
name|logger
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|resourceDMBean
operator|.
name|isManagedResource
argument_list|()
condition|)
block|{
return|return;
block|}
if|if
condition|(
operator|!
name|started
condition|)
block|{
name|constructionMBeans
operator|.
name|add
argument_list|(
name|resourceDMBean
argument_list|)
expr_stmt|;
return|return;
block|}
name|register
argument_list|(
name|resourceDMBean
argument_list|)
expr_stmt|;
block|}
DECL|method|unregisterGroup
specifier|public
name|void
name|unregisterGroup
parameter_list|(
name|String
name|groupName
parameter_list|)
block|{
for|for
control|(
name|ResourceDMBean
name|resource
range|:
name|registeredMBeans
control|)
block|{
if|if
condition|(
operator|!
name|groupName
operator|.
name|equals
argument_list|(
name|resource
operator|.
name|getGroupName
argument_list|()
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|registeredMBeans
operator|.
name|remove
argument_list|(
name|resource
argument_list|)
expr_stmt|;
name|String
name|resourceName
init|=
name|resource
operator|.
name|getFullObjectName
argument_list|()
decl_stmt|;
try|try
block|{
name|ObjectName
name|objectName
init|=
operator|new
name|ObjectName
argument_list|(
name|getObjectName
argument_list|(
name|resourceName
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|mBeanServer
operator|.
name|isRegistered
argument_list|(
name|objectName
argument_list|)
condition|)
block|{
name|mBeanServer
operator|.
name|unregisterMBean
argument_list|(
name|objectName
argument_list|)
expr_stmt|;
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"Unregistered "
operator|+
name|objectName
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Failed to unregister "
operator|+
name|resource
operator|.
name|getFullObjectName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
operator|!
name|started
condition|)
block|{
return|return;
block|}
name|started
operator|=
literal|false
expr_stmt|;
comment|// unregister mbeans
for|for
control|(
name|ResourceDMBean
name|resource
range|:
name|registeredMBeans
control|)
block|{
name|String
name|resourceName
init|=
name|resource
operator|.
name|getFullObjectName
argument_list|()
decl_stmt|;
try|try
block|{
name|ObjectName
name|objectName
init|=
operator|new
name|ObjectName
argument_list|(
name|getObjectName
argument_list|(
name|resourceName
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|mBeanServer
operator|.
name|isRegistered
argument_list|(
name|objectName
argument_list|)
condition|)
block|{
name|mBeanServer
operator|.
name|unregisterMBean
argument_list|(
name|objectName
argument_list|)
expr_stmt|;
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"Unregistered "
operator|+
name|objectName
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Failed to unregister "
operator|+
name|resource
operator|.
name|getFullObjectName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|connectorServer
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|connectorServer
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Failed to close connector"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|register
specifier|private
name|void
name|register
parameter_list|(
name|ResourceDMBean
name|resourceDMBean
parameter_list|)
throws|throws
name|JmxRegistrationException
block|{
try|try
block|{
name|String
name|resourceName
init|=
name|resourceDMBean
operator|.
name|getFullObjectName
argument_list|()
decl_stmt|;
name|ObjectName
name|objectName
init|=
operator|new
name|ObjectName
argument_list|(
name|getObjectName
argument_list|(
name|resourceName
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|mBeanServer
operator|.
name|isRegistered
argument_list|(
name|objectName
argument_list|)
condition|)
block|{
try|try
block|{
name|mBeanServer
operator|.
name|registerMBean
argument_list|(
name|resourceDMBean
argument_list|,
name|objectName
argument_list|)
expr_stmt|;
name|registeredMBeans
operator|.
name|add
argument_list|(
name|resourceDMBean
argument_list|)
expr_stmt|;
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"Registered "
operator|+
name|resourceDMBean
operator|+
literal|" under "
operator|+
name|objectName
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InstanceAlreadyExistsException
name|e
parameter_list|)
block|{
comment|//this might happen if multiple instances are trying to concurrently register same objectName
name|logger
operator|.
name|warn
argument_list|(
literal|"Could not register object with name:"
operator|+
name|objectName
operator|+
literal|"("
operator|+
name|e
operator|.
name|getMessage
argument_list|()
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Could not register object with name: "
operator|+
name|objectName
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Could not register object with name: "
operator|+
name|resourceDMBean
operator|.
name|getFullObjectName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|getObjectName
specifier|private
name|String
name|getObjectName
parameter_list|(
name|String
name|resourceName
parameter_list|)
block|{
return|return
name|getObjectName
argument_list|(
name|jmxDomain
argument_list|,
name|resourceName
argument_list|)
return|;
block|}
DECL|method|getObjectName
specifier|private
name|String
name|getObjectName
parameter_list|(
name|String
name|jmxDomain
parameter_list|,
name|String
name|resourceName
parameter_list|)
block|{
return|return
name|jmxDomain
operator|+
literal|":"
operator|+
name|resourceName
return|;
block|}
block|}
end_class

end_unit

