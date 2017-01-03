begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cloud.aws
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cloud
operator|.
name|aws
package|;
end_package

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
name|AWSCredentialsProvider
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
name|com
operator|.
name|amazonaws
operator|.
name|auth
operator|.
name|DefaultAWSCredentialsProviderChain
import|;
end_import

begin_import
import|import
name|com
operator|.
name|amazonaws
operator|.
name|http
operator|.
name|IdleConnectionReaper
import|;
end_import

begin_import
import|import
name|com
operator|.
name|amazonaws
operator|.
name|internal
operator|.
name|StaticCredentialsProvider
import|;
end_import

begin_import
import|import
name|com
operator|.
name|amazonaws
operator|.
name|services
operator|.
name|s3
operator|.
name|AmazonS3
import|;
end_import

begin_import
import|import
name|com
operator|.
name|amazonaws
operator|.
name|services
operator|.
name|s3
operator|.
name|AmazonS3Client
import|;
end_import

begin_import
import|import
name|com
operator|.
name|amazonaws
operator|.
name|services
operator|.
name|s3
operator|.
name|S3ClientOptions
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|collect
operator|.
name|Tuple
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
name|repositories
operator|.
name|s3
operator|.
name|S3Repository
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
name|Map
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|repositories
operator|.
name|s3
operator|.
name|S3Repository
operator|.
name|getValue
import|;
end_import

begin_class
DECL|class|InternalAwsS3Service
specifier|public
class|class
name|InternalAwsS3Service
extends|extends
name|AbstractLifecycleComponent
implements|implements
name|AwsS3Service
block|{
comment|/**      * (acceskey, endpoint) -&gt; client      */
DECL|field|clients
specifier|private
name|Map
argument_list|<
name|Tuple
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|,
name|AmazonS3Client
argument_list|>
name|clients
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|method|InternalAwsS3Service
specifier|public
name|InternalAwsS3Service
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|client
specifier|public
specifier|synchronized
name|AmazonS3
name|client
parameter_list|(
name|Settings
name|repositorySettings
parameter_list|,
name|String
name|endpoint
parameter_list|,
name|Protocol
name|protocol
parameter_list|,
name|String
name|region
parameter_list|,
name|Integer
name|maxRetries
parameter_list|,
name|boolean
name|useThrottleRetries
parameter_list|,
name|Boolean
name|pathStyleAccess
parameter_list|)
block|{
name|String
name|foundEndpoint
init|=
name|findEndpoint
argument_list|(
name|logger
argument_list|,
name|settings
argument_list|,
name|endpoint
argument_list|,
name|region
argument_list|)
decl_stmt|;
name|AWSCredentialsProvider
name|credentials
init|=
name|buildCredentials
argument_list|(
name|logger
argument_list|,
name|settings
argument_list|,
name|repositorySettings
argument_list|)
decl_stmt|;
name|Tuple
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|clientDescriptor
init|=
operator|new
name|Tuple
argument_list|<>
argument_list|(
name|foundEndpoint
argument_list|,
name|credentials
operator|.
name|getCredentials
argument_list|()
operator|.
name|getAWSAccessKeyId
argument_list|()
argument_list|)
decl_stmt|;
name|AmazonS3Client
name|client
init|=
name|clients
operator|.
name|get
argument_list|(
name|clientDescriptor
argument_list|)
decl_stmt|;
if|if
condition|(
name|client
operator|!=
literal|null
condition|)
block|{
return|return
name|client
return|;
block|}
name|client
operator|=
operator|new
name|AmazonS3Client
argument_list|(
name|credentials
argument_list|,
name|buildConfiguration
argument_list|(
name|logger
argument_list|,
name|settings
argument_list|,
name|protocol
argument_list|,
name|maxRetries
argument_list|,
name|foundEndpoint
argument_list|,
name|useThrottleRetries
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|pathStyleAccess
operator|!=
literal|null
condition|)
block|{
name|client
operator|.
name|setS3ClientOptions
argument_list|(
operator|new
name|S3ClientOptions
argument_list|()
operator|.
name|withPathStyleAccess
argument_list|(
name|pathStyleAccess
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|foundEndpoint
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|client
operator|.
name|setEndpoint
argument_list|(
name|foundEndpoint
argument_list|)
expr_stmt|;
block|}
name|clients
operator|.
name|put
argument_list|(
name|clientDescriptor
argument_list|,
name|client
argument_list|)
expr_stmt|;
return|return
name|client
return|;
block|}
DECL|method|buildConfiguration
specifier|public
specifier|static
name|ClientConfiguration
name|buildConfiguration
parameter_list|(
name|Logger
name|logger
parameter_list|,
name|Settings
name|settings
parameter_list|,
name|Protocol
name|protocol
parameter_list|,
name|Integer
name|maxRetries
parameter_list|,
name|String
name|endpoint
parameter_list|,
name|boolean
name|useThrottleRetries
parameter_list|)
block|{
name|ClientConfiguration
name|clientConfiguration
init|=
operator|new
name|ClientConfiguration
argument_list|()
decl_stmt|;
comment|// the response metadata cache is only there for diagnostics purposes,
comment|// but can force objects from every response to the old generation.
name|clientConfiguration
operator|.
name|setResponseMetadataCacheSize
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|clientConfiguration
operator|.
name|setProtocol
argument_list|(
name|protocol
argument_list|)
expr_stmt|;
name|String
name|proxyHost
init|=
name|CLOUD_S3
operator|.
name|PROXY_HOST_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
decl_stmt|;
if|if
condition|(
name|Strings
operator|.
name|hasText
argument_list|(
name|proxyHost
argument_list|)
condition|)
block|{
name|Integer
name|proxyPort
init|=
name|CLOUD_S3
operator|.
name|PROXY_PORT_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|String
name|proxyUsername
init|=
name|CLOUD_S3
operator|.
name|PROXY_USERNAME_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|String
name|proxyPassword
init|=
name|CLOUD_S3
operator|.
name|PROXY_PASSWORD_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|clientConfiguration
operator|.
name|withProxyHost
argument_list|(
name|proxyHost
argument_list|)
operator|.
name|withProxyPort
argument_list|(
name|proxyPort
argument_list|)
operator|.
name|withProxyUsername
argument_list|(
name|proxyUsername
argument_list|)
operator|.
name|withProxyPassword
argument_list|(
name|proxyPassword
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|maxRetries
operator|!=
literal|null
condition|)
block|{
comment|// If not explicitly set, default to 3 with exponential backoff policy
name|clientConfiguration
operator|.
name|setMaxErrorRetry
argument_list|(
name|maxRetries
argument_list|)
expr_stmt|;
block|}
name|clientConfiguration
operator|.
name|setUseThrottleRetries
argument_list|(
name|useThrottleRetries
argument_list|)
expr_stmt|;
comment|// #155: we might have 3rd party users using older S3 API version
name|String
name|awsSigner
init|=
name|CLOUD_S3
operator|.
name|SIGNER_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
decl_stmt|;
if|if
condition|(
name|Strings
operator|.
name|hasText
argument_list|(
name|awsSigner
argument_list|)
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"using AWS API signer [{}]"
argument_list|,
name|awsSigner
argument_list|)
expr_stmt|;
name|AwsSigner
operator|.
name|configureSigner
argument_list|(
name|awsSigner
argument_list|,
name|clientConfiguration
argument_list|,
name|endpoint
argument_list|)
expr_stmt|;
block|}
name|clientConfiguration
operator|.
name|setSocketTimeout
argument_list|(
operator|(
name|int
operator|)
name|CLOUD_S3
operator|.
name|READ_TIMEOUT
operator|.
name|get
argument_list|(
name|settings
argument_list|)
operator|.
name|millis
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|clientConfiguration
return|;
block|}
DECL|method|buildCredentials
specifier|public
specifier|static
name|AWSCredentialsProvider
name|buildCredentials
parameter_list|(
name|Logger
name|logger
parameter_list|,
name|Settings
name|settings
parameter_list|,
name|Settings
name|repositorySettings
parameter_list|)
block|{
name|AWSCredentialsProvider
name|credentials
decl_stmt|;
name|String
name|key
init|=
name|getValue
argument_list|(
name|repositorySettings
argument_list|,
name|settings
argument_list|,
name|S3Repository
operator|.
name|Repository
operator|.
name|KEY_SETTING
argument_list|,
name|S3Repository
operator|.
name|Repositories
operator|.
name|KEY_SETTING
argument_list|)
decl_stmt|;
name|String
name|secret
init|=
name|getValue
argument_list|(
name|repositorySettings
argument_list|,
name|settings
argument_list|,
name|S3Repository
operator|.
name|Repository
operator|.
name|SECRET_SETTING
argument_list|,
name|S3Repository
operator|.
name|Repositories
operator|.
name|SECRET_SETTING
argument_list|)
decl_stmt|;
if|if
condition|(
name|key
operator|.
name|isEmpty
argument_list|()
operator|&&
name|secret
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Using either environment variables, system properties or instance profile credentials"
argument_list|)
expr_stmt|;
name|credentials
operator|=
operator|new
name|DefaultAWSCredentialsProviderChain
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Using basic key/secret credentials"
argument_list|)
expr_stmt|;
name|credentials
operator|=
operator|new
name|StaticCredentialsProvider
argument_list|(
operator|new
name|BasicAWSCredentials
argument_list|(
name|key
argument_list|,
name|secret
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|credentials
return|;
block|}
DECL|method|findEndpoint
specifier|protected
specifier|static
name|String
name|findEndpoint
parameter_list|(
name|Logger
name|logger
parameter_list|,
name|Settings
name|settings
parameter_list|,
name|String
name|endpoint
parameter_list|,
name|String
name|region
parameter_list|)
block|{
if|if
condition|(
name|Strings
operator|.
name|isNullOrEmpty
argument_list|(
name|endpoint
argument_list|)
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"no repository level endpoint has been defined. Trying to guess from repository region [{}]"
argument_list|,
name|region
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|region
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|endpoint
operator|=
name|getEndpoint
argument_list|(
name|region
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"using s3 region [{}], with endpoint [{}]"
argument_list|,
name|region
argument_list|,
name|endpoint
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// No region has been set so we will use the default endpoint
if|if
condition|(
name|CLOUD_S3
operator|.
name|ENDPOINT_SETTING
operator|.
name|exists
argument_list|(
name|settings
argument_list|)
condition|)
block|{
name|endpoint
operator|=
name|CLOUD_S3
operator|.
name|ENDPOINT_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"using explicit s3 endpoint [{}]"
argument_list|,
name|endpoint
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|REGION_SETTING
operator|.
name|exists
argument_list|(
name|settings
argument_list|)
operator|||
name|CLOUD_S3
operator|.
name|REGION_SETTING
operator|.
name|exists
argument_list|(
name|settings
argument_list|)
condition|)
block|{
name|region
operator|=
name|CLOUD_S3
operator|.
name|REGION_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|endpoint
operator|=
name|getEndpoint
argument_list|(
name|region
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"using s3 region [{}], with endpoint [{}]"
argument_list|,
name|region
argument_list|,
name|endpoint
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"using repository level endpoint [{}]"
argument_list|,
name|endpoint
argument_list|)
expr_stmt|;
block|}
return|return
name|endpoint
return|;
block|}
DECL|method|getEndpoint
specifier|private
specifier|static
name|String
name|getEndpoint
parameter_list|(
name|String
name|region
parameter_list|)
block|{
specifier|final
name|String
name|endpoint
decl_stmt|;
switch|switch
condition|(
name|region
condition|)
block|{
case|case
literal|"us-east"
case|:
case|case
literal|"us-east-1"
case|:
name|endpoint
operator|=
literal|"s3.amazonaws.com"
expr_stmt|;
break|break;
case|case
literal|"us-east-2"
case|:
name|endpoint
operator|=
literal|"s3.us-east-2.amazonaws.com"
expr_stmt|;
break|break;
case|case
literal|"us-west"
case|:
case|case
literal|"us-west-1"
case|:
name|endpoint
operator|=
literal|"s3-us-west-1.amazonaws.com"
expr_stmt|;
break|break;
case|case
literal|"us-west-2"
case|:
name|endpoint
operator|=
literal|"s3-us-west-2.amazonaws.com"
expr_stmt|;
break|break;
case|case
literal|"ap-south"
case|:
case|case
literal|"ap-south-1"
case|:
name|endpoint
operator|=
literal|"s3-ap-south-1.amazonaws.com"
expr_stmt|;
break|break;
case|case
literal|"ap-southeast"
case|:
case|case
literal|"ap-southeast-1"
case|:
name|endpoint
operator|=
literal|"s3-ap-southeast-1.amazonaws.com"
expr_stmt|;
break|break;
case|case
literal|"ap-southeast-2"
case|:
name|endpoint
operator|=
literal|"s3-ap-southeast-2.amazonaws.com"
expr_stmt|;
break|break;
case|case
literal|"ap-northeast"
case|:
case|case
literal|"ap-northeast-1"
case|:
name|endpoint
operator|=
literal|"s3-ap-northeast-1.amazonaws.com"
expr_stmt|;
break|break;
case|case
literal|"ap-northeast-2"
case|:
name|endpoint
operator|=
literal|"s3-ap-northeast-2.amazonaws.com"
expr_stmt|;
break|break;
case|case
literal|"eu-west"
case|:
case|case
literal|"eu-west-1"
case|:
name|endpoint
operator|=
literal|"s3-eu-west-1.amazonaws.com"
expr_stmt|;
break|break;
case|case
literal|"eu-west-2"
case|:
name|endpoint
operator|=
literal|"s3-eu-west-2.amazonaws.com"
expr_stmt|;
break|break;
case|case
literal|"eu-central"
case|:
case|case
literal|"eu-central-1"
case|:
name|endpoint
operator|=
literal|"s3.eu-central-1.amazonaws.com"
expr_stmt|;
break|break;
case|case
literal|"sa-east"
case|:
case|case
literal|"sa-east-1"
case|:
name|endpoint
operator|=
literal|"s3-sa-east-1.amazonaws.com"
expr_stmt|;
break|break;
case|case
literal|"cn-north"
case|:
case|case
literal|"cn-north-1"
case|:
name|endpoint
operator|=
literal|"s3.cn-north-1.amazonaws.com.cn"
expr_stmt|;
break|break;
case|case
literal|"us-gov-west"
case|:
case|case
literal|"us-gov-west-1"
case|:
name|endpoint
operator|=
literal|"s3-us-gov-west-1.amazonaws.com"
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No automatic endpoint could be derived from region ["
operator|+
name|region
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
name|endpoint
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
name|ElasticsearchException
block|{     }
annotation|@
name|Override
DECL|method|doStop
specifier|protected
name|void
name|doStop
parameter_list|()
throws|throws
name|ElasticsearchException
block|{     }
annotation|@
name|Override
DECL|method|doClose
specifier|protected
name|void
name|doClose
parameter_list|()
throws|throws
name|ElasticsearchException
block|{
for|for
control|(
name|AmazonS3Client
name|client
range|:
name|clients
operator|.
name|values
argument_list|()
control|)
block|{
name|client
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
comment|// Ensure that IdleConnectionReaper is shutdown
name|IdleConnectionReaper
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

