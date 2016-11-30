begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.reindex
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|reindex
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
name|CompositeIndicesRequest
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
name|IndicesRequest
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
name|index
operator|.
name|IndexRequest
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
name|search
operator|.
name|SearchRequest
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
name|index
operator|.
name|reindex
operator|.
name|remote
operator|.
name|RemoteInfo
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|tasks
operator|.
name|TaskId
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
name|Arrays
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
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|singletonList
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|unmodifiableList
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

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|VersionType
operator|.
name|INTERNAL
import|;
end_import

begin_comment
comment|/**  * Request to reindex some documents from one index to another. This implements CompositeIndicesRequest but in a misleading way. Rather than  * returning all the subrequests that it will make it tries to return a representative set of subrequests. This is best-effort for a bunch  * of reasons, not least of which that scripts are allowed to change the destination request in drastic ways, including changing the index  * to which documents are written.  */
end_comment

begin_class
DECL|class|ReindexRequest
specifier|public
class|class
name|ReindexRequest
extends|extends
name|AbstractBulkIndexByScrollRequest
argument_list|<
name|ReindexRequest
argument_list|>
implements|implements
name|CompositeIndicesRequest
block|{
comment|/**      * Prototype for index requests.      */
DECL|field|destination
specifier|private
name|IndexRequest
name|destination
decl_stmt|;
DECL|field|remoteInfo
specifier|private
name|RemoteInfo
name|remoteInfo
decl_stmt|;
DECL|method|ReindexRequest
specifier|public
name|ReindexRequest
parameter_list|()
block|{     }
DECL|method|ReindexRequest
specifier|public
name|ReindexRequest
parameter_list|(
name|SearchRequest
name|search
parameter_list|,
name|IndexRequest
name|destination
parameter_list|)
block|{
name|this
argument_list|(
name|search
argument_list|,
name|destination
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
DECL|method|ReindexRequest
specifier|private
name|ReindexRequest
parameter_list|(
name|SearchRequest
name|search
parameter_list|,
name|IndexRequest
name|destination
parameter_list|,
name|boolean
name|setDefaults
parameter_list|)
block|{
name|super
argument_list|(
name|search
argument_list|,
name|setDefaults
argument_list|)
expr_stmt|;
name|this
operator|.
name|destination
operator|=
name|destination
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|self
specifier|protected
name|ReindexRequest
name|self
parameter_list|()
block|{
return|return
name|this
return|;
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
name|e
init|=
name|super
operator|.
name|validate
argument_list|()
decl_stmt|;
if|if
condition|(
name|getSearchRequest
argument_list|()
operator|.
name|indices
argument_list|()
operator|==
literal|null
operator|||
name|getSearchRequest
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|e
operator|=
name|addValidationError
argument_list|(
literal|"use _all if you really want to copy from all existing indexes"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
comment|/*          * Note that we don't call index's validator - it won't work because          * we'll be filling in portions of it as we receive the docs. But we can          * validate some things so we do that below.          */
if|if
condition|(
name|destination
operator|.
name|index
argument_list|()
operator|==
literal|null
condition|)
block|{
name|e
operator|=
name|addValidationError
argument_list|(
literal|"index must be specified"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
name|e
return|;
block|}
if|if
condition|(
literal|false
operator|==
name|routingIsValid
argument_list|()
condition|)
block|{
name|e
operator|=
name|addValidationError
argument_list|(
literal|"routing must be unset, [keep], [discard] or [=<some new value>]"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|destination
operator|.
name|versionType
argument_list|()
operator|==
name|INTERNAL
condition|)
block|{
if|if
condition|(
name|destination
operator|.
name|version
argument_list|()
operator|!=
name|Versions
operator|.
name|MATCH_ANY
operator|&&
name|destination
operator|.
name|version
argument_list|()
operator|!=
name|Versions
operator|.
name|MATCH_DELETED
condition|)
block|{
name|e
operator|=
name|addValidationError
argument_list|(
literal|"unsupported version for internal versioning ["
operator|+
name|destination
operator|.
name|version
argument_list|()
operator|+
literal|']'
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|getRemoteInfo
argument_list|()
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|getSearchRequest
argument_list|()
operator|.
name|source
argument_list|()
operator|.
name|query
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|e
operator|=
name|addValidationError
argument_list|(
literal|"reindex from remote sources should use RemoteInfo's query instead of source's query"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|getSlices
argument_list|()
operator|!=
literal|1
condition|)
block|{
name|e
operator|=
name|addValidationError
argument_list|(
literal|"reindex from remote sources doesn't support workers> 1 but was ["
operator|+
name|getSlices
argument_list|()
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|e
return|;
block|}
DECL|method|routingIsValid
specifier|private
name|boolean
name|routingIsValid
parameter_list|()
block|{
if|if
condition|(
name|destination
operator|.
name|routing
argument_list|()
operator|==
literal|null
operator|||
name|destination
operator|.
name|routing
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"="
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
switch|switch
condition|(
name|destination
operator|.
name|routing
argument_list|()
condition|)
block|{
case|case
literal|"keep"
case|:
case|case
literal|"discard"
case|:
return|return
literal|true
return|;
default|default:
return|return
literal|false
return|;
block|}
block|}
DECL|method|getDestination
specifier|public
name|IndexRequest
name|getDestination
parameter_list|()
block|{
return|return
name|destination
return|;
block|}
DECL|method|setRemoteInfo
specifier|public
name|void
name|setRemoteInfo
parameter_list|(
name|RemoteInfo
name|remoteInfo
parameter_list|)
block|{
name|this
operator|.
name|remoteInfo
operator|=
name|remoteInfo
expr_stmt|;
block|}
DECL|method|getRemoteInfo
specifier|public
name|RemoteInfo
name|getRemoteInfo
parameter_list|()
block|{
return|return
name|remoteInfo
return|;
block|}
annotation|@
name|Override
DECL|method|forSlice
name|ReindexRequest
name|forSlice
parameter_list|(
name|TaskId
name|slicingTask
parameter_list|,
name|SearchRequest
name|slice
parameter_list|)
block|{
name|ReindexRequest
name|sliced
init|=
name|doForSlice
argument_list|(
operator|new
name|ReindexRequest
argument_list|(
name|slice
argument_list|,
name|destination
argument_list|,
literal|false
argument_list|)
argument_list|,
name|slicingTask
argument_list|)
decl_stmt|;
name|sliced
operator|.
name|setRemoteInfo
argument_list|(
name|remoteInfo
argument_list|)
expr_stmt|;
return|return
name|sliced
return|;
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
name|destination
operator|=
operator|new
name|IndexRequest
argument_list|()
expr_stmt|;
name|destination
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|remoteInfo
operator|=
name|in
operator|.
name|readOptionalWriteable
argument_list|(
name|RemoteInfo
operator|::
operator|new
argument_list|)
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
name|destination
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalWriteable
argument_list|(
name|remoteInfo
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
name|StringBuilder
name|b
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|b
operator|.
name|append
argument_list|(
literal|"reindex from "
argument_list|)
expr_stmt|;
if|if
condition|(
name|remoteInfo
operator|!=
literal|null
condition|)
block|{
name|b
operator|.
name|append
argument_list|(
literal|'['
argument_list|)
operator|.
name|append
argument_list|(
name|remoteInfo
argument_list|)
operator|.
name|append
argument_list|(
literal|']'
argument_list|)
expr_stmt|;
block|}
name|searchToString
argument_list|(
name|b
argument_list|)
expr_stmt|;
name|b
operator|.
name|append
argument_list|(
literal|" to ["
argument_list|)
operator|.
name|append
argument_list|(
name|destination
operator|.
name|index
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|']'
argument_list|)
expr_stmt|;
if|if
condition|(
name|destination
operator|.
name|type
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|b
operator|.
name|append
argument_list|(
literal|'['
argument_list|)
operator|.
name|append
argument_list|(
name|destination
operator|.
name|type
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|']'
argument_list|)
expr_stmt|;
block|}
return|return
name|b
operator|.
name|toString
argument_list|()
return|;
block|}
comment|// CompositeIndicesRequest implementation so plugins can reason about the request. This is really just a best effort thing.
comment|/**      * Accessor to get the underlying {@link IndicesRequest}s that this request wraps. Note that this method is<strong>not      * accurate</strong> since it returns a prototype {@link IndexRequest} and not the actual requests that will be issued as part of the      * execution of this request. Additionally, scripts can modify the underlying {@link IndexRequest} and change values such as the index,      * type, {@link org.elasticsearch.action.support.IndicesOptions}. In short - only use this for very course reasoning about the request.      *      * @return a list comprising of the {@link SearchRequest} and the prototype {@link IndexRequest}      */
annotation|@
name|Override
DECL|method|subRequests
specifier|public
name|List
argument_list|<
name|?
extends|extends
name|IndicesRequest
argument_list|>
name|subRequests
parameter_list|()
block|{
assert|assert
name|getSearchRequest
argument_list|()
operator|!=
literal|null
assert|;
assert|assert
name|getDestination
argument_list|()
operator|!=
literal|null
assert|;
if|if
condition|(
name|remoteInfo
operator|!=
literal|null
condition|)
block|{
return|return
name|singletonList
argument_list|(
name|getDestination
argument_list|()
argument_list|)
return|;
block|}
return|return
name|unmodifiableList
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|getSearchRequest
argument_list|()
argument_list|,
name|getDestination
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

