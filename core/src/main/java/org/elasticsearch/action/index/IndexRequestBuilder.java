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
name|support
operator|.
name|WriteRequestBuilder
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
name|ReplicationRequestBuilder
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
name|ElasticsearchClient
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
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  * An index document action request builder.  */
end_comment

begin_class
DECL|class|IndexRequestBuilder
specifier|public
class|class
name|IndexRequestBuilder
extends|extends
name|ReplicationRequestBuilder
argument_list|<
name|IndexRequest
argument_list|,
name|IndexResponse
argument_list|,
name|IndexRequestBuilder
argument_list|>
implements|implements
name|WriteRequestBuilder
argument_list|<
name|IndexRequestBuilder
argument_list|>
block|{
DECL|method|IndexRequestBuilder
specifier|public
name|IndexRequestBuilder
parameter_list|(
name|ElasticsearchClient
name|client
parameter_list|,
name|IndexAction
name|action
parameter_list|)
block|{
name|super
argument_list|(
name|client
argument_list|,
name|action
argument_list|,
operator|new
name|IndexRequest
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|IndexRequestBuilder
specifier|public
name|IndexRequestBuilder
parameter_list|(
name|ElasticsearchClient
name|client
parameter_list|,
name|IndexAction
name|action
parameter_list|,
annotation|@
name|Nullable
name|String
name|index
parameter_list|)
block|{
name|super
argument_list|(
name|client
argument_list|,
name|action
argument_list|,
operator|new
name|IndexRequest
argument_list|(
name|index
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Sets the type to index the document to.      */
DECL|method|setType
specifier|public
name|IndexRequestBuilder
name|setType
parameter_list|(
name|String
name|type
parameter_list|)
block|{
name|request
operator|.
name|type
argument_list|(
name|type
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the id to index the document under. Optional, and if not set, one will be automatically      * generated.      */
DECL|method|setId
specifier|public
name|IndexRequestBuilder
name|setId
parameter_list|(
name|String
name|id
parameter_list|)
block|{
name|request
operator|.
name|id
argument_list|(
name|id
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Controls the shard routing of the request. Using this value to hash the shard      * and not the id.      */
DECL|method|setRouting
specifier|public
name|IndexRequestBuilder
name|setRouting
parameter_list|(
name|String
name|routing
parameter_list|)
block|{
name|request
operator|.
name|routing
argument_list|(
name|routing
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the parent id of this document. If routing is not set, automatically set it as the      * routing as well.      */
DECL|method|setParent
specifier|public
name|IndexRequestBuilder
name|setParent
parameter_list|(
name|String
name|parent
parameter_list|)
block|{
name|request
operator|.
name|parent
argument_list|(
name|parent
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the source.      */
DECL|method|setSource
specifier|public
name|IndexRequestBuilder
name|setSource
parameter_list|(
name|BytesReference
name|source
parameter_list|)
block|{
name|request
operator|.
name|source
argument_list|(
name|source
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Index the Map as a JSON.      *      * @param source The map to index      */
DECL|method|setSource
specifier|public
name|IndexRequestBuilder
name|setSource
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|?
argument_list|>
name|source
parameter_list|)
block|{
name|request
operator|.
name|source
argument_list|(
name|source
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Index the Map as the provided content type.      *      * @param source The map to index      */
DECL|method|setSource
specifier|public
name|IndexRequestBuilder
name|setSource
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|?
argument_list|>
name|source
parameter_list|,
name|XContentType
name|contentType
parameter_list|)
block|{
name|request
operator|.
name|source
argument_list|(
name|source
argument_list|,
name|contentType
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the document source to index.      *<p>      * Note, its preferable to either set it using {@link #setSource(org.elasticsearch.common.xcontent.XContentBuilder)}      * or using the {@link #setSource(byte[])}.      */
DECL|method|setSource
specifier|public
name|IndexRequestBuilder
name|setSource
parameter_list|(
name|String
name|source
parameter_list|)
block|{
name|request
operator|.
name|source
argument_list|(
name|source
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the content source to index.      */
DECL|method|setSource
specifier|public
name|IndexRequestBuilder
name|setSource
parameter_list|(
name|XContentBuilder
name|sourceBuilder
parameter_list|)
block|{
name|request
operator|.
name|source
argument_list|(
name|sourceBuilder
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the document to index in bytes form.      */
DECL|method|setSource
specifier|public
name|IndexRequestBuilder
name|setSource
parameter_list|(
name|byte
index|[]
name|source
parameter_list|)
block|{
name|request
operator|.
name|source
argument_list|(
name|source
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the document to index in bytes form (assumed to be safe to be used from different      * threads).      *      * @param source The source to index      * @param offset The offset in the byte array      * @param length The length of the data      */
DECL|method|setSource
specifier|public
name|IndexRequestBuilder
name|setSource
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
name|request
operator|.
name|source
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
comment|/**      * Constructs a simple document with a field name and value pairs.      *<p>      *<b>Note: the number of objects passed to this method must be an even      * number. Also the first argument in each pair (the field name) must have a      * valid String representation.</b>      *</p>      */
DECL|method|setSource
specifier|public
name|IndexRequestBuilder
name|setSource
parameter_list|(
name|Object
modifier|...
name|source
parameter_list|)
block|{
name|request
operator|.
name|source
argument_list|(
name|source
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The content type that will be used to generate a document from user provided objects (like Map).      */
DECL|method|setContentType
specifier|public
name|IndexRequestBuilder
name|setContentType
parameter_list|(
name|XContentType
name|contentType
parameter_list|)
block|{
name|request
operator|.
name|contentType
argument_list|(
name|contentType
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the type of operation to perform.      */
DECL|method|setOpType
specifier|public
name|IndexRequestBuilder
name|setOpType
parameter_list|(
name|DocWriteRequest
operator|.
name|OpType
name|opType
parameter_list|)
block|{
name|request
operator|.
name|opType
argument_list|(
name|opType
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Set to<tt>true</tt> to force this index to use {@link org.elasticsearch.action.index.IndexRequest.OpType#CREATE}.      */
DECL|method|setCreate
specifier|public
name|IndexRequestBuilder
name|setCreate
parameter_list|(
name|boolean
name|create
parameter_list|)
block|{
name|request
operator|.
name|create
argument_list|(
name|create
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the version, which will cause the index operation to only be performed if a matching      * version exists and no changes happened on the doc since then.      */
DECL|method|setVersion
specifier|public
name|IndexRequestBuilder
name|setVersion
parameter_list|(
name|long
name|version
parameter_list|)
block|{
name|request
operator|.
name|version
argument_list|(
name|version
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the versioning type. Defaults to {@link VersionType#INTERNAL}.      */
DECL|method|setVersionType
specifier|public
name|IndexRequestBuilder
name|setVersionType
parameter_list|(
name|VersionType
name|versionType
parameter_list|)
block|{
name|request
operator|.
name|versionType
argument_list|(
name|versionType
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the ingest pipeline to be executed before indexing the document      */
DECL|method|setPipeline
specifier|public
name|IndexRequestBuilder
name|setPipeline
parameter_list|(
name|String
name|pipeline
parameter_list|)
block|{
name|request
operator|.
name|setPipeline
argument_list|(
name|pipeline
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
block|}
end_class

end_unit

