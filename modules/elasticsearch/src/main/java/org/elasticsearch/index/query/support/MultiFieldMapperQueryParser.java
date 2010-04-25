begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query.support
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|support
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
name|analysis
operator|.
name|Analyzer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|queryParser
operator|.
name|ParseException
import|;
end_import

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
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|cache
operator|.
name|IndexCache
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
name|mapper
operator|.
name|MapperService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|trove
operator|.
name|ExtTObjectFloatHashMap
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|annotation
operator|.
name|Nullable
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
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|MultiFieldMapperQueryParser
specifier|public
class|class
name|MultiFieldMapperQueryParser
extends|extends
name|MapperQueryParser
block|{
DECL|field|fields
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|fields
decl_stmt|;
DECL|field|boosts
specifier|private
name|ExtTObjectFloatHashMap
argument_list|<
name|String
argument_list|>
name|boosts
decl_stmt|;
DECL|field|tieBreaker
specifier|private
name|float
name|tieBreaker
init|=
literal|0.0f
decl_stmt|;
DECL|field|useDisMax
specifier|private
name|boolean
name|useDisMax
init|=
literal|true
decl_stmt|;
DECL|method|MultiFieldMapperQueryParser
specifier|public
name|MultiFieldMapperQueryParser
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|fields
parameter_list|,
annotation|@
name|Nullable
name|ExtTObjectFloatHashMap
argument_list|<
name|String
argument_list|>
name|boosts
parameter_list|,
name|Analyzer
name|analyzer
parameter_list|,
annotation|@
name|Nullable
name|MapperService
name|mapperService
parameter_list|,
annotation|@
name|Nullable
name|IndexCache
name|indexCache
parameter_list|)
block|{
name|super
argument_list|(
literal|null
argument_list|,
name|analyzer
argument_list|,
name|mapperService
argument_list|,
name|indexCache
argument_list|)
expr_stmt|;
name|this
operator|.
name|fields
operator|=
name|fields
expr_stmt|;
name|this
operator|.
name|boosts
operator|=
name|boosts
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|boosts
operator|!=
literal|null
condition|)
block|{
name|boosts
operator|.
name|defaultReturnValue
argument_list|(
literal|1.0f
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|setTieBreaker
specifier|public
name|void
name|setTieBreaker
parameter_list|(
name|float
name|tieBreaker
parameter_list|)
block|{
name|this
operator|.
name|tieBreaker
operator|=
name|tieBreaker
expr_stmt|;
block|}
DECL|method|setUseDisMax
specifier|public
name|void
name|setUseDisMax
parameter_list|(
name|boolean
name|useDisMax
parameter_list|)
block|{
name|this
operator|.
name|useDisMax
operator|=
name|useDisMax
expr_stmt|;
block|}
DECL|method|getFieldQuery
annotation|@
name|Override
specifier|public
name|Query
name|getFieldQuery
parameter_list|(
name|String
name|field
parameter_list|,
name|String
name|queryText
parameter_list|)
throws|throws
name|ParseException
block|{
return|return
name|getFieldQuery
argument_list|(
name|field
argument_list|,
name|queryText
argument_list|,
literal|0
argument_list|)
return|;
block|}
DECL|method|getFieldQuery
annotation|@
name|Override
specifier|public
name|Query
name|getFieldQuery
parameter_list|(
name|String
name|xField
parameter_list|,
name|String
name|queryText
parameter_list|,
name|int
name|slop
parameter_list|)
throws|throws
name|ParseException
block|{
if|if
condition|(
name|xField
operator|!=
literal|null
condition|)
block|{
name|Query
name|q
init|=
name|super
operator|.
name|getFieldQuery
argument_list|(
name|xField
argument_list|,
name|queryText
argument_list|)
decl_stmt|;
name|applySlop
argument_list|(
name|q
argument_list|,
name|slop
argument_list|)
expr_stmt|;
return|return
name|q
return|;
block|}
if|if
condition|(
name|useDisMax
condition|)
block|{
name|DisjunctionMaxQuery
name|disMaxQuery
init|=
operator|new
name|DisjunctionMaxQuery
argument_list|(
name|tieBreaker
argument_list|)
decl_stmt|;
name|boolean
name|added
init|=
literal|false
decl_stmt|;
for|for
control|(
name|String
name|field
range|:
name|fields
control|)
block|{
name|Query
name|q
init|=
name|super
operator|.
name|getFieldQuery
argument_list|(
name|field
argument_list|,
name|queryText
argument_list|)
decl_stmt|;
if|if
condition|(
name|q
operator|!=
literal|null
condition|)
block|{
name|added
operator|=
literal|true
expr_stmt|;
name|applyBoost
argument_list|(
name|field
argument_list|,
name|q
argument_list|)
expr_stmt|;
name|applySlop
argument_list|(
name|q
argument_list|,
name|slop
argument_list|)
expr_stmt|;
name|disMaxQuery
operator|.
name|add
argument_list|(
name|q
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|added
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|disMaxQuery
return|;
block|}
else|else
block|{
name|List
argument_list|<
name|BooleanClause
argument_list|>
name|clauses
init|=
operator|new
name|ArrayList
argument_list|<
name|BooleanClause
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|field
range|:
name|fields
control|)
block|{
name|Query
name|q
init|=
name|super
operator|.
name|getFieldQuery
argument_list|(
name|field
argument_list|,
name|queryText
argument_list|)
decl_stmt|;
if|if
condition|(
name|q
operator|!=
literal|null
condition|)
block|{
name|applyBoost
argument_list|(
name|field
argument_list|,
name|q
argument_list|)
expr_stmt|;
name|applySlop
argument_list|(
name|q
argument_list|,
name|slop
argument_list|)
expr_stmt|;
name|clauses
operator|.
name|add
argument_list|(
operator|new
name|BooleanClause
argument_list|(
name|q
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|clauses
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
comment|// happens for stopwords
return|return
literal|null
return|;
return|return
name|getBooleanQuery
argument_list|(
name|clauses
argument_list|,
literal|true
argument_list|)
return|;
block|}
block|}
DECL|method|getRangeQuery
annotation|@
name|Override
specifier|protected
name|Query
name|getRangeQuery
parameter_list|(
name|String
name|xField
parameter_list|,
name|String
name|part1
parameter_list|,
name|String
name|part2
parameter_list|,
name|boolean
name|inclusive
parameter_list|)
throws|throws
name|ParseException
block|{
if|if
condition|(
name|xField
operator|!=
literal|null
condition|)
block|{
return|return
name|super
operator|.
name|getRangeQuery
argument_list|(
name|xField
argument_list|,
name|part1
argument_list|,
name|part2
argument_list|,
name|inclusive
argument_list|)
return|;
block|}
if|if
condition|(
name|useDisMax
condition|)
block|{
name|DisjunctionMaxQuery
name|disMaxQuery
init|=
operator|new
name|DisjunctionMaxQuery
argument_list|(
name|tieBreaker
argument_list|)
decl_stmt|;
name|boolean
name|added
init|=
literal|false
decl_stmt|;
for|for
control|(
name|String
name|field
range|:
name|fields
control|)
block|{
name|Query
name|q
init|=
name|super
operator|.
name|getRangeQuery
argument_list|(
name|field
argument_list|,
name|part1
argument_list|,
name|part2
argument_list|,
name|inclusive
argument_list|)
decl_stmt|;
if|if
condition|(
name|q
operator|!=
literal|null
condition|)
block|{
name|added
operator|=
literal|true
expr_stmt|;
name|applyBoost
argument_list|(
name|field
argument_list|,
name|q
argument_list|)
expr_stmt|;
name|disMaxQuery
operator|.
name|add
argument_list|(
name|q
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|added
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|disMaxQuery
return|;
block|}
else|else
block|{
name|List
argument_list|<
name|BooleanClause
argument_list|>
name|clauses
init|=
operator|new
name|ArrayList
argument_list|<
name|BooleanClause
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|field
range|:
name|fields
control|)
block|{
name|Query
name|q
init|=
name|super
operator|.
name|getRangeQuery
argument_list|(
name|field
argument_list|,
name|part1
argument_list|,
name|part2
argument_list|,
name|inclusive
argument_list|)
decl_stmt|;
if|if
condition|(
name|q
operator|!=
literal|null
condition|)
block|{
name|applyBoost
argument_list|(
name|field
argument_list|,
name|q
argument_list|)
expr_stmt|;
name|clauses
operator|.
name|add
argument_list|(
operator|new
name|BooleanClause
argument_list|(
name|q
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|clauses
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
comment|// happens for stopwords
return|return
literal|null
return|;
return|return
name|getBooleanQuery
argument_list|(
name|clauses
argument_list|,
literal|true
argument_list|)
return|;
block|}
block|}
DECL|method|getPrefixQuery
annotation|@
name|Override
specifier|protected
name|Query
name|getPrefixQuery
parameter_list|(
name|String
name|xField
parameter_list|,
name|String
name|termStr
parameter_list|)
throws|throws
name|ParseException
block|{
if|if
condition|(
name|xField
operator|!=
literal|null
condition|)
block|{
return|return
name|super
operator|.
name|getPrefixQuery
argument_list|(
name|xField
argument_list|,
name|termStr
argument_list|)
return|;
block|}
if|if
condition|(
name|useDisMax
condition|)
block|{
name|DisjunctionMaxQuery
name|disMaxQuery
init|=
operator|new
name|DisjunctionMaxQuery
argument_list|(
name|tieBreaker
argument_list|)
decl_stmt|;
name|boolean
name|added
init|=
literal|false
decl_stmt|;
for|for
control|(
name|String
name|field
range|:
name|fields
control|)
block|{
name|Query
name|q
init|=
name|super
operator|.
name|getPrefixQuery
argument_list|(
name|field
argument_list|,
name|termStr
argument_list|)
decl_stmt|;
if|if
condition|(
name|q
operator|!=
literal|null
condition|)
block|{
name|added
operator|=
literal|true
expr_stmt|;
name|applyBoost
argument_list|(
name|field
argument_list|,
name|q
argument_list|)
expr_stmt|;
name|disMaxQuery
operator|.
name|add
argument_list|(
name|q
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|added
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|disMaxQuery
return|;
block|}
else|else
block|{
name|List
argument_list|<
name|BooleanClause
argument_list|>
name|clauses
init|=
operator|new
name|ArrayList
argument_list|<
name|BooleanClause
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|field
range|:
name|fields
control|)
block|{
name|Query
name|q
init|=
name|super
operator|.
name|getPrefixQuery
argument_list|(
name|field
argument_list|,
name|termStr
argument_list|)
decl_stmt|;
if|if
condition|(
name|q
operator|!=
literal|null
condition|)
block|{
name|applyBoost
argument_list|(
name|field
argument_list|,
name|q
argument_list|)
expr_stmt|;
name|clauses
operator|.
name|add
argument_list|(
operator|new
name|BooleanClause
argument_list|(
name|q
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|clauses
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
comment|// happens for stopwords
return|return
literal|null
return|;
return|return
name|getBooleanQuery
argument_list|(
name|clauses
argument_list|,
literal|true
argument_list|)
return|;
block|}
block|}
DECL|method|getWildcardQuery
annotation|@
name|Override
specifier|protected
name|Query
name|getWildcardQuery
parameter_list|(
name|String
name|xField
parameter_list|,
name|String
name|termStr
parameter_list|)
throws|throws
name|ParseException
block|{
if|if
condition|(
name|xField
operator|!=
literal|null
condition|)
block|{
return|return
name|super
operator|.
name|getWildcardQuery
argument_list|(
name|xField
argument_list|,
name|termStr
argument_list|)
return|;
block|}
if|if
condition|(
name|useDisMax
condition|)
block|{
name|DisjunctionMaxQuery
name|disMaxQuery
init|=
operator|new
name|DisjunctionMaxQuery
argument_list|(
name|tieBreaker
argument_list|)
decl_stmt|;
name|boolean
name|added
init|=
literal|false
decl_stmt|;
for|for
control|(
name|String
name|field
range|:
name|fields
control|)
block|{
name|Query
name|q
init|=
name|super
operator|.
name|getWildcardQuery
argument_list|(
name|field
argument_list|,
name|termStr
argument_list|)
decl_stmt|;
if|if
condition|(
name|q
operator|!=
literal|null
condition|)
block|{
name|added
operator|=
literal|true
expr_stmt|;
name|applyBoost
argument_list|(
name|field
argument_list|,
name|q
argument_list|)
expr_stmt|;
name|disMaxQuery
operator|.
name|add
argument_list|(
name|q
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|added
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|disMaxQuery
return|;
block|}
else|else
block|{
name|List
argument_list|<
name|BooleanClause
argument_list|>
name|clauses
init|=
operator|new
name|ArrayList
argument_list|<
name|BooleanClause
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|field
range|:
name|fields
control|)
block|{
name|Query
name|q
init|=
name|super
operator|.
name|getWildcardQuery
argument_list|(
name|field
argument_list|,
name|termStr
argument_list|)
decl_stmt|;
if|if
condition|(
name|q
operator|!=
literal|null
condition|)
block|{
name|applyBoost
argument_list|(
name|field
argument_list|,
name|q
argument_list|)
expr_stmt|;
name|clauses
operator|.
name|add
argument_list|(
operator|new
name|BooleanClause
argument_list|(
name|q
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|clauses
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
comment|// happens for stopwords
return|return
literal|null
return|;
return|return
name|getBooleanQuery
argument_list|(
name|clauses
argument_list|,
literal|true
argument_list|)
return|;
block|}
block|}
DECL|method|getFuzzyQuery
annotation|@
name|Override
specifier|protected
name|Query
name|getFuzzyQuery
parameter_list|(
name|String
name|xField
parameter_list|,
name|String
name|termStr
parameter_list|,
name|float
name|minSimilarity
parameter_list|)
throws|throws
name|ParseException
block|{
if|if
condition|(
name|xField
operator|!=
literal|null
condition|)
block|{
return|return
name|super
operator|.
name|getFuzzyQuery
argument_list|(
name|xField
argument_list|,
name|termStr
argument_list|,
name|minSimilarity
argument_list|)
return|;
block|}
if|if
condition|(
name|useDisMax
condition|)
block|{
name|DisjunctionMaxQuery
name|disMaxQuery
init|=
operator|new
name|DisjunctionMaxQuery
argument_list|(
name|tieBreaker
argument_list|)
decl_stmt|;
name|boolean
name|added
init|=
literal|false
decl_stmt|;
for|for
control|(
name|String
name|field
range|:
name|fields
control|)
block|{
name|Query
name|q
init|=
name|super
operator|.
name|getFuzzyQuery
argument_list|(
name|field
argument_list|,
name|termStr
argument_list|,
name|minSimilarity
argument_list|)
decl_stmt|;
if|if
condition|(
name|q
operator|!=
literal|null
condition|)
block|{
name|added
operator|=
literal|true
expr_stmt|;
name|applyBoost
argument_list|(
name|field
argument_list|,
name|q
argument_list|)
expr_stmt|;
name|disMaxQuery
operator|.
name|add
argument_list|(
name|q
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|added
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|disMaxQuery
return|;
block|}
else|else
block|{
name|List
argument_list|<
name|BooleanClause
argument_list|>
name|clauses
init|=
operator|new
name|ArrayList
argument_list|<
name|BooleanClause
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|field
range|:
name|fields
control|)
block|{
name|Query
name|q
init|=
name|super
operator|.
name|getFuzzyQuery
argument_list|(
name|field
argument_list|,
name|termStr
argument_list|,
name|minSimilarity
argument_list|)
decl_stmt|;
name|applyBoost
argument_list|(
name|field
argument_list|,
name|q
argument_list|)
expr_stmt|;
name|clauses
operator|.
name|add
argument_list|(
operator|new
name|BooleanClause
argument_list|(
name|q
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|getBooleanQuery
argument_list|(
name|clauses
argument_list|,
literal|true
argument_list|)
return|;
block|}
block|}
DECL|method|applyBoost
specifier|private
name|void
name|applyBoost
parameter_list|(
name|String
name|field
parameter_list|,
name|Query
name|q
parameter_list|)
block|{
if|if
condition|(
name|boosts
operator|!=
literal|null
condition|)
block|{
name|float
name|boost
init|=
name|boosts
operator|.
name|get
argument_list|(
name|field
argument_list|)
decl_stmt|;
name|q
operator|.
name|setBoost
argument_list|(
name|boost
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|applySlop
specifier|private
name|void
name|applySlop
parameter_list|(
name|Query
name|q
parameter_list|,
name|int
name|slop
parameter_list|)
block|{
if|if
condition|(
name|q
operator|instanceof
name|PhraseQuery
condition|)
block|{
operator|(
operator|(
name|PhraseQuery
operator|)
name|q
operator|)
operator|.
name|setSlop
argument_list|(
name|slop
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|q
operator|instanceof
name|MultiPhraseQuery
condition|)
block|{
operator|(
operator|(
name|MultiPhraseQuery
operator|)
name|q
operator|)
operator|.
name|setSlop
argument_list|(
name|slop
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

