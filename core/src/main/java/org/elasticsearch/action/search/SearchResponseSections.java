begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.search
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
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
name|xcontent
operator|.
name|ToXContent
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
name|search
operator|.
name|SearchHits
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
name|aggregations
operator|.
name|Aggregations
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
name|profile
operator|.
name|ProfileShardResult
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
name|profile
operator|.
name|SearchProfileShardResults
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
name|suggest
operator|.
name|Suggest
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
name|Collections
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
comment|/**  * Base class that holds the various sections which a search response is  * composed of (hits, aggs, suggestions etc.) and allows to retrieve them.  *  * The reason why this class exists is that the high level REST client uses its own classes  * to parse aggregations into, which are not serializable. This is the common part that can be  * shared between core and client.  */
end_comment

begin_class
DECL|class|SearchResponseSections
specifier|public
class|class
name|SearchResponseSections
implements|implements
name|ToXContent
block|{
DECL|field|hits
specifier|protected
specifier|final
name|SearchHits
name|hits
decl_stmt|;
DECL|field|aggregations
specifier|protected
specifier|final
name|Aggregations
name|aggregations
decl_stmt|;
DECL|field|suggest
specifier|protected
specifier|final
name|Suggest
name|suggest
decl_stmt|;
DECL|field|profileResults
specifier|protected
specifier|final
name|SearchProfileShardResults
name|profileResults
decl_stmt|;
DECL|field|timedOut
specifier|protected
specifier|final
name|boolean
name|timedOut
decl_stmt|;
DECL|field|terminatedEarly
specifier|protected
specifier|final
name|Boolean
name|terminatedEarly
decl_stmt|;
DECL|field|numReducePhases
specifier|protected
specifier|final
name|int
name|numReducePhases
decl_stmt|;
DECL|method|SearchResponseSections
specifier|public
name|SearchResponseSections
parameter_list|(
name|SearchHits
name|hits
parameter_list|,
name|Aggregations
name|aggregations
parameter_list|,
name|Suggest
name|suggest
parameter_list|,
name|boolean
name|timedOut
parameter_list|,
name|Boolean
name|terminatedEarly
parameter_list|,
name|SearchProfileShardResults
name|profileResults
parameter_list|,
name|int
name|numReducePhases
parameter_list|)
block|{
name|this
operator|.
name|hits
operator|=
name|hits
expr_stmt|;
name|this
operator|.
name|aggregations
operator|=
name|aggregations
expr_stmt|;
name|this
operator|.
name|suggest
operator|=
name|suggest
expr_stmt|;
name|this
operator|.
name|profileResults
operator|=
name|profileResults
expr_stmt|;
name|this
operator|.
name|timedOut
operator|=
name|timedOut
expr_stmt|;
name|this
operator|.
name|terminatedEarly
operator|=
name|terminatedEarly
expr_stmt|;
name|this
operator|.
name|numReducePhases
operator|=
name|numReducePhases
expr_stmt|;
block|}
DECL|method|timedOut
specifier|public
specifier|final
name|boolean
name|timedOut
parameter_list|()
block|{
return|return
name|this
operator|.
name|timedOut
return|;
block|}
DECL|method|terminatedEarly
specifier|public
specifier|final
name|Boolean
name|terminatedEarly
parameter_list|()
block|{
return|return
name|this
operator|.
name|terminatedEarly
return|;
block|}
DECL|method|hits
specifier|public
specifier|final
name|SearchHits
name|hits
parameter_list|()
block|{
return|return
name|hits
return|;
block|}
DECL|method|aggregations
specifier|public
specifier|final
name|Aggregations
name|aggregations
parameter_list|()
block|{
return|return
name|aggregations
return|;
block|}
DECL|method|suggest
specifier|public
specifier|final
name|Suggest
name|suggest
parameter_list|()
block|{
return|return
name|suggest
return|;
block|}
comment|/**      * Returns the number of reduce phases applied to obtain this search response      */
DECL|method|getNumReducePhases
specifier|public
specifier|final
name|int
name|getNumReducePhases
parameter_list|()
block|{
return|return
name|numReducePhases
return|;
block|}
comment|/**      * Returns the profile results for this search response (including all shards).      * An empty map is returned if profiling was not enabled      *      * @return Profile results      */
DECL|method|profile
specifier|public
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|ProfileShardResult
argument_list|>
name|profile
parameter_list|()
block|{
if|if
condition|(
name|profileResults
operator|==
literal|null
condition|)
block|{
return|return
name|Collections
operator|.
name|emptyMap
argument_list|()
return|;
block|}
return|return
name|profileResults
operator|.
name|getShardResults
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
specifier|final
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|hits
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
if|if
condition|(
name|aggregations
operator|!=
literal|null
condition|)
block|{
name|aggregations
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|suggest
operator|!=
literal|null
condition|)
block|{
name|suggest
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|profileResults
operator|!=
literal|null
condition|)
block|{
name|profileResults
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
DECL|method|writeTo
specifier|protected
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
end_class

end_unit

