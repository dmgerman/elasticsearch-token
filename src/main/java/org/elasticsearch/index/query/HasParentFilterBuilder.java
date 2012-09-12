begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * Builder for the 'has_parent' filter.  */
end_comment

begin_class
DECL|class|HasParentFilterBuilder
specifier|public
class|class
name|HasParentFilterBuilder
extends|extends
name|BaseFilterBuilder
block|{
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
DECL|field|scope
specifier|private
name|String
name|scope
decl_stmt|;
DECL|field|filterName
specifier|private
name|String
name|filterName
decl_stmt|;
DECL|field|executionType
specifier|private
name|String
name|executionType
decl_stmt|;
comment|/**      * @param parentType The parent type      * @param parentQuery The query that will be matched with parent documents      */
DECL|method|HasParentFilterBuilder
specifier|public
name|HasParentFilterBuilder
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
DECL|method|scope
specifier|public
name|HasParentFilterBuilder
name|scope
parameter_list|(
name|String
name|scope
parameter_list|)
block|{
name|this
operator|.
name|scope
operator|=
name|scope
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|filterName
specifier|public
name|HasParentFilterBuilder
name|filterName
parameter_list|(
name|String
name|filterName
parameter_list|)
block|{
name|this
operator|.
name|filterName
operator|=
name|filterName
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Expert: Sets the low level parent to child filtering implementation. Can be: 'indirect' or 'uid'      *      * This option is experimental and will be removed.      */
DECL|method|executionType
specifier|public
name|HasParentFilterBuilder
name|executionType
parameter_list|(
name|String
name|executionType
parameter_list|)
block|{
name|this
operator|.
name|executionType
operator|=
name|executionType
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
name|HasParentFilterParser
operator|.
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
name|scope
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"_scope"
argument_list|,
name|scope
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|filterName
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
name|filterName
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|executionType
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"execution_type"
argument_list|,
name|executionType
argument_list|)
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

