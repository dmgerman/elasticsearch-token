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

begin_comment
comment|/**  * Builder for the 'has_parent' query.  */
end_comment

begin_class
DECL|class|HasParentQueryBuilder
specifier|public
class|class
name|HasParentQueryBuilder
extends|extends
name|AbstractQueryBuilder
argument_list|<
name|HasParentQueryBuilder
argument_list|>
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"has_parent"
decl_stmt|;
DECL|field|queryBuilder
specifier|private
specifier|final
name|QueryBuilder
name|queryBuilder
decl_stmt|;
DECL|field|parentType
specifier|private
specifier|final
name|String
name|parentType
decl_stmt|;
DECL|field|scoreType
specifier|private
name|String
name|scoreType
decl_stmt|;
DECL|field|innerHit
specifier|private
name|QueryInnerHitBuilder
name|innerHit
init|=
literal|null
decl_stmt|;
DECL|field|PROTOTYPE
specifier|static
specifier|final
name|HasParentQueryBuilder
name|PROTOTYPE
init|=
operator|new
name|HasParentQueryBuilder
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|/**      * @param parentType  The parent type      * @param parentQuery The query that will be matched with parent documents      */
DECL|method|HasParentQueryBuilder
specifier|public
name|HasParentQueryBuilder
parameter_list|(
name|String
name|parentType
parameter_list|,
name|QueryBuilder
name|parentQuery
parameter_list|)
block|{
name|this
operator|.
name|parentType
operator|=
name|parentType
expr_stmt|;
name|this
operator|.
name|queryBuilder
operator|=
name|parentQuery
expr_stmt|;
block|}
comment|/**      * Defines how the parent score is mapped into the child documents.      */
DECL|method|scoreType
specifier|public
name|HasParentQueryBuilder
name|scoreType
parameter_list|(
name|String
name|scoreType
parameter_list|)
block|{
name|this
operator|.
name|scoreType
operator|=
name|scoreType
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets inner hit definition in the scope of this query and reusing the defined type and query.      */
DECL|method|innerHit
specifier|public
name|HasParentQueryBuilder
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
name|NAME
argument_list|)
expr_stmt|;
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
name|builder
operator|.
name|field
argument_list|(
literal|"parent_type"
argument_list|,
name|parentType
argument_list|)
expr_stmt|;
if|if
condition|(
name|scoreType
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"score_type"
argument_list|,
name|scoreType
argument_list|)
expr_stmt|;
block|}
name|printBoostAndQueryName
argument_list|(
name|builder
argument_list|)
expr_stmt|;
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
annotation|@
name|Override
DECL|method|getName
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|NAME
return|;
block|}
block|}
end_class

end_unit

