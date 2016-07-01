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
name|Protocol
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
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|component
operator|.
name|LifecycleComponent
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
name|Setting
operator|.
name|Property
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
name|function
operator|.
name|Function
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_interface
DECL|interface|AwsS3Service
specifier|public
interface|interface
name|AwsS3Service
extends|extends
name|LifecycleComponent
block|{
comment|// Global AWS settings (shared between discovery-ec2 and repository-s3)
comment|// Each setting starting with `cloud.aws` also exists in discovery-ec2 project. Don't forget to update
comment|// the code there if you change anything here.
comment|/**      * cloud.aws.access_key: AWS Access key. Shared with discovery-ec2 plugin      */
DECL|field|KEY_SETTING
name|Setting
argument_list|<
name|String
argument_list|>
name|KEY_SETTING
init|=
name|Setting
operator|.
name|simpleString
argument_list|(
literal|"cloud.aws.access_key"
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|,
name|Property
operator|.
name|Filtered
argument_list|,
name|Property
operator|.
name|Shared
argument_list|)
decl_stmt|;
comment|/**      * cloud.aws.secret_key: AWS Secret key. Shared with discovery-ec2 plugin      */
DECL|field|SECRET_SETTING
name|Setting
argument_list|<
name|String
argument_list|>
name|SECRET_SETTING
init|=
name|Setting
operator|.
name|simpleString
argument_list|(
literal|"cloud.aws.secret_key"
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|,
name|Property
operator|.
name|Filtered
argument_list|,
name|Property
operator|.
name|Shared
argument_list|)
decl_stmt|;
comment|/**      * cloud.aws.protocol: Protocol for AWS API: http or https. Defaults to https. Shared with discovery-ec2 plugin      */
DECL|field|PROTOCOL_SETTING
name|Setting
argument_list|<
name|Protocol
argument_list|>
name|PROTOCOL_SETTING
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"cloud.aws.protocol"
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
name|Property
operator|.
name|NodeScope
argument_list|,
name|Property
operator|.
name|Shared
argument_list|)
decl_stmt|;
comment|/**      * cloud.aws.proxy.host: In case of proxy, define its hostname/IP. Shared with discovery-ec2 plugin      */
DECL|field|PROXY_HOST_SETTING
name|Setting
argument_list|<
name|String
argument_list|>
name|PROXY_HOST_SETTING
init|=
name|Setting
operator|.
name|simpleString
argument_list|(
literal|"cloud.aws.proxy.host"
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|,
name|Property
operator|.
name|Shared
argument_list|)
decl_stmt|;
comment|/**      * cloud.aws.proxy.port: In case of proxy, define its port. Defaults to 80. Shared with discovery-ec2 plugin      */
DECL|field|PROXY_PORT_SETTING
name|Setting
argument_list|<
name|Integer
argument_list|>
name|PROXY_PORT_SETTING
init|=
name|Setting
operator|.
name|intSetting
argument_list|(
literal|"cloud.aws.proxy.port"
argument_list|,
literal|80
argument_list|,
literal|0
argument_list|,
literal|1
operator|<<
literal|16
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|,
name|Property
operator|.
name|Shared
argument_list|)
decl_stmt|;
comment|/**      * cloud.aws.proxy.username: In case of proxy with auth, define the username. Shared with discovery-ec2 plugin      */
DECL|field|PROXY_USERNAME_SETTING
name|Setting
argument_list|<
name|String
argument_list|>
name|PROXY_USERNAME_SETTING
init|=
name|Setting
operator|.
name|simpleString
argument_list|(
literal|"cloud.aws.proxy.username"
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|,
name|Property
operator|.
name|Shared
argument_list|)
decl_stmt|;
comment|/**      * cloud.aws.proxy.password: In case of proxy with auth, define the password. Shared with discovery-ec2 plugin      */
DECL|field|PROXY_PASSWORD_SETTING
name|Setting
argument_list|<
name|String
argument_list|>
name|PROXY_PASSWORD_SETTING
init|=
name|Setting
operator|.
name|simpleString
argument_list|(
literal|"cloud.aws.proxy.password"
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|,
name|Property
operator|.
name|Filtered
argument_list|,
name|Property
operator|.
name|Shared
argument_list|)
decl_stmt|;
comment|/**      * cloud.aws.signer: If you are using an old AWS API version, you can define a Signer. Shared with discovery-ec2 plugin      */
DECL|field|SIGNER_SETTING
name|Setting
argument_list|<
name|String
argument_list|>
name|SIGNER_SETTING
init|=
name|Setting
operator|.
name|simpleString
argument_list|(
literal|"cloud.aws.signer"
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|,
name|Property
operator|.
name|Shared
argument_list|)
decl_stmt|;
comment|/**      * cloud.aws.region: Region. Shared with discovery-ec2 plugin      */
DECL|field|REGION_SETTING
name|Setting
argument_list|<
name|String
argument_list|>
name|REGION_SETTING
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"cloud.aws.region"
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
name|Property
operator|.
name|NodeScope
argument_list|,
name|Property
operator|.
name|Shared
argument_list|)
decl_stmt|;
comment|/**      * Defines specific s3 settings starting with cloud.aws.s3.      */
DECL|interface|CLOUD_S3
interface|interface
name|CLOUD_S3
block|{
comment|/**          * cloud.aws.s3.access_key: AWS Access key specific for S3 API calls. Defaults to cloud.aws.access_key.          * @see AwsS3Service#KEY_SETTING          */
DECL|field|KEY_SETTING
name|Setting
argument_list|<
name|String
argument_list|>
name|KEY_SETTING
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"cloud.aws.s3.access_key"
argument_list|,
name|AwsS3Service
operator|.
name|KEY_SETTING
argument_list|,
name|Function
operator|.
name|identity
argument_list|()
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|,
name|Property
operator|.
name|Filtered
argument_list|)
decl_stmt|;
comment|/**          * cloud.aws.s3.secret_key: AWS Secret key specific for S3 API calls. Defaults to cloud.aws.secret_key.          * @see AwsS3Service#SECRET_SETTING          */
DECL|field|SECRET_SETTING
name|Setting
argument_list|<
name|String
argument_list|>
name|SECRET_SETTING
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"cloud.aws.s3.secret_key"
argument_list|,
name|AwsS3Service
operator|.
name|SECRET_SETTING
argument_list|,
name|Function
operator|.
name|identity
argument_list|()
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|,
name|Property
operator|.
name|Filtered
argument_list|)
decl_stmt|;
comment|/**          * cloud.aws.s3.protocol: Protocol for AWS API specific for S3 API calls: http or https. Defaults to cloud.aws.protocol.          * @see AwsS3Service#PROTOCOL_SETTING          */
DECL|field|PROTOCOL_SETTING
name|Setting
argument_list|<
name|Protocol
argument_list|>
name|PROTOCOL_SETTING
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"cloud.aws.s3.protocol"
argument_list|,
name|AwsS3Service
operator|.
name|PROTOCOL_SETTING
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
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
comment|/**          * cloud.aws.s3.proxy.host: In case of proxy, define its hostname/IP specific for S3 API calls. Defaults to cloud.aws.proxy.host.          * @see AwsS3Service#PROXY_HOST_SETTING          */
DECL|field|PROXY_HOST_SETTING
name|Setting
argument_list|<
name|String
argument_list|>
name|PROXY_HOST_SETTING
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"cloud.aws.s3.proxy.host"
argument_list|,
name|AwsS3Service
operator|.
name|PROXY_HOST_SETTING
argument_list|,
name|Function
operator|.
name|identity
argument_list|()
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
comment|/**          * cloud.aws.s3.proxy.port: In case of proxy, define its port specific for S3 API calls.  Defaults to cloud.aws.proxy.port.          * @see AwsS3Service#PROXY_PORT_SETTING          */
DECL|field|PROXY_PORT_SETTING
name|Setting
argument_list|<
name|Integer
argument_list|>
name|PROXY_PORT_SETTING
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"cloud.aws.s3.proxy.port"
argument_list|,
name|AwsS3Service
operator|.
name|PROXY_PORT_SETTING
argument_list|,
name|s
lambda|->
name|Setting
operator|.
name|parseInt
argument_list|(
name|s
argument_list|,
literal|0
argument_list|,
literal|1
operator|<<
literal|16
argument_list|,
literal|"cloud.aws.s3.proxy.port"
argument_list|)
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
comment|/**          * cloud.aws.s3.proxy.username: In case of proxy with auth, define the username specific for S3 API calls.          * Defaults to cloud.aws.proxy.username.          * @see AwsS3Service#PROXY_USERNAME_SETTING          */
DECL|field|PROXY_USERNAME_SETTING
name|Setting
argument_list|<
name|String
argument_list|>
name|PROXY_USERNAME_SETTING
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"cloud.aws.s3.proxy.username"
argument_list|,
name|AwsS3Service
operator|.
name|PROXY_USERNAME_SETTING
argument_list|,
name|Function
operator|.
name|identity
argument_list|()
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
comment|/**          * cloud.aws.s3.proxy.password: In case of proxy with auth, define the password specific for S3 API calls.          * Defaults to cloud.aws.proxy.password.          * @see AwsS3Service#PROXY_PASSWORD_SETTING          */
DECL|field|PROXY_PASSWORD_SETTING
name|Setting
argument_list|<
name|String
argument_list|>
name|PROXY_PASSWORD_SETTING
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"cloud.aws.s3.proxy.password"
argument_list|,
name|AwsS3Service
operator|.
name|PROXY_PASSWORD_SETTING
argument_list|,
name|Function
operator|.
name|identity
argument_list|()
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|,
name|Property
operator|.
name|Filtered
argument_list|)
decl_stmt|;
comment|/**          * cloud.aws.s3.signer: If you are using an old AWS API version, you can define a Signer. Specific for S3 API calls.          * Defaults to cloud.aws.signer.          * @see AwsS3Service#SIGNER_SETTING          */
DECL|field|SIGNER_SETTING
name|Setting
argument_list|<
name|String
argument_list|>
name|SIGNER_SETTING
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"cloud.aws.s3.signer"
argument_list|,
name|AwsS3Service
operator|.
name|SIGNER_SETTING
argument_list|,
name|Function
operator|.
name|identity
argument_list|()
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
comment|/**          * cloud.aws.s3.region: Region specific for S3 API calls. Defaults to cloud.aws.region.          * @see AwsS3Service#REGION_SETTING          */
DECL|field|REGION_SETTING
name|Setting
argument_list|<
name|String
argument_list|>
name|REGION_SETTING
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"cloud.aws.s3.region"
argument_list|,
name|AwsS3Service
operator|.
name|REGION_SETTING
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
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
comment|/**          * cloud.aws.s3.endpoint: Endpoint. If not set, endpoint will be guessed based on region setting.          */
DECL|field|ENDPOINT_SETTING
name|Setting
argument_list|<
name|String
argument_list|>
name|ENDPOINT_SETTING
init|=
name|Setting
operator|.
name|simpleString
argument_list|(
literal|"cloud.aws.s3.endpoint"
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
block|}
DECL|method|client
name|AmazonS3
name|client
parameter_list|(
name|String
name|endpoint
parameter_list|,
name|Protocol
name|protocol
parameter_list|,
name|String
name|region
parameter_list|,
name|String
name|account
parameter_list|,
name|String
name|key
parameter_list|,
name|Integer
name|maxRetries
parameter_list|,
name|boolean
name|useThrottleRetries
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

