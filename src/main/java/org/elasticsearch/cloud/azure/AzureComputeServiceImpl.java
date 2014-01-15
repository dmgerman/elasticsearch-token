begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cloud.azure
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cloud
operator|.
name|azure
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchException
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
name|Strings
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
name|component
operator|.
name|AbstractLifecycleComponent
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
name|inject
operator|.
name|Inject
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
name|settings
operator|.
name|SettingsException
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
name|SettingsFilter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|w3c
operator|.
name|dom
operator|.
name|Document
import|;
end_import

begin_import
import|import
name|org
operator|.
name|w3c
operator|.
name|dom
operator|.
name|Node
import|;
end_import

begin_import
import|import
name|org
operator|.
name|w3c
operator|.
name|dom
operator|.
name|NodeList
import|;
end_import

begin_import
import|import
name|org
operator|.
name|xml
operator|.
name|sax
operator|.
name|SAXException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|net
operator|.
name|ssl
operator|.
name|HttpsURLConnection
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|net
operator|.
name|ssl
operator|.
name|KeyManagerFactory
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|net
operator|.
name|ssl
operator|.
name|SSLContext
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|net
operator|.
name|ssl
operator|.
name|SSLSocketFactory
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|parsers
operator|.
name|DocumentBuilder
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|parsers
operator|.
name|DocumentBuilderFactory
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|parsers
operator|.
name|ParserConfigurationException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|xpath
operator|.
name|XPath
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|xpath
operator|.
name|XPathConstants
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|xpath
operator|.
name|XPathExpressionException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|xpath
operator|.
name|XPathFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileInputStream
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
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URL
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|cert
operator|.
name|CertificateException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|AzureComputeServiceImpl
specifier|public
class|class
name|AzureComputeServiceImpl
extends|extends
name|AbstractLifecycleComponent
argument_list|<
name|AzureComputeServiceImpl
argument_list|>
implements|implements
name|AzureComputeService
block|{
DECL|class|Azure
specifier|static
specifier|final
class|class
name|Azure
block|{
DECL|field|ENDPOINT
specifier|private
specifier|static
specifier|final
name|String
name|ENDPOINT
init|=
literal|"https://management.core.windows.net/"
decl_stmt|;
DECL|field|VERSION
specifier|private
specifier|static
specifier|final
name|String
name|VERSION
init|=
literal|"2013-03-01"
decl_stmt|;
block|}
DECL|field|socketFactory
specifier|private
name|SSLSocketFactory
name|socketFactory
decl_stmt|;
DECL|field|keystore
specifier|private
specifier|final
name|String
name|keystore
decl_stmt|;
DECL|field|password
specifier|private
specifier|final
name|String
name|password
decl_stmt|;
DECL|field|subscription_id
specifier|private
specifier|final
name|String
name|subscription_id
decl_stmt|;
DECL|field|service_name
specifier|private
specifier|final
name|String
name|service_name
decl_stmt|;
DECL|field|port_name
specifier|private
specifier|final
name|String
name|port_name
decl_stmt|;
annotation|@
name|Inject
DECL|method|AzureComputeServiceImpl
specifier|public
name|AzureComputeServiceImpl
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|SettingsFilter
name|settingsFilter
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|settingsFilter
operator|.
name|addFilter
argument_list|(
operator|new
name|AzureSettingsFilter
argument_list|()
argument_list|)
expr_stmt|;
comment|// Creating socketFactory
name|subscription_id
operator|=
name|componentSettings
operator|.
name|get
argument_list|(
name|Fields
operator|.
name|SUBSCRIPTION_ID
argument_list|,
name|settings
operator|.
name|get
argument_list|(
literal|"cloud.azure."
operator|+
name|Fields
operator|.
name|SUBSCRIPTION_ID
argument_list|)
argument_list|)
expr_stmt|;
name|service_name
operator|=
name|componentSettings
operator|.
name|get
argument_list|(
name|Fields
operator|.
name|SERVICE_NAME
argument_list|,
name|settings
operator|.
name|get
argument_list|(
literal|"cloud.azure."
operator|+
name|Fields
operator|.
name|SERVICE_NAME
argument_list|)
argument_list|)
expr_stmt|;
name|keystore
operator|=
name|componentSettings
operator|.
name|get
argument_list|(
name|Fields
operator|.
name|KEYSTORE
argument_list|,
name|settings
operator|.
name|get
argument_list|(
literal|"cloud.azure."
operator|+
name|Fields
operator|.
name|KEYSTORE
argument_list|)
argument_list|)
expr_stmt|;
name|password
operator|=
name|componentSettings
operator|.
name|get
argument_list|(
name|Fields
operator|.
name|PASSWORD
argument_list|,
name|settings
operator|.
name|get
argument_list|(
literal|"cloud.azure."
operator|+
name|Fields
operator|.
name|PASSWORD
argument_list|)
argument_list|)
expr_stmt|;
name|port_name
operator|=
name|componentSettings
operator|.
name|get
argument_list|(
name|Fields
operator|.
name|PORT_NAME
argument_list|,
name|settings
operator|.
name|get
argument_list|(
literal|"cloud.azure."
operator|+
name|Fields
operator|.
name|PORT_NAME
argument_list|,
literal|"elasticsearch"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Check that we have all needed properties
try|try
block|{
name|checkProperty
argument_list|(
name|Fields
operator|.
name|SUBSCRIPTION_ID
argument_list|,
name|subscription_id
argument_list|)
expr_stmt|;
name|checkProperty
argument_list|(
name|Fields
operator|.
name|SERVICE_NAME
argument_list|,
name|service_name
argument_list|)
expr_stmt|;
name|checkProperty
argument_list|(
name|Fields
operator|.
name|KEYSTORE
argument_list|,
name|keystore
argument_list|)
expr_stmt|;
name|checkProperty
argument_list|(
name|Fields
operator|.
name|PASSWORD
argument_list|,
name|password
argument_list|)
expr_stmt|;
name|socketFactory
operator|=
name|getSocketFactory
argument_list|(
name|keystore
argument_list|,
name|password
argument_list|)
expr_stmt|;
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
name|logger
operator|.
name|trace
argument_list|(
literal|"creating new Azure client for [{}], [{}], [{}], [{}]"
argument_list|,
name|subscription_id
argument_list|,
name|service_name
argument_list|,
name|port_name
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// Can not start Azure Client
name|logger
operator|.
name|error
argument_list|(
literal|"can not start azure client: {}"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|socketFactory
operator|=
literal|null
expr_stmt|;
block|}
block|}
DECL|method|getXML
specifier|private
name|InputStream
name|getXML
parameter_list|(
name|String
name|api
parameter_list|)
throws|throws
name|UnrecoverableKeyException
throws|,
name|NoSuchAlgorithmException
throws|,
name|KeyStoreException
throws|,
name|IOException
throws|,
name|CertificateException
throws|,
name|KeyManagementException
block|{
name|String
name|https_url
init|=
name|Azure
operator|.
name|ENDPOINT
operator|+
name|subscription_id
operator|+
name|api
decl_stmt|;
name|URL
name|url
init|=
operator|new
name|URL
argument_list|(
name|https_url
argument_list|)
decl_stmt|;
name|HttpsURLConnection
name|con
init|=
operator|(
name|HttpsURLConnection
operator|)
name|url
operator|.
name|openConnection
argument_list|()
decl_stmt|;
name|con
operator|.
name|setSSLSocketFactory
argument_list|(
name|socketFactory
argument_list|)
expr_stmt|;
name|con
operator|.
name|setRequestProperty
argument_list|(
literal|"x-ms-version"
argument_list|,
name|Azure
operator|.
name|VERSION
argument_list|)
expr_stmt|;
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
name|logger
operator|.
name|debug
argument_list|(
literal|"calling azure REST API: {}"
argument_list|,
name|api
argument_list|)
expr_stmt|;
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
name|logger
operator|.
name|trace
argument_list|(
literal|"get {} from azure"
argument_list|,
name|https_url
argument_list|)
expr_stmt|;
return|return
name|con
operator|.
name|getInputStream
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|instances
specifier|public
name|Set
argument_list|<
name|Instance
argument_list|>
name|instances
parameter_list|()
block|{
if|if
condition|(
name|socketFactory
operator|==
literal|null
condition|)
block|{
comment|// Azure plugin is disabled
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
name|logger
operator|.
name|trace
argument_list|(
literal|"azure plugin is disabled. Returning an empty list of nodes."
argument_list|)
expr_stmt|;
return|return
operator|new
name|HashSet
argument_list|<
name|Instance
argument_list|>
argument_list|()
return|;
block|}
else|else
block|{
try|try
block|{
name|InputStream
name|stream
init|=
name|getXML
argument_list|(
literal|"/services/hostedservices/"
operator|+
name|service_name
operator|+
literal|"?embed-detail=true"
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|Instance
argument_list|>
name|instances
init|=
name|buildInstancesFromXml
argument_list|(
name|stream
argument_list|,
name|port_name
argument_list|)
decl_stmt|;
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
name|logger
operator|.
name|trace
argument_list|(
literal|"get instances from azure: {}"
argument_list|,
name|instances
argument_list|)
expr_stmt|;
return|return
name|instances
return|;
block|}
catch|catch
parameter_list|(
name|ParserConfigurationException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"can not parse XML response: {}"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|HashSet
argument_list|<
name|Instance
argument_list|>
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|XPathExpressionException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"can not parse XML response: {}"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|HashSet
argument_list|<
name|Instance
argument_list|>
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|SAXException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"can not parse XML response: {}"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|HashSet
argument_list|<
name|Instance
argument_list|>
argument_list|()
return|;
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
literal|"can not get list of azure nodes: {}"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|HashSet
argument_list|<
name|Instance
argument_list|>
argument_list|()
return|;
block|}
block|}
block|}
DECL|method|extractValueFromPath
specifier|private
specifier|static
name|String
name|extractValueFromPath
parameter_list|(
name|Node
name|node
parameter_list|,
name|String
name|path
parameter_list|)
throws|throws
name|XPathExpressionException
block|{
name|XPath
name|xPath
init|=
name|XPathFactory
operator|.
name|newInstance
argument_list|()
operator|.
name|newXPath
argument_list|()
decl_stmt|;
name|Node
name|subnode
init|=
operator|(
name|Node
operator|)
name|xPath
operator|.
name|compile
argument_list|(
name|path
argument_list|)
operator|.
name|evaluate
argument_list|(
name|node
argument_list|,
name|XPathConstants
operator|.
name|NODE
argument_list|)
decl_stmt|;
return|return
name|subnode
operator|.
name|getFirstChild
argument_list|()
operator|.
name|getNodeValue
argument_list|()
return|;
block|}
DECL|method|buildInstancesFromXml
specifier|public
specifier|static
name|Set
argument_list|<
name|Instance
argument_list|>
name|buildInstancesFromXml
parameter_list|(
name|InputStream
name|inputStream
parameter_list|,
name|String
name|port_name
parameter_list|)
throws|throws
name|ParserConfigurationException
throws|,
name|IOException
throws|,
name|SAXException
throws|,
name|XPathExpressionException
block|{
name|Set
argument_list|<
name|Instance
argument_list|>
name|instances
init|=
operator|new
name|HashSet
argument_list|<
name|Instance
argument_list|>
argument_list|()
decl_stmt|;
name|DocumentBuilderFactory
name|dbFactory
init|=
name|DocumentBuilderFactory
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|DocumentBuilder
name|dBuilder
init|=
name|dbFactory
operator|.
name|newDocumentBuilder
argument_list|()
decl_stmt|;
name|Document
name|doc
init|=
name|dBuilder
operator|.
name|parse
argument_list|(
name|inputStream
argument_list|)
decl_stmt|;
name|doc
operator|.
name|getDocumentElement
argument_list|()
operator|.
name|normalize
argument_list|()
expr_stmt|;
name|XPath
name|xPath
init|=
name|XPathFactory
operator|.
name|newInstance
argument_list|()
operator|.
name|newXPath
argument_list|()
decl_stmt|;
comment|// We only fetch Started nodes (TODO: should we start with all nodes whatever the status is?)
name|String
name|expression
init|=
literal|"/HostedService/Deployments/Deployment/RoleInstanceList/RoleInstance[PowerState='Started']"
decl_stmt|;
name|NodeList
name|nodeList
init|=
operator|(
name|NodeList
operator|)
name|xPath
operator|.
name|compile
argument_list|(
name|expression
argument_list|)
operator|.
name|evaluate
argument_list|(
name|doc
argument_list|,
name|XPathConstants
operator|.
name|NODESET
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|nodeList
operator|.
name|getLength
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Instance
name|instance
init|=
operator|new
name|Instance
argument_list|()
decl_stmt|;
name|Node
name|node
init|=
name|nodeList
operator|.
name|item
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|instance
operator|.
name|setPrivateIp
argument_list|(
name|extractValueFromPath
argument_list|(
name|node
argument_list|,
literal|"IpAddress"
argument_list|)
argument_list|)
expr_stmt|;
name|instance
operator|.
name|setName
argument_list|(
name|extractValueFromPath
argument_list|(
name|node
argument_list|,
literal|"InstanceName"
argument_list|)
argument_list|)
expr_stmt|;
name|instance
operator|.
name|setStatus
argument_list|(
name|Instance
operator|.
name|Status
operator|.
name|STARTED
argument_list|)
expr_stmt|;
comment|// Let's digg into<InstanceEndpoints>
name|expression
operator|=
literal|"InstanceEndpoints/InstanceEndpoint[Name='"
operator|+
name|port_name
operator|+
literal|"']"
expr_stmt|;
name|NodeList
name|endpoints
init|=
operator|(
name|NodeList
operator|)
name|xPath
operator|.
name|compile
argument_list|(
name|expression
argument_list|)
operator|.
name|evaluate
argument_list|(
name|node
argument_list|,
name|XPathConstants
operator|.
name|NODESET
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|endpoints
operator|.
name|getLength
argument_list|()
condition|;
name|j
operator|++
control|)
block|{
name|Node
name|endpoint
init|=
name|endpoints
operator|.
name|item
argument_list|(
name|j
argument_list|)
decl_stmt|;
name|instance
operator|.
name|setPublicIp
argument_list|(
name|extractValueFromPath
argument_list|(
name|endpoint
argument_list|,
literal|"Vip"
argument_list|)
argument_list|)
expr_stmt|;
name|instance
operator|.
name|setPublicPort
argument_list|(
name|extractValueFromPath
argument_list|(
name|endpoint
argument_list|,
literal|"PublicPort"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|instances
operator|.
name|add
argument_list|(
name|instance
argument_list|)
expr_stmt|;
block|}
return|return
name|instances
return|;
block|}
DECL|method|getSocketFactory
specifier|private
name|SSLSocketFactory
name|getSocketFactory
parameter_list|(
name|String
name|keystore
parameter_list|,
name|String
name|password
parameter_list|)
throws|throws
name|NoSuchAlgorithmException
throws|,
name|KeyStoreException
throws|,
name|IOException
throws|,
name|CertificateException
throws|,
name|UnrecoverableKeyException
throws|,
name|KeyManagementException
block|{
name|File
name|pKeyFile
init|=
operator|new
name|File
argument_list|(
name|keystore
argument_list|)
decl_stmt|;
name|KeyManagerFactory
name|keyManagerFactory
init|=
name|KeyManagerFactory
operator|.
name|getInstance
argument_list|(
literal|"SunX509"
argument_list|)
decl_stmt|;
name|KeyStore
name|keyStore
init|=
name|KeyStore
operator|.
name|getInstance
argument_list|(
literal|"PKCS12"
argument_list|)
decl_stmt|;
name|InputStream
name|keyInput
init|=
operator|new
name|FileInputStream
argument_list|(
name|pKeyFile
argument_list|)
decl_stmt|;
name|keyStore
operator|.
name|load
argument_list|(
name|keyInput
argument_list|,
name|password
operator|.
name|toCharArray
argument_list|()
argument_list|)
expr_stmt|;
name|keyInput
operator|.
name|close
argument_list|()
expr_stmt|;
name|keyManagerFactory
operator|.
name|init
argument_list|(
name|keyStore
argument_list|,
name|password
operator|.
name|toCharArray
argument_list|()
argument_list|)
expr_stmt|;
name|SSLContext
name|context
init|=
name|SSLContext
operator|.
name|getInstance
argument_list|(
literal|"TLS"
argument_list|)
decl_stmt|;
name|context
operator|.
name|init
argument_list|(
name|keyManagerFactory
operator|.
name|getKeyManagers
argument_list|()
argument_list|,
literal|null
argument_list|,
operator|new
name|SecureRandom
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|context
operator|.
name|getSocketFactory
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|doStart
specifier|protected
name|void
name|doStart
parameter_list|()
throws|throws
name|ElasticSearchException
block|{     }
annotation|@
name|Override
DECL|method|doStop
specifier|protected
name|void
name|doStop
parameter_list|()
throws|throws
name|ElasticSearchException
block|{     }
annotation|@
name|Override
DECL|method|doClose
specifier|protected
name|void
name|doClose
parameter_list|()
throws|throws
name|ElasticSearchException
block|{     }
DECL|method|checkProperty
specifier|private
name|void
name|checkProperty
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|value
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
if|if
condition|(
operator|!
name|Strings
operator|.
name|hasText
argument_list|(
name|value
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SettingsException
argument_list|(
literal|"cloud.azure."
operator|+
name|name
operator|+
literal|" is not set or is incorrect."
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

