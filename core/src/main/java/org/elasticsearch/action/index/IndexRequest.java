begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.index
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|index
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchGenerationException
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
name|action
operator|.
name|ActionRequestValidationException
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
name|DocWriteRequest
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
name|RoutingMissingException
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
name|replication
operator|.
name|ReplicatedWriteRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Requests
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
name|metadata
operator|.
name|MappingMetaData
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
name|metadata
operator|.
name|MetaData
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
name|Nullable
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
name|UUIDs
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
name|bytes
operator|.
name|BytesArray
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
name|bytes
operator|.
name|BytesReference
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|lucene
operator|.
name|uid
operator|.
name|Versions
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
name|xcontent
operator|.
name|XContentBuilder
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
name|xcontent
operator|.
name|XContentFactory
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
name|xcontent
operator|.
name|XContentHelper
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
name|xcontent
operator|.
name|XContentType
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
name|VersionType
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
name|charset
operator|.
name|StandardCharsets
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
name|Map
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|ValidateActions
operator|.
name|addValidationError
import|;
end_import

begin_comment
comment|/**  * Index request to index a typed JSON document into a specific index and make it searchable. Best  * created using {@link org.elasticsearch.client.Requests#indexRequest(String)}.  *  * The index requires the {@link #index()}, {@link #type(String)}, {@link #id(String)} and  * {@link #source(byte[])} to be set.  *  * The source (content to index) can be set in its bytes form using ({@link #source(byte[])}),  * its string form ({@link #source(String)}) or using a {@link org.elasticsearch.common.xcontent.XContentBuilder}  * ({@link #source(org.elasticsearch.common.xcontent.XContentBuilder)}).  *  * If the {@link #id(String)} is not set, it will be automatically generated.  *  * @see IndexResponse  * @see org.elasticsearch.client.Requests#indexRequest(String)  * @see org.elasticsearch.client.Client#index(IndexRequest)  */
end_comment

begin_class
DECL|class|IndexRequest
specifier|public
class|class
name|IndexRequest
extends|extends
name|ReplicatedWriteRequest
argument_list|<
name|IndexRequest
argument_list|>
implements|implements
name|DocWriteRequest
argument_list|<
name|IndexRequest
argument_list|>
block|{
DECL|field|type
specifier|private
name|String
name|type
decl_stmt|;
DECL|field|id
specifier|private
name|String
name|id
decl_stmt|;
annotation|@
name|Nullable
DECL|field|routing
specifier|private
name|String
name|routing
decl_stmt|;
annotation|@
name|Nullable
DECL|field|parent
specifier|private
name|String
name|parent
decl_stmt|;
DECL|field|source
specifier|private
name|BytesReference
name|source
decl_stmt|;
DECL|field|opType
specifier|private
name|OpType
name|opType
init|=
name|OpType
operator|.
name|INDEX
decl_stmt|;
DECL|field|version
specifier|private
name|long
name|version
init|=
name|Versions
operator|.
name|MATCH_ANY
decl_stmt|;
DECL|field|versionType
specifier|private
name|VersionType
name|versionType
init|=
name|VersionType
operator|.
name|INTERNAL
decl_stmt|;
DECL|field|contentType
specifier|private
name|XContentType
name|contentType
init|=
name|Requests
operator|.
name|INDEX_CONTENT_TYPE
decl_stmt|;
DECL|field|pipeline
specifier|private
name|String
name|pipeline
decl_stmt|;
comment|/**      * Value for {@link #getAutoGeneratedTimestamp()} if the document has an external      * provided ID.      */
DECL|field|UNSET_AUTO_GENERATED_TIMESTAMP
specifier|public
specifier|static
specifier|final
name|int
name|UNSET_AUTO_GENERATED_TIMESTAMP
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|autoGeneratedTimestamp
specifier|private
name|long
name|autoGeneratedTimestamp
init|=
name|UNSET_AUTO_GENERATED_TIMESTAMP
decl_stmt|;
DECL|field|isRetry
specifier|private
name|boolean
name|isRetry
init|=
literal|false
decl_stmt|;
DECL|method|IndexRequest
specifier|public
name|IndexRequest
parameter_list|()
block|{     }
comment|/**      * Constructs a new index request against the specific index. The {@link #type(String)}      * {@link #source(byte[])} must be set.      */
DECL|method|IndexRequest
specifier|public
name|IndexRequest
parameter_list|(
name|String
name|index
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
block|}
comment|/**      * Constructs a new index request against the specific index and type. The      * {@link #source(byte[])} must be set.      */
DECL|method|IndexRequest
specifier|public
name|IndexRequest
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|type
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
block|}
comment|/**      * Constructs a new index request against the index, type, id and using the source.      *      * @param index The index to index into      * @param type  The type to index into      * @param id    The id of document      */
DECL|method|IndexRequest
specifier|public
name|IndexRequest
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|validate
specifier|public
name|ActionRequestValidationException
name|validate
parameter_list|()
block|{
name|ActionRequestValidationException
name|validationException
init|=
name|super
operator|.
name|validate
argument_list|()
decl_stmt|;
if|if
condition|(
name|type
operator|==
literal|null
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"type is missing"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|source
operator|==
literal|null
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"source is missing"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|opType
argument_list|()
operator|==
name|OpType
operator|.
name|CREATE
condition|)
block|{
if|if
condition|(
name|versionType
operator|!=
name|VersionType
operator|.
name|INTERNAL
operator|||
name|version
operator|!=
name|Versions
operator|.
name|MATCH_DELETED
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"create operations do not support versioning. use index instead"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
return|return
name|validationException
return|;
block|}
block|}
if|if
condition|(
name|opType
argument_list|()
operator|!=
name|OpType
operator|.
name|INDEX
operator|&&
name|id
operator|==
literal|null
condition|)
block|{
name|addValidationError
argument_list|(
literal|"an id is required for a "
operator|+
name|opType
argument_list|()
operator|+
literal|" operation"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|versionType
operator|.
name|validateVersionForWrites
argument_list|(
name|version
argument_list|)
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"illegal version value ["
operator|+
name|version
operator|+
literal|"] for version type ["
operator|+
name|versionType
operator|.
name|name
argument_list|()
operator|+
literal|"]"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|versionType
operator|==
name|VersionType
operator|.
name|FORCE
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"version type [force] may no longer be used"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|id
operator|!=
literal|null
operator|&&
name|id
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
operator|.
name|length
operator|>
literal|512
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"id is too long, must be no longer than 512 bytes but was: "
operator|+
name|id
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
operator|.
name|length
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|id
operator|==
literal|null
operator|&&
operator|(
name|versionType
operator|==
name|VersionType
operator|.
name|INTERNAL
operator|&&
name|version
operator|==
name|Versions
operator|.
name|MATCH_ANY
operator|)
operator|==
literal|false
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"an id must be provided if version type or value are set"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
return|return
name|validationException
return|;
block|}
comment|/**      * The content type that will be used when generating a document from user provided objects like Maps.      */
DECL|method|getContentType
specifier|public
name|XContentType
name|getContentType
parameter_list|()
block|{
return|return
name|contentType
return|;
block|}
comment|/**      * Sets the content type that will be used when generating a document from user provided objects (like Map).      */
DECL|method|contentType
specifier|public
name|IndexRequest
name|contentType
parameter_list|(
name|XContentType
name|contentType
parameter_list|)
block|{
name|this
operator|.
name|contentType
operator|=
name|contentType
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The type of the indexed document.      */
annotation|@
name|Override
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|type
return|;
block|}
comment|/**      * Sets the type of the indexed document.      */
DECL|method|type
specifier|public
name|IndexRequest
name|type
parameter_list|(
name|String
name|type
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The id of the indexed document. If not set, will be automatically generated.      */
annotation|@
name|Override
DECL|method|id
specifier|public
name|String
name|id
parameter_list|()
block|{
return|return
name|id
return|;
block|}
comment|/**      * Sets the id of the indexed document. If not set, will be automatically generated.      */
DECL|method|id
specifier|public
name|IndexRequest
name|id
parameter_list|(
name|String
name|id
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Controls the shard routing of the request. Using this value to hash the shard      * and not the id.      */
annotation|@
name|Override
DECL|method|routing
specifier|public
name|IndexRequest
name|routing
parameter_list|(
name|String
name|routing
parameter_list|)
block|{
if|if
condition|(
name|routing
operator|!=
literal|null
operator|&&
name|routing
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|this
operator|.
name|routing
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|routing
operator|=
name|routing
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
comment|/**      * Controls the shard routing of the request. Using this value to hash the shard      * and not the id.      */
annotation|@
name|Override
DECL|method|routing
specifier|public
name|String
name|routing
parameter_list|()
block|{
return|return
name|this
operator|.
name|routing
return|;
block|}
comment|/**      * Sets the parent id of this document.      */
DECL|method|parent
specifier|public
name|IndexRequest
name|parent
parameter_list|(
name|String
name|parent
parameter_list|)
block|{
name|this
operator|.
name|parent
operator|=
name|parent
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|parent
specifier|public
name|String
name|parent
parameter_list|()
block|{
return|return
name|this
operator|.
name|parent
return|;
block|}
comment|/**      * Sets the ingest pipeline to be executed before indexing the document      */
DECL|method|setPipeline
specifier|public
name|IndexRequest
name|setPipeline
parameter_list|(
name|String
name|pipeline
parameter_list|)
block|{
name|this
operator|.
name|pipeline
operator|=
name|pipeline
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Returns the ingest pipeline to be executed before indexing the document      */
DECL|method|getPipeline
specifier|public
name|String
name|getPipeline
parameter_list|()
block|{
return|return
name|this
operator|.
name|pipeline
return|;
block|}
comment|/**      * The source of the document to index, recopied to a new array if it is unsafe.      */
DECL|method|source
specifier|public
name|BytesReference
name|source
parameter_list|()
block|{
return|return
name|source
return|;
block|}
DECL|method|sourceAsMap
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|sourceAsMap
parameter_list|()
block|{
return|return
name|XContentHelper
operator|.
name|convertToMap
argument_list|(
name|source
argument_list|,
literal|false
argument_list|)
operator|.
name|v2
argument_list|()
return|;
block|}
comment|/**      * Index the Map as a {@link org.elasticsearch.client.Requests#INDEX_CONTENT_TYPE}.      *      * @param source The map to index      */
DECL|method|source
specifier|public
name|IndexRequest
name|source
parameter_list|(
name|Map
name|source
parameter_list|)
throws|throws
name|ElasticsearchGenerationException
block|{
return|return
name|source
argument_list|(
name|source
argument_list|,
name|contentType
argument_list|)
return|;
block|}
comment|/**      * Index the Map as the provided content type.      *      * @param source The map to index      */
DECL|method|source
specifier|public
name|IndexRequest
name|source
parameter_list|(
name|Map
name|source
parameter_list|,
name|XContentType
name|contentType
parameter_list|)
throws|throws
name|ElasticsearchGenerationException
block|{
try|try
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|contentBuilder
argument_list|(
name|contentType
argument_list|)
decl_stmt|;
name|builder
operator|.
name|map
argument_list|(
name|source
argument_list|)
expr_stmt|;
return|return
name|source
argument_list|(
name|builder
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchGenerationException
argument_list|(
literal|"Failed to generate ["
operator|+
name|source
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**      * Sets the document source to index.      *      * Note, its preferable to either set it using {@link #source(org.elasticsearch.common.xcontent.XContentBuilder)}      * or using the {@link #source(byte[])}.      */
DECL|method|source
specifier|public
name|IndexRequest
name|source
parameter_list|(
name|String
name|source
parameter_list|)
block|{
name|this
operator|.
name|source
operator|=
operator|new
name|BytesArray
argument_list|(
name|source
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the content source to index.      */
DECL|method|source
specifier|public
name|IndexRequest
name|source
parameter_list|(
name|XContentBuilder
name|sourceBuilder
parameter_list|)
block|{
name|source
operator|=
name|sourceBuilder
operator|.
name|bytes
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|source
specifier|public
name|IndexRequest
name|source
parameter_list|(
name|String
name|field1
parameter_list|,
name|Object
name|value1
parameter_list|)
block|{
try|try
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|contentBuilder
argument_list|(
name|contentType
argument_list|)
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
name|field1
argument_list|,
name|value1
argument_list|)
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|source
argument_list|(
name|builder
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchGenerationException
argument_list|(
literal|"Failed to generate"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|source
specifier|public
name|IndexRequest
name|source
parameter_list|(
name|String
name|field1
parameter_list|,
name|Object
name|value1
parameter_list|,
name|String
name|field2
parameter_list|,
name|Object
name|value2
parameter_list|)
block|{
try|try
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|contentBuilder
argument_list|(
name|contentType
argument_list|)
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
name|field1
argument_list|,
name|value1
argument_list|)
operator|.
name|field
argument_list|(
name|field2
argument_list|,
name|value2
argument_list|)
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|source
argument_list|(
name|builder
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchGenerationException
argument_list|(
literal|"Failed to generate"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|source
specifier|public
name|IndexRequest
name|source
parameter_list|(
name|String
name|field1
parameter_list|,
name|Object
name|value1
parameter_list|,
name|String
name|field2
parameter_list|,
name|Object
name|value2
parameter_list|,
name|String
name|field3
parameter_list|,
name|Object
name|value3
parameter_list|)
block|{
try|try
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|contentBuilder
argument_list|(
name|contentType
argument_list|)
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
name|field1
argument_list|,
name|value1
argument_list|)
operator|.
name|field
argument_list|(
name|field2
argument_list|,
name|value2
argument_list|)
operator|.
name|field
argument_list|(
name|field3
argument_list|,
name|value3
argument_list|)
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|source
argument_list|(
name|builder
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchGenerationException
argument_list|(
literal|"Failed to generate"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|source
specifier|public
name|IndexRequest
name|source
parameter_list|(
name|String
name|field1
parameter_list|,
name|Object
name|value1
parameter_list|,
name|String
name|field2
parameter_list|,
name|Object
name|value2
parameter_list|,
name|String
name|field3
parameter_list|,
name|Object
name|value3
parameter_list|,
name|String
name|field4
parameter_list|,
name|Object
name|value4
parameter_list|)
block|{
try|try
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|contentBuilder
argument_list|(
name|contentType
argument_list|)
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
name|field1
argument_list|,
name|value1
argument_list|)
operator|.
name|field
argument_list|(
name|field2
argument_list|,
name|value2
argument_list|)
operator|.
name|field
argument_list|(
name|field3
argument_list|,
name|value3
argument_list|)
operator|.
name|field
argument_list|(
name|field4
argument_list|,
name|value4
argument_list|)
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|source
argument_list|(
name|builder
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchGenerationException
argument_list|(
literal|"Failed to generate"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|source
specifier|public
name|IndexRequest
name|source
parameter_list|(
name|Object
modifier|...
name|source
parameter_list|)
block|{
if|if
condition|(
name|source
operator|.
name|length
operator|%
literal|2
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"The number of object passed must be even but was ["
operator|+
name|source
operator|.
name|length
operator|+
literal|"]"
argument_list|)
throw|;
block|}
if|if
condition|(
name|source
operator|.
name|length
operator|==
literal|2
operator|&&
name|source
index|[
literal|0
index|]
operator|instanceof
name|BytesReference
operator|&&
name|source
index|[
literal|1
index|]
operator|instanceof
name|Boolean
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"you are using the removed method for source with bytes and unsafe flag, the unsafe flag was removed, please just use source(BytesReference)"
argument_list|)
throw|;
block|}
try|try
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|contentBuilder
argument_list|(
name|contentType
argument_list|)
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|source
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|source
index|[
name|i
operator|++
index|]
operator|.
name|toString
argument_list|()
argument_list|,
name|source
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|source
argument_list|(
name|builder
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchGenerationException
argument_list|(
literal|"Failed to generate"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**      * Sets the document to index in bytes form.      */
DECL|method|source
specifier|public
name|IndexRequest
name|source
parameter_list|(
name|BytesReference
name|source
parameter_list|)
block|{
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the document to index in bytes form.      */
DECL|method|source
specifier|public
name|IndexRequest
name|source
parameter_list|(
name|byte
index|[]
name|source
parameter_list|)
block|{
return|return
name|source
argument_list|(
name|source
argument_list|,
literal|0
argument_list|,
name|source
operator|.
name|length
argument_list|)
return|;
block|}
comment|/**      * Sets the document to index in bytes form (assumed to be safe to be used from different      * threads).      *      * @param source The source to index      * @param offset The offset in the byte array      * @param length The length of the data      */
DECL|method|source
specifier|public
name|IndexRequest
name|source
parameter_list|(
name|byte
index|[]
name|source
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|this
operator|.
name|source
operator|=
operator|new
name|BytesArray
argument_list|(
name|source
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the type of operation to perform.      */
DECL|method|opType
specifier|public
name|IndexRequest
name|opType
parameter_list|(
name|OpType
name|opType
parameter_list|)
block|{
if|if
condition|(
name|opType
operator|!=
name|OpType
operator|.
name|CREATE
operator|&&
name|opType
operator|!=
name|OpType
operator|.
name|INDEX
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"opType must be 'create' or 'index', found: ["
operator|+
name|opType
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|opType
operator|=
name|opType
expr_stmt|;
if|if
condition|(
name|opType
operator|==
name|OpType
operator|.
name|CREATE
condition|)
block|{
name|version
argument_list|(
name|Versions
operator|.
name|MATCH_DELETED
argument_list|)
expr_stmt|;
name|versionType
argument_list|(
name|VersionType
operator|.
name|INTERNAL
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
comment|/**      * Sets a string representation of the {@link #opType(OpType)}. Can      * be either "index" or "create".      */
DECL|method|opType
specifier|public
name|IndexRequest
name|opType
parameter_list|(
name|String
name|opType
parameter_list|)
block|{
name|String
name|op
init|=
name|opType
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
decl_stmt|;
if|if
condition|(
name|op
operator|.
name|equals
argument_list|(
literal|"create"
argument_list|)
condition|)
block|{
name|opType
argument_list|(
name|OpType
operator|.
name|CREATE
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|op
operator|.
name|equals
argument_list|(
literal|"index"
argument_list|)
condition|)
block|{
name|opType
argument_list|(
name|OpType
operator|.
name|INDEX
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"opType must be 'create' or 'index', found: ["
operator|+
name|opType
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
name|this
return|;
block|}
comment|/**      * Set to<tt>true</tt> to force this index to use {@link OpType#CREATE}.      */
DECL|method|create
specifier|public
name|IndexRequest
name|create
parameter_list|(
name|boolean
name|create
parameter_list|)
block|{
if|if
condition|(
name|create
condition|)
block|{
return|return
name|opType
argument_list|(
name|OpType
operator|.
name|CREATE
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|opType
argument_list|(
name|OpType
operator|.
name|INDEX
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|opType
specifier|public
name|OpType
name|opType
parameter_list|()
block|{
return|return
name|this
operator|.
name|opType
return|;
block|}
annotation|@
name|Override
DECL|method|version
specifier|public
name|IndexRequest
name|version
parameter_list|(
name|long
name|version
parameter_list|)
block|{
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|version
specifier|public
name|long
name|version
parameter_list|()
block|{
return|return
name|this
operator|.
name|version
return|;
block|}
annotation|@
name|Override
DECL|method|versionType
specifier|public
name|IndexRequest
name|versionType
parameter_list|(
name|VersionType
name|versionType
parameter_list|)
block|{
name|this
operator|.
name|versionType
operator|=
name|versionType
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|versionType
specifier|public
name|VersionType
name|versionType
parameter_list|()
block|{
return|return
name|this
operator|.
name|versionType
return|;
block|}
DECL|method|process
specifier|public
name|void
name|process
parameter_list|(
annotation|@
name|Nullable
name|MappingMetaData
name|mappingMd
parameter_list|,
name|boolean
name|allowIdGeneration
parameter_list|,
name|String
name|concreteIndex
parameter_list|)
block|{
if|if
condition|(
name|mappingMd
operator|!=
literal|null
condition|)
block|{
comment|// might as well check for routing here
if|if
condition|(
name|mappingMd
operator|.
name|routing
argument_list|()
operator|.
name|required
argument_list|()
operator|&&
name|routing
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RoutingMissingException
argument_list|(
name|concreteIndex
argument_list|,
name|type
argument_list|,
name|id
argument_list|)
throw|;
block|}
if|if
condition|(
name|parent
operator|!=
literal|null
operator|&&
operator|!
name|mappingMd
operator|.
name|hasParentField
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Can't specify parent if no parent field has been configured"
argument_list|)
throw|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|parent
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Can't specify parent if no parent field has been configured"
argument_list|)
throw|;
block|}
block|}
comment|// generate id if not already provided and id generation is allowed
if|if
condition|(
name|allowIdGeneration
operator|&&
name|id
operator|==
literal|null
condition|)
block|{
assert|assert
name|autoGeneratedTimestamp
operator|==
operator|-
literal|1
assert|;
name|autoGeneratedTimestamp
operator|=
name|Math
operator|.
name|max
argument_list|(
literal|0
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
comment|// extra paranoia
name|id
argument_list|(
name|UUIDs
operator|.
name|base64UUID
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/* resolve the routing if needed */
DECL|method|resolveRouting
specifier|public
name|void
name|resolveRouting
parameter_list|(
name|MetaData
name|metaData
parameter_list|)
block|{
name|routing
argument_list|(
name|metaData
operator|.
name|resolveIndexRouting
argument_list|(
name|parent
argument_list|,
name|routing
argument_list|,
name|index
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|type
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|id
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|routing
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|parent
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|getVersion
argument_list|()
operator|.
name|before
argument_list|(
name|Version
operator|.
name|V_6_0_0_alpha1_UNRELEASED
argument_list|)
condition|)
block|{
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
comment|// timestamp
name|in
operator|.
name|readOptionalWriteable
argument_list|(
name|TimeValue
operator|::
operator|new
argument_list|)
expr_stmt|;
comment|// ttl
block|}
name|source
operator|=
name|in
operator|.
name|readBytesReference
argument_list|()
expr_stmt|;
name|opType
operator|=
name|OpType
operator|.
name|fromId
argument_list|(
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
expr_stmt|;
name|version
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|versionType
operator|=
name|VersionType
operator|.
name|fromValue
argument_list|(
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
expr_stmt|;
name|pipeline
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|isRetry
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|autoGeneratedTimestamp
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|type
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|id
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|routing
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|parent
argument_list|)
expr_stmt|;
if|if
condition|(
name|out
operator|.
name|getVersion
argument_list|()
operator|.
name|before
argument_list|(
name|Version
operator|.
name|V_6_0_0_alpha1_UNRELEASED
argument_list|)
condition|)
block|{
name|out
operator|.
name|writeOptionalString
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalWriteable
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeBytesReference
argument_list|(
name|source
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeByte
argument_list|(
name|opType
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|version
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeByte
argument_list|(
name|versionType
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|pipeline
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|isRetry
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|autoGeneratedTimestamp
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|String
name|sSource
init|=
literal|"_na_"
decl_stmt|;
try|try
block|{
name|sSource
operator|=
name|XContentHelper
operator|.
name|convertToJson
argument_list|(
name|source
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// ignore
block|}
return|return
literal|"index {["
operator|+
name|index
operator|+
literal|"]["
operator|+
name|type
operator|+
literal|"]["
operator|+
name|id
operator|+
literal|"], source["
operator|+
name|sSource
operator|+
literal|"]}"
return|;
block|}
comment|/**      * Returns<code>true</code> if this request has been sent to a shard copy more than once.      */
DECL|method|isRetry
specifier|public
name|boolean
name|isRetry
parameter_list|()
block|{
return|return
name|isRetry
return|;
block|}
annotation|@
name|Override
DECL|method|onRetry
specifier|public
name|void
name|onRetry
parameter_list|()
block|{
name|isRetry
operator|=
literal|true
expr_stmt|;
block|}
comment|/**      * Returns the timestamp the auto generated ID was created or {@value #UNSET_AUTO_GENERATED_TIMESTAMP} if the      * document has no auto generated timestamp. This method will return a positive value iff the id was auto generated.      */
DECL|method|getAutoGeneratedTimestamp
specifier|public
name|long
name|getAutoGeneratedTimestamp
parameter_list|()
block|{
return|return
name|autoGeneratedTimestamp
return|;
block|}
block|}
end_class

end_unit

