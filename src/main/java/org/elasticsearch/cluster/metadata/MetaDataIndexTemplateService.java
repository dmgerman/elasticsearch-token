begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.metadata
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalArgumentException
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
name|ClusterService
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
name|ClusterState
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
name|ProcessedClusterStateUpdateTask
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
name|Strings
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
name|ImmutableSettings
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
name|indices
operator|.
name|IndexTemplateAlreadyExistsException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|IndexTemplateMissingException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|InvalidIndexTemplateException
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|MetaDataIndexTemplateService
specifier|public
class|class
name|MetaDataIndexTemplateService
extends|extends
name|AbstractComponent
block|{
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
annotation|@
name|Inject
DECL|method|MetaDataIndexTemplateService
specifier|public
name|MetaDataIndexTemplateService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|clusterService
operator|=
name|clusterService
expr_stmt|;
block|}
DECL|method|removeTemplate
specifier|public
name|void
name|removeTemplate
parameter_list|(
specifier|final
name|RemoveRequest
name|request
parameter_list|,
specifier|final
name|RemoveListener
name|listener
parameter_list|)
block|{
name|clusterService
operator|.
name|submitStateUpdateTask
argument_list|(
literal|"remove-index-template ["
operator|+
name|request
operator|.
name|name
operator|+
literal|"]"
argument_list|,
operator|new
name|ProcessedClusterStateUpdateTask
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|ClusterState
name|execute
parameter_list|(
name|ClusterState
name|currentState
parameter_list|)
block|{
if|if
condition|(
operator|!
name|currentState
operator|.
name|metaData
argument_list|()
operator|.
name|templates
argument_list|()
operator|.
name|containsKey
argument_list|(
name|request
operator|.
name|name
argument_list|)
condition|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
operator|new
name|IndexTemplateMissingException
argument_list|(
name|request
operator|.
name|name
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|currentState
return|;
block|}
name|MetaData
operator|.
name|Builder
name|metaData
init|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|metaData
argument_list|(
name|currentState
operator|.
name|metaData
argument_list|()
argument_list|)
operator|.
name|removeTemplate
argument_list|(
name|request
operator|.
name|name
argument_list|)
decl_stmt|;
return|return
name|ClusterState
operator|.
name|builder
argument_list|()
operator|.
name|state
argument_list|(
name|currentState
argument_list|)
operator|.
name|metaData
argument_list|(
name|metaData
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|clusterStateProcessed
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|RemoveResponse
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|putTemplate
specifier|public
name|void
name|putTemplate
parameter_list|(
specifier|final
name|PutRequest
name|request
parameter_list|,
specifier|final
name|PutListener
name|listener
parameter_list|)
block|{
name|ImmutableSettings
operator|.
name|Builder
name|updatedSettingsBuilder
init|=
name|ImmutableSettings
operator|.
name|settingsBuilder
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
name|String
argument_list|>
name|entry
range|:
name|request
operator|.
name|settings
operator|.
name|getAsMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"index."
argument_list|)
condition|)
block|{
name|updatedSettingsBuilder
operator|.
name|put
argument_list|(
literal|"index."
operator|+
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|updatedSettingsBuilder
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|request
operator|.
name|settings
argument_list|(
name|updatedSettingsBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|request
operator|.
name|name
operator|==
literal|null
condition|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"index_template must provide a name"
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|request
operator|.
name|template
operator|==
literal|null
condition|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"index_template must provide a template"
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
try|try
block|{
name|validate
argument_list|(
name|request
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
return|return;
block|}
name|IndexTemplateMetaData
operator|.
name|Builder
name|templateBuilder
decl_stmt|;
try|try
block|{
name|templateBuilder
operator|=
name|IndexTemplateMetaData
operator|.
name|builder
argument_list|(
name|request
operator|.
name|name
argument_list|)
expr_stmt|;
name|templateBuilder
operator|.
name|order
argument_list|(
name|request
operator|.
name|order
argument_list|)
expr_stmt|;
name|templateBuilder
operator|.
name|template
argument_list|(
name|request
operator|.
name|template
argument_list|)
expr_stmt|;
name|templateBuilder
operator|.
name|settings
argument_list|(
name|request
operator|.
name|settings
argument_list|)
expr_stmt|;
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
name|entry
range|:
name|request
operator|.
name|mappings
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|templateBuilder
operator|.
name|putMapping
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
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
name|listener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
return|return;
block|}
specifier|final
name|IndexTemplateMetaData
name|template
init|=
name|templateBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|clusterService
operator|.
name|submitStateUpdateTask
argument_list|(
literal|"create-index-template ["
operator|+
name|request
operator|.
name|name
operator|+
literal|"], cause ["
operator|+
name|request
operator|.
name|cause
operator|+
literal|"]"
argument_list|,
operator|new
name|ProcessedClusterStateUpdateTask
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|ClusterState
name|execute
parameter_list|(
name|ClusterState
name|currentState
parameter_list|)
block|{
if|if
condition|(
name|request
operator|.
name|create
operator|&&
name|currentState
operator|.
name|metaData
argument_list|()
operator|.
name|templates
argument_list|()
operator|.
name|containsKey
argument_list|(
name|request
operator|.
name|name
argument_list|)
condition|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
operator|new
name|IndexTemplateAlreadyExistsException
argument_list|(
name|request
operator|.
name|name
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|currentState
return|;
block|}
name|MetaData
operator|.
name|Builder
name|builder
init|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|metaData
argument_list|(
name|currentState
operator|.
name|metaData
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|template
argument_list|)
decl_stmt|;
return|return
name|ClusterState
operator|.
name|builder
argument_list|()
operator|.
name|state
argument_list|(
name|currentState
argument_list|)
operator|.
name|metaData
argument_list|(
name|builder
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|clusterStateProcessed
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|PutResponse
argument_list|(
literal|true
argument_list|,
name|template
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|validate
specifier|private
name|void
name|validate
parameter_list|(
name|PutRequest
name|request
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
if|if
condition|(
name|request
operator|.
name|name
operator|.
name|contains
argument_list|(
literal|" "
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|InvalidIndexTemplateException
argument_list|(
name|request
operator|.
name|name
argument_list|,
literal|"name must not contain a space"
argument_list|)
throw|;
block|}
if|if
condition|(
name|request
operator|.
name|name
operator|.
name|contains
argument_list|(
literal|","
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|InvalidIndexTemplateException
argument_list|(
name|request
operator|.
name|name
argument_list|,
literal|"name must not contain a ','"
argument_list|)
throw|;
block|}
if|if
condition|(
name|request
operator|.
name|name
operator|.
name|contains
argument_list|(
literal|"#"
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|InvalidIndexTemplateException
argument_list|(
name|request
operator|.
name|name
argument_list|,
literal|"name must not contain a '#'"
argument_list|)
throw|;
block|}
if|if
condition|(
name|request
operator|.
name|name
operator|.
name|startsWith
argument_list|(
literal|"_"
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|InvalidIndexTemplateException
argument_list|(
name|request
operator|.
name|name
argument_list|,
literal|"name must not start with '_'"
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|request
operator|.
name|name
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
name|request
operator|.
name|name
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|InvalidIndexTemplateException
argument_list|(
name|request
operator|.
name|name
argument_list|,
literal|"name must be lower cased"
argument_list|)
throw|;
block|}
if|if
condition|(
name|request
operator|.
name|template
operator|.
name|contains
argument_list|(
literal|" "
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|InvalidIndexTemplateException
argument_list|(
name|request
operator|.
name|name
argument_list|,
literal|"template must not contain a space"
argument_list|)
throw|;
block|}
if|if
condition|(
name|request
operator|.
name|template
operator|.
name|contains
argument_list|(
literal|","
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|InvalidIndexTemplateException
argument_list|(
name|request
operator|.
name|name
argument_list|,
literal|"template must not contain a ','"
argument_list|)
throw|;
block|}
if|if
condition|(
name|request
operator|.
name|template
operator|.
name|contains
argument_list|(
literal|"#"
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|InvalidIndexTemplateException
argument_list|(
name|request
operator|.
name|name
argument_list|,
literal|"template must not contain a '#'"
argument_list|)
throw|;
block|}
if|if
condition|(
name|request
operator|.
name|template
operator|.
name|startsWith
argument_list|(
literal|"_"
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|InvalidIndexTemplateException
argument_list|(
name|request
operator|.
name|name
argument_list|,
literal|"template must not start with '_'"
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|Strings
operator|.
name|validFileNameExcludingAstrix
argument_list|(
name|request
operator|.
name|template
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|InvalidIndexTemplateException
argument_list|(
name|request
operator|.
name|name
argument_list|,
literal|"template must not container the following characters "
operator|+
name|Strings
operator|.
name|INVALID_FILENAME_CHARS
argument_list|)
throw|;
block|}
block|}
DECL|interface|PutListener
specifier|public
specifier|static
interface|interface
name|PutListener
block|{
DECL|method|onResponse
name|void
name|onResponse
parameter_list|(
name|PutResponse
name|response
parameter_list|)
function_decl|;
DECL|method|onFailure
name|void
name|onFailure
parameter_list|(
name|Throwable
name|t
parameter_list|)
function_decl|;
block|}
DECL|class|PutRequest
specifier|public
specifier|static
class|class
name|PutRequest
block|{
DECL|field|name
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|cause
specifier|final
name|String
name|cause
decl_stmt|;
DECL|field|create
name|boolean
name|create
decl_stmt|;
DECL|field|order
name|int
name|order
decl_stmt|;
DECL|field|template
name|String
name|template
decl_stmt|;
DECL|field|settings
name|Settings
name|settings
init|=
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
decl_stmt|;
DECL|field|mappings
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|mappings
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
DECL|method|PutRequest
specifier|public
name|PutRequest
parameter_list|(
name|String
name|cause
parameter_list|,
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|cause
operator|=
name|cause
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
DECL|method|order
specifier|public
name|PutRequest
name|order
parameter_list|(
name|int
name|order
parameter_list|)
block|{
name|this
operator|.
name|order
operator|=
name|order
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|template
specifier|public
name|PutRequest
name|template
parameter_list|(
name|String
name|template
parameter_list|)
block|{
name|this
operator|.
name|template
operator|=
name|template
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|create
specifier|public
name|PutRequest
name|create
parameter_list|(
name|boolean
name|create
parameter_list|)
block|{
name|this
operator|.
name|create
operator|=
name|create
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|settings
specifier|public
name|PutRequest
name|settings
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|this
operator|.
name|settings
operator|=
name|settings
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|mappings
specifier|public
name|PutRequest
name|mappings
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|mappings
parameter_list|)
block|{
name|this
operator|.
name|mappings
operator|.
name|putAll
argument_list|(
name|mappings
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|putMapping
specifier|public
name|PutRequest
name|putMapping
parameter_list|(
name|String
name|mappingType
parameter_list|,
name|String
name|mappingSource
parameter_list|)
block|{
name|mappings
operator|.
name|put
argument_list|(
name|mappingType
argument_list|,
name|mappingSource
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
block|}
DECL|class|PutResponse
specifier|public
specifier|static
class|class
name|PutResponse
block|{
DECL|field|acknowledged
specifier|private
specifier|final
name|boolean
name|acknowledged
decl_stmt|;
DECL|field|template
specifier|private
specifier|final
name|IndexTemplateMetaData
name|template
decl_stmt|;
DECL|method|PutResponse
specifier|public
name|PutResponse
parameter_list|(
name|boolean
name|acknowledged
parameter_list|,
name|IndexTemplateMetaData
name|template
parameter_list|)
block|{
name|this
operator|.
name|acknowledged
operator|=
name|acknowledged
expr_stmt|;
name|this
operator|.
name|template
operator|=
name|template
expr_stmt|;
block|}
DECL|method|acknowledged
specifier|public
name|boolean
name|acknowledged
parameter_list|()
block|{
return|return
name|acknowledged
return|;
block|}
DECL|method|template
specifier|public
name|IndexTemplateMetaData
name|template
parameter_list|()
block|{
return|return
name|template
return|;
block|}
block|}
DECL|class|RemoveRequest
specifier|public
specifier|static
class|class
name|RemoveRequest
block|{
DECL|field|name
specifier|final
name|String
name|name
decl_stmt|;
DECL|method|RemoveRequest
specifier|public
name|RemoveRequest
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
block|}
DECL|class|RemoveResponse
specifier|public
specifier|static
class|class
name|RemoveResponse
block|{
DECL|field|acknowledged
specifier|private
specifier|final
name|boolean
name|acknowledged
decl_stmt|;
DECL|method|RemoveResponse
specifier|public
name|RemoveResponse
parameter_list|(
name|boolean
name|acknowledged
parameter_list|)
block|{
name|this
operator|.
name|acknowledged
operator|=
name|acknowledged
expr_stmt|;
block|}
DECL|method|acknowledged
specifier|public
name|boolean
name|acknowledged
parameter_list|()
block|{
return|return
name|acknowledged
return|;
block|}
block|}
DECL|interface|RemoveListener
specifier|public
specifier|static
interface|interface
name|RemoveListener
block|{
DECL|method|onResponse
name|void
name|onResponse
parameter_list|(
name|RemoveResponse
name|response
parameter_list|)
function_decl|;
DECL|method|onFailure
name|void
name|onFailure
parameter_list|(
name|Throwable
name|t
parameter_list|)
function_decl|;
block|}
block|}
end_class

end_unit

