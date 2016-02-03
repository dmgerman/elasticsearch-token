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
name|ESLoggerFactory
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
name|HashMap
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

begin_class
DECL|class|AzureStorageSettings
specifier|public
class|class
name|AzureStorageSettings
block|{
DECL|field|logger
specifier|private
specifier|static
name|ESLogger
name|logger
init|=
name|ESLoggerFactory
operator|.
name|getLogger
argument_list|(
name|AzureStorageSettings
operator|.
name|class
operator|.
name|getName
argument_list|()
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
name|AzureStorageSettings
name|primaryStorage
init|=
literal|null
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|AzureStorageSettings
argument_list|>
name|secondaryStorage
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|TimeValue
name|globalTimeout
init|=
name|Storage
operator|.
name|TIMEOUT_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|Settings
name|storageSettings
init|=
name|settings
operator|.
name|getByPrefix
argument_list|(
name|Storage
operator|.
name|PREFIX
argument_list|)
decl_stmt|;
if|if
condition|(
name|storageSettings
operator|!=
literal|null
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|asMap
init|=
name|storageSettings
operator|.
name|getAsStructuredMap
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|storage
range|:
name|asMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|storage
operator|.
name|getValue
argument_list|()
operator|instanceof
name|Map
condition|)
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
init|=
operator|(
name|Map
operator|)
name|storage
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|TimeValue
name|timeout
init|=
name|TimeValue
operator|.
name|parseTimeValue
argument_list|(
name|map
operator|.
name|get
argument_list|(
literal|"timeout"
argument_list|)
argument_list|,
name|globalTimeout
argument_list|,
name|Storage
operator|.
name|PREFIX
operator|+
name|storage
operator|.
name|getKey
argument_list|()
operator|+
literal|".timeout"
argument_list|)
decl_stmt|;
name|AzureStorageSettings
name|current
init|=
operator|new
name|AzureStorageSettings
argument_list|(
name|storage
operator|.
name|getKey
argument_list|()
argument_list|,
name|map
operator|.
name|get
argument_list|(
literal|"account"
argument_list|)
argument_list|,
name|map
operator|.
name|get
argument_list|(
literal|"key"
argument_list|)
argument_list|,
name|timeout
argument_list|)
decl_stmt|;
name|boolean
name|activeByDefault
init|=
name|Boolean
operator|.
name|parseBoolean
argument_list|(
name|map
operator|.
name|getOrDefault
argument_list|(
literal|"default"
argument_list|,
literal|"false"
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|activeByDefault
condition|)
block|{
if|if
condition|(
name|primaryStorage
operator|==
literal|null
condition|)
block|{
name|primaryStorage
operator|=
name|current
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"default storage settings has already been defined. You can not define it to [{}]"
argument_list|,
name|storage
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|secondaryStorage
operator|.
name|put
argument_list|(
name|storage
operator|.
name|getKey
argument_list|()
argument_list|,
name|current
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|secondaryStorage
operator|.
name|put
argument_list|(
name|storage
operator|.
name|getKey
argument_list|()
argument_list|,
name|current
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// If we did not set any default storage, we should complain and define it
if|if
condition|(
name|primaryStorage
operator|==
literal|null
operator|&&
name|secondaryStorage
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|AzureStorageSettings
argument_list|>
name|fallback
init|=
name|secondaryStorage
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
comment|// We only warn if the number of secondary storage if> to 1
comment|// If the user defined only one storage account, that's fine. We know it's the default one.
if|if
condition|(
name|secondaryStorage
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"no default storage settings has been defined. "
operator|+
literal|"Add \"default\": true to the settings you want to activate by default. "
operator|+
literal|"Forcing default to [{}]."
argument_list|,
name|fallback
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|primaryStorage
operator|=
name|fallback
operator|.
name|getValue
argument_list|()
expr_stmt|;
name|secondaryStorage
operator|.
name|remove
argument_list|(
name|fallback
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|Tuple
operator|.
name|tuple
argument_list|(
name|primaryStorage
argument_list|,
name|secondaryStorage
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

