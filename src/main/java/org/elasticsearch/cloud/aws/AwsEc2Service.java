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
name|*
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
name|ElasticsearchIllegalArgumentException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cloud
operator|.
name|aws
operator|.
name|network
operator|.
name|Ec2NameResolver
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cloud
operator|.
name|aws
operator|.
name|node
operator|.
name|Ec2CustomNodeAttributes
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|node
operator|.
name|DiscoveryNodeService
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
name|settings
operator|.
name|SettingsFilter
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|AwsEc2Service
specifier|public
class|class
name|AwsEc2Service
extends|extends
name|AbstractLifecycleComponent
argument_list|<
name|AwsEc2Service
argument_list|>
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
DECL|method|AwsEc2Service
specifier|public
name|AwsEc2Service
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|SettingsFilter
name|settingsFilter
parameter_list|,
name|NetworkService
name|networkService
parameter_list|,
name|DiscoveryNodeService
name|discoveryNodeService
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
name|AwsSettingsFilter
argument_list|()
argument_list|)
expr_stmt|;
comment|// add specific ec2 name resolver
name|networkService
operator|.
name|addCustomNameResolver
argument_list|(
operator|new
name|Ec2NameResolver
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|discoveryNodeService
operator|.
name|addCustomAttributeProvider
argument_list|(
operator|new
name|Ec2CustomNodeAttributes
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|ClientConfiguration
name|clientConfiguration
init|=
operator|new
name|ClientConfiguration
argument_list|()
decl_stmt|;
name|String
name|protocol
init|=
name|componentSettings
operator|.
name|get
argument_list|(
literal|"protocol"
argument_list|,
literal|"https"
argument_list|)
operator|.
name|toLowerCase
argument_list|()
decl_stmt|;
name|protocol
operator|=
name|componentSettings
operator|.
name|get
argument_list|(
literal|"ec2.protocol"
argument_list|,
name|protocol
argument_list|)
operator|.
name|toLowerCase
argument_list|()
expr_stmt|;
if|if
condition|(
literal|"http"
operator|.
name|equals
argument_list|(
name|protocol
argument_list|)
condition|)
block|{
name|clientConfiguration
operator|.
name|setProtocol
argument_list|(
name|Protocol
operator|.
name|HTTP
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"https"
operator|.
name|equals
argument_list|(
name|protocol
argument_list|)
condition|)
block|{
name|clientConfiguration
operator|.
name|setProtocol
argument_list|(
name|Protocol
operator|.
name|HTTPS
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"No protocol supported ["
operator|+
name|protocol
operator|+
literal|"], can either be [http] or [https]"
argument_list|)
throw|;
block|}
name|String
name|account
init|=
name|componentSettings
operator|.
name|get
argument_list|(
literal|"access_key"
argument_list|,
name|settings
operator|.
name|get
argument_list|(
literal|"cloud.account"
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|key
init|=
name|componentSettings
operator|.
name|get
argument_list|(
literal|"secret_key"
argument_list|,
name|settings
operator|.
name|get
argument_list|(
literal|"cloud.key"
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|proxyHost
init|=
name|componentSettings
operator|.
name|get
argument_list|(
literal|"proxy_host"
argument_list|)
decl_stmt|;
if|if
condition|(
name|proxyHost
operator|!=
literal|null
condition|)
block|{
name|String
name|portString
init|=
name|componentSettings
operator|.
name|get
argument_list|(
literal|"proxy_port"
argument_list|,
literal|"80"
argument_list|)
decl_stmt|;
name|Integer
name|proxyPort
decl_stmt|;
try|try
block|{
name|proxyPort
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|portString
argument_list|,
literal|10
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"The configured proxy port value ["
operator|+
name|portString
operator|+
literal|"] is invalid"
argument_list|,
name|ex
argument_list|)
throw|;
block|}
name|clientConfiguration
operator|.
name|withProxyHost
argument_list|(
name|proxyHost
argument_list|)
operator|.
name|setProxyPort
argument_list|(
name|proxyPort
argument_list|)
expr_stmt|;
block|}
name|AWSCredentialsProvider
name|credentials
decl_stmt|;
if|if
condition|(
name|account
operator|==
literal|null
operator|&&
name|key
operator|==
literal|null
condition|)
block|{
name|credentials
operator|=
operator|new
name|AWSCredentialsProviderChain
argument_list|(
operator|new
name|EnvironmentVariableCredentialsProvider
argument_list|()
argument_list|,
operator|new
name|SystemPropertiesCredentialsProvider
argument_list|()
argument_list|,
operator|new
name|InstanceProfileCredentialsProvider
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|credentials
operator|=
operator|new
name|AWSCredentialsProviderChain
argument_list|(
operator|new
name|StaticCredentialsProvider
argument_list|(
operator|new
name|BasicAWSCredentials
argument_list|(
name|account
argument_list|,
name|key
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|client
operator|=
operator|new
name|AmazonEC2Client
argument_list|(
name|credentials
argument_list|,
name|clientConfiguration
argument_list|)
expr_stmt|;
if|if
condition|(
name|componentSettings
operator|.
name|get
argument_list|(
literal|"ec2.endpoint"
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|String
name|endpoint
init|=
name|componentSettings
operator|.
name|get
argument_list|(
literal|"ec2.endpoint"
argument_list|)
decl_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"using explicit ec2 endpoint [{}]"
argument_list|,
name|endpoint
argument_list|)
expr_stmt|;
name|client
operator|.
name|setEndpoint
argument_list|(
name|endpoint
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|componentSettings
operator|.
name|get
argument_list|(
literal|"region"
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|String
name|region
init|=
name|componentSettings
operator|.
name|get
argument_list|(
literal|"region"
argument_list|)
operator|.
name|toLowerCase
argument_list|()
decl_stmt|;
name|String
name|endpoint
decl_stmt|;
if|if
condition|(
name|region
operator|.
name|equals
argument_list|(
literal|"us-east-1"
argument_list|)
operator|||
name|region
operator|.
name|equals
argument_list|(
literal|"us-east"
argument_list|)
condition|)
block|{
name|endpoint
operator|=
literal|"ec2.us-east-1.amazonaws.com"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|region
operator|.
name|equals
argument_list|(
literal|"us-west"
argument_list|)
operator|||
name|region
operator|.
name|equals
argument_list|(
literal|"us-west-1"
argument_list|)
condition|)
block|{
name|endpoint
operator|=
literal|"ec2.us-west-1.amazonaws.com"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|region
operator|.
name|equals
argument_list|(
literal|"us-west-2"
argument_list|)
condition|)
block|{
name|endpoint
operator|=
literal|"ec2.us-west-2.amazonaws.com"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|region
operator|.
name|equals
argument_list|(
literal|"ap-southeast"
argument_list|)
operator|||
name|region
operator|.
name|equals
argument_list|(
literal|"ap-southeast-1"
argument_list|)
condition|)
block|{
name|endpoint
operator|=
literal|"ec2.ap-southeast-1.amazonaws.com"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|region
operator|.
name|equals
argument_list|(
literal|"ap-southeast-2"
argument_list|)
condition|)
block|{
name|endpoint
operator|=
literal|"ec2.ap-southeast-2.amazonaws.com"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|region
operator|.
name|equals
argument_list|(
literal|"ap-northeast"
argument_list|)
operator|||
name|region
operator|.
name|equals
argument_list|(
literal|"ap-northeast-1"
argument_list|)
condition|)
block|{
name|endpoint
operator|=
literal|"ec2.ap-northeast-1.amazonaws.com"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|region
operator|.
name|equals
argument_list|(
literal|"eu-west"
argument_list|)
operator|||
name|region
operator|.
name|equals
argument_list|(
literal|"eu-west-1"
argument_list|)
condition|)
block|{
name|endpoint
operator|=
literal|"ec2.eu-west-1.amazonaws.com"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|region
operator|.
name|equals
argument_list|(
literal|"eu-central"
argument_list|)
operator|||
name|region
operator|.
name|equals
argument_list|(
literal|"eu-central-1"
argument_list|)
condition|)
block|{
name|endpoint
operator|=
literal|"ec2.eu-central-1.amazonaws.com"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|region
operator|.
name|equals
argument_list|(
literal|"sa-east"
argument_list|)
operator|||
name|region
operator|.
name|equals
argument_list|(
literal|"sa-east-1"
argument_list|)
condition|)
block|{
name|endpoint
operator|=
literal|"ec2.sa-east-1.amazonaws.com"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|region
operator|.
name|equals
argument_list|(
literal|"cn-north"
argument_list|)
operator|||
name|region
operator|.
name|equals
argument_list|(
literal|"cn-north-1"
argument_list|)
condition|)
block|{
name|endpoint
operator|=
literal|"ec2.cn-north-1.amazonaws.com.cn"
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"No automatic endpoint could be derived from region ["
operator|+
name|region
operator|+
literal|"]"
argument_list|)
throw|;
block|}
if|if
condition|(
name|endpoint
operator|!=
literal|null
condition|)
block|{
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
name|client
operator|.
name|setEndpoint
argument_list|(
name|endpoint
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|this
operator|.
name|client
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
block|}
block|}
end_class

end_unit

