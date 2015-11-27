begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.support
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
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
name|settings
operator|.
name|ClusterSettingsService
import|;
end_import

begin_comment
comment|/**  * Helper for dealing with destructive operations and wildcard usage.  */
end_comment

begin_class
DECL|class|DestructiveOperations
specifier|public
specifier|final
class|class
name|DestructiveOperations
extends|extends
name|AbstractComponent
block|{
comment|/**      * Setting which controls whether wildcard usage (*, prefix*, _all) is allowed.      */
DECL|field|REQUIRES_NAME_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Boolean
argument_list|>
name|REQUIRES_NAME_SETTING
init|=
name|Setting
operator|.
name|boolSetting
argument_list|(
literal|"action.destructive_requires_name"
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
name|Setting
operator|.
name|Scope
operator|.
name|Cluster
argument_list|)
decl_stmt|;
DECL|field|destructiveRequiresName
specifier|private
specifier|volatile
name|boolean
name|destructiveRequiresName
decl_stmt|;
annotation|@
name|Inject
DECL|method|DestructiveOperations
specifier|public
name|DestructiveOperations
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterSettingsService
name|clusterSettingsService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|destructiveRequiresName
operator|=
name|REQUIRES_NAME_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|clusterSettingsService
operator|.
name|addSettingsUpdateConsumer
argument_list|(
name|REQUIRES_NAME_SETTING
argument_list|,
name|this
operator|::
name|setDestructiveRequiresName
argument_list|)
expr_stmt|;
block|}
DECL|method|setDestructiveRequiresName
specifier|private
name|void
name|setDestructiveRequiresName
parameter_list|(
name|boolean
name|destructiveRequiresName
parameter_list|)
block|{
name|this
operator|.
name|destructiveRequiresName
operator|=
name|destructiveRequiresName
expr_stmt|;
block|}
comment|/**      * Fail if there is wildcard usage in indices and the named is required for destructive operations.      */
DECL|method|failDestructive
specifier|public
name|void
name|failDestructive
parameter_list|(
name|String
index|[]
name|aliasesOrIndices
parameter_list|)
block|{
if|if
condition|(
operator|!
name|destructiveRequiresName
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|aliasesOrIndices
operator|==
literal|null
operator|||
name|aliasesOrIndices
operator|.
name|length
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Wildcard expressions or all indices are not allowed"
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|aliasesOrIndices
operator|.
name|length
operator|==
literal|1
condition|)
block|{
if|if
condition|(
name|hasWildcardUsage
argument_list|(
name|aliasesOrIndices
index|[
literal|0
index|]
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Wildcard expressions or all indices are not allowed"
argument_list|)
throw|;
block|}
block|}
else|else
block|{
for|for
control|(
name|String
name|aliasesOrIndex
range|:
name|aliasesOrIndices
control|)
block|{
if|if
condition|(
name|hasWildcardUsage
argument_list|(
name|aliasesOrIndex
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Wildcard expressions or all indices are not allowed"
argument_list|)
throw|;
block|}
block|}
block|}
block|}
DECL|method|hasWildcardUsage
specifier|private
specifier|static
name|boolean
name|hasWildcardUsage
parameter_list|(
name|String
name|aliasOrIndex
parameter_list|)
block|{
return|return
literal|"_all"
operator|.
name|equals
argument_list|(
name|aliasOrIndex
argument_list|)
operator|||
name|aliasOrIndex
operator|.
name|indexOf
argument_list|(
literal|'*'
argument_list|)
operator|!=
operator|-
literal|1
return|;
block|}
block|}
end_class

end_unit

