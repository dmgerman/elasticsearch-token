begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.facets.terms
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facets
operator|.
name|terms
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
name|regex
operator|.
name|Regex
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
name|XContentParser
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
name|facets
operator|.
name|collector
operator|.
name|FacetCollector
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
name|facets
operator|.
name|collector
operator|.
name|FacetCollectorParser
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
name|Pattern
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|TermsFacetCollectorParser
specifier|public
class|class
name|TermsFacetCollectorParser
implements|implements
name|FacetCollectorParser
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"terms"
decl_stmt|;
DECL|method|names
annotation|@
name|Override
specifier|public
name|String
index|[]
name|names
parameter_list|()
block|{
return|return
operator|new
name|String
index|[]
block|{
name|NAME
block|}
return|;
block|}
DECL|method|parse
annotation|@
name|Override
specifier|public
name|FacetCollector
name|parse
parameter_list|(
name|String
name|facetName
parameter_list|,
name|XContentParser
name|parser
parameter_list|,
name|SearchContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|field
init|=
literal|null
decl_stmt|;
name|int
name|size
init|=
literal|10
decl_stmt|;
name|String
name|fieldName
init|=
literal|null
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
name|ImmutableSet
argument_list|<
name|String
argument_list|>
name|excluded
init|=
name|ImmutableSet
operator|.
name|of
argument_list|()
decl_stmt|;
name|String
name|regex
init|=
literal|null
decl_stmt|;
name|String
name|regexFlags
init|=
literal|null
decl_stmt|;
name|TermsFacet
operator|.
name|ComparatorType
name|comparatorType
init|=
name|TermsFacet
operator|.
name|ComparatorType
operator|.
name|COUNT
decl_stmt|;
name|String
name|scriptLang
init|=
literal|null
decl_stmt|;
name|String
name|script
init|=
literal|null
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
init|=
literal|null
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|fieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
if|if
condition|(
literal|"params"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|params
operator|=
name|parser
operator|.
name|map
argument_list|()
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_ARRAY
condition|)
block|{
if|if
condition|(
literal|"exclude"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|ImmutableSet
operator|.
name|Builder
argument_list|<
name|String
argument_list|>
name|builder
init|=
name|ImmutableSet
operator|.
name|builder
argument_list|()
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_ARRAY
condition|)
block|{
name|builder
operator|.
name|add
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|excluded
operator|=
name|builder
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|.
name|isValue
argument_list|()
condition|)
block|{
if|if
condition|(
literal|"field"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|field
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"size"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|size
operator|=
name|parser
operator|.
name|intValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"regex"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|regex
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"regex_flags"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
operator|||
literal|"regexFlags"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|regexFlags
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"order"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
operator|||
literal|"comparator"
operator|.
name|equals
argument_list|(
name|field
argument_list|)
condition|)
block|{
name|comparatorType
operator|=
name|TermsFacet
operator|.
name|ComparatorType
operator|.
name|fromString
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"script"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|script
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"lang"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|scriptLang
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
literal|"_index"
operator|.
name|equals
argument_list|(
name|field
argument_list|)
condition|)
block|{
return|return
operator|new
name|IndexNameFacetCollector
argument_list|(
name|facetName
argument_list|,
name|context
operator|.
name|shardTarget
argument_list|()
operator|.
name|index
argument_list|()
argument_list|,
name|comparatorType
argument_list|,
name|size
argument_list|)
return|;
block|}
name|Pattern
name|pattern
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|regex
operator|!=
literal|null
condition|)
block|{
name|pattern
operator|=
name|Regex
operator|.
name|compile
argument_list|(
name|regex
argument_list|,
name|regexFlags
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|TermsFacetCollector
argument_list|(
name|facetName
argument_list|,
name|field
argument_list|,
name|size
argument_list|,
name|comparatorType
argument_list|,
name|context
argument_list|,
name|excluded
argument_list|,
name|pattern
argument_list|,
name|scriptLang
argument_list|,
name|script
argument_list|,
name|params
argument_list|)
return|;
block|}
block|}
end_class

end_unit

