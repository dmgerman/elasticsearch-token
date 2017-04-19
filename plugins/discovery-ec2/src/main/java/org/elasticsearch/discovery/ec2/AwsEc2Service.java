begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.discovery.ec2
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|ec2
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
name|services
operator|.
name|ec2
operator|.
name|AmazonEC2
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
name|Setting
operator|.
name|Property
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

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
name|List
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

begin_interface
DECL|interface|AwsEc2Service
interface|interface
name|AwsEc2Service
block|{
DECL|field|AUTO_ATTRIBUTE_SETTING
name|Setting
argument_list|<
name|Boolean
argument_list|>
name|AUTO_ATTRIBUTE_SETTING
init|=
name|Setting
operator|.
name|boolSetting
argument_list|(
literal|"cloud.node.auto_attributes"
argument_list|,
literal|false
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|class|HostType
class|class
name|HostType
block|{
DECL|field|PRIVATE_IP
specifier|public
specifier|static
specifier|final
name|String
name|PRIVATE_IP
init|=
literal|"private_ip"
decl_stmt|;
DECL|field|PUBLIC_IP
specifier|public
specifier|static
specifier|final
name|String
name|PUBLIC_IP
init|=
literal|"public_ip"
decl_stmt|;
DECL|field|PRIVATE_DNS
specifier|public
specifier|static
specifier|final
name|String
name|PRIVATE_DNS
init|=
literal|"private_dns"
decl_stmt|;
DECL|field|PUBLIC_DNS
specifier|public
specifier|static
specifier|final
name|String
name|PUBLIC_DNS
init|=
literal|"public_dns"
decl_stmt|;
DECL|field|TAG_PREFIX
specifier|public
specifier|static
specifier|final
name|String
name|TAG_PREFIX
init|=
literal|"tag:"
decl_stmt|;
block|}
comment|/** The access key (ie login id) for connecting to ec2. */
DECL|field|ACCESS_KEY_SETTING
name|Setting
argument_list|<
name|SecureString
argument_list|>
name|ACCESS_KEY_SETTING
init|=
name|SecureSetting
operator|.
name|secureString
argument_list|(
literal|"discovery.ec2.access_key"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|/** The secret key (ie password) for connecting to ec2. */
DECL|field|SECRET_KEY_SETTING
name|Setting
argument_list|<
name|SecureString
argument_list|>
name|SECRET_KEY_SETTING
init|=
name|SecureSetting
operator|.
name|secureString
argument_list|(
literal|"discovery.ec2.secret_key"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|/** An override for the ec2 endpoint to connect to. */
DECL|field|ENDPOINT_SETTING
name|Setting
argument_list|<
name|String
argument_list|>
name|ENDPOINT_SETTING
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"discovery.ec2.endpoint"
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
argument_list|)
decl_stmt|;
comment|/** The protocol to use to connect to to ec2. */
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
literal|"discovery.ec2.protocol"
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
argument_list|)
decl_stmt|;
comment|/** The host name of a proxy to connect to ec2 through. */
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
literal|"discovery.ec2.proxy.host"
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
comment|/** The port of a proxy to connect to ec2 through. */
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
literal|"discovery.ec2.proxy.port"
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
argument_list|)
decl_stmt|;
comment|/** The username of a proxy to connect to s3 through. */
DECL|field|PROXY_USERNAME_SETTING
name|Setting
argument_list|<
name|SecureString
argument_list|>
name|PROXY_USERNAME_SETTING
init|=
name|SecureSetting
operator|.
name|secureString
argument_list|(
literal|"discovery.ec2.proxy.username"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|/** The password of a proxy to connect to s3 through. */
DECL|field|PROXY_PASSWORD_SETTING
name|Setting
argument_list|<
name|SecureString
argument_list|>
name|PROXY_PASSWORD_SETTING
init|=
name|SecureSetting
operator|.
name|secureString
argument_list|(
literal|"discovery.ec2.proxy.password"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|/** The socket timeout for connecting to s3. */
DECL|field|READ_TIMEOUT_SETTING
name|Setting
argument_list|<
name|TimeValue
argument_list|>
name|READ_TIMEOUT_SETTING
init|=
name|Setting
operator|.
name|timeSetting
argument_list|(
literal|"discovery.ec2.read_timeout"
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
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
comment|/**      * discovery.ec2.host_type: The type of host type to use to communicate with other instances.      * Can be one of private_ip, public_ip, private_dns, public_dns or tag:XXXX where      * XXXX refers to a name of a tag configured for all EC2 instances. Instances which don't      * have this tag set will be ignored by the discovery process. Defaults to private_ip.      */
DECL|field|HOST_TYPE_SETTING
name|Setting
argument_list|<
name|String
argument_list|>
name|HOST_TYPE_SETTING
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"discovery.ec2.host_type"
argument_list|,
name|HostType
operator|.
name|PRIVATE_IP
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
comment|/**      * discovery.ec2.any_group: If set to false, will require all security groups to be present for the instance to be used for the      * discovery. Defaults to true.      */
DECL|field|ANY_GROUP_SETTING
name|Setting
argument_list|<
name|Boolean
argument_list|>
name|ANY_GROUP_SETTING
init|=
name|Setting
operator|.
name|boolSetting
argument_list|(
literal|"discovery.ec2.any_group"
argument_list|,
literal|true
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
comment|/**      * discovery.ec2.groups: Either a comma separated list or array based list of (security) groups. Only instances with the provided      * security groups will be used in the cluster discovery. (NOTE: You could provide either group NAME or group ID.)      */
DECL|field|GROUPS_SETTING
name|Setting
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|GROUPS_SETTING
init|=
name|Setting
operator|.
name|listSetting
argument_list|(
literal|"discovery.ec2.groups"
argument_list|,
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|,
name|s
lambda|->
name|s
operator|.
name|toString
argument_list|()
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
comment|/**      * discovery.ec2.availability_zones: Either a comma separated list or array based list of availability zones. Only instances within      * the provided availability zones will be used in the cluster discovery.      */
DECL|field|AVAILABILITY_ZONES_SETTING
name|Setting
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|AVAILABILITY_ZONES_SETTING
init|=
name|Setting
operator|.
name|listSetting
argument_list|(
literal|"discovery.ec2.availability_zones"
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|,
name|s
lambda|->
name|s
operator|.
name|toString
argument_list|()
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
comment|/**      * discovery.ec2.node_cache_time: How long the list of hosts is cached to prevent further requests to the AWS API. Defaults to 10s.      */
DECL|field|NODE_CACHE_TIME_SETTING
name|Setting
argument_list|<
name|TimeValue
argument_list|>
name|NODE_CACHE_TIME_SETTING
init|=
name|Setting
operator|.
name|timeSetting
argument_list|(
literal|"discovery.ec2.node_cache_time"
argument_list|,
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|10
argument_list|)
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
comment|/**      * discovery.ec2.tag.*: The ec2 discovery can filter machines to include in the cluster based on tags (and not just groups).      * The settings to use include the discovery.ec2.tag. prefix. For example, setting discovery.ec2.tag.stage to dev will only filter      * instances with a tag key set to stage, and a value of dev. Several tags set will require all of those tags to be set for the      * instance to be included.      */
DECL|field|TAG_SETTING
name|Setting
argument_list|<
name|Settings
argument_list|>
name|TAG_SETTING
init|=
name|Setting
operator|.
name|groupSetting
argument_list|(
literal|"discovery.ec2.tag."
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|method|client
name|AmazonEC2
name|client
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

