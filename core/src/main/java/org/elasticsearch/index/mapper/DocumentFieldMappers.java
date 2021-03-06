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
name|FieldNameAnalyzer
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
name|Collections
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
name|Map
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
DECL|method|put
specifier|private
specifier|static
name|void
name|put
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Analyzer
argument_list|>
name|analyzers
parameter_list|,
name|String
name|key
parameter_list|,
name|Analyzer
name|value
parameter_list|,
name|Analyzer
name|defaultValue
parameter_list|)
block|{
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|value
operator|=
name|defaultValue
expr_stmt|;
block|}
name|analyzers
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
DECL|method|DocumentFieldMappers
specifier|public
name|DocumentFieldMappers
parameter_list|(
name|Collection
argument_list|<
name|FieldMapper
argument_list|>
name|mappers
parameter_list|,
name|Analyzer
name|defaultIndex
parameter_list|,
name|Analyzer
name|defaultSearch
parameter_list|,
name|Analyzer
name|defaultSearchQuote
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|FieldMapper
argument_list|>
name|fieldMappers
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Analyzer
argument_list|>
name|indexAnalyzers
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Analyzer
argument_list|>
name|searchAnalyzers
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Analyzer
argument_list|>
name|searchQuoteAnalyzers
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|FieldMapper
name|mapper
range|:
name|mappers
control|)
block|{
name|fieldMappers
operator|.
name|put
argument_list|(
name|mapper
operator|.
name|name
argument_list|()
argument_list|,
name|mapper
argument_list|)
expr_stmt|;
name|MappedFieldType
name|fieldType
init|=
name|mapper
operator|.
name|fieldType
argument_list|()
decl_stmt|;
name|put
argument_list|(
name|indexAnalyzers
argument_list|,
name|fieldType
operator|.
name|name
argument_list|()
argument_list|,
name|fieldType
operator|.
name|indexAnalyzer
argument_list|()
argument_list|,
name|defaultIndex
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|searchAnalyzers
argument_list|,
name|fieldType
operator|.
name|name
argument_list|()
argument_list|,
name|fieldType
operator|.
name|searchAnalyzer
argument_list|()
argument_list|,
name|defaultSearch
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|searchQuoteAnalyzers
argument_list|,
name|fieldType
operator|.
name|name
argument_list|()
argument_list|,
name|fieldType
operator|.
name|searchQuoteAnalyzer
argument_list|()
argument_list|,
name|defaultSearchQuote
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|fieldMappers
operator|=
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|fieldMappers
argument_list|)
expr_stmt|;
name|this
operator|.
name|indexAnalyzer
operator|=
operator|new
name|FieldNameAnalyzer
argument_list|(
name|indexAnalyzers
argument_list|)
expr_stmt|;
name|this
operator|.
name|searchAnalyzer
operator|=
operator|new
name|FieldNameAnalyzer
argument_list|(
name|searchAnalyzers
argument_list|)
expr_stmt|;
name|this
operator|.
name|searchQuoteAnalyzer
operator|=
operator|new
name|FieldNameAnalyzer
argument_list|(
name|searchQuoteAnalyzers
argument_list|)
expr_stmt|;
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
name|name
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
name|name
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
name|name
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

