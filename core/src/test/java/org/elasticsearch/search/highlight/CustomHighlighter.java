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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|text
operator|.
name|StringText
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
name|text
operator|.
name|Text
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
name|FieldMapper
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
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

begin_comment
comment|/**  * total dumb highlighter used to test the pluggable highlighting functionality  */
end_comment

begin_class
DECL|class|CustomHighlighter
specifier|public
class|class
name|CustomHighlighter
implements|implements
name|Highlighter
block|{
annotation|@
name|Override
DECL|method|highlight
specifier|public
name|HighlightField
name|highlight
parameter_list|(
name|HighlighterContext
name|highlighterContext
parameter_list|)
block|{
name|SearchContextHighlight
operator|.
name|Field
name|field
init|=
name|highlighterContext
operator|.
name|field
decl_stmt|;
name|CacheEntry
name|cacheEntry
init|=
operator|(
name|CacheEntry
operator|)
name|highlighterContext
operator|.
name|hitContext
operator|.
name|cache
argument_list|()
operator|.
name|get
argument_list|(
literal|"test-custom"
argument_list|)
decl_stmt|;
specifier|final
name|int
name|docId
init|=
name|highlighterContext
operator|.
name|hitContext
operator|.
name|readerContext
argument_list|()
operator|.
name|docBase
operator|+
name|highlighterContext
operator|.
name|hitContext
operator|.
name|docId
argument_list|()
decl_stmt|;
if|if
condition|(
name|cacheEntry
operator|==
literal|null
condition|)
block|{
name|cacheEntry
operator|=
operator|new
name|CacheEntry
argument_list|()
expr_stmt|;
name|highlighterContext
operator|.
name|hitContext
operator|.
name|cache
argument_list|()
operator|.
name|put
argument_list|(
literal|"test-custom"
argument_list|,
name|cacheEntry
argument_list|)
expr_stmt|;
name|cacheEntry
operator|.
name|docId
operator|=
name|docId
expr_stmt|;
name|cacheEntry
operator|.
name|position
operator|=
literal|1
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|cacheEntry
operator|.
name|docId
operator|==
name|docId
condition|)
block|{
name|cacheEntry
operator|.
name|position
operator|++
expr_stmt|;
block|}
else|else
block|{
name|cacheEntry
operator|.
name|docId
operator|=
name|docId
expr_stmt|;
name|cacheEntry
operator|.
name|position
operator|=
literal|1
expr_stmt|;
block|}
block|}
name|List
argument_list|<
name|Text
argument_list|>
name|responses
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|responses
operator|.
name|add
argument_list|(
operator|new
name|StringText
argument_list|(
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ENGLISH
argument_list|,
literal|"standard response for %s at position %s"
argument_list|,
name|field
operator|.
name|field
argument_list|()
argument_list|,
name|cacheEntry
operator|.
name|position
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|field
operator|.
name|fieldOptions
argument_list|()
operator|.
name|options
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|entry
range|:
name|field
operator|.
name|fieldOptions
argument_list|()
operator|.
name|options
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|responses
operator|.
name|add
argument_list|(
operator|new
name|StringText
argument_list|(
literal|"field:"
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|+
literal|":"
operator|+
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|new
name|HighlightField
argument_list|(
name|highlighterContext
operator|.
name|fieldName
argument_list|,
name|responses
operator|.
name|toArray
argument_list|(
operator|new
name|Text
index|[]
block|{}
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|canHighlight
specifier|public
name|boolean
name|canHighlight
parameter_list|(
name|FieldMapper
name|fieldMapper
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
DECL|class|CacheEntry
specifier|private
specifier|static
class|class
name|CacheEntry
block|{
DECL|field|position
specifier|private
name|int
name|position
decl_stmt|;
DECL|field|docId
specifier|private
name|int
name|docId
decl_stmt|;
block|}
block|}
end_class

end_unit

