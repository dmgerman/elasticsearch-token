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
name|elasticsearch
operator|.
name|common
operator|.
name|SuppressForbidden
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FilePermission
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
name|net
operator|.
name|SocketPermission
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
name|CodeSource
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|Permission
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PermissionCollection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|Permissions
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|Policy
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|ProtectionDomain
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
name|function
operator|.
name|Predicate
import|;
end_import

begin_comment
comment|/** custom policy for union of static and dynamic permissions */
end_comment

begin_class
DECL|class|ESPolicy
specifier|final
class|class
name|ESPolicy
extends|extends
name|Policy
block|{
comment|/** template policy file, the one used in tests */
DECL|field|POLICY_RESOURCE
specifier|static
specifier|final
name|String
name|POLICY_RESOURCE
init|=
literal|"security.policy"
decl_stmt|;
comment|/** limited policy for scripts */
DECL|field|UNTRUSTED_RESOURCE
specifier|static
specifier|final
name|String
name|UNTRUSTED_RESOURCE
init|=
literal|"untrusted.policy"
decl_stmt|;
DECL|field|template
specifier|final
name|Policy
name|template
decl_stmt|;
DECL|field|untrusted
specifier|final
name|Policy
name|untrusted
decl_stmt|;
DECL|field|system
specifier|final
name|Policy
name|system
decl_stmt|;
DECL|field|dynamic
specifier|final
name|PermissionCollection
name|dynamic
decl_stmt|;
DECL|field|plugins
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Policy
argument_list|>
name|plugins
decl_stmt|;
DECL|method|ESPolicy
name|ESPolicy
parameter_list|(
name|PermissionCollection
name|dynamic
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Policy
argument_list|>
name|plugins
parameter_list|,
name|boolean
name|filterBadDefaults
parameter_list|)
block|{
name|this
operator|.
name|template
operator|=
name|Security
operator|.
name|readPolicy
argument_list|(
name|getClass
argument_list|()
operator|.
name|getResource
argument_list|(
name|POLICY_RESOURCE
argument_list|)
argument_list|,
name|JarHell
operator|.
name|parseClassPath
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|untrusted
operator|=
name|Security
operator|.
name|readPolicy
argument_list|(
name|getClass
argument_list|()
operator|.
name|getResource
argument_list|(
name|UNTRUSTED_RESOURCE
argument_list|)
argument_list|,
operator|new
name|URL
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
if|if
condition|(
name|filterBadDefaults
condition|)
block|{
name|this
operator|.
name|system
operator|=
operator|new
name|SystemPolicy
argument_list|(
name|Policy
operator|.
name|getPolicy
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|system
operator|=
name|Policy
operator|.
name|getPolicy
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|dynamic
operator|=
name|dynamic
expr_stmt|;
name|this
operator|.
name|plugins
operator|=
name|plugins
expr_stmt|;
block|}
annotation|@
name|Override
annotation|@
name|SuppressForbidden
argument_list|(
name|reason
operator|=
literal|"fast equals check is desired"
argument_list|)
DECL|method|implies
specifier|public
name|boolean
name|implies
parameter_list|(
name|ProtectionDomain
name|domain
parameter_list|,
name|Permission
name|permission
parameter_list|)
block|{
name|CodeSource
name|codeSource
init|=
name|domain
operator|.
name|getCodeSource
argument_list|()
decl_stmt|;
comment|// codesource can be null when reducing privileges via doPrivileged()
if|if
condition|(
name|codeSource
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|URL
name|location
init|=
name|codeSource
operator|.
name|getLocation
argument_list|()
decl_stmt|;
comment|// location can be null... ??? nobody knows
comment|// https://bugs.openjdk.java.net/browse/JDK-8129972
if|if
condition|(
name|location
operator|!=
literal|null
condition|)
block|{
comment|// run scripts with limited permissions
if|if
condition|(
name|BootstrapInfo
operator|.
name|UNTRUSTED_CODEBASE
operator|.
name|equals
argument_list|(
name|location
operator|.
name|getFile
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|untrusted
operator|.
name|implies
argument_list|(
name|domain
argument_list|,
name|permission
argument_list|)
return|;
block|}
comment|// check for an additional plugin permission: plugin policy is
comment|// only consulted for its codesources.
name|Policy
name|plugin
init|=
name|plugins
operator|.
name|get
argument_list|(
name|location
operator|.
name|getFile
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|plugin
operator|!=
literal|null
operator|&&
name|plugin
operator|.
name|implies
argument_list|(
name|domain
argument_list|,
name|permission
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
comment|// Special handling for broken Hadoop code: "let me execute or my classes will not load"
comment|// yeah right, REMOVE THIS when hadoop is fixed
if|if
condition|(
name|permission
operator|instanceof
name|FilePermission
operator|&&
literal|"<<ALL FILES>>"
operator|.
name|equals
argument_list|(
name|permission
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
for|for
control|(
name|StackTraceElement
name|element
range|:
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getStackTrace
argument_list|()
control|)
block|{
if|if
condition|(
literal|"org.apache.hadoop.util.Shell"
operator|.
name|equals
argument_list|(
name|element
operator|.
name|getClassName
argument_list|()
argument_list|)
operator|&&
literal|"runCommand"
operator|.
name|equals
argument_list|(
name|element
operator|.
name|getMethodName
argument_list|()
argument_list|)
condition|)
block|{
comment|// we found the horrible method: the hack begins!
comment|// force the hadoop code to back down, by throwing an exception that it catches.
name|rethrow
argument_list|(
operator|new
name|IOException
argument_list|(
literal|"no hadoop, you cannot do this."
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// otherwise defer to template + dynamic file permissions
return|return
name|template
operator|.
name|implies
argument_list|(
name|domain
argument_list|,
name|permission
argument_list|)
operator|||
name|dynamic
operator|.
name|implies
argument_list|(
name|permission
argument_list|)
operator|||
name|system
operator|.
name|implies
argument_list|(
name|domain
argument_list|,
name|permission
argument_list|)
return|;
block|}
comment|/**      * Classy puzzler to rethrow any checked exception as an unchecked one.      */
DECL|class|Rethrower
specifier|private
specifier|static
class|class
name|Rethrower
parameter_list|<
name|T
extends|extends
name|Throwable
parameter_list|>
block|{
DECL|method|rethrow
specifier|private
name|void
name|rethrow
parameter_list|(
name|Throwable
name|t
parameter_list|)
throws|throws
name|T
block|{
throw|throw
operator|(
name|T
operator|)
name|t
throw|;
block|}
block|}
comment|/**      * Rethrows<code>t</code> (identical object).      */
DECL|method|rethrow
specifier|private
name|void
name|rethrow
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
operator|new
name|Rethrower
argument_list|<
name|Error
argument_list|>
argument_list|()
operator|.
name|rethrow
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getPermissions
specifier|public
name|PermissionCollection
name|getPermissions
parameter_list|(
name|CodeSource
name|codesource
parameter_list|)
block|{
comment|// code should not rely on this method, or at least use it correctly:
comment|// https://bugs.openjdk.java.net/browse/JDK-8014008
comment|// return them a new empty permissions object so jvisualvm etc work
for|for
control|(
name|StackTraceElement
name|element
range|:
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getStackTrace
argument_list|()
control|)
block|{
if|if
condition|(
literal|"sun.rmi.server.LoaderHandler"
operator|.
name|equals
argument_list|(
name|element
operator|.
name|getClassName
argument_list|()
argument_list|)
operator|&&
literal|"loadClass"
operator|.
name|equals
argument_list|(
name|element
operator|.
name|getMethodName
argument_list|()
argument_list|)
condition|)
block|{
return|return
operator|new
name|Permissions
argument_list|()
return|;
block|}
block|}
comment|// return UNSUPPORTED_EMPTY_COLLECTION since it is safe.
return|return
name|super
operator|.
name|getPermissions
argument_list|(
name|codesource
argument_list|)
return|;
block|}
comment|// TODO: remove this hack when insecure defaults are removed from java
comment|/**      * Wraps a bad default permission, applying a pre-implies to any permissions before checking if the wrapped bad default permission      * implies a permission.      */
DECL|class|BadDefaultPermission
specifier|private
specifier|static
class|class
name|BadDefaultPermission
extends|extends
name|Permission
block|{
DECL|field|badDefaultPermission
specifier|private
specifier|final
name|Permission
name|badDefaultPermission
decl_stmt|;
DECL|field|preImplies
specifier|private
specifier|final
name|Predicate
argument_list|<
name|Permission
argument_list|>
name|preImplies
decl_stmt|;
comment|/**          * Construct an instance with a pre-implies check to apply to desired permissions.          *          * @param badDefaultPermission the bad default permission to wrap          * @param preImplies           a test that is applied to a desired permission before checking if the bad default permission that          *                             this instance wraps implies the desired permission          */
DECL|method|BadDefaultPermission
name|BadDefaultPermission
parameter_list|(
specifier|final
name|Permission
name|badDefaultPermission
parameter_list|,
specifier|final
name|Predicate
argument_list|<
name|Permission
argument_list|>
name|preImplies
parameter_list|)
block|{
name|super
argument_list|(
name|badDefaultPermission
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|badDefaultPermission
operator|=
name|badDefaultPermission
expr_stmt|;
name|this
operator|.
name|preImplies
operator|=
name|preImplies
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|implies
specifier|public
specifier|final
name|boolean
name|implies
parameter_list|(
name|Permission
name|permission
parameter_list|)
block|{
return|return
name|preImplies
operator|.
name|test
argument_list|(
name|permission
argument_list|)
operator|&&
name|badDefaultPermission
operator|.
name|implies
argument_list|(
name|permission
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
specifier|final
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
return|return
name|badDefaultPermission
operator|.
name|equals
argument_list|(
name|obj
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|badDefaultPermission
operator|.
name|hashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|getActions
specifier|public
name|String
name|getActions
parameter_list|()
block|{
return|return
name|badDefaultPermission
operator|.
name|getActions
argument_list|()
return|;
block|}
block|}
comment|// default policy file states:
comment|// "It is strongly recommended that you either remove this permission
comment|//  from this policy file or further restrict it to code sources
comment|//  that you specify, because Thread.stop() is potentially unsafe."
comment|// not even sure this method still works...
DECL|field|BAD_DEFAULT_NUMBER_ONE
specifier|private
specifier|static
specifier|final
name|Permission
name|BAD_DEFAULT_NUMBER_ONE
init|=
operator|new
name|BadDefaultPermission
argument_list|(
operator|new
name|RuntimePermission
argument_list|(
literal|"stopThread"
argument_list|)
argument_list|,
name|p
lambda|->
literal|true
argument_list|)
decl_stmt|;
comment|// default policy file states:
comment|// "allows anyone to listen on dynamic ports"
comment|// specified exactly because that is what we want, and fastest since it won't imply any
comment|// expensive checks for the implicit "resolve"
DECL|field|BAD_DEFAULT_NUMBER_TWO
specifier|private
specifier|static
specifier|final
name|Permission
name|BAD_DEFAULT_NUMBER_TWO
init|=
operator|new
name|BadDefaultPermission
argument_list|(
operator|new
name|SocketPermission
argument_list|(
literal|"localhost:0"
argument_list|,
literal|"listen"
argument_list|)
argument_list|,
comment|// we apply this pre-implies test because some SocketPermission#implies calls do expensive reverse-DNS resolves
name|p
lambda|->
name|p
operator|instanceof
name|SocketPermission
operator|&&
name|p
operator|.
name|getActions
argument_list|()
operator|.
name|contains
argument_list|(
literal|"listen"
argument_list|)
argument_list|)
decl_stmt|;
comment|/**      * Wraps the Java system policy, filtering out bad default permissions that      * are granted to all domains. Note, before java 8 these were even worse.      */
DECL|class|SystemPolicy
specifier|static
class|class
name|SystemPolicy
extends|extends
name|Policy
block|{
DECL|field|delegate
specifier|final
name|Policy
name|delegate
decl_stmt|;
DECL|method|SystemPolicy
name|SystemPolicy
parameter_list|(
name|Policy
name|delegate
parameter_list|)
block|{
name|this
operator|.
name|delegate
operator|=
name|delegate
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|implies
specifier|public
name|boolean
name|implies
parameter_list|(
name|ProtectionDomain
name|domain
parameter_list|,
name|Permission
name|permission
parameter_list|)
block|{
if|if
condition|(
name|BAD_DEFAULT_NUMBER_ONE
operator|.
name|implies
argument_list|(
name|permission
argument_list|)
operator|||
name|BAD_DEFAULT_NUMBER_TWO
operator|.
name|implies
argument_list|(
name|permission
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|delegate
operator|.
name|implies
argument_list|(
name|domain
argument_list|,
name|permission
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

