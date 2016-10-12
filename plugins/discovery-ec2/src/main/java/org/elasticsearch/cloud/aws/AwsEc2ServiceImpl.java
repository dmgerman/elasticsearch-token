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
name|AmazonClientException
import|;
end_import

begin_import
import|import
name|com
operator|.
name|amazonaws
operator|.
name|AmazonWebServiceRequest
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
name|retry
operator|.
name|RetryPolicy
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
name|ec2
operator|.
name|AmazonEC2
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
name|ec2
operator|.
name|AmazonEC2Client
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
name|Randomness
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
name|java
operator|.
name|util
operator|.
name|Random
import|;
end_import

begin_class
DECL|class|AwsEc2ServiceImpl
specifier|public
class|class
name|AwsEc2ServiceImpl
extends|extends
name|AbstractLifecycleComponent
implements|implements
name|AwsEc2Service
block|{
DECL|field|EC2_METADATA_URL
specifier|public
specifier|static
specifier|final
name|String
name|EC2_METADATA_URL
init|=
literal|"http://169.254.169.254/latest/meta-data/"
decl_stmt|;
DECL|field|client
specifier|private
name|AmazonEC2Client
name|client
decl_stmt|;
annotation|@
name|Inject
DECL|method|AwsEc2ServiceImpl
specifier|public
name|AwsEc2ServiceImpl
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
name|AmazonEC2
name|client
parameter_list|()
block|{
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
name|this
operator|.
name|client
operator|=
operator|new
name|AmazonEC2Client
argument_list|(
name|buildCredentials
argument_list|(
name|logger
argument_list|,
name|settings
argument_list|)
argument_list|,
name|buildConfiguration
argument_list|(
name|logger
argument_list|,
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|endpoint
init|=
name|findEndpoint
argument_list|(
name|logger
argument_list|,
name|settings
argument_list|)
decl_stmt|;
if|if
condition|(
name|endpoint
operator|!=
literal|null
condition|)
block|{
name|client
operator|.
name|setEndpoint
argument_list|(
name|endpoint
argument_list|)
expr_stmt|;
block|}
return|return
name|this
operator|.
name|client
return|;
block|}
DECL|method|buildCredentials
specifier|protected
specifier|static
name|AWSCredentialsProvider
name|buildCredentials
parameter_list|(
name|Logger
name|logger
parameter_list|,
name|Settings
name|settings
parameter_list|)
block|{
name|AWSCredentialsProvider
name|credentials
decl_stmt|;
name|String
name|key
init|=
name|CLOUD_EC2
operator|.
name|KEY_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|String
name|secret
init|=
name|CLOUD_EC2
operator|.
name|SECRET_SETTING
operator|.
name|get
argument_list|(
name|settings
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
DECL|method|buildConfiguration
specifier|protected
specifier|static
name|ClientConfiguration
name|buildConfiguration
parameter_list|(
name|Logger
name|logger
parameter_list|,
name|Settings
name|settings
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
name|CLOUD_EC2
operator|.
name|PROTOCOL_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|PROXY_HOST_SETTING
operator|.
name|exists
argument_list|(
name|settings
argument_list|)
operator|||
name|CLOUD_EC2
operator|.
name|PROXY_HOST_SETTING
operator|.
name|exists
argument_list|(
name|settings
argument_list|)
condition|)
block|{
name|String
name|proxyHost
init|=
name|CLOUD_EC2
operator|.
name|PROXY_HOST_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|Integer
name|proxyPort
init|=
name|CLOUD_EC2
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
name|CLOUD_EC2
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
name|CLOUD_EC2
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
comment|// #155: we might have 3rd party users using older EC2 API version
name|String
name|awsSigner
init|=
name|CLOUD_EC2
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
argument_list|)
expr_stmt|;
block|}
comment|// Increase the number of retries in case of 5xx API responses
specifier|final
name|Random
name|rand
init|=
name|Randomness
operator|.
name|get
argument_list|()
decl_stmt|;
name|RetryPolicy
name|retryPolicy
init|=
operator|new
name|RetryPolicy
argument_list|(
name|RetryPolicy
operator|.
name|RetryCondition
operator|.
name|NO_RETRY_CONDITION
argument_list|,
operator|new
name|RetryPolicy
operator|.
name|BackoffStrategy
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|long
name|delayBeforeNextRetry
parameter_list|(
name|AmazonWebServiceRequest
name|originalRequest
parameter_list|,
name|AmazonClientException
name|exception
parameter_list|,
name|int
name|retriesAttempted
parameter_list|)
block|{
comment|// with 10 retries the max delay time is 320s/320000ms (10 * 2^5 * 1 * 1000)
name|logger
operator|.
name|warn
argument_list|(
literal|"EC2 API request failed, retry again. Reason was:"
argument_list|,
name|exception
argument_list|)
expr_stmt|;
return|return
literal|1000L
operator|*
call|(
name|long
call|)
argument_list|(
literal|10d
operator|*
name|Math
operator|.
name|pow
argument_list|(
literal|2
argument_list|,
name|retriesAttempted
operator|/
literal|2.0d
argument_list|)
operator|*
operator|(
literal|1.0d
operator|+
name|rand
operator|.
name|nextDouble
argument_list|()
operator|)
argument_list|)
return|;
block|}
block|}
argument_list|,
literal|10
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|clientConfiguration
operator|.
name|setRetryPolicy
argument_list|(
name|retryPolicy
argument_list|)
expr_stmt|;
return|return
name|clientConfiguration
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
parameter_list|)
block|{
name|String
name|endpoint
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|CLOUD_EC2
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
name|CLOUD_EC2
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
literal|"using explicit ec2 endpoint [{}]"
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
name|CLOUD_EC2
operator|.
name|REGION_SETTING
operator|.
name|exists
argument_list|(
name|settings
argument_list|)
condition|)
block|{
specifier|final
name|String
name|region
init|=
name|CLOUD_EC2
operator|.
name|REGION_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|region
condition|)
block|{
case|case
literal|"us-east-1"
case|:
case|case
literal|"us-east"
case|:
name|endpoint
operator|=
literal|"ec2.us-east-1.amazonaws.com"
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
literal|"ec2.us-west-1.amazonaws.com"
expr_stmt|;
break|break;
case|case
literal|"us-west-2"
case|:
name|endpoint
operator|=
literal|"ec2.us-west-2.amazonaws.com"
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
literal|"ec2.ap-southeast-1.amazonaws.com"
expr_stmt|;
break|break;
case|case
literal|"ap-south-1"
case|:
name|endpoint
operator|=
literal|"ec2.ap-south-1.amazonaws.com"
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
literal|"ec2.us-gov-west-1.amazonaws.com"
expr_stmt|;
break|break;
case|case
literal|"ap-southeast-2"
case|:
name|endpoint
operator|=
literal|"ec2.ap-southeast-2.amazonaws.com"
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
literal|"ec2.ap-northeast-1.amazonaws.com"
expr_stmt|;
break|break;
case|case
literal|"ap-northeast-2"
case|:
name|endpoint
operator|=
literal|"ec2.ap-northeast-2.amazonaws.com"
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
literal|"ec2.eu-west-1.amazonaws.com"
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
literal|"ec2.eu-central-1.amazonaws.com"
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
literal|"ec2.sa-east-1.amazonaws.com"
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
literal|"ec2.cn-north-1.amazonaws.com.cn"
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
name|logger
operator|.
name|debug
argument_list|(
literal|"using ec2 region [{}], with endpoint [{}]"
argument_list|,
name|region
argument_list|,
name|endpoint
argument_list|)
expr_stmt|;
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
if|if
condition|(
name|client
operator|!=
literal|null
condition|)
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

