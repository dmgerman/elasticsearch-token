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
name|action
operator|.
name|support
operator|.
name|IndicesOptions
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
comment|/**  * Creates a new {@link DeleteByQueryRequest} that uses scrolling and bulk requests to delete all documents matching  * the query. This can have performance as well as visibility implications.  *  * Delete-by-query now has the following semantics:  *<ul>  *<li>it's<tt>non-atomic</tt>, a delete-by-query may fail at any time while some documents matching the query have already been  *     deleted</li>  *<li>it's<tt>syntactic sugar</tt>, a delete-by-query is equivalent to a scroll search and corresponding bulk-deletes by ID</li>  *<li>it's executed on a<tt>point-in-time</tt> snapshot, a delete-by-query will only delete the documents that are visible at the  *     point in time the delete-by-query was started, equivalent to the scroll API</li>  *<li>it's<tt>consistent</tt>, a delete-by-query will yield consistent results across all replicas of a shard</li>  *<li>it's<tt>forward-compatible</tt>, a delete-by-query will only send IDs to the shards as deletes such that no queries are  *     stored in the transaction logs that might not be supported in the future.</li>  *<li>it's results won't be visible until the index is refreshed.</li>  *</ul>  */
end_comment

begin_class
DECL|class|DeleteByQueryRequest
specifier|public
class|class
name|DeleteByQueryRequest
extends|extends
name|AbstractBulkByScrollRequest
argument_list|<
name|DeleteByQueryRequest
argument_list|>
implements|implements
name|IndicesRequest
operator|.
name|Replaceable
block|{
DECL|method|DeleteByQueryRequest
specifier|public
name|DeleteByQueryRequest
parameter_list|()
block|{     }
DECL|method|DeleteByQueryRequest
specifier|public
name|DeleteByQueryRequest
parameter_list|(
name|SearchRequest
name|search
parameter_list|)
block|{
name|this
argument_list|(
name|search
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
DECL|method|DeleteByQueryRequest
specifier|private
name|DeleteByQueryRequest
parameter_list|(
name|SearchRequest
name|search
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
comment|// Delete-By-Query does not require the source
if|if
condition|(
name|setDefaults
condition|)
block|{
name|search
operator|.
name|source
argument_list|()
operator|.
name|fetchSource
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|self
specifier|protected
name|DeleteByQueryRequest
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
literal|"use _all if you really want to delete from all existing indexes"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|getSearchRequest
argument_list|()
operator|==
literal|null
operator|||
name|getSearchRequest
argument_list|()
operator|.
name|source
argument_list|()
operator|==
literal|null
condition|)
block|{
name|e
operator|=
name|addValidationError
argument_list|(
literal|"source is missing"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
elseif|else
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
operator|==
literal|null
condition|)
block|{
name|e
operator|=
name|addValidationError
argument_list|(
literal|"query is missing"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|e
return|;
block|}
annotation|@
name|Override
DECL|method|forSlice
specifier|public
name|DeleteByQueryRequest
name|forSlice
parameter_list|(
name|TaskId
name|slicingTask
parameter_list|,
name|SearchRequest
name|slice
parameter_list|)
block|{
return|return
name|doForSlice
argument_list|(
operator|new
name|DeleteByQueryRequest
argument_list|(
name|slice
argument_list|,
literal|false
argument_list|)
argument_list|,
name|slicingTask
argument_list|)
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
literal|"delete-by-query "
argument_list|)
expr_stmt|;
name|searchToString
argument_list|(
name|b
argument_list|)
expr_stmt|;
return|return
name|b
operator|.
name|toString
argument_list|()
return|;
block|}
comment|//delete by query deletes all documents that match a query. The indices and indices options that affect how
comment|//indices are resolved depend entirely on the inner search request. That's why the following methods delegate to it.
annotation|@
name|Override
DECL|method|indices
specifier|public
name|IndicesRequest
name|indices
parameter_list|(
name|String
modifier|...
name|indices
parameter_list|)
block|{
assert|assert
name|getSearchRequest
argument_list|()
operator|!=
literal|null
assert|;
name|getSearchRequest
argument_list|()
operator|.
name|indices
argument_list|(
name|indices
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|indices
specifier|public
name|String
index|[]
name|indices
parameter_list|()
block|{
assert|assert
name|getSearchRequest
argument_list|()
operator|!=
literal|null
assert|;
return|return
name|getSearchRequest
argument_list|()
operator|.
name|indices
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|indicesOptions
specifier|public
name|IndicesOptions
name|indicesOptions
parameter_list|()
block|{
assert|assert
name|getSearchRequest
argument_list|()
operator|!=
literal|null
assert|;
return|return
name|getSearchRequest
argument_list|()
operator|.
name|indicesOptions
argument_list|()
return|;
block|}
DECL|method|types
specifier|public
name|String
index|[]
name|types
parameter_list|()
block|{
assert|assert
name|getSearchRequest
argument_list|()
operator|!=
literal|null
assert|;
return|return
name|getSearchRequest
argument_list|()
operator|.
name|types
argument_list|()
return|;
block|}
DECL|method|types
specifier|public
name|DeleteByQueryRequest
name|types
parameter_list|(
name|String
modifier|...
name|types
parameter_list|)
block|{
assert|assert
name|getSearchRequest
argument_list|()
operator|!=
literal|null
assert|;
name|getSearchRequest
argument_list|()
operator|.
name|types
argument_list|(
name|types
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
block|}
end_class

end_unit

