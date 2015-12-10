begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.highlight
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|highlight
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
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
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
name|Set
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|SearchContextHighlight
specifier|public
class|class
name|SearchContextHighlight
block|{
DECL|field|fields
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Field
argument_list|>
name|fields
decl_stmt|;
DECL|field|globalForceSource
specifier|private
name|boolean
name|globalForceSource
init|=
literal|false
decl_stmt|;
DECL|method|SearchContextHighlight
specifier|public
name|SearchContextHighlight
parameter_list|(
name|Collection
argument_list|<
name|Field
argument_list|>
name|fields
parameter_list|)
block|{
assert|assert
name|fields
operator|!=
literal|null
assert|;
name|this
operator|.
name|fields
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|Field
argument_list|>
argument_list|(
name|fields
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Field
name|field
range|:
name|fields
control|)
block|{
name|this
operator|.
name|fields
operator|.
name|put
argument_list|(
name|field
operator|.
name|field
argument_list|,
name|field
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|fields
specifier|public
name|Collection
argument_list|<
name|Field
argument_list|>
name|fields
parameter_list|()
block|{
return|return
name|fields
operator|.
name|values
argument_list|()
return|;
block|}
DECL|method|globalForceSource
specifier|public
name|void
name|globalForceSource
parameter_list|(
name|boolean
name|globalForceSource
parameter_list|)
block|{
name|this
operator|.
name|globalForceSource
operator|=
name|globalForceSource
expr_stmt|;
block|}
DECL|method|globalForceSource
name|boolean
name|globalForceSource
parameter_list|()
block|{
return|return
name|this
operator|.
name|globalForceSource
return|;
block|}
DECL|method|forceSource
specifier|public
name|boolean
name|forceSource
parameter_list|(
name|Field
name|field
parameter_list|)
block|{
if|if
condition|(
name|globalForceSource
condition|)
block|{
return|return
literal|true
return|;
block|}
name|Field
name|_field
init|=
name|fields
operator|.
name|get
argument_list|(
name|field
operator|.
name|field
argument_list|)
decl_stmt|;
return|return
name|_field
operator|==
literal|null
condition|?
literal|false
else|:
name|_field
operator|.
name|fieldOptions
operator|.
name|forceSource
return|;
block|}
DECL|class|Field
specifier|public
specifier|static
class|class
name|Field
block|{
DECL|field|field
specifier|private
specifier|final
name|String
name|field
decl_stmt|;
DECL|field|fieldOptions
specifier|private
specifier|final
name|FieldOptions
name|fieldOptions
decl_stmt|;
DECL|method|Field
name|Field
parameter_list|(
name|String
name|field
parameter_list|,
name|FieldOptions
name|fieldOptions
parameter_list|)
block|{
assert|assert
name|field
operator|!=
literal|null
assert|;
assert|assert
name|fieldOptions
operator|!=
literal|null
assert|;
name|this
operator|.
name|field
operator|=
name|field
expr_stmt|;
name|this
operator|.
name|fieldOptions
operator|=
name|fieldOptions
expr_stmt|;
block|}
DECL|method|field
specifier|public
name|String
name|field
parameter_list|()
block|{
return|return
name|field
return|;
block|}
DECL|method|fieldOptions
specifier|public
name|FieldOptions
name|fieldOptions
parameter_list|()
block|{
return|return
name|fieldOptions
return|;
block|}
block|}
DECL|class|FieldOptions
specifier|public
specifier|static
class|class
name|FieldOptions
block|{
comment|// Field options that default to null or -1 are often set to their real default in HighlighterParseElement#parse
DECL|field|fragmentCharSize
specifier|private
name|int
name|fragmentCharSize
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|numberOfFragments
specifier|private
name|int
name|numberOfFragments
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|fragmentOffset
specifier|private
name|int
name|fragmentOffset
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|encoder
specifier|private
name|String
name|encoder
decl_stmt|;
DECL|field|preTags
specifier|private
name|String
index|[]
name|preTags
decl_stmt|;
DECL|field|postTags
specifier|private
name|String
index|[]
name|postTags
decl_stmt|;
DECL|field|scoreOrdered
specifier|private
name|Boolean
name|scoreOrdered
decl_stmt|;
DECL|field|highlightFilter
specifier|private
name|Boolean
name|highlightFilter
decl_stmt|;
DECL|field|requireFieldMatch
specifier|private
name|Boolean
name|requireFieldMatch
decl_stmt|;
DECL|field|highlighterType
specifier|private
name|String
name|highlighterType
decl_stmt|;
DECL|field|forceSource
specifier|private
name|Boolean
name|forceSource
decl_stmt|;
DECL|field|fragmenter
specifier|private
name|String
name|fragmenter
decl_stmt|;
DECL|field|boundaryMaxScan
specifier|private
name|int
name|boundaryMaxScan
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|boundaryChars
specifier|private
name|Character
index|[]
name|boundaryChars
init|=
literal|null
decl_stmt|;
DECL|field|highlightQuery
specifier|private
name|Query
name|highlightQuery
decl_stmt|;
DECL|field|noMatchSize
specifier|private
name|int
name|noMatchSize
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|matchedFields
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|matchedFields
decl_stmt|;
DECL|field|options
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|options
decl_stmt|;
DECL|field|phraseLimit
specifier|private
name|int
name|phraseLimit
init|=
operator|-
literal|1
decl_stmt|;
DECL|method|fragmentCharSize
specifier|public
name|int
name|fragmentCharSize
parameter_list|()
block|{
return|return
name|fragmentCharSize
return|;
block|}
DECL|method|numberOfFragments
specifier|public
name|int
name|numberOfFragments
parameter_list|()
block|{
return|return
name|numberOfFragments
return|;
block|}
DECL|method|fragmentOffset
specifier|public
name|int
name|fragmentOffset
parameter_list|()
block|{
return|return
name|fragmentOffset
return|;
block|}
DECL|method|encoder
specifier|public
name|String
name|encoder
parameter_list|()
block|{
return|return
name|encoder
return|;
block|}
DECL|method|preTags
specifier|public
name|String
index|[]
name|preTags
parameter_list|()
block|{
return|return
name|preTags
return|;
block|}
DECL|method|postTags
specifier|public
name|String
index|[]
name|postTags
parameter_list|()
block|{
return|return
name|postTags
return|;
block|}
DECL|method|scoreOrdered
specifier|public
name|Boolean
name|scoreOrdered
parameter_list|()
block|{
return|return
name|scoreOrdered
return|;
block|}
DECL|method|highlightFilter
specifier|public
name|Boolean
name|highlightFilter
parameter_list|()
block|{
return|return
name|highlightFilter
return|;
block|}
DECL|method|requireFieldMatch
specifier|public
name|Boolean
name|requireFieldMatch
parameter_list|()
block|{
return|return
name|requireFieldMatch
return|;
block|}
DECL|method|highlighterType
specifier|public
name|String
name|highlighterType
parameter_list|()
block|{
return|return
name|highlighterType
return|;
block|}
DECL|method|fragmenter
specifier|public
name|String
name|fragmenter
parameter_list|()
block|{
return|return
name|fragmenter
return|;
block|}
DECL|method|boundaryMaxScan
specifier|public
name|int
name|boundaryMaxScan
parameter_list|()
block|{
return|return
name|boundaryMaxScan
return|;
block|}
DECL|method|boundaryChars
specifier|public
name|Character
index|[]
name|boundaryChars
parameter_list|()
block|{
return|return
name|boundaryChars
return|;
block|}
DECL|method|highlightQuery
specifier|public
name|Query
name|highlightQuery
parameter_list|()
block|{
return|return
name|highlightQuery
return|;
block|}
DECL|method|noMatchSize
specifier|public
name|int
name|noMatchSize
parameter_list|()
block|{
return|return
name|noMatchSize
return|;
block|}
DECL|method|phraseLimit
specifier|public
name|int
name|phraseLimit
parameter_list|()
block|{
return|return
name|phraseLimit
return|;
block|}
DECL|method|matchedFields
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|matchedFields
parameter_list|()
block|{
return|return
name|matchedFields
return|;
block|}
DECL|method|options
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|options
parameter_list|()
block|{
return|return
name|options
return|;
block|}
DECL|class|Builder
specifier|static
class|class
name|Builder
block|{
DECL|field|fieldOptions
specifier|private
specifier|final
name|FieldOptions
name|fieldOptions
init|=
operator|new
name|FieldOptions
argument_list|()
decl_stmt|;
DECL|method|fragmentCharSize
name|Builder
name|fragmentCharSize
parameter_list|(
name|int
name|fragmentCharSize
parameter_list|)
block|{
name|fieldOptions
operator|.
name|fragmentCharSize
operator|=
name|fragmentCharSize
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|numberOfFragments
name|Builder
name|numberOfFragments
parameter_list|(
name|int
name|numberOfFragments
parameter_list|)
block|{
name|fieldOptions
operator|.
name|numberOfFragments
operator|=
name|numberOfFragments
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|fragmentOffset
name|Builder
name|fragmentOffset
parameter_list|(
name|int
name|fragmentOffset
parameter_list|)
block|{
name|fieldOptions
operator|.
name|fragmentOffset
operator|=
name|fragmentOffset
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|encoder
name|Builder
name|encoder
parameter_list|(
name|String
name|encoder
parameter_list|)
block|{
name|fieldOptions
operator|.
name|encoder
operator|=
name|encoder
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|preTags
name|Builder
name|preTags
parameter_list|(
name|String
index|[]
name|preTags
parameter_list|)
block|{
name|fieldOptions
operator|.
name|preTags
operator|=
name|preTags
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|postTags
name|Builder
name|postTags
parameter_list|(
name|String
index|[]
name|postTags
parameter_list|)
block|{
name|fieldOptions
operator|.
name|postTags
operator|=
name|postTags
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|scoreOrdered
name|Builder
name|scoreOrdered
parameter_list|(
name|boolean
name|scoreOrdered
parameter_list|)
block|{
name|fieldOptions
operator|.
name|scoreOrdered
operator|=
name|scoreOrdered
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|highlightFilter
name|Builder
name|highlightFilter
parameter_list|(
name|boolean
name|highlightFilter
parameter_list|)
block|{
name|fieldOptions
operator|.
name|highlightFilter
operator|=
name|highlightFilter
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|requireFieldMatch
name|Builder
name|requireFieldMatch
parameter_list|(
name|boolean
name|requireFieldMatch
parameter_list|)
block|{
name|fieldOptions
operator|.
name|requireFieldMatch
operator|=
name|requireFieldMatch
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|highlighterType
name|Builder
name|highlighterType
parameter_list|(
name|String
name|type
parameter_list|)
block|{
name|fieldOptions
operator|.
name|highlighterType
operator|=
name|type
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|forceSource
name|Builder
name|forceSource
parameter_list|(
name|boolean
name|forceSource
parameter_list|)
block|{
name|fieldOptions
operator|.
name|forceSource
operator|=
name|forceSource
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|fragmenter
name|Builder
name|fragmenter
parameter_list|(
name|String
name|fragmenter
parameter_list|)
block|{
name|fieldOptions
operator|.
name|fragmenter
operator|=
name|fragmenter
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|boundaryMaxScan
name|Builder
name|boundaryMaxScan
parameter_list|(
name|int
name|boundaryMaxScan
parameter_list|)
block|{
name|fieldOptions
operator|.
name|boundaryMaxScan
operator|=
name|boundaryMaxScan
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|boundaryChars
name|Builder
name|boundaryChars
parameter_list|(
name|Character
index|[]
name|boundaryChars
parameter_list|)
block|{
name|fieldOptions
operator|.
name|boundaryChars
operator|=
name|boundaryChars
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|highlightQuery
name|Builder
name|highlightQuery
parameter_list|(
name|Query
name|highlightQuery
parameter_list|)
block|{
name|fieldOptions
operator|.
name|highlightQuery
operator|=
name|highlightQuery
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|noMatchSize
name|Builder
name|noMatchSize
parameter_list|(
name|int
name|noMatchSize
parameter_list|)
block|{
name|fieldOptions
operator|.
name|noMatchSize
operator|=
name|noMatchSize
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|phraseLimit
name|Builder
name|phraseLimit
parameter_list|(
name|int
name|phraseLimit
parameter_list|)
block|{
name|fieldOptions
operator|.
name|phraseLimit
operator|=
name|phraseLimit
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|matchedFields
name|Builder
name|matchedFields
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|matchedFields
parameter_list|)
block|{
name|fieldOptions
operator|.
name|matchedFields
operator|=
name|matchedFields
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|options
name|Builder
name|options
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|options
parameter_list|)
block|{
name|fieldOptions
operator|.
name|options
operator|=
name|options
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|build
name|FieldOptions
name|build
parameter_list|()
block|{
return|return
name|fieldOptions
return|;
block|}
DECL|method|merge
name|Builder
name|merge
parameter_list|(
name|FieldOptions
name|globalOptions
parameter_list|)
block|{
if|if
condition|(
name|fieldOptions
operator|.
name|preTags
operator|==
literal|null
operator|&&
name|globalOptions
operator|.
name|preTags
operator|!=
literal|null
condition|)
block|{
name|fieldOptions
operator|.
name|preTags
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|globalOptions
operator|.
name|preTags
argument_list|,
name|globalOptions
operator|.
name|preTags
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|fieldOptions
operator|.
name|postTags
operator|==
literal|null
operator|&&
name|globalOptions
operator|.
name|postTags
operator|!=
literal|null
condition|)
block|{
name|fieldOptions
operator|.
name|postTags
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|globalOptions
operator|.
name|postTags
argument_list|,
name|globalOptions
operator|.
name|postTags
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|fieldOptions
operator|.
name|highlightFilter
operator|==
literal|null
condition|)
block|{
name|fieldOptions
operator|.
name|highlightFilter
operator|=
name|globalOptions
operator|.
name|highlightFilter
expr_stmt|;
block|}
if|if
condition|(
name|fieldOptions
operator|.
name|scoreOrdered
operator|==
literal|null
condition|)
block|{
name|fieldOptions
operator|.
name|scoreOrdered
operator|=
name|globalOptions
operator|.
name|scoreOrdered
expr_stmt|;
block|}
if|if
condition|(
name|fieldOptions
operator|.
name|fragmentCharSize
operator|==
operator|-
literal|1
condition|)
block|{
name|fieldOptions
operator|.
name|fragmentCharSize
operator|=
name|globalOptions
operator|.
name|fragmentCharSize
expr_stmt|;
block|}
if|if
condition|(
name|fieldOptions
operator|.
name|numberOfFragments
operator|==
operator|-
literal|1
condition|)
block|{
name|fieldOptions
operator|.
name|numberOfFragments
operator|=
name|globalOptions
operator|.
name|numberOfFragments
expr_stmt|;
block|}
if|if
condition|(
name|fieldOptions
operator|.
name|encoder
operator|==
literal|null
condition|)
block|{
name|fieldOptions
operator|.
name|encoder
operator|=
name|globalOptions
operator|.
name|encoder
expr_stmt|;
block|}
if|if
condition|(
name|fieldOptions
operator|.
name|requireFieldMatch
operator|==
literal|null
condition|)
block|{
name|fieldOptions
operator|.
name|requireFieldMatch
operator|=
name|globalOptions
operator|.
name|requireFieldMatch
expr_stmt|;
block|}
if|if
condition|(
name|fieldOptions
operator|.
name|boundaryMaxScan
operator|==
operator|-
literal|1
condition|)
block|{
name|fieldOptions
operator|.
name|boundaryMaxScan
operator|=
name|globalOptions
operator|.
name|boundaryMaxScan
expr_stmt|;
block|}
if|if
condition|(
name|fieldOptions
operator|.
name|boundaryChars
operator|==
literal|null
operator|&&
name|globalOptions
operator|.
name|boundaryChars
operator|!=
literal|null
condition|)
block|{
name|fieldOptions
operator|.
name|boundaryChars
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|globalOptions
operator|.
name|boundaryChars
argument_list|,
name|globalOptions
operator|.
name|boundaryChars
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|fieldOptions
operator|.
name|highlighterType
operator|==
literal|null
condition|)
block|{
name|fieldOptions
operator|.
name|highlighterType
operator|=
name|globalOptions
operator|.
name|highlighterType
expr_stmt|;
block|}
if|if
condition|(
name|fieldOptions
operator|.
name|fragmenter
operator|==
literal|null
condition|)
block|{
name|fieldOptions
operator|.
name|fragmenter
operator|=
name|globalOptions
operator|.
name|fragmenter
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|fieldOptions
operator|.
name|options
operator|==
literal|null
operator|||
name|fieldOptions
operator|.
name|options
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|)
operator|&&
name|globalOptions
operator|.
name|options
operator|!=
literal|null
condition|)
block|{
name|fieldOptions
operator|.
name|options
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|globalOptions
operator|.
name|options
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|fieldOptions
operator|.
name|highlightQuery
operator|==
literal|null
operator|&&
name|globalOptions
operator|.
name|highlightQuery
operator|!=
literal|null
condition|)
block|{
name|fieldOptions
operator|.
name|highlightQuery
operator|=
name|globalOptions
operator|.
name|highlightQuery
expr_stmt|;
block|}
if|if
condition|(
name|fieldOptions
operator|.
name|noMatchSize
operator|==
operator|-
literal|1
condition|)
block|{
name|fieldOptions
operator|.
name|noMatchSize
operator|=
name|globalOptions
operator|.
name|noMatchSize
expr_stmt|;
block|}
if|if
condition|(
name|fieldOptions
operator|.
name|forceSource
operator|==
literal|null
condition|)
block|{
name|fieldOptions
operator|.
name|forceSource
operator|=
name|globalOptions
operator|.
name|forceSource
expr_stmt|;
block|}
if|if
condition|(
name|fieldOptions
operator|.
name|phraseLimit
operator|==
operator|-
literal|1
condition|)
block|{
name|fieldOptions
operator|.
name|phraseLimit
operator|=
name|globalOptions
operator|.
name|phraseLimit
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
block|}
block|}
block|}
end_class

end_unit

