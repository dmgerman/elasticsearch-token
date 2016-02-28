begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.bootstrap
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|bootstrap
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|Constants
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
name|logging
operator|.
name|Loggers
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
name|monitor
operator|.
name|process
operator|.
name|ProcessProbe
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
name|TransportSettings
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|Locale
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
comment|/**  * We enforce limits once any network host is configured. In this case we assume the node is running in production  * and all production limit checks must pass. This should be extended as we go to settings like:  * - discovery.zen.minimum_master_nodes  * - discovery.zen.ping.unicast.hosts is set if we use zen disco  * - ensure we can write in all data directories  * - fail if mlockall failed and was configured  * - fail if vm.max_map_count is under a certain limit (not sure if this works cross platform)  * - fail if the default cluster.name is used, if this is setup on network a real clustername should be used?  */
end_comment

begin_class
DECL|class|BootstrapCheck
specifier|final
class|class
name|BootstrapCheck
block|{
DECL|method|BootstrapCheck
specifier|private
name|BootstrapCheck
parameter_list|()
block|{     }
comment|/**      * checks the current limits against the snapshot or release build      * checks      *      * @param settings the current node settings      */
DECL|method|check
specifier|public
specifier|static
name|void
name|check
parameter_list|(
specifier|final
name|Settings
name|settings
parameter_list|)
block|{
name|check
argument_list|(
name|enforceLimits
argument_list|(
name|settings
argument_list|)
argument_list|,
name|checks
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * executes the provided checks and fails the node if      * enforceLimits is true, otherwise logs warnings      *      * @param enforceLimits true if the checks should be enforced or      *                      warned      * @param checks        the checks to execute      */
comment|// visible for testing
DECL|method|check
specifier|static
name|void
name|check
parameter_list|(
name|boolean
name|enforceLimits
parameter_list|,
name|List
argument_list|<
name|Check
argument_list|>
name|checks
parameter_list|)
block|{
specifier|final
name|ESLogger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|BootstrapCheck
operator|.
name|class
argument_list|)
decl_stmt|;
for|for
control|(
name|Check
name|check
range|:
name|checks
control|)
block|{
specifier|final
name|boolean
name|fail
init|=
name|check
operator|.
name|check
argument_list|()
decl_stmt|;
if|if
condition|(
name|fail
condition|)
block|{
if|if
condition|(
name|enforceLimits
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|check
operator|.
name|errorMessage
argument_list|()
argument_list|)
throw|;
block|}
else|else
block|{
name|logger
operator|.
name|warn
argument_list|(
name|check
operator|.
name|errorMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**      * The set of settings such that if any are set for the node, then      * the checks are enforced      *      * @return the enforcement settings      */
comment|// visible for testing
DECL|method|enforceSettings
specifier|static
name|Set
argument_list|<
name|Setting
argument_list|>
name|enforceSettings
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableSet
argument_list|(
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|TransportSettings
operator|.
name|BIND_HOST
argument_list|,
name|TransportSettings
operator|.
name|HOST
argument_list|,
name|TransportSettings
operator|.
name|PUBLISH_HOST
argument_list|,
name|NetworkService
operator|.
name|GLOBAL_NETWORK_HOST_SETTING
argument_list|,
name|NetworkService
operator|.
name|GLOBAL_NETWORK_BINDHOST_SETTING
argument_list|,
name|NetworkService
operator|.
name|GLOBAL_NETWORK_PUBLISHHOST_SETTING
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Tests if the checks should be enforced      *      * @param settings the current node settings      * @return true if the checks should be enforced      */
comment|// visible for testing
DECL|method|enforceLimits
specifier|static
name|boolean
name|enforceLimits
parameter_list|(
specifier|final
name|Settings
name|settings
parameter_list|)
block|{
return|return
name|enforceSettings
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|anyMatch
argument_list|(
name|s
lambda|->
name|s
operator|.
name|exists
argument_list|(
name|settings
argument_list|)
argument_list|)
return|;
block|}
comment|// the list of checks to execute
DECL|method|checks
specifier|private
specifier|static
name|List
argument_list|<
name|Check
argument_list|>
name|checks
parameter_list|()
block|{
name|FileDescriptorCheck
name|fileDescriptorCheck
init|=
name|Constants
operator|.
name|MAC_OS_X
condition|?
operator|new
name|OsXFileDescriptorCheck
argument_list|()
else|:
operator|new
name|FileDescriptorCheck
argument_list|()
decl_stmt|;
return|return
name|Collections
operator|.
name|singletonList
argument_list|(
name|fileDescriptorCheck
argument_list|)
return|;
block|}
comment|/**      * Encapsulates a limit check      */
DECL|interface|Check
interface|interface
name|Check
block|{
comment|/**          * test if the node fails the check          *          * @return true if the node failed the check          */
DECL|method|check
name|boolean
name|check
parameter_list|()
function_decl|;
comment|/**          * the message for a failed check          *          * @return the error message on check failure          */
DECL|method|errorMessage
name|String
name|errorMessage
parameter_list|()
function_decl|;
block|}
DECL|class|OsXFileDescriptorCheck
specifier|static
class|class
name|OsXFileDescriptorCheck
extends|extends
name|FileDescriptorCheck
block|{
DECL|method|OsXFileDescriptorCheck
specifier|public
name|OsXFileDescriptorCheck
parameter_list|()
block|{
comment|// see constant OPEN_MAX defined in
comment|// /usr/include/sys/syslimits.h on OS X and its use in JVM
comment|// initialization in int os:init_2(void) defined in the JVM
comment|// code for BSD (contains OS X)
name|super
argument_list|(
literal|10240
argument_list|)
expr_stmt|;
block|}
block|}
comment|// visible for testing
DECL|class|FileDescriptorCheck
specifier|static
class|class
name|FileDescriptorCheck
implements|implements
name|Check
block|{
DECL|field|limit
specifier|private
specifier|final
name|int
name|limit
decl_stmt|;
DECL|method|FileDescriptorCheck
name|FileDescriptorCheck
parameter_list|()
block|{
name|this
argument_list|(
literal|1
operator|<<
literal|16
argument_list|)
expr_stmt|;
block|}
DECL|method|FileDescriptorCheck
specifier|protected
name|FileDescriptorCheck
parameter_list|(
name|int
name|limit
parameter_list|)
block|{
if|if
condition|(
name|limit
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"limit must be positive but was ["
operator|+
name|limit
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|limit
operator|=
name|limit
expr_stmt|;
block|}
DECL|method|check
specifier|public
specifier|final
name|boolean
name|check
parameter_list|()
block|{
specifier|final
name|long
name|maxFileDescriptorCount
init|=
name|getMaxFileDescriptorCount
argument_list|()
decl_stmt|;
return|return
name|maxFileDescriptorCount
operator|!=
operator|-
literal|1
operator|&&
name|maxFileDescriptorCount
operator|<
name|limit
return|;
block|}
annotation|@
name|Override
DECL|method|errorMessage
specifier|public
specifier|final
name|String
name|errorMessage
parameter_list|()
block|{
return|return
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
literal|"max file descriptors [%d] for elasticsearch process likely too low, increase to at least [%d]"
argument_list|,
name|getMaxFileDescriptorCount
argument_list|()
argument_list|,
name|limit
argument_list|)
return|;
block|}
comment|// visible for testing
DECL|method|getMaxFileDescriptorCount
name|long
name|getMaxFileDescriptorCount
parameter_list|()
block|{
return|return
name|ProcessProbe
operator|.
name|getInstance
argument_list|()
operator|.
name|getMaxFileDescriptorCount
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

