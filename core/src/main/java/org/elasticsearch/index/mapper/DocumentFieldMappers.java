begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
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
name|elasticsearch
operator|.
name|common
operator|.
name|collect
operator|.
name|CopyOnWriteHashMap
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
name|index
operator|.
name|analysis
operator|.
name|AnalysisService
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
name|analysis
operator|.
name|FieldNameAnalyzer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|AbstractMap
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
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
DECL|class|DocumentFieldMappers
specifier|public
specifier|final
class|class
name|DocumentFieldMappers
implements|implements
name|Iterable
argument_list|<
name|FieldMapper
argument_list|>
block|{
comment|/** Full field name to mapper */
DECL|field|fieldMappers
specifier|private
specifier|final
name|CopyOnWriteHashMap
argument_list|<
name|String
argument_list|,
name|FieldMapper
argument_list|>
name|fieldMappers
decl_stmt|;
DECL|field|indexAnalyzer
specifier|private
specifier|final
name|FieldNameAnalyzer
name|indexAnalyzer
decl_stmt|;
DECL|field|searchAnalyzer
specifier|private
specifier|final
name|FieldNameAnalyzer
name|searchAnalyzer
decl_stmt|;
DECL|field|searchQuoteAnalyzer
specifier|private
specifier|final
name|FieldNameAnalyzer
name|searchQuoteAnalyzer
decl_stmt|;
DECL|method|DocumentFieldMappers
specifier|public
name|DocumentFieldMappers
parameter_list|(
name|AnalysisService
name|analysisService
parameter_list|)
block|{
name|this
argument_list|(
operator|new
name|CopyOnWriteHashMap
argument_list|<
name|String
argument_list|,
name|FieldMapper
argument_list|>
argument_list|()
argument_list|,
operator|new
name|FieldNameAnalyzer
argument_list|(
name|analysisService
operator|.
name|defaultIndexAnalyzer
argument_list|()
argument_list|)
argument_list|,
operator|new
name|FieldNameAnalyzer
argument_list|(
name|analysisService
operator|.
name|defaultSearchAnalyzer
argument_list|()
argument_list|)
argument_list|,
operator|new
name|FieldNameAnalyzer
argument_list|(
name|analysisService
operator|.
name|defaultSearchQuoteAnalyzer
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|DocumentFieldMappers
specifier|private
name|DocumentFieldMappers
parameter_list|(
name|CopyOnWriteHashMap
argument_list|<
name|String
argument_list|,
name|FieldMapper
argument_list|>
name|fieldMappers
parameter_list|,
name|FieldNameAnalyzer
name|indexAnalyzer
parameter_list|,
name|FieldNameAnalyzer
name|searchAnalyzer
parameter_list|,
name|FieldNameAnalyzer
name|searchQuoteAnalyzer
parameter_list|)
block|{
name|this
operator|.
name|fieldMappers
operator|=
name|fieldMappers
expr_stmt|;
name|this
operator|.
name|indexAnalyzer
operator|=
name|indexAnalyzer
expr_stmt|;
name|this
operator|.
name|searchAnalyzer
operator|=
name|searchAnalyzer
expr_stmt|;
name|this
operator|.
name|searchQuoteAnalyzer
operator|=
name|searchQuoteAnalyzer
expr_stmt|;
block|}
DECL|method|copyAndAllAll
specifier|public
name|DocumentFieldMappers
name|copyAndAllAll
parameter_list|(
name|Collection
argument_list|<
name|FieldMapper
argument_list|>
name|newMappers
parameter_list|)
block|{
name|CopyOnWriteHashMap
argument_list|<
name|String
argument_list|,
name|FieldMapper
argument_list|>
name|map
init|=
name|this
operator|.
name|fieldMappers
decl_stmt|;
for|for
control|(
name|FieldMapper
name|fieldMapper
range|:
name|newMappers
control|)
block|{
name|map
operator|=
name|map
operator|.
name|copyAndPut
argument_list|(
name|fieldMapper
operator|.
name|fieldType
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|fullName
argument_list|()
argument_list|,
name|fieldMapper
argument_list|)
expr_stmt|;
block|}
name|FieldNameAnalyzer
name|indexAnalyzer
init|=
name|this
operator|.
name|indexAnalyzer
operator|.
name|copyAndAddAll
argument_list|(
name|newMappers
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
parameter_list|(
name|input
parameter_list|)
lambda|->
operator|new
name|AbstractMap
operator|.
name|SimpleImmutableEntry
argument_list|<>
argument_list|(
name|input
operator|.
name|fieldType
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|,
operator|(
name|Analyzer
operator|)
name|input
operator|.
name|fieldType
argument_list|()
operator|.
name|indexAnalyzer
argument_list|()
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|FieldNameAnalyzer
name|searchAnalyzer
init|=
name|this
operator|.
name|searchAnalyzer
operator|.
name|copyAndAddAll
argument_list|(
name|newMappers
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
parameter_list|(
name|input
parameter_list|)
lambda|->
operator|new
name|AbstractMap
operator|.
name|SimpleImmutableEntry
argument_list|<>
argument_list|(
name|input
operator|.
name|fieldType
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|,
operator|(
name|Analyzer
operator|)
name|input
operator|.
name|fieldType
argument_list|()
operator|.
name|searchAnalyzer
argument_list|()
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|FieldNameAnalyzer
name|searchQuoteAnalyzer
init|=
name|this
operator|.
name|searchQuoteAnalyzer
operator|.
name|copyAndAddAll
argument_list|(
name|newMappers
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
parameter_list|(
name|input
parameter_list|)
lambda|->
operator|new
name|AbstractMap
operator|.
name|SimpleImmutableEntry
argument_list|<>
argument_list|(
name|input
operator|.
name|fieldType
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|,
operator|(
name|Analyzer
operator|)
name|input
operator|.
name|fieldType
argument_list|()
operator|.
name|searchQuoteAnalyzer
argument_list|()
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
return|return
operator|new
name|DocumentFieldMappers
argument_list|(
name|map
argument_list|,
name|indexAnalyzer
argument_list|,
name|searchAnalyzer
argument_list|,
name|searchQuoteAnalyzer
argument_list|)
return|;
block|}
comment|/** Returns the mapper for the given field */
DECL|method|getMapper
specifier|public
name|FieldMapper
name|getMapper
parameter_list|(
name|String
name|field
parameter_list|)
block|{
return|return
name|fieldMappers
operator|.
name|get
argument_list|(
name|field
argument_list|)
return|;
block|}
DECL|method|simpleMatchToFullName
specifier|public
name|Collection
argument_list|<
name|String
argument_list|>
name|simpleMatchToFullName
parameter_list|(
name|String
name|pattern
parameter_list|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|fields
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|FieldMapper
name|fieldMapper
range|:
name|this
control|)
block|{
if|if
condition|(
name|Regex
operator|.
name|simpleMatch
argument_list|(
name|pattern
argument_list|,
name|fieldMapper
operator|.
name|fieldType
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|fullName
argument_list|()
argument_list|)
condition|)
block|{
name|fields
operator|.
name|add
argument_list|(
name|fieldMapper
operator|.
name|fieldType
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|fullName
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|Regex
operator|.
name|simpleMatch
argument_list|(
name|pattern
argument_list|,
name|fieldMapper
operator|.
name|fieldType
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|)
condition|)
block|{
name|fields
operator|.
name|add
argument_list|(
name|fieldMapper
operator|.
name|fieldType
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|fullName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|fields
return|;
block|}
DECL|method|smartNameFieldMapper
specifier|public
name|FieldMapper
name|smartNameFieldMapper
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|FieldMapper
name|fieldMapper
init|=
name|getMapper
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldMapper
operator|!=
literal|null
condition|)
block|{
return|return
name|fieldMapper
return|;
block|}
for|for
control|(
name|FieldMapper
name|otherFieldMapper
range|:
name|this
control|)
block|{
if|if
condition|(
name|otherFieldMapper
operator|.
name|fieldType
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
operator|.
name|equals
argument_list|(
name|name
argument_list|)
condition|)
block|{
return|return
name|otherFieldMapper
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/**      * A smart analyzer used for indexing that takes into account specific analyzers configured      * per {@link FieldMapper}.      */
DECL|method|indexAnalyzer
specifier|public
name|Analyzer
name|indexAnalyzer
parameter_list|()
block|{
return|return
name|this
operator|.
name|indexAnalyzer
return|;
block|}
comment|/**      * A smart analyzer used for indexing that takes into account specific analyzers configured      * per {@link FieldMapper} with a custom default analyzer for no explicit field analyzer.      */
DECL|method|indexAnalyzer
specifier|public
name|Analyzer
name|indexAnalyzer
parameter_list|(
name|Analyzer
name|defaultAnalyzer
parameter_list|)
block|{
return|return
operator|new
name|FieldNameAnalyzer
argument_list|(
name|indexAnalyzer
operator|.
name|analyzers
argument_list|()
argument_list|,
name|defaultAnalyzer
argument_list|)
return|;
block|}
comment|/**      * A smart analyzer used for searching that takes into account specific analyzers configured      * per {@link FieldMapper}.      */
DECL|method|searchAnalyzer
specifier|public
name|Analyzer
name|searchAnalyzer
parameter_list|()
block|{
return|return
name|this
operator|.
name|searchAnalyzer
return|;
block|}
DECL|method|searchQuoteAnalyzer
specifier|public
name|Analyzer
name|searchQuoteAnalyzer
parameter_list|()
block|{
return|return
name|this
operator|.
name|searchQuoteAnalyzer
return|;
block|}
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|FieldMapper
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|fieldMappers
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
return|;
block|}
block|}
end_class

end_unit

