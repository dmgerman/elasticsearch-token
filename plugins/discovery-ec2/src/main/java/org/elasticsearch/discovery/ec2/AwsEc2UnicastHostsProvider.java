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
name|AmazonClientException
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
name|model
operator|.
name|DescribeInstancesRequest
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
name|model
operator|.
name|DescribeInstancesResult
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
name|model
operator|.
name|Filter
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
name|model
operator|.
name|GroupIdentifier
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
name|model
operator|.
name|Instance
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
name|model
operator|.
name|Reservation
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
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
name|AwsEc2Service
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
name|AwsEc2Service
operator|.
name|DISCOVERY_EC2
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
name|DiscoveryNode
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
name|AbstractComponent
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
name|transport
operator|.
name|TransportAddress
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
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|util
operator|.
name|SingleObjectCache
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|zen
operator|.
name|ping
operator|.
name|unicast
operator|.
name|UnicastHostsProvider
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportService
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
name|HashSet
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|AwsEc2UnicastHostsProvider
specifier|public
class|class
name|AwsEc2UnicastHostsProvider
extends|extends
name|AbstractComponent
implements|implements
name|UnicastHostsProvider
block|{
DECL|field|transportService
specifier|private
specifier|final
name|TransportService
name|transportService
decl_stmt|;
DECL|field|client
specifier|private
specifier|final
name|AmazonEC2
name|client
decl_stmt|;
DECL|field|version
specifier|private
specifier|final
name|Version
name|version
decl_stmt|;
DECL|field|bindAnyGroup
specifier|private
specifier|final
name|boolean
name|bindAnyGroup
decl_stmt|;
DECL|field|groups
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|groups
decl_stmt|;
DECL|field|tags
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tags
decl_stmt|;
DECL|field|availabilityZones
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|availabilityZones
decl_stmt|;
DECL|field|hostType
specifier|private
specifier|final
name|DISCOVERY_EC2
operator|.
name|HostType
name|hostType
decl_stmt|;
DECL|field|discoNodes
specifier|private
specifier|final
name|DiscoNodesCache
name|discoNodes
decl_stmt|;
annotation|@
name|Inject
DECL|method|AwsEc2UnicastHostsProvider
specifier|public
name|AwsEc2UnicastHostsProvider
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|AwsEc2Service
name|awsEc2Service
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|transportService
operator|=
name|transportService
expr_stmt|;
name|this
operator|.
name|client
operator|=
name|awsEc2Service
operator|.
name|client
argument_list|()
expr_stmt|;
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
name|this
operator|.
name|hostType
operator|=
name|DISCOVERY_EC2
operator|.
name|HOST_TYPE_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|discoNodes
operator|=
operator|new
name|DiscoNodesCache
argument_list|(
name|DISCOVERY_EC2
operator|.
name|NODE_CACHE_TIME_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|bindAnyGroup
operator|=
name|DISCOVERY_EC2
operator|.
name|ANY_GROUP_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|groups
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
expr_stmt|;
name|this
operator|.
name|groups
operator|.
name|addAll
argument_list|(
name|DISCOVERY_EC2
operator|.
name|GROUPS_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|tags
operator|=
name|DISCOVERY_EC2
operator|.
name|TAG_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
operator|.
name|getAsMap
argument_list|()
expr_stmt|;
name|this
operator|.
name|availabilityZones
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
expr_stmt|;
name|availabilityZones
operator|.
name|addAll
argument_list|(
name|DISCOVERY_EC2
operator|.
name|AVAILABILITY_ZONES_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"using host_type [{}], tags [{}], groups [{}] with any_group [{}], availability_zones [{}]"
argument_list|,
name|hostType
argument_list|,
name|tags
argument_list|,
name|groups
argument_list|,
name|bindAnyGroup
argument_list|,
name|availabilityZones
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|buildDynamicNodes
specifier|public
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|buildDynamicNodes
parameter_list|()
block|{
return|return
name|discoNodes
operator|.
name|getOrRefresh
argument_list|()
return|;
block|}
DECL|method|fetchDynamicNodes
specifier|protected
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|fetchDynamicNodes
parameter_list|()
block|{
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|discoNodes
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|DescribeInstancesResult
name|descInstances
decl_stmt|;
try|try
block|{
comment|// Query EC2 API based on AZ, instance state, and tag.
comment|// NOTE: we don't filter by security group during the describe instances request for two reasons:
comment|// 1. differences in VPCs require different parameters during query (ID vs Name)
comment|// 2. We want to use two different strategies: (all security groups vs. any security groups)
name|descInstances
operator|=
name|client
operator|.
name|describeInstances
argument_list|(
name|buildDescribeInstancesRequest
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AmazonClientException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"Exception while retrieving instance list from AWS API: {}"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"Full exception:"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
name|discoNodes
return|;
block|}
name|logger
operator|.
name|trace
argument_list|(
literal|"building dynamic unicast discovery nodes..."
argument_list|)
expr_stmt|;
for|for
control|(
name|Reservation
name|reservation
range|:
name|descInstances
operator|.
name|getReservations
argument_list|()
control|)
block|{
for|for
control|(
name|Instance
name|instance
range|:
name|reservation
operator|.
name|getInstances
argument_list|()
control|)
block|{
comment|// lets see if we can filter based on groups
if|if
condition|(
operator|!
name|groups
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|List
argument_list|<
name|GroupIdentifier
argument_list|>
name|instanceSecurityGroups
init|=
name|instance
operator|.
name|getSecurityGroups
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|securityGroupNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|securityGroupIds
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|GroupIdentifier
name|sg
range|:
name|instanceSecurityGroups
control|)
block|{
name|securityGroupNames
operator|.
name|add
argument_list|(
name|sg
operator|.
name|getGroupName
argument_list|()
argument_list|)
expr_stmt|;
name|securityGroupIds
operator|.
name|add
argument_list|(
name|sg
operator|.
name|getGroupId
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|bindAnyGroup
condition|)
block|{
comment|// We check if we can find at least one group name or one group id in groups.
if|if
condition|(
name|Collections
operator|.
name|disjoint
argument_list|(
name|securityGroupNames
argument_list|,
name|groups
argument_list|)
operator|&&
name|Collections
operator|.
name|disjoint
argument_list|(
name|securityGroupIds
argument_list|,
name|groups
argument_list|)
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"filtering out instance {} based on groups {}, not part of {}"
argument_list|,
name|instance
operator|.
name|getInstanceId
argument_list|()
argument_list|,
name|instanceSecurityGroups
argument_list|,
name|groups
argument_list|)
expr_stmt|;
comment|// continue to the next instance
continue|continue;
block|}
block|}
else|else
block|{
comment|// We need tp match all group names or group ids, otherwise we ignore this instance
if|if
condition|(
operator|!
operator|(
name|securityGroupNames
operator|.
name|containsAll
argument_list|(
name|groups
argument_list|)
operator|||
name|securityGroupIds
operator|.
name|containsAll
argument_list|(
name|groups
argument_list|)
operator|)
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"filtering out instance {} based on groups {}, does not include all of {}"
argument_list|,
name|instance
operator|.
name|getInstanceId
argument_list|()
argument_list|,
name|instanceSecurityGroups
argument_list|,
name|groups
argument_list|)
expr_stmt|;
comment|// continue to the next instance
continue|continue;
block|}
block|}
block|}
name|String
name|address
init|=
literal|null
decl_stmt|;
switch|switch
condition|(
name|hostType
condition|)
block|{
case|case
name|PRIVATE_DNS
case|:
name|address
operator|=
name|instance
operator|.
name|getPrivateDnsName
argument_list|()
expr_stmt|;
break|break;
case|case
name|PRIVATE_IP
case|:
name|address
operator|=
name|instance
operator|.
name|getPrivateIpAddress
argument_list|()
expr_stmt|;
break|break;
case|case
name|PUBLIC_DNS
case|:
name|address
operator|=
name|instance
operator|.
name|getPublicDnsName
argument_list|()
expr_stmt|;
break|break;
case|case
name|PUBLIC_IP
case|:
name|address
operator|=
name|instance
operator|.
name|getPublicIpAddress
argument_list|()
expr_stmt|;
break|break;
block|}
if|if
condition|(
name|address
operator|!=
literal|null
condition|)
block|{
try|try
block|{
comment|// we only limit to 1 port per address, makes no sense to ping 100 ports
name|TransportAddress
index|[]
name|addresses
init|=
name|transportService
operator|.
name|addressesFromString
argument_list|(
name|address
argument_list|,
literal|1
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
name|addresses
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"adding {}, address {}, transport_address {}"
argument_list|,
name|instance
operator|.
name|getInstanceId
argument_list|()
argument_list|,
name|address
argument_list|,
name|addresses
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|discoNodes
operator|.
name|add
argument_list|(
operator|new
name|DiscoveryNode
argument_list|(
literal|"#cloud-"
operator|+
name|instance
operator|.
name|getInstanceId
argument_list|()
operator|+
literal|"-"
operator|+
name|i
argument_list|,
name|addresses
index|[
name|i
index|]
argument_list|,
name|version
operator|.
name|minimumCompatibilityVersion
argument_list|()
argument_list|)
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
literal|"failed ot add {}, address {}"
argument_list|,
name|e
argument_list|,
name|instance
operator|.
name|getInstanceId
argument_list|()
argument_list|,
name|address
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"not adding {}, address is null, host_type {}"
argument_list|,
name|instance
operator|.
name|getInstanceId
argument_list|()
argument_list|,
name|hostType
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|logger
operator|.
name|debug
argument_list|(
literal|"using dynamic discovery nodes {}"
argument_list|,
name|discoNodes
argument_list|)
expr_stmt|;
return|return
name|discoNodes
return|;
block|}
DECL|method|buildDescribeInstancesRequest
specifier|private
name|DescribeInstancesRequest
name|buildDescribeInstancesRequest
parameter_list|()
block|{
name|DescribeInstancesRequest
name|describeInstancesRequest
init|=
operator|new
name|DescribeInstancesRequest
argument_list|()
operator|.
name|withFilters
argument_list|(
operator|new
name|Filter
argument_list|(
literal|"instance-state-name"
argument_list|)
operator|.
name|withValues
argument_list|(
literal|"running"
argument_list|,
literal|"pending"
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tagFilter
range|:
name|tags
operator|.
name|entrySet
argument_list|()
control|)
block|{
comment|// for a given tag key, OR relationship for multiple different values
name|describeInstancesRequest
operator|.
name|withFilters
argument_list|(
operator|new
name|Filter
argument_list|(
literal|"tag:"
operator|+
name|tagFilter
operator|.
name|getKey
argument_list|()
argument_list|)
operator|.
name|withValues
argument_list|(
name|tagFilter
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|availabilityZones
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// OR relationship amongst multiple values of the availability-zone filter
name|describeInstancesRequest
operator|.
name|withFilters
argument_list|(
operator|new
name|Filter
argument_list|(
literal|"availability-zone"
argument_list|)
operator|.
name|withValues
argument_list|(
name|availabilityZones
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|describeInstancesRequest
return|;
block|}
DECL|class|DiscoNodesCache
specifier|private
specifier|final
class|class
name|DiscoNodesCache
extends|extends
name|SingleObjectCache
argument_list|<
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
argument_list|>
block|{
DECL|field|empty
specifier|private
name|boolean
name|empty
init|=
literal|true
decl_stmt|;
DECL|method|DiscoNodesCache
specifier|protected
name|DiscoNodesCache
parameter_list|(
name|TimeValue
name|refreshInterval
parameter_list|)
block|{
name|super
argument_list|(
name|refreshInterval
argument_list|,
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|needsRefresh
specifier|protected
name|boolean
name|needsRefresh
parameter_list|()
block|{
return|return
operator|(
name|empty
operator|||
name|super
operator|.
name|needsRefresh
argument_list|()
operator|)
return|;
block|}
annotation|@
name|Override
DECL|method|refresh
specifier|protected
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|refresh
parameter_list|()
block|{
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|nodes
init|=
name|fetchDynamicNodes
argument_list|()
decl_stmt|;
name|empty
operator|=
name|nodes
operator|.
name|isEmpty
argument_list|()
expr_stmt|;
return|return
name|nodes
return|;
block|}
block|}
block|}
end_class

end_unit

