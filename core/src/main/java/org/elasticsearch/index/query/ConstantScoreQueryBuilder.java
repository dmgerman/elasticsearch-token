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
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|Query
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
name|Objects
import|;
end_import

begin_comment
comment|/**  * A query that wraps a filter and simply returns a constant score equal to the  * query boost for every document in the filter.  */
end_comment

begin_class
DECL|class|ConstantScoreQueryBuilder
specifier|public
class|class
name|ConstantScoreQueryBuilder
extends|extends
name|QueryBuilder
implements|implements
name|BoostableQueryBuilder
argument_list|<
name|ConstantScoreQueryBuilder
argument_list|>
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"constant_score"
decl_stmt|;
DECL|field|filterBuilder
specifier|private
specifier|final
name|QueryBuilder
name|filterBuilder
decl_stmt|;
DECL|field|boost
specifier|private
name|float
name|boost
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|PROTOTYPE
specifier|static
specifier|final
name|ConstantScoreQueryBuilder
name|PROTOTYPE
init|=
operator|new
name|ConstantScoreQueryBuilder
argument_list|()
decl_stmt|;
comment|/**      * A query that wraps a query and simply returns a constant score equal to the      * query boost for every document in the query.      *      * @param filterBuilder The query to wrap in a constant score query      */
DECL|method|ConstantScoreQueryBuilder
specifier|public
name|ConstantScoreQueryBuilder
parameter_list|(
name|QueryBuilder
name|filterBuilder
parameter_list|)
block|{
name|this
operator|.
name|filterBuilder
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|filterBuilder
argument_list|)
expr_stmt|;
block|}
comment|/**      * private constructor only used for serialization      */
DECL|method|ConstantScoreQueryBuilder
specifier|private
name|ConstantScoreQueryBuilder
parameter_list|()
block|{
name|this
operator|.
name|filterBuilder
operator|=
literal|null
expr_stmt|;
block|}
comment|/**      * Sets the boost for this query.  Documents matching this query will (in addition to the normal      * weightings) have their score multiplied by the boost provided.      */
annotation|@
name|Override
DECL|method|boost
specifier|public
name|ConstantScoreQueryBuilder
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
name|NAME
argument_list|)
expr_stmt|;
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
if|if
condition|(
name|boost
operator|!=
operator|-
literal|1
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
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|queryId
specifier|public
name|String
name|queryId
parameter_list|()
block|{
return|return
name|NAME
return|;
block|}
block|}
end_class

end_unit
