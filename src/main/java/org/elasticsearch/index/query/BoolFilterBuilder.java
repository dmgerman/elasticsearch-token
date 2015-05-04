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
name|ArrayList
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

begin_comment
comment|/**  * A filter that matches documents matching boolean combinations of other filters.  */
end_comment

begin_class
DECL|class|BoolFilterBuilder
specifier|public
class|class
name|BoolFilterBuilder
extends|extends
name|BaseFilterBuilder
block|{
DECL|field|mustClauses
specifier|private
name|ArrayList
argument_list|<
name|FilterBuilder
argument_list|>
name|mustClauses
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|mustNotClauses
specifier|private
name|ArrayList
argument_list|<
name|FilterBuilder
argument_list|>
name|mustNotClauses
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|shouldClauses
specifier|private
name|ArrayList
argument_list|<
name|FilterBuilder
argument_list|>
name|shouldClauses
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|filterName
specifier|private
name|String
name|filterName
decl_stmt|;
comment|/**      * Adds a filter that<b>must</b> appear in the matching documents.      */
DECL|method|must
specifier|public
name|BoolFilterBuilder
name|must
parameter_list|(
name|FilterBuilder
name|filterBuilder
parameter_list|)
block|{
name|mustClauses
operator|.
name|add
argument_list|(
name|filterBuilder
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds a filter that<b>must not</b> appear in the matching documents.      */
DECL|method|mustNot
specifier|public
name|BoolFilterBuilder
name|mustNot
parameter_list|(
name|FilterBuilder
name|filterBuilder
parameter_list|)
block|{
name|mustNotClauses
operator|.
name|add
argument_list|(
name|filterBuilder
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds a filter that<i>should</i> appear in the matching documents. For a boolean filter      * with no<tt>MUST</tt> clauses one or more<code>SHOULD</code> clauses must match a document      * for the BooleanQuery to match.      */
DECL|method|should
specifier|public
name|BoolFilterBuilder
name|should
parameter_list|(
name|FilterBuilder
name|filterBuilder
parameter_list|)
block|{
name|shouldClauses
operator|.
name|add
argument_list|(
name|filterBuilder
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds multiple<i>must</i> filters.      */
DECL|method|must
specifier|public
name|BoolFilterBuilder
name|must
parameter_list|(
name|FilterBuilder
modifier|...
name|filterBuilders
parameter_list|)
block|{
for|for
control|(
name|FilterBuilder
name|fb
range|:
name|filterBuilders
control|)
block|{
name|mustClauses
operator|.
name|add
argument_list|(
name|fb
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
comment|/**      * Adds multiple<i>must not</i> filters.      */
DECL|method|mustNot
specifier|public
name|BoolFilterBuilder
name|mustNot
parameter_list|(
name|FilterBuilder
modifier|...
name|filterBuilders
parameter_list|)
block|{
for|for
control|(
name|FilterBuilder
name|fb
range|:
name|filterBuilders
control|)
block|{
name|mustNotClauses
operator|.
name|add
argument_list|(
name|fb
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
comment|/**      * Adds multiple<i>should</i> filters.      */
DECL|method|should
specifier|public
name|BoolFilterBuilder
name|should
parameter_list|(
name|FilterBuilder
modifier|...
name|filterBuilders
parameter_list|)
block|{
for|for
control|(
name|FilterBuilder
name|fb
range|:
name|filterBuilders
control|)
block|{
name|shouldClauses
operator|.
name|add
argument_list|(
name|fb
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
comment|/**      * Returns<code>true</code> iff this filter builder has at least one should, must or mustNot clause.      * Otherwise<code>false</code>.      */
DECL|method|hasClauses
specifier|public
name|boolean
name|hasClauses
parameter_list|()
block|{
return|return
operator|!
operator|(
name|mustClauses
operator|.
name|isEmpty
argument_list|()
operator|&&
name|shouldClauses
operator|.
name|isEmpty
argument_list|()
operator|&&
name|mustNotClauses
operator|.
name|isEmpty
argument_list|()
operator|)
return|;
block|}
comment|/**      * Sets the filter name for the filter that can be used when searching for matched_filters per hit.      */
DECL|method|filterName
specifier|public
name|BoolFilterBuilder
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
literal|"bool"
argument_list|)
expr_stmt|;
name|doXArrayContent
argument_list|(
literal|"must"
argument_list|,
name|mustClauses
argument_list|,
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|doXArrayContent
argument_list|(
literal|"must_not"
argument_list|,
name|mustNotClauses
argument_list|,
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|doXArrayContent
argument_list|(
literal|"should"
argument_list|,
name|shouldClauses
argument_list|,
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
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
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
DECL|method|doXArrayContent
specifier|private
name|void
name|doXArrayContent
parameter_list|(
name|String
name|field
parameter_list|,
name|List
argument_list|<
name|FilterBuilder
argument_list|>
name|clauses
parameter_list|,
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|clauses
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|clauses
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|field
argument_list|)
expr_stmt|;
name|clauses
operator|.
name|get
argument_list|(
literal|0
argument_list|)
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
name|startArray
argument_list|(
name|field
argument_list|)
expr_stmt|;
for|for
control|(
name|FilterBuilder
name|clause
range|:
name|clauses
control|)
block|{
name|clause
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
name|endArray
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

