begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.plugins
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
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
name|IOUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cli
operator|.
name|Terminal
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cli
operator|.
name|Terminal
operator|.
name|Verbosity
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
name|nio
operator|.
name|file
operator|.
name|Files
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Path
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|NoSuchAlgorithmException
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
name|URIParameter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|UnresolvedPermission
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
name|Comparator
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
name|function
operator|.
name|Supplier
import|;
end_import

begin_class
DECL|class|PluginSecurity
class|class
name|PluginSecurity
block|{
comment|/**      * Reads plugin policy, prints/confirms exceptions      */
DECL|method|readPolicy
specifier|static
name|void
name|readPolicy
parameter_list|(
name|PluginInfo
name|info
parameter_list|,
name|Path
name|file
parameter_list|,
name|Terminal
name|terminal
parameter_list|,
name|Supplier
argument_list|<
name|Path
argument_list|>
name|tmpFile
parameter_list|,
name|boolean
name|batch
parameter_list|)
throws|throws
name|IOException
block|{
name|PermissionCollection
name|permissions
init|=
name|parsePermissions
argument_list|(
name|terminal
argument_list|,
name|file
argument_list|,
name|tmpFile
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Permission
argument_list|>
name|requested
init|=
name|Collections
operator|.
name|list
argument_list|(
name|permissions
operator|.
name|elements
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|requested
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|terminal
operator|.
name|println
argument_list|(
name|Verbosity
operator|.
name|VERBOSE
argument_list|,
literal|"plugin has a policy file with no additional permissions"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// sort permissions in a reasonable order
name|Collections
operator|.
name|sort
argument_list|(
name|requested
argument_list|,
operator|new
name|Comparator
argument_list|<
name|Permission
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|Permission
name|o1
parameter_list|,
name|Permission
name|o2
parameter_list|)
block|{
name|int
name|cmp
init|=
name|o1
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|compareTo
argument_list|(
name|o2
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|cmp
operator|==
literal|0
condition|)
block|{
name|String
name|name1
init|=
name|o1
operator|.
name|getName
argument_list|()
decl_stmt|;
name|String
name|name2
init|=
name|o2
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
name|name1
operator|==
literal|null
condition|)
block|{
name|name1
operator|=
literal|""
expr_stmt|;
block|}
if|if
condition|(
name|name2
operator|==
literal|null
condition|)
block|{
name|name2
operator|=
literal|""
expr_stmt|;
block|}
name|cmp
operator|=
name|name1
operator|.
name|compareTo
argument_list|(
name|name2
argument_list|)
expr_stmt|;
if|if
condition|(
name|cmp
operator|==
literal|0
condition|)
block|{
name|String
name|actions1
init|=
name|o1
operator|.
name|getActions
argument_list|()
decl_stmt|;
name|String
name|actions2
init|=
name|o2
operator|.
name|getActions
argument_list|()
decl_stmt|;
if|if
condition|(
name|actions1
operator|==
literal|null
condition|)
block|{
name|actions1
operator|=
literal|""
expr_stmt|;
block|}
if|if
condition|(
name|actions2
operator|==
literal|null
condition|)
block|{
name|actions2
operator|=
literal|""
expr_stmt|;
block|}
name|cmp
operator|=
name|actions1
operator|.
name|compareTo
argument_list|(
name|actions2
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|cmp
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|terminal
operator|.
name|println
argument_list|(
name|Verbosity
operator|.
name|NORMAL
argument_list|,
literal|"@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"
argument_list|)
expr_stmt|;
name|terminal
operator|.
name|println
argument_list|(
name|Verbosity
operator|.
name|NORMAL
argument_list|,
literal|"@     WARNING: plugin requires additional permissions     @"
argument_list|)
expr_stmt|;
name|terminal
operator|.
name|println
argument_list|(
name|Verbosity
operator|.
name|NORMAL
argument_list|,
literal|"@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"
argument_list|)
expr_stmt|;
comment|// print all permissions:
for|for
control|(
name|Permission
name|permission
range|:
name|requested
control|)
block|{
name|terminal
operator|.
name|println
argument_list|(
name|Verbosity
operator|.
name|NORMAL
argument_list|,
literal|"* "
operator|+
name|formatPermission
argument_list|(
name|permission
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|terminal
operator|.
name|println
argument_list|(
name|Verbosity
operator|.
name|NORMAL
argument_list|,
literal|"See http://docs.oracle.com/javase/8/docs/technotes/guides/security/permissions.html"
argument_list|)
expr_stmt|;
name|terminal
operator|.
name|println
argument_list|(
name|Verbosity
operator|.
name|NORMAL
argument_list|,
literal|"for descriptions of what these permissions allow and the associated risks."
argument_list|)
expr_stmt|;
name|prompt
argument_list|(
name|terminal
argument_list|,
name|batch
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|info
operator|.
name|hasNativeController
argument_list|()
condition|)
block|{
name|terminal
operator|.
name|println
argument_list|(
name|Verbosity
operator|.
name|NORMAL
argument_list|,
literal|"@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"
argument_list|)
expr_stmt|;
name|terminal
operator|.
name|println
argument_list|(
name|Verbosity
operator|.
name|NORMAL
argument_list|,
literal|"@        WARNING: plugin forks a native controller        @"
argument_list|)
expr_stmt|;
name|terminal
operator|.
name|println
argument_list|(
name|Verbosity
operator|.
name|NORMAL
argument_list|,
literal|"@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"
argument_list|)
expr_stmt|;
name|terminal
operator|.
name|println
argument_list|(
name|Verbosity
operator|.
name|NORMAL
argument_list|,
literal|"This plugin launches a native controller that is not subject to the Java"
argument_list|)
expr_stmt|;
name|terminal
operator|.
name|println
argument_list|(
name|Verbosity
operator|.
name|NORMAL
argument_list|,
literal|"security manager nor to system call filters."
argument_list|)
expr_stmt|;
name|prompt
argument_list|(
name|terminal
argument_list|,
name|batch
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|prompt
specifier|private
specifier|static
name|void
name|prompt
parameter_list|(
specifier|final
name|Terminal
name|terminal
parameter_list|,
specifier|final
name|boolean
name|batch
parameter_list|)
block|{
if|if
condition|(
operator|!
name|batch
condition|)
block|{
name|terminal
operator|.
name|println
argument_list|(
name|Verbosity
operator|.
name|NORMAL
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|String
name|text
init|=
name|terminal
operator|.
name|readText
argument_list|(
literal|"Continue with installation? [y/N]"
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|text
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"y"
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"installation aborted by user"
argument_list|)
throw|;
block|}
block|}
block|}
comment|/** Format permission type, name, and actions into a string */
DECL|method|formatPermission
specifier|static
name|String
name|formatPermission
parameter_list|(
name|Permission
name|permission
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|String
name|clazz
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|permission
operator|instanceof
name|UnresolvedPermission
condition|)
block|{
name|clazz
operator|=
operator|(
operator|(
name|UnresolvedPermission
operator|)
name|permission
operator|)
operator|.
name|getUnresolvedType
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|clazz
operator|=
name|permission
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|clazz
argument_list|)
expr_stmt|;
name|String
name|name
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|permission
operator|instanceof
name|UnresolvedPermission
condition|)
block|{
name|name
operator|=
operator|(
operator|(
name|UnresolvedPermission
operator|)
name|permission
operator|)
operator|.
name|getUnresolvedName
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|name
operator|=
name|permission
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|name
operator|!=
literal|null
operator|&&
name|name
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|' '
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
name|String
name|actions
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|permission
operator|instanceof
name|UnresolvedPermission
condition|)
block|{
name|actions
operator|=
operator|(
operator|(
name|UnresolvedPermission
operator|)
name|permission
operator|)
operator|.
name|getUnresolvedActions
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|actions
operator|=
name|permission
operator|.
name|getActions
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|actions
operator|!=
literal|null
operator|&&
name|actions
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|' '
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|actions
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**      * Parses plugin policy into a set of permissions      */
DECL|method|parsePermissions
specifier|static
name|PermissionCollection
name|parsePermissions
parameter_list|(
name|Terminal
name|terminal
parameter_list|,
name|Path
name|file
parameter_list|,
name|Path
name|tmpDir
parameter_list|)
throws|throws
name|IOException
block|{
comment|// create a zero byte file for "comparison"
comment|// this is necessary because the default policy impl automatically grants two permissions:
comment|// 1. permission to exitVM (which we ignore)
comment|// 2. read permission to the code itself (e.g. jar file of the code)
name|Path
name|emptyPolicyFile
init|=
name|Files
operator|.
name|createTempFile
argument_list|(
name|tmpDir
argument_list|,
literal|"empty"
argument_list|,
literal|"tmp"
argument_list|)
decl_stmt|;
specifier|final
name|Policy
name|emptyPolicy
decl_stmt|;
try|try
block|{
name|emptyPolicy
operator|=
name|Policy
operator|.
name|getInstance
argument_list|(
literal|"JavaPolicy"
argument_list|,
operator|new
name|URIParameter
argument_list|(
name|emptyPolicyFile
operator|.
name|toUri
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchAlgorithmException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|IOUtils
operator|.
name|rm
argument_list|(
name|emptyPolicyFile
argument_list|)
expr_stmt|;
comment|// parse the plugin's policy file into a set of permissions
specifier|final
name|Policy
name|policy
decl_stmt|;
try|try
block|{
name|policy
operator|=
name|Policy
operator|.
name|getInstance
argument_list|(
literal|"JavaPolicy"
argument_list|,
operator|new
name|URIParameter
argument_list|(
name|file
operator|.
name|toUri
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchAlgorithmException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|PermissionCollection
name|permissions
init|=
name|policy
operator|.
name|getPermissions
argument_list|(
name|PluginSecurity
operator|.
name|class
operator|.
name|getProtectionDomain
argument_list|()
argument_list|)
decl_stmt|;
comment|// this method is supported with the specific implementation we use, but just check for safety.
if|if
condition|(
name|permissions
operator|==
name|Policy
operator|.
name|UNSUPPORTED_EMPTY_COLLECTION
condition|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"JavaPolicy implementation does not support retrieving permissions"
argument_list|)
throw|;
block|}
name|PermissionCollection
name|actualPermissions
init|=
operator|new
name|Permissions
argument_list|()
decl_stmt|;
for|for
control|(
name|Permission
name|permission
range|:
name|Collections
operator|.
name|list
argument_list|(
name|permissions
operator|.
name|elements
argument_list|()
argument_list|)
control|)
block|{
if|if
condition|(
operator|!
name|emptyPolicy
operator|.
name|implies
argument_list|(
name|PluginSecurity
operator|.
name|class
operator|.
name|getProtectionDomain
argument_list|()
argument_list|,
name|permission
argument_list|)
condition|)
block|{
name|actualPermissions
operator|.
name|add
argument_list|(
name|permission
argument_list|)
expr_stmt|;
block|}
block|}
name|actualPermissions
operator|.
name|setReadOnly
argument_list|()
expr_stmt|;
return|return
name|actualPermissions
return|;
block|}
block|}
end_class

end_unit

