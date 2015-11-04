begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.deletebyquery
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|deletebyquery
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
name|ActionRequest
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
name|index
operator|.
name|query
operator|.
name|QueryBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|Scroll
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
name|search
operator|.
name|Scroll
operator|.
name|readScroll
import|;
end_import

begin_comment
comment|/**  * Creates a new {@link DeleteByQueryRequest}. Delete-by-query is since elasticsearch 2.0.0 moved into a plugin  * and is not part of elasticsearch core. In contrast to the previous, in-core, implementation delete-by-query now  * uses scan/scroll and the returned IDs do delete all documents matching the query. This can have performance  * as well as visibility implications. Delete-by-query now has the following semantics:  *<ul>  *<li>it's<tt>non-actomic</tt>, a delete-by-query may fail at any time while some documents matching the query have already been deleted</li>  *<li>it's<tt>try-once</tt>, a delete-by-query may fail at any time and will not retry it's execution. All retry logic is left to the user</li>  *<li>it's<tt>syntactic sugar</tt>, a delete-by-query is equivalent to a scan/scroll search and corresponding bulk-deletes by ID</li>  *<li>it's executed on a<tt>point-in-time</tt> snapshot, a delete-by-query will only delete the documents that are visible at the point in time the delete-by-query was started, equivalent to the scan/scroll API</li>  *<li>it's<tt>consistent</tt>, a delete-by-query will yield consistent results across all replicas of a shard</li>  *<li>it's<tt>forward-compativle</tt>, a delete-by-query will only send IDs to the shards as deletes such that no queries are stored in the transaction logs that might not be supported in the future.</li>  *<li>it's results won't be visible until the user refreshes the index.</li>  *</ul>  *  * The main reason why delete-by-query is now extracted as a plugin are:  *<ul>  *<li><tt>forward-compatibility</tt>, the previous implementation was prone to store unsupported queries in the transaction logs which is equvalent to data-loss</li>  *<li><tt>consistency&amp; correctness</tt>, the previous implementation was prone to produce different results on a shards replica which can essentially result in a corrupted index</li>  *<li><tt>resiliency</tt>, the previous implementation could cause OOM errors, merge-storms and dramatic slowdowns if used incorrectly</li>  *</ul>  *  * While delete-by-query is a very useful feature, it's implementation is very tricky in system that is based on per-document modifications. The move towards  * a plugin based solution was mainly done to minimize the risk of cluster failures or corrupted indices which where easily possible wiht the previous implementation.  * Users that rely delete by query should install the plugin in oder to use this functionality.  */
end_comment

begin_class
DECL|class|DeleteByQueryRequest
specifier|public
class|class
name|DeleteByQueryRequest
extends|extends
name|ActionRequest
argument_list|<
name|DeleteByQueryRequest
argument_list|>
implements|implements
name|IndicesRequest
operator|.
name|Replaceable
block|{
DECL|field|indices
specifier|private
name|String
index|[]
name|indices
init|=
name|Strings
operator|.
name|EMPTY_ARRAY
decl_stmt|;
DECL|field|indicesOptions
specifier|private
name|IndicesOptions
name|indicesOptions
init|=
name|IndicesOptions
operator|.
name|fromOptions
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
DECL|field|types
specifier|private
name|String
index|[]
name|types
init|=
name|Strings
operator|.
name|EMPTY_ARRAY
decl_stmt|;
DECL|field|query
specifier|private
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|query
decl_stmt|;
DECL|field|routing
specifier|private
name|String
name|routing
decl_stmt|;
DECL|field|size
specifier|private
name|int
name|size
init|=
literal|0
decl_stmt|;
DECL|field|scroll
specifier|private
name|Scroll
name|scroll
init|=
operator|new
name|Scroll
argument_list|(
name|TimeValue
operator|.
name|timeValueMinutes
argument_list|(
literal|10
argument_list|)
argument_list|)
decl_stmt|;
DECL|field|timeout
specifier|private
name|TimeValue
name|timeout
decl_stmt|;
DECL|method|DeleteByQueryRequest
specifier|public
name|DeleteByQueryRequest
parameter_list|()
block|{     }
comment|/**      * Constructs a new delete by query request to run against the provided indices. No indices means      * it will run against all indices.      */
DECL|method|DeleteByQueryRequest
specifier|public
name|DeleteByQueryRequest
parameter_list|(
name|String
modifier|...
name|indices
parameter_list|)
block|{
name|this
operator|.
name|indices
operator|=
name|indices
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
literal|null
decl_stmt|;
if|if
condition|(
name|query
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
return|return
name|validationException
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
return|return
name|this
operator|.
name|indices
return|;
block|}
annotation|@
name|Override
DECL|method|indices
specifier|public
name|DeleteByQueryRequest
name|indices
parameter_list|(
name|String
index|[]
name|indices
parameter_list|)
block|{
name|this
operator|.
name|indices
operator|=
name|indices
expr_stmt|;
return|return
name|this
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
return|return
name|indicesOptions
return|;
block|}
DECL|method|indicesOptions
specifier|public
name|DeleteByQueryRequest
name|indicesOptions
parameter_list|(
name|IndicesOptions
name|indicesOptions
parameter_list|)
block|{
if|if
condition|(
name|indicesOptions
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"IndicesOptions must not be null"
argument_list|)
throw|;
block|}
name|this
operator|.
name|indicesOptions
operator|=
name|indicesOptions
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|types
specifier|public
name|String
index|[]
name|types
parameter_list|()
block|{
return|return
name|this
operator|.
name|types
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
name|this
operator|.
name|types
operator|=
name|types
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|query
specifier|public
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|query
parameter_list|()
block|{
return|return
name|query
return|;
block|}
DECL|method|query
specifier|public
name|DeleteByQueryRequest
name|query
parameter_list|(
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|queryBuilder
parameter_list|)
block|{
name|this
operator|.
name|query
operator|=
name|queryBuilder
expr_stmt|;
return|return
name|this
return|;
block|}
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
DECL|method|routing
specifier|public
name|DeleteByQueryRequest
name|routing
parameter_list|(
name|String
name|routing
parameter_list|)
block|{
name|this
operator|.
name|routing
operator|=
name|routing
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|routing
specifier|public
name|DeleteByQueryRequest
name|routing
parameter_list|(
name|String
modifier|...
name|routings
parameter_list|)
block|{
name|this
operator|.
name|routing
operator|=
name|Strings
operator|.
name|arrayToCommaDelimitedString
argument_list|(
name|routings
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|size
specifier|public
name|DeleteByQueryRequest
name|size
parameter_list|(
name|int
name|size
parameter_list|)
block|{
if|if
condition|(
name|size
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"size must be greater than zero"
argument_list|)
throw|;
block|}
name|this
operator|.
name|size
operator|=
name|size
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|size
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|size
return|;
block|}
DECL|method|scroll
specifier|public
name|Scroll
name|scroll
parameter_list|()
block|{
return|return
name|scroll
return|;
block|}
DECL|method|scroll
specifier|public
name|DeleteByQueryRequest
name|scroll
parameter_list|(
name|Scroll
name|scroll
parameter_list|)
block|{
name|this
operator|.
name|scroll
operator|=
name|scroll
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|scroll
specifier|public
name|DeleteByQueryRequest
name|scroll
parameter_list|(
name|TimeValue
name|keepAlive
parameter_list|)
block|{
return|return
name|scroll
argument_list|(
operator|new
name|Scroll
argument_list|(
name|keepAlive
argument_list|)
argument_list|)
return|;
block|}
DECL|method|scroll
specifier|public
name|DeleteByQueryRequest
name|scroll
parameter_list|(
name|String
name|keepAlive
parameter_list|)
block|{
return|return
name|scroll
argument_list|(
operator|new
name|Scroll
argument_list|(
name|TimeValue
operator|.
name|parseTimeValue
argument_list|(
name|keepAlive
argument_list|,
literal|null
argument_list|,
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|".keepAlive"
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
DECL|method|timeout
specifier|public
name|TimeValue
name|timeout
parameter_list|()
block|{
return|return
name|timeout
return|;
block|}
DECL|method|timeout
specifier|public
name|DeleteByQueryRequest
name|timeout
parameter_list|(
name|TimeValue
name|timeout
parameter_list|)
block|{
if|if
condition|(
name|timeout
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"timeout must not be null"
argument_list|)
throw|;
block|}
name|this
operator|.
name|timeout
operator|=
name|timeout
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|timeout
specifier|public
name|DeleteByQueryRequest
name|timeout
parameter_list|(
name|String
name|timeout
parameter_list|)
block|{
name|timeout
argument_list|(
name|TimeValue
operator|.
name|parseTimeValue
argument_list|(
name|timeout
argument_list|,
literal|null
argument_list|,
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|".timeout"
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
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
name|indices
operator|=
name|in
operator|.
name|readStringArray
argument_list|()
expr_stmt|;
name|indicesOptions
operator|=
name|IndicesOptions
operator|.
name|readIndicesOptions
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|types
operator|=
name|in
operator|.
name|readStringArray
argument_list|()
expr_stmt|;
name|query
operator|=
name|in
operator|.
name|readQuery
argument_list|()
expr_stmt|;
name|routing
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|size
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|scroll
operator|=
name|readScroll
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|timeout
operator|=
name|TimeValue
operator|.
name|readTimeValue
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
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
name|writeStringArray
argument_list|(
name|indices
argument_list|)
expr_stmt|;
name|indicesOptions
operator|.
name|writeIndicesOptions
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeStringArray
argument_list|(
name|types
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeQuery
argument_list|(
name|query
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
name|writeVInt
argument_list|(
name|size
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalStreamable
argument_list|(
name|scroll
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalStreamable
argument_list|(
name|timeout
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
return|return
literal|"delete-by-query indices:"
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|indices
argument_list|)
operator|+
literal|", types:"
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|types
argument_list|)
operator|+
literal|", size:"
operator|+
name|size
operator|+
literal|", timeout:"
operator|+
name|timeout
operator|+
literal|", routing:"
operator|+
name|routing
operator|+
literal|", query:"
operator|+
name|query
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

