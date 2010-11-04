begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.lucene.search
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|search
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
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Field
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
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|Queries
specifier|public
class|class
name|Queries
block|{
comment|// We don't use MatchAllDocsQuery, its slower than the one below ... (much slower)
DECL|field|MATCH_ALL_QUERY
specifier|public
specifier|final
specifier|static
name|Query
name|MATCH_ALL_QUERY
init|=
operator|new
name|DeletionAwareConstantScoreQuery
argument_list|(
operator|new
name|MatchAllDocsFilter
argument_list|()
argument_list|)
decl_stmt|;
comment|/**      * A match all docs filter. Note, requires no caching!.      */
DECL|field|MATCH_ALL_FILTER
specifier|public
specifier|final
specifier|static
name|Filter
name|MATCH_ALL_FILTER
init|=
operator|new
name|MatchAllDocsFilter
argument_list|()
decl_stmt|;
DECL|field|disjuncts
specifier|private
specifier|final
specifier|static
name|Field
name|disjuncts
decl_stmt|;
static|static
block|{
name|Field
name|disjunctsX
decl_stmt|;
try|try
block|{
name|disjunctsX
operator|=
name|DisjunctionMaxQuery
operator|.
name|class
operator|.
name|getDeclaredField
argument_list|(
literal|"disjuncts"
argument_list|)
expr_stmt|;
name|disjunctsX
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|disjunctsX
operator|=
literal|null
expr_stmt|;
block|}
name|disjuncts
operator|=
name|disjunctsX
expr_stmt|;
block|}
DECL|method|disMaxClauses
specifier|public
specifier|static
name|List
argument_list|<
name|Query
argument_list|>
name|disMaxClauses
parameter_list|(
name|DisjunctionMaxQuery
name|query
parameter_list|)
block|{
try|try
block|{
return|return
operator|(
name|List
argument_list|<
name|Query
argument_list|>
operator|)
name|disjuncts
operator|.
name|get
argument_list|(
name|query
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IllegalAccessException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
comment|/**      * Optimizes the given query and returns the optimized version of it.      */
DECL|method|optimizeQuery
specifier|public
specifier|static
name|Query
name|optimizeQuery
parameter_list|(
name|Query
name|q
parameter_list|)
block|{
return|return
name|q
return|;
block|}
DECL|method|isNegativeQuery
specifier|public
specifier|static
name|boolean
name|isNegativeQuery
parameter_list|(
name|Query
name|q
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|q
operator|instanceof
name|BooleanQuery
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|List
argument_list|<
name|BooleanClause
argument_list|>
name|clauses
init|=
operator|(
operator|(
name|BooleanQuery
operator|)
name|q
operator|)
operator|.
name|clauses
argument_list|()
decl_stmt|;
if|if
condition|(
name|clauses
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
for|for
control|(
name|BooleanClause
name|clause
range|:
name|clauses
control|)
block|{
if|if
condition|(
operator|!
name|clause
operator|.
name|isProhibited
argument_list|()
condition|)
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
DECL|method|fixNegativeQueryIfNeeded
specifier|public
specifier|static
name|Query
name|fixNegativeQueryIfNeeded
parameter_list|(
name|Query
name|q
parameter_list|)
block|{
if|if
condition|(
name|isNegativeQuery
argument_list|(
name|q
argument_list|)
condition|)
block|{
name|BooleanQuery
name|newBq
init|=
operator|(
name|BooleanQuery
operator|)
name|q
operator|.
name|clone
argument_list|()
decl_stmt|;
name|newBq
operator|.
name|add
argument_list|(
name|MATCH_ALL_QUERY
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|MUST
argument_list|)
expr_stmt|;
return|return
name|newBq
return|;
block|}
return|return
name|q
return|;
block|}
block|}
end_class

end_unit

