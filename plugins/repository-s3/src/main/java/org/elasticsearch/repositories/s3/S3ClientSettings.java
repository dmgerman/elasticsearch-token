begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.repositories.s3
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|repositories
operator|.
name|s3
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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

begin_import
import|import
name|com
operator|.
name|amazonaws
operator|.
name|ClientConfiguration
import|;
end_import

begin_import
import|import
name|com
operator|.
name|amazonaws
operator|.
name|Protocol
import|;
end_import

begin_import
import|import
name|com
operator|.
name|amazonaws
operator|.
name|auth
operator|.
name|BasicAWSCredentials
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
name|SecureSetting
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
name|SecureString
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
name|Setting
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
name|unit
operator|.
name|TimeValue
import|;
end_import

begin_comment
comment|/**  * A container for settings used to create an S3 client.  */
end_comment

begin_class
DECL|class|S3ClientSettings
class|class
name|S3ClientSettings
block|{
comment|// prefix for s3 client settings
DECL|field|PREFIX
specifier|private
specifier|static
specifier|final
name|String
name|PREFIX
init|=
literal|"s3.client."
decl_stmt|;
comment|/** The access key (ie login id) for connecting to s3. */
DECL|field|ACCESS_KEY_SETTING
specifier|static
specifier|final
name|Setting
operator|.
name|AffixSetting
argument_list|<
name|SecureString
argument_list|>
name|ACCESS_KEY_SETTING
init|=
name|Setting
operator|.
name|affixKeySetting
argument_list|(
name|PREFIX
argument_list|,
literal|"access_key"
argument_list|,
name|key
lambda|->
name|SecureSetting
operator|.
name|secureString
argument_list|(
name|key
argument_list|,
literal|null
argument_list|)
argument_list|)
decl_stmt|;
comment|/** The secret key (ie password) for connecting to s3. */
DECL|field|SECRET_KEY_SETTING
specifier|static
specifier|final
name|Setting
operator|.
name|AffixSetting
argument_list|<
name|SecureString
argument_list|>
name|SECRET_KEY_SETTING
init|=
name|Setting
operator|.
name|affixKeySetting
argument_list|(
name|PREFIX
argument_list|,
literal|"secret_key"
argument_list|,
name|key
lambda|->
name|SecureSetting
operator|.
name|secureString
argument_list|(
name|key
argument_list|,
literal|null
argument_list|)
argument_list|)
decl_stmt|;
comment|/** An override for the s3 endpoint to connect to. */
DECL|field|ENDPOINT_SETTING
specifier|static
specifier|final
name|Setting
operator|.
name|AffixSetting
argument_list|<
name|String
argument_list|>
name|ENDPOINT_SETTING
init|=
name|Setting
operator|.
name|affixKeySetting
argument_list|(
name|PREFIX
argument_list|,
literal|"endpoint"
argument_list|,
name|key
lambda|->
operator|new
name|Setting
argument_list|<>
argument_list|(
name|key
argument_list|,
literal|""
argument_list|,
name|s
lambda|->
name|s
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|,
name|Setting
operator|.
name|Property
operator|.
name|NodeScope
argument_list|)
argument_list|)
decl_stmt|;
comment|/** The protocol to use to connect to s3. */
DECL|field|PROTOCOL_SETTING
specifier|static
specifier|final
name|Setting
operator|.
name|AffixSetting
argument_list|<
name|Protocol
argument_list|>
name|PROTOCOL_SETTING
init|=
name|Setting
operator|.
name|affixKeySetting
argument_list|(
name|PREFIX
argument_list|,
literal|"protocol"
argument_list|,
name|key
lambda|->
operator|new
name|Setting
argument_list|<>
argument_list|(
name|key
argument_list|,
literal|"https"
argument_list|,
name|s
lambda|->
name|Protocol
operator|.
name|valueOf
argument_list|(
name|s
operator|.
name|toUpperCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
argument_list|,
name|Setting
operator|.
name|Property
operator|.
name|NodeScope
argument_list|)
argument_list|)
decl_stmt|;
comment|/** The host name of a proxy to connect to s3 through. */
DECL|field|PROXY_HOST_SETTING
specifier|static
specifier|final
name|Setting
operator|.
name|AffixSetting
argument_list|<
name|String
argument_list|>
name|PROXY_HOST_SETTING
init|=
name|Setting
operator|.
name|affixKeySetting
argument_list|(
name|PREFIX
argument_list|,
literal|"proxy.host"
argument_list|,
name|key
lambda|->
name|Setting
operator|.
name|simpleString
argument_list|(
name|key
argument_list|,
name|Setting
operator|.
name|Property
operator|.
name|NodeScope
argument_list|)
argument_list|)
decl_stmt|;
comment|/** The port of a proxy to connect to s3 through. */
DECL|field|PROXY_PORT_SETTING
specifier|static
specifier|final
name|Setting
operator|.
name|AffixSetting
argument_list|<
name|Integer
argument_list|>
name|PROXY_PORT_SETTING
init|=
name|Setting
operator|.
name|affixKeySetting
argument_list|(
name|PREFIX
argument_list|,
literal|"proxy.port"
argument_list|,
name|key
lambda|->
name|Setting
operator|.
name|intSetting
argument_list|(
name|key
argument_list|,
literal|80
argument_list|,
literal|0
argument_list|,
literal|1
operator|<<
literal|16
argument_list|,
name|Setting
operator|.
name|Property
operator|.
name|NodeScope
argument_list|)
argument_list|)
decl_stmt|;
comment|/** The username of a proxy to connect to s3 through. */
DECL|field|PROXY_USERNAME_SETTING
specifier|static
specifier|final
name|Setting
operator|.
name|AffixSetting
argument_list|<
name|SecureString
argument_list|>
name|PROXY_USERNAME_SETTING
init|=
name|Setting
operator|.
name|affixKeySetting
argument_list|(
name|PREFIX
argument_list|,
literal|"proxy.username"
argument_list|,
name|key
lambda|->
name|SecureSetting
operator|.
name|secureString
argument_list|(
name|key
argument_list|,
literal|null
argument_list|)
argument_list|)
decl_stmt|;
comment|/** The password of a proxy to connect to s3 through. */
DECL|field|PROXY_PASSWORD_SETTING
specifier|static
specifier|final
name|Setting
operator|.
name|AffixSetting
argument_list|<
name|SecureString
argument_list|>
name|PROXY_PASSWORD_SETTING
init|=
name|Setting
operator|.
name|affixKeySetting
argument_list|(
name|PREFIX
argument_list|,
literal|"proxy.password"
argument_list|,
name|key
lambda|->
name|SecureSetting
operator|.
name|secureString
argument_list|(
name|key
argument_list|,
literal|null
argument_list|)
argument_list|)
decl_stmt|;
comment|/** The socket timeout for connecting to s3. */
DECL|field|READ_TIMEOUT_SETTING
specifier|static
specifier|final
name|Setting
operator|.
name|AffixSetting
argument_list|<
name|TimeValue
argument_list|>
name|READ_TIMEOUT_SETTING
init|=
name|Setting
operator|.
name|affixKeySetting
argument_list|(
name|PREFIX
argument_list|,
literal|"read_timeout"
argument_list|,
name|key
lambda|->
name|Setting
operator|.
name|timeSetting
argument_list|(
name|key
argument_list|,
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
name|ClientConfiguration
operator|.
name|DEFAULT_SOCKET_TIMEOUT
argument_list|)
argument_list|,
name|Setting
operator|.
name|Property
operator|.
name|NodeScope
argument_list|)
argument_list|)
decl_stmt|;
comment|/** Credentials to authenticate with s3. */
DECL|field|credentials
specifier|final
name|BasicAWSCredentials
name|credentials
decl_stmt|;
comment|/** The s3 endpoint the client should talk to, or empty string to use the default. */
DECL|field|endpoint
specifier|final
name|String
name|endpoint
decl_stmt|;
comment|/** The protocol to use to talk to s3. Defaults to https. */
DECL|field|protocol
specifier|final
name|Protocol
name|protocol
decl_stmt|;
comment|/** An optional proxy host that requests to s3 should be made through. */
DECL|field|proxyHost
specifier|final
name|String
name|proxyHost
decl_stmt|;
comment|/** The port number the proxy host should be connected on. */
DECL|field|proxyPort
specifier|final
name|int
name|proxyPort
decl_stmt|;
comment|// these should be "secure" yet the api for the s3 client only takes String, so storing them
comment|// as SecureString here won't really help with anything
comment|/** An optional username for the proxy host, for basic authentication. */
DECL|field|proxyUsername
specifier|final
name|String
name|proxyUsername
decl_stmt|;
comment|/** An optional password for the proxy host, for basic authentication. */
DECL|field|proxyPassword
specifier|final
name|String
name|proxyPassword
decl_stmt|;
comment|/** The read timeout for the s3 client. */
DECL|field|readTimeoutMillis
specifier|final
name|int
name|readTimeoutMillis
decl_stmt|;
DECL|method|S3ClientSettings
specifier|private
name|S3ClientSettings
parameter_list|(
name|BasicAWSCredentials
name|credentials
parameter_list|,
name|String
name|endpoint
parameter_list|,
name|Protocol
name|protocol
parameter_list|,
name|String
name|proxyHost
parameter_list|,
name|int
name|proxyPort
parameter_list|,
name|String
name|proxyUsername
parameter_list|,
name|String
name|proxyPassword
parameter_list|,
name|int
name|readTimeoutMillis
parameter_list|)
block|{
name|this
operator|.
name|credentials
operator|=
name|credentials
expr_stmt|;
name|this
operator|.
name|endpoint
operator|=
name|endpoint
expr_stmt|;
name|this
operator|.
name|protocol
operator|=
name|protocol
expr_stmt|;
name|this
operator|.
name|proxyHost
operator|=
name|proxyHost
expr_stmt|;
name|this
operator|.
name|proxyPort
operator|=
name|proxyPort
expr_stmt|;
name|this
operator|.
name|proxyUsername
operator|=
name|proxyUsername
expr_stmt|;
name|this
operator|.
name|proxyPassword
operator|=
name|proxyPassword
expr_stmt|;
name|this
operator|.
name|readTimeoutMillis
operator|=
name|readTimeoutMillis
expr_stmt|;
block|}
comment|/**      * Load all client settings from the given settings.      *      * Note this will always at least return a client named "default".      */
DECL|method|load
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|S3ClientSettings
argument_list|>
name|load
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|clientNames
init|=
name|settings
operator|.
name|getGroups
argument_list|(
name|PREFIX
argument_list|)
operator|.
name|keySet
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|S3ClientSettings
argument_list|>
name|clients
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|clientName
range|:
name|clientNames
control|)
block|{
name|clients
operator|.
name|put
argument_list|(
name|clientName
argument_list|,
name|getClientSettings
argument_list|(
name|settings
argument_list|,
name|clientName
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|clients
operator|.
name|containsKey
argument_list|(
literal|"default"
argument_list|)
operator|==
literal|false
condition|)
block|{
comment|// this won't find any settings under the default client,
comment|// but it will pull all the fallback static settings
name|clients
operator|.
name|put
argument_list|(
literal|"default"
argument_list|,
name|getClientSettings
argument_list|(
name|settings
argument_list|,
literal|"default"
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|clients
argument_list|)
return|;
block|}
comment|// pkg private for tests
comment|/** Parse settings for a single client. */
DECL|method|getClientSettings
specifier|static
name|S3ClientSettings
name|getClientSettings
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|String
name|clientName
parameter_list|)
block|{
try|try
init|(
name|SecureString
name|accessKey
init|=
name|getConfigValue
argument_list|(
name|settings
argument_list|,
name|clientName
argument_list|,
name|ACCESS_KEY_SETTING
argument_list|)
init|;
name|SecureString
name|secretKey
operator|=
name|getConfigValue
argument_list|(
name|settings
argument_list|,
name|clientName
argument_list|,
name|SECRET_KEY_SETTING
argument_list|)
init|;
name|SecureString
name|proxyUsername
operator|=
name|getConfigValue
argument_list|(
name|settings
argument_list|,
name|clientName
argument_list|,
name|PROXY_USERNAME_SETTING
argument_list|)
init|;
name|SecureString
name|proxyPassword
operator|=
name|getConfigValue
argument_list|(
name|settings
argument_list|,
name|clientName
argument_list|,
name|PROXY_PASSWORD_SETTING
argument_list|)
init|)
block|{
name|BasicAWSCredentials
name|credentials
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|accessKey
operator|.
name|length
argument_list|()
operator|!=
literal|0
condition|)
block|{
if|if
condition|(
name|secretKey
operator|.
name|length
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|credentials
operator|=
operator|new
name|BasicAWSCredentials
argument_list|(
name|accessKey
operator|.
name|toString
argument_list|()
argument_list|,
name|secretKey
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Missing secret key for s3 client ["
operator|+
name|clientName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|secretKey
operator|.
name|length
argument_list|()
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Missing access key for s3 client ["
operator|+
name|clientName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
operator|new
name|S3ClientSettings
argument_list|(
name|credentials
argument_list|,
name|getConfigValue
argument_list|(
name|settings
argument_list|,
name|clientName
argument_list|,
name|ENDPOINT_SETTING
argument_list|)
argument_list|,
name|getConfigValue
argument_list|(
name|settings
argument_list|,
name|clientName
argument_list|,
name|PROTOCOL_SETTING
argument_list|)
argument_list|,
name|getConfigValue
argument_list|(
name|settings
argument_list|,
name|clientName
argument_list|,
name|PROXY_HOST_SETTING
argument_list|)
argument_list|,
name|getConfigValue
argument_list|(
name|settings
argument_list|,
name|clientName
argument_list|,
name|PROXY_PORT_SETTING
argument_list|)
argument_list|,
name|proxyUsername
operator|.
name|toString
argument_list|()
argument_list|,
name|proxyPassword
operator|.
name|toString
argument_list|()
argument_list|,
operator|(
name|int
operator|)
name|getConfigValue
argument_list|(
name|settings
argument_list|,
name|clientName
argument_list|,
name|READ_TIMEOUT_SETTING
argument_list|)
operator|.
name|millis
argument_list|()
argument_list|)
return|;
block|}
block|}
DECL|method|getConfigValue
specifier|private
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|getConfigValue
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|String
name|clientName
parameter_list|,
name|Setting
operator|.
name|AffixSetting
argument_list|<
name|T
argument_list|>
name|clientSetting
parameter_list|)
block|{
name|Setting
argument_list|<
name|T
argument_list|>
name|concreteSetting
init|=
name|clientSetting
operator|.
name|getConcreteSettingForNamespace
argument_list|(
name|clientName
argument_list|)
decl_stmt|;
return|return
name|concreteSetting
operator|.
name|get
argument_list|(
name|settings
argument_list|)
return|;
block|}
block|}
end_class

end_unit

