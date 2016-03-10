begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cloud.azure.storage
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cloud
operator|.
name|azure
operator|.
name|storage
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cloud
operator|.
name|azure
operator|.
name|storage
operator|.
name|AzureStorageService
operator|.
name|Storage
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
name|SettingsException
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
name|repositories
operator|.
name|RepositorySettings
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
name|HashMap
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
name|function
operator|.
name|Function
import|;
end_import

begin_class
DECL|class|AzureStorageSettings
specifier|public
specifier|final
class|class
name|AzureStorageSettings
block|{
DECL|field|TIMEOUT_SUFFIX
specifier|private
specifier|static
specifier|final
name|String
name|TIMEOUT_SUFFIX
init|=
literal|"timeout"
decl_stmt|;
DECL|field|ACCOUNT_SUFFIX
specifier|private
specifier|static
specifier|final
name|String
name|ACCOUNT_SUFFIX
init|=
literal|"account"
decl_stmt|;
DECL|field|KEY_SUFFIX
specifier|private
specifier|static
specifier|final
name|String
name|KEY_SUFFIX
init|=
literal|"key"
decl_stmt|;
DECL|field|DEFAULT_SUFFIX
specifier|private
specifier|static
specifier|final
name|String
name|DEFAULT_SUFFIX
init|=
literal|"default"
decl_stmt|;
DECL|field|TIMEOUT_KEY
specifier|private
specifier|static
specifier|final
name|Setting
operator|.
name|AffixKey
name|TIMEOUT_KEY
init|=
name|Setting
operator|.
name|AffixKey
operator|.
name|withAdfix
argument_list|(
name|Storage
operator|.
name|PREFIX
argument_list|,
name|TIMEOUT_SUFFIX
argument_list|)
decl_stmt|;
DECL|field|TIMEOUT_SETTING
specifier|private
specifier|static
specifier|final
name|Setting
argument_list|<
name|TimeValue
argument_list|>
name|TIMEOUT_SETTING
init|=
name|Setting
operator|.
name|affixKeySetting
argument_list|(
name|TIMEOUT_KEY
argument_list|,
parameter_list|(
name|s
parameter_list|)
lambda|->
name|Storage
operator|.
name|TIMEOUT_SETTING
operator|.
name|get
argument_list|(
name|s
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
parameter_list|(
name|s
parameter_list|)
lambda|->
name|Setting
operator|.
name|parseTimeValue
argument_list|(
name|s
argument_list|,
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|,
name|TIMEOUT_KEY
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|,
literal|false
argument_list|,
name|Setting
operator|.
name|Scope
operator|.
name|CLUSTER
argument_list|)
decl_stmt|;
DECL|field|ACCOUNT_SETTING
specifier|private
specifier|static
specifier|final
name|Setting
argument_list|<
name|String
argument_list|>
name|ACCOUNT_SETTING
init|=
name|Setting
operator|.
name|adfixKeySetting
argument_list|(
name|Storage
operator|.
name|PREFIX
argument_list|,
name|ACCOUNT_SUFFIX
argument_list|,
literal|""
argument_list|,
name|Function
operator|.
name|identity
argument_list|()
argument_list|,
literal|false
argument_list|,
name|Setting
operator|.
name|Scope
operator|.
name|CLUSTER
argument_list|)
decl_stmt|;
DECL|field|KEY_SETTING
specifier|private
specifier|static
specifier|final
name|Setting
argument_list|<
name|String
argument_list|>
name|KEY_SETTING
init|=
name|Setting
operator|.
name|adfixKeySetting
argument_list|(
name|Storage
operator|.
name|PREFIX
argument_list|,
name|KEY_SUFFIX
argument_list|,
literal|""
argument_list|,
name|Function
operator|.
name|identity
argument_list|()
argument_list|,
literal|false
argument_list|,
name|Setting
operator|.
name|Scope
operator|.
name|CLUSTER
argument_list|)
decl_stmt|;
DECL|field|DEFAULT_SETTING
specifier|private
specifier|static
specifier|final
name|Setting
argument_list|<
name|Boolean
argument_list|>
name|DEFAULT_SETTING
init|=
name|Setting
operator|.
name|adfixKeySetting
argument_list|(
name|Storage
operator|.
name|PREFIX
argument_list|,
name|DEFAULT_SUFFIX
argument_list|,
literal|"false"
argument_list|,
name|Boolean
operator|::
name|valueOf
argument_list|,
literal|false
argument_list|,
name|Setting
operator|.
name|Scope
operator|.
name|CLUSTER
argument_list|)
decl_stmt|;
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|account
specifier|private
specifier|final
name|String
name|account
decl_stmt|;
DECL|field|key
specifier|private
specifier|final
name|String
name|key
decl_stmt|;
DECL|field|timeout
specifier|private
specifier|final
name|TimeValue
name|timeout
decl_stmt|;
DECL|field|activeByDefault
specifier|private
specifier|final
name|boolean
name|activeByDefault
decl_stmt|;
DECL|method|AzureStorageSettings
specifier|public
name|AzureStorageSettings
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|account
parameter_list|,
name|String
name|key
parameter_list|,
name|TimeValue
name|timeout
parameter_list|,
name|boolean
name|activeByDefault
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|account
operator|=
name|account
expr_stmt|;
name|this
operator|.
name|key
operator|=
name|key
expr_stmt|;
name|this
operator|.
name|timeout
operator|=
name|timeout
expr_stmt|;
name|this
operator|.
name|activeByDefault
operator|=
name|activeByDefault
expr_stmt|;
block|}
DECL|method|getName
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
DECL|method|getKey
specifier|public
name|String
name|getKey
parameter_list|()
block|{
return|return
name|key
return|;
block|}
DECL|method|getAccount
specifier|public
name|String
name|getAccount
parameter_list|()
block|{
return|return
name|account
return|;
block|}
DECL|method|getTimeout
specifier|public
name|TimeValue
name|getTimeout
parameter_list|()
block|{
return|return
name|timeout
return|;
block|}
DECL|method|isActiveByDefault
specifier|public
name|boolean
name|isActiveByDefault
parameter_list|()
block|{
return|return
name|activeByDefault
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
specifier|final
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"AzureStorageSettings{"
argument_list|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"name='"
argument_list|)
operator|.
name|append
argument_list|(
name|name
argument_list|)
operator|.
name|append
argument_list|(
literal|'\''
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", account='"
argument_list|)
operator|.
name|append
argument_list|(
name|account
argument_list|)
operator|.
name|append
argument_list|(
literal|'\''
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", key='"
argument_list|)
operator|.
name|append
argument_list|(
name|key
argument_list|)
operator|.
name|append
argument_list|(
literal|'\''
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", activeByDefault='"
argument_list|)
operator|.
name|append
argument_list|(
name|activeByDefault
argument_list|)
operator|.
name|append
argument_list|(
literal|'\''
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", timeout="
argument_list|)
operator|.
name|append
argument_list|(
name|timeout
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|'}'
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**      * Parses settings and read all settings available under cloud.azure.storage.*      * @param settings settings to parse      * @return A tuple with v1 = primary storage and v2 = secondary storage      */
DECL|method|parse
specifier|public
specifier|static
name|Tuple
argument_list|<
name|AzureStorageSettings
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|AzureStorageSettings
argument_list|>
argument_list|>
name|parse
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|List
argument_list|<
name|AzureStorageSettings
argument_list|>
name|storageSettings
init|=
name|createStorageSettings
argument_list|(
name|settings
argument_list|)
decl_stmt|;
return|return
name|Tuple
operator|.
name|tuple
argument_list|(
name|getPrimary
argument_list|(
name|storageSettings
argument_list|)
argument_list|,
name|getSecondaries
argument_list|(
name|storageSettings
argument_list|)
argument_list|)
return|;
block|}
DECL|method|createStorageSettings
specifier|private
specifier|static
name|List
argument_list|<
name|AzureStorageSettings
argument_list|>
name|createStorageSettings
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|Setting
argument_list|<
name|Settings
argument_list|>
name|storageGroupSetting
init|=
name|Setting
operator|.
name|groupSetting
argument_list|(
name|Storage
operator|.
name|PREFIX
argument_list|,
literal|false
argument_list|,
name|Setting
operator|.
name|Scope
operator|.
name|CLUSTER
argument_list|)
decl_stmt|;
comment|// ignore global timeout which has the same prefix but does not belong to any group
name|Settings
name|groups
init|=
name|storageGroupSetting
operator|.
name|get
argument_list|(
name|settings
operator|.
name|filter
argument_list|(
parameter_list|(
name|k
parameter_list|)
lambda|->
name|k
operator|.
name|equals
argument_list|(
name|Storage
operator|.
name|TIMEOUT_SETTING
operator|.
name|getKey
argument_list|()
argument_list|)
operator|==
literal|false
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|AzureStorageSettings
argument_list|>
name|storageSettings
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|groupName
range|:
name|groups
operator|.
name|getAsGroups
argument_list|()
operator|.
name|keySet
argument_list|()
control|)
block|{
name|storageSettings
operator|.
name|add
argument_list|(
operator|new
name|AzureStorageSettings
argument_list|(
name|groupName
argument_list|,
name|getValue
argument_list|(
name|settings
argument_list|,
name|groupName
argument_list|,
name|ACCOUNT_SETTING
argument_list|)
argument_list|,
name|getValue
argument_list|(
name|settings
argument_list|,
name|groupName
argument_list|,
name|KEY_SETTING
argument_list|)
argument_list|,
name|getValue
argument_list|(
name|settings
argument_list|,
name|groupName
argument_list|,
name|TIMEOUT_SETTING
argument_list|)
argument_list|,
name|getValue
argument_list|(
name|settings
argument_list|,
name|groupName
argument_list|,
name|DEFAULT_SETTING
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|storageSettings
return|;
block|}
DECL|method|getValue
specifier|private
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|getValue
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|String
name|groupName
parameter_list|,
name|Setting
argument_list|<
name|T
argument_list|>
name|setting
parameter_list|)
block|{
name|Setting
operator|.
name|AffixKey
name|k
init|=
operator|(
name|Setting
operator|.
name|AffixKey
operator|)
name|setting
operator|.
name|getRawKey
argument_list|()
decl_stmt|;
name|String
name|fullKey
init|=
name|k
operator|.
name|toConcreteKey
argument_list|(
name|groupName
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
return|return
name|setting
operator|.
name|getConcreteSetting
argument_list|(
name|fullKey
argument_list|)
operator|.
name|get
argument_list|(
name|settings
argument_list|)
return|;
block|}
DECL|method|getPrimary
specifier|private
specifier|static
name|AzureStorageSettings
name|getPrimary
parameter_list|(
name|List
argument_list|<
name|AzureStorageSettings
argument_list|>
name|settings
parameter_list|)
block|{
if|if
condition|(
name|settings
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
elseif|else
if|if
condition|(
name|settings
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
comment|// the only storage settings belong (implicitly) to the default primary storage
name|AzureStorageSettings
name|storage
init|=
name|settings
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
return|return
operator|new
name|AzureStorageSettings
argument_list|(
name|storage
operator|.
name|getName
argument_list|()
argument_list|,
name|storage
operator|.
name|getAccount
argument_list|()
argument_list|,
name|storage
operator|.
name|getKey
argument_list|()
argument_list|,
name|storage
operator|.
name|getTimeout
argument_list|()
argument_list|,
literal|true
argument_list|)
return|;
block|}
else|else
block|{
name|AzureStorageSettings
name|primary
init|=
literal|null
decl_stmt|;
for|for
control|(
name|AzureStorageSettings
name|setting
range|:
name|settings
control|)
block|{
if|if
condition|(
name|setting
operator|.
name|isActiveByDefault
argument_list|()
condition|)
block|{
if|if
condition|(
name|primary
operator|==
literal|null
condition|)
block|{
name|primary
operator|=
name|setting
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|SettingsException
argument_list|(
literal|"Multiple default Azure data stores configured: ["
operator|+
name|primary
operator|.
name|getName
argument_list|()
operator|+
literal|"] and ["
operator|+
name|setting
operator|.
name|getName
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
if|if
condition|(
name|primary
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SettingsException
argument_list|(
literal|"No default Azure data store configured"
argument_list|)
throw|;
block|}
return|return
name|primary
return|;
block|}
block|}
DECL|method|getSecondaries
specifier|private
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|AzureStorageSettings
argument_list|>
name|getSecondaries
parameter_list|(
name|List
argument_list|<
name|AzureStorageSettings
argument_list|>
name|settings
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|AzureStorageSettings
argument_list|>
name|secondaries
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|// when only one setting is defined, we don't have secondaries
if|if
condition|(
name|settings
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
block|{
for|for
control|(
name|AzureStorageSettings
name|setting
range|:
name|settings
control|)
block|{
if|if
condition|(
name|setting
operator|.
name|isActiveByDefault
argument_list|()
operator|==
literal|false
condition|)
block|{
name|secondaries
operator|.
name|put
argument_list|(
name|setting
operator|.
name|getName
argument_list|()
argument_list|,
name|setting
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|secondaries
argument_list|)
return|;
block|}
DECL|method|getValue
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|getValue
parameter_list|(
name|RepositorySettings
name|repositorySettings
parameter_list|,
name|Setting
argument_list|<
name|T
argument_list|>
name|repositorySetting
parameter_list|,
name|Setting
argument_list|<
name|T
argument_list|>
name|repositoriesSetting
parameter_list|)
block|{
if|if
condition|(
name|repositorySetting
operator|.
name|exists
argument_list|(
name|repositorySettings
operator|.
name|settings
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|repositorySetting
operator|.
name|get
argument_list|(
name|repositorySettings
operator|.
name|settings
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|repositoriesSetting
operator|.
name|get
argument_list|(
name|repositorySettings
operator|.
name|globalSettings
argument_list|()
argument_list|)
return|;
block|}
block|}
DECL|method|getEffectiveSetting
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|Setting
argument_list|<
name|T
argument_list|>
name|getEffectiveSetting
parameter_list|(
name|RepositorySettings
name|repositorySettings
parameter_list|,
name|Setting
argument_list|<
name|T
argument_list|>
name|repositorySetting
parameter_list|,
name|Setting
argument_list|<
name|T
argument_list|>
name|repositoriesSetting
parameter_list|)
block|{
if|if
condition|(
name|repositorySetting
operator|.
name|exists
argument_list|(
name|repositorySettings
operator|.
name|settings
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|repositorySetting
return|;
block|}
else|else
block|{
return|return
name|repositoriesSetting
return|;
block|}
block|}
block|}
end_class

end_unit

