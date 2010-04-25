begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.get
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|get
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|guice
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
name|apache
operator|.
name|lucene
operator|.
name|document
operator|.
name|Document
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|document
operator|.
name|FieldSelector
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|document
operator|.
name|Fieldable
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
name|action
operator|.
name|TransportActions
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|single
operator|.
name|TransportSingleOperationAction
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
name|index
operator|.
name|engine
operator|.
name|Engine
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|*
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|service
operator|.
name|IndexService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|shard
operator|.
name|service
operator|.
name|IndexShard
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
name|IndicesService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
operator|.
name|ThreadPool
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
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|lucene
operator|.
name|Lucene
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|settings
operator|.
name|Settings
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
name|Map
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|gcommon
operator|.
name|collect
operator|.
name|Maps
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * Performs the get operation.  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|TransportGetAction
specifier|public
class|class
name|TransportGetAction
extends|extends
name|TransportSingleOperationAction
argument_list|<
name|GetRequest
argument_list|,
name|GetResponse
argument_list|>
block|{
DECL|method|TransportGetAction
annotation|@
name|Inject
specifier|public
name|TransportGetAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|IndicesService
name|indicesService
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|,
name|clusterService
argument_list|,
name|transportService
argument_list|,
name|indicesService
argument_list|)
expr_stmt|;
block|}
DECL|method|transportAction
annotation|@
name|Override
specifier|protected
name|String
name|transportAction
parameter_list|()
block|{
return|return
name|TransportActions
operator|.
name|GET
return|;
block|}
DECL|method|transportShardAction
annotation|@
name|Override
specifier|protected
name|String
name|transportShardAction
parameter_list|()
block|{
return|return
literal|"indices/get/shard"
return|;
block|}
DECL|method|shardOperation
annotation|@
name|Override
specifier|protected
name|GetResponse
name|shardOperation
parameter_list|(
name|GetRequest
name|request
parameter_list|,
name|int
name|shardId
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|IndexService
name|indexService
init|=
name|indicesService
operator|.
name|indexServiceSafe
argument_list|(
name|request
operator|.
name|index
argument_list|()
argument_list|)
decl_stmt|;
name|IndexShard
name|indexShard
init|=
name|indexService
operator|.
name|shardSafe
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
name|DocumentMapper
name|docMapper
init|=
name|indexService
operator|.
name|mapperService
argument_list|()
operator|.
name|type
argument_list|(
name|request
operator|.
name|type
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|docMapper
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|DocumentMapperNotFoundException
argument_list|(
literal|"No mapper found for type ["
operator|+
name|request
operator|.
name|type
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|Engine
operator|.
name|Searcher
name|searcher
init|=
name|indexShard
operator|.
name|searcher
argument_list|()
decl_stmt|;
name|boolean
name|exists
init|=
literal|false
decl_stmt|;
name|byte
index|[]
name|source
init|=
literal|null
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|GetField
argument_list|>
name|fields
init|=
literal|null
decl_stmt|;
try|try
block|{
name|int
name|docId
init|=
name|Lucene
operator|.
name|docId
argument_list|(
name|searcher
operator|.
name|reader
argument_list|()
argument_list|,
name|docMapper
operator|.
name|uidMapper
argument_list|()
operator|.
name|term
argument_list|(
name|request
operator|.
name|type
argument_list|()
argument_list|,
name|request
operator|.
name|id
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|docId
operator|!=
name|Lucene
operator|.
name|NO_DOC
condition|)
block|{
name|exists
operator|=
literal|true
expr_stmt|;
name|FieldSelector
name|fieldSelector
init|=
name|buildFieldSelectors
argument_list|(
name|docMapper
argument_list|,
name|request
operator|.
name|fields
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldSelector
operator|!=
literal|null
condition|)
block|{
name|Document
name|doc
init|=
name|searcher
operator|.
name|reader
argument_list|()
operator|.
name|document
argument_list|(
name|docId
argument_list|,
name|fieldSelector
argument_list|)
decl_stmt|;
name|source
operator|=
name|extractSource
argument_list|(
name|doc
argument_list|,
name|docMapper
argument_list|)
expr_stmt|;
for|for
control|(
name|Object
name|oField
range|:
name|doc
operator|.
name|getFields
argument_list|()
control|)
block|{
name|Fieldable
name|field
init|=
operator|(
name|Fieldable
operator|)
name|oField
decl_stmt|;
name|String
name|name
init|=
name|field
operator|.
name|name
argument_list|()
decl_stmt|;
name|Object
name|value
init|=
literal|null
decl_stmt|;
name|FieldMappers
name|fieldMappers
init|=
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|indexName
argument_list|(
name|field
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldMappers
operator|!=
literal|null
condition|)
block|{
name|FieldMapper
name|mapper
init|=
name|fieldMappers
operator|.
name|mapper
argument_list|()
decl_stmt|;
if|if
condition|(
name|mapper
operator|!=
literal|null
condition|)
block|{
name|name
operator|=
name|mapper
operator|.
name|names
argument_list|()
operator|.
name|fullName
argument_list|()
expr_stmt|;
name|value
operator|=
name|mapper
operator|.
name|valueForSearch
argument_list|(
name|field
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|field
operator|.
name|isBinary
argument_list|()
condition|)
block|{
name|value
operator|=
name|field
operator|.
name|getBinaryValue
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|value
operator|=
name|field
operator|.
name|stringValue
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|fields
operator|==
literal|null
condition|)
block|{
name|fields
operator|=
name|newHashMapWithExpectedSize
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
name|GetField
name|getField
init|=
name|fields
operator|.
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|getField
operator|==
literal|null
condition|)
block|{
name|getField
operator|=
operator|new
name|GetField
argument_list|(
name|name
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|fields
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|getField
argument_list|)
expr_stmt|;
block|}
name|getField
operator|.
name|values
argument_list|()
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchException
argument_list|(
literal|"Failed to get type ["
operator|+
name|request
operator|.
name|type
argument_list|()
operator|+
literal|"] and id ["
operator|+
name|request
operator|.
name|id
argument_list|()
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|searcher
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|GetResponse
argument_list|(
name|request
operator|.
name|index
argument_list|()
argument_list|,
name|request
operator|.
name|type
argument_list|()
argument_list|,
name|request
operator|.
name|id
argument_list|()
argument_list|,
name|exists
argument_list|,
name|source
argument_list|,
name|fields
argument_list|)
return|;
block|}
DECL|method|buildFieldSelectors
specifier|private
name|FieldSelector
name|buildFieldSelectors
parameter_list|(
name|DocumentMapper
name|docMapper
parameter_list|,
name|String
modifier|...
name|fields
parameter_list|)
block|{
if|if
condition|(
name|fields
operator|==
literal|null
condition|)
block|{
return|return
name|docMapper
operator|.
name|sourceMapper
argument_list|()
operator|.
name|fieldSelector
argument_list|()
return|;
block|}
comment|// don't load anything
if|if
condition|(
name|fields
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
name|FieldMappersFieldSelector
name|fieldSelector
init|=
operator|new
name|FieldMappersFieldSelector
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|fieldName
range|:
name|fields
control|)
block|{
name|FieldMappers
name|x
init|=
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|smartName
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|x
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchException
argument_list|(
literal|"No mapping for field ["
operator|+
name|fieldName
operator|+
literal|"] in type ["
operator|+
name|docMapper
operator|.
name|type
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|fieldSelector
operator|.
name|add
argument_list|(
name|x
argument_list|)
expr_stmt|;
block|}
return|return
name|fieldSelector
return|;
block|}
DECL|method|extractSource
specifier|private
name|byte
index|[]
name|extractSource
parameter_list|(
name|Document
name|doc
parameter_list|,
name|DocumentMapper
name|documentMapper
parameter_list|)
block|{
name|byte
index|[]
name|source
init|=
literal|null
decl_stmt|;
name|Fieldable
name|sourceField
init|=
name|doc
operator|.
name|getFieldable
argument_list|(
name|documentMapper
operator|.
name|sourceMapper
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|sourceField
operator|!=
literal|null
condition|)
block|{
name|source
operator|=
name|documentMapper
operator|.
name|sourceMapper
argument_list|()
operator|.
name|value
argument_list|(
name|sourceField
argument_list|)
expr_stmt|;
name|doc
operator|.
name|removeField
argument_list|(
name|documentMapper
operator|.
name|sourceMapper
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|source
return|;
block|}
DECL|method|newRequest
annotation|@
name|Override
specifier|protected
name|GetRequest
name|newRequest
parameter_list|()
block|{
return|return
operator|new
name|GetRequest
argument_list|()
return|;
block|}
DECL|method|newResponse
annotation|@
name|Override
specifier|protected
name|GetResponse
name|newResponse
parameter_list|()
block|{
return|return
operator|new
name|GetResponse
argument_list|()
return|;
block|}
block|}
end_class

end_unit

