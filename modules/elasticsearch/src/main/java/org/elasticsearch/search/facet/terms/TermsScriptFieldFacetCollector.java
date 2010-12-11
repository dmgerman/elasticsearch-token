begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.facet.terms
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facet
operator|.
name|terms
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
name|index
operator|.
name|IndexReader
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
name|collect
operator|.
name|BoundedTreeSet
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
name|collect
operator|.
name|ImmutableList
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
name|collect
operator|.
name|ImmutableSet
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
name|collect
operator|.
name|Maps
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
name|trove
operator|.
name|TObjectIntHashMap
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
name|trove
operator|.
name|TObjectIntIterator
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
name|field
operator|.
name|data
operator|.
name|FieldData
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|search
operator|.
name|SearchScript
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
name|facet
operator|.
name|Facet
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
name|facet
operator|.
name|support
operator|.
name|AbstractFacetCollector
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
name|internal
operator|.
name|SearchContext
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|TermsScriptFieldFacetCollector
specifier|public
class|class
name|TermsScriptFieldFacetCollector
extends|extends
name|AbstractFacetCollector
block|{
DECL|field|comparatorType
specifier|private
specifier|final
name|InternalTermsFacet
operator|.
name|ComparatorType
name|comparatorType
decl_stmt|;
DECL|field|size
specifier|private
specifier|final
name|int
name|size
decl_stmt|;
DECL|field|numberOfShards
specifier|private
specifier|final
name|int
name|numberOfShards
decl_stmt|;
DECL|field|sScript
specifier|private
specifier|final
name|String
name|sScript
decl_stmt|;
DECL|field|script
specifier|private
specifier|final
name|SearchScript
name|script
decl_stmt|;
DECL|field|matcher
specifier|private
specifier|final
name|Matcher
name|matcher
decl_stmt|;
DECL|field|excluded
specifier|private
specifier|final
name|ImmutableSet
argument_list|<
name|String
argument_list|>
name|excluded
decl_stmt|;
DECL|field|facets
specifier|private
specifier|final
name|TObjectIntHashMap
argument_list|<
name|String
argument_list|>
name|facets
decl_stmt|;
DECL|method|TermsScriptFieldFacetCollector
specifier|public
name|TermsScriptFieldFacetCollector
parameter_list|(
name|String
name|facetName
parameter_list|,
name|int
name|size
parameter_list|,
name|InternalTermsFacet
operator|.
name|ComparatorType
name|comparatorType
parameter_list|,
name|SearchContext
name|context
parameter_list|,
name|ImmutableSet
argument_list|<
name|String
argument_list|>
name|excluded
parameter_list|,
name|Pattern
name|pattern
parameter_list|,
name|String
name|scriptLang
parameter_list|,
name|String
name|script
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
parameter_list|)
block|{
name|super
argument_list|(
name|facetName
argument_list|)
expr_stmt|;
name|this
operator|.
name|size
operator|=
name|size
expr_stmt|;
name|this
operator|.
name|comparatorType
operator|=
name|comparatorType
expr_stmt|;
name|this
operator|.
name|numberOfShards
operator|=
name|context
operator|.
name|numberOfShards
argument_list|()
expr_stmt|;
name|this
operator|.
name|sScript
operator|=
name|script
expr_stmt|;
name|this
operator|.
name|script
operator|=
operator|new
name|SearchScript
argument_list|(
name|context
operator|.
name|lookup
argument_list|()
argument_list|,
name|scriptLang
argument_list|,
name|script
argument_list|,
name|params
argument_list|,
name|context
operator|.
name|scriptService
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|excluded
operator|=
name|excluded
expr_stmt|;
name|this
operator|.
name|matcher
operator|=
name|pattern
operator|!=
literal|null
condition|?
name|pattern
operator|.
name|matcher
argument_list|(
literal|""
argument_list|)
else|:
literal|null
expr_stmt|;
name|this
operator|.
name|facets
operator|=
name|TermsFacetCollector
operator|.
name|popFacets
argument_list|()
expr_stmt|;
block|}
DECL|method|doSetNextReader
annotation|@
name|Override
specifier|protected
name|void
name|doSetNextReader
parameter_list|(
name|IndexReader
name|reader
parameter_list|,
name|int
name|docBase
parameter_list|)
throws|throws
name|IOException
block|{
name|script
operator|.
name|setNextReader
argument_list|(
name|reader
argument_list|)
expr_stmt|;
block|}
DECL|method|doCollect
annotation|@
name|Override
specifier|protected
name|void
name|doCollect
parameter_list|(
name|int
name|doc
parameter_list|)
throws|throws
name|IOException
block|{
name|Object
name|o
init|=
name|script
operator|.
name|execute
argument_list|(
name|doc
argument_list|)
decl_stmt|;
if|if
condition|(
name|o
operator|==
literal|null
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|o
operator|instanceof
name|Iterable
condition|)
block|{
for|for
control|(
name|Object
name|o1
range|:
operator|(
operator|(
name|Iterable
operator|)
name|o
operator|)
control|)
block|{
name|String
name|value
init|=
name|o1
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|match
argument_list|(
name|value
argument_list|)
condition|)
block|{
name|facets
operator|.
name|adjustOrPutValue
argument_list|(
name|value
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|o
operator|instanceof
name|Object
index|[]
condition|)
block|{
for|for
control|(
name|Object
name|o1
range|:
operator|(
operator|(
name|Object
index|[]
operator|)
name|o
operator|)
control|)
block|{
name|String
name|value
init|=
name|o1
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|match
argument_list|(
name|value
argument_list|)
condition|)
block|{
name|facets
operator|.
name|adjustOrPutValue
argument_list|(
name|value
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|String
name|value
init|=
name|o
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|match
argument_list|(
name|value
argument_list|)
condition|)
block|{
name|facets
operator|.
name|adjustOrPutValue
argument_list|(
name|value
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|match
specifier|private
name|boolean
name|match
parameter_list|(
name|String
name|value
parameter_list|)
block|{
if|if
condition|(
name|excluded
operator|!=
literal|null
operator|&&
name|excluded
operator|.
name|contains
argument_list|(
name|value
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|matcher
operator|!=
literal|null
operator|&&
operator|!
name|matcher
operator|.
name|reset
argument_list|(
name|value
argument_list|)
operator|.
name|matches
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
DECL|method|facet
annotation|@
name|Override
specifier|public
name|Facet
name|facet
parameter_list|()
block|{
if|if
condition|(
name|facets
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|TermsFacetCollector
operator|.
name|pushFacets
argument_list|(
name|facets
argument_list|)
expr_stmt|;
return|return
operator|new
name|InternalTermsFacet
argument_list|(
name|facetName
argument_list|,
name|sScript
argument_list|,
name|comparatorType
argument_list|,
name|size
argument_list|,
name|ImmutableList
operator|.
expr|<
name|InternalTermsFacet
operator|.
name|Entry
operator|>
name|of
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
comment|// we need to fetch facets of "size * numberOfShards" because of problems in how they are distributed across shards
name|BoundedTreeSet
argument_list|<
name|InternalTermsFacet
operator|.
name|Entry
argument_list|>
name|ordered
init|=
operator|new
name|BoundedTreeSet
argument_list|<
name|InternalTermsFacet
operator|.
name|Entry
argument_list|>
argument_list|(
name|InternalTermsFacet
operator|.
name|ComparatorType
operator|.
name|COUNT
operator|.
name|comparator
argument_list|()
argument_list|,
name|size
operator|*
name|numberOfShards
argument_list|)
decl_stmt|;
for|for
control|(
name|TObjectIntIterator
argument_list|<
name|String
argument_list|>
name|it
init|=
name|facets
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|it
operator|.
name|advance
argument_list|()
expr_stmt|;
name|ordered
operator|.
name|add
argument_list|(
operator|new
name|InternalTermsFacet
operator|.
name|Entry
argument_list|(
name|it
operator|.
name|key
argument_list|()
argument_list|,
name|it
operator|.
name|value
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|TermsFacetCollector
operator|.
name|pushFacets
argument_list|(
name|facets
argument_list|)
expr_stmt|;
return|return
operator|new
name|InternalTermsFacet
argument_list|(
name|facetName
argument_list|,
name|sScript
argument_list|,
name|comparatorType
argument_list|,
name|size
argument_list|,
name|ordered
argument_list|)
return|;
block|}
block|}
DECL|class|AggregatorValueProc
specifier|public
specifier|static
class|class
name|AggregatorValueProc
extends|extends
name|StaticAggregatorValueProc
block|{
DECL|field|excluded
specifier|private
specifier|final
name|ImmutableSet
argument_list|<
name|String
argument_list|>
name|excluded
decl_stmt|;
DECL|field|matcher
specifier|private
specifier|final
name|Matcher
name|matcher
decl_stmt|;
DECL|field|script
specifier|private
specifier|final
name|SearchScript
name|script
decl_stmt|;
DECL|field|scriptParams
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|scriptParams
decl_stmt|;
DECL|method|AggregatorValueProc
specifier|public
name|AggregatorValueProc
parameter_list|(
name|TObjectIntHashMap
argument_list|<
name|String
argument_list|>
name|facets
parameter_list|,
name|ImmutableSet
argument_list|<
name|String
argument_list|>
name|excluded
parameter_list|,
name|Pattern
name|pattern
parameter_list|,
name|SearchScript
name|script
parameter_list|)
block|{
name|super
argument_list|(
name|facets
argument_list|)
expr_stmt|;
name|this
operator|.
name|excluded
operator|=
name|excluded
expr_stmt|;
name|this
operator|.
name|matcher
operator|=
name|pattern
operator|!=
literal|null
condition|?
name|pattern
operator|.
name|matcher
argument_list|(
literal|""
argument_list|)
else|:
literal|null
expr_stmt|;
name|this
operator|.
name|script
operator|=
name|script
expr_stmt|;
if|if
condition|(
name|script
operator|!=
literal|null
condition|)
block|{
name|scriptParams
operator|=
name|Maps
operator|.
name|newHashMapWithExpectedSize
argument_list|(
literal|4
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|scriptParams
operator|=
literal|null
expr_stmt|;
block|}
block|}
DECL|method|onValue
annotation|@
name|Override
specifier|public
name|void
name|onValue
parameter_list|(
name|int
name|docId
parameter_list|,
name|String
name|value
parameter_list|)
block|{
if|if
condition|(
name|excluded
operator|!=
literal|null
operator|&&
name|excluded
operator|.
name|contains
argument_list|(
name|value
argument_list|)
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|matcher
operator|!=
literal|null
operator|&&
operator|!
name|matcher
operator|.
name|reset
argument_list|(
name|value
argument_list|)
operator|.
name|matches
argument_list|()
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|script
operator|!=
literal|null
condition|)
block|{
name|scriptParams
operator|.
name|put
argument_list|(
literal|"term"
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|Object
name|scriptValue
init|=
name|script
operator|.
name|execute
argument_list|(
name|docId
argument_list|,
name|scriptParams
argument_list|)
decl_stmt|;
if|if
condition|(
name|scriptValue
operator|==
literal|null
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|scriptValue
operator|instanceof
name|Boolean
condition|)
block|{
if|if
condition|(
operator|!
operator|(
operator|(
name|Boolean
operator|)
name|scriptValue
operator|)
condition|)
block|{
return|return;
block|}
block|}
else|else
block|{
name|value
operator|=
name|scriptValue
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
block|}
name|super
operator|.
name|onValue
argument_list|(
name|docId
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|StaticAggregatorValueProc
specifier|public
specifier|static
class|class
name|StaticAggregatorValueProc
implements|implements
name|FieldData
operator|.
name|StringValueInDocProc
block|{
DECL|field|facets
specifier|private
specifier|final
name|TObjectIntHashMap
argument_list|<
name|String
argument_list|>
name|facets
decl_stmt|;
DECL|method|StaticAggregatorValueProc
specifier|public
name|StaticAggregatorValueProc
parameter_list|(
name|TObjectIntHashMap
argument_list|<
name|String
argument_list|>
name|facets
parameter_list|)
block|{
name|this
operator|.
name|facets
operator|=
name|facets
expr_stmt|;
block|}
DECL|method|onValue
annotation|@
name|Override
specifier|public
name|void
name|onValue
parameter_list|(
name|int
name|docId
parameter_list|,
name|String
name|value
parameter_list|)
block|{
name|facets
operator|.
name|adjustOrPutValue
argument_list|(
name|value
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
DECL|method|facets
specifier|public
specifier|final
name|TObjectIntHashMap
argument_list|<
name|String
argument_list|>
name|facets
parameter_list|()
block|{
return|return
name|facets
return|;
block|}
block|}
block|}
end_class

end_unit

