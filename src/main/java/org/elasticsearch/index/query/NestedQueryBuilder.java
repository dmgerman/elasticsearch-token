begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
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
name|index
operator|.
name|query
operator|.
name|support
operator|.
name|QueryInnerHitBuilder
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

begin_class
DECL|class|NestedQueryBuilder
specifier|public
class|class
name|NestedQueryBuilder
extends|extends
name|BaseQueryBuilder
implements|implements
name|BoostableQueryBuilder
argument_list|<
name|NestedQueryBuilder
argument_list|>
block|{
DECL|field|queryBuilder
specifier|private
specifier|final
name|QueryBuilder
name|queryBuilder
decl_stmt|;
DECL|field|filterBuilder
specifier|private
specifier|final
name|FilterBuilder
name|filterBuilder
decl_stmt|;
DECL|field|path
specifier|private
specifier|final
name|String
name|path
decl_stmt|;
DECL|field|scoreMode
specifier|private
name|String
name|scoreMode
decl_stmt|;
DECL|field|boost
specifier|private
name|float
name|boost
init|=
literal|1.0f
decl_stmt|;
DECL|field|queryName
specifier|private
name|String
name|queryName
decl_stmt|;
DECL|field|innerHit
specifier|private
name|QueryInnerHitBuilder
name|innerHit
decl_stmt|;
DECL|method|NestedQueryBuilder
specifier|public
name|NestedQueryBuilder
parameter_list|(
name|String
name|path
parameter_list|,
name|QueryBuilder
name|queryBuilder
parameter_list|)
block|{
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
name|this
operator|.
name|queryBuilder
operator|=
name|queryBuilder
expr_stmt|;
name|this
operator|.
name|filterBuilder
operator|=
literal|null
expr_stmt|;
block|}
DECL|method|NestedQueryBuilder
specifier|public
name|NestedQueryBuilder
parameter_list|(
name|String
name|path
parameter_list|,
name|FilterBuilder
name|filterBuilder
parameter_list|)
block|{
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
name|this
operator|.
name|queryBuilder
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|filterBuilder
operator|=
name|filterBuilder
expr_stmt|;
block|}
comment|/**      * The score mode.      */
DECL|method|scoreMode
specifier|public
name|NestedQueryBuilder
name|scoreMode
parameter_list|(
name|String
name|scoreMode
parameter_list|)
block|{
name|this
operator|.
name|scoreMode
operator|=
name|scoreMode
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the boost for this query.  Documents matching this query will (in addition to the normal      * weightings) have their score multiplied by the boost provided.      */
annotation|@
name|Override
DECL|method|boost
specifier|public
name|NestedQueryBuilder
name|boost
parameter_list|(
name|float
name|boost
parameter_list|)
block|{
name|this
operator|.
name|boost
operator|=
name|boost
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the query name for the filter that can be used when searching for matched_filters per hit.      */
DECL|method|queryName
specifier|public
name|NestedQueryBuilder
name|queryName
parameter_list|(
name|String
name|queryName
parameter_list|)
block|{
name|this
operator|.
name|queryName
operator|=
name|queryName
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets inner hit definition in the scope of this nested query and reusing the defined path and query.      */
DECL|method|innerHit
specifier|public
name|NestedQueryBuilder
name|innerHit
parameter_list|(
name|QueryInnerHitBuilder
name|innerHit
parameter_list|)
block|{
name|this
operator|.
name|innerHit
operator|=
name|innerHit
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|doXContent
specifier|protected
name|void
name|doXContent
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
name|builder
operator|.
name|startObject
argument_list|(
name|NestedQueryParser
operator|.
name|NAME
argument_list|)
expr_stmt|;
if|if
condition|(
name|queryBuilder
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"query"
argument_list|)
expr_stmt|;
name|queryBuilder
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"filter"
argument_list|)
expr_stmt|;
name|filterBuilder
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|field
argument_list|(
literal|"path"
argument_list|,
name|path
argument_list|)
expr_stmt|;
if|if
condition|(
name|scoreMode
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"score_mode"
argument_list|,
name|scoreMode
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|boost
operator|!=
literal|1.0f
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"boost"
argument_list|,
name|boost
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|queryName
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"_name"
argument_list|,
name|queryName
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|innerHit
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
literal|"inner_hits"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|value
argument_list|(
name|innerHit
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

